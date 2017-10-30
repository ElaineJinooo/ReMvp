package com.remvp.library.http;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.TypeReference;
import com.remvp.library.bean.BaseResponse;
import com.remvp.library.util.NetUtil;

import java.net.ConnectException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;

import static com.remvp.library.http.ApiException.ERROR.CATCH_ERROR;

/**
 * <p>描述：订阅的基类</p>
 * 1.在{@link #onSubscribe(Disposable)}中无网络时直接中断请求<br>
 * 2.统一处理了异常<br>
 */
public abstract class BaseObserver<T extends BaseResponse> implements Observer<T> {
    private static final String TAG = "BaseObserver";
    public Disposable disposable;
    private Context context;

    public BaseObserver() {
    }

    public BaseObserver(Context context) {
        this.context = context;
    }

    /**
     * 返回的参数类型
     *
     * @return
     */
    public abstract TypeReference getTypeReference();

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        Log.i(TAG, "onSubscribe: 开始");
        disposable = d;
        if (context != null && !NetUtil.isConnect(context)) {
            Log.i(TAG, "onSubscribe: 无网络");
            ApiException a = ApiException.handleException(new ConnectException());
            handleError(a.getCode(), a.getMessage());
            onComplete();
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        ApiException apiException;
        if (e instanceof ApiException) {
            apiException = (ApiException) e;
        } else {
            apiException = ApiException.handleException(e);
        }
        if (!apiException.getCode().equals(CATCH_ERROR)) {
            handleError(apiException.getCode(), apiException.getMessage());
        }
        Log.i(TAG, "onError: " + e.toString());
        if (e instanceof CompositeException) {
            CompositeException compositeE = (CompositeException) e;
            for (Throwable throwable : compositeE.getExceptions()) {
                Log.i(TAG, "onError: CompositeException " + throwable);
            }
        }
    }

    public void handleError(String code, String message) {
        Log.i(TAG, "handleError: code:" + code + ";msg:" + message);
    }

    @Override
    public void onComplete() {
        Log.i(TAG, "onComplete: 关闭");
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public Disposable getDisposable() {
        return disposable;
    }

}
