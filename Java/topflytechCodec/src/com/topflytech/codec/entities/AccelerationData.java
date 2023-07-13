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

    public float getGyroscopeAxisZ() {
        return gyroscopeAxisZ;
    }

    public void setGyroscopeAxisZ(float gyroscopeAxisZ) {
        this.gyroscopeAxisZ = gyroscopeAxisZ;
    }

    public float getGyroscopeAxisX() {
        return gyroscopeAxisX;
    }

    public void setGyroscopeAxisX(float gyroscopeAxisX) {
        this.gyroscopeAxisX = gyroscopeAxisX;
    }

    public float getGyroscopeAxisY() {
        return gyroscopeAxisY;
    }

    public void setGyroscopeAxisY(float gyroscopeAxisY) {
        this.gyroscopeAxisY = gyroscopeAxisY;
    }

    private float gyroscopeAxisX;
    private float gyroscopeAxisY;
    private float gyroscopeAxisZ;

    private Boolean is_4g_lbs = false;
    private Integer mcc_4g;
    private Integer mnc_4g;
    private Long ci_4g;
    private Integer earfcn_4g_1;
    private Integer pcid_4g_1;
    private Integer earfcn_4g_2;
    private Integer pcid_4g_2;



    private Boolean is_2g_lbs = false;
    private Integer mcc_2g;
    private Integer mnc_2g;
    private Integer lac_2g_1;
    private Integer ci_2g_1;
    private Integer lac_2g_2;
    private Integer ci_2g_2;
    private Integer lac_2g_3;
    private Integer ci_2g_3;

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

    public Long getCi_4g() {
        return ci_4g;
    }

    public void setCi_4g(Long ci_4g) {
        this.ci_4g = ci_4g;
    }

    public Integer getEarfcn_4g_1() {
        return earfcn_4g_1;
    }

    public void setEarfcn_4g_1(Integer earfcn_4g_1) {
        this.earfcn_4g_1 = earfcn_4g_1;
    }

    public Integer getPcid_4g_1() {
        return pcid_4g_1;
    }

    public void setPcid_4g_1(Integer pcid_4g_1) {
        this.pcid_4g_1 = pcid_4g_1;
    }

    public Integer getEarfcn_4g_2() {
        return earfcn_4g_2;
    }

    public void setEarfcn_4g_2(Integer earfcn_4g_2) {
        this.earfcn_4g_2 = earfcn_4g_2;
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
