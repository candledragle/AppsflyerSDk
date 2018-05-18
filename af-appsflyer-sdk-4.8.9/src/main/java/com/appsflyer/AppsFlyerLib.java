package com.appsflyer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.appsflyer.cache.CacheManager;
import com.appsflyer.cache.RequestCacheData;
import com.appsflyer.share.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.appsflyer.AppsFlyerProperties.AF_HOST;


public class AppsFlyerLib implements AFInstallReferrerListener {

    static final String JENKINS_BUILD_NUMBER = "379";
    static final String BUILD_NUMBER = "4.8.9";

    static final String SERVER_BUILD_NUMBER = BUILD_NUMBER.substring(0,BUILD_NUMBER.indexOf("."));
    public static final String LOG_TAG = LogMessages.LOG_TAG_PREFIX+BUILD_NUMBER;
    public static final String IS_STOP_TRACKING_USED = "is_stop_tracking_used";

    //private String host = ServerParameters.DEFAULT_HOST;
    private static final String BUILD_URL_SUFFIX = SERVER_BUILD_NUMBER + "/androidevent?buildnumber=" + BUILD_NUMBER + "&app_id=";
    static final String REFERRER_TRACKING_URL = "https://attr.%s/api/v" + BUILD_URL_SUFFIX;
    static final String APPS_TRACKING_URL = "https://t.%s/api/v" + BUILD_URL_SUFFIX;
    static final String EVENTS_TRACKING_URL = "https://events.%s/api/v" + BUILD_URL_SUFFIX;
    static final String REGISTER_URL = "https://register.%s/api/v" + BUILD_URL_SUFFIX;

    static final String STATS_URL = "https://stats.%s/stats";
    static final String CONVERSION_DATA_URL = "https://api.%s/install_data/v3/";

    static final String INSTALL_UPDATE_DATE_FORMAT = "yyyy-MM-dd_HHmmssZ";

    static final String AF_SHARED_PREF = "appsflyer-data";
    private static final String SENT_SUCCESSFULLY_PREF = "sentSuccessfully";
    private long lastLaunchAttemptTimestamp = -1L;
    private long lastSuccessfulLaunchTimestamp = -1L;
    private static final String AF_COUNTER_PREF = "appsFlyerCount";
    private static final String AF_EVENT_COUNTER_PREF = "appsFlyerInAppEventCount";
    private static final String AF_TIME_PASSED_SINCE_LAST_LAUNCH  = "AppsFlyerTimePassedSincePrevLaunch";

    static final String FIRST_INSTALL_PREF = "appsFlyerFirstInstall";
    static final String REFERRER_PREF = "referrer";
    static final String INSTALL_REFERRER_PREF = "rfr";

    static final String EXTRA_REFERRERS_PREF = "extraReferrers";
    static final String ATTRIBUTION_ID_PREF = "attributionId";
    private static final String PREPARE_DATA_ACTION = "collect data for server";
    private static final String CALL_SERVER_ACTION = "call server.";
    private static final String SERVER_RESPONDED_ACTION = "response from server. status=";

    public static final String ATTRIBUTION_ID_CONTENT_URI = "content://com.facebook.katana.provider.AttributionIdProvider";

    public static final String ATTRIBUTION_ID_COLUMN_NAME = "aid";
    private static final String CACHED_CHANNEL_PREF = "CACHED_CHANNEL";
    private static final String IS_FIRST_LAUNCH = "is_first_launch";
    private static final String CACHED_URL_PARAMETER = "&isCachedRequest=true&timeincache=";
    private static final String INSTALL_STORE_PREF = "INSTALL_STORE";
    private static final List<String> IGNORABLE_KEYS = Arrays.asList(new String[]{"is_cache"});
    private static final List<String> IGNORABLE_CHANNELS = Arrays.asList(new String[]{"googleplay", "playstore", "googleplaystore"});
    private static final String DEEPLINK_ATTR_PREF = "deeplinkAttribution";
    static final String PRE_INSTALL_PREF = "preInstallName";
    private static final String IMEI_CACHED_PREF = "imeiCached";
    private static final String PREV_EVENT_TIMESTAMP = "prev_event_timestamp";
    private static final String PREV_EVENT_VALUE = "prev_event_value";
    private static final String PREV_EVENT_NAME = "prev_event_name";
    private static final String PREV_EVENT = "prev_event";
    private static final long TEST_MODE_MAX_DURATION = 30*1000;
    private static final long PUSH_PAYLOAD_MAX_AGING_DEFAULT_VALUE = 30L * 60L * 1000L; // 30 minutes
    private static final int PUSH_PAYLOAD_HISTORY_SIZE_DEFAULT_VALUE = 2;
    private static final String ANDROID_ID_CACHED_PREF = "androidIdCached";
    private static final String IN_APP_EVENTS_API = "1";

    private static final long EXTRA_REFERRER_BUF = 4;
    private static final long EXTRA_REFERRER_SUB_BUF = 5;

    public static final String PRE_INSTALL_SYSTEM_RO_PROP = "ro.appsflyer.preinstall.path";
    public static final String PRE_INSTALL_SYSTEM_DEFAULT = "/data/local/tmp/pre_install.appsflyer";
    public static final String PRE_INSTALL_SYSTEM_DEFAULT_ETC = "/etc/pre_install.appsflyer";
    public static final String AF_PRE_INSTALL_PATH = "AF_PRE_INSTALL_PATH";
    static final String RESPONSE_NOT_JSON = "string_response";
    private long launchBlockingTimeWindow = TimeUnit.SECONDS.toMillis(5);


    private static AppsFlyerConversionListener conversionDataListener = null;
    static AppsFlyerInAppPurchaseValidatorListener validatorListener = null;

    private AFInstallReferrer afInstallReferrer = null;
    private Map<String,String> installReferrerMap;

    private boolean isDuringCheckCache = false;
    private long lastCacheCheck;
    private ScheduledExecutorService cacheScheduler = null;
    private long timeEnteredForeground;
    private long timeWentToBackground;
    private static final String CONVERSION_REQUEST_RETRIES  = "appsflyerConversionDataRequestRetries";
    private static final int NUMBER_OF_CONVERSION_DATA_RETRIES = 5;
    private static final String CONVERSION_DATA_CACHE_EXPIRATION = "appsflyerConversionDataCacheExpiration";
    private static final String GET_CONVERSION_DATA_TIME = "appsflyerGetConversionDataTiming";
    private static final long SIXTY_DAYS =  60 * 60 * 24 * 60 * 1000L; // in milli seconds
    private static final String VERSION_CODE = "versionCode";

    private static AppsFlyerLib instance = new AppsFlyerLib();


    private Foreground.Listener listener;
    String userCustomImei;
    String userCustomAndroidId;
    private Uri latestDeepLink = null;
    private long testModeStartTime;
    private boolean isCollectLocation = false;
    private boolean isRetargetingTestMode=false;
    private String pushPayload;
    private Map<Long,String> pushPayloadHistory;
    private boolean isTokenRefreshServiceConfigured;
    private boolean didUseApplicationInit;
    private PlatformExtension platformExtension = new PlatformExtension();
    private boolean isSendBackground = false;
    private boolean mIsTrackingStopped = false;


    void resetTimeEnteredForeground() {
        timeEnteredForeground = System.currentTimeMillis();
    }

    void resetTimeWentToBackground() {
        timeWentToBackground = System.currentTimeMillis();
    }

    void onReceive(Context context, Intent intent) {
        String shouldMonitor = intent.getStringExtra("shouldMonitor");
        if (shouldMonitor != null){
            AFLogger.afInfoLog("Turning on monitoring.");
            AppsFlyerProperties.getInstance().set(AppsFlyerProperties.IS_MONITOR,shouldMonitor.equals("true"));
            monitor(context, null,  MonitorMessages.START_TRACKING, context.getPackageName());
            return;
        }

        AFLogger.afInfoLog( "****** onReceive called *******");

        AppsFlyerProperties.getInstance().setOnReceiveCalled();

        String referrer = intent.getStringExtra(REFERRER_PREF);
        AFLogger.afInfoLog( LogMessages.PLAY_STORE_REFERRER_RECIEVED + referrer);

        if(referrer != null) {
            // check if test app
            String testIntegration = intent.getStringExtra("TestIntegrationMode");

            if (InternalConstants.TEST_MEDIA_SOURCE.equals(testIntegration)) {

                SharedPreferences sharedPreferences = getSharedPreferences(context);
                SharedPreferences.Editor editor =  sharedPreferences.edit();
                editor.clear();
                editorCommit(editor);
//                editor.apply();
                AppsFlyerProperties.getInstance().setFirstLaunchCalled(false);
                startTestMode();
            }

            saveDataToSharedPreferences(context, REFERRER_PREF, referrer);

            // set in memory value in case the shared pref will not be sync on time
            AppsFlyerProperties.getInstance().setReferrer(referrer);

            if  (AppsFlyerProperties.getInstance().isFirstLaunchCalled()){ // send to server only if it's after the onCreate call
                AFLogger.afInfoLog("onReceive: isLaunchCalled");
                backgroundReferrerLaunch(context, referrer, intent);
            }
        }
    }

    /**
     * This method manages extra referrers (max 5)
     *
     * We run over all referrers in map, aggregate and sort timestamps.
     * After, we remove referrer that does not related to oldest two and newest two referrer
     * based on their timestamps.
     *
     * For example: In following structure 'aaaa' will be replaced with new referrer
     * <code>
     * {" +
     "\"aaaa\":\"[100,200]\","+
     "\"bbbb\":\"[101,201,303]\"," +
     "\"cccc\":\"[102]\"," +
     "\"dddd\":\"[103,203]\"," +
     "\"eeee\":\"[104,304,404,504]\"" +
     "}"
     * </code>
     *
     * @param referrers
     * @throws JSONException
     */
    private void manageExtraReferrers(JSONObject referrers) {

        List<Long> items = new ArrayList<>();

        Iterator<?> keys = referrers.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();


            JSONArray timeStampArr = null;
            try {
                timeStampArr = new JSONArray((String) referrers.get(key));
                for (int i = 0; i < timeStampArr.length(); i++) {
                    items.add(timeStampArr.getLong(i));

                }
            } catch (JSONException e) {
            }
        }

        // items size should be at least 5
        Collections.sort(items);

        String candidateKey = null;

        keys = referrers.keys();
        while (keys.hasNext() && candidateKey == null) {
            String key = (String) keys.next();

            try {
                JSONArray timeStampArr = new JSONArray((String) referrers.get(key));

                //search for proper key
                for (int i = 0; i < timeStampArr.length(); i++) {
                    if (    // skip all keys where one of timestamps are related to our algorithm
                           timeStampArr.getLong(i) == items.get(0)                // check 1st oldest
                        || timeStampArr.getLong(i) == items.get(1)                // check 2nd oldest
                        || timeStampArr.getLong(i) == items.get(items.size() - 1) // check newest
                            )
                    {
                        candidateKey = null;
                        break;
                    }
                    else{
                        candidateKey = key;
                    }
                }
            } catch (JSONException e) {
            }
        }

        if (candidateKey != null) {
            referrers.remove(candidateKey);
        }
    }

    /**
     * If we have multiple records (>4), we will leave 1st 2 and last two based on sub timestamp
     * */
    void addReferrer(Context context, String referrer) {
        AFLogger.afDebugLog("received a new (extra) referrer: "+referrer);
        try {
            JSONObject referrers = null;
            JSONArray occurrencesTimestamps = null;
            long now = System.currentTimeMillis();

            SharedPreferences sp = context.getSharedPreferences(AF_SHARED_PREF, 0);
            String referrersString = sp.getString(EXTRA_REFERRERS_PREF, null);
            if (referrersString == null) { // so far only 1 referrer received
                referrers = new JSONObject();
                occurrencesTimestamps = new JSONArray();
            } else { // more than 1 referrer so far
                referrers = new JSONObject(referrersString);
                if (referrers.has(referrer)) { // referrer was received in the past, adding occurrence
                    occurrencesTimestamps = new JSONArray((String) referrers.get(referrer));
                } else { // first occurrence of this referrer
                    occurrencesTimestamps = new JSONArray();
                }
            }
            if (occurrencesTimestamps.length() < EXTRA_REFERRER_SUB_BUF) { // limiting to maximum 5 timestamp occurrences for each extra-referrer
                occurrencesTimestamps.put(now);
            }

            // we we have more then 4 records, add
            if(referrers.length() >= EXTRA_REFERRER_BUF){
                manageExtraReferrers(referrers);
            }
            // put new referrer
            referrers.put(referrer,occurrencesTimestamps.toString());


            saveDataToSharedPreferences(context, EXTRA_REFERRERS_PREF, referrers.toString());

        } catch (JSONException ignored) {
        } catch (Throwable t) {
            AFLogger.afErrorLog("Couldn't save referrer - "+referrer+": ",t);
        }
    }

    @SuppressLint("CommitPrefEdits")
    void editorCommit(SharedPreferences.Editor editor) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private void startTestMode() {
        AFLogger.afInfoLog("Test mode started..");
        testModeStartTime = System.currentTimeMillis();
    }
    private void endTestMode() {
        AFLogger.afInfoLog("Test mode ended!");
        testModeStartTime = 0;
    }
    private boolean isInTestMode(Context context) {
        long interval = System.currentTimeMillis() - testModeStartTime;
        String referrer = AppsFlyerProperties.getInstance().getReferrer(context);
        return (interval <= TEST_MODE_MAX_DURATION) && referrer != null && referrer.contains(InternalConstants.TEST_MEDIA_SOURCE);
    }

    private AppsFlyerLib() {
        AFVersionDeclaration.init();
    }



    public static AppsFlyerLib getInstance() {
        return instance;
    }

    public void stopTracking(boolean isTrackingStopped, Context context) {
        mIsTrackingStopped = isTrackingStopped;
        CacheManager.getInstance().clearCache(context);
        if (mIsTrackingStopped) {
            saveBooleanToSharedPreferences(context, IS_STOP_TRACKING_USED, true);
        }
    }


    public String getSdkVersion() {
        ProxyManager.getInstance().addApiEvent("getSdkVersion");
        return "version: "+ BUILD_NUMBER + " (build "+JENKINS_BUILD_NUMBER+")";
    }

    private void registerForAppEvents(Application application) {

        AppsFlyerProperties.getInstance().loadProperties(application.getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if (Build.VERSION.SDK_INT >= 14 ) {

              if (listener == null) {

                Foreground.init();

                listener = new Foreground.Listener() {
                    public void onBecameForeground(Activity currentActivity) {
                        // We have not sent launch event more than 2 times previously
                        if (AFSensorManager.LAUNCH_COUNTER > getLaunchCounter(getSharedPreferences(currentActivity), false)) {
                            AFSensorManager.getInstance(currentActivity).startNewTracking();
                        }
                        AFLifecycleCallbacks.doOnResume(currentActivity);
                    }

                    public void onBecameBackground(WeakReference<Context> currentActivity) {
                        AFLifecycleCallbacks.doOnPause(currentActivity.get());
                        AFSensorManager.getInstance(currentActivity.get()).stopStartedTracking();
                    }
                };
                Foreground.getInstance().registerListener(application, listener);
              }
            }
        } else {
            AFLogger.afInfoLog("SDK<14 call trackEvent manually");
            AFLifecycleCallbacks.doOnResume(application);
        }
    }

    /**
     *
     * @deprecated use {@link #enableUninstallTracking(String)} instead.
     */
    @Deprecated
    public void setGCMProjectID(String projectNumber){
        ProxyManager.getInstance().addApiEvent("setGCMProjectID",projectNumber);
        AFLogger.afWarnLog("Method 'setGCMProjectNumber' is deprecated. Please follow the documentation.");
        enableUninstallTracking(projectNumber);
    }

    /**
     *
     * @deprecated use {@link #enableUninstallTracking(String)} instead.
     */
    @Deprecated
    public void setGCMProjectNumber(String projectNumber){
        ProxyManager.getInstance().addApiEvent("setGCMProjectNumber", projectNumber);
        AFLogger.afWarnLog("Method 'setGCMProjectNumber' is deprecated. Please follow the documentation.");
        enableUninstallTracking(projectNumber);
    }

    /**
     *
     * @deprecated use {@link #enableUninstallTracking(String)} instead.
     */
    @Deprecated
    public void setGCMProjectNumber(Context context, String projectNumber){
        ProxyManager.getInstance().addApiEvent("setGCMProjectNumber", projectNumber);
        AFLogger.afWarnLog("Method 'setGCMProjectNumber' is deprecated. Please use 'enableUninstallTracking'.");
        enableUninstallTracking(projectNumber);
    }

    public void enableUninstallTracking(String senderId) {
        ProxyManager.getInstance().addApiEvent("enableUninstallTracking", senderId);
        setProperty(AppsFlyerProperties.GCM_PROJECT_NUMBER, senderId);
    }

    public void updateServerUninstallToken(Context context, String token) {

        if (token != null) {
            AFUninstallToken afToken = new AFUninstallToken(token);
            UninstallUtils.updateServerUninstallToken(context, afToken);
        }
    }

    public void setDebugLog(boolean shouldEnable){
        ProxyManager.getInstance().addApiEvent("setDebugLog", String.valueOf(shouldEnable));
        AppsFlyerProperties.getInstance().enableLogOutput(shouldEnable);
        AppsFlyerProperties.getInstance().setLogLevel(shouldEnable ? AFLogger.LogLevel.DEBUG : AFLogger.LogLevel.NONE);
    }

    public void setImeiData(String aImei) {
        ProxyManager.getInstance().addApiEvent("setImeiData",aImei);
        userCustomImei = aImei;
    }

    public void setAndroidIdData(String aAndroidId) {
        ProxyManager.getInstance().addApiEvent("setAndroidIdData",aAndroidId);
        userCustomAndroidId = aAndroidId;
    }

    public AppsFlyerLib enableLocationCollection(boolean flag){
        isCollectLocation = flag;
        return this;
    }

    private boolean isAppsFlyerPackage(Context context) {
        return context != null && context.getPackageName().length() > 12 && "com.appsflyer".equals(context.getPackageName().toLowerCase().substring(0, 13));
    }

    private void saveDataToSharedPreferences(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editorCommit(editor);
    }

    private void saveBooleanToSharedPreferences(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editorCommit(editor);
    }


    private void saveIntegerToSharedPreferences(Context context, String key, int value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editorCommit(editor);
    }

    private void saveLongToSharedPreferences(Context context, String key, long value) {
        saveLongToSharedPreferences(getSharedPreferences(context),key,value);
    }
    private void saveLongToSharedPreferences(SharedPreferences sharedPreferences, String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editorCommit(editor);
    }

    private boolean checkWriteExternalPermission(Context context)
    {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }


    private void setProperty(String key,String value){
        AppsFlyerProperties.getInstance().set(key,value);
    }

    private void setProperty(String key, int value){
        AppsFlyerProperties.getInstance().set(key,value);
    }

    void setProperty(String key, boolean value){
        AppsFlyerProperties.getInstance().set(key,value);
    }

    void setProperty(String key, long value){
        AppsFlyerProperties.getInstance().set(key,value);
    }

    private String getProperty(String key){
        return AppsFlyerProperties.getInstance().getString(key);
    }

    private int getProperty(String key, int defaultValue){
        return AppsFlyerProperties.getInstance().getInt(key, defaultValue);
    }

    boolean getProperty(String key, boolean defaultValue){
        return AppsFlyerProperties.getInstance().getBoolean(key, defaultValue);
    }

    long getProperty(String key, long defaultValue){
        return AppsFlyerProperties.getInstance().getLong(key, defaultValue);
    }

    /**
     *
     * @deprecated use {@link #setCustomerUserId(String)} instead
     */
    @Deprecated
    public void setAppUserId(String id){
        ProxyManager.getInstance().addApiEvent("setAppUserId", id);
        setCustomerUserId(id);
    }

    public void setCustomerUserId(String id){
        ProxyManager.getInstance().addApiEvent("setCustomerUserId", id);
        AFLogger.afInfoLog( "setCustomerUserId = " + id);
        setProperty(AppsFlyerProperties.APP_USER_ID, id);
    }

    private boolean waitingForId() { return getProperty(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false) && getProperty(AppsFlyerProperties.APP_USER_ID) == null; }

    public void waitForCustomerUserId(boolean wait) {
        AFLogger.afInfoLog("initAfterCustomerUserID: "+wait, true);
        setProperty(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, wait);
    }

    public void  setCustomerIdAndTrack(String id, @NonNull Context context) {
        if (context != null) {
            if (waitingForId()) {
                setCustomerUserId(id);
                AFLogger.afInfoLog("CustomerUserId set: " + id + " - Initializing AppsFlyer Tacking", true);
                String referrer = AppsFlyerProperties.getInstance().getReferrer(context);
                runInBackground(context, getProperty(AppsFlyerProperties.AF_KEY), null, null, referrer == null ? "" : referrer, true, context instanceof Activity ? ((Activity) context).getIntent() : null);
                if (getProperty(AppsFlyerProperties.AF_UNINSTALL_TOKEN) != null) {
                    callRegisterBackground(context, getProperty(AppsFlyerProperties.AF_UNINSTALL_TOKEN));
                }
            } else {
                setCustomerUserId(id);
                AFLogger.afInfoLog("waitForCustomerUserId is false; setting CustomerUserID: " + id, true);
            }
        }
    }

    public void setAppInviteOneLink(String oneLinkId){
        ProxyManager.getInstance().addApiEvent("setAppInviteOneLink", oneLinkId);
        AFLogger.afInfoLog( "setAppInviteOneLink = " + oneLinkId);
        if (oneLinkId == null || !oneLinkId.equals(AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_ID))){
            AppsFlyerProperties.getInstance().remove(AppsFlyerProperties.ONELINK_DOMAIN);
            AppsFlyerProperties.getInstance().remove(AppsFlyerProperties.ONELINK_VERSION);
            AppsFlyerProperties.getInstance().remove(AppsFlyerProperties.ONELINK_SCHEME);
        }
        setProperty(AppsFlyerProperties.ONELINK_ID, oneLinkId);
    }

    public void setAdditionalData (HashMap<String, Object> customData){
        if (customData != null) {
            ProxyManager.getInstance().addApiEvent("setAdditionalData", customData.toString());
            JSONObject jsonObject = new JSONObject(customData);
            AppsFlyerProperties.getInstance().setCustomData(jsonObject.toString());
        }
    }

    public void sendDeepLinkData(Activity activity) {
        if (activity != null && activity.getIntent() != null) {
            ProxyManager.getInstance().addApiEvent("sendDeepLinkData", activity.getLocalClassName() ,"activity_intent_"+activity.getIntent().toString());
        } else if (activity != null) {
            ProxyManager.getInstance().addApiEvent("sendDeepLinkData", activity.getLocalClassName() , "activity_intent_null");
        } else {
            ProxyManager.getInstance().addApiEvent("sendDeepLinkData", "activity_null");
        }

        AFLogger.afInfoLog("getDeepLinkData with activity " + activity.getIntent().getDataString());
        registerForAppEvents(activity.getApplication());
    }

    public void sendPushNotificationData(Activity activity) {
        if (activity != null && activity.getIntent() != null) {
            ProxyManager.getInstance().addApiEvent("sendPushNotificationData", activity.getLocalClassName() ,"activity_intent_"+activity.getIntent().toString());
        } else if (activity != null) {
            ProxyManager.getInstance().addApiEvent("sendPushNotificationData", activity.getLocalClassName() , "activity_intent_null");
        } else {
            ProxyManager.getInstance().addApiEvent("sendPushNotificationData", "activity_null");
        }
        pushPayload = getPushPayloadFromIntent(activity);
        if (pushPayload != null) {
            long now = System.currentTimeMillis();
            long oldestPayloadTimestamp = now;
            if (pushPayloadHistory == null) {
                AFLogger.afInfoLog("pushes: initializing pushes history..");
                pushPayloadHistory = new ConcurrentHashMap<>();
            } else {
                try {
                    long pushPayloadMaxAging = AppsFlyerProperties.getInstance().getLong(AppsFlyerProperties.PUSH_PAYLOAD_MAX_AGING, PUSH_PAYLOAD_MAX_AGING_DEFAULT_VALUE);
                    for (Long age : pushPayloadHistory.keySet()) {
                        // handle pid duplications
                        JSONObject newPush = new JSONObject(pushPayload);
                        JSONObject oldPush = new JSONObject(pushPayloadHistory.get(age));
                        if (newPush.get("pid").equals(oldPush.get("pid"))) {
                            AFLogger.afInfoLog("PushNotificationMeasurement: A previous payload with same PID was already acknowledged! (old: "+oldPush+", new: "+newPush+")");
                            pushPayload = null;
                            return;
                        }

                        // remove aged-out entries from history
                        if (now - age > pushPayloadMaxAging) {
                            pushPayloadHistory.remove(age);
                        }

                        // update oldest push payload
                        if (age <= oldestPayloadTimestamp) {
                            oldestPayloadTimestamp = age;
                        }
                    }
                } catch (Throwable t) {
                    AFLogger.afErrorLog("Error while handling push notification measurement: "+t.getClass().getSimpleName(),t);
                }
            }

            // make room for new push payload
            int pushPayloadHistorySize = AppsFlyerProperties.getInstance().getInt(AppsFlyerProperties.PUSH_PAYLOAD_HISTORY_SIZE, PUSH_PAYLOAD_HISTORY_SIZE_DEFAULT_VALUE);
            if (pushPayloadHistory.size() == pushPayloadHistorySize) {
                AFLogger.afInfoLog("pushes: removing oldest overflowing push (oldest push:"+oldestPayloadTimestamp+")");
                pushPayloadHistory.remove(oldestPayloadTimestamp);
            }
            pushPayloadHistory.put(now, pushPayload);

            // send launch event with push payload
            registerForAppEvents(activity.getApplication());

        }
    }

    /**
     *
     * @deprecated use {@link #setUserEmails(AppsFlyerProperties.EmailsCryptType, String...)} instead
     */
    @Deprecated
    public void setUserEmail(String email ) {
        ProxyManager.getInstance().addApiEvent("setUserEmail", email);
        setProperty(AppsFlyerProperties.USER_EMAIL, email);
    }

    public void setUserEmails(String... emails) {
        ProxyManager.getInstance().addApiEvent("setUserEmails", emails);
        setUserEmails(AppsFlyerProperties.EmailsCryptType.NONE, emails);
    }

    public void setUserEmails(AppsFlyerProperties.EmailsCryptType cryptMethod, String... emails) {
        List<String> args = new ArrayList<>(emails.length + 1);
        args.add(cryptMethod.toString());
        args.addAll(Arrays.asList(emails));
        ProxyManager.getInstance().addApiEvent("setUserEmails", args.toArray(new String[emails.length+1]));

        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.EMAIL_CRYPT_TYPE, cryptMethod.getValue());
        Map<String, Object> emailData = new HashMap<>();
        String cryptKey = null;
        ArrayList<String> hashedEmailList = new ArrayList<>();

        for (String email : emails) {
            switch (cryptMethod) {
                default:
                case SHA1:
                    cryptKey = "sha1_el_arr";
                    hashedEmailList.add(HashUtils.toSHA1(email));
                    break;
                case MD5:
                    cryptKey = "md5_el_arr";
                    hashedEmailList.add(HashUtils.toMD5(email));
                    break;
                case SHA256:
                    cryptKey = "sha256_el_arr";
                    hashedEmailList.add(HashUtils.toSha256(email));
                    break;
                case NONE:
                    cryptKey = "plain_el_arr";
                    hashedEmailList.add(email);
                    break;
            }
        }

        emailData.put(cryptKey, hashedEmailList);
        JSONObject jObj = new JSONObject(emailData);
        AppsFlyerProperties.getInstance().setUserEmails(jObj.toString());
    }

    public void setCollectAndroidID(boolean isCollect){
        ProxyManager.getInstance().addApiEvent("setCollectAndroidID", String.valueOf(isCollect));
        setProperty(AppsFlyerProperties.COLLECT_ANDROID_ID, Boolean.toString(isCollect));
    }

    public void setCollectIMEI(boolean isCollect){
        ProxyManager.getInstance().addApiEvent("setCollectIMEI", String.valueOf(isCollect));
        setProperty(AppsFlyerProperties.COLLECT_IMEI, Boolean.toString(isCollect));
    }

    @Deprecated
    public void setCollectFingerPrint(boolean isCollect){
        ProxyManager.getInstance().addApiEvent("setCollectFingerPrint", String.valueOf(isCollect));
        setProperty(AppsFlyerProperties.COLLECT_FINGER_PRINT, Boolean.toString(isCollect));
    }

    /**
     * Use this method to initialize AppsFlyer SDK.
     * This API should be called inside your Application class's onCreate method.
     * @param key AppsFlyer's Dev-Key, which is accessible from your AppsFlyer account,
     *            under 'App Settings' in the dashboard.
     * @param conversionDataListener (Optional) implement the ConversionDataListener to
     *                               access AppsFlyer's conversion data. Can be null.
     */
    public AppsFlyerLib init(String key, AppsFlyerConversionListener conversionDataListener) { // TODO: verify Javadoc with Eran
        ProxyManager.getInstance().addApiEvent("init", key, conversionDataListener == null ? "null" : "conversionDataListener");
        AFLogger.afLogForce(String.format("Initializing AppsFlyer SDK: (v%s.%s)",BUILD_NUMBER,JENKINS_BUILD_NUMBER));
        didUseApplicationInit = true;
        setProperty(AppsFlyerProperties.AF_KEY, key);
        LogMessages.setDevKey(key);
        AppsFlyerLib.conversionDataListener = conversionDataListener;
        return this;
    }

    /**
     * Same as {@link #init(String, AppsFlyerConversionListener)}
     * with additional argument {@link Context} to handle new install referrer
     * @param key
     * @param conversionDataListener
     * @param context
     * @return
     */
    //TODO: take care when user doesn't have internet (comes from cache)
    public AppsFlyerLib init(String key,
                             AppsFlyerConversionListener conversionDataListener,
                             Context context) {

        if(context != null){

            if(allowInstallReferrer(context)){
                AFInstallReferrerListener installReferrerListener = this;

                if(afInstallReferrer == null){
                    afInstallReferrer = new AFInstallReferrer();
                    afInstallReferrer.start(context, installReferrerListener);
                }
                else{
                    AFLogger.afWarnLog("AFInstallReferrer instance already created");
                }
            }
        }
        return init(key, conversionDataListener);
    }

    private boolean allowInstallReferrer(@NonNull Context context){

        SharedPreferences sharedPreferences = getSharedPreferences(context);

        // we send install referrer for 1,2 launch counter
        if(getLaunchCounter(sharedPreferences, false) > 2 ){
            AFLogger.afRDLog("Install referrer will not load, the counter > 2, ");
            return false;
        }

        try {
            Class.forName(AFInstallReferrer.INSTALL_REFERRER_CLIENT_CLASS);
            if (AndroidUtils.isPermissionAvailable(context, AFInstallReferrer.INSTALL_REFERRER_PERMISSION)) {
                AFLogger.afDebugLog("Install referrer is allowed");
                return true;
            }
        } catch (ClassNotFoundException ignore) {
            AFLogger.afRDLog("Class " + AFInstallReferrer.INSTALL_REFERRER_CLIENT_CLASS + " not found");
            return false;
        } catch (Throwable e) {
            AFLogger.afErrorLog("An error occurred while trying to verify manifest : " + AFInstallReferrer.INSTALL_REFERRER_CLIENT_CLASS, e);
            return false;
        }
        AFLogger.afDebugLog("Install referrer is not allowed");
        return false;
    }

    /**
     * Use this method to start tracking the application,
     * only if you call 'init' inside you Application class's onCreate method.
     * AppsFlyer's Dev-Key must be provided in the 'init' method,
     * or in the legacy API: {@link #startTracking(Application, String)}
     * @param application the Application object which is used for registering
     *                    for the app's life-cycle
     */
    public void startTracking(Application application) { // TODO: verify Javadoc with Eran
        if (!didUseApplicationInit) {
            AFLogger.afWarnLog("ERROR: AppsFlyer SDK is not initialized! The API call 'startTracking(Application)' must be called " +
                    "after the 'init(String, AppsFlyerConversionListener)' API method, which should be called on the Application's onCreate.");
            return;
        }
        startTracking(application,null);
    }

    /**
     * Use this method to start tracking the application.
     * Notice that AppsFlyer's Dev-Key must be provided.
     * @param application the Application object which is used for registering
     *                    for the app's life-cycle
     * @param key AppsFlyer's Dev-Key, which is accessible from your AppsFlyer account,
     *            under 'App Settings' in the dashboard.
     */
    public void startTracking(Application application, String key) { // TODO: verify Javadoc with Eran
        ProxyManager.getInstance().addApiEvent("startTracking", key);
        AFLogger.afInfoLog(String.format("Starting AppsFlyer Tracking: (v%s.%s)", BUILD_NUMBER, JENKINS_BUILD_NUMBER));
        AFLogger.afInfoLog("Build Number: "+ JENKINS_BUILD_NUMBER);
        AppsFlyerProperties.getInstance().loadProperties(application.getApplicationContext());
        if (!TextUtils.isEmpty(key)) {
            setProperty(AppsFlyerProperties.AF_KEY, key);
            LogMessages.setDevKey(key);
        } else {
            if (TextUtils.isEmpty(getProperty(AppsFlyerProperties.AF_KEY))) {
                AFLogger.afWarnLog("ERROR: AppsFlyer SDK is not initialized! You must provide AppsFlyer Dev-Key either " +
                        "in the 'init' API method (should be called on Application's onCreate)," +
                        "or in the startTracking API method (should be called on Activity's onCreate).");
                return;
            }
        }
        registerForAppEvents(application);
    }

    private void getReInstallData(Context context) {

        int api = android.os.Build.VERSION_CODES.JELLY_BEAN_MR2; // api 18

        if(AndroidUtils.isOPPODevice()){
            api = android.os.Build.VERSION_CODES.M;// api 23
            AFLogger.afRDLog("OPPO device found");
        }

        if (android.os.Build.VERSION.SDK_INT >= api) {
            AFLogger.afRDLog("OS SDK is=" + android.os.Build.VERSION.SDK_INT + "; use KeyStore");
            AFKeystoreWrapper afKeystore = new AFKeystoreWrapper(context);
            if (!afKeystore.loadData()) {
                afKeystore.createFirstInstallData(Installation.id(new WeakReference<>(context)));
                setProperty(AFKeystoreWrapper.AF_KEYSTORE_UID, afKeystore.getUid());
                setProperty(AFKeystoreWrapper.AF_KEYSTORE_REINSTALL_COUNTER, String.valueOf(afKeystore.getReInstallCounter()));
            } else {
                afKeystore.incrementReInstallCounter();
                setProperty(AFKeystoreWrapper.AF_KEYSTORE_UID, afKeystore.getUid());
                setProperty(AFKeystoreWrapper.AF_KEYSTORE_REINSTALL_COUNTER, String.valueOf(afKeystore.getReInstallCounter()));
            }
        }
        else{
            AFLogger.afRDLog("OS SDK is=" + android.os.Build.VERSION.SDK_INT + "; no KeyStore usage");
        }
    }

    private String getCustomerUserId(){
        return getProperty(AppsFlyerProperties.APP_USER_ID);
    }

    public void setAppId(String id) {
        ProxyManager.getInstance().addApiEvent("setAppId",id);
        setProperty(AppsFlyerProperties.APP_ID, id);
    }

    private String getAppId() {
        return getProperty(AppsFlyerProperties.APP_ID);
    }

    /**
     * SDK plugins and extensions will set this field
     * @param extension
     */
    public void setExtension(String extension) {
        ProxyManager.getInstance().addApiEvent("setExtension", extension);
        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.EXTENSION, extension);
    }

    public void setIsUpdate(boolean isUpdate) {
        ProxyManager.getInstance().addApiEvent("setIsUpdate", String.valueOf(isUpdate));
        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.IS_UPDATE, isUpdate);
    }

    public void setCurrencyCode(String currencyCode){
        ProxyManager.getInstance().addApiEvent("setCurrencyCode", currencyCode);
        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.CURRENCY_CODE, currencyCode);
    }

    public void trackLocation(Context context, double latitude, double longitude) {
        ProxyManager.getInstance().addApiEvent("trackLocation", String.valueOf(latitude), String.valueOf(longitude));
        Map<String,Object> location = new HashMap<String, Object>();
        location.put(AFInAppEventParameterName.LONGTITUDE, Double.toString(longitude));
        location.put(AFInAppEventParameterName.LATITUDE, Double.toString(latitude));

        trackEventInternal(context, AFInAppEventType.LOCATION_COORDINATES, location);
    }

    void callStatsBackground(WeakReference<Context> context) {
        if (context.get() == null) {
            return;
        }

        AFLogger.afInfoLog("app went to background");
        SharedPreferences sharedPreferences = context.get().getSharedPreferences(AF_SHARED_PREF, 0);
        AppsFlyerProperties.getInstance().saveProperties(sharedPreferences);

        // measure session time.
        long sessionTime =  timeWentToBackground - timeEnteredForeground;

        Map<String,String> statsParams = new HashMap<>();
        String afDevKey = getProperty(AppsFlyerProperties.AF_KEY);
        if (afDevKey == null) {
            AFLogger.afWarnLog("[callStats] AppsFlyer's SDK cannot send any event without providing DevKey.");
            return;
        }
        String originalAFUID = getProperty(AFKeystoreWrapper.AF_KEYSTORE_UID);

        boolean deviceTrackingDisabled = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.DEVICE_TRACKING_DISABLED,false);
        if (deviceTrackingDisabled) {
            statsParams.put(ServerParameters.DEVICE_TRACKING_DISABLED, "true");
        }
        AdvertisingIdObject amazonAdvIdObject = AdvertisingIdUtil.getAmazonAID(context.get().getContentResolver());
        if (amazonAdvIdObject != null) {
            statsParams.put(ServerParameters.AMAZON_AID,amazonAdvIdObject.getAdvertisingId());
            statsParams.put(ServerParameters.AMAZON_AID_LIMIT, String.valueOf(amazonAdvIdObject.isLimitAdTracking()));
        }
        String advertisingId = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM);
        if (advertisingId != null) {
            statsParams.put(ServerParameters.ADVERTISING_ID_PARAM, advertisingId);
        }
        statsParams.put(ServerParameters.APP_ID, context.get().getPackageName());
        statsParams.put(ServerParameters.DEV_KEY, afDevKey);
        statsParams.put(ServerParameters.AF_USER_ID, Installation.id(context));
        statsParams.put(ServerParameters.TIME_SPENT_IN_APP, String.valueOf(sessionTime / 1000));
        statsParams.put(ServerParameters.STATUS_TYPE, "user_closed_app");
        statsParams.put(ServerParameters.PLATFORM, "Android");
        statsParams.put(ServerParameters.LAUNCH_COUNTER ,Integer.toString(getLaunchCounter(sharedPreferences, false)));
        statsParams.put(ServerParameters.CONVERSION_DATA_TIMING, Long.toString(sharedPreferences.getLong(GET_CONVERSION_DATA_TIME,0)));
        statsParams.put(ServerParameters.CHANNEL_SERVER_PARAM, getConfiguredChannel(context));
        statsParams.put(ServerParameters.ORIGINAL_AF_UID, originalAFUID != null ? originalAFUID : "");

        if(this.isSendBackground){
            try {
                BackgroundHttpTask statTask = new BackgroundHttpTask(null, isTrackingStopped());
                statTask.bodyParameters = statsParams;
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    AFLogger.afDebugLog("Main thread detected. Running callStats task in a new thread.");
                    statTask.execute(ServerConfigHandler.getUrl(STATS_URL));
                } else {
                    AFLogger.afDebugLog("Running callStats task (on current thread: " + Thread.currentThread().toString() + " )");
                    statTask.onPreExecute();
                    statTask.onPostExecute(statTask.doInBackground(ServerConfigHandler.getUrl(STATS_URL)));
                }
            } catch (Throwable t) {
                AFLogger.afErrorLog("Could not send callStats request",t);
            }
        }
        else{
            AFLogger.afDebugLog("Stats call is disabled, ignore ...");
        }
    }

    // for Unity
    public void trackAppLaunch(Context ctx, String devKey) {

        runInBackground(ctx, devKey, null, null, "" ,true, ctx instanceof Activity ? ((Activity) ctx).getIntent() : null);
    }

    // for Unity's Helper Class
    protected void setDeepLinkData(Intent intent) {
        try {
            if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
                latestDeepLink = intent.getData();
                AFLogger.afDebugLog("Unity setDeepLinkData = " + latestDeepLink);
            }
        }catch (Throwable t) {
            AFLogger.afErrorLog("Exception while setting deeplink data (unity). ",t);
        }
    }

    public void reportTrackSession(Context ctx) {
        ProxyManager.getInstance().addApiEvent("reportTrackSession");
        // Disabling the RemoteDebugging for apps with no frequent Foreground/Background event - Mostly Service-based apps (Anti-Virus, for instance)
        ProxyManager.getInstance().disableRemoteDebuggingForThisApp();
        trackEventInternal(ctx,null,null);
    }

    public void trackEvent(Context context, String eventName, Map<String,Object> eventValues){
        JSONObject eventValuesJSON = new JSONObject(eventValues == null ? new HashMap() : eventValues);
        ProxyManager.getInstance().addApiEvent("trackEvent", eventName, eventValuesJSON.toString());
        trackEventInternal(context,eventName,eventValues);
    }

    void trackEventInternal(Context context, String eventName, Map<String, Object> eventValues){
        Intent activityIntent = context instanceof Activity ? ((Activity) context).getIntent() : null;
        String afDevKey = getProperty(AppsFlyerProperties.AF_KEY);
        if (afDevKey == null) {
            AFLogger.afWarnLog("[TrackEvent/Launch] AppsFlyer's SDK cannot send any event without providing DevKey.");
            return;
        }
        JSONObject eventValuesJSON = new JSONObject(eventValues == null ? new HashMap() : eventValues);
        String referrer = AppsFlyerProperties.getInstance().getReferrer(context);
        runInBackground(context, null, eventName, eventValuesJSON.toString(), referrer == null ? "" : referrer,true, activityIntent);
    }

    private void monitor(Context context, String eventIdentifier, String message, String value) {
        if (AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.IS_MONITOR,false)){
            // tell other SDK to send back messages
            Intent localIntent = new Intent(MonitorMessages.BROADCAST_ACTION);
            localIntent.setPackage("com.appsflyer.nightvision");
            localIntent.putExtra(MonitorMessages.MESSAGE, message);
            localIntent.putExtra(MonitorMessages.VALUE, value);
            localIntent.putExtra(MonitorMessages.PACKAGE, "true");
            localIntent.putExtra(MonitorMessages.PROCESS_ID, new Integer(android.os.Process.myPid()));
            localIntent.putExtra(MonitorMessages.EVENT_IDENTIFIER, eventIdentifier);
            localIntent.putExtra(MonitorMessages.SDK_VERSION, BUILD_NUMBER);

            context.sendBroadcast(localIntent);

        }
    }

    void callRegisterBackground(Context context, String token) {

        if (waitingForId()) {
            AFLogger.afInfoLog("CustomerUserId not set, Tracking is disabled", true);
            return;
        }

        final Map<String,String> registerParams = new HashMap<>();
        final String afDevKey = getProperty(AppsFlyerProperties.AF_KEY);
        if (afDevKey == null) {
            AFLogger.afWarnLog("[registerUninstall] AppsFlyer's SDK cannot send any event without providing DevKey.");
            return;
        }


        final PackageManager packageManager = context.getPackageManager();
        final String packageName = context.getPackageName();
        try {
            // app version
            final PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            registerParams.put(ServerParameters.APP_VERSION_CODE,Integer.toString(packageInfo.versionCode));
            registerParams.put(ServerParameters.APP_VERSION_NAME, packageInfo.versionName);

            // app name
            final String appName = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
            registerParams.put(ServerParameters.APP_NAME, appName);

            // install date
            final long firstInstallTime = packageInfo.firstInstallTime;
            final SimpleDateFormat dateFormatter = getDataFormatter(INSTALL_UPDATE_DATE_FORMAT);
            registerParams.put(ServerParameters.INSTALL_DATE, dateFormatUTC(dateFormatter, firstInstallTime));
        } catch (Throwable e){
            AFLogger.afErrorLog("Exception while collecting application version info.",e);
        }
        addNetworkData(context, registerParams);

        // Customer User ID
        final String appUserId = getCustomerUserId();
        if (appUserId != null) {
            registerParams.put(ServerParameters.APP_USER_ID,appUserId);
        }

        // device type (brand & model)
        try {
            registerParams.put(ServerParameters.MODEL, Build.MODEL);
            registerParams.put(ServerParameters.BRAND, Build.BRAND);
        } catch (Throwable e){
            AFLogger.afErrorLog("Exception while collecting device brand and model.",e);
        }

        // device ids
        final boolean deviceTrackingDisabled = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.DEVICE_TRACKING_DISABLED,false);
        if (deviceTrackingDisabled) {
            registerParams.put(ServerParameters.DEVICE_TRACKING_DISABLED, "true");
        }

        final AdvertisingIdObject amazonAdvIdObject = AdvertisingIdUtil.getAmazonAID(context.getContentResolver());
        if (amazonAdvIdObject != null) {
            registerParams.put(ServerParameters.AMAZON_AID,amazonAdvIdObject.getAdvertisingId());
            registerParams.put(ServerParameters.AMAZON_AID_LIMIT, String.valueOf(amazonAdvIdObject.isLimitAdTracking()));
        }

        final String advertisingId = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM);
        if (advertisingId != null) {
            registerParams.put(ServerParameters.ADVERTISING_ID_PARAM, advertisingId);
        }

        registerParams.put(ServerParameters.DEV_KEY, afDevKey);
        registerParams.put(ServerParameters.AF_USER_ID, Installation.id(new WeakReference<>(context)));
        registerParams.put(ServerParameters.AF_GCM_TOKEN, token);
        final int counter = getLaunchCounter(getSharedPreferences(context), false);
        registerParams.put(ServerParameters.LAUNCH_COUNTER, Integer.toString(counter));
        registerParams.put(ServerParameters.ANDROID_SDK_INT,Integer.toString(android.os.Build.VERSION.SDK_INT));
        final String configuredChannel = getConfiguredChannel(new WeakReference<>(context));
        if (configuredChannel != null) {
            registerParams.put(ServerParameters.CHANNEL_SERVER_PARAM, configuredChannel);
        }

        try {
            final BackgroundHttpTask registerTask = new BackgroundHttpTask(context, isTrackingStopped());
            registerTask.bodyParameters = registerParams;
            final String url = ServerConfigHandler.getUrl(REGISTER_URL) + packageName;
            registerTask.execute(url);
        } catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(),t);
        }
    }

    private static void broadcastBacktoTestApp(Context context, String paramsString) {

        Intent localIntent = new Intent(MonitorMessages.TEST_INTEGRATION_ACTION);
        localIntent.putExtra("params", paramsString);
        context.sendBroadcast(localIntent);

    }


    public void setDeviceTrackingDisabled(boolean isDisabled){
        ProxyManager.getInstance().addApiEvent("setDeviceTrackingDisabled", String.valueOf(isDisabled));
        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.DEVICE_TRACKING_DISABLED, isDisabled);
    }

    /*
     Return the cached attribution data
     */
    private Map<String,String> getConversionData(Context context) throws AttributionIDNotReady {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AF_SHARED_PREF, 0);
// RD-8172 - remove referrer parsing for GCD (always go to server)
//        String referrer = AppsFlyerProperties.getInstance().getReferrer(context);
//        if (referrer != null && referrer.length() > 0 && referrer.contains("af_tranid")){
//            return referrerStringToMap(context, referrer);
//        }
        String attributionString = sharedPreferences.getString(ATTRIBUTION_ID_PREF,null);

        if (attributionString != null && attributionString.length() > 0){
            return attributionStringToMap(attributionString);
        } else {
            throw new AttributionIDNotReady();
        }
    }

    public void registerConversionListener(Context context, AppsFlyerConversionListener conversionDataListener){
        ProxyManager.getInstance().addApiEvent("registerConversionListener");
        registerConversionListenerInternal(context, conversionDataListener);
    }

    //TODO: remove unused Context parameter
    private void registerConversionListenerInternal(Context context, AppsFlyerConversionListener conversionDataListener){
        if (conversionDataListener == null){
            return;
        }
        AppsFlyerLib.conversionDataListener = conversionDataListener;
    }

    public void unregisterConversionListener(){
        ProxyManager.getInstance().addApiEvent("unregisterConversionListener");
        AppsFlyerLib.conversionDataListener = null;
    }

    public void registerValidatorListener(Context context, AppsFlyerInAppPurchaseValidatorListener validationListener){
        ProxyManager.getInstance().addApiEvent("registerValidatorListener");

        AFLogger.afDebugLog("registerValidatorListener called");

        if (validationListener == null) {
            AFLogger.afDebugLog("registerValidatorListener null listener");
            return;
        }
        AppsFlyerLib.validatorListener = validationListener;

    }

    // For Unity's Helper Class
    protected void getConversionData(final Context context, final ConversionDataListener conversionDataListener) {
        registerConversionListenerInternal(context, new AppsFlyerConversionListener() {
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                conversionDataListener.onConversionDataLoaded(conversionData);
            }

            public void onInstallConversionFailure(String errorMessage) {
                conversionDataListener.onConversionFailure(errorMessage);
            }

            public void onAppOpenAttribution(Map<String, String> attributionData) {

            }

            public void onAttributionFailure(String errorMessage) {

            }
        });
    }


    private Map<String, String> referrerStringToMap(Context context, String referrer)  {
        final Map<String, String> conversionData = new LinkedHashMap<>();
        final String[] pairs = referrer.split("&");
        boolean didFindPrt = false;

        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            String name = idx > 0 ? pair.substring(0, idx) : pair;
            if (!conversionData.containsKey(name)) {

                if (name.equals("c")){
                    name = "campaign";
                } else if (name.equals("pid")){
                    name = "media_source";
                } else if (name.equals("af_prt")){
                    didFindPrt = true;
                    name = "agency";
                }

                conversionData.put(name, "");
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
            conversionData.put(name, value);
        }
        try {
            if (!conversionData.containsKey("install_time")) {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                // ***Note this will work only on android 9 and above!!!!!!!!!!!!!!!!!!!!!!!!
                conversionData.put("install_time", dateFormatUTC(getDataFormatter("yyyy-MM-dd HH:mm:ss"),
                        packageInfo.firstInstallTime));
            }
        } catch (Exception e){
            AFLogger.afErrorLog("Could not fetch install time. ", e);
        }
        if (!conversionData.containsKey("af_status")){
            conversionData.put("af_status","Non-organic");
        }

        if (didFindPrt) {
            conversionData.remove("media_source");
        }


        return conversionData;
    }

    private String dateFormatUTC(SimpleDateFormat dateFormat, long time) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    private SimpleDateFormat getDataFormatter(String pattern) {
        return new SimpleDateFormat(pattern, Locale.US);
    }


    private Map<String,String> attributionStringToMap(String inputString){
        Map<String,String> conversionData = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(inputString);
            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                if (!IGNORABLE_KEYS.contains(key)){
                    String value = jsonObject.getString(key);
                    if (!TextUtils.isEmpty(value) && !"null".equals(value)) {
                        conversionData.put(key, value);
                    }
                }
            }
        } catch(JSONException e) {
            AFLogger.afErrorLog(e.getMessage(),e);
            return null;
        }

        return conversionData;
    }


    void runInBackground(Context context, String appsFlyerKey, String eventName, String eventValue, String referrer, boolean isNewAPI, Intent activityIntent){
        context = context.getApplicationContext();
        boolean isLaunchEvent = eventName == null;

        if (waitingForId()) {
            AFLogger.afInfoLog("CustomerUserId not set, Tracking is disabled", true);
            return;
        }


        if (isLaunchEvent) {
            // opt-out for non native platforms
            boolean launchProtectEnabled = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.LAUNCH_PROTECT_ENABLED, true);
            if (launchProtectEnabled) {
                if (isLaunchWithinBlockingTimeWindow()) {
                    return;
                }
            } else {
                AFLogger.afInfoLog("Allowing multiple launches within a 5 second time window.");
            }
            lastLaunchAttemptTimestamp = System.currentTimeMillis();
        }
        ScheduledExecutorService scheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();
//        scheduler.schedule(new DataCollector(new WeakReference<>(context), appsFlyerKey, eventName,
//                eventValue, referrer, isNewAPI, scheduler, false), 150, TimeUnit.MILLISECONDS);

        Runnable job = new DataCollector(
                new WeakReference<>(context),
                appsFlyerKey,
                eventName,
                eventValue,
                referrer,
                isNewAPI,
                scheduler,
                false, activityIntent);

        scheduleJob(scheduler, job, 150, TimeUnit.MILLISECONDS);
    }

    boolean isLaunchWithinBlockingTimeWindow() {
        if (lastLaunchAttemptTimestamp > 0) { // if this is the 2nd or later LAUNCH event
            long now = System.currentTimeMillis();
            long timeSinceLastLaunchAttempt = (now - lastLaunchAttemptTimestamp);
            final SimpleDateFormat dateFormatter = getDataFormatter("yyyy/MM/dd HH:mm:ss.SSS Z");
            String lastLaunchAttempt = dateFormatUTC(dateFormatter, lastLaunchAttemptTimestamp);
            String lastSuccessfulLaunchTime = dateFormatUTC(dateFormatter, lastSuccessfulLaunchTimestamp);

            if ((timeSinceLastLaunchAttempt < launchBlockingTimeWindow) && !isTrackingStopped()) { // block the launch
                String format = "Last Launch attempt: %s;\nLast successful Launch event: %s;\nThis launch is blocked: %s ms < %s ms";
                AFLogger.afInfoLog(String.format(Locale.US, format, lastLaunchAttempt, lastSuccessfulLaunchTime, timeSinceLastLaunchAttempt, launchBlockingTimeWindow));
                // TODO: maybe add all the blocked launches timestamps to the next successful launch request's body.
                return true;
            } else {  // send the launch
                if (!isTrackingStopped()) {
                    String format = "Last Launch attempt: %s;\nLast successful Launch event: %s;\nSending launch (+%s ms)";
                    AFLogger.afInfoLog(String.format(Locale.US, format, lastLaunchAttempt, lastSuccessfulLaunchTime, timeSinceLastLaunchAttempt));
                }
            }
        } else { // if this is the 1st LAUNCH event
            if (!isTrackingStopped()) {
                AFLogger.afInfoLog("Sending first launch for this session!");
            }
        }
        return false;
    }

    private void backgroundReferrerLaunch(Context context, String referrer, Intent activityIntent){
        if (referrer != null && referrer.length() > 5) {
            ScheduledExecutorService scheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();
//            scheduler.schedule(new DataCollector(new WeakReference<>(context),
//                    null, null, null, referrer,
//                    true, scheduler, true), 5, TimeUnit.MILLISECONDS);

            Runnable job = new DataCollector(
                    new WeakReference<>(context.getApplicationContext()),
                    null,
                    null,
                    null,
                    referrer,
                    true,
                    scheduler,
                    true, activityIntent);

            //scheduler.schedule(job, 5, TimeUnit.MILLISECONDS);
            scheduleJob(scheduler, job, 5, TimeUnit.MILLISECONDS);
        }
    }


    private void sendTrackingWithEvent(Context context, String appsFlyerKey,
                                       String eventName, String eventValue,
                                       String referrer, boolean isUseNewAPI, boolean isBackgroundReferrerLaunch, Intent activityIntent) {

        if (context == null) {
            AFLogger.afDebugLog("sendTrackingWithEvent - got null context. skipping event/launch.");
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(context);
        AppsFlyerProperties.getInstance().saveProperties(sharedPreferences);
        if (!isTrackingStopped()) {
            AFLogger.afInfoLog("sendTrackingWithEvent from activity: " + context.getClass().getName());
        }
        boolean isLaunchEvent = eventName == null;


        Map<String,Object> params = getEventParameters(context, appsFlyerKey, eventName, eventValue,
                referrer, isUseNewAPI, sharedPreferences, isLaunchEvent, activityIntent);
        String afDevKey = (String) params.get(ServerParameters.AF_DEV_KEY);
        if (afDevKey == null || afDevKey.length() == 0){
            AFLogger.afDebugLog("Not sending data yet, waiting for dev key");
            return;
        }
        if (!isTrackingStopped()) {
            AFLogger.afInfoLog("AppsFlyerLib.sendTrackingWithEvent");
        }
        String urlString;
        if (isLaunchEvent) {
            if (isBackgroundReferrerLaunch) {
                urlString = ServerConfigHandler.getUrl(REFERRER_TRACKING_URL);
            } else {
                urlString = ServerConfigHandler.getUrl(APPS_TRACKING_URL);
            }
        } else {
            urlString = ServerConfigHandler.getUrl(EVENTS_TRACKING_URL);
        }
        urlString += context.getPackageName();

        int launchCounter = getLaunchCounter(sharedPreferences, false);

        Runnable job = new SendToServerRunnable(
                urlString,
                params,
                context.getApplicationContext(),
                isLaunchEvent,
                launchCounter);


        // catch first 2 launches
        if (isLaunchEvent && allowInstallReferrer(context)) {
           //send install referrer with launch if we got it already or not supported:
            if (isInstallReferrerAvailable()){
                job.run();
            }
            else{// lets wait for 500 sec till we get new referrer
                AFLogger.afDebugLog("Failed to get new referrer, wait ...");
                ScheduledExecutorService scheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();
                scheduleJob(scheduler, job, 500, TimeUnit.MILLISECONDS);
            }
        }
        else{
            job.run();
        }
    }

    private boolean isInstallReferrerAvailable(){
        return installReferrerMap != null && installReferrerMap.size() > 0;
    }

    Map<String,Object> getEventParameters(Context context,
                                          String appsFlyerKey,
                                          String eventName,
                                          String eventValue,
                                          String referrer,
                                          boolean isUseNewAPI,
                                          SharedPreferences sharedPreferences,
                                          boolean isLaunchEvent, Intent activityIntent) {
        Map<String,Object> params = new HashMap<>();
        AdvertisingIdUtil.addGoogleAID(context, params);

        final long lastServerReportTimestamp = new Date().getTime();
        params.put(ServerParameters.TIMESTAMP, Long.toString(lastServerReportTimestamp));

        String cSum = AFSecretsManager.getCheckSum(context, lastServerReportTimestamp);
        if (cSum != null){
            params.put(ServerParameters.CHECK_SUM1, cSum);
        }

        try {
           if (!isTrackingStopped()) {
               AFLogger.afInfoLog(LogMessages.EVENT_CREATED_WITH_NAME + (isLaunchEvent ? "Launch" : eventName));
           }else {
               AFLogger.afInfoLog("SDK tracking has been stopped");
            }
            monitor(context, AppsFlyerLib.LOG_TAG, MonitorMessages.EVENT_CREATED_WITH_NAME,isLaunchEvent ? "Launch" : eventName);
            CacheManager.getInstance().init(context);

            try {
                // permissions
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
                List<String> requestedPermissions = Arrays.asList(packageInfo.requestedPermissions);
                if (!requestedPermissions.contains("android.permission.INTERNET")){
                    AFLogger.afWarnLog(LogMessages.PERMISSION_INTERNET_MISSING);
                    monitor(context, null, MonitorMessages.PERMISSION_INTERNET_MISSING,null);
                }
                if (!requestedPermissions.contains("android.permission.ACCESS_NETWORK_STATE")){
                    AFLogger.afWarnLog(LogMessages.PERMISSION_ACCESS_NETWORK_MISSING);
                }
                if (!requestedPermissions.contains("android.permission.ACCESS_WIFI_STATE")){
                    AFLogger.afWarnLog(LogMessages.PERMISSION_ACCESS_WIFI_MISSING);
                }
            } catch (Exception e){
                AFLogger.afErrorLog("Exception while validation permissions. ",e);
            }

            if (isUseNewAPI){
                params.put("af_events_api",IN_APP_EVENTS_API);
            }
            params.put(ServerParameters.BRAND,android.os.Build.BRAND);
            params.put("device",android.os.Build.DEVICE);
            params.put("product",android.os.Build.PRODUCT); // key was brand
            params.put(ServerParameters.ANDROID_SDK_INT,Integer.toString(android.os.Build.VERSION.SDK_INT));
            params.put(ServerParameters.MODEL,Build.MODEL);
            params.put("deviceType",Build.TYPE);

            if (isLaunchEvent){
                if (isAppsFlyerFirstLaunch(context)) {
                    if (!AppsFlyerProperties.getInstance().isOtherSdkStringDisabled()) {
                        params.put(ServerParameters.OTHER_SDKS, generateOtherSDKsString());
                        float batteryLevel = getBatteryLevel(context);
                        params.put(ServerParameters.DEVICE_CURRENT_BATTERY_LEVEL, String.valueOf(batteryLevel));
                    }
                    getReInstallData(context);
                }
                params.put(ServerParameters.TIME_PASSED_SINCE_LAST_LAUNCH, Long.toString(getTimePassedSinceLastLaunch(context, true)));

                addOneLinkData(params);
            } else {
                lastEventsProcessing(context, params, eventName, eventValue);
            }

            String originalAFUID = getProperty(AFKeystoreWrapper.AF_KEYSTORE_UID);
            String reInstallCounter = getProperty(AFKeystoreWrapper.AF_KEYSTORE_REINSTALL_COUNTER);
            if (originalAFUID != null && reInstallCounter != null && Integer.valueOf(reInstallCounter) > 0) {
                params.put(ServerParameters.REINSTALL_COUNTER,reInstallCounter);
                params.put(ServerParameters.ORIGINAL_AF_UID,originalAFUID);
            }

            String customData = getProperty(AppsFlyerProperties.ADDITIONAL_CUSTOM_DATA);
            if (customData != null) {
                params.put("customData", customData);
            }

            try {
                String installerPackage = context.getPackageManager().getInstallerPackageName(context.getPackageName());
                if (installerPackage != null){
                    params.put("installer_package",installerPackage);
                }
            } catch (Exception e){
                AFLogger.afErrorLog("Exception while getting the app's installer package. ",e);
            }

            String sdkExtension = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.EXTENSION);
            if (sdkExtension != null && sdkExtension.length() > 0){
                params.put("sdkExtension",sdkExtension);
            }

            String currentChannel = getConfiguredChannel(new WeakReference<>(context));

            String originalChannel = getCachedChannel(context,currentChannel);
            if (originalChannel != null){
                params.put(ServerParameters.CHANNEL_SERVER_PARAM,originalChannel);
            }

            if (originalChannel != null && !originalChannel.equals(currentChannel)
                    || originalChannel == null && currentChannel != null){
                params.put(ServerParameters.LATEST_CHANNEL_SERVER_PARAM,currentChannel);
            }

            String installStore = getCachedStore(context);
            if (installStore != null){
                params.put(ServerParameters.INSTALL_STORE,installStore.toLowerCase());
            }

            String preInstallName = getPreInstallName(context);
            if (preInstallName != null){
                params.put(ServerParameters.PRE_INSTALL_NAME,preInstallName.toLowerCase());
            }

            String currentStore = getCurrentStore(context);
            if (currentStore != null){
                params.put(ServerParameters.CURRENT_STORE,currentStore.toLowerCase());
            }

            if (appsFlyerKey != null && appsFlyerKey.length() >= 0) {
                params.put(ServerParameters.AF_DEV_KEY,appsFlyerKey);
            } else {
                String afKeyFromProperties = getProperty(AppsFlyerProperties.AF_KEY);
                if (afKeyFromProperties != null && afKeyFromProperties.length() >= 0) {
                    params.put(ServerParameters.AF_DEV_KEY,afKeyFromProperties);
                } else {
                    AFLogger.afInfoLog(LogMessages.DEV_KEY_MISSING);
                    monitor(context,LOG_TAG,MonitorMessages.DEV_KEY_MISSING,null);
                    AFLogger.afInfoLog("AppsFlyer will not track this event.");
                    return null;
                }
            }

            String appUserId = getCustomerUserId();
            if (appUserId != null){
                params.put(ServerParameters.APP_USER_ID,appUserId);
            }

            String emailData = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.USER_EMAILS);

            if (emailData != null) {
                params.put("user_emails", emailData);
            } else { // should be removed in the future and not use from now on
                String userEmail = getProperty(AppsFlyerProperties.USER_EMAIL);
                if (userEmail != null) {
                    params.put("sha1_el", HashUtils.toSHA1(userEmail)); // for testing todo remove it
                }
            }

            if (eventName != null){
                params.put(ServerParameters.EVENT_NAME,eventName);
                if (eventValue != null) {
                    params.put(ServerParameters.EVENT_VALUE, eventValue);
                }
            }

            if(getAppId() != null) {
                params.put("appid", getProperty(AppsFlyerProperties.APP_ID));
            }
            String currencyCode = getProperty(AppsFlyerProperties.CURRENCY_CODE);
            if(currencyCode != null) {
                if(currencyCode.length() != 3)  {
                    AFLogger.afWarnLog((new StringBuilder()).append(LogMessages.WARNING_PREFIX + "currency code should be 3 characters!!! '").append(currencyCode).append("' is not a legal value.").toString());
                }
                params.put("currency", currencyCode);
            }

            String isUpdate = getProperty(AppsFlyerProperties.IS_UPDATE);
            if(isUpdate != null) {
                params.put("isUpdate", isUpdate);
            }
            boolean isPreInstall = isPreInstalledApp(context);
            params.put("af_preinstalled",Boolean.toString(isPreInstall));

            boolean shouldCollectFBId = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.COLLECT_FACEBOOK_ATTR_ID, true);

            if (shouldCollectFBId) {
                String facebookAttributeId;
                try{
                    context.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
                    facebookAttributeId = getAttributionId(context.getContentResolver());
                } catch (PackageManager.NameNotFoundException ignored) {
                    facebookAttributeId = null;
                    AFLogger.afWarnLog("Exception while collecting facebook's attribution ID. ");
                } catch (Throwable t) {
                    facebookAttributeId = null;
                    AFLogger.afErrorLog("Exception while collecting facebook's attribution ID. ", t);
                }
                if (facebookAttributeId != null){
                    params.put("fb",facebookAttributeId);
                }
            }

            addDeviceTracking(context, params);

            try {
                String uid = Installation.id(new WeakReference<>(context));
                if(uid != null)
                    params.put(ServerParameters.AF_USER_ID, uid);
            }
            catch(Exception e){
                AFLogger.afErrorLog((new StringBuilder()).append(LogMessages.ERROR_PREFIX).append("could not get uid ").append(e.getMessage()).toString(),e);
            }

            try {
                params.put(ServerParameters.LANG, Locale.getDefault().getDisplayLanguage());
            } catch(Exception e) {
                AFLogger.afErrorLog("Exception while collecting display language name. ",e);
            }

            try {
                params.put(ServerParameters.LANG_CODE, Locale.getDefault().getLanguage());
            } catch(Exception e) {
                AFLogger.afErrorLog("Exception while collecting display language code. ",e);
            }

            try {
                params.put(ServerParameters.COUNTRY, Locale.getDefault().getCountry());
            } catch(Exception e) {
                AFLogger.afErrorLog("Exception while collecting country name. ",e);
            }

            params.put(ServerParameters.PLATFORM_EXTENSION, platformExtension.getAvailablePlatformExtension());

            addNetworkData(context, params);


            SimpleDateFormat dataFormatter = getDataFormatter(INSTALL_UPDATE_DATE_FORMAT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
                try {
                    long installed = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
                    params.put(ServerParameters.INSTALL_DATE, dateFormatUTC(dataFormatter, installed));
                } catch (Exception e){
                    AFLogger.afErrorLog("Exception while collecting install date. ",e);
                }
            }

            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

                int versioncode = sharedPreferences.getInt(VERSION_CODE, 0);

                if (packageInfo.versionCode > versioncode) {
                    // New version detected.
                    // Zeroing the conversion data error counter.
                    saveIntegerToSharedPreferences(context, CONVERSION_REQUEST_RETRIES, 0);
                    saveIntegerToSharedPreferences(context, VERSION_CODE, packageInfo.versionCode);

                }

                params.put(ServerParameters.APP_VERSION_CODE,Integer.toString(packageInfo.versionCode));
                params.put(ServerParameters.APP_VERSION_NAME, packageInfo.versionName);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {

                    // ***Note this will work only on android 9 and above!!!!!!!!!!!!!!!!!!!!!!!!
                    long firstInstallTime = packageInfo.firstInstallTime;
                    long lastUpdateTime = packageInfo.lastUpdateTime;
                    params.put("date1", dateFormatUTC(dataFormatter, firstInstallTime));
                    params.put("date2", dateFormatUTC(dataFormatter, lastUpdateTime));
                    String firstInstallDate = getFirstInstallDate(dataFormatter, context);
                    params.put("firstLaunchDate", firstInstallDate);
                }


            } catch (Throwable t){
                AFLogger.afErrorLog("Exception while collecting app version data ",t);
            }

            if (referrer.length() > 0){
                params.put(REFERRER_PREF,referrer);
            }
// we don't need install attribution
//            String attributionString = sharedPreferences.getString(ATTRIBUTION_ID_PREF, null);
//            if (attributionString != null && attributionString.length() > 0){
//                params.put("installAttribution",attributionString);
//            }

            String referrersString = sharedPreferences.getString(EXTRA_REFERRERS_PREF, null);
            if (referrersString != null) { // other referrers received after the main one
                params.put(EXTRA_REFERRERS_PREF, referrersString);
            }


            String uninstallToken = getProperty(AppsFlyerProperties.AF_UNINSTALL_TOKEN);
            if (uninstallToken != null) {
                AFUninstallToken tokenObject = AFUninstallToken.parse(uninstallToken);
                if (tokenObject != null) {
                    params.put(ServerParameters.AF_GCM_TOKEN, tokenObject.getToken());
                }
            }

            // Uninstall Tracking pre-condition (InstanceID Service existence)
            isTokenRefreshServiceConfigured = UninstallUtils.didConfigureTokenRefreshService(context);
            AFLogger.afDebugLog("didConfigureTokenRefreshService="+ isTokenRefreshServiceConfigured);
            if (!isTokenRefreshServiceConfigured) {
                params.put(ServerParameters.TOKEN_REFRESH_CONFIGURED,false);
            }

            // Push Notification Measurement
            if (isLaunchEvent) {
                if (pushPayload != null) {
                    JSONObject jsonPushPayload = new JSONObject(pushPayload);
                    jsonPushPayload.put("isPush","true");
                    params.put(ServerParameters.DEEP_LINK,jsonPushPayload.toString());
                }
                pushPayload = null;
            }

            // DeepLink
            if (isLaunchEvent) {
                Uri uri = getDeepLinkUri(activityIntent);
                if (uri != null) {
                    handleDeepLinkCallback(context,params,uri);
                } else if (latestDeepLink != null) {
                    // For Unity
                    handleDeepLinkCallback(context, params, latestDeepLink);
                }
            }

            // Integration TestApp - Retargeting mode
            if(isRetargetingTestMode) {
                params.put("testAppMode_retargeting", "true");
                JSONObject paramsJSON = new JSONObject(params);
                broadcastBacktoTestApp(context, paramsJSON.toString());
                AFLogger.afInfoLog("Sent retargeting params to test app");
            }

            // Integration TestApp - Referrer mode
            if (isInTestMode(context)) {
                params.put("testAppMode","true");
                JSONObject paramsJSON = new JSONObject(params);
                broadcastBacktoTestApp(context, paramsJSON.toString());
                AFLogger.afInfoLog("Sent params to test app");
                endTestMode();
            }

            if (getProperty(ServerParameters.ADVERTISING_ID_PARAM) == null) {
                AdvertisingIdUtil.addGoogleAID(context, params);
                if (getProperty(ServerParameters.ADVERTISING_ID_PARAM) != null) {
                    params.put("GAID_retry", "true");
                } else {
                    params.put("GAID_retry","false");
                }
            }

            AdvertisingIdObject amazonAdvIdObject = AdvertisingIdUtil.getAmazonAID(context.getContentResolver());
            if (amazonAdvIdObject != null) {
                params.put(ServerParameters.AMAZON_AID,amazonAdvIdObject.getAdvertisingId());
                params.put(ServerParameters.AMAZON_AID_LIMIT, String.valueOf(amazonAdvIdObject.isLimitAdTracking()));
            }

            // moving the 'params' part of SendRequestToServer
            boolean sentSuccessfully = false;
            String referrerFromProperties = AppsFlyerProperties.getInstance().getReferrer(context);
            if (referrerFromProperties != null && referrerFromProperties.length() > 0 && params.get(REFERRER_PREF) == null){
                //referrer exist in storage but not in the URL - we need to add it
                params.put(REFERRER_PREF,referrerFromProperties);
            }
            sentSuccessfully = "true".equals(sharedPreferences.getString(SENT_SUCCESSFULLY_PREF,""));

            boolean sentRegisterRequestSuccessfully = sharedPreferences.getBoolean(AppsFlyerProperties.AF_UNINSTALL_TOKEN_SENT, false);
            params.put(ServerParameters.REGISTERED_TO_UNINSTALL, sentRegisterRequestSuccessfully);
            int launchCounter = getLaunchCounter(sharedPreferences, isLaunchEvent);
            params.put("counter", Integer.toString(launchCounter)); // eventName == null on launch
            params.put("iaecounter", Integer.toString(getNonlaunchCounter(sharedPreferences, eventName != null))); // eventName == null on launch

            if (isLaunchEvent && launchCounter == 1) { // we set it as late as we can.
                AppsFlyerProperties.getInstance().setFirstLaunchCalled();
                if(getProperty(AppsFlyerProperties.AF_WAITFOR_CUSTOMERID, false)){
                    params.put("wait_cid", Boolean.toString(true));
                }
            }
            params.put("isFirstCall",Boolean.toString(!sentSuccessfully));

            // ******* Add params to deviceData ******* //
            updateDeviceData(context, isLaunchEvent, params, launchCounter);

            // for verification against frauds
            String hash = new HashUtils().getHashCode(params);
            params.put("af_v", hash);

            String hashV2 = new HashUtils().getHashCodeV2(params);
            params.put("af_v2", hashV2);


            // Check if stop tracking has been ever used
            if(sharedPreferences.contains(IS_STOP_TRACKING_USED)) {
                params.put(ServerParameters.IS_STOP_TRACKING_USED, String.valueOf(sharedPreferences.getBoolean(IS_STOP_TRACKING_USED, false)));
            }
        } catch (Throwable e) {
            AFLogger.afErrorLog(e.getLocalizedMessage(),e);
        }
        return params;
    }

    private void updateDeviceData(Context context, boolean isLaunchEvent, Map<String, Object> params, int launchCounter) {
        // Prepare the system data
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("cpu_abi", getSystemProperty("ro.product.cpu.abi"));
        deviceData.put("cpu_abi2", getSystemProperty("ro.product.cpu.abi2"));
        deviceData.put("arch", getSystemProperty("os.arch"));
        deviceData.put("build_display_id", getSystemProperty("ro.build.display.id"));

        if (isLaunchEvent) {
            if(isCollectLocation){
                // Add last known location and a battery status to the report
                Map<String, Object> locationData = getLocationData(context);
                if (!locationData.isEmpty()) {
                    deviceData.put(ServerParameters.LOCATION_KEY, locationData);
                }
            }

            addBatteryData(context, deviceData);

            // Sensors data will be sent only in two first launches
            if(AFSensorManager.LAUNCH_COUNTER >= launchCounter) {
                // Prepare sensors data to be added to params
                List<Map<String, Object>> sensorData = AFSensorManager.getInstance(context).getData();
                if (!sensorData.isEmpty()) {
                    deviceData.put(AFSensorManager.SENSORS_KEY, sensorData);
                }
            }
        }

        Map<String,String> metrics = AFScreenManager.getScreenMetrics(context);
        deviceData.put(AFScreenManager.SCREEN_KEY, metrics);

        // Add the collected data to params as a "deviceData"
        params.put(ServerParameters.DEVICE_DATA, deviceData);
    }

    private void addNetworkData(Context context, Map<String, ? super String> registerParams) {
        AFNetworkManager.NetworkData networkData = AFNetworkManager.getInstance().getData(context);
        registerParams.put(ServerParameters.NETWORK, networkData.getNetworkType());
        if (null != networkData.getSimOperator()) {
            registerParams.put(ServerParameters.OPERATOR, networkData.getSimOperator());
        }
        if (null != networkData.getCarrierName()) {
            registerParams.put(ServerParameters.CARRIER, networkData.getCarrierName());
        }
    }

    private void addBatteryData(Context context, Map<String, Object> deviceData)  {
        AFBatteryManager.BatteryData batteryData = AFBatteryManager.getInstance().getData(context);
        deviceData.put(ServerParameters.BATTERY_LEVEL_KEY, Float.toString(batteryData.getLevel()));
        if (batteryData.getCharging() != null) {
            deviceData.put(ServerParameters.BATTERY_CHARGING_KEY, batteryData.getCharging());
        }
    }

    @Nullable
    private Map<String, Object> getLocationData(Context context)  {
       Location data = AFLocationManager.getInstance().getData(context);
        Map<String, Object> location = new HashMap<>(3);
        if (data != null) {
            location.put(ServerParameters.LAT_KEY, String.valueOf(data.getLatitude()));
            location.put(ServerParameters.LON_KEY, String.valueOf(data.getLongitude()));
            location.put(ServerParameters.TIMESTAMP_KEY, String.valueOf(data.getTime()));
        }
        return location;
    }

    private void addOneLinkData(Map<String, Object> params) {
        final String onelinkId = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_ID);
        if (onelinkId != null){
            params.put(ServerParameters.ONELINK_ID,onelinkId);
            params.put(ServerParameters.ONELINK_VERSION,AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.ONELINK_VERSION));
        }
    }

    private String getPushPayloadFromIntent(Context context) {
        String _pushPayload = null;
        if (context instanceof Activity) {
            Intent intent = ((Activity) context).getIntent();
            if (intent != null) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    _pushPayload = bundle.getString("af");
                    if (_pushPayload != null) {
                        AFLogger.afInfoLog("Push Notification received af payload = " + _pushPayload);
                        bundle.remove("af");
                        ((Activity) context).setIntent(intent.putExtras(bundle));
                    }
                }
            }
        }
        return _pushPayload;
    }

    @Nullable
    private Uri getDeepLinkUri(Intent intent) {
        Uri res = null;
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            res = intent.getData();
        }
        return res;
    }

    private void handleDeepLinkCallback(Context context, Map<String, Object> params, Uri uri) {
        params.put(ServerParameters.DEEP_LINK, uri.toString());

        Map<String, String> attributionMap;
        if (uri.getQueryParameter(ServerParameters.DEEP_LINK) != null) {

            final String mediaSource = uri.getQueryParameter("media_source");
            final String isRetargeting = uri.getQueryParameter("is_retargeting");

            isRetargetingTestMode = InternalConstants.TEST_MEDIA_SOURCE.equals(mediaSource) && Boolean.parseBoolean(isRetargeting);

            attributionMap = referrerStringToMap(context, uri.getQuery());
            addValueIfNotNull(attributionMap, "path", uri.getPath());
            addValueIfNotNull(attributionMap, "scheme", uri.getScheme());
            addValueIfNotNull(attributionMap, "host", uri.getHost());

        } else {
            attributionMap = new HashMap<>();
            attributionMap.put("link", uri.toString());
        }

        final WeakReference<Context> contextRef = new WeakReference<>(context);
        GetOneLinkDataHttpTask oneLinkHttpTask = new GetOneLinkDataHttpTask(uri, this);
        oneLinkHttpTask.setConnProvider(new GetOneLinkDataHttpTask.HttpsUrlConnectionProvider());
        if (oneLinkHttpTask.isShortLinkValid()) {
            GetOneLinkDataHttpTask.OneLinkDataListener dateListener = getDateListener(attributionMap, contextRef);
            oneLinkHttpTask.setListener(dateListener);
            AFExecutor.getInstance().getThreadPoolExecutor().execute(oneLinkHttpTask);
        } else {
            onAppOpenAttribution(attributionMap);
        }

    }

    private void onAppOpenAttribution(Map<String, String> data) {
        if (conversionDataListener != null) {

        try{
            conversionDataListener.onAppOpenAttribution(data);
        } catch (Throwable e) {
            AFLogger.afErrorLog(e.getLocalizedMessage(),e);
        }
        }
    }

    private GetOneLinkDataHttpTask.OneLinkDataListener getDateListener(final Map<String, String> attributionMap,
                                                                       final WeakReference<Context> contextRef) {
        return new GetOneLinkDataHttpTask.OneLinkDataListener() {
            @Override
            public void onGetOneLinkParameters(Map<String, String> data) {
                mergeDataToAttributionMap(data);
                saveToSharedPref(attributionMap);
                onAppOpenAttribution(attributionMap);
            }

            @Override
            public void onGetOneLinkParametersError(String message) {
                if (conversionDataListener != null) {
                    saveToSharedPref(attributionMap);
                    conversionDataListener.onAttributionFailure(message);
                }
            }

            private void mergeDataToAttributionMap(Map<String, String> data) {
                for (String key : data.keySet()) {
                    attributionMap.put(key, data.get(key));
                }
            }

            private void saveToSharedPref(Map<String, String> data) {
                if (contextRef.get() != null) {
                    String json = new JSONObject(data).toString();
                    saveDataToSharedPreferences(contextRef.get(), DEEPLINK_ATTR_PREF, json);
                }
            }
        };
    }

    /**
     * +     * Add value to map with specific key if not null
     * +
     */
    private void addValueIfNotNull(Map<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    /**
     * Note - the order should never be changed !!!!!!!!
     * @return
     */
    private String generateOtherSDKsString() {
        return new StringBuilder()
                .append(numericBooleanIsClassExist("com.tune.Tune"))
                .append(numericBooleanIsClassExist("com.adjust.sdk.Adjust"))
                .append(numericBooleanIsClassExist("com.kochava.android.tracker.Feature"))
                .append(numericBooleanIsClassExist("io.branch.referral.Branch"))
                .append(numericBooleanIsClassExist("com.apsalar.sdk.Apsalar"))
                .append(numericBooleanIsClassExist("com.localytics.android.Localytics"))
                .append(numericBooleanIsClassExist("com.tenjin.android.TenjinSDK"))
                .append(numericBooleanIsClassExist("place holder for TD"))
                .append(numericBooleanIsClassExist("it.partytrack.sdk.Track"))
                .append(numericBooleanIsClassExist("jp.appAdForce.android.LtvManager"))
                .toString();
    }

    private int numericBooleanIsClassExist(String className) {
        return platformExtension.isClassExist(className) ? 1 : 0;
    }

    private void lastEventsProcessing(Context context, Map<String, Object> params, String newEventName, String newEventValue) {
        SharedPreferences sp = getSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = sp.edit();
        try {

            String previousEventName = sp.getString(PREV_EVENT_NAME,null);

            if (previousEventName != null) { // not the first event, previous event exists
                JSONObject json = new JSONObject();
                json.put(PREV_EVENT_TIMESTAMP,sp.getLong(PREV_EVENT_TIMESTAMP,-1)+"");
                json.put(PREV_EVENT_VALUE,sp.getString(PREV_EVENT_VALUE,null));
                json.put(PREV_EVENT_NAME,previousEventName);
                params.put(PREV_EVENT,json.toString());
            }

            editor.putString(PREV_EVENT_NAME,newEventName);
            editor.putString(PREV_EVENT_VALUE,newEventValue);
            editor.putLong(PREV_EVENT_TIMESTAMP, System.currentTimeMillis());
            editorCommit(editor);
        } catch (Exception e) {
            AFLogger.afErrorLog("Error while processing previous event.",e);
        }

    }

    boolean isGooglePlayServicesAvailable(Context context) {

        try {
            int statusCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (statusCode == ConnectionResult.SUCCESS) {
                return true;
            }

        } catch (Throwable t) {
            AFLogger.afErrorLog(LogMessages.WARNING_PREFIX + " Google play services is unavailable. ",t);
        }

        try {
            context.getPackageManager().getPackageInfo("com.google.android.gms", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            AFLogger.afErrorLog(LogMessages.WARNING_PREFIX + " Google Play Services is unavailable. ",e);
        }

        return false;
    }

    private void addDeviceTracking(Context context, Map<String, Object> params) {
        boolean deviceTrackingDisabled = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.DEVICE_TRACKING_DISABLED,false);

        if (deviceTrackingDisabled){
            params.put(ServerParameters.DEVICE_TRACKING_DISABLED,"true");
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            boolean collectIMEI = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.COLLECT_IMEI,true);
            String cachedImei = sharedPreferences.getString(IMEI_CACHED_PREF,null);
            String imei = null;
            if (collectIMEI){
                if (isIdCollectionAllowed(context)) {
                    try {
                        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        String deviceImei = (String) manager.getClass().getMethod("ge" + "tDe" + "vic" + 'e' + "Id").invoke(manager); // mask for Kingsoft chinese regulations. They should use collectIMEI = false, this is just for the robots
                        if (deviceImei != null) {
                            imei = deviceImei;
                        } else if (userCustomImei != null) { // fallback to user-imei in case of failure collecting device-IMEI
                            imei = userCustomImei;
                        } else if (cachedImei != null) {
                            imei = cachedImei;
                        } // else IMEI not collected
                    } catch (java.lang.reflect.InvocationTargetException ignored) {
                        AFLogger.afWarnLog(LogMessages.WARNING_PREFIX + "READ_PHONE_STATE is missing.");
                    } catch (Exception e) {
                        AFLogger.afErrorLog(LogMessages.WARNING_PREFIX + "READ_PHONE_STATE is missing. ", e);
                    }
                } else {
                    if (userCustomImei != null) {
                        imei = userCustomImei;
                    } // else IMEI not collected
                }
            } else {
                if (userCustomImei != null) {
                    imei = userCustomImei;
                } // else IMEI not collected
            }

            if (imei != null) {
                saveDataToSharedPreferences(context, IMEI_CACHED_PREF, imei);
                params.put("imei", imei);
            } else {
                AFLogger.afInfoLog("IMEI was not collected.");
            }


            boolean collectAndroidId = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.COLLECT_ANDROID_ID,true);
            String cachedAndroidId = sharedPreferences.getString(ANDROID_ID_CACHED_PREF,null);
            String androidId = null;
            if (collectAndroidId) {
                if (isIdCollectionAllowed(context)) {
                    try {
                        String deviceAndroidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (deviceAndroidId != null) {
                            androidId = deviceAndroidId;
                        } else if (userCustomAndroidId != null) { // fallback to user-android-id in case of failure collecting device-AndroidId
                            androidId = userCustomAndroidId;
                        } else if (cachedAndroidId != null) {
                            androidId = cachedAndroidId;
                        } // else Android-ID not collected
                    } catch (Exception e) {
                        AFLogger.afErrorLog(e.getMessage(),e);
                    }
                } else {
                    if (userCustomAndroidId != null) {
                        androidId = userCustomAndroidId;
                    } // else Android-ID not collected
                }
            } else {
                if (userCustomAndroidId != null) {
                    androidId = userCustomAndroidId;
                } // else Android-ID not collected
            }

            if (androidId != null) {
                saveDataToSharedPreferences(context, ANDROID_ID_CACHED_PREF, androidId);
                params.put(ServerParameters.ANDROID_ID, androidId);
            } else {
                AFLogger.afInfoLog("Android ID was not collected.");
            }

        }
    }

    private boolean isIdCollectionAllowed(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || !isGooglePlayServicesAvailable(context);
    }

    private boolean isAppsFlyerFirstLaunch(Context context){
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        return !sharedPreferences.contains(AF_COUNTER_PREF);
    }

    private String getCachedStore(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(INSTALL_STORE_PREF)) {
            return sharedPreferences.getString(INSTALL_STORE_PREF,null);
        } else {
            boolean isFirstLaunch = isAppsFlyerFirstLaunch(context);
            String store = isFirstLaunch ? getCurrentStore(context) : null;
            saveDataToSharedPreferences(context, INSTALL_STORE_PREF, store);
            return store;
        }
    }

    private String getCurrentStore(Context context) {

        return getManifestMetaData(new WeakReference<>(context),"AF_STORE");
    }

    String getSystemProperty(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Throwable e) {
            AFLogger.afErrorLog(e.getMessage(),e);
        }
        return value;
    }


    @Nullable
    private String getManifestMetaData(WeakReference<Context> context, String key) {
        if (context.get() == null) {
            return null;
        }
        return getManifestMetaData(key, context.get().getPackageManager(), context.get().getPackageName());
    }

    @Nullable
    private String getManifestMetaData(String key, PackageManager packageManager, String packageName) {
        String res = null;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            if (bundle != null){
                Object storeObj = bundle.get(key);
                if (storeObj != null){
                    res = storeObj.toString();
                }
            }
        } catch (Throwable e){
            AFLogger.afErrorLog("Could not find "+key+" value in the manifest",e);
        }

        return res;
    }

    @SuppressWarnings("ConstantConditions")
    private String preInstallValueFromFile(Context context) {

        // (1) try fetching pre-install value from system-property
        String preInstallFilePathFromSysProp = getSystemProperty(PRE_INSTALL_SYSTEM_RO_PROP); // creating system property requires root
        File preInstallFile = getFileFromString(preInstallFilePathFromSysProp);

        if (isPreInstallFileInvalid(preInstallFile)) {
            // (2) try fetching pre-install value from manifest's meta-data
            String preInstallFilePathFromManifest = getManifestMetaData(AF_PRE_INSTALL_PATH,context.getPackageManager(), context.getPackageName());
            preInstallFile = getFileFromString(preInstallFilePathFromManifest);
        }
        // (3) try fetching pre-install value from default pre-defined locations
        if (isPreInstallFileInvalid(preInstallFile)) {
            preInstallFile = getFileFromString(PRE_INSTALL_SYSTEM_DEFAULT);
        }
        if (isPreInstallFileInvalid(preInstallFile)) {
            preInstallFile = getFileFromString(PRE_INSTALL_SYSTEM_DEFAULT_ETC); // creating a file on /etc requires root (and re-mounting system with permissions)
        }

        if (isPreInstallFileInvalid(preInstallFile)) {
            return null;
        }

        String preInstallValue = extractPropertyFromFile(preInstallFile, context.getPackageName());
        if (preInstallValue != null) {
            return preInstallValue;
        }
        return null;
    }

    private String extractPropertyFromFile(File preInstallFile, String propertyKey) {
        FileReader reader = null;
        try {
            // preInstallFile is valid
            Properties props = new Properties();
            reader = new FileReader(preInstallFile);
            props.load(reader);
            AFLogger.afInfoLog("Found PreInstall property!");
            return props.getProperty(propertyKey);
        } catch (FileNotFoundException ignored) {
            AFLogger.afDebugLog("PreInstall file wasn't found: "+preInstallFile.getAbsolutePath());
        } catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(),t);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable t) {
                AFLogger.afErrorLog(t.getMessage(),t);
            }
        }
        return null;
    }

    private boolean isPreInstallFileInvalid(File preInstallFile) {
        return preInstallFile == null || !preInstallFile.exists();
    }

    private File getFileFromString(String filePath) {
        try {
            if (filePath != null && filePath.trim().length() > 0) {
                return new File(filePath.trim());
            }
        } catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(),t);
        }
        return null;
    }

    private String getPreInstallName(Context context) {

        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String result = getProperty(PRE_INSTALL_PREF);
        if (result != null) {
            return result;
        } else if (sharedPreferences.contains(PRE_INSTALL_PREF)) {
            result = sharedPreferences.getString(PRE_INSTALL_PREF,null);
        } else {
            boolean isFirstLaunch = isAppsFlyerFirstLaunch(context);
            if (isFirstLaunch){
                String valueFromFile = preInstallValueFromFile(context);
                if (valueFromFile != null) {
                    result = valueFromFile;
                } else {
                    result = getManifestMetaData(new WeakReference<>(context),"AF_PRE_INSTALL_NAME");
                }
            }
            if (result != null) {
                saveDataToSharedPreferences(context, PRE_INSTALL_PREF, result);
            }
        }
        if (result != null) {
            setProperty(PRE_INSTALL_PREF,result);
        }
        return result;
    }


    private void checkCache(Context context) {
        if (isDuringCheckCache || (System.currentTimeMillis() - lastCacheCheck) < 15000) {
            return;
        }
        if (cacheScheduler != null){
            return;
        }
        cacheScheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();

        Runnable job = new CachedRequestSender(context);

        //cacheScheduler.schedule(job,1,TimeUnit.SECONDS);
        scheduleJob(cacheScheduler, job, 1, TimeUnit.SECONDS);
    }

    @Nullable
    private String getConfiguredChannel(WeakReference<Context> context) {

        String channel = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.CHANNEL);
        if (channel == null){
            channel = getManifestMetaData(context,"CHANNEL");
        }
        return channel;
    }

    public boolean isPreInstalledApp(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            // FLAG_SYSTEM is only set to system applications,
            // this will work even if application is installed in external storage

            // Check if package is system app
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            AFLogger.afErrorLog("Could not check if app is pre installed", e);
        }
        return false;
    }

    private String getCachedChannel(Context context,String currentChannel) throws PackageManager.NameNotFoundException {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        if (sharedPreferences.contains(CACHED_CHANNEL_PREF)) {
            return sharedPreferences.getString(CACHED_CHANNEL_PREF,null);
        } else {

            saveDataToSharedPreferences(context, CACHED_CHANNEL_PREF, currentChannel);
            return currentChannel;
        }
    }

    private String getFirstInstallDate(SimpleDateFormat dateFormat,Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        String firstLaunchDate = sharedPreferences.getString(FIRST_INSTALL_PREF, null);
        if (firstLaunchDate == null) {
            if (isAppsFlyerFirstLaunch(context)){
                AFLogger.afDebugLog("AppsFlyer: first launch detected");
                firstLaunchDate = dateFormat.format(new Date());
            } else {
                firstLaunchDate = ""; // unknown
            }
            saveDataToSharedPreferences(context, FIRST_INSTALL_PREF, firstLaunchDate);
        }

        AFLogger.afInfoLog("AppsFlyer: first launch date: "+firstLaunchDate);

        return firstLaunchDate;
    }

    public String getAttributionId(ContentResolver contentResolver) {
        String [] projection = {ATTRIBUTION_ID_COLUMN_NAME};
        Cursor cursor  = contentResolver.query(Uri.parse(ATTRIBUTION_ID_CONTENT_URI), projection, null, null, null);
        String attributionId = null;
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            } else {
                attributionId = cursor.getString(cursor.getColumnIndex(ATTRIBUTION_ID_COLUMN_NAME));
            }
        } catch (Exception e){
            AFLogger.afErrorLog("Could not collect cursor attribution. ", e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e){
                AFLogger.afErrorLog(e.getMessage(),e);
            }
        }
        return attributionId;
    }

    final SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(AF_SHARED_PREF, Context.MODE_PRIVATE);
    }

    final int getLaunchCounter(SharedPreferences sharedPreferences, boolean isIncrease) {
        return getCounter(sharedPreferences, AF_COUNTER_PREF, isIncrease);
    }

    final int getNonlaunchCounter(SharedPreferences sharedPreferences, boolean isIncrease) {
        return getCounter(sharedPreferences, AF_EVENT_COUNTER_PREF, isIncrease);
    }

    private int getCounter(SharedPreferences sharedPreferences, String parameterName, boolean isIncrease) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(AF_SHARED_PREF, 0);

        int counter = sharedPreferences.getInt(parameterName, 0);

        if (isIncrease){
            counter++;
            @SuppressLint("CommitPrefEdits") android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(parameterName, counter);
            editorCommit(editor);
        }

        if(ProxyManager.getInstance().isProxyEnabledFromServer()){
            ProxyManager.getInstance().setLauncCounter(String.valueOf(counter));
        }

        return counter;
    }


    private long getTimePassedSinceLastLaunch(Context context, boolean shouldSave) {

        SharedPreferences sharedPreferences = getSharedPreferences(context);

        long lastLaunchTime = sharedPreferences.getLong(AF_TIME_PASSED_SINCE_LAST_LAUNCH, 0);

        long currentTime = System.currentTimeMillis();

        if (shouldSave) {
            saveLongToSharedPreferences(context, AF_TIME_PASSED_SINCE_LAST_LAUNCH, currentTime);
        }

        long timeInterval = -1L;
        if (lastLaunchTime > 0) {
            timeInterval = currentTime - lastLaunchTime;
        }
        else {
            return timeInterval;
        }

        return timeInterval / 1000; // for seconds

    }

    public String getAppsFlyerUID(Context context){
        ProxyManager.getInstance().addApiEvent("getAppsFlyerUID");
        return Installation.id(new WeakReference<>(context));
    }

    private void sendRequestToServer(String urlString,
                                     String postDataString,
                                     String afDevKey, WeakReference<Context> ctxReference,
                                     String cacheKey,
                                     boolean isLaunch) throws IOException {
        URL url = new URL(urlString);

        AFLogger.afInfoLog("url: "+url.toString());

        LogMessages.logMessageMaskKey(LogMessages.EVENT_DATA+postDataString);

        monitor(ctxReference.get(), LOG_TAG, MonitorMessages.EVENT_DATA, postDataString);
        try {
            callServer(url,postDataString,afDevKey,ctxReference,cacheKey,isLaunch);
        } catch (IOException e){
            AFLogger.afErrorLog("Exception in sendRequestToServer. ",e);
            boolean useHttpFallback = AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.USE_HTTP_FALLBACK,false);
            if (useHttpFallback){
                callServer(new URL(urlString.replace("https:","http:")),postDataString,afDevKey,ctxReference,cacheKey,isLaunch);
            } else {
                AFLogger.afInfoLog(LogMessages.SERVER_CALL_FAILRED+e.getLocalizedMessage());
                monitor(ctxReference.get(),LOG_TAG, MonitorMessages.ERROR, e.getLocalizedMessage());
                throw (e); // throw exception for handling the cache data from its caller.
            }
        }
    }

    private void callServer(URL url,
                            String postData,
                            String appsFlyerDevKey,
                            WeakReference<Context> ctxReference,
                            String cacheKey,
                            boolean isLaunch) throws IOException {
        Context context = ctxReference.get();
        boolean shouldRequestConversion = isLaunch && AppsFlyerLib.conversionDataListener != null;
        HttpURLConnection connection = null;
        try {
            ProxyManager.getInstance().addServerRequestEvent(url.toString(), postData);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            int contentLength = postData.getBytes().length;
            connection.setRequestProperty("Content-Length", contentLength + "");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setDoOutput(true);

            // write to server
            OutputStreamWriter out = null;
            try {
                out = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
                out.write(postData);
            } finally {
                if (out != null){
                    out.close();
                }
            }

            // read server response
            int statusCode = connection.getResponseCode();

            String response = readServerResponse(connection);
            ProxyManager.getInstance().addServerResponseEvent(url.toString(), statusCode, response); // monitoring launches and events requests
            AFLogger.afInfoLog(LogMessages.SERVER_RESPONSE_CODE + statusCode);
            monitor(context, LOG_TAG, MonitorMessages.SERVER_RESPONSE_CODE, Integer.toString(statusCode));
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            if (statusCode == HttpURLConnection.HTTP_OK) {

                // Post Successful-Launch Actions:

                if (ctxReference.get() != null && isLaunch) {
                    lastSuccessfulLaunchTimestamp = System.currentTimeMillis();
                }

                String uninstallTokenString = getProperty(AppsFlyerProperties.AF_UNINSTALL_TOKEN);
                if (uninstallTokenString != null) {
                    AFLogger.afDebugLog("Uninstall Token exists: " + uninstallTokenString);
                    // Assure the Register Request was sent successfully to server (GCM / Firebase)
                    boolean sentRegisterRequestSuccessfully = sharedPreferences.getBoolean(AppsFlyerProperties.AF_UNINSTALL_TOKEN_SENT, false);
                    if (!sentRegisterRequestSuccessfully) { // token exists, but was not sent to our servers yet
                        AFLogger.afDebugLog("Resending Uninstall token to AF servers: " + uninstallTokenString);
                        AFUninstallToken uninstallToken = new AFUninstallToken(uninstallTokenString);
                        UninstallUtils.updateServerUninstallToken(context, uninstallToken); // send the token to our servers
                    }
                    // Fetch GCM token (Firebase is fetched automatically via the InstanceID Service)
                } else if (getProperty(AppsFlyerProperties.GCM_PROJECT_NUMBER) != null) { // we have GCM project number, but don't have the token yet
                    AFLogger.afDebugLog("GCM Project number exists. Fetching token and sending to AF servers");
                    UninstallUtils.registerDeviceForUninstalls(new WeakReference<>(context)); // get the token and then send to our servers
                }

                if (latestDeepLink != null) {
                    latestDeepLink = null;
                }
                if (cacheKey != null){
                    CacheManager.getInstance().deleteRequest(cacheKey,context);
                }
                if (ctxReference.get() != null && cacheKey == null){
                    // we getString it again just to be sure the context still exist.
                    saveDataToSharedPreferences(context, SENT_SUCCESSFULLY_PREF, "true");
                    checkCache(context);
                }

                JSONObject responseJSON = ServerConfigHandler.handleResponse(response);

                this.isSendBackground = responseJSON.optBoolean(ServerConfigHandler.SEND_BACKGROUND_FLAG, false);
            }

            int retries = sharedPreferences.getInt(CONVERSION_REQUEST_RETRIES,0);

            long conversionDataCachedExpiration  = sharedPreferences.getLong(CONVERSION_DATA_CACHE_EXPIRATION, 0);
            if (conversionDataCachedExpiration != 0 && System.currentTimeMillis() - conversionDataCachedExpiration > SIXTY_DAYS) {
                saveDataToSharedPreferences(context, ATTRIBUTION_ID_PREF, null);
                saveLongToSharedPreferences(context, CONVERSION_DATA_CACHE_EXPIRATION, 0);
            }

            if (sharedPreferences.getString(ATTRIBUTION_ID_PREF,null) == null && appsFlyerDevKey != null && shouldRequestConversion && AppsFlyerLib.conversionDataListener != null && retries <= NUMBER_OF_CONVERSION_DATA_RETRIES){
                // Out of store
                ScheduledExecutorService scheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();

                Runnable job = new InstallAttributionIdFetcher(context.getApplicationContext(),appsFlyerDevKey,scheduler);

                //scheduler.schedule(job,10, TimeUnit.MILLISECONDS);// it used to be 5000 but as the server have the delay I canceled it
                scheduleJob(scheduler, job, 10, TimeUnit.MILLISECONDS);

//                scheduler.schedule(new InstallAttributionIdFetcher(context.getApplicationContext(),appsFlyerDevKey,scheduler),10, TimeUnit.MILLISECONDS);// it used to be 5000 but as the server have the delay I canceled it
            } else if (appsFlyerDevKey == null){
                AFLogger.afWarnLog("AppsFlyer dev key is missing.");
            } else if (shouldRequestConversion
                    && AppsFlyerLib.conversionDataListener != null
                    && sharedPreferences.getString(ATTRIBUTION_ID_PREF,null) != null
                    && getLaunchCounter(sharedPreferences, false) > 1) {

                Map<String, String> conversionData;
                try {
                    conversionData = getConversionData(context);
                    if (conversionData != null) {
                        try{
                            // for backward compatibility:
                            if(!conversionData.containsKey(IS_FIRST_LAUNCH)){
                                conversionData.put(IS_FIRST_LAUNCH, Boolean.toString(false));
                            }

                            AppsFlyerLib.conversionDataListener.onInstallConversionDataLoaded(conversionData);
                        } catch (Throwable e) {
                            AFLogger.afErrorLog(e.getLocalizedMessage(),e);
                        }
                    }
                } catch (AttributionIDNotReady ae) {
                    AFLogger.afErrorLog(ae.getMessage(),ae);
                }
            }
        } finally {
            if (connection != null){
                connection.disconnect();
            }
        }
    }

    public void validateAndTrackInAppPurchase(Context context, String publicKey, String signature, String purchaseData, String price, String currency, Map<String, String> additionalParameters) {
        ProxyManager.getInstance().addApiEvent("validateAndTrackInAppPurchase", publicKey, signature, purchaseData, price, currency, (additionalParameters == null ? "" : additionalParameters.toString()));

        if (!isTrackingStopped()) {
            AFLogger.afInfoLog("Validate in app called with parameters: " + purchaseData + " " + price + " " + currency);
        }
        if (publicKey == null || price == null || signature == null || currency == null || purchaseData == null)  {
            if (AppsFlyerLib.validatorListener != null) {
                AppsFlyerLib.validatorListener.onValidateInAppFailure("Please provide purchase parameters");
            }
        }
        else {
            ScheduledExecutorService scheduler = AFExecutor.getInstance().getScheduledThreadPoolExecutor();

            Runnable job = new AFValidateInAppPurchase(
                    context.getApplicationContext(),
                    getProperty(AppsFlyerProperties.AF_KEY),
                    publicKey,
                    signature,
                    purchaseData,
                    price,
                    currency,
                    additionalParameters,
                    scheduler, context instanceof Activity ? ((Activity) context).getIntent() : null);

            //scheduler.schedule(job, 10, TimeUnit.MILLISECONDS);
            scheduleJob(scheduler, job, 10, TimeUnit.MILLISECONDS);

            //scheduler.schedule(new AFValidateInAppPurchase(context.getApplicationContext(), getProperty(AppsFlyerProperties.AF_KEY), publicKey, signature, purchaseData, price, currency, additionalParameters, scheduler), 10, TimeUnit.MILLISECONDS);
        }
    }


    private void scheduleJob(ScheduledExecutorService scheduler, Runnable job, long delay, TimeUnit timeUnits){
        try {
            if(scheduler != null             // validation comes from https://appsflyer.atlassian.net/browse/RD-6336
                    && !scheduler.isShutdown()
                    && !scheduler.isTerminated())
            {
                scheduler.schedule(job, delay, timeUnits);
            }
            else{
                AFLogger.afWarnLog("scheduler is null, shut downed or terminated");
            }
        }catch(RejectedExecutionException exp){
            AFLogger.afErrorLog("scheduleJob failed with RejectedExecutionException Exception", exp);
        }catch(Throwable th){
            AFLogger.afErrorLog("scheduleJob failed with Exception", th);
        }
    }

    @Override
    public void onHandleReferrer(Map<String, String> referrer) {
       installReferrerMap = referrer;
    }

    public boolean isTrackingStopped() {
        return mIsTrackingStopped;
    }

    private class DataCollector implements Runnable {

        private final Intent activityIntent;
        private WeakReference<Context> context;
        private String appsFlyerKey;
        private String eventName;
        private String eventValue;
        private String referrer;
        private ExecutorService executor;
        private boolean isNewAPI;
        private boolean isBackgroundReferrerLaunch;

        private DataCollector(WeakReference<Context> context,
                              String appsFlyerKey,
                              String eventName,
                              String eventValue,
                              String referrer,
                              boolean useNewAPI,
                              ExecutorService executorService,
                              boolean isBackgroundReferrerLaunch, Intent activityIntent) {
            this.context = context;
            this.appsFlyerKey = appsFlyerKey;
            this.eventName = eventName;
            this.eventValue = eventValue;
            this.referrer = referrer;
            this.isNewAPI = useNewAPI;
            this.executor = executorService;
            this.isBackgroundReferrerLaunch = isBackgroundReferrerLaunch;
            this.activityIntent = activityIntent;
        }

        public void run() {
            sendTrackingWithEvent(context.get(), appsFlyerKey, eventName, eventValue,
                    referrer, isNewAPI, isBackgroundReferrerLaunch, activityIntent);
            //executor.shutdown(); ommitted, we manage shutdowns in @AFExecutor.shutdownExecutors
        }
    }

    private class SendToServerRunnable implements Runnable{

        private String urlString;
        private WeakReference<Context> ctxReference = null;
        Map<String,Object> params;
        boolean isLaunch;
        private int launchCounter;

        private SendToServerRunnable(String urlString,
                                     Map<String, Object> params,
                                     Context ctx,
                                     boolean isLaunch,
                                     int launchCounter) {
            this.urlString = urlString;
            this.params = params;
            this.ctxReference = new WeakReference<>(ctx);
            this.isLaunch = isLaunch;
            this.launchCounter = launchCounter;
        }

        public void run() {
            // Check if the SDK is opt-out
            if (isTrackingStopped()) {
                return;
            }

            String postDataString = null;
            //
            if (   isLaunch
                && launchCounter <= 2
                && isInstallReferrerAvailable())
            {
                params.put(INSTALL_REFERRER_PREF, installReferrerMap);
            }

            try {
                String afDevKey = (String) params.get(ServerParameters.AF_DEV_KEY);

                postDataString = AFHelper.convertToJsonObject(params).toString();

                sendRequestToServer(urlString,postDataString,afDevKey,ctxReference,null, isLaunch);

            } catch (IOException e){
                AFLogger.afErrorLog("Exception while sending request to server. ",e);
                if (postDataString != null && ctxReference != null && !urlString.contains(CACHED_URL_PARAMETER)){
                    CacheManager.getInstance().cacheRequest(new RequestCacheData(urlString, postDataString, BUILD_NUMBER), ctxReference.get());
                    AFLogger.afErrorLog(e.getMessage(),e);
                }
            } catch (Throwable t){
                AFLogger.afErrorLog(t.getMessage(),t);
            }
        }
    }

    private class InstallAttributionIdFetcher extends AttributionIdFetcher{

        public InstallAttributionIdFetcher(Context context, String appsFlyerDevKey, ScheduledExecutorService executorService) {
            super(context, appsFlyerDevKey, executorService);
        }

        @Override
        public String getUrl() {
            return ServerConfigHandler.getUrl(CONVERSION_DATA_URL);
        }
        @Override
        protected void attributionCallback(Map<String, String> conversionData) {
            conversionData.put(IS_FIRST_LAUNCH, Boolean.toString(true));
            AppsFlyerLib.conversionDataListener.onInstallConversionDataLoaded(conversionData);
            saveIntegerToSharedPreferences(this.ctxReference.get(), CONVERSION_REQUEST_RETRIES, 0);
        }
        @Override
        protected void attributionCallbackFailure(String error, int responseCode) {
            AppsFlyerLib.conversionDataListener.onInstallConversionFailure(error);

            if (responseCode >= 400 && responseCode < 500) {
                SharedPreferences sharedPreferences = getSharedPreferences(this.ctxReference.get());
                int retries = sharedPreferences.getInt(CONVERSION_REQUEST_RETRIES,0);
                saveIntegerToSharedPreferences(this.ctxReference.get(), CONVERSION_REQUEST_RETRIES, ++retries);

            }
        }
    }

    private abstract class AttributionIdFetcher implements Runnable {

        WeakReference<Context> ctxReference = null;
        private String appsFlyerDevKey;
        private ScheduledExecutorService executorService;

        protected abstract void attributionCallback(Map<String, String> conversionData);
        public abstract String getUrl();
        protected abstract void attributionCallbackFailure(String error, int responseCode);

        private AtomicInteger currentRequestsCounter = new AtomicInteger(0);

        AttributionIdFetcher(Context context, String appsFlyerDevKey, ScheduledExecutorService executorService) {
            this.ctxReference = new WeakReference<>(context);
            this.appsFlyerDevKey = appsFlyerDevKey;
            if (executorService == null) {
                this.executorService = AFExecutor.getInstance().getScheduledThreadPoolExecutor();
            } else {
                this.executorService = executorService;
            }
        }

        public void run() {
            if (appsFlyerDevKey == null || appsFlyerDevKey.length() == 0){
                return;
            }

            // Check if the SDK is opt-out
            if (isTrackingStopped()){
                return;
            }

            currentRequestsCounter.incrementAndGet();
            HttpURLConnection connection = null;
            try {
                Context context = ctxReference.get();
                if (context == null){
                    return;
                }

                long now = System.currentTimeMillis();
                String channel = getCachedChannel(context,getConfiguredChannel(new WeakReference<>(context)));
                String channelPostfix = "";
                if ((channel != null)){
                    if (!IGNORABLE_CHANNELS.contains(channel.toLowerCase())) {
                        channelPostfix = '-' + channel;
                    } else {
                        AFLogger.afWarnLog(String.format("AF detected using redundant Google-Play channel for attribution - %s. Using without channel postfix.", channel));
                    }
                }
                StringBuilder urlString = new StringBuilder()
                        .append(getUrl())
                        .append(context.getPackageName())
                        .append(channelPostfix)
                        .append("?devkey=").append(appsFlyerDevKey)
                        .append("&device_id=").append(Installation.id(new WeakReference<>(context)));

                ProxyManager.getInstance().addServerRequestEvent(urlString.toString(), "");
                LogMessages.logMessageMaskKey("Calling server for attribution url: "+urlString.toString());

                connection= (HttpURLConnection)new URL(urlString.toString()).openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setRequestProperty("Connection","close");
                connection.connect();

                int responseCode = connection.getResponseCode();
                String response = readServerResponse(connection);
                ProxyManager.getInstance().addServerResponseEvent(urlString.toString(), responseCode, response); // monitoring attributionId requests
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    long responseTime = System.currentTimeMillis();

                    saveLongToSharedPreferences(context, GET_CONVERSION_DATA_TIME, (responseTime - now) / 1000);


                    LogMessages.logMessageMaskKey("Attribution data: " + response);

                    if (response.length() > 0 && context != null){
                        Map<String,String> conversionDataMap = attributionStringToMap(response);
                        String isCache = conversionDataMap.get("iscache");


                        if (isCache != null && Boolean.toString(false).equals(isCache)) {
                            // save expiration date.
                            saveLongToSharedPreferences(context, CONVERSION_DATA_CACHE_EXPIRATION, System.currentTimeMillis());
                        }
                        if (conversionDataMap.containsKey(Constants.URL_SITE_ID)) {
                            if (conversionDataMap.containsKey(Constants.URL_CHANNEL)) {
                                // Detected App-Invite from another app user
                                AFLogger.afDebugLog(Constants.LOG_INVITE_DETECTED_APP_INVITE_VIA_CHANNEL + conversionDataMap.get(Constants.URL_CHANNEL));
                            } else {
                                // App was installed from another app's (same developer) Cross Promotion campaign
                                AFLogger.afDebugLog(String.format(
                                        Constants.LOG_CROSS_PROMOTION_APP_INSTALLED_FROM_CROSS_PROMOTION,
                                        conversionDataMap.get(Constants.URL_SITE_ID)));
                            }
                        }
                        if (conversionDataMap.containsKey(Constants.URL_SITE_ID)) {
                            AFLogger.afDebugLog(Constants.LOG_INVITE_DETECTED_APP_INVITE_VIA_CHANNEL + conversionDataMap.get(Constants.URL_CHANNEL));
                        }

                        // for future conversionData usage, set  IS_FIRST_LAUNCH=false
                        conversionDataMap.put(IS_FIRST_LAUNCH, Boolean.toString(false));

                        String conversionJsonString = new JSONObject(conversionDataMap).toString();
                        if (conversionJsonString != null) {
                            saveDataToSharedPreferences(context, ATTRIBUTION_ID_PREF, conversionJsonString);
                        }
                        else {
                            saveDataToSharedPreferences(context, ATTRIBUTION_ID_PREF, response);
                        }

                        AFLogger.afDebugLog("iscache=" + isCache + " caching conversion data");

                        if (AppsFlyerLib.conversionDataListener != null){
                            if (currentRequestsCounter.intValue() <= 1){ // if we had 2 requests from onReceive and from onCreate we wait for the last one which should be he none organic
                                Map<String,String> conversionData;
                                try {
                                    conversionData = getConversionData(context);
                                } catch (AttributionIDNotReady ae){
                                    AFLogger.afErrorLog("Exception while trying to fetch attribution data. ",ae);
                                    conversionData = conversionDataMap;
                                }
                                attributionCallback(conversionData);
                            }
                        }
                    }

                } else {
                    if (AppsFlyerLib.conversionDataListener != null){
                        attributionCallbackFailure("Error connection to server: " + responseCode, responseCode);
                    }
                    LogMessages.logMessageMaskKey("AttributionIdFetcher response code: "+responseCode+"  url: "+urlString);

                }
            } catch (Throwable t){
                if (AppsFlyerLib.conversionDataListener != null){
                    attributionCallbackFailure(t.getMessage(), 0);
                }
                AFLogger.afErrorLog(t.getMessage(),t);
            } finally {
                currentRequestsCounter.decrementAndGet();
                if (connection != null){
                    connection.disconnect();
                }
            }
            executorService.shutdown();
        }
    }

    @NonNull
    String readServerResponse(HttpURLConnection connection) {
        // read the output from the server
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        try {
            InputStream responseStream = connection.getErrorStream();
            if (responseStream == null)  {
                responseStream = connection.getInputStream();
            }

            inputStreamReader = new InputStreamReader(responseStream);
            reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Throwable t) {
            AFLogger.afErrorLog("Could not read connection response from: "+connection.getURL().toString(),t);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (Throwable ignore) {
            }
        }
        String result = stringBuilder.toString();
        try {
            new JSONObject(result);
            return result;
        } catch (JSONException e) {
            JSONObject json = new JSONObject();
            try {
                json.put(RESPONSE_NOT_JSON,result);
                return json.toString();
            } catch (JSONException e1) {
                return new JSONObject().toString();
            }
        }
    }

    private class CachedRequestSender implements Runnable {

        private WeakReference<Context> ctxReference = null;

        public CachedRequestSender(Context context) {
            ctxReference = new WeakReference<Context>(context);
        }

        public void run() {
            if (isDuringCheckCache){
                return;
            }
            lastCacheCheck = System.currentTimeMillis();
            if (ctxReference == null){
                return;
            }
            isDuringCheckCache = true;
            try {
                String afDevKey = getProperty(AppsFlyerProperties.AF_KEY);
                synchronized (ctxReference){
                    for (RequestCacheData requestCacheData : CacheManager.getInstance().getCachedRequests(ctxReference.get())){

                        AFLogger.afInfoLog("resending request: "+requestCacheData.getRequestURL());

                        try {
                            // convert cache key name (file name) to miliseconds

                            long currentTime = System.currentTimeMillis();
                            String cachedTimeString = requestCacheData.getCacheKey();
                            long cachedTime =  Long.parseLong(cachedTimeString, 10);

                            sendRequestToServer(requestCacheData.getRequestURL() + CACHED_URL_PARAMETER + Long.toString((currentTime - cachedTime) / 1000),
                                    requestCacheData.getPostData(),
                                    afDevKey,
                                    ctxReference,
                                    requestCacheData.getCacheKey(),
                                    false);

                        } catch (Exception e){
                            AFLogger.afErrorLog("Failed to resend cached request",e);
                        }
                    }
                }
            } catch (Exception e){
                AFLogger.afErrorLog("failed to check cache. ",e);
            } finally {
                isDuringCheckCache = false;
            }
            cacheScheduler.shutdown();
            cacheScheduler = null;
        }
    }

    float getBatteryLevel(Context context) {

        float result = 1;
        try {
            Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            // Error checking that probably isn't needed but I added just in case.
            if (level == -1 || scale == -1) {
                return 50.0f;
            }

            result = ((float)level / (float)scale) * 100.0f;
        }
        catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(),t);
        }

        return result;

    }

    public void setLogLevel(AFLogger.LogLevel logLevel) {
        AppsFlyerProperties.getInstance().setLogLevel(logLevel);
    }

    public void setHostName(String hostname) {
        setProperty(AF_HOST, hostname);
    }

    public String getHost() {

        String currentHost = getProperty(AF_HOST);

        if(currentHost != null){
           return currentHost;
        }
        return ServerParameters.DEFAULT_HOST;
    }

    public void setMinTimeBetweenSessions(int seconds) {
        launchBlockingTimeWindow = TimeUnit.SECONDS.toMillis(seconds);
    }

}
