package vishal.chetan.splash;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.commonsware.cwac.anddown.AndDown;

import org.json.JSONArray;
import org.json.JSONObject;

import vishal.chetan.splash.android.NotificationReceiver;
import vishal.chetan.splash.android.SettingsActivity;
import vishal.chetan.splash.android.SourcesManagerActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GlobalFunctions extends Application {
    public static ConnectivityManager connMan;
    public static SharedPreferences preferences;

    @Nullable
    public static Locale getLocale() {
        return locale;
    }

    @Nullable
    private static Locale locale;
    private static HTTP_CODE regNameStatus = HTTP_CODE.UNKNOWN;
    private static HTTP_CODE regEmailStatus = HTTP_CODE.UNKNOWN;
    public static final ServerList servers = ServerList.getInstance();
    @Nullable
    public static UserIdentity defaultIdentity;

    private static final AndDown andDown = new AndDown();
    private static final int andDownExts = AndDown.HOEDOWN_EXT_AUTOLINK |
            AndDown.HOEDOWN_EXT_FENCED_CODE | AndDown.HOEDOWN_EXT_TABLES |
            AndDown.HOEDOWN_EXT_QUOTE | AndDown.HOEDOWN_EXT_STRIKETHROUGH |
            AndDown.HOEDOWN_EXT_SUPERSCRIPT | AndDown.HOEDOWN_EXT_UNDERLINE |
            AndDown.HOEDOWN_EXT_FOOTNOTES | AndDown.HOEDOWN_EXT_HIGHLIGHT |
            AndDown.HOEDOWN_EXT_NO_INTRA_EMPHASIS;

    public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());


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
        con.getResources().updateConfiguration(config, con.getResources().getDisplayMetrics());
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
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        lookupLocale(this);
        initializeData(this);

        if (BuildConfig.BUILD_TYPE.equals("debug") && GlobalFunctions.servers.size() == 0) {
            if (Build.PRODUCT.startsWith("sdk") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://10.0.2.1:5000"));
            } else {
                GlobalFunctions.servers.add(new ServerList.SplashSource("TESTING", "http://192.168.1.2:5000"));
            }
        }

        if (GlobalFunctions.servers.size() == 0) {
            Toast.makeText(this, R.string.errNoLoginServer, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SourcesManagerActivity.class));
        } else {
            for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                new GlobalFunctions.CheckSource(this).execute(i);
            }
        }
    }

    public static void broadcastToNotifications(Context context, int serverIndex) {
        int intervalSeconds = preferences.getInt("notificationInterval", 60);

        Intent i = new Intent(context, NotificationReceiver.class).putExtra("serverIndex", serverIndex);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, 0);

        if (intervalSeconds > 0) {
            if (GlobalFunctions.servers.get(serverIndex).identity != null) {
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 100 * intervalSeconds * 10, pi);
            } else {
                alarmManager.cancel(pi);
            }
        } else {
            alarmManager.cancel(pi);
        }
    }

    public static String parseMarkdown(String data) {
        return andDown.markdownToHtml(data, andDownExts, 0);
    }

    public static class CheckSource extends AsyncTask<Integer, Void, Boolean> {
        ServerList.SplashSource source;
        final Context context;
        private int serverIndex = -1;

        public CheckSource(Context context) {
            this.context = context;
        }

        @Nullable
        @Override
        protected Boolean doInBackground(final Integer... params) {
            serverIndex = params[0];
            source = GlobalFunctions.servers.get(serverIndex);
            boolean result = false;
            NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                URL urlServer;
                HttpURLConnection urlConn;
                try {
                    urlServer = new URL(source.getUrl() + "/banner");
                    urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000);
                    urlConn.connect();
                    if (urlConn.getResponseCode() == 200) {
                        source.banner = BitmapFactory.decodeStream(urlConn.getInputStream());
                    }
                } catch (Exception ignored) { }

                try {
                    urlServer = new URL(source.getUrl() + "/topics");
                    urlConn = (HttpURLConnection) urlServer.openConnection();
                    urlConn.setConnectTimeout(3000);
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
                        source.clearTopics();
                        for (int i = 0; i < data.length(); ++i) {
                            JSONObject topic = data.getJSONObject(i);
                            source.addTopic(topic.getInt("topicid"), topic.getString("name"));
                        }
                        result = true;
                    }
                } catch (Exception e) {
                    result = false;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(context, source.getName() + context.getString(R.string.strNoResponse), Toast.LENGTH_LONG).show();
                GlobalFunctions.servers.setDisabled(serverIndex, true);
            }
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
}
