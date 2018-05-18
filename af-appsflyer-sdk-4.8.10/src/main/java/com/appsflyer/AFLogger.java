package com.appsflyer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author by golan on 12/10/15.
 */


public class AFLogger {

    private static final String LOG_TAG = LogMessages.LOG_TAG_PREFIX + AppsFlyerLib.BUILD_NUMBER;

    private static long starTime = System.currentTimeMillis();

    public enum LogLevel {

        NONE(0), ERROR(1), WARNING(2), INFO(3), DEBUG(4), VERBOSE(5); //VERBOSE = RD (for proxy purposes)

        private int level;

        LogLevel(int s) {
            level = s;
        }

        public int getLevel() {
            return level;
        }
    }

    public static void afInfoLog(String logMessage, boolean shouldRemoteDebug) {
        if (shouldLog(LogLevel.INFO)) {
            Log.i(LOG_TAG, getMessage(logMessage));
        }
        if (shouldRemoteDebug) {
            ProxyManager.getInstance().addLogEntry("I", getMessage(logMessage, true));
        }
    }

    public static void resetDeltaTime(){
        starTime = System.currentTimeMillis();
    }

    @NonNull
    private static String getMessage(String logMessage) {
        return getMessage(logMessage, false);
    }

    @NonNull
    private static String getMessage(String logMessage, boolean forceTimerDelta) {

        if(forceTimerDelta || LogLevel.VERBOSE.getLevel() == AppsFlyerProperties.getInstance().getLogLevel()){
            return "(" + timeString(System.currentTimeMillis() - starTime) + ") " + logMessage;
        }
        return logMessage;
    }


    static void afDebugLog(String debugLogMessage, boolean shouldRemoteDebug) {
        if (shouldLog(LogLevel.DEBUG)) {
            Log.d(LOG_TAG, getMessage(debugLogMessage));
        }
        if (shouldRemoteDebug) {
            ProxyManager.getInstance().addLogEntry("D", getMessage(debugLogMessage, true));
        }
    }


    static void afErrorLog(String errorLogMessage, Throwable ex, boolean shouldRemoteDebug, boolean shouldOutputToLog) {
        if (shouldLog(LogLevel.ERROR) && shouldOutputToLog) {
            Log.e(LOG_TAG, getMessage(errorLogMessage), ex);
        }
        if (shouldRemoteDebug) {
            ProxyManager.getInstance().addExceptionEvent(ex);
        }
    }

    static void afWarnLog(String warningLogMessage, boolean shouldRemoteDebug) {
        if (shouldLog(LogLevel.WARNING)) {
            Log.w(LOG_TAG, getMessage(warningLogMessage));
        }
        if (shouldRemoteDebug) {
            ProxyManager.getInstance().addLogEntry("W", getMessage(warningLogMessage, true));
        }
    }

    public static void afRDLog(String rdLogMessage) {
        if (shouldLog(LogLevel.VERBOSE)) {
            Log.v(LOG_TAG, getMessage(rdLogMessage));
        }

        ProxyManager.getInstance().addLogEntry("V", getMessage(rdLogMessage, true));
    }

    private static boolean shouldLog(LogLevel level) {
        return level.getLevel() <= AppsFlyerProperties.getInstance().getLogLevel();
    }

    static void afLogForce(String logMessage) {
        if (!noLogsAllowed()) {
            Log.d(LOG_TAG, getMessage(logMessage));
        }
        ProxyManager.getInstance().addLogEntry("F", logMessage);
    }

    private static boolean noLogsAllowed() {
        return AppsFlyerProperties.getInstance().isLogsDisabledCompletely();
    }

    public static void afDebugLog(String debugLogMessage) {
        afDebugLog(debugLogMessage, true);
    }

    public static void afInfoLog(String logMessage) {
        afInfoLog(logMessage, true);
    }

    public static void afErrorLog(String errorLogMessage, Throwable ex) {
        afErrorLog(errorLogMessage, ex, true, false);
    }

    public static void afErrorLog(String errorLogMessage, Throwable ex, boolean shouldOutputToLog) {
        afErrorLog(errorLogMessage, ex, true, shouldOutputToLog);
    }

    public static void afWarnLog(String warningLogMessage) {
        afWarnLog(warningLogMessage, true);
    }


    static String timeString(long gap) {
        long hours = TimeUnit.MILLISECONDS.toHours(gap);
        gap -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(gap);
        gap -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(gap);
        gap -= TimeUnit.SECONDS.toMillis(seconds);
        long milli = TimeUnit.MILLISECONDS.toMillis(gap);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d:%03d", hours, minutes, seconds, milli);
    }
}
