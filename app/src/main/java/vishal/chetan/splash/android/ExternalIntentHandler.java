package vishal.chetan.splash.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.ServerList;

public class ExternalIntentHandler extends AppCompatActivity {
    private static final String REPLY_COMMENT = "vishal.chetan.splash.action.REPLY_COMMENT";
    private static final int LOGIN_CODE = 1;
    private Intent postIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            final String action = getIntent().getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                List<CharSequence> items = new ArrayList<>();
                for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                    items.add(GlobalFunctions.servers.get(i).getName());
                }
                new AlertDialog.Builder(this).setTitle("Select Server to post in...")
                        .setItems(items.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                postIntent = new Intent(ExternalIntentHandler.this, PostActivity.class).putExtra("serverIndex", i);
                                postIntent.setDataAndType(getIntent().getData(), getIntent().getType());
                                postIntent.putExtras(getIntent());
                                if (GlobalFunctions.servers.get(i).identity == null) {
                                    startActivityForResult(new Intent(ExternalIntentHandler.this, LoginActivity.class).putExtra("serverIndex", i), LOGIN_CODE);
                                } else {
                                    startActivity(postIntent);
                                    finish();
                                }
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                finish();
                            }
                        }).show();
            } else if (REPLY_COMMENT.equals(action)) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_CODE && resultCode == RESULT_OK) {
            startActivity(postIntent);
            finish();
        }
    }
}
