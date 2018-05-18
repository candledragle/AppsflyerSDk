package com.appsflyer;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.ref.WeakReference;

/**
 * @author by shacharaharon on 19/01/2017.
 */

class AFLifecycleCallbacks {

    static void doOnResume(Context context) {
        AFLogger.afInfoLog("onBecameForeground");
        AppsFlyerLib.getInstance().resetTimeEnteredForeground();
        AppsFlyerLib.getInstance().trackEventInternal(context, null, null);

        AFLogger.resetDeltaTime();
    }

    static void doOnPause(Context context) {
        context = context.getApplicationContext();
        AFLogger.afInfoLog("onBecameBackground");
        AppsFlyerLib.getInstance().resetTimeWentToBackground();
        AFLogger.afInfoLog("callStatsBackground background call");
        WeakReference<Context> weakContext = new WeakReference<>(context);
        AppsFlyerLib.getInstance().callStatsBackground(weakContext);
        ProxyManager rdInstance = ProxyManager.getInstance();
        if (rdInstance.isProxyEnabledFromServer()) {
            rdInstance.stopProxyMode();
            if (context != null) {
                String packageName = context.getPackageName();
                PackageManager packageManager = context.getPackageManager();
                rdInstance.sendProxyData(packageName,packageManager);
            }
            rdInstance.releaseProxy();
        } else {
            AFLogger.afDebugLog("RD status is OFF");
        }

        AFExecutor.getInstance().shutdownExecutors();
    }

}
