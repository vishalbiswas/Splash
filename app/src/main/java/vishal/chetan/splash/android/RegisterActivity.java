package vishal.chetan.splash.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import vishal.chetan.splash.R;
import vishal.chetan.splash.asyncs.AsyncHelper;
import vishal.chetan.splash.GlobalFunctions;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText viewUsername;
    private EditText viewEmail;
    private EditText viewPassword;
    private View snackLayout;
    final private FieldValidator fieldValidator = new FieldValidator(new ErrorProvider());
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

    private void checkStatus(boolean checkForUser, @NonNull GlobalFunctions.HTTP_CODE code) {
        if (checkForUser) {
            GlobalFunctions.setRegNameStatus(code);
        } else {
            GlobalFunctions.setRegEmailStatus(code);
        }
        switch (code) {
            case SUCCESS:
                return;
            case FAILED:
                if (checkForUser) {
                    fieldValidator.errorProvider.setErrorUsername(R.string.errNameUsed);
                } else {
                    fieldValidator.errorProvider.setErrorEmail(R.string.errEmailUsed);
                }
                break;
            case NO_ACCESS:
                Snackbar.make(snackLayout, getString(R.string.errNoAccess), Snackbar.LENGTH_LONG).show();
                break;
            case REQUEST_FAILED:
                Snackbar.make(snackLayout, getString(R.string.errConFailed), Snackbar.LENGTH_LONG).show();
                break;
            case UNKNOWN:
                Snackbar.make(snackLayout, getString(R.string.errUnknown), Snackbar.LENGTH_LONG).show();
        }
    }

    private void register() {
        String username = viewUsername.getText().toString().trim();
        String email = viewEmail.getText().toString().trim();
        String password = viewPassword.getText().toString().trim();
        String postMessage = "user=" + username + "&email=" + email + "&pass=" + password;

        new AsyncHelper(serverIndex, "register", postMessage) {
            @Override
            protected void onPostExecute(JSONObject jsonObject) {
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
        toolbar.setTitle(String.format("%s (%s)", getString(R.string.title_activity_register), GlobalFunctions.servers.get(serverIndex)));

        viewUsername = (EditText) findViewById(R.id.regUser);
        viewEmail = (EditText) findViewById(R.id.regEmail);
        viewPassword = (EditText) findViewById(R.id.regPassword);
        Button regButton = (Button) findViewById(R.id.regButton);
        snackLayout = findViewById(R.id.frag);

        viewUsername.setText(GlobalFunctions.defaultIdentity.getUsername());
        viewEmail.setText(GlobalFunctions.defaultIdentity.getEmail());

        viewUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    fieldValidator.validateUsername(((EditText) v).getText().toString());
                }
            }
        });

        viewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    fieldValidator.validateEmail(((EditText) v).getText().toString());
                }
            }
        });

        viewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(RegisterActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FieldValidator {
        final ErrorProvider errorProvider;
        String username;
        String email;
        String password;

        FieldValidator(ErrorProvider errorProvider) {
            this.errorProvider = errorProvider;
        }

        void validateUsername(final String username) {
            if (this.username == null || !(username.equals(this.username))) {
                if (username.isEmpty()) {
                    errorProvider.setErrorUsername(R.string.errEmpty);
                    return;
                }

                GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.BUSY);
                new CheckAvailable(serverIndex, "check/" + username).execute();
                this.username = username;
            }
        }

        void validateEmail(final String email) {
            if (this.email == null || !email.equals(this.email)) {
                if (email.isEmpty()) {
                    errorProvider.setErrorEmail(R.string.errEmpty);
                    return;
                }

                final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                if (!(emailPattern.matcher(email).find())) {
                    errorProvider.setErrorEmail(R.string.errInvalidEmail);
                    return;
                }

                GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.BUSY);
                new CheckAvailable(serverIndex, "check/" + email).execute();
                this.email = email;
            }
        }

        void validatePassword(final String password) {
            if (this.password == null || !password.equals(this.password)) {
                if (password.isEmpty()) {
                    errorProvider.setErrorPassword(R.string.errEmpty);
                    return;
                }

                if (password.length() < 8) {
                    errorProvider.setErrorPassword(R.string.errShortPass);
                    return;
                }
                this.password = password;
            }
        }
    }

    private class ErrorProvider {
        void setErrorUsername(int resId) {
            viewUsername.setError(getString(resId));
        }

        void setErrorEmail(int resId) {
            viewEmail.setError(getString(resId));
        }

        void setErrorPassword(int resId) {
            viewPassword.setError(getString(resId));
        }
    }

    private class CheckAvailable extends AsyncHelper {
        private Boolean checkForUser = true;

        CheckAvailable(int serverIndex, String pageUrl) {
            super(serverIndex, pageUrl);
            if (pageUrl.contains("@")) {
                checkForUser = false;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            GlobalFunctions.HTTP_CODE status = GlobalFunctions.HTTP_CODE.UNKNOWN;
            if (jsonObject != null) {
                try {
                    Boolean isAvailable;
                    if (checkForUser) {
                        isAvailable = jsonObject.getBoolean("user");
                    } else {
                        isAvailable = jsonObject.getBoolean("email");
                    }


                    if (isAvailable) {
                        status = GlobalFunctions.HTTP_CODE.SUCCESS;
                    } else {
                        status = GlobalFunctions.HTTP_CODE.FAILED;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (checkForUser) {
                        status = GlobalFunctions.HTTP_CODE.REQUEST_FAILED;
                    }
                }
            } else {
                status = GlobalFunctions.HTTP_CODE.NO_ACCESS;
            }
            checkStatus(checkForUser, status);
        }
    }
}