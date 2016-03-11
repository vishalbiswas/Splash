package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

class NewsFeed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!GlobalFunctions.isSessionAlive) {
            finish();
        }
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarNews);
        setSupportActionBar(toolbar);

        int uid = GlobalFunctions.getUid();
        String username = GlobalFunctions.getUsername();
        String name = GlobalFunctions.getName();
        //Drawable profpic = GlobalFunctions.getProfpic();

        //ImageView imgWelcome = (ImageView) findViewById(R.id.imgWelcome);
        TextView txtMessage = (TextView) findViewById(R.id.txtMessage);
        //imgWelcome.setImageResource(R.mipmap.ic_launcher);
        if (txtMessage != null) {
            txtMessage.setText(name);
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

}
