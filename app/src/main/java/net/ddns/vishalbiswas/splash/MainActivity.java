package net.ddns.vishalbiswas.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class MainActivity extends AppCompatActivity {
    final private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent(MainActivity.this, NewsFeed.class);
                    startActivity(intent);
                    break;
                case 1:
                    setError(R.string.errInvalidCreds);
                    break;
                case 2:
                    setError(R.string.errPartialData);
                    break;
                case 3:
                    setError(R.string.errRequest);
                    break;
                case 6:
                    setError(R.string.errNoAccess);
                    break;
            }
        }
    };
    private EditText txtUsername, txtPassword;

    private void setError(int resId) {
        Snackbar.make(findViewById(R.id.frag), getString(resId), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);

        txtUsername=(EditText)findViewById(R.id.txtUsername);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("remember", false)) {
            if (sharedPreferences.getBoolean("autolog", false)) {
                doLogin();
            } else {
                txtUsername.setText(sharedPreferences.getString("username", ""));
                txtPassword.setText(sharedPreferences.getString("password", ""));
            }
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!checkEmpty()) {
                        String username = txtUsername.getText().toString();
                        String password = txtPassword.getText().toString();

                        GlobalFunctions.setUsername(username);
                        if (sharedPreferences.getBoolean("remember", false)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.apply();
                        }
                        doLogin();
                    } else {
                        Snackbar.make(v, getText(R.string.error_credentials), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                }
            });
        }
    }

    private void doLogin() {
        String username = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (!(username.isEmpty() || password.isEmpty())) {
            AsyncLogin asyncLogin = new AsyncLogin();
            asyncLogin.setHandler(handler);
            asyncLogin.execute(username, password);
        }
    }

    private boolean checkEmpty() {
        return txtUsername == null || txtUsername.getText().toString().isEmpty() || txtPassword == null || txtPassword.getText().toString().isEmpty();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(MainActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
