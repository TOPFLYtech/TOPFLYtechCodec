package com.topflytech.codec.entities;

public class OneWireData {
    private byte deviceType;
    private String deviceId;
    private byte[] oneWireContent;

    public byte getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(byte deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public byte[] getOneWireContent() {
        return oneWireContent;
    }

    public void setOneWireContent(byte[] oneWireContent) {
        this.oneWireContent = oneWireContent;
    }
}
