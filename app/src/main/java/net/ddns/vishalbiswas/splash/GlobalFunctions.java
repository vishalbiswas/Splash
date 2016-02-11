package net.ddns.vishalbiswas.splash;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Vishal Biswas on 2/10/2016.
 */
public class GlobalFunctions {
    public static void launchSettings(Context context) {
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }
}
