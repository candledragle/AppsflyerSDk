package com.appsflyer;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Map;

/**
 * Created by shacharaharon on 29/01/2017.
 */

//TODO: refactor - use reflection ( > Android Marshmallow: prints exeption)
class AdvertisingIdUtil {

    public static final String AMAZON_MANUFACTURER = "Amazon";
    static final String AMAZON_SETTING_LIMIT_AD_TRACKING = "limit_ad_tracking";
    static final String AMAZON_SETTING_ADVERTISING_ID = "advertising_id";

    static AdvertisingIdObject getAmazonAID(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return null;
        }
        String advertisingIdObjectString = AppsFlyerProperties.getInstance().getString(ServerParameters.AMAZON_AID);
        AdvertisingIdObject advertisingIdObject = null;
        if (advertisingIdObjectString == null) {
            // Check whether this is an Amazon device
            if (AMAZON_MANUFACTURER.equals(android.os.Build.MANUFACTURER)) {
                // Check whether the user has disabled tracking
                int limitAdTrackingInt = Settings.Secure.getInt(contentResolver, AMAZON_SETTING_LIMIT_AD_TRACKING, 2);
                if (limitAdTrackingInt == 0) {
                    // Interest-based tracking is allowed, retrieve the Advertising ID
                    String advertisingID = Settings.Secure.getString(contentResolver, AMAZON_SETTING_ADVERTISING_ID);
                    advertisingIdObject = new AdvertisingIdObject(AdvertisingIdObject.IdType.AMAZON, advertisingID, false);
                } else if (limitAdTrackingInt == 2) {
                    // This version of Fire OS does not have this setting; if an ID is needed, use an alternate source
                    advertisingIdObject = null;
                } else {
                    String advertisingID = "";
                    try {
                        advertisingID = Settings.Secure.getString(contentResolver, AMAZON_SETTING_ADVERTISING_ID);
                    } catch (Throwable t) {
                        AFLogger.afErrorLog("Couldn't fetch Amazon Advertising ID (Ad-Tracking is limited!)", t);
                    }
                    advertisingIdObject = new AdvertisingIdObject(AdvertisingIdObject.IdType.AMAZON, advertisingID, true);
                }
            }

        }
        return advertisingIdObject;
    }

    static void addGoogleAID(Context context, Map<String, Object> params) {
        AFLogger.afInfoLog("Trying to fetch GAID..");
        com.google.android.gms.ads.identifier.AdvertisingIdClient.Info gpsAdInfo;
        com.appsflyer.AdvertisingIdClient.AdInfo internalAdInfo;
        String advertisingId = null;
        String advertisingIdEnabled = null;
        boolean advertisingIdWithGps = false;
        String gaidError = null;
        int statusCode = -1;
        try {
            Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
            gpsAdInfo = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(context);

            if (gpsAdInfo != null) {
                advertisingId = gpsAdInfo.getId();
                advertisingIdEnabled = Boolean.toString(!gpsAdInfo.isLimitAdTrackingEnabled());
                advertisingIdWithGps = true;
                if (advertisingId == null || advertisingId.length() == 0) {
                    gaidError = "emptyOrNull";
                }
            } else {
                gaidError = "gpsAdInfo-null";
            }
        } catch (Throwable t1) {
            AFLogger.afErrorLog(t1.getMessage(), t1);
            try {
                statusCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            } catch (Throwable t) {
                AFLogger.afErrorLog(t.getMessage(), t);
            }
            gaidError = t1.getClass().getSimpleName();
            AFLogger.afInfoLog(LogMessages.WARNING_PREFIX + "Google Play Services is missing.");
            if (AppsFlyerProperties.getInstance().getBoolean(AppsFlyerProperties.ENABLE_GPS_FALLBACK, true)) {
                try {
                    internalAdInfo = com.appsflyer.AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (internalAdInfo != null) {
                        advertisingId = internalAdInfo.getId();
                        advertisingIdEnabled = Boolean.toString(!internalAdInfo.isLimitAdTrackingEnabled());
                        if (advertisingId == null || advertisingId.length() == 0) {
                            gaidError = "emptyOrNull (bypass)";
                        }
                    } else {
                        gaidError = "gpsAdInfo-null (bypass)";
                    }
                } catch (Throwable t2) {
                    AFLogger.afErrorLog(t2.getMessage(), t2);
                    gaidError += "/" + t2.getClass().getSimpleName();
                    advertisingId = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM);
                    advertisingIdEnabled = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_ENABLED_PARAM);

                    if (t2.getLocalizedMessage() != null) {
                        AFLogger.afInfoLog(t2.getLocalizedMessage());
                    } else {
                        AFLogger.afInfoLog(t2.toString());
                    }
                }
            }
        }

        if (context.getClass().getName().equals("android.app.ReceiverRestrictedContext")) {
            advertisingId = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_PARAM);
            advertisingIdEnabled = AppsFlyerProperties.getInstance().getString(ServerParameters.ADVERTISING_ID_ENABLED_PARAM);
            gaidError = "context = android.app.ReceiverRestrictedContext";
        }

        if (gaidError != null) {
            params.put("gaidError", statusCode + ": " + gaidError);
        }

        if (advertisingId != null && advertisingIdEnabled != null) {
            params.put(ServerParameters.ADVERTISING_ID_PARAM, advertisingId);
            params.put(ServerParameters.ADVERTISING_ID_ENABLED_PARAM, advertisingIdEnabled);
            AppsFlyerProperties.getInstance().set(ServerParameters.ADVERTISING_ID_PARAM, advertisingId);
            AppsFlyerProperties.getInstance().set(ServerParameters.ADVERTISING_ID_ENABLED_PARAM, advertisingIdEnabled);
            params.put(ServerParameters.ADVERTISING_ID_WITH_GPS, String.valueOf(advertisingIdWithGps));
        }
    }
}
