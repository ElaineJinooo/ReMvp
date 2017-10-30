package com.remvp.library.mvp;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.remvp.library.R;
import com.remvp.library.view.refresh.PtrClassicFrameLayout;
import com.remvp.library.view.refresh.PtrDefaultHandler2;
import com.remvp.library.view.refresh.PtrFrameLayout;

/**
 * C-ContentView
 * R-ErrorView
 * 带有空界面或错误界面的activity基类，
 * 如果需要使用下拉刷新功能，必须使用{@link PtrClassicFrameLayout}控件，ID为pull_refresh_view，
 * 默认只支持下拉刷新，使用上推加载自行设置{@link PtrClassicFrameLayout#setMode(PtrFrameLayout.Mode)}
 * <p>
 * 容器控件的ID必须使用{@link R.id#content_view}
 * 错误或空控件的ID必须使用{@link R.id#error_view}
 * 下拉刷新控件的ID必须使用{@link R.id#pull_refresh_view}
 */
public abstract class CEAct<CV extends View, T, V extends CEView<T>, P extends BasePresenter<V>>
        extends BaseAct<T, V, P> implements CEView<T> {
    private static final String TAG = "CEAct";
    protected CV contentView;
    protected TextView errorView;
    protected PtrClassicFrameLayout refreshView;

    @Override
    protected void initView() {
        contentView = findView(R.id.content_view);
        errorView = findView(R.id.error_view);
        refreshView = findView(R.id.pull_refresh_view);
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
        Log.i(TAG, "setDataInfo: " + data);
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
