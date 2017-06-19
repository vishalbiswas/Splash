package vishal.chetan.splash.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncArrayHelper;
import vishal.chetan.splash.asyncs.AsyncHelper;

import static android.content.ContentValues.TAG;

public class NotificationReceiver extends BroadcastReceiver {
    private Context context;

    public interface OnNotificationAdded {
        void onNotificationAdded();
    }

    public static OnNotificationAdded listener = null;


    public static class SplashNotification {
        public int serverIndex;
        long notifyid;
        int code;
        boolean done;
        public long attachid = -1;

        public boolean isDone() {
            return done;
        }

        public void markDone() {
            if (!done) {
                new AsyncHelper(serverIndex, "notifications/done", String.format("sessionid=%s&notifyid=%s", GlobalFunctions.servers.get(serverIndex).identity.getSessionid(), notifyid)) {
                    @Override
                    protected void onPostExecute(JSONObject jsonObject) {
                        try {
                            if (jsonObject != null && jsonObject.getBoolean("status")) {
                                SplashNotification.this.done = done;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            }
        }

        public String getMessage() {
            return msg;
        }

        void setMessage(String msg) {
            this.msg = msg;
        }


        public Intent getIntent() {
            return intent;
        }

        public void setIntent(Intent intent) {
            this.intent = intent;
        }

        public Intent intent = null;
        public String title = "New from Splash";
        String msg = "";
        long threadid = -1;
        long commentid = -1;
        long actionuid = -1;

        SplashNotification(long notifyid, int serverIndex, int code, boolean done) {
            this.notifyid = notifyid;
            this.serverIndex = serverIndex;
            this.code = code;
            this.done = done;
        }
    }

    public static final int OTHERS = 0;
    public static final int COMMENT_TO_THREAD = 1;
    public static final int COMMENT_TO_COMMENT = 2;
    public static final int COMMENT_LOCKED = 3;
    public static final int THREAD_LOCKED = 4;
    public static final int COMMENT_UNLOCKED = 5;
    public static final int THREAD_UNLOCKED = 6;
    public static final int UNBANNED = 7;
    public static final int REVOKED = 8;
    public static final int REVIVED = 9;
    public static final int COMMENT_UNHIDDEN = 10;
    public static final int THREAD_UNHIDDEN = 11;
    public static final int COMMENT_HIDDEN = 12;
    public static final int THREAD_HIDDEN = 13;
    public static final int PROMOTED = 14;
    public static final int DEMOTED = 15;

    public static final List<SplashNotification> notifications = new ArrayList<>();
    public static final SparseArray<LongSparseArray<Integer>> notification_indices = new SparseArray<>();


    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        this.context = context;
        int serverIndex = intent.getIntExtra("serverIndex", -2);

        if (serverIndex == -1) {
            for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                checkNotifications(i);
            }
        } else {
            checkNotifications(serverIndex);
        }

        wl.release();
    }

    private void checkNotifications(int serverIndex) {
        if (GlobalFunctions.preferences.getInt("notificationInterval", 60) > 0
                && GlobalFunctions.servers.get(serverIndex) != null
                && GlobalFunctions.servers.get(serverIndex).isEnabled()
                && GlobalFunctions.servers.get(serverIndex).identity != null) {
            new AsyncArrayHelper(serverIndex, "notifications/unread", "sessionid=" + GlobalFunctions.servers.get(serverIndex).identity.getSessionid()) {
                @Override
                protected void onPostExecute(JSONArray jsonArray) {
                    long my_uid = GlobalFunctions.servers.get(serverIndex).identity.getUid();
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); ++i) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                LongSparseArray<Integer> indices = notification_indices.get(serverIndex);
                                if (indices == null) {
                                    indices = new LongSparseArray<>();
                                    notification_indices.append(serverIndex, indices);
                                }
                                if (indices.get(jsonObject.getInt("notifyid")) != null) continue;
                                if (jsonObject.getLong("uid") != my_uid) continue;
                                final SplashNotification notification = new SplashNotification(jsonObject.getInt("notifyid"), serverIndex, jsonObject.getInt("code"), jsonObject.getBoolean("done"));
                                if (jsonObject.has("actionuid"))
                                    notification.actionuid = jsonObject.getLong("actionuid");
                                if (jsonObject.has("threadid"))
                                    notification.threadid = jsonObject.getLong("threadid");
                                if (jsonObject.has("commentid"))
                                    notification.commentid = jsonObject.getLong("commentid");
                                if (jsonObject.has("custom")) {
                                    notification.title = jsonObject.getString("custom");
                                }
                                notifications.add(notification);
                                notification_indices.get(serverIndex).append(notification.notifyid, notifications.size() - 1);
                                if (!notification.isDone()) {
                                    getMessageAndIntent(notification);
                                }
                                if (listener != null) {
                                    listener.onNotificationAdded();
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error processing notification jsonObject at index " + i + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }.execute();
        } else {
            GlobalFunctions.broadcastToNotifications(context, serverIndex);
        }
    }

    private void getMessageAndIntent(final SplashNotification notification) {
        if (notification.threadid > 0) {
            SplashCache.ThreadCache.getThread(notification.serverIndex, notification.threadid, new SplashCache.ThreadCache.OnGetThreadListener() {
                @Override
                public void onGetThread(final Thread thread) {
                    if (thread != null) {
                        notification.setIntent(new Intent(context, ViewThreadActivity.class).putExtra("serverIndex", notification.serverIndex).putExtra("threadId", notification.threadid));
                        if (notification.commentid > 0) {
                            thread.loadComments(new Thread.LoadCommentsListener() {
                                @Override
                                public void onCommentsLoaded(boolean result) {
                                    if (notification.actionuid > 0) {
                                        SplashCache.UsersCache.getUser(notification.serverIndex, notification.actionuid, new SplashCache.UsersCache.OnGetUserListener() {
                                            @Override
                                            public void onGetUser(UserIdentity user) {
                                                if (user != null) {
                                                    String username = user.getUsername();
                                                    switch (notification.code) {
                                                        case COMMENT_TO_THREAD:
                                                            notification.title = context.getString(R.string.strCommentThread, username, thread.getTitle());
                                                            break;
                                                        case COMMENT_TO_COMMENT:
                                                            notification.title = context.getString(R.string.strComentComent, username, thread.getTitle());
                                                            break;
                                                    }
                                                    showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid));
                                                }
                                            }
                                        });
                                    } else {
                                        switch (notification.code) {
                                            case COMMENT_LOCKED:
                                                notification.title = context.getString(R.string.strCommentLocked, thread.getTitle());
                                                break;
                                            case COMMENT_UNLOCKED:
                                                notification.title = context.getString(R.string.strCommentUnlocked, thread.getTitle());
                                                break;
                                            case COMMENT_UNHIDDEN:
                                                notification.title = context.getString(R.string.strCommentUnhidden, thread.getTitle());
                                                break;
                                        }
                                    }
                                    notification.msg = thread.getComment(notification.commentid).getText();
                                    showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid));
                                }
                            });
                        } else {
                            switch (notification.code) {
                                case THREAD_LOCKED:
                                    notification.title = context.getString(R.string.strThreadLocked, thread.getTitle());
                                    break;
                                case THREAD_UNLOCKED:
                                    notification.title = context.getString(R.string.strThreadUnlocked, thread.getTitle());
                                    break;
                                case THREAD_UNHIDDEN:
                                    notification.title = context.getString(R.string.strThreadUnhidden, thread.getTitle());
                                    break;
                            }
                            showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid));
                        }
                    }
                }
            });
        } else {
            switch (notification.code) {
                case COMMENT_HIDDEN:
                    notification.title = context.getString(R.string.strCommentHidden, notification.title);
                    break;
                case THREAD_HIDDEN:
                    notification.title = context.getString(R.string.strThreadBanned, notification.title);
                    break;
                case UNBANNED:
                    notification.title = "You were unbanned";
                    break;
                case REVOKED:
                    notification.title = "Your posting or commenting right(s) were revoked";
                    break;
                case REVIVED:
                    notification.title = "Your posting or commenting right(s) were restored";
                    break;
                case PROMOTED:
                    notification.title = "You are now a moderator";
                    break;
                case DEMOTED:
                    notification.title = "You have lost moderation rights";
                    break;
            }
            showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid));
        }
    }

    private Bitmap getThumbnail(SplashCache.AttachmentCache.SplashAttachment attachment) {
        if (attachment != null && attachment.data != null) {
            if (attachment.type == SplashCache.AttachmentCache.SplashAttachment.IMAGE) {
                return (Bitmap) attachment.data;
            }
        }
        return null;
    }

    private void showNotification(final int notificationIndex) {
        SplashNotification notification = notifications.get(notificationIndex);
        if (notification.attachid > 0) {
            SplashCache.AttachmentCache.get(notification.serverIndex, notification.attachid, new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                @Override
                public void onGetAttachment(SplashCache.AttachmentCache.SplashAttachment attachment) {
                    showNotification(notificationIndex, getThumbnail(attachment));
                }
            });
        } else {
            showNotification(notificationIndex, null);
        }
    }

    private void showNotification(int notificationIndex, Bitmap userImage) {
        SplashNotification notification = notifications.get(notificationIndex);
        if (notification.isDone()) {
            return;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_news)
                .setContentTitle(notification.title)
                .setAutoCancel(true);
        if (userImage != null) {
            builder.setLargeIcon(userImage);
        }
        if (notification.msg.length() > 0) {
            builder.setContentText(notification.getMessage());
        } else {
            builder.setContentText(GlobalFunctions.servers.get(notification.serverIndex).getName());
        }

        if (notification.getIntent() != null) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(notification.getIntent().getComponent());
            stackBuilder.addNextIntent(notification.getIntent());
            PendingIntent resultIntent = stackBuilder.getPendingIntent(notificationIndex, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultIntent);
        }
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationIndex, builder.build());
        notification.markDone();
    }
}