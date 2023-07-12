package com.topflytech.codec.entities;

/**
 * Created by admin on 2017/10/9.
 */
public class BleCustomer2397SensorData extends BleData{


    private String mac;
    private byte[] rawData;
    private Integer rssi;

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }
}
