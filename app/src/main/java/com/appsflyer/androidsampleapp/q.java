//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.appsflyer.androidsampleapp;

import com.appsflyer.AFLogger;

import java.security.MessageDigest;
import java.util.Formatter;

final class q {
    q() {
    }

    public static String ॱ(String var0) {
        String var1 = null;

        try {
            MessageDigest var2;
            (var2 = MessageDigest.getInstance("SHA-1")).reset();
            var2.update(var0.getBytes("UTF-8"));
            var1 = ॱ(var2.digest());
        } catch (Exception var3) {
            AFLogger.afErrorLog((new StringBuilder("Error turning ")).append(var0.substring(0, 6)).append(".. to SHA1").toString(), var3);
        }

        return var1;
    }

    public static String ˊ(String var0) {
        String var1 = null;

        try {
            MessageDigest var2;
            (var2 = MessageDigest.getInstance("MD5")).reset();
            var2.update(var0.getBytes("UTF-8"));
            var1 = ॱ(var2.digest());
        } catch (Exception var3) {
            AFLogger.afErrorLog((new StringBuilder("Error turning ")).append(var0.substring(0, 6)).append(".. to MD5").toString(), var3);
        }

        return var1;
    }

    public static String ˋ(String var0) {
        String var1 = null;

        try {
            MessageDigest var2;
            (var2 = MessageDigest.getInstance("SHA-256")).update(var0.getBytes());
            byte[] var8 = var2.digest();
            StringBuffer var3 = new StringBuffer();
            int var4 = var8.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                byte var6 = var8[var5];
                var3.append(Integer.toString((var6 & 255) + 256, 16).substring(1));
            }

            var1 = var3.toString();
        } catch (Exception var7) {
            AFLogger.afErrorLog((new StringBuilder("Error turning ")).append(var0.substring(0, 6)).append(".. to SHA-256").toString(), var7);
        }

        return var1;
    }

    private static String ॱ(byte[] var0) {
        Formatter var1 = new Formatter();
        int var2 = var0.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            byte var4 = var0[var3];
            var1.format("%02x", var4);
        }

        String var5 = var1.toString();
        var1.close();
        return var5;
    }
}
