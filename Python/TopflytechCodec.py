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

class BleTempData(BleData):
    voltage = 0
    batteryPercent = 0
    temp = 0
    humidity = 0
    isOpenBox = False
    lightIntensity = 0
    rssi = 0

class BleTireData(BleData):
    voltage = 0
    airPressure = 0
    airTemp = 0
    status = 0

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


class SignInMessage(Message):
    """
        The type Sign in message.Protocol number is 25 25 01.
        Older devices like 8806,8803Pro,You need to respond to the message to the device, otherwise the device will not send other data.
        The new device, like the 8806 plus, needs to be based on the device configuration to decide whether or not to respond to the message
    """
    software = ""  #The software version.like 1.1.1
    firmware = ""  #The firmware version.like 1.1
    hareware = ""  #The hareware version.like 5.0
    platform = ""  #The platform. like 6250
    obdHardware = ""
    obdSoftware = ""

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
    IOP = 0L #The Digital I/O Status
    iopIgnition = False #The vehicle ignition state obtained from the digital I / O status.when ignition return true.
    iopPowerCutOff = False #The external power supply connection status of the vehicle obtained from the digital I / O status.If connected,return true.
    iopACOn = False #The air conditioning status of the vehicle obtained from the digital I / O status.If opened,retrun true.
    speed = 0.0 #The speed.The unit is km / h
    mileage = 0L #The vehicle current mileage.The unit is meter.
    azimuth = 0
    alarm = 0L
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

class Event:
    """
        Device Alarm Type Description.
    """
    NONE = 1
    IGNITION= 2 #ACC from 0 to 1
    PARKING = 3 #ACC from 1 to 0
    ALARM_EXTERNAL_POWER_LOST= 4 #External power disconnect;
    ALARM_LOW_BATTERY = 5 #Low power alarm(inner power voltage 3.5V)
    ALARM_SOS = 6 #SOS alarm
    ALARM_OVER_SPEED= 7 #Over speed alarm
    ALARM_TOWING= 8 #Drag alarm when set
    ADDRESS_REQUESTED = 9 #Device apply address
    ALARM_ANTI_THEFT= 10 #Anti-theft alarm
    FILL_TANK = 11 #Ananlog 1 voltage increase
    ALARM_FUEL_LEAK = 12 #Analog 1 voltage decrease
    ALARM_GEOFENCE_OUT= 13 #In Geofence alarm
    ALARM_GEOFENCE_IN = 14 #Out Geofence alarm
    AC_ON= 15 #Air conditioning opens alarm
    AC_OFF= 16 #Air conditioning off alarm
    IDLE_START= 17 #One time idle start, you can define the idle timing
    IDLE_END= 18 #One time idle end
    ALARM_VIBRATION = 19 #Vibration alarm
    GSM_JAMMER_DETECTION_START= 20 #GSM jammer detection start ,this need config
    GSM_JAMMER_DETECTION_END= 21 #GSM jammer detection end
    ALARM_EXTERNAL_POWER_RECOVER = 22 #External power recover
    ALARM_EXTERNAL_POWER_LOWER = 23 #External power lower than preset external power , this need config
    ALARM_RUDE_DRIVER = 24 #Rude driver alert, this need config
    ALARM_COLLISION = 25 #Collision alert, this need config
    ALARM_TURN_OVER = 26 #Turn over alert, this need config
    ALARM_TIRE_LEAKS = 27
    ALARM_TIRE_BLE_POWER_LOWER = 28
    ALARM_TRACKER_POWER_LOWER = 29
    ALARM_DEVICE_REMOVE = 30
    ALARM_DEVICE_CASE_OPEN = 31
    ALARM_BOX_OPEN = 32
    ALARM_FALL_DOWN = 33
    ALARM_BATTERY_POWER_RECOVER = 34
    ALARM_INNER_TEMP_HIGH = 35
    ALARM_MOVE = 36
    ALARM_INCLINE = 37
    ALARM_USB_RECHARGE_START = 38
    ALARM_USB_RECHARGE_END = 39
    ALARM_DEVICE_MOUNTED = 40
    ALARM_DEVICE_CASE_CLOSED = 41
    ALARM_BOX_CLOSED = 42
    ALARM_FALL_DOWN_REC = 43
    ALARM_INNER_TEMP_HIGH_REC = 44
    ALARM_MOVE_REC = 45
    ALARM_COLLISION_REC = 46
    ALARM_INCLINE_REC = 47
    ALARM_POWER_ON = 48
    ALARM_INNER_TEMP_LOW = 49
    ALARM_INNER_TEMP_LOW_REC = 50
    @staticmethod
    def getEvent(eventByte):
        if eventByte == 0x01:
            return  Event.ALARM_EXTERNAL_POWER_LOST
        elif eventByte == 0x02:
            return Event.ALARM_LOW_BATTERY
        elif eventByte == 0x03:
            return Event.ALARM_SOS
        elif eventByte == 0x04:
            return Event.ALARM_OVER_SPEED
        elif eventByte == 0x05:
            return Event.ALARM_GEOFENCE_IN
        elif eventByte == 0x06:
            return Event.ALARM_GEOFENCE_OUT
        elif eventByte == 0x07:
            return Event.ALARM_TOWING
        elif eventByte == 0x08:
            return Event.ALARM_VIBRATION
        elif eventByte == 0x09:
            return Event.ADDRESS_REQUESTED;
        elif eventByte == 0x10:
            return Event.ALARM_ANTI_THEFT
        elif eventByte == 0x11 or eventByte == 0x13:
            return Event.FILL_TANK
        elif eventByte == 0x12 or eventByte == 0x14:
            return Event.ALARM_FUEL_LEAK
        elif eventByte == 0x15:
            return Event.IGNITION
        elif eventByte == 0x16:
            return Event.PARKING
        elif eventByte == 0x17:
            return Event.AC_ON
        elif eventByte == 0x18:
            return Event.AC_OFF
        elif eventByte == 0x19:
            return Event.IDLE_START
        elif eventByte == 0x20:
            return Event.IDLE_END
        elif eventByte == 0x21:
            return Event.GSM_JAMMER_DETECTION_START
        elif eventByte == 0x22:
            return Event.GSM_JAMMER_DETECTION_END
        elif eventByte == 0x23:
            return Event.ALARM_EXTERNAL_POWER_RECOVER
        elif eventByte == 0x24:
            return Event.ALARM_EXTERNAL_POWER_LOWER
        elif eventByte == 0x25:
            return Event.ALARM_RUDE_DRIVER
        elif eventByte == 0x26:
            return Event.ALARM_COLLISION
        elif eventByte == 0x27:
            return Event.ALARM_TURN_OVER
        else:
            return Event.NONE


def byte2HexString(byteArray,index):
    return ''.join('{:02x}'.format(x) for x in byteArray[index:])

def hexString2Bytes(hexStr):
     hexData = hexStr.decode("hex")
     return map(ord,hexData)

def formatNumb(numb):
    return Decimal(numb).quantize(Decimal('0.00'))

def short2Bytes(number):
    return map(ord,'{:04x}'.format(number).decode("hex"))

def bytes2Short(byteArray,offset):
    return (byteArray[offset]  << 8 & 0xFF00) + (byteArray[offset+1] & 0xFF)

def bytes2Integer(byteArray,offset):
    return (byteArray[offset]<< 24) + (byteArray[offset+1] << 16) + (byteArray[offset+2] << 8) +(byteArray[offset+3] )

def bytes2Float(byteArray,offset):
    data = [byteArray[offset + 3],byteArray[offset +2],byteArray[offset+1],byteArray[offset]]
    b = ''.join(chr(i) for i in data)
    return struct.unpack(">f",b)[0]
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
        bkey = aesKeyMD5(key)
        raw = pad(bytearray(bytes))
        cipher = AES.new(str(bytearray(bkey)), AES.MODE_CBC, iv )
        ciphertext= cipher.encrypt(str(raw))
        destBytes = bytearray(ciphertext)
        return destBytes
    @staticmethod
    def aesDecrypt(bytes,key):
        bkey = aesKeyMD5(key)
        cipher = AES.new(str(bytearray(bkey)), AES.MODE_CBC, iv )
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
    latlngInvalidData = [0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]
    rs232TireHead = [0x00,0x01]
    rs232RfidHead = [0x00,0x03]
    rs232FingerprintHead = [0x00,0x02,0x6C,0x62,0x63]
    rs232CapacitorFuelHead = [0x00,0x04]
    rs232UltrasonicFuelHead = [0x00,0x05]
    MASK_IGNITION =0x4000
    MASK_POWER_CUT =0x8000
    MASK_AC=0x2000
    IOP_RS232_DEVICE_VALID=0x20
    encryptType = 0
    aesKey = ""
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
               or byteArray == self.BLUETOOTH_DATA or byteArray == self.NETWORK_INFO_DATA

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
            elif byteArray[2] == 0x02 or byteArray[2] == 0x04:
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
            else:
                return None
        return None


    def parseSignInMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        str = byte2HexString(byteArray,15)
        if len(str) == 12:
            software = "V{0}.{1}.{2}".format((byteArray[15] & 0xf0) >> 4, byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4)
            firmware = "V{0}.{1}.{2}".format(byteArray[16] & 0xf, (byteArray[17] & 0xf0) >> 4, byteArray[17] & 0xf)
            platform = str[6:10]
            hareware = "V{0}.{1}".format( (byteArray[20] & 0xf0) >> 4, byteArray[20] & 0xf)
            signInMessage = SignInMessage()
            signInMessage.firmware = firmware
            signInMessage.imei = imei
            signInMessage.serialNo = serialNo
            signInMessage.platform = platform
            signInMessage.software = software
            signInMessage.hareware = hareware
            signInMessage.orignBytes = byteArray
            return signInMessage
        return None


    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def parseRS232Message(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        ignition = (byteArray[21] == 0x01)
        data = byteArray[22:]
        rs232Message = RS232Message()
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        rs232Message.date = GTM0(dateStr)
        rs232Message.imei = imei
        rs232Message.serialNo = serialNo
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
            for i in range(0,dataCount):
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
            for i in range(0,dataCount):
                curIndex = i * 10
                rs232RfidMessage = Rs232RfidMessage()
                rfidByte = [data[curIndex + 2],data[curIndex + 3],data[curIndex + 4],data[curIndex + 5], data[curIndex + 6],data[curIndex + 7],data[curIndex + 8],data[curIndex + 9]]
                rs232RfidMessage.rfid = ''.join(chr(i) for i in rfidByte).decode("UTF-8")
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
                fuelStr = ''.join(chr(i) for i in fuelData).decode("UTF-8")
                try:
                    rs232FuelMessage.fuelPercent = (float)(fuelStr) / 100
                except Exception,e:
                    print e.message
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

    def parseBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
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
            except Exception,e:
                print e.message
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
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
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
            except Exception,e:
                print e.message
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
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
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
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 2:i + 6]
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
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                isOpenBox = False
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0xfff;
                    isOpenBox = (0x8000 & lightTemp) == 0x8000
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 128
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.isOpenBox = isOpenBox
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None

    def parseGpsDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        gpsDriverBehaviorMessage = GpsDriverBehaviorMessage()
        behaviorType = (int)(byteArray[15])
        gpsDriverBehaviorMessage.imei = imei
        gpsDriverBehaviorMessage.serialNo = serialNo
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
        acceleration.altitude = bytes2Float(byteArray,curParseIndex + 12)
        acceleration.longitude = bytes2Float(byteArray,curParseIndex + 16)
        acceleration.latitude = bytes2Float(byteArray,curParseIndex + 20)
        speedStr = byte2HexString(byteArray[curParseIndex + 24:curParseIndex + 26],0)
        acceleration.speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        acceleration.azimuth = bytes2Short(byteArray,curParseIndex + 26)
        return acceleration


    def parseAccelerationAlarmMessage(self,byteArray):
        length = bytes2Short(byteArray,3)
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        accidentAccelerationMessage = AccidentAccelerationMessage()
        accidentAccelerationMessage.serialNo = serialNo
        accidentAccelerationMessage.imei = imei
        accidentAccelerationMessage.orignBytes = byteArray
        dataLength = length - 16
        beginIndex = 16
        accidentAccelerationList = []
        while beginIndex < dataLength:
            curParseIndex = beginIndex
            beginIndex = beginIndex + 28
            accelerationData = self.getAccelerationData(byteArray,imei,curParseIndex)
            accidentAccelerationList.append(accelerationData)
        accidentAccelerationMessage.accelerationList = accidentAccelerationList
        return accidentAccelerationMessage

    def parseAccelerationDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        behaviorType = (int)(byteArray[15])
        accelerationDriverBehaviorMessage = AccelerationDriverBehaviorMessage()
        accelerationDriverBehaviorMessage.imei = imei
        accelerationDriverBehaviorMessage.serialNo = serialNo
        accelerationDriverBehaviorMessage.orignBytes = byteArray
        accelerationDriverBehaviorMessage.behaviorType = behaviorType
        beginIndex = 16
        accelerationData = self.getAccelerationData(byteArray,imei,beginIndex)
        accelerationDriverBehaviorMessage.accelerationData = accelerationData
        return accelerationDriverBehaviorMessage

    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
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
        isRelayWorking = (data[13] & 0xC0) == 0xC0
        relayStatus = 0
        if isRelayWorking:
            relayStatus = 1
        isRelayWaiting = ((data[13] & 0xC0) != 0x00) and ((data[13] & 0x80) == 0x00 or (data[13] & 0x40) == 0x00)
        relaySpeedLimit = data[13] & 0x3F
        dragThreshold = bytes2Short(data, 14)
        iop = bytes2Short(data, 16)
        iopIgnition = (iop & self.MASK_IGNITION) == self.MASK_IGNITION
        iopPowerCutOff = (iop & self.MASK_POWER_CUT) == self.MASK_POWER_CUT
        iopACOn = (iop & self.MASK_AC) == self.MASK_AC
        iopRs232DeviceValid = (iop & self.IOP_RS232_DEVICE_VALID) != self.IOP_RS232_DEVICE_VALID
        str = byte2HexString(data, 18)
        analoginput = 0
        try:
            analoginput = (float)("{0}.{1}".format(str[0:2],str[2:4]))
        except Exception,e:
            print e.message
        analoginput2 = 0
        str = byte2HexString(data, 20)
        try:
            analoginput2 = (float)("{0}.{1}".format(str[0:2],str[2:4]))
        except Exception,e:
            print e.message
        originalAlarmCode = (int) (data[22])
        isAlarmData = command[2] == 0x04
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
        if  latlngValid:
            altitude = bytes2Float(data,35)
            latitude = bytes2Float(data,43)
            longitude = bytes2Float(data,39)
            azimuth = bytes2Short(data,49)
            speedStr = byte2HexString(data[47:49],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
        externalPowerVoltage = 0
        if len(data) >= 53:
            externalPowerVoltageStr = byte2HexString(data[51:53], 0)
            externalPowerVoltage = (float(externalPowerVoltageStr) ) / 100
        locationmessage = LocationInfoMessage()
        if isAlarmData:
            locationmessage = LocationAlarmMessage()
        locationmessage.orignBytes = byteArray
        locationmessage.serialNo = serialNo
        locationmessage.imei = imei
        locationmessage.samplingIntervalAccOn = samplingIntervalAccOn
        locationmessage.samplingIntervalAccOff = samplingIntervalAccOff
        locationmessage.angleCompensation = angleCompensation
        locationmessage.distanceCompensation = distanceCompensation
        locationmessage.overspeedLimit = speedLimit
        locationmessage.gpsWorking = isGpsWorking
        locationmessage.isHistoryData = isHistoryData
        locationmessage.satelliteNumber = satelliteNumber
        locationmessage.gSensorSensitivity = gSensorSensitivity
        locationmessage.isManagerConfigured1 = isManagerConfigured1
        locationmessage.isManagerConfigured2 = isManagerConfigured2
        locationmessage.isManagerConfigured3 = isManagerConfigured3
        locationmessage.isManagerConfigured4 = isManagerConfigured4
        locationmessage.antitheftedStatus = antitheftedStatus
        locationmessage.heartbeatInterval = heartbeatInterval
        locationmessage.relayStatus =relayStatus
        locationmessage.isRelayWaiting = isRelayWaiting
        locationmessage.dragThreshold = dragThreshold
        locationmessage.IOP = iop
        locationmessage.iopIgnition = iopIgnition
        locationmessage.iopPowerCutOff = iopPowerCutOff
        locationmessage.iopACOn = iopACOn
        locationmessage.analogInput1 = analoginput
        locationmessage.analogInput2 = analoginput2
        locationmessage.originalAlarmCode = originalAlarmCode
        locationmessage.alarm = Event.getEvent(data[22])
        locationmessage.mileage = mileage
        try:
            charge = (int)(batteryStr)
            if  charge == 0:
                charge = 100
            locationmessage.batteryCharge = charge
        except Exception,e:
            print e.message
        locationmessage.date = gtm0
        locationmessage.latlngValid = latlngValid
        locationmessage.altitude = altitude
        locationmessage.latitude = latitude
        locationmessage.longitude = longitude
        if  latlngValid:
            locationmessage.speed = speed
        else:
            locationmessage.speed = 0
        locationmessage.azimuth = azimuth
        locationmessage.externalPowerVoltage = externalPowerVoltage
        locationmessage.networkSignal = networkSignal
        locationmessage.rs232DeviceValid = iopRs232DeviceValid
        return locationmessage




class Encoder:
    @staticmethod
    def encodeImei(imei):
        assert imei and len(imei) == 15
        return hexString2Bytes("0" + imei)

    @staticmethod
    def encode(imei,useSerialNo,serialNo,command,protocol,content,length):
        command.extend([0x00,length])
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
        command.extend([0x00,length])
        if useSerialNo:
            command.extend(short2Bytes(serialNo))
        else:
            command.extend([0x00,0x01])
        command.extend(Encoder.encodeImei(imei))
        command.extend(content)
        return bytearray(command)

    @staticmethod
    def encodeConfig(imei,useSerialNo,serialNo,command,protocol,content):
        command.extend([0x00,0x10])
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
        data = Encoder.encode(imei,needSerialNo,serialNo,command,bytearray(content),15)
        return Encoder.encrypt(data,messageEncryptType,aesKey)


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


    def getLocationMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x02]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x25,0x25,0x04]
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


    def getBluetoothPeripheralMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x10]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getRS232MsgReply(self,imei,needSerialNo,serialNo):
        command = [0x25,0x25,0x09]
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
    latlngInvalidData = [0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]

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
               or byteArray == self.BLUETOOTH_DATA or byteArray == self.NETWORK_INFO_DATA

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
            elif byteArray[2] == 0x02 or byteArray[2] == 0x04:
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
            elif byteArray[2] == 0x81:
                return self.parseInteractMessage(byteArray)
            else:
                return None
        return None

    def parseSignInMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        str = byte2HexString(byteArray,15)
        if len(str) == 20:
            software = "V{0}.{1}.{2}".format((byteArray[15] & 0xf0) >> 4, byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4)
            firmware = "V{0}.{1}.{2}".format(byteArray[16] & 0xf, (byteArray[17] & 0xf0) >> 4, byteArray[17] & 0xf)
            platform = str[6:10]
            hareware = "V{0}.{1}".format( (byteArray[20] & 0xf0) >> 4, byteArray[20] & 0xf)
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
            signInMessage.platform = platform
            signInMessage.software = software
            signInMessage.hareware = hareware
            signInMessage.orignBytes = byteArray
            signInMessage.obdHardware = obdHardware
            signInMessage.obdSoftware = obdSoftware
            return signInMessage
        return None

    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
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
        imei = decodeImei(byteArray,7)
        dateStr = "20" + byte2HexString(byteArray[15:21],0)
        gtm0 = GTM0(dateStr)
        obdData = ObdMessage()
        obdData.imei = imei
        obdData.orignBytes = byteArray
        obdData.serialNo = serialNo
        obdData.date = gtm0
        obdBytes = byteArray[21:len(byteArray)]

        head = [obdBytes[0],obdBytes[1]]
        if head == self.obdHead:
            obdBytes[2] = obdBytes[2] & 0x0F
            length = bytes2Short(obdBytes,2)
            if length > 0:
                try:
                    data = obdBytes[4:4+length]
                    if (data[0] & 0x41) == 0x41 and data[1] == 0x04 and len(data) > 3:
                        obdData.messageType = ObdMessage.CLEAR_ERROR_CODE_MESSAGE
                        obdData.clearErrorCodeSuccess = data[2] == 0x01
                    elif (data[0] & 0x41) == 0x41 and data[1] == 0x05 and len(data) > 2:
                        vinData = data[2:len(data) - 1]
                        dataValid = False
                        for i in range(len(vinData)):
                            item = vinData[i]
                            if (item & 0xFF) != 0xFF:
                                dataValid = True
                        if len(vinData) and dataValid:
                            obdData.messageType = ObdMessage.VIN_MESSAGE
                            obdData.vin = ''.join(chr(i) for i in vinData).decode("UTF-8")
                    elif (data[0] & 0x41) == 0x41 and (data[1] == 0x03 or data[1] == 0x0A):
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
                except Exception,e:
                    print e.message
        return obdData;


    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
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
        isRelayWorking = (data[13] & 0xC0) == 0xC0
        relayStatus = 0
        if isRelayWorking:
            relayStatus = 1
        isRelayWaiting = ((data[13] & 0xC0) != 0x00) and ((data[13] & 0x80) == 0x00 or (data[13] & 0x40) == 0x00)
        relaySpeedLimit = data[13] & 0x3F
        dragThreshold = bytes2Short(data, 14)
        iop = bytes2Short(data, 16)
        iopIgnition = (iop & self.MASK_IGNITION) == self.MASK_IGNITION
        iopPowerCutOff = (iop & self.MASK_POWER_CUT) == self.MASK_POWER_CUT
        iopACOn = (iop & self.MASK_AC) == self.MASK_AC
        originalAlarmCode = (int) (data[18])
        isAlarmData = command[2] == 0x04
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
        if  latlngValid:
            altitude = bytes2Float(data,31)
            latitude = bytes2Float(data,39)
            longitude = bytes2Float(data,35)
            azimuth = bytes2Short(data,45)
            speedStr = byte2HexString(data[49:51],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
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
        remainFuelRate = (int)(data[67])
        if remainFuelRate < 0:
            remainFuelRate += 256
        if remainFuelRate == 255:
            remainFuelRate = -999
        locationmessage = LocationInfoMessage()
        if isAlarmData:
            locationmessage = LocationAlarmMessage()
        locationmessage.orignBytes = byteArray
        locationmessage.serialNo = serialNo
        locationmessage.imei = imei
        locationmessage.samplingIntervalAccOn = samplingIntervalAccOn
        locationmessage.samplingIntervalAccOff = samplingIntervalAccOff
        locationmessage.angleCompensation = angleCompensation
        locationmessage.distanceCompensation = distanceCompensation
        locationmessage.overspeedLimit = speedLimit
        locationmessage.gpsWorking = isGpsWorking
        locationmessage.isHistoryData = isHistoryData
        locationmessage.satelliteNumber = satelliteNumber
        locationmessage.gSensorSensitivity = gSensorSensitivity
        locationmessage.isManagerConfigured1 = isManagerConfigured1
        locationmessage.isManagerConfigured2 = isManagerConfigured2
        locationmessage.isManagerConfigured3 = isManagerConfigured3
        locationmessage.isManagerConfigured4 = isManagerConfigured4
        locationmessage.antitheftedStatus = antitheftedStatus
        locationmessage.heartbeatInterval = heartbeatInterval
        locationmessage.relayStatus =relayStatus
        locationmessage.isRelayWaiting = isRelayWaiting
        locationmessage.dragThreshold = dragThreshold
        locationmessage.IOP = iop
        locationmessage.iopIgnition = iopIgnition
        locationmessage.iopPowerCutOff = iopPowerCutOff
        locationmessage.iopACOn = iopACOn
        locationmessage.originalAlarmCode = originalAlarmCode
        locationmessage.alarm = Event.getEvent(data[18])
        locationmessage.mileage = mileage
        try:
            charge = (int)(batteryStr)
            if  charge == 0:
                charge = 100
            locationmessage.batteryCharge = charge
        except Exception,e:
            print e.message
        locationmessage.date = gtm0
        locationmessage.latlngValid = latlngValid
        locationmessage.altitude = altitude
        locationmessage.latitude = latitude
        locationmessage.longitude = longitude
        if  latlngValid:
            locationmessage.speed = speed
        else:
            locationmessage.speed = 0
        locationmessage.azimuth = azimuth
        locationmessage.externalPowerVoltage = externalPowerVoltage
        locationmessage.networkSignal = networkSignal
        locationmessage.accumulatingFuelConsumption = accumulatingFuelConsumption
        locationmessage.instantFuelConsumption = instantFuelConsumption
        locationmessage.rpm = rpm
        locationmessage.airInflowTemp = airInflowTemp
        locationmessage.airInput = airInput
        locationmessage.airPressure = airPressure
        locationmessage.coolingFluidTemp  = coolingFluidTemp
        locationmessage.engineLoad = engineLoad
        locationmessage.throttlePosition = throttlePosition
        locationmessage.remainFuelRate  =remainFuelRate
        return locationmessage

    def parseGpsDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        gpsDriverBehaviorMessage = GpsDriverBehaviorMessage()
        behaviorType = (int)(byteArray[15])
        gpsDriverBehaviorMessage.imei = imei
        gpsDriverBehaviorMessage.serialNo = serialNo
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
        imei = decodeImei(byteArray,7)
        accidentAccelerationMessage = AccidentAccelerationMessage()
        accidentAccelerationMessage.serialNo = serialNo
        accidentAccelerationMessage.imei = imei
        accidentAccelerationMessage.orignBytes = byteArray
        dataLength = length - 16
        beginIndex = 16
        accidentAccelerationList = []
        while beginIndex < dataLength:
            curParseIndex = beginIndex
            beginIndex = beginIndex + 30
            accelerationData = self.getAccelerationData(byteArray,imei,curParseIndex)
            accidentAccelerationList.append(accelerationData)
        accidentAccelerationMessage.accelerationList = accidentAccelerationList
        return accidentAccelerationMessage

    def parseAccelerationDriverBehaviorMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        behaviorType = (int)(byteArray[15])
        accelerationDriverBehaviorMessage = AccelerationDriverBehaviorMessage()
        accelerationDriverBehaviorMessage.imei = imei
        accelerationDriverBehaviorMessage.serialNo = serialNo
        accelerationDriverBehaviorMessage.orignBytes = byteArray
        accelerationDriverBehaviorMessage.behaviorType = behaviorType
        beginIndex = 16
        accelerationData = self.getAccelerationData(byteArray,imei,beginIndex)
        accelerationDriverBehaviorMessage.accelerationData = accelerationData
        return accelerationDriverBehaviorMessage

    def parseBluetoothDataMessage(self,byteArray):
        bluetoothPeripheralDataMessage = BluetoothPeripheralDataMessage()
        serialNo = bytes2Short(byteArray,5)
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
            except Exception,e:
                print e.message
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
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
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
            except Exception,e:
                print e.message
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
            if  latlngValid:
                altitude = bytes2Float(bleData, 11)
                latitude = bytes2Float(bleData, 19)
                longitude = bytes2Float(bleData, 15)
                azimuth = bytes2Short(bleData, 25)
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
            bleDataList.append(bleDriverSignInData);
        elif bleData[0] == 0x00 and bleData[1] == 0x04:
            bluetoothPeripheralDataMessage.messageType = BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP
            i = 2
            while i < len(bleData):
                bleTempData = BleTempData()
                macArray = bleData[i + 2:i + 6]
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
                humidityTemp = bytes2Short(bleData,i+10)
                humidity = -999
                if humidityTemp == 65535:
                    humidity = -999
                else:
                    humidity = humidityTemp * 0.01
                lightTemp = bytes2Short(bleData,i+12)
                isOpenBox = False
                lightIntensity = -999
                if lightTemp == 65535:
                    lightIntensity = -999
                else:
                    lightIntensity = lightTemp & 0xfff;
                    isOpenBox = (0x8000 & lightTemp) == 0x8000
                rssiTemp = (int) (bleData[i + 14])
                if rssiTemp < 0:
                    rssiTemp += 256
                rssi = 0
                if rssiTemp == 255:
                    rssi = -999
                else:
                    rssi = rssiTemp - 128
                bleTempData.rssi = rssi
                bleTempData.mac = mac
                bleTempData.lightIntensity = lightIntensity
                bleTempData.isOpenBox = isOpenBox
                bleTempData.humidity = formatNumb(humidity)
                bleTempData.voltage = formatNumb(voltage)
                bleTempData.batteryPercent = batteryPercent
                bleTempData.temp = formatNumb(temperature)
                bleDataList.append(bleTempData);
                i+=15
        bluetoothPeripheralDataMessage.bleDataList =bleDataList
        return bluetoothPeripheralDataMessage;

    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
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


    def getLocationMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x02]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x26,0x26,0x04]
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


    def getBluetoothPeripheralMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x26,0x26,0x10]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)

    def getObdMsgReply(self,imei,needSerialNo,serialNo):
        command = [0x26,0x26,0x09]
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
               or byteArray == self.ALARM or byteArray == self.NETWORK_INFO_DATA


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
            elif byteArray[2] == 0x11:
                return self.parseNetworkInfoMessage(byteArray)
            elif byteArray[2] == 0x81:
                return self.parseInteractMessage(byteArray)
            else:
                return None
        return None

    def parseSignInMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        software = "V{0}.{1}.{2}".format(byteArray[15] & 0xf, (byteArray[16] & 0xf0) >> 4, byteArray[16] & 0xf)
        firmware = byte2HexString(byteArray[20:22], 0);
        firmware = "V{0}.{1}.{2}".format(firmware[0:1], firmware[1: 2], firmware[2:4])
        hareware = byte2HexString(byteArray[22:23], 0);
        hareware = "V{0}.{1}".format(firmware[0:1], firmware[1: 2])
        signInMessage = SignInMessage()
        signInMessage.firmware = firmware
        signInMessage.imei = imei
        signInMessage.serialNo = serialNo
        signInMessage.software = software
        signInMessage.hareware = hareware
        signInMessage.orignBytes = byteArray
        return signInMessage

    def parseHeartbeatMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        heartbeatMessage = HeartbeatMessage()
        heartbeatMessage.serialNo = serialNo
        heartbeatMessage.imei = imei
        heartbeatMessage.orignBytes = byteArray
        return heartbeatMessage

    def parseNetworkInfoMessage(self,bytes):
        networkInfoMessage = NetworkInfoMessage()
        serialNo = bytes2Short(bytes,5)
        imei = decodeImei(bytes,7)
        dateStr = "20" + byte2HexString(bytes[15:21],0)
        gtm0 = GTM0(dateStr)
        networkOperatorLen = bytes[21]
        networkOperatorStartIndex = 22
        networkOperatorByte = bytes[networkOperatorStartIndex:networkOperatorStartIndex + networkOperatorLen]
        networkOperator = ''.join(chr(i) for i in networkOperatorByte).decode("UTF-16LE")
        accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen]
        accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        accessTechnologyByte = bytes[accessTechnologyStartIndex:accessTechnologyStartIndex + accessTechnologyLen]
        accessTechnology = ''.join(chr(i) for i in accessTechnologyByte).decode("UTF-8")
        bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen]
        bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1
        bandLenByte = bytes[bandStartIndex:bandStartIndex + bandLen]
        band = ''.join(chr(i) for i in bandLenByte).decode("UTF-8")
        msgLen = bytes2Short(bytes,3);
        if msgLen > bandStartIndex + bandLen:
            IMSILen = bytes[bandStartIndex + bandLen]
            IMSIStartIndex = bandStartIndex + bandLen + 1
            IMSILenByte = bytes[IMSIStartIndex:IMSIStartIndex + IMSILen]
            IMSI = ''.join(chr(i) for i in IMSILenByte).decode("UTF-8")
            networkInfoMessage.imsi = IMSI
            if msgLen > IMSIStartIndex + IMSILen:
                iccidLen = bytes[IMSIStartIndex + IMSILen]
                iccidStartIndex = IMSIStartIndex + IMSILen + 1
                iccidLenByte = bytes[iccidStartIndex:iccidStartIndex + iccidLen]
                iccid = ''.join(chr(i) for i in iccidLenByte).decode("UTF-8")
                networkInfoMessage.iccid = iccid

        networkInfoMessage.serialNo = serialNo
        networkInfoMessage.imei= imei
        networkInfoMessage.orignBytes = bytes
        networkInfoMessage.date = gtm0
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage

    def parseInteractMessage(self,byteArray):
        serialNo = bytes2Short(byteArray,5)
        imei = decodeImei(byteArray,7)
        protocol = byteArray[15]
        data = byteArray[16:]
        if protocol == 0x01:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            configMessage = ConfigMessage()
            configMessage.serialNo = serialNo
            configMessage.imei = imei
            configMessage.configContent = messageData
            configMessage.orignBytes = byteArray
            return configMessage
        elif protocol == 0x03:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16LE")
            forwardMessage = ForwardMessage()
            forwardMessage.serialNo = serialNo
            forwardMessage.imei = imei
            forwardMessage.content = messageData
            forwardMessage.orignBytes = byteArray
            return forwardMessage
        elif protocol == 0x05:
            messageData = ''.join(chr(i) for i in data).decode("UTF-16lE")
            ussdMessage = USSDMessage()
            ussdMessage.serialNo = serialNo
            ussdMessage.imei = imei
            ussdMessage.content = messageData
            ussdMessage.orignBytes = byteArray
            return ussdMessage
        return None

    def getEvent(self,alarmCodeByte):
        if alarmCodeByte == 0x01:
            return Event.ALARM_DEVICE_REMOVE
        elif alarmCodeByte == 0x02:
            return Event.ALARM_DEVICE_CASE_OPEN
        elif alarmCodeByte == 0x03:
            return Event.ALARM_SOS
        elif alarmCodeByte == 0x04:
            return Event.ALARM_BOX_OPEN
        elif alarmCodeByte == 0x05:
            return Event.ALARM_FALL_DOWN
        elif alarmCodeByte == 0x06:
            return Event.ALARM_LOW_BATTERY
        elif alarmCodeByte == 0x07:
            return Event.ALARM_BATTERY_POWER_RECOVER
        elif alarmCodeByte == 0x08:
            return Event.ALARM_INNER_TEMP_HIGH
        elif alarmCodeByte == 0x09:
            return Event.ALARM_MOVE
        elif alarmCodeByte == 0x10:
            return Event.ALARM_COLLISION
        elif alarmCodeByte == 0x11:
            return Event.ALARM_INCLINE
        elif alarmCodeByte == 0x12:
            return Event.ALARM_USB_RECHARGE_START
        elif alarmCodeByte == 0x13:
            return Event.ALARM_USB_RECHARGE_END
        elif alarmCodeByte == 0x14:
            return Event.ALARM_GEOFENCE_IN
        elif alarmCodeByte == 0x15:
            return Event.ALARM_GEOFENCE_OUT
        elif alarmCodeByte == 0x16:
            return Event.IGNITION
        elif alarmCodeByte == 0x17:
            return Event.PARKING
        elif alarmCodeByte == 0x18:
            return Event.IDLE_START
        elif alarmCodeByte == 0x19:
            return Event.IDLE_END
        elif alarmCodeByte == 0x20:
            return Event.ADDRESS_REQUESTED
        elif alarmCodeByte == 0x21:
            return Event.ALARM_DEVICE_MOUNTED
        elif alarmCodeByte == 0x22:
            return Event.ALARM_DEVICE_CASE_CLOSED
        elif alarmCodeByte == 0x23:
            return Event.ALARM_BOX_CLOSED
        elif alarmCodeByte == 0x24:
            return Event.ALARM_FALL_DOWN_REC
        elif alarmCodeByte == 0x25:
            return Event.ALARM_INNER_TEMP_HIGH_REC
        elif alarmCodeByte == 0x26:
            return Event.ALARM_MOVE_REC
        elif alarmCodeByte == 0x27:
            return Event.ALARM_COLLISION_REC
        elif alarmCodeByte == 0x28:
            return Event.ALARM_INCLINE_REC
        elif alarmCodeByte == 0x29:
            return Event.ALARM_POWER_ON
        elif alarmCodeByte == 0x30:
            return Event.ALARM_INNER_TEMP_LOW
        elif alarmCodeByte == 0x31:
            return Event.ALARM_INNER_TEMP_LOW_REC
        return Event.NONE

    def parseDataMessage(self,byteArray):
        command = byteArray[0:self.HEADER_LENGTH]
        serialNo = bytes2Short(byteArray,5)
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
        azimuth = 0
        if  latlngValid:
            altitude = bytes2Float(byteArray,23)
            latitude = bytes2Float(byteArray,31)
            longitude = bytes2Float(byteArray,27)
            azimuth = bytes2Short(byteArray,37)
            speedStr = byte2HexString(byteArray[35:37],0)
            speed = (float)("{0}.{1}".format(speedStr[0:3],speedStr[3:]))
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
            batteryPercent = 100
        else:
            batteryPercent = (int)(batteryPercentStr)
            if  batteryPercent == 0:
                batteryPercent = 100
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
        iopIgnition = (status & 0x4) == 0x4
        alarmByte = byteArray[16]
        originalAlarmCode = (int) (alarmByte)
        isAlarmData = command[2] == 0x04
        locationmessage = LocationInfoMessage()
        if isAlarmData:
            locationmessage = LocationAlarmMessage()
        locationmessage.orignBytes = byteArray
        locationmessage.serialNo = serialNo
        locationmessage.imei = imei
        locationmessage.networkSignal = network
        locationmessage.isSolarCharging = isSolarCharging
        locationmessage.isUsbCharging = isUsbCharging
        locationmessage.samplingIntervalAccOn = accOnInterval
        locationmessage.samplingIntervalAccOff = accOffInterval
        locationmessage.angleCompensation = angleCompensation
        locationmessage.distanceCompensation = distanceCompensation
        locationmessage.gpsWorking = isGpsWorking
        locationmessage.isHistoryData = isHistoryData
        locationmessage.satelliteNumber = satelliteNumber
        locationmessage.heartbeatInterval = heartbeatInterval
        locationmessage.originalAlarmCode = originalAlarmCode
        locationmessage.alarm = self.getEvent(alarmByte)
        locationmessage.mileage = mileage
        locationmessage.batteryPercent = batteryPercent
        locationmessage.date = gtm0
        locationmessage.latlngValid = latlngValid
        locationmessage.altitude = altitude
        locationmessage.latitude = latitude
        locationmessage.longitude =longitude
        locationmessage.speed = speed
        locationmessage.azimuth = azimuth
        locationmessage.axisX = axisX
        locationmessage.axisY = axisY
        locationmessage.axisZ = axisZ
        locationmessage.deviceTemp = deviceTemp
        locationmessage.lightSensor = lightSensor
        locationmessage.batteryVoltage = batteryVoltage
        locationmessage.solarVoltage = solarVoltage
        return locationmessage


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


    def getLocationMsgReply(self,imei,needSerialNo,serialNo):
        """
        Get location message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x02]
        return Encoder.getCommonMsgReply(imei,needSerialNo,serialNo,command,self.encryptType,self.aesKey)


    def getLocationAlarmMsgReply(self,imei,needSerialNo,serialNo,sourceAlarmCode):
        """
        Get location alarm message reply
        :param imei:The imei,the type is string
        :param needSerialNo:Need serial No or not,the type is boolean.
        :param serialNo:The serial No.The type is int.
        :param sourceAlarmCode: the source alarm code from the source message.The type is int.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x04]
        return Encoder.getLocationAlarmMsgReply(imei,needSerialNo,serialNo,sourceAlarmCode,command,self.encryptType,self.aesKey)




    def getConfigSettingMsg(self,imei,content):
        """
        Get config setting message
        :param imei:The imei,the type is string
        :param content:The config content.You can use sms config content.The type is string.
        :return:The message reply.The type is bytearray
        """
        command = [0x27,0x27,0x81]
        return Encoder.getConfigSettingMsg(imei,content,command,self.encryptType,self.aesKey)


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
