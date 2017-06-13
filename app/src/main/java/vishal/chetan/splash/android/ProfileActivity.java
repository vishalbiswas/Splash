package vishal.chetan.splash.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.FieldValidator;
import vishal.chetan.splash.R;
import vishal.chetan.splash.SplashCache;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncHelper;
import vishal.chetan.splash.GlobalFunctions;

import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends BaseActivity {
    private final int requestCode = 0x0421;
    private int serverIndex;
    private long uid = -1;
    private FieldValidator fieldValidator;
    @Nullable
    private UserIdentity identity;
    private AsyncTask updateTask = null;
    private MenuItem menu_save = null;

    private ImageView imgPic;
    @Nullable
    private Bitmap profPic;
    private TextView FName;
    private TextView LName;
    private TextView Email;
    private EditText editPass, editPass2;

    private void setError(@StringRes int resId) {
        View view = findViewById(android.R.id.content);
        assert view != null;
        Snackbar.make(view, getString(resId), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        uid = getIntent().getLongExtra("uid", -1);
        if (uid == -1) {
            setContentView(R.layout.activity_profile);
            identity = GlobalFunctions.servers.get(serverIndex).identity;
            fieldValidator = new FieldValidator(serverIndex, new ProfileErrorProvider());
        } else {
            setContentView(R.layout.activity_profile_readonly);
            identity = SplashCache.UsersCache.getUser(serverIndex, uid, null);
            if (identity == null) {
                Toast.makeText(this, getString(R.string.errUserData), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        imgPic = (ImageView) findViewById(R.id.imgPic);
        assert identity != null;
        ((TextView) findViewById(R.id.Username)).setText(identity.getUsername());
        FName = (TextView) findViewById(R.id.FName);
        LName = (TextView) findViewById(R.id.LName);
        Email = (TextView) findViewById(R.id.Email);

        if (identity.getProfpic() >= 0) {
            SplashCache.AttachmentCache.get(serverIndex, identity.getProfpic(), new SplashCache.AttachmentCache.OnGetAttachmentListener() {
                @Override
                public void onGetAttachment(final SplashCache.AttachmentCache.SplashAttachment attachment) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgPic.setImageBitmap((Bitmap)attachment.data);
                        }
                    });
                }
            });
        }
        FName.setText(identity.getFirstname());
        LName.setText(identity.getLastname());
        Email.setText(identity.getEmail());

        Email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                String email = Email.getText().toString().trim();
                if (!identity.getEmail().equals(email)){
                    fieldValidator.validateEmail(email);
                }
            }
        });

        if (uid == -1) {
            editPass = (EditText) findViewById(R.id.editPass);
            editPass2 = (EditText) findViewById(R.id.editPass2);

            imgPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, requestCode);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK)
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                assert stream != null;
                profPic = BitmapFactory.decodeStream(stream);
                imgPic.setImageBitmap(profPic);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (uid == -1) {
            getMenuInflater().inflate(R.menu.menu_profile, menu);
        }
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu_save = menu.findItem(R.id.menuSave);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                GlobalFunctions.launchSettings(ProfileActivity.this);
                break;
            case R.id.menuSave:
                if (!Email.getText().toString().equals(GlobalFunctions.servers.get(serverIndex).identity.getEmail())) {
                    if (GlobalFunctions.getRegEmailStatus() != GlobalFunctions.HTTP_CODE.BUSY) {
                        if (Email.getError() != null || GlobalFunctions.getRegEmailStatus() != GlobalFunctions.HTTP_CODE.SUCCESS) {
                            break;
                        }
                    } else {
                        fieldValidator.errorProvider.setErrorSnack(R.string.warnRegBusy);
                        break;
                    }
                }
                String pass = editPass.getText().toString().trim();
                String pass2 = editPass2.getText().toString().trim();
                String postMessage = String.format("sessionid=%s&fname=%s&lname=%s&email=%s", GlobalFunctions.servers.get(serverIndex).identity.getSessionid(), FName.getText(), LName.getText(), Email.getText());
                if (!pass.isEmpty()) {
                    if (!pass.equals(pass2)) {
                        setError(R.string.errPassNoMatch);
                        break;
                    } else {
                        fieldValidator.validatePassword(editPass.getText().toString());
                        if (editPass.getError() == null) {
                            postMessage = String.format("%s&password=%s", postMessage, editPass.getText());
                        } else {
                            break;
                        }
                    }
                }
                final String notFinalPostMessage = postMessage;
                menu_save.setEnabled(false);
                if (profPic != null) {
                    SplashCache.AttachmentCache.SplashAttachment attachment = new SplashCache.AttachmentCache.SplashAttachment(profPic);
                    SplashCache.AttachmentCache.upload(serverIndex, attachment, new SplashCache.AttachmentCache.OnUploadCompleteListener() {
                        @Override
                        public void onUpload(long attachId) {
                            doUpdate(String.format("%s&profpic=%s", notFinalPostMessage, attachId));
                        }
                    });
                } else {
                    assert identity != null;
                    doUpdate(String.format("%s&profpic=%s", postMessage, identity.getProfpic()));
                }
                break;
        }
        return true;
    }

    private void doUpdate(String postMessage) {
        if (updateTask == null || updateTask.getStatus() == AsyncTask.Status.FINISHED) {
            assert identity != null;
            updateTask = new AsyncHelper(serverIndex, "update", postMessage) {
                @Override
                protected void onPostExecute(@Nullable JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            switch (jsonObject.getInt("status")) {
                                case 0:
                                    UserIdentity identity = GlobalFunctions.servers.get(serverIndex).identity;
                                    identity.setFirstname(jsonObject.getString("fname"));
                                    identity.setLastname(jsonObject.getString("lname"));

                                    if (jsonObject.has("email")) {
                                        identity.setEmail(jsonObject.getString("email"));
                                    }
                                    if (jsonObject.has("profpic")) {
                                        identity.setProfpic(jsonObject.getLong("profpic"));
                                    }
                                    setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex));
                                    finish();
                                    break;
                                case 1:
                                    setError(R.string.errUnknown);
                                    break;
                                case 2:
                                    setError(R.string.errPartialData);
                                    break;
                                case 3:
                                    setError(R.string.errRequest);
                                    break;
                                case 5:
                                    setError(R.string.errUnknown);
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        setError(R.string.errNoAccess);
                    }
                    menu_save.setEnabled(true);
                }
            }.execute();
        } else {
            setError(R.string.strAlreadyRunning);
        }
    }

    private class ProfileErrorProvider implements FieldValidator.ErrorProvider {
        @Override
        public void setErrorUsername(@StringRes int resId) {
            //Don't need it
        }

        @Override
        public void setErrorEmail(@StringRes int resId) {
            Email.setError(getString(resId));
        }

        @Override
        public void setErrorPassword(@StringRes int resId) {
            editPass.setError(getString(resId));
        }

        @Override
        public void setErrorSnack(@StringRes int resId) {
            setError(resId);
        }
    }
}
