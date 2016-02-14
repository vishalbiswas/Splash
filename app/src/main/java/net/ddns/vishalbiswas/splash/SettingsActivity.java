package net.ddns.vishalbiswas.splash;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    static void bindSummary(Preference preference, String newValue) {
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().add(R.id.frag, new GeneralPreferences()).commit();

    }

    @Override
    protected void onStop() {
        GlobalFunctions.showSnack();
        super.onStop();
    }

    public static class GeneralPreferences extends PreferenceFragment {
        public static Preference.OnPreferenceChangeListener bindSummaryChange = new Preference.OnPreferenceChangeListener() {
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
            //for (int i = 0;i<getPreferenceScreen().getPreferenceCount();++i) getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(bindSummaryChange);
            ListPreference listPreference = (ListPreference) findPreference("locale");
            listPreference.setOnPreferenceChangeListener(bindSummaryChange);
            bindSummary(listPreference, PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("locale", "en"));
            //listPreference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue(]);
        }

    }

}
