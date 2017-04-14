package vishal.chetan.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import vishal.chetan.splash.android.ViewThreadActivity;

public class ThreadsAdapter extends RecyclerView.Adapter<ThreadsAdapter.ThreadViewHolder> {
    class ThreadViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView content;
        final TextView creator;
        final TextView mtime;

        ThreadViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.threadTitle);
            content = (TextView) view.findViewById(R.id.threadPreview);
            creator = (TextView) view.findViewById(R.id.creator);
            mtime = (TextView) view.findViewById(R.id.mtime);
        }
    }

    private final ArrayList<Thread> threadList;
    private final Context context;

    public ThreadsAdapter(Context context, int filter) {
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
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThreadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread, parent, false));
    }

    @Override
    public void onBindViewHolder(final ThreadViewHolder holder, final int position) {
        final Thread thread = threadList.get(position);
        holder.title.setText(thread.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.content.setText(Html.fromHtml(thread.getContent(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.content.setText(Html.fromHtml(thread.getContent()));
        }
        String name = SplashCache.UsernameCache.getUser(thread.getServerIndex(), thread.getCreatorID());
        if (name == null) {
            SplashCache.UsernameCache.addGetUserListener(thread.getServerIndex(), new SplashCache.UsernameCache.OnGetUserListener() {
                @Override
                public void onGetUser(long uid) {
                    if (uid == thread.getCreatorID()) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }
            });
            name = "UID:" + thread.getCreatorID();
        }
        holder.creator.setText(name);
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

}
