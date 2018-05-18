package com.appsflyer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: gil
 * Date: 7/31/12
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
class Installation {
    private static String sID = null;
    private static final String INSTALLATION = "AF_INSTALLATION";

    public synchronized static String id(WeakReference<Context> context) {
        if (context.get() == null) {
            return sID;
        }
        if (sID == null) {
            String tmpSId = readInstallationSP(context);
            if (tmpSId != null) {
                sID = tmpSId;
            } else {
                try {
                    File installation = new File(context.get().getFilesDir(), INSTALLATION);
                    if (!installation.exists()) {
                        sID = generateId(context);
                    } else {
                        sID = readInstallationFile(installation);
                        installation.delete(); // remove the old file since it won't be in use anymore
                    }
                    writeInstallationSP(context,sID);
                } catch (Exception e) {
                    AFLogger.afErrorLog("Error getting AF unique ID",e);
                }
            }
            if (sID != null) {
                AppsFlyerProperties.getInstance().set(ServerParameters.AF_USER_ID,sID);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation)  {
        RandomAccessFile f = null;
        byte[] bytes = null;
        try {
            f = new RandomAccessFile(installation, "r");
            bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
        } catch (IOException e) {
            AFLogger.afErrorLog("Exception while reading InstallationFile: ",e);
        } finally {
            try {
                if (f != null) {
                    f.close();
                }
            } catch (IOException e) {
                AFLogger.afErrorLog("Exception while trying to close the InstallationFile",e);
            }
        }
        return new String(bytes != null ? bytes : new byte[0]);
    }

    private static void writeInstallationFile(File installation,WeakReference<Context> context)  {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(installation);
            String id = generateId(context);
            out.write(id.getBytes());
            out.close();
        } catch (IOException | PackageManager.NameNotFoundException e) {
            AFLogger.afErrorLog("Exception while writing InstallationFile",e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                AFLogger.afErrorLog("Exception while trying to close InstallationFile",e);
            }
        }
    }

    private static String generateId(WeakReference<Context> context) throws PackageManager.NameNotFoundException {

        String id;
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            long now = System.currentTimeMillis();
            id = generateId(now);
        } else {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    @NonNull
    static String generateId(final long now) {
        return now + "-" + Math.abs(new SecureRandom().nextLong());
    }

    private static String readInstallationSP(WeakReference<Context> context) {
        if (context.get() == null) {
            return null;
        }
        SharedPreferences sharedPreferences = context.get().getSharedPreferences(AppsFlyerLib.AF_SHARED_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(INSTALLATION,null);
    }

    private static void writeInstallationSP(WeakReference<Context> context) throws PackageManager.NameNotFoundException {
        writeInstallationSP(context, generateId(context));
    }

    @SuppressLint("CommitPrefEdits")
    private static void writeInstallationSP(WeakReference<Context> context, String sId) throws PackageManager.NameNotFoundException {
        SharedPreferences sharedPreferences = context.get().getSharedPreferences(AppsFlyerLib.AF_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(INSTALLATION,sId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
