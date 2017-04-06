package vishal.chetan.splash;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ThreadsAdapter extends RecyclerView.Adapter<ThreadsAdapter.ThreadViewHolder> {
    static class ThreadViewHolder extends RecyclerView.ViewHolder {
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

    public ThreadsAdapter(int filter) {
        threadList = SplashCache.ThreadCache.getAllForIndex(filter);
    }

    @Override
    public ThreadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ThreadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_thread, parent, false));
    }

    @Override
    public void onBindViewHolder(final ThreadViewHolder holder, final int position) {
        final Thread thread = threadList.get(position);
        holder.title.setText(thread.getTitle());
        holder.content.setText(thread.getContent());
        String name = SplashCache.UsernameCache.getUser(thread.getServerIndex(), thread.getCreatorID());
        if (name == null) {
            SplashCache.UsernameCache.addGetUserListener(thread.getServerIndex(), new SplashCache.UsernameCache.OnGetUserListener() {
                @Override
                public void onGetUser(int uid) {
                    if (uid == thread.getCreatorID()) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }
            });
            name = "UID:" + thread.getCreatorID();
        }
        holder.creator.setText(name);
        holder.mtime.setText(thread.getMtime().toString());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return threadList.size();
    }

}
