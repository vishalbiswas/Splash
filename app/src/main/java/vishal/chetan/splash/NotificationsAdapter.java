package vishal.chetan.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import vishal.chetan.splash.android.NotificationReceiver;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final Context context;

    public NotificationsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final NotificationReceiver.SplashNotification notification = NotificationReceiver.notifications.get(position);
        SplashCache.AttachmentCache.get(notification.serverIndex, notification.attachid, new SplashCache.AttachmentCache.OnGetAttachmentListener() {
            @Override
            public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                if (attachment != null && attachment.data != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (attachment.type == SplashCache.AttachmentCache.SplashAttachment.IMAGE) {
                                holder.notificationImage.setImageBitmap((Bitmap) attachment.data);
                            } else {
                                holder.notificationImage.setImageBitmap(null);
                            }
                        }
                    });
                }
            }
        });
        holder.notificationTitle.setText(notification.title);
        holder.notificationContent.setHtml(notification.getMessage());
        holder.notificationServer.setText(GlobalFunctions.servers.get(notification.serverIndex).getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notification.intent != null) {
                    context.startActivity(notification.intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return NotificationReceiver.notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView notificationImage;
        public final TextView notificationTitle;
        public final NoScrollHtmlTextView notificationContent;
        public final TextView notificationServer;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            notificationImage = (ImageView) view.findViewById(R.id.notificationImage);
            notificationTitle = (TextView) view.findViewById(R.id.notificationTitle);
            notificationContent = (NoScrollHtmlTextView) view.findViewById(R.id.notificationContent);
            notificationServer = (TextView) view.findViewById(R.id.notificationServer);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + notificationContent.getText() + "'";
        }
    }
}
