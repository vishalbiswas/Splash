package net.ddns.vishalbiswas.splash.classes.asyncs;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import net.ddns.vishalbiswas.splash.classes.GlobalFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncRegister extends AsyncTask<Object, Void, JSONObject> {
    private final String registerPath = "/signup.php";
    private Handler handler;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        String username = params[1].toString();
        String email = params[2].toString();
        String password = params[3].toString();
        String postMessage = String.format("name=%s&email=%s&pwd=%s", username, email, password);

        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(GlobalFunctions.servers.get((int)params[0]) + registerPath);
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
            try {
                int status = jsonObject.getInt("status");
                handler.sendEmptyMessage((status));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            handler.sendEmptyMessage(6);
        }
    }
}
