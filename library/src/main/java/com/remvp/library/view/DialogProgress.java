package com.remvp.library.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.remvp.library.R;

/**
 * 加载对话框
 */
public class DialogProgress extends Dialog {

    private TextView mTvContent;

    public DialogProgress(Context context) {
        super(context, R.style.Dialog_No_Border);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater m_inflater = LayoutInflater.from(context);
        View layout = m_inflater.inflate(R.layout.dialog_progress, null);
        mTvContent = (TextView) layout
                .findViewById(R.id.dialog_progress_content);
        setContentView(layout);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    /**
     * 设置内容
     *
     * @param content
     */
    public void setContent(String content) {
        mTvContent.setText(content);
    }
    /**
     * 设置内容
     *
     * @param id
     */
    public void setContent(int id) {
        mTvContent.setText(id);
    }

}
