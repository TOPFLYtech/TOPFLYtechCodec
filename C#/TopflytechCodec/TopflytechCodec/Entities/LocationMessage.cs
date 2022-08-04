using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class LocationMessage : Message
    { 
        private bool gpsWorking = false;
        /// <summary>
        ///  Is gps working or sleeping. when gps working ,return true, otherwise return false.
        /// </summary>
        public bool GpsWorking
        {
            get { return gpsWorking; }
            set { gpsWorking = value; }
        }
        private bool latlngValid = false;
        /// <summary>
        /// Is GPS data or LBS data. when GPS data,return true.
        /// </summary>
        public bool LatlngValid
        {
            get { return latlngValid; }
            set { latlngValid = value; }
        }
        private double latitude = 0.0;
        public double Latitude
        {
            get { return latitude; }
            set { latitude = value; }
        }
      
        private double longitude = 0.0;
        public double Longitude
        {
            get { return longitude; }
            set { longitude = value; }
        }
        private double altitude = 0.0;
        public double Altitude
        {
            get { return altitude; }
            set { altitude = value; }
        }
        private int relayStatus = 0;
        /// <summary>
        ///  Gets relay status.When in the oil off state, return 1,otherwise return 0.
        /// </summary>
        public int RelayStatus
        {
            get { return relayStatus; }
            set { relayStatus = value; }
        }
        private int antitheftedStatus = 0;
        /// <summary>
        /// Gets antithefted status.When in the anti-theft state, return 1,otherwire return 0.
        /// </summary>
        public int AntitheftedStatus
        {
            get { return antitheftedStatus; }
            set { antitheftedStatus = value; }
        }
        private DateTime date;     
        /// <summary>
        /// Gets date.The message snapshot time
        /// </summary>
     
        public System.DateTime Date
        {
            get { return date; }
            set { date = value; }
        }
        private long iop = 0L;   
        /// <summary>
        /// Gets iop.The Digital I/O Status
        /// </summary>
  
        public long IOP
        {
            get { return iop; }
            set { iop = value; }
        }
        private bool iopIgnition = false;
        /// <summary>
        /// The vehicle ignition state obtained from the digital I / O status.when ignition return true.
        /// </summary>
        public bool IopIgnition
        {
            get { return iopIgnition; }
            set { iopIgnition = value; }
        }
        private bool iopPowerCutOff = false;
        /// <summary>
        /// The external power supply connection status of the vehicle obtained from the digital I / O status.If connected,return true.
        /// </summary>
        public bool IopPowerCutOff
        {
            get { return iopPowerCutOff; }
            set { iopPowerCutOff = value; }
        }
        private bool iopACOn = false;
        /// <summary>
        ///  The air conditioning status of the vehicle obtained from the digital I / O status.If opened,retrun true.
        /// </summary>
        public bool IopACOn
        {
            get { return iopACOn; }
            set { iopACOn = value; }
        }
        private float speed = 0.0f;  
        /// <summary>
        /// The speed.The unit is km / h
        /// </summary>
        public float Speed
        {
            get { return speed; }
            set { speed = value; }
        }
        private long mileage = 0L;    
        /// <summary>
        /// The vehicle current mileage.The unit is meter.
        /// </summary>
        public long Mileage
        {
            get { return mileage; }
            set { mileage = value; }
        }
        private int azimuth = 0;   
        /// <summary>
        /// The vehicle current azimuth.
        /// </summary>
        public int Azimuth
        {
            get { return azimuth; }
            set { azimuth = value; }
        }
   
         
        private float analogInput1 = 0f;   
        /// <summary>
        /// The analog input 1.
        /// </summary>
        public float AnalogInput1
        {
            get { return analogInput1; }
            set { analogInput1 = value; }
        }
        private float analogInput2 = 0f;
        /// <summary>
        /// The analog input 2.
        /// </summary>
        public float AnalogInput2
        {
            get { return analogInput2; }
            set { analogInput2 = value; }
        }
        private int batteryCharge = 0;  
        public int BatteryCharge
        {
            get { return batteryCharge; }
            set { batteryCharge = value; }
        }
        private int dragThreshold = 0; //880x
        public int DragThreshold
        {
            get { return dragThreshold; }
            set { dragThreshold = value; }
        }
        private float externalPowerVoltage = 0f;
        /// <summary>
        /// The external power voltage.The Old vehicle has not this value.The new one has this value.
        /// </summary>
        public float ExternalPowerVoltage
        {
            get { return externalPowerVoltage; }
            set { externalPowerVoltage = value; }
        }
        private int originalAlarmCode;
        /// <summary>
        ///  The original alarm code.Response to the device's alert message requires it.
        /// </summary>
        public int OriginalAlarmCode
        {
            get { return originalAlarmCode; }
            set { originalAlarmCode = value; }
        }
         
  
        private bool isRelayWaiting = false;
        /// <summary>
        /// Is relay waiting boolean.If engine cut fail,the vehicle maybe waiting ,if is waiting cut off engine,return true.
        /// </summary>
        public bool IsRelayWaiting
        {
            get { return isRelayWaiting; }
            set { isRelayWaiting = value; }
        }
        private int heartbeatInterval = 0;
        public int HeartbeatInterval
        {
            get { return heartbeatInterval; }
            set { heartbeatInterval = value; }
        }
        private bool isManagerConfigured1 = false;
        /// <summary>
        /// Is Gsnesor manager configured 1 boolean.
        /// </summary>
        public bool IsManagerConfigured1
        {
            get { return isManagerConfigured1; }
            set { isManagerConfigured1 = value; }
        }
        private bool isManagerConfigured2 = false;
        /// <summary>
        /// Is Gsnesor manager configured 2 boolean.
        /// </summary>
        public bool IsManagerConfigured2
        {
            get { return isManagerConfigured2; }
            set { isManagerConfigured2 = value; }
        }
        private bool isManagerConfigured3 = false;
        /// <summary>
        /// Is Gsnesor manager configured 3 boolean.
        /// </summary>
        public bool IsManagerConfigured3
        {
            get { return isManagerConfigured3; }
            set { isManagerConfigured3 = value; }
        }
        private bool isManagerConfigured4 = false;
        /// <summary>
        /// Is Gsnesor manager configured 4 boolean.
        /// </summary>
        public bool IsManagerConfigured4
        {
            get { return isManagerConfigured4; }
            set { isManagerConfigured4 = value; }
        }
        private int gSensorSensitivity = 0;
        public int GSensorSensitivity
        {
            get { return gSensorSensitivity; }
            set { gSensorSensitivity = value; }
        }
        private bool isHistoryData = false;
        public bool IsHistoryData
        {
            get { return isHistoryData; }
            set { isHistoryData = value; }
        }
        private int satelliteNumber;
        public int SatelliteNumber
        {
            get { return satelliteNumber; }
            set { satelliteNumber = value; }
        }
        private int overspeedLimit = 0;
        /// <summary>
        ///  The over speed limit.The unit is km / h
        /// </summary>
        public int OverspeedLimit
        {
            get { return overspeedLimit; }
            set { overspeedLimit = value; }
        }
        private int samplingIntervalAccOn = 0;
        /// <summary>
        /// The acc on upload interval.
        /// </summary>
        public int SamplingIntervalAccOn
        {
            get { return samplingIntervalAccOn; }
            set { samplingIntervalAccOn = value; }
        }
        private int samplingIntervalAccOff = 0;
        /// <summary>
        /// The acc off upload interval.
        /// </summary>
        public int SamplingIntervalAccOff
        {
            get { return samplingIntervalAccOff; }
            set { samplingIntervalAccOff = value; }
        }
        private int angleCompensation = 0;
        public int AngleCompensation
        {
            get { return angleCompensation; }
            set { angleCompensation = value; }
        }
        private int distanceCompensation = 0;
        public int DistanceCompensation
        {
            get { return distanceCompensation; }
            set { distanceCompensation = value; }
        }

        private long accumulatingFuelConsumption;

        public long AccumulatingFuelConsumption
        {
            get { return accumulatingFuelConsumption; }
            set { accumulatingFuelConsumption = value; }
        }
        private long instantFuelConsumption;

        public long InstantFuelConsumption
        {
            get { return instantFuelConsumption; }
            set { instantFuelConsumption = value; }
        }
        private int rpm;

        public int Rpm
        {
            get { return rpm; }
            set { rpm = value; }
        }
        private int airInput;

        public int AirInput
        {
            get { return airInput; }
            set { airInput = value; }
        }
        private int airPressure;

        public int AirPressure
        {
            get { return airPressure; }
            set { airPressure = value; }
        }
        private int coolingFluidTemp;

        public int CoolingFluidTemp
        {
            get { return coolingFluidTemp; }
            set { coolingFluidTemp = value; }
        }
        private int airInflowTemp;

        public int AirInflowTemp
        {
            get { return airInflowTemp; }
            set { airInflowTemp = value; }
        }
        private int engineLoad;

        public int EngineLoad
        {
            get { return engineLoad; }
            set { engineLoad = value; }
        }
        private int throttlePosition;

        public int ThrottlePosition
        {
            get { return throttlePosition; }
            set { throttlePosition = value; }
        }
        private int remainFuelRate;

        public int RemainFuelRate
        {
            get { return remainFuelRate; }
            set { remainFuelRate = value; }
        }

        private bool rs232DeviceValid = false;
        public bool Rs232DeviceValid
        {
            get { return rs232DeviceValid; }
            set { rs232DeviceValid = value; }
        }

        private int networkSignal;

        public int NetworkSignal
        {
            get { return networkSignal; }
            set { networkSignal = value; }
        }

        //personal asset special
        private float axisX;

        public float AxisX
        {
            get { return axisX; }
            set { axisX = value; }
        }
        private float axisY;

        public float AxisY
        {
            get { return axisY; }
            set { axisY = value; }
        }
        private float axisZ;

        public float AxisZ
        {
            get { return axisZ; }
            set { axisZ = value; }
        }
        private float deviceTemp;

        public float DeviceTemp
        {
            get { return deviceTemp; }
            set { deviceTemp = value; }
        }
        private float lightSensor;

        public float LightSensor
        {
            get { return lightSensor; }
            set { lightSensor = value; }
        }
        private float batteryVoltage;

        public float BatteryVoltage
        {
            get { return batteryVoltage; }
            set { batteryVoltage = value; }
        }
        private float solarVoltage;

        public float SolarVoltage
        {
            get { return solarVoltage; }
            set { solarVoltage = value; }
        }
        private Boolean isUsbCharging;

        public Boolean IsUsbCharging
        {
            get { return isUsbCharging; }
            set { isUsbCharging = value; }
        }
        private Boolean isSolarCharging;

        public Boolean IsSolarCharging
        {
            get { return isSolarCharging; }
            set { isSolarCharging = value; }
        }

        private String smartPowerSettingStatus;
        public System.String SmartPowerSettingStatus
        {
            get { return smartPowerSettingStatus; }
            set { smartPowerSettingStatus = value; }
        }
        private String smartPowerOpenStatus;
        public System.String SmartPowerOpenStatus
        {
            get { return smartPowerOpenStatus; }
            set { smartPowerOpenStatus = value; }
        }
        private int output1;
        public int Output1
        {
            get { return output1; }
            set { output1 = value; }
        }
        private int output2;
        public int Output2
        {
            get { return output2; }
            set { output2 = value; }
        }
        private int output3;
        public int Output3
        {
            get { return output3; }
            set { output3 = value; }
        }
        private Boolean output12V;
        public Boolean Output12V
        {
            get { return output12V; }
            set { output12V = value; }
        }
        private Boolean outputVout;
        public Boolean OutputVout
        {
            get { return outputVout; }
            set { outputVout = value; }
        }
        private float analogInput3 = 0f;
        public float AnalogInput3
        {
            get { return analogInput3; }
            set { analogInput3 = value; }
        }
        private int input1 = 0;
        public int Input1
        {
            get { return input1; }
            set { input1 = value; }
        }
        private int input2 = 0;
        public int Input2
        {
            get { return input2; }
            set { input2 = value; }
        }

        private int input3 = 0;
        public int Input3
        {
            get { return input3; }
            set { input3 = value; }
        }
        private int input4 = 0;
        public int Input4
        {
            get { return input4; }
            set { input4 = value; }
        }
        private int input5 = 0;
        public int Input5
        {
            get { return input5; }
            set { input5 = value; }
        }
        private int input6 = 0;
        public int Input6
        {
            get { return input6; }
            set { input6 = value; }
        }
        private float analogInput4 = 0f;
        public float AnalogInput4
        {
            get { return analogInput4; }
            set { analogInput4 = value; }
        }
        private float analogInput5 = 0f;
        public float AnalogInput5
        {
            get { return analogInput5; }
            set { analogInput5 = value; }
        }
        private Boolean isSmartUploadSupport;
        public Boolean IsSmartUploadSupport
        {
            get { return isSmartUploadSupport; }
            set { isSmartUploadSupport = value; }
        }
        private Boolean supportChangeBattery;
        public Boolean SupportChangeBattery
        {
            get { return supportChangeBattery; }
            set { supportChangeBattery = value; }
        }

        private Boolean is_4g_lbs = false;
        public Boolean Is_4g_lbs
        {
            get { return is_4g_lbs; }
            set { is_4g_lbs = value; }
        }
        private Int32 mcc_4g;
        public Int32 Mcc_4g
        {
            get { return mcc_4g; }
            set { mcc_4g = value; }
        }
        private Int32 mnc_4g;
        public Int32 Mnc_4g
        {
            get { return mnc_4g; }
            set { mnc_4g = value; }
        }
        private Int64 ci_4g;
        public Int64 Ci_4g
        {
            get { return ci_4g; }
            set { ci_4g = value; }
        }
        private Int32 earfcn_4g_1;
        public Int32 Earfcn_4g_1
        {
            get { return earfcn_4g_1; }
            set { earfcn_4g_1 = value; }
        }
        private Int32 pcid_4g_1;
        public Int32 Pcid_4g_1
        {
            get { return pcid_4g_1; }
            set { pcid_4g_1 = value; }
        }
        private Int32 earfcn_4g_2;
        public Int32 Earfcn_4g_2
        {
            get { return earfcn_4g_2; }
            set { earfcn_4g_2 = value; }
        }
        private Int32 pcid_4g_2;
        public Int32 Pcid_4g_2
        {
            get { return pcid_4g_2; }
            set { pcid_4g_2 = value; }
        }


        private Boolean is_2g_lbs = false;
        public Boolean Is_2g_lbs
        {
            get { return is_2g_lbs; }
            set { is_2g_lbs = value; }
        }
        private Int32 mcc_2g;
        public Int32 Mcc_2g
        {
            get { return mcc_2g; }
            set { mcc_2g = value; }
        }
        private Int32 mnc_2g;
        public Int32 Mnc_2g
        {
            get { return mnc_2g; }
            set { mnc_2g = value; }
        }
        private Int32 lac_2g_1;
        public Int32 Lac_2g_1
        {
            get { return lac_2g_1; }
            set { lac_2g_1 = value; }
        }
        private Int32 ci_2g_1;
        public Int32 Ci_2g_1
        {
            get { return ci_2g_1; }
            set { ci_2g_1 = value; }
        }
        private Int32 lac_2g_2;
        public Int32 Lac_2g_2
        {
            get { return lac_2g_2; }
            set { lac_2g_2 = value; }
        }
        private Int32 ci_2g_2;
        public Int32 Ci_2g_2
        {
            get { return ci_2g_2; }
            set { ci_2g_2 = value; }
        }
        private Int32 lac_2g_3;
        public Int32 Lac_2g_3
        {
            get { return lac_2g_3; }
            set { lac_2g_3 = value; }
        }
        private Int32 ci_2g_3;
        public Int32 Ci_2g_3
        {
            get { return ci_2g_3; }
            set { ci_2g_3 = value; }
        }

        private Int32 rlyMode = 0;
        public Int32 RlyMode
        {
            get { return rlyMode;}
            set{rlyMode = value; }
        }

        private Int32 smsLanguageType = 0;
        public Int32 SmsLanguageType
        {
            get { return smsLanguageType;}
            set{smsLanguageType = value; }
        }

        private Int32 speakerStatus = 0;
         public Int32 SpeakerStatus
        {
            get { return speakerStatus;}
            set{speakerStatus = value; }
        }
        private Int32 rs232PowerOf5V = 0;
         public Int32 Rs232PowerOf5V
        {
            get { return rs232PowerOf5V;}
            set{rs232PowerOf5V = value; }
        }
        private Int32 accdetSettingStatus = 0;
        public Int32 AccdetSettingStatus
        {
            get { return accdetSettingStatus;}
            set{accdetSettingStatus = value; }
        }

        private bool isSendSmsAlarmToManagerPhone = false;
        public Boolean IsSendSmsAlarmToManagerPhone
        {
            get { return isSendSmsAlarmToManagerPhone; }
            set { isSendSmsAlarmToManagerPhone = value; }
        }
        private bool isSendSmsAlarmWhenDigitalInput2Change = false;
        public Boolean IsSendSmsAlarmWhenDigitalInput2Change
        {
            get { return isSendSmsAlarmWhenDigitalInput2Change; }
            set { isSendSmsAlarmWhenDigitalInput2Change = value; }
        }
        private Int32 jammerDetectionStatus = 0;
        public Int32 JammerDetectionStatus
        {
            get { return jammerDetectionStatus;}
            set{jammerDetectionStatus = value; }
        }
        private bool isLockSim = false;
        public Boolean IsLockSim
        {
            get { return isLockSim; }
            set { isLockSim = value; }
        }
        private bool isLockDevice = false;
        public Boolean IsLockDevice
        {
            get { return isLockDevice; }
            set { isLockDevice = value; }
        }
        private bool aGPSEphemerisDataDownloadSettingStatus = false;
        public Boolean AGPSEphemerisDataDownloadSettingStatus
        {
            get { return aGPSEphemerisDataDownloadSettingStatus; }
            set { aGPSEphemerisDataDownloadSettingStatus = value; }
        }
        private bool gSensorSettingStatus = false;
        public Boolean GSensorSettingStatus
        {
            get { return gSensorSettingStatus; }
            set { gSensorSettingStatus = value; }
        }
        private bool frontSensorSettingStatus = false;
        public Boolean FrontSensorSettingStatus
        {
            get { return frontSensorSettingStatus; }
            set { frontSensorSettingStatus = value; }
        }
        private bool deviceRemoveAlarmSettingStatus = false;
        public Boolean DeviceRemoveAlarmSettingStatus
        {
            get { return deviceRemoveAlarmSettingStatus; }
            set { deviceRemoveAlarmSettingStatus = value; }
        }
        private bool openCaseAlarmSettingStatus = false;
        public Boolean OpenCaseAlarmSettingStatus
        {
            get { return openCaseAlarmSettingStatus; }
            set { openCaseAlarmSettingStatus = value; }
        }
        private bool deviceInternalTempReadingANdUploadingSettingStatus = false;
        public Boolean DeviceInternalTempReadingANdUploadingSettingStatus
        {
            get { return deviceInternalTempReadingANdUploadingSettingStatus; }
            set { deviceInternalTempReadingANdUploadingSettingStatus = value; }
        }

        private float gyroscopeAxisX;
        public float GyroscopeAxisX
        {
            get { return gyroscopeAxisX; }
            set { gyroscopeAxisX = value; }
        }
        private float gyroscopeAxisY;
        public float GyroscopeAxisY
        {
            get { return gyroscopeAxisY; }
            set { gyroscopeAxisY = value; }
        }
        private float gyroscopeAxisZ;
        public float GyroscopeAxisZ
        {
            get { return gyroscopeAxisZ; }
            set { gyroscopeAxisZ = value; }
        }

        private int lockType;
        public int LockType
        {
            get { return lockType; }
            set { lockType = value; }
        }

        private Int32 hasThirdPartyObd; //0,1
        public Int32 HasThirdPartyObd
        {
            get { return hasThirdPartyObd; }
            set { hasThirdPartyObd = value; }
        }

        private Int32 ignitionSource;
        public Int32 IgnitionSource
        {
            get { return ignitionSource; }
            set { ignitionSource = value; }
        }

        private Int32 exPowerConsumpStatus;//0:unknown,1:normal,2abnormal
        public Int32 ExPowerConsumpStatus
        {
            get { return exPowerConsumpStatus; }
            set { exPowerConsumpStatus = value; }
        }
 

        private int remainFuelUnit = 0; // 0:% 1:L
        public Int32 RemainFuelUnit
        {
            get { return remainFuelUnit; }
            set { remainFuelUnit = value; }
        }

        private int mileageSource = 0;//0:GPS; 1:ECU
        public Int32 MileageSource
        {
            get { return mileageSource; }
            set { mileageSource = value; }
        }
    }
}
