package net.ddns.vishalbiswas.splash;

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

class CheckAvailable extends AsyncTask<String, Void, Void> {
    final String checkURL = "http://vishalbiswas.asuscomm.com/checkuser.php";
    public Handler handler;

    @Override
    protected void onPreExecute() {
        GlobalFunctions.setRegStatus(GlobalFunctions.HTTP_CODE.BUSY);
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            URL url = new URL(checkURL);
            HttpURLConnection webservice = (HttpURLConnection) url.openConnection();
            webservice.setRequestMethod("POST");
            webservice.setConnectTimeout(3000);
            String postMessage = String.format("user=%s", params[0]);

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
                JSONObject jsonObject = new JSONObject(response.toString());
                Boolean isAvailable = jsonObject.getBoolean("user");

                if (isAvailable) {
                    GlobalFunctions.setRegStatus(GlobalFunctions.HTTP_CODE.SUCCESS);
                } else {
                    GlobalFunctions.setRegStatus(GlobalFunctions.HTTP_CODE.FAILED);
                }
            } else {
                GlobalFunctions.setRegStatus(GlobalFunctions.HTTP_CODE.REQUEST_FAILED);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            GlobalFunctions.setRegStatus(GlobalFunctions.HTTP_CODE.UNKNOWN);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        handler.sendEmptyMessage(0);
    }
}