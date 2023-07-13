package com.topflytech.codec.entities;
 

/**
 * Created by admin on 2019/8/29.
 */
public class BleTempData extends BleData{

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

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }


    public Integer getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(Integer lightIntensity) {
        this.lightIntensity = lightIntensity;
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
    private Float humidity;
    private Integer lightIntensity;
    private Integer rssi;


}
