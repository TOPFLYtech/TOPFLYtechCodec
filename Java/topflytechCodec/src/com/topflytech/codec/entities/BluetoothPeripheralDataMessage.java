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
    public static int MESSAGE_TYPE_Customer2397 = 7;
    public static int MESSAGE_TYPE_MIX = 8;

    private Boolean is_4g_lbs = false;
    private Integer mcc_4g;
    private Integer mnc_4g;
    private Long eci_4g;
    private Integer pcid_4g_1;
    private Integer pcid_4g_2;
    private Integer tac;
    private Integer pcid_4g_3;

    private Boolean is_2g_lbs = false;
    private Integer mcc_2g;
    private Integer mnc_2g;
    private Integer lac_2g_1;
    private Integer ci_2g_1;
    private Integer lac_2g_2;
    private Integer ci_2g_2;
    private Integer lac_2g_3;
    private Integer ci_2g_3;
    public Integer getTac() {
        return tac;
    }

    public void setTac(Integer tac) {
        this.tac = tac;
    }

    public Integer getPcid_4g_3() {
        return pcid_4g_3;
    }

    public void setPcid_4g_3(Integer pcid_4g_3) {
        this.pcid_4g_3 = pcid_4g_3;
    }
    public Boolean is_4g_lbs() {
        return is_4g_lbs;
    }

    public void setIs_4g_lbs(Boolean is_4g_lbs) {
        this.is_4g_lbs = is_4g_lbs;
    }

    public Integer getMcc_4g() {
        return mcc_4g;
    }

    public void setMcc_4g(Integer mcc_4g) {
        this.mcc_4g = mcc_4g;
    }

    public Integer getMnc_4g() {
        return mnc_4g;
    }

    public void setMnc_4g(Integer mnc_4g) {
        this.mnc_4g = mnc_4g;
    }

    public Long getEci_4g() {
        return eci_4g;
    }

    public void setEci_4g(Long eci_4g) {
        this.eci_4g = eci_4g;
    }



    public Integer getPcid_4g_1() {
        return pcid_4g_1;
    }

    public void setPcid_4g_1(Integer pcid_4g_1) {
        this.pcid_4g_1 = pcid_4g_1;
    }


    public Integer getPcid_4g_2() {
        return pcid_4g_2;
    }

    public void setPcid_4g_2(Integer pcid_4g_2) {
        this.pcid_4g_2 = pcid_4g_2;
    }

    public Boolean is_2g_lbs() {
        return is_2g_lbs;
    }

    public void setIs_2g_lbs(Boolean is_2g_lbs) {
        this.is_2g_lbs = is_2g_lbs;
    }

    public Integer getMcc_2g() {
        return mcc_2g;
    }

    public void setMcc_2g(Integer mcc_2g) {
        this.mcc_2g = mcc_2g;
    }

    public Integer getMnc_2g() {
        return mnc_2g;
    }

    public void setMnc_2g(Integer mnc_2g) {
        this.mnc_2g = mnc_2g;
    }

    public Integer getLac_2g_1() {
        return lac_2g_1;
    }

    public void setLac_2g_1(Integer lac_2g_1) {
        this.lac_2g_1 = lac_2g_1;
    }

    public Integer getCi_2g_1() {
        return ci_2g_1;
    }

    public void setCi_2g_1(Integer ci_2g_1) {
        this.ci_2g_1 = ci_2g_1;
    }

    public Integer getLac_2g_2() {
        return lac_2g_2;
    }

    public void setLac_2g_2(Integer lac_2g_2) {
        this.lac_2g_2 = lac_2g_2;
    }

    public Integer getCi_2g_2() {
        return ci_2g_2;
    }

    public void setCi_2g_2(Integer ci_2g_2) {
        this.ci_2g_2 = ci_2g_2;
    }

    public Integer getLac_2g_3() {
        return lac_2g_3;
    }

    public void setLac_2g_3(Integer lac_2g_3) {
        this.lac_2g_3 = lac_2g_3;
    }

    public Integer getCi_2g_3() {
        return ci_2g_3;
    }

    public void setCi_2g_3(Integer ci_2g_3) {
        this.ci_2g_3 = ci_2g_3;
    }
}
