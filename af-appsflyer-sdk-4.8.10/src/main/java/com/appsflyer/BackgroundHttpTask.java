package com.appsflyer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author by golan on 5/21/15.
 */
class BackgroundHttpTask extends AsyncTask<String, Void, String> {
    private boolean mTrackingStopped = false;
    private static final int WAIT_TIMEOUT = 30 * 1000;
    Map<String, String> bodyParameters;
    String bodyAsString;
    private String content = "";
    private boolean error = false;
    private WeakReference<Context> context;
    private URL url;
    private boolean proxyMode;
    private HttpURLConnection conn;

    private boolean shouldReadResponse;

    BackgroundHttpTask(Context context, boolean isTrackingStopped) {
        this.context = new WeakReference<>(context);
        proxyMode = true;
        shouldReadResponse = true;
        mTrackingStopped = isTrackingStopped;
    }

    protected void onPreExecute() {
        if (bodyAsString == null) {
            JSONObject jsonObject = new JSONObject(this.bodyParameters);
            bodyAsString = jsonObject.toString();
        }
    }

    protected String doInBackground(String... urls) {
        // Check if the message should be sent
        if (mTrackingStopped) {
            return null;
        }

        try {
            url = new URL(urls[0]);
            if (proxyMode) {
                ProxyManager.getInstance().addServerRequestEvent(url.toString(), bodyAsString);
                int sizeOfBody = bodyAsString.getBytes("UTF-8").length;
                LogMessages.logMessageMaskKey("call = " + url + "; size = " + sizeOfBody + " byte" + (sizeOfBody > 1 ? "s" : "") + "; body = " + bodyAsString);

            }

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(WAIT_TIMEOUT);
            conn.setConnectTimeout(WAIT_TIMEOUT);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(bodyAsString);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (shouldReadResponse) {
                content = AppsFlyerLib.getInstance().readServerResponse(conn);
            }
            if (proxyMode) {
                ProxyManager.getInstance().addServerResponseEvent(url.toString(), responseCode, content);
            }

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                AFLogger.afInfoLog("Status 200 ok");
                Context contextObj = context.get();
                if (url.toString().startsWith(ServerConfigHandler.getUrl(AppsFlyerLib.REGISTER_URL)) && contextObj != null) {
                    SharedPreferences sp = contextObj.getSharedPreferences(AppsFlyerLib.AF_SHARED_PREF, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean(AppsFlyerProperties.AF_UNINSTALL_TOKEN_SENT, true);
                    editor.apply();
                    AFLogger.afDebugLog("Successfully registered for Uninstall Tracking");
                }
            } else {
                error = true;
            }
        } catch (Throwable t) {
            AFLogger.afErrorLog("Error while calling " + url.toString(), t);
            error = true;
        }

        return content;
    }

    protected void onCancelled() {
    }

    protected void onPostExecute(String response) {
        if (error) {
            AFLogger.afInfoLog("Connection error: " + response);
        } else {
            AFLogger.afInfoLog("Connection call succeeded: " + response);
        }
    }

    void setProxyMode(boolean proxyMode) {
        this.proxyMode = proxyMode;
    }

    HttpURLConnection getConnection() {
        return conn;
    }

    void setShouldReadResponse(boolean shouldReadResponse) {
        this.shouldReadResponse = shouldReadResponse;
    }

}