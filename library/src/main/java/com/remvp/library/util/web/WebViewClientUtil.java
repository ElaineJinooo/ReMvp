package com.remvp.library.util.web;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.remvp.library.R;

/**
 * WebViewClient工具类
 */
public class WebViewClientUtil extends WebViewClient {
    private static final String TAG = "WebViewClientUtil";
    private IWebView iWebView;

    public WebViewClientUtil(IWebView baseActivity) {
        this.iWebView = baseActivity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(TAG, "InformationH5Act: shouldOverrideUrlLoading:" +
                " [view, url]=" + url);
        iWebView.shouldOverrideUrlLoading(url);
        if (iWebView.isWhiteList(url)) {
            //URL在白名单中，根据参数判断是否新开页面
            return iWebView.isOpenNewWindow(url);
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        Log.i(TAG, "InformationH5Act: shouldOverrideUrlLoading:" +
                " [view, url]=" + url);
        iWebView.shouldOverrideUrlLoading(url);
        if (iWebView.isWhiteList(url)) {
            //URL在白名单中，根据参数判断是否新开页面
            return iWebView.isOpenNewWindow(url);
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        iWebView.showCommonDialog();
    }

    @Override
    public void onReceivedError(WebView view, final int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Log.i(TAG, "InformationH5Act: onReceivedError: " +
                "[view, request, error]=" + errorCode + ",description=" + description);
        iWebView.onReceivedError(errorCode);
        iWebView.closeDialog();
        if (errorCode == -2) {
            iWebView.toast(R.string.no_network);
            view.setVisibility(View.INVISIBLE);
            iWebView.setTitle("");
        }
    }

    @TargetApi(android.os.Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view,
                                WebResourceRequest request, final WebResourceError error) {
        super.onReceivedError(view, request, error);
        Log.i(TAG, "InformationH5Act: onReceivedError: " +
                "[view, request, error]=" + error.getErrorCode());
        iWebView.onReceivedError(error.getErrorCode());
        iWebView.closeDialog();
        if (error.getErrorCode() == -2) {
            iWebView.toast(R.string.no_network);
            view.setVisibility(View.INVISIBLE);
            iWebView.setTitle("");
        }
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        Log.i(TAG, "InformationH5Act: onReceivedHttpError:" +
                " [view, request, errorResponse]=" + errorResponse);
        iWebView.closeDialog();
    }

    @Override
    public void onPageFinished(final WebView view, final String url) {
        super.onPageFinished(view, url);
        iWebView.setTitle(view.getTitle());
        if (iWebView.isWhiteList(url)) {
            Log.i(TAG, "InformationH5Act onPageFinished close dialog");
            iWebView.closeDialog();
        }
    }
}
