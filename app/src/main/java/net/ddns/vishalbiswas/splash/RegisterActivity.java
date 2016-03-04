package net.ddns.vishalbiswas.splash;

import android.content.Context;
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

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    static EditText viewUsername;
    static EditText viewEmail;
    static EditText viewPassword;
    static Button regButton;
    static View snackLayout;

    public static Handler regHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    checkStatus();
                    register();
            }
        }
    };

    private static boolean checkStatus() {
        switch (GlobalFunctions.getRegStatus()) {
            case SUCCESS:
                return true;
            case FAILED:
                FieldValidator.errorProvider.setErrorUsername(R.string.errNameUsed);
                break;
            case NO_ACCESS:
                Snackbar.make(snackLayout, FieldValidator.context.getString(R.string.errNoAccess), Snackbar.LENGTH_LONG).show();
                break;
            case REQUEST_FAILED:
                Snackbar.make(snackLayout, FieldValidator.context.getString(R.string.errConFailed), Snackbar.LENGTH_LONG).show();
                break;
            case UNKNOWN:
                Snackbar.make(snackLayout, FieldValidator.context.getString(R.string.errUnknown), Snackbar.LENGTH_LONG).show();
        }
        return false;
    }

    private static boolean register() {
        //TODO: implement registration
        return true;
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
        regButton = (Button) findViewById(R.id.regButton);
        snackLayout = findViewById(R.id.frag);

        FieldValidator.errorProvider = new ErrorProvider();
        FieldValidator.context = this;

        viewUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    FieldValidator.validateUsername(viewUsername.getText().toString());
                }
            }
        });

        viewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    FieldValidator.validateEmail(((EditText) v).getText().toString());
                }
            }
        });

        viewPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    FieldValidator.validatePassword(((EditText) v).getText().toString());
                }
            }
        });

        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalFunctions.getRegStatus() != GlobalFunctions.HTTP_CODE.BUSY) {
                    if (viewUsername.getError() == null && viewEmail == null && viewPassword == null && GlobalFunctions.getRegStatus() == GlobalFunctions.HTTP_CODE.SUCCESS) {
                        register();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.frag), R.string.warnRegBusy, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(RegisterActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class FieldValidator {
        static String username;
        static String email;
        static String password;
        static ErrorProvider errorProvider;
        static Context context;

        public FieldValidator(ErrorProvider errorProvider, Context context) {
            FieldValidator.errorProvider = errorProvider;
            FieldValidator.context = context;
        }

        public static void validateUsername(final String username) {
            if (FieldValidator.username == null || !(username.equals(FieldValidator.username))) {
                if (username.isEmpty()) {
                    errorProvider.setErrorUsername(R.string.errEmpty);
                    return;
                }

                CheckAvailable checkAvailable = new CheckAvailable();
                checkAvailable.handler = regHandler;
                checkAvailable.execute(username);
                FieldValidator.username = username;
            }
        }

        public static void validateEmail(final String email) {
            if (FieldValidator.email == null || !email.equals(FieldValidator.email)) {
                if (email.isEmpty()) {
                    errorProvider.setErrorEmail(R.string.errEmpty);
                    return;
                }

                final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
                if (!(emailPattern.matcher(email).find())) {
                    errorProvider.setErrorEmail(R.string.errInvalidEmail);
                    return;
                }
                FieldValidator.email = email;
            }
        }

        public static void validatePassword(final String password) {
            if (FieldValidator.password == null || !password.equals(FieldValidator.password)) {
                if (password.isEmpty()) {
                    errorProvider.setErrorPassword(R.string.errEmpty);
                    return;
                }

                if (password.length() < 8) {
                    errorProvider.setErrorPassword(R.string.errShortPass);
                    return;
                }
                FieldValidator.password = password;
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
