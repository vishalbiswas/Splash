package vishal.chetan.splash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;

import vishal.chetan.splash.android.SettingsActivity;

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
        Configuration config = new Configuration();
        config.locale = locale;
        ((Activity) con).getBaseContext().getResources().updateConfiguration(config, ((Activity) con).getBaseContext().getResources().getDisplayMetrics());
    }

    public static void initializeData(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        locale = new Locale(preferences.getString("locale", "en"));
    }

    public static void launchSettings(Context context) {
        Intent intent = new Intent(context,SettingsActivity.class);
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
        servers.addChangeListener(new ServerList.ServerListChangeListener() {
            @Override
            public void onChange(String name, String url, SourceOperation sourceOperation) {
                switch (sourceOperation) {
                    case ADD:
                    case UPDATE:
                        getSharedPreferences("sources", Context.MODE_PRIVATE).edit()
                                .putString(name, url).apply();
                        break;
                    case DELETE:
                        getSharedPreferences("sources", Context.MODE_PRIVATE).edit()
                                .remove(name).apply();
                }
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sources = getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", null);
        if (sources == null) {
            SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
            sources = new HashSet<>(0);
            editor.putStringSet("sourceKeys", null);
            editor.apply();
        }
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