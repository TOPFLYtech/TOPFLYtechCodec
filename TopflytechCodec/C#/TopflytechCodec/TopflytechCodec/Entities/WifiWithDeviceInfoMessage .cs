using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace TopflytechCodec.Entities
{
    public class WifiWithDeviceInfoMessage : Message
    {
        private DateTime date;
        private string selfMac;
        private string ap1Mac;
        private int ap1RSSI;
        private string ap2Mac;
        private int ap2RSSI;
        private string ap3Mac;
        private int ap3RSSI;

        private float axisX;
        private float axisY;
        private float axisZ;
        private float? deviceTemp;
        private float? lightSensor;
        private float? batteryVoltage;
        private int batteryCharge = 0;
        private float? solarVoltage;
        private long mileage = 0L;
        private bool? isUsbCharging;
        private bool? isSolarCharging;
        private bool isManagerConfigured1 = false;
        private bool isManagerConfigured2 = false;
        private bool isManagerConfigured3 = false;
        private bool isManagerConfigured4 = false;
        private int networkSignal;
        private bool iopIgnition = false;
        private int samplingIntervalAccOn = 0;
        private int samplingIntervalAccOff = 0;
        private int angleCompensation = 0;
        private int distanceCompensation = 0;
        private int heartbeatInterval = 0;
        private int originalAlarmCode;
        private string smartPowerSettingStatus;
        private string smartPowerOpenStatus;
        private int lockType;

        private bool isLockSim = false;
        private bool isLockDevice = false;
        private bool AGPSEphemerisDataDownloadSettingStatus = false;
        private bool gSensorSettingStatus = false;
        private bool frontSensorSettingStatus = false;
        private bool deviceRemoveAlarmSettingStatus = false;
        private bool openCaseAlarmSettingStatus = false;
        private bool deviceInternalTempReadingANdUploadingSettingStatus = false;

        public bool IsHistoryData { get; set; } = false;

        public int OriginalAlarmCode
        {
            get { return originalAlarmCode; }
            set { originalAlarmCode = value; }
        }

        public bool IsLockSim
        {
            get { return isLockSim; }
            set { isLockSim = value; }
        }

        public bool IsLockDevice
        {
            get { return isLockDevice; }
            set { isLockDevice = value; }
        }

        public bool IsAGPSEphemerisDataDownloadSettingStatus
        {
            get { return AGPSEphemerisDataDownloadSettingStatus; }
            set { AGPSEphemerisDataDownloadSettingStatus = value; }
        }

        public bool IsGSensorSettingStatus
        {
            get { return gSensorSettingStatus; }
            set { gSensorSettingStatus = value; }
        }

        public bool IsFrontSensorSettingStatus
        {
            get { return frontSensorSettingStatus; }
            set { frontSensorSettingStatus = value; }
        }

        public bool IsDeviceRemoveAlarmSettingStatus
        {
            get { return deviceRemoveAlarmSettingStatus; }
            set { deviceRemoveAlarmSettingStatus = value; }
        }

        public bool IsOpenCaseAlarmSettingStatus
        {
            get { return openCaseAlarmSettingStatus; }
            set { openCaseAlarmSettingStatus = value; }
        }

        public bool IsDeviceInternalTempReadingANdUploadingSettingStatus
        {
            get { return deviceInternalTempReadingANdUploadingSettingStatus; }
            set { deviceInternalTempReadingANdUploadingSettingStatus = value; }
        }

        public string SmartPowerSettingStatus
        {
            get { return smartPowerSettingStatus; }
            set { smartPowerSettingStatus = value; }
        }

        public string SmartPowerOpenStatus
        {
            get { return smartPowerOpenStatus; }
            set { smartPowerOpenStatus = value; }
        }

        public int LockType
        {
            get { return lockType; }
            set { lockType = value; }
        }

        public float AxisX
        {
            get { return axisX; }
            set { axisX = value; }
        }

        public float AxisY
        {
            get { return axisY; }
            set { axisY = value; }
        }

        public float AxisZ
        {
            get { return axisZ; }
            set { axisZ = value; }
        }

        public float? DeviceTemp
        {
            get { return deviceTemp; }
            set { deviceTemp = value; }
        }

        public float? LightSensor
        {
            get { return lightSensor; }
            set { lightSensor = value; }
        }

        public float? BatteryVoltage
        {
            get { return batteryVoltage; }
            set { batteryVoltage = value; }
        }

        public int BatteryCharge
        {
            get { return batteryCharge; }
            set { batteryCharge = value; }
        }

        public float? SolarVoltage
        {
            get { return solarVoltage; }
            set { solarVoltage = value; }
        }

        public long Mileage
        {
            get { return mileage; }
            set { mileage = value; }
        }

        public bool? IsUsbCharging
        {
            get { return isUsbCharging; }
            set { isUsbCharging = value; }
        }

        public bool? IsSolarCharging
        {
            get { return isSolarCharging; }
            set { isSolarCharging = value; }
        }

        public bool IsManagerConfigured1
        {
            get { return isManagerConfigured1; }
            set { isManagerConfigured1 = value; }
        }

        public bool IsManagerConfigured2
        {
            get { return isManagerConfigured2; }
            set { isManagerConfigured2 = value; }
        }

        public bool IsManagerConfigured3
        {
            get { return isManagerConfigured3; }
            set { isManagerConfigured3 = value; }
        }

        public bool IsManagerConfigured4
        {
            get { return isManagerConfigured4; }
            set { isManagerConfigured4 = value; }
        }

        public int NetworkSignal
        {
            get { return networkSignal; }
            set { networkSignal = value; }
        }

        public bool IopIgnition
        {
            get { return iopIgnition; }
            set { iopIgnition = value; }
        }

        public int SamplingIntervalAccOn
        {
            get { return samplingIntervalAccOn; }
            set { samplingIntervalAccOn = value; }
        }

        public int SamplingIntervalAccOff
        {
            get { return samplingIntervalAccOff; }
            set { samplingIntervalAccOff = value; }
        }

        public int AngleCompensation
        {
            get { return angleCompensation; }
            set { angleCompensation = value; }
        }

        public int DistanceCompensation
        {
            get { return distanceCompensation; }
            set { distanceCompensation = value; }
        }

        public int HeartbeatInterval
        {
            get { return heartbeatInterval; }
            set { heartbeatInterval = value; }
        }

        public DateTime Date
        {
            get { return date; }
            set { date = value; }
        }

        public string SelfMac
        {
            get { return selfMac; }
            set { selfMac = value; }
        }

        public string Ap1Mac
        {
            get { return ap1Mac; }
            set { ap1Mac = value; }
        }

        public int Ap1RSSI
        {
            get { return ap1RSSI; }
            set { ap1RSSI = value; }
        }

        public string Ap2Mac
        {
            get { return ap2Mac; }
            set { ap2Mac = value; }
        }

        public int Ap2RSSI
        {
            get { return ap2RSSI; }
            set { ap2RSSI = value; }
        }

        public string Ap3Mac
        {
            get { return ap3Mac; }
            set { ap3Mac = value; }
        }

        public int Ap3RSSI
        {
            get { return ap3RSSI; }
            set { ap3RSSI = value; }
        }
    }
}
