package com.topflytech.codec.entities;
 
/**
 * Created by admin on 2020/12/24.
 */
public class BleFuelData extends BleData{
    private Float temp;
    private Integer rssi;
    private Float voltage;
    private int alarm = 0;
    private int value;
    private int online;

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }
    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Float getVoltage() {
        return voltage;
    }

    public void setVoltage(Float voltage) {
        this.voltage = voltage;
    }

    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public final static int ALARM_NONE = 0;
    public final static int ALARM_FILL_TANK = 1;
    public final static int ALARM_FUEL_LEAK = 2;

}
