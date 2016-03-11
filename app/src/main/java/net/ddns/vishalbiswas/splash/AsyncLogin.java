package net.ddns.vishalbiswas.splash;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class AsyncLogin extends AsyncTask<String, Void, JSONObject> {
    final String loginURL = String.format("%s/login.php", GlobalFunctions.getServer());
    private Handler handler;

    @Override
    protected JSONObject doInBackground(String... params) {
        String username = params[0];
        String password = params[1];
        String postMessage = String.format("user=%s&pass=%s", username, password);

        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(loginURL);
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
                    GlobalFunctions.setName(jsonObject.getString("name"));
                    /*if (jsonObject.has("profpic")) {
                        GlobalFunctions.setProfpic((Drawable) jsonObject.get("profpic"));
                    } else {
                        GlobalFunctions.setProfpic(null);
                    }*/
                    GlobalFunctions.setUid(jsonObject.getInt("uid"));
                    GlobalFunctions.setUsername(jsonObject.getString("user"));
                    GlobalFunctions.isSessionAlive = true;
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
