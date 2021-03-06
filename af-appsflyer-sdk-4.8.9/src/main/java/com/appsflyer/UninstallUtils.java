package com.appsflyer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import static com.appsflyer.AppsFlyerLib.AF_SHARED_PREF;

/**
 * Created by shacharaharon on 29/01/2017.
 */

class UninstallUtils {

    private static final String GCM_INSTANCE_ID_INTENT_FILTER_ACTION = "com.google.android.gms.iid.InstanceID";
    private static final String FIREBASE_INSTANCE_ID_INTENT_FILTER_ACTION = "com.google.firebase.INSTANCE_ID_EVENT";
    private static final String PERMISSION_C2_D_MESSAGE = ".permission.C2D_MESSAGE";
    private static final String GCM_RECEIVER = "com.google.android.gms.gcm.GcmReceiver";
    private static final String GCM_RECEIVER_INTENT_FILTER_ACTION = "com.google.android.c2dm.intent.RECEIVE";

    static boolean didConfigureTokenRefreshService(Context context) {
        boolean result = didConfigureGcmTokenRefreshService(context);
        result |= didConfigureFirebaseTokenRefreshService(context);
        return result;
    }

    private static boolean didConfigureGcmTokenRefreshService(Context context) {
        // Check if the SDK is opt-out
        if(AppsFlyerLib.getInstance().isTrackingStopped()){
            return false;
        }

        try {
            Class.forName("com.google.android.gms.iid.InstanceIDListenerService");
            Intent afGcmInstanceIdServiceIntent = new Intent(GCM_INSTANCE_ID_INTENT_FILTER_ACTION, null, context, GcmInstanceIdListener.class);
            Intent gcmInstanceIdServiceIntent = new Intent(GCM_INSTANCE_ID_INTENT_FILTER_ACTION, null, context, com.google.android.gms.iid.InstanceIDListenerService.class);
            if (AndroidUtils.isServiceAvailable(context, afGcmInstanceIdServiceIntent)
                    || AndroidUtils.isServiceAvailable(context, gcmInstanceIdServiceIntent)) {
                Intent gcmReceiverIntent = new Intent(GCM_RECEIVER_INTENT_FILTER_ACTION, null, context, Class.forName(GCM_RECEIVER));
                if (AndroidUtils.isReceiverAvailable(context, gcmReceiverIntent)) {
                    String packageName = context.getPackageName();
                    if (AndroidUtils.isPermissionAvailable(context, packageName + PERMISSION_C2_D_MESSAGE)) {
                        return true;
                    } else {
                        AFLogger.afWarnLog(LogMessages.GCM_PERMISSION_MISSING_WARNING);
                    }
                } else {
                    AFLogger.afWarnLog(LogMessages.GCM_RECEIVER_MISSING_WARNING);
                }
            }
        } catch (ClassNotFoundException ignore) {
            AFLogger.afRDLog(ignore.getMessage());
        } catch (Throwable e) {
            AFLogger.afErrorLog("An error occurred while trying to verify manifest declarations: ", e);
        }
        return false;
    }

    private static boolean didConfigureFirebaseTokenRefreshService(Context context) {
        // Check if the SDK is opt-out
        if(AppsFlyerLib.getInstance().isTrackingStopped()){
            return false;
        }

        try {
            Class.forName("com.google.firebase.iid.FirebaseInstanceIdService");
            Intent afFirebaseInstanceIdServiceIntent = new Intent(FIREBASE_INSTANCE_ID_INTENT_FILTER_ACTION, null, context, FirebaseInstanceIdListener.class);
            Intent firebaseInstanceIdServiceIntent = new Intent(FIREBASE_INSTANCE_ID_INTENT_FILTER_ACTION, null, context, com.google.firebase.iid.FirebaseInstanceIdService.class);
            if (AndroidUtils.isServiceAvailable(context, afFirebaseInstanceIdServiceIntent)
                    || (AndroidUtils.isServiceAvailable(context, firebaseInstanceIdServiceIntent))) {
                return true;
            } else {
                AFLogger.afWarnLog(LogMessages.UNINSTALL_INSTANCE_ID_MISSING_WARNING);
            }
        } catch (ClassNotFoundException ignore) {
        } catch (Throwable e) {
            AFLogger.afErrorLog("An error occurred while trying to verify manifest declarations: ", e);
        }
        return false;
    }

    private static String getGCMToken(WeakReference<Context> context, String gcmProjectNumber) {
        try {
            Class<?> gcmInstanceId = Class.forName(GCM_INSTANCE_ID_INTENT_FILTER_ACTION);
            Class.forName("com.google.android.gms.gcm.GcmReceiver"); // for Unity
            Method getInstance = gcmInstanceId.getDeclaredMethod("getInstance", Context.class);
            Object instance = getInstance.invoke(gcmInstanceId, context.get());
            Method getToken = gcmInstanceId.getDeclaredMethod("getToken", String.class, String.class);
            String token = (String) getToken.invoke(instance, gcmProjectNumber, "GCM");
            if (token == null) {
                AFLogger.afWarnLog("Couldn't get token using reflection.");
                return null;
            }
            return token;
        } catch (ClassNotFoundException e) {
            // InstanceID doesn't exists, continue as usual
        } catch (Throwable t) {
            AFLogger.afErrorLog("Couldn't get token using GoogleCloudMessaging. ", t);
        }
        return null;
    }

    static void registerDeviceForUninstalls(final WeakReference<Context> weakContext) {

        new FetchGcmAsyncTask(weakContext).execute();
    }

    static void updateServerUninstallToken(Context context, AFUninstallToken token) {
        AFLogger.afInfoLog("updateServerUninstallToken called with: " + token.toString());

        String prevTokenStr = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.AF_UNINSTALL_TOKEN);

        AFUninstallToken prevToken = AFUninstallToken.parse(prevTokenStr);

        // skip calling end-point with the same token if we already sent it
        SharedPreferences sharedPreferences = context.getSharedPreferences(AF_SHARED_PREF, Context.MODE_PRIVATE);
        boolean uninstallTokenSent = sharedPreferences.getBoolean(AppsFlyerProperties.AF_UNINSTALL_TOKEN_SENT, false);

        if(uninstallTokenSent
                && prevToken!= null
                && prevToken.getToken() != null
                && prevToken.getToken().equals(token.getToken())){
            return;
        }

        AppsFlyerProperties.getInstance().set(AppsFlyerProperties.AF_UNINSTALL_TOKEN, token.toString());

        AppsFlyerLib.getInstance().callRegisterBackground(context, token.getToken());
    }

    private static class FetchGcmAsyncTask extends AsyncTask<Void, Void, String> {
        private final WeakReference<Context> weakContext;
        private String gcmProjectNumber;

        FetchGcmAsyncTask(WeakReference<Context> weakContext) {
            this.weakContext = weakContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            gcmProjectNumber = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.GCM_PROJECT_NUMBER);
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                String uninstallToken = null;
                if (gcmProjectNumber != null) {
                    uninstallToken = getGCMToken(weakContext, gcmProjectNumber);
                }
                return uninstallToken;
            } catch (Throwable t) {
                AFLogger.afErrorLog("Error registering for uninstall feature", t);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String UninstallTokenString) {
            if (!TextUtils.isEmpty(UninstallTokenString)) {
                // get saved Uninstall Token
                String tokenObjectString = AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.AF_UNINSTALL_TOKEN);
                AFUninstallToken uninstallToken = new AFUninstallToken(UninstallTokenString);
                if (tokenObjectString == null) { // first time!
                    updateServerUninstallToken(weakContext.get(), uninstallToken);
                } else {
                    AFUninstallToken existingAFUninstallToken = AFUninstallToken.parse(tokenObjectString);
                    // compare existing Uninstall Token to the new one
                    if (existingAFUninstallToken != null) {
                        boolean shouldUpdateServerWithNewToken = existingAFUninstallToken.testAndUpdate(uninstallToken);
                        if (shouldUpdateServerWithNewToken) {
                            updateServerUninstallToken(weakContext.get(), existingAFUninstallToken);
                        }
                    }
                }
            }
        }
    }
}
