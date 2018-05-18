package com.appsflyer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by shacharaharon on 01/02/2017.
 */
class AFValidateInAppPurchase implements Runnable {

    private WeakReference<Context> ctxReference = null;
    private String appsFlyerDevKey, googlePublicKey, signature, purchaseData, price, currency;
    private Map<String, String> additionalParams;
    private ScheduledExecutorService executorService;
    static final String VALIDATE_URL = "https://sdk-services.%s/validate-android-signature";
    static final String VALIDATE_WH_URL = "https://validate.%s/api/v" + AppsFlyerLib.SERVER_BUILD_NUMBER
            + "/androidevent?buildnumber=" + AppsFlyerLib.BUILD_NUMBER + "&app_id=";
    private final Intent activityIntent;

    AFValidateInAppPurchase(Context context,
                            String appsFlyerDevKey,
                            String aPublicKey,
                            String aSignature,
                            String aPurchaseData,
                            String aPrice,
                            String aCurrency,
                            Map<String, String> aAdditionalParams,
                            ScheduledExecutorService executorService, Intent activityIntent) {
        this.ctxReference = new WeakReference<>(context);
        this.appsFlyerDevKey = appsFlyerDevKey;
        this.googlePublicKey = aPublicKey;
        this.purchaseData = aPurchaseData;
        this.price = aPrice;
        this.currency = aCurrency;
        this.additionalParams = aAdditionalParams;
        this.signature = aSignature;
        this.executorService = executorService;
        this.activityIntent = activityIntent;
    }

    @Override
    public void run() {
        if (appsFlyerDevKey == null || appsFlyerDevKey.length() == 0) {
            return;
        }

        // Check if the SDK is opt-out
        if (AppsFlyerLib.getInstance().isTrackingStopped()) {
            return;
        }

        HttpURLConnection legacyValidateConnection = null;
        try {
            final Context context = ctxReference.get();
            if (context == null) {
                return;
            }
            Map<String,Object> validateParams = new HashMap<>();
            validateParams.put("public-key", this.googlePublicKey);
            validateParams.put("sig-data", this.purchaseData);
            validateParams.put("signature", this.signature);

            final Map<String,Object> validateParamsForWH = new HashMap<>();
            validateParamsForWH.putAll(validateParams);

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    validateWHPurchaseEvent(validateParamsForWH, additionalParams, ctxReference);
                }
            }, 5, TimeUnit.MILLISECONDS);

            validateParams.put("dev_key", this.appsFlyerDevKey);
            validateParams.put("app_id", context.getPackageName());
            validateParams.put("uid", AppsFlyerLib.getInstance().getAppsFlyerUID(context));
            validateParams.put(ServerParameters.ADVERTISING_ID_PARAM, AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM));
            JSONObject validateParamsJSON = new JSONObject(validateParams);
            String postData = validateParamsJSON.toString();
            String url = ServerConfigHandler.getUrl(VALIDATE_URL);
            ProxyManager.getInstance().addServerRequestEvent(url, postData);

            legacyValidateConnection = sendDataToServer(postData, url);

            int responseCode = -1;
            if (legacyValidateConnection != null) {
                responseCode = legacyValidateConnection.getResponseCode();
            }
            String str = AppsFlyerLib.getInstance().readServerResponse(legacyValidateConnection);
            ProxyManager.getInstance().addServerResponseEvent(url, responseCode, str); // monitoring validateInApp requests

            JSONObject responseJsonObject = new JSONObject(str);
            responseJsonObject.put("code",responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                AFLogger.afInfoLog("Validate response 200 ok: " + responseJsonObject.toString());

                boolean validated = false;
                if (responseJsonObject.optBoolean("result")) {
                    validated = responseJsonObject.getBoolean("result");
                }
                validateCallback(validated, this.purchaseData, this.price, this.currency, this.additionalParams, responseJsonObject.toString());

            } else {
                AFLogger.afInfoLog("Failed Validate request");
                validateCallback(false, this.purchaseData, this.price, this.currency, this.additionalParams, responseJsonObject.toString());
            }
        } catch (Throwable t) {
            if (AppsFlyerLib.validatorListener != null) {
                AFLogger.afErrorLog("Failed Validate request + ex",t);
                validateCallback(false, this.purchaseData, this.price, this.currency, this.additionalParams, t.getMessage());
            }

            AFLogger.afErrorLog(t.getMessage(), t);
        } finally {
            if (legacyValidateConnection != null) {
                legacyValidateConnection.disconnect();
            }
        }
//        executorService.shutdown(); ommitted, we manage shutdowns in @AFExecutor.shutdownExecutors
    }

    private void validateWHPurchaseEvent(Map<String, Object> validateParams, Map<String,String> extraParams ,WeakReference<Context> context) {

        if (context.get() == null) {
            return;
        }

        String validateWhUrlString = ServerConfigHandler.getUrl(VALIDATE_WH_URL) + context.get().getPackageName();

        SharedPreferences sharedPreferences = context.get().getSharedPreferences(AppsFlyerLib.AF_SHARED_PREF, 0);
        String referrer = getReferrer(sharedPreferences);

        // new validate request is sent to WebHandler, which will trigger the trackEvent with purchase event.
        Map<String,Object> validateWHParams = AppsFlyerLib.getInstance().getEventParameters(
                context.get(),appsFlyerDevKey,AFInAppEventType.PURCHASE,
                "", referrer, true, sharedPreferences, false, activityIntent);

        addValidateParameters(validateWHParams);

        JSONObject validateWHParamsJSON = new JSONObject(validateWHParams);
        JSONObject validateParamsAsJSON = new JSONObject();

        try {
            for (Map.Entry<String, Object> entry : validateParams.entrySet()){
                validateParamsAsJSON.put(entry.getKey(), entry.getValue());
            }

            validateWHParamsJSON.put("receipt_data", validateParamsAsJSON);
        } catch (JSONException err) {
            AFLogger.afErrorLog("Failed to build 'receipt_data'", err);
        }

        if (extraParams != null){
            JSONObject extraParamsAsJSON = new JSONObject();
            try {
                for (Map.Entry<String, String> entry : extraParams.entrySet()) {
                    extraParamsAsJSON.put(entry.getKey(), entry.getValue());
                }

                validateWHParamsJSON.put("extra_prms", extraParamsAsJSON);
            } catch (JSONException err) {
                AFLogger.afErrorLog("Failed to build 'extra_prms'", err);
            }
        }

        String postData = validateWHParamsJSON.toString();
        ProxyManager.getInstance().addServerRequestEvent(validateWhUrlString, postData);

        HttpURLConnection validatePurchaseConnection = null;
        try {
            validatePurchaseConnection = sendDataToServer(postData, validateWhUrlString);

            int responseCode = -1;
            if (validatePurchaseConnection != null) {
                responseCode = validatePurchaseConnection.getResponseCode();
            }
            String responseString = AppsFlyerLib.getInstance().readServerResponse(validatePurchaseConnection);
            ProxyManager.getInstance().addServerResponseEvent(validateWhUrlString, responseCode, responseString);

            JSONObject responseJsonObject = new JSONObject(responseString);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                AFLogger.afInfoLog("Validate-WH response - 200: " + responseJsonObject.toString());
            } else {
                AFLogger.afWarnLog("Validate-WH response failed - " + responseCode + ": " + responseJsonObject.toString());
            }
        } catch (Throwable t) {
            AFLogger.afErrorLog(t.getMessage(), t);
        } finally {
            if (validatePurchaseConnection != null) {
                validatePurchaseConnection.disconnect();
            }
        }
    }

    private void addValidateParameters(Map<String, Object> validateWHParams) {
        validateWHParams.put("price",price);
        validateWHParams.put("currency",currency);
//        validateWHParams.put("product_identifier",productIdentifier);
    }

    private String getReferrer(SharedPreferences sharedPreferences) {
        String referrer = sharedPreferences.getString(AppsFlyerLib.REFERRER_PREF,null);
        referrer = (referrer == null ? "" : referrer);
        return referrer;
    }

    private HttpURLConnection sendDataToServer(String dataString, String url) throws IOException {
        try {
            BackgroundHttpTask validateRequest = new BackgroundHttpTask(null, AppsFlyerLib.getInstance().isTrackingStopped());
            validateRequest.bodyAsString = dataString;
            validateRequest.setShouldReadResponse(false);
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                AFLogger.afDebugLog("Main thread detected. Calling " + url + " in a new thread.");
                validateRequest.execute(url);
            } else {
                AFLogger.afDebugLog("Calling " + url + " (on current thread: " + Thread.currentThread().toString() + " )");

                validateRequest.onPreExecute();
                validateRequest.onPostExecute(validateRequest.doInBackground(url));
            }
            return validateRequest.getConnection();
        } catch (Throwable t) {
            AFLogger.afErrorLog("Could not send callStats request",t);
            return null;
        }
    }

    private void validateCallback(boolean validated, String purchaseData, String price, String currency, Map<String, String> additionalParams, String result) {
        if (AppsFlyerLib.validatorListener != null) {
            AFLogger.afDebugLog("Validate callback parameters: " + purchaseData + " " + price + " " + currency);
            if (validated) {
                AFLogger.afDebugLog("Validate in app purchase success: " + result);
                AppsFlyerLib.validatorListener.onValidateInApp();
            } else {
                AFLogger.afDebugLog("Validate in app purchase failed: " + result);
                AppsFlyerLib.validatorListener.onValidateInAppFailure(result == null ? "Failed validating" : result);
            }
        }
    }
}
