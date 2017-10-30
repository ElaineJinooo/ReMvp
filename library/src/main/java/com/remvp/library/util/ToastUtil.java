package com.remvp.library.util;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import com.remvp.library.view.CustomToast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * toast工具类
 * 防止同时弹出多个toast，现只显示最后一个toast内容
 */
public class ToastUtil {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    private static Object toast;
    private static int checkNotification = -1;

    private static void toast(Context ctx, CharSequence msg, int duration) {
        try {
            if (checkNotification == -1) {
                checkNotification = isNotificationEnabled(ctx) ? 0 : 1;
            }

            if (checkNotification == 1 && ctx instanceof Activity) {
                toast = CustomToast.makeText(ctx, msg, duration);
            } else {
                if (toast == null) {
                    toast = Toast.makeText(ctx.getApplicationContext(), msg, duration);
                } else {
                    if (((Toast) toast).getView() == null) {
                        toast = null;
                        toast(ctx, msg, duration);
                        return;
                    }
                    ((Toast) toast).setText(msg);
                    ((Toast) toast).setDuration(duration);
                }
            }
            if (toast instanceof CustomToast) {
                ((CustomToast) toast).show();
            } else if (toast instanceof Toast) {
                ((Toast) toast).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 弹出Toast
     *
     * @param ctx      弹出Toast的上下文
     * @param msg      弹出Toast的内容
     * @param duration 弹出Toast的持续时间
     */
    public static void show(Context ctx, CharSequence msg, int duration) {
        if (null == ctx) {
            return;
        }
        toast(ctx, msg, duration);
    }

    /**
     * 弹出Toast
     *
     * @param ctx      弹出Toast的上下文
     * @param msg      弹出Toast的内容
     */
    public static void show(Context ctx, CharSequence msg) {
        if (null == ctx) {
            return;
        }
        toast(ctx, msg, Toast.LENGTH_SHORT);
    }

    /**
     * 弹出Toast
     *
     * @param ctx      弹出Toast的上下文
     * @param resId    弹出Toast的内容的资源ID
     * @param duration 弹出Toast的持续时间
     */
    public static void show(Context ctx, int resId, int duration) {
        if (null == ctx) {
            return;
        }
        toast(ctx, ctx.getResources().getString(resId), duration);
    }

    /**
     * 弹出Toast
     *
     * @param ctx   弹出Toast的上下文
     * @param resId 弹出Toast的内容的资源ID
     */
    public static void show(Context ctx, int resId)
            throws NullPointerException {
        toast(ctx, ctx.getResources().getString(resId), Toast.LENGTH_SHORT);
    }

    /**
     * 用来判断是否开启通知权限
     */
    private static boolean isNotificationEnabled(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return true;
        }
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {
            appOpsClass = Class.forName(AppOpsManager.class.getName());
            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW,
                    Integer.TYPE, Integer.TYPE, String.class);
            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg)
                    == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
