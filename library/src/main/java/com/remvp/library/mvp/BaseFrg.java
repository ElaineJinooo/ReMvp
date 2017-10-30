package com.remvp.library.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.remvp.library.BaseFragment;

/**
 * 普通MVP模式Fragment基类
 * 注：功能代码请与{@link CEAct}保持一致
 */
public abstract class BaseFrg<T, V extends BaseView<T>, P extends BasePresenter<V>>
        extends BaseFragment implements BaseView<T> {
    private static final String TAG = "BaseFrg";
    protected P presenter;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
     * 初始化View
     */
    protected abstract void initView();
}
