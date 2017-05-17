package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncHelper;

public class CommentActivity extends BaseActivity {
    private int serverIndex;
    private int requestCode;
    private long threadId;
    private long commentId;
    private HtmlTextView previewPost;
    private EditText editPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        requestCode = getIntent().getIntExtra("requestCode", ViewThreadActivity.REPLY_THREAD_CODE);
        commentId = getIntent().getLongExtra("commentId", -1);
        previewPost = (HtmlTextView) findViewById(R.id.previewPost);
        editPost = (EditText) findViewById(R.id.editPost);
        editPost.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                updatePreview();
                return false;
            }
        });

        if (commentId != -1) {
            Thread.Comment comment = SplashCache.ThreadCache.getThread(serverIndex, threadId).getComment(commentId);
            if (comment.isBlocked()) {
                // cannot edit or reply to blocked comments
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            if (requestCode == ViewThreadActivity.MODIFY_COMMENT_CODE) {
                // editing a comment
                editPost.setText(comment.getText());
                updatePreview();
                if (comment.getParentCommentId() != -1) {
                    updateParent(SplashCache.ThreadCache.getThread(serverIndex, threadId).getComment(comment.getParentCommentId()));
                }
            } else {
                updateParent(comment);
            }
        }

        final Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editPost.getText().toString().trim().isEmpty()) {
                    btnSubmit.setEnabled(false);
                    UserIdentity user = GlobalFunctions.servers.get(serverIndex).identity;
                    final Thread.Comment comment = new Thread.Comment(serverIndex, threadId, editPost.getText().toString().trim());
                    String path = "comment/";
                    String postMessage = String.format("sessionid=%s&content=%s&threadid=%s", user.getSessionid(), comment.getText(), threadId);
                    if (requestCode == ViewThreadActivity.REPLY_THREAD_CODE) {
                        path += "new";
                    } else if (requestCode == ViewThreadActivity.REPLY_COMMENT_CODE) {
                        path += "reply";
                        postMessage += "&commentid=" + commentId;
                    } else if (requestCode == ViewThreadActivity.MODIFY_COMMENT_CODE) {
                        path += "edit";
                        postMessage += "&commentid=" + commentId;
                    }
                    new AsyncHelper(serverIndex, path, postMessage) {
                        @Override
                        protected void onPostExecute(@Nullable JSONObject jsonObject) {
                            if (jsonObject != null) {
                                try {
                                    switch (jsonObject.getInt("status")) {
                                        case 0:
                                            Thread.Comment finalComment = new Thread.Comment(serverIndex, threadId, comment.getCreatorID(), comment.getText(), jsonObject.getLong("commentid"), jsonObject.getLong("ctime"), jsonObject.getLong("mtime"));
                                            if (jsonObject.has("parent")) {
                                                finalComment.setParentCommentId(jsonObject.getLong("parent"));
                                            }
                                            SplashCache.ThreadCache.getThread(serverIndex, threadId).addComment(finalComment);
                                            setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("commentId", finalComment.getCommentId()));
                                            finish();
                                            break;
                                        case 1:
                                            Snackbar.make(previewPost, R.string.errInvalidSession, Snackbar.LENGTH_LONG).show();
                                            break;
                                        case 2:
                                            Snackbar.make(previewPost, R.string.errCommentNotFound, Snackbar.LENGTH_LONG).show();
                                            break;
                                        default:
                                            Snackbar.make(previewPost, R.string.errUnknown, Snackbar.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            btnSubmit.setEnabled(true);
                        }
                    }.execute();
                }
            }
        });
    }

    private void updatePreview() {
        previewPost.setHtml(GlobalFunctions.parseMarkdown(editPost.getText().toString()));
    }

    private void updateParent(Thread.Comment comment) {
        HtmlTextView parentComment = (HtmlTextView) findViewById(R.id.parentComment);
        parentComment.setVisibility(View.VISIBLE);
        UserIdentity user = SplashCache.UsersCache.getUser(comment.getServerIndex(), comment.getCreatorID(), null);
        String username;
        if (user == null) {
            username = "UID: " + comment.getCreatorID();
        } else {
            username = user.getUsername();
        }
        String text = String.format("<i>%s wrote<i><break /><blockquote>%s</blockquote>", username, comment.getText());
        parentComment.setHtml(text);
    }
}
