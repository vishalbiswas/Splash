package vishal.chetan.splash.android;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import vishal.chetan.splash.GlobalFunctions;
import vishal.chetan.splash.R;

public class SettingsActivity extends AppCompatActivity {

    private static void bindSummary(Preference preference, String newValue) {
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue(newValue)]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalFunctions.lookupLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        getFragmentManager().beginTransaction().add(R.id.frag, new GeneralPreferences()).commit();

    }

    @Override
    protected void onStop() {
        GlobalFunctions.showSnack(SettingsActivity.this);
        super.onStop();
    }

    public static class GeneralPreferences extends PreferenceFragment {
        final public static Preference.OnPreferenceChangeListener bindSummaryChange = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                bindSummary(preference, (String) newValue);
                return true;
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            ListPreference listPreference = (ListPreference) findPreference("locale");
            listPreference.setOnPreferenceChangeListener(bindSummaryChange);
            bindSummary(listPreference, PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("locale", "en"));
        }

    }

}
