package vishal.chetan.splash;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.asyncs.ThreadHelper;

public class ModerationManager {
    private static void report(final String path, int serverIndex, final long id, final String msg) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "report/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Report submitted for %s id %s with message \"%s\"", path, id, msg));
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
        report("thread", serverIndex, id, msg);
    }

    public static void reportComment(int serverIndex, final long id, final String msg) {
        report("comment", serverIndex, id, msg);
    }

    public static void reportUser(int serverIndex, final long id, final String msg) {
        report("user", serverIndex, id, msg);
    }

    private static void unlock(final String path, int serverIndex, final long id) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unlock/" + path, "id=" + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Blocked %s id %s", path, id));
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
        unlock("thread", serverIndex, id);
    }

    public static void unlockComment(int serverIndex, final long id) {
        unlock("comment", serverIndex, id);
    }

    public static void unlockUser(int serverIndex, final long id) {
        unlock("user", serverIndex, id);
    }

    private static void unhide(final String path, int serverIndex, final long id) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unhide/" + path, "id=" + id + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Hidden %s id %s", path, id));
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
        unhide("thread", serverIndex, id);
    }

    public static void unhideComment(int serverIndex, final long id) {
        unhide("comment", serverIndex, id);
    }

    public static void unhideUser(int serverIndex, final long id) {
        unhide("user", serverIndex, id);
    }

    private static void lock(final String path, int serverIndex, final long id, final String msg) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "lock/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Blocked %s id %s with message \"%s\"", path, id, msg));
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
        lock("thread", serverIndex, id, msg);
    }

    public static void lockComment(int serverIndex, final long id, final String msg) {
        lock("comment", serverIndex, id, msg);
    }

    public static void lockUser(int serverIndex, final long id, final String msg) {
        lock("user", serverIndex, id, msg);
    }

    private static void hide(final String path, int serverIndex, final long id, final String msg) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "hide/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Hidden %s id %s with message \"%s\"", path, id, msg));
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
        hide("thread", serverIndex, id, msg);
    }

    public static void hideComment(int serverIndex, final long id, final String msg) {
        hide("comment", serverIndex, id, msg);
    }

    public static void hideUser(int serverIndex, final long id, final String msg) {
        hide("user", serverIndex, id, msg);
    }
}
