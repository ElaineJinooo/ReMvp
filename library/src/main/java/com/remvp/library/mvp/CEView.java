package com.remvp.library.mvp;

/**
 * C-ContentView
 * R-ErrorView
 * 由于NullView一般都是和ErrorView使用同一个控件，自己在代码中实现
 */
public interface CEView<T> extends BaseView<T> {
    /**
     * 显示错误信息，默认为toast
     * 如需要修改请重写
     *
     * @param code      错误code
     * @param errorMsg  错误信息
     * @param isRefresh 是否下拉刷新，用来判断显示ContentView还是ErrorView
     */
    void showError(String code, String errorMsg, boolean isRefresh);

    /**
     * 获取到数据，可以是网络请求或者是本地
     *
     * @param data      返回数据
     * @param isRefresh 是否下拉刷新，用来判断显示ContentView还是NullView
     */
    void setDataInfo(T data, boolean isRefresh);

    /**
     * 请求数据
     *
     * @param isRefresh 是否下拉刷新
     */
    void loadData(boolean isRefresh);

    /**
     * 设置ContentView和ErrorView的显示状态
     *
     * @param flag 0：显示ContentView 1：显示ErrorView，2：其他
     */
    void setViewDisplayStatus(int flag);
}
