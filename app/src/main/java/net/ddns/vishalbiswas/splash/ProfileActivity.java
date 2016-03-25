package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {
    ImageView imgPic;
    EditText editFName;
    EditText editLName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgPic = (ImageView) findViewById(R.id.imgPic);
        editFName = (EditText) findViewById(R.id.editFName);
        editLName = (EditText) findViewById(R.id.editLName);

        imgPic.setImageBitmap(GlobalFunctions.getProfpic());
        editFName.setText(GlobalFunctions.getFirstname());
        editLName.setText(GlobalFunctions.getLastname());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            GlobalFunctions.launchSettings(ProfileActivity.this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
