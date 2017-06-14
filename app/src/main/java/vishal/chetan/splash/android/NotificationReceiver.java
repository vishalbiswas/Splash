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
        public String title = "New action";
        String msg = null;
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
            for(int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                checkNotifications(i);
            }
        } else {
            checkNotifications(serverIndex);
        }

        wl.release();
    }

    private void checkNotifications(int serverIndex) {
        if (GlobalFunctions.preferences.getInt("notificationInterval", 60) > 0
                &&GlobalFunctions.servers.get(serverIndex) != null
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
                                if (notification_indices.get(serverIndex).get(jsonObject.getInt("notifyid")) != null) continue;
                                if (jsonObject.getLong("uid") != my_uid) continue;
                                final SplashNotification notification = new SplashNotification(jsonObject.getInt("notifyid"), serverIndex, jsonObject.getInt("code"), jsonObject.getBoolean("done"));
                                if (jsonObject.has("actionuid"))
                                    notification.actionuid = jsonObject.getLong("actionuid");
                                if (jsonObject.has("threadid"))
                                    notification.threadid = jsonObject.getLong("threadid");
                                if (jsonObject.has("commentid"))
                                    notification.commentid = jsonObject.getLong("commentid");
                                if (notification.code == OTHERS) {
                                    notification.setMessage(jsonObject.getString("custom"));
                                }
                                notifications.add(notification);
                                notification_indices.get(serverIndex).append(notification.notifyid, notifications.size() - 1);
                                if (!notification.isDone()) {
                                    if (notification.actionuid > 0) {
                                        getMessageAndIntent(notification);
                                    } else {
                                        showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid), null);
                                    }
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
        SplashCache.UsersCache.getUser(notification.serverIndex, notification.actionuid, new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(final UserIdentity user) {
                if (user != null) {
                    SplashCache.ThreadCache.getThread(notification.serverIndex, notification.threadid, new SplashCache.ThreadCache.OnGetThreadListener() {
                        @Override
                        public void onGetThread(Thread thread) {
                            String username = user.getUsername();
                            switch (notification.code) {
                                case COMMENT_TO_THREAD:
                                    notification.title = context.getString(R.string.strCommentThread, username, thread.getTitle());
                                    showCommentNotification(notification);
                                    break;
                                case COMMENT_TO_COMMENT:
                                    notification.title = context.getString(R.string.strComentComent, username, thread.getTitle());
                                    showCommentNotification(notification);
                                    break;

                            }
                        }
                    });
                }
            }
        });
    }

    private void showCommentNotification(final SplashNotification notification) {
        final Thread thread = SplashCache.ThreadCache.getThread(notification.serverIndex, notification.threadid, null);
        thread.getCommentAsync(notification.commentid, new Thread.LoadCommentsListener() {
            @Override
            public void onCommentsLoaded(boolean result) {
                if (result && thread.getComment(notification.commentid) != null) {
                    notification.msg = thread.getComment(notification.commentid).getText();
                    notification.setIntent(new Intent(context, ViewThreadActivity.class).putExtra("serverIndex", notification.serverIndex).putExtra("threadId", notification.threadid));
                    notification.attachid = SplashCache.UsersCache.getUser(notification.serverIndex, notification.actionuid, null).getProfpic();
                    UserIdentity user = SplashCache.UsersCache.getUser(notification.serverIndex, notification.actionuid, null);
                    if (notification.attachid > 0) {
                        SplashCache.AttachmentCache.get(notification.serverIndex, notification.attachid, new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                            @Override
                            public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                                showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid), getThumbnail(attachment));
                            }
                        });
                    } else {
                        showNotification(notification_indices.get(notification.serverIndex).get(notification.notifyid), null);
                    }
                }
            }
        });

    }

    private Bitmap getThumbnail(SplashCache.AttachmentCache.SplashAttachment attachment) {
        if (attachment != null && attachment.data != null) {
            if (attachment.type == SplashCache.AttachmentCache.SplashAttachment.IMAGE) {
                return (Bitmap) attachment.data;
            }
        }
        return null;
    }

    private void showNotification(int notificationIndex, Bitmap userImage) {
        SplashNotification notification = notifications.get(notificationIndex);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_news)
                .setContentTitle(notification.title)
                .setContentText(notification.getMessage())
                .setAutoCancel(true);
        if (userImage != null) {
            builder.setLargeIcon(userImage);
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