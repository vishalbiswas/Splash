package vishal.chetan.splash.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.Date;
import java.util.Random;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;

public class CommentActivity extends BaseActivity {
    int serverIndex;
    long threadId;
    HtmlTextView previewPost;
    EditText editPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);
        previewPost =  (HtmlTextView) findViewById(R.id.previewPost);
        editPost = (EditText) findViewById(R.id.editPost);
        editPost.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                updatePreview();
                return false;
            }
        });

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editPost.getText().toString().isEmpty()) {
                    Thread.Comment comment;
                    if (getIntent().getLongExtra("commentId", -1) != -1) {
                        comment = SplashCache.ThreadCache.getThread(serverIndex, threadId).getComment(getIntent().getLongExtra("commentId", -1));
                        comment.setMtime(new Date());
                        comment.setText(editPost.getText().toString());
                        SplashCache.ThreadCache.getThread(serverIndex, threadId).setComment(comment);
                    } else {
                        comment = new Thread.Comment(editPost.getText().toString(), GlobalFunctions.identities.get(serverIndex).getUid(), new Random(new Date().getTime()).nextLong(), new Date(), new Date(), threadId, serverIndex);
                        SplashCache.ThreadCache.getThread(serverIndex, threadId).addComment(comment);
                    }
                    setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", threadId).putExtra("commentId", comment.getCommentId()));
                    finish();
                }
            }
        });

        if (getIntent().getLongExtra("commentId", -1) != -1) {
            Thread.Comment comment = SplashCache.ThreadCache.getThread(serverIndex, threadId).getComment(getIntent().getLongExtra("commentId", -1));
            editPost.setText(comment.getText());
            setTitle(getString(R.string.strEditComment));
            updatePreview();
        }
    }

    private void updatePreview() {
        previewPost.setHtml(GlobalFunctions.parseMarkdown(editPost.getText().toString()));
    }
}
