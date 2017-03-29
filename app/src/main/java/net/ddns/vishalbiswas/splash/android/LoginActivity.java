package net.ddns.vishalbiswas.splash.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.ddns.vishalbiswas.splash.R;
import net.ddns.vishalbiswas.splash.classes.asyncs.AsyncLogin;
import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private EditText txtUsername, txtPassword;
    private Spinner spinServer;

    final private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent(LoginActivity.this, NewsFeed.class);
                    startActivity(intent);
                    if (sharedPreferences.getBoolean("remember", false)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", txtUsername.getText().toString().trim());
                        editor.putString("password", txtPassword.getText().toString().trim());
                        editor.putInt("server", spinServer.getSelectedItemPosition());
                        editor.apply();
                    }
                    finish();
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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(this, R.string.warnConfigServer, Toast.LENGTH_SHORT);
            startActivity(new Intent(this, SourcesManagerActivity.class));
            finish();
            return;
        }

        txtUsername=(EditText)findViewById(R.id.txtUsername);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
        spinServer = (Spinner) findViewById(R.id.spinServer);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);

        if (sharedPreferences.getBoolean("remember", false)) {
            String user = sharedPreferences.getString("username", "");
            String pass = sharedPreferences.getString("password", "");
            int serverIndex = sharedPreferences.getInt("server", -1);
            txtUsername.setText(user);
            txtPassword.setText(pass);
            spinServer.setSelection(serverIndex);
            if (sharedPreferences.getBoolean("autolog", false)) {
                doLogin(user, pass, serverIndex);
            }
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkEmpty()) {
                        doLogin(txtUsername.getText().toString(),
                                txtPassword.getText().toString(),
                                spinServer.getSelectedItemPosition());
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
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class)
                            .putExtra("serverIndex", spinServer.getSelectedItemPosition()));
                }
            });
        }

        if (spinServer != null) {
            spinServer.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    GlobalFunctions.servers));
        }
    }

    private void doLogin(@NonNull String user, String pass, int serverIndex) {
        String username = user.trim();
        String password = pass.trim();

        if (!(username.isEmpty() || password.isEmpty() || serverIndex < 0)) {
            AsyncLogin asyncLogin = new AsyncLogin();
            asyncLogin.setHandler(handler);
            asyncLogin.execute(serverIndex, username, password);
        }
    }

    private boolean checkEmpty() {
        return txtUsername != null && !txtUsername.getText().toString().trim().isEmpty()
                && txtPassword != null && !txtPassword.getText().toString().trim().isEmpty();
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
            GlobalFunctions.launchSettings(LoginActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
