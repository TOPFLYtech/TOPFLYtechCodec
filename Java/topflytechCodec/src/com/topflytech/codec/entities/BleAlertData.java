package com.topflytech.codec.entities;

/**
 * Created by admin on 2018/4/24.
 */
public class BleAlertData  extends BleData{
    public static int ALERT_TYPE_SOS = 0;
    public static int ALERT_TYPE_LOW_BATTERY = 1;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public float getInnerVoltage() {
        return innerVoltage;
    }

    public void setInnerVoltage(float innerVoltage) {
        this.innerVoltage = innerVoltage;
    }

    public int getAlertType() {
        return alertType;
    }

    public void setAlertType(int alertType) {
        this.alertType = alertType;
    }

    public Boolean isLatlngValid() {
        return latlngValid;
    }

    public void setLatlngValid(Boolean latlngValid) {
        this.latlngValid = latlngValid;
    }

    public Boolean isHistoryData() {
        return isHistoryData;
    }

    public void setIsHistoryData(Boolean isHistoryData) {
        this.isHistoryData = isHistoryData;
    }

    public int getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(int satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Integer azimuth) {
        this.azimuth = azimuth;
    }

    private String mac;
    private float innerVoltage;
    private int alertType;
    private Boolean latlngValid = false;//current is gps data or lbs data
    private Boolean isHistoryData = false;
    private int satelliteCount = 0;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;
    private Float speed = 0.0f;
    private Integer azimuth = 0;


}
