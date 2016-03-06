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
    int message = -1;
    private Handler handler;

    @Override
    protected Void doInBackground(String... params) {
        Boolean checkForUser = true;
        String postMessage = String.format("user=%s", params[0]);

        if (params.length == 2) {
            if (params[1].equals("email")) {
                postMessage = String.format("email=%s", params[0]);
                message = -2;
                checkForUser = false;
            }
        }

        if (checkForUser) {
            GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.BUSY);
        } else {
            GlobalFunctions.setRegEmailStatus(GlobalFunctions.HTTP_CODE.BUSY);
        }

        try {
            URL url = new URL(checkURL);
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
                JSONObject jsonObject = new JSONObject(response.toString());
                Boolean isAvailable;
                if (checkForUser) {
                    isAvailable = jsonObject.getBoolean("user");
                } else {
                    isAvailable = jsonObject.getBoolean("email");
                }

                if (isAvailable) {
                    if (checkForUser) {
                        GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.SUCCESS);
                    } else {
                        GlobalFunctions.setRegEmailStatus(GlobalFunctions.HTTP_CODE.SUCCESS);
                    }
                } else {
                    if (checkForUser) {
                        GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.FAILED);
                    } else {
                        GlobalFunctions.setRegEmailStatus(GlobalFunctions.HTTP_CODE.FAILED);
                    }
                }
            } else {
                if (checkForUser) {
                    GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.REQUEST_FAILED);
                } else {
                    GlobalFunctions.setRegEmailStatus(GlobalFunctions.HTTP_CODE.REQUEST_FAILED);
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (checkForUser) {
                GlobalFunctions.setRegNameStatus(GlobalFunctions.HTTP_CODE.UNKNOWN);
            } else {
                GlobalFunctions.setRegEmailStatus(GlobalFunctions.HTTP_CODE.UNKNOWN);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        getHandler().sendEmptyMessage(message);
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}