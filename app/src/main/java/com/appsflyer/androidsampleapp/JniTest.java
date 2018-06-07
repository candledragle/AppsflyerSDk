package com.appsflyer.androidsampleapp;

/**
 * Created on 2018/6/6.
 *
 * @author sym
 */

public class JniTest {
    static {
        System.loadLibrary("jni-test");
    }

    public static void main(String args[]){
        JniTest jniTest = new JniTest();

    }

    public native String get();

    public native void set(String str);

}
