package com.appsflyer;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Get short one link data form based on deep link data
 *
 * @author oleksandr Dudinskyi (oleksandr.d@appsflyer.com)
 *         TODO add abstraction for HttpURLConnection
 */
class GetOneLinkDataHttpTask extends OneLinkHttpTask {

    static final String ONE_LINK_SCHEMA = "onelink.me";
    private OneLinkDataListener oneLinkParametersListener;
    private String shortLinkId;

    GetOneLinkDataHttpTask(Uri uri, AppsFlyerLib afLib) {
        super(afLib);
        if (!TextUtils.isEmpty(uri.getHost()) && !TextUtils.isEmpty(uri.getPath())) {
            String[] path = uri.getPath().split("/");
            if (uri.getHost().contains(ONE_LINK_SCHEMA) && path.length == 3) {
                oneLinkId = path[1];
                shortLinkId = path[2];
            }
        }
    }

    void setListener(@NonNull OneLinkDataListener onGetOnLinkParameters) {
        this.oneLinkParametersListener = onGetOnLinkParameters;
    }

    boolean isShortLinkValid() {
        return !TextUtils.isEmpty(oneLinkId) && !TextUtils.isEmpty(shortLinkId);
    }

    @Override
    void initRequest(HttpsURLConnection conn) throws JSONException, IOException {
        conn.setRequestMethod("GET");
    }

    @Override
    String getOneLinkUrl() {
        return ServerConfigHandler.getUrl(BASE_URL) + "/" + oneLinkId + "?id=" + shortLinkId;
    }

    @Override
    void handleResponse(String content) {
        try {
            Map<String, String> dataMap = new HashMap<>();
            JSONObject responseObj = new JSONObject(content);
            Iterator<String> keys = responseObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                dataMap.put(key, responseObj.optString(key));
            }
            oneLinkParametersListener.onGetOneLinkParameters(dataMap);
        } catch (JSONException e) {
            oneLinkParametersListener.onGetOneLinkParametersError("Can't parse one link data");
            AFLogger.afErrorLog("Error while parsing to json " + content, e);
        }
    }

    @Override
    void onErrorResponse() {
        oneLinkParametersListener.onGetOneLinkParametersError(NO_CONNECTION_ERROR_MSG);
    }

    /**
     * Listener for short one link attribution response
     */
    interface OneLinkDataListener {
        void onGetOneLinkParameters(Map<String, String> data);

        void onGetOneLinkParametersError(String message);
    }
}