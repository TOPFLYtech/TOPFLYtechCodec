package com.topflytech.tftble.data;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

/**
 * Created by Paul on 17-01-26.
 */

public class DfuService extends DfuBaseService {
    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return Activity.class;
    }
}
