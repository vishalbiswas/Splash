package net.ddns.vishalbiswas.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    static final String EXTRA_USERNAME = "net.ddns.vishalbiswas.splash.EXTRA_USERNAME", EXTRA_PASSWORD = "net.ddns.vishalbiswas.splash.EXTRA_PASSWORD";
    private EditText txtUsername, txtPassword;
    private AppCompatButton btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalFunctions.lookupLocale(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);

        txtUsername=(EditText)findViewById(R.id.txtUsername);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
        btnLogin=(AppCompatButton)findViewById(R.id.btnLogin);
        btnRegister = (AppCompatButton) findViewById(R.id.btnRegister);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("remember", false)) {
            txtUsername.setText(sharedPreferences.getString("username", ""));
            txtPassword.setText(sharedPreferences.getString("password", ""));
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEmpty()) {
                    String username = txtUsername.getText().toString();
                    String password = txtPassword.getText().toString();

                    Intent intent = new Intent(MainActivity.this, NewsFeed.class);
                    intent.putExtra(EXTRA_USERNAME, username);
                    intent.putExtra(EXTRA_PASSWORD, password);
                    startActivity(intent);
                    if (sharedPreferences.getBoolean("remember", false)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.apply();
                    }
                    finish();
                }
                else {
                    Snackbar.make(v,getText(R.string.error_credentials),Snackbar.LENGTH_LONG).show();
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    private boolean checkEmpty() {
        return txtUsername == null || txtUsername.getText().toString().isEmpty() || txtPassword == null || txtPassword.getText().toString().isEmpty();
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
            GlobalFunctions.launchSettings(MainActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
