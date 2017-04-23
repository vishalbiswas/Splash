package vishal.chetan.splash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;

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

    private final static int NORMAL = 0, IMAGE = 1;
    @Nullable
    private final ArrayList<Thread> threadList;
    private final Context context;

    public ThreadsAdapter(Context context, final int filter) {
        threadList = SplashCache.ThreadCache.getAllForIndex(filter);
        this.context = context;
        SplashCache.ThreadCache.setOnThreadModifyListener(new SplashCache.ThreadCache.OnThreadModifiedListener() {
            @Override
            public void onModify(@Nullable Thread thread) {
                if (thread != null) {
                    if (filter == -1 || thread.getServerIndex() == filter) {
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public ThreadsAdapter(Context context, @Nullable final ArrayList<Thread> threadList) {
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
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ThreadViewHolder holder, final int position) {
        assert threadList != null;
        final Thread thread = threadList.get(position);
        switch (holder.getItemViewType()) {
            case IMAGE:
                SplashCache.ImageCache.get(thread.getServerIndex(), thread.getAttachId(), new SplashCache.ImageCache.OnGetImageListener() {
                    @Override
                    public void onGetImage(final Bitmap image) {
                        ((AppCompatActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ThreadWithImageViewHolder) holder).imgAttach.setImageBitmap(image);
                            }
                        });
                    }
                });
                break;
        }
        holder.title.setText(thread.getTitle());
        holder.content.setHtml(thread.getContent());
        UserIdentity user = SplashCache.UsersCache.getUser(thread.getServerIndex(), thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(@NonNull final UserIdentity user) {
                ((Activity)context).runOnUiThread(new Runnable() {
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
        return threadList.get(i).getThreadId();
    }

    @Override
    public int getItemCount() {
        assert threadList != null;
        return threadList.size();
    }

    @Override
    public int getItemViewType(int position) {
        assert threadList != null;
        if (threadList.get(position).getAttachId() >= 0) {
            return IMAGE;
        } else {
            return NORMAL;
        }
    }
}
