package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class NewsFeed extends AppCompatActivity {
    private TextView txtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //txtMessage = (TextView)findViewById(R.id.txtMessage);

        String username = getIntent().getStringExtra("net.ddns.vishalbiswas.splash.EXTRA_USERNAME");
        String password = getIntent().getStringExtra("net.ddns.vishalbiswas.splash.EXTRA_PASSWORD");

        //txtMessage.setText(String.format("%s: %s\n%s: %s", getText(R.string.msgUsername), username, getText(R.string.msgPassword), password));
        AppCompatImageView imgWelcome = (AppCompatImageView) findViewById(R.id.imgWelcome);
        AppCompatTextView txtWelcome = (AppCompatTextView) findViewById(R.id.txtWelcome);
        imgWelcome.setImageResource(R.mipmap.ic_launcher);
        txtWelcome.setText(username);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(NewsFeed.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
