package com.appsflyer.androidsampleapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import dalvik.system.DexClassLoader;

import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AFLogger;
import com.appsflyer.AppsFlyerLib;
import com.wifiup.R;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import lab.galaxy.yahfa.HookMain;


public class MainActivity extends Activity {


    final Handler handler = new Handler();

    private static String a(String var0, String var1) {
        StackTraceElement var2 = (new Throwable()).getStackTrace()[2];
        SimpleDateFormat var3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        return "[" + var0 + "] " + var3.format(new Date()) + " in " + var2.getFileName() + "(" + var2.getLineNumber() + ") >" + var1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetInstallDataText();
        Button trackEventButton = findViewById(R.id.trackEventButton);
        trackEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //imei
                String imei = com.appsflyer.androidsampleapp.AppsFlyerLib.getImei(getApplicationContext());
                SLog.e("imei", imei);

                //android_id
                String android_id = com.appsflyer.androidsampleapp.AppsFlyerLib.getAndroidId(getApplicationContext());
                SLog.e("android_id", android_id);

                //advertising_id
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String adid = AdvertisingIdClient.getGoogleAdId(getApplicationContext());
                            Log.e("MainActivity", "advertising_id:  " + adid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                System.out.println("大家好！");

                /* Track Events in real time */
                Map<String, Object> eventValue = new HashMap<String, Object>();
                eventValue.put(AFInAppEventParameterName.REVENUE, 200);
                eventValue.put(AFInAppEventParameterName.CONTENT_TYPE, "category_a");
                eventValue.put(AFInAppEventParameterName.CONTENT_ID, "1234567");
                eventValue.put(AFInAppEventParameterName.CURRENCY, "USD");
                AppsFlyerLib.getInstance().trackEvent(getApplicationContext(),
                        AFInAppEventType.PURCHASE, eventValue);

                // Test.sayHello("test say hello world!");

                try {
                    Class<?> clazz = Class.forName("com.appsflyer.e");

                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        Log.e("SYM", method.getName());
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Log.e("SYM", "参数类型");
                        for (Class temp : parameterTypes) {
                            Log.e("SYM", temp.getName());
                        }
                        Log.e("SYM", " ======== ");
                    }
                    Method method = clazz.getDeclaredMethod("ˊ", Context.class, long.class);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("SYM", e.toString());
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /*** Ignore - used to display install data ***/
    public void SetInstallDataText() {
//        Log.e("APP-OPEN", "onCreate: ");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView installData = findViewById(R.id.installDataText);
                installData.setText(AFApplication.InstallConversionData);
            }
        }, 2500);

    }


    static final String INSTALL_UPDATE_DATE_FORMAT = "yyyy-MM-dd_HHmmssZ";  //时间戳格式
    static final String FIRST_INSTALL_PREF = "appsFlyerFirstInstall";
    private static final String AF_COUNTER_PREF = "appsFlyerCount";
    private final static String TAG = "validate";

    /**
     * 验证参数
     */

    public void validate(View view) throws PackageManager.NameNotFoundException {

        /*try {
            InputStream stream = getAssets().open("client.cer");
            SSLContext tls = SSLContext.getInstance("TLS");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null); //去掉系统默认证书
            Certificate certificate =
                    CertificateFactory.getInstance("X.509").generateCertificate(stream);
            //设置自己的证书
            keyStore.setCertificateEntry("skxy", certificate);
            //通过信任管理器获取一个默认的算法
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            //算法工厂创建
            TrustManagerFactory instance = TrustManagerFactory.getInstance(algorithm);
            instance.init(keyStore);
            tls.init(null, instance.getTrustManagers(), null);
            SSLSocketFactory socketFactory = tls.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }*/

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                com.appsflyer.androidsampleapp.AppsFlyerLib appsFlyerLib1 = new com.appsflyer.androidsampleapp.AppsFlyerLib();
                try {
                    String json = appsFlyerLib1.assembleParams(MainActivity.this.getApplicationContext());
                    String url = "https://t.appsflyer.com/api/v4/androidevent?buildnumber=4.8.12&app_id=com.wifiup";
                    //String url = "https://10.200.10.230:8443/Test01/helloWorld";
                    appsFlyerLib1.post(url,json);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                //com.appsflyer.androidsampleapp.AppsFlyerLib.test();
            }
        });



        //1529053599654
        //cb37a8d3ffdd22f68f813d5791ad7f5f0c

        /*try {
            Class<?> clazz = Class.forName("com.appsflyer.e");
            Method method = clazz.getDeclaredMethod("ˊ", Context.class, long.class);
            method.setAccessible(true);
            String ret = (String) method.invoke(clazz,this,Long.valueOf("1529053599654"));
            SLog.e("cksm_v1",ret);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }*/

        /*String cksm_v1 = CKSM_V11.generate(getApplication(), 1529027234660L);
        SLog.e("cksm_v1",cksm_v1);*/


        /*Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                com.appsflyer.androidsampleapp.AppsFlyerLib.test();
            }
        });
*/

        com.appsflyer.androidsampleapp.AppsFlyerLib appsFlyerLib = new com.appsflyer.androidsampleapp.AppsFlyerLib();
        String af_v2 = appsFlyerLib.get_af_v2(this);
        SLog.e("af_v2", af_v2);

        File filesDir = getFilesDir();
        SLog.e("fileDir", filesDir.getAbsolutePath());

        String cachePath = this.getCacheDir().getPath(); //获取缓存路径
        SLog.e(cachePath, cachePath);

        String format = "yyyy-MM-dd_HHmmssZ";
        SimpleDateFormat var47 = new SimpleDateFormat(format, Locale.US);

        if (Build.VERSION.SDK_INT >= 9) {
            try {
                //直接使用Android 自带的api 不再使用sp 去保存记录
                long var13 = getPackageManager().getPackageInfo(getPackageName(),
                        0).firstInstallTime;
                var47.setTimeZone(TimeZone.getTimeZone("UTC"));
                SLog.e("installDate", var47.format(new Date(var13)));

            } catch (Exception var30) {
                AFLogger.afErrorLog("Exception while collecting install date. ", var30);
            }
        }

        // firstLaunchDate 生成规则

        if (Build.VERSION.SDK_INT >= 9) {
            var47.setTimeZone(TimeZone.getTimeZone("UTC"));
            SharedPreferences sp = getSharedPreferences("appsflyer-data", 0);
            String var68 = sp.getString("appsFlyerFirstInstall", null);

            if (var68 == null) {

                SharedPreferences data = getSharedPreferences("appsflyer-data", 0);
                if (!data.contains("appsFlyerCount")) {
                    var68 = var47.format(new Date());
                } else {
                    var68 = "";
                }
            }

            SLog.e("firstLaunchDate", var68);
        }
        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

        long firstInstallTime = packageInfo.firstInstallTime;
        long lastUpdateTime = packageInfo.lastUpdateTime;

        //date1
        String date1 = var47.format(new Date(firstInstallTime));
        SLog.e("date1", date1);

        //date2
        String date2 = var47.format(new Date(lastUpdateTime));
        SLog.e("date2", date2);


        //device 生成
        String device = Build.DEVICE;
        Log.e(TAG, "device: " + device);

        //product
        String product = Build.PRODUCT;
        Log.e(TAG, "product: " + product);

        //deviceType
        String deviceType = Build.TYPE;
        Log.e(TAG, "deviceType: " + deviceType);

        //sdk
        int sdk = Build.VERSION.SDK_INT;
        SLog.e("sdk", String.valueOf(sdk));

        //network
        NetUtil.Result result = NetUtil.getResult(this.getApplicationContext());
        String network = result.getVar1();
        SLog.e("network", network);

        //operator
        String operator = result.getVar3();
        SLog.e("operator", operator);

        //carrier
        String carrier = result.getVar2();
        SLog.e("carrier", carrier);

        //app_version_code
        String app_version_code = Integer.toString(packageInfo.versionCode);
        SLog.e("app_version_code", app_version_code);

        //app_version_name
        String app_version_name = packageInfo.versionName;
        SLog.e("app_version_name", app_version_name);

        //af_timestamp 当前时间
        long var11 = (new Date()).getTime();
        String af_timestamp = Long.toString(var11);

        //appsflyerKey
        String appsflyerKey = AF_DEV_KEY;


        //af_preinstalled  是否是预装


        //checkPermisson();

        //installer_package
        PackageManager packageManager = getPackageManager();
        String packageName = getPackageName();
        String installerPackageName = packageManager.getInstallerPackageName(packageName);
        SLog.e("installerPackageName", installerPackageName);

    }

    private static final String AF_DEV_KEY = "WpSoFapu6Pwg8Z5kcJrN2b";


    private String dateFormatUTC(SimpleDateFormat dateFormat, long time) {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date(time));
    }

    static class SLog {
        private static final String TAG = "validate";

        public static void e(String key, String value) {
            Log.e(TAG, key + " : " + value);
        }
    }

    //dex 压缩文件的路径
    private static String DEX_PATH = "";
    //dex 文件解压后释放的目录
    private static String DEX_OUT_PATH = "";
    private static String CLASS_NAME = "";
    private static String METHOD_NAME = "";
    private static Class param[] = null;
    private static final int REQUEST_CODE = 123;
    private static String DEX_NAME = "classes.dex";

    /**
     * 创建目录
     */
    private void makeDir() {
        DEX_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator + "dex";
        SLog.e("SYM", DEX_PATH);
        DEX_OUT_PATH = getDir("dex", 0).getAbsolutePath();
        File file = new File(DEX_PATH);
        SLog.e("SYM", DEX_OUT_PATH);

        if (file.exists()) {
            dexTest();
        } else {
            if (file.mkdirs()) {
                dexTest();
            }
        }

    }


    private void dexTest() {

        // File dex = new File("dex");
        DEX_PATH = new File(DEX_PATH + File.separator + DEX_NAME).toString();
        CLASS_NAME = "com.appsflyer.androidsampleapp.CKSM_V1";
        METHOD_NAME = "sayHello";

        SLog.e("SYM", "测试开始 ++++ ");
        DexClassLoader dexClassLoader = new DexClassLoader(DEX_PATH, DEX_OUT_PATH, null,
                getClassLoader());

        try {
            Class<?> clazz = dexClassLoader.loadClass(CLASS_NAME);
            Object instance = clazz.newInstance();
            Method method = clazz.getMethod(METHOD_NAME, param);
            method.invoke(instance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            SLog.e("SYM", "测试结束 +++++ ");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (REQUEST_CODE == requestCode) {
            makeDir();
        } else if (READ_PHONE_STATE == requestCode) {
            //imei
            String imei = com.appsflyer.androidsampleapp.AppsFlyerLib.getImei(getApplicationContext());
            SLog.e("imei", imei);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static final int READ_PHONE_STATE = 1011;

    /**
     * 检查权限
     */
    private void checkPermisson() {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissonStrs = new ArrayList<>();
            int open = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (open != PackageManager.PERMISSION_GRANTED) {
                permissonStrs.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                requestPermissions(permissonStrs.toArray(new String[0]), REQUEST_CODE);
            } else {
                makeDir();
            }
        }
    }

    /**
     * 检查制定的权限
     */
    private boolean chekPermisson(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissonStrs = new ArrayList<>();
            int open = ContextCompat.checkSelfPermission(this, permission);
            if (open != PackageManager.PERMISSION_GRANTED) {
                permissonStrs.add(permission);
                requestPermissions(permissonStrs.toArray(new String[0]), requestCode);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
