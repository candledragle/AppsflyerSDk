package com.appsflyer;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author by gilmeroz on 24/01/2017.
 */
public class ServerConfigHandler {

    final static String SEND_BACKGROUND_FLAG = "send_background";

    /**
     * Called from Unit tests only. TODO: get rid of this method
     * @param response
     */
    static void handle(String response){
        AFLogger.afDebugLog("server response body: "+response);
        // Remote Debugging (Monitoring) Internal Feature
        JSONObject responseJSON = handleResponse(response);
        if (responseJSON != null){
            try {
                String onelinkDomain = responseJSON.optString("ol_domain", null);
                if (onelinkDomain != null && onelinkDomain.length() > 0){
                    AppsFlyerProperties.getInstance().set(AppsFlyerProperties.ONELINK_DOMAIN,onelinkDomain);
                    String version = responseJSON.optString(ServerParameters.ONELINK_VERSION, null);
                    AppsFlyerProperties.getInstance().set(AppsFlyerProperties.ONELINK_VERSION,version);

                    String onelinkScheme = responseJSON.optString("ol_scheme", null);
                    if (onelinkScheme != null && onelinkScheme.length() > 0) {
                        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.ONELINK_SCHEME,onelinkScheme);
                    }
                }
            } catch (Throwable e) {
                AFLogger.afErrorLog(e.getMessage(),e);
            }
        }
    }

    @Nullable
    static JSONObject handleResponse(String response) {
        JSONObject responseJSON = null;
        try {
            responseJSON = new JSONObject(response);
            boolean proxyServerFlag = responseJSON.optBoolean(ProxyManager.PROXY_SERVER_FLAG, false);
            if (proxyServerFlag) {
                ProxyManager.getInstance().startProxyMode();
            } else {
                ProxyManager.getInstance().dropPreLaunchDebugData(); // dropping initial data we collected
                ProxyManager.getInstance().stopProxyMode();
            }
        } catch (JSONException ignored) {
            ProxyManager.getInstance().dropPreLaunchDebugData(); // dropping initial data we collected
            ProxyManager.getInstance().stopProxyMode();
        } catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(), t);
            ProxyManager.getInstance().dropPreLaunchDebugData(); // dropping initial data we collected
            ProxyManager.getInstance().stopProxyMode();
        }
        return responseJSON;
    }

    public static String getUrl(String domain) {
        return String.format(domain, AppsFlyerLib.getInstance().getHost());
    }
}
