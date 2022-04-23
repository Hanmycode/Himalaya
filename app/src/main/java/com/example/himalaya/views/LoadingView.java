package com.example.himalaya.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.himalaya.R;

public class LoadingView extends AppCompatImageView {

    // 旋转角度
    private int mRotateDegree = 0;

    // 是否旋转
    private boolean mNeedRotate = false;

    public LoadingView(@NonNull Context context) {
        this(context, null);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置图标
        setImageResource(R.mipmap.loading);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mNeedRotate = true;
        // 锁定到window的时候
        post(new Runnable() {
            @Override
            public void run() {
                mRotateDegree += 10;
                mRotateDegree = mRotateDegree <= 360 ? mRotateDegree : 0;
                // 更新View，会重新调用onDraw()
                invalidate();
                // 是否继续旋转
                if (mNeedRotate) {
                    postDelayed(this, 100);
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 从window中解绑了
        mNeedRotate = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         * 三个参数分别是：
         * 旋转角度，旋转的X坐标，旋转的Y坐标
         * 宽度高度/2得到中心点，围绕中心点旋转
         */
        canvas.rotate(mRotateDegree, getWidth() / 2, getHeight() / 2);
        super.onDraw(canvas);
    }
}
