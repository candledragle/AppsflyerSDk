package com.appsflyer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;



public class AFScreenManager {

    // Display params
    private static final String PIXELS_X = "x_px";
    private static final String PIXELS_Y = "y_px";
    private static final String SCREEN_SIZE = "size";
    private static final String DENSITY_DPI = "d_dpi";
    private static final String X_DPI = "xdp";
    private static final String Y_DPI = "ydp";


    public static final String SCREEN_KEY = "dim";
    /**
     * Provide an access to screen metrics
     * @param context
     */
    public static Map<String,String> getScreenMetrics(@NonNull Context context){

        Map metricsResults = new HashMap<>();

        try {
            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wManager =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wManager.getDefaultDisplay().getMetrics(metrics);

            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            int screenLayout = configuration.screenLayout;

            /*
            * 1 - SMALL
            * 2 - MEDIUM
            * 3 - LARGE
            * 4 - XLARGE
            * */
            int screenSize = screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

            metricsResults.put(PIXELS_X, String.valueOf(metrics.widthPixels));
            metricsResults.put(PIXELS_Y, String.valueOf(metrics.heightPixels));
            metricsResults.put(DENSITY_DPI, String.valueOf(metrics.densityDpi));
            metricsResults.put(SCREEN_SIZE, String.valueOf(screenSize));
            metricsResults.put(X_DPI, String.valueOf(metrics.xdpi));
            metricsResults.put(Y_DPI, String.valueOf(metrics.ydpi));
        }catch (Throwable t) {
            AFLogger.afErrorLog("Couldn't aggregate screen stats: ",t);
        }
        return metricsResults;
    }

}
