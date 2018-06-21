package com.appsflyer.androidsampleapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import java.lang.reflect.Method;
import java.util.Map;

import lab.galaxy.yahfa.HookMain;


/*********************************************************************
 In order for us to provide optimal support,
 we would kindly ask you to submit any issues to support@appsflyer.com
 *********************************************************************/



public class AFApplication extends Application {


    private static final String AF_DEV_KEY = "WpSoFapu6Pwg8Z5kcJrN2b";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);

    }


    @Override
    public void onCreate(){
        super.onCreate();

        //hook 方法获取参数内容不用在通过修改 smali文件的语法
        /*try {
            Class<?> targetClazz = Class.forName("com.appsflyer.e");
            String methodName = "ˊ";
            String methodSig = "(Landroid/content/Context;)Ljava/lang/String";
            Class hookClass = Class.forName("com.appsflyer.androidsampleapp.Hook");

            Method hook = hookClass.getDeclaredMethod("hookMethod",Context.class,String.class);
            Method backup = hookClass.getDeclaredMethod("backup",Context.class,String.class);
            HookMain.findAndBackupAndHook(targetClazz, methodName, methodSig, hook, backup);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/

        Log.d(AFApplication.class.getName(), "onCreate: ---");

        /**  Set Up Conversion Listener to get attribution data **/

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {

            /* Returns the attribution data. Note - the same conversion data is returned every time per install */
            @Override
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.d(Constant.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
                setInstallData(conversionData);
            }

            @Override
            public void onInstallConversionFailure(String errorMessage) {
                Log.d(Constant.LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            /* Called only when a Deep Link is opened */
            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {
                for (String attrName : conversionData.keySet()) {
                    Log.d(Constant.LOG_TAG, "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(Constant.LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };


        /* This API enables AppsFlyer to detect installations, sessions, and updates. */

        AppsFlyerLib.getInstance().init(AF_DEV_KEY , conversionListener , getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this, AF_DEV_KEY);


        /* Set to true to see the debug logs. Comment out or set to false to stop the function */

        AppsFlyerLib.getInstance().setDebugLog(true);

    }



    /* IGNORE - USED TO DISPLAY INSTALL DATA */
    public static String InstallConversionData =  "";
    public static int sessionCount = 0;
    public static void setInstallData(Map<String, String> conversionData){
        if(sessionCount == 0){
            final String install_type = "Install Type: " + conversionData.get("af_status") + "\n";
            final String media_source = "Media Source: " + conversionData.get("media_source") + "\n";
            final String install_time = "Install Time(GMT): " + conversionData.get("install_time") + "\n";
            final String click_time = "Click Time(GMT): " + conversionData.get("click_time") + "\n";
            final String is_first_launch = "Is First Launch: " + conversionData.get("is_first_launch") + "\n";
            InstallConversionData += install_type + media_source + install_time + click_time + is_first_launch;
            sessionCount++;
        }

    }


}
