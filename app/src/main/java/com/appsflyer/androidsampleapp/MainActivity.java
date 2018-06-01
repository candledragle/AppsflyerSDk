package com.appsflyer.androidsampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerLib;
import com.speed.booster.kim.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.StreamHandler;


public class MainActivity extends Activity {


    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetInstallDataText();
        Button trackEventButton = findViewById(R.id.trackEventButton);
        trackEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Track Events in real time */
                Map<String, Object> eventValue = new HashMap<String, Object>();
                eventValue.put(AFInAppEventParameterName.REVENUE, 200);
                eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "category_a");
                eventValue.put(AFInAppEventParameterName.CONTENT_ID, "1234567");
                eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
                AppsFlyerLib.getInstance().trackEvent(getApplicationContext(), AFInAppEventType.PURCHASE, eventValue);
            }
        });
    }



    /*** Ignore - used to display install data ***/
    public void SetInstallDataText(){
//        Log.e("APP-OPEN", "onCreate: ");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView installData = findViewById(R.id.installDataText);
                installData.setText(AFApplication.InstallConversionData);
            }
        } , 2500);

    }


    static final String INSTALL_UPDATE_DATE_FORMAT = "yyyy-MM-dd_HHmmssZ";  //时间戳格式
    static final String FIRST_INSTALL_PREF = "appsFlyerFirstInstall";
    private static final String AF_COUNTER_PREF = "appsFlyerCount";
    private final static String TAG = "validate";

    /**
     * 验证参数
     * @param view
     */

    public void validate(View view) throws PackageManager.NameNotFoundException {

        String cachePath = this.getCacheDir().getPath(); //获取缓存路径
        SLog.e(cachePath,cachePath);

        String format = "yyyy-MM-dd_HHmmssZ";
        SimpleDateFormat var47 = new SimpleDateFormat(format, Locale.US);

        if (Build.VERSION.SDK_INT >= 9) {
            try {
                //直接使用Android 自带的api 不再使用sp 去保存记录
                long var13 = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime;
                var47.setTimeZone(TimeZone.getTimeZone("UTC"));
                SLog.e("installDate",var47.format(new Date(var13)));

            } catch (Exception var30) {
                AFLogger.afErrorLog("Exception while collecting install date. ", var30);
            }
        }

        // firstLaunchDate 生成规则

        if(Build.VERSION.SDK_INT >= 9){
            var47.setTimeZone(TimeZone.getTimeZone("UTC"));
            SharedPreferences sp = getSharedPreferences("appsflyer-data", 0);
            String var68 = sp.getString("appsFlyerFirstInstall", null);

            if(var68 == null){

                SharedPreferences data = getSharedPreferences("appsflyer-data", 0);
                if(!data.contains("appsFlyerCount")){
                    var68 = var47.format(new Date());
                }else{
                    var68 = "";
                }
            }

            SLog.e("firstLaunchDate",var68);
        }
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

        long firstInstallTime = packageInfo.firstInstallTime;
        long lastUpdateTime = packageInfo.lastUpdateTime;

        //date1
        String date1 = var47.format(new Date(firstInstallTime));
        SLog.e("date1",date1);

        //date2
        String date2 = var47.format(new Date(lastUpdateTime));
        SLog.e("date2",date2);


        //device 生成
        String device = Build.DEVICE;
        Log.e(TAG,"device: "+device);

        //product
        String product = Build.PRODUCT;
        Log.e(TAG,"product: "+product);

        //deviceType
        String deviceType = Build.TYPE;
        Log.e(TAG,"deviceType: "+deviceType);

        //sdk
        int sdk = Build.VERSION.SDK_INT;
        SLog.e("sdk",String.valueOf(sdk));

        //network
        NetUtil.Result result = NetUtil.getResult(this.getApplicationContext());
        String network = result.getVar1();
        SLog.e("network",network);

        //operator
        String operator = result.getVar3();
        SLog.e("operator",operator);

        //carrier
        String carrier = result.getVar2();
        SLog.e("carrier",carrier);

        //app_version_code
        String app_version_code = Integer.toString(packageInfo.versionCode);
        SLog.e("app_version_code",app_version_code);

        //app_version_name
        String app_version_name = packageInfo.versionName;
        SLog.e("app_version_name",app_version_name);



    }

    private String dateFormatUTC(SimpleDateFormat dateFormat, long time) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    static class SLog{
        private static final String TAG = "validate";
        public static void e(String key, String value){
            Log.e(TAG,key+ " : "+value);
        }
    }
}
