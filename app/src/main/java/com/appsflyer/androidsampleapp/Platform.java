package com.appsflyer.androidsampleapp;

final public class Platform {

    /**
     * 获取集成平台
     * @return
     */
    public String getPlatformExtension() {

        extension[] values = extension.values();
        int length = values.length;
        for (int i = 0; i < length; ++i) {
            if (isFound(values[i].clazz)) {
                return values[i].extensionName;
            }
        }

        return extension.android_native.extensionName;
    }

    private final boolean isFound(String clazz) {

        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    static enum extension {

        android_unity("android_unity", "com.unity3d.player.UnityPlayer"),
        android_reactNative("android_reactNative", "com.facebook.react.ReactApplication"),
        android_cordova("android_cordova", "org.apache.cordova.CordovaActivity"),
        android_segment("android_segment", "com.segment.analytics.integrations.Integration"),
        android_cocos2dx("android_cocos2dx", "org.cocos2dx.lib.Cocos2dxActivity"),
        android_native("android_native", "android_native");

        private String extensionName;
        private String clazz;

        private extension(String extensionName, String clazz) {
            this.extensionName = extensionName;
            this.clazz = clazz;
        }
    }
}
