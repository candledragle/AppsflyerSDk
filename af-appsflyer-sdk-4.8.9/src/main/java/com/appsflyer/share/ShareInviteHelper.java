package com.appsflyer.share;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.appsflyer.AFExecutor;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerProperties;
import com.appsflyer.CreateOneLinkHttpTask;
import com.appsflyer.OneLinkHttpTask;

import java.util.HashMap;
import java.util.Map;

import static com.appsflyer.share.Constants.URL_CHANNEL;
import static com.appsflyer.share.Constants.URL_SITE_ID;
import static com.appsflyer.share.Constants.USER_INVITE_LINK_TYPE;
import static com.appsflyer.share.Constants.USER_SHARE_LINK_TYPE;

/**
 * @author  by gilmeroz on 06/01/2017.
 */

public class ShareInviteHelper {

    // modifying to private as this part of the feature was dropped per product decision
    private static LinkGenerator generateShareUrl(Context context){
        return createLink(USER_SHARE_LINK_TYPE,context);
    }

    public static LinkGenerator generateInviteUrl(Context context){
        return createLink(USER_INVITE_LINK_TYPE,context);
    }

    public static void generateUserInviteLink(@NonNull Context ctx,
                                              @NonNull String oneLinkId,
                                              @NonNull Map<String, String> data,
                                              @NonNull CreateOneLinkHttpTask.ResponseListener responseListener) {

        if(AppsFlyerProperties.getInstance()
                .getBoolean(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false)){
            AFLogger.afInfoLog("CustomerUserId not set, generate User Invite Link is disabled", true);
            return;
        }

        CreateOneLinkHttpTask createOneLinkHttpTask = new CreateOneLinkHttpTask(oneLinkId, data, AppsFlyerLib.getInstance(), ctx, AppsFlyerLib.getInstance().isTrackingStopped());
        createOneLinkHttpTask.setConnProvider(new OneLinkHttpTask.HttpsUrlConnectionProvider());
        createOneLinkHttpTask.setListener(responseListener);
        AFExecutor.getInstance().getThreadPoolExecutor().execute(createOneLinkHttpTask);
    }

    private static LinkGenerator createLink(String type, Context context){
        String appsflyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(context);
        String onelinkID = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_ID);
        String domain = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_DOMAIN);
        String customerUserId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID);
        LinkGenerator linkGenerator = new LinkGenerator(type)
                .setBaseURL(onelinkID, domain, context.getPackageName())
                .setReferrerUID(appsflyerUID)
                .setReferrerCustomerId(customerUserId)
                .addParameter(URL_SITE_ID,context.getPackageName());

        String deeplinkURI = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_SCHEME);
        if (deeplinkURI != null && deeplinkURI.length() > 3){
            linkGenerator.setBaseDeeplink(deeplinkURI);
        }
        return linkGenerator;
    }

    public static void trackInvite(Context context, String channel, Map<String, String> eventParameters){
        if (TextUtils.isEmpty(channel)) {
            AFLogger.afWarnLog(Constants.LOG_INVITE_ERROR_NO_CHANNEL);
            return;
        }

        if(AppsFlyerProperties.getInstance()
                .getBoolean(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false)){
            AFLogger.afInfoLog("CustomerUserId not set, track Invite is disabled", true);
            return;
        }

        LinkGenerator linkGenerator = generateInviteUrl(context);
        linkGenerator.addParameters(eventParameters);
        AFLogger.afDebugLog(Constants.LOG_INVITE_TRACKING_APP_INVITE_VIA_CHANNEL +channel);
        AFLogger.afDebugLog(Constants.LOG_INVITE_GENERATED_URL + linkGenerator.generateLink());
        trackLinkSent(linkGenerator, channel, context);
    }

    // modifying to private as this part of the feature was dropped per product decision
    private static void trackShare(Context context,String channel, Map<String, String> eventParameters){
        LinkGenerator linkGenerator = generateShareUrl(context);
        linkGenerator.addParameters(eventParameters);
        trackLinkSent(linkGenerator, channel, context);
    }

    private static void trackLinkSent(LinkGenerator linkGenerator, String channel, Context context){
        String eventName = linkGenerator.getMediaSource();
        if (USER_INVITE_LINK_TYPE.equals(eventName)) {
            eventName = AFInAppEventType.INVITE;
        } else if (USER_SHARE_LINK_TYPE.equals(eventName)){
            eventName = AFInAppEventType.SHARE;
        }
        Map<String, Object> eventValue = new HashMap<String, Object>();

        if (linkGenerator.getParameters() != null) {
            eventValue.putAll(linkGenerator.getParameters());
        }

        eventValue.put(URL_CHANNEL,channel);

        AppsFlyerLib.getInstance().trackEvent(context, eventName, eventValue);
    }

}
