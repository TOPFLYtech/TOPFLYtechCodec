package com.topflytech.tftble.data;

import android.os.SystemClock;
import android.view.View;

import com.github.gzuliyujiang.wheelpicker.contract.OnOptionPickedListener;


public abstract class SingleOptionSelectClickListener implements OnOptionPickedListener {
    private static final long MIN_CLICK_INTERVAL = 1000; // 设置最小点击间隔时间
    private long lastClickTime;

    public abstract void onSingleOptionClick(int position, Object item) ;

    @Override
        public final void onOptionPicked(int position, Object item)    {
        long currentClickTime = SystemClock.uptimeMillis();
        if (currentClickTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentClickTime;
            onSingleOptionClick(position,item);
        }
    }
}
