package com.remvp.library.mvp;

import android.os.Bundle;
import android.util.Log;

import com.remvp.library.BaseActivity;

/**
 * 普通MVP模式activity基类，普通的网络请求
 */
public abstract class BaseAct<T, V extends BaseView<T>, P extends BasePresenter<V>>
        extends BaseActivity implements BaseView<T> {
    private static final String TAG = "BaseAct";
    protected P presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        presenter = getPresenter();
        initView();
    }

    @Override
    public void showError(String code, String errorMsg) {
        toast(errorMsg);
    }

    @Override
    public void setDataInfo(T data) {
        if (data == null) {
            return;
        }
        Log.i(TAG, "setDataInfo: " + data.toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    /**
     * 初始化Presenter
     *
     * @return
     */
    protected abstract P getPresenter();

    /**
     * 设置layout id
     *
     * @return
     */
    protected abstract int getContentView();

    /**
     * 初始化View
     */
    protected abstract void initView();

}
