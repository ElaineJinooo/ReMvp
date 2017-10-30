package com.remvp.library.mvp;

import android.content.Context;

public interface BaseView<T> {
    /**
     * 显示加载对话框
     */
    void showCommonDialog();

    /**
     * 关闭加载对话框
     */
    void closeDialog();

    /**
     * 显示错误信息，默认为toast
     * 如需要修改请重写
     *
     * @param code
     * @param errorMsg
     */
    void showError(String code, String errorMsg);

    /**
     * 获取到数据，可以是网络请求或者是本地，通过
     *
     * @param data
     */
    void setDataInfo(T data);

    /**
     * 请求数据
     */
    void loadData();

    /**
     * 获取context
     */
    Context getContext();
}
