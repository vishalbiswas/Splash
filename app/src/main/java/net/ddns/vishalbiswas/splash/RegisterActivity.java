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

public class RegisterActivity extends AppCompatActivity {
    EditText viewUsername;
    final public Handler regHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    checkStatus();
                    register();
            }
        }
    };
    EditText viewEmail;
    EditText viewPassword;
    Button regButton;

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

        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalFunctions.getRegStatus() != GlobalFunctions.HTTP_CODE.BUSY) {
                    validateFields();
                } else {
                    Snackbar.make(findViewById(R.id.frag), R.string.warnRegBusy, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateFields() {
        String username = viewUsername.getText().toString().trim();
        String email = viewEmail.getText().toString().trim();
        String password = viewPassword.getText().toString().trim();

        FieldValidator fieldValidator = new FieldValidator();

        int resultCode = fieldValidator.validateUsername(username);
        if (resultCode != -1) {
            viewUsername.setError(getString(resultCode));
            viewUsername.requestFocus();
            return false;
        }

        resultCode = fieldValidator.validateEmail(email);
        if (resultCode != -1) {
            viewEmail.setError(getString(resultCode));
            viewEmail.requestFocus();
            return false;
        }

        resultCode = fieldValidator.validatePassword(password);
        if (resultCode != -1) {
            viewPassword.setError(getString(resultCode));
            viewPassword.requestFocus();
            return false;
        }

        CheckAvailable checkAvailable = new CheckAvailable();
        checkAvailable.handler = regHandler;
        checkAvailable.execute(username);

        return true;
    }

    private boolean checkStatus() {
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
