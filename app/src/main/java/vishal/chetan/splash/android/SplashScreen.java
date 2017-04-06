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
                    /*sources.add("http://vishalbiswas.asuscomm.com");
                    sources.add("http://vishalbiswas.ddns.net");
                    sources.add("http://vishalbiswas.tigrimigri.com");*/


        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(SplashScreen.this, R.string.errNoLoginServer, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SplashScreen.this, SourcesManagerActivity.class));
            finish();
            return;
        }

        class pingNetworks extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                GlobalFunctions.connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    URL urlServer;
                    HttpURLConnection urlConn;

                    for (final ServerList.SplashSource source
                            : GlobalFunctions.servers
                            ) {
                        try {
                            urlServer = new URL(source.getUrl());
                            urlConn = (HttpURLConnection) urlServer.openConnection();
                            urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                            urlConn.connect();
                            if (urlConn.getResponseCode() != 200) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SplashScreen.this, String.format("%s %s", source.getName(), getString(R.string.strNoResponse)), Toast.LENGTH_LONG).show();
                                    source.setDisabled(true);
                                }
                            });
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SplashScreen.this, R.string.warnOffline, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent intent = new Intent(SplashScreen.this, NewsFeed.class);
                startActivity(intent);
                SplashScreen.this.finish();
                super.onPostExecute(aVoid);
            }
        }

        new pingNetworks().execute();
    }

}