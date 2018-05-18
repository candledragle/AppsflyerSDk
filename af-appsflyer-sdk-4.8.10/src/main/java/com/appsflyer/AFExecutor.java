package com.appsflyer;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author by shacharaharon on 21/03/2017.
 */

public class AFExecutor {

    private static final int CORE_POOL_SIZE = 2;
    private static AFExecutor instance;
    //TODO: avoid to use multiple executors. We can stay with one ScheduledExecutorService only
    private Executor afThreadPoolExecutor;
    private ScheduledExecutorService afScheduledThreadPoolExecutor;
    private Executor afSerialExecutor;

    private AFExecutor() {
    }

    public static AFExecutor getInstance() {
        if (instance == null) {
            instance = new AFExecutor();
        }
        return instance;
    }

    public Executor getSerialExecutor() {
        if (afSerialExecutor == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                afSerialExecutor = AsyncTask.SERIAL_EXECUTOR;
            } else {
                return Executors.newSingleThreadExecutor();
            }
        }
        return afSerialExecutor;
    }

    public Executor getThreadPoolExecutor() {

        boolean isExecutorTerminated = afThreadPoolExecutor == null ||
                (afThreadPoolExecutor instanceof ThreadPoolExecutor &&
                        (((ThreadPoolExecutor) afThreadPoolExecutor).isShutdown()
                                || ((ThreadPoolExecutor) afThreadPoolExecutor).isTerminated()
                                || ((ThreadPoolExecutor) afThreadPoolExecutor).isTerminating()));

        if (isExecutorTerminated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                afThreadPoolExecutor = Executors.newFixedThreadPool(CORE_POOL_SIZE);
            } else {
                return Executors.newSingleThreadExecutor();
            }
        }

        return afThreadPoolExecutor;
    }

    ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor() {

        boolean isExecutorTerminated =
                afScheduledThreadPoolExecutor == null
                        || afScheduledThreadPoolExecutor.isShutdown()
                        || afScheduledThreadPoolExecutor.isTerminated();

        if (isExecutorTerminated) {
            afScheduledThreadPoolExecutor = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
        }

        return (ScheduledThreadPoolExecutor) afScheduledThreadPoolExecutor;
    }

    void shutdownExecutors() {

        try {
            stopExecutorService(afScheduledThreadPoolExecutor);

            if (afThreadPoolExecutor instanceof ThreadPoolExecutor) {
                stopExecutorService((ThreadPoolExecutor) afThreadPoolExecutor);
            }
        } catch (Throwable ex) {
            AFLogger.afErrorLog("failed to stop Executors", ex);
        }
    }

    /**
     * This method stops executor with shutdown a.e. blocks executor to receive new job,
     * waits for 10 sec and tries to stop it ASAP a.e. best-effort attempts to stop
     * processing actively executing tasks
     * @param executor - Executor that should be stopped gracefully.
     */
    private void stopExecutorService(ExecutorService executor) {
        try {
            AFLogger.afRDLog("shut downing executor ...");
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            AFLogger.afRDLog("InterruptedException!!!");

        } finally {
            if (!executor.isTerminated()) {
                AFLogger.afRDLog("killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }
}
