package vishal.chetan.splash.asyncs;

import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import vishal.chetan.splash.GlobalFunctions;

public abstract class AsyncHelper extends AsyncTask<Void, Void, JSONObject> {
    private final int serverIndex;
    private final String pageUrl;
    private String postMessage = null;

    @Override
    protected abstract void onPostExecute(JSONObject jsonObject);

    public AsyncHelper(int serverIndex, String pageUrl) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
    }

    public AsyncHelper(int serverIndex, String pageUrl, String postMessage) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
        this.postMessage = postMessage;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
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
                HttpURLConnection webservice = (HttpURLConnection) url.openConnection();
                webservice.setConnectTimeout(3000);
                if (postMessage != null) {
                    webservice.setRequestMethod("POST");
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
                    return (new JSONObject(response.toString()));
                } else {
                    return new JSONObject("{status:5,msg:\"Internal error");
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
}