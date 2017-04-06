package vishal.chetan.splash.asyncs;

import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;

import vishal.chetan.splash.GlobalFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncUpdate extends AsyncTask<Object, Void, JSONObject> {
    private final static String updatePath = "/update.php";
    private final static String crlf = "\r\n";
    private final static String twoHyphens = "--";
    private final static String mainBoundary = "mainBoundary";
    final static String subBoundary = "subBoundary";
    private Handler handler;
    private int serverIndex;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected JSONObject doInBackground(Object... params) {
        if (GlobalFunctions.servers.size() == 0) return null;
        serverIndex = (int) params[0];
        Bitmap profpic = (Bitmap) params[1];
        String firstname = params[2].toString().trim();
        String lastname = params[3].toString().trim();
        int uid = GlobalFunctions.identities.get(serverIndex).getUid();
        String postMessage = String.format("uid=%s&fname=%s&lname=%s", String.valueOf(uid), firstname, lastname);


        NetworkInfo netInfo = GlobalFunctions.connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                HttpURLConnection webservice = (HttpURLConnection) (new URL(GlobalFunctions.servers.get(serverIndex).getUrl() + updatePath)).openConnection();
                webservice.setRequestMethod("POST");
                webservice.setUseCaches(false);
                webservice.setDoOutput(true);
                /*webservice.setDoInput(true);*/
                webservice.setRequestProperty("Connection", "Keep-Alive");
                webservice.setRequestProperty("Cache-Control", "no-cache");
/*                outputStream.writeBytes(postMessage);
                outputStream.flush();

                if (profpic != GlobalFunctions.getProfpic() && profpic != null) {
                    outputStream.writeBytes("&profpic=");
                    profpic.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                }*/
                webservice.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + mainBoundary);
                DataOutputStream outputStream = new DataOutputStream(webservice.getOutputStream());

                outputStream.writeBytes(twoHyphens + mainBoundary + crlf);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"uid\"" + crlf);
                outputStream.writeBytes(crlf);
                outputStream.writeBytes("Content-Type: text/plain");
                outputStream.writeBytes(crlf);
                outputStream.writeInt(uid);
                outputStream.writeBytes(crlf);
                outputStream.flush();

                outputStream.writeBytes(twoHyphens + mainBoundary + crlf);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"fname\"" + crlf);
                outputStream.writeBytes(crlf);
                outputStream.writeBytes("Content-Type: text/plain");
                outputStream.writeBytes(crlf);
                outputStream.writeBytes(firstname);
                outputStream.writeBytes(crlf);
                outputStream.flush();

                outputStream.writeBytes(twoHyphens + mainBoundary + crlf);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"lname\"" + crlf);
                outputStream.writeBytes(crlf);
                outputStream.writeBytes("Content-Type: text/plain");
                outputStream.writeBytes(crlf);
                outputStream.writeBytes(lastname);
                outputStream.writeBytes(crlf);
                outputStream.flush();

                if (profpic != GlobalFunctions.identities.get(serverIndex).getProfpic()) {
                    outputStream.writeBytes(twoHyphens + mainBoundary + crlf);
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"profpic\"; filename=\"" + String.valueOf(uid) + ".jpg\"" + crlf);
                    outputStream.writeBytes(crlf);
                    outputStream.writeBytes("Content-Type: image/jpeg");
                    outputStream.writeBytes(crlf);
                    outputStream.writeBytes("Content-Transfer-Encoding: binary");
                    outputStream.writeBytes(crlf);
                    profpic.compress(Bitmap.CompressFormat.JPEG, 40, outputStream);
                    outputStream.writeBytes(crlf);
                    outputStream.flush();
                }

                outputStream.writeBytes(crlf);
                outputStream.writeBytes(twoHyphens + mainBoundary + twoHyphens + crlf);
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
                    webservice.disconnect();
                    JSONObject data = new JSONObject(response.toString());
                    if (data.getInt("status") == 0) {
                        data.put("fname", firstname);
                        data.put("lname", lastname);
                        data.put("profpic", profpic);
                    }
                    return data;
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
                        Bitmap profpic = (Bitmap) jsonObject.get("profpic");
                        GlobalFunctions.identities.get(serverIndex).setProfpic(profpic);
                    } else {
                        GlobalFunctions.identities.get(serverIndex).setProfpic(null);
                    }
                }

                handler.sendEmptyMessage(status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            handler.sendEmptyMessage(6);
        }
    }
}
