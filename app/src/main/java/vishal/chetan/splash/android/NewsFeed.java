package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;
import vishal.chetan.splash.ThreadsAdapter;
import vishal.chetan.splash.UserIdentity;

public class NewsFeed extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView threadsListView;
    private DrawerLayout newsDrawer;
    private NavigationView nav_view;
    private int previousItemId = -2;
    private final static int login_result_id = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNews);
        threadsListView = (RecyclerView) findViewById(R.id.threadsListView);
        newsDrawer = (DrawerLayout) findViewById(R.id.newsDrawer);
        nav_view = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);
        threadsListView.setLayoutManager(new LinearLayoutManager(this));
        threadsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        threadsListView.setHasFixedSize(true);

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
                if (previousItemId == -1) {
                    return;
                } else if (GlobalFunctions.identities.get(previousItemId) == null) {
                    startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", previousItemId), login_result_id);
                } else {
                    startActivity(new Intent(NewsFeed.this, ProfileActivity.class).putExtra("serverIndex", previousItemId));
                }
            }
        });
    }

    private void updateOptionsMenu() {
        Menu nav_menu = nav_view.getMenu();
        nav_menu.setGroupCheckable(Menu.NONE, true, true);
        nav_menu.clear();
        nav_menu.add(Menu.NONE, -1, Menu.NONE, "All").setCheckable(true);
        onNavigationItemSelected(nav_menu.findItem(-1));
        for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
            if (GlobalFunctions.servers.get(i).isEnabled()) {
                nav_menu.add(Menu.NONE, i, Menu.NONE, GlobalFunctions.servers.get(i).getName()).setCheckable(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(NewsFeed.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int ItemId = item.getItemId();
        if (ItemId != previousItemId) {
            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            if (ItemId == -1) {
                actionBar.setTitle(getString(R.string.title_activity_news_feed));
            } else {
                actionBar.setTitle(GlobalFunctions.servers.get(ItemId).getName());
            }
            updateNavHeader(ItemId);
            threadsListView.setAdapter(new ThreadsAdapter(this, ItemId));
            nav_view.setCheckedItem(ItemId);
            item.setChecked(true);
            previousItemId = ItemId;
        }
        newsDrawer.closeDrawers();
        return true;
    }

    void updateNavHeader(final int itemId) {
        UserIdentity identity;
        if (itemId == -1) {
            identity = GlobalFunctions.defaultIdentity;
        } else {
            identity = GlobalFunctions.identities.get(itemId);
        }
        if (identity == null) { // user has not logged in to the selected server
            Snackbar.make(findViewById(R.id.root_coord), getString(R.string.strAskLogin), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.login), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(new Intent(NewsFeed.this, LoginActivity.class).putExtra("serverIndex", itemId), login_result_id);
                        }
                    }).show();
            return;
        }
        View header = nav_view.getHeaderView(0);
        ((ImageView)header.findViewById(R.id.headerPic)).setImageBitmap(identity.getProfpic());
        ((TextView)header.findViewById(R.id.headerUser)).setText(identity.getUsername());
        ((TextView)header.findViewById(R.id.headerEmail)).setText(identity.getEmail());
        if (itemId != -1) {
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(NewsFeed.this, ProfileActivity.class).putExtra("serverIndex", itemId));
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case login_result_id:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == previousItemId) {
                    updateNavHeader(previousItemId);
                }
        }
    }
}
