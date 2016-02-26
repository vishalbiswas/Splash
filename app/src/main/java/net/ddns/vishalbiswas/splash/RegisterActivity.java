package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppCompatButton regButton = (AppCompatButton) findViewById(R.id.regButton);
        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    register();
                }
            }
        });
    }

    private boolean validateFields() {
        AppCompatEditText viewUsername = (AppCompatEditText) findViewById(R.id.regUser);
        AppCompatEditText viewEmail = (AppCompatEditText) findViewById(R.id.regEmail);
        AppCompatEditText viewPassword = (AppCompatEditText) findViewById(R.id.regPassword);

        String username = viewUsername.getText().toString().trim();
        String email = viewEmail.getText().toString().trim();
        String password = viewPassword.getText().toString().trim();

        if (username.isEmpty()) {
            viewUsername.setError(getText(R.string.errEmpty));
            viewUsername.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            viewEmail.setError(getText(R.string.errEmpty));
            viewEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            viewPassword.setError(getText(R.string.errEmpty));
            viewPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            viewPassword.setError(getText(R.string.errShortPass));
            viewPassword.requestFocus();
            return false;
        }

        final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        if (!(emailPattern.matcher(email).find())) {
            viewEmail.setError(getText(R.string.errInvalidEmail));
            viewEmail.requestFocus();
            return false;
        }

        checkUsernameAvailability(username);

        switch (GlobalFunctions.getRegStatus()) {
            case SUCCESS:
                return true;
            case FAILED:
                viewUsername.setError(getString(R.string.errNameUsed));
                break;
            case NO_ACCESS:
                Snackbar.make(findViewById(R.id.frag), getString(R.string.errNoAccess), Snackbar.LENGTH_LONG).show();
                break;
            case REQUEST_FAILED:
                Snackbar.make(findViewById(R.id.frag), getString(R.string.errConFailed), Snackbar.LENGTH_LONG).show();
                break;
            case UNKNOWN:
                Snackbar.make(findViewById(R.id.frag), getString(R.string.errUnknown), Snackbar.LENGTH_LONG).show();
        }
        return false;
    }

    private boolean register() {
        //TODO: implement registration
        return true;
    }

    private void checkUsernameAvailability(final String username) {
        CheckAvailable checkAvailable = new CheckAvailable();
        try {
            synchronized (checkAvailable.execute(username)) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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


}
