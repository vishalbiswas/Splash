package vishal.chetan.splash.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

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

    public static final int EDIT_THREAD_CODE = 1;
    public static final int REPLY_THREAD_CODE = 2;
    public static final int REPLY_COMMENT_CODE = 3;
    public static final int MODIFY_COMMENT_CODE = 4;

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

        if (GlobalFunctions.servers.get(serverIndex).identity != null) {
            if (thread.getCreatorID() == GlobalFunctions.servers.get(serverIndex).identity.getUid()) {
                View threadEdit = findViewById(R.id.threadEdit);
                threadEdit.setVisibility(View.VISIBLE);
                threadEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(new Intent(ViewThreadActivity.this, PostActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId), EDIT_THREAD_CODE);
                    }
                });
            }

            View threadReply = findViewById(R.id.threadReply);
            threadReply.setVisibility(View.VISIBLE);
            threadReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(ViewThreadActivity.this, CommentActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("requestCode", REPLY_THREAD_CODE), REPLY_THREAD_CODE);
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
                            JSONObject commentJSON = jsonArray.getJSONObject(i);
                            Thread.Comment comment = new Thread.Comment(serverIndex, threadId, commentJSON.getLong("author"), commentJSON.getString("content"), commentJSON.getLong("commentid"), commentJSON.getLong("ctime"), commentJSON.getLong("mtime"));
                            if (commentJSON.has("blocked")) {
                                comment.setBlocked(commentJSON.getBoolean("blocked"));
                            }
                            if (commentJSON.has("hidden")) {
                                comment.setHidden(commentJSON.getBoolean("hidden"));
                            }
                            if (commentJSON.has("parent")) {
                                comment.setParentCommentId(commentJSON.getLong("parent"));
                            }
                            if (comment.canShow()) {
                                thread.addComment(comment);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                            comments.getAdapter().notifyDataSetChanged();
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
            case EDIT_THREAD_CODE:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    updateThread();
                }
                break;
            case REPLY_THREAD_CODE:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyItemInserted(thread.getComments().size()-1);
                }
                break;
            case REPLY_COMMENT_CODE:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyItemInserted(thread.getComments().size()-1);
                }
                break;
            case MODIFY_COMMENT_CODE:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyDataSetChanged();
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
            @NonNull
            final TextView parentAuthor;

            ViewHolder(@NonNull View view) {
                super(view);
                txtComment = (HtmlTextView) view.findViewById(R.id.txtComment);
                txtCommenter = (TextView) view.findViewById(R.id.txtCommenter);
                parentAuthor = (TextView) view.findViewById(R.id.parentAuthor);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.list_item_comment, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final Thread.Comment comment = thread.getComments().valueAt(position);
            final ArrayList<CharSequence> items = new ArrayList<>();
            holder.txtComment.setHtml(GlobalFunctions.parseMarkdown(comment.getText()));
            if (comment.getParentCommentId() != -1) {
                CharSequence dummy = "@UID: " + thread.getComment(comment.getParentCommentId()).getCreatorID();
                items.add(dummy);
                UserIdentity parentAuthor = SplashCache.UsersCache.getUser(serverIndex, thread.getComment(comment.getParentCommentId()).getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                    @Override
                    public void onGetUser(@NonNull UserIdentity user) {
                        holder.parentAuthor.setText("@" + user.getUsername());
                        items.set(0, "@" + user.getUsername());
                    }
                });
                if (parentAuthor == null) {
                    holder.parentAuthor.setText(dummy);
                }
                holder.parentAuthor.setVisibility(View.VISIBLE);
            }
            if (GlobalFunctions.servers.get(serverIndex).identity != null) {
                items.add("Reply");
                if (comment.getCreatorID() == GlobalFunctions.servers.get(serverIndex).identity.getUid()) {
                    items.add("Edit");
                }
            }
            items.add("Report");
            CharSequence dummy = "@UID: " + comment.getCreatorID();
            items.add(dummy);
            UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, comment.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                @Override
                public void onGetUser(@NonNull UserIdentity user) {
                    holder.txtCommenter.setText(user.getUsername());
                    items.set(items.size() - 1, user.getUsername());
                }
            });
            if (user == null) {
                holder.txtCommenter.setText("UID: " + comment.getCreatorID());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(ViewThreadActivity.this).setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String choice = items.get(i).toString();
                            if (choice.equals("Reply")) {
                                startActivityForResult(new Intent(ViewThreadActivity.this, CommentActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("commentId", comment.getCommentId()).putExtra("requestCode", REPLY_COMMENT_CODE), REPLY_COMMENT_CODE);
                            } else if (choice.equals("Edit")) {
                                startActivityForResult(new Intent(ViewThreadActivity.this, CommentActivity.class).putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("commentId", comment.getCommentId()).putExtra("requestCode", MODIFY_COMMENT_CODE), MODIFY_COMMENT_CODE);
                            } else if (i == 0) {
                                // parent author name selected
                                startActivity(new Intent(ViewThreadActivity.this, ProfileActivity.class).putExtra("serverIndex", serverIndex).putExtra("uid", thread.getComment(comment.getParentCommentId()).getCreatorID()));
                            } else if (i == items.size() - 1) {
                                // commenter's name selected
                                startActivity(new Intent(ViewThreadActivity.this, ProfileActivity.class).putExtra("serverIndex", serverIndex).putExtra("uid", comment.getCreatorID()));
                            }
                        }
                    }).show();
                }
            });
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
