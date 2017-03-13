package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;

class UsernameCache {
    private static SparseArray<String> usernames = new SparseArray<>();

    static String getUser(Handler handler, int uid) {
        String name;
        name = usernames.get(uid);

        if (name == null) {
            AsyncGetUser getUser = new AsyncGetUser();
            getUser.setHandler(handler);
            getUser.execute(uid);

            name = "UID:" + uid;
        }

        return name;
    }

    static void setUser(int uid, String name) {
        usernames.append(uid, name);
    }
}
