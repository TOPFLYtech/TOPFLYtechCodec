package com.topflytech.bleAntiLost.data;

public interface BleStatusCallback {
    void onNotifyValue(String imei,byte[] value);
    void onBleStatusCallback(String imei,int connectStatus);
    void onRssiCallback(String imei,int rssi);
}
