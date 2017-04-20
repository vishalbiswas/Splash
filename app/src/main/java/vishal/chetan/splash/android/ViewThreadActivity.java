package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.Date;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.ThreadInfoActivity;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncArrayHelper;

public class ViewThreadActivity extends BaseActivity {
    private int serverIndex;
    private long threadId;
    private Thread thread;
    private RecyclerView comments;
    private SwipeRefreshLayout refreshLayout;

    private static final int edit_thread_code = 1;
    private static final int reply_thread_code = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_thread);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        updateThread();
        comments = (RecyclerView) findViewById(R.id.comments);
        comments.setLayoutManager(new LinearLayoutManager(this));
        comments.setAdapter(new CommentsAdapter());
        comments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        if (GlobalFunctions.identities.get(serverIndex) != null && thread.getCreatorID() == GlobalFunctions.identities.get(serverIndex).getUid()) {
            ImageButton threadEdit = (ImageButton) findViewById(R.id.threadEdit);
            threadEdit.setVisibility(View.VISIBLE);
            threadEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(ViewThreadActivity.this, PostActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId), edit_thread_code);
                }
            });
        }

        findViewById(R.id.threadInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewThreadActivity.this, ThreadInfoActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId));
            }
        });

        findViewById(R.id.threadReply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(ViewThreadActivity.this, CommentActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId), reply_thread_code);
            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshComments);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchComments();
            }
        });

        fetchComments();
    }

    private void fetchComments() {
        new AsyncArrayHelper(serverIndex, "comments/" + threadId) {
            @Override
            protected void onPostExecute(JSONArray jsonArray) {
                if (jsonArray != null) {
                    thread.clearComments();
                    for(int i = 0; i < jsonArray.length(); ++i) {
                        try {
                            JSONObject comment = jsonArray.getJSONObject(i);
                            thread.addComment(new Thread.Comment(comment.getString("text"), comment.getLong("uid"), comment.getLong("commentid"), new Date(comment.getLong("ctime")), new Date(comment.getLong("mtime")), threadId, serverIndex));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    refreshLayout.setRefreshing(false);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(comments, getString(R.string.strCommentFetchFailed), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.execute();
    }

    private void updateThread() {
        thread = SplashCache.ThreadCache.getThread(serverIndex, threadId);
        setTitle(thread.getTitle());
        ((HtmlTextView) findViewById(R.id.threadContent)).setHtml(thread.getContent());
        UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(UserIdentity user) {
                ((TextView) findViewById(R.id.threadCreator)).setText(user.getUsername());
            }
        });
        if (user == null) {
            ((TextView) findViewById(R.id.threadCreator)).setText("UID: " + thread.getCreatorID());
        }
        ((TextView) findViewById(R.id.threadTime)).setText(DateUtils.getRelativeTimeSpanString(thread.getMtime().getTime()));
        ((TextView) findViewById(R.id.threadSubforum)).setText(GlobalFunctions.servers.get(serverIndex).getTopic(thread.getTopicId()));
        ImageView imgAttach = (ImageView) findViewById(R.id.imgAttach);
        if (thread.getAttachId() >= 0) {
            imgAttach.setImageBitmap(SplashCache.ImageCache.get(serverIndex, thread.getAttachId()));
            imgAttach.setVisibility(View.VISIBLE);
        } else {
            imgAttach.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case edit_thread_code:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    updateThread();
                }
                break;
            case reply_thread_code:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyItemInserted(-1);
                }
                break;
        }
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            final HtmlTextView txtComment;
            final TextView txtCommenter;
            ViewHolder(View view) {
                super(view);
                txtComment = (HtmlTextView) view.findViewById(R.id.txtComment);
                txtCommenter = (TextView) view.findViewById(R.id.txtCommenter);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.list_item_comment, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            Thread.Comment comment = thread.getComments().valueAt(position);
            holder.txtComment.setHtml(GlobalFunctions.parseMarkdown(comment.getText()));
            UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, comment.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                @Override
                public void onGetUser(UserIdentity user) {
                    holder.txtCommenter.setText(user.getUsername());
                }
            });
            if (user == null) {
                holder.txtCommenter.setText("UID: " + comment.getCreatorID());
            }
        }

        @Override
        public int getItemCount() {
            return thread.getComments().size();
        }
    }
}
