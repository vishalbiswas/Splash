package net.ddns.vishalbiswas.splash.classes.asyncs;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;
import net.ddns.vishalbiswas.splash.classes.UsernameCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class AsyncGetUser extends AsyncTask<Integer, Void, JSONObject> {
    private final String getuserPath = "/getuser.php";
    private int serverIndex;
    private Handler handler;

    @Override
    protected JSONObject doInBackground(Integer... params) {
        int uid = params[1];
        serverIndex = params[0];
        String postMessage = String.format(Locale.ENGLISH, "uid=%d", uid);

        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(GlobalFunctions.servers.get(serverIndex) + getuserPath);
                HttpURLConnection webservice = (HttpURLConnection) url.openConnection();
                webservice.setRequestMethod("POST");
                webservice.setConnectTimeout(3000);
                webservice.setDoOutput(true);
                OutputStream outputStream = webservice.getOutputStream();
                outputStream.write(postMessage.getBytes());
                outputStream.flush();
                outputStream.close();

                if (webservice.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(webservice.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufferedReader.close();
                    return (new JSONObject(response.toString()));
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                return (new JSONObject("{status:6,msg:\"No Access\"}"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (jsonObject != null) {
            int uid;
            try {
                uid = jsonObject.getInt("uid");
            } catch (JSONException ex) {
                handler.sendEmptyMessage(-2);
                return;
            }
            try {
                String name = jsonObject.getString("user");
                UsernameCache.setUser(serverIndex, uid, name);
            } catch (JSONException ex) {
                handler.sendEmptyMessage(uid);
            }
        }
        else {
            handler.sendEmptyMessage(-1);
        }
        handler.sendEmptyMessage(0);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
