package vishal.chetan.splash.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.ModerationManager;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.UserIdentity;

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
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshComments);
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

        if (thread.isHidden()) {
            (findViewById(R.id.threadBG)).setBackgroundColor(getResources().getColor(R.color.hidden));
        } else if (thread.isBlocked()) {
            (findViewById(R.id.threadBG)).setBackgroundColor(getResources().getColor(R.color.locked));
        } else if (thread.reported > 0 && GlobalFunctions.servers.get(serverIndex).identity.getMod() > 0) {
            (findViewById(R.id.threadBG)).setBackgroundColor(getResources().getColor(R.color.reported));
        }
        findViewById(R.id.threadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ArrayList<CharSequence> items = new ArrayList<>();
                if (GlobalFunctions.servers.get(serverIndex).identity != null) {
                    if (GlobalFunctions.servers.get(serverIndex).identity != null && GlobalFunctions.servers.get(serverIndex).identity.getMod() > UserIdentity.MODERATOR_NONE) {
                        items.add("Moderate");
                    } else {
                        items.add("Report");
                    }
                }
                CharSequence dummy = "@UID: " + thread.getCreatorID();
                items.add(dummy);
                SplashCache.UsersCache.getUser(serverIndex, thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                    @Override
                    public void onGetUser(@NonNull final UserIdentity user) {
                        items.set(items.size() - 1, user.getUsername());
                    }
                });
                new AlertDialog.Builder(ViewThreadActivity.this).setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String choice = items.get(i).toString();
                        if (choice.equals("Report")) {
                            final EditText msgReport = new EditText(ViewThreadActivity.this);
                            new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Report message (Optional)").setView(msgReport).setPositiveButton("Report", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ModerationManager.reportThread(serverIndex, threadId, msgReport.getText().toString());
                                }
                            }).show();
                        } else if (choice.equals("Moderate")) {
                            final ArrayList<CharSequence> list = new ArrayList<CharSequence>();
                            if (thread.isBlocked()) {
                                list.add("Unlock");
                            } else {
                                list.add("Lock");
                            }
                            if (thread.isHidden()) {
                                list.add("Unhide");
                            } else {
                                list.add("Hide");
                            }
                            new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Select Action").setItems(list.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, final int i) {
                                    if (list.get(i).toString().startsWith("Un")) {
                                        String choice = list.get(i).toString();
                                        if (choice.equals("Lock")) {
                                            ModerationManager.unlockThread(serverIndex, threadId);
                                        } else if (choice.equals("Hide")) {
                                            ModerationManager.unhideThread(serverIndex, threadId);
                                        }
                                    } else {
                                        final EditText msgModerate = new EditText(ViewThreadActivity.this);
                                        new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Action message (Optional)").setView(msgModerate).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int j) {
                                                String choice = list.get(i).toString();
                                                if (choice.equals("Lock")) {
                                                    ModerationManager.lockThread(serverIndex, threadId, msgModerate.getText().toString());
                                                } else if (choice.equals("Hide")) {
                                                    ModerationManager.hideThread(serverIndex, threadId, msgModerate.getText().toString());
                                                }
                                            }
                                        }).show();
                                    }
                                }
                            }).show();
                        } else if (i == items.size() - 1) {
                            // poster's name selected
                            startActivity(new Intent(ViewThreadActivity.this, ProfileActivity.class).putExtra("serverIndex", serverIndex).putExtra("uid", thread.getCreatorID()));
                        }
                    }
                }).show();
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchComments();
            }
        });

        fetchComments();
    }

    public void fetchComments() {
        thread.loadComments(new Thread.LoadCommentsListener() {
            @Override
            public void onCommentsLoaded(boolean result) {
                if (result) {
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
        });
    }

    private void updateThread() {
        thread = SplashCache.ThreadCache.getThread(serverIndex, threadId, null);
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
        if (thread.getAttachId() >= 0) {
            SplashCache.AttachmentCache.get(serverIndex, thread.getAttachId(), new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                @Override
                public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                    if (attachment != null && attachment.data != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (attachment.type) {
                                    case SplashCache.AttachmentCache.SplashAttachment.IMAGE:
                                        final ImageView imgAttach = (ImageView) findViewById(R.id.imgAttach);
                                        imgAttach.setImageBitmap((Bitmap) attachment.data);
                                        imgAttach.setVisibility(View.VISIBLE);
                                        imgAttach.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openInExternal(attachment);
                                            }
                                        });
                                        break;
                                    default:
                                        Drawable icon;
                                        switch (attachment.type) {
                                            case SplashCache.AttachmentCache.SplashAttachment.VIDEO:
                                                icon = getResources().getDrawable(R.drawable.ic_video);
                                                break;
                                            case SplashCache.AttachmentCache.SplashAttachment.AUDIO:
                                                icon = getResources().getDrawable(R.drawable.ic_audio);
                                                break;
                                            default:
                                                icon = getResources().getDrawable(R.drawable.ic_file);
                                                break;
                                        }
                                        LinearLayout threadAttach = (LinearLayout) findViewById(R.id.threadAttach);
                                        ((ImageView) threadAttach.findViewById(R.id.threadAttachIcon)).setImageDrawable(icon);
                                        ((TextView) threadAttach.findViewById(R.id.threadDummy)).setText(attachment.name);
                                        threadAttach.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openInExternal(attachment);
                                            }
                                        });
                                        threadAttach.setVisibility(View.VISIBLE);
                                        break;
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void openInExternal(SplashCache.AttachmentCache.SplashAttachment attachment) {
        if (attachment != null && attachment.data != null) {
            try {
                File file = new File(getFilesDir(), GlobalFunctions.servers.get(serverIndex).getName() + thread.getAttachId() + attachment.name);
                FileOutputStream out = new FileOutputStream(file);
                if (attachment.type == SplashCache.AttachmentCache.SplashAttachment.IMAGE) {
                    ((Bitmap) attachment.data).compress(Bitmap.CompressFormat.JPEG, 100, out);
                } else {
                    out.write((byte[])attachment.data);
                }
                out.close();
                Intent shareIntent = new Intent();
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setAction(Intent.ACTION_VIEW);
                shareIntent.setDataAndType(FileProvider.getUriForFile(ViewThreadActivity.this, getApplicationContext().getPackageName() + ".provider", file), attachment.mimeType);
                startActivity(shareIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    comments.getAdapter().notifyItemInserted(thread.getComments().size() - 1);
                }
                break;
            case REPLY_COMMENT_CODE:
                if (resultCode == RESULT_OK && data.getIntExtra("serverIndex", -1) == serverIndex && data.getLongExtra("threadId", -1) == threadId) {
                    comments.getAdapter().notifyItemInserted(thread.getComments().size() - 1);
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
            if (comment.isHidden()) {
                holder.setIsRecyclable(false);
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.hidden));
            } else if (comment.isBlocked()) {
                holder.setIsRecyclable(false);
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.locked));
            } else if (comment.reported > 0 && GlobalFunctions.servers.get(serverIndex).identity.getMod() > 0) {
                holder.setIsRecyclable(false);
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.reported));
            }
            holder.txtComment.setHtml(GlobalFunctions.parseMarkdown(comment.getText()));
            if (comment.getParentCommentId() != -1) {
                CharSequence dummy = "@UID: " + thread.getComment(comment.getParentCommentId()).getCreatorID();
                items.add(dummy);
                UserIdentity parentAuthor = SplashCache.UsersCache.getUser(serverIndex, thread.getComment(comment.getParentCommentId()).getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                    @Override
                    public void onGetUser(@NonNull final UserIdentity user) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.parentAuthor.setText("@" + user.getUsername());
                            }
                        });
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
                if (GlobalFunctions.servers.get(serverIndex).identity != null && GlobalFunctions.servers.get(serverIndex).identity.getMod() > UserIdentity.MODERATOR_NONE) {
                    items.add("Moderate");
                } else {
                    items.add("Report");
                }
            }
            CharSequence dummy = "@UID: " + comment.getCreatorID();
            items.add(dummy);
            UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, comment.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
                @Override
                public void onGetUser(@NonNull final UserIdentity user) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.txtCommenter.setText(user.getUsername());
                        }
                    });
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
                            } else if (choice.equals("Report")) {
                                final EditText msgReport = new EditText(ViewThreadActivity.this);
                                new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Report message (Optional)").setView(msgReport).setPositiveButton("Report", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ModerationManager.reportComment(serverIndex, comment.getCommentId(), msgReport.getText().toString());
                                    }
                                }).show();
                            } else if (choice.equals("Moderate")) {
                                final ArrayList<CharSequence> list = new ArrayList<CharSequence>();
                                if (comment.isBlocked()) {
                                    list.add("Unlock");
                                } else {
                                    list.add("Lock");
                                }
                                if (comment.isHidden()) {
                                    list.add("Unhide");
                                } else {
                                    list.add("Hide");
                                }
                                new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Select Action").setItems(list.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, final int i) {
                                        if (list.get(i).toString().startsWith("Un")) {
                                            String choice = list.get(i).toString();
                                            if (choice.equals("Lock")) {
                                                ModerationManager.unlockThread(serverIndex, threadId);
                                            } else if (choice.equals("Hide")) {
                                                ModerationManager.unhideThread(serverIndex, threadId);
                                            }
                                        } else {
                                            final EditText msgModerate = new EditText(ViewThreadActivity.this);
                                            new AlertDialog.Builder(ViewThreadActivity.this).setTitle("Action message (Optional)").setView(msgModerate).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int j) {
                                                    String choice = list.get(i).toString();
                                                    if (choice.equals("Lock")) {
                                                        ModerationManager.lockComment(serverIndex, comment.getCommentId(), msgModerate.getText().toString());
                                                    } else if (choice.equals("Hide")) {
                                                        ModerationManager.hideComment(serverIndex, comment.getCommentId(), msgModerate.getText().toString());
                                                    }
                                                }
                                            }).show();
                                        }
                                    }
                                }).show();
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
