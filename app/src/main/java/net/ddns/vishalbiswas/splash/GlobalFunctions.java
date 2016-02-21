package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import java.util.Locale;

public class GlobalFunctions extends Application {
    private static Context context;
    private static Locale locale;

    public static void lookupLocale(Context con) {
        if (locale == null) initializeData(con);
        Configuration config = new Configuration();
        config.locale = locale;
        ((Activity) con).getBaseContext().getResources().updateConfiguration(config, ((Activity) con).getBaseContext().getResources().getDisplayMetrics());
    }

    public static void initializeData(Context con) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(con.getApplicationContext());
        locale = new Locale(preferences.getString("locale", "en"));
    }

    public static void launchSettings(Context con) {
        context = con;
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void showSnack() {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getString("locale", "en").equals(locale.getLanguage())) {
            Snackbar.make(((Activity) context).findViewById(R.id.frag), R.string.restartNotify, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
}
