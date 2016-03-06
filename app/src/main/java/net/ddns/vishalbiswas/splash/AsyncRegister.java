package net.ddns.vishalbiswas.splash;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncRegister extends AsyncTask<String, Void, JSONObject> {
    final String registerURL = "http://vishalbiswas.asuscomm.com/signup.php";
    private Handler handler;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String username = params[0];
        String email = params[1];
        String password = params[2];
        String postMessage = String.format("name=%s&email=%s&pwd=%s", username, email, password);

        try {
            URL url = new URL(registerURL);
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

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                int status = jsonObject.getInt("status");
                Message msg = new Message();
                msg.what = status;
                getHandler().sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            handler.sendEmptyMessage(6);
        }
    }
}
