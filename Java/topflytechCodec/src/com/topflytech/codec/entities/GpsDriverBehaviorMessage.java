package com.topflytech.codec.entities;
 
import java.util.Date;


/**
 * Driver Behavior Via GPS (AST Command Control, Default Disable the feature).Protocol number is 25 25 05.
 */
public class GpsDriverBehaviorMessage extends Message{
    /**
     * Gets behavior type.
     *
     * @return the behavior type
     */
    public int getBehaviorType() {
        return behaviorType;
    }

    /**
     * Sets behavior type.
     *
     * @param behaviorType the behavior type
     */
    public void setBehaviorType(int behaviorType) {
        this.behaviorType = behaviorType;
    }

    /**
     * Gets start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets start latitude.
     *
     * @return the start latitude
     */
    public double getStartLatitude() {
        return startLatitude;
    }

    /**
     * Sets start latitude.
     *
     * @param startLatitude the start latitude
     */
    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    /**
     * Gets start longitude.
     *
     * @return the start longitude
     */
    public double getStartLongitude() {
        return startLongitude;
    }

    /**
     * Sets start longitude.
     *
     * @param startLongitude the start longitude
     */
    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }

    /**
     * Gets start altitude.
     *
     * @return the start altitude
     */
    public double getStartAltitude() {
        return startAltitude;
    }

    /**
     * Sets start altitude.
     *
     * @param startAltitude the start altitude
     */
    public void setStartAltitude(double startAltitude) {
        this.startAltitude = startAltitude;
    }

    /**
     * Gets start speed.
     *
     * @return the start speed
     */
    public float getStartSpeed() {
        return startSpeed;
    }

    /**
     * Sets start speed.
     *
     * @param startSpeed the start speed
     */
    public void setStartSpeed(float startSpeed) {
        this.startSpeed = startSpeed;
    }

    /**
     * Gets start azimuth.
     *
     * @return the start azimuth
     */
    public int getStartAzimuth() {
        return startAzimuth;
    }

    /**
     * Sets start azimuth.
     *
     * @param startAzimuth the start azimuth
     */
    public void setStartAzimuth(int startAzimuth) {
        this.startAzimuth = startAzimuth;
    }

    /**
     * Gets end date.
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets end date.
     *
     * @param endDate the end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Gets end latitude.
     *
     * @return the end latitude
     */
    public double getEndLatitude() {
        return endLatitude;
    }

    /**
     * Sets end latitude.
     *
     * @param endLatitude the end latitude
     */
    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    /**
     * Gets end longitude.
     *
     * @return the end longitude
     */
    public double getEndLongitude() {
        return endLongitude;
    }

    /**
     * Sets end longitude.
     *
     * @param endLongitude the end longitude
     */
    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }

    /**
     * Gets end altitude.
     *
     * @return the end altitude
     */
    public double getEndAltitude() {
        return endAltitude;
    }

    /**
     * Sets end altitude.
     *
     * @param endAltitude the end altitude
     */
    public void setEndAltitude(double endAltitude) {
        this.endAltitude = endAltitude;
    }

    /**
     * Gets end speed.
     *
     * @return the end speed
     */
    public float getEndSpeed() {
        return endSpeed;
    }

    /**
     * Sets end speed.
     *
     * @param endSpeed the end speed
     */
    public void setEndSpeed(float endSpeed) {
        this.endSpeed = endSpeed;
    }

    /**
     * Gets end azimuth.
     *
     * @return the end azimuth
     */
    public int getEndAzimuth() {
        return endAzimuth;
    }

    /**
     * Sets end azimuth.
     *
     * @param endAzimuth the end azimuth
     */
    public void setEndAzimuth(int endAzimuth) {
        this.endAzimuth = endAzimuth;
    }

    private int behaviorType;
    private Date startDate;
    private double startLatitude;
    private double startLongitude;
    private double startAltitude;
    private float startSpeed;

    public int getStartRpm() {
        return startRpm;
    }

    public void setStartRpm(int startRpm) {
        this.startRpm = startRpm;
    }

    public int getEndRpm() {
        return endRpm;
    }

    public void setEndRpm(int endRpm) {
        this.endRpm = endRpm;
    }

    private int startAzimuth;
    private int startRpm;
    private Date endDate;
    private double endLatitude;
    private double endLongitude;
    private double endAltitude;
    private float endSpeed;
    private int endAzimuth;
    private int endRpm;
}
