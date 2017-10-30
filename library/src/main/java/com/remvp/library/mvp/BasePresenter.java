package com.remvp.library.mvp;

import com.alibaba.fastjson.TypeReference;
import com.remvp.library.bean.BaseResponse;
import com.remvp.library.http.BaseObserver;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Presenter的基类
 */
public class BasePresenter<V extends BaseView> {
    /**
     * 绑定的视图
     */
    protected V view;
    /**
     * 监听发起的Observer，退出时{@link CompositeDisposable#dispose()}
     */
    private CompositeDisposable compositeDisposable;

    public BasePresenter(V view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
    }

    /**
     * 与View解绑
     * {@link #compositeDisposable}中断，清空,界面返回时停止网络请求
     */
    public void detachView() {
        if (view != null) {
            view = null;
        }
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
    }

    /**
     * 将Subscriber增加到{@link #compositeDisposable}中
     *
     * @param observer
     */
    protected void add(BaseObserver observer) {
        if (compositeDisposable != null && observer != null && observer.getDisposable() != null) {
            compositeDisposable.add(observer.getDisposable());
        }
    }

    /**
     * 隐藏加载对话框并显示错误信息
     *
     * @param code
     * @param errMsg
     */
    protected void closeLoadingAndShowError(String code, String errMsg) {
        if (view == null) {
            return;
        }
        view.closeDialog();
        view.showError(code, errMsg);
    }

    /**
     * 隐藏加载对话框并显示错误信息
     *
     * @param code
     * @param errMsg
     */
    protected void closeLoadingAndShowError(String code, String errMsg, boolean isRefresh) {
        if (view == null) {
            return;
        }
        view.closeDialog();
        if (view instanceof CEView) {
            ((CEView) view).showError(code, errMsg, isRefresh);
        } else {
            view.showError(code, errMsg);
        }
    }

    /**
     * 创建通用Observer，onNext中view.setDataInfo设置的数据是data
     * 所创建的Observer增加{@link #add(BaseObserver)}，
     * 以便{@link #detachView()}时取消正在进行的网络请求
     *
     * @param type 解析类型
     * @param <T>
     * @return
     */
    protected <T extends BaseResponse> BaseObserver<T> createObserver(final TypeReference<T> type) {
        BaseObserver<T> subscriber = new BaseObserver<T>(view.getContext().getApplicationContext()) {

            @Override
            public TypeReference<T> getTypeReference() {
                return type;
            }

            @Override
            public void onNext(@NonNull T t) {
                if (view == null) {
                    return;
                }
                view.closeDialog();
                view.setDataInfo(t.getData());
            }

            @Override
            public void handleError(String code, String message) {
                super.handleError(code, message);
                closeLoadingAndShowError(code, message);
            }
        };
        add(subscriber);
        return subscriber;
    }

    /**
     * 创建通用Observer，onNext中view.setDataInfo设置的数据是data
     * 所创建的Observer增加{@link #add(BaseObserver)}，
     * 以便{@link #detachView()}时取消正在进行的网络请求
     *
     * @param type      解析类型
     * @param isRefresh 是否下拉刷新
     * @param <T>
     * @return
     */
    protected <T extends BaseResponse> BaseObserver<T> createObserver(final TypeReference<T> type,
                                                                      final boolean isRefresh) {
        BaseObserver<T> subscriber = new BaseObserver<T>(view.getContext().getApplicationContext()) {

            @Override
            public TypeReference<T> getTypeReference() {
                return type;
            }

            @Override
            public void onNext(@NonNull T t) {
                if (view == null) {
                    return;
                }
                view.closeDialog();
                if (view instanceof CEView) {
                    ((CEView) view).setDataInfo(t.getData(), isRefresh);
                } else {
                    view.setDataInfo(t.getData());
                }
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
     * 创建返回所有数据的通用Observer，onNext中view.setDataInfo设置的数据是data
     * 所创建的Observer增加{@link #add(BaseObserver)}，
     * 以便{@link #detachView()}时取消正在进行的网络请求
     *
     * @param type 解析类型
     * @param <T>
     * @return
     */
    protected <T extends BaseResponse> BaseObserver<T> createAllObserver(final TypeReference<T> type) {
        BaseObserver<T> subscriber = new BaseObserver<T>(view.getContext().getApplicationContext()) {

            @Override
            public TypeReference<T> getTypeReference() {
                return type;
            }

            @Override
            public void onNext(@NonNull T t) {
                if (view == null) {
                    return;
                }
                view.closeDialog();
                view.setDataInfo(t);
            }

            @Override
            public void handleError(String code, String message) {
                super.handleError(code, message);
                closeLoadingAndShowError(code, message);
            }
        };
        add(subscriber);
        return subscriber;
    }

}
