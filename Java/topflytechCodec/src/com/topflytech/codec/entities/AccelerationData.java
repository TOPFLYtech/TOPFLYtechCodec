package com.topflytech.codec.entities;

import java.util.Date;


/**
 * The type Acceleration data.
 */
public class AccelerationData {
    /**
     * Gets date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Gets acceleration of x axis
     *
     * @return the acceleration of x axis
     */
    public float getAxisX() {
        return axisX;
    }

    /**
     * Sets acceleration of x axis.
     *
     * @param axisX the acceleration of x axis
     */
    public void setAxisX(float axisX) {
        this.axisX = axisX;
    }

    /**
     * Gets acceleration of y axis.
     *
     * @return the acceleration of y axis
     */
    public float getAxisY() {
        return axisY;
    }

    /**
     * Sets acceleration of x axis.
     *
     * @param axisY the acceleration of y axis
     */
    public void setAxisY(float axisY) {
        this.axisY = axisY;
    }

    /**
     * Gets acceleration of z axis.
     *
     * @return the acceleration of z axis
     */
    public float getAxisZ() {
        return axisZ;
    }

    /**
     * Sets acceleration of z axis.
     *
     * @param axisZ the acceleration of z axis
     */
    public void setAxisZ(float axisZ) {
        this.axisZ = axisZ;
    }

    /**
     * Gets speed.
     *
     * @return the speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Sets speed.
     *
     * @param speed the speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Gets latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets imei.
     *
     * @return the imei
     */
    public String getImei() {
        return imei;
    }

    /**
     * Sets imei.
     *
     * @param imei the imei
     */
    public void setImei(String imei) {
        this.imei = imei;
    }

    /**
     * Gets altitude.
     *
     * @return the altitude
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * Sets altitude.
     *
     * @param altitude the altitude
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    /**
     * Is gps working or sleeping. when gps working ,return true, otherwise return false.
     *
     * @return the boolean
     */
    public Boolean isGpsWorking() {
        return gpsWorking;
    }

    /**
     * Sets gps working.
     *
     * @param gpsWorking the gps working
     */
    public void setGpsWorking(Boolean gpsWorking) {
        this.gpsWorking = gpsWorking;
    }
    /**
     * Is GPS data or LBS data. when GPS data,return true.
     *
     * @return the boolean
     */
    public Boolean isLatlngValid() {
        return latlngValid;
    }

    /**
     * Sets latlng valid.
     *
     * @param latlngValid the latlng valid
     */
    public void setLatlngValid(Boolean latlngValid) {
        this.latlngValid = latlngValid;
    }

    /**
     * Gets satellite number.
     *
     * @return the satellite number
     */
    public Integer getSatelliteNumber() {
        return satelliteNumber;
    }

    /**
     * Sets satellite number.
     *
     * @param satelliteNumber the satellite number
     */
    public void setSatelliteNumber(Integer satelliteNumber) {
        this.satelliteNumber = satelliteNumber;
    }

    /**
     * Gets the vehicle current azimuth.
     *
     * @return the azimuth
     */
    public Integer getAzimuth() {
        return azimuth;
    }

    /**
     * Sets azimuth.
     *
     * @param azimuth the azimuth
     */
    public void setAzimuth(Integer azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Is history data boolean.
     *
     * @return the boolean
     */
    public Boolean isHistoryData() {
        return isHistoryData;
    }

    /**
     * Sets is history data.
     *
     * @param isHistoryData the is history data
     */
    public void setIsHistoryData(Boolean isHistoryData) {
        this.isHistoryData = isHistoryData;
    }


    public int getRpm() {

        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }
    private String imei;
    private Date date;
    private float axisX;
    private float axisY;
    private float axisZ;
    private float speed;
    private double latitude;
    private double longitude;
    private double altitude;
    private int azimuth;

    private Integer satelliteNumber;
    private Boolean gpsWorking = false;
    private Boolean latlngValid = false;
    private Boolean isHistoryData = false;

    private int rpm;
}
