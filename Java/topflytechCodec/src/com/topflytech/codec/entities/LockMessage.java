package com.topflytech.codec.entities;

import java.util.Date;

/**
 * Created by admin on 2021/6/30.
 */
public class LockMessage extends Message{
    private Boolean latlngValid = false;//current is gps data or lbs data
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;
    private Float speed = 0.0f;         //速度
    private Integer azimuth = 0;        //方位角
    private int lockType;
    private int lockResult;
    private String lockId;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public Boolean isLatlngValid() {
        return latlngValid;
    }

    public void setLatlngValid(Boolean latlngValid) {
        this.latlngValid = latlngValid;
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

    public int getLockType() {
        return lockType;
    }

    public void setLockType(int lockType) {
        this.lockType = lockType;
    }

    public int getLockResult() {
        return lockResult;
    }

    public void setLockResult(int lockResult) {
        this.lockResult = lockResult;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }
}
