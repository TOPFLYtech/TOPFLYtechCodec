package com.topflytech.bleAntiLost.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    public static String cancelClickAction = "ACTION_CANCEL_RESULT";
    @Override
    public void onReceive(Context context, Intent intent) {
        // 在这里处理通知被点击的逻辑
        // 例如：打开某个Activity，或者执行特定的操作
        Log.e("TFTBLE SERVICE","在这里处理通知被点击的逻辑");
//        isCurWarning = false;
        Intent resultIntent = new Intent(cancelClickAction);


        LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent);
    }
}