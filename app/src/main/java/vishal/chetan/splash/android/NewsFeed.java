package vishal.chetan.splash.android;

import android.app.SearchManager;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
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
    private int previousItemId;
    private int serverIndex = -1;
    private final static int login_result_id = 1;
    private final static int create_thread_result_id = 2;
    private final static int update_user_result_id = 3;

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
                    startActivityForResult(new Intent(NewsFeed.this, PostActivity.class).putExtra("serverIndex", previousItemId), create_thread_result_id);
                }
            }
        });

        setSupportActionBar(toolbar);
        threadsListView.setLayoutManager(new LinearLayoutManager(this));

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
        GlobalFunctions.servers.addListener(new ServerList.OnServerDisabledListener() {
            @Override
            public void onDisabled() {
                updateOptionsMenu();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, newsDrawer, toolbar, R.string.strFilterSource, R.string.strCloseSources);
        newsDrawer.addDrawerListener(toggle);
        toggle.syncState();
        newsDrawer.closeDrawers();

        updateOptionsMenu();

        nav_view.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousItemId < 0) {
                    return;
                } else if (GlobalFunctions.identities.get(previousItemId) == null) {
                    startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", previousItemId), login_result_id);
                } else {
                    startActivity(new Intent(NewsFeed.this, ProfileActivity.class).putExtra("serverIndex", previousItemId));
                }
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshThreads);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (previousItemId == -1) {
                    for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                        if (i == GlobalFunctions.servers.size() - 1) {
                            new FetchThreads(i, 50).execute();
                        } else {
                            new FetchThreads(i, 50, false).execute();
                        }
                    }
                } else {
                    new FetchThreads(previousItemId, 50).execute();
                }
            }
        });
    }

    private void updateOptionsMenu() {
        previousItemId = -3;
        Menu nav_menu = nav_view.getMenu();
        nav_menu.setGroupCheckable(Menu.NONE, true, true);
        nav_menu.clear();
        SubMenu menu = nav_menu.addSubMenu(R.string.strSource);
        menu.add(Menu.NONE, -1, Menu.NONE, "All").setCheckable(true);
        onNavigationItemSelected(menu.findItem(-1));
        for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
            if (GlobalFunctions.servers.get(i).isEnabled()) {
                menu.add(Menu.NONE, i, Menu.NONE, GlobalFunctions.servers.get(i).getName()).setCheckable(true);
            }
        }
        nav_menu.add(Menu.NONE, -2, Menu.NONE, "Search").setCheckable(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_feed, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (serverIndex != -1) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                new AsyncArrayHelper(serverIndex, "search/" + query) {
                    @Override
                    protected void onPostExecute(JSONArray jsonArray) {
                        if (jsonArray == null) {
                            Snackbar.make(nav_view, R.string.errUnknown, Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                        final ArrayList<Thread> threads = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            try {
                            JSONObject thread = jsonArray.getJSONObject(i);
                                threads.add(new Thread(thread.getLong("threadId"), serverIndex, thread.getString("title"), thread.getString("content"), thread.getLong("author"), new Date(thread.getString("ctime")), new Date(thread.getString("mtime")), thread.getInt("topicid")));
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
                Snackbar.make(nav_view, "Please, select a server to search in...", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                GlobalFunctions.launchSettings(NewsFeed.this);
                break;
            case R.id.action_about:
                startActivity(new Intent(NewsFeed.this, AboutActivity.class));
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int ItemId = item.getItemId();
        if (ItemId != previousItemId) {
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            if (ItemId < 0) {
                actionBar.setTitle(getString(R.string.title_activity_news_feed));
            } else {
                actionBar.setTitle(GlobalFunctions.servers.get(ItemId).getName());
            }
            if (ItemId < -1) {
                threadsListView.setAdapter(null);
            } else {
                serverIndex = ItemId;
                threadsListView.swapAdapter(new ThreadsAdapter(this, ItemId), false);
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
        UserIdentity identity;
        if (itemId < 0) {
            identity = GlobalFunctions.defaultIdentity;
            fab.setVisibility(View.GONE);
        } else {
            identity = GlobalFunctions.identities.get(itemId);
            if (identity != null) {
                fab.setVisibility(View.VISIBLE);
            } else {
                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)) {
                    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autolog", false)) {
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
                return;
            }
        }
        View header = nav_view.getHeaderView(0);
        ImageView profpic = (ImageView) header.findViewById(R.id.headerPic);
        if (identity.getProfpic() != null) {
            profpic.setImageBitmap(circleCrop(identity.getProfpic()));
        } else {
            profpic.setImageResource(R.mipmap.ic_news);
        }
        ((TextView) header.findViewById(R.id.headerUser)).setText(identity.getUsername());
        ((TextView) header.findViewById(R.id.headerEmail)).setText(identity.getEmail());
        if (itemId >= 0) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(NewsFeed.this, ProfileActivity.class).putExtra("serverIndex", itemId), update_user_result_id);
                }
            });
        } else {
            header.setOnClickListener(null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case login_result_id:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == previousItemId) {
                    updateNavHeader(previousItemId);
                }
                break;
            case create_thread_result_id:
                if (resultCode == RESULT_OK && (previousItemId == -1 || data.getIntExtra("serverIndex", -3) == previousItemId)) {
                    threadsListView.swapAdapter(new ThreadsAdapter(this, previousItemId), false);
                    startActivity(new Intent(NewsFeed.this, ViewThreadActivity.class).putExtras(data));
                }
                break;
            case update_user_result_id:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == previousItemId) {
                    updateNavHeader(previousItemId);
                }
                break;
        }
    }

    private static Bitmap circleCrop(Bitmap source) {
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

    private class FetchThreads extends AsyncTask<Void, Void, Void> {
        int serverIndex;
        String path;
        boolean refreshStatus = true;

        FetchThreads(int serverIndex, int numberOfThreads) {
            this.serverIndex = serverIndex;
            path = "threads/" + numberOfThreads;
        }

        FetchThreads(int serverIndex, int numberOfThreads, boolean resetRefreshStatus) {
            this.serverIndex = serverIndex;
            path = "threads/" + numberOfThreads;
            this.refreshStatus = resetRefreshStatus;
        }

        @Override
        protected Void doInBackground(Void... params) {
            ServerList.SplashSource source = GlobalFunctions.servers.get(serverIndex);
            NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                URL urlServer;
                HttpURLConnection urlConn;
                try {
                    urlServer = new URL(source.getUrl() + path);
                    urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                    urlConn.connect();
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
                            JSONObject thread = data.getJSONObject(i);
                            SplashCache.ThreadCache.add(new Thread(thread.getLong("threadId"), serverIndex, thread.getString("title"), thread.getString("content"), thread.getLong("author"), new Date(thread.getString("ctime")), new Date(thread.getString("mtime")), thread.getInt("topicid")));
                        }
                        if (previousItemId == -1 || previousItemId == serverIndex) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    threadsListView.getAdapter().notifyDataSetChanged();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (refreshStatus) {
                        refreshLayout.setRefreshing(false);
                    }
                }
            });
            return null;
        }
    }
}
