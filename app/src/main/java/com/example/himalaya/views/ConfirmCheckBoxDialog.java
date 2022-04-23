package com.example.himalaya.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.himalaya.R;

public class ConfirmCheckBoxDialog extends Dialog {
    private View mDelHistoryBtn;
    private View mGiveUpDelBtn;
    private OnDialogDelActionListener mDialogActionListener = null;
    private CheckBox mDelAllCheckBox;

    public ConfirmCheckBoxDialog(@NonNull Context context) {
        this(context, 0);
    }

    public ConfirmCheckBoxDialog(@NonNull Context context, int themeResId) {
        this(context, true, null);
    }

    protected ConfirmCheckBoxDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_checkbox_confirm);
        initView();
        initEvent();
    }

    private void initView() {
        mGiveUpDelBtn = this.findViewById(R.id.dialog_give_up_del_tv);
        mDelHistoryBtn = this.findViewById(R.id.dialog_del_history_tv);
        mDelAllCheckBox = this.findViewById(R.id.del_all_check_box);
    }

    private void initEvent() {
        mGiveUpDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mDelHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDialogActionListener != null) {
                    boolean checked = mDelAllCheckBox.isChecked();
                    mDialogActionListener.onDelHistoryClick(checked);
                }
                dismiss();
            }
        });


    }

    public void setOnDialogActionListener (OnDialogDelActionListener listener) {
        this.mDialogActionListener = listener;
    }

    public interface OnDialogDelActionListener {
        void onDelHistoryClick(boolean checked);
    }
}
