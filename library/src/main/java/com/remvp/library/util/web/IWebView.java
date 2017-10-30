package com.remvp.library.util.web;

import android.content.Context;
import android.support.annotation.StringRes;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

public interface IWebView {
    /**
     * 显示加载对话框
     */
    void showCommonDialog();

    /**
     * 关闭加载对话框
     */
    void closeDialog();

    /**
     * toast
     *
     * @param str 文字
     */
    void toast(String str);

    /**
     * toast
     *
     * @param id string Id
     */
    void toast(@StringRes int id);

    /**
     * 要加载的URL是否在白名单里面
     *
     * @param url 加载URL
     * @return
     */
    boolean isWhiteList(String url);

    /**
     * 设置标题
     *
     * @param str 标题
     */
    void setTitle(String str);

    /**
     * 加载错误
     *
     * @param errorCode 错误码
     */
    void onReceivedError(int errorCode);

    /**
     * 是否开启一个新的页面
     *
     * @param url 加载地址
     * @return true 新开页面
     * 同{@link android.webkit.WebViewClient#shouldOverrideUrlLoading(WebView, WebResourceRequest)}
     */
    boolean isOpenNewWindow(String url);

    /**
     * onResume 是否reload
     *
     * @return
     */
    boolean isResumeReload();

    /**
     * 设置onResume 是否reload
     *
     * @param isResumeReload 是否reload
     */
    void setResumeReload(boolean isResumeReload);

    /**
     * 执行js回调
     *
     * @param str js
     */
    void evaluateJavascript(String str);

    /**
     * 获取Context
     *
     * @return
     */
    Context getContext();

    /**
     * 当前URL是否在白名单内
     *
     * @return
     */
    boolean isWhite();

    /**
     * 关闭当前页面
     */
    void dismiss();

    /**
     * shouldOverrideUrlLoading方法执行时进行的操作
     *
     * @param url URL地址
     */
    void shouldOverrideUrlLoading(String url);
}
