package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatButton;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.*;
import android.view.View.*;
import android.view.View;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarRegister);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart()
    {
        // TODO: Implement this method
        super.onStart();
        AppCompatButton regButton = (AppCompatButton) findViewById(R.id.regButton);
        regButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFields();
            }
        });
        
    }

    private boolean validateFields() {
        AppCompatEditText username = (AppCompatEditText) findViewById(R.id.regUser);
        AppCompatEditText email = (AppCompatEditText) findViewById(R.id.regEmail);
        AppCompatEditText password = (AppCompatEditText) findViewById(R.id.regPassword);
        
        if (username.length() == 0 || email.length()==0 || password.length() == 0) {
            Snackbar.make(findViewById(R.id.frag), R.string.errEmpty, Snackbar.LENGTH_LONG);
            return false;
        }
        
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
