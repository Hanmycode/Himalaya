package com.example.himalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.himalaya.R;

public class ConfirmDialog extends Dialog {

    private View mCancelSubBtn;
    private View mGiveUpBtn;
    private OnDialogActionListener mDialogActionListener = null;

    public ConfirmDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmDialog(@NonNull Context context, int themeResId) {
        this(context, true, null);
    }

    protected ConfirmDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        initView();
        initEvent();
    }

    private void initView() {
        mGiveUpBtn = this.findViewById(R.id.dialog_give_up_tv);
        mCancelSubBtn = this.findViewById(R.id.dialog_cancel_sub_tv);
    }

    private void initEvent() {
        mGiveUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mCancelSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialogActionListener.onCancelSubClick();
                dismiss();
            }
        });
    }

    public void setOnDialogActionListener (OnDialogActionListener listener) {
        this.mDialogActionListener = listener;
    }

    public interface OnDialogActionListener {
        void onCancelSubClick();
    }
}
