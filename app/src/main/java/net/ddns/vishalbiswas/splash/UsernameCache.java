package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;

class UsernameCache {
    private static SparseArray<SparseArray<String>> usernames = new SparseArray<>();

    static String getUser(Handler handler, int serverIndex, int uid) {
        SparseArray<String> serverArray = usernames.get(serverIndex);
        String name = null;
        if (serverArray == null) {
            serverArray = new SparseArray<>();
            usernames.append(serverIndex, serverArray);
        } else {
            name = serverArray.get(serverIndex);
        }

        if (name == null) {
            AsyncGetUser getUser = new AsyncGetUser();
            getUser.setHandler(handler);
            getUser.execute(serverIndex, uid);

            name = "UID:" + uid;
        }

        return name;
    }

    static void setUser(int serverIndex, int uid, String name) {
        SparseArray<String> serverArray= usernames.get(serverIndex);
        if (serverArray == null) {
            serverArray = new SparseArray<>();
        }
        serverArray.append(uid, name);
    }
}
