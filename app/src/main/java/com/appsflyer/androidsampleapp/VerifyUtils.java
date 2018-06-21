package com.appsflyer.androidsampleapp;

import android.app.Application;
import android.util.Log;

import com.wifiup.R;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * 工具类为了检查文件的完整性，防止低级的反编译
 *
 * Created on 2018/6/5.
 *
 * @author sym
 */

public class VerifyUtils {

    private static boolean DEBUG = true;
    private static final String TAG = "VerifyUtils";

    /**
     * 校验dex文件的完整性
     * 根据crc值
     *
     * @return
     */
    public static boolean verifyDex(Application application){

        //获取dex文件的crc值
        long dexCrc = Long.parseLong(application.getString(R.string.crc_value));
        String apkPath = application.getPackageCodePath();
        if(DEBUG){
            Log.e(TAG,"apkPath is "+apkPath);
        }
        try {
            ZipFile zipFile = new ZipFile(apkPath);
            ZipEntry dexEntry = zipFile.getEntry("classes.dex");
            //计算classes.dex 的crc值
            long crc = dexEntry.getCrc();
            //对比
            if(crc == dexCrc){
                if(DEBUG){
                    Log.e(TAG,"dex文件校验通过");
                }
                return true;
            }else{
                if(DEBUG){
                    Log.e(TAG,"dex文件被修改了！");
                }
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
