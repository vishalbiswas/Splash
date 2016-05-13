package net.ddns.vishalbiswas.splash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {
    final int requestCode = 0x0421;
    final private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    UserDisplayFragment.updateViews();
                    finish();
                    break;
                case 1:
                    setError(R.string.errUnknown);
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
    ImageView imgPic;
    Bitmap profPic;
    EditText editFName;
    EditText editLName;

    private void setError(int resId) {
        View view = findViewById(android.R.id.content);
        assert view != null;
        Snackbar.make(view, getString(resId), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgPic = (ImageView) findViewById(R.id.imgPic);
        editFName = (EditText) findViewById(R.id.editFName);
        editLName = (EditText) findViewById(R.id.editLName);

        if (GlobalFunctions.getProfpic() != null) {
            profPic = GlobalFunctions.getProfpic();
            imgPic.setImageBitmap(profPic);
        }
        editFName.setText(GlobalFunctions.getFirstname());
        editLName.setText(GlobalFunctions.getLastname());

        imgPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, requestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK)
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                profPic = BitmapFactory.decodeStream(stream);
                imgPic.setImageBitmap(profPic);
                assert stream != null;
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
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

        switch (id) {
            case R.id.action_settings:
                GlobalFunctions.launchSettings(ProfileActivity.this);
                return true;
            case R.id.menuSave:
                AsyncUpdate update = new AsyncUpdate();
                update.setHandler(handler);
                update.execute(profPic, editFName.getText(), editLName.getText());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
