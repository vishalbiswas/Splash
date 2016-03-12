package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

class RegisterActivity extends AppCompatActivity {
    private static EditText viewUsername;
    private static EditText viewEmail;
    private static EditText viewPassword;
    private static View snackLayout;
    final FieldValidator fieldValidator = new FieldValidator(new ErrorProvider());

    final public Handler regHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    checkStatus(true, GlobalFunctions.getRegNameStatus());
                    break;
                case -2:
                    checkStatus(false, GlobalFunctions.getRegEmailStatus());
                    break;
                case 0:
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                    finish();
                    break;
                default:
                    failed(msg.what);
                    break;
            }
        }
    };

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

    private void checkStatus(boolean checkForUser, GlobalFunctions.HTTP_CODE code) {
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
        AsyncRegister register = new AsyncRegister();
        register.setHandler(regHandler);
        register.execute(viewUsername.getText().toString(), viewEmail.getText().toString(), viewPassword.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewUsername = (EditText) findViewById(R.id.regUser);
        viewEmail = (EditText) findViewById(R.id.regEmail);
        viewPassword = (EditText) findViewById(R.id.regPassword);
        Button regButton = (Button) findViewById(R.id.regButton);
        snackLayout = findViewById(R.id.frag);

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

    class FieldValidator {
        final ErrorProvider errorProvider;
        String username;
        String email;
        String password;

        public FieldValidator(ErrorProvider errorProvider) {
            this.errorProvider = errorProvider;
        }

        public void validateUsername(final String username) {
            if (this.username == null || !(username.equals(this.username))) {
                if (username.isEmpty()) {
                    errorProvider.setErrorUsername(R.string.errEmpty);
                    return;
                }

                CheckAvailable checkAvailable = new CheckAvailable();
                checkAvailable.setHandler(regHandler);
                checkAvailable.execute(username);
                this.username = username;
            }
        }

        public void validateEmail(final String email) {
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

                CheckAvailable checkAvailable = new CheckAvailable();
                checkAvailable.setHandler(regHandler);
                checkAvailable.execute(email, "email");
                this.email = email;
            }
        }

        public void validatePassword(final String password) {
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

    class ErrorProvider {
        public void setErrorUsername(int resId) {
            viewUsername.setError(getString(resId));
        }

        public void setErrorEmail(int resId) {
            viewEmail.setError(getString(resId));
        }

        public void setErrorPassword(int resId) {
            viewPassword.setError(getString(resId));
        }
    }
}
