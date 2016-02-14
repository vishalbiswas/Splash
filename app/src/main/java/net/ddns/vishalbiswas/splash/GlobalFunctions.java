package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

public class GlobalFunctions {
    static Context context;

    public static void launchSettings(Context con) {
        context = con;
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void showSnack() {
        if (!PreferenceManager.getDefaultSharedPreferences(context).getString("locale", "en").equals(context.getResources().getConfiguration().locale.getLanguage())) {
            Snackbar.make(((Activity) context).findViewById(R.id.userInputLayout), R.string.restartNotify, Snackbar.LENGTH_LONG).show();
        }
    }
}
