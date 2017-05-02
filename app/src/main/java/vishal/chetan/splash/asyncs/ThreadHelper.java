package vishal.chetan.splash.asyncs;

import android.net.NetworkInfo;
import android.support.annotation.NonNull;

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

public abstract class ThreadHelper implements Runnable {
    private static final String boundary = "mainBoundary";

    private int serverIndex;
    private String pageUrl;
    private String postMessage = null;
    private boolean rawPost = false;

    protected ThreadHelper(int serverIndex, String pageUrl) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
    }

    protected ThreadHelper(int serverIndex, String pageUrl, String postMessage) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
        this.postMessage = postMessage;
    }

    protected ThreadHelper(int serverIndex, String pageUrl, boolean rawPost) {
        this.serverIndex = serverIndex;
        this.pageUrl = pageUrl;
        this.rawPost = rawPost;
    }



    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        JSONObject result = null;
        String serverAddress;
        try {
            serverAddress = GlobalFunctions.servers.get(serverIndex).getUrl();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }
        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                String completeUrl = String.format("%s/%s", serverAddress, pageUrl);
                URL url = new URL(completeUrl);
                HttpURLConnection webservice = (HttpURLConnection) url.openConnection();
                webservice.setConnectTimeout(3000);
                if (postMessage != null || rawPost) {
                    webservice.setRequestMethod("POST");
                    webservice.setDoOutput(true);
                    OutputStream outputStream;
                    if (postMessage != null) {
                        webservice.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        outputStream = webservice.getOutputStream();
                        outputStream.write(postMessage.getBytes());
                    } else {
                        webservice.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        outputStream = webservice.getOutputStream();
                        outputStream.write(("--" + boundary + "\r\n").getBytes());
                        workOutput(outputStream);
                        outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
                    }
                    outputStream.flush();
                    outputStream.close();
                } else {
                    webservice.setRequestMethod("GET");
                }
                if (webservice.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = workInput(webservice.getInputStream());
                } else {
                    result = new JSONObject("{status:5,msg:\"Internal error");
                }

            } catch (@NonNull IOException | JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                result = new JSONObject("{status:6,msg:\"No Access\"}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        doWork(result);
    }

    abstract protected void doWork(JSONObject jsonObject);

    protected JSONObject workInput(InputStream rawInputStream) throws JSONException {
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

    protected void workOutput(OutputStream rawOutputStream) throws IOException {}
}
