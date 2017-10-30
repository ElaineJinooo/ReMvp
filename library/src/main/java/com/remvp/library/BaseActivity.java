package com.remvp.library;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.remvp.library.bean.ViewMessage;
import com.remvp.library.util.RxBus;
import com.remvp.library.util.ToastUtil;
import com.remvp.library.view.CustomToast;
import com.remvp.library.view.DialogProgress;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 通用Activity基类
 * 1.初始化{@link #toolbar}
 * 2.显示/隐藏加载对话框
 * 3.通用toast{@link #toast(int)},{@link #toast(String)}
 * 4.通用界面跳转{@link #openAct(Class)},{@link #openAct(Intent)}
 */
public abstract class BaseActivity extends AppCompatActivity {
    /**
     * 标题栏
     */
    protected Toolbar toolbar;
    /**
     * 居中标题
     */
    protected TextView toolbarTitle;
    /**
     * 右标题
     */
    protected TextView toolbarRight;
    /**
     * 加载对话框
     */
    private DialogProgress dialogProgress;
    protected Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = RxBus.getDefault().register(ViewMessage.class, new Consumer<ViewMessage>() {
            @Override
            public void accept(ViewMessage o) throws Exception {
                onViewEvent(o);
            }
        });
    }

    /**
     * 事件总线接收
     *
     * @param o 事件
     */
    protected void onViewEvent(ViewMessage o) {
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolbar();
    }

    /**
     * 初始化toolbar，ID必须使用{@link R.id#toolbar}
     * 标题ID{@link R.id#toolbar_title}
     * 右标题ID{@link R.id#toolbar_right}
     */
    protected void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarRight = (TextView) toolbar.findViewById(R.id.toolbar_right);
    }

    /**
     * 显示返回icon，默认为@drawable/back_white，更换请调
     * app:navigationIcon="@drawable/back_white"
     */
    protected void showToolbarBackIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onDestroy() {
        CustomToast.reset();
        closeDialog();
        RxBus.getDefault().unRegister(disposable);
        super.onDestroy();
    }

    /**
     * 退出当前界面
     */
    public void dismiss() {
        hideInput();
        finish();
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
            dialogProgress = new DialogProgress(this);
        }
        if (!dialogProgress.isShowing()) {
            dialogProgress.show();
        }
    }

    /**
     * 显示加载对话框
     */
    public void showCommonDialog(String str) {
        if (dialogProgress == null) {
            dialogProgress = new DialogProgress(this);
        }
        if (!dialogProgress.isShowing()) {
            dialogProgress.setContent(str);
            dialogProgress.show();
        }
    }

    /**
     * 显示加载对话框
     */
    public void showCommonDialog(int id) {
        if (dialogProgress == null) {
            dialogProgress = new DialogProgress(this);
        }
        if (!dialogProgress.isShowing()) {
            dialogProgress.setContent(id);
            dialogProgress.show();
        }
    }


    /**
     * 初始化view {@link #findViewById(int)}
     *
     * @param id  id
     * @param <T>
     * @return
     */
    public <T> T findView(@IdRes int id) {
        return (T) findViewById(id);
    }

    /**
     * toast
     *
     * @param str 文字
     */
    public void toast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(BaseActivity.this, str, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * toast
     *
     * @param id string Id
     */
    public void toast(@StringRes final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(BaseActivity.this, id, Toast.LENGTH_SHORT);
            }
        });
    }

    /**
     * 隐藏软键盘
     */
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
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
        Intent intent = new Intent(this, actClass);
        openAct(intent);
    }

    /**
     * 获取Context
     *
     * @return
     */
    public Context getContext() {
        return this;
    }

    public boolean isAppOnForeground() {
        ActivityManager mActivityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> tasksInfo = mActivityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {
            Class<?> cla = this.getClass();
            String str = cla.getName();
            String name = tasksInfo.get(0).topActivity
                    .getClassName();
            if (name.equals(str)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
