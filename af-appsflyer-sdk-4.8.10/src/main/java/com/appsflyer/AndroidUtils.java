package com.appsflyer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.List;

/**
 * @author by shacharaharon on 29/01/2017.
 */

class AndroidUtils {

    private final static String OPPO_BRAND_PATTERN = "OPPO";

    static boolean isReceiverAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo =
                packageManager.queryBroadcastReceivers(intent, 0);
        return resolveInfo.size() > 0;
    }

    static boolean isServiceAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo =
                packageManager.queryIntentServices(intent, 0);
        return resolveInfo.size() > 0;
    }

    static boolean isPermissionAvailable(Context context, String permissionString) {
        int res = ContextCompat.checkSelfPermission(context, permissionString);

        AFLogger.afRDLog("is Permission Available: " + permissionString + "; res: " + res);

        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
    * Workaround for OPPO series, https://appsflyer.atlassian.net/browse/RD-4790
    * */
    static boolean isOPPODevice(){
        if(OPPO_BRAND_PATTERN.equals(Build.BRAND)){
            return true;
        }
        return false;
    }
}
