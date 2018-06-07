package com.appsflyer.androidsampleapp;

import android.content.Context;
import android.util.Log;

/**
 * Created on 2018/6/5.
 *
 * @author sym
 */

public class Hook {
    public static void hookMethod(Context context,String content){

        SLog.e("SYM+++","hook method "+content);
        backup(context,content);
    }

    public static void backup(Context context,String content){

        SLog.e("SYM+++","backup method "+content);
    }

    static class SLog {
        private static final String TAG = "validate";

        public static void e(String key, String value) {
            Log.e(TAG, key + " : " + value);
        }
    }
}
