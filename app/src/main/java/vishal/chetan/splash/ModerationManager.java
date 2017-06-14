package vishal.chetan.splash;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.asyncs.ThreadHelper;

public class ModerationManager {
    interface OnTaskCompleteListener {
        void onCompleted(int serverIndex, long id);
    }

    private static void report(final String path, final int serverIndex, final long id, final String msg, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "report/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Report submitted for %s id %s with message \"%s\"", path, id, msg));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        GlobalFunctions.executor.execute(reporter);
    }

    public static void reportThread(int serverIndex, final long id, final String msg) {
        report("thread", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void reportComment(int serverIndex, final long id, final String msg) {
        report("comment", serverIndex, id, msg, null);
    }

    public static void reportUser(int serverIndex, final long id, final String msg) {
        report("user", serverIndex, id, msg, null);
    }

    private static void unlock(final String path, final int serverIndex, final long id, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unlock/" + path, "id=" + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Blocked %s id %s", path, id));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        GlobalFunctions.executor.execute(reporter);
    }

    public static void unlockThread(int serverIndex, final long id) {
        unlock("thread", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void unlockComment(int serverIndex, final long id) {
        unlock("comment", serverIndex, id, null);
    }

    public static void reviveUser(int serverIndex, final long id) {
        unlock("user", serverIndex, id, null);
    }

    private static void unhide(final String path, final int serverIndex, final long id, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unhide/" + path, "id=" + id + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Hidden %s id %s", path, id));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        GlobalFunctions.executor.execute(reporter);
    }

    public static void unhideThread(int serverIndex, final long id) {
        unhide("thread", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void unhideComment(int serverIndex, final long id) {
        unhide("comment", serverIndex, id, null);
    }

    public static void unbanUser(int serverIndex, final long id) {
        unhide("user", serverIndex, id, null);
    }

    private static void lock(final String path, final int serverIndex, final long id, final String msg, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "lock/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Blocked %s id %s with message \"%s\"", path, id, msg));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        GlobalFunctions.executor.execute(reporter);
    }

    public static void lockThread(int serverIndex, final long id, final String msg) {
        lock("thread", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void lockComment(int serverIndex, final long id, final String msg) {
        lock("comment", serverIndex, id, msg, null);
    }

    public static void revokeUser(int serverIndex, final long id, final String msg) {
        lock("user", serverIndex, id, msg, null);
    }

    private static void hide(final String path, final int serverIndex, final long id, final String msg, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "hide/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Hidden %s id %s with message \"%s\"", path, id, msg));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        GlobalFunctions.executor.execute(reporter);
    }

    public static void hideThread(int serverIndex, final long id, final String msg) {
        hide("thread", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void hideComment(int serverIndex, final long id, final String msg) {
        hide("comment", serverIndex, id, msg, null);
    }

    public static void banUser(int serverIndex, final long id, final String msg) {
        hide("user", serverIndex, id, msg, null);
    }
}
