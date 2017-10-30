package com.remvp.library.mvp;

import android.view.View;
import android.widget.TextView;

import com.remvp.library.R;
import com.remvp.library.view.refresh.PtrClassicFrameLayout;
import com.remvp.library.view.refresh.PtrDefaultHandler2;
import com.remvp.library.view.refresh.PtrFrameLayout;

/**
 * C-ContentView
 * R-ErrorView
 * 带有空界面或错误界面的Fragment基类
 * 注：功能代码请与{@link CEAct}保持一致
 */
public abstract class CEFrag<CV extends View, T, V extends CEView<T>, P extends BasePresenter<V>>
        extends BaseFrg<T, V, P> implements CEView<T> {
    protected CV contentView;
    protected TextView errorView;
    protected PtrClassicFrameLayout refreshView;

    @Override
    protected void initView() {
        contentView = (CV) getView().findViewById(R.id.content_view);
        errorView = (TextView) getView().findViewById(R.id.error_view);
        refreshView = (PtrClassicFrameLayout) getView().findViewById(R.id.pull_refresh_view);
        if (contentView == null) {
            throw new NullPointerException(
                    "Content view is null! Have you specified a content view in your layout xml file?"
                            + " You have to give your content View the id R.id.contentView");
        }
        if (refreshView != null) {
            //默认只支持下拉刷新
            refreshView.setMode(PtrFrameLayout.Mode.REFRESH);
            refreshView.setPtrHandler(new PtrDefaultHandler2() {
                @Override
                public void onLoadMoreBegin(PtrFrameLayout frame) {
                    onLoadMore();
                }

                @Override
                public void onRefreshBegin(PtrFrameLayout frame) {
                    loadData(true);
                }
            });
        }
    }

    /**
     * 上推加载
     */
    protected void onLoadMore() {
        loadData(false);
    }

    @Override
    public void loadData() {
        loadData(false);
    }

    @Override
    public void showError(String code, String errorMsg, boolean isRefresh) {
        if (refreshView != null && refreshView.isRefreshing()) {
            refreshView.refreshComplete();
        }
        if (!isRefresh) {
            setViewDisplayStatus(1);
        }
        showError(code, errorMsg);
    }

    @Override
    public void setDataInfo(T data, boolean isRefresh) {
        if (refreshView != null && refreshView.isRefreshing()) {
            refreshView.refreshComplete();
        }
        if (data == null && isRefresh) {
            setViewDisplayStatus(1);
            return;
        }
        setViewDisplayStatus(0);
    }

    @Override
    public void setViewDisplayStatus(int flag) {
        if (errorView == null) {
            return;
        }
        switch (flag) {
            case 0:
                if (contentView.getVisibility() == View.VISIBLE) {
                    return;
                }
                contentView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
                break;
            case 1:
                if (errorView.getVisibility() == View.VISIBLE) {
                    return;
                }
                contentView.setVisibility(View.INVISIBLE);
                errorView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
