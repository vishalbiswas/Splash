package vishal.chetan.splash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.SparseArray;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;

import org.json.JSONArray;
import org.json.JSONObject;

import vishal.chetan.splash.android.SettingsActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GlobalFunctions extends Application {
    public static ConnectivityManager connMan;
    @Nullable
    private static Locale locale;
    private static HTTP_CODE regNameStatus;
    private static HTTP_CODE regEmailStatus;
    public static SessionState sessionState = SessionState.UNKNOWN;

    public static final ServerList servers = ServerList.getInstance();
    public static final SparseArray<UserIdentity> identities = new SparseArray<>();
    @Nullable
    public static UserIdentity defaultIdentity;

    private static final AndDown andDown = new AndDown();
    private static final int andDownExts = AndDown.HOEDOWN_EXT_AUTOLINK |
            AndDown.HOEDOWN_EXT_FENCED_CODE | AndDown.HOEDOWN_EXT_TABLES |
            AndDown.HOEDOWN_EXT_QUOTE | AndDown.HOEDOWN_EXT_STRIKETHROUGH |
            AndDown.HOEDOWN_EXT_SUPERSCRIPT | AndDown.HOEDOWN_EXT_UNDERLINE |
            AndDown.HOEDOWN_EXT_FOOTNOTES | AndDown.HOEDOWN_EXT_HIGHLIGHT |
            AndDown.HOEDOWN_EXT_NO_INTRA_EMPHASIS;

    public static HTTP_CODE getRegNameStatus() {
        return regNameStatus;
    }

    public static void setRegNameStatus(HTTP_CODE regNameStatus) {
        GlobalFunctions.regNameStatus = regNameStatus;
    }

    public static void lookupLocale(@NonNull Context con) {
        if (locale == null) initializeData(con);
        final Configuration config = new Configuration();
        config.locale = locale;
        ((Activity) con).getBaseContext().getResources().updateConfiguration(config, ((Activity) con).getBaseContext().getResources().getDisplayMetrics());
    }

    public static void initializeData(@NonNull Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        locale = new Locale(preferences.getString("locale", "en"));
    }

    public static void launchSettings(@NonNull Context context) {
        final Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void showSnack(@NonNull Context context) {
        assert locale != null;
        if (!PreferenceManager.getDefaultSharedPreferences(context).getString("locale", "en").equals(locale.getLanguage())) {
            Snackbar.make(((Activity) context).findViewById(R.id.frag), R.string.restartNotify, Snackbar.LENGTH_LONG).show();
        }
    }

    public static HTTP_CODE getRegEmailStatus() {
        return regEmailStatus;
    }

    public static void setRegEmailStatus(HTTP_CODE regEmailStatus) {
        GlobalFunctions.regEmailStatus = regEmailStatus;
    }

    public static boolean getRegStatus() {
        return regNameStatus == HTTP_CODE.SUCCESS && regEmailStatus == HTTP_CODE.SUCCESS;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Set<String> sources = getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>());
        for (String key :
                sources) {
            String sourceUrl = getSharedPreferences("sources", MODE_PRIVATE).getString(key, null);
            if (sourceUrl == null) continue;
            servers.add(new ServerList.SplashSource(key, sourceUrl));
        }

        defaultIdentity = new UserIdentity(preferences.getString("defaultUsername", "SplashUser"),
                preferences.getString("defaultFname", ""),
                preferences.getString("defaultLname", ""),
                preferences.getString("defaultEmail", ""));
        servers.addListener(new ServerList.OnServerListChangeListener() {
            @Override
            public void onAdd(@NonNull ServerList.SplashSource source) {
                Set<String> sourceKeys = new HashSet<>(getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>()));
                if (!sourceKeys.contains(source.getName())) {
                    sourceKeys.add(source.getName());
                    getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putStringSet("sourceKeys", sourceKeys).apply();
                    getSharedPreferences("sources", Context.MODE_PRIVATE).edit()
                            .putString(source.getName(), source.getUrl()).apply();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.strSrcExists), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRemove(@NonNull ServerList.SplashSource source) {
                Set<String> sourceKeys = new HashSet<>(getSharedPreferences("settings", MODE_PRIVATE).getStringSet("sourceKeys", new HashSet<String>()));
                sourceKeys.remove(source.getName());
                getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putStringSet("sourceKeys", sourceKeys).apply();
                getSharedPreferences("sources", Context.MODE_PRIVATE).edit().remove(source.getName()).apply();
            }

            @Override
            public void onUpdate(@NonNull ServerList.SplashSource previousSource, @NonNull ServerList.SplashSource updatedSource) {
                onRemove(previousSource);
                onAdd(updatedSource);
            }
        });
    }

    public static String parseMarkdown(String data) {
        return andDown.markdownToHtml(data, andDownExts, 0);
    }

    public static Date parseDate(String date) {
        assert locale != null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", locale);
        try {
            return format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class CheckSource extends AsyncTask<Integer, Void, Void> {
        ServerList.SplashSource source;
        final Activity activity;

        public CheckSource(Activity activity) {
            this.activity = activity;
        }

        @Nullable
        @Override
        protected Void doInBackground(final Integer... params) {
            source = GlobalFunctions.servers.get(params[0]);
            boolean result = false;
            NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                URL urlServer;
                HttpURLConnection urlConn;
                try {
                    urlServer = new URL(source.getUrl() + "/topics");
                    urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                    urlConn.connect();
                    if (urlConn.getResponseCode() != 200) {
                        throw new Exception();
                    } else {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                        String line;
                        StringBuilder response = new StringBuilder();

                        while ((line = bufferedReader.readLine()) != null) {
                            response.append(line);
                        }
                        bufferedReader.close();
                        JSONArray data = new JSONArray(response.toString());
                        for(int i = 0; i < data.length(); ++i) {
                            JSONObject topic = data.getJSONObject(i);
                            source.addTopic(topic.getInt("topicid"), topic.getString("name"));
                        }
                        result = true;
                    }
                } catch (Exception e) {
                    result = false;
                }
            }
            if (!result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "Error occurred while connecting to " + source.getName() + ". Disabling...", Toast.LENGTH_LONG).show();
                        GlobalFunctions.servers.setDisabled(params[0], true);
                    }
                });
            }
            return null;
        }
    }

    public enum HTTP_CODE {
        SUCCESS,
        FAILED,
        NO_ACCESS,
        REQUEST_FAILED,
        BUSY,
        UNKNOWN
    }

    public enum SessionState {
        ALIVE,
        DEAD,
        UNKNOWN
    }
}
