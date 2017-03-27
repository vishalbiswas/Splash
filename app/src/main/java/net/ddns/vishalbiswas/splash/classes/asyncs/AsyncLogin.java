package net.ddns.vishalbiswas.splash.classes.asyncs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Base64;

import net.ddns.vishalbiswas.splash.classes.UserIdentity;
import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncLogin extends AsyncTask<Object, Void, JSONObject> {
    private final String loginPath = "/login.php";
    private Handler handler;
    private int serverIndex;

    @Override
    protected JSONObject doInBackground(Object... params) {
        serverIndex = (int)params[0];
        String username = params[1].toString();
        String password = params[2].toString();
        String postMessage = String.format("user=%s&pass=%s", username, password);

        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(GlobalFunctions.servers.get(serverIndex) + loginPath);
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
    public void onPostExecute(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                int status = jsonObject.getInt("status");

                if (status == 0) {
                    if (GlobalFunctions.identities.get(serverIndex) == null) {
                        GlobalFunctions.identities.append(serverIndex, new UserIdentity());
                    }

                    if (jsonObject.has("fname")) {
                        GlobalFunctions.identities.get(serverIndex).setFirstname(jsonObject.getString("fname"));
                    } else {
                        GlobalFunctions.identities.get(serverIndex).setFirstname("");
                    }
                    if (jsonObject.has("lname")) {
                        GlobalFunctions.identities.get(serverIndex).setLastname(jsonObject.getString("lname"));
                    } else {
                        GlobalFunctions.identities.get(serverIndex).setLastname("");
                    }

                    if (jsonObject.has("profpic")) {
                        byte[] picBytes = Base64.decode(jsonObject.getString("profpic"), Base64.DEFAULT);
                        Bitmap profpic = BitmapFactory.decodeByteArray(picBytes, 0, picBytes.length);
                        GlobalFunctions.identities.get(serverIndex).setProfpic(profpic);
                    } else {
                        GlobalFunctions.identities.get(serverIndex).setProfpic(null);
                    }
                    GlobalFunctions.identities.get(serverIndex).setUid(jsonObject.getInt("uid"));
                    GlobalFunctions.identities.get(serverIndex).setUsername(jsonObject.getString("user"));
                    GlobalFunctions.identities.get(serverIndex).setEmail(jsonObject.getString("email"));
                }

                handler.sendEmptyMessage(status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            handler.sendEmptyMessage(6);
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
