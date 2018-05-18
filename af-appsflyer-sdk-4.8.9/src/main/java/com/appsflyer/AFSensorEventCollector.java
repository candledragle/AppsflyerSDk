package com.appsflyer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector of sensor values. May be used as a map key, because sensors' description is final.
 */
final class AFSensorEventCollector implements SensorEventListener {
    static final String SENSOR_TYPE_KEY = "sT";
    static final String SENSOR_NAME_KEY = "sN";
    static final String SENSOR_VENDOR_KEY = "sV";
    static final String SENSOR_EVENT_VALUES_START_KEY = "sVS";
    static final String SENSOR_EVENT_VALUES_END_KEY = "sVE";
    static final long NANOSEC_LAG = 50_000_000L;
    private static final String EMPTY_STRING = "";
    private final int mType;
    @NonNull
    private final String mName;
    @NonNull
    private final String mVendor;
    private final float[][] mValues = new float[2][];
    private final long[] mTimesMillis = new long[2];
    private final int mHash;
    private double mDistance;
    private long mLastTimeNano;

    private AFSensorEventCollector(int type, @Nullable String name, @Nullable String vendor) {
        mType = type;
        mName = emptyIfNull(name);
        mVendor = emptyIfNull(vendor);
        mHash = calculateHash(type, mName, mVendor);
    }

    @NonNull
    private static String emptyIfNull(String text) {
        return null == text ? EMPTY_STRING : text;
    }

    private static int calculateHash(int type, @NonNull String name, @NonNull String vendor) {
        int constant = 31;
        int result = constant + type;
        result = constant * result + name.hashCode();
        result = constant * result + vendor.hashCode();
        return result;
    }

    static AFSensorEventCollector newInstance(Sensor sensor) {
        return newInstance(sensor.getType(), sensor.getName(), sensor.getVendor());
    }

    static AFSensorEventCollector newInstance(int type, String name, String vendor) {
        return new AFSensorEventCollector(type, name, vendor);
    }

    /**
     * Finds distance to the other set of values. As other values could have different
     * dimension, consider min number of available values only.
     */
    private static double distanceBetween(@NonNull float[] values, @NonNull float[] other) {
        int size = Math.min(values.length, other.length);
        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum += StrictMath.pow(values[i] - other[i], 2.0);
        }
        return Math.sqrt(sum);
    }

    private static boolean isNotNull(Sensor sensor) {
        return null != sensor && null != sensor.getName() && null != sensor.getVendor();
    }

    @NonNull
    private static List<Float> toList(@NonNull float[] floats) {
        List<Float> array = new ArrayList<>(floats.length);
        for (float aStart : floats) {
            array.add(aStart);
        }
        return array;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (null != event && null != event.values && isNotNull(event.sensor)) {
            onSensorChanged(event.sensor.getType(), event.sensor.getName(),
                    event.sensor.getVendor(), event.timestamp, event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    /**
     * Updates internal values if the sensor has the same params as the listener.
     */
    void onSensorChanged(int type, @NonNull String name, @NonNull String vendor, long time, @NonNull float[] values) {
        if (isSameSensor(type, name, vendor)) {
            updateValues(time, values);
        }
    }

    /**
     * Updates given map with sensors' data if data is available. If sensors' data is empty, updates
     * map with sensors' description in case that map doesn't contain this key before.
     *
     * @param events Map to update
     */
    void moveDataTo(@NonNull Map<AFSensorEventCollector, Map<String, Object>> events) {
        pushDataTo(events, true);
    }

    public void copyDataTo(Map<AFSensorEventCollector, Map<String, Object>> events) {
        pushDataTo(events, false);
    }

    private void pushDataTo(@NonNull Map<AFSensorEventCollector, Map<String, Object>> events, boolean shouldClear) {
        if (hasData()) {
            events.put(this, getData());
            if (shouldClear) {
                clearData();
            }
        } else if (!events.containsKey(this)) {
            events.put(this, getData());
        }
    }

    private boolean isSameSensor(int type, @NonNull String name, @NonNull String vendor) {
        return mType == type &&
                mName.equals(name) &&
                mVendor.equals(vendor);
    }

    private void updateValues(long timestamp, @NonNull float[] values) {
        // Current device time is saved as event time since we need to have connection to the world clock
        long currentTimeMillis = System.currentTimeMillis();
        float[] start = mValues[0];
        if (null == start) {
            mValues[0] = Arrays.copyOf(values, values.length);
            mTimesMillis[0] = currentTimeMillis;
        } else {
            float[] end = mValues[1];
            if (null == end) {
                end = Arrays.copyOf(values, values.length);
                mValues[1] = end;
                mTimesMillis[1] = currentTimeMillis;
                mDistance = distanceBetween(start, end);
            } else {
                // Made assumption that later events will have bigger timestamp. This comparison
                // is used while scanning only
                if (NANOSEC_LAG <= timestamp - mLastTimeNano) {
                    mLastTimeNano = timestamp;
                    if (Arrays.equals(end, values)) {
                        // With new timestamp
                        mTimesMillis[1] = currentTimeMillis;
                    } else {
                        double newDistance = distanceBetween(start, values);
                        if (newDistance > mDistance) {
                            mValues[1] = Arrays.copyOf(values, values.length);
                            mTimesMillis[1] = currentTimeMillis;
                            mDistance = newDistance;
                        }
                    }
                }
            }
        }
    }

    /**
     * Is exposed for testing purpose.
     *
     * @return Sensor information available so far. Changes to any mutable map values will be
     * reflected in the source. If no sensor data is available, map will contain  descriptions
     * only ({@link #mType}, {@link #mName}, {@link #mVendor}).
     */
    @NonNull
    Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>(7);
        data.put(SENSOR_TYPE_KEY, mType);
        data.put(SENSOR_NAME_KEY, mName);
        data.put(SENSOR_VENDOR_KEY, mVendor);
        float[] start = mValues[0];
        if (null != start) {
            data.put(SENSOR_EVENT_VALUES_START_KEY, toList(start));
        }
        float[] end = mValues[1];
        if (null != end) {
            data.put(SENSOR_EVENT_VALUES_END_KEY, toList(end));
        }
        return data;
    }

    /**
     * Is exposed for testing purpose.
     * Resets collected values and make listener ready for the new collection cycle.
     */
    void clearData() {
        for (int i = 0, size = mValues.length; i < size; i++) {
            mValues[i] = null;
        }
        for (int i = 0, size = mTimesMillis.length; i < size; i++) {
            mTimesMillis[i] = 0L;
        }
        mDistance = 0.0;
        mLastTimeNano = 0L;
    }

    /**
     * Is exposed for testing purpose.
     *
     * @return True if this listener has collected values.
     */
    boolean hasData() {
        return null != mValues[0];
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    /**
     * Listeners are considered to be equal if they store the same sensor description
     * ({@link #mType}, {@link #mName}, {@link #mVendor}).
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AFSensorEventCollector) {
            AFSensorEventCollector other = (AFSensorEventCollector) obj;
            return isSameSensor(other.mType, other.mName, other.mVendor);
        }
        return false;
    }
}
