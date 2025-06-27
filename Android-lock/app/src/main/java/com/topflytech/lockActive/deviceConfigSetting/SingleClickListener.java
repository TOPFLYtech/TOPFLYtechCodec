package com.topflytech.lockActive.deviceConfigSetting;

import android.os.SystemClock;
import android.view.View;

public abstract class SingleClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL = 1000; // 设置最小点击间隔时间
    private long lastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = SystemClock.uptimeMillis();
        if (currentClickTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentClickTime;
            onSingleClick(v);
        }
    }
}
