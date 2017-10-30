package com.remvp.library.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.remvp.library.R;

/**
 * 通用对话框
 * 1.支持单/双按钮
 * 2.默认左边取消右边确认，可通过{@link #DialogEh(Context, boolean)}和
 * {@link #setConfirmRight(boolean)}设置
 * 3.dimens中设置dialog_btn_text_size可修改按钮字体大小，默认14dp
 * 4.dimens中设置dialog_content_text_size可修改标题和内容字体大小，默认17dp
 */
public class DialogEh extends Dialog {
    /**
     * 标题，内容，取消按钮，确定按钮
     */
    private TextView titleTv, contentTv, leftTv, rightTv;
    /**
     * 取消和确定按钮的分割线，内容和按钮的分割线
     */
    private View middleLine, btnLine;
    /**
     * 一个按钮的点击监听
     */
    private SingleClickListener singleClickListener;
    /**
     * 两个按钮的点击监听
     */
    private ClickListener clickListener;
    /**
     * 确定按钮是否在右边
     */
    private boolean isConfirmRight = true;

    public DialogEh(@NonNull Context context) {
        this(context, true);
    }

    /**
     * @param context
     * @param isConfirmRight 确定按钮是否在右边
     */
    public DialogEh(@NonNull Context context, boolean isConfirmRight) {
        super(context, R.style.Dialog_No_Border);
        this.isConfirmRight = isConfirmRight;
        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        setContentView(R.layout.dialog_common);
        titleTv = (TextView) findViewById(R.id.dialog_eh_title);
        contentTv = (TextView) findViewById(R.id.dialog_eh_content);
        leftTv = (TextView) findViewById(R.id.dialog_eh_cancel);
        rightTv = (TextView) findViewById(R.id.dialog_eh_ok);
        middleLine = findViewById(R.id.middle_line);
        btnLine = findViewById(R.id.de_line);
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConfirmRight) {
                    if (clickListener != null) {
                        clickListener.onClick();
                    } else if (singleClickListener != null) {
                        singleClickListener.onClick();
                    }
                } else {
                    if (clickListener != null) {
                        clickListener.onCancel();
                    }
                }
                dismiss();
            }
        });
        leftTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isConfirmRight) {
                    if (clickListener != null) {
                        clickListener.onClick();
                    } else if (singleClickListener != null) {
                        singleClickListener.onClick();
                    }
                } else {
                    if (clickListener != null) {
                        clickListener.onCancel();
                    }
                }
                dismiss();
            }
        });
        if (!isConfirmRight) {
            rightTv.setText(R.string.cancel);
            leftTv.setText(R.string.confirm);
        }
    }

    /**
     * 设置确认按钮位置
     *
     * @param confirmRight true:确认按钮在左边，false:确认按钮在右边
     */
    public void setConfirmRight(boolean confirmRight) {
        this.isConfirmRight = confirmRight;
        if (!confirmRight) {
            rightTv.setText(R.string.cancel);
            leftTv.setText(R.string.confirm);
        } else {
            rightTv.setText(R.string.confirm);
            leftTv.setText(R.string.cancel);
        }
    }

    /**
     * 设置标题
     *
     * @param id 标题Id
     */
    public void setTitle(@StringRes int id) {
        titleTv.setText(id);
        titleTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置标题
     *
     * @param str 标题
     */
    public void setTitle(String str) {
        titleTv.setText(str);
        titleTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置内容
     *
     * @param content 内容
     */
    public void setContent(String content) {
        contentTv.setText(content);
        contentTv.setVisibility(View.VISIBLE);
    }

    /**
     * 以Html设置内容
     *
     * @param content 内容
     */
    public void setContentHtml(String content) {
        contentTv.setText(Html.fromHtml(content));
        contentTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置内容
     *
     * @param id 内容Id
     */
    public void setContent(@StringRes int id) {
        contentTv.setText(id);
        contentTv.setVisibility(View.VISIBLE);
    }

    /**
     * 设置内容，最大行数
     *
     * @param content  内容
     * @param maxLines 最多行数
     */
    public void setContent(String content, int maxLines) {
        contentTv.setMaxLines(maxLines);
        contentTv.setEllipsize(TextUtils.TruncateAt.END);
        setContent(content);
    }

    /**
     * 设置内容，最大行数
     *
     * @param id       内容Id
     * @param maxLines 最多行数
     */
    public void setContent(@StringRes int id, int maxLines) {
        contentTv.setMaxLines(maxLines);
        contentTv.setEllipsize(TextUtils.TruncateAt.END);
        setContent(id);
    }

    /**
     * 设置内容的显示方向
     *
     * @param Gravity 显示方向
     */
    public void setContentGravity(int Gravity) {
        contentTv.setGravity(Gravity);
    }

    /**
     * 设置确定按钮文案
     *
     * @param id 确定按钮文案Id
     */
    public void setConfirmText(@StringRes int id) {
        setConfirmText(getContext().getResources().getText(id).toString());
    }

    /**
     * 设置确定按钮文案
     *
     * @param str 确定按钮文案
     */
    public void setConfirmText(String str) {
        if (!isConfirmRight) {
            leftTv.setText(str);
            leftTv.setVisibility(View.VISIBLE);
        } else {
            rightTv.setText(str);
            rightTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置取消按钮文案
     *
     * @param id 取消按钮文案Id
     */
    public void setCancelText(@StringRes int id) {
        setCancelText(getContext().getResources().getText(id).toString());
    }

    /**
     * 设置取消按钮文案
     *
     * @param str 取消按钮文案
     */
    public void setCancelText(String str) {
        if (isConfirmRight) {
            leftTv.setText(str);
            leftTv.setVisibility(View.VISIBLE);
        } else {
            rightTv.setText(str);
            rightTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 一个按钮监听事件，默认值：确认
     *
     * @param singleClickListener
     */
    public void setSingleClickListener(SingleClickListener singleClickListener) {
        this.singleClickListener = singleClickListener;
        btnLine.setVisibility(View.VISIBLE);
        middleLine.setVisibility(View.GONE);
        if (isConfirmRight) {
            rightTv.setVisibility(View.VISIBLE);
            leftTv.setVisibility(View.GONE);
        } else {
            leftTv.setVisibility(View.VISIBLE);
            rightTv.setVisibility(View.GONE);
        }
    }

    /**
     * 两个按钮监听事件，默认值：取消和确认
     *
     * @param clickListener
     */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
        rightTv.setVisibility(View.VISIBLE);
        btnLine.setVisibility(View.VISIBLE);
        middleLine.setVisibility(View.VISIBLE);
        leftTv.setVisibility(View.VISIBLE);
    }

    /**
     * 一个按钮的点击监听类
     */
    public interface SingleClickListener {
        void onClick();
    }

    /**
     * 两个按钮的点击监听类
     */
    public interface ClickListener {
        void onClick();

        void onCancel();
    }
}
