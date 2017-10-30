package com.remvp.library.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * 系统信息工具类
 */
public class SysInfoUtil {
    public static final String TAG = "SysInfoUtil";
    public static final String DISPLAY_SPLIT_STR = "x";

    /**
     * 获取手机屏幕分辨率
     *
     * @param context
     * @return 返回格式为 屏幕宽x屏幕高 的字符串，例如：480x800
     */
    public static String getScreen(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels + DISPLAY_SPLIT_STR + dm.heightPixels;
    }

    /**
     * 获取手机IMEI设备号
     *
     * @param context
     */
    public static String getIMEI(Context context) {
        String imei = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        return imei;
    }

    /**
     * 获取手机SIM卡的IMSI号
     *
     * @param context
     */
    public static String getIMSI(Context context) {
        String imsi = ((TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
        return imsi;
    }

    /**
     * 获取客户端软件版本名称信息
     *
     * @param context
     */
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = null;
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取客户端软件版本号信息
     *
     * @param context
     */
    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取手机androidSDK的版本名称，例如：2.2、2.3.5
     */
    public static String getSDKVersionName() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机androidSDK的版本号,例如：8，10
     */
    public static int getSDKVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机的厂商型号
     */
    public static String getProduct() {
        return Build.MODEL;
    }

    /**
     * 打电话
     *
     * @param phoneNum
     */
    protected void phoneCall(Context context, String phoneNum) {
        Intent phoneIntent = new Intent("android.intent.action.CALL",
                Uri.parse("tel:" + phoneNum));
        // 启动
        context.startActivity(phoneIntent);

    }
}
