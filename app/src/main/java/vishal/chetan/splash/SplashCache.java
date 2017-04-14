package vishal.chetan.splash;

import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import vishal.chetan.splash.asyncs.AsyncHelper;

import static android.content.ContentValues.TAG;

public class SplashCache {
    public static class UsernameCache {
        interface OnGetUserListener {
            void onGetUser(long uid);
        }

        private static final SparseArray<LongSparseArray<String>> usernames = new SparseArray<>();
        private static final SparseArray<Stack<OnGetUserListener>> listeners = new SparseArray<>();

        public static String getUser(final int serverIndex, final long uid) {
            Log.e(TAG, String.format("getting uid:%s server=%s", uid, serverIndex));
            LongSparseArray<String> serverArray = usernames.get(serverIndex);
            String name = null;
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                usernames.append(serverIndex, serverArray);
            } else {
                name = serverArray.get(uid);
            }
            if (name == null) {
                loadUsername(serverIndex, uid);
            }
            return name;
        }

        static private void loadUsername(final int serverIndex, long uid) {
            new AsyncHelper(serverIndex, "getuser/" + uid) {
                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    if (jsonObject != null) {
                        long uid;
                        try {
                            uid = jsonObject.getLong("uid");
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

        public static void setUser(int serverIndex, long uid, String name) {
            LongSparseArray<String> serverArray = usernames.get(serverIndex);
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
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
        public static void setFilterListener(OnThreadFilteredListener filterListener) {
            ThreadCache.filterListener = filterListener;
        }

        public interface OnThreadAddedListener {
            void onAddThread(Thread thread);
        }

        public interface OnThreadFilteredListener {
            // always adds an item to start
            void onFilter(int position);
        }

        private static final SparseArray<LongSparseArray<Thread>> threads = new SparseArray<>();
        private static final ArrayList<Thread> allThreads = new ArrayList<>();
        private static final List<OnThreadAddedListener> addListeners = new ArrayList<>();
        private static OnThreadFilteredListener filterListener;

        @Nullable
        public static ArrayList<Thread> getAllForIndex(final int filterIndex) {
            //// FIXME: 3/13/17 demo code to be removed
            add(new Thread(0, 0, "Hello", "Welcome to `Splash app`! Visit https://github.com/vishalbiswas/splash to know more.\n\n Have fun!", 1, new Date(), new Date(), 0));
            if (filterIndex == -1) {
                Collections.sort(allThreads, new Thread.ModificationTimeComparator());
                return allThreads;
            } else if (filterIndex < 0) {
                // index should be positive
                return null;
            } else {
                LongSparseArray<Thread> threadList = threads.get(filterIndex, new LongSparseArray<Thread>());
                ArrayList<Thread> returnList = new ArrayList<>();
                for(int i = 0; i < threadList.size(); ++i) {
                    returnList.add(threadList.valueAt(i));
                }
                Collections.sort(returnList, new Thread.ModificationTimeComparator());
                return returnList;
            }
        }

        public static void add(Thread thread) {
            LongSparseArray<Thread> threadList = threads.get(thread.getServerIndex());
            if (threadList == null) {
                threadList = new LongSparseArray<>();
                threadList.append(thread.getThreadId(), thread);
                threads.append(thread.getServerIndex(), threadList);
            } else {
                threadList.append(thread.getThreadId(), thread);
            }
            allThreads.add(0, thread);
            for (OnThreadAddedListener listener : addListeners) {
                listener.onAddThread(thread);
            }
        }

        public static Thread getThread(int serverIndex, long threadId) {
            return threads.get(serverIndex).get(threadId);
        }

        public static void addOnThreadAddedListener(OnThreadAddedListener listener) {
            addListeners.add(listener);
        }
    }
}