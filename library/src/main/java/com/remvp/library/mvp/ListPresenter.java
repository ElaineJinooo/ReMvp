package com.remvp.library.mvp;

import com.alibaba.fastjson.TypeReference;
import com.remvp.library.bean.BaseListResponse;
import com.remvp.library.http.BaseObserver;

/**
 * 列表Presenter
 */
public class ListPresenter<V extends BaseListView> extends BasePresenter<V> {
    public ListPresenter(V view) {
        super(view);
    }

    /**
     * 创建通用Observer，handleMsg中view.setDataInfo设置的数据是data
     * 所创建的Observer增加{@link #add(BaseObserver)}，
     * 以便{@link #detachView()}时取消正在进行的网络请求
     *
     * @param type      解析类型
     * @param isRefresh 是否下拉刷新
     * @param <T>
     * @return
     */
    protected <T extends BaseListResponse> BaseObserver<T> createListSubscriber(
            final TypeReference<T> type, final boolean isRefresh) {
        BaseObserver<T> subscriber = new BaseObserver<T>(view.getContext().getApplicationContext()) {
            @Override
            public TypeReference getTypeReference() {
                return type;
            }

            @Override
            public void onNext(T tBaseResponse) {
                if (view == null) {
                    return;
                }
                view.closeDialog();
                view.setPageCount(tBaseResponse.getPage_count());
                view.setDataInfo(tBaseResponse.getData(), isRefresh);
            }

            @Override
            public void handleError(String code, String message) {
                super.handleError(code, message);
                closeLoadingAndShowError(code, message, isRefresh);
            }
        };
        add(subscriber);
        return subscriber;
    }

    /**
     * 是否显示加载对话框，只有在不是下拉刷新并且page=1
     *
     * @param page      当前页数
     * @param isRefresh 是否下拉刷新
     */
    protected void showCommonDialog(int page, boolean isRefresh) {
        if (view == null) {
            return;
        }
        if (!isRefresh && page == 1)
            view.showCommonDialog();
    }
}
