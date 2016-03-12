package net.ddns.vishalbiswas.splash;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
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
        String email = GlobalFunctions.getEmail();
        Bitmap profpic = GlobalFunctions.getProfpic();

        ImageView fragPic = (ImageView) findViewById(R.id.fragPic);
        TextView fragUser = (TextView) findViewById(R.id.fragUser);
        TextView fragEmail = (TextView) findViewById(R.id.fragEmail);

        if (profpic != null) {
            assert fragPic != null;
            fragPic.setImageBitmap(profpic);
        }

        assert fragUser != null;
        fragUser.setText(name);

        assert fragEmail != null;
        fragEmail.setText(email);
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
