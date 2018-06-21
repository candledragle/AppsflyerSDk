package com.appsflyer.androidsampleapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.appsflyer.AFLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class AppsFlyerLib {

    private final static String TAG = "AppsFlyerLib";
    private HashMap params = new HashMap();


    //判断是否是预装
    public boolean isPreInstalledApp(Context context) {

        boolean flag = false;

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            int flags = applicationInfo.flags;
            flags = flags & 1;
            if (flags != 0) {
                return true;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not check if app is pre installed");
        }

        return flag;
    }

    /**
     * GAID_retry 的值为true 或者 false
     * 如果能在 AppsFlyerProperties 维护的集合
     * 中查到advertiserId 就返回true 否则返回false
     */

    public void put_GAID_retry() {

        String ret = ""; //查询结果为一个字符串
        if (TextUtils.isEmpty(ret)) {
            params.put("GAID_retry", "false");
        } else {
            params.put("GAID_retry", "true");
        }

    }

    /**
     * 判断 tokenRefreshConfigured的值
     *
     * @param
     * @return
     */

    public void put_token_Refresh_Configured() {
        // todo 有待确认具体逻辑
        params.put("tokenRefreshConfigured", Boolean.FALSE);
    }

    /**
     * 集成平台获取
     */
    private void put_platform_extension() {
        Platform platform = new Platform();
        params.put("platformextension", platform.getPlatformExtension());
    }


    //判断安装来源
    public String getInstallerPackage(Context context) {

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        String installerPackageName = pm.getInstallerPackageName(packageName);
        return installerPackageName;
    }

    //获取device_id

    public static String getDevice_id() {
        String ret = null;

        if (Build.VERSION.SDK_INT >= 9) {
            long currentTimeMillis = System.currentTimeMillis();
            ret = (new StringBuilder()).append(currentTimeMillis).append("-").append(Math.abs((new SecureRandom()).nextLong())).toString();

        } else {
            ret = UUID.randomUUID().toString();
        }

        //uid
        String uid = ret;
        hashMap.put("uid", uid);

        return ret;
    }

    // isFirstCall 是否是sdk第一次请求

    public boolean isFirstCall(SharedPreferences sp) {

        /**
         * sentSuccessfully 记录
         * 当sdk第一次请求完成之后就会将这个字段记录到sp 文件中
         * sp 文件的名字是 appsflyer-data
         */

        boolean var16 = "true".equals(sp.getString("sentSuccessfully", ""));
        params.put("isFirstCall", Boolean.toString(!var16));

        return !var16;
    }

    //sentRegisterRequestToAF
    // TODO: 6/13/18  待确定sentRegisterRequestToAF 这个字段什么时候记录到sp文件中的，并且代表什么意思

    //registeredUninstall
    public boolean registeredUninstall(SharedPreferences sp) {

        return sp.getBoolean("sentRegisterRequestToAF", false);
    }

    //timepassedsincelastlaunch 记录软件本次启动时间

    public long timepassedsincelastlaunch(Context context) {

        long var23 = System.currentTimeMillis();
        long var21 = context.getSharedPreferences("appsflyer-data", 0).getLong("AppsFlyerTimePassedSincePrevLaunch", 0L);
        addSp(context, "AppsFlyerTimePassedSincePrevLaunch", var23);
        long var88;
        if (var21 > 0L) {
            long var25 = var23 - var21;
            var88 = var25 / 1000L;
        } else {
            var88 = -1L;
        }

        params.put("timepassedsincelastlaunch", Long.toString(var88));
        return var88;
    }

    private void addSp(Context var0, String var1, long var2) {
        SharedPreferences.Editor var6;
        (var6 = var0.getSharedPreferences("appsflyer-data", 0).edit()).putLong(var1, var2);
        if (Build.VERSION.SDK_INT >= 9) {
            var6.apply();
        } else {
            var6.commit();
        }
    }

    private static final String AF_DEV_KEY = "WpSoFapu6Pwg8Z5kcJrN2b";
    /*
     * 1528880709531
     * "af_v2":"1047414c5bb6f12b0d7c6485c7055954f68ed066"
     * */

    public String get_af_v2(Context context) {

        //appsflyerKey
        String appsflyerKey = AF_DEV_KEY;

        //af_timestamp 当前时间
        //long var11 = (new Date()).getTime();
        String af_timestamp = (String) hashMap.get("af_timestamp");
        //String af_timestamp = "1528882343560";

        //uid
        String uid = getDevice_id();
        //String uid = "1529294447612-3396698917248139127";

        //installDate
        String installDate = get_installDate(context);


        //counter
        // TODO: 6/13/18  有待进一步确认counter的具体意义 sp appsFlyerCount值
        String counter = "40";

        hashMap.put("counter", counter);

        //iaecounter
        // TODO: 6/13/18 记录数字  sp appsFlyerInAppEventCount值
        String iaecounter = "0";
        hashMap.put("iaecounter", iaecounter);


        String str = new StringBuilder().append(appsflyerKey).append(af_timestamp).append(uid).append(installDate).append(counter).append(iaecounter).toString();

        return sha_1(md5(str));

    }

    public String md5(String str) {
        String var1 = null;

        try {
            MessageDigest var2;
            (var2 = MessageDigest.getInstance("MD5")).reset();
            var2.update(str.getBytes("UTF-8"));
            var1 = format(var2.digest());
        } catch (Exception var3) {
            //AFLogger.afErrorLog((new StringBuilder("Error turning ")).append(str.substring(0, 6)).append(".. to MD5").toString(), var3);
        }

        return var1;
    }

    public String sha_1(String var0) {
        String var1 = null;

        try {
            MessageDigest var2;
            (var2 = MessageDigest.getInstance("SHA-1")).reset();
            var2.update(var0.getBytes("UTF-8"));
            var1 = format(var2.digest());
        } catch (Exception var3) {
            //AFLogger.afErrorLog((new StringBuilder("Error turning ")).append(var0.substring(0, 6)).append(".. to SHA1").toString(), var3);
        }

        return var1;
    }

    private static String format(byte[] var0) {
        Formatter var1 = new Formatter();
        int var2 = var0.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            byte var4 = var0[var3];
            var1.format("%02x", var4);
        }

        String var5 = var1.toString();
        var1.close();
        return var5;
    }


    static HashMap hashMap = new HashMap();

    //参数组装
    public String assembleParams(Context context) throws JSONException, PackageManager.NameNotFoundException {

        JSONObject body = new JSONObject();
        body.put("country", Locale.getDefault().getCountry());
        long timestamp = new Date().getTime();
        hashMap.put("af_timestamp", Long.toString(timestamp));
        body.put("af_timestamp", timestamp);
        body.put("appsflyerKey", AF_DEV_KEY);
        body.put("af_events_api", "1");
        body.put("isFirstCall", "true");
        body.put("registeredUninstall", false); //未知、
        body.put("operator", "");
        body.put("network", "WIFI");
        body.put("timepassedsincelastlaunch", "9"); //本次软件运行时间
        body.put("af_v2", get_af_v2(context));

        body.put("uid", hashMap.get("uid"));
        body.put("isGaidWithGps", "false");

        body.put("lang_code", Locale.getDefault().getLanguage());

        body.put("installDate", hashMap.get("installDate"));

        body.put("app_version_code", "1");

        body.put("firstLaunchDate", get_firstLaunchDate());

        body.put("model", Build.MODEL);

        body.put("lang", "English");

        body.put("brand", Build.BRAND);

        body.put("deviceType", Build.TYPE);

        body.put("product", Build.PRODUCT);

        JSONObject deviceData = new JSONObject();
        body.put("deviceData", deviceData);
        deviceData.put("cpu_abi", getDeviceData("ro.product.cpu.abi"));
        deviceData.put("build_display_id", getDeviceData("ro.build.display.id"));
        deviceData.put("arch", getDeviceData("os.arch"));
        deviceData.put("cpu_abi2", "ro.product.cpu.abi2");
        deviceData.put("btch", "no"); //坚挺电量变化
        deviceData.put("btl","94.0"); //剩余电量

        JSONObject dim = new JSONObject();
        deviceData.put("dim", dim);
        dim.put("size", 2);
        dim.put("ydp", "399.393");
        dim.put("xdp", "403.411");
        dim.put("y_px", "2154");
        dim.put("x_px", "1080");
        dim.put("d_dpi", "420");

        body.put("date2", getData2(context));
        body.put("counter", hashMap.get("counter"));

        body.put("date1", getData1(context));

        body.put("advertiserId", ADVERTISER_ID);

        body.put("advertiserIdEnabled", "true");

        body.put("referrer", REFERRER);

        body.put("af_v", get_af_v());

        body.put("carrier", ""); //网络类型

        body.put("af_preinstalled", String.valueOf(isPreInstalledApp(context))); //是否是预装

        body.put("iaecounter", hashMap.get("iaecounter"));

        body.put("tokenRefreshConfigured", false);

        body.put("sdk", Integer.toString(Build.VERSION.SDK_INT));

        body.put("gaidError", ""); //

        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        body.put("app_version_name", packageInfo.versionName);

        body.put("device", Build.DEVICE);

        body.put("platformextension", "android_native");

        body.put("cksm_v1", get_cksm_v1(context, timestamp));

        JSONObject rfr = new JSONObject();
        body.put("rfr", rfr);
        rfr.put("val", REFERRER);
        rfr.put("code", "0");
        long random = Math.abs((new SecureRandom()).nextLong());
        rfr.put("clk", String.valueOf(random));
        long random1 = Math.abs((new SecureRandom()).nextLong());
        rfr.put("install", String.valueOf(random1));

        return body.toString();
    }


    public String get_cksm_v1(Context context, long timestamp) {

        try {
            Class<?> clazz = Class.forName("com.appsflyer.e");
            Method method = clazz.getDeclaredMethod("ˊ", Context.class, long.class);
            method.setAccessible(true);
            String ret = (String) method.invoke(clazz, context, Long.valueOf(timestamp));
            return ret;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return "";
    }


    String format_str = "yyyy-MM-dd_HHmmssZ";
    SimpleDateFormat format = new SimpleDateFormat(format_str, Locale.US);
    String ADVERTISER_ID = "02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704";

    String REFERRER = "af_tranid%3DMHoJkcIRElf9Or6sHgJ88A%26c%3D123%26af_siteid%3Dwerr%26pid%3D124%26af_click_lookback%3D1d%26clickid%3D2069849869957480259%26android_id%3Dc05cb39adca5b091%26advertising_id%3D02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704%26imei%3D869897036845351";

    public String get_af_v() {

        String var64 = AF_DEV_KEY;
        String var68 = (String) hashMap.get("uid");
        String var65 = (String) hashMap.get("af_timestamp");
        String temp = (new StringBuilder()).append(var64.substring(0, 7)).append(var68.substring(0, 7)).append(var65.substring(var65.length() - 7)).toString();
        return sha_1(temp);
    }

    public String getData1(Context context) {
        String ret = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long firstInstallTime = packageInfo.firstInstallTime;

            return format.format(new Date(firstInstallTime));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String getData2(Context context) {
        String ret = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long lastUpdateTime = packageInfo.lastUpdateTime;

            return format.format(new Date(lastUpdateTime));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public String getDeviceData(String name) {

        String var1 = null;
        try {
            var1 = (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke((Object) null, name);
        } catch (Throwable var2) {
            AFLogger.afErrorLog(var2.getMessage(), var2);
        }

        return var1;
    }

    public String get_firstLaunchDate() {

        String ret = "";
        String format = "yyyy-MM-dd_HHmmssZ";
        SimpleDateFormat var47 = new SimpleDateFormat(format, Locale.US);
        if (Build.VERSION.SDK_INT >= 9) {

            ret = var47.format(new Date());
        }
        return ret;
    }

    //installDate
    public String get_installDate(Context context) {

        String format = "yyyy-MM-dd_HHmmssZ";
        SimpleDateFormat var47 = new SimpleDateFormat(format, Locale.US);
        String ret = null;

        if (Build.VERSION.SDK_INT >= 9) {
            try {
                //直接使用Android 自带的api 不再使用sp 去保存记录
                long var13 = context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0).firstInstallTime;
                var47.setTimeZone(TimeZone.getTimeZone("UTC"));
                ret = var47.format(new Date(var13));
                hashMap.put("installDate", ret);

            } catch (Exception var30) {
                AFLogger.afErrorLog("Exception while collecting install date. ", var30);
            }
        }
        return ret;
    }

    //android_id
    public static String getAndroidId(Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return ANDROID_ID;
    }

    //imei
    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            String imei = (String) tm.getClass().getMethod("getDeviceId").invoke(tm);
            return imei;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    //post
    public void post(String url, String json) {



        MainActivity.SLog.e("post : ",url+"\n"+json);

        OutputStreamWriter outputStreamWriter = null;
        HttpsURLConnection httpURLConnection = null;

        try {
            httpURLConnection = (HttpsURLConnection) new URL(url).openConnection();
            //设置ip授权认证：如果已经安装该证书，可以不设置，否则需要设置
            /*httpURLConnection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });*/
            httpURLConnection.setRequestMethod("POST");
            int length = json.getBytes().length;
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(length));
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setDoOutput(true);
            //  httpURLConnection.setInstanceFollowRedirects(false); //关闭重定向
            //httpURLConnection.setRequestProperty("Connection","Keep-Alive");


            //httpURLConnection.setRequestProperty("Kept Alive","No");
            //httpURLConnection.setRequestProperty("Client Address","10.200.10.184:42464");
            //httpURLConnection.setRequestProperty("");


            try {
                outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
                outputStreamWriter.write(json);
                //outputStreamWriter.flush();
            }finally {

                if(null != outputStreamWriter){

                    outputStreamWriter.close();
                }
            }

            int responseCode = httpURLConnection.getResponseCode();
            MainActivity.SLog.e("code: ",String.valueOf(responseCode));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*try {
                if (null != outputStreamWriter)
                    outputStreamWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            if(null != httpURLConnection){
                httpURLConnection.disconnect();
            }
        }
    }

    //获取refer值
    public static void test() {
        String str = "https://app.appsflyer.com/com.wifiup?c=12344&af_siteid=werr3&pid=124&af_click_lookback=13d&clickid=3980800333050374703&android_id=c05cb39adca5b093&advertising_id=02b3dbb7-9fe1-44c3-bc3a-6b65c68ef706&imei=869897036845354\n";
        ByteArrayOutputStream baos = null;
        InputStream is = null;
        try {
            URL url = new URL(str);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setInstanceFollowRedirects(false);

            if (200 == urlConnection.getResponseCode()) {
                //得到输入流
                is = urlConnection.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                Log.e("SYM", baos.toString("utf-8"));
            } else {
                Log.e("SYM", urlConnection.getResponseCode() + " ++++ ");
                //得到输入流
                is = urlConnection.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = is.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                Log.e("SYM", "ddd " + urlConnection.getHeaderField("Location") + " || " + baos.toString("utf-8"));
                String location = urlConnection.getHeaderField("Location");
                URL serverUrl = new URL(location);
                HttpURLConnection urlConnection1 = (HttpURLConnection) serverUrl.openConnection();
                urlConnection1.setRequestMethod("GET");
                urlConnection1.connect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public final static String testJson = "{\n" +
            "\t\"country\": \"US\",\n" +
            "\t\"af_timestamp\": \"1529372427985\",\n" +
            "\t\"appsflyerKey\": \"WpSoFapu6Pwg8Z5kcJrN2b\",\n" +
            "\t\"af_events_api\": \"1\",\n" +
            "\t\"isFirstCall\": \"false\",\n" +
            "\t\"registeredUninstall\": false,\n" +
            "\t\"operator\": \"\",\n" +
            "\t\"network\": \"WIFI\",\n" +
            "\t\"timepassedsincelastlaunch\": \"14\",\n" +
            "\t\"af_v2\": \"f94e4b7fd4fd3f44d115be32e2811dbcdc3b6bbe\",\n" +
            "\t\"uid\": \"1529294447612-3396698917248139127\",\n" +
            "\t\"isGaidWithGps\": \"false\",\n" +
            "\t\"lang_code\": \"en\",\n" +
            "\t\"installDate\": \"2018-06-18_040046+0000\",\n" +
            "\t\"app_version_code\": \"1\",\n" +
            "\t\"firstLaunchDate\": \"2018-06-18_040047+0000\",\n" +
            "\t\"model\": \"ONEPLUS A6000\",\n" +
            "\t\"lang\": \"English\",\n" +
            "\t\"brand\": \"OnePlus\",\n" +
            "\t\"deviceType\": \"user\",\n" +
            "\t\"product\": \"OnePlus6\",\n" +
            "\t\"deviceData\": {\n" +
            "\t\t\"cpu_abi\": \"arm64-v8a\",\n" +
            "\t\t\"build_display_id\": \"ONEPLUS A6000_22_180517\",\n" +
            "\t\t\"btch\": \"usb\",\n" +
            "\t\t\"dim\": {\n" +
            "\t\t\t\"size\": \"2\",\n" +
            "\t\t\t\"ydp\": \"399.393\",\n" +
            "\t\t\t\"xdp\": \"403.411\",\n" +
            "\t\t\t\"y_px\": \"2154\",\n" +
            "\t\t\t\"x_px\": \"1080\",\n" +
            "\t\t\t\"d_dpi\": \"420\"\n" +
            "\t\t},\n" +
            "\t\t\"arch\": \"\",\n" +
            "\t\t\"btl\": \"93.0\",\n" +
            "\t\t\"cpu_abi2\": \"\"\n" +
            "\t},\n" +
            "\t\"date2\": \"2018-06-19_014012+0000\",\n" +
            "\t\"counter\": \"28\",\n" +
            "\t\"date1\": \"2018-06-18_040046+0000\",\n" +
            "\t\"advertiserId\": \"02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704\",\n" +
            "\t\"advertiserIdEnabled\": \"true\",\n" +
            "\t\"af_v\": \"abe27a0367bb7fc053a42e476f30eb63ab1106f9\",\n" +
            "\t\"carrier\": \"\",\n" +
            "\t\"af_preinstalled\": \"false\",\n" +
            "\t\"iaecounter\": \"0\",\n" +
            "\t\"tokenRefreshConfigured\": false,\n" +
            "\t\"sdk\": \"27\",\n" +
            "\t\"gaidError\": \"-1: ClassNotFoundException\",\n" +
            "\t\"app_version_name\": \"1.0\",\n" +
            "\t\"device\": \"OnePlus6\",\n" +
            "\t\"platformextension\": \"android_native\",\n" +
            "\t\"cksm_v1\": \"8de5f2b408e2a7ce0f3e14b56a391f15eb\"\n" +
            "}";

}
