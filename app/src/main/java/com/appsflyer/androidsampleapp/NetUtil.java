package com.appsflyer.androidsampleapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetUtil {

    private static final String TAG = "NetUtil";

    public static Result getResult(@NonNull Context context){
        String var1 = "unknown";
        String var2 = null;
        String var3 = null;

        try {

            String var10000;
            label97:{
                ConnectivityManager var4 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if(var4 != null){
                    NetworkInfo var9;
                    if(Build.VERSION.SDK_INT >= 21){
                        Network[] networks = var4.getAllNetworks();
                        int length = networks.length;
                        for(int i=0;i<length;i++){
                            Network network = networks[i];
                            NetworkInfo var13;
                            var13 = var4.getNetworkInfo(network);
                            var9 = var13;
                            if(var9 != null && var9.isConnectedOrConnecting()){
                                var10000 = 1 == var13.getType()?"WIFI":(0 == var13.getType()?"MOBILE" : "unknown");
                                break label97;
                            }

                            var10000 = "unknown";
                            break label97;
                        }

                        if ((var9 = var4.getNetworkInfo(1)) != null && var9.isConnectedOrConnecting()) {
                            var10000 = "WIFI";
                            break label97;
                        }

                        if ((var9 = var4.getNetworkInfo(0)) != null && var9.isConnectedOrConnecting()) {
                            var10000 = "MOBILE";
                            break label97;
                        }

                        NetworkInfo var5;
                        if ((var9 = var5 = var4.getActiveNetworkInfo()) != null && var9.isConnectedOrConnecting()) {
                            if (1 == var5.getType()) {
                                var10000 = "WIFI";
                                break label97;
                            }

                            if (0 == var5.getType()) {
                                var10000 = "MOBILE";
                                break label97;
                            }
                        }

                    }
                }

                var10000 = "unknown";
            }
            var1 = var10000;
            TelephonyManager var11;
            var2 = (var11 = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getSimOperatorName();
            if (((var3 = var11.getNetworkOperatorName()) == null || var3.isEmpty()) && 2 == var11.getPhoneType()) {
                var3 = "CDMA";
            }

        }catch (Throwable var10) {
            Log.e(TAG,"Exception while collecting network info. ", var10);
        }


        return new Result(var1,var3,var2);
    }

    static final class Single {
        static final NetUtil instance = new NetUtil();
    }


    static final class Result{

        private final String var1;
        private final String var2;
        private final String var3;

        Result(@NonNull String var1, @Nullable String var2,@Nullable String var3){

            this.var1 = var1;
            this.var2 = var2;
            this.var3 = var3;
        }

        final String getVar1(){
            return this.var1;
        }

        @Nullable
        final String getVar2(){
            return this.var2;
        }

        @Nullable
        final String getVar3(){
            return this.var3;
        }

        /**
         * 检查权限
         * @param context
         * @param var
         * @return
         */
        static boolean checkPermission(Context context,String var){
            int temp = ContextCompat.checkSelfPermission(context,var);
            return temp == 0;
        }
    }
}
