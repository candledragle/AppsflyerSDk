package com.appsflyer;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

final class AFLocationManager {
    private static final long SIGNIFICANT_TIME_LAG = DateUtils.MINUTE_IN_MILLIS;

    public static AFLocationManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * @return Last best known location. If application has no permissions
     * {@link Manifest.permission#ACCESS_COARSE_LOCATION}, {@link Manifest.permission#ACCESS_FINE_LOCATION}
     * returns null.
     */
    @Nullable
    Location getData(@NonNull Context context) {
        Location result = null;
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = getLastKnownLocation(context, locationManager);

            if (lastKnownLocation != null) {
                result = lastKnownLocation;
            }
        } catch (Throwable ignore) {
            // If the provider or location manager is absent
        }
        return result;
    }

    @Nullable
    private Location getLastKnownLocation(Context context, LocationManager locationManager) {
        Location networkLocation = getLastKnownLocationIfGranted(context, locationManager, NETWORK_PROVIDER,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        Location gpsLocation = getLastKnownLocationIfGranted(context, locationManager, GPS_PROVIDER,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return chooseBestLocation(networkLocation, gpsLocation);
    }


    /**
     * Choose best location based on timestamp and accuracy.
     *
     * @param network - Network location
     * @param gps     - GPS location
     * @return null if both locations are null. The most fresh location otherwise.
     */
    @Nullable
    private Location chooseBestLocation(Location network, Location gps) {
        // No other actions available with it
        if (gps == null && network == null) {
            return null;
        }
        // Only network location available
        if (gps == null && network != null) {
            return network;
        }
        // Only gps location available
        if (network == null && gps != null) {
            return gps;
        }

        // Both are not null. Compare the timestamp.
        long timeLag = network.getTime() - gps.getTime();
        if (SIGNIFICANT_TIME_LAG < timeLag) {
            return network;
        }
        return gps;
    }

    @Nullable
    @SuppressWarnings("MissingPermission")
    private Location getLastKnownLocationIfGranted(@NonNull Context context, @NonNull LocationManager locationManager, @NonNull String locationProvider, @NonNull String... permissions) {
        if (anyGranted(context, permissions)) {
            return locationManager.getLastKnownLocation(locationProvider);
        }
        return null;
    }

    private static boolean anyGranted(@NonNull Context context, @NonNull String[] permissions) {
        for (String permission : permissions) {
            if (AndroidUtils.isPermissionAvailable(context, permission)) {
                return true;
            }
        }
        return false;
    }

    private static final class InstanceHolder {
        static final AFLocationManager INSTANCE = new AFLocationManager();
    }
}
