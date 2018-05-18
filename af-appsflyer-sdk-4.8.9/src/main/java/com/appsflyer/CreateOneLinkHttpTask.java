package com.appsflyer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.appsflyer.share.Constants;
import com.appsflyer.share.LinkGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static com.appsflyer.share.Constants.URL_SITE_ID;

/**
 * Create shortened OneLink tracking link url
 *
 * @author oleksandr Dudinskyi (oleksandr.d@appsflyer.com)
 */
public class CreateOneLinkHttpTask extends OneLinkHttpTask {

    private static final String TRACKING_LINK_LIVE_TIME_KEY = "ttl";
    private static final String TRACKING_LINK_DATA_KEY = "data";
    private ResponseListener responseListener;
    private Map<String, String> data;
    private String ttl;
    private String packageName = "";
    private Context context;
    private boolean mTrackingStopped = false;


    public CreateOneLinkHttpTask(@NonNull String oneLinkId,
                                 @NonNull Map<String, String> data,
                                 AppsFlyerLib afLib, @NonNull Context context, boolean isTrackingStopped) {
        super(afLib);
        mTrackingStopped = isTrackingStopped;
        this.context = context;

        if (this.context != null) {
            this.packageName = context.getPackageName();
        } else {
            AFLogger.afWarnLog("CreateOneLinkHttpTask: context can't be null");
        }
        this.oneLinkId = oneLinkId;
        this.ttl = "-1";
        this.data = data;
    }

    public void setListener(@NonNull ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    void initRequest(HttpsURLConnection conn) throws JSONException, IOException {
        if (mTrackingStopped){
            return;
        }
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        JSONObject output = new JSONObject();
        JSONObject dataObject = new JSONObject(data);
        output.put(TRACKING_LINK_LIVE_TIME_KEY, this.ttl);
        output.put(TRACKING_LINK_DATA_KEY, dataObject);
        conn.connect();

        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(output.toString());
        wr.flush();
        wr.close();
    }

    @Override
    String getOneLinkUrl() {
        return ServerConfigHandler.getUrl(BASE_URL) + "/" + oneLinkId;
    }

    @Override
    void handleResponse(String content) {
        try {
            JSONObject responseObj = new JSONObject(content);
            Iterator<String> keys = responseObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                responseListener.onResponse(responseObj.optString(key));
            }
        } catch (JSONException e) {
            responseListener.onResponseError("Can't parse one link data");
            AFLogger.afErrorLog("Error while parsing to json " + content, e);
        }
    }

    @Override
    void onErrorResponse() {

        final String domain = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_DOMAIN);

        LinkGenerator lg = new LinkGenerator(Constants.USER_INVITE_LINK_TYPE)
                .setBaseURL(oneLinkId, domain, packageName)
                .addParameter(URL_SITE_ID,packageName)
                .addParameters(data);

        String customerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID);

        if(customerUserId != null){
            lg.setReferrerCustomerId(customerUserId);
        }
        final String link = lg.generateLink();


        responseListener.onResponse(link);
    }

    /**
     * Listener for short one link attribution response
     */
    public interface ResponseListener {
        @WorkerThread
        void onResponse(String oneLinkUrl);

        @WorkerThread
        void onResponseError(String message);
    }
}