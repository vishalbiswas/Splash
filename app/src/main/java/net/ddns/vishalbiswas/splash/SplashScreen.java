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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        GlobalFunctions.initializeData(SplashScreen.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        class pingNetworks extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                GlobalFunctions.connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    URL urlServer;
                    HttpURLConnection urlConn;
                    String[] sources = {"http://vishalbiswas.asuscomm.com", "http://vishalbiswas.ddns.net"};
                    for (final String source : sources) {
                        try {
                            urlServer = new URL(source);
                            urlConn = (HttpURLConnection) urlServer.openConnection();
                            urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                            urlConn.connect();
                            if (urlConn.getResponseCode() == 200) {
                                GlobalFunctions.setServer(source);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (GlobalFunctions.getServer() == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SplashScreen.this, R.string.errNoLoginServer, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    for (
                            final String source
                            : PreferenceManager.getDefaultSharedPreferences(SplashScreen.this).getStringSet("sourcesNDTV", new HashSet<String>())
                            ) {
                        try {
                            urlServer = new URL(source);
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
                                    Toast.makeText(SplashScreen.this, source, Toast.LENGTH_LONG).show();
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
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                SplashScreen.this.finish();
                super.onPostExecute(aVoid);
            }
        }

        new pingNetworks().execute();
    }

}
