package com.appsflyer;

import android.support.annotation.WorkerThread;

import java.util.Map;

/**
 * @author by gilmeroz on 7/7/14.
 */
public interface AppsFlyerConversionListener {
    @WorkerThread
    void onInstallConversionDataLoaded(Map<String, String> conversionData);

    void onInstallConversionFailure(String errorMessage);

    @WorkerThread
    void onAppOpenAttribution(Map<String, String> attributionData);

    void onAttributionFailure(String errorMessage);

}
