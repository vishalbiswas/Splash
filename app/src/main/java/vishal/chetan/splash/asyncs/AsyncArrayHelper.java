package vishal.chetan.splash.asyncs;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import vishal.chetan.splash.GlobalFunctions;

public abstract class AsyncArrayHelper extends AsyncTask<Void, Void, JSONArray> {
    protected final int serverIndex;
    private final String pageUrl;
    private String postMessage = null;

    protected AsyncArrayHelper(int serverIndex, String pageUrl) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
    }

    protected AsyncArrayHelper(int serverIndex, String pageUrl, String postMessage) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
        this.postMessage = postMessage;
    }

    protected void workInBackground(JSONArray jsonArray) {}

    @Nullable
    @Override
    protected JSONArray doInBackground(Void... params) {
        JSONArray result = null;
        String serverAddress;
        try {
            serverAddress = GlobalFunctions.servers.get(serverIndex).getUrl();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                String completeUrl = String.format("%s/%s", serverAddress, pageUrl);
                URL url = new URL(completeUrl);
                HttpsURLConnection webservice = (HttpsURLConnection) url.openConnection();
                webservice.setConnectTimeout(3000);
                if (postMessage != null) {
                    webservice.setRequestMethod("POST");
                    webservice.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    webservice.setDoOutput(true);
                    OutputStream outputStream = webservice.getOutputStream();
                    outputStream.write(postMessage.getBytes());
                    outputStream.flush();
                    outputStream.close();
                } else {
                    webservice.setRequestMethod("GET");
                }
                if (webservice.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(webservice.getInputStream()));
                    String line;
                    StringBuilder response = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    bufferedReader.close();
                    result = new JSONArray(response.toString());
                }
            } catch (@NonNull IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        workInBackground(result);
        return result;
    }
}