package com.topflytech.codec.entities;


/**
 * Created by admin on 2019/8/29.
 */
public class BleCtrlData extends BleData{

    public Float getVoltage() {
        return voltage;
    }

    public void setVoltage(Float voltage) {
        this.voltage = voltage;
    }

    public Integer getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(Integer batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }


    public int getCtrlStatus() {
        return ctrlStatus;
    }

    public void setCtrlStatus(int CtrlStatus) {
        this.ctrlStatus = CtrlStatus;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    private Float voltage;
    private Integer batteryPercent;
    private Float temp;
    private int ctrlStatus;
    private int online;

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    private Integer rssi;


}
