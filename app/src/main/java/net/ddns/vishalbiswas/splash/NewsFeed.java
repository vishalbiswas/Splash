package net.ddns.vishalbiswas.splash;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsFeed extends AppCompatActivity {

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
        String name;
        if (GlobalFunctions.getFirstname().isEmpty() && GlobalFunctions.getLastname().isEmpty()) {
            name = username;
        } else {
            name = String.format("%s %s", GlobalFunctions.getFirstname(), GlobalFunctions.getLastname());
        }
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

        View userDisplayFragmet = findViewById(R.id.userDisplayFragment);
        assert userDisplayFragmet != null;
        userDisplayFragmet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsFeed.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
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
