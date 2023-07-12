//
//  LocationMessage.swift
//  TFT Elock
//
//  Created by china topflytech on 2023/5/10.
//  Copyright © 2023 com.tftiot. All rights reserved.
//

import Foundation
class LocationMessage {
    public var gpsWorking = false // gps is working mode or sleep mode
    public var latlngValid = false // current is gps data or lbs data
    public var latitude = 0.0
    public var longitude = 0.0
    public var altitude = 0.0
   
    public var relayStatus = 0
    
    public var rlyMode = 0
    
    public var smsLanguageType = 0
    public var antitheftedStatus = 0
    public var date: Date?
    public var IOP: Int64 = 0
    public var iopIgnition: Bool?
    public var iopPowerCutOff: Bool?
    public var iopACOn: Bool?
    
    public var speed: Float = 0.0
    public var mileage: Int64 = 0
    public var azimuth = 0
    
    public var address = ""
    
    public var analogInput1: Float = 0
    public var analogInput2: Float = 0
    
    public var batteryCharge = 0
    public var dragThreshold = 0
    
    public var externalPowerVoltage: Float = 0
    public var originalAlarmCode: Int?
    
    //    protected Integer relaySpeedLimit = 0;
    public var isRelayWaiting = false
    public var heartbeatInterval = 0
    public var isManagerConfigured1 = false
    public var isManagerConfigured2 = false
    public var isManagerConfigured3 = false
    public var isManagerConfigured4 = false
    public var gSensorSensitivity = 0
    public var isHistoryData = false
    public var satelliteNumber: Int?
    public var overSpeedLimit = 0
    public var samplingIntervalAccOn = 0
    public var samplingIntervalAccOff = 0
    public var angleCompensation = 0
    public var distanceCompensation = 0
    
    //odb special message
    public var accumulatingFuelConsumption: Int64 = 0
    public var instantFuelConsumption: Int64 = 0
    public var rpm = 0
    public var airInput = 0
    public var airPressure = 0
    public var coolingFluidTemp = 0
    public var airInflowTemp = 0
    public var engineLoad = 0
    public var throttlePosition = 0
    public var remainFuelRate = 0
    public var rs232DeviceValid = false
    
    public var networkSignal = 0
    
    //personal asset special
    public var axisX: Float = 0
    public var axisY: Float = 0
    public var axisZ: Float = 0
    public var deviceTemp: Float?
    public var lightSensor: Float?
    public var batteryVoltage: Float?
    public var solarVoltage: Float?
    public var isUsbCharging: Bool?
    public var isSolarCharging: Bool?
    public var smartPowerSettingStatus: String?
    public var smartPowerOpenStatus: String?
    
    public var speakerStatus = 0
    public var rs232PowerOf5V = 0
    public var accdetSettingStatus = 0
    
    public var input1 = 0
    public var input2 = 0
    public var input3 = 0
    public var input4 = 0
    
    public var output1 = 0
    public var output2: Int?
    public var output3: Int?
    
    public var analogInput3: Float = 0
    
    public var input5 = 0
    public var input6 = 0
    public var analogInput4: Float = 0
    public var analogInput5: Float = 0
    
    public var output12V: Bool?
    public var outputVout: Bool?
    public var isSmartUploadSupport: Bool?
    public var supportChangeBattery: Bool?
    
    public var is_4g_lbs = false
    public var mcc_4g: Int?
    public var mnc_4g: Int?
    public var ci_4g: Int64?
    public var earfcn_4g_1: Int?
    public var pcid_4g_1: Int?
    public var earfcn_4g_2: Int?
    public var pcid_4g_2: Int?
    
    public var is_2g_lbs = false
    public var mcc_2g: Int?
    public var mnc_2g: Int?
    public var lac_2g_1: Int?
    public var ci_2g_1: Int?
    public var lac_2g_2: Int?
    public var ci_2g_2: Int?
    public var lac_2g_3: Int?
    public var ci_2g_3: Int?
    public var externalPowerReduceStatus: Int?
    
    public var isSendSmsAlarmToManagerPhone = false
    public var isSendSmsAlarmWhenDigitalInput2Change = false
    public var jammerDetectionStatus = 0
    public var isLockSim = false
    public var isLockDevice = false
    public var AGPSEphemerisDataDownloadSettingStatus = false
    public var gSensorSettingStatus = false
    public var frontSensorSettingStatus = false
    public var deviceRemoveAlarmSettingStatus = false
    public var openCaseAlarmSettingStatus = false
    public var deviceInternalTempReadingANdUploadingSettingStatus = false
    
    public var gyroscopeAxisX: Float = 0
    public var gyroscopeAxisY: Float = 0
    public var gyroscopeAxisZ: Float = 0
    
    public var lockType: Int?
    
    public var ignitionSource = 0
    public var hasThirdPartyObd = 0
    public var exPowerConsumpStatus = 0 //0:unknown,1:normal,2abnormal
    
    public var remainFuelUnit = 0 //0:% ,1:L
    public var mileageSource = 0 //0:GPS 1:ECU
    public var imei = ""
    public var serialNo = 0
    public var orignBytes: [UInt8]?
    //    public var isNeedResp = true
    public var protocolHeadType = 0
    public var lockId:String! = nil
    public var longitudeStr: String?
    public var latitudeStr: String?
    public var dateStr: String?
    public var lockTypeStr: String?
    public var speedStr: String?
    public var mileageStr: String?
    public var satelliteNumberStr: String?
    public var batteryChargeStr: String?
    public var networkSignalStr: String?
    public var originalAlarmCodeStr: String?
}
