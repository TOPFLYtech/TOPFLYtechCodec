package com.topflytech.codec.entities;
 
import java.util.Date;

public class OneWireMessage extends Message{
    private Date date;
    private boolean isIgnition = false;
    private String deviceId;
    private byte[] oneWireData;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getOneWireData() {
        return oneWireData;
    }

    public void setOneWireData(byte[] oneWireData) {
        this.oneWireData = oneWireData;
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
