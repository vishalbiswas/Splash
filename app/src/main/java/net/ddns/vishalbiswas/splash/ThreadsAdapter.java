package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

class ThreadsAdapter extends BaseAdapter {

    private ArrayList<Thread> threadLists = new ArrayList<>();
    private LayoutInflater inflater;

    ThreadsAdapter (Activity activity) {
        inflater = activity.getLayoutInflater();

        //// FIXME: 3/13/17 demo code to be removed
        addThread(new Thread(0, "Hello", "Welcome to Splash app! Have fun!", GlobalFunctions.identities.get(0).getUid(), new Date(), new Date()));
    }

    void addThread(Thread thread) {
        threadLists.add(thread);
    }

    @Override
    public int getCount() {
        return threadLists.size();
    }

    @Override
    public Object getItem(int i) {
        return threadLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ThreadViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.threads_list_item, null);

            holder = new ThreadViewHolder();
            holder.title = (TextView) view.findViewById(R.id.threadTitle);
            holder.content = (TextView) view.findViewById(R.id.threadPreview);
            holder.creator = (TextView) view.findViewById(R.id.creator);
            holder.mtime = (TextView) view.findViewById(R.id.mtime);

            view.setTag(holder);
        } else {
            holder = (ThreadViewHolder) view.getTag();
        }

        if (getCount() > 0) {
            Thread thread = threadLists.get(i);

            holder.title.setText(thread.getTitle());
            holder.content.setText(thread.getContent());
            holder.creator.setText(UsernameCache.getUser(handler, thread.getServerIndex(), thread.getCreatorID()));
            holder.mtime.setText(thread.getMtime().toString());
        }

        return view;
    }

    private static class ThreadViewHolder {
        TextView title;
        TextView content;
        TextView creator;
        TextView mtime;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 0) {
                Log.e("Splash", "Unable to get Username for UID:" + msg.what);
            }
            else if (msg.what < 0) {
                Log.e("Splash", "Error parsing server response.");
            }
            else {
                notifyDataSetChanged();
            }
        }
    };
}
