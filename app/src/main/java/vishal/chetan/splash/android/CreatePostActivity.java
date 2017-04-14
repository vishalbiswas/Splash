package vishal.chetan.splash.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.Random;

import javax.microedition.khronos.opengles.GL;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;

public class CreatePostActivity extends AppCompatActivity {
    private int serverIndex;
    TextView previewPost;
    EditText editPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        previewPost =  (TextView) findViewById(R.id.previewPost);
        editPost = (EditText) findViewById(R.id.editPost);
        editPost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                updatePreview();
            }
        });
        final EditText editPostTitle = (EditText) findViewById(R.id.editPostTitle);
        final Spinner spinTopic = (Spinner) findViewById(R.id.spinTopic);
        spinTopic.setAdapter(new TopicAdapter(serverIndex));

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(editPostTitle.getText().toString().isEmpty() || editPost.getText().toString().isEmpty())) {
                    Thread thread = new Thread(new Random(new Date().getTime()).nextLong(), serverIndex, editPostTitle.getText().toString(), editPost.getText().toString(), GlobalFunctions.identities.get(serverIndex).getUid(), new Date(), new Date(), (int) spinTopic.getSelectedItemId());
                    SplashCache.ThreadCache.add(thread);
                    setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", thread.getThreadId()));
                    finish();
                }
            }
        });
    }

    private void updatePreview() {
        previewPost.setText(GlobalFunctions.mdBypass.markdownToSpannable(editPost.getText().toString()));
    }

    private class TopicAdapter extends ArrayAdapter<String> {
        int serverIndex;

        TopicAdapter(int serverIndex) {
            super(CreatePostActivity.this, android.R.layout.simple_spinner_dropdown_item, GlobalFunctions.servers.get(serverIndex).getTopics());
            this.serverIndex = serverIndex;
        }

        @Override
        public int getCount() {
            return GlobalFunctions.servers.get(serverIndex).topics.size();
        }

        @Override
        public String getItem(int position) {
            return GlobalFunctions.servers.get(serverIndex).topics.get(position).name;
        }

        @Override
        public long getItemId(int position) {
            return GlobalFunctions.servers.get(serverIndex).topics.get(position).topicid;
        }
    }
}
