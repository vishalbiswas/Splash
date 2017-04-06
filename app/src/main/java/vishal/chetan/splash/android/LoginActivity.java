package vishal.chetan.splash.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import vishal.chetan.splash.R;
import vishal.chetan.splash.ServerList.SplashSource;
import vishal.chetan.splash.UserIdentity;
import vishal.chetan.splash.asyncs.AsyncHelper;
import vishal.chetan.splash.GlobalFunctions;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private EditText txtUsername, txtPassword;
    private int serverIndex;

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
        sharedPreferences = getPreferences(MODE_PRIVATE);

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(this, R.string.warnConfigServer, Toast.LENGTH_SHORT);
            startActivity(new Intent(this, SourcesManagerActivity.class));
            finish();
            return;
        }

        txtUsername=(EditText)findViewById(R.id.txtUsername);
        txtPassword=(EditText)findViewById(R.id.txtPassword);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnRegister = (Button) findViewById(R.id.btnRegister);

        int tempServerIndex = getIntent().getIntExtra("serverIndex", -1);
        if (tempServerIndex != -1) {
            serverIndex = tempServerIndex;
            SplashSource source = GlobalFunctions.servers.get(serverIndex);

            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)) {
                String user = sharedPreferences.getString("username" + source.getName(), "");
                String pass = sharedPreferences.getString("password" + source.getName(), "");
                txtUsername.setText(user);
                txtPassword.setText(pass);
                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autolog", false)) {
                    doLogin(user, pass);
                }
            }
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkEmpty()) {
                        doLogin(txtUsername.getText().toString(),
                                txtPassword.getText().toString());
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
                            .putExtra("serverIndex", serverIndex));
                }
            });
        }
    }

    private void doLogin(@NonNull String user, @NonNull String pass) {
        final String username = user.trim();
        final String password = pass.trim();

        if (!(username.isEmpty() || password.isEmpty() || serverIndex < 0)) {
            String postMessage = "user=" + username +"&password=" + password;

            new AsyncHelper(serverIndex, "login", postMessage) {
                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            int status = jsonObject.getInt("status");

                            if (status == 0) {
                                UserIdentity identity = GlobalFunctions.identities.get(serverIndex);
                                if (identity == null) {
                                    identity = new UserIdentity();
                                    GlobalFunctions.identities.append(serverIndex, identity);
                                }

                                if (jsonObject.has("fname")) {
                                    identity.setFirstname(jsonObject.getString("fname"));
                                } else {
                                    identity.setFirstname("");
                                }
                                if (jsonObject.has("lname")) {
                                    identity.setLastname(jsonObject.getString("lname"));
                                } else {
                                    identity.setLastname("");
                                }

                                if (jsonObject.has("profpic")) {
                                    byte[] picBytes = Base64.decode(jsonObject.getString("profpic"), Base64.DEFAULT);
                                    Bitmap profpic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
                                    identity.setProfpic(profpic);
                                } else {
                                    identity.setProfpic(null);
                                }
                                identity.setUid(jsonObject.getInt("uid"));
                                identity.setUsername(jsonObject.getString("user"));
                                identity.setEmail(jsonObject.getString("email"));

                                setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex));
                                if (PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getBoolean("remember", false)) {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("username" + GlobalFunctions.servers.get(serverIndex).getName(), username);
                                    editor.putString("password" + GlobalFunctions.servers.get(serverIndex).getName(), password);
                                    editor.apply();
                                }
                                finish();
                            } else {
                                setError(R.string.errInvalidCreds);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        setError(R.string.errNoAccess);
                    }
                }
            }.execute();
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
