package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;
import vishal.chetan.splash.asyncs.AsyncHelper;

public class CommentActivity extends BaseActivity {
    private int serverIndex;
    private long threadId;
    private HtmlTextView previewPost;
    private EditText editPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        previewPost = (HtmlTextView) findViewById(R.id.previewPost);
        editPost = (EditText) findViewById(R.id.editPost);
        editPost.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                updatePreview();
                return false;
            }
        });

        final Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editPost.getText().toString().trim().isEmpty()) {
                    btnSubmit.setEnabled(false);
                    final Thread.Comment comment = new Thread.Comment(serverIndex, threadId, GlobalFunctions.identities.get(serverIndex).getUid(), editPost.getText().toString().trim());
                    String postMessage = String.format("author=%s&content=%s", comment.getCreatorID(), comment.getText());
                    new AsyncHelper(serverIndex, "comment/" + threadId, postMessage) {
                        @Override
                        protected void onPostExecute(@Nullable JSONObject jsonObject) {
                            if (jsonObject != null) {
                                try {
                                    Thread.Comment finalComment = new Thread.Comment(serverIndex, threadId, comment.getCreatorID(), comment.getText(), jsonObject.getLong("commentid"), GlobalFunctions.parseDate(jsonObject.getString("ctime")), GlobalFunctions.parseDate(jsonObject.getString("mtime")));
                                    SplashCache.ThreadCache.getThread(serverIndex, threadId).addComment(finalComment);
                                    setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("commentId", finalComment.getCommentId()));
                                    finish();
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
}
