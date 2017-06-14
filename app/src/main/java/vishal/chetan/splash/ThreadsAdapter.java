package vishal.chetan.splash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Random;

import vishal.chetan.splash.android.NewsFeed;
import vishal.chetan.splash.android.ViewThreadActivity;

public class ThreadsAdapter extends RecyclerView.Adapter<ThreadsAdapter.ThreadViewHolder> {
    class ThreadViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        final TextView title;
        @NonNull
        final HtmlTextView content;
        @NonNull
        final TextView creator;
        @NonNull
        final TextView mtime;

        ThreadViewHolder(@NonNull View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.threadTitle);
            content = (HtmlTextView) view.findViewById(R.id.threadPreview);
            creator = (TextView) view.findViewById(R.id.creator);
            mtime = (TextView) view.findViewById(R.id.mtime);
        }
    }

    private class ThreadWithImageViewHolder extends ThreadViewHolder {
        @NonNull
        final ImageView imgAttach;

        ThreadWithImageViewHolder(@NonNull View view) {
            super(view);
            imgAttach = (ImageView) view.findViewById(R.id.imgAttach);
        }
    }

    private class ThreadGenericViewHolder extends ThreadViewHolder {
        @NonNull
        final ImageView threadAttachIcon;
        final TextView threadDummy;

        ThreadGenericViewHolder(@NonNull View view) {
            super(view);
            threadAttachIcon = (ImageView) view.findViewById(R.id.threadAttachIcon);
            threadDummy = (TextView) view.findViewById(R.id.threadDummy);
        }
    }

    private final static int NORMAL = 0, IMAGE = 1, GENERIC = 2;
    @Nullable
    private final ArrayList<Thread> threadList;
    private final Activity context;
    private static int previousSize;

    public ThreadsAdapter(final Activity context, final int filter) {
        threadList = SplashCache.ThreadCache.getAllForIndex(filter);
        this.context = context;
        SplashCache.ThreadCache.adapterListener = new SplashCache.ThreadCache.OnThreadModifiedListener() {
            @Override
            public void onModify(@Nullable Thread thread) {
                if (thread != null) {
                    if (filter == -1 || thread.getServerIndex() == filter) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        };
    }

    public ThreadsAdapter(Activity context, @Nullable final ArrayList<Thread> threadList) {
        this.context = context;
        this.threadList = threadList;
    }

    @Nullable
    @Override
    public ThreadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case NORMAL:
                return new ThreadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread, parent, false));
            case IMAGE:
                return new ThreadWithImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread_with_image, parent, false));
            default:
                return new ThreadGenericViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread_generic_attachment, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ThreadViewHolder holder, final int position) {
        assert threadList != null;
        final Thread thread = threadList.get(position);
        if (thread.isHidden()) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.hidden));
        } else if (thread.isBlocked()) {
            holder.setIsRecyclable(false);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.locked));
        }
        if (thread.getAttachType() != SplashCache.AttachmentCache.SplashAttachment.NONE) {
            SplashCache.AttachmentCache.get(thread.getServerIndex(), thread.getAttachId(), new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                        @Override
                        public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                            if (attachment != null) {
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (holder.getItemViewType()) {
                                            case IMAGE:
                                                ((ThreadWithImageViewHolder) holder).imgAttach.setImageBitmap((Bitmap)attachment.data);
                                                break;
                                            case GENERIC:
                                                Drawable icon;
                                                switch (attachment.type) {
                                                    case SplashCache.AttachmentCache.SplashAttachment.VIDEO:
                                                        icon = context.getResources().getDrawable(R.drawable.ic_video);
                                                        break;
                                                    case SplashCache.AttachmentCache.SplashAttachment.AUDIO:
                                                        icon = context.getResources().getDrawable(R.drawable.ic_audio);
                                                        break;
                                                    default:
                                                        icon = context.getResources().getDrawable(R.drawable.ic_file);
                                                        break;
                                                }
                                                ((ThreadGenericViewHolder)holder).threadAttachIcon.setImageDrawable(icon);
                                                ((ThreadGenericViewHolder)holder).threadDummy.setText(attachment.name);
                                        }
                                    }
                                });
                            }
                        }
                    });

        }
        holder.title.setText(thread.getTitle());
        holder.content.setHtml(thread.getContent());
        UserIdentity user = SplashCache.UsersCache.getUser(thread.getServerIndex(), thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(@NonNull final UserIdentity user) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.creator.setText(user.getUsername());
                    }
                });
            }
        });
        if (user == null) {
            holder.creator.setText("UID:" + thread.getCreatorID());
        }
        holder.mtime.setText(DateUtils.getRelativeTimeSpanString(thread.getMtime().getTime()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ViewThreadActivity.class).putExtra("serverIndex", thread.getServerIndex()).putExtra("threadId", thread.getThreadId()));
            }
        });
    }

    @Override
    public long getItemId(int i) {
        assert threadList != null;
        return threadList.get(i).adapterId;
    }

    @Override
    public int getItemCount() {
        assert threadList != null;
        return threadList.size();
    }

    public Thread getThread(int position) {
        assert threadList != null;
        return threadList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        assert threadList != null;
        if (threadList.get(position).getAttachType() == SplashCache.AttachmentCache.SplashAttachment.IMAGE) {
            return IMAGE;
        } else if (threadList.get(position).getAttachId() < 0) {
            return NORMAL;
        } else {
            return GENERIC;
        }
    }
}
