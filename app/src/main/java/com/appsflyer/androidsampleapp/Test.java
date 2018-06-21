package com.appsflyer.androidsampleapp;

import android.util.Log;

/**
 * Created on 2018/6/5.
 *
 * @author sym
 */

public class Test {

    public static void sayHello(String content) {
        Log.e("SYM", content);
    }

    public static void methodCalledByJni(String msgFromJni){
        Log.e("SYM","methodCalledByJni,msg:" +msgFromJni);
    }
}
