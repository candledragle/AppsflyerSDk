package com.appsflyer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets you access device sensors via device {@link SensorManager}.
 */
final class AFSensorManager {
    static final String SENSORS_KEY = "sensors";
    static final int LAUNCH_COUNTER = 2;
    private static final long SCAN_DURATION_MILLIS = 500L;
    private static final long SCAN_DELAY_MILLIS = 30L * 60L * 1000L;
    private static final BitSet SENSOR_TYPES = new BitSet(6);
    private static final Handler SCHEDULING_HANDLER = new Handler(Looper.getMainLooper());
    private static volatile AFSensorManager sInstance;

    static {
        SENSOR_TYPES.set(Sensor.TYPE_ACCELEROMETER);
        SENSOR_TYPES.set(Sensor.TYPE_MAGNETIC_FIELD);
        SENSOR_TYPES.set(Sensor.TYPE_GYROSCOPE);
    }

    final Handler mSchedulingHandler;
    final Object mLock = new Object();
    private final Map<AFSensorEventCollector, AFSensorEventCollector> mListeners = new HashMap<>(SENSOR_TYPES.size());
    private final Map<AFSensorEventCollector, Map<String, Object>> mSensorsData = new HashMap<>(SENSOR_TYPES.size());
    private final SensorManager mSensorManager;
    boolean mStarted;
    private boolean mScanning;
    final Runnable mUnregisterRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                unregisterListeners();
                mSchedulingHandler.postDelayed(mRegisterRunnable, SCAN_DELAY_MILLIS);
            }
        }
    };
    final Runnable mRegisterRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                registerListeners();
                mSchedulingHandler.postDelayed(mUnregisterRunnable, SCAN_DURATION_MILLIS);
                mStarted = true;
            }
        }
    };
    final Runnable mStopStartedTrackingRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                if (mStarted) {
                    // Avoids battery usage by holding old runnables
                    mSchedulingHandler.removeCallbacks(mRegisterRunnable);
                    mSchedulingHandler.removeCallbacks(mUnregisterRunnable);
                    // If listeners where registered last time.
                    unregisterListeners();
                    mStarted = false;
                }
            }
        }
    };

    private AFSensorManager(@NonNull SensorManager sensorManager, Handler schedulingHandler) {
        mSensorManager = sensorManager;
        mSchedulingHandler = schedulingHandler;
    }

    /**
     * @param context Context to get the {@link SensorManager} from.
     */
    static AFSensorManager getInstance(Context context) {
        SensorManager systemService = (SensorManager) context.getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        return getInstance(systemService, SCHEDULING_HANDLER);
    }

    /**
     * @param sensorManager     {@link SensorManager} to get sensor updates.
     * @param schedulingHandler linked to the thread where {@link #registerListeners()} method
     *                          will be run.
     */
    static AFSensorManager getInstance(SensorManager sensorManager, Handler schedulingHandler) {
        if (null == sInstance) {
            synchronized (AFSensorManager.class) {
                if (null == sInstance) {
                    sInstance = newInstance(sensorManager, schedulingHandler);
                }
            }
        }
        return sInstance;
    }

    static AFSensorManager newInstance(SensorManager sensorManager, Handler schedulingHandler) {
        return new AFSensorManager(sensorManager, schedulingHandler);
    }

    /**
     * Checks if this sensor type is in the list of sensors we are looking for.
     *
     * @param type Type of the sensor from the {@link Sensor} class constants.
     * @return True if sensor is in the list.
     */
    private static boolean isValid(int type) {
        return 0 <= type && SENSOR_TYPES.get(type);
    }

    /**
     * Starts gathering sensor information. All previously started scanning will be canceled.
     * Call {@link #stopStartedTracking()} when you are done.
     */
    void startNewTracking() {
        // Calls this in case user forgot to call stop previously
        mSchedulingHandler.post(mStopStartedTrackingRunnable);
        // Changes started state after call to stop tracking, not before
        mSchedulingHandler.post(mRegisterRunnable);
    }

    /**
     * Stops periodic gathering sensor information if it was previously started.
     */
    void stopStartedTracking() {
        mSchedulingHandler.post(mStopStartedTrackingRunnable);
    }

    /**
     * For internal usage.
     */
    void registerListeners() {
        try {
            List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            // Modify mListeners map here
            for (Sensor sensor : sensorList) {
                if (isValid(sensor.getType())) {
                    AFSensorEventCollector key = AFSensorEventCollector.newInstance(sensor);
                    if (!mListeners.containsKey(key)) {
                        mListeners.put(key, key);
                    }
                    SensorEventListener listener = mListeners.get(key);
                    mSensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
            }
        } catch (Throwable ignore) {
            // This should never happen
        }
        mScanning = true;
    }

    /**
     * For internal usage. Unregisters all event collectors and moves data from the search
     * results to the cache. Empty search results should not override existing data.
     */
    void unregisterListeners() {
        try {
            if (!mListeners.isEmpty()) {
                // Stops gathering sensor information
                for (AFSensorEventCollector listener : mListeners.values()) {
                    mSensorManager.unregisterListener(listener);
                    listener.moveDataTo(mSensorsData);
                }
            }
        } catch (Throwable ignore) {
            // In case of device-specific error
        }
        mScanning = false;
    }

    /**
     * @return List of sensor information. If at least  one scan was made - data with sensor
     * information will be available. Changes to lists' content (map or map values mutations)
     * will be reflected in the source. Use them for the read operations only.
     */
    @NonNull
    List<Map<String, Object>> getData() {
        synchronized (mLock) {
            // If we are in scanning process - temp values will be received
            if (!mListeners.isEmpty() && mScanning) {
                for (AFSensorEventCollector listener : mListeners.values()) {
                    listener.copyDataTo(mSensorsData);
                }
            }
            if (mSensorsData.isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(mSensorsData.values());
        }
    }

    /**
     * Is exposed For testing purpose.
     *
     * @return True if scan was scheduled. False if all schedulers were removed and listeners
     * were un-registered, in this case further un-registering will not be done.
     */
    boolean isStarted() {
        return mStarted;
    }
}
