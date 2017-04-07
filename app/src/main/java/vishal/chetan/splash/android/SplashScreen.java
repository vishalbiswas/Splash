package vishal.chetan.splash.android;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import vishal.chetan.splash.BuildConfig;
import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;

import java.net.HttpURLConnection;
import java.net.URL;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        GlobalFunctions.initializeData(SplashScreen.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (BuildConfig.BUILD_TYPE.equals("debug") && GlobalFunctions.servers.size() == 0) {
            if (Build.PRODUCT.startsWith("sdk")) {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://10.0.2.1"));
            } else {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://192.168.1.2"));
            }
        }

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(SplashScreen.this, R.string.errNoLoginServer, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SplashScreen.this, SourcesManagerActivity.class));
            finish();
            return;
        }

        final GlobalFunctions.CheckSource checkSource = new GlobalFunctions.CheckSource(this);
        for(int i = 0; i < GlobalFunctions.servers.size(); ++i) {
            checkSource.execute(i);
        }

        startActivity(new Intent(SplashScreen.this, NewsFeed.class));
        finish();
    }

}
