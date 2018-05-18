package com.appsflyer;

import android.text.TextUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Base class for one link feature
 *
 * @author oleksandr Dudinskyi (oleksandr.d@appsflyer.com)
 */

public abstract class OneLinkHttpTask implements Runnable {

    private static final int WAIT_TIMEOUT = 3 * 1000;
    private HttpsUrlConnectionProvider connectionProvider;
    static final String NO_CONNECTION_ERROR_MSG = "Can't get one link data";
    static final String BASE_URL = "https://onelink.%s/shortlink-sdk/v1";
    private AppsFlyerLib afLib;
    String oneLinkId;

    OneLinkHttpTask(AppsFlyerLib afLib) {
        this.afLib = afLib;
    }

    public void setConnProvider(HttpsUrlConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void run() {
        doRequest();
    }

    private void doRequest() {

        long timestamp = System.currentTimeMillis() / 1000;

        String content = "";
        String error = "";
        String oneLinkUrl = getOneLinkUrl();

        AFLogger.afRDLog("oneLinkUrl: " + oneLinkUrl);
        try {
            HttpsURLConnection conn = connectionProvider.getHttpsURLConnection(oneLinkUrl);
            conn.addRequestProperty("content-type", "application/json");
            conn.addRequestProperty("authorization", HashUtils.getOneLinkAuthorization(timestamp));
            conn.addRequestProperty("af-timestamp", String.valueOf(timestamp));
            conn.setReadTimeout(WAIT_TIMEOUT);
            conn.setConnectTimeout(WAIT_TIMEOUT);

            initRequest(conn);

            int responseCode = conn.getResponseCode();
            content = afLib.readServerResponse(conn);

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                AFLogger.afInfoLog("Status 200 ok");
            } else {
                error = "Response code = " + responseCode + " content = " + content;
            }
        } catch (Throwable t) {
            AFLogger.afErrorLog("Error while calling " + oneLinkUrl, t);
            error = "Error while calling " + oneLinkUrl + " stacktrace: " + t.toString();
        }
        if (TextUtils.isEmpty(error)) {
            AFLogger.afInfoLog("Connection call succeeded: " + content);
            handleResponse(content);
        } else {
            AFLogger.afWarnLog("Connection error: " + error);
            onErrorResponse();
        }
    }

    abstract void initRequest(HttpsURLConnection conn) throws JSONException, IOException;

    abstract String getOneLinkUrl();

    /**
     * Provide HttpsURLConnection using request url
     */
    public static class HttpsUrlConnectionProvider {
        HttpsURLConnection getHttpsURLConnection(String requestUrl) throws IOException {
            URL url = new URL(requestUrl);
            return (HttpsURLConnection) url.openConnection();
        }
    }

    abstract void handleResponse(String content);

    abstract void onErrorResponse();
}
