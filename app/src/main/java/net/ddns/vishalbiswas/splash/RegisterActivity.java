package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

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
                validateFields();
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

        return checkUsernameAvailability(username);
    }

    private boolean checkUsernameAvailability(String username) {
        //TODO: Implement validation through webservice
        return true;
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
