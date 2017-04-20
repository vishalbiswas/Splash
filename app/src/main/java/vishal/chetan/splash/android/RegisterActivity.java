package vishal.chetan.splash.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.FieldValidator;
import vishal.chetan.splash.R;
import vishal.chetan.splash.asyncs.AsyncHelper;
import vishal.chetan.splash.GlobalFunctions;

public class RegisterActivity extends BaseActivity {
    private EditText viewUsername;
    private EditText viewEmail;
    private EditText viewPassword;
    private View snackLayout;
    private FieldValidator fieldValidator;
    private int serverIndex;

    private void failed(int errorCode) {
        int errorMessageResId = R.string.errUnknown;
        switch (errorCode) {
            case 1:
                errorMessageResId = R.string.errServer;
                break;
            case 2:
                errorMessageResId = R.string.errUsername;
                break;
            case 3:
                errorMessageResId = R.string.errEmail;
                break;
            case 4:
                errorMessageResId = R.string.errPassword;
                break;
            case 5:
                errorMessageResId = R.string.errRequest;
                break;
            case 6:
                errorMessageResId = R.string.errNoAccess;
        }
        Snackbar.make(findViewById(R.id.frag), errorMessageResId, Snackbar.LENGTH_LONG).show();
    }

    private void register() {
        String username = viewUsername.getText().toString().trim();
        String email = viewEmail.getText().toString().trim();
        String password = viewPassword.getText().toString().trim();
        String postMessage = "user=" + username + "&email=" + email + "&pass=" + password;

        new AsyncHelper(serverIndex, "register", postMessage) {
            @Override
            protected void onPostExecute(@Nullable JSONObject jsonObject) {
                if (jsonObject != null) {
                    try {
                        int status = jsonObject.getInt("status");
                        if (status == 0) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            failed(status);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    failed(-1);
                }
            }
        }.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);

        serverIndex = getIntent().getIntExtra("serverIndex", -1);
        if (serverIndex < 0) {
            Log.e("Splash", "RegisterActivity received negative serverIndex");
            finish();
            return;
        }

        setContentView(R.layout.activity_register);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbarRegister));
        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(String.format("%s (%s)", getString(R.string.title_activity_register), GlobalFunctions.servers.get(serverIndex).getName()));

        fieldValidator = new FieldValidator(serverIndex, new RegisterErrorProvider());

        viewUsername = (EditText) findViewById(R.id.regUser);
        viewEmail = (EditText) findViewById(R.id.regEmail);
        viewPassword = (EditText) findViewById(R.id.regPassword);
        Button regButton = (Button) findViewById(R.id.regButton);
        snackLayout = findViewById(R.id.frag);

        viewUsername.setText(GlobalFunctions.defaultIdentity.getUsername());
        viewEmail.setText(GlobalFunctions.defaultIdentity.getEmail());

        viewUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(@NonNull View v, boolean hasFocus) {
                if (!hasFocus) {
                    fieldValidator.validateUsername(((EditText) v).getText().toString());
                }
            }
        });

        viewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(@NonNull View v, boolean hasFocus) {
                if (!hasFocus) {
                    fieldValidator.validateEmail(((EditText) v).getText().toString());
                }
            }
        });

        viewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(@NonNull View v, boolean hasFocus) {
                if (!hasFocus) {
                    fieldValidator.validatePassword(((EditText) v).getText().toString());
                }
            }
        });

        if (regButton != null) {
            regButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalFunctions.getRegNameStatus() != GlobalFunctions.HTTP_CODE.BUSY && GlobalFunctions.getRegEmailStatus() != GlobalFunctions.HTTP_CODE.BUSY) {
                        if (viewUsername.getError() == null && viewEmail.getError() == null && viewPassword.getError() == null && GlobalFunctions.getRegStatus()) {
                            register();
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.frag), R.string.warnRegBusy, Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(RegisterActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RegisterErrorProvider implements FieldValidator.ErrorProvider {
        public void setErrorUsername(@StringRes int resId) {
            viewUsername.setError(getString(resId));
        }

        public void setErrorEmail(@StringRes int resId) {
            viewEmail.setError(getString(resId));
        }

        public void setErrorPassword(@StringRes int resId) {
            viewPassword.setError(getString(resId));
        }

        @Override
        public void setErrorSnack(@StringRes int resId) {
            Snackbar.make(snackLayout, resId, Snackbar.LENGTH_LONG).show();
        }
    }
}
