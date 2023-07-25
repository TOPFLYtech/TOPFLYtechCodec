#version 1.0.0
#Copyright ? 2012-2019 TOPFLYTECH Co., Limitd . All rights reserved.
from datetime import  tzinfo,timedelta
from hashlib import md5
from Crypto.Cipher import AES
from decimal import Decimal
import datetime
import struct
class Message:
    """
        Message is the base class for all decoded messages
    """
    imei = ""
    serialNo = 0 #The serial number of the message,The serial number is counted on the device
    orignBytes = [] #the orign bytes
    # isNeedResp = True
    protocolHeadType = 0

class ConfigMessage(Message):
    """
        The type Config message.When the server sends a configuration command to the device,
        the device returns the result of the setup. This class describes the setting result
    """
    configContent = "" #The Config result content.


class ForwardMessage(Message):
    """
        The type Forward message.New devices like 8806+ support this message.Old device like 8806,8803Pro does not support this message.
    """
    content = "" # The message content

class USSDMessage(Message):
    """
        The type USSD message.New devices like 8806+ support this message.Old device like 8806,8803Pro does not support this message.
    """
    content = "" # The message content

class RS232Message(Message):
    """
    The type RS232 message.Protocol number is 25 25 09.
    """
    date = datetime.datetime
    isIgnition = False #Is vehicle ignition boolean.
    rs232DataType = 0
    rs232DeviceMessageList = []
    OTHER_DEVICE_DATA = 0
    TIRE_DATA = 1
    RDID_DATA = 3
    FINGERPRINT_DATA = 2
    CAPACITOR_FUEL_DATA = 4
    ULTRASONIC_FUEL_DATA = 5


class Rs232DeviceMessage():
    RS232Data = []

class Rs232TireMessage(Rs232DeviceMessage):
    sensorId = ""
    voltage = 0
    airPressure = 0
    airTemp = 0
    status = 0

class Rs232FingerprintMessage(Rs232DeviceMessage):
    fingerprintId = 0
    data = ""
    fingerprintType = 0
    status = 0
    fingerprintDataIndex = 0
    remarkId = 0
    FINGERPRINT_TYPE_OF_NONE = 0
    FINGERPRINT_TYPE_OF_CLOUND_REGISTER = 1
    FINGERPRINT_TYPE_OF_PATCH = 2
    FINGERPRINT_TYPE_OF_DELETE = 3
    FINGERPRINT_TYPE_GET_TEMPLATE = 4
    FINGERPRINT_TYPE_WRITE_TEMPLATE = 5
    FINGERPRINT_TYPE_OF_ALL_CLEAR = 6
    FINGERPRINT_TYPE_OF_SET_PERMISSION = 7
    FINGERPRINT_TYPE_OF_GET_PERMISSION = 8
    FINGERPRINT_TYPE_OF_GET_EMPTY_ID = 9
    FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION = 10
    FINGERPRINT_TYPE_OF_SET_DEVICE_ID = 11
    FINGERPRINT_TYPE_OF_REGISTER = 11
    FINGERPRITN_MSG_STATUS_SUCC = 0
    FINGERPRINT_MSG_STATUS_ERROR = 1

class WifiMessage(Message):
    date=datetime.datetime
    selfMac=""
    ap1Mac=""
    ap1RSSI=0
    ap2Mac=""
    ap2RSSI=0
    ap3Mac=""
    ap3RSSI=0
class WifiWithDeviceInfoMessage(Message):
    def __init__(self):
        self.date = None
        self.selfMac = None
        self.ap1Mac = None
        self.ap1RSSI = 0
        self.ap2Mac = None
        self.ap2RSSI = 0
        self.ap3Mac = None
        self.ap3RSSI = 0

        self.axisX = 0.0
        self.axisY = 0.0
        self.axisZ = 0.0
        self.deviceTemp = None
        self.lightSensor = None
        self.batteryVoltage = None
        self.batteryCharge = 0
        self.solarVoltage = None
        self.mileage = 0
        self.isUsbCharging = None
        self.isSolarCharging = None
        self.isManagerConfigured1 = False
        self.isManagerConfigured2 = False
        self.isManagerConfigured3 = False
        self.isManagerConfigured4 = False
        self.networkSignal = 0
        self.iopIgnition = None
        self.samplingIntervalAccOn = 0
        self.samplingIntervalAccOff = 0
        self.angleCompensation = 0
        self.distanceCompensation = 0
        self.heartbeatInterval = 0
        self.originalAlarmCode = None
        self.smartPowerSettingStatus = None
        self.smartPowerOpenStatus = None
        self.lockType = None

        self.isLockSim = False
        self.isLockDevice = False
        self.AGPSEphemerisDataDownloadSettingStatus = False
        self.gSensorSettingStatus = False
        self.frontSensorSettingStatus = False
        self.deviceRemoveAlarmSettingStatus = False
        self.openCaseAlarmSettingStatus = False
        self.deviceInternalTempReadingANdUploadingSettingStatus = False

class LockMessage(Message):
    latlngValid = False
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
    speed = 0.0
    azimuth = 0
    date = datetime.datetime
    lockType = 0
    lockId = ""
    gpsWorking = False
    isHistoryData = False
    satelliteNumber = 0
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0

class Rs232FuelMessage(Rs232DeviceMessage):
    fuelPercent = 0.0
    temp = 0.0
    liquidType = 0
    curLiquidHeight = 0.0
    fullLiquidHeight = 0.0
    alarm = 0
    LIQUID_TYPE_OF_DIESEL = 1
    LIQUID_TYPE_OF_PETROL = 2
    LIQUID_TYPE_OF_WATER = 3
    ALARM_NONE = 0
    ALARM_FILL_TANK = 1
    ALARM_FUEL_LEAK = 2

class Rs232RfidMessage(Rs232DeviceMessage):
    rfid = ""


class BluetoothPeripheralDataMessage(Message):
    date = datetime.datetime
    isIgnition = False #Is vehicle ignition boolean.
    isHistoryData = False
    bleDataList = []
    messageType = 0
    MESSAGE_TYPE_TIRE = 0
    MESSAGE_TYPE_DRIVER = 1
    MESSAGE_TYPE_SOS = 2
    MESSAGE_TYPE_TEMP = 3
    MESSAGE_TYPE_DOOR = 4
    MESSAGE_TYPE_CTRL = 5
    MESSAGE_TYPE_FUEL = 6
    MESSAGE_TYPE_Customer2397 = 7
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0
    isHadLocationInfo = False
    latlngValid = False
    altitude = 0
    latitude = 0
    longitude = 0
    azimuth = 0
    speed = 0

class BleData:
    mac = ""


class BleAlertData(BleData):
    ALERT_TYPE_SOS = 0
    ALERT_TYPE_LOW_BATTERY = 1
    innerVoltage = 0.0
    alertType = 0
    latlngValid = False
    isHistoryData = False
    satelliteCount = 0
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
    speed = 0.0
    azimuth = 0
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0

class BleDriverSignInData(BleData):
    ALERT_TYPE_DRIVER = 0
    ALERT_TYPE_LOW_BATTERY = 1
    voltage = 0
    alert = 0
    latlngValid = False
    isHistoryData = False
    satelliteCount = 0
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
    speed = 0.0
    azimuth = 0
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0

class BleTempData(BleData):
    voltage = 0
    batteryPercent = 0
    temp = 0
    humidity = 0
    lightIntensity = 0
    rssi = 0

class BleDoorData(BleData):
    voltage = 0
    batteryPercent = 0
    temp = 0
    doorStatus = 0
    online = 0
    rssi = 0

class BleCtrlData(BleData):
    voltage = 0
    batteryPercent = 0
    temp = 0
    ctrlStatus = 0
    online = 0
    rssi = 0

class BleTireData(BleData):
    voltage = 0
    airPressure = 0
    airTemp = 0
    status = 0

class BleFuelData(BleData):
    voltage = 0
    value = 0
    temp = 0
    alarm = 0
    online = 0
    rssi = 0

class BleCustomer2397SensorData(BleData):
    rawData = []
    rssi = 0

class AccelerationData:
    imei=""
    date=datetime.datetime
    axisX = 0.0  #The acceleration of x axis
    axisY = 0.0  #The acceleration of y axis
    axisZ = 0.0  #The acceleration of z axis
    speed = 0.0
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
    azimuth = 0  #The vehicle current azimuth.
    satelliteNumber = 0
    gpsWorking = False  #Is gps working or sleeping. when gps working ,return true, otherwise return false.
    latlngValid = False  #Is GPS data or LBS data. when GPS data,return true.
    isHistoryData = False
    rpm = 0
    gyroscopeAxisX = 0.0
    gyroscopeAxisY = 0.0
    gyroscopeAxisZ = 0.0
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0


class SignInMessage(Message):
    """
        The type Sign in message.Protocol number is 25 25 01.
        Older devices like 8806,8803Pro,You need to respond to the message to the device, otherwise the device will not send other data.
        The new device, like the 8806 plus, needs to be based on the device configuration to decide whether or not to respond to the message
    """
    software = ""  #The software version.like 1.1.1
    firmware = ""  #The firmware version.like 1.1
    hardware = ""  #The hardware version.like 5.0
    platform = ""  #The platform. like 6250
    obdHardware = ""
    obdSoftware = ""
    obdBootVersion = ""
    obdDataVersion = ""

class HeartbeatMessage(Message):
    pass

class LocationMessage(Message):
    gpsWorking = False #Is gps working or sleeping. when gps working ,return true, otherwise return false.
    latlngValid = False #Is GPS data or LBS data. when GPS data,return true.
    latitude = 0.0
    longitude = 0.0
    altitude = 0.0
    relayStatus = 0 #The relay status.When in the oil off state, return 1,otherwise return 0.
    antitheftedStatus = 0 #The antithefted status.When in the anti-theft state, return 1,otherwire return 0.
    date=datetime.datetime #The message snapshot time
    IOP = 0 #The Digital I/O Status
    iopIgnition = False #The vehicle ignition state obtained from the digital I / O status.when ignition return true.
    iopPowerCutOff = False #The external power supply connection status of the vehicle obtained from the digital I / O status.If connected,return true.
    iopACOn = False #The air conditioning status of the vehicle obtained from the digital I / O status.If opened,retrun true.
    speed = 0.0 #The speed.The unit is km / h
    mileage = 0 #The vehicle current mileage.The unit is meter.
    azimuth = 0
    address = ""
    analogInput1 = 0
    analogInput2 = 0
    batteryCharge = 0
    dragThreshold = 0
    externalPowerVoltage = 0 #The external power voltage.The Old vehicle has not this value.The new one has this value.
    originalAlarmCode = 0 #The original alarm code.Response to the device's alert message requires it.
    isRelayWaiting = False #The relay waiting status.If engine cut fail,the vehicle maybe waiting ,if is waiting cut off engine,return true.
    heartbeatInterval = 0
    isManagerConfigured1 = False #The Gsnesor manager configured 1 status.
    isManagerConfigured2 = False #The Gsnesor manager configured 2 status.
    isManagerConfigured3 = False #The Gsnesor manager configured 3 status.
    isManagerConfigured4 = False #The Gsnesor manager configured 4 status.
    gSensorSensitivity = 0
    isHistoryData = False
    satelliteNumber = 0
    overspeedLimit = 0 #The over speed limit.The unit is km / h
    samplingIntervalAccOn = 0 #The acc on upload interval.
    samplingIntervalAccOff = 0 #The acc off upload interval.
    angleCompensation = 0
    distanceCompensation = 0
    rs232DeviceValid = False
    networkSignal = 0
    accumulatingFuelConsumption = 0
    instantFuelConsumption = 0
    rpm = 0
    airInput = 0
    airPressure = 0
    coolingFluidTemp = 0
    airInflowTemp = 0
    engineLoad = 0
    throttlePosition = 0
    remainFuelRate = 0
    axisX = 0
    axisY = 0
    axisZ = 0
    deviceTemp = 0
    lightSensor = 0
    batteryVoltage = 0
    solarVoltage = 0
    isUsbCharging = False
    isSolarCharging = False
    smartPowerSettingStatus = ""
    smartPowerOpenStatus = ""
    output2 = False
    output3 = False
    output12V = False
    outputVout = False
    analogInput3 = 0
    externalPowerSupply = False
    input1 = False
    input2 = False
    input3 = False
    input4 = False
    input5 = False
    input6 = False
    lastMileageDiff = 0
    output1 = False
    isSmartUploadSupport = False
    supportChangeBattery = False
    batteryVoltage = 0
    is_4g_lbs = False
    mcc_4g = 0
    mnc_4g = 0
    ci_4g = 0
    earfcn_4g_1 = 0
    pcid_4g_1 = 0
    earfcn_4g_2 = 0
    pcid_4g_2 = 0
    bci_4g = 0
    tac = 0
    pcid_4g_3 = 0
    is_2g_lbs = False
    mcc_2g = 0
    mnc_2g = 0
    lac_2g_1 = 0
    ci_2g_1 = 0
    lac_2g_2 = 0
    ci_2g_2 = 0
    lac_2g_3 = 0
    ci_2g_3 = 0
    rlyMode = 0
    smsLanguageType = 0
    speakerStatus = 0
    rs232PowerOf5V = 0
    accdetSettingStatus = 0
    isSendSmsAlarmToManagerPhone = False
    isSendSmsAlarmWhenDigitalInput2Change = False
    jammerDetectionStatus = 0
    isLockSim = False
    isLockDevice = False
    AGPSEphemerisDataDownloadSettingStatus = False
    gSensorSettingStatus = False
    frontSensorSettingStatus = False
    deviceRemoveAlarmSettingStatus = False
    openCaseAlarmSettingStatus = False
    deviceInternalTempReadingANdUploadingSettingStatus = False
    gyroscopeAxisX = 0
    gyroscopeAxisY = 0
    gyroscopeAxisZ = 0
    lockType = 0xff
    ignitionSource = 0
    exPowerConsumpStatus = 0 #0:unknown,1:normal,2abnormal
    hasThirdPartyObd = 0 # 0, 1
    remainFuelUnit = 0
    mileageSource = 0  #0:GPS 1:ECU,2:FMS
    isHadFmsData = False
    fmsEngineHours = 0
    hdop = 0
    fmsSpeed = 0
    fmsAccumulatingFuelConsumption = 0


class LocationInfoMessage(LocationMessage):
    """
        The type Location info message.The alarm code is zero.Protocol number is 25 25 02.
    """
    pass

class LocationAlarmMessage(LocationMessage):
    """
        The type Location alarm message.When the device triggers an alert, the server receives such messages.
        Protocol number is 25 25 04.
    """
    pass

class NetworkInfoMessage(Message):
    date=datetime.datetime
    networkOperator=""
    accessTechnology=""
    band=""
    imsi=""
    iccid=""

class GpsDriverBehaviorType:
    HIGH_SPEED_BRAKE = 0
    HIGH_SPEED_ACCELERATE = 1
    MEDIUM_SPEED_BRAKE = 2
    MEDIUM_SPEED_ACCELERATE = 3
    LOW_SPEED_BRAKE = 4
    LOW_SPEED_ACCELERATE = 5


class MessageEncryptType:
    NONE = 0
    MD5 = 1
    AES = 2

class GpsDriverBehaviorMessage(Message):
    """
        Driver Behavior Via GPS (AST Command Control, Default Disable the feature)
        Protocol number is 25 25 05.
    """
    behaviorType = -1
    startDate = datetime.datetime
    startLatitude = 0.0
    startLongitude = 0.0
    startAltitude = 0.0
    startSpeed = 0.0
    startAzimuth = 0
    endDate = datetime.datetime
    endLatitude = 0.0
    endLongitude = 0.0
    endAltitude = 0.0
    endSpeed = 0.0
    endAzimuth = 0
    startRpm = 0
    endRpm = 0

class InnerGeoDataMessage(Message):
    def __init__(self):
        self.date = None
        self.geoList = []
        self.lockGeofenceEnable = 0

    def getLockGeofenceEnable(self):
        return self.lockGeofenceEnable

    def setLockGeofenceEnable(self, lockGeofenceEnable):
        self.lockGeofenceEnable = lockGeofenceEnable

    def getDate(self):
        return self.date

    def setDate(self, date):
        self.date = date

    def getGeoList(self):
        return self.geoList

    def setGeoList(self, geoList):
        self.geoList = geoList

    def addGeoPoint(self, geo):
        self.geoList.append(geo)

class InnerGeofence:
    def __init__(self):
        self.id = 0
        self.type = 0
        self.radius = -1
        self.points = []

    def getId(self):
        return self.id

    def setId(self, id):
        self.id = id

    def getType(self):
        return self.type

    def setType(self, type):
        self.type = type

    def getRadius(self):
        return self.radius

    def setRadius(self, radius):
        self.radius = radius

    def getPoints(self):
        return self.points

    def setPoints(self, points):
        self.points = points

    def addPoint(self, lat, lng):
        self.points.append([lat, lng])

class OneWireMessage(Message):
    def __init__(self):
        self.date = None
        self.isIgnition = False
        self.deviceId = ""
        self.oneWireData = b""

    def getDeviceId(self):
        return self.deviceId

    def setDeviceId(self, deviceId):
        self.deviceId = deviceId

    def getOneWireData(self):
        return self.oneWireData

    def setOneWireData(self, oneWireData):
        self.oneWireData = oneWireData

    def getDate(self):
        return self.date

    def setDate(self, date):
        self.date = date

    def isIgnition(self):
        return self.isIgnition

    def setIsIgnition(self, ignition):
        self.isIgnition = ignition

class TalkEndMessage(Message):
    pass
class TalkStartMessage(Message):
    pass

class VoiceMessage(Message):
    def __init__(self):
        self.encodeType = 0
        self.voiceData = b""

    def getEncodeType(self):
        return self.encodeType

    def setEncodeType(self, encodeType):
        self.encodeType = encodeType

    def getVoiceData(self):
        return self.voiceData

    def setVoiceData(self, voiceData):
        self.voiceData = voiceData

class RS485Message(Message):
    def __init__(self):
        self.date = None
        self.isIgnition = False
        self.deviceId = 0
        self.rs485Data = b""

    def getDeviceId(self):
        return self.deviceId

    def setDeviceId(self, deviceId):
        self.deviceId = deviceId

    def getRs485Data(self):
        return self.rs485Data

    def setRs485Data(self, rs485Data):
        self.rs485Data = rs485Data

    def getDate(self):
        return self.date

    def setDate(self, date):
        self.date = date

    def isIgnition(self):
        return self.isIgnition

    def setIsIgnition(self, ignition):
        self.isIgnition = ignition
class AccelerationDriverBehaviorMessage(Message):
    """
        Driver Behavior Via Acceleration (AST Command Control, Default Disable the feature)
        Protocol number is 25 25 06.
    """
    accelerationData = AccelerationData()
    behaviorType = -1
    BEHAVIOR_TURN_AND_BRAKE = 0
    BEHAVIOR_ACCELERATE = 1


class AccidentAccelerationMessage(Message):
    """
        The type Accident acceleration message.Accident Data (AST Command Control, Default Disable the feature,From Tracker)
        Protocol number is 25 25 07.
    """
    accelerationList = [] # list item is AccelerationData


class ObdMessage(Message):
    ERROR_CODE_MESSAGE = 0
    VIN_MESSAGE = 1
    CLEAR_ERROR_CODE_MESSAGE = 2
    errorCode = ""
    errorData = ""
    date = datetime.datetime
    messageType = 0
    vin = ""
    clearErrorCodeSuccess = False



def byte2HexString(byteArray,index):
    return ''.join('{:02x}'.format(x) for x in byteArray[index:])


def hexString2Bytes(hexStr):
    hexData = hexStr.decode("hex")#python2.7
    return map(ord,hexData)#python2.7
    #return bytes.fromhex(hexStr)  # python 3.x


def formatNumb(numb):
    return Decimal(numb).quantize(Decimal('0.00'))


def short2Bytes(number):
    str = '{:04x}'.format(number)
    #return bytes.fromhex(str)  # python 3.X
    return map(ord,'{:04x}'.format(number).decode("hex"))#python 2.7


def bytes2Short(byteArray,offset):
    return (byteArray[offset]  << 8 & 0xFF00) + (byteArray[offset+1] & 0xFF)

def bytes2SingleShort(byteArray,offset):
    if len(byteArray) < offset + 2:
        return 0
    first = byteArray[offset]
    second = byteArray[offset + 1]
    firstValue = first & 0x7f
    sourceValue = 0
    if (first & 0x80) == 0x80:
        sourceValue = -32768
    incValue = (firstValue << 8) + int(second)
    return sourceValue + incValue
    

def bytes2Integer(byteArray,offset):
    return (byteArray[offset]<< 24) + (byteArray[offset+1] << 16) + (byteArray[offset+2] << 8) +(byteArray[offset+3] )

def bytes2Float(byteArray,offset):
    # data = [byteArray[offset + 3],byteArray[offset +2],byteArray[offset+1],byteArray[offset]]
    # b = ''.join(chr(i) for i in data)
    # return struct.unpack(">f",b)[0]
    ba = bytearray()
    ba.append(byteArray[offset + 3])
    ba.append(byteArray[offset + 2])
    ba.append(byteArray[offset + 1])
    ba.append(byteArray[offset])
    return struct.unpack("!f",ba)[0]
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
unpad = lambda s : s[0:-ord(s[-1])]

class Crypto:
    iv = "topflytech201205"
    @staticmethod
    def aesKeyMD5(src):
        result = md5(bytearray(src)).hexdigest()
        return hexString2Bytes(result)
    @staticmethod
    def aesEncrypt(bytes,key):
        bkey = Crypto.aesKeyMD5(key)
        raw = pad(bytearray(bytes))
        cipher = AES.new(str(bytearray(bkey)), AES.MODE_CBC, Crypto.iv )
        ciphertext= cipher.encrypt(str(raw))
        destBytes = bytearray(ciphertext)
        return destBytes
    @staticmethod
    def aesDecrypt(bytes,key):
        bkey = Crypto.aesKeyMD5(key)
        cipher = AES.new(str(bytearray(bkey)), AES.MODE_CBC, Crypto.iv )
        orignBytes = bytearray(cipher.decrypt(str(bytes)).strip())
        return orignBytes
    @staticmethod
    def getMD5Path(bytes):
        result = md5(bytearray(bytes)).hexdigest()
        md5AllBytes = hexString2Bytes(result)
        return md5AllBytes[4:12]
    @staticmethod
    def getAesLength(packageLength):
        if packageLength <= 15:
            return packageLength
        return ((packageLength - 15) / 16 + 1) * 16 + 15

    @staticmethod
    def decryptData(data,encryptType,aesKey):
        if encryptType == MessageEncryptType.MD5:
            realData = data[0:(len(data) - 8)]
            md5Data = data[(len(data) - 8):]
            pathMd5 = Crypto.getMD5Path(realData)
            if pathMd5:
                if bytearray(pathMd5) == md5Data:
                    return realData
                else:
                    return
            else:
                return
        elif encryptType == MessageEncryptType.AES:
            head = data[0:15]
            aesData = data[15:]
            if len(aesData) <=0 :
                return data
            realData = Crypto.aesDecrypt(aesData,aesKey)
            if realData:
                head.extend(realData)
                return head
            else:
                return
        else:
            return data



def decodeImei(byteArray,index):
    str = byte2HexString(byteArray,index)
    return str[1:16]
class UTC(tzinfo):
    """UTC"""
    def __init__(self,offset = 0):
        self._offset = offset

    def utcoffset(self, dt):
        return timedelta(hours=self._offset)

    def tzname(self, dt):
        return "UTC +%s" % self._offset

    def dst(self, dt):
        return timedelta(hours=self._offset)
def GTM0(dateStr):
    year = int(dateStr[0:4])
    month = int(dateStr[4:6])
    day = int(dateStr[6:8])
    hour = int(dateStr[8:10])
    minutes = int(dateStr[10:12])
    seconds = int(dateStr[12:14])
    return datetime.datetime(year,month,day,hour,minutes,seconds,0, UTC(0))

def encodeImei(imei):
    return hexString2Bytes("0" + imei)

class TopflytechByteBuf:
    selfBuf = [0 for x in range(0, 4096)]
    readIndex = 0
    writeIndex = 0
    capacity = 4096
    markerReadIndex = 0
    def pubBuf(self,inBuf):
        if self.capacity - self.writeIndex >= len(inBuf):
            for i in range(len(inBuf)):
                self.selfBuf[self.writeIndex] = inBuf[i]
                self.writeIndex += 1
        else:
            if self.capacity - self.writeIndex + self.readIndex >= len(inBuf):
                currentDataLength = self.writeIndex - self.readIndex;
                for i in range(currentDataLength):
                    self.selfBuf[i] = self.selfBuf[self.readIndex + i]
                self.writeIndex = currentDataLength;
                self.readIndex = 0;
                self.markerReadIndex = 0;
                for i in range(len(inBuf)):
                    self.selfBuf[self.writeIndex] = inBuf[i]
                    self.writeIndex += 1
            else:
                needLength = ((self.writeIndex - self.readIndex + len(inBuf)) / 4096 + 1) * 4096;
                tmp =  [0 for x in range(0, needLength)]
                for i in range(self.writeIndex - self.readIndex):
                    tmp[i] = self.selfBuf[self.readIndex + i]
                self.selfBuf = tmp
                self.writeIndex = self.writeIndex - self.readIndex
                self.readIndex = 0
                self.markerReadIndex = 0
                for i in range(len(inBuf)):
                    self.selfBuf[self.writeIndex] = inBuf[i]
                    self.writeIndex += 1

    def getReadableBytes(self):
        return self.writeIndex - self.readIndex
    def getReadIndex(self):
        return self.readIndex
    def getByte(self,index):
        if index >= self.writeIndex - self.readIndex:
            return '0'
        return self.selfBuf[self.readIndex + index]

    def markReaderIndex(self):
        self.markerReadIndex = self.readIndex

    def resetReaderIndex(self):
        self.readIndex = self.markerReadIndex
    def skipBytes(self,length):
        self.readIndex += length

    def readBytes(self,length):
        if length > self.getReadableBytes():
            return
        result = self.selfBuf[self.readIndex:self.readIndex+length]
        self.readIndex += length
        return result

class Decoder:
    HEADER_LENGTH = 3
    SIGNUP = [0x23, 0x23, 0x01]
    DATA = [0x23, 0x23, 0x02]
    HEARTBEAT = [0x23, 0x23, 0x03]
    ALARM = [0x23, 0x23, 0x04]
    CONFIG = [0x23, 0x23, 0x81]
    SIGNUP_880XPlUS = [0x25, 0x25, 0x01]
    DATA_880XPlUS = [0x25, 0x25, 0x02]
    HEARTBEAT_880XPlUS = [0x25, 0x25, 0x03]
    ALARM_880XPlUS = [0x25, 0x25, 0x04]
    CONFIG_880XPlUS = [0x25, 0x25, 0x81]
    GPS_DRIVER_BEHAVIOR = [0x25, 0x25, 0x05]
    ACCELERATION_DRIVER_BEHAVIOR = [0x25, 0x25, 0x06]
    ACCELERATION_ALARM = [0x25, 0x25, 0x07]
    BLUETOOTH_MAC = [0x25, 0x25, 0x08]
    RS232 = [0x25,0x25,0x09]
    BLUETOOTH_DATA = [0x25,0x25,0x10]
    NETWORK_INFO_DATA = [0x25,0x25,0x11]
    BLUETOOTH_SECOND_DATA = [0x25,0x25,0x12]
    LOCATION_SECOND_DATA = [0x25,0x25,0x13]
    ALARM_SECOND_DATA = [0x25,0x25,0x14]
    LOCATION_DATA_WITH_SENSOR = [0x25,0x25,0x16]
    LOCATION_ALARM_WITH_SENSOR = [0x25,0x25,0x18]
    latlngInvalidData = [0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]
    rs232TireHead = [0x00,0x01]
    rs232RfidHead = [0x00,0x03]
    rs232FingerprintHead = [0x00,0x02,0x6C,0x62,0x63]
    rs232CapacitorFuelHead = [0x00,0x04]
    rs232UltrasonicFuelHead = [0x00,0x05]
    WIFI_DATA = [0x25, 0x25,0x15]
    RS485_DATA = [0x25, 0x25, 0x21]
    OBD_DATA = [0x25, 0x25, 0x22]
    ONE_WIRE_DATA = [0x25, 0x25, 0x23]
    MASK_IGNITION =0x4000
    MASK_POWER_CUT =0x8000
    MASK_AC=0x2000
    IOP_RS232_DEVICE_VALID=0x20
    encryptType = 0
    aesKey = ""
    obdHead = [0x55, 0xAA]
    def __init__(self,messageEncryptType,aesKey):
        """
            Instantiates a new Decoder.
        :param messageEncryptType:The message encrypt type .Use the value of MessageEncryptType.
        :param aesKey:The aes key.If you do not use AES encryption, the value can be empty.The type is string
        :return:
        """
        self.encryptType = messageEncryptType
        self.aesKey = aesKey


    def match (self,byteArray):
        return byteArray == self.SIGNUP or byteArray == self.DATA or byteArray == self.HEARTBEAT \
               or byteArray == self.ALARM or byteArray == self.CONFIG or byteArray == self.SIGNUP_880XPlUS \
               or  byteArray == self.DATA_880XPlUS or byteArray == self.HEARTBEAT_880XPlUS or \
               byteArray == self.ALARM_880XPlUS or byteArray == self.CONFIG_880XPlUS or \
               byteArray == self.GPS_DRIVER_BEHAVIOR or byteArray == self.ACCELERATION_DRIVER_BEHAVIOR or \
               byteArray == self.ACCELERATION_ALARM or byteArray == self.BLUETOOTH_MAC or byteArray == self.RS232\
               or byteArray == self.BLUETOOTH_DATA or byteArray == self.NETWORK_INFO_DATA or \
               byteArray == self.BLUETOOTH_SECOND_DATA or byteArray == self.LOCATION_SECOND_DATA or \
               byteArray == self.ALARM_SECOND_DATA or byteArray == self.LOCATION_DATA_WITH_SENSOR or byteArray == self.LOCATION_ALARM_WITH_SENSOR \
               or byteArray == self.WIFI_DATA or byteArray == self.RS485_DATA or byteArray == self.OBD_DATA \
               or byteArray == self.ONE_WIRE_DATA

    decoderBuf = TopflytechByteBuf()

    def decode(self,buf):
        """
        Decode list.You can get all message at once.
        :param buf:The buf is from socket
        :return:The message list
        """
        buf = bytearray(buf)
        self.decoderBuf.pubBuf(buf)
        messages =[]
        if self.decoderBuf.getReadableBytes() < self.HEADER_LENGTH + 2:
            return messages
        bytes = [0,0,0]
        while self.decoderBuf.getReadableBytes() > 5:
            self.decoderBuf.markReaderIndex()
            bytes[0] = self.decoderBuf.getByte(0)
            bytes[1] = self.decoderBuf.getByte(1)
            bytes[2] = self.decoderBuf.getByte(2)
            if self.match(bytes):
                self.decoderBuf.skipBytes(self.HEADER_LENGTH)
                lengthBuf = self.decoderBuf.readBytes(2)
                packageLength = bytes2Short(lengthBuf,0)
                if self.encryptType == MessageEncryptType.MD5:
                    packageLength = packageLength + 8
                elif self.encryptType == MessageEncryptType.AES:
                    packageLength = self.getAesLength(packageLength)
                self.decoderBuf.resetReaderIndex()
                if packageLength <= 0:
                    self.decoderBuf.skipBytes(5)
                    break
                if packageLength > self.decoderBuf.getReadableBytes():
                    break
                data = self.decoderBuf.readBytes(packageLength)
                data = Crypto.decryptData(data,self.encryptType,self.aesKey)
                if data:
                    message = self.build(data)
                    if message:
                        messages.append(message)
            else:
                self.decoderBuf.skipBytes(1)
        return messages
    def build(self,byteArray):
        if (byteArray[0] == 0x23 and byteArray[1] == 0x23) or (byteArray[0] == 0x25 and byteArray[1] == 0x25):
            if byteArray[2] == 0x01:
                return self.parseSignInMessage(byteArray)
            elif byteArray[2] == 0x03:
                return self.parseHeartbeatMessage(byteArray)
            elif byteArray[2] == 0x02 or byteArray[2] == 0x04 or byteArray[2] == 0x16 or byteArray[2] == 0x18:
                return self.parseDataMessage(byteArray)
            elif byteArray[2] == 0x05:
                return self.parseGpsDriverBehaviorMessage(byteArray)
            elif byteArray[2] == 0x06:
                return self.parseAccelerationDriverBehaviorMessage(byteArray)
            elif byteArray[2] == 0x07:
                return self.parseAccelerationAlarmMessage(byteArray)
            elif byteArray[2] == 0x09:
                return self.parseRS232Message(byteArray)
            elif byteArray[2] == 0x10:
                return self.parseBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x11:
                return self.parseNetworkInfoMessage(byteArray)
            elif byteArray[2] == 0x81:
                return self.parseInteractMessage(byteArray)
            elif byteArray[2] == 0x12:
                return self.parseSecondBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x13 or byteArray[2] == 0x14:
                return self.parseSecondDataMessage(byteArray)
            elif byteArray[2] == 0x15:
                return self.parseWifiMessage(byteArray)
            elif byteArray[2] == 0x21:
                return self.parseRs485Message(byteArray)
            elif byteArray[2] == 0x22:
                return self.parseObdMessage(byteArray)
            elif byteArray[2] == 0x23:
                return self.parseOneWireMessage(byteArray)
            else:
                return None
        return None

    def parseRs485Message(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        rS485Message = RS485Message()
        rS485Message.serialNo = serialNo
        rS485Message.imei = imei
        rS485Message.orignBytes = byteArray
        isIgnition = (byteArray[21] & 0x01) == 0x01
        deviceId = byteArray[22]
        rs485Data = byteArray[23:]
        rS485Message.isIgnition = isIgnition
        rS485Message.deviceId = deviceId
        rS485Message.rs485Data = rs485Data
        return rS485Message

    def parseOneWireMessage(self, byteArray):
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        oneWireMessage = OneWireMessage()
        oneWireMessage.serialNo = serialNo
        oneWireMessage.imei = imei
        oneWireMessage.orignBytes = byteArray
        isIgnition = (byteArray[21] & 0x01) == 0x01
        deviceId = byte2HexString(byteArray[22:30], 0)
        oneWireData = byteArray[30:]
        oneWireMessage.deviceId = deviceId
        oneWireMessage.oneWireData = oneWireData
        oneWireMessage.isIgnition = isIgnition
        return oneWireMessage

    def getObdErrorCode(self,errorCode):
        if errorCode == 0:
            return "J1979"
        elif errorCode == 1:
            return "J1939"
        return ""

    def getObdErrorFlag(self,srcFlag):
        data = int(srcFlag,16)
        if data >= 0 and data < 4:
            return "P" + str(data)
        elif data[0] >= 4 and data < 8:
            return "C" + str(data - 4)
        elif data >= 8 and data < 12:
            return "B" + str(data - 8)
        else:
            return "U" + str(data - 12)

    def parseObdMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        obdData = ObdMessage()
        obdData.imei = imei
        obdData.orignBytes = byteArray
        obdData.serialNo = serialNo
        # obdData.isNeedResp = isNeedResp
        obdData.date = gtm0
        obdBytes = byteArray[21:len(byteArray)]

        head = [obdBytes[0],obdBytes[1]]
        if head == self.obdHead:
            obdBytes[2] = obdBytes[2] & 0x0F
            length = bytes2Short(obdBytes,2)
            if length > 0:
                try:
                    data = obdBytes[4:4+length]
                    if data[0] == 0x41 and data[1] == 0x04 and len(data) > 3:
                        obdData.messageType = ObdMessage.CLEAR_ERROR_CODE_MESSAGE
                        obdData.clearErrorCodeSuccess = data[2] == 0x01
                    elif data[0] == 0x41 and data[1] == 0x05 and len(data) > 2:
                        vinData = data[2:len(data) - 1]
                        dataValid = False
                        for i in range(len(vinData)):
                            item = vinData[i]
                            if (item & 0xFF) != 0xFF:
                                dataValid = True
                        if len(vinData) and dataValid:
                            obdData.messageType = ObdMessage.VIN_MESSAGE
                            obdData.vin = ''.join(chr(i) for i in vinData).encode().decode("UTF-8")
                    elif data[0] == 0x41 and (data[1] == 0x03 or data[1] == 0x0A):
                        errorCode = data[2]
                        errorDataByte = data[3:len(data) - 1]
                        errorDataStr = byte2HexString(errorDataByte,0);
                        if errorDataStr is not None:
                            errorDataSum = "";
                            i = 0
                            while i < len(errorDataStr):
                                errorDataItem = errorDataStr[i:i+6]
                                srcFlag = errorDataItem[0:1]
                                errorDataCode = self.getObdErrorFlag(srcFlag) + errorDataItem[1:4]
                                if errorDataSum.find(errorDataCode) == -1:
                                    if i != 0:
                                        errorDataSum += ";"
                                    errorDataSum += errorDataCode;
                                if i+6 >= len(errorDataStr) :
                                    break;
                                i+=6
                            obdData.messageType = ObdMessage.ERROR_CODE_MESSAGE
                            obdData.errorCode = self.getObdErrorCode(errorCode)
                            obdData.errorData = errorDataSum
                except:
                    print ("error")
        return obdData

    def parseWifiMessage(self, bytes):
        wifiMessage = WifiMessage();
        serialNo = bytes2Short(bytes, 5)
        imei = decodeImei(bytes, 7)
        dateStr = "20" + byte2HexString(bytes[15:21], 0)
        gtm0 = GTM0(dateStr)
        selfMacByte = bytes[21:27]
        selfMac = byte2HexString(selfMacByte, 0)
        ap1MacByte = bytes[27:33]
        ap1Mac = byte2HexString(ap1MacByte, 0)
        rssiTemp = (int)(bytes[33])
        if rssiTemp < 0:
            rssiTemp += 256
        ap1RSSI = 0
        if rssiTemp == 255:
            ap1RSSI = -999
        else:
            ap1RSSI = rssiTemp - 256
        ap2MacByte = bytes[34:40]
        ap2Mac = byte2HexString(ap2MacByte, 0)
        rssiTemp = (int)(bytes[40])
        if rssiTemp < 0:
            rssiTemp += 256
        ap2RSSI = 0
        if rssiTemp == 255:
            ap2RSSI = -999
        else:
            ap2RSSI = rssiTemp - 256
        ap3MacByte = bytes[41:47]
        ap3Mac = byte2HexString(ap3MacByte, 0)
        rssiTemp = (int)(bytes[47])
        if rssiTemp < 0:
            rssiTemp += 256
        ap3RSSI = 0
        if rssiTemp == 255:
            ap3RSSI = -999
        else:
            ap3RSSI = rssiTemp - 256
        wifiMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        wifiMessage.imei = imei
        wifiMessage.orignBytes = bytes
        wifiMessage.date = gtm0
        wifiMessage.selfMac = selfMac
        wifiMessage.ap1Mac = ap1Mac
        wifiMessage.ap1RSSI = ap1RSSI
        wifiMessage.ap2Mac = ap2Mac
        wifiMessage.ap2RSSI = ap2RSSI
        wifiMessage.ap3Mac = ap3Mac
        wifiMessage.ap3RSSI = ap3RSSI
        return wifiMessage

    def parseSignInMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        str = byte2HexString(byteArray,15)
        if len(str) == 12:
            software = "V{0}.{1}.{2}".format((byteArray[15] & 0xf0) >> 4, byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4)
            firmware = "V{0}.{1}.{2}".format(byteArray[16] & 0xf, (byteArray[17] & 0xf0) >> 4, byteArray[17] & 0xf)
            platform = str[6:10]
            hardware = "V{0}.{1}".format( (byteArray[20] & 0xf0) >> 4, byteArray[20] & 0xf)
            signInMessage = SignInMessage()
            signInMessage.firmware = firmware
            signInMessage.imei = imei
            signInMessage.serialNo = serialNo
            # signInMessage.isNeedResp = isNeedResp
            signInMessage.platform = platform
            signInMessage.software = software
            signInMessage.hardware = hardware
            signInMessage.orignBytes = byteArray
            return signInMessage
        elif len(str) == 16:
            software = "V{0}.{1}.{2}.{3}".format((byteArray[15] & 0xf0) >> 4, byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4, byteArray[16] & 0xf)
            firmware = "V{0}.{1}.{2}.{3}".format((byteArray[20] & 0xf0) >> 4, byteArray[20] & 0xf, (byteArray[21] & 0xf0) >> 4, byteArray[21] & 0xf)
            hardware = "V{0}.{1}".format( (byteArray[22] & 0xf0) >> 4, byteArray[22] & 0xf)
            signInMessage = SignInMessage()
            signInMessage.firmware = firmware
            signInMessage.imei = imei
            signInMessage.serialNo = serialNo
            # signInMessage.isNeedResp = isNeedResp
            signInMessage.software = software
            signInMessage.hardware = hardware
            signInMessage.orignBytes = byteArray
            return signInMessage
        return None


    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        # heartbeatMessage.isNeedResp = isNeedResp
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def parseRS232Message(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        ignition = (byteArray[21] == 0x01)
        data = byteArray[22:]
        rs232Message = RS232Message()
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        rs232Message.date = GTM0(dateStr)
        rs232Message.imei = imei
        rs232Message.serialNo = serialNo
        # rs232Message.isNeedResp = isNeedResp
        rs232Message.orignBytes = byteArray
        rs232Message.isIgnition = ignition
        if len(data) < 2:
            return rs232Message
        rs232Head = [data[0],data[1]]
        fingerprintHead = None
        if len(data) > 4:
            fingerprintHead = [data[0],data[1],data[2],data[3],data[4]]
        messageList = []
        if rs232Head == self.rs232TireHead:
            rs232Message.rs232DataType = RS232Message.TIRE_DATA
            dataCount = (len(data) - 2 ) / 7
            curIndex = 2
            for i in range(0,(int)(dataCount)):
                curIndex = i * 7 + 2
                rs232TireMessage=Rs232TireMessage()
                airPressureTmp = (int)(data[curIndex + 4])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = airPressureTmp * 1.572 * 2
                rs232TireMessage.airPressure = airPressure
                airTempTmp = (int)( data[curIndex + 5])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = airTempTmp - 55
                rs232TireMessage.airTemp = airTemp
                statusTmp = (int)( data[curIndex + 6])
                if statusTmp < 0:
                    statusTmp += 256
                rs232TireMessage.status =  statusTmp
                voltageTmp = (int) (data[curIndex + 3])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = 0.01 * voltageTmp + 1.22
                rs232TireMessage.voltage = voltage
                sensorIdByte = [data[curIndex],data[curIndex + 1],data[curIndex + 2]]
                rs232TireMessage.sensorId = byte2HexString(sensorIdByte,0)
                messageList.append(rs232TireMessage)
        elif rs232Head == self.rs232RfidHead:
            rs232Message.rs232DataType = RS232Message.RDID_DATA
            dataCount = len(data) / 10
            curIndex = 0
            for i in range(0,(int)(dataCount)):
                curIndex = i * 10
                rs232RfidMessage = Rs232RfidMessage()
                rfidByte = [data[curIndex + 2],data[curIndex + 3],data[curIndex + 4],data[curIndex + 5], data[curIndex + 6],data[curIndex + 7],data[curIndex + 8],data[curIndex + 9]]
                rs232RfidMessage.rfid = ''.join(chr(i) for i in rfidByte)
                messageList.append(rs232RfidMessage)
        elif fingerprintHead is not None and fingerprintHead == self.rs232FingerprintHead:
            rs232Message.rs232DataType = RS232Message.FINGERPRINT_DATA
            rs232FingerprintMessage = Rs232FingerprintMessage()
            if data[6] == 0x00:
                rs232FingerprintMessage.status = Rs232FingerprintMessage.FINGERPRITN_MSG_STATUS_SUCC
            else:
                rs232FingerprintMessage.status = Rs232FingerprintMessage.FINGERPRINT_MSG_STATUS_ERROR
            if data[5] == 0x25:
                rs232FingerprintMessage.fingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_CLOUND_REGISTER
                dataIndex = (int)(data[8])
                rs232FingerprintMessage.fingerprintDataIndex = dataIndex
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
                if data.length > 10:
                   rs232FingerprintMessage.data = byte2HexString(data,10)
            elif data[5]== 0x71:
                rs232FingerprintMessage.fingerprintType = Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_CLOUND_REGISTER
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId= fingerprintId
            elif data[5] == 0x73:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_DELETE
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
            elif data[5] == 0x78:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_WRITE_TEMPLATE
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
                remarkId = (int)(data[8])
                if remarkId < 0:
                    remarkId += 256
                rs232FingerprintMessage.remarkId = remarkId
            elif data[5] == 0xA6:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PERMISSION
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
            elif data[5] == 0xA7:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_PERMISSION
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
            elif data[5] == 0x54:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_ALL_CLEAR
            elif data[5] == 0x74:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_EMPTY_ID
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
            elif data[5] == 0xA5:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION
            elif data[5] == 0xA3:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_REGISTER
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
            elif data[5] == 0x77:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_GET_TEMPLATE
                dataIndex = (int)(data[8])
                rs232FingerprintMessage.fingerprintDataIndex=dataIndex
                fingerprintId = (int)(data[7])
                if fingerprintId < 0:
                    fingerprintId += 256
                rs232FingerprintMessage.fingerprintId = fingerprintId
                rs232FingerprintMessage.data = byte2HexString(data,10)
            elif data[5] == 0x59:
                rs232FingerprintMessage.fingerprintType=Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_DEVICE_ID
            messageList.append(rs232FingerprintMessage)
        elif rs232Head == self.rs232CapacitorFuelHead:
            rs232Message.rs232DataType = RS232Message.CAPACITOR_FUEL_DATA
            rs232FuelMessage = Rs232FuelMessage()
            fuelData = data[2:6]
            if len(fuelData) > 0:
                fuelStr = ''.join(chr(i) for i in fuelData).encode().decode("UTF-8")
                try:
                    rs232FuelMessage.fuelPercent = (float)(fuelStr) / 100
                except:
                    print ("rs232 fuel error")
            rs232FuelMessage.alarm = (int)(data[6])
            messageList.append(rs232FuelMessage);
        elif rs232Head == self.rs232UltrasonicFuelHead:
            rs232Message.rs232DataType = RS232Message.ULTRASONIC_FUEL_DATA
            rs232FuelMessage = Rs232FuelMessage()
            curHeightData = data[2:4]
            curHeightStr = byte2HexString(curHeightData, 0);
            if curHeightStr.lower() == "ffff":
                rs232FuelMessage.curLiquidHeight=-999
            else:
                rs232FuelMessage.curLiquidHeight=(float)(curHeightStr) / 10
            tempData = data[4:6]
            tempStr = byte2HexString(tempData, 0);
            if tempStr.lower() == "ffff":
                rs232FuelMessage.temp = -999
            else:
                rs232FuelMessage.temp = (int)(tempStr[1:4]) / 10.0;
                if tempStr[0:1] == "1":
                    rs232FuelMessage.temp = -1 * rs232FuelMessage.temp
            fullHeightData = data[6:8]
            fullHeightStr = byte2HexString(fullHeightData, 0)
            if fullHeightStr.lower() == "ffff":
                rs232FuelMessage.fullLiquidHeight=-999
            else:
                rs232FuelMessage.fullLiquidHeight=(float)(fullHeightStr)
            rs232FuelMessage.liquidType = (int)(data[8]) + 1
            rs232FuelMessage.alarm = (int)(data[9])
            messageList.append(rs232FuelMessage);
        else:
            rs232DeviceMessage = Rs232DeviceMessage()
            rs232DeviceMessage.RS232Data = data
            messageList.append(rs232DeviceMessage);
            rs232Message.rs232DataType =RS232Message.OTHER_DEVICE_DATA
        rs232Message.rs232DeviceMessageList = messageList
        return rs232Message

    def parseSecondBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        isHistoryData = (byteArray[22] & 0x40) == 0x40
        latlngValid = (byteArray[22] & 0x80) == 0x80
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        strSp = byte2HexString(byteArray[35:37], 0);
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray, 23)
            longitude = bytes2Float(byteArray, 27)
            latitude = bytes2Float(byteArray, 31)
            azimuth = bytes2Short(byteArray, 37)
        else:
            if (byteArray[23] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,23)
            mnc_2g = bytes2Short(byteArray,25)
            lac_2g_1 = bytes2Short(byteArray,27)
            ci_2g_1 = bytes2Short(byteArray,29)
            lac_2g_2 = bytes2Short(byteArray,31)
            ci_2g_2 = bytes2Short(byteArray,33)
            lac_2g_3 = bytes2Short(byteArray,35)
            ci_2g_3 = bytes2Short(byteArray,37)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,23) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,25)
            ci_4g = bytes2Integer(byteArray, 27)
            earfcn_4g_1 = bytes2Short(byteArray, 31)
            pcid_4g_1 = bytes2Short(byteArray, 33)
            earfcn_4g_2 = bytes2Short(byteArray, 35)
            pcid_4g_2 = bytes2Short(byteArray,37)
        if strSp.find("f") == -1:
                speed = -1
        else:
            speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.isHistoryData = isHistoryData
        bluetoothPeripheralDataMessage.serialNo = serialNo
        bluetoothPeripheralDataMessage.latlngValid = latlngValid
        bluetoothPeripheralDataMessage.altitude = altitude
        bluetoothPeripheralDataMessage.latitude = latitude
        bluetoothPeripheralDataMessage.longitude = longitude
        bluetoothPeripheralDataMessage.azimuth = azimuth
        bluetoothPeripheralDataMessage.isHadLocationInfo = True
        bluetoothPeripheralDataMessage.protocolHeadType = 0x12
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bluetoothPeripheralDataMessage.is_4g_lbs = is_4g_lbs
        bluetoothPeripheralDataMessage.is_2g_lbs = is_2g_lbs
        bluetoothPeripheralDataMessage.mcc_4g = mcc_4g
        bluetoothPeripheralDataMessage.mnc_4g = mnc_4g
        bluetoothPeripheralDataMessage.ci_4g = ci_4g
        bluetoothPeripheralDataMessage.earfcn_4g_1 = earfcn_4g_1
        bluetoothPeripheralDataMessage.pcid_4g_1 = pcid_4g_1
        bluetoothPeripheralDataMessage.earfcn_4g_2 = earfcn_4g_2
        bluetoothPeripheralDataMessage.pcid_4g_2 = pcid_4g_2
        bluetoothPeripheralDataMessage.mcc_2g = mcc_2g
        bluetoothPeripheralDataMessage.mnc_2g = mnc_2g
        bluetoothPeripheralDataMessage.lac_2g_1 = lac_2g_1
        bluetoothPeripheralDataMessage.ci_2g_1 = ci_2g_1
        bluetoothPeripheralDataMessage.lac_2g_2 = lac_2g_2
        bluetoothPeripheralDataMessage.ci_2g_2 = ci_2g_2
        bluetoothPeripheralDataMessage.lac_2g_3 = lac_2g_3
        bluetoothPeripheralDataMessage.ci_2g_3 = ci_2g_3
        bleData = byteArray[39:len(byteArray)]
        if len(bleData) <= 0:
            return bluetoothPeripheralDataMessage
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0);
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001;
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x07:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL
            i = 2
            while i < len(bleData):
                bleFuelData = BleFuelData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                valueTemp = bytes2Short(bleData,i+7)
                value=0
                if valueTemp == 65535:
                    value = -999
                else:
                    value = valueTemp
                temperatureTemp = bytes2Short(bleData,i+9)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                status = bleData[i+13]
                if status == 255:
                    status = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 14]) < 0:
                    rssiTemp =  (int) (bleData[i + 14]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 14])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.alarm = status
                bleFuelData.value = value
                bleFuelData.voltage = formatNumb(voltage)
                bleFuelData.temp = formatNumb(temperature)
                bleDataList.append(bleFuelData)
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage

    def parseBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.isHistoryData = (byteArray[15] & 0x80) != 0x00
        bluetoothPeripheralDataMessage.serialNo = serialNo
        bluetoothPeripheralDataMessage.protocolHeadType = byteArray[2]
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bleData = byteArray[22:len(byteArray)]
        if len(bleData) <= 0:
            return bluetoothPeripheralDataMessage
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0);
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            strSp = byte2HexString(bleData[23:25], 0);
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)
            if strSp.find("f") == -1:
                speed = -1;
            else:
                speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.satelliteCount  =satelliteNumber
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            strSp = byte2HexString(bleData[23:25], 0);
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)
            if strSp.find("f") == -1:
                speed = -1;
            else:
                speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.satelliteCount  =satelliteNumber
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x07:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL
            i = 2
            while i < len(bleData):
                bleFuelData = BleFuelData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                valueTemp = bytes2Short(bleData,i+7)
                value=0
                if valueTemp == 65535:
                    value = -999
                else:
                    value = valueTemp
                temperatureTemp = bytes2Short(bleData,i+9)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                status = bleData[i+13]
                if status == 255:
                    status = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 14]) < 0:
                    rssiTemp =  (int) (bleData[i + 14]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 14])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.alarm = status
                bleFuelData.value = value
                bleFuelData.voltage = formatNumb(voltage)
                bleFuelData.temp = formatNumb(temperature)
                bleDataList.append(bleFuelData)
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x0d:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_Customer2397
            i = 2
            while i < len(bleData):
                bleCustomer2397SensorData = BleCustomer2397SensorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                i+=6
                i+=1
                rawDataLen = (int)(bleData[i])
                if rawDataLen < 0:
                    rawDataLen += 256
                if i + rawDataLen >= len(bleData) or rawDataLen < 1:
                    break
                i+=1
                rawData = bleData[i:i+rawDataLen-1]
                i += rawDataLen -1
                rssiTemp = 0
                if (int)(bleData[i]) < 0:
                    rssiTemp = (int)(bleData[i]) + 256
                else:
                    rssiTemp = (int)(bleData[i])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                i+=1
                bleCustomer2397SensorData.rssi = rssi
                bleCustomer2397SensorData.mac = mac
                bleCustomer2397SensorData.rawData = rawData
                bleDataList.append(bleCustomer2397SensorData)
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).encode().decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).encode().decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).encode().decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).encode().decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).encode().decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            # configMessage.isNeedResp = isNeedResp
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            # forwardMessage.isNeedResp = isNeedResp
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            # ussdMessage.isNeedResp = isNeedResp
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None

    def parseGpsDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        gpsDriverBehaviorMessage = GpsDriverBehaviorMessage()
        behaviorType = (int)(byteArray[15])
        gpsDriverBehaviorMessage.imei = imei
        gpsDriverBehaviorMessage.serialNo = serialNo
        # gpsDriverBehaviorMessage.isNeedResp = isNeedResp
        gpsDriverBehaviorMessage.orignBytes = byteArray
        gpsDriverBehaviorMessage.behaviorType = behaviorType
        dateStr = "20" + byte2HexString(byteArray[16:22],0)
        gpsDriverBehaviorMessage.startDate = GTM0(dateStr)
        gpsDriverBehaviorMessage.startAltitude = bytes2Float(byteArray,22)
        gpsDriverBehaviorMessage.startLongitude = bytes2Float(byteArray,26)
        gpsDriverBehaviorMessage.startLatitude = bytes2Float(byteArray,30)
        speedStr = byte2HexString(byteArray[34:36],0)
        gpsDriverBehaviorMessage.startSpeed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        gpsDriverBehaviorMessage.startAzimuth = bytes2Short(byteArray,36)

        dateStr = "20" + byte2HexString(byteArray[38:44],0)
        gpsDriverBehaviorMessage.endDate = GTM0(dateStr)
        gpsDriverBehaviorMessage.endAltitude = bytes2Float(byteArray,44)
        gpsDriverBehaviorMessage.endLongitude = bytes2Float(byteArray,48)
        gpsDriverBehaviorMessage.endLatitude = bytes2Float(byteArray,52)
        speedStr = byte2HexString(byteArray[56:58],0)
        gpsDriverBehaviorMessage.endSpeed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        gpsDriverBehaviorMessage.endAzimuth = bytes2Short(byteArray,58)

        return gpsDriverBehaviorMessage



    def getAccelerationData(self,byteArray,imei,curParseIndex):
        acceleration = AccelerationData()
        acceleration.imei = imei
        dateStr = "20" + byte2HexString(byteArray[curParseIndex:curParseIndex + 6],0)
        acceleration.date = GTM0(dateStr)
        acceleration.gpsWorking = (byteArray[curParseIndex + 6] & 0x20) == 0x00
        acceleration.isHistoryData = (byteArray[curParseIndex + 6] & 0x80) != 0x00
        acceleration.satelliteNumber = byteArray[curParseIndex + 6] & 0x1F
        acceleration.latlngValid = (byteArray[curParseIndex + 6] & 0x40) != 0x00
        axisXDirect = -1
        if (byteArray[curParseIndex + 7] & 0x80) == 0x80:
            axisXDirect = 1
        acceleration.axisX = ((byteArray[curParseIndex + 7] & 0x7F & 0xff) + (((byteArray[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[curParseIndex + 8] & 0x08) == 0x08:
            axisYDirect = 1
        acceleration.axisY = (((((byteArray[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((byteArray[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (byteArray[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect
        axisZDirect = -1
        if (byteArray[curParseIndex + 10] & 0x80) == 0x80:
            axisZDirect = 1
        acceleration.axisZ = ((byteArray[curParseIndex + 10] & 0x7F & 0xff) + (((byteArray[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if acceleration.latlngValid:
            acceleration.altitude = bytes2Float(byteArray,curParseIndex + 12)
            acceleration.longitude = bytes2Float(byteArray,curParseIndex + 16)
            acceleration.latitude = bytes2Float(byteArray,curParseIndex + 20)
            acceleration.azimuth = bytes2Short(byteArray,curParseIndex + 26)
            speedStr = byte2HexString(byteArray[curParseIndex + 24:curParseIndex + 26],0)
            acceleration.speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (byteArray[curParseIndex + 12] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,curParseIndex + 12)
            mnc_2g = bytes2Short(byteArray,curParseIndex + 14)
            lac_2g_1 = bytes2Short(byteArray,curParseIndex + 16)
            ci_2g_1 = bytes2Short(byteArray,curParseIndex + 18)
            lac_2g_2 = bytes2Short(byteArray,curParseIndex + 20)
            ci_2g_2 = bytes2Short(byteArray,curParseIndex + 22)
            lac_2g_3 = bytes2Short(byteArray,curParseIndex + 24)
            ci_2g_3 = bytes2Short(byteArray,curParseIndex + 26)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,curParseIndex + 12) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,curParseIndex + 14)
            ci_4g = bytes2Integer(byteArray, curParseIndex + 16)
            earfcn_4g_1 = bytes2Short(byteArray, curParseIndex + 20)
            pcid_4g_1 = bytes2Short(byteArray, curParseIndex + 22)
            earfcn_4g_2 = bytes2Short(byteArray, curParseIndex + 24)
            pcid_4g_2 = bytes2Short(byteArray,curParseIndex + 26)

        acceleration.is_2g_lbs = is_2g_lbs
        acceleration.mcc_4g = mcc_4g
        acceleration.mnc_4g = mnc_4g
        acceleration.ci_4g = ci_4g
        acceleration.earfcn_4g_1 = earfcn_4g_1
        acceleration.pcid_4g_1 = pcid_4g_1
        acceleration.earfcn_4g_2 = earfcn_4g_2
        acceleration.pcid_4g_2 = pcid_4g_2
        acceleration.mcc_2g = mcc_2g
        acceleration.mnc_2g = mnc_2g
        acceleration.lac_2g_1 = lac_2g_1
        acceleration.ci_2g_1 = ci_2g_1
        acceleration.lac_2g_2 = lac_2g_2
        acceleration.ci_2g_2 = ci_2g_2
        acceleration.lac_2g_3 = lac_2g_3
        acceleration.ci_2g_3 = ci_2g_3
        if len(byteArray) > 44:
            gyroscopeAxisXDirect = -1
            if (byteArray[curParseIndex + 28] & 0x80) == 0x80:
                gyroscopeAxisXDirect = 1
            gyroscopeAxisX = (((byteArray[curParseIndex + 28] & 0x7F & 0xff) << 4) + ((byteArray[curParseIndex + 29] & 0xf0) >> 4)) * gyroscopeAxisXDirect;
            acceleration.gyroscopeAxisX = gyroscopeAxisX
            gyroscopeAxisYDirect = -1
            if (byteArray[curParseIndex + 29] & 0x08) == 0x08:
                gyroscopeAxisYDirect = 1
            gyroscopeAxisY = ((((byteArray[curParseIndex + 29] & 0x07) << 4) & 0xff) + (byteArray[curParseIndex + 30] &  0xff))* gyroscopeAxisYDirect;
            acceleration.gyroscopeAxisY = gyroscopeAxisY
            gyroscopeAxisZDirect = -1
            if (byteArray[curParseIndex + 31] & 0x80) == 0x80:
                gyroscopeAxisZDirect = 1
            gyroscopeAxisZ = (((byteArray[curParseIndex + 31] & 0x7F & 0xff)<< 4) + ((byteArray[curParseIndex + 32] & 0xf0) >> 4)) * gyroscopeAxisZDirect;
            acceleration.gyroscopeAxisZ = gyroscopeAxisZ
        return acceleration

    def getAccidentAccelerationData(self,byteArray,imei,curParseIndex):
        acceleration = AccelerationData()
        acceleration.imei = imei
        dateStr = "20" + byte2HexString(byteArray[curParseIndex:curParseIndex + 6],0)
        acceleration.date = GTM0(dateStr)
        acceleration.gpsWorking = (byteArray[curParseIndex + 6] & 0x20) == 0x00
        acceleration.isHistoryData = (byteArray[curParseIndex + 6] & 0x80) != 0x00
        acceleration.satelliteNumber = byteArray[curParseIndex + 6] & 0x1F
        acceleration.latlngValid = (byteArray[curParseIndex + 6] & 0x40) != 0x00
        axisXDirect = -1
        if (byteArray[curParseIndex + 7] & 0x80) == 0x80:
            axisXDirect = 1
        acceleration.axisX = ((byteArray[curParseIndex + 7] & 0x7F & 0xff) + (((byteArray[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[curParseIndex + 8] & 0x08) == 0x08:
            axisYDirect = 1
        acceleration.axisY = (((((byteArray[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((byteArray[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (byteArray[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect
        axisZDirect = -1
        if (byteArray[curParseIndex + 10] & 0x80) == 0x80:
            axisZDirect = 1
        acceleration.axisZ = ((byteArray[curParseIndex + 10] & 0x7F & 0xff) + (((byteArray[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if acceleration.latlngValid:
            acceleration.altitude = bytes2Float(byteArray,curParseIndex + 12)
            acceleration.longitude = bytes2Float(byteArray,curParseIndex + 16)
            acceleration.latitude = bytes2Float(byteArray,curParseIndex + 20)
            speedStr = byte2HexString(byteArray[curParseIndex + 24:curParseIndex + 26],0)
            acceleration.speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
            acceleration.azimuth = bytes2Short(byteArray,curParseIndex + 26)
        else:
            if (byteArray[curParseIndex + 12] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,curParseIndex + 12)
            mnc_2g = bytes2Short(byteArray,curParseIndex + 14)
            lac_2g_1 = bytes2Short(byteArray,curParseIndex + 16)
            ci_2g_1 = bytes2Short(byteArray,curParseIndex + 18)
            lac_2g_2 = bytes2Short(byteArray,curParseIndex + 20)
            ci_2g_2 = bytes2Short(byteArray,curParseIndex + 22)
            lac_2g_3 = bytes2Short(byteArray,curParseIndex + 24)
            ci_2g_3 = bytes2Short(byteArray,curParseIndex + 26)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,curParseIndex + 12) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,curParseIndex + 14)
            ci_4g = bytes2Integer(byteArray, curParseIndex + 16)
            earfcn_4g_1 = bytes2Short(byteArray, curParseIndex + 20)
            pcid_4g_1 = bytes2Short(byteArray, curParseIndex + 22)
            earfcn_4g_2 = bytes2Short(byteArray, curParseIndex + 24)
            pcid_4g_2 = bytes2Short(byteArray,curParseIndex + 26)
        acceleration.is_4g_lbs = is_4g_lbs
        acceleration.is_2g_lbs = is_2g_lbs
        acceleration.mcc_4g = mcc_4g
        acceleration.mnc_4g = mnc_4g
        acceleration.ci_4g = ci_4g
        acceleration.earfcn_4g_1 = earfcn_4g_1
        acceleration.pcid_4g_1 = pcid_4g_1
        acceleration.earfcn_4g_2 = earfcn_4g_2
        acceleration.pcid_4g_2 = pcid_4g_2
        acceleration.mcc_2g = mcc_2g
        acceleration.mnc_2g = mnc_2g
        acceleration.lac_2g_1 = lac_2g_1
        acceleration.ci_2g_1 = ci_2g_1
        acceleration.lac_2g_2 = lac_2g_2
        acceleration.ci_2g_2 = ci_2g_2
        acceleration.lac_2g_3 = lac_2g_3
        acceleration.ci_2g_3 = ci_2g_3
        return acceleration

    def parseAccelerationAlarmMessage(self,byteArray):
        length = bytes2Short(byteArray,3)
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        accidentAccelerationMessage = AccidentAccelerationMessage()
        accidentAccelerationMessage.serialNo = serialNo
        # accidentAccelerationMessage.isNeedResp = isNeedResp
        accidentAccelerationMessage.imei = imei
        accidentAccelerationMessage.orignBytes = byteArray
        dataLength = length - 16
        beginIndex = 16
        accidentAccelerationList = []
        while beginIndex < dataLength:
            curParseIndex = beginIndex
            beginIndex = beginIndex + 28
            accelerationData = self.getAccidentAccelerationData(byteArray,imei,curParseIndex)
            accidentAccelerationList.append(accelerationData)
        accidentAccelerationMessage.accelerationList = accidentAccelerationList
        return accidentAccelerationMessage

    def parseAccelerationDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        behaviorType = (int)(byteArray[15])
        accelerationDriverBehaviorMessage = AccelerationDriverBehaviorMessage()
        accelerationDriverBehaviorMessage.imei = imei
        accelerationDriverBehaviorMessage.serialNo = serialNo
        # accelerationDriverBehaviorMessage.isNeedResp = isNeedResp
        accelerationDriverBehaviorMessage.orignBytes = byteArray
        accelerationDriverBehaviorMessage.behaviorType = behaviorType
        beginIndex = 16
        accelerationData = self.getAccelerationData(byteArray,imei,beginIndex)
        accelerationDriverBehaviorMessage.accelerationData = accelerationData
        return accelerationDriverBehaviorMessage


    def parseSecondDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        data = byteArray[15:len(byteArray)]
        samplingIntervalAccOn = bytes2Short(data, 0)
        samplingIntervalAccOff = bytes2Short(data, 2)
        angleCompensation = (int)(data[4])
        distanceCompensation = bytes2Short(data, 5)
        limit = bytes2Short(data,7)
        speedLimit = limit & 0x7FFF
        if (limit & 0x8000) != 0 :
            speedLimit = speedLimit * 1.609344
        networkSignal = limit & 0x7F
        isGpsWorking = (data[9] & 0x20) == 0x00
        isHistoryData = (data[9] & 0x80) != 0x00
        satelliteNumber = data[9] & 0x1F
        gSensorSensitivity = (data[10] & 0xF0) >> 4
        isManagerConfigured1 = (data[10] & 0x01) != 0x00
        isManagerConfigured2 = (data[10] & 0x02) != 0x00
        isManagerConfigured3 = (data[10] & 0x04) != 0x00
        isManagerConfigured4 = (data[10] & 0x08) != 0x00
        antitheftedStatus = 0
        if (data[11] & 0x10) != 0x00:
            antitheftedStatus = 1
        heartbeatInterval = data[12] & 0x00FF
        relayStatus = data[13] & 0x3F
        rlyMode =  data[13] & 0xCF
        smsLanguageType = data[13] & 0xF
        isRelayWaiting = ((data[13] & 0xC0) != 0x00) and ((data[13] & 0x80) == 0x00)
        dragThreshold = bytes2Short(data, 14)

        input = bytes2Short(data, 16)
        iopIgnition = (input & self.MASK_IGNITION) == self.MASK_IGNITION
        iopPowerCutOff = (input & self.MASK_POWER_CUT) == self.MASK_POWER_CUT
        iopACOn = (input & self.MASK_AC) == self.MASK_AC
        externalPowerSupply = (input & 0x8000) == 0x8000
        input1 = (input & 0x100) == 0x100
        input2 = (input & 0x2000) == 0x2000
        input3 = (input & 0x1000) == 0x1000
        input4 = (input & 0x800) == 0x800
        input5 = (input & 0x400) == 0x400
        input6 = (input & 0x200) == 0x200
        isHadFmsData = (input & 0x80) == 0x80;
        isMileageSrcIsFms = (input & 0x40) == 0x40
        output1 = (data[18] & 0x20) == 0x20
        output2 = (data[18] & 0x10) == 0x10
        output3 = (data[18] & 0x8) == 0x8
        output12V = (data[18] & 0x20) == 0x20
        outputVout = (data[18] & 0x40) == 0x40
        accdetSettingStatus = 0
        if (input & 0x1) == 0x1:
            accdetSettingStatus = 1
        str = byte2HexString(data, 20)
        analoginput = 0
        if str.lower().startswith("ffff"):
            try:
                analoginput = (float)("{0}.{1}".format(str[0:2],str[2:4]))
            except:
                print ("alalog1 error")
        else:
            analoginput = -999
        analoginput2 = 0
        str = byte2HexString(data, 22)
        if str.lower().startswith("ffff"):
            try:
                analoginput2 = (float)("{0}.{1}".format(str[0:2],str[2:4]))
            except:
                print ("alalog2 error")
        else:
            analoginput2 = -999
        analoginput3 = 0
        str = byte2HexString(data, 24)
        if str.lower().startswith("ffff"):
            try:
                analoginput3 = (float)("{0}.{1}".format(str[0:2],str[2:4]))
            except:
                print ("alalog3 error")
        else:
            analoginput3 = -999
        lastMileageDiff = bytes2Integer(data, 26)
        originalAlarmCode = (int) (data[30])
        isAlarmData = command[2] == 0x14
        isSendSmsAlarmToManagerPhone = (data[31] & 0x20) == 0x20
        isSendSmsAlarmWhenDigitalInput2Change = (data[31] & 0x10) == 0x10
        jammerDetectionStatus = (data[31] & 0xC)
        mileage = bytes2Integer(data, 32)
        batteryBytes = [data[36]]
        batteryStr = byte2HexString(batteryBytes, 0)
        dateStr = "20" + byte2HexString(data[37:43],0)
        gtm0 = GTM0(dateStr)
        latlngValid = (data[9] & 0x40) != 0x00
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(data,43)
            latitude = bytes2Float(data,51)
            longitude = bytes2Float(data,47)
            azimuth = bytes2Short(data,57)
            speedStr = byte2HexString(data[55:57],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (data[43] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(data,43)
            mnc_2g = bytes2Short(data,45)
            lac_2g_1 = bytes2Short(data,47)
            ci_2g_1 = bytes2Short(data,49)
            lac_2g_2 = bytes2Short(data,51)
            ci_2g_2 = bytes2Short(data,53)
            lac_2g_3 = bytes2Short(data,55)
            ci_2g_3 = bytes2Short(data,57)
        if is_4g_lbs:
            mcc_4g = bytes2Short(data,43) & 0x7FFF
            mnc_4g = bytes2Short(data,45)
            ci_4g = bytes2Integer(data, 47)
            earfcn_4g_1 = bytes2Short(data, 51)
            pcid_4g_1 = bytes2Short(data, 53)
            earfcn_4g_2 = bytes2Short(data, 55)
            pcid_4g_2 = bytes2Short(data,57)
        batteryVoltageStr = byte2HexString(data[59:61],0)
        batteryVoltage = (float)(batteryVoltageStr) / 100
        externalPowerVoltage = 0
        externalPowerVoltageStr = byte2HexString(data[61:63], 0)
        externalPowerVoltage = (float(externalPowerVoltageStr) ) / 100


        rpm = 0
        rpm = bytes2Short(data,63)
        if rpm == 32768 or rpm == 65535:
            rpm = -999
        isSmartUploadSupport = (data[65] & 0x8) == 0x8
        supportChangeBattery =  (data[66] & 0x8) == 0x8
        deviceTemp = -999
        if byteArray[68] != 0xff:
            deviceTemp = byteArray[68] & 0x7F
            if (byteArray[68] & 0x80) == 0x80:
                deviceTemp = -1 * deviceTemp
        fmsSpeed = data[66]
        locationMessage = LocationInfoMessage()
        locationMessage.protocolHeadType = 0x13
        if isAlarmData:
            locationMessage = LocationAlarmMessage()
            locationMessage.protocolHeadType = 0x14
        locationMessage.isHadFmsData = isHadFmsData
        if len(data) >= 74:
            hdop = bytes2Short(data,69)
            locationMessage.hdop = hdop
            if isHadFmsData:
                lastMileageDiff = -999
                engineHours = bytes2Integer(data, 26)
                if engineHours == 4294967295l:
                    engineHours = -999
                coolingFluidTemp = data[71]
                if coolingFluidTemp < 0:
                    coolingFluidTemp = coolingFluidTemp + 256
                if coolingFluidTemp == 255:
                    coolingFluidTemp = -999;
                else:
                    coolingFluidTemp = coolingFluidTemp - 40;

                remainFuel = data[72]
                if remainFuel < 0:
                    remainFuel = remainFuel + 256
                if remainFuel == 255:
                    remainFuel = -999

                engineLoad = data[73]
                if engineLoad < 0:
                    engineLoad = engineLoad + 256
                if engineLoad == 255:
                    engineLoad = -999
                locationMessage.fmsEngineHours = engineHours
                locationMessage.remainFuelRate = remainFuel
                locationMessage.engineLoad = engineLoad
                locationMessage.coolingFluidTemp = coolingFluidTemp
                locationMessage.fmsSpeed  = fmsSpeed
                if data.length >= 78:
                    fmsAccumulatingFuelConsumption =  bytes2Integer(data, 74);
                    if fmsAccumulatingFuelConsumption == 4294967295:
                        fmsAccumulatingFuelConsumption = -999;
                    else:
                        fmsAccumulatingFuelConsumption = fmsAccumulatingFuelConsumption * 1000
                    locationMessage.accumulatingFuelConsumption = fmsAccumulatingFuelConsumption
                supportChangeBattery = False

        if isMileageSrcIsFms:
            locationMessage.mileageSource = 2
        locationMessage.orignBytes = byteArray
        locationMessage.serialNo = serialNo
        # locationMessage.isNeedResp = isNeedResp
        locationMessage.imei = imei
        locationMessage.samplingIntervalAccOn = samplingIntervalAccOn
        locationMessage.samplingIntervalAccOff = samplingIntervalAccOff
        locationMessage.angleCompensation = angleCompensation
        locationMessage.distanceCompensation = distanceCompensation
        locationMessage.overspeedLimit = speedLimit
        locationMessage.gpsWorking = isGpsWorking
        locationMessage.isHistoryData = isHistoryData
        locationMessage.satelliteNumber = satelliteNumber
        locationMessage.gSensorSensitivity = gSensorSensitivity
        locationMessage.isManagerConfigured1 = isManagerConfigured1
        locationMessage.isManagerConfigured2 = isManagerConfigured2
        locationMessage.isManagerConfigured3 = isManagerConfigured3
        locationMessage.isManagerConfigured4 = isManagerConfigured4
        locationMessage.antitheftedStatus = antitheftedStatus
        locationMessage.heartbeatInterval = heartbeatInterval
        locationMessage.relayStatus =relayStatus
        locationMessage.isRelayWaiting = isRelayWaiting
        locationMessage.dragThreshold = dragThreshold
        locationMessage.IOP = input
        locationMessage.iopIgnition = iopIgnition
        locationMessage.iopPowerCutOff = iopPowerCutOff
        locationMessage.iopACOn = iopACOn
        locationMessage.analogInput1 = analoginput
        locationMessage.analogInput2 = analoginput2
        locationMessage.originalAlarmCode = originalAlarmCode
        locationMessage.mileage = mileage
        locationMessage.externalPowerSupply = externalPowerSupply
        locationMessage.input1 = input1
        locationMessage.input2 = input2
        locationMessage.input3 = input3
        locationMessage.input4 = input4
        locationMessage.input5 = input5
        locationMessage.input6 = input6
        locationMessage.lastMileageDiff = lastMileageDiff
        locationMessage.output1 = output1
        locationMessage.isSmartUploadSupport = isSmartUploadSupport
        locationMessage.supportChangeBattery = supportChangeBattery
        locationMessage.batteryVoltage = batteryVoltage
        locationMessage.deviceTemp = deviceTemp
        locationMessage.is_4g_lbs = is_4g_lbs
        locationMessage.is_2g_lbs = is_2g_lbs
        locationMessage.mcc_4g = mcc_4g
        locationMessage.mnc_4g = mnc_4g
        locationMessage.ci_4g = ci_4g
        locationMessage.earfcn_4g_1 = earfcn_4g_1
        locationMessage.pcid_4g_1 = pcid_4g_1
        locationMessage.earfcn_4g_2 = earfcn_4g_2
        locationMessage.pcid_4g_2 = pcid_4g_2
        locationMessage.mcc_2g = mcc_2g
        locationMessage.mnc_2g = mnc_2g
        locationMessage.lac_2g_1 = lac_2g_1
        locationMessage.ci_2g_1 = ci_2g_1
        locationMessage.lac_2g_2 = lac_2g_2
        locationMessage.ci_2g_2 = ci_2g_2
        locationMessage.lac_2g_3 = lac_2g_3
        locationMessage.ci_2g_3 = ci_2g_3
        try:
            charge = (int)(batteryStr)
            if  charge == 0:
                charge = 100
            locationMessage.batteryCharge = charge
        except:
            print ("charge error")
        locationMessage.date = gtm0
        locationMessage.latlngValid = latlngValid
        locationMessage.altitude = altitude
        locationMessage.latitude = latitude
        locationMessage.longitude = longitude
        if  latlngValid:
            locationMessage.speed = speed
        else:
            locationMessage.speed = 0
        locationMessage.azimuth = azimuth
        locationMessage.externalPowerVoltage = externalPowerVoltage
        locationMessage.networkSignal = networkSignal
        locationMessage.output2 = output2
        locationMessage.output3 = output3
        locationMessage.output12V = output12V
        locationMessage.outputVout = outputVout
        locationMessage.rpm = rpm
        locationMessage.analogInput3 = analoginput3
        locationMessage.rlyMode = rlyMode
        locationMessage.smsLanguageType = smsLanguageType
        locationMessage.accdetSettingStatus = accdetSettingStatus
        locationMessage.isSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigitalInput2Change
        locationMessage.isSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone
        locationMessage.jammerDetectionStatus = jammerDetectionStatus
        return locationMessage

    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        data = byteArray[15:len(byteArray)]
        samplingIntervalAccOn = bytes2Short(data, 0)
        samplingIntervalAccOff = bytes2Short(data, 2)
        angleCompensation = (int)(data[4])
        distanceCompensation = bytes2Short(data, 5)
        limit = bytes2Short(data,7)
        speedLimit = limit & 0x7FFF
        if (limit & 0x8000) != 0 :
            speedLimit = speedLimit * 1.609344
        networkSignal = limit & 0x7F
        isGpsWorking = (data[9] & 0x20) == 0x00
        isHistoryData = (data[9] & 0x80) != 0x00
        satelliteNumber = data[9] & 0x1F
        gSensorSensitivity = (data[10] & 0xF0) >> 4
        isManagerConfigured1 = (data[10] & 0x01) != 0x00
        isManagerConfigured2 = (data[10] & 0x02) != 0x00
        isManagerConfigured3 = (data[10] & 0x04) != 0x00
        isManagerConfigured4 = (data[10] & 0x08) != 0x00
        antitheftedStatus = 0
        if (data[11] & 0x10) != 0x00:
            antitheftedStatus = 1
        heartbeatInterval = data[12] & 0x00FF
        relayStatus = data[13] & 0x3F
        rlyMode =  data[13] & 0xCF
        smsLanguageType = data[13] & 0xF
        isRelayWaiting = ((data[13] & 0xC0) != 0x00) and ((data[13] & 0x80) == 0x00)
        dragThreshold = bytes2Short(data, 14)
        iop = bytes2Short(data, 16)
        iopIgnition = (iop & self.MASK_IGNITION) == self.MASK_IGNITION
        iopPowerCutOff = (iop & self.MASK_POWER_CUT) == self.MASK_POWER_CUT
        iopACOn = (iop & self.MASK_AC) == self.MASK_AC
        iopRs232DeviceValid = (iop & self.IOP_RS232_DEVICE_VALID) != self.IOP_RS232_DEVICE_VALID
        input1 = iopIgnition
        input2 = iopACOn
        input3 = (iop & 0x1000) == 0x1000
        input4 = (iop & 0x800) == 0x800
        input5 = (iop & 0x10) == 0x10
        input6 = (iop & 0x08) == 0x08
        output1 = (iop & 0x0400) == 0x0400
        output2 = (iop & 0x200) == 0x200
        output3 = (iop & 0x100) == 0x100
        output12V = (iop & 0x10) == 0x10
        outputVout = (iop & 0x8) == 0x8
        speakerStatus = 0
        if (iop & 0x40) ==  0x40:
            speakerStatus = 1
        rs232PowerOf5V = 0
        if (iop & 0x20) ==  0x20:
            rs232PowerOf5V = 1
        accdetSettingStatus = 0
        if (iop & 0x1) ==  0x1:
            accdetSettingStatus = 1
        str = byte2HexString(data, 18)
        analoginput = 0
        try:
            analoginput = (float)("{0}.{1}".format(str[0:2],str[2:4]))
        except:
            print ("analog error")
        analoginput2 = 0
        str = byte2HexString(data, 20)
        try:
            analoginput2 = (float)("{0}.{1}".format(str[0:2],str[2:4]))
        except:
            print ("analog2 error")
        originalAlarmCode = (int) (data[22])
        isAlarmData = command[2] == 0x04 or command[2] == 0x18
        isSendSmsAlarmToManagerPhone = (data[23] & 0x20) == 0x20
        isSendSmsAlarmWhenDigitalInput2Change = (data[23] & 0x10) == 0x10
        jammerDetectionStatus = (data[23] & 0xC)
        mileage = bytes2Integer(data, 24)
        batteryBytes = [data[28]]
        batteryStr = byte2HexString(batteryBytes, 0)
        dateStr = "20" + byte2HexString(data[29:35],0)
        gtm0 = GTM0(dateStr)
        latlngValid = (data[9] & 0x40) != 0x00
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(data,35)
            latitude = bytes2Float(data,43)
            longitude = bytes2Float(data,39)
            azimuth = bytes2Short(data,49)
            speedStr = byte2HexString(data[47:49],0)
            if speedStr != "ffff":
                speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (data[35] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(data,35)
            mnc_2g = bytes2Short(data,37)
            lac_2g_1 = bytes2Short(data,39)
            ci_2g_1 = bytes2Short(data,41)
            lac_2g_2 = bytes2Short(data,43)
            ci_2g_2 = bytes2Short(data,45)
            lac_2g_3 = bytes2Short(data,47)
            ci_2g_3 = bytes2Short(data,49)
        if is_4g_lbs:
            mcc_4g = bytes2Short(data,35) & 0x7FFF
            mnc_4g = bytes2Short(data,37)
            ci_4g = bytes2Integer(data, 39)
            earfcn_4g_1 = bytes2Short(data, 43)
            pcid_4g_1 = bytes2Short(data, 45)
            earfcn_4g_2 = bytes2Short(data, 47)
            pcid_4g_2 = bytes2Short(data,49)
        externalPowerVoltage = 0
        if len(data) >= 53:
            externalPowerVoltageStr = byte2HexString(data[51:53], 0)
            externalPowerVoltage = (float(externalPowerVoltageStr) ) / 100
        analogInput3 = 0
        rpm = 0
        protocolHead = byteArray[2]
        locationMessage = LocationInfoMessage()
        if isAlarmData:
            locationMessage = LocationAlarmMessage()
        locationMessage.protocolHeadType = protocolHead
        if len(data) >= 65 and (protocolHead == 0x16 or protocolHead == 0x18):
            axisX = bytes2SingleShort(data[53:55],0)
            axisY = bytes2SingleShort(data[55:57],0)
            axisZ = bytes2SingleShort(data[57:59],0)
            gyroscopeAxisX = bytes2SingleShort(data[59:61],0)
            gyroscopeAxisY = bytes2SingleShort(data[61:63],0)
            gyroscopeAxisZ = bytes2SingleShort(data[63:65],0)
            locationMessage.axisX = axisX
            locationMessage.axisY = axisY
            locationMessage.axisZ = axisZ
            locationMessage.gyroscopeAxisX = gyroscopeAxisX
            locationMessage.gyroscopeAxisY = gyroscopeAxisY
            locationMessage.gyroscopeAxisZ = gyroscopeAxisZ
        locationMessage.orignBytes = byteArray
        locationMessage.serialNo = serialNo
        # locationMessage.isNeedResp = isNeedResp
        locationMessage.imei = imei
        locationMessage.samplingIntervalAccOn = samplingIntervalAccOn
        locationMessage.samplingIntervalAccOff = samplingIntervalAccOff
        locationMessage.angleCompensation = angleCompensation
        locationMessage.distanceCompensation = distanceCompensation
        locationMessage.overspeedLimit = speedLimit
        locationMessage.gpsWorking = isGpsWorking
        locationMessage.isHistoryData = isHistoryData
        locationMessage.satelliteNumber = satelliteNumber
        locationMessage.gSensorSensitivity = gSensorSensitivity
        locationMessage.isManagerConfigured1 = isManagerConfigured1
        locationMessage.isManagerConfigured2 = isManagerConfigured2
        locationMessage.isManagerConfigured3 = isManagerConfigured3
        locationMessage.isManagerConfigured4 = isManagerConfigured4
        locationMessage.antitheftedStatus = antitheftedStatus
        locationMessage.heartbeatInterval = heartbeatInterval
        locationMessage.relayStatus =relayStatus
        locationMessage.isRelayWaiting = isRelayWaiting
        locationMessage.dragThreshold = dragThreshold
        locationMessage.IOP = iop
        locationMessage.iopIgnition = iopIgnition
        locationMessage.iopPowerCutOff = iopPowerCutOff
        locationMessage.iopACOn = iopACOn
        locationMessage.analogInput1 = analoginput
        locationMessage.analogInput2 = analoginput2
        locationMessage.originalAlarmCode = originalAlarmCode
        locationMessage.mileage = mileage
        locationMessage.is_4g_lbs = is_4g_lbs
        locationMessage.is_2g_lbs = is_2g_lbs
        locationMessage.mcc_4g = mcc_4g
        locationMessage.mnc_4g = mnc_4g
        locationMessage.ci_4g = ci_4g
        locationMessage.earfcn_4g_1 = earfcn_4g_1
        locationMessage.pcid_4g_1 = pcid_4g_1
        locationMessage.earfcn_4g_2 = earfcn_4g_2
        locationMessage.pcid_4g_2 = pcid_4g_2
        locationMessage.mcc_2g = mcc_2g
        locationMessage.mnc_2g = mnc_2g
        locationMessage.lac_2g_1 = lac_2g_1
        locationMessage.ci_2g_1 = ci_2g_1
        locationMessage.lac_2g_2 = lac_2g_2
        locationMessage.ci_2g_2 = ci_2g_2
        locationMessage.lac_2g_3 = lac_2g_3
        locationMessage.ci_2g_3 = ci_2g_3
        try:
            charge = (int)(batteryStr)
            if  charge == 0:
                charge = 100
            locationMessage.batteryCharge = charge
        except:
            print ("charge error")
        locationMessage.date = gtm0
        locationMessage.latlngValid = latlngValid
        locationMessage.altitude = altitude
        locationMessage.latitude = latitude
        locationMessage.longitude = longitude
        if  latlngValid:
            locationMessage.speed = speed
        else:
            locationMessage.speed = 0
        locationMessage.azimuth = azimuth
        locationMessage.externalPowerVoltage = externalPowerVoltage
        locationMessage.networkSignal = networkSignal
        locationMessage.rs232DeviceValid = iopRs232DeviceValid
        locationMessage.input1 = input1
        locationMessage.input2 = input2
        locationMessage.input3 = input3
        locationMessage.input4 = input4
        locationMessage.input5 = input5
        locationMessage.input6 = input6
        locationMessage.output1 = output1
        locationMessage.output2 = output2
        locationMessage.output3 = output3
        locationMessage.output12V = output12V
        locationMessage.outputVout = outputVout
        locationMessage.rpm = rpm
        locationMessage.analogInput3 = analogInput3
        locationMessage.rlyMode = rlyMode
        locationMessage.smsLanguageType = smsLanguageType
        locationMessage.speakerStatus = speakerStatus
        locationMessage.rs232PowerOf5V = rs232PowerOf5V
        locationMessage.isSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigitalInput2Change
        locationMessage.isSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone
        locationMessage.jammerDetectionStatus = jammerDetectionStatus
        locationMessage.accdetSettingStatus = accdetSettingStatus
        return locationMessage




class Encoder:
    @staticmethod
    def encodeImei(imei):
        assert imei and len(imei) == 15
        return hexString2Bytes("0" + imei)

    @staticmethod
    def encode(imei,useSerialNo,serialNo,command,protocol,content,length):
        command.extend(short2Bytes(length))
        if useSerialNo:
            command.extend(short2Bytes(serialNo))
        else:
            command.extend([0x00,0x01])
        command.extend(Encoder.encodeImei(imei))
        command.extend(protocol);
        command.extend(content)
        return bytearray(command)

    @staticmethod
    def encode(imei,useSerialNo,serialNo,command,content,length):
        command.extend(short2Bytes(length))
        if useSerialNo:
            command.extend(short2Bytes(serialNo))
        else:
            command.extend([0x00,0x01])
        command.extend(Encoder.encodeImei(imei))
        command.extend(content)
        return bytearray(command)

    @staticmethod
    def encodeConfig(imei,useSerialNo,serialNo,command,protocol,content):
        length = 15 + len(content)
        command.extend(short2Bytes(length))
        if useSerialNo:
            command.extend(short2Bytes(serialNo))
        else:
            command.extend([0x00,0x01])
        command.extend(Encoder.encodeImei(imei))
        command.append(protocol)
        command.extend(content)
        return bytearray(command)
    @staticmethod
    def getCommonMsgReply(imei,needSerialNo,serialNo,command,messageEncryptType,aesKey):
        content = ""
        data = Encoder.encode(imei,needSerialNo,serialNo,command,bytearray(content,"ascii"),15)
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def getNormalMsgReply(imei, serialNo, command,content, messageEncryptType, aesKey):
        data = Encoder.encode(imei, True, serialNo, command, content, 15 + len(content))
        return Encoder.encrypt(data, messageEncryptType, aesKey)

    @staticmethod
    def getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,commad,messageEncryptType,aesKey):
        data = Encoder.encode(imei,needSerialNo,serialNo,commad,[sourceAlarmCode],16)
        return Encoder.encrypt(data,messageEncryptType,aesKey)


    @staticmethod
    def getConfigSettingMsg(imei,content,command,messageEncryptType,aesKey):
        data = Encoder.encodeConfig(imei,False,1,command,1,bytearray(content))
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def getBrocastMsg(imei,content,command,messageEncryptType,aesKey):
        data = Encoder.encodeConfig(imei,False,1,command,2,bytearray(content.encode("utf-16le")))
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def getForwardMsg(imei,phoneNumb,content,command,messageEncryptType,aesKey):
        numberBytes = bytearray(phoneNumb.encode("utf-16le"))
        for i in range(len(numberBytes) / 2 - 1,20):
            numberBytes.extend([0x00,0x00])
        numberBytes.extend(bytearray(content.encode("utf-16le")))
        data = Encoder.encodeConfig(imei,False,1,command,3,numberBytes)
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def getUSSDMsg(imei,content,command,messageEncryptType,aesKey):
        data = Encoder.encodeConfig(imei,False,1,command,5,bytearray(content))
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def getObdConfigSettingMsg(imei,content,command,protocolType,messageEncryptType,aesKey):
        length = 15 + len(content)
        data = Encoder.encode(imei, False, 1, command,  content,length)
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def get82ConfigSettingMsg(imei,content,command,protocolType,messageEncryptType,aesKey):
        length = 16 + len(content)
        data = Encoder.encode(imei,False,1,command,protocolType,length)
        return Encoder.encrypt(data,messageEncryptType,aesKey)

    @staticmethod
    def encrypt(data,messageEncryptType,aesKey):
        if messageEncryptType == MessageEncryptType.MD5:
            md5Data = Crypto.getMD5Path(data)
            data.extend(md5Data)
            return data
        elif messageEncryptType == MessageEncryptType.AES:
            if len(aesKey) <= 0:
                return
            head = data[0:15]
            realData = data[15:]
            if len(realData) <= 0:
                return data
            aesData = Crypto.aesEncrypt(realData,aesKey)
            if aesData:
                head.extend(aesData)
                return head
            else:
                return
        else:
            return data

class T880xEncoder:
    """
    Old Device Encoder like 8806,8803PRO
    """
    @staticmethod
    def getSignInMsgReply(imei,needSerialNo,serialNo):
        """
        Get sign in message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x01]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,MessageEncryptType.NONE,"")

    @staticmethod
    def getHeartbeatMsgReply(imei,needSerialNo,serialNo):
        """
        Get heartbeat message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x03]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,MessageEncryptType.NONE,"")

    @staticmethod
    def getLocationMsgReply(imei,needSerialNo,serialNo):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x02]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,MessageEncryptType.NONE,"")

    @staticmethod
    def getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x04]
        return Encoder.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,MessageEncryptType.NONE,"")

    @staticmethod
    def getConfigSettingMsg(imei,content):
        """
        Get config setting message
        :param imei:The imei,the type is string
        :param content:The config content.You can use sms config content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x81]
        return Encoder.getConfigSettingMsg(imei,content,command,MessageEncryptType.NONE,"")


    @staticmethod
    def getRS232MsgReply(imei,serialNo):
        """
            Get RS232 config setting msg byte [ ].
        :param imei: The imei,the type is string
        :param serialNo: The serial No.The type is int.
        :return:RS232 msg reply byte [ ].
        """
        command = [0x23,0x23,0x09]
        return Encoder.getCommonMsgReply(imei,True,serialNo,command,MessageEncryptType.NONE,"")

    @staticmethod
    def getBrocastSettingMsg(imei,content):
        """
        Get brocast setting message
        :param imei:The imei,the type is string
        :param content:The brocast content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x23,0x23,0x81]
        return Encoder.getBrocastMsg(imei,content,command,MessageEncryptType.NONE,"")
    @staticmethod
    def getRS232ConfigSettingMsg(imei,content,protocolType):
        command = [0x23,0x23,0x82]
        return Encoder.get82ConfigSettingMsg(imei,content,command,protocolType,MessageEncryptType.NONE,"")
    @staticmethod
    def getRS232ConfigSettingMsg(imei,content):
        command = [0x23,0x23,0x82]
        return Encoder.getConfigSettingMsg(imei,content,command,MessageEncryptType.NONE,"")

class T880xPlusEncoder:

    encryptType = 0
    aesKey = ""
    def __init__(self,messageEncryptType,aesKey):
        """
            Instantiates a new T880xPlusEncoder.
        :param messageEncryptType:The message encrypt type .Use the value of MessageEncryptType.
        :param aesKey:The aes key.If you do not use AES encryption, the value can be empty.The type is string
        :return:
        """
        self.encryptType = messageEncryptType
        self.aesKey = aesKey
    """
    New Device Encoder like 8806+,8806+r
    """

    def getSignInMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get sign in message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x01]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getHeartbeatMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get heartbeat message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x03]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationMsgReply(self,imei,needSerialNo,serialNo,protocolHeadType):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,protocolHeadType]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode,protocolHeadType):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,protocolHeadType]
        return Encoder.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,self.encryptType,self.aesKey)



    def getGpsDriverBehaviorMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get gps driver behavior message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x05]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getAccelerationDriverBehaviorMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get acceleration driver behavior message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x06]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getAccelerationAlarmMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get acceleration alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x07]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getBluetoothPeripheralMsgReply(self,imei,needSerialNo,serialNo,protocolHeadType):
        command = [0x25,0x25,protocolHeadType]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getRS232MsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x09]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getNetworkMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x11]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getWifiMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x15]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getRs485MsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x21]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getOneWireMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x23]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getObdMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x22]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getConfigSettingMsg(self,imei,content):
        """
        Get config setting message
        :param imei:The imei,the type is string
        :param content:The config content.You can use sms config content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x81]
        return Encoder.getConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)


    def getBrocastSettingMsg(self,imei,content):
        """
        Get brocast setting message
        :param imei:The imei,the type is string
        :param content:The brocast content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x81]
        return Encoder.getBrocastMsg(imei,content,command,self.encryptType,self.aesKey)


    def getForwardMsg(self,imei,phoneNumb,content):
        """
        Get fowward message
        :param imei:The imei,the type is string
        :param phoneNumb: The phone number need send to,the type is string
        :param content:The content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x81]
        return Encoder.getForwardMsg(imei,phoneNumb,content,command,self.encryptType,self.aesKey)


    def getUSSDMsg(self,imei,content):
        """
        Get USSD message
        :param imei: The imei,the type is string
        :param content: The content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x81]
        return Encoder.getUSSDMsg(imei,content,command,self.encryptType,self.aesKey)

    def getRS232ConfigSettingMsg(self,imei,content):
        command = [0x25,0x25,0x82]
        return Encoder.getConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)


class ObdDecoder:
    HEADER_LENGTH = 3
    SIGNUP = [0x26, 0x26, 0x01]

    DATA = [0x26, 0x26, 0x02]

    HEARTBEAT = [0x26, 0x26, 0x03]

    ALARM = [0x26, 0x26, 0x04]

    CONFIG = [0x26, 0x26, 0x81]
    GPS_DRIVER_BEHAVIOR = [0x26, 0x26, 0x05]
    ACCELERATION_DRIVER_BEHAVIOR = [0x26, 0x26, 0x06]
    ACCELERATION_ALARM = [0x26, 0x26, 0x07]
    OBD_DATA = [0x26, 0x26, 0x09]
    BLUETOOTH_DATA = [0x26, 0x26, 0x10]
    NETWORK_INFO_DATA = [0x26, 0x26, 0x11]
    SECOND_BLUETOOTH_DATA = [0x26, 0x26, 0x12]
    LOCATION_DATA_WITH_SENSOR = [0x26, 0x26, 0x16]
    LOCATION_ALARM_WITH_SENSOR = [0x26, 0x26, 0x18]
    latlngInvalidData = [0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]
    WIFI_DATA = [0x26, 0x26, 0x15]
    obdHead = [0x55,0xAA]
    encryptType = 0
    aesKey=""
    MASK_IGNITION = 0x4000
    MASK_POWER_CUT = 0x8000
    MASK_AC = 0x2000
    def __init__(self,messageEncryptType,aesKey):
        self.encryptType = messageEncryptType
        self.aesKey = aesKey

    def match (self,byteArray):
        return byteArray == self.SIGNUP or byteArray == self.DATA or byteArray == self.HEARTBEAT \
               or byteArray == self.ALARM or byteArray == self.CONFIG or \
               byteArray == self.GPS_DRIVER_BEHAVIOR or byteArray == self.ACCELERATION_DRIVER_BEHAVIOR or \
               byteArray == self.ACCELERATION_ALARM or byteArray == self.OBD_DATA\
               or byteArray == self.BLUETOOTH_DATA or byteArray == self.NETWORK_INFO_DATA or byteArray == self.SECOND_BLUETOOTH_DATA\
               or byteArray == self.LOCATION_DATA_WITH_SENSOR or byteArray == self.LOCATION_ALARM_WITH_SENSOR  or byteArray == self.WIFI_DATA

    decoderBuf = TopflytechByteBuf()

    def decode(self,buf):
        """
        Decode list.You can get all message at once.
        :param buf:The buf is from socket
        :return:The message list
        """
        buf = bytearray(buf)
        self.decoderBuf.pubBuf(buf)
        messages =[]
        if self.decoderBuf.getReadableBytes() < self.HEADER_LENGTH + 2:
            return messages
        bytes = [0,0,0]
        while self.decoderBuf.getReadableBytes() > 5:
            self.decoderBuf.markReaderIndex()
            bytes[0] = self.decoderBuf.getByte(0)
            bytes[1] = self.decoderBuf.getByte(1)
            bytes[2] = self.decoderBuf.getByte(2)
            if self.match(bytes):
                self.decoderBuf.skipBytes(self.HEADER_LENGTH)
                lengthBuf = self.decoderBuf.readBytes(2)
                packageLength = bytes2Short(lengthBuf,0)
                if self.encryptType == MessageEncryptType.MD5:
                    packageLength = packageLength + 8
                elif self.encryptType == MessageEncryptType.AES:
                    packageLength = self.getAesLength(packageLength)
                self.decoderBuf.resetReaderIndex()
                if packageLength <= 0:
                    self.decoderBuf.skipBytes(5)
                    break
                if packageLength > self.decoderBuf.getReadableBytes():
                    break
                data = self.decoderBuf.readBytes(packageLength)
                data = Crypto.decryptData(data,self.encryptType,self.aesKey)
                if data:
                    message = self.build(data)
                    if message:
                        messages.append(message)
            else:
                self.decoderBuf.skipBytes(1)
        return messages
    def build(self,byteArray):
        if byteArray[0] == 0x26 and byteArray[1] == 0x26:
            if byteArray[2] == 0x01:
                return self.parseSignInMessage(byteArray)
            elif byteArray[2] == 0x03:
                return self.parseHeartbeatMessage(byteArray)
            elif byteArray[2] == 0x02 or byteArray[2] == 0x04 or byteArray[2] == 0x16 or byteArray[2] == 0x18:
                return self.parseDataMessage(byteArray)
            elif byteArray[2] == 0x05:
                return self.parseGpsDriverBehaviorMessage(byteArray)
            elif byteArray[2] == 0x06:
                return self.parseAccelerationDriverBehaviorMessage(byteArray)
            elif byteArray[2] == 0x07:
                return self.parseAccelerationAlarmMessage(byteArray)
            elif byteArray[2] == 0x09:
                return self.parseObdMessage(byteArray)
            elif byteArray[2] == 0x10:
                return self.parseBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x11:
                return self.parseNetworkInfoMessage(byteArray)
            elif byteArray[2] == 0x12:
                return self.parseSecondBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x15:
                return self.parseWifiMessage(byteArray)
            elif byteArray[2] == 0x81:
                return self.parseInteractMessage(byteArray)
            else:
                return None
        return None

    def parseWifiMessage(self, bytes):
        wifiMessage = WifiMessage();
        serialNo = bytes2Short(bytes, 5)
        imei = decodeImei(bytes, 7)
        dateStr = "20" + byte2HexString(bytes[15:21], 0)
        gtm0 = GTM0(dateStr)
        selfMacByte = bytes[21:27]
        selfMac = byte2HexString(selfMacByte, 0)
        ap1MacByte = bytes[27:33]
        ap1Mac = byte2HexString(ap1MacByte, 0)
        rssiTemp = (int)(bytes[33])
        if rssiTemp < 0:
            rssiTemp += 256
        ap1RSSI = 0
        if rssiTemp == 255:
            ap1RSSI = -999
        else:
            ap1RSSI = rssiTemp - 256
        ap2MacByte = bytes[34:40]
        ap2Mac = byte2HexString(ap2MacByte, 0)
        rssiTemp = (int)(bytes[40])
        if rssiTemp < 0:
            rssiTemp += 256
        ap2RSSI = 0
        if rssiTemp == 255:
            ap2RSSI = -999
        else:
            ap2RSSI = rssiTemp - 256
        ap3MacByte = bytes[41:47]
        ap3Mac = byte2HexString(ap3MacByte, 0)
        rssiTemp = (int)(bytes[47])
        if rssiTemp < 0:
            rssiTemp += 256
        ap3RSSI = 0
        if rssiTemp == 255:
            ap3RSSI = -999
        else:
            ap3RSSI = rssiTemp - 256
        wifiMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        wifiMessage.imei = imei
        wifiMessage.orignBytes = bytes
        wifiMessage.date = gtm0
        wifiMessage.selfMac = selfMac
        wifiMessage.ap1Mac = ap1Mac
        wifiMessage.ap1RSSI = ap1RSSI
        wifiMessage.ap2Mac = ap2Mac
        wifiMessage.ap2RSSI = ap2RSSI
        wifiMessage.ap3Mac = ap3Mac
        wifiMessage.ap3RSSI = ap3RSSI
        return wifiMessage

    def parseSignInMessage(self,byteArray):
        if len(byteArray) == 25:
            serialNo = bytes2Short(byteArray,5)
            # isNeedResp = (serialNo & 0x8000) != 0x8000
            imei = decodeImei(byteArray,7)
            str = byte2HexString(byteArray,15)
            if len(str) == 20:
                software = "V{0}.{1}.{2}".format((byteArray[15] & 0xf0) >> 4, byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4)
                firmware = "V{0}.{1}.{2}".format(byteArray[16] & 0xf, (byteArray[17] & 0xf0) >> 4, byteArray[17] & 0xf)
                platform = str[6:10]
                hardware = "V{0}.{1}".format( (byteArray[20] & 0xf0) >> 4, byteArray[20] & 0xf)
                obdV1 = (int)(byteArray[21])
                obdV2 = (int)(byteArray[22])
                obdV3 = (int)(byteArray[23])
                if obdV1 < 0:
                    obdV1 += 256
                if obdV2 < 0:
                    obdV2 += 256
                if obdV3 < 0:
                    obdV3 += 256
                obdSoftware = "V{0}.{1}.{2}".format(obdV1, obdV2, obdV3)
                obdHardware = "V{0}.{1}".format((byteArray[24] & 0xf0) >> 4, byteArray[24] & 0xf)
                signInMessage = SignInMessage()
                signInMessage.firmware = firmware
                signInMessage.imei = imei
                signInMessage.serialNo = serialNo
                # signInMessage.isNeedResp = isNeedResp
                signInMessage.platform = platform
                signInMessage.software = software
                signInMessage.hardware = hardware
                signInMessage.orignBytes = byteArray
                signInMessage.obdHardware = obdHardware
                signInMessage.obdSoftware = obdSoftware
                return signInMessage
        elif len(byteArray) == 27:
            serialNo = bytes2Short(byteArray, 5)
            imei = decodeImei(byteArray, 7)
            software = "V{0}.{1}.{2}".format(byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4, byteArray[16] & 0xf)
            str = byte2HexString(byteArray, 17)
            platform = str[0:6]
            firmware = "V{0}.{1}.{2}.{3}".format(str[6:7], str[7:8], str[8:9], str[9:10])
            hardware = "V{0}.{1}".format(str[10:11], str[11:12])
            obdV1 = (int)(byteArray[21])
            obdV2 = (int)(byteArray[22])
            obdV3 = (int)(byteArray[23])
            if obdV1 < 0:
                obdV1 += 256
            if obdV2 < 0:
                obdV2 += 256
            if obdV3 < 0:
                obdV3 += 256
            obdSoftware = "V{0}.{1}.{2}".format(obdV1, obdV2, obdV3)
            obdHardware = "V{0}.{1}".format((byteArray[26] & 0xf0) >> 4, byteArray[26] & 0xf)
            signInMessage = SignInMessage()
            signInMessage.firmware = firmware
            signInMessage.imei = imei
            signInMessage.serialNo = serialNo
            signInMessage.platform = platform
            signInMessage.software = software
            signInMessage.hardware = hardware
            signInMessage.orignBytes = byteArray
            signInMessage.obdHardware = obdHardware
            signInMessage.obdSoftware = obdSoftware
            return signInMessage
        elif len(byteArray) >= 35:
            serialNo = bytes2Short(byteArray, 5)
            imei = decodeImei(byteArray, 7)
            model = bytes2Short(byteArray,15)
            str = byte2HexString(byteArray, 17)
            software = "V{0}.{1}.{2}.{3}".format(str[0:1], str[1:2],str[2:3],str[3:4])
            platform = str[4:10]
            firmware = "V{0}.{1}.{2}.{3}".format(str[10:11], str[11:12], str[12:13], str[13:14])
            hardware = "V{0}.{1}".format(str[14:15], str[15:16])
            obdV1 = (int)(byteArray[21])
            obdV2 = (int)(byteArray[22])
            obdV3 = (int)(byteArray[23])
            if obdV1 < 0:
                obdV1 += 256
            if obdV2 < 0:
                obdV2 += 256
            if obdV3 < 0:
                obdV3 += 256
            obdSoftware = "V{0}.{1}.{2}".format(obdV1, obdV2, obdV3)
            obdV1 = (int)(byteArray[24])
            obdV2 = (int)(byteArray[25])
            obdV3 = (int)(byteArray[26])
            if obdV1 < 0:
                obdV1 += 256
            if obdV2 < 0:
                obdV2 += 256
            if obdV3 < 0:
                obdV3 += 256
            obdBootVersion = "V{0}.{1}.{2}".format(obdV1, obdV2, obdV3)
            obdV1 = (int)(byteArray[27])
            obdV2 = (int)(byteArray[28])
            obdV3 = (int)(byteArray[29])
            if obdV1 < 0:
                obdV1 += 256
            if obdV2 < 0:
                obdV2 += 256
            if obdV3 < 0:
                obdV3 += 256
            obdDataVersion = "V{0}.{1}.{2}".format(obdV1, obdV2, obdV3)
            obdHardware = "V{0}.{1}".format((bytes[34] & 0xf0) >> 4, bytes[34] & 0xf)
            signInMessage = SignInMessage()
            signInMessage.firmware = firmware
            signInMessage.imei = imei
            signInMessage.serialNo = serialNo
            signInMessage.platform = platform
            signInMessage.software = software
            signInMessage.hardware = hardware
            signInMessage.orignBytes = byteArray
            signInMessage.obdHardware = obdHardware
            signInMessage.obdSoftware = obdSoftware
            signInMessage.obdDataVersion = obdDataVersion
            signInMessage.obdBootVersion = obdBootVersion
            return signInMessage
        return None

    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        # heartbeatMessage.isNeedResp = isNeedResp
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def getObdErrorCode(self,errorCode):
        if errorCode == 0:
            return "J1979"
        elif errorCode == 1:
            return "J1939"
        return ""

    def getObdErrorFlag(self,srcFlag):
        data = int(srcFlag,16)
        if data >= 0 and data < 4:
            return "P" + str(data)
        elif data[0] >= 4 and data < 8:
            return "C" + str(data - 4)
        elif data >= 8 and data < 12:
            return "B" + str(data - 8)
        else:
            return "U" + str(data - 12)

    def parseObdMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        obdData = ObdMessage()
        obdData.imei = imei
        obdData.orignBytes = byteArray
        obdData.serialNo = serialNo
        # obdData.isNeedResp = isNeedResp
        obdData.date = gtm0
        obdBytes = byteArray[21:len(byteArray)]

        head = [obdBytes[0],obdBytes[1]]
        if head == self.obdHead:
            obdBytes[2] = obdBytes[2] & 0x0F
            length = bytes2Short(obdBytes,2)
            if length > 0:
                try:
                    data = obdBytes[4:4+length]
                    if data[0] == 0x41 and data[1] == 0x04 and len(data) > 3:
                        obdData.messageType = ObdMessage.CLEAR_ERROR_CODE_MESSAGE
                        obdData.clearErrorCodeSuccess = data[2] == 0x01
                    elif data[0] == 0x41 and data[1] == 0x05 and len(data) > 2:
                        vinData = data[2:len(data) - 1]
                        dataValid = False
                        for i in range(len(vinData)):
                            item = vinData[i]
                            if (item & 0xFF) != 0xFF:
                                dataValid = True
                        if len(vinData) and dataValid:
                            obdData.messageType = ObdMessage.VIN_MESSAGE
                            obdData.vin = ''.join(chr(i) for i in vinData).encode().decode("UTF-8")
                    elif data[0] == 0x41 and (data[1] == 0x03 or data[1] == 0x0A):
                        errorCode = data[2]
                        errorDataByte = data[3:len(data) - 1]
                        errorDataStr = byte2HexString(errorDataByte,0);
                        if errorDataStr is not None:
                            errorDataSum = "";
                            i = 0
                            while i < len(errorDataStr):
                                errorDataItem = errorDataStr[i:i+6]
                                srcFlag = errorDataItem[0:1]
                                errorDataCode = self.getObdErrorFlag(srcFlag) + errorDataItem[1:4]
                                if errorDataSum.find(errorDataCode) == -1:
                                    if i != 0:
                                        errorDataSum += ";"
                                    errorDataSum += errorDataCode;
                                if i+6 >= len(errorDataStr) :
                                    break;
                                i+=6
                            obdData.messageType = ObdMessage.ERROR_CODE_MESSAGE
                            obdData.errorCode = self.getObdErrorCode(errorCode)
                            obdData.errorData = errorDataSum
                except:
                    print ("error")
        return obdData


    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        data = byteArray[15:len(byteArray)]
        samplingIntervalAccOn = bytes2Short(data, 0)
        samplingIntervalAccOff = bytes2Short(data, 2)
        angleCompensation = (int)(data[4])
        distanceCompensation = bytes2Short(data, 5)
        limit = bytes2Short(data,7)
        speedLimit = limit & 0x7FFF
        if (limit & 0x8000) != 0 :
            speedLimit = speedLimit * 1.609344
        networkSignal = limit & 0x7F
        isGpsWorking = (data[9] & 0x20) == 0x00
        isHistoryData = (data[9] & 0x80) != 0x00
        satelliteNumber = data[9] & 0x1F
        gSensorSensitivity = (data[10] & 0xF0) >> 4
        isManagerConfigured1 = (data[10] & 0x01) != 0x00
        isManagerConfigured2 = (data[10] & 0x02) != 0x00
        isManagerConfigured3 = (data[10] & 0x04) != 0x00
        isManagerConfigured4 = (data[10] & 0x08) != 0x00
        antitheftedStatus = 0
        if (data[11] & 0x10) != 0x00:
            antitheftedStatus = 1
        heartbeatInterval = data[12] & 0x00FF
        relayStatus = data[13] & 0x3F
        rlyMode =  data[13] & 0xCF
        ignitionSource = data[13] & 0xf
        smsLanguageType = data[13] & 0xF
        isRelayWaiting = ((data[13] & 0xC0) != 0x00) and ((data[13] & 0x80) == 0x00)
        dragThreshold = bytes2Short(data, 14)
        iop = bytes2Short(data, 16)
        iopIgnition = (iop & self.MASK_IGNITION) == self.MASK_IGNITION
        iopPowerCutOff = (iop & self.MASK_POWER_CUT) == self.MASK_POWER_CUT
        iopACOn = (iop & self.MASK_AC) == self.MASK_AC
        hasThirdPartyObd = 0
        if (iop & 0x10) == 0x10:
            hasThirdPartyObd = 1
        exPowerConsumpStatus = 0
        if (iop & 0x03) == 0x01:
            exPowerConsumpStatus = 2
        elif (iop & 0x03) == 0x02:
            exPowerConsumpStatus = 1
        else:
            exPowerConsumpStatus = 0

        output1 = (iop & 0x0400) == 0x0400
        input1 = iopIgnition
        input2 = iopACOn
        speakerStatus = 0
        if (iop & 0x40) ==  0x40:
            speakerStatus = 1
        rs232PowerOf5V = 0
        if (iop & 0x20) ==  0x20:
            rs232PowerOf5V = 1
        originalAlarmCode = (int) (data[18])
        isAlarmData = command[2] == 0x04 or command[2] == 0x18
        isSendSmsAlarmToManagerPhone = (data[19] & 0x20) == 0x20
        isSendSmsAlarmWhenDigitalInput2Change = (data[19] & 0x10) == 0x10
        jammerDetectionStatus = (data[19] & 0xC)
        mileageSource = 1
        if (data[19] & 0x02) == 0x02:
            mileageSource = 0
        mileage = bytes2Integer(data, 20)
        batteryBytes = [data[24]]
        batteryStr = byte2HexString(batteryBytes, 0)
        dateStr = "20" + byte2HexString(data[25:31],0)
        gtm0 = GTM0(dateStr)
        latlngValid = (data[9] & 0x40) != 0x00
        latlngData = data[31:37]
        if latlngData == self.latlngInvalidData:
            latlngValid = False
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(data,31)
            latitude = bytes2Float(data,39)
            longitude = bytes2Float(data,35)
            azimuth = bytes2Short(data,45)
            speedStr = byte2HexString(data[49:51],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (data[31] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(data,31)
            mnc_2g = bytes2Short(data,33)
            lac_2g_1 = bytes2Short(data,35)
            ci_2g_1 = bytes2Short(data,37)
            lac_2g_2 = bytes2Short(data,39)
            ci_2g_2 = bytes2Short(data,41)
            lac_2g_3 = bytes2Short(data,43)
            ci_2g_3 = bytes2Short(data,45)
        if is_4g_lbs:
            mcc_4g = bytes2Short(data,31) & 0x7FFF
            mnc_4g = bytes2Short(data,33)
            ci_4g = bytes2Integer(data, 35)
            earfcn_4g_1 = bytes2Short(data, 39)
            pcid_4g_1 = bytes2Short(data, 41)
            earfcn_4g_2 = bytes2Short(data, 43)
            pcid_4g_2 = bytes2Short(data,45)
        externalPowerVoltageStr = byte2HexString(data[47:49], 0)
        externalPowerVoltage = (float(externalPowerVoltageStr) ) / 100
        accumulatingFuelConsumption = bytes2Integer(data, 51)
        if accumulatingFuelConsumption == 4294967295:
            accumulatingFuelConsumption = -999
        instantFuelConsumption =  bytes2Integer(data, 55)
        if instantFuelConsumption == 4294967295:
            instantFuelConsumption = -999
        rpm = bytes2Short(data, 59)
        if rpm == 65535:
            rpm = -999
        airInput = (int)(data[61])
        if airInput < 0:
            airInput += 256
        if airInput == 255:
            airInput = -999
        airPressure = (int)(data[62])
        if airPressure < 0:
            airPressure += 256
        if airPressure == 255:
            airPressure = -999
        coolingFluidTemp = (int)(data[63]) < 0
        if coolingFluidTemp < 0:
            coolingFluidTemp += 256
        if coolingFluidTemp == 255:
            coolingFluidTemp = -999
        else:
            coolingFluidTemp = coolingFluidTemp - 40
        airInflowTemp = (int)(data[64])
        if airInflowTemp < 0:
            airInflowTemp += 256
        if airInflowTemp == 255:
            airInflowTemp = -999
        else:
            airInflowTemp = airInflowTemp - 40
        engineLoad = (int)(data[65])
        if engineLoad < 0:
            engineLoad += 256
        if engineLoad == 255:
            engineLoad = -999
        throttlePosition = (int)(data[66])
        if throttlePosition < 0:
            throttlePosition += 256
        if throttlePosition == 255:
            throttlePosition = -999
        remainFuelRate = data[67] & 0x7f
        if data[67] == 255:
            remainFuelRate = -999
        remainFuelUnit = 0
        if (data[67] & 0x80) == 0x80:
            remainFuelUnit = 1
        locationMessage = LocationInfoMessage()
        if isAlarmData:
            locationMessage = LocationAlarmMessage()
        protocolHead = byteArray[2]
        locationMessage.protocolHeadType = protocolHead
        if len(data) >= 80 and (protocolHead == 0x16 or protocolHead == 0x18):
            axisX = bytes2SingleShort(data[68:70],0)
            axisY = bytes2SingleShort(data[70:72],0)
            axisZ = bytes2SingleShort(data[72:74],0)
            gyroscopeAxisX = bytes2SingleShort(data[74:76],0)
            gyroscopeAxisY = bytes2SingleShort(data[76:78],0)
            gyroscopeAxisZ = bytes2SingleShort(data[78:80],0)
            locationMessage.axisX = axisX
            locationMessage.axisY = axisY
            locationMessage.axisZ = axisZ
            locationMessage.gyroscopeAxisX = gyroscopeAxisX
            locationMessage.gyroscopeAxisY = gyroscopeAxisY
            locationMessage.gyroscopeAxisZ = gyroscopeAxisZ
        locationMessage.orignBytes = byteArray
        locationMessage.serialNo = serialNo
        # locationMessage.isNeedResp = isNeedResp
        locationMessage.imei = imei
        locationMessage.samplingIntervalAccOn = samplingIntervalAccOn
        locationMessage.samplingIntervalAccOff = samplingIntervalAccOff
        locationMessage.angleCompensation = angleCompensation
        locationMessage.distanceCompensation = distanceCompensation
        locationMessage.overspeedLimit = speedLimit
        locationMessage.gpsWorking = isGpsWorking
        locationMessage.isHistoryData = isHistoryData
        locationMessage.satelliteNumber = satelliteNumber
        locationMessage.gSensorSensitivity = gSensorSensitivity
        locationMessage.isManagerConfigured1 = isManagerConfigured1
        locationMessage.isManagerConfigured2 = isManagerConfigured2
        locationMessage.isManagerConfigured3 = isManagerConfigured3
        locationMessage.isManagerConfigured4 = isManagerConfigured4
        locationMessage.antitheftedStatus = antitheftedStatus
        locationMessage.heartbeatInterval = heartbeatInterval
        locationMessage.relayStatus =relayStatus
        locationMessage.isRelayWaiting = isRelayWaiting
        locationMessage.dragThreshold = dragThreshold
        locationMessage.IOP = iop
        locationMessage.input1 = input1
        locationMessage.input2 = input2
        locationMessage.output1 = output1
        locationMessage.iopIgnition = iopIgnition
        locationMessage.iopPowerCutOff = iopPowerCutOff
        locationMessage.iopACOn = iopACOn
        locationMessage.originalAlarmCode = originalAlarmCode
        locationMessage.mileage = mileage
        try:
            charge = (int)(batteryStr)
            if  charge == 0:
                charge = 100
            locationMessage.batteryCharge = charge
        except:
            print ("charge error")
        locationMessage.date = gtm0
        locationMessage.is_4g_lbs = is_4g_lbs
        locationMessage.is_2g_lbs = is_2g_lbs
        locationMessage.mcc_4g = mcc_4g
        locationMessage.mnc_4g = mnc_4g
        locationMessage.ci_4g = ci_4g
        locationMessage.earfcn_4g_1 = earfcn_4g_1
        locationMessage.pcid_4g_1 = pcid_4g_1
        locationMessage.earfcn_4g_2 = earfcn_4g_2
        locationMessage.pcid_4g_2 = pcid_4g_2
        locationMessage.mcc_2g = mcc_2g
        locationMessage.mnc_2g = mnc_2g
        locationMessage.lac_2g_1 = lac_2g_1
        locationMessage.ci_2g_1 = ci_2g_1
        locationMessage.lac_2g_2 = lac_2g_2
        locationMessage.ci_2g_2 = ci_2g_2
        locationMessage.lac_2g_3 = lac_2g_3
        locationMessage.ci_2g_3 = ci_2g_3
        locationMessage.latlngValid = latlngValid
        locationMessage.altitude = altitude
        locationMessage.latitude = latitude
        locationMessage.longitude = longitude
        if  latlngValid:
            locationMessage.speed = speed
        else:
            locationMessage.speed = 0
        locationMessage.azimuth = azimuth
        locationMessage.externalPowerVoltage = externalPowerVoltage
        locationMessage.networkSignal = networkSignal
        locationMessage.accumulatingFuelConsumption = accumulatingFuelConsumption
        locationMessage.instantFuelConsumption = instantFuelConsumption
        locationMessage.rpm = rpm
        locationMessage.airInflowTemp = airInflowTemp
        locationMessage.airInput = airInput
        locationMessage.airPressure = airPressure
        locationMessage.coolingFluidTemp  = coolingFluidTemp
        locationMessage.engineLoad = engineLoad
        locationMessage.throttlePosition = throttlePosition
        locationMessage.remainFuelRate  =remainFuelRate
        locationMessage.rlyMode = rlyMode
        locationMessage.smsLanguageType = smsLanguageType
        locationMessage.speakerStatus = speakerStatus
        locationMessage.rs232PowerOf5V = rs232PowerOf5V
        locationMessage.isSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigitalInput2Change
        locationMessage.isSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone
        locationMessage.jammerDetectionStatus = jammerDetectionStatus
        locationMessage.ignitionSource = ignitionSource
        locationMessage.exPowerConsumpStatus = exPowerConsumpStatus
        locationMessage.hasThirdPartyObd = hasThirdPartyObd
        locationMessage.mileageSource = mileageSource
        locationMessage.remainFuelUnit = remainFuelUnit
        return locationMessage

    def parseGpsDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        gpsDriverBehaviorMessage = GpsDriverBehaviorMessage()
        behaviorType = (int)(byteArray[15])
        gpsDriverBehaviorMessage.imei = imei
        gpsDriverBehaviorMessage.serialNo = serialNo
        # gpsDriverBehaviorMessage.isNeedResp = isNeedResp
        gpsDriverBehaviorMessage.orignBytes = byteArray
        gpsDriverBehaviorMessage.behaviorType = behaviorType
        dateStr = "20" + byte2HexString(byteArray[16:22],0)
        gpsDriverBehaviorMessage.startDate = GTM0(dateStr)
        gpsDriverBehaviorMessage.startAltitude = bytes2Float(byteArray,22)
        gpsDriverBehaviorMessage.startLongitude = bytes2Float(byteArray,26)
        gpsDriverBehaviorMessage.startLatitude = bytes2Float(byteArray,30)
        speedStr = byte2HexString(byteArray[34:36],0)
        gpsDriverBehaviorMessage.startSpeed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        gpsDriverBehaviorMessage.startAzimuth = bytes2Short(byteArray,36)
        rpm = bytes2Short(byteArray, 38);
        if rpm == 65535:
            rpm = -1
        gpsDriverBehaviorMessage.startRpm = rpm

        dateStr = "20" + byte2HexString(byteArray[40:46],0)
        gpsDriverBehaviorMessage.endDate = GTM0(dateStr)
        gpsDriverBehaviorMessage.endAltitude = bytes2Float(byteArray,46)
        gpsDriverBehaviorMessage.endLongitude = bytes2Float(byteArray,50)
        gpsDriverBehaviorMessage.endLatitude = bytes2Float(byteArray,52)
        speedStr = byte2HexString(byteArray[58:60],0)
        gpsDriverBehaviorMessage.endSpeed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        gpsDriverBehaviorMessage.endAzimuth = bytes2Short(byteArray,60)
        rpm = bytes2Short(byteArray, 62)
        if rpm == 65535:
            rpm = -1
        gpsDriverBehaviorMessage.endRpm = rpm
        return gpsDriverBehaviorMessage

    def getAccidentAccelerationData(self,byteArray,imei,curParseIndex):
        acceleration = AccelerationData()
        acceleration.imei = imei
        dateStr = "20" + byte2HexString(byteArray[curParseIndex:curParseIndex + 6],0)
        acceleration.date = GTM0(dateStr)
        acceleration.gpsWorking = (byteArray[curParseIndex + 6] & 0x20) == 0x00
        acceleration.isHistoryData = (byteArray[curParseIndex + 6] & 0x80) != 0x00
        acceleration.satelliteNumber = byteArray[curParseIndex + 6] & 0x1F
        acceleration.latlngValid = (byteArray[curParseIndex + 6] & 0x40) != 0x00
        axisXDirect = -1
        if (byteArray[curParseIndex + 7] & 0x80) == 0x80:
            axisXDirect = 1
        acceleration.axisX = ((byteArray[curParseIndex + 7] & 0x7F & 0xff) + (((byteArray[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[curParseIndex + 8] & 0x08) == 0x08:
            axisYDirect = 1
        acceleration.axisY = (((((byteArray[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((byteArray[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (byteArray[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect
        axisZDirect = -1
        if (byteArray[curParseIndex + 10] & 0x80) == 0x80:
            axisZDirect = 1
        acceleration.axisZ = ((byteArray[curParseIndex + 10] & 0x7F & 0xff) + (((byteArray[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  acceleration.latlngValid:
            acceleration.altitude = bytes2Float(byteArray,curParseIndex + 12)
            acceleration.longitude = bytes2Float(byteArray,curParseIndex + 16)
            acceleration.latitude = bytes2Float(byteArray,curParseIndex + 20)
            speedStr = byte2HexString(byteArray[curParseIndex + 24:curParseIndex + 26],0)
            acceleration.speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
            acceleration.azimuth = bytes2Short(byteArray,curParseIndex + 26)
        else:
            if (byteArray[curParseIndex + 12] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,curParseIndex + 12)
            mnc_2g = bytes2Short(byteArray,curParseIndex + 14)
            lac_2g_1 = bytes2Short(byteArray,curParseIndex + 16)
            ci_2g_1 = bytes2Short(byteArray,curParseIndex + 18)
            lac_2g_2 = bytes2Short(byteArray,curParseIndex + 20)
            ci_2g_2 = bytes2Short(byteArray,curParseIndex + 22)
            lac_2g_3 = bytes2Short(byteArray,curParseIndex + 24)
            ci_2g_3 = bytes2Short(byteArray,curParseIndex + 26)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,curParseIndex + 12) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,curParseIndex + 14)
            ci_4g = bytes2Integer(byteArray, curParseIndex + 16)
            earfcn_4g_1 = bytes2Short(byteArray, curParseIndex + 20)
            pcid_4g_1 = bytes2Short(byteArray, curParseIndex + 22)
            earfcn_4g_2 = bytes2Short(byteArray, curParseIndex + 24)
            pcid_4g_2 = bytes2Short(byteArray,curParseIndex + 26)

        acceleration.is_4g_lbs = is_4g_lbs
        acceleration.is_2g_lbs = is_2g_lbs
        acceleration.mcc_4g = mcc_4g
        acceleration.mnc_4g = mnc_4g
        acceleration.ci_4g = ci_4g
        acceleration.earfcn_4g_1 = earfcn_4g_1
        acceleration.pcid_4g_1 = pcid_4g_1
        acceleration.earfcn_4g_2 = earfcn_4g_2
        acceleration.pcid_4g_2 = pcid_4g_2
        acceleration.mcc_2g = mcc_2g
        acceleration.mnc_2g = mnc_2g
        acceleration.lac_2g_1 = lac_2g_1
        acceleration.ci_2g_1 = ci_2g_1
        acceleration.lac_2g_2 = lac_2g_2
        acceleration.ci_2g_2 = ci_2g_2
        acceleration.lac_2g_3 = lac_2g_3
        acceleration.ci_2g_3 = ci_2g_3
        rpm = bytes2Short(byteArray, curParseIndex + 28);
        if rpm == 65535:
            rpm = -1
        acceleration.rpm = rpm
        return acceleration

    def getAccelerationData(self,byteArray,imei,curParseIndex):
        acceleration = AccelerationData()
        acceleration.imei = imei
        dateStr = "20" + byte2HexString(byteArray[curParseIndex:curParseIndex + 6],0)
        acceleration.date = GTM0(dateStr)
        acceleration.gpsWorking = (byteArray[curParseIndex + 6] & 0x20) == 0x00
        acceleration.isHistoryData = (byteArray[curParseIndex + 6] & 0x80) != 0x00
        acceleration.satelliteNumber = byteArray[curParseIndex + 6] & 0x1F
        acceleration.latlngValid = (byteArray[curParseIndex + 6] & 0x40) != 0x00
        axisXDirect = -1
        if (byteArray[curParseIndex + 7] & 0x80) == 0x80:
            axisXDirect = 1
        acceleration.axisX = ((byteArray[curParseIndex + 7] & 0x7F & 0xff) + (((byteArray[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[curParseIndex + 8] & 0x08) == 0x08:
            axisYDirect = 1
        acceleration.axisY = (((((byteArray[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((byteArray[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (byteArray[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect
        axisZDirect = -1
        if (byteArray[curParseIndex + 10] & 0x80) == 0x80:
            axisZDirect = 1
        acceleration.axisZ = ((byteArray[curParseIndex + 10] & 0x7F & 0xff) + (((byteArray[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect
        acceleration.altitude = bytes2Float(byteArray,curParseIndex + 12)
        acceleration.longitude = bytes2Float(byteArray,curParseIndex + 16)
        acceleration.latitude = bytes2Float(byteArray,curParseIndex + 20)
        speedStr = byte2HexString(byteArray[curParseIndex + 24:curParseIndex + 26],0)
        acceleration.speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        acceleration.azimuth = bytes2Short(byteArray,curParseIndex + 26)
        rpm = bytes2Short(byteArray, curParseIndex + 28);
        if rpm == 65535:
            rpm = -1
        acceleration.rpm = rpm
        return acceleration

    def parseAccelerationAlarmMessage(self,byteArray):
        length = bytes2Short(byteArray,3)
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        accidentAccelerationMessage = AccidentAccelerationMessage()
        accidentAccelerationMessage.serialNo = serialNo
        # accidentAccelerationMessage.isNeedResp = isNeedResp
        accidentAccelerationMessage.imei = imei
        accidentAccelerationMessage.orignBytes = byteArray
        dataLength = length - 16
        beginIndex = 16
        accidentAccelerationList = []
        while beginIndex < dataLength:
            curParseIndex = beginIndex
            beginIndex = beginIndex + 30
            accelerationData = self.getAccidentAccelerationData(byteArray,imei,curParseIndex)
            accidentAccelerationList.append(accelerationData)
        accidentAccelerationMessage.accelerationList = accidentAccelerationList
        return accidentAccelerationMessage

    def parseAccelerationDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        behaviorType = (int)(byteArray[15])
        accelerationDriverBehaviorMessage = AccelerationDriverBehaviorMessage()
        accelerationDriverBehaviorMessage.imei = imei
        accelerationDriverBehaviorMessage.serialNo = serialNo
        # accelerationDriverBehaviorMessage.isNeedResp = isNeedResp
        accelerationDriverBehaviorMessage.orignBytes = byteArray
        accelerationDriverBehaviorMessage.behaviorType = behaviorType
        beginIndex = 16
        accelerationData = self.getAccelerationData(byteArray,imei,beginIndex)
        accelerationDriverBehaviorMessage.accelerationData = accelerationData
        return accelerationDriverBehaviorMessage


    def parseSecondBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        isHistoryData = (byteArray[22] & 0x40) == 0x40
        latlngValid = (byteArray[22] & 0x80) == 0x80
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        strSp = byte2HexString(byteArray[35:37], 0);
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray, 23)
            longitude = bytes2Float(byteArray, 27)
            latitude = bytes2Float(byteArray, 31)
            azimuth = bytes2Short(byteArray, 37)
        else:
            if (byteArray[23] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,23)
            mnc_2g = bytes2Short(byteArray,25)
            lac_2g_1 = bytes2Short(byteArray,27)
            ci_2g_1 = bytes2Short(byteArray,29)
            lac_2g_2 = bytes2Short(byteArray,31)
            ci_2g_2 = bytes2Short(byteArray,33)
            lac_2g_3 = bytes2Short(byteArray,35)
            ci_2g_3 = bytes2Short(byteArray,37)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,23) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,25)
            ci_4g = bytes2Integer(byteArray, 27)
            earfcn_4g_1 = bytes2Short(byteArray, 31)
            pcid_4g_1 = bytes2Short(byteArray, 33)
            earfcn_4g_2 = bytes2Short(byteArray, 35)
            pcid_4g_2 = bytes2Short(byteArray,37)
        if strSp.find("f") == -1:
                speed = -1
        else:
            speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.isHistoryData = isHistoryData
        bluetoothPeripheralDataMessage.serialNo = serialNo
        bluetoothPeripheralDataMessage.latlngValid = latlngValid
        bluetoothPeripheralDataMessage.altitude = altitude
        bluetoothPeripheralDataMessage.latitude = latitude
        bluetoothPeripheralDataMessage.longitude = longitude
        bluetoothPeripheralDataMessage.azimuth = azimuth
        bluetoothPeripheralDataMessage.isHadLocationInfo = True
        bluetoothPeripheralDataMessage.protocolHeadType = 0x12
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bluetoothPeripheralDataMessage.is_4g_lbs = is_4g_lbs
        bluetoothPeripheralDataMessage.is_2g_lbs = is_2g_lbs
        bluetoothPeripheralDataMessage.mcc_4g = mcc_4g
        bluetoothPeripheralDataMessage.mnc_4g = mnc_4g
        bluetoothPeripheralDataMessage.ci_4g = ci_4g
        bluetoothPeripheralDataMessage.earfcn_4g_1 = earfcn_4g_1
        bluetoothPeripheralDataMessage.pcid_4g_1 = pcid_4g_1
        bluetoothPeripheralDataMessage.earfcn_4g_2 = earfcn_4g_2
        bluetoothPeripheralDataMessage.pcid_4g_2 = pcid_4g_2
        bluetoothPeripheralDataMessage.mcc_2g = mcc_2g
        bluetoothPeripheralDataMessage.mnc_2g = mnc_2g
        bluetoothPeripheralDataMessage.lac_2g_1 = lac_2g_1
        bluetoothPeripheralDataMessage.ci_2g_1 = ci_2g_1
        bluetoothPeripheralDataMessage.lac_2g_2 = lac_2g_2
        bluetoothPeripheralDataMessage.ci_2g_2 = ci_2g_2
        bluetoothPeripheralDataMessage.lac_2g_3 = lac_2g_3
        bluetoothPeripheralDataMessage.ci_2g_3 = ci_2g_3
        bleData = byteArray[39:len(byteArray)]
        if len(bleData) <= 0:
            return bluetoothPeripheralDataMessage
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001;
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x07:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL
            i = 2
            while i < len(bleData):
                bleFuelData = BleFuelData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                valueTemp = bytes2Short(bleData,i+7)
                value=0
                if valueTemp == 65535:
                    value = -999
                else:
                    value = valueTemp
                temperatureTemp = bytes2Short(bleData,i+9)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                status = bleData[i+13]
                if status == 255:
                    status = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 14]) < 0:
                    rssiTemp =  (int) (bleData[i + 14]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 14])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.alarm = status
                bleFuelData.value = value
                bleFuelData.voltage = formatNumb(voltage)
                bleFuelData.temp = formatNumb(temperature)
                bleDataList.append(bleFuelData)
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.protocolHeadType = byteArray[2]
        bluetoothPeripheralDataMessage.isHistoryData = (byteArray[15] & 0x80) != 0x00
        bluetoothPeripheralDataMessage.serialNo = serialNo
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bleData = byteArray[22:len(byteArray)]
        if len(bleData) <= 0:
            return bluetoothPeripheralDataMessage
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0);
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except :
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            strSp = byte2HexString(bleData[23:25], 0);
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
                if strSp.find("f") == -1:
                    speed = -1;
                else:
                    speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)

            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.satelliteCount  =satelliteNumber
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except :
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            strSp = byte2HexString(bleData[23:25], 0);
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
                if strSp.find("f") == -1:
                    speed = -1;
                else:
                    speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.satelliteCount  =satelliteNumber
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x07:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL
            i = 2
            while i < len(bleData):
                bleFuelData = BleFuelData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                valueTemp = bytes2Short(bleData,i+7)
                value=0
                if valueTemp == 65535:
                    value = -999
                else:
                    value = valueTemp
                temperatureTemp = bytes2Short(bleData,i+9)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                status = bleData[i+13]
                if status == 255:
                    status = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 14]) < 0:
                    rssiTemp =  (int) (bleData[i + 14]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 14])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.alarm = status
                bleFuelData.value = value
                bleFuelData.voltage = formatNumb(voltage)
                bleFuelData.temp = formatNumb(temperature)
                bleDataList.append(bleFuelData)
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).encode().decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).encode().decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).encode().decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).encode().decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).encode().decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            # configMessage.isNeedResp = isNeedResp
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            # forwardMessage.isNeedResp = isNeedResp
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            # ussdMessage.isNeedResp = isNeedResp
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None

class T880xdEncoder:

    encryptType = 0
    aesKey = ""
    def __init__(self,messageEncryptType,aesKey):
        self.encryptType = messageEncryptType
        self.aesKey = aesKey
    """
    New Device Encoder like 8806+,8806+r
    """

    def getSignInMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get sign in message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x01]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getHeartbeatMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get heartbeat message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x03]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationMsgReply(self,imei,needSerialNo,serialNo,protocolHead):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,protocolHead]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode,protocolHead):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,protocolHead]
        return Encoder.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,self.encryptType,self.aesKey)



    def getGpsDriverBehaviorMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get gps driver behavior message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x05]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getAccelerationDriverBehaviorMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get acceleration driver behavior message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x06]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getAccelerationAlarmMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get acceleration alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x07]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getBluetoothPeripheralMsgReply(self,imei,needSerialNo,serialNo,protocolHeadType):
        command = [0x26,0x26,protocolHeadType]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getObdMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x26,0x26,0x09]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getNetworkMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x26,0x26,0x11]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getConfigSettingMsg(self,imei,content):
        """
        Get config setting message
        :param imei:The imei,the type is string
        :param content:The config content.You can use sms config content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x81]
        return Encoder.getConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)

    def getWifiMsgReply(self, imei, needSerialNo, serialNo):
        command = [0x26, 0x26, 0x15]
        return Encoder.getCommonMsgReply(imei, needSerialNo, serialNo, command, self.encryptType, self.aesKey)

    def getBrocastSettingMsg(self,imei,content):
        """
        Get brocast setting message
        :param imei:The imei,the type is string
        :param content:The brocast content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x81]
        return Encoder.getBrocastMsg(imei,content,command,self.encryptType,self.aesKey)


    def getForwardMsg(self,imei,phoneNumb,content):
        """
        Get fowward message
        :param imei:The imei,the type is string
        :param phoneNumb: The phone number need send to,the type is string
        :param content:The content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x81]
        return Encoder.getForwardMsg(imei,phoneNumb,content,command,self.encryptType,self.aesKey)


    def getUSSDMsg(self,imei,content):
        """
        Get USSD message
        :param imei: The imei,the type is string
        :param content: The content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x81]
        return Encoder.getUSSDMsg(imei,content,command,self.encryptType,self.aesKey)

    def getObdConfigSettingMsg(self,imei,content):
        command = [0x26,0x26,0x82]
        return Encoder.getObdConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)

    def getClearObdErrorCodeMsg(self,imei):
        content = [0x55,0xAA,0x00,0x03,0x01,0x04,0x06,0x0D,0x0A]
        command = [0x26, 0x26, 0x82]
        return Encoder.getObdConfigSettingMsg(imei, content, command, self.encryptType, self.aesKey)

class PersonalAssetMsgDecoder:
    HEADER_LENGTH = 3
    SIGNUP = [0x27, 0x27, 0x01]

    DATA = [0x27, 0x27, 0x02]

    HEARTBEAT = [0x27, 0x27, 0x03]

    ALARM = [0x27, 0x27, 0x04]

    NETWORK_INFO_DATA = [0x27, 0x27, 0x05]

    BLUETOOTH_DATA = [0x27, 0x27, 0x10]

    WIFI_DATA = [0x27,0x27,0x15]
    
    LOCK_DATA = [0x27,0x27,0x17]
    GEO_DATA = [0x27, 0x27, 0x20]
    BLUETOOTH_SECOND_DATA = [0x27, 0x27, 0x12]
    WIFI_WITH_DEVICE_INFO_DATA = [0x27, 0x27, 0x24]
    WIFI_ALARM_WITH_DEVICE_INFO_DATA = [0x27, 0x27, 0x25]
    CONFIG = [0x27, 0x27, 0x81]
    encryptType = 0
    esKey = ""

    def __init__(self,messageEncryptType,aesKey):
        """
        :param messageEncryptType:The message encrypt type .Use the value of MessageEncryptType.
        :param aesKey:The aes key.If you do not use AES encryption, the value can be empty.The type is string
        :return:
        """
        self.encryptType = messageEncryptType
        self.aesKey = aesKey

    def match (self,byteArray):
        return byteArray == self.SIGNUP or byteArray == self.DATA or byteArray == self.HEARTBEAT \
               or byteArray == self.ALARM or byteArray == self.NETWORK_INFO_DATA or byteArray == self.BLUETOOTH_DATA \
               or byteArray == self.CONFIG or byteArray == self.WIFI_DATA or byteArray == self.LOCK_DATA  \
               or byteArray == self.GEO_DATA or byteArray == self.BLUETOOTH_SECOND_DATA or byteArray == self.WIFI_WITH_DEVICE_INFO_DATA  \
               or byteArray == self.WIFI_ALARM_WITH_DEVICE_INFO_DATA


    decoderBuf = TopflytechByteBuf()

    def decode(self,buf):
        """
        Decode list.You can get all message at once.
        :param buf:The buf is from socket
        :return:The message list
        """
        buf = bytearray(buf)
        self.decoderBuf.pubBuf(buf)
        messages =[]
        if self.decoderBuf.getReadableBytes() < self.HEADER_LENGTH + 2:
            return messages
        bytes = [0,0,0]
        while self.decoderBuf.getReadableBytes() > 5:
            self.decoderBuf.markReaderIndex()
            bytes[0] = self.decoderBuf.getByte(0)
            bytes[1] = self.decoderBuf.getByte(1)
            bytes[2] = self.decoderBuf.getByte(2)
            if self.match(bytes):
                self.decoderBuf.skipBytes(self.HEADER_LENGTH)
                lengthBuf = self.decoderBuf.readBytes(2)
                packageLength = bytes2Short(lengthBuf,0)
                if self.encryptType == MessageEncryptType.MD5:
                    packageLength = packageLength + 8
                elif self.encryptType == MessageEncryptType.AES:
                    packageLength = self.getAesLength(packageLength)
                self.decoderBuf.resetReaderIndex()
                if packageLength <= 0:
                    self.decoderBuf.skipBytes(5)
                    break
                if packageLength > self.decoderBuf.getReadableBytes():
                    break
                data = self.decoderBuf.readBytes(packageLength)
                data = Crypto.decryptData(data,self.encryptType,self.aesKey)
                if data:
                    message = self.build(data)
                    if message:
                        messages.append(message)
            else:
                self.decoderBuf.skipBytes(1)
        return messages
    def build(self,byteArray):
        if byteArray[0] == 0x27 and byteArray[1] == 0x27:
            if byteArray[2] == 0x01:
                return self.parseSignInMessage(byteArray)
            elif byteArray[2] == 0x03:
                return self.parseHeartbeatMessage(byteArray)
            elif byteArray[2] == 0x02 or byteArray[2] == 0x04:
                return self.parseDataMessage(byteArray)
            elif byteArray[2] == 0x05:
                return self.parseNetworkInfoMessage(byteArray)
            elif byteArray[2] == 0x10:
                return self.parseBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x81:
                return self.parseInteractMessage(byteArray)
            elif byteArray[2] == 0x15:
                return self.parseWifiMessage(byteArray)
            elif byteArray[2] == 0x17:
                return self.parseLockMessage(byteArray)
            elif byteArray[2] == 0x12:
                return self.parseSecondBluetoothDataMessage(byteArray)
            elif byteArray[2] == 0x20:
                return self.parseInnerGeoMessage(byteArray)
            elif byteArray[2] == 0x24 or byteArray[2] == 0x25:
                return self.parseWifiWithDeviceInfoMessage(byteArray)
            else:
                return None
        return None

    def parseInnerGeoMessage(self,byteArray):
        innerGeoDataMessage = InnerGeoDataMessage()
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        dateStr = "20" + byte2HexString(byteArray[15:21], 0)
        gtm0 = GTM0(dateStr)
        innerGeoDataMessage.serialNo = serialNo
        innerGeoDataMessage.imei = imei
        innerGeoDataMessage.orignBytes = bytes
        innerGeoDataMessage.date = gtm0
        lockGeofenceEnable = byteArray[21]
        innerGeoDataMessage.lockGeofenceEnable = lockGeofenceEnable
        i = 22
        while i + 3 <= len(byteArray):
            id = byteArray[i];
            itemLen = bytes2Short(byteArray, i + 1);
            innerGeofence = InnerGeofence()
            innerGeofence.id = id
            if i + 3 + itemLen > len(byteArray):
                break
            if itemLen == 0:
                i = i + 3 + itemLen
                innerGeofence.type = -1
                innerGeoDataMessage.addGeoPoint(innerGeofence)
                continue
            geoType = byteArray[i + 3]
            innerGeofence.type = geoType
            if geoType == 0x01 or geoType == 0x02 or geoType == 0x03 or geoType == 0x07 or geoType == 0x08:
                j = i + 4
                while j < i + 3 + itemLen:
                    lng = bytes2Float(byteArray, j)
                    lat = bytes2Float(byteArray, j + 4)
                    innerGeofence.addPoint(lat,lng)
                    j = j + 8
            else:
                radius = bytes2Integer(byteArray, i + 4)
                lng = bytes2Float(byteArray, i + 8)
                lat = bytes2Float(byteArray, i + 12)
            innerGeoDataMessage.addGeoPoint(innerGeofence)
            i = i + 3 + itemLen
        return innerGeoDataMessage

    def parseSecondBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        isHistoryData = (byteArray[22] & 0x40) == 0x40
        latlngValid = (byteArray[22] & 0x80) == 0x80
        altitude = 0
        latitude = 0
        longitude = 0
        azimuth = 0
        speed = 0
        strSp = byte2HexString(byteArray[35:37], 0);
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray, 23)
            longitude = bytes2Float(byteArray, 27)
            latitude = bytes2Float(byteArray, 31)
            azimuth = bytes2Short(byteArray, 37)
        else:
            if (byteArray[23] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,23)
            mnc_2g = bytes2Short(byteArray,25)
            lac_2g_1 = bytes2Short(byteArray,27)
            ci_2g_1 = bytes2Short(byteArray,29)
            lac_2g_2 = bytes2Short(byteArray,31)
            ci_2g_2 = bytes2Short(byteArray,33)
            lac_2g_3 = bytes2Short(byteArray,35)
            ci_2g_3 = bytes2Short(byteArray,37)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,23) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,25)
            ci_4g = bytes2Integer(byteArray, 27)
            earfcn_4g_1 = bytes2Short(byteArray, 31)
            pcid_4g_1 = bytes2Short(byteArray, 33)
            earfcn_4g_2 = bytes2Short(byteArray, 35)
            pcid_4g_2 = bytes2Short(byteArray,37)
        if strSp.find("f") == -1:
                speed = -1
        else:
            speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.isHistoryData = isHistoryData
        bluetoothPeripheralDataMessage.serialNo = serialNo
        bluetoothPeripheralDataMessage.latlngValid = latlngValid
        bluetoothPeripheralDataMessage.altitude = altitude
        bluetoothPeripheralDataMessage.latitude = latitude
        bluetoothPeripheralDataMessage.longitude = longitude
        bluetoothPeripheralDataMessage.azimuth = azimuth
        bluetoothPeripheralDataMessage.isHadLocationInfo = True
        bluetoothPeripheralDataMessage.protocolHeadType = 0x12
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bluetoothPeripheralDataMessage.is_4g_lbs = is_4g_lbs
        bluetoothPeripheralDataMessage.is_2g_lbs = is_2g_lbs
        bluetoothPeripheralDataMessage.mcc_4g = mcc_4g
        bluetoothPeripheralDataMessage.mnc_4g = mnc_4g
        bluetoothPeripheralDataMessage.ci_4g = ci_4g
        bluetoothPeripheralDataMessage.earfcn_4g_1 = earfcn_4g_1
        bluetoothPeripheralDataMessage.pcid_4g_1 = pcid_4g_1
        bluetoothPeripheralDataMessage.earfcn_4g_2 = earfcn_4g_2
        bluetoothPeripheralDataMessage.pcid_4g_2 = pcid_4g_2
        bluetoothPeripheralDataMessage.mcc_2g = mcc_2g
        bluetoothPeripheralDataMessage.mnc_2g = mnc_2g
        bluetoothPeripheralDataMessage.lac_2g_1 = lac_2g_1
        bluetoothPeripheralDataMessage.ci_2g_1 = ci_2g_1
        bluetoothPeripheralDataMessage.lac_2g_2 = lac_2g_2
        bluetoothPeripheralDataMessage.ci_2g_2 = ci_2g_2
        bluetoothPeripheralDataMessage.lac_2g_3 = lac_2g_3
        bluetoothPeripheralDataMessage.ci_2g_3 = ci_2g_3
        bleData = byteArray[39:len(byteArray)]
        if len(bleData) <= 0:
            return bluetoothPeripheralDataMessage
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0);
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001;
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x07:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL
            i = 2
            while i < len(bleData):
                bleFuelData = BleFuelData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                valueTemp = bytes2Short(bleData,i+7)
                value=0
                if valueTemp == 65535:
                    value = -999
                else:
                    value = valueTemp
                temperatureTemp = bytes2Short(bleData,i+9)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                status = bleData[i+13]
                if status == 255:
                    status = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 14]) < 0:
                    rssiTemp =  (int) (bleData[i + 14]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 14])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.alarm = status
                bleFuelData.value = value
                bleFuelData.voltage = formatNumb(voltage)
                bleFuelData.temp = formatNumb(temperature)
                bleDataList.append(bleFuelData)
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage

    def parseLockMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        isGpsWorking = (byteArray[21] & 0x20) == 0x00
        isHistoryData = (byteArray[21] & 0x80) != 0x00
        latlngValid = (byteArray[21] & 0x40) == 0x40
        satelliteNumber = byteArray[21] & 0x1F
        is_4g_lbs = False

        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        altitude = 0
        latitude = 0
        longitude = 0
        speed = 0
        azimuth = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray,22)
            latitude = bytes2Float(byteArray,26)
            longitude = bytes2Float(byteArray,30)
            azimuth = bytes2Short(byteArray,36)
            speedStr = byte2HexString(byteArray[34:36],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (byteArray[22] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray, 22)
            mnc_2g = bytes2Short(byteArray, 24)
            lac_2g_1 = bytes2Short(byteArray, 26)
            ci_2g_1 = bytes2Short(byteArray, 28)
            lac_2g_2 = bytes2Short(byteArray, 30)
            ci_2g_2 = bytes2Short(byteArray, 32)
            lac_2g_3 = bytes2Short(byteArray, 34)
            ci_2g_3 = bytes2Short(byteArray, 36)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray, 22) & 0x7FFF
            mnc_4g = bytes2Short(byteArray, 24)
            ci_4g = bytes2Integer(byteArray, 26)
            earfcn_4g_1 = bytes2Short(byteArray, 30)
            pcid_4g_1 = bytes2Short(byteArray, 32)
            earfcn_4g_2 = bytes2Short(byteArray, 34)
            pcid_4g_2 = bytes2Short(byteArray, 36)
        lockType = byteArray[38] & 0xff
        idLen = (byteArray[39] & 0xff) * 2
        idStr = byte2HexString(byteArray[40:],0)
        id = idStr
        if len(idStr) > idLen:
            id = idStr[0:idLen]
        lockMessage = LockMessage()
        lockMessage.serialNo = serialNo
        lockMessage.imei= imei
        lockMessage.orignBytes = bytes
        lockMessage.date = gtm0
        lockMessage.latlngValid = latlngValid
        lockMessage.altitude = altitude
        lockMessage.latitude = latitude
        lockMessage.longitude =longitude
        lockMessage.speed = speed
        lockMessage.azimuth = azimuth
        lockMessage.lockType = lockType
        lockMessage.lockId = id
        lockMessage.isHistoryData = isHistoryData
        lockMessage.satelliteNumber = satelliteNumber
        lockMessage.gpsWorking = isGpsWorking
        lockMessage.is_4g_lbs = is_4g_lbs
        lockMessage.is_2g_lbs = is_2g_lbs
        lockMessage.mcc_4g = mcc_4g
        lockMessage.mnc_4g = mnc_4g
        lockMessage.ci_4g = ci_4g
        lockMessage.earfcn_4g_1 = earfcn_4g_1
        lockMessage.pcid_4g_1 = pcid_4g_1
        lockMessage.earfcn_4g_2 = earfcn_4g_2
        lockMessage.pcid_4g_2 = pcid_4g_2
        lockMessage.mcc_2g = mcc_2g
        lockMessage.mnc_2g = mnc_2g
        lockMessage.lac_2g_1 = lac_2g_1
        lockMessage.ci_2g_1 = ci_2g_1
        lockMessage.lac_2g_2 = lac_2g_2
        lockMessage.ci_2g_2 = ci_2g_2
        lockMessage.lac_2g_3 = lac_2g_3
        lockMessage.ci_2g_3 = ci_2g_3
        return lockMessage
		
        
    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            # configMessage.isNeedResp = isNeedResp
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            # forwardMessage.isNeedResp = isNeedResp
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            # ussdMessage.isNeedResp = isNeedResp
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None

    def parseSignInMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        software = "V{0}.{1}.{2}".format(byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4, byteArray[16] & 0xf)
        firmware = byte2HexString(byteArray[20:22], 0);
        firmware = "V{0}.{1}.{2}".format(firmware[0:1], firmware[1: 2], firmware[2:4])
        hardware = ""
        hardwareByte = byteArray[22]
        if (hardwareByte & 0x80) == 0x80:
            hardware = byte2HexString([hardwareByte & 0x7f],0)
        else:
            hardware = byte2HexString(byteArray[22:23], 0)
        hardware = "V{0}.{1}".format(hardware[0:1], hardware[1: 2])
        signInMessage = SignInMessage()
        signInMessage.firmware = firmware
        signInMessage.imei = imei
        signInMessage.serialNo = serialNo
        # signInMessage.isNeedResp = isNeedResp
        signInMessage.software = software
        signInMessage.hardware = hardware
        signInMessage.orignBytes = byteArray
        return signInMessage

    def parseBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        if (byteArray[21] & 0x01) == 0x01:
            bluetoothPeripheralDataMessage.isIgnition = True
        else:
            bluetoothPeripheralDataMessage.isIgnition = False
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        bluetoothPeripheralDataMessage.date = gtm0
        bluetoothPeripheralDataMessage.orignBytes = byteArray
        bluetoothPeripheralDataMessage.protocolHeadType = byteArray[2]
        bluetoothPeripheralDataMessage.isHistoryData = (byteArray[15] & 0x80) != 0x00
        bluetoothPeripheralDataMessage.serialNo = serialNo
        # bluetoothPeripheralDataMessage.isNeedResp = isNeedResp
        bluetoothPeripheralDataMessage.imei = imei
        bleData = byteArray[22:len(byteArray)]
        bleDataList = []
        if bleData[0] == 0x00 and bleData[1] == 0x01:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE
            i = 2
            while i < len(bleData):
                bleTireData = BleTireData()
                macArray = bleData[i:i + 6]
                mac = byte2HexString(macArray, 0);
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                    voltageTmp += 256
                voltage = -999
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 1.22 + 0.01 * voltageTmp
                airPressureTmp = (int) (bleData[i + 7])
                if airPressureTmp < 0:
                    airPressureTmp += 256
                airPressure = - 999
                if airPressureTmp == 255:
                    airPressure = -999;
                else:
                    airPressure = 1.572 * 2 * airPressureTmp
                airTempTmp = (int) (bleData[i + 8])
                if airTempTmp < 0:
                    airTempTmp += 256
                airTemp = 0
                if airTempTmp == 255:
                    airTemp = -999
                else:
                    airTemp = airTempTmp - 55
                bleTireData.mac = mac
                bleTireData.voltage = voltage
                bleTireData.airPressure = airPressure
                bleTireData.airTemp = airTemp
                alarm = (int) (bleData[i + 9])
                if alarm == -1:
                    alarm = 0
                bleTireData.status  = alarm
                bleDataList.append(bleTireData)
                i+=10
        elif bleData[0] == 0x00 and bleData[1] == 0x02:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS
            bleAlertData = BleAlertData()
            macArray = bleData[2: 8]
            mac = byte2HexString(macArray, 0)
            voltageStr = byte2HexString(bleData,8)[0:2]
            voltage = 0
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9]
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            strSp = byte2HexString(bleData[23:25], 0);
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
                if strSp.find("f") == -1:
                    speed = -1;
                else:
                    speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude  =latitude
            bleAlertData.latlngValid  =latlngValid
            bleAlertData.satelliteCount  =satelliteNumber
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speed
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleAlertData);
        elif bleData[0] == 0x00 and bleData[1] == 0x03:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER
            bleDriverSignInData = BleDriverSignInData()
            macArray = bleData[2:8]
            mac = byte2HexString(macArray, 0);
            voltageStr = byte2HexString(bleData,8)[0: 2]
            voltage = 0;
            try:
                voltage = (float)(voltageStr) / 10
            except:
                print ("voltage error")
            alertByte = bleData[9];
            alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            if alertByte == 0x01:
                alert = BleAlertData.ALERT_TYPE_LOW_BATTERY
            else:
                alert = BleAlertData.ALERT_TYPE_SOS
            isHistoryData = (bleData[10] & 0x80) != 0x00
            latlngValid = (bleData[10] & 0x40) != 0x00
            satelliteNumber = bleData[10] & 0x1F
            altitude = 0
            latitude = 0
            longitude = 0
            azimuth = 0
            speed = 0
            is_4g_lbs = False
            mcc_4g = 0
            mnc_4g = 0
            ci_4g = 0
            earfcn_4g_1 = 0
            pcid_4g_1 = 0
            earfcn_4g_2 = 0
            pcid_4g_2 = 0
            is_2g_lbs = False
            mcc_2g = 0
            mnc_2g = 0
            lac_2g_1 = 0
            ci_2g_1 = 0
            lac_2g_2 = 0
            ci_2g_2 = 0
            lac_2g_3 = 0
            ci_2g_3 = 0
            strSp = byte2HexString(bleData[23:25], 0);
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
                if strSp.find("f") == -1:
                    speed = -1;
                else:
                    speed = (float)("{0}.{1}".format(strSp[0:3],strSp[3:]))
            else:
                if (bleData[11] & 0x80) == 0x80:
                    is_4g_lbs = True
                else:
                    is_2g_lbs = True
            if is_2g_lbs:
                mcc_2g = bytes2Short(bleData,11)
                mnc_2g = bytes2Short(bleData,13)
                lac_2g_1 = bytes2Short(bleData,15)
                ci_2g_1 = bytes2Short(bleData,17)
                lac_2g_2 = bytes2Short(bleData,19)
                ci_2g_2 = bytes2Short(bleData,21)
                lac_2g_3 = bytes2Short(bleData,23)
                ci_2g_3 = bytes2Short(bleData,25)
            if is_4g_lbs:
                mcc_4g = bytes2Short(bleData,11) & 0x7FFF
                mnc_4g = bytes2Short(bleData,13)
                ci_4g = bytes2Integer(bleData, 15)
                earfcn_4g_1 = bytes2Short(bleData, 19)
                pcid_4g_1 = bytes2Short(bleData, 21)
                earfcn_4g_2 = bytes2Short(bleData, 23)
                pcid_4g_2 = bytes2Short(bleData,25)
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude  =latitude
            bleDriverSignInData.latlngValid  =latlngValid
            bleDriverSignInData.satelliteCount  =satelliteNumber
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speed
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                if mac.startswith('0000'):
                    mac = mac[4:12]
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0x0001
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        elif bleData[0] == 0x00 and bleData[1] == 0x05:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR
            i = 2
            while i < len(bleData):
                bleDoorData = BleDoorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                doorStatus = bleData[i+10]
                if doorStatus == 255:
                    doorStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleDoorData.rssi = rssi
                bleDoorData.mac = mac
                bleDoorData.online = online
                bleDoorData.doorStatus = doorStatus
                bleDoorData.voltage = formatNumb(voltage)
                bleDoorData.batteryPercent = batteryPercent
                bleDoorData.temp = formatNumb(temperature)
                bleDataList.append(bleDoorData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x06:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL
            i = 2
            while i < len(bleData):
                bleCtrlData = BleCtrlData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                voltageTmp = (int) (bleData[i + 6])
                if voltageTmp < 0:
                   voltageTmp +=  256
                voltage = 0
                if voltageTmp == 255:
                    voltage = -999
                else:
                    voltage = 2 + 0.01 * voltageTmp
                batteryPercentTemp = (int) (bleData[i + 7])
                if batteryPercentTemp < 0:
                    batteryPercentTemp += 256
                batteryPercent=0
                if batteryPercentTemp == 255:
                    batteryPercent = -999
                else:
                    batteryPercent = batteryPercentTemp
                temperatureTemp = bytes2Short(bleData,i+8)
                tempPositive = 1
                if (temperatureTemp & 0x8000) == 0:
                    tempPositive = -1
                temperature = -999
                if temperatureTemp == 65535:
                    temperature = -999
                else:
                    temperature = (temperatureTemp & 0x7fff) * 0.01 * tempPositive
                online = 1
                ctrlStatus = bleData[i+10]
                if ctrlStatus == 255:
                    ctrlStatus = -999
                    online = 0
                rssiTemp = 0
                if (int)(bleData[i + 11]) < 0:
                    rssiTemp =  (int) (bleData[i + 11]) + 256
                else:
                    rssiTemp = (int) (bleData[i + 11])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                bleCtrlData.rssi = rssi
                bleCtrlData.mac = mac
                bleCtrlData.online = online
                bleCtrlData.ctrlStatus = ctrlStatus
                bleCtrlData.voltage = formatNumb(voltage)
                bleCtrlData.batteryPercent = batteryPercent
                bleCtrlData.temp = formatNumb(temperature)
                bleDataList.append(bleCtrlData)
                i+=12
        elif bleData[0] == 0x00 and bleData[1] == 0x0d:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_Customer2397
            i = 2
            while i < len(bleData):
                bleCustomer2397SensorData = BleCustomer2397SensorData()
                macArray = bleData[i + 0:i + 6]
                mac = byte2HexString(macArray, 0)
                i+=6
                i+=1
                rawDataLen = (int)(bleData[i])
                if rawDataLen < 0:
                    rawDataLen += 256
                if i + rawDataLen >= len(bleData) or rawDataLen < 1:
                    break
                i+=1
                rawData = bleData[i:i+rawDataLen-1]
                i += rawDataLen -1
                rssiTemp = 0
                if (int)(bleData[i]) < 0:
                    rssiTemp = (int)(bleData[i]) + 256
                else:
                    rssiTemp = (int)(bleData[i])
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 256
                i+=1
                bleCustomer2397SensorData.rssi = rssi
                bleCustomer2397SensorData.mac = mac
                bleCustomer2397SensorData.rawData = rawData
                bleDataList.append(bleCustomer2397SensorData)
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        # heartbeatMessage.isNeedResp = isNeedResp
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def parseWifiWithDeviceInfoMessage(self,byteArray):
        isWifiMsg = (byteArray[15] & 0x20) == 0x20

        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        isHisData = (byteArray[15] & 0x80) == 0x80
        isGpsWorking = (byteArray[15] & 0x8) == 0x8
        dateStr = "20" + byte2HexString(byteArray[17:23],0)
        gtm0 = GTM0(dateStr)
        selfMacByte = byteArray[23:29]
        selfMac = byte2HexString(selfMacByte,0)
        ap1MacByte = byteArray[29:35]
        ap1Mac = byte2HexString(ap1MacByte,0)
        rssiTemp = (int) (byteArray[35])
        if rssiTemp < 0:
            rssiTemp += 256
        ap1RSSI = 0
        if rssiTemp == 255:
            ap1RSSI = -999
        else:
            ap1RSSI = rssiTemp - 256
        ap2MacByte = byteArray[36:42]
        ap2Mac = byte2HexString(ap2MacByte,0)
        rssiTemp = (int) (byteArray[42])
        if rssiTemp < 0:
            rssiTemp += 256
        ap2RSSI = 0
        if rssiTemp == 255:
            ap2RSSI = -999
        else:
            ap2RSSI = rssiTemp - 256
        ap3MacByte = byteArray[43:49]
        ap3Mac = byte2HexString(ap3MacByte, 0)
        rssiTemp = (int)(byteArray[49])
        if rssiTemp < 0:
            rssiTemp += 256
        ap3RSSI = 0
        if rssiTemp == 255:
            ap3RSSI = -999
        else:
            ap3RSSI = rssiTemp - 256

        latlngValid = (byteArray[15] & 0x40) == 0x40
        altitude = 0
        latitude = 0
        longitude = 0
        speed = 0
        azimuth = 0
        satelliteCount = 0
        hdop = 0
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        tac = 0
        pcid_4g_1 = 0
        pcid_4g_2 = 0
        pcid_4g_3 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if latlngValid:
            altitude = bytes2Float(byteArray, 23)
            latitude = bytes2Float(byteArray, 31)
            longitude = bytes2Float(byteArray, 27)
            azimuth = bytes2Short(byteArray, 37)
            speedStr = byte2HexString(byteArray[35:37], 0)
            speed = (float)("{0}.{1}".format(speedStr[0:3], speedStr[3:]))
            satelliteCount = byteArray[39]
            hdop = bytes2Short(byteArray, 40)
        else:
            if (byteArray[23] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray, 23)
            mnc_2g = bytes2Short(byteArray, 25)
            lac_2g_1 = bytes2Short(byteArray, 27)
            ci_2g_1 = bytes2Short(byteArray, 29)
            lac_2g_2 = bytes2Short(byteArray, 31)
            ci_2g_2 = bytes2Short(byteArray, 33)
            lac_2g_3 = bytes2Short(byteArray, 35)
            ci_2g_3 = bytes2Short(byteArray, 37)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray, 23) & 0x7FFF
            mnc_4g = bytes2Short(byteArray, 25)
            ci_4g = bytes2Integer(byteArray, 27)
            tac = bytes2Short(byteArray, 31)
            pcid_4g_1 = bytes2Short(byteArray, 33)
            pcid_4g_2 = bytes2Short(byteArray, 35)
            pcid_4g_3 = bytes2Short(byteArray, 37)

        axisXDirect = -1
        if (byteArray[50] & 0x80) == 0x80:
            axisXDirect = -1
        axisX = ((byteArray[50] & 0x7F & 0xff) + (((byteArray[51] & 0xf0) >> 4) & 0xff) / 10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[51] & 0x08) == 0x08:
            axisYDirect = 1
        axisY = (((((byteArray[51] & 0x07) << 4) & 0xff) + (((byteArray[52] & 0xf0) >> 4) & 0xff)) + (
                    byteArray[52] & 0x0F & 0xff) / 10.0) * axisYDirect
        axisZDirect = -1
        if (byteArray[53] & 0x80) == 0x80:
            axisZDirect = 1
        axisZ = ((byteArray[53] & 0x7F & 0xff) + (((byteArray[54] & 0xf0) >> 4) & 0xff) / 10.0) * axisZDirect

        batteryPercentBytes = [byteArray[55]]
        batteryPercentStr = byte2HexString(batteryPercentBytes, 0)
        batteryPercent = 100
        if batteryPercentStr.lower() == "ff":
            batteryPercent = -999
        else:
            batteryPercent = (int)(batteryPercentStr)
            if batteryPercent == 0:
                batteryPercent = 100
        deviceTemp = -999
        if byteArray[56] != 0xff:
            deviceTemp = byteArray[45] & 0x7F
            if (byteArray[56] & 0x80) == 0x80:
                deviceTemp = -1 * deviceTemp
            lightSensorBytes = [byteArray[57]]
        lightSensorStr = byte2HexString(lightSensorBytes, 0)
        lightSensor = 0
        if lightSensorStr.lower() == "ff":
            lightSensor = -1
        else:
            lightSensor = (int)(lightSensorStr) / 10.0

        batteryVoltageBytes = [byteArray[58]]
        batteryVoltageStr = byte2HexString(batteryVoltageBytes, 0)
        batteryVoltage = 0
        if batteryVoltageStr.lower() == "ff":
            batteryVoltage = -1
        else:
            batteryVoltage = (int)(batteryVoltageStr) / 10.0
        solarVoltageBytes = [byteArray[59]]
        solarVoltageStr = byte2HexString(solarVoltageBytes, 0)
        solarVoltage = 0
        if solarVoltageStr.lower() == "ff":
            solarVoltage = -1
        else:
            solarVoltage = (int)(solarVoltageStr) / 10.0

        mileage = bytes2Integer(byteArray, 60)
        status = bytes2Short(byteArray, 64)
        network = (status & 0x7F0) >> 4
        accOnInterval = bytes2Short(byteArray, 66)
        accOffInterval = bytes2Integer(byteArray, 68)
        angleCompensation = (int)(byteArray[72])
        distanceCompensation = bytes2Short(byteArray, 73)
        heartbeatInterval = (int)(byteArray[75])
        isUsbCharging = (status & 0x8000) == 0x8000
        isSolarCharging = (status & 0x8) == 0x8
        iopIgnition = (status & 0x4) == 0x4
        alarmByte = byteArray[16]
        originalAlarmCode = (int)(alarmByte)
        status1 = byteArray[68]
        smartPowerOpenStatus = "close"
        if (status1 & 0x01) == 0x01:
            smartPowerOpenStatus = "enable"
        status2 = byteArray[77]
        isLockSim = (status2 & 0x80) == 0x80
        isLockDevice = (status2 & 0x40) == 0x40
        AGPSEphemerisDataDownloadSettingStatus = (status2 & 0x20) == 0x10
        gSensorSettingStatus = (status2 & 0x10) == 0x10
        frontSensorSettingStatus = (status2 & 0x08) == 0x08
        deviceRemoveAlarmSettingStatus = (status2 & 0x04) == 0x04
        openCaseAlarmSettingStatus = (status2 & 0x02) == 0x02
        deviceInternalTempReadingANdUploadingSettingStatus = (status2 & 0x01) == 0x01
        status3 = byteArray[78];
        smartPowerSettingStatus = "disable"
        if (status3 & 0x80) == 0x80:
            smartPowerSettingStatus = "enable"
        lockType = 0xff
        if len(byteArray) >= 82:
            lockType = byteArray[81]
        if isWifiMsg:
            wifiWithDeviceInfoMessage = WifiWithDeviceInfoMessage()
            wifiWithDeviceInfoMessage.protocolHeadType = byteArray[2]
            wifiWithDeviceInfoMessage.serialNo = serialNo
            wifiWithDeviceInfoMessage.imei= imei
            wifiWithDeviceInfoMessage.orignBytes = byteArray
            wifiWithDeviceInfoMessage.date = gtm0
            wifiWithDeviceInfoMessage.selfMac = selfMac
            wifiWithDeviceInfoMessage.ap1Mac = ap1Mac
            wifiWithDeviceInfoMessage.ap1RSSI = ap1RSSI
            wifiWithDeviceInfoMessage.ap2Mac = ap2Mac
            wifiWithDeviceInfoMessage.ap2RSSI = ap2RSSI
            wifiWithDeviceInfoMessage.ap3Mac = ap3Mac
            wifiWithDeviceInfoMessage.ap3RSSI = ap3RSSI
            wifiWithDeviceInfoMessage.isHisData = isHisData
            wifiWithDeviceInfoMessage.originalAlarmCode = originalAlarmCode
            wifiWithDeviceInfoMessage.axisX = axisX
            wifiWithDeviceInfoMessage.axisY = axisY
            wifiWithDeviceInfoMessage.axisZ = axisZ
            wifiWithDeviceInfoMessage.mileage = mileage
            wifiWithDeviceInfoMessage.networkSignal = network
            wifiWithDeviceInfoMessage.accOnInterval = accOnInterval
            wifiWithDeviceInfoMessage.accOffInterval = accOffInterval
            wifiWithDeviceInfoMessage.angleCompensation = angleCompensation
            wifiWithDeviceInfoMessage.distanceCompensation = distanceCompensation
            wifiWithDeviceInfoMessage.heartbeatInterval = heartbeatInterval
            wifiWithDeviceInfoMessage.isUsbCharging = isUsbCharging
            wifiWithDeviceInfoMessage.isSolarCharging = isSolarCharging
            wifiWithDeviceInfoMessage.iopIgnition = iopIgnition
            if iopIgnition:
                wifiWithDeviceInfoMessage.iop = 0x4000
            else:
                wifiWithDeviceInfoMessage.iop = 0x0000
            wifiWithDeviceInfoMessage.batteryCharge = batteryPercent
            wifiWithDeviceInfoMessage.isLockSim = isLockSim
            wifiWithDeviceInfoMessage.isLockDevice = isLockDevice
            wifiWithDeviceInfoMessage.AGPSEphemerisDataDownloadSettingStatus = AGPSEphemerisDataDownloadSettingStatus
            wifiWithDeviceInfoMessage.gSensorSettingStatus = gSensorSettingStatus
            wifiWithDeviceInfoMessage.frontSensorSettingStatus = frontSensorSettingStatus
            wifiWithDeviceInfoMessage.deviceRemoveAlarmSettingStatus = deviceRemoveAlarmSettingStatus
            wifiWithDeviceInfoMessage.openCaseAlarmSettingStatus = openCaseAlarmSettingStatus
            wifiWithDeviceInfoMessage.deviceInternalTempReadingANdUploadingSettingStatus = deviceInternalTempReadingANdUploadingSettingStatus
            wifiWithDeviceInfoMessage.deviceTemp = deviceTemp
            wifiWithDeviceInfoMessage.lightSensor = lightSensor
            wifiWithDeviceInfoMessage.batteryVoltage = batteryVoltage
            wifiWithDeviceInfoMessage.solarVoltage = solarVoltage
            wifiWithDeviceInfoMessage.smartPowerSettingStatus = smartPowerSettingStatus
            wifiWithDeviceInfoMessage.smartPowerOpenStatus = smartPowerOpenStatus
            return wifiWithDeviceInfoMessage
        else:
            locationMessage = LocationInfoMessage()
            if originalAlarmCode != 0:
                locationMessage = LocationAlarmMessage()
            locationMessage.orignBytes = byteArray
            locationMessage.serialNo = serialNo
            locationMessage.imei = imei
            locationMessage.protocolHeadType = byteArray[2]
            locationMessage.networkSignal = network
            locationMessage.isSolarCharging = isSolarCharging
            locationMessage.isUsbCharging = isUsbCharging
            locationMessage.samplingIntervalAccOn = accOnInterval
            locationMessage.samplingIntervalAccOff = accOffInterval
            locationMessage.angleCompensation = angleCompensation
            locationMessage.distanceCompensation = distanceCompensation
            locationMessage.gpsWorking = isGpsWorking
            locationMessage.isHistoryData = isHisData
            locationMessage.satelliteNumber = satelliteCount
            locationMessage.hdop = hdop
            locationMessage.heartbeatInterval = heartbeatInterval
            locationMessage.originalAlarmCode = originalAlarmCode
            locationMessage.mileage = mileage
            locationMessage.batteryPercent = batteryPercent
            locationMessage.date = gtm0
            locationMessage.latlngValid = latlngValid
            locationMessage.is_4g_lbs = is_4g_lbs
            locationMessage.is_2g_lbs = is_2g_lbs
            locationMessage.mcc_4g = mcc_4g
            locationMessage.mnc_4g = mnc_4g
            locationMessage.ci_4g = ci_4g
            locationMessage.tac = tac
            locationMessage.pcid_4g_1 = pcid_4g_1
            locationMessage.pcid_4g_2 = pcid_4g_2
            locationMessage.pcid_4g_3 = pcid_4g_3
            locationMessage.mcc_2g = mcc_2g
            locationMessage.mnc_2g = mnc_2g
            locationMessage.lac_2g_1 = lac_2g_1
            locationMessage.ci_2g_1 = ci_2g_1
            locationMessage.lac_2g_2 = lac_2g_2
            locationMessage.ci_2g_2 = ci_2g_2
            locationMessage.lac_2g_3 = lac_2g_3
            locationMessage.ci_2g_3 = ci_2g_3
            locationMessage.altitude = altitude
            locationMessage.latitude = latitude
            locationMessage.longitude = longitude
            locationMessage.speed = speed
            locationMessage.azimuth = azimuth
            locationMessage.axisX = axisX
            locationMessage.axisY = axisY
            locationMessage.axisZ = axisZ
            locationMessage.deviceTemp = deviceTemp
            locationMessage.lightSensor = lightSensor
            locationMessage.batteryVoltage = batteryVoltage
            locationMessage.solarVoltage = solarVoltage
            locationMessage.smartPowerOpenStatus = smartPowerOpenStatus
            locationMessage.smartPowerSettingStatus = smartPowerSettingStatus
            locationMessage.isLockSim = isLockSim
            locationMessage.isLockDevice = isLockDevice
            locationMessage.AGPSEphemerisDataDownloadSettingStatus = AGPSEphemerisDataDownloadSettingStatus
            locationMessage.gSensorSettingStatus = gSensorSettingStatus
            locationMessage.frontSensorSettingStatus = frontSensorSettingStatus
            locationMessage.deviceRemoveAlarmSettingStatus = deviceRemoveAlarmSettingStatus
            locationMessage.openCaseAlarmSettingStatus = openCaseAlarmSettingStatus
            locationMessage.deviceInternalTempReadingANdUploadingSettingStatus = deviceInternalTempReadingANdUploadingSettingStatus
            locationMessage.lockType = lockType
            return locationMessage

    def parseWifiMessage(self,bytes):
        wifiMessage = WifiMessage();
        serialNo = bytes2Short(bytes,5)
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        selfMacByte = bytes[21:27]
        selfMac = byte2HexString(selfMacByte,0)
        ap1MacByte = bytes[27:33]
        ap1Mac = byte2HexString(ap1MacByte,0)
        rssiTemp = (int) (bytes[33])
        if rssiTemp < 0:
            rssiTemp += 256
        ap1RSSI = 0
        if rssiTemp == 255:
            ap1RSSI = -999
        else:
            ap1RSSI = rssiTemp - 256
        ap2MacByte = bytes[34:40]
        ap2Mac = byte2HexString(ap2MacByte,0)
        rssiTemp = (int) (bytes[40])
        if rssiTemp < 0:
            rssiTemp += 256
        ap2RSSI = 0
        if rssiTemp == 255:
            ap2RSSI = -999
        else:
            ap2RSSI = rssiTemp - 256
        ap3MacByte = bytes[41:47]
        ap3Mac = byte2HexString(ap3MacByte,0)
        rssiTemp = (int) (bytes[47])
        if rssiTemp < 0:
            rssiTemp += 256
        ap3RSSI = 0
        if rssiTemp == 255:
            ap3RSSI = -999
        else:
            ap3RSSI = rssiTemp - 256
        wifiMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        wifiMessage.imei= imei
        wifiMessage.orignBytes = bytes
        wifiMessage.date = gtm0
        wifiMessage.selfMac = selfMac
        wifiMessage.ap1Mac = ap1Mac
        wifiMessage.ap1RSSI = ap1RSSI
        wifiMessage.ap2Mac = ap2Mac
        wifiMessage.ap2RSSI = ap2RSSI
        wifiMessage.ap3Mac = ap3Mac
        wifiMessage.ap3RSSI = ap3RSSI
        return wifiMessage


    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).encode().decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).encode().decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).encode().decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).encode().decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).encode().decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        # networkInfoMessage.isNeedResp = isNeedResp
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            # configMessage.isNeedResp = isNeedResp
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            # forwardMessage.isNeedResp = isNeedResp
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).encode().decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            # ussdMessage.isNeedResp = isNeedResp
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None


    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
        # isNeedResp = (serialNo & 0x8000) != 0x8000
        imei = decodeImei(byteArray,7)
        dateStr = "20" + byte2HexString(byteArray[17:23],0)
        gtm0 = GTM0(dateStr)
        isGpsWorking = (byteArray[15] & 0x20) == 0x00
        isHistoryData = (byteArray[15] & 0x80) != 0x00
        latlngValid = (byteArray[15] & 0x40) == 0x40
        satelliteNumber = byteArray[15] & 0x1F
        altitude = 0
        latitude = 0
        longitude = 0
        speed = 0
        azimuth = 0
        is_4g_lbs = False
        mcc_4g = 0
        mnc_4g = 0
        ci_4g = 0
        earfcn_4g_1 = 0
        pcid_4g_1 = 0
        earfcn_4g_2 = 0
        pcid_4g_2 = 0
        is_2g_lbs = False
        mcc_2g = 0
        mnc_2g = 0
        lac_2g_1 = 0
        ci_2g_1 = 0
        lac_2g_2 = 0
        ci_2g_2 = 0
        lac_2g_3 = 0
        ci_2g_3 = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray,23)
            latitude = bytes2Float(byteArray,31)
            longitude = bytes2Float(byteArray,27)
            azimuth = bytes2Short(byteArray,37)
            speedStr = byte2HexString(byteArray[35:37],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        else:
            if (byteArray[23] & 0x80) == 0x80:
                is_4g_lbs = True
            else:
                is_2g_lbs = True
        if is_2g_lbs:
            mcc_2g = bytes2Short(byteArray,23)
            mnc_2g = bytes2Short(byteArray,25)
            lac_2g_1 = bytes2Short(byteArray,27)
            ci_2g_1 = bytes2Short(byteArray,29)
            lac_2g_2 = bytes2Short(byteArray,31)
            ci_2g_2 = bytes2Short(byteArray,33)
            lac_2g_3 = bytes2Short(byteArray,35)
            ci_2g_3 = bytes2Short(byteArray,37)
        if is_4g_lbs:
            mcc_4g = bytes2Short(byteArray,23) & 0x7FFF
            mnc_4g = bytes2Short(byteArray,25)
            ci_4g = bytes2Integer(byteArray, 27)
            earfcn_4g_1 = bytes2Short(byteArray, 31)
            pcid_4g_1 = bytes2Short(byteArray, 33)
            earfcn_4g_2 = bytes2Short(byteArray, 35)
            pcid_4g_2 = bytes2Short(byteArray,37)
        axisXDirect = -1
        if (byteArray[39] & 0x80) == 0x80:
            axisXDirect = -1
        axisX = ((byteArray[39] & 0x7F & 0xff) + (((byteArray[40] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect
        axisYDirect = -1
        if (byteArray[40] & 0x08) == 0x08:
            axisYDirect = 1
        axisY = (((((byteArray[40] & 0x07) << 4) & 0xff) + (((byteArray[41] & 0xf0) >> 4) & 0xff)) + (byteArray[41] & 0x0F & 0xff)/10.0)* axisYDirect
        axisZDirect = -1
        if (byteArray[42] & 0x80) == 0x80:
            axisZDirect = 1
        axisZ = ((byteArray[42] & 0x7F & 0xff) + (((byteArray[43] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect

        batteryPercentBytes = [byteArray[44]]
        batteryPercentStr = byte2HexString(batteryPercentBytes, 0)
        batteryPercent = 100
        if batteryPercentStr.lower() == "ff":
            batteryPercent = -999
        else:
            batteryPercent = (int)(batteryPercentStr)
            if  batteryPercent == 0:
                batteryPercent = 100
        deviceTemp = -999
        if byteArray[45] != 0xff:
            deviceTemp = byteArray[45] & 0x7F
            if (byteArray[45] & 0x80) == 0x80:
                deviceTemp = -1 * deviceTemp
            lightSensorBytes = [byteArray[46]]
        lightSensorStr = byte2HexString(lightSensorBytes, 0)
        lightSensor = 0
        if lightSensorStr.lower() == "ff":
            lightSensor = -1
        else:
            lightSensor = (int)(lightSensorStr) / 10.0

        batteryVoltageBytes = [byteArray[47]]
        batteryVoltageStr = byte2HexString(batteryVoltageBytes, 0)
        batteryVoltage = 0
        if batteryVoltageStr.lower() == "ff":
            batteryVoltage = -1
        else:
             batteryVoltage = (int)(batteryVoltageStr) / 10.0
        solarVoltageBytes = [byteArray[48]]
        solarVoltageStr = byte2HexString(solarVoltageBytes, 0)
        solarVoltage = 0
        if solarVoltageStr.lower() == "ff":
            solarVoltage = -1
        else:
            solarVoltage = (int)(solarVoltageStr) / 10.0

        mileage = bytes2Integer(byteArray, 49)
        status = bytes2Short(byteArray, 53)
        network = (status & 0x7F0) >> 4
        accOnInterval = bytes2Short(byteArray, 55)
        accOffInterval = bytes2Integer(byteArray, 57)
        angleCompensation = (int) (byteArray[61])
        distanceCompensation = bytes2Short(byteArray, 62)
        heartbeatInterval = (int) (byteArray[64])
        isUsbCharging = (status & 0x8000) == 0x8000
        isSolarCharging = (status & 0x8) == 0x8
        alarmByte = byteArray[16]
        originalAlarmCode = (int) (alarmByte)
        isAlarmData = command[2] == 0x04
        status1 = byteArray[57]
        smartPowerOpenStatus = "close"
        if (status1 & 0x01) == 0x01:
            smartPowerOpenStatus = "enable"
        status2 = byteArray[66]
        isLockSim = (status2 & 0x80) == 0x80
        isLockDevice = (status2 & 0x40) == 0x40
        AGPSEphemerisDataDownloadSettingStatus = (status2 & 0x20) == 0x10
        gSensorSettingStatus = (status2 & 0x10) == 0x10
        frontSensorSettingStatus = (status2 & 0x08) == 0x08
        deviceRemoveAlarmSettingStatus = (status2 & 0x04) == 0x04
        openCaseAlarmSettingStatus = (status2 & 0x02) == 0x02
        deviceInternalTempReadingANdUploadingSettingStatus = (status2 & 0x01) == 0x01
        status3 = byteArray[67];
        smartPowerSettingStatus = "disable"
        if (status3 & 0x80) == 0x80:
            smartPowerSettingStatus = "enable"
        lockType = 0xff
        if len(byteArray) >= 71:
            lockType = byteArray[70]
        locationMessage = LocationInfoMessage()
        if isAlarmData:
            locationMessage = LocationAlarmMessage()
        locationMessage.orignBytes = byteArray
        locationMessage.serialNo = serialNo
        locationMessage.protocolHeadType = byteArray[2]
        # locationMessage.isNeedResp = isNeedResp
        locationMessage.imei = imei
        locationMessage.networkSignal = network
        locationMessage.isSolarCharging = isSolarCharging
        locationMessage.isUsbCharging = isUsbCharging
        locationMessage.samplingIntervalAccOn = accOnInterval
        locationMessage.samplingIntervalAccOff = accOffInterval
        locationMessage.angleCompensation = angleCompensation
        locationMessage.distanceCompensation = distanceCompensation
        locationMessage.gpsWorking = isGpsWorking
        locationMessage.isHistoryData = isHistoryData
        locationMessage.satelliteNumber = satelliteNumber
        locationMessage.heartbeatInterval = heartbeatInterval
        locationMessage.originalAlarmCode = originalAlarmCode
        locationMessage.mileage = mileage
        locationMessage.batteryPercent = batteryPercent
        locationMessage.date = gtm0
        locationMessage.latlngValid = latlngValid
        locationMessage.is_4g_lbs = is_4g_lbs
        locationMessage.is_2g_lbs = is_2g_lbs
        locationMessage.mcc_4g = mcc_4g
        locationMessage.mnc_4g = mnc_4g
        locationMessage.ci_4g = ci_4g
        locationMessage.earfcn_4g_1 = earfcn_4g_1
        locationMessage.pcid_4g_1 = pcid_4g_1
        locationMessage.earfcn_4g_2 = earfcn_4g_2
        locationMessage.pcid_4g_2 = pcid_4g_2
        locationMessage.mcc_2g = mcc_2g
        locationMessage.mnc_2g = mnc_2g
        locationMessage.lac_2g_1 = lac_2g_1
        locationMessage.ci_2g_1 = ci_2g_1
        locationMessage.lac_2g_2 = lac_2g_2
        locationMessage.ci_2g_2 = ci_2g_2
        locationMessage.lac_2g_3 = lac_2g_3
        locationMessage.ci_2g_3 = ci_2g_3
        locationMessage.altitude = altitude
        locationMessage.latitude = latitude
        locationMessage.longitude =longitude
        locationMessage.speed = speed
        locationMessage.azimuth = azimuth
        locationMessage.axisX = axisX
        locationMessage.axisY = axisY
        locationMessage.axisZ = axisZ
        locationMessage.deviceTemp = deviceTemp
        locationMessage.lightSensor = lightSensor
        locationMessage.batteryVoltage = batteryVoltage
        locationMessage.solarVoltage = solarVoltage
        locationMessage.smartPowerOpenStatus = smartPowerOpenStatus
        locationMessage.smartPowerSettingStatus = smartPowerSettingStatus
        locationMessage.isLockSim = isLockSim
        locationMessage.isLockDevice = isLockDevice
        locationMessage.AGPSEphemerisDataDownloadSettingStatus = AGPSEphemerisDataDownloadSettingStatus
        locationMessage.gSensorSettingStatus = gSensorSettingStatus
        locationMessage.frontSensorSettingStatus = frontSensorSettingStatus
        locationMessage.deviceRemoveAlarmSettingStatus = deviceRemoveAlarmSettingStatus
        locationMessage.openCaseAlarmSettingStatus = openCaseAlarmSettingStatus
        locationMessage.deviceInternalTempReadingANdUploadingSettingStatus = deviceInternalTempReadingANdUploadingSettingStatus
        locationMessage.lockType = lockType
        return locationMessage


class PersonalAssetMsgEncoder:

    encryptType = 0
    aesKey = ""
    def __init__(self,messageEncryptType,aesKey):
        """
        :param messageEncryptType:The message encrypt type .Use the value of MessageEncryptType.
        :param aesKey:The aes key.If you do not use AES encryption, the value can be empty.The type is string
        :return:
        """
        self.encryptType = messageEncryptType
        self.aesKey = aesKey


    def getSignInMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get sign in message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x01]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getHeartbeatMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get heartbeat message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x03]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationMsgReply(self,imei,needSerialNo,serialNo,protocolHeadType):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,protocolHeadType]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode,protocolHeadType):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,protocolHeadType]
        return Encoder.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,self.encryptType,self.aesKey)

    def getLockMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x27,0x27,0x17]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getWifiMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x27,0x27,0x15]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getBluetoothPeripheralMsgReply(self,imei,needSerialNo,serialNo,protocolHeadType):
        command = [0x27,0x27,protocolHeadType]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getNetworkMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x27,0x27,0x05]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)
    def getConfigSettingMsg(self,imei,content):
        """
        Get config setting message
        :param imei:The imei,the type is string
        :param content:The config content.You can use sms config content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x81]
        return Encoder.getConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)

    def getInnerGeoDataMsgReply(self,imei,serialNo,protocolHeadType):
        command = [0x27, 0x27, 0x20]
        return Encoder.getCommonMsgReply(imei,  True,serialNo, command, self.encryptType, self.aesKey)
    def getWifiWithDeviceInfoMsgReply(self,imei,serialNo,sourceAlarmCode,protocolHeadType):
        command = [0x27, 0x27, protocolHeadType]
        content = []
        if sourceAlarmCode != 0:
            content = [sourceAlarmCode]
        return Encoder.getNormalMsgReply(imei,serialNo,command,content,self.encryptType,self.aesKey)

    def getBrocastSettingMsg(self,imei,content):
        """
        Get brocast setting message
        :param imei:The imei,the type is string
        :param content:The brocast content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x81]
        return Encoder.getBrocastMsg(imei,content,command,self.encryptType,self.aesKey)



    def getUSSDMsg(self,imei,content):
        """
        Get USSD message
        :param imei: The imei,the type is string
        :param content: The content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x81]
        return Encoder.getUSSDMsg(imei,content,command,self.encryptType,self.aesKey)

class PTTDecoder:
    HEADER_LENGTH = 3
    HEARTBEAT = [0x28, 0x28, 0x03]
    TALK_START = [0x28, 0x28, 0x04]
    TALK_END = [0x28, 0x28, 0x05]
    VOICE_DATA = [0x28, 0x28, 0x06]
    encryptType = 0
    aesKey = ""

    def __init__(self, messageEncryptType, aesKey):
        """
            Instantiates a new Decoder.
        :param messageEncryptType:The message encrypt type .Use the value of MessageEncryptType.
        :param aesKey:The aes key.If you do not use AES encryption, the value can be empty.The type is string
        :return:
        """
        self.encryptType = messageEncryptType
        self.aesKey = aesKey

    def match(self, byteArray):
        return byteArray == self.HEARTBEAT or byteArray == self.TALK_START or byteArray == self.TALK_END or byteArray == self.VOICE_DATA

    decoderBuf = TopflytechByteBuf()

    def decode(self, buf):
        """
        Decode list.You can get all message at once.
        :param buf:The buf is from socket
        :return:The message list
        """
        buf = bytearray(buf)
        self.decoderBuf.pubBuf(buf)
        messages = []
        if self.decoderBuf.getReadableBytes() < self.HEADER_LENGTH + 2:
            return messages
        bytes = [0, 0, 0]
        while self.decoderBuf.getReadableBytes() > 5:
            self.decoderBuf.markReaderIndex()
            bytes[0] = self.decoderBuf.getByte(0)
            bytes[1] = self.decoderBuf.getByte(1)
            bytes[2] = self.decoderBuf.getByte(2)
            if self.match(bytes):
                self.decoderBuf.skipBytes(self.HEADER_LENGTH)
                lengthBuf = self.decoderBuf.readBytes(2)
                packageLength = bytes2Short(lengthBuf, 0)
                if self.encryptType == MessageEncryptType.MD5:
                    packageLength = packageLength + 8
                elif self.encryptType == MessageEncryptType.AES:
                    packageLength = self.getAesLength(packageLength)
                self.decoderBuf.resetReaderIndex()
                if packageLength <= 0:
                    self.decoderBuf.skipBytes(5)
                    break
                if packageLength > self.decoderBuf.getReadableBytes():
                    break
                data = self.decoderBuf.readBytes(packageLength)
                data = Crypto.decryptData(data, self.encryptType, self.aesKey)
                if data:
                    message = self.build(data)
                    if message:
                        messages.append(message)
            else:
                self.decoderBuf.skipBytes(1)
        return messages

    def build(self, byteArray):
        if (byteArray is not None and len(byteArray) > PTTDecoder.HEADER_LENGTH) or  (byteArray[0] == 0x28 and byteArray[1] == 0x28):
            if byteArray[2] == 0x03:
                return self.parseHeartbeatMessage(byteArray)
            elif byteArray[2] == 0x04:
                return self.parseTalkStartMessage(byteArray)
            elif byteArray[2] == 0x05:
                return self.parseTalkEndMessage(byteArray)
            elif byteArray[2] == 0x06:
                return self.parseVoiceMessage(byteArray)
            else:
                return None
        return None

    def parseHeartbeatMessage(self, byteArray):
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def parseTalkStartMessage(self, byteArray):
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        talkStartMessage = TalkStartMessage()
        talkStartMessage.serialNo = serialNo
        talkStartMessage.imei = imei
        talkStartMessage.orignBytes = byteArray
        return talkStartMessage

    def parseTalkEndMessage(self, byteArray):
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        talkEndMessage = TalkEndMessage()
        talkEndMessage.serialNo = serialNo
        talkEndMessage.imei = imei
        talkEndMessage.orignBytes = byteArray
        return talkEndMessage

    def parseVoiceMessage(self, byteArray):
        serialNo = bytes2Short(byteArray, 5)
        imei = decodeImei(byteArray, 7)
        voiceMessage = VoiceMessage()
        voiceMessage.serialNo = serialNo
        voiceMessage.imei = imei
        voiceMessage.orignBytes = byteArray
        voiceMessage.setEncodeType(byteArray[15])
        voiceLen = bytes2Short(byteArray, 16);
        if len(byteArray) >= 18 + voiceLen:
            voiceData = byteArray[18:18 + voiceLen]
            voiceMessage.setVoiceData(voiceData)
        return voiceMessage

class PTTEncoder:
    def __init__(self, message_encrypt_type, aes_key):
        self.encrypt_type = message_encrypt_type
        self.aes_key = aes_key

    def get_heartbeat_msg_reply(self, imei, serial_no):
        command = [0x28,0x28,0x03]
        return Encoder.getCommonMsgReply(imei, True, serial_no, command, self.encrypt_type, self.aes_key)

    def get_talk_start_msg_reply(self, imei, serial_no, status):
        command = [0x28, 0x28, 0x04]
        content = [status]
        return Encoder.getNormalMsgReply(imei, serial_no, command, content, self.encrypt_type, self.aes_key)

    def get_talk_end_msg_reply(self, imei, serial_no, status):
        command = [0x28, 0x28, 0x05]
        content = struct.pack('b', status)
        return Encoder.getNormalMsgReply(imei, serial_no, command, content, self.encrypt_type, self.aes_key)

    def get_voice_data(self, imei, serial_no, encode_type, voice_data):
        command = [0x28, 0x28, 0x06]
        content = []
        content.extend([encode_type])
        content.extend(voice_data)
        return Encoder.getNormalMsgReply(imei, serial_no, command, content, self.encrypt_type, self.aes_key)

    def get_listen_start_data(self, imei, serial_no):
        command = [0x28, 0x28, 0x07]
        return Encoder.getNormalMsgReply(imei, serial_no, command, [], self.encrypt_type, self.aes_key)

    def get_listen_end_data(self, imei, serial_no):
        command = [0x28, 0x28, 0x08]
        return Encoder.getNormalMsgReply(imei, serial_no, command, [], self.encrypt_type, self.aes_key)