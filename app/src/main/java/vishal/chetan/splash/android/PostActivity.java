package vishal.chetan.splash.android;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;

public class PostActivity extends BaseActivity {
    private int serverIndex;
    private static final int attachRequesCode = 1;
    private static final int permissionRequestCode = 2;
    private HtmlTextView previewPost;
    private EditText editPost;
    private Button btnImage;
    @Nullable
    private SplashCache.AttachmentCache.SplashAttachment attach = null;
    private long attachid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!GlobalFunctions.servers.get(serverIndex).identity.canPost()) {
            Toast.makeText(this, R.string.errCannotPost, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        if (getIntent().getLongExtra("threadId", -1) != -1) {
            if (SplashCache.ThreadCache.getThread(serverIndex, getIntent().getLongExtra("threadId", -1), null).isBlocked()) {
                Toast.makeText(this, R.string.errCannotPost, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }
        setContentView(R.layout.activity_post);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        previewPost = (HtmlTextView) findViewById(R.id.previewPost);
        editPost = (EditText) findViewById(R.id.editPost);
        editPost.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                updatePreview();
                return false;
            }
        });
        final EditText editPostTitle = (EditText) findViewById(R.id.editPostTitle);
        final Spinner spinTopic = (Spinner) findViewById(R.id.spinTopic);
        spinTopic.setAdapter(new TopicAdapter(serverIndex));

        btnImage = (Button) findViewById(R.id.btnImage);

        if (getIntent().getType() != null) {
            if (getIntent().getType().equals("text/plain")) {
                editPost.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
            } else {
                attachImage((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            }
        }

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PostActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(PostActivity.this).setMessage(R.string.strPermImage).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                ActivityCompat.requestPermissions(PostActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        permissionRequestCode);
                            }
                        }).show();
                    } else {
                        ActivityCompat.requestPermissions(PostActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                permissionRequestCode);
                    }
                } else {
                    getImage();
                }
            }
        });

        final Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        final SplashCache.ThreadCache.OnThreadModifiedListener modifiedListener = new SplashCache.ThreadCache.OnThreadModifiedListener() {
            @Override
            public void onModify(@Nullable final Thread thread) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSubmit.setEnabled(true);
                        if (thread != null) {
                            Intent launchIntent = new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", thread.getThreadId());
                            setResult(RESULT_OK, launchIntent);
                            startActivity(new Intent(PostActivity.this, ViewThreadActivity.class).putExtra("serverIndex", serverIndex).putExtras(launchIntent));
                            finish();
                        } else {
                            Snackbar.make(btnSubmit, getString(R.string.errPost), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                if (!(editPostTitle.getText().toString().isEmpty() || editPost.getText().toString().isEmpty())) {
                    final Thread thread;
                    v.setEnabled(false);
                    SplashCache.ThreadCache.postListener = modifiedListener;
                    if (getIntent().getLongExtra("threadId", -1) != -1) {
                        thread = SplashCache.ThreadCache.getThread(serverIndex, getIntent().getLongExtra("threadId", -1), null);
                        thread.setMtime(new Date());
                        thread.setTitle(editPostTitle.getText().toString());
                        thread.setContent(editPost.getText().toString());
                        thread.setTopicId((int) spinTopic.getSelectedItemId());
                        if (attach != null) {
                            SplashCache.AttachmentCache.upload(serverIndex, attach, new SplashCache.AttachmentCache.OnUploadCompleteListener() {
                                @Override
                                public void onUpload(long attachId) {
                                    if (attachId < 0) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                v.setEnabled(true);
                                                Snackbar.make(btnSubmit, R.string.errAttach, Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        thread.setAttachId(attachId, attach.getMimeType());
                                        SplashCache.ThreadCache.set(thread);
                                    }
                                }
                            });
                        } else {
                            thread.setAttachId(attachid, attach.getMimeType());
                            SplashCache.ThreadCache.set(thread);
                        }
                    } else {
                        thread = new Thread(serverIndex, editPostTitle.getText().toString(), editPost.getText().toString(), (int) spinTopic.getSelectedItemId());
                        if (attach != null) {
                            thread.setAttachName(attach.name);
                            SplashCache.AttachmentCache.upload(serverIndex, attach, new SplashCache.AttachmentCache.OnUploadCompleteListener() {
                                @Override
                                public void onUpload(long attachId) {
                                    if (attachId < 0) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                v.setEnabled(true);
                                                Snackbar.make(btnSubmit, R.string.errAttach, Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    } else {
                                        thread.setAttachId(attachId, attach.getMimeType());
                                        SplashCache.ThreadCache.create(thread);
                                    }
                                }
                            });
                        } else {
                            SplashCache.ThreadCache.create(thread);
                        }
                    }

                }
            }
        });

        if (getIntent().getLongExtra("threadId", -1) != -1) {
            Thread thread = SplashCache.ThreadCache.getThread(serverIndex, getIntent().getLongExtra("threadId", -1), null);
            spinTopic.setSelection(GlobalFunctions.servers.get(thread.getServerIndex()).getTopicIndex(thread.getTopicId()));
            editPostTitle.setText(thread.getTitle());
            editPost.setText(thread.getRawContent());
            if (thread.getAttachId() >= 0) {
                btnImage.setText(R.string.strChangeAttach);
                attachid = thread.getAttachId();
            }
            setTitle(getString(R.string.strEditThread));
            updatePreview();
        }
    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, attachRequesCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == PostActivity.attachRequesCode) {
            if (resultCode == Activity.RESULT_OK) {
                attachImage(data.getData());
            } else {
                attach = null;
                attachid = -1;
                btnImage.setText(R.string.strAttach);
            }
        }
    }

    private void attachImage(Uri data) {
        try {
            String name = getFileName(data);
            String mime = getContentResolver().getType(data);
            if (mime == null) {
                mime = "application/octet-stream";
            }
            InputStream stream = getContentResolver().openInputStream(data);
            assert stream != null;
            if (mime.startsWith("image")) {
                attach = new SplashCache.AttachmentCache.SplashAttachment(BitmapFactory.decodeStream(stream), name);
            } else {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] byteArray = new byte[16384];

                while ((nRead = stream.read(byteArray, 0, byteArray.length)) != -1) {
                    buffer.write(byteArray, 0, nRead);
                }

                buffer.flush();
                attach = new SplashCache.AttachmentCache.SplashAttachment(buffer.toByteArray(), name);
                attach.setMimeType(mime);
            }
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnImage.setText(R.string.strChangeAttach);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage();
            }
        }
    }

    private void updatePreview() {
        previewPost.setHtml(GlobalFunctions.parseMarkdown(editPost.getText().toString()));
    }

    private class TopicAdapter extends ArrayAdapter<String> {
        final int serverIndex;

        TopicAdapter(int serverIndex) {
            super(PostActivity.this, android.R.layout.simple_spinner_dropdown_item, GlobalFunctions.servers.get(serverIndex).getTopics());
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
