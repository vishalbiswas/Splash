package vishal.chetan.splash;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.asyncs.ThreadHelper;

public class ModerationManager {
    public interface OnTaskCompleteListener {
        void onCompleted(int serverIndex, long id);
    }

    public static OnTaskCompleteListener extraListener = null;

    private static void report(final String path, final int serverIndex, final long id, final String msg, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "report/" + path, "id=" + id + "&msg=" + msg + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        int status = jsonObject.getInt("status");
                        if (status == 0) {
                            Log.d(ContentValues.TAG, String.format("Report submitted for %s id %s with message \"%s\"", path, id, msg));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
                            }
                        } else if (status == 3) {
                            Log.d(ContentValues.TAG, "Can only be reported once");
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

    private static void clearReport(final String path, final int serverIndex, final long id, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "clear/" + path, "id=" + id + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        int status = jsonObject.getInt("status");
                        if (status == 0) {
                            Log.d(ContentValues.TAG, String.format("Reports cleared for %s id %s", path, id));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
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

    public static void clearThread(int serverIndex, final long id) {
        Thread thread = SplashCache.ThreadCache.getThread(serverIndex, id, null);
        thread.reported = 0;
        if (SplashCache.ThreadCache.adapterListener != null) {
            SplashCache.ThreadCache.adapterListener.onModify(thread);
        }
        clearReport("thread", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void clearComment(int serverIndex, final long id) {
        clearReport("comment", serverIndex, id, null);
    }

    public static void clearUser(int serverIndex, final long id) {
        clearReport("user", serverIndex, id, null);
    }

    private static void unlock(final String path, final int serverIndex, final long id, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unlock/" + path, "id=" + id + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Unblocked %s id %s", path, id));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
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
        Thread thread = SplashCache.ThreadCache.getThread(serverIndex, id, null);
        thread.setBlocked(false);
        if (SplashCache.ThreadCache.adapterListener != null) {
            SplashCache.ThreadCache.adapterListener.onModify(thread);
        }
        unlock("thread", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void unlockComment(int serverIndex, final long threadid, final long id) {
        unlock("comment", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.getThread(serverIndex, threadid, null).clearComments();
            }
        });
    }

    public static void reviveUser(int serverIndex, final long id) {
        unlock("user", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.UsersCache.loadUser(serverIndex, id, null);
            }
        });
    }

    private static void unhide(final String path, final int serverIndex, final long id, final OnTaskCompleteListener listener) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "unhide/" + path, "id=" + id + "&sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Uhidden %s id %s", path, id));
                            if (listener != null) {
                                listener.onCompleted(serverIndex, id);
                            }
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
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
        Thread thread = SplashCache.ThreadCache.getThread(serverIndex, id, null);
        thread.setHidden(false);
        if (SplashCache.ThreadCache.adapterListener != null) {
            SplashCache.ThreadCache.adapterListener.onModify(thread);
        }
        unhide("thread", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void unhideComment(int serverIndex, final long threadid, final long id) {
        unhide("comment", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.getThread(serverIndex, threadid, null).clearComments();
            }
        });
    }

    public static void unbanUser(int serverIndex, final long id) {
        unhide("user", serverIndex, id, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.UsersCache.loadUser(serverIndex, id, null);
            }
        });
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
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
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
        Thread thread = SplashCache.ThreadCache.getThread(serverIndex, id, null);
        thread.setBlocked(true);
        if (SplashCache.ThreadCache.adapterListener != null) {
            SplashCache.ThreadCache.adapterListener.onModify(thread);
        }
        lock("thread", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void lockComment(int serverIndex, final long threadid, final long id, final String msg) {
        lock("comment", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.getThread(serverIndex, threadid, null).clearComments();
            }
        });
    }

    public static void revokeUser(int serverIndex, final long id, final String msg) {
        lock("user", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.UsersCache.loadUser(serverIndex, id, null);
            }
        });
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
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, id);
                                extraListener = null;
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
        Thread thread = SplashCache.ThreadCache.getThread(serverIndex, id, null);
        thread.setHidden(true);
        if (SplashCache.ThreadCache.adapterListener != null) {
            SplashCache.ThreadCache.adapterListener.onModify(thread);
        }
        hide("thread", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.updateThread(serverIndex, id);
            }
        });
    }

    public static void hideComment(int serverIndex, final long threadid, final long id, final String msg) {
        hide("comment", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.ThreadCache.getThread(serverIndex, threadid, null).clearComments();
            }
        });
    }

    public static void banUser(int serverIndex, final long id, final String msg) {
        hide("user", serverIndex, id, msg, new OnTaskCompleteListener() {
            @Override
            public void onCompleted(int serverIndex, long id) {
                SplashCache.UsersCache.loadUser(serverIndex, id, null);
            }
        });
    }

    public static void promote(final int serverIndex, final long uid) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "promote/" + uid, "sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Promoted user id %s", uid));
                            SplashCache.UsersCache.loadUser(serverIndex, uid, null);
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, uid);
                                extraListener = null;
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

    public static void demote(final int serverIndex, final long uid) {
        String session = GlobalFunctions.servers.get(serverIndex).identity.getSessionid();
        Runnable reporter = new ThreadHelper(serverIndex, "demote/" + uid, "sessionid=" + session) {
            @Override
            protected void doWork(JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        if (jsonObject.getInt("status") == 0) {
                            Log.d(ContentValues.TAG, String.format("Demoted user id %s", uid));
                            SplashCache.UsersCache.loadUser(serverIndex, uid, null);
                            if (extraListener != null) {
                                extraListener.onCompleted(serverIndex, uid);
                                extraListener = null;
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
}
