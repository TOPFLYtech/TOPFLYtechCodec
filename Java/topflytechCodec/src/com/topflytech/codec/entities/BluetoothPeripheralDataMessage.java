package com.topflytech.codec.entities;

import java.util.Date;
import java.util.List;

/**
 * Bluetooth Data Forward ( Need Device Support BLE )
 */
public class BluetoothPeripheralDataMessage extends Message{
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

    /**
     * current acc status.
     *
     * @return the boolean
     */
    public boolean isIgnition() {
        return isIgnition;
    }

    /**
     * Sets is ignition.
     *
     * @param isIgnition the is ignition
     */
    public void setIsIgnition(boolean isIgnition) {
        this.isIgnition = isIgnition;
    }
    /**
     * Gets bluetooth data list.
     *
     * @return the bluetooth data list
     */
    public List<BleData> getBleDataList() {
        return bleDataList;
    }

    /**
     * Sets bluetooth data list.
     *
     * @param bleTireDataList the bluetooth data list
     */
    public void setBleDataList(List<BleData> bleTireDataList) {
        this.bleDataList = bleTireDataList;
    }


    public Integer getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(Integer azimuth) {
        this.azimuth = azimuth;
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


    private Date date;

    public boolean isHadLocationInfo() {
        return isHadLocationInfo;
    }

    public void setIsHadLocationInfo(boolean isHadLocationInfo) {
        this.isHadLocationInfo = isHadLocationInfo;
    }

    private boolean isHadLocationInfo = false;

    private Boolean isHistoryData = false;
    private boolean isIgnition = false;
    private Boolean latlngValid = false;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;

    private Float speed = 0.0f;         //速度
    private Integer azimuth = 0;        //方位角
    private List<BleData> bleDataList;
    private int messageType;
    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }


    public static int MESSAGE_TYPE_TIRE = 0;
    public static int MESSAGE_TYPE_DRIVER = 1;
    public static int MESSAGE_TYPE_SOS = 2;
    public static int MESSAGE_TYPE_TEMP = 3;
    public static int MESSAGE_TYPE_DOOR = 4;
    public static int MESSAGE_TYPE_CTRL = 5;
    public static int MESSAGE_TYPE_FUEL = 6;
}
