package com.remvp.library.util;

import android.app.Activity;
import android.widget.Toast;

import com.remvp.library.view.DialogEh;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

/**
 * 权限检查工具类
 */
public class PermissionUtil {
    Activity context;
    PerListener listener;
    private String[] permission;
    private boolean isToast = true;

    /**
     * 构造函数，new时会检查一次
     *
     * @param context    activity
     * @param listener   监听器
     * @param permission 权限 可以是一个或多个
     */
    public PermissionUtil(Activity context, PerListener listener, final String... permission) {
        this(context, listener, true, permission);
    }

    /**
     * @param context    activity
     * @param listener   监听器
     * @param isToast    授权失败时是否显示toast
     * @param permission 权限 可以是一个或多个
     */
    public PermissionUtil(Activity context, PerListener listener, boolean isToast, String[] permission) {
        this.context = context;
        this.listener = listener;
        this.permission = permission;
        this.isToast = isToast;
        checkPermission();
    }

    /**
     * 设置授权失败是否toast
     *
     * @param toast 是否toast，默认为true
     */
    public void setToast(boolean toast) {
        isToast = toast;
    }

    /**
     * 检查权限
     */
    public void checkPermission() {
        if (context == null || listener == null || permission == null) {
            return;
        }
        if (permission.length == 1) {
            Dexter.withActivity(context)
                    .withPermission(permission[0])
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            listener.onPermissionSuccess();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            toast(response.getPermissionName());
                            listener.onPermissionFail();
                        }


                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                       final PermissionToken token) {
                            continueRequestPermission(token);
                        }
                    }).check();
        } else {
            Dexter.withActivity(context)
                    .withPermissions(permission)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.getGrantedPermissionResponses().size() == permission.length) {
                                listener.onPermissionSuccess();
                                return;
                            }
                            for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                                toast(response.getPermissionName());
                            }
                            listener.onPermissionFail();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            continueRequestPermission(token);
                        }
                    }).check();
        }
    }

    private void toast(String str) {
        if (!isToast) {
            return;
        }
        ToastUtil.show(context, str + "\n授权失败",
                Toast.LENGTH_SHORT);
    }

    private void continueRequestPermission(final PermissionToken token) {
        DialogEh dialog = new DialogEh(context);
        dialog.setTitle("我们需要这项权限");
        dialog.setContent(listener.continuePermissionRequest());
        dialog.setClickListener(new DialogEh.ClickListener() {
            @Override
            public void onClick() {
                token.continuePermissionRequest();
            }

            @Override
            public void onCancel() {
                token.cancelPermissionRequest();
            }
        });
        dialog.setConfirmText(android.R.string.ok);
        dialog.show();
    }

    public interface PerListener {
        void onPermissionSuccess();

        void onPermissionFail();

        int continuePermissionRequest();
    }
}
