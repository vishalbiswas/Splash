package vishal.chetan.splash.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.Thread;

public class PostActivity extends BaseActivity {
    private int serverIndex;
    private static final int requestCode = 1;
    private HtmlTextView previewPost;
    private EditText editPost;
    private Button btnImage;
    @Nullable
    private Bitmap attach = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        previewPost =  (HtmlTextView) findViewById(R.id.previewPost);
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

        btnImage = (Button) findViewById(R.id.btnImage);

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, requestCode);
            }
        });

        final Button btnSubmit = (Button) findViewById(R.id.btnSubmit);
        final SplashCache.ThreadCache.OnThreadModifiedListener modifiedListener = new SplashCache.ThreadCache.OnThreadModifiedListener() {
            @Override
            public void onModify(@Nullable Thread thread) {
                btnSubmit.setEnabled(true);
                if (thread != null) {
                    setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex).putExtra("threadId", thread.getThreadId()));
                    finish();
                } else {
                    Snackbar.make(btnSubmit, getString(R.string.errPost), Snackbar.LENGTH_LONG).show();
                }
            }
        };
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull final View v) {
                if (!(editPostTitle.getText().toString().isEmpty() || editPost.getText().toString().isEmpty())) {
                    final Thread thread;
                    v.setEnabled(false);
                    SplashCache.ThreadCache.setOnThreadModifyListener(modifiedListener);
                    if (getIntent().getLongExtra("threadId", -1) != -1) {
                        thread = SplashCache.ThreadCache.getThread(serverIndex, getIntent().getLongExtra("threadId", -1));
                        thread.setMtime(new Date());
                        thread.setTitle(editPostTitle.getText().toString());
                        thread.setContent(editPost.getText().toString());
                        thread.setTopicId((int) spinTopic.getSelectedItemId());
                        if (attach != null) {
                            SplashCache.ImageCache.upload(serverIndex, attach, new SplashCache.ImageCache.OnUploadCompleteListener() {
                                @Override
                                public void onUpload(long attachId) {
                                    if (attachId < 0) {
                                        v.setEnabled(true);
                                        Snackbar.make(btnSubmit, R.string.errAttach, Snackbar.LENGTH_LONG).show();
                                    } else {
                                        thread.setAttachId(attachId);
                                        SplashCache.ThreadCache.set(thread);
                                    }
                                }
                            });
                        } else {
                            SplashCache.ThreadCache.set(thread);
                        }
                    } else {
                         thread = new Thread(new Random(new Date().getTime()).nextLong(), serverIndex, editPostTitle.getText().toString(), editPost.getText().toString(), GlobalFunctions.identities.get(serverIndex).getUid(), new Date(), new Date(), (int) spinTopic.getSelectedItemId());
                        if (attach != null) {
                            SplashCache.ImageCache.upload(serverIndex, attach, new SplashCache.ImageCache.OnUploadCompleteListener() {
                                @Override
                                public void onUpload(long attachId) {
                                    if (attachId < 0) {
                                        v.setEnabled(true);
                                        Snackbar.make(btnSubmit, R.string.errAttach, Snackbar.LENGTH_LONG).show();
                                    } else {
                                        thread.setAttachId(attachId);
                                        SplashCache.ThreadCache.add(thread);
                                    }
                                }
                            });
                        } else {
                            SplashCache.ThreadCache.add(thread);
                        }
                    }

                }
            }
        });

        if (getIntent().getLongExtra("threadId", -1) != -1) {
            Thread thread = SplashCache.ThreadCache.getThread(serverIndex, getIntent().getLongExtra("threadId", -1));
            editPostTitle.setText(thread.getTitle());
            editPost.setText(thread.getRawContent());
            attach = SplashCache.ImageCache.get(serverIndex, thread.getAttachId());
            btnImage.setText(R.string.strChangeAttach);
            setTitle(getString(R.string.strEditThread));
            updatePreview();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == PostActivity.requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    InputStream stream = getContentResolver().openInputStream(data.getData());
                    assert stream != null;
                    attach = BitmapFactory.decodeStream(stream);
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                btnImage.setText(R.string.strChangeAttach);
            } else {
                attach = null;
                btnImage.setText(R.string.strAttach);
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
