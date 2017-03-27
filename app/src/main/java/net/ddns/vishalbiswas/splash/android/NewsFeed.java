package net.ddns.vishalbiswas.splash.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import net.ddns.vishalbiswas.splash.R;
import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;
import net.ddns.vishalbiswas.splash.classes.ThreadsAdapter;

public class NewsFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNews);
        setSupportActionBar(toolbar);

        ((ListView) findViewById(R.id.threadsListView)).setAdapter(new ThreadsAdapter(this));
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

}
