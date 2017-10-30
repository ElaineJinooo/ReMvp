package com.remvp.library.util.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * WebView 帮助类
 */
public class WebViewUtil {
    private WebView webView;
    private IWebView iWebView;
    private WebSettings webSettings;
    private WebChromeFileClient webChromeClient;
    private WebViewClientUtil webViewClient;

    public WebViewUtil(WebView webView, IWebView iWebView) {
        this.webView = webView;
        this.iWebView = iWebView;
        webSettings = webView.getSettings();
    }

    /**
     * 设置WebChromeClient
     *
     * @param client WebChromeClient
     * @return
     */
    public WebViewUtil setWebChromeClient(WebChromeFileClient client) {
        webChromeClient = client;
        return this;
    }

    /**
     * 设置WebViewClient
     *
     * @param client WebViewClient
     * @return
     */
    public WebViewUtil setWebClient(WebViewClientUtil client) {
        webViewClient = client;
        return this;
    }

    /**
     * 设置user-agent
     *
     * @param string user-agent
     * @return
     */
    public WebViewUtil setUserAgentString(String string) {
        if (!TextUtils.isEmpty(string)) {
            webSettings.setUserAgentString(string);
        }
        return this;
    }

    /**
     * 增加js接口
     *
     * @param object
     * @param name
     * @return
     */
    @SuppressLint("JavascriptInterface")
    public WebViewUtil addJavascriptInterface(Object object, String name) {
        webView.addJavascriptInterface(object, name);
        return this;
    }

    /**
     * 加载URL
     *
     * @param url URL
     */
    public void loadUrl(String url) {
        webView.loadUrl(url);
    }

    public void init() {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        // 开启DOM缓存，开启LocalStorage存储（html5的本地存储方式）
        webSettings.setDomStorageEnabled(true);//设置可以使用localStorage
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//默认使用缓存
        webSettings.setAppCachePath(webView.getContext().getApplicationContext().getCacheDir().getAbsolutePath());
        webSettings.setAppCacheMaxSize(8 * 1024 * 1024);//缓存最多可以有8M
        webSettings.setAllowFileAccess(true);//可以读取文件缓存(manifest生效)
        webSettings.setAppCacheEnabled(true);//应用可以有缓存
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        if (webChromeClient == null) {
            webChromeClient = new WebChromeFileClient((Activity) webView.getContext());
        }
        webView.setWebChromeClient(webChromeClient);
        if (webViewClient == null) {
            webViewClient = new WebViewClientUtil(iWebView);
        }
        webView.setWebViewClient(webViewClient);
    }

    /**
     * 回调js方法
     *
     * @param js js 方法 如："javascript:device_callback(" + code + ",\"" + msg + "\")"
     */
    public void evaluateJavascript(String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, null);
        } else {
            webView.loadUrl(js);
        }
    }

    /**
     * 回调js方法
     *
     * @param js js 方法 如："javascript:device_callback(" + code + ",\"" + msg + "\")"
     */
    public void evaluateJavascript(String js, ValueCallback<String> callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, callback);
        } else {
            webView.loadUrl(js);
        }
    }

    /**
     * WebView与activity生命周期{@link Activity#onResume()}绑定
     */
    public void onResume() {
        webView.onResume();
        if (iWebView.isResumeReload()) {
            webView.reload();
        }
    }

    /**
     * WebView与activity生命周期{@link Activity#onPause()}绑定
     */
    public void onPause() {
        webView.onPause();
    }

    /**
     * 当视图从当前窗口解除订阅时销毁webView
     * WebView与activity生命周期{@link Activity#onDetachedFromWindow()}绑定
     */
    public void onDetachedFromWindow() {
        if (webView != null) {
            webView.destroy();
        }
    }

    /**
     * 销毁WebView
     * WebView与activity生命周期{@link Activity#onDestroy()}绑定
     */
    public void onDestroy() {
        if (webView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destroy()
            ViewParent parent = webView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(webView);
            }

            webView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            webView.getSettings().setJavaScriptEnabled(false);
            webView.clearHistory();
            webView.clearView();
            webView.removeAllViews();

            try {
                webView.destroy();
            } catch (Throwable ex) {

            }
        }
    }

    /**
     * 文件上传回传
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        webChromeClient.onActivityResult(requestCode, resultCode, intent);
    }

}
