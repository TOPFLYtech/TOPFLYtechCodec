package com.topflytech.codec.entities;

import java.util.Date;

public class RS485Message extends Message{
    private Date date;
    private boolean isIgnition = false;
    private int deviceId;
    private byte[] rs485Data;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getRs485Data() {
        return rs485Data;
    }

    public void setRs485Data(byte[] rs485Data) {
        this.rs485Data = rs485Data;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isIgnition() {
        return isIgnition;
    }

    public void setIsIgnition(boolean ignition) {
        isIgnition = ignition;
    }
}
