package com.topflytech.codec.entities;

import java.util.Date;


/**
 * The type Location message.
 */
public class LocationMessage extends Message{
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
     * Gets latitude.
     *
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets longitude.
     *
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets altitude.
     *
     * @return the altitude
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * Sets altitude.
     *
     * @param altitude the altitude
     */
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets relay status.When in the oil off state, return 1,otherwise return 0.
     *
     * @return the relay status
     */
    public Integer getRelayStatus() {
        return relayStatus;
    }

    /**
     * Sets relay status.
     *
     * @param relayStatus the relay status
     */
    public void setRelayStatus(Integer relayStatus) {
        this.relayStatus = relayStatus;
    }

    /**
     * Gets antithefted status.When in the anti-theft state, return 1,otherwire return 0.
     *
     * @return the antithefted status
     */
    public Integer getAntitheftedStatus() {
        return antitheftedStatus;
    }

    /**
     * Sets antithefted status.
     *
     * @param antitheftedStatus the antithefted status
     */
    public void setAntitheftedStatus(Integer antitheftedStatus) {
        this.antitheftedStatus = antitheftedStatus;
    }

    /**
     * Gets date.The message snapshot time
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
     * Gets iop.The Digital I/O Status
     *
     * @return the iop
     */
    public Long getIOP() {
        return IOP;
    }

    /**
     * Sets iop.
     *
     * @param IOP the iop
     */
    public void setIOP(Long IOP) {
        this.IOP = IOP;
    }

    /**
     * The vehicle ignition state obtained from the digital I / O status.when ignition return true.
     *
     * @return the boolean
     */
    public Boolean isIopIgnition() {
        return iopIgnition;
    }

    /**
     * Sets iop ignition.
     *
     * @param iopIgnition the iop ignition
     */
    public void setIopIgnition(Boolean iopIgnition) {
        this.iopIgnition = iopIgnition;
    }

    /**
     * The external power supply connection status of the vehicle obtained from the digital I / O status.If connected,return true.
     *
     * @return the boolean
     */
    public Boolean isIopPowerCutOff() {
        return iopPowerCutOff;
    }

    /**
     * Sets iop power cut off.
     *
     * @param iopPowerCutOff the iop power cut off
     */
    public void setIopPowerCutOff(Boolean iopPowerCutOff) {
        this.iopPowerCutOff = iopPowerCutOff;
    }

    /**
     * The air conditioning status of the vehicle obtained from the digital I / O status.If opened,retrun true.
     *
     * @return the boolean
     */
    public Boolean isIopACOn() {
        return iopACOn;
    }

    /**
     * Sets iop ac on.
     *
     * @param iopACOn the iop ac on
     */
    public void setIopACOn(Boolean iopACOn) {
        this.iopACOn = iopACOn;
    }

    /**
     * Gets speed.The unit is km / h
     *
     * @return the speed
     */
    public Float getSpeed() {
        return speed;
    }

    /**
     * Sets speed.
     *
     * @param speed the speed
     */
    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    /**
     * Gets the vehicle current mileage.The unit is meter.
     *
     * @return the mileage
     */
    public Long getMileage() {
        return mileage;
    }

    /**
     * Sets mileage.
     *
     * @param mileage the mileage
     */
    public void setMileage(Long mileage) {
        this.mileage = mileage;
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
     * Gets alarm code.
     *
     * @return the alarm
     */
    public int getAlarm() {
        return alarm;
    }

    /**
     * Sets alarm.
     *
     * @param alarm the alarm
     */
    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    /**
     * Gets address description.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets address.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * Gets analog input 1.
     *
     * @return the analog input 1
     */
    public Float getAnalogInput1() {
        return analogInput1;
    }

    /**
     * Sets analog input 1.
     *
     * @param analogInput1 the analog input 1
     */
    public void setAnalogInput1(Float analogInput1) {
        this.analogInput1 = analogInput1;
    }

    /**
     * Gets analog input 2.
     *
     * @return the analog input 2
     */
    public Float getAnalogInput2() {
        return analogInput2;
    }

    /**
     * Sets analog input 2.
     *
     * @param analogInput2 the analog input 2
     */
    public void setAnalogInput2(Float analogInput2) {
        this.analogInput2 = analogInput2;
    }

    /**
     * Gets battery charge.
     *
     * @return the battery charge
     */
    public Integer getBatteryCharge() {
        return batteryCharge;
    }

    /**
     * Sets battery charge.
     *
     * @param batteryCharge the battery charge
     */
    public void setBatteryCharge(Integer batteryCharge) {
        this.batteryCharge = batteryCharge;
    }

    /**
     * Gets drag threshold.
     *
     * @return the drag threshold
     */
    public Integer getDragThreshold() {
        return dragThreshold;
    }

    /**
     * Sets drag threshold.
     *
     * @param dragThreshold the drag threshold
     */
    public void setDragThreshold(Integer dragThreshold) {
        this.dragThreshold = dragThreshold;
    }

    /**
     * Gets external power voltage.The Old vehicle has not this value.The new one has this value.
     *
     * @return the external power voltage
     */
    public Float getExternalPowerVoltage() {
        return externalPowerVoltage;
    }

    /**
     * Sets external power voltage.
     *
     * @param externalPowerVoltage the external power voltage
     */
    public void setExternalPowerVoltage(Float externalPowerVoltage) {
        this.externalPowerVoltage = externalPowerVoltage;
    }

    /**
     * Gets original alarm code.Response to the device's alert message requires it.
     *
     * @return the original alarm code
     */
    public Integer getOriginalAlarmCode() {
        return originalAlarmCode;
    }

    /**
     * Sets original alarm code.
     *
     * @param originalAlarmCode the original alarm code
     */
    public void setOriginalAlarmCode(Integer originalAlarmCode) {
        this.originalAlarmCode = originalAlarmCode;
    }

    /**
     * Is relay waiting boolean.If engine cut fail,the vehicle maybe waiting ,if is waiting cut off engine,return true.
     *
     * @return the boolean
     */
    public Boolean isRelayWaiting() {
        return isRelayWaiting;
    }

    /**
     * Sets is relay waiting.
     *
     * @param isRelayWaiting the is relay waiting
     */
    public void setIsRelayWaiting(Boolean isRelayWaiting) {
        this.isRelayWaiting = isRelayWaiting;
    }

    /**
     * Gets heartbeat interval.
     *
     * @return the heartbeat interval
     */
    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * Sets heartbeat interval.
     *
     * @param heartbeatInterval the heartbeat interval
     */
    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    /**
     * Is Gsnesor manager configured 1 boolean.
     *
     * @return the boolean
     */
    public Boolean isManagerConfigured1() {
        return isManagerConfigured1;
    }

    /**
     * Sets is manager configured 1.
     *
     * @param isManagerConfigured1 the is manager configured 1
     */
    public void setIsManagerConfigured1(Boolean isManagerConfigured1) {
        this.isManagerConfigured1 = isManagerConfigured1;
    }

    /**
     * Is Gsnesor manager configured 2 boolean.
     *
     * @return the boolean
     */
    public Boolean isManagerConfigured2() {
        return isManagerConfigured2;
    }

    /**
     * Sets is manager configured 2.
     *
     * @param isManagerConfigured2 the is manager configured 2
     */
    public void setIsManagerConfigured2(Boolean isManagerConfigured2) {
        this.isManagerConfigured2 = isManagerConfigured2;
    }

    /**
     * Is Gsnesor manager configured 3 boolean.
     *
     * @return the boolean
     */
    public Boolean isManagerConfigured3() {
        return isManagerConfigured3;
    }

    /**
     * Sets is manager configured 3.
     *
     * @param isManagerConfigured3 the is manager configured 3
     */
    public void setIsManagerConfigured3(Boolean isManagerConfigured3) {
        this.isManagerConfigured3 = isManagerConfigured3;
    }

    /**
     * Is Gsnesor manager configured 4 boolean.
     *
     * @return the boolean
     */
    public Boolean isManagerConfigured4() {
        return isManagerConfigured4;
    }

    /**
     * Sets is manager configured 4.
     *
     * @param isManagerConfigured4 the is manager configured 4
     */
    public void setIsManagerConfigured4(Boolean isManagerConfigured4) {
        this.isManagerConfigured4 = isManagerConfigured4;
    }

    /**
     * Gets sensor sensitivity.
     *
     * @return the sensor sensitivity
     */
    public Integer getgSensorSensitivity() {
        return gSensorSensitivity;
    }

    /**
     * Sets sensor sensitivity.
     *
     * @param gSensorSensitivity the g sensor sensitivity
     */
    public void setgSensorSensitivity(Integer gSensorSensitivity) {
        this.gSensorSensitivity = gSensorSensitivity;
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
     * Gets over speed limit.The unit is km / h
     *
     * @return the over speed limit
     */
    public Integer getOverspeedLimit() {
        return overspeedLimit;
    }

    /**
     * Sets over speed limit.
     *
     * @param overspeedLimit the over speed limit
     */
    public void setOverspeedLimit(Integer overspeedLimit) {
        this.overspeedLimit = overspeedLimit;
    }

    /**
     * Gets acc on upload interval.
     *
     * @return the sampling interval acc on
     */
    public Integer getSamplingIntervalAccOn() {
        return samplingIntervalAccOn;
    }

    /**
     * Sets sampling interval acc on.
     *
     * @param samplingIntervalAccOn the sampling interval acc on
     */
    public void setSamplingIntervalAccOn(Integer samplingIntervalAccOn) {
        this.samplingIntervalAccOn = samplingIntervalAccOn;
    }

    /**
     * Gets acc off upload interval.
     *
     * @return the sampling interval acc off
     */
    public Integer getSamplingIntervalAccOff() {
        return samplingIntervalAccOff;
    }

    /**
     * Sets sampling interval acc off.
     *
     * @param samplingIntervalAccOff the sampling interval acc off
     */
    public void setSamplingIntervalAccOff(Integer samplingIntervalAccOff) {
        this.samplingIntervalAccOff = samplingIntervalAccOff;
    }

    /**
     * Gets angle compensation.
     *
     * @return the angle compensation
     */
    public Integer getAngleCompensation() {
        return angleCompensation;
    }

    /**
     * Sets angle compensation.
     *
     * @param angleCompensation the angle compensation
     */
    public void setAngleCompensation(Integer angleCompensation) {
        this.angleCompensation = angleCompensation;
    }

    /**
     * Gets distance compensation.
     *
     * @return the distance compensation
     */
    public Integer getDistanceCompensation() {
        return distanceCompensation;
    }

    /**
     * Sets distance compensation.
     *
     * @param distanceCompensation the distance compensation
     */
    public void setDistanceCompensation(Integer distanceCompensation) {
        this.distanceCompensation = distanceCompensation;
    }

    public long getAccumulatingFuelConsumption() {
        return accumulatingFuelConsumption;
    }

    public void setAccumulatingFuelConsumption(long accumulatingFuelConsumption) {
        this.accumulatingFuelConsumption = accumulatingFuelConsumption;
    }

    public long getInstantFuelConsumption() {
        return instantFuelConsumption;
    }

    public void setInstantFuelConsumption(long instantFuelConsumption) {
        this.instantFuelConsumption = instantFuelConsumption;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getAirInput() {
        return airInput;
    }

    public void setAirInput(int airInput) {
        this.airInput = airInput;
    }

    public int getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(int airPressure) {
        this.airPressure = airPressure;
    }

    public int getCoolingFluidTemp() {
        return coolingFluidTemp;
    }

    public void setCoolingFluidTemp(int coolingFluidTemp) {
        this.coolingFluidTemp = coolingFluidTemp;
    }

    public int getAirInflowTemp() {
        return airInflowTemp;
    }

    public void setAirInflowTemp(int airInflowTemp) {
        this.airInflowTemp = airInflowTemp;
    }

    public int getEngineLoad() {
        return engineLoad;
    }

    public void setEngineLoad(int engineLoad) {
        this.engineLoad = engineLoad;
    }

    public int getThrottlePosition() {
        return throttlePosition;
    }

    public void setThrottlePosition(int throttlePosition) {
        this.throttlePosition = throttlePosition;
    }

    public int getRemainFuelRate() {
        return remainFuelRate;
    }

    public void setRemainFuelRate(int remainFuelRate) {
        this.remainFuelRate = remainFuelRate;
    }


    public boolean isRs232DeviceValid() {
        return rs232DeviceValid;
    }

    public void setRs232DeviceValid(boolean rs232DeviceValid) {
        this.rs232DeviceValid = rs232DeviceValid;
    }

    public int getNetworkSignal() {
        return networkSignal;
    }

    public void setNetworkSignal(int networkSignal) {
        this.networkSignal = networkSignal;
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

    public Float getSolarVoltage() {
        return solarVoltage;
    }

    public void setSolarVoltage(Float solarVoltage) {
        this.solarVoltage = solarVoltage;
    }

    public Boolean isUsbCharging() {
        return isUsbCharging;
    }

    public void setIsUsbCharging(Boolean isUsbCharging) {
        this.isUsbCharging = isUsbCharging;
    }

    public Boolean isSolarCharging() {
        return isSolarCharging;
    }

    public void setIsSolarCharging(Boolean isSolarCharging) {
        this.isSolarCharging = isSolarCharging;
    }

    private Boolean gpsWorking = false;//gps is working mode or sleep mode
    private Boolean latlngValid = false;//current is gps data or lbs data
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;

    private Integer relayStatus = 0;
    private Integer antitheftedStatus = 0;
    private Date date;                  //这个点的时间
    private Long IOP = 0L;              //报警信息
    private Boolean iopIgnition = null;
    private Boolean iopPowerCutOff = null;
    private Boolean iopACOn = null;

    private Float speed = 0.0f;         //速度
    private Long mileage = 0L;          //里程
    private Integer azimuth = 0;        //方位角

    private int alarm = 0;            //报警类型
    private String address = "";        //地址

    private Float analogInput1 = 0f;    //模拟1
    private Float analogInput2 = 0f;    //模拟2

    private Integer batteryCharge = 0; //880x的 要存入mysql，所以需要加到这
    private  Integer dragThreshold = 0; //880x

    private Float externalPowerVoltage = 0f;
    private Integer originalAlarmCode;


//    protected Integer relaySpeedLimit = 0;
    private Boolean isRelayWaiting = false;
    private Integer heartbeatInterval = 0;
    private Boolean isManagerConfigured1 = false;
    private Boolean isManagerConfigured2 = false;
    private Boolean isManagerConfigured3 = false;
    private Boolean isManagerConfigured4 = false;
    private Integer gSensorSensitivity = 0;
    private Boolean isHistoryData = false;
    private Integer satelliteNumber;
    private Integer overspeedLimit = 0;
    private Integer samplingIntervalAccOn = 0;//ACC_ON上传数据间隔？
    private Integer samplingIntervalAccOff = 0;//ACC_OFF上传数据间隔？
    private Integer angleCompensation = 0;
    private Integer distanceCompensation = 0;


    //odb special message
    private long accumulatingFuelConsumption;
    private long instantFuelConsumption;
    private int rpm;
    private int airInput;
    private int airPressure;
    private int coolingFluidTemp;
    private int airInflowTemp;
    private int engineLoad;
    private int throttlePosition;
    private int remainFuelRate;
    private boolean rs232DeviceValid = false;

    private int networkSignal;

    //personal asset special
    private float axisX;
    private float axisY;
    private float axisZ;
    private Float deviceTemp;
    private Float lightSensor;
    private Float batteryVoltage;
    private Float solarVoltage;
    private Boolean isUsbCharging;
    private Boolean isSolarCharging;
}
