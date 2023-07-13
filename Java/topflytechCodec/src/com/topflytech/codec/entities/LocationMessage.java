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
    public Integer getOverSpeedLimit() {
        return overSpeedLimit;
    }

    /**
     * Sets over speed limit.
     *
     * @param overSpeedLimit the over speed limit
     */
    public void setOverSpeedLimit(Integer overSpeedLimit) {
        this.overSpeedLimit = overSpeedLimit;
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

    public int getOutput2() {
        return output2;
    }

    public void setOutput2(int output2) {
        this.output2 = output2;
    }

    public int getOutput3() {
        return output3;
    }

    public void setOutput3(int output3) {
        this.output3 = output3;
    }




    public Float getAnalogInput3() {
        return analogInput3;
    }

    public void setAnalogInput3(Float analogInput3) {
        this.analogInput3 = analogInput3;
    }

    public int getInput3() {
        return input3;
    }

    public void setInput3(int input3) {
        this.input3 = input3;
    }

    public int getInput4() {
        return input4;
    }

    public void setInput4(int input4) {
        this.input4 = input4;
    }


    public Boolean isSupportChangeBattery() {
        return supportChangeBattery;
    }

    public void setSupportChangeBattery(Boolean supportChangeBattery) {
        this.supportChangeBattery = supportChangeBattery;
    }

    public int getInput5() {
        return input5;
    }

    public void setInput5(int input5) {
        this.input5 = input5;
    }

    public int getInput6() {
        return input6;
    }

    public void setInput6(int input6) {
        this.input6 = input6;
    }

    public Float getAnalogInput4() {
        return analogInput4;
    }

    public void setAnalogInput4(Float analogInput4) {
        this.analogInput4 = analogInput4;
    }

    public Float getAnalogInput5() {
        return analogInput5;
    }

    public void setAnalogInput5(Float analogInput5) {
        this.analogInput5 = analogInput5;
    }

    public Boolean isSmartUploadSupport() {
        return isSmartUploadSupport;
    }

    public void setIsSmartUploadSupport(Boolean isSmartUploadSupport) {
        this.isSmartUploadSupport = isSmartUploadSupport;
    }

    public Boolean isOutput12V() {
        return output12V;
    }

    public void setOutput12V(Boolean output12V) {
        this.output12V = output12V;
    }

    public Boolean isOutputVout() {
        return outputVout;
    }

    public void setOutputVout(Boolean outputVout) {
        this.outputVout = outputVout;
    }

    public int getInput2() {
        return input2;
    }

    public void setInput2(int input2) {
        this.input2 = input2;
    }

    public int getInput1() {
        return input1;
    }

    public void setInput1(int input1) {
        this.input1 = input1;
    }

    public Integer getRlyMode() {
        return rlyMode;
    }

    public void setRlyMode(Integer rlyMode) {
        this.rlyMode = rlyMode;
    }

    public Integer getSmsLanguageType() {
        return smsLanguageType;
    }

    public void setSmsLanguageType(Integer smsLanguageType) {
        this.smsLanguageType = smsLanguageType;
    }

    public int getSpeakerStatus() {
        return speakerStatus;
    }

    public void setSpeakerStatus(int speakerStatus) {
        this.speakerStatus = speakerStatus;
    }

    public int getRs232PowerOf5V() {
        return rs232PowerOf5V;
    }

    public void setRs232PowerOf5V(int rs232PowerOf5V) {
        this.rs232PowerOf5V = rs232PowerOf5V;
    }

    public int getAccdetSettingStatus() {
        return accdetSettingStatus;
    }

    public void setAccdetSettingStatus(int accdetSettingStatus) {
        this.accdetSettingStatus = accdetSettingStatus;
    }


    public int getOutput1() {
        return output1;
    }

    public void setOutput1(int output1) {
        this.output1 = output1;
    }

    private Boolean gpsWorking = false;//gps is working mode or sleep mode
    private Boolean latlngValid = false;//current is gps data or lbs data
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Double altitude = 0.0;

    private Integer relayStatus = 0;

    private Integer rlyMode = 0;

    private Integer smsLanguageType = 0;
    private Integer antitheftedStatus = 0;
    private Date date;
    private Long IOP = 0L;
    private Boolean iopIgnition = null;
    private Boolean iopPowerCutOff = null;
    private Boolean iopACOn = null;

    private Float speed = 0.0f;
    private Long mileage = 0L;
    private Integer azimuth = 0;


    private String address = "";

    private Float analogInput1 = 0f;
    private Float analogInput2 = 0f;

    private Integer batteryCharge = 0;
    private  Integer dragThreshold = 0;

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
    private Integer overSpeedLimit = 0;
    private Integer samplingIntervalAccOn = 0;
    private Integer samplingIntervalAccOff = 0;
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
    private String smartPowerSettingStatus;
    private String smartPowerOpenStatus;

    private int speakerStatus = 0;
    private int rs232PowerOf5V = 0;
    private int accdetSettingStatus = 0;

    private int input1 = 0;
    private int input2 = 0;
    private int input3 = 0;
    private int input4 = 0;

    private int output1 = 0;
    private int output2;
    private int output3;

    private Float analogInput3 = 0f;

    private int input5 = 0;
    private int input6 = 0;
    private Float analogInput4 = 0f;
    private Float analogInput5 = 0f;

    private Boolean output12V;
    private Boolean outputVout;
    private Boolean isSmartUploadSupport;
    private Boolean supportChangeBattery;

    private Boolean is_4g_lbs = false;
    private Integer mcc_4g;
    private Integer mnc_4g;
    private Long ci_4g;
    private Integer earfcn_4g_1;
    private Integer pcid_4g_1;
    private Integer earfcn_4g_2;
    private Integer pcid_4g_2;
    private Long bci_4g;
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
    private Integer externalPowerReduceStatus;

    private boolean isSendSmsAlarmToManagerPhone = false;
    private boolean isSendSmsAlarmWhenDigitalInput2Change = false;
    private int jammerDetectionStatus = 0;
    private boolean isLockSim = false;
    private boolean isLockDevice = false;
    private boolean AGPSEphemerisDataDownloadSettingStatus = false;
    private boolean gSensorSettingStatus = false;
    private boolean frontSensorSettingStatus = false;
    private boolean deviceRemoveAlarmSettingStatus = false;
    private boolean openCaseAlarmSettingStatus = false;
    private boolean deviceInternalTempReadingANdUploadingSettingStatus = false;


    private float gyroscopeAxisX;
    private float gyroscopeAxisY;
    private float gyroscopeAxisZ;

    private Integer lockType;

    private int ignitionSource;
    private int hasThirdPartyObd;
    private int exPowerConsumpStatus; //0:unknown,1:normal,2abnormal

    private int remainFuelUnit = 0;//0:% ,1:L
    private int mileageSource = 0;//0:GPS 1:ECU,2:FMS
    private boolean isHadFmsData = false;
    private Long fmsEngineHours;
    private Integer hdop;
    private Integer fmsSpeed;// km/h
    private Long fmsAccumulatingFuelConsumption;
    public Long getBci_4g() {
        return bci_4g;
    }

    public void setBci_4g(Long bci_4g) {
        this.bci_4g = bci_4g;
    }

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
    public Long getFmsAccumulatingFuelConsumption() {
        return fmsAccumulatingFuelConsumption;
    }

    public void setFmsAccumulatingFuelConsumption(Long fmsAccumulatingFuelConsumption) {
        this.fmsAccumulatingFuelConsumption = fmsAccumulatingFuelConsumption;
    }

    public Integer getFmsSpeed() {
        return fmsSpeed;
    }

    public void setFmsSpeed(Integer fmsSpeed) {
        this.fmsSpeed = fmsSpeed;
    }

    public boolean isHadFmsData() {
        return isHadFmsData;
    }

    public void setHadFmsData(boolean hadFmsData) {
        isHadFmsData = hadFmsData;
    }

    public Long getFmsEngineHours() {
        return fmsEngineHours;
    }

    public void setFmsEngineHours(Long fmsEngineHours) {
        this.fmsEngineHours = fmsEngineHours;
    }
    public Integer getHdop() {
        return hdop;
    }

    public void setHdop(Integer hdop) {
        this.hdop = hdop;
    }



    public int getMileageSource() {
        return mileageSource;
    }

    public void setMileageSource(int mileageSource) {
        this.mileageSource = mileageSource;
    }


    public int getRemainFuelUnit() {
        return remainFuelUnit;
    }

    public void setRemainFuelUnit(int remainFuelUnit) {
        this.remainFuelUnit = remainFuelUnit;
    }

    public int getIgnitionSource() {
        return ignitionSource;
    }

    public void setIgnitionSource(int ignitionSource) {
        this.ignitionSource = ignitionSource;
    }

    public int getHasThirdPartyObd() {
        return hasThirdPartyObd;
    }

    public void setHasThirdPartyObd(int hasThirdPartyObd) {
        this.hasThirdPartyObd = hasThirdPartyObd;
    }

    public int getExPowerConsumpStatus() {
        return exPowerConsumpStatus;
    }

    public void setExPowerConsumpStatus(int exPowerConsumpStatus) {
        this.exPowerConsumpStatus = exPowerConsumpStatus;
    }



    public Integer getLockType() {
        return lockType;
    }

    public void setLockType(Integer lockType) {
        this.lockType = lockType;
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

    public float getGyroscopeAxisZ() {
        return gyroscopeAxisZ;
    }

    public void setGyroscopeAxisZ(float gyroscopeAxisZ) {
        this.gyroscopeAxisZ = gyroscopeAxisZ;
    }



    public boolean isLockSim() {
        return isLockSim;
    }

    public void setIsLockSim(boolean isLockSim) {
        this.isLockSim = isLockSim;
    }

    public boolean isLockDevice() {
        return isLockDevice;
    }

    public void setIsLockDevice(boolean isLockDevice) {
        this.isLockDevice = isLockDevice;
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



    public boolean isSendSmsAlarmToManagerPhone() {
        return isSendSmsAlarmToManagerPhone;
    }

    public void setIsSendSmsAlarmToManagerPhone(boolean isSendSmsAlarmToManagerPhone) {
        this.isSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone;
    }

    public boolean isSendSmsAlarmWhenDigitalInput2Change() {
        return isSendSmsAlarmWhenDigitalInput2Change;
    }

    public void setIsSendSmsAlarmWhenDigtalInput2Change(boolean isSendSmsAlarmWhenDigtalInput2Change) {
        this.isSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigtalInput2Change;
    }

    public int getJammerDetectionStatus() {
        return jammerDetectionStatus;
    }

    public void setJammerDetectionStatus(int jammerDetectionStatus) {
        this.jammerDetectionStatus = jammerDetectionStatus;
    }

    public Integer getExternalPowerReduceStatus() {
        return externalPowerReduceStatus;
    }

    public void setExternalPowerReduceStatus(Integer externalPowerReduceStatus) {
        this.externalPowerReduceStatus = externalPowerReduceStatus;
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
