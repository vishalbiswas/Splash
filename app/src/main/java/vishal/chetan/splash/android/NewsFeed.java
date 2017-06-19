package vishal.chetan.splash.android;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.NotificationsAdapter;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.ThreadSearchSuggestionProvider;
import vishal.chetan.splash.ThreadsAdapter;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncArrayHelper;
import vishal.chetan.splash.asyncs.AsyncHelper;

public class NewsFeed extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView threadsListView;
    private DrawerLayout newsDrawer;
    private NavigationView nav_view;
    private FloatingActionButton fab;
    private SwipeRefreshLayout refreshLayout;
    private SubMenu subMenu;

    private MenuItem menu_logout;
    private MenuItem menu_search;

    private int previousItemId;
    private int serverIndex = -1;

    private FetchThreads fetcher = null;

    private final static int login_result_id = 1;
    private final static int create_thread_result_id = 2;
    private final static int update_user_result_id = 3;
    public final static int NUMBER_OF_THREADS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNews);
        threadsListView = (RecyclerView) findViewById(R.id.threadsListView);
        newsDrawer = (DrawerLayout) findViewById(R.id.newsDrawer);
        nav_view = (NavigationView) findViewById(R.id.nav_view);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousItemId >= 0) {
                    startActivityForResult(new Intent(NewsFeed.this, PostActivity.class).putExtra("serverIndex", serverIndex), create_thread_result_id);
                }
            }
        });

        setSupportActionBar(toolbar);
        threadsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        threadsListView.setLayoutManager(new LinearLayoutManager(this));
        threadsListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                if (!(previousItemId == -2 || previousItemId == -4) && scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        fetchNext(((ThreadsAdapter) recyclerView.getAdapter()).getThread(totalItemCount - 1));
                    }
                }
            }
        });

        nav_view.setNavigationItemSelectedListener(this);
        GlobalFunctions.servers.addListener(new ServerList.OnServerListChangeListener() {
            @Override
            public void onAdd(ServerList.SplashSource source) {
                updateOptionsMenu();
            }

            @Override
            public void onRemove(ServerList.SplashSource source) {
                updateOptionsMenu();
            }

            @Override
            public void onUpdate(ServerList.SplashSource previousSource, ServerList.SplashSource updatedSource) {
                updateOptionsMenu();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, newsDrawer, toolbar, R.string.strFilterSource, R.string.strCloseSources);
        newsDrawer.addDrawerListener(toggle);
        toggle.syncState();
        newsDrawer.closeDrawers();

        updateOptionsMenu();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshThreads);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (previousItemId == -4) {
                    sendBroadcast(new Intent(NewsFeed.this, NotificationReceiver.class).putExtra("serverIndex", serverIndex));
                } else if (previousItemId == -3) {
                    for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                        if (GlobalFunctions.servers.get(i).isEnabled()) {
                            new FetchThreads(NewsFeed.this, NUMBER_OF_THREADS, true).execute(i);
                        }
                    }
                } else {
                    fillThreadCache();
                }
                refreshLayout.setRefreshing(false);
            }
        });


        GlobalFunctions.servers.addListener(new ServerList.OnServerEnabledListener() {
            @Override
            public void onEnabledChanged(int serverIndex, boolean enabled) {
                fillThreadCache();
                updateOptionsMenu();
            }
        });

        fillThreadCache();
    }

    private void fillThreadCache() {
        if (previousItemId == -1) {
            for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                if (GlobalFunctions.servers.get(i).isEnabled()) {
                    new FetchThreads(this, NUMBER_OF_THREADS, false).execute(i);
                }
            }
        } else if (previousItemId == -3) {
            for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                if (GlobalFunctions.servers.get(i).isEnabled()) {
                    fetcher = new FetchThreads(this, NUMBER_OF_THREADS, true);
                    fetcher.execute(i);
                }
            }
        } else if (previousItemId == -4) {
            sendBroadcast(new Intent(NewsFeed.this, NotificationReceiver.class).putExtra("serverIndex", serverIndex));
        } else if (previousItemId >= 0) {
            new FetchThreads(this, NUMBER_OF_THREADS, false).execute(serverIndex);
        }
    }

    public void fetchNext(Thread thread) {
        if (fetcher == null || fetcher.getStatus() == AsyncTask.Status.FINISHED) {
            if (previousItemId == -1) {
                for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                    if (GlobalFunctions.servers.get(i).isEnabled()) {
                        fetcher = new FetchThreads(this, NUMBER_OF_THREADS, false, thread.getMtime().getTime());
                        fetcher.execute(i);
                    }
                }
            } else if (previousItemId == -3) {
                for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                    if (GlobalFunctions.servers.get(i).isEnabled()) {
                        fetcher = new FetchThreads(this, NUMBER_OF_THREADS, true, thread.getMtime().getTime());
                        fetcher.execute(i);
                    }
                }
            } else {
                fetcher = new FetchThreads(this, NUMBER_OF_THREADS, false, thread.getMtime().getTime());
                fetcher.execute(serverIndex);
            }
        }
    }

    private void updateOptionsMenu() {
        previousItemId = -5;
        Menu nav_menu = nav_view.getMenu();
        nav_menu.setGroupCheckable(Menu.NONE, true, true);
        nav_menu.clear();
        subMenu = nav_menu.addSubMenu(R.string.strSource);
        subMenu.add(Menu.NONE, -1, Menu.NONE, R.string.strAll).setCheckable(true);
        onNavigationItemSelected(subMenu.findItem(-1));
        boolean canModerate = false;
        boolean notifications = false;
        for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
            subMenu.add(Menu.NONE, i, Menu.NONE, GlobalFunctions.servers.get(i).getName()).setCheckable(true).setEnabled(false);
            if (GlobalFunctions.servers.get(i).isEnabled()) {
                subMenu.findItem(i).setEnabled(true);
                if (GlobalFunctions.servers.get(i).identity != null) {
                    if (!notifications) {
                        notifications = true;
                    }
                    if (!canModerate && GlobalFunctions.servers.get(i).identity.getMod() > 0) {
                        canModerate = true;
                    }
                }
            }
        }
        nav_menu.add(Menu.NONE, -2, Menu.NONE, R.string.strSearch).setCheckable(true);
        if (canModerate) {
            nav_menu.add(Menu.NONE, -3, Menu.NONE, R.string.strModerate).setCheckable(true);
        }
        if (notifications) {
            nav_menu.add(Menu.NONE, -4, Menu.NONE, "Notifications").setCheckable(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_feed, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu_logout = menu.findItem(R.id.logout);
        menu_search = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu_search.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            menu_search.collapseActionView();
            if (serverIndex != -1) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        ThreadSearchSuggestionProvider.AUTHORITY, ThreadSearchSuggestionProvider.MODE);
                suggestions.saveRecentQuery(query, GlobalFunctions.servers.get(serverIndex).getName());
                new AsyncArrayHelper(serverIndex, "search/" + query) {
                    @Override
                    protected void workInBackground(@Nullable JSONArray jsonArray) {
                        if (jsonArray == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(nav_view, R.string.errUnknown, Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onNavigationItemSelected(nav_view.getMenu().findItem(-2));
                            }
                        });
                        final ArrayList<Thread> threads = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            try {
                                threads.add(SplashCache.ThreadCache.createThreadfromJSON(serverIndex, jsonArray.getJSONObject(i)));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                threadsListView.setAdapter(new ThreadsAdapter(NewsFeed.this, threads));
                            }
                        });
                    }
                }.execute();
            } else {
                Snackbar.make(nav_view, R.string.errSelectSearchServer, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                GlobalFunctions.launchSettings(NewsFeed.this);
                break;
            case R.id.action_about:
                startActivity(new Intent(NewsFeed.this, AboutActivity.class));
                break;
            case R.id.refresh:
                fillThreadCache();
                break;
            case R.id.logout:
                if (previousItemId >= 0) {
                    menu_logout.setEnabled(false);
                    new AsyncHelper(previousItemId, "logout", "sessionid=" + GlobalFunctions.servers.get(previousItemId).identity.getSessionid()) {
                        @Override
                        protected void onPostExecute(JSONObject jsonObject) {
                            try {
                                if (jsonObject != null && jsonObject.getInt("status") == 0) {
                                    Log.d(ContentValues.TAG, "Logged out");
                                    GlobalFunctions.servers.get(previousItemId).identity = null;
                                    GlobalFunctions.broadcastToNotifications(getApplicationContext(), serverIndex);
                                    GlobalFunctions.servers.get(previousItemId).session = ServerList.SplashSource.SessionState.DEAD;
                                    getSharedPreferences("sessions", MODE_PRIVATE).edit().remove(GlobalFunctions.servers.get(serverIndex).getName()).apply();
                                    int temp = serverIndex;
                                    updateOptionsMenu();
                                    //nav_view.setCheckedItem(temp);
                                    //subMenu.findItem(-1).setChecked(false);
                                    onNavigationItemSelected(subMenu.findItem(temp));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            menu_logout.setEnabled(true);
                        }
                    }.execute();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int ItemId = item.getItemId();
        if (ItemId != previousItemId) {
            if (previousItemId > -5) {
                nav_view.getMenu().findItem(previousItemId).setChecked(false);
                if (previousItemId >= 0) {
                    subMenu.findItem(previousItemId).setChecked(false);
                }
            }
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            if (ItemId == -1) {
                actionBar.setTitle(getString(R.string.title_activity_news_feed));
            } else {
                actionBar.setTitle(item.getTitle());
            }
            if (ItemId == -3) {
                for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                    if (GlobalFunctions.servers.get(i).isEnabled()) {
                        new FetchThreads(this, NUMBER_OF_THREADS, true).execute(i);
                    }
                }
                threadsListView.setAdapter(new ThreadsAdapter(this, SplashCache.ThreadCache.getModeratable()));
            } else if (ItemId == -4) {
                final NotificationsAdapter adapter = new NotificationsAdapter(this);
                NotificationReceiver.listener = new NotificationReceiver.OnNotificationAdded() {
                    @Override
                    public void onNotificationAdded() {
                        adapter.notifyItemInserted(NotificationReceiver.notifications.size() - 1);
                    }
                };
                threadsListView.setAdapter(adapter);
            } else if (ItemId < -1) {
                threadsListView.setAdapter(null);
            } else {
                serverIndex = ItemId;
                if (previousItemId != -4) {
                    threadsListView.swapAdapter(new ThreadsAdapter(this, ItemId), false);
                } else {
                    threadsListView.setAdapter(new ThreadsAdapter(this, ItemId));
                }
            }
            updateNavHeader(ItemId);
            nav_view.setCheckedItem(ItemId);
            item.setChecked(true);
            previousItemId = ItemId;
        }
        newsDrawer.closeDrawers();
        return true;
    }

    private void updateNavHeader(final int itemId) {
        final UserIdentity identity;
        View header = nav_view.getHeaderView(0);
        final ImageView bg = (ImageView) header.findViewById(R.id.bg);
        if (itemId < 0) {
            if (menu_logout != null) {
                menu_logout.setVisible(false);
            }
            if (itemId == -3) {
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", serverIndex), login_result_id);
                    }
                });
            } else {
                header.setOnClickListener(null);
            }
            identity = GlobalFunctions.defaultIdentity;
            fab.setVisibility(View.GONE);
            bg.setImageResource(R.drawable.ic_nav_header);
        } else {
            if (GlobalFunctions.servers.get(itemId).banner != null) {
                bg.setImageBitmap(GlobalFunctions.servers.get(itemId).banner);
            } else {
                bg.setImageResource(R.drawable.ic_nav_header);
            }
            identity = GlobalFunctions.servers.get(itemId).identity;
            if (identity != null) {
                if (menu_logout != null) {
                    menu_logout.setVisible(true);
                }
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(NewsFeed.this, ProfileActivity.class).putExtra("serverIndex", previousItemId), update_user_result_id);
                    }
                });
                if (identity.canPost()) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            } else {
                if (menu_logout != null) {
                    menu_logout.setVisible(false);
                }
                if (GlobalFunctions.servers.get(itemId).session == ServerList.SplashSource.SessionState.UNKNOWN) {
                    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)
                            && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autolog", false)) {
                        startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", itemId), login_result_id);
                    } else {
                        Snackbar.make(findViewById(R.id.root_coord), getString(R.string.strAskLogin), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.login), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", itemId), login_result_id);
                                    }
                                }).show();
                    }
                }
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", itemId), login_result_id);
                    }
                });
                return;
            }
        }
        final ImageView profpic = (ImageView) header.findViewById(R.id.headerPic);
        assert identity != null;
        if (identity.getProfpic() >= 0) {
            SplashCache.AttachmentCache.get(serverIndex, identity.getProfpic(), new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                @Override
                public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                    if (attachment != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                profpic.setImageBitmap(circleCrop((Bitmap) attachment.data));
                            }
                        });
                    }
                }
            });
        } else {
            profpic.setImageResource(R.mipmap.ic_news);
        }
        ((TextView) header.findViewById(R.id.headerUser)).setText(identity.getUsername());
        ((TextView) header.findViewById(R.id.headerEmail)).setText(identity.getEmail());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        switch (requestCode) {
            case login_result_id:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == previousItemId) {
                    GlobalFunctions.servers.get(data.getIntExtra("serverIndex", -1)).session = ServerList.SplashSource.SessionState.ALIVE;
                    int temp = serverIndex;
                    updateOptionsMenu();
                    //nav_view.setCheckedItem(temp);
                    //subMenu.findItem(-1).setChecked(false);
                    onNavigationItemSelected(subMenu.findItem(temp));
                }
                break;
            case create_thread_result_id:
                if (resultCode == RESULT_OK && (previousItemId == -1 || data.getIntExtra("serverIndex", -3) == previousItemId)) {
                    threadsListView.swapAdapter(new ThreadsAdapter(this, previousItemId), false);
                }
                break;
            case update_user_result_id:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == previousItemId) {
                    updateNavHeader(previousItemId);
                }
                break;
        }
    }

    @Nullable
    private static Bitmap circleCrop(@Nullable Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);
        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    public static class FetchThreads extends AsyncTask<Integer, Void, Void> {
        int serverIndex;
        @NonNull
        final String path;
        final boolean forModeration;
        String postMessage = null;
        NewsFeed feed;

        public FetchThreads(NewsFeed feed, int numberOfThreads, boolean forModeration) {
            this.feed = feed;
            path = "/threads/" + numberOfThreads;
            this.forModeration = forModeration;
        }

        public FetchThreads(NewsFeed feed, int numberOfThreads, boolean forModeration, long after) {
            this.feed = feed;
            path = "/threads/" + numberOfThreads;
            this.forModeration = forModeration;
            postMessage = "after=" + after;
        }

        @Nullable
        @Override
        protected Void doInBackground(Integer... params) {
            feed.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    feed.refreshLayout.setRefreshing(true);
                }
            });
            serverIndex = params[0];
            ServerList.SplashSource source = GlobalFunctions.servers.get(serverIndex);
            if (forModeration) {
                if (postMessage != null) {
                    postMessage += "&sessionid=" + source.identity.getSessionid();
                } else {
                    postMessage = "sessionid=" + source.identity.getSessionid();
                }
            }
            NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                URL urlServer;
                HttpsURLConnection urlConn;
                try {
                    urlServer = new URL(source.getUrl() + path);
                    urlConn = (HttpsURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                    if (postMessage != null) {
                        urlConn.setRequestMethod("POST");
                    } else {
                        urlConn.setRequestMethod("GET");
                    }
                    urlConn.connect();
                    if (postMessage != null) {
                        urlConn.getOutputStream().write(postMessage.getBytes());
                    }
                    if (urlConn.getResponseCode() != 200) {
                        throw new Exception();
                    } else {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null) {
                            response.append(line);
                        }
                        bufferedReader.close();
                        JSONArray data = new JSONArray(response.toString());
                        for (int i = 0; i < data.length(); ++i) {
                            SplashCache.ThreadCache.add(SplashCache.ThreadCache.createThreadfromJSON(serverIndex, data.getJSONObject(i)));
                        }
                        if (feed.previousItemId == -1 || feed.previousItemId == serverIndex) {
                            feed.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    feed.threadsListView.swapAdapter(new ThreadsAdapter(feed, feed.previousItemId), false);
                                }
                            });
                        } else if (feed.previousItemId == -3 && forModeration) {
                            feed.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    feed.threadsListView.swapAdapter(new ThreadsAdapter(feed, SplashCache.ThreadCache.getModeratable()), false);
                                }
                            });
                        }
                    }
                    urlConn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            feed.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    feed.refreshLayout.setRefreshing(false);
                }
            });
            return null;
        }
    }
}
