package com.topflytech.lockActive.Ble;

public interface BleStatusCallback {
    void onNotifyValue(byte[] value);
    void onBleStatusCallback(int connectStatus);
    void onRssiCallback(int rssi);
    void onUpgradeNotifyValue(byte[] value);
}
