package com.appsflyer;

/**
 * Available platform extension handler
 *
 * @author Oleksandr Dudinskyi oleksandr.d@appsflyer.com
 */

class PlatformExtension {
    /**
     * Common base class with available platforms
     */
    enum Platform {
        UNITY("android_unity", "com.unity3d.player.UnityPlayer"),
        REACT_NATIVE("android_reactNative", "com.facebook.react.ReactApplication"),
        CORDOVA("android_cordova", "org.apache.cordova.CordovaActivity"),
        SEGMENT("android_segment", "com.segment.analytics.integrations.Integration"),
        COCOS2DX("android_cocos2dx", "org.cocos2dx.lib.Cocos2dxActivity"),
        DEFAULT("android_native", "android_native");

        private String platformName;
        private String classLoaderName;

        Platform(String platformName, String classLoaderName) {
            this.platformName = platformName;
            this.classLoaderName = classLoaderName;
        }

        String getClassLoaderName() {
            return classLoaderName;
        }

        public String getPlatformName() {
            return platformName;
        }
    }

    private ClassLoadingBehaviorInterface loadingStrategy;

    /**
     * Get platform extension that is in use
     *
     * @return platform extension name
     */
    String getAvailablePlatformExtension() {

        for (Platform platform : Platform.values()) {
            if (isClassExist(platform.classLoaderName)) {
                return platform.platformName;
            }
        }
        return Platform.DEFAULT.platformName;
    }

    PlatformExtension() {
        this.loadingStrategy = new ClassLoadingBehaviorInterface() {
            @Override
            public Class<?> forName(String name) throws ClassNotFoundException {
                return Class.forName(name);
            }
        };
    }

    PlatformExtension(ClassLoadingBehaviorInterface loadingStrategy) {
        this.loadingStrategy = loadingStrategy;
    }

    boolean isClassExist(String className) {
        try {
            loadingStrategy.forName(className);
            AFLogger.afRDLog("Class: " + className + " is found.");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Throwable e) {
            AFLogger.afErrorLog(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Strategy to checking if particular class exist based by class name,
     * replace with mock implementation for testing
     */
    interface ClassLoadingBehaviorInterface {

        Class<?> forName(final String name) throws ClassNotFoundException;
    }
}
