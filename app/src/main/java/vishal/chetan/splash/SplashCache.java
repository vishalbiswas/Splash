package vishal.chetan.splash;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Stack;

import vishal.chetan.splash.asyncs.AsyncHelper;

import static android.content.ContentValues.TAG;

class SplashCache {
    static class UsernameCache {
        interface OnGetUserListener {
            void onGetUser(int uid);
        }

        private static final SparseArray<SparseArray<String>> usernames = new SparseArray<>();
        private static final SparseArray<Stack<OnGetUserListener>> listeners = new SparseArray<>();

        static String getUser(final int serverIndex, final int uid) {
            Log.e(TAG, String.format("getting uid:%s server=%s", uid, serverIndex));
            SparseArray<String> serverArray = usernames.get(serverIndex);
            String name = null;
            if (serverArray == null) {
                serverArray = new SparseArray<>();
                usernames.append(serverIndex, serverArray);
            } else {
                name = serverArray.get(uid);
            }
            if (name == null) {
                loadUsername(serverIndex, uid);
            }
            return name;
        }

        static private void loadUsername(final int serverIndex, int uid) {
            new AsyncHelper(serverIndex, "getuser/" + uid) {
                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    if (jsonObject != null) {
                        int uid;
                        try {
                            uid = jsonObject.getInt("uid");
                        } catch (JSONException ex) {
                            Log.e(TAG, "uid not found");
                            return;
                        }
                        try {
                            String name = jsonObject.getString("user");
                            setUser(serverIndex, uid, name);
                        } catch (JSONException ex) {
                            Log.e(TAG, "user not found");
                        }
                    } else {
                        Log.e(TAG, "Unknown error");
                    }
                }
            }.execute();
        }

        public static void setUser(int serverIndex, int uid, String name) {
            SparseArray<String> serverArray = usernames.get(serverIndex);
            if (serverArray == null) {
                serverArray = new SparseArray<>();
                usernames.append(serverIndex, serverArray);
            }
            serverArray.append(uid, name);
            while (!listeners.get(serverIndex).isEmpty()) {
                listeners.get(serverIndex).pop().onGetUser(uid);
            }
        }

        public static void addGetUserListener(int serverIndex, OnGetUserListener listener) {
            Stack<OnGetUserListener> serverListener = listeners.get(serverIndex);
            if (serverListener == null) {
                serverListener = new Stack<>();
                listeners.append(serverIndex, serverListener);
            }
            serverListener.add(listener);
        }
    }

    public static class ThreadCache {
        private static final SparseArray<ArrayList<Thread>> threads = new SparseArray<>();

        public static ArrayList<Thread> getAllForIndex(int filterIndex) {
            if (filterIndex == -1) {
                ArrayList<Thread> allThreads = new ArrayList<>();
                //// FIXME: 3/13/17 demo code to be removed
                add(new Thread(0, "Hello", "Welcome to Splash app! Have fun!", 1, new Date(), new Date()));
                for (int index = 0; index < GlobalFunctions.servers.size(); ++index) {
                    allThreads.addAll(threads.get(index, new ArrayList<Thread>()));
                }
                Collections.sort(allThreads, new Thread.ModificationTimeComparator());
                return allThreads;
            } else if (filterIndex < 0) {
                // index should be positive
                return null;
            } else {
                return threads.get(filterIndex, new ArrayList<Thread>());
            }
        }

        public static void add(Thread thread) {
            ArrayList<Thread> threadList = threads.get(thread.getServerIndex());
            if (threadList == null) {
                threadList = new ArrayList<>();
                threadList.add(thread);
                threads.append(thread.getServerIndex(), threadList);
            } else {
                threadList.add(thread);
            }
        }
    }
}