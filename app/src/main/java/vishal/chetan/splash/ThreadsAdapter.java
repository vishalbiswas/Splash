package vishal.chetan.splash;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.util.Linkify;
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
        final TextView title;
        final HtmlTextView content;
        final TextView creator;
        final TextView mtime;

        ThreadViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.threadTitle);
            content = (HtmlTextView) view.findViewById(R.id.threadPreview);
            creator = (TextView) view.findViewById(R.id.creator);
            mtime = (TextView) view.findViewById(R.id.mtime);
        }
    }

    private class ThreadWithImageViewHolder extends ThreadViewHolder {
        final ImageView imgAttach;

        ThreadWithImageViewHolder(View view) {
            super(view);
            imgAttach = (ImageView) view.findViewById(R.id.imgAttach);
        }
    }

    private final static int NORMAL = 0, IMAGE = 1;
    private final ArrayList<Thread> threadList;
    private final Context context;

    public ThreadsAdapter(Context context, final int filter) {
        threadList = SplashCache.ThreadCache.getAllForIndex(filter);
        if (filter == -1) {
            SplashCache.ThreadCache.setFilterListener(new SplashCache.ThreadCache.OnThreadFilteredListener() {
                @Override
                public void onFilter(int position) {
                    notifyItemInserted(position);
                }
            });
        }
        this.context = context;
        SplashCache.ThreadCache.setOnThreadModifyListener(new SplashCache.ThreadCache.OnThreadModifiedListener() {
            @Override
            public void onModify(Thread thread) {
                if (thread != null) {
                    if (filter == -1 || thread.getServerIndex() == filter) {
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public ThreadsAdapter(Context context, final ArrayList<Thread> threadList) {
        this.context = context;
        this.threadList = threadList;
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(final ThreadViewHolder holder, final int position) {
        final Thread thread = threadList.get(position);
        switch (holder.getItemViewType()) {
            case IMAGE:
                ((ThreadWithImageViewHolder) holder).imgAttach.setImageBitmap(SplashCache.ImageCache.get(thread.getServerIndex(), thread.getAttachId()));
                break;
        }
        holder.title.setText(thread.getTitle());
        holder.content.setHtml(thread.getContent());
        UserIdentity user = SplashCache.UsersCache.getUser(thread.getServerIndex(), thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(UserIdentity user) {
                holder.creator.setText(user.getUsername());
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
        return threadList.get(i).getThreadId();
    }

    @Override
    public int getItemCount() {
        return threadList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (threadList.get(position).getAttachId() >= 0) {
            return IMAGE;
        } else {
            return NORMAL;
        }
    }
}
