package vishal.chetan.splash.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
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

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        updateThread();
        comments = (RecyclerView) findViewById(R.id.comments);
        comments.setLayoutManager(new LinearLayoutManager(this));
        comments.setAdapter(new CommentsAdapter());
        comments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        if (GlobalFunctions.identities.get(serverIndex) != null) {
            if (thread.getCreatorID() == GlobalFunctions.identities.get(serverIndex).getUid()) {
                View threadEdit = findViewById(R.id.threadEdit);
                threadEdit.setVisibility(View.VISIBLE);
                threadEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(ViewThreadActivity.this, PostActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId), edit_thread_code);
                    }
                });
            }

            View threadReply = findViewById(R.id.threadReply);
            threadReply.setVisibility(View.VISIBLE);
            threadReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(ViewThreadActivity.this, CommentActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId), reply_thread_code);
                }
            });
        }

        findViewById(R.id.threadInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewThreadActivity.this, ThreadInfoActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId));
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
            protected void workInBackground(@Nullable JSONArray jsonArray) {
                if (jsonArray != null) {
                    thread.clearComments();
                    for(int i = 0; i < jsonArray.length(); ++i) {
                        try {
                            JSONObject comment = jsonArray.getJSONObject(i);
                            thread.addComment(new Thread.Comment(serverIndex, threadId, comment.getLong("author"), comment.getString("content"), comment.getLong("commentid"), comment.getLong("ctime"), comment.getLong("mtime")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(comments, getString(R.string.strCommentFetchFailed), Snackbar.LENGTH_LONG).show();
                            refreshLayout.setRefreshing(false);
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
            public void onGetUser(@NonNull UserIdentity user) {
                ((TextView) findViewById(R.id.threadCreator)).setText(user.getUsername());
            }
        });
        if (user == null) {
            ((TextView) findViewById(R.id.threadCreator)).setText("UID: " + thread.getCreatorID());
        }
        ((TextView) findViewById(R.id.threadTime)).setText(DateUtils.getRelativeTimeSpanString(thread.getMtime().getTime()));
        ((TextView) findViewById(R.id.threadSubforum)).setText(GlobalFunctions.servers.get(serverIndex).getTopic(thread.getTopicId()));
        final ImageView imgAttach = (ImageView) findViewById(R.id.imgAttach);
        if (thread.getAttachId() >= 0) {
            SplashCache.ImageCache.get(serverIndex, thread.getAttachId(), new SplashCache.ImageCache.OnGetImageListener() {
                @Override
                public void onGetImage(final Bitmap image) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgAttach.setImageBitmap(image);
                        }
                    });
                }
            });
            imgAttach.setVisibility(View.VISIBLE);
        } else {
            imgAttach.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        switch (requestCode) {
            case edit_thread_code:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    updateThread();
                }
                break;
            case reply_thread_code:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyItemInserted(thread.getComments().size()-1);
                }
                break;
        }
    }

    class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            @NonNull
            final HtmlTextView txtComment;
            @NonNull
            final TextView txtCommenter;
            ViewHolder(@NonNull View view) {
                super(view);
                txtComment = (HtmlTextView) view.findViewById(R.id.txtComment);
                txtCommenter = (TextView) view.findViewById(R.id.txtCommenter);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.list_item_comment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            Thread.Comment comment = thread.getComments().valueAt(position);
            holder.txtComment.setHtml(GlobalFunctions.parseMarkdown(comment.getText()));
            UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, comment.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                @Override
                public void onGetUser(@NonNull UserIdentity user) {
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

        @Override
        public long getItemId(int position) {
            return thread.getComments().valueAt(position).getCommentId();
        }
    }
}
