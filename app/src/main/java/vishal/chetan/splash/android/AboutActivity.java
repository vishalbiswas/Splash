package vishal.chetan.splash.android;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import vishal.chetan.splash.R;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView) findViewById(R.id.myLicense)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.anddownLicense)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.htmltvLicense)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}
