package vishal.chetan.splash.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;

public class ViewThreadActivity extends AppCompatActivity {
    int serverIndex;
    long threadId;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_thread);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        thread = SplashCache.ThreadCache.getThread(serverIndex, threadId);
        setTitle(thread.getTitle());
        ((TextView) findViewById(R.id.threadContent)).setText(thread.getContent());
        ((TextView) findViewById(R.id.threadCreator)).setText(SplashCache.UsernameCache.getUser(serverIndex, thread.getCreatorID()));
        ((TextView) findViewById(R.id.threadTime)).setText(DateUtils.getRelativeTimeSpanString(thread.getMtime().getTime()));
        ((TextView) findViewById(R.id.threadSubforum)).setText(Integer.toString(thread.getTopicId()));
        RecyclerView comments = (RecyclerView) findViewById(R.id.comments);
        comments.setLayoutManager(new LinearLayoutManager(this));
        comments.setAdapter(new CommentsAdapter());
        comments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView comment;
            ViewHolder(TextView view) {
                super(view);
                comment = view;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder((TextView)getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.comment.setText(thread.getComments().get(position));
        }

        @Override
        public int getItemCount() {
            return thread.getComments().size();
        }
    }
}
