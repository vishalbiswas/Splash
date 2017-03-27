package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GlobalFunctions extends Application {
    public static ConnectivityManager connMan;
    public static boolean isSessionAlive = false;
    private static Locale locale;
    private static HTTP_CODE regNameStatus;
    private static HTTP_CODE regEmailStatus;
    /**
     * Run updateServerList after adding or deleting from it
     */
    static ArrayList<String> servers = new ArrayList<>();
    static SparseArray<UserIdentity> identities = new SparseArray<>();
    static UserIdentity defaultIdentity;

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

    static public void updateServerList(Context context) {
        context.getSharedPreferences("sources", Context.MODE_PRIVATE).edit()
                .putStringSet("sources", new HashSet<>(servers)).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sources = preferences.getStringSet("sources", null);
        if (sources == null) {
            SharedPreferences.Editor editor = getSharedPreferences("sources", MODE_PRIVATE).edit();
            sources = new HashSet<>(0);
            editor.putStringSet("sources", null);
            editor.apply();
        }
        servers.addAll(sources);

        defaultIdentity = new UserIdentity(preferences.getString("defaultUsername", "SplashUser"),
                preferences.getString("defaultFname", ""),
                preferences.getString("defaultLname", ""),
                preferences.getString("defaultEmail", ""));
        defaultIdentity.setProfpic(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
    }

    enum HTTP_CODE {
        SUCCESS,
        FAILED,
        NO_ACCESS,
        REQUEST_FAILED,
        BUSY,
        UNKNOWN
    }
}
