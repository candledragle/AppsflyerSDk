package com.appsflyer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;

final class AFNetworkManager {

    static final String NETWORK_WIFI = "WIFI";
    static final String NETWORK_MOBILE = "MOBILE";
    static final String NETWORK_UNKNOWN = "unknown";
    static final String CARRIER_CDMA = "CDMA";

    public static AFNetworkManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @return If network is valid and connected or connecting.
     */
    private static boolean isActive(NetworkInfo networkInfo) {
        return null != networkInfo && networkInfo.isConnectedOrConnecting();
    }

    @RequiresApi(VERSION_CODES.LOLLIPOP)
    private static String getNetworkTypeL(@NonNull ConnectivityManager cm) {
        Network[] allNetworks = cm.getAllNetworks();
        for (Network network : allNetworks) {
            NetworkInfo networkInfo = cm.getNetworkInfo(network);
            if (isActive(networkInfo)) {
                if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
                    return NETWORK_WIFI;
                }
                if (ConnectivityManager.TYPE_MOBILE == networkInfo.getType()) {
                    return NETWORK_MOBILE;
                }
                return NETWORK_UNKNOWN;
            }
        }
        return NETWORK_UNKNOWN;
    }

    private static String getNetworkTypePreL(@NonNull ConnectivityManager cm) {
        if (isActive(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI))) {
            return NETWORK_WIFI;
        }
        if (isActive(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE))) {
            return NETWORK_MOBILE;
        }
        NetworkInfo activeInfo = cm.getActiveNetworkInfo();
        if (isActive(activeInfo)) {
            if (ConnectivityManager.TYPE_WIFI == activeInfo.getType()) {
                return NETWORK_WIFI;
            } else if (ConnectivityManager.TYPE_MOBILE == activeInfo.getType()) {
                return NETWORK_MOBILE;
            }
        }
        return NETWORK_UNKNOWN;
    }

    private static String getNetworkType(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm) {
            return NETWORK_UNKNOWN;
        }
        return VERSION_CODES.LOLLIPOP <= VERSION.SDK_INT ?
                getNetworkTypeL(cm) : getNetworkTypePreL(cm);
    }

    /**
     * @param context Context to access system service.
     * @return Object that contains network data. Network type is one of the {@link #NETWORK_MOBILE},
     * {@link #NETWORK_WIFI}, {@link #NETWORK_UNKNOWN}. If network is not wifi, sim operator name
     * and network operator name (carrier) will be fetched.
     * If network type is {@link TelephonyManager#PHONE_TYPE_CDMA} carrier name will be {@link #CARRIER_CDMA}.
     * Carrier name and sim operator name may be null.
     */
    NetworkData getData(@NonNull Context context) {
        String networkType = NETWORK_UNKNOWN;
        String simOperatorName = null;
        String networkOperatorName = null;
        try {
            networkType = getNetworkType(context);

            //if (!NETWORK_WIFI.equals(networkType)) { RD-11070
                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                simOperatorName = manager.getSimOperatorName();
                networkOperatorName = manager.getNetworkOperatorName();
                if (null == networkOperatorName || networkOperatorName.isEmpty()) {
                    if (TelephonyManager.PHONE_TYPE_CDMA == manager.getPhoneType()) {
                        networkOperatorName = CARRIER_CDMA;
                    }
                }
            //}
        } catch (Throwable e) {
            AFLogger.afErrorLog("Exception while collecting network info. ", e);
        }
        return new NetworkData(networkType, networkOperatorName, simOperatorName);
    }

    static final class NetworkData {
        private final String mNetworkType;
        private final String mCarrierName;
        private final String mSimOperator;

        NetworkData(@NonNull String networkType, @Nullable String carrierName, @Nullable String simOperator) {
            mNetworkType = networkType;
            mCarrierName = carrierName;
            mSimOperator = simOperator;
        }

        String getNetworkType() {
            return mNetworkType;
        }

        @Nullable
        String getCarrierName() {
            return mCarrierName;
        }

        @Nullable
        String getSimOperator() {
            return mSimOperator;
        }
    }

    private static final class InstanceHolder {
        static final AFNetworkManager INSTANCE = new AFNetworkManager();
    }
}
