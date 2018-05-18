package com.appsflyer;


import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

import java.util.HashMap;
import java.util.Map;

import static com.android.installreferrer.api.InstallReferrerClient.newBuilder;


interface AFInstallReferrerListener {
    void onHandleReferrer(Map<String, String> referrer);
}

class AFInstallReferrer implements InstallReferrerStateListener {

    final static String INSTALL_REFERRER_CLIENT_CLASS = "com.android.installreferrer.api.InstallReferrerClient";
    final static String INSTALL_REFERRER_PERMISSION = "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE";

    private InstallReferrerClient mReferrerClient;
    private AFInstallReferrerListener mInstallReferrerListener;



    protected void start(Context context, AFInstallReferrerListener installReferrerListener) {

        mInstallReferrerListener = installReferrerListener;

        mReferrerClient = newBuilder(context).build();

        try {
            mReferrerClient.startConnection(this);
        } catch (Exception e) {
            //e.printStackTrace();
            //TODO
            AFLogger.afErrorLog("referrerClient -> startConnection", e);
        }
    }

    /**
     * {
     * "rfr": {
     * "val": "Google Play new  install_referrer value: string",
     * "clk": "Google Play new 'referrer_last_click_timestamp' value: long",
     * "install": "Google Play new 'install_begin_timestamp' value: long",
     * "code": "0"  see codes @{@link com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse}
     * }
     * }
     */
    private void handleReferrer(@Nullable ReferrerDetails response, Map<String, String> referrer) {

        if (response != null){
            if (response.getInstallReferrer() != null) {
                referrer.put("val", response.getInstallReferrer());
            }

            referrer.put("clk", Long.toString(response.getReferrerClickTimestampSeconds()));
            referrer.put("install", Long.toString(response.getInstallBeginTimestampSeconds()));
        }

        if (mInstallReferrerListener != null) {
            mInstallReferrerListener.onHandleReferrer(referrer);
        }
    }

    @Override
    public void onInstallReferrerSetupFinished(int responseCode) {

        Map<String, String> referrer = new HashMap<>();
        referrer.put("code", String.valueOf(responseCode));

        ReferrerDetails response = null;

        switch (responseCode) {
            case InstallReferrerClient.InstallReferrerResponse.OK:
                try {
                    AFLogger.afDebugLog("InstallReferrer connected");
                    response = mReferrerClient.getInstallReferrer();
                    mReferrerClient.endConnection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                AFLogger.afWarnLog("InstallReferrer not supported");
                break;
            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                AFLogger.afWarnLog("InstallReferrer not supported");
                break;
            default:
                AFLogger.afWarnLog("responseCode not found.");
        }
        handleReferrer(response, referrer);
    }

    @Override
    public void onInstallReferrerServiceDisconnected() {
        AFLogger.afDebugLog("Install Referrer service disconnected");
    }
}
