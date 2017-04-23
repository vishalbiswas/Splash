package vishal.chetan.splash.asyncs;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import vishal.chetan.splash.GlobalFunctions;

public abstract class AsyncRawHelper extends AsyncTask<Void, Void, JSONObject> {
    public static final String boundary = "mainBoundary";

    private final int serverIndex;
    private final String pageUrl;
    private boolean isPost = false;

    public AsyncRawHelper(int serverIndex, String pageUrl, boolean isPost) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
        this.isPost = isPost;
    }

    @Nullable
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
                if (isPost) {
                    webservice.setRequestMethod("POST");
                    webservice.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    webservice.setDoOutput(true);
                    OutputStream outputStream = webservice.getOutputStream();
                    outputStream.write(("--" + boundary + "\r\n").getBytes());
                    workOutput(outputStream);
                    outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
                    outputStream.flush();
                    outputStream.close();
                } else {
                    webservice.setRequestMethod("GET");
                }
                if (webservice.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    JSONObject returnObject = workInput(webservice.getInputStream());
                    webservice.disconnect();
                    return returnObject;
                } else {
                    return new JSONObject("{status:5,msg:\"Internal error");
                }

            } catch (@NonNull IOException | JSONException e) {
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

    @NonNull
    protected JSONObject workInput(@NonNull InputStream rawInputStream) throws JSONException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(rawInputStream));
        String line;
        StringBuilder response = new StringBuilder();

        try {
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(response.toString());
    }

    protected void workOutput(OutputStream rawOutputStream) throws IOException {
    }

}
