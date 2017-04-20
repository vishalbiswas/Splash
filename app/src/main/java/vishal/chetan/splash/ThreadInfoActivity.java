package vishal.chetan.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import vishal.chetan.splash.android.BaseActivity;
import vishal.chetan.splash.android.ProfileActivity;

public class ThreadInfoActivity extends BaseActivity {
    private int serverIndex = -1;
    private long threadId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        threadId = getIntent().getLongExtra("threadId", -1);

        setContentView(R.layout.activity_thread_info);
        final Thread thread = SplashCache.ThreadCache.getThread(serverIndex, threadId);

        ((TextView) findViewById(R.id.txtTitle)).setText(thread.getTitle());
        ((TextView) findViewById(R.id.txtServer)).setText(GlobalFunctions.servers.get(serverIndex).getName());
        ((TextView) findViewById(R.id.txtSubForum)).setText(GlobalFunctions.servers.get(serverIndex).getTopic(thread.getTopicId()));
        final TextView author = (TextView) findViewById(R.id.txtAuthor);
        UserIdentity user = SplashCache.UsersCache.getUser(serverIndex, thread.getCreatorID(), new SplashCache.UsersCache.OnGetUserListener() {
            @Override
            public void onGetUser(UserIdentity user) {
                author.setText(user.getUsername());
            }
        });
        if (user == null) {
            author.setText("UID: " + thread.getCreatorID());
        }
        ((TextView) findViewById(R.id.txtCreated)).setText(thread.getCtime().toString());
        ((TextView) findViewById(R.id.txtModified)).setText(thread.getMtime().toString());
        ((TextView) findViewById(R.id.txtID)).setText(Long.toString(thread.getThreadId()));
        ((TextView) findViewById(R.id.txtComments)).setText(Integer.toString(thread.getComments().size()));

        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThreadInfoActivity.this, ProfileActivity.class).putExtra("serverIndex", serverIndex).putExtra("uid", thread.getCreatorID()));
            }
        });
    }
}
