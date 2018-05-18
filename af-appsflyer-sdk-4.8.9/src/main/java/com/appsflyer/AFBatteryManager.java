package com.appsflyer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;

final class AFBatteryManager {
    static final String BATTERY_CHARGING_USB = "usb";
    static final String BATTERY_CHARGING_AC = "ac";
    static final String BATTERY_CHARGING_WIRELESS = "wireless";
    static final String BATTERY_CHARGING_OTHER = "other";
    static final String BATTERY_CHARGING_NO = "no";
    final IntentFilter mFilter;

    AFBatteryManager() {
        mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    static AFBatteryManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @param context Context to fetch system information.
     * @return Object containing current battery level and charging state.
     * Charging state could be one of the constants {@link #BATTERY_CHARGING_AC},
     * {@link #BATTERY_CHARGING_USB},  {@link #BATTERY_CHARGING_WIRELESS}. {@link #BATTERY_CHARGING_OTHER},
     * {@link #BATTERY_CHARGING_NO}.
     * If manager fails to register for battery update or battery information is missing returned
     * object will have null charging state and 0 charging level.
     */
    @NonNull
    BatteryData getData(Context context) {
        String chargingSource = null;
        float batteryPct = 0.0f;
        try {
            Intent batteryStatus = context.registerReceiver(null, mFilter);

            if (null != batteryStatus) {
                // Are we charging?
                boolean isCharging = BatteryManager.BATTERY_STATUS_CHARGING ==
                        batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                if (isCharging) {
                    // How are we charging?
                    int pluggedSource = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    switch (pluggedSource) {
                        case BatteryManager.BATTERY_PLUGGED_USB:
                            chargingSource = BATTERY_CHARGING_USB;
                            break;
                        case BatteryManager.BATTERY_PLUGGED_AC:
                            chargingSource = BATTERY_CHARGING_AC;
                            break;
                        case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                            chargingSource = BATTERY_CHARGING_WIRELESS;
                            break;
                        default:
                            chargingSource = BATTERY_CHARGING_OTHER;
                    }
                } else {
                    chargingSource = BATTERY_CHARGING_NO;
                }

                // Battery level
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                if (-1 != level && -1 != scale) {
                    batteryPct = 100.0f * level / scale;
                }
            }
        } catch (Throwable ignore) {
            // Device that failed to register receiver
        }
        return new BatteryData(batteryPct, chargingSource);
    }

    static final class BatteryData {
        private final float mLevel;
        private final String mCharging;

        BatteryData(float level, String charging) {
            mLevel = level;
            mCharging = charging;
        }

        float getLevel() {
            return mLevel;
        }

        String getCharging() {
            return mCharging;
        }
    }

    private static final class InstanceHolder {
        static final AFBatteryManager INSTANCE = new AFBatteryManager();
    }
}

