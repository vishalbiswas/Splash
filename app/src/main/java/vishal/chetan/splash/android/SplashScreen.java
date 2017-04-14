package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import vishal.chetan.splash.BuildConfig;
import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        GlobalFunctions.initializeData(SplashScreen.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (BuildConfig.BUILD_TYPE.equals("debug") && GlobalFunctions.servers.size() == 0) {
            if (Build.PRODUCT.startsWith("sdk")) {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://10.0.2.1:5000"));
            } else {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://192.168.1.2:5000"));
            }
        }

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(SplashScreen.this, R.string.errNoLoginServer, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SplashScreen.this, SourcesManagerActivity.class));
            finish();
            return;
        }

        for(int i = 0; i < GlobalFunctions.servers.size(); ++i) {
            new GlobalFunctions.CheckSource(this).execute(i);
        }

        startActivity(new Intent(SplashScreen.this, NewsFeed.class));
        finish();
    }

}
