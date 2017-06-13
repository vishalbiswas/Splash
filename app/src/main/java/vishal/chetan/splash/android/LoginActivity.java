package vishal.chetan.splash.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
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

public class LoginActivity extends BaseActivity {
    private EditText txtUsername, txtPassword;
    private int serverIndex;
    private static AsyncTask loginTask;

    private void setError(@StringRes int resId) {
        Snackbar.make(findViewById(R.id.frag), getString(resId), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarLogin);
        setSupportActionBar(toolbar);

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(this, R.string.warnConfigServer, Toast.LENGTH_SHORT).show();
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
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)) {
                if (GlobalFunctions.servers.get(serverIndex).session == SplashSource.SessionState.UNKNOWN && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autolog", false)) {
                    doLogin();
                }
            }
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
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
            String postMessage = "user=" + username +"&pass=" + password;

            if (loginTask == null || loginTask.getStatus() == AsyncTask.Status.FINISHED) {
                loginTask = new LoginHelper(serverIndex, postMessage).execute();
            } else {
                setError(R.string.strAlreadyRunning);
            }
        }
    }

    private void doLogin() {
        String sessionid = getSharedPreferences("sessions", MODE_PRIVATE).getString(GlobalFunctions.servers.get(serverIndex).getName(), null);
        if (sessionid != null) {
            String postMessage = "sessionid=" + sessionid;

            if (loginTask == null || loginTask.getStatus() == AsyncTask.Status.FINISHED) {
                loginTask = new LoginHelper(serverIndex, postMessage).execute();
            } else {
                setError(R.string.strAlreadyRunning);
            }
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(LoginActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoginHelper extends AsyncHelper {
        public LoginHelper(int serverIndex, String postMessage) {
            super(serverIndex, "login", postMessage);
        }

        @Override
        protected void onPostExecute(@Nullable JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    int status = jsonObject.getInt("status");

                    if (status == 0) {
                        UserIdentity identity = GlobalFunctions.servers.get(serverIndex).identity;
                        if (identity == null) {
                            identity = new UserIdentity();
                            GlobalFunctions.servers.get(serverIndex).identity = identity;
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
                            identity.setProfpic(jsonObject.getLong("profpic"));
                        }

                        if (jsonObject.has("mod")) {
                            identity.setMod(jsonObject.getInt("mod"));
                        }
                        identity.setUid(jsonObject.getLong("uid"));
                        identity.setUsername(jsonObject.getString("user"));
                        identity.setEmail(jsonObject.getString("email"));
                        identity.setSessionid(jsonObject.getString("sessionid"));

                        GlobalFunctions.broadcastToNotifications(getApplicationContext(), serverIndex);

                        setResult(RESULT_OK, new Intent().putExtra("serverIndex", serverIndex));
                        if (PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getBoolean("remember", false)) {
                            SharedPreferences.Editor editor = getSharedPreferences("sessions", MODE_PRIVATE).edit();
                            editor.putString(GlobalFunctions.servers.get(serverIndex).getName(), identity.getSessionid());
                            editor.apply();
                        }
                        finish();
                    } else if (status == 4) {
                        SharedPreferences.Editor editor = getSharedPreferences("sessions", MODE_PRIVATE).edit();
                        editor.remove(GlobalFunctions.servers.get(serverIndex).getName());
                        editor.apply();
                    }
                    else {
                        setError(R.string.errInvalidCreds);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                setError(R.string.errNoAccess);
            }
        }
    }
}
