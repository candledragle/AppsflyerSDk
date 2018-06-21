package com.example.testlib;

public class Json {

    public static final String Right = "{\n" +
            "\t\"country\": \"US\",\n" +
            "\t\"af_timestamp\": \"1529375339703\",\n" +
            "\t\"appsflyerKey\": \"WpSoFapu6Pwg8Z5kcJrN2b\",\n" +
            "\t\"af_events_api\": \"1\",\n" +
            "\t\"isFirstCall\": \"false\",\n" +
            "\t\"registeredUninstall\": false,\n" +
            "\t\"operator\": \"\",\n" +
            "\t\"network\": \"WIFI\",\n" +
            "\t\"timepassedsincelastlaunch\": \"8\",\n" +
            "\t\"af_v2\": \"720657d85ded8f2ed609395b06a878ca15ffd5ae\",\n" +
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
            "\t\t\"btch\": \"no\",\n" +
            "\t\t\"dim\": {\n" +
            "\t\t\t\"size\": \"2\",\n" +
            "\t\t\t\"ydp\": \"399.393\",\n" +
            "\t\t\t\"xdp\": \"403.411\",\n" +
            "\t\t\t\"y_px\": \"2154\",\n" +
            "\t\t\t\"x_px\": \"1080\",\n" +
            "\t\t\t\"d_dpi\": \"420\"\n" +
            "\t\t},\n" +
            "\t\t\"arch\": \"\",\n" +
            "\t\t\"btl\": \"100.0\",\n" +
            "\t\t\"cpu_abi2\": \"\"\n" +
            "\t},\n" +
            "\t\"date2\": \"2018-06-19_022858+0000\",\n" +
            "\t\"counter\": \"34\",\n" +
            "\t\"date1\": \"2018-06-18_040046+0000\",\n" +
            "\t\"advertiserId\": \"02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704\",\n" +
            "\t\"advertiserIdEnabled\": \"true\",\n" +
            "\t\"af_v\": \"3a21cd5053c9c19c189d2d497e18ad66e59f6d48\",\n" +
            "\t\"carrier\": \"\",\n" +
            "\t\"af_preinstalled\": \"false\",\n" +
            "\t\"iaecounter\": \"0\",\n" +
            "\t\"tokenRefreshConfigured\": false,\n" +
            "\t\"sdk\": \"27\",\n" +
            "\t\"gaidError\": \"-1: ClassNotFoundException\",\n" +
            "\t\"app_version_name\": \"1.0\",\n" +
            "\t\"device\": \"OnePlus6\",\n" +
            "\t\"platformextension\": \"android_native\",\n" +
            "\t\"cksm_v1\": \"1febefc394a9f1fbff700ac06c581f7ece\"\n" +
            "}";

    public static final String Bad = "{\n" +
            "\t\"country\": \"US\",\n" +
            "\t\"af_timestamp\": 1529375376871,\n" +
            "\t\"appsflyerKey\": \"WpSoFapu6Pwg8Z5kcJrN2b\",\n" +
            "\t\"af_events_api\": \"1\",\n" +
            "\t\"isFirstCall\": \"true\",\n" +
            "\t\"registeredUninstall\": \"false\",\n" +
            "\t\"operator\": \"\",\n" +
            "\t\"network\": \"WIFI\",\n" +
            "\t\"timepassedsincelastlaunch\": \"9\",\n" +
            "\t\"af_v2\": \"3f40b66448d40c46a73cf07133e874f2658f366d\",\n" +
            "\t\"uid\": \"1529375376871-3972403684656123643\",\n" +
            "\t\"isGaidWithGps\": \"false\",\n" +
            "\t\"lang_code\": \"en\",\n" +
            "\t\"installDate\": \"2018-06-18_040046+0000\",\n" +
            "\t\"app_version_code\": \"1\",\n" +
            "\t\"firstLaunchDate\": \"2018-06-18_222936-0400\",\n" +
            "\t\"model\": \"ONEPLUS A6000\",\n" +
            "\t\"lang\": \"English\",\n" +
            "\t\"brand\": \"OnePlus\",\n" +
            "\t\"deviceType\": \"user\",\n" +
            "\t\"product\": \"OnePlus6\",\n" +
            "\t\"deviceData\": {\n" +
            "\t\t\"cpu_abi\": \"arm64-v8a\",\n" +
            "\t\t\"build_display_id\": \"ONEPLUS A6000_22_180517\",\n" +
            "\t\t\"arch\": \"\",\n" +
            "\t\t\"cpu_abi2\": \"ro.product.cpu.abi2\",\n" +
            "\t\t\"btch\": \"no\",\n" +
            "\t\t\"btl\": \"94.0\",\n" +
            "\t\t\"dim\": {\n" +
            "\t\t\t\"size\": 2,\n" +
            "\t\t\t\"ydp\": \"399.393\",\n" +
            "\t\t\t\"xdp\": \"403.411\",\n" +
            "\t\t\t\"y_px\": \"2154\",\n" +
            "\t\t\t\"x_px\": \"1080\",\n" +
            "\t\t\t\"d_dpi\": \"420\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"date2\": \"2018-06-18_222858-0400\",\n" +
            "\t\"counter\": \"com.appsflyer.androidsampleapp.MainActivity@19c6a46\",\n" +
            "\t\"data1\": \"2018-06-18_000046-0400\",\n" +
            "\t\"advertiserId\": \"02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704\",\n" +
            "\t\"advertiserIdEnabled\": \"true\",\n" +
            "\t\"referrer\": \"af_tranid%3DMHoJkcIRElf9Or6sHgJ88A%26c%3D123%26af_siteid%3Dwerr%26pid%3D124%26af_click_lookback%3D1d%26clickid%3D2069849869957480259%26android_id%3Dc05cb39adca5b091%26advertising_id%3D02b3dbb7-9fe1-44c3-bc3a-6b65c68ef704%26imei%3D869897036845351\",\n" +
            "\t\"af_v\": \"20cdff6f5665e600417a1f64476a34e9fc931a83\",\n" +
            "\t\"carrier\": \"\",\n" +
            "\t\"af_preinstalled\": false,\n" +
            "\t\"iaecounter\": \"0\",\n" +
            "\t\"tokenRefreshConfigured\": false,\n" +
            "\t\"sdk\": \"27\",\n" +
            "\t\"gaidError\": \"\",\n" +
            "\t\"app_version_name\": \"1.0\",\n" +
            "\t\"device\": \"OnePlus6\",\n" +
            "\t\"platformextension\": \"android_native\",\n" +
            "\t\"cksm_v1\": \"466eafa40862b69c2f65a6360ce6af15cf\"\n" +
            "}";
}
