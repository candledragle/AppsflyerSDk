package com.appsflyer;

import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by shacharaharon on 15/09/2016.
 */
@SuppressWarnings("FieldCanBeLocal")
class ProxyManager {

    final static String PROXY_SERVER_FLAG = "monitor";
    private final static int MONITORING_REQUEST_MAX_SIZE_KB = 96 * 1024; // 96 KB ~ 99000 bytes
    private static ProxyManager instance;
    private boolean shouldCollectPreLaunchDebugData = true;
    private boolean shouldEnableProxyForThisApp = true;
    private final String DEVICE_DATA_BRAND = "brand";
    private final String DEVICE_DATA_MODEL = "model";
    private final String DEVICE_DATA_PLATFORM_NAME = "platform";
    private final String DEVICE_DATA_PLATFORM_VERSION = "platform_version";
    private final String DEVICE_DATA_GAID = "advertiserId";
    private final String DEVICE_DATA_IMEI = "imei";
    private final String DEVICE_DATA_ANDROID_ID = "android_id";
    private final String SDK_DATA_SDK_VERSION = "sdk_version";
    private final String SDK_DATA_DEV_KEY = "devkey";
    private final String SDK_DATA_ORIGINAL_AF_UID = "originalAppsFlyerId";
    private final String SDK_DATA_CURRENT_AF_UID = "uid";
    private final String APP_DATA_APP_ID = "app_id";
    private final String APP_DATA_APP_VERSION = "app_version";
    private final String APP_DATA_CHANNEL = "channel";
    private final String APP_DATA_PRE_INSTALL = "preInstall";
    private final String CHRONOLOGICAL_EVENTS_DATA = "data";
    private final String PROXY_STOPPED = "r_debugging_off";
    private final String PROXY_STARTED = "r_debugging_on";
    private final String PUBLIC_API_CALL = "public_api_call";
    private final String EXCEPTION = "exception";
    private final String SERVER_REQUEST = "server_request";
    private final String SERVER_RESPONSE = "server_response";
    private final String BQ_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
    private final String EVENT_DATE_FORMAT = "MM-dd HH:mm:ss.SSS";
    private JSONObject proxyJSON;
    private JSONArray chronologicalEvents;
    private int requestSize = 0;
    private boolean proxyEnabledFromServer;
    static final String RD_BACKEND_URL = "https://monitorsdk.%s/remote-debug?app_id=";


    private String launchCounter = "-1";

    private ProxyManager() {
        chronologicalEvents = new JSONArray();
        requestSize = 0;
        proxyEnabledFromServer = false;
    }

    static ProxyManager getInstance() {
        if (instance == null) {
            instance = new ProxyManager();
        }
        return instance;
    }

    synchronized void setLauncCounter(String counter) {
        launchCounter = counter;
    }


    synchronized void startProxyMode() {
        proxyEnabledFromServer = true;
        addEvent(PROXY_STARTED, new SimpleDateFormat(BQ_DATE_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis()));
    }

    synchronized void stopProxyMode(/*boolean shouldClearInstance*/) {
        addEvent(PROXY_STOPPED, new SimpleDateFormat(BQ_DATE_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis()));
        proxyEnabledFromServer = false;
        shouldCollectPreLaunchDebugData = false;
    }

    synchronized void releaseProxy() {
        proxyJSON = null;
        chronologicalEvents = null;
        instance = null;
    }

    void sendProxyData(String packageName, PackageManager packageManager) {
        try {
            // Send Proxy data, and clear.
            ProxyManager.getInstance().loadStaticData(packageName, packageManager);
            String proxyData = ProxyManager.getInstance().getJSONString(true);
             BackgroundHttpTask proxyTask = new BackgroundHttpTask(null, AppsFlyerLib.getInstance().isTrackingStopped());

            proxyTask.bodyAsString = proxyData;
            proxyTask.setProxyMode(false);
            proxyTask.execute(ServerConfigHandler.getUrl(RD_BACKEND_URL) + packageName);
        } catch (Throwable t) {

        }
    }

    private boolean isProxy() {
        return shouldEnableProxyForThisApp &&
                (shouldCollectPreLaunchDebugData || proxyEnabledFromServer);
    }

    private synchronized void setDeviceData(String brand, String model, String osVersion, String gaid, String imei, String androidId) {
        try {
            proxyJSON.put(DEVICE_DATA_BRAND, brand);
            proxyJSON.put(DEVICE_DATA_MODEL, model);
            proxyJSON.put(DEVICE_DATA_PLATFORM_NAME, "Android");
            proxyJSON.put(DEVICE_DATA_PLATFORM_VERSION, osVersion);
            if (gaid != null && gaid.length() > 0) {
                proxyJSON.put(DEVICE_DATA_GAID, gaid);
            }
            if (imei != null && imei.length() > 0) {
                proxyJSON.put(DEVICE_DATA_IMEI, imei);
            }
            if (androidId != null && androidId.length() > 0) {
                proxyJSON.put(DEVICE_DATA_ANDROID_ID, androidId);
            }

        } catch (Throwable t) {
            //AFLogger.afErrorLog("Error adding device data.", t, false, false);
        }
    }

    private synchronized void setSDKData(String version, String devKey, String originalAFUID, String currentAFUID) {
        try {
            proxyJSON.put(SDK_DATA_SDK_VERSION, version);
            if (devKey != null && devKey.length() > 0) {
                proxyJSON.put(SDK_DATA_DEV_KEY, devKey);
            }
            if (originalAFUID != null && originalAFUID.length() > 0) {
                proxyJSON.put(SDK_DATA_ORIGINAL_AF_UID, originalAFUID);
            }
            if (currentAFUID != null && currentAFUID.length() > 0) {
                proxyJSON.put(SDK_DATA_CURRENT_AF_UID, currentAFUID);
            }
        } catch (Throwable t) {
            //AFLogger.afErrorLog("Error adding SDK data.", t, false, false);
        }
    }

    private synchronized void setAppData(String appId, String appVersion, String channel, String preInstall) {
        try {
            if (appId != null && appId.length() > 0) {
                proxyJSON.put(APP_DATA_APP_ID, appId);
            }
            if (appVersion != null && appVersion.length() > 0) {
                proxyJSON.put(APP_DATA_APP_VERSION, appVersion);
            }
            if (channel != null && channel.length() > 0) {
                proxyJSON.put(APP_DATA_CHANNEL, channel);
            }
            if (preInstall != null && preInstall.length() > 0) {
                proxyJSON.put(APP_DATA_PRE_INSTALL, preInstall);
            }
        } catch (Throwable t) {
            //AFLogger.afErrorLog("Error adding app data.", t, false, false);
        }
    }

    void addApiEvent(String methodName, String... args) {
        addEvent(PUBLIC_API_CALL, methodName, args);
    }

    void addExceptionEvent(Throwable t) {
        Throwable cause = t.getCause();
        addEvent(EXCEPTION, t.getClass().getSimpleName(), getThrowableStringData(cause == null ? t.getMessage() : cause.getMessage(), cause == null ? t.getStackTrace() : cause.getStackTrace()));
    }

    void addServerRequestEvent(String url, String requestBody) {
        addEvent(SERVER_REQUEST, url, requestBody);
    }

    void addServerResponseEvent(String url, int responseCode, String responseBody) {
        addEvent(SERVER_RESPONSE, url, String.valueOf(responseCode), responseBody);
    }

    void addLogEntry(String type, String logMessage) {
        addEvent(null, type, logMessage);
    }

    private synchronized void addEvent(String eventType, String title, String... body) {
        if (!isProxy() || requestSize >= MONITORING_REQUEST_MAX_SIZE_KB) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            String bodyStr = "";
            if (body.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = body.length - 1; i >= 1; i--) {
                    sb.append(body[i]).append(", ");
                }


                sb.append(body[0]);
                bodyStr = sb.toString();
            }
            String event;
            String formattedTimestamp = new SimpleDateFormat(EVENT_DATE_FORMAT, Locale.ENGLISH).format(now);
            if (eventType != null) {
                event = String.format("%18s %5s _/%s [%s] %s %s", formattedTimestamp, Thread.currentThread().getId(), AppsFlyerLib.LOG_TAG, eventType, title, bodyStr);
            } else {
                event = String.format("%18s %5s %s/%s %s", formattedTimestamp, Thread.currentThread().getId(), title, AppsFlyerLib.LOG_TAG, bodyStr);
            }

            //AFLogger.afDebugLog(String.format("Adding RD event:\n(***) %s", event), false);
            chronologicalEvents.put(event);

            requestSize += event.getBytes().length;
        } catch (Throwable t) {
            //AFLogger.afErrorLog("Error adding event: '" + title + "'.", t, false, false);
        }
    }

    private synchronized String getJSONString(boolean shouldClearData) {
        String result = null;
        try {
            long now = System.currentTimeMillis();
            proxyJSON.put(CHRONOLOGICAL_EVENTS_DATA, chronologicalEvents);

            result = proxyJSON.toString();
            if (shouldClearData) {
                clearData();
            }
        } catch (JSONException e) {
            //AFLogger.afErrorLog("Error converting JSON into String", e, false, false);
        }
        return result;
    }

    private synchronized void loadStaticData(String packageName, PackageManager packageManager) {
        //AFLogger.afInfoLog("Collecting RemoteDebugging data..", false);
        //check if static data exists on AppsFlyerProperties
        AppsFlyerProperties props = AppsFlyerProperties.getInstance();
        AppsFlyerLib afLib = AppsFlyerLib.getInstance();
        String proxyStaticDataFromProperties = props.getString("remote_debug_static_data");
        if (proxyStaticDataFromProperties != null) {
            try {
                proxyJSON = new JSONObject(proxyStaticDataFromProperties);
            } catch (Throwable ignored) {
            }
        } else { // collect static data from scratch
            proxyJSON = new JSONObject();
            setDeviceData(
                    Build.BRAND,
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    props.getString(ServerParameters.ADVERTISING_ID_PARAM),
                    afLib.userCustomImei,
                    afLib.userCustomAndroidId);
            setSDKData(
                    AppsFlyerLib.BUILD_NUMBER + "." + AppsFlyerLib.JENKINS_BUILD_NUMBER,
                    props.getString(AppsFlyerProperties.AF_KEY),
                    props.getString(AFKeystoreWrapper.AF_KEYSTORE_UID),
                    props.getString(ServerParameters.AF_USER_ID));

            // when application update from store occur, update stored app-data
            try {
                int appVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode;
                String appChannel = props.getString(AppsFlyerProperties.CHANNEL);
                String appPreInstallName = props.getString(AppsFlyerLib.PRE_INSTALL_PREF);

                setAppData(packageName, String.valueOf(appVersionCode), appChannel, appPreInstallName);
            } catch (Throwable ignore) {
            }
            props.set("remote_debug_static_data", proxyJSON.toString());
        }
        //AFLogger.afInfoLog("Done collecting RemoteDebugging data", false);
        try {
            proxyJSON.put(ServerParameters.LAUNCH_COUNTER, launchCounter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String[] getThrowableStringData(String msg, StackTraceElement[] stackTrace) {
        if (stackTrace == null) {
            return new String[]{msg};
        }
        String[] strArr = new String[stackTrace.length + 1];
        strArr[0] = msg;
        for (int i = 1; i < stackTrace.length; i++) {
            strArr[i] = stackTrace[i].toString();
        }
        return strArr;
    }

    int getNumberOfLines() {
        return chronologicalEvents.length();
    }

    private synchronized void clearData() {
        chronologicalEvents = null;
        chronologicalEvents = new JSONArray();
        requestSize = 0;
        //AFLogger.afInfoLog("Cleared RemoteDebugging data", false);
    }

    synchronized void dropPreLaunchDebugData() {
        shouldCollectPreLaunchDebugData = false;
        clearData();
    }

    void disableRemoteDebuggingForThisApp() {
        shouldEnableProxyForThisApp = false;
    }

    boolean isProxyEnabledFromServer() {
        return proxyEnabledFromServer;
    }
}
