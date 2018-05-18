package com.appsflyer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.appsflyer.AFDateFormat.dateFormatUTC;
import static com.appsflyer.AFDateFormat.getDataFormatter;
import static com.appsflyer.AppsFlyerLib.INSTALL_UPDATE_DATE_FORMAT;
import static com.appsflyer.HashUtils.toMD5;
import static com.appsflyer.HashUtils.toSha256;



class AFSecretsManager {

    private static final String DOT = ".";
    private static final String FALLBACK_CHECKSUM = "babeae059d53ab934f4e4e40d91f2011";
    private static final String AF_TELEPHONY_MANAGER_PATH = "android.telephony.TelephonyManager";
    private static final String AF_BATTERY_MANAGER_PATH = "android.os.BatteryManager";
    private static final String SENSOR_PATH = "android.hardware.Sensor";
    private static final String ASYNC_TASK_PATH = "android.os.AsyncTask";
    private static final String URI_PATH = "android.net.Uri";
    private static final String EXISTS = "1";
    private static final String NOT_EXISTS = "0";
    private static final int FLAG_INDEX_1 = 17;
    private static final int FLAG_INDEX_2 = 27;

    /**
     * Checksum structure:
     * Checksum contains 8 flags,
     * FIRST BLOCK
     * 1 - check if "android.telephony.TelephonyManager" class exists
     * 2 - package name has been extracted from Context
     * 3 - package name has been extracted from process ID
     * 4 - package name has been extracted from Manifest
     *
     * SECOND BLOCK
     * 5 - check if "android.telephony.TelephonyManager" class exists
     * 6 - check if "android.os.BatteryManager" class exists
     * 7 - check if "android.hardware.Sensor" class exists
     * 8 - check if "android.net.Uri" class exists
     *
     * Every block will be converted from binary to hexadecimal
     * and stored in specific index into the checksum
     * @param context
     * @return
     */
    @Nullable
    static String getCheckSum(Context context, final long timestamp) {
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder firstFlagStringBuilder = new StringBuilder();
        StringBuilder secondFlagStringBuilder = new StringBuilder();

        // Check if a "AF_TELEPHONY_MANAGER_PATH" class exists
        firstFlagStringBuilder.append(isClass(AF_TELEPHONY_MANAGER_PATH) ? EXISTS : NOT_EXISTS);

        // Add package name
        sBuilder.append(getPackageName(context, firstFlagStringBuilder));

        // Add first install time
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long firstInstallTime = packageInfo.firstInstallTime;
            sBuilder.append(dateFormatUTC(getDataFormatter(INSTALL_UPDATE_DATE_FORMAT), firstInstallTime));
        } catch (PackageManager.NameNotFoundException e) {
            return FALLBACK_CHECKSUM;
        }

        // Add timestamp
       // long timestamp = AppsFlyerLib.getInstance().getLastServerReportTimestamp();
        sBuilder.append(dateFormatUTC(getDataFormatter(INSTALL_UPDATE_DATE_FORMAT), timestamp));

        // Create second block of flags
        addClassesSignature(secondFlagStringBuilder);

        // Replace a specific char with a flags
        String result = addFlagsToChecksum(encrypt(sBuilder.toString()), firstFlagStringBuilder.toString(), FLAG_INDEX_1);
        result = addFlagsToChecksum(result, secondFlagStringBuilder.toString(), FLAG_INDEX_2);
        return result;
    }

    /**
     * Encrypt a given string, using SHA256 and MD5
     * @param toEncrypt
     * @return
     */
    private static String encrypt(String toEncrypt) {
        //SHA256
        String afterSHA256 = toSha256(toEncrypt);
        // MD5
        String afterMD5 = toMD5(afterSHA256);

        return afterMD5;
    }

    /**
     * Swap prefix and a postfix of a package name
     * @param name
     * @return
     */
    private static String swapPackageName(String name) {
        if (!name.contains(DOT)) {
            return name;
        }

        String[] split = name.split("\\.");
        int length = split.length;
        StringBuilder sBuilder = new StringBuilder();

        // Add a first string
        sBuilder.append(split[length - 1]).append(DOT);

        // Add a rest of values
        for (int i = 1; i < length - 1; i++) {
            sBuilder.append(split[i]).append(DOT);
        }

        // Add a last string
        sBuilder.append(split[0]);

        return sBuilder.toString();
    }

    /**
     * Get package name, usnig process ID
     *
     * @param context
     * @return
     */
    private static String getCachePath(Context context) {
        String pName = null;
        if (System.getProperties().containsKey("java.io.tmpdir")) {
            try {
                String cachePath  = context.getCacheDir().getPath();
                String cacheLessPath = cachePath.replace("/cache", "");
                Pattern sPattern = Pattern.compile(".*/\\s*(.*)");
                Matcher sMatcher = sPattern.matcher(cacheLessPath);
                if (sMatcher.find()) {
                    pName = sMatcher.group(1);
                }
            } catch (Exception ex) {
                ProxyManager.getInstance().addLogEntry("ChecksumException", "Failed to get cache path with exception: "+ex);
                return null;
            }
        }
        return pName;
    }


    private static String getPackageFromManifest(Context context) {
        String pName;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            pName = packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
        return pName;
    }

    /**
     * Retrieve a package name Using
     * @param context
     * @param flagsBuilder
     * @return
     */
    private static String getPackageName(Context context, StringBuilder flagsBuilder) {
        StringBuilder packageBuilder = new StringBuilder();

        // Package name from a Context
        String pNameFromContext = context.getPackageName();
        String swappedPNameFromContext = swapPackageName(pNameFromContext);
        flagsBuilder.append(EXISTS);
        packageBuilder.append(swappedPNameFromContext);

        // validate package name with cache path
        String pNameFromCachePath =getCachePath(context);
        if (pNameFromCachePath == null) {
            flagsBuilder.append(NOT_EXISTS);
            packageBuilder.append(pNameFromContext);
        }
        else {
            flagsBuilder.append(EXISTS);
            packageBuilder.append(pNameFromContext); // todo: Change on next version after verification to pNameFromCachePath
        }

        // Package name from a Manifest
        String pNameFromManifest = getPackageFromManifest(context);
        if (pNameFromManifest == null) {
            flagsBuilder.append(NOT_EXISTS);
            packageBuilder.append(pNameFromContext);
        } else {
            flagsBuilder.append(EXISTS);
            packageBuilder.append(pNameFromManifest);
        }
        return packageBuilder.toString();
    }


    /**
     * @param className
     * @return true if a given class exists, otherwise return false
     */
    private static boolean isClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Add flags as a hex in specific index
     * @param checksum
     * @param flags
     * @param index
     * @return
     */
    private static String addFlagsToChecksum(String checksum, String flags, int index) {
        StringBuilder stringBuilder = new StringBuilder(checksum);
        stringBuilder.setCharAt(index, convertFlagsToChar(flags).charAt(0));
        return stringBuilder.toString();
    }

    /**
     * Convert flags as a binary string to char
     *
     * @param flags
     * @return
     */
    private static String convertFlagsToChar(String flags) {
        int decimal = Integer.parseInt(flags, 2);
        return Integer.toString(decimal, 16);
    }

    private static void addClassesSignature(StringBuilder sBuilder) {
        // Check if a "AF_BATTERY_MANAGER_PATH" class exists
        sBuilder.append(isClass(AF_BATTERY_MANAGER_PATH) ? EXISTS : NOT_EXISTS);
        // Check if a "SENSOR_PATH" class exists
        sBuilder.append(isClass(SENSOR_PATH) ? EXISTS : NOT_EXISTS);
        // Check if a "ASYNC_TASK_PATH" class exists
        sBuilder.append(isClass(ASYNC_TASK_PATH) ? EXISTS : NOT_EXISTS);
        // Check if a "URI_PATH" class exists
        sBuilder.append(isClass(URI_PATH) ? EXISTS : NOT_EXISTS);
    }
}
