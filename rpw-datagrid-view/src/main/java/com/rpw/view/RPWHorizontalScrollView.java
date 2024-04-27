package com.rpw.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class RPWHorizontalScrollView extends HorizontalScrollView {
    private OnCustomScrollChangeListener listener;

    public interface OnCustomScrollChangeListener {
        void onCustomScrollChange(RPWHorizontalScrollView view, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

    public void setOnCustomScrollChangeListener(OnCustomScrollChangeListener listener) {
        this.listener = listener;
    }

    public RPWHorizontalScrollView(Context context) {
        this(context, null);
    }

    public RPWHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RPWHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (null != listener)
            listener.onCustomScrollChange(RPWHorizontalScrollView.this, l, t, oldl, oldt);
    }

    //    @Override
//    public void fling(int velocityX) {
//        // 禁止惯性滑动
//        // 惯性滑动会导致行错位的问题
//        super.fling(velocityX / 1000);
//    }
}
