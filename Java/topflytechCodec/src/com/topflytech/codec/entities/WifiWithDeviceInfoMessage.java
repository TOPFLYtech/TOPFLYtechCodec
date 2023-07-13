package com.topflytech.codec.entities;
 
import java.util.Date;

/**
 * Created by admin on 2021/3/11.
 */
public class WifiWithDeviceInfoMessage extends Message{
    private Date date;
    private String selfMac;
    private String ap1Mac;
    private int ap1RSSI;
    private String ap2Mac;
    private int ap2RSSI;
    private String ap3Mac;
    private int ap3RSSI;

    private float axisX;
    private float axisY;
    private float axisZ;
    private Float deviceTemp;
    private Float lightSensor;
    private Float batteryVoltage;
    private Integer batteryCharge = 0;
    private Float solarVoltage;
    private Long mileage = 0L;
    private Boolean isUsbCharging;
    private Boolean isSolarCharging;
    private Boolean isManagerConfigured1 = false;
    private Boolean isManagerConfigured2 = false;
    private Boolean isManagerConfigured3 = false;
    private Boolean isManagerConfigured4 = false;
    private int networkSignal;
    private Boolean iopIgnition = null;
    private Integer samplingIntervalAccOn = 0;
    private Integer samplingIntervalAccOff = 0;
    private Integer angleCompensation = 0;
    private Integer distanceCompensation = 0;
    private Integer heartbeatInterval = 0;
    private Integer originalAlarmCode;
    private String smartPowerSettingStatus;
    private String smartPowerOpenStatus;
    private Integer lockType;

    private boolean isLockSim = false;
    private boolean isLockDevice = false;
    private boolean AGPSEphemerisDataDownloadSettingStatus = false;
    private boolean gSensorSettingStatus = false;
    private boolean frontSensorSettingStatus = false;
    private boolean deviceRemoveAlarmSettingStatus = false;
    private boolean openCaseAlarmSettingStatus = false;
    private boolean deviceInternalTempReadingANdUploadingSettingStatus = false;

    public Boolean getHistoryData() {
        return isHistoryData;
    }

    public void setHistoryData(Boolean historyData) {
        isHistoryData = historyData;
    }

    private Boolean isHistoryData = false;

    public Integer getOriginalAlarmCode() {
        return originalAlarmCode;
    }

    public void setOriginalAlarmCode(Integer originalAlarmCode) {
        this.originalAlarmCode = originalAlarmCode;
    }

    public boolean isLockSim() {
        return isLockSim;
    }

    public void setIsLockSim(boolean lockSim) {
        isLockSim = lockSim;
    }

    public boolean isLockDevice() {
        return isLockDevice;
    }

    public void setIsLockDevice(boolean lockDevice) {
        isLockDevice = lockDevice;
    }

    public boolean isAGPSEphemerisDataDownloadSettingStatus() {
        return AGPSEphemerisDataDownloadSettingStatus;
    }

    public void setAGPSEphemerisDataDownloadSettingStatus(boolean AGPSEphemerisDataDownloadSettingStatus) {
        this.AGPSEphemerisDataDownloadSettingStatus = AGPSEphemerisDataDownloadSettingStatus;
    }

    public boolean isgSensorSettingStatus() {
        return gSensorSettingStatus;
    }

    public void setgSensorSettingStatus(boolean gSensorSettingStatus) {
        this.gSensorSettingStatus = gSensorSettingStatus;
    }

    public boolean isFrontSensorSettingStatus() {
        return frontSensorSettingStatus;
    }

    public void setFrontSensorSettingStatus(boolean frontSensorSettingStatus) {
        this.frontSensorSettingStatus = frontSensorSettingStatus;
    }

    public boolean isDeviceRemoveAlarmSettingStatus() {
        return deviceRemoveAlarmSettingStatus;
    }

    public void setDeviceRemoveAlarmSettingStatus(boolean deviceRemoveAlarmSettingStatus) {
        this.deviceRemoveAlarmSettingStatus = deviceRemoveAlarmSettingStatus;
    }

    public boolean isOpenCaseAlarmSettingStatus() {
        return openCaseAlarmSettingStatus;
    }

    public void setOpenCaseAlarmSettingStatus(boolean openCaseAlarmSettingStatus) {
        this.openCaseAlarmSettingStatus = openCaseAlarmSettingStatus;
    }

    public boolean isDeviceInternalTempReadingANdUploadingSettingStatus() {
        return deviceInternalTempReadingANdUploadingSettingStatus;
    }

    public void setDeviceInternalTempReadingANdUploadingSettingStatus(boolean deviceInternalTempReadingANdUploadingSettingStatus) {
        this.deviceInternalTempReadingANdUploadingSettingStatus = deviceInternalTempReadingANdUploadingSettingStatus;
    }

    public String getSmartPowerSettingStatus() {
        return smartPowerSettingStatus;
    }

    public void setSmartPowerSettingStatus(String smartPowerSettingStatus) {
        this.smartPowerSettingStatus = smartPowerSettingStatus;
    }

    public String getSmartPowerOpenStatus() {
        return smartPowerOpenStatus;
    }

    public void setSmartPowerOpenStatus(String smartPowerOpenStatus) {
        this.smartPowerOpenStatus = smartPowerOpenStatus;
    }

    public Integer getLockType() {
        return lockType;
    }

    public void setLockType(Integer lockType) {
        this.lockType = lockType;
    }


    public float getAxisX() {
        return axisX;
    }

    public void setAxisX(float axisX) {
        this.axisX = axisX;
    }

    public float getAxisY() {
        return axisY;
    }

    public void setAxisY(float axisY) {
        this.axisY = axisY;
    }

    public float getAxisZ() {
        return axisZ;
    }

    public void setAxisZ(float axisZ) {
        this.axisZ = axisZ;
    }

    public Float getDeviceTemp() {
        return deviceTemp;
    }

    public void setDeviceTemp(Float deviceTemp) {
        this.deviceTemp = deviceTemp;
    }

    public Float getLightSensor() {
        return lightSensor;
    }

    public void setLightSensor(Float lightSensor) {
        this.lightSensor = lightSensor;
    }

    public Float getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Float batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public Integer getBatteryCharge() {
        return batteryCharge;
    }

    public void setBatteryCharge(Integer batteryCharge) {
        this.batteryCharge = batteryCharge;
    }

    public Float getSolarVoltage() {
        return solarVoltage;
    }

    public void setSolarVoltage(Float solarVoltage) {
        this.solarVoltage = solarVoltage;
    }

    public Long getMileage() {
        return mileage;
    }

    public void setMileage(Long mileage) {
        this.mileage = mileage;
    }

    public Boolean getUsbCharging() {
        return isUsbCharging;
    }

    public void setUsbCharging(Boolean usbCharging) {
        isUsbCharging = usbCharging;
    }

    public Boolean getIsSolarCharging() {
        return isSolarCharging;
    }

    public void setIsSolarCharging(Boolean solarCharging) {
        isSolarCharging = solarCharging;
    }

    public Boolean getIsManagerConfigured1() {
        return isManagerConfigured1;
    }

    public void setIsManagerConfigured1(Boolean managerConfigured1) {
        isManagerConfigured1 = managerConfigured1;
    }

    public Boolean getIsManagerConfigured2() {
        return isManagerConfigured2;
    }

    public void setIsManagerConfigured2(Boolean managerConfigured2) {
        isManagerConfigured2 = managerConfigured2;
    }

    public Boolean getIsManagerConfigured3() {
        return isManagerConfigured3;
    }

    public void setIsManagerConfigured3(Boolean managerConfigured3) {
        isManagerConfigured3 = managerConfigured3;
    }

    public Boolean getIsManagerConfigured4() {
        return isManagerConfigured4;
    }

    public void setIsManagerConfigured4(Boolean managerConfigured4) {
        isManagerConfigured4 = managerConfigured4;
    }

    public int getNetworkSignal() {
        return networkSignal;
    }

    public void setNetworkSignal(int networkSignal) {
        this.networkSignal = networkSignal;
    }

    public Boolean getIopIgnition() {
        return iopIgnition;
    }

    public void setIopIgnition(Boolean iopIgnition) {
        this.iopIgnition = iopIgnition;
    }

    public Integer getSamplingIntervalAccOn() {
        return samplingIntervalAccOn;
    }

    public void setSamplingIntervalAccOn(Integer samplingIntervalAccOn) {
        this.samplingIntervalAccOn = samplingIntervalAccOn;
    }

    public Integer getSamplingIntervalAccOff() {
        return samplingIntervalAccOff;
    }

    public void setSamplingIntervalAccOff(Integer samplingIntervalAccOff) {
        this.samplingIntervalAccOff = samplingIntervalAccOff;
    }

    public Integer getAngleCompensation() {
        return angleCompensation;
    }

    public void setAngleCompensation(Integer angleCompensation) {
        this.angleCompensation = angleCompensation;
    }

    public Integer getDistanceCompensation() {
        return distanceCompensation;
    }

    public void setDistanceCompensation(Integer distanceCompensation) {
        this.distanceCompensation = distanceCompensation;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSelfMac() {
        return selfMac;
    }

    public void setSelfMac(String selfMac) {
        this.selfMac = selfMac;
    }

    public String getAp1Mac() {
        return ap1Mac;
    }

    public void setAp1Mac(String ap1Mac) {
        this.ap1Mac = ap1Mac;
    }

    public int getAp1RSSI() {
        return ap1RSSI;
    }

    public void setAp1RSSI(int ap1RSSI) {
        this.ap1RSSI = ap1RSSI;
    }

    public String getAp2Mac() {
        return ap2Mac;
    }

    public void setAp2Mac(String ap2Mac) {
        this.ap2Mac = ap2Mac;
    }

    public int getAp2RSSI() {
        return ap2RSSI;
    }

    public void setAp2RSSI(int ap2RSSI) {
        this.ap2RSSI = ap2RSSI;
    }

    public String getAp3Mac() {
        return ap3Mac;
    }

    public void setAp3Mac(String ap3Mac) {
        this.ap3Mac = ap3Mac;
    }

    public int getAp3RSSI() {
        return ap3RSSI;
    }

    public void setAp3RSSI(int ap3RSSI) {
        this.ap3RSSI = ap3RSSI;
    }


}
