package vishal.chetan.splash.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.w3c.dom.Text;

import vishal.chetan.splash.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView) findViewById(R.id.myLicense)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.bypassLicense)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}
