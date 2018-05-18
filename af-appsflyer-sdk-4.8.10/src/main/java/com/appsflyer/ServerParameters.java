package com.appsflyer;

/**
 * Created with IntelliJ IDEA.
 * User: gilmeroz
 * Date: 3/4/14
 * Time: 5:11 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ServerParameters {
    // NOTE:
    //  constants are explicitly declared as 'static final' for best practice,
    //  as this interface might be converted to an abstract class.

    private ServerParameters() {
    }

    public static final String DEFAULT_HOST = "appsflyer.com";
    static final String TIMESTAMP = "af_timestamp";
    static final String AF_DEV_KEY = "appsflyerKey";
    static final String AF_USER_ID = "uid";
    static final String CHANNEL_SERVER_PARAM = "channel";
    static final String LATEST_CHANNEL_SERVER_PARAM = "af_latestchannel";  // Not used in RTA team
    static final String EVENT_NAME = "eventName";
    static final String EVENT_VALUE = "eventValue";
    static final String INSTALL_STORE = "af_installstore";
    static final String CURRENT_STORE = "af_currentstore";
    static final String DEEP_LINK = "af_deeplink";
    static final String PRE_INSTALL_NAME = "af_preinstall_name";
    public static final String ADVERTISING_ID_PARAM = "advertiserId";
    static final String ADVERTISING_ID_ENABLED_PARAM = "advertiserIdEnabled";
    static final String APP_ID = "app_id";
    static final String DEV_KEY = "devkey";
    static final String TIME_SPENT_IN_APP = "time_in_app";
    static final String STATUS_TYPE = "statType";
    static final String PLATFORM = "platform";
    static final String LAUNCH_COUNTER = "launch_counter";
    static final String TIME_PASSED_SINCE_LAST_LAUNCH = "timepassedsincelastlaunch";
    static final String ANDROID_ID = "android_id";
    static final String CONVERSION_DATA_TIMING = "gcd_conversion_data_timing";
    static final String AF_GCM_TOKEN = "af_gcm_token";
    static final String GOOGLE_INSTANCE_ID = "af_google_instance_id";
    static final String DEVICE_CURRENT_BATTERY_LEVEL = "batteryLevel";
    static final String ONELINK_ID = "onelink_id";
    static final String ONELINK_VERSION = "ol_ver";
    static final String OTHER_SDKS = "af_sdks";
    static final String ADVERTISING_ID_WITH_GPS = "isGaidWithGps";
    static final String ORIGINAL_AF_UID = "originalAppsflyerId";
    static final String REINSTALL_COUNTER = "reinstallCounter";
    static final String DEVICE_TRACKING_DISABLED = "deviceTrackingDisabled";
    static final String AMAZON_AID = "amazon_aid";
    static final String AMAZON_AID_LIMIT = "amazon_aid_limit";
    static final String TOKEN_REFRESH_CONFIGURED = "tokenRefreshConfigured";
    static final String INSTALL_DATE = "installDate";
    static final String APP_NAME = "app_name";
    static final String APP_VERSION_NAME = "app_version_name";
    static final String APP_VERSION_CODE = "app_version_code";
    static final String APP_USER_ID = "appUserId";
    static final String MODEL = "model";
    static final String BRAND = "brand";
    static final String ANDROID_SDK_INT = "sdk";
    static final String REGISTERED_TO_UNINSTALL = "registeredUninstall";
    static final String PLATFORM_EXTENSION = "platformextension";
    static final String DEVICE_DATA = "deviceData";
    static final String LANG = "lang";
    static final String LANG_CODE = "lang_code";
    static final String COUNTRY = "country";
    // Location keys
    static final String LOCATION_KEY = "loc";
    static final String LAT_KEY = "lat";
    static final String LON_KEY = "lon";
    static final String TIMESTAMP_KEY = "ts";
    // Battery keys
    static final String BATTERY_LEVEL_KEY = "btl";
    static final String BATTERY_CHARGING_KEY = "btch";
    // Network keys
    static final String NETWORK = "network";
    static final String OPERATOR = "operator";
    static final String CARRIER = "carrier";
    static final String CHECK_SUM1 = "cksm_v1";
    static final String IS_STOP_TRACKING_USED = "istu";


    // Display params
    static final String PIXELS_X = "x_px";
    static final String PIXELS_Y = "y_px";
    static final String SCREEN_SIZE = "in";
    static final String X_DPI = "xdp";
    static final String Y_DPI = "ydp";

}

