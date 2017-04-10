package vishal.chetan.splash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.widget.Toast;

import vishal.chetan.splash.android.SettingsActivity;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GlobalFunctions extends Application {
    public static ConnectivityManager connMan;
    private static Locale locale;
    private static HTTP_CODE regNameStatus;
    private static HTTP_CODE regEmailStatus;
    /**
     * Run updateServerList after adding or deleting from it
     */
    public static final ServerList servers = ServerList.getInstance();
    public static final SparseArray<UserIdentity> identities = new SparseArray<>();
    public static UserIdentity defaultIdentity;

    public static HTTP_CODE getRegNameStatus() {
        return regNameStatus;
    }

    public static void setRegNameStatus(HTTP_CODE regNameStatus) {
        GlobalFunctions.regNameStatus = regNameStatus;
    }

    public static void lookupLocale(Context con) {
        if (locale == null) initializeData(con);
        final Configuration config = new Configuration();
        config.locale = locale;
        ((Activity) con).getBaseContext().getResources().updateConfiguration(config, ((Activity) con).getBaseContext().getResources().getDisplayMetrics());
    }

    public static void initializeData(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        locale = new Locale(preferences.getString("locale", "en"));
    }

    public static void launchSettings(Context context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void showSnack(Context context) {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getString("locale", "en").equals(locale.getLanguage())) {
            Snackbar.make(((Activity) context).findViewById(R.id.frag), R.string.restartNotify, Snackbar.LENGTH_LONG).show();
        }
    }

    public static HTTP_CODE getRegEmailStatus() {
        return regEmailStatus;
    }

    public static void setRegEmailStatus(HTTP_CODE regEmailStatus) {
        GlobalFunctions.regEmailStatus = regEmailStatus;
    }

    public static boolean getRegStatus() {
        return regNameStatus == HTTP_CODE.SUCCESS && regEmailStatus == HTTP_CODE.SUCCESS;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sources = getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>());
        for (String key :
                sources) {
            String sourceUrl = getSharedPreferences("sources", MODE_PRIVATE).getString(key, null);
            if (sourceUrl == null) continue;
            servers.add(new ServerList.SplashSource(key, sourceUrl));
        }

        defaultIdentity = new UserIdentity(preferences.getString("defaultUsername", "SplashUser"),
                preferences.getString("defaultFname", ""),
                preferences.getString("defaultLname", ""),
                preferences.getString("defaultEmail", ""));
        defaultIdentity.setProfpic(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        servers.addListener(new ServerList.OnServerListChangeListener() {
            @Override
            public void onAdd(ServerList.SplashSource source) {
                Set<String> sourceKeys = new HashSet<>(getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>()));
                if (!sourceKeys.contains(source.getName())) {
                    sourceKeys.add(source.getName());
                    getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putStringSet("sourceKeys", sourceKeys).apply();
                    getSharedPreferences("sources", Context.MODE_PRIVATE).edit()
                            .putString(source.getName(), source.getUrl()).apply();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.strSrcExists), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRemove(ServerList.SplashSource source) {
                Set<String> sourceKeys = new HashSet<>(getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>()));
                sourceKeys.remove(source.getName());
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putStringSet("sourceKeys", sourceKeys).apply();
                getSharedPreferences("sources", Context.MODE_PRIVATE).edit().remove(source.getName()).apply();
            }

            @Override
            public void onUpdate(ServerList.SplashSource previousSource, ServerList.SplashSource updatedSource) {
                onRemove(previousSource);
                onAdd(updatedSource);
            }
        });

    }

    public static class CheckSource extends AsyncTask<Integer, Void, Void> {
        protected ServerList.SplashSource source;
        Activity activity;

        public CheckSource(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(final Integer... params) {
            source = GlobalFunctions.servers.get(params[0]);
            boolean result = false;
            NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                URL urlServer;
                HttpURLConnection urlConn;
                try {
                    urlServer = new URL(source.getUrl());
                    urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                    urlConn.connect();
                    if (urlConn.getResponseCode() != 200) {
                        throw new Exception();
                    }
                    result = true;
                } catch (Exception e) {
                    result = false;
                }
            }
            if (!result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Error occurred while connecting to " + source.getName() + ". Disabling...", Toast.LENGTH_LONG).show();
                        GlobalFunctions.servers.setDisabled(params[0], true);
                    }
                });
            }
            return null;
        }
    }

    public enum HTTP_CODE {
        SUCCESS,
        FAILED,
        NO_ACCESS,
        REQUEST_FAILED,
        BUSY,
        UNKNOWN
    }
}
