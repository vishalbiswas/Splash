package net.ddns.vishalbiswas.splash;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

public class SplashScreen extends AppCompatActivity {
    //private final int launchDelay = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        class pingNetworks extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                GlobalFunctions.initializeData(SplashScreen.this);
                ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connMan.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    for (
                            final String source
                            : PreferenceManager.getDefaultSharedPreferences(SplashScreen.this).getStringSet("sourcesNDTV", new HashSet<String>())
                            ) {
                        try {
                            URL urlServer = new URL(source);
                            HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                            urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                            urlConn.connect();
                            if (urlConn.getResponseCode() != 200) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SplashScreen.this, source, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                SplashScreen.this.finish();
                super.onPostExecute(aVoid);
            }
        }

        new pingNetworks().execute();
    }

}
