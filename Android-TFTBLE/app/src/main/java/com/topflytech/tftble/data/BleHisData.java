package com.topflytech.tftble.data;


import java.util.Date;

public class BleHisData {
    private Date date;
    private int battery;
    private float temp;
    private float humidity;
    private int prop;
    private byte alarm;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public int getProp() {
        return prop;
    }

    public void setProp(int prop) {
        this.prop = prop;
    }

    public byte getAlarm() {
        return alarm;
    }

    public void setAlarm(byte alarm) {
        this.alarm = alarm;
    }
}
