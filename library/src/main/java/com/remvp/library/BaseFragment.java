package com.remvp.library;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.remvp.library.bean.ViewMessage;
import com.remvp.library.util.RxBus;
import com.remvp.library.util.ToastUtil;
import com.remvp.library.view.DialogProgress;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 通用Activity基类
 * 1.显示/隐藏加载对话框
 * 2.通用toast{@link #toast(int)},{@link #toast(String)}
 * 3.通用界面跳转{@link #openAct(Class)},{@link #openAct(Intent)}
 */
public class BaseFragment extends Fragment {
    /**
     * 加载对话框
     */
    private DialogProgress dialogProgress;
    protected Activity context;
    private Disposable disposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = RxBus.getDefault().register(ViewMessage.class, new Consumer<ViewMessage>() {
            @Override
            public void accept(ViewMessage o) throws Exception {
                onViewEvent(o);
            }
        });
    }

    protected void onViewEvent(ViewMessage o) {
    }

    /**
     * 初始化view
     *
     * @param id  id
     * @param <T>
     * @return
     */
    public <T> T findView(@IdRes int id) {
        return (T) getView().findViewById(id);
    }

    /**
     * 关闭加载对话框
     */
    public void closeDialog() {
        if (dialogProgress != null) {
            dialogProgress.dismiss();
            dialogProgress = null;
        }
    }

    /**
     * 显示加载对话框
     */
    public void showCommonDialog() {
        if (dialogProgress == null) {
            dialogProgress = new DialogProgress(context);
        }
        if (!dialogProgress.isShowing()) {
            dialogProgress.show();
        }
    }

    /**
     * toast
     *
     * @param str 文字
     */
    public void toast(final String str) {
        if (context == null) {
            return;
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(context, str, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * toast
     *
     * @param id string Id
     */
    public void toast(@StringRes final int id) {
        if (context == null) {
            return;
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(context, id, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 打开指定的Activity页面
     *
     * @param intent
     */
    protected void openAct(Intent intent) {
        startActivity(intent);
    }

    /**
     * 打开指定的Activity页面,该方法适用于不需要传递数据的页面跳转
     *
     * @param actClass Activity页面类
     */
    protected void openAct(Class<?> actClass) {
        Intent intent = new Intent(context, actClass);
        openAct(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDialog();
        RxBus.getDefault().unRegister(disposable);
    }

    /**
     * 获取Context
     *
     * @return
     */
    public Context getContext() {
        return context;
    }
}
