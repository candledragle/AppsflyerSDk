package com.appsflyer.share;

import android.content.Context;
import android.os.AsyncTask;

import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerProperties;
import com.appsflyer.ServerConfigHandler;
import com.appsflyer.ServerParameters;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gilmeroz on 13/01/2017.
 */

public class CrossPromotionHelper {

    private static final String CROSS_PROMOTION_PID = "af_cross_promotion";
    static final String IMPRESSION_DOMAIN = "https://impression.%s";

    public static void trackAndOpenStore(Context context, String promotedAppId, String campaign){
        trackAndOpenStore(context, promotedAppId, campaign , null);
    }

    public static void trackAndOpenStore(Context context,
                                         String promoted_app_id,
                                         String campaign,
                                         Map<String,String> userParams){
        LinkGenerator linkGenerator = createLinkGenerator(context,
                promoted_app_id,
                campaign,
                userParams,
                ServerConfigHandler.getUrl(Constants.BASE_URL_APP_APPSFLYER_COM));

        if(AppsFlyerProperties.getInstance()
                .getBoolean(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false)){
            AFLogger.afInfoLog("CustomerUserId not set, track And Open Store is disabled", true);
            return;
        }

        Map<String, Object> eventValue = new HashMap<>();
        if (userParams != null){
            eventValue.putAll(userParams);
        }
        eventValue.put("af_campaign", campaign);
        AppsFlyerLib.getInstance().trackEvent(context, "af_cross_promotion", eventValue);

        RedirectHandler redirectHandler = new RedirectHandler();
        new HTTPGetTask(redirectHandler, context, AppsFlyerLib.getInstance().isTrackingStopped()).execute(linkGenerator.generateLink());
    }

    //TODO: add device ID (make sure that it's sent)
    public static void trackCrossPromoteImpression(Context context,
                                                   String appID,
                                                   String campaign) {

        if(AppsFlyerProperties.getInstance()
                .getBoolean(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false)){
            AFLogger.afInfoLog("CustomerUserId not set, Promote Impression is disabled", true);
            return;
        }

        LinkGenerator linkGenerator = createLinkGenerator(context,
                appID,
                campaign,
                null,
                ServerConfigHandler.getUrl(IMPRESSION_DOMAIN));
        new HTTPGetTask(null, null, AppsFlyerLib.getInstance().isTrackingStopped()).execute(linkGenerator.generateLink());
    }

    static LinkGenerator createLinkGenerator(Context context,
                                       String appID,
                                       String campaign,
                                       Map<String, String> parameters,
                                       String baseURL){
        LinkGenerator linkGenerator = new LinkGenerator(CROSS_PROMOTION_PID);
        linkGenerator.setBaseURL(baseURL)
                .setAppPackage(appID)
                .addParameter("af_siteid", context.getPackageName());
        if (campaign != null){
            linkGenerator.setCampaign(campaign);
        }
        if (parameters != null){
            linkGenerator.addParameters(parameters);
        }
        String advertisingId = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM);
        if (advertisingId != null) {
            linkGenerator.addParameter("advertising_id", advertisingId);
        }
        return linkGenerator;
    }

    private static class HTTPGetTask extends AsyncTask<String, Void, Void> {

        private RedirectHandler redirectHandler;
        private WeakReference<Context> contextRef;
        private boolean mTrackingStopped;

        HTTPGetTask(RedirectHandler redirectHandler, Context context, boolean isTrackingStopped) {
            this.redirectHandler = redirectHandler;
            this.contextRef = new WeakReference<>(context);
            mTrackingStopped = isTrackingStopped;
        }

        @Override
        protected Void doInBackground(String... params) {
            if (mTrackingStopped) {
                return null;
            }
            HttpURLConnection connection = null;
            try {
                String urlString = params[0];
                URL url = new URL(urlString);
                connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setInstanceFollowRedirects(false);
                int statusCode = connection.getResponseCode();
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    AFLogger.afInfoLog("Cross promotion impressions success: " + urlString, false);
                } else if (statusCode == HttpURLConnection.HTTP_MOVED_PERM
                        || statusCode == HttpURLConnection.HTTP_MOVED_TEMP){
                    AFLogger.afInfoLog("Cross promotion redirection success: " + urlString, false);
                    if (redirectHandler != null && contextRef.get() != null) {
                        String redirectUrlString = connection.getHeaderField("Location");
                        redirectHandler.setRedirectUrl(redirectUrlString);
                        redirectHandler.redirect(contextRef.get());
                    }
                } else {
                    AFLogger.afInfoLog("call to "+urlString+ " failed: "+statusCode);
                }
            } catch (Throwable t){
                AFLogger.afErrorLog(t.getMessage(),t, true);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            return null;
        }
    }
}
