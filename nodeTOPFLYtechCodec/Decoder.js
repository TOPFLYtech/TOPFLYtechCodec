var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
const TopflytechByteBuf = require("./TopflytechByteBuf")

var Decoder = {
    HEADER_LENGTH:3,
    SIGNUP:[0x23, 0x23, 0x01],
    DATA:[0x23, 0x23, 0x02],
    HEARTBEAT:[0x23, 0x23, 0x03],
    ALARM:[0x23, 0x23, 0x04],
    CONFIG:[0x23, 0x23, 0x81],
    SIGNUP_880XPlUS:[0x25, 0x25, 0x01],
    DATA_880XPlUS:[0x25, 0x25, 0x02],
    HEARTBEAT_880XPlUS:[0x25, 0x25, 0x03],
    ALARM_880XPlUS:[0x25, 0x25, 0x04],
    CONFIG_880XPlUS:[0x25, 0x25, 0x81],
    GPS_DRIVER_BEHAVIOR:[0x25, 0x25, 0x05],
    ACCELERATION_DRIVER_BEHAVIOR:[0x25, 0x25, 0x06],
    ACCELERATION_ALARM:[0x25, 0x25, 0x07],
    BLUETOOTH_MAC:[0x25, 0x25, 0x08],
    BLUETOOTH_DATA:[0x25, 0x25, 0x10],
    NETWORK_INFO_DATA:[0x25, 0x25, 0x11],
    BLUETOOTH_SECOND_DATA:[0x25, 0x25, 0x12],
    LOCATION_SECOND_DATA:[0x25, 0x25, 0x13],
    ALARM_SECOND_DATA:[0x25, 0x25, 0x14],
    RS232:[0x25, 0x25, 0x09],
    LOCATION_DATA_WITH_SENSOR:[0x25, 0x25, 0x16],
    LOCATION_ALARM_WITH_SENSOR:[0x25, 0x25, 0x18],
    latlngInvalidData:[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
        0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF],
    rs232TireHead:[0x00,0x01],
    rs232RfidHead:[0x00,0x03],
    rs232FingerprintHead:[0x00,0x02,0x6C,0x62,0x63],
    rs232CapacitorFuelHead:[0x00,0x04],
    rs232UltrasonicFuelHead:[0x00,0x05],
    encryptType:0,
    aesKey:"",
    MASK_IGNITION : 0x4000,
    MASK_POWER_CUT :0x8000,
    MASK_AC       : 0x2000,
    IOP_RS232_DEVICE_VALID       : 0x20,
    MASK_RELAY : 0x400,
    match:function (bytes){
        if(bytes.length != this.HEADER_LENGTH){
            return false
        }
        return ByteUtils.arrayEquals(this.SIGNUP, bytes)
            || ByteUtils.arrayEquals(this.HEARTBEAT, bytes)
            || ByteUtils.arrayEquals(this.DATA, bytes)
            || ByteUtils.arrayEquals(this.ALARM, bytes)
            || ByteUtils.arrayEquals(this.CONFIG, bytes)
            || ByteUtils.arrayEquals(this.SIGNUP_880XPlUS, bytes)
            || ByteUtils.arrayEquals(this.HEARTBEAT_880XPlUS, bytes)
            || ByteUtils.arrayEquals(this.DATA_880XPlUS, bytes)
            || ByteUtils.arrayEquals(this.ALARM_880XPlUS, bytes)
            || ByteUtils.arrayEquals(this.CONFIG_880XPlUS, bytes)
            || ByteUtils.arrayEquals(this.GPS_DRIVER_BEHAVIOR, bytes)
            || ByteUtils.arrayEquals(this.ACCELERATION_DRIVER_BEHAVIOR, bytes)
            || ByteUtils.arrayEquals(this.ACCELERATION_ALARM, bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_MAC, bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_DATA, bytes)
            || ByteUtils.arrayEquals(this.NETWORK_INFO_DATA,bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_SECOND_DATA, bytes)
            || ByteUtils.arrayEquals(this.LOCATION_SECOND_DATA, bytes)
            || ByteUtils.arrayEquals(this.ALARM_SECOND_DATA, bytes)
            || ByteUtils.arrayEquals(this.RS232,bytes)
            || ByteUtils.arrayEquals(this.LOCATION_DATA_WITH_SENSOR,bytes)
            || ByteUtils.arrayEquals(this.LOCATION_ALARM_WITH_SENSOR,bytes)
    },

    decode(buf){
        TopflytechByteBuf.putBuf(buf);
        var messages = [];
        if (TopflytechByteBuf.getReadableBytes() < (this.HEADER_LENGTH + 2)){
            return messages;
        }
        var foundHead = false;
        var bytes= [3]
        while (TopflytechByteBuf.getReadableBytes() > 5){
            TopflytechByteBuf.markReaderIndex();
            bytes[0] = TopflytechByteBuf.getByte(0);
            bytes[1] = TopflytechByteBuf.getByte(1);
            bytes[2] = TopflytechByteBuf.getByte(2);
            if (this.match(bytes)){
                foundHead = true;
                TopflytechByteBuf.skipBytes(this.HEADER_LENGTH);
                var lengthBytes = TopflytechByteBuf.readBytes(2);
                var packageLength = ByteUtils.byteToShort(lengthBytes, 0);
                if (this.encryptType == CryptoTool.MessageEncryptType.MD5){
                    packageLength = packageLength + 8;
                }else if(this.encryptType == CryptoTool.MessageEncryptType.AES){
                    packageLength = CryptoTool.AES.getAesLength(packageLength);
                }
                TopflytechByteBuf.resetReaderIndex();
                if(packageLength <= 0){
                    TopflytechByteBuf.skipBytes(5);
                    break;
                }
                if (packageLength > TopflytechByteBuf.getReadableBytes()){
                    break;
                }
                var data = TopflytechByteBuf.readBytes(packageLength);
                data = CryptoTool.decryptData(data, this.encryptType, this.aesKey);
                if (data != null){
                    try {
                        var message = this.build(data);
                        if (message != null){
                            messages.push(message);
                        }
                    }catch (e){
                        console.log(e)
                    }
                }
            }else{
                TopflytechByteBuf.skipBytes(1);
            }
        }
        return messages
    },
    build:function(bytes){
        if (bytes != null && bytes.length > this.HEADER_LENGTH
            && ((bytes[0] == 0x23 && bytes[1] == 0x23) || (bytes[0] == 0x25 && bytes[1] == 0x25))) {
            switch (bytes[2]) {
                case 0x01:
                    var signInMessage = this.parseLoginMessage(bytes);
                    return signInMessage;
                case 0x03:
                    var heartbeatMessage = this.parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x02:
                case 0x04:
                case 0x16:
                case 0x18:
                    var locationMessage = this.parseDataMessage(bytes);
                    return locationMessage;
                case 0x05:
                    var gpsDriverBehaviorMessage = this.parseGpsDriverBehaviorMessage(bytes);
                    return gpsDriverBehaviorMessage;
                case 0x06:
                    var accelerationDriverBehaviorMessage = this.parseAccelerationDriverBehaviorMessage(bytes);
                    return accelerationDriverBehaviorMessage;
                case 0x07:
                    var accidentAccelerationMessage = this.parseAccelerationAlarmMessage(bytes);
                    return accidentAccelerationMessage;
                case 0x09:
                    var rs232Message = this.parseRs232Message(bytes);
                    return rs232Message;
                case 0x10:
                    var bluetoothPeripheralDataMessage = this.parseBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataMessage;
                case 0x11:
                    var networkInfoMessage = this.parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case 0x12:
                    var bluetoothPeripheralDataSecondMessage = this.parseSecondBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataSecondMessage;
                case 0x13:
                case 0x14:
                    var locationSecondMessage = this.parseSecondDataMessage(bytes);
                    return locationSecondMessage;
                case 0x81:
                    var message =  this.parseInteractMessage(bytes);
                    return message;
                default:
                    return null;
            }
        }
    },
    parseLoginMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var str = ByteUtils.bytes2HexString(bytes,15)
        if(12 == str.length){
            var software =  "V" + ((bytes[15] & 0xf0) >> 4) + "." +  (bytes[15] & 0xf) + "." + ((bytes[16] & 0xf0) >> 4)
            var firmware =  "V" + (bytes[16] & 0xf) + "." + ((bytes[17] & 0xf0) >> 4) + "." + (bytes[17] & 0xf)
            var platform = str.substring(6, 10)
            var hardware = ((bytes[20] & 0xf0) >> 4) + "." + (bytes[20] & 0xf)
            var signInMessage = {
                serialNo:serialNo,
                imei:imei,
                software:software,
                firmware:firmware,
                platform:platform,
                hardware:hardware,
                srcBytes:bytes,
                messageType:"signIn",
            }
            return signInMessage
        }else if(16 == str.length){
            var software = "V" + (bytes[15] & 0xf) + "." +  ((bytes[16] & 0xf0) >> 4) + "." + (bytes[16] & 0xf)
            var firmware = ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes,20,22),0)
            firmware = "V" + firmware.substring(0,1)+ "." +firmware.substring(1, 2)+ "." + firmware.substring(2,3)+ "." +firmware.substring(3,4)
            var hardware = ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes,22,23),0)
            hardware = "V" + hardware.substring(0,1)+ "." +hardware.substring(1, 2)
            var signInMessage = {
                serialNo:serialNo,
                imei:imei,
                software:software,
                firmware:firmware,
                hardware:hardware,
                srcBytes:bytes,
                messageType:"signIn",
            }
            return signInMessage
        }
        return null
    },
    parseHeartbeat:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var HeartbeatMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"heartbeat",
        }
        return HeartbeatMessage
    },
    parseDataMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var message = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"location",
        }
        var data = ByteUtils.arrayOfRange(bytes, 15, bytes.length);
        var samplingIntervalAccOn = ByteUtils.byteToShort(data, 0);
        var samplingIntervalAccOff = ByteUtils.byteToShort(data, 2);
        var angleCompensation = (data[4] & 0xff);
        var distanceCompensation = ByteUtils.byteToShort(data, 5);
        var limit = ByteUtils.byteToShort(data, 7);
        var speedLimit = ((0x7F80 & limit)) >> 7;
        if ((0x8000 & limit) != 0) {
            speedLimit *= 1.609344;
        }
        var networkSignal = limit & 0x7F;
        var isGpsWorking = (data[9] & 0x20) == 0x00;
        var isHistoryData = (data[9] & 0x80) != 0x00;
        var satelliteNumber = data[9] & 0x1F;
        var gSensorSensitivity = (data[10] & 0xF0) >> 4;
        var isManagerConfigured1 = (data[10] & 0x01) != 0x00;
        var isManagerConfigured2 = (data[10] & 0x02) != 0x00;
        var isManagerConfigured3 = (data[10] & 0x04) != 0x00;
        var isManagerConfigured4 = (data[10] & 0x08) != 0x00;
        var antitheftedStatus = (data[11] & 0x10) != 0x00 ? 1 : 0;
        var heartbeatInterval = data[12] & 0x00FF;
        var relayStatus = data[13] & 0x3F;
        var rlyMode =  data[13] & 0xCF;
        var smsLanguageType = data[13] & 0xF;
        var isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);

        var dragThreshold = ByteUtils.byteToShort(data, 14);

        var iop = ByteUtils.byteToShort(data, 16);
        var iopIgnition = (iop & this.MASK_IGNITION) == this.MASK_IGNITION;
        var iopPowerCutOff = (iop & this.MASK_POWER_CUT) == this.MASK_POWER_CUT;
        var iopACOn = (iop & this.MASK_AC) == this.MASK_AC;
        var iopRs232DeviceValid = (iop & this.IOP_RS232_DEVICE_VALID) != this.IOP_RS232_DEVICE_VALID;
        var iopRelay =(iop & this.MASK_RELAY) == this.MASK_RELAY;
        var input1 = iopIgnition ? 1 : 0;
        var input2 = iopACOn ? 1 : 0;
        var input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
        var input4 = (iop & 0x800) == 0x800 ? 1 : 0;
        var input5 = (iop & 0x10) == 0x10 ? 1 : 0;
        var input6 = (iop & 0x08) == 0x08 ? 1 : 0;
        var output1 = (iop & 0x0400) == 0x0400 ? 1 : 0;
        var output2 = (iop & 0x200) == 0x200 ? 1 : 0;
        var output3 = (iop & 0x100) == 0x100 ? 1 : 0;
        var output12V = (iop & 0x10) == 0x10;
        var outputVout = (iop & 0x8) == 0x8;
        var speakerStatus = (iop & 0x40) ==  0x40  ? 1 : 0;
        var rs232PowerOf5V = (iop & 0x20) ==  0x20  ? 1 : 0;
        var accdetSettingStatus = (iop & 0x1) ==  0x1  ? 1 : 0;
        var str = ByteUtils.bytes2HexString(data, 18);
        var analoginput = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput  = parseFloat(str.substring(0, 2) + "." + str.substring(2, 4));
            } catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput = -999;
        }

        str = ByteUtils.bytes2HexString(data, 20);
        var analoginput2 = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput2 = parseFloat(str.substring(0, 2) + "." + str.substring(2, 4));
            }catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput2 = -999;
        }


        var alarmByte = data[22];
        var originalAlarmCode = alarmByte;
        var externalPowerReduceValue = (data[23] & 0x11);
        var isSendSmsAlarmToManagerPhone = (data[23] & 0x20) == 0x20;
        var isSendSmsAlarmWhenDigitalInput2Change = (data[23] & 0x10) == 0x10;
        var jammerDetectionStatus = (data[23] & 0xC);
        var isAlarmData = bytes[2] == 0x04 || bytes[2] == 0x18;
        var mileage =  ByteUtils.byteToLong(data, 24);
        var batteryBytes = [data[28]]
        var batteryStr = ByteUtils.bytes2HexString(batteryBytes, 0);
        var batteryCharge = parseInt(batteryStr);
        if (0 == batteryCharge) {
            batteryCharge = 100;
        }
        var gmt0 = ByteUtils.getGTM0Date(data, 29);
        var latlngValid = (data[9] & 0x40) != 0x00;
        var latlngData = ByteUtils.arrayOfRange(data,35,51);
        if (ByteUtils.arrayEquals(latlngData,this.latlngInvalidData)){
            latlngValid = false;
        }
        var altitude = latlngValid? ByteUtils.bytes2Float(data, 35) : 0.0;
        var latitude = latlngValid ? ByteUtils.bytes2Float(data, 43) : 0.0;
        var longitude = latlngValid ? ByteUtils.bytes2Float(data, 39) : 0.0;
        var speedf = 0.0;
        if (latlngValid){
            try{
                if (latlngValid) {
                    var bytesSpeed = ByteUtils.arrayOfRange(data, 47, 49);
                    var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
                    speedf = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
                }
            }catch (e){

            }
        }
        var azimuth = latlngValid ? ByteUtils.byteToShort(data, 49) : 0;
        var is_4g_lbs = false;
        var mcc_4g = null;
        var mnc_4g = null;
        var ci_4g = null;
        var earfcn_4g_1 = null;
        var pcid_4g_1 = null;
        var earfcn_4g_2 = null;
        var pcid_4g_2 = null;
        var is_2g_lbs = false;
        var mcc_2g = null;
        var mnc_2g = null;
        var lac_2g_1 = null;
        var ci_2g_1 = null;
        var lac_2g_2 = null;
        var ci_2g_2 = null;
        var lac_2g_3 = null;
        var ci_2g_3 = null;
        if (!latlngValid){
            var lbsByte = data[35];
            if ((lbsByte & 0x8) == 0x8){
                is_2g_lbs = true;
            }else{
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(data,35);
            mnc_2g = ByteUtils.byteToShort(data,37);
            lac_2g_1 = ByteUtils.byteToShort(data,39);
            ci_2g_1 = ByteUtils.byteToShort(data,41);
            lac_2g_2 = ByteUtils.byteToShort(data,43);
            ci_2g_2 = ByteUtils.byteToShort(data,45);
            lac_2g_3 = ByteUtils.byteToShort(data,47);
            ci_2g_3 = ByteUtils.byteToShort(data,49);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(data,35);
            mnc_4g = ByteUtils.byteToShort(data,37);
            ci_4g = ByteUtils.byteToLong(data, 39);
            earfcn_4g_1 = ByteUtils.byteToShort(data, 43);
            pcid_4g_1 = ByteUtils.byteToShort(data, 45);
            earfcn_4g_2 = ByteUtils.byteToShort(data, 47);
            pcid_4g_2 = ByteUtils.byteToShort(data,49);
        }
        var externalPowerVoltage = 0;
        if (data.length >=53) {
            var externalPowerVoltageBytes = ByteUtils.arrayOfRange(data, 51, 53);
            var externalPowerVoltageStr = ByteUtils.bytes2HexString(externalPowerVoltageBytes, 0);
            externalPowerVoltage = parseFloat(externalPowerVoltageStr) / 100.0;
        }
        message.isAlarmData = isAlarmData;
        var protocolHead = bytes[2];

        if ((protocolHead == 0x16 || protocolHead == 0x18) && data.length >= 65){
            var axisX = ByteUtils.byteToShort(data,53);
            if (axisX > 32767){
                axisX = axisX - 65536
            }
            var axisY = ByteUtils.byteToShort(data,55);
            if (axisY > 32767){
                axisY = axisY - 65536
            }
            var axisZ = ByteUtils.byteToShort(data,57);
            if (axisZ > 32767){
                axisZ = axisZ - 65536
            }
            var gyroscopeAxisX = ByteUtils.byteToShort(data,59);
            if (gyroscopeAxisX > 32767){
                gyroscopeAxisX = gyroscopeAxisX - 65536
            }
            var gyroscopeAxisY = ByteUtils.byteToShort(data,61);
            if (gyroscopeAxisY > 32767){
                gyroscopeAxisY = gyroscopeAxisY - 65536
            }
            var gyroscopeAxisZ = ByteUtils.byteToShort(data,63);
            if (gyroscopeAxisZ > 32767){
                gyroscopeAxisZ = gyroscopeAxisZ - 65536
            }
            message.axisX = axisX
            message.axisY = axisY
            message.axisZ = axisZ
            message.gyroscopeAxisX = gyroscopeAxisX
            message.gyroscopeAxisY = gyroscopeAxisY
            message.gyroscopeAxisZ = gyroscopeAxisZ

        }
        message.protocolHeadType = protocolHead
        message.networkSignal = networkSignal
        message.samplingIntervalAccOn = samplingIntervalAccOn
        message.samplingIntervalAccOff = samplingIntervalAccOff
        message.angleCompensation = angleCompensation
        message.distanceCompensation = distanceCompensation
        message.overSpeedLimit= speedLimit// 统一单位为 km/h
        message.isGpsWorking =isGpsWorking
        message.isHistoryData =isHistoryData
        message.satelliteNumber =satelliteNumber
        message.gSensorSensitivity = gSensorSensitivity
        message.isManagerConfigured1 = isManagerConfigured1
        message.isManagerConfigured2 = isManagerConfigured2
        message.isManagerConfigured3 = isManagerConfigured3
        message.isManagerConfigured4 = isManagerConfigured4
        message.antitheftedStatus = antitheftedStatus
        message.heartbeatInterval = heartbeatInterval
        message.relayStatus = relayStatus
        message.rlyMode = rlyMode
        message.smsLanguageType = smsLanguageType
        message.isRelayWaiting = isRelayWaiting
        message.dragThreshold = dragThreshold
        message.iop = iop
        message.iopIgnition = iopIgnition
        message.iopPowerCutOff = iopPowerCutOff
        message.iopACOn = iopACOn
        message.iopRs232DeviceValid = iopRs232DeviceValid
        message.analoginput = analoginput
        message.analoginput2 = analoginput2
        message.externalPowerReduceValue = externalPowerReduceValue
        message.originalAlarmCode = originalAlarmCode
        message.mileage = mileage
        message.output12V = output12V
        message.output1 = output1
        message.output2 = output2
        message.output3 = output3
        message.input1 = input1
        message.input2 = input2
        message.input3 = input3
        message.input4 = input4
        message.input5 = input5
        message.input6 = input6
        message.outputVout = outputVout
        message.speakerStatus = speakerStatus
        message.rs232PowerOf5V = rs232PowerOf5V
        message.accdetSettingStatus = accdetSettingStatus
        message.batteryCharge = batteryCharge
        message.date = gmt0
        message.latlngValid = latlngValid
        message.altitude = altitude
        message.latitude = latitude
        message.longitude = longitude
        if(message.latlngValid) {
            message.speed = speedf
        } else {
            message.speed = 0
        }
        message.azimuth = azimuth
        message.externalPowerVoltage = externalPowerVoltage
        message.is_4g_lbs = is_4g_lbs
        message.is_2g_lbs = is_2g_lbs
        message.mcc_2g = mcc_2g
        message.mnc_2g = mnc_2g
        message.lac_2g_1 = lac_2g_1
        message.ci_2g_1 = ci_2g_1
        message.lac_2g_2 = lac_2g_2
        message.ci_2g_2 = ci_2g_2
        message.lac_2g_3 = lac_2g_3
        message.ci_2g_3 = ci_2g_3
        message.mcc_4g = mcc_4g
        message.mnc_4g = mnc_4g
        message.ci_4g = ci_4g
        message.earfcn_4g_1 = earfcn_4g_1
        message.pcid_4g_1 = pcid_4g_1
        message.earfcn_4g_2 = earfcn_4g_2
        message.pcid_4g_2 = pcid_4g_2
        message.isSendSmsAlarmWhenDigitalInput2Change = isSendSmsAlarmWhenDigitalInput2Change
        message.isSendSmsAlarmToManagerPhone = isSendSmsAlarmToManagerPhone
        message.jammerDetectionStatus = jammerDetectionStatus
        return message;
    },
    parseGpsDriverBehaviorMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var behaviorType =  bytes[15]
        var gpsDriverBehaviorMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            behaviorType:behaviorType,
            messageType:"gpsDriverBehavior",
        }
        var startDate = ByteUtils.getGTM0Date(bytes, 16);
        gpsDriverBehaviorMessage.startDate = startDate
        var startAltitude = ByteUtils.bytes2Float(bytes, 22)
        gpsDriverBehaviorMessage.startAltitude = startAltitude;
        var startLongitude = ByteUtils.bytes2Float(bytes, 26);
        gpsDriverBehaviorMessage.startLongitude = startLongitude
        var startLatitude = ByteUtils.bytes2Float(bytes, 30);
        gpsDriverBehaviorMessage.startLatitude = startLatitude
        var bytesSpeed = ByteUtils.arrayOfRange(bytes, 34, 36);
        var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        var startSpeed = 0;
        if (!strSp.toLowerCase() == "ffff"){
            startSpeed = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
        }
        gpsDriverBehaviorMessage.startSpeed = startSpeed;
        var azimuth = ByteUtils.byteToShort(bytes, 36);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.startAzimuth = azimuth;

        var endDate = ByteUtils.getGTM0Date(bytes, 38);
        gpsDriverBehaviorMessage.endDate = endDate;
        var endAltitude = ByteUtils.bytes2Float(bytes, 44);
        gpsDriverBehaviorMessage.endAltitude = endAltitude

        var endLongitude = ByteUtils.bytes2Float(bytes, 48);
        gpsDriverBehaviorMessage.endLongitude = endLongitude

        var endLatitude = ByteUtils.bytes2Float(bytes, 52);
        gpsDriverBehaviorMessage.endLatitude = endLatitude

        bytesSpeed = ByteUtils.arrayOfRange(bytes, 56, 58);
        var endSpeed = 0;
        strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if (!strSp.toLowerCase() == "ffff"){
            endSpeed = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
        }
        gpsDriverBehaviorMessage.endSpeed = endSpeed
        azimuth = ByteUtils.byteToShort(bytes, 58);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.endAzimuth = azimuth
        return gpsDriverBehaviorMessage;
    },
    parseAccelerationDriverBehaviorMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var message = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            behavior:bytes[15],
            messageType:"accelerationDriverBehavior",
        }

        var beginIndex = 16;
        var acceleration = this.getAccelerationData(bytes, imei, beginIndex);
        message.accelerationData = acceleration
        return message;
    },
    getAccelerationData:function (bytes,imei,curParseIndex){
        var acceleration = {};
        acceleration.imei = imei
        acceleration.date = ByteUtils.getGTM0Date(bytes, curParseIndex)
        var isGpsWorking = (bytes[curParseIndex + 6] & 0x20) == 0x00;
        var isHistoryData = (bytes[curParseIndex + 6] & 0x80) != 0x00;
        var satelliteNumber = bytes[curParseIndex + 6] & 0x1F;
        var latlngValid = (bytes[curParseIndex + 6] & 0x40) != 0x00;
        acceleration.isHistoryData =isHistoryData
        acceleration.isGpsWorking =isGpsWorking
        acceleration.satelliteNumber =satelliteNumber
        acceleration.latlngValid =latlngValid
        var axisXDirect = (bytes[curParseIndex + 7] & 0x80) == 0x80 ? 1 : -1;
        var axisX = ((bytes[curParseIndex + 7] & 0x7F & 0xff) + (((bytes[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect;
        acceleration.axisX = axisX
        var axisYDirect = (bytes[curParseIndex + 8] & 0x08) == 0x08 ? 1 : -1;
        var axisY = (((((bytes[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((bytes[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (bytes[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect;
        acceleration.axisY = axisY
        var axisZDirect = (bytes[curParseIndex + 10] & 0x80) == 0x80 ? 1 : -1;
        var axisZ = ((bytes[curParseIndex + 10] & 0x7F & 0xff) + (((bytes[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect;
        acceleration.axisZ= axisZ

        var altitude = ByteUtils.bytes2Float(bytes, curParseIndex + 12);
        acceleration.altitude = altitude

        var longitude = ByteUtils.bytes2Float(bytes, curParseIndex + 16);
        acceleration.longitude = longitude

        var latitude = ByteUtils.bytes2Float(bytes, curParseIndex + 20);
        acceleration.latitude = latitude

        var bytesSpeed = ByteUtils.arrayOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        var speedf = 0;
        var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if (!strSp.toLowerCase() == "ffff"){
            speedf = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length))

        }
        acceleration.speed = speedf
        var azimuth = ByteUtils.byteToShort(bytes, curParseIndex + 26);
        if (azimuth == 255){
            azimuth = 0;
        }
        acceleration.azimuth= azimuth
        var is_4g_lbs = false;
        var mcc_4g = null;
        var mnc_4g = null;
        var ci_4g = null;
        var earfcn_4g_1 = null;
        var pcid_4g_1 = null;
        var earfcn_4g_2 = null;
        var pcid_4g_2 = null;
        var is_2g_lbs = false;
        var mcc_2g = null;
        var mnc_2g = null;
        var lac_2g_1 = null;
        var ci_2g_1 = null;
        var lac_2g_2 = null;
        var ci_2g_2 = null;
        var lac_2g_3 = null;
        var ci_2g_3 = null;
        if (!latlngValid){
            var lbsByte = bytes[curParseIndex + 12];
            if ((lbsByte & 0x8) == 0x8){
                is_2g_lbs = true;
            }else{
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(bytes,curParseIndex + 12);
            mnc_2g = ByteUtils.byteToShort(bytes,curParseIndex + 14);
            lac_2g_1 = ByteUtils.byteToShort(bytes,curParseIndex + 16);
            ci_2g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 18);
            lac_2g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 20);
            ci_2g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 22);
            lac_2g_3 = ByteUtils.byteToShort(bytes,curParseIndex + 24);
            ci_2g_3 = ByteUtils.byteToShort(bytes,curParseIndex + 26);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 12);
            mnc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 14);
            ci_4g = ByteUtils.byteToLong(bytes, curParseIndex + 16);
            earfcn_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 20);
            pcid_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 22);
            earfcn_4g_2 = ByteUtils.byteToShort(bytes, curParseIndex + 24);
            pcid_4g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 26);
        }
        if(bytes.length > 44){
            var gyroscopeAxisXDirect = (bytes[curParseIndex + 28] & 0x80) == 0x80 ? 1 : -1;
            var gyroscopeAxisX = (((bytes[curParseIndex + 28] & 0x7F & 0xff) << 4) + ((bytes[curParseIndex + 29] & 0xf0) >> 4)) * gyroscopeAxisXDirect;
            acceleration.gyroscopeAxisX= gyroscopeAxisX
            var gyroscopeAxisYDirect = (bytes[curParseIndex + 29] & 0x08) == 0x08 ? 1 : -1;
            var gyroscopeAxisY = ((((bytes[curParseIndex + 29] & 0x07) << 4) & 0xff) + (bytes[curParseIndex + 30] &  0xff))* gyroscopeAxisYDirect;
            acceleration.gyroscopeAxisY = gyroscopeAxisY
            var gyroscopeAxisZDirect = (bytes[curParseIndex + 31] & 0x80) == 0x80 ? 1 : -1;
            var gyroscopeAxisZ = (((bytes[curParseIndex + 31] & 0x7F & 0xff)<< 4) + ((bytes[curParseIndex + 32] & 0xf0) >> 4)) * gyroscopeAxisZDirect;
            acceleration.gyroscopeAxisZ = gyroscopeAxisZ
        }
        acceleration.is_4g_lbs =is_4g_lbs
        acceleration.is_2g_lbs =is_2g_lbs
        acceleration.mcc_2g =mcc_2g
        acceleration.mnc_2g =mnc_2g
        acceleration.lac_2g_1 =lac_2g_1
        acceleration.ci_2g_1 =ci_2g_1
        acceleration.lac_2g_2 =lac_2g_2
        acceleration.ci_2g_2 =ci_2g_2
        acceleration.lac_2g_3 =lac_2g_3
        acceleration.ci_2g_3 =ci_2g_3
        acceleration.mcc_4g =mcc_4g
        acceleration.mnc_4g =mnc_4g
        acceleration.ci_4g =ci_4g
        acceleration.earfcn_4g_1 =earfcn_4g_1
        acceleration.pcid_4g_1 =pcid_4g_1
        acceleration.earfcn_4g_2 =earfcn_4g_2
        acceleration.pcid_4g_2 =pcid_4g_2
        return acceleration;
    },
    parseAccelerationAlarmMessage:function (bytes){
        var length = ByteUtils.byteToShort(bytes,3);
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var behaviorType = bytes[15];
        var accidentAccelerationMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            behaviorType:behaviorType,
            messageType:"accidentAcceleration",
        }

        var dataLength = length - 16;
        var dataCount = dataLength / 28;
        var beginIndex = 16;
        var accidentAccelerationList = []
        for (var i = 0 ; i < dataCount;i++){
            var curParseIndex = beginIndex + i * 28;
            var accidentAcceleration = this.getAlarmAccelerationData(bytes,imei,curParseIndex);
            accidentAccelerationList.push(accidentAcceleration);
        }
        accidentAccelerationMessage.accelerationList = accidentAccelerationList
        return accidentAccelerationMessage;
    },
    getAlarmAccelerationData(bytes,imei,curParseIndex){
        var acceleration = {}
        acceleration.imei = imei
        acceleration.date = ByteUtils.getGTM0Date(bytes,curParseIndex)
        var isGpsWorking = (bytes[curParseIndex + 6] & 0x20) == 0x00;
        var isHistoryData = (bytes[curParseIndex + 6] & 0x80) != 0x00;
        var satelliteNumber = bytes[curParseIndex + 6] & 0x1F;
        var latlngValid = (bytes[curParseIndex + 6] & 0x40) != 0x00;
        acceleration.isHistoryData = isHistoryData
        acceleration.isGpsWorking = isGpsWorking
        acceleration.satelliteNumber = satelliteNumber
        acceleration.latlngValid = latlngValid
        var axisXDirect = (bytes[curParseIndex + 7] & 0x80) == 0x80 ? 1 : -1;
        var axisX = ((bytes[curParseIndex + 7] & 0x7F & 0xff) + (((bytes[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0) * axisXDirect;
        acceleration.axisX = axisX
        var axisYDirect = (bytes[curParseIndex + 8] & 0x08) == 0x08 ? 1 : -1;
        var axisY = (((((bytes[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((bytes[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (bytes[curParseIndex + 9] & 0x0F & 0xff)/10.0)* axisYDirect;
        acceleration.axisY = axisY
        var axisZDirect = (bytes[curParseIndex + 10] & 0x80) == 0x80 ? 1 : -1;
        var axisZ = ((bytes[curParseIndex + 10] & 0x7F & 0xff) + (((bytes[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0) * axisZDirect;
        acceleration.axisZ  = axisZ

        var altitude = ByteUtils.bytes2Float(bytes, curParseIndex + 12);
        acceleration.altitude  = altitude

        var longitude = ByteUtils.bytes2Float(bytes, curParseIndex + 16);
        acceleration.longitude = longitude

        var latitude = ByteUtils.bytes2Float(bytes, curParseIndex + 20);
        acceleration.latitude = latitude

        var bytesSpeed = ByteUtils.arrayOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        var speedf = 0;
        var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if (!strSp.toLowerCase() == "ffff"){
            try {
                speedf = parseFloat(strSp.substring(0, 3)  + "." +  strSp.substring(3, strSp.length))
            }catch (e){
                console.log("GPS Acceleration Speed Error ; imei is :" + imei);
            }
        }
        acceleration.speed = speedf
        var is_4g_lbs = false;
        var mcc_4g = null;
        var mnc_4g = null;
        var ci_4g = null;
        var earfcn_4g_1 = null;
        var pcid_4g_1 = null;
        var earfcn_4g_2 = null;
        var pcid_4g_2 = null;
        var is_2g_lbs = false;
        var mcc_2g = null;
        var mnc_2g = null;
        var lac_2g_1 = null;
        var ci_2g_1 = null;
        var lac_2g_2 = null;
        var ci_2g_2 = null;
        var lac_2g_3 = null;
        var ci_2g_3 = null;
        if (!latlngValid){
            var lbsByte = bytes[curParseIndex + 12];
            if ((lbsByte & 0x8) == 0x8){
                is_2g_lbs = true;
            }else{
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(bytes,curParseIndex + 12);
            mnc_2g = ByteUtils.byteToShort(bytes,curParseIndex + 14);
            lac_2g_1 = ByteUtils.byteToShort(bytes,curParseIndex + 16);
            ci_2g_1 = ByteUtils.byteToShort(bytes,curParseIndex + 18);
            lac_2g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 20);
            ci_2g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 22);
            lac_2g_3 = ByteUtils.byteToShort(bytes,curParseIndex + 24);
            ci_2g_3 = ByteUtils.byteToShort(bytes,curParseIndex + 26);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 12);
            mnc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 14);
            ci_4g = ByteUtils.byteToLong(bytes, curParseIndex + 16);
            earfcn_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 20);
            pcid_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 22);
            earfcn_4g_2 = ByteUtils.byteToShort(bytes, curParseIndex + 24);
            pcid_4g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 26);
        }
        var azimuth = 0;
        try {
            azimuth = ByteUtils.byteToShort(bytes, curParseIndex + 26);
            if (azimuth == 255){
                azimuth = 0;
            }
        }catch (e){
            console.log("GPS Acceleration azimuth Error ; imei is :" + imei + ":" + ByteUtils.bytes2HexString(bytes,0));
        }
        acceleration.azimuth = azimuth
        acceleration.is_4g_lbs = is_4g_lbs
        acceleration.is_2g_lbs = is_2g_lbs
        acceleration.mcc_2g = mcc_2g
        acceleration.mnc_2g = mnc_2g
        acceleration.lac_2g_1 = lac_2g_1
        acceleration.ci_2g_1 = ci_2g_1
        acceleration.lac_2g_2 = lac_2g_2
        acceleration.ci_2g_2 = ci_2g_2
        acceleration.lac_2g_3 = lac_2g_3
        acceleration.ci_2g_3 = ci_2g_3
        acceleration.mcc_4g = mcc_4g
        acceleration.mnc_4g = mnc_4g
        acceleration.ci_4g = ci_4g
        acceleration.earfcn_4g_1 = earfcn_4g_1
        acceleration.pcid_4g_1 = pcid_4g_1
        acceleration.earfcn_4g_2 = earfcn_4g_2
        acceleration.pcid_4g_2 = pcid_4g_2
        return acceleration;
    },
    parseRs232Message:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var date = ByteUtils.getGTM0Date(bytes,15)
        var isIgnition = (bytes[21] == 0x01);
        var data = ByteUtils.arrayOfRange(bytes, 22, bytes.length);
        var rs232Message = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            date:date,
            isIgnition:isIgnition,
            messageType:"rs232Data",
        }
        if(data.length < 2){
            return rs232Message;
        }
        var rs232Head = [data[0],data[1]]
        var fingerprintHead;
        if(data.length > 4 ){
            fingerprintHead = [data[0],data[1],data[2],data[3],data[4]];
        }
        var messageList = [];
        if(ByteUtils.arrayEquals(rs232Head,this.rs232TireHead)){
            rs232Message.rs232DataType = "tire"
            var dataCount = (data.length -2 ) / 7
            var curIndex = 2;
            for(var i = 0;i < dataCount;i++){
                curIndex = i * 7 + 2;
                var rs232TireMessage = {}
                var airPressureTmp =  data[curIndex + 4];
                if(airPressureTmp < 0){
                    airPressureTmp += 256;
                }
                var airPressure = airPressureTmp * 1.572 * 2;
                rs232TireMessage.airPressure = airPressure
                var airTempTmp = data[curIndex + 5];
                if(airTempTmp < 0){
                    airTempTmp += 256;
                }
                var airTemp = airTempTmp - 55;
                rs232TireMessage.airTemp = airTemp
                var statusTmp = data[curIndex + 6];
                if(statusTmp < 0){
                    statusTmp += 256;
                }
                rs232TireMessage.statusTmp = statusTmp
                var voltageTmp = data[curIndex + 3];
                if(voltageTmp < 0){
                    voltageTmp += 256;
                }
                var voltage = 0.01 * voltageTmp + 1.22;
                rs232TireMessage.voltage = voltage
                var sensorIdByte = [data[curIndex],data[curIndex + 1],data[curIndex + 2]];
                rs232TireMessage.sensorId = ByteUtils.bytes2HexString(sensorIdByte,0)
                messageList.push(rs232TireMessage);
            }
        }else if(ByteUtils.arrayEquals(rs232Head,this.rs232RfidHead)){
            rs232Message.rs232DataType = "rfid"
            var dataCount = data.length / 10;
            var curIndex = 0;
            for(var i = 0;i < dataCount;i++){
                curIndex = i * 10;
                var rs232RfidMessage = {};
                var rfidByte = [data[curIndex + 2],data[curIndex + 3],data[curIndex + 4],data[curIndex + 5],
                    data[curIndex + 6],data[curIndex + 7],data[curIndex + 8],data[curIndex + 9]];
                rs232RfidMessage.rfid = ByteUtils.bin2String(rfidByte);
                messageList.push(rs232RfidMessage);
            }
        }else if(ByteUtils.arrayEquals(rs232Head,this.rs232FingerprintHead)){
            rs232Message.rs232DataType = "fingerprint"
            var rs232FingerprintMessage = {}
            if(data[6] == 0x00){
                rs232FingerprintMessage.status = "succ"
            }else{
                rs232FingerprintMessage.status = "error"
            }
            if(data[5] == 0x25){
                rs232FingerprintMessage.fingerprintType = "cloud_register"
                var dataIndex = data[8];
                rs232FingerprintMessage.fingerprintDataIndex = dataIndex;
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
                if(data.length > 10){
                    rs232FingerprintMessage.data = ByteUtils.bytes2HexString(data,10)
                }
            }else if(data[5]== 0x71){
                rs232FingerprintMessage.fingerprintType = "patch"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0x73){
                rs232FingerprintMessage.fingerprintType = "delete"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0x78){
                rs232FingerprintMessage.fingerprintType = "write_template"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
                var remarkId = data[8]
                if(remarkId < 0){
                    remarkId += 256;
                }
                rs232FingerprintMessage.remarkId = remarkId
            }else if(data[5] == 0xA6){
                rs232FingerprintMessage.fingerprintType = "set_permission"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0xA7){
                rs232FingerprintMessage.fingerprintType = "get_permission"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0x54){
                rs232FingerprintMessage.fingerprintType = "all_clear"
            }else if(data[5] == 0x74){
                rs232FingerprintMessage.fingerprintType = "get_empty_id"
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0xA5){
                rs232FingerprintMessage.fingerprintType = "patch_permission"
            }else if(data[5] == 0xA3){
                rs232FingerprintMessage.fingerprintType = "register"
                var fingerprintId = data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
            }else if(data[5] == 0x77){
                rs232FingerprintMessage.fingerprintType= "get_template"
                var dataIndex = data[8]
                rs232FingerprintMessage.fingerprintDataIndex =dataIndex
                var fingerprintId = data[7]
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.fingerprintId = fingerprintId
                rs232FingerprintMessage.data = ByteUtils.bytes2HexString(data, 10)
            }else if(data[5] == 0x59){
                rs232FingerprintMessage.fingerprintType = "get_device_id"
            }
            messageList.push(rs232FingerprintMessage);
        }else if(ByteUtils.arrayEquals(rs232Head,this.rs232CapacitorFuelHead)){
            rs232Message.rs232DataType = "capacitor_fuel"
            var rs232FuelMessage = {}
            var fuelData = ByteUtils.arrayOfRange(data,2,6);
            if(fuelData.length > 0){
                var fuelStr = ByteUtils.bin2String(fuelData);
                try {
                    if(!fuelStr.trim() == ""){
                        rs232FuelMessage.fuelPercent = parseFloat(fuelStr) / 100;
                    }
                }catch (e){
                    console.log("empty fuel:" + fuelStr + " " + ByteUtils.bytes2HexString(fuelData,0));
                }
            }
            rs232FuelMessage.alarm = data[6];
            messageList.push(rs232FuelMessage);
        }else if(ByteUtils.arrayEquals(rs232Head,this.rs232UltrasonicFuelHead)){
            rs232Message.rs232DataType = "ultrasonic_fuel"
            var rs232FuelMessage = {}
            var curHeightData = ByteUtils.arrayOfRange(data, 2, 4);
            var curHeightStr = ByteUtils.bytes2HexString(curHeightData, 0);
            if(curHeightStr.toLowerCase() == "ffff"){
                rs232FuelMessage.curLiquidHeight = -999
            }else{
                rs232FuelMessage.curLiquidHeight = parseFloat(curHeightStr) / 10
            }
            var tempData = ByteUtils.arrayOfRange(data,4,6)
            var tempStr = ByteUtils.bytes2HexString(tempData, 0)
            if(tempStr.toLowerCase() == "ffff"){
                rs232FuelMessage.temp = -999
            }else{
                rs232FuelMessage.temp = parseFloat(tempStr.substring(1,4)) / 10.0
                if(tempStr.substring(0,1) == "1"){
                    rs232FuelMessage.temp = -1 * rs232FuelMessage.temp
                }
            }
            var fullHeightData = ByteUtils.arrayOfRange(data,6,8);
            var fullHeightStr = ByteUtils.bytes2HexString(fullHeightData, 0);
            if(fullHeightStr.toLowerCase() == "ffff"){
                rs232FuelMessage.fullLiquidHeight = -999
            }else{
                rs232FuelMessage.fullLiquidHeight = parseFloat(fullHeightStr )
            }
            rs232FuelMessage.liquidType = data[8] + 1
            rs232FuelMessage.alarm = data[9]
            messageList.push(rs232FuelMessage);
        }else{
            var rs232DeviceMessage = {}
            rs232DeviceMessage.rS232Data = data
            rs232Message.rs232DataType = "other_Device"
            messageList.push(rs232DeviceMessage);
        }
        rs232Message.rs232DeviceMessageList = messageList
        return rs232Message;
    },
    parseBluetoothDataMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var bluetoothPeripheralDataMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"bluetoothData",
        }
        bluetoothPeripheralDataMessage.isIgnition = (bytes[21] & 0x01) == 0x01
        bluetoothPeripheralDataMessage.protocolHeadType = bytes[2]
        bluetoothPeripheralDataMessage.date = ByteUtils.getGTM0Date(bytes, 15);
        var bleData = ByteUtils.arrayOfRange(bytes,22,bytes.length);
        if (bleData.length <= 0){
            return bluetoothPeripheralDataMessage;
        }
        var bleDataList = []
        if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.bleMessageType = "tire"
            for (var i = 2;i+10 <= bleData.length;i+=10){
                var bleTireData = [];
                var macArray = ByteUtils.arrayOfRange(bleData, i, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp =  bleData[i + 6] < 0 ?  bleData[i + 6] + 256 :  bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 1.22 + 0.01 * voltageTmp;
                }
                var airPressureTmp =  bleData[i + 7] < 0 ?  bleData[i + 7] + 256 :  bleData[i + 7];
                var airPressure;
                if(airPressureTmp == 255){
                    airPressure = -999;
                }else{
                    airPressure = 1.572 * 2 * airPressureTmp;
                }
                var airTempTmp =  bleData[i + 8] < 0 ? bleData[i + 8] + 256 :  bleData[i + 8];
                var airTemp;
                if(airTempTmp == 255){
                    airTemp = -999;
                }else{
                    airTemp = airTempTmp - 55;
                }
//            var isTireLeaks = (bleData[i+5] == 0x01);
                bleTireData.mac =mac
                bleTireData.voltage =voltage
                bleTireData.airPressure =airPressure
                bleTireData.airTemp =airTemp
//            bleTireData.setIsTireLeaks(isTireLeaks);
                var alarm =  bleData[i + 9];
                if(alarm == -1){
                    alarm = 0;
                }
                bleTireData.alarm = alarm
                bleDataList.push(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.bleMessageType = "sos"
            var bleAlertData = {}
            var macArray = ByteUtils.arrayOfRange(bleData, 2, 8);
            var mac = ByteUtils.bytes2HexString(macArray, 0);
            var voltageStr = ByteUtils.bytes2HexString(bleData,8).substring(0, 2);
            var voltage = parseFloat(voltageStr) / 10;
            var alertByte = bleData[9];
            var alert = alertByte == 0x01 ? "low_battery" : "sos";
            var isHistoryData = (bleData[10] & 0x80) != 0x00;
            var latlngValid = (bleData[10] & 0x40) != 0x00;
            var satelliteCount = bleData[10] & 0x1F;
            var altitude = latlngValid? ByteUtils.bytes2Float(bleData, 11) : 0.0;
            var longitude = latlngValid ? ByteUtils.bytes2Float(bleData, 15) : 0.0;
            var latitude = latlngValid ? ByteUtils.bytes2Float(bleData, 19) : 0.0;
            var azimuth = latlngValid ? ByteUtils.byteToShort(bleData, 25) : 0;
            var speedf = 0.0;
            if (latlngValid){
                var bytesSpeed = ByteUtils.arrayOfRange(bleData, 23, 25);
                var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
                if(strSp.indexOf("f") != -1){
                    speedf = -1;
                }else {
                    speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
                }
            }
            var is_4g_lbs = false;
            var mcc_4g = null;
            var mnc_4g = null;
            var ci_4g = null;
            var earfcn_4g_1 = null;
            var pcid_4g_1 = null;
            var earfcn_4g_2 = null;
            var pcid_4g_2 = null;
            var is_2g_lbs = false;
            var mcc_2g = null;
            var mnc_2g = null;
            var lac_2g_1 = null;
            var ci_2g_1 = null;
            var lac_2g_2 = null;
            var ci_2g_2 = null;
            var lac_2g_3 = null;
            var ci_2g_3 = null;
            if (!latlngValid){
                var lbsByte = bleData[11];
                if ((lbsByte & 0x8) == 0x8){
                    is_2g_lbs = true;
                }else{
                    is_4g_lbs = true;
                }
            }
            if (is_2g_lbs){
                mcc_2g = ByteUtils.byteToShort(bleData,11);
                mnc_2g = ByteUtils.byteToShort(bleData,13);
                lac_2g_1 = ByteUtils.byteToShort(bleData,15);
                ci_2g_1 = ByteUtils.byteToShort(bleData,17);
                lac_2g_2 = ByteUtils.byteToShort(bleData,19);
                ci_2g_2 = ByteUtils.byteToShort(bleData,21);
                lac_2g_3 = ByteUtils.byteToShort(bleData,23);
                ci_2g_3 = ByteUtils.byteToShort(bleData,25);
            }
            if (is_4g_lbs){
                mcc_4g = ByteUtils.byteToShort(bleData,11);
                mnc_4g = ByteUtils.byteToShort(bleData,13);
                ci_4g = ByteUtils.byteToLong(bleData, 15);
                earfcn_4g_1 = ByteUtils.byteToShort(bleData, 19);
                pcid_4g_1 = ByteUtils.byteToShort(bleData, 21);
                earfcn_4g_2 = ByteUtils.byteToShort(bleData, 23);
                pcid_4g_2 = ByteUtils.byteToShort(bleData,25);
            }
            bleAlertData.alertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHistoryData
            bleAlertData.latitude = latitude
            bleAlertData.latlngValid = latlngValid
            bleAlertData.satelliteCount = satelliteCount
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speedf
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleDataList.push(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.bleMessageType = "driver"
            var bleDriverSignInData = {}
            var macArray = ByteUtils.arrayOfRange(bleData, 2,8);
            var mac = ByteUtils.bytes2HexString(macArray, 0);
            var voltageStr = ByteUtils.bytes2HexString(bleData,8).substring(0, 2);
            var voltage = parseFloat(voltageStr) / 10;
            var alertByte = bleData[9];
            var alert = alertByte == 0x01 ? "low_battery" : "driver";
            var isHistoryData = (bleData[10] & 0x80) != 0x00;
            var latlngValid = (bleData[10] & 0x40) != 0x00;
            var satelliteCount = bleData[10] & 0x1F;
            var altitude = latlngValid? ByteUtils.bytes2Float(bleData, 11) : 0.0;
            var longitude = latlngValid ? ByteUtils.bytes2Float(bleData, 15) : 0.0;
            var latitude = latlngValid ? ByteUtils.bytes2Float(bleData, 19) : 0.0;
            var azimuth = latlngValid ? ByteUtils.byteToShort(bleData, 25) : 0;
            var speedf = 0.0;
            if (latlngValid){
                var bytesSpeed = ByteUtils.arrayOfRange(bleData, 23, 25);
                var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
                if(strSp.indexOf("f") != -1){
                    speedf = -1;
                }else {
                    speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
                }
            }
            var is_4g_lbs = false;
            var mcc_4g = null;
            var mnc_4g = null;
            var ci_4g = null;
            var earfcn_4g_1 = null;
            var pcid_4g_1 = null;
            var earfcn_4g_2 = null;
            var pcid_4g_2 = null;
            var is_2g_lbs = false;
            var mcc_2g = null;
            var mnc_2g = null;
            var lac_2g_1 = null;
            var ci_2g_1 = null;
            var lac_2g_2 = null;
            var ci_2g_2 = null;
            var lac_2g_3 = null;
            var ci_2g_3 = null;
            if (!latlngValid){
                var lbsByte = bleData[11];
                if ((lbsByte & 0x8) == 0x8){
                    is_2g_lbs = true;
                }else{
                    is_4g_lbs = true;
                }
            }
            if (is_2g_lbs){
                mcc_2g = ByteUtils.byteToShort(bleData,11);
                mnc_2g = ByteUtils.byteToShort(bleData,13);
                lac_2g_1 = ByteUtils.byteToShort(bleData,15);
                ci_2g_1 = ByteUtils.byteToShort(bleData,17);
                lac_2g_2 = ByteUtils.byteToShort(bleData,19);
                ci_2g_2 = ByteUtils.byteToShort(bleData,21);
                lac_2g_3 = ByteUtils.byteToShort(bleData,23);
                ci_2g_3 = ByteUtils.byteToShort(bleData,25);
            }
            if (is_4g_lbs){
                mcc_4g = ByteUtils.byteToShort(bleData,11);
                mnc_4g = ByteUtils.byteToShort(bleData,13);
                ci_4g = ByteUtils.byteToLong(bleData, 15);
                earfcn_4g_1 = ByteUtils.byteToShort(bleData, 19);
                pcid_4g_1 = ByteUtils.byteToShort(bleData, 21);
                earfcn_4g_2 = ByteUtils.byteToShort(bleData, 23);
                pcid_4g_2 = ByteUtils.byteToShort(bleData,25);
            }
            bleDriverSignInData.alert = alert
            bleDriverSignInData.altitude = altitude
            bleDriverSignInData.azimuth = azimuth
            bleDriverSignInData.voltage = voltage
            bleDriverSignInData.isHistoryData = isHistoryData
            bleDriverSignInData.latitude = latitude
            bleDriverSignInData.latlngValid = latlngValid
            bleDriverSignInData.satelliteCount = satelliteCount
            bleDriverSignInData.longitude = longitude
            bleDriverSignInData.mac = mac
            bleDriverSignInData.speed = speedf
            bleDriverSignInData.is_4g_lbs = is_4g_lbs
            bleDriverSignInData.is_2g_lbs = is_2g_lbs
            bleDriverSignInData.mcc_2g = mcc_2g
            bleDriverSignInData.mnc_2g = mnc_2g
            bleDriverSignInData.lac_2g_1 = lac_2g_1
            bleDriverSignInData.ci_2g_1 = ci_2g_1
            bleDriverSignInData.lac_2g_2 = lac_2g_2
            bleDriverSignInData.ci_2g_2 = ci_2g_2
            bleDriverSignInData.lac_2g_3 = lac_2g_3
            bleDriverSignInData.ci_2g_3 = ci_2g_3
            bleDriverSignInData.mcc_4g = mcc_4g
            bleDriverSignInData.mnc_4g = mnc_4g
            bleDriverSignInData.ci_4g = ci_4g
            bleDriverSignInData.earfcn_4g_1 = earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 = pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 = earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 = pcid_4g_2
            bleDataList.push(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.bleMessageType = "temp"
            for (var i = 2;i +15 <= bleData.length;i+=15){
                var bleTempData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                if(mac.startsWith("0000")){
                    mac = mac.substring(4,12);
                }
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6]
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var humidityTemp = ByteUtils.byteToShort(bleData,i+10);
                var humidity;
                if(humidityTemp == 65535){
                    humidity = -999;
                }else{
                    humidity = parseFloat((humidityTemp * 0.01).toFixed(2));
                }
                var lightTemp = ByteUtils.byteToShort(bleData,i+12);
                var lightIntensity ;
                if(lightTemp == 65535){
                    lightIntensity = -999;
                }else{
                    lightIntensity = lightTemp & 0x0001;
                }
                var rssiTemp = bleData[i + 14] < 0 ? bleData[i + 14] + 256 : bleData[i + 14];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleTempData.rssi =rssi
                bleTempData.mac =mac
                bleTempData.lightIntensity =lightIntensity
                bleTempData.humidity =humidity
                bleTempData.voltage =voltage
                bleTempData.batteryPercent =batteryPercent
                bleTempData.temp =temperature
                bleDataList.push(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.bleMessageType = "door"
            for (var i = 2;i+12 <= bleData.length;i+=12){
                var bleDoorData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ?  bleData[i + 6] + 256 :  bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var doorStatus = bleData[i+10] < 0 ? bleData[i+10] + 256 : bleData[i+10];
                var online = 1;
                if(doorStatus == 255){
                    doorStatus = -999;
                    online = 0;
                }

                var rssiTemp = bleData[i + 11] < 0 ? bleData[i + 11] + 256 : bleData[i + 11];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleDoorData.rssi =rssi
                bleDoorData.mac =mac
                bleDoorData.online =online
                bleDoorData.doorStatus =doorStatus
                bleDoorData.voltage =voltage
                bleDoorData.batteryPercent =batteryPercent
                bleDoorData.temp =temperature
                bleDataList.push(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.bleMessageType = "ctrl"
            for (var i = 2;i+12 <= bleData.length;i+=12){
                var bleCtrlData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed());
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var CtrlStatus =bleData[i+10] < 0 ? bleData[i+10] + 256 : bleData[i+10];
                var online = 1;
                if(CtrlStatus == 255){
                    CtrlStatus = -999;
                    online = 0;
                }

                var rssiTemp = bleData[i + 11] < 0 ? bleData[i + 11] + 256 : bleData[i + 11];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleCtrlData.setRssi = rssi
                bleCtrlData.setMac = mac
                bleCtrlData.setOnline = online
                bleCtrlData.setCtrlStatus = CtrlStatus
                bleCtrlData.voltage = voltage
                bleCtrlData.setBatteryPercent = batteryPercent
                bleCtrlData.temp = temperature
                bleDataList.push(bleCtrlData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x07){
            bluetoothPeripheralDataMessage.bleMessageType = "fuel"
            for (var i = 2;i+15 <= bleData.length;i+=15){
                var bleFuelData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var valueTemp = ByteUtils.byteToShort(bleData,i+7);
                var value;
                if(valueTemp == 65535){
                    value = -999;
                }else{
                    value = valueTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+9);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var status =bleData[i+13] < 0 ? bleData[i+13] + 256 : bleData[i+13];
                var online = 1;
                if(status == 255){
                    status = 0;
                    online = 0;
                }
                var rssiTemp = bleData[i + 14] < 0 ? bleData[i + 14] + 256 : bleData[i + 14];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.status = status
                bleFuelData.voltage = voltage
                bleFuelData.value = value
                bleFuelData.temp = temperature
                bleDataList.push(bleFuelData);
            }
        }
        bluetoothPeripheralDataMessage.bleDataList = bleDataList
        return bluetoothPeripheralDataMessage;
    },
    parseNetworkInfoMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var networkInfoMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"networkInfo",
        }
        var gmt0 = ByteUtils.getGTM0Date(bytes,15)
        var networkOperatorLen = bytes[21];
        var networkOperatorStartIndex = 22;
        var networkOperatorByte = ByteUtils.arrayOfRange(bytes, networkOperatorStartIndex, networkOperatorStartIndex + networkOperatorLen);
        var networkOperator = ByteUtils.charArrayToStr(networkOperatorByte,"ascii")
        var accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen];
        var accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        var accessTechnologyByte = ByteUtils.arrayOfRange(bytes, accessTechnologyStartIndex,accessTechnologyStartIndex + accessTechnologyLen);
        var accessTechnology = ByteUtils.bin2String(accessTechnologyByte)
        var bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen];
        var bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1;
        var bandLenByte = ByteUtils.arrayOfRange(bytes, bandStartIndex,bandStartIndex + bandLen);
        var band = ByteUtils.bin2String(bandLenByte)
        var msgLen = ByteUtils.byteToShort(bytes,3);
        if(msgLen > bandStartIndex + bandLen ){
            var IMSILen = bytes[bandStartIndex + bandLen];
            var IMSIStartIndex = bandStartIndex + bandLen + 1;
            var IMSILenByte = ByteUtils.arrayOfRange(bytes,IMSIStartIndex,IMSIStartIndex + IMSILen);
            var IMSI = ByteUtils.bin2String(IMSILenByte)
            networkInfoMessage.imsi = IMSI
            if(msgLen > IMSIStartIndex + IMSILen){
                var iccidLen = bytes[IMSIStartIndex + IMSILen];
                var iccidStartIndex = IMSIStartIndex + IMSILen + 1;
                var iccidLenByte = ByteUtils.arrayOfRange(bytes,iccidStartIndex,iccidStartIndex + iccidLen);
                var iccid = ByteUtils.bin2String(iccidLenByte)
                networkInfoMessage.iccid = iccid
            }
        }
        networkInfoMessage.date = gmt0;
        networkInfoMessage.accessTechnology = accessTechnology
        networkInfoMessage.networkOperator = networkOperator
        networkInfoMessage.band = band
        return networkInfoMessage;
    },
    parseSecondBluetoothDataMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var bluetoothPeripheralDataMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"bluetoothData",
        }
        bluetoothPeripheralDataMessage.isIgnition = (bytes[21] & 0x01) == 0x01
        bluetoothPeripheralDataMessage.protocolHeadType = bytes[2]
        bluetoothPeripheralDataMessage.date = ByteUtils.getGTM0Date(bytes, 15)
        var latlngValid = (bytes[22] & 0x40) == 0x40;
        var isHisData = (bytes[22] & 0x80) == 0x80;
        bluetoothPeripheralDataMessage.latlngValid = latlngValid
        bluetoothPeripheralDataMessage.isHistoryData = isHisData
        var altitude = latlngValid? ByteUtils.bytes2Float(bytes, 23) : 0.0;
        var latitude = latlngValid ? ByteUtils.bytes2Float(bytes, 27) : 0.0;
        var longitude = latlngValid ? ByteUtils.bytes2Float(bytes, 31) : 0.0;
        var azimuth = latlngValid ? ByteUtils.byteToShort(bytes, 37) : 0;
        var speedf = 0.0;
        if(latlngValid){
            var bytesSpeed = ByteUtils.arrayOfRange(bytes, 35, 37);
            var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
            if(strSp.indexOf("f") != -1){
                speedf = -999
            }else {
                speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
            }
        }
        var is_4g_lbs = false;
        var mcc_4g = null;
        var mnc_4g = null;
        var ci_4g = null;
        var earfcn_4g_1 = null;
        var pcid_4g_1 = null;
        var earfcn_4g_2 = null;
        var pcid_4g_2 = null;
        var is_2g_lbs = false;
        var mcc_2g = null;
        var mnc_2g = null;
        var lac_2g_1 = null;
        var ci_2g_1 = null;
        var lac_2g_2 = null;
        var ci_2g_2 = null;
        var lac_2g_3 = null;
        var ci_2g_3 = null;
        if (!latlngValid){
            var lbsByte = bytes[23];
            if ((lbsByte & 0x8) == 0x8){
                is_2g_lbs = true;
            }else{
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(bytes,23);
            mnc_2g = ByteUtils.byteToShort(bytes,25);
            lac_2g_1 = ByteUtils.byteToShort(bytes,27);
            ci_2g_1 = ByteUtils.byteToShort(bytes,29);
            lac_2g_2 = ByteUtils.byteToShort(bytes,31);
            ci_2g_2 = ByteUtils.byteToShort(bytes,33);
            lac_2g_3 = ByteUtils.byteToShort(bytes,35);
            ci_2g_3 = ByteUtils.byteToShort(bytes,37);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(bytes,23);
            mnc_4g = ByteUtils.byteToShort(bytes,25);
            ci_4g = ByteUtils.byteToLong(bytes, 27);
            earfcn_4g_1 = ByteUtils.byteToShort(bytes, 31);
            pcid_4g_1 = ByteUtils.byteToShort(bytes, 33);
            earfcn_4g_2 = ByteUtils.byteToShort(bytes, 35);
            pcid_4g_2 = ByteUtils.byteToShort(bytes,37);
        }
        bluetoothPeripheralDataMessage.latitude = latitude
        bluetoothPeripheralDataMessage.longitude = longitude
        bluetoothPeripheralDataMessage.azimuth = azimuth
        bluetoothPeripheralDataMessage.speed = speedf
        bluetoothPeripheralDataMessage.altitude = altitude
        bluetoothPeripheralDataMessage.isHadLocationInfo = true
        bluetoothPeripheralDataMessage.is_4g_lbs =is_4g_lbs
        bluetoothPeripheralDataMessage.is_2g_lbs =is_2g_lbs
        bluetoothPeripheralDataMessage.mcc_2g =mcc_2g
        bluetoothPeripheralDataMessage.mnc_2g =mnc_2g
        bluetoothPeripheralDataMessage.lac_2g_1 =lac_2g_1
        bluetoothPeripheralDataMessage.ci_2g_1 =ci_2g_1
        bluetoothPeripheralDataMessage.lac_2g_2 =lac_2g_2
        bluetoothPeripheralDataMessage.ci_2g_2 =ci_2g_2
        bluetoothPeripheralDataMessage.lac_2g_3 =lac_2g_3
        bluetoothPeripheralDataMessage.ci_2g_3 =ci_2g_3
        bluetoothPeripheralDataMessage.mcc_4g =mcc_4g
        bluetoothPeripheralDataMessage.mnc_4g =mnc_4g
        bluetoothPeripheralDataMessage.ci_4g =ci_4g
        bluetoothPeripheralDataMessage.earfcn_4g_1 =earfcn_4g_1
        bluetoothPeripheralDataMessage.pcid_4g_1 =pcid_4g_1
        bluetoothPeripheralDataMessage.earfcn_4g_2 =earfcn_4g_2
        bluetoothPeripheralDataMessage.pcid_4g_2 =pcid_4g_2
        var bleData = ByteUtils.arrayOfRange(bytes,39,bytes.length);
        if (bleData.length <= 0){
            return bluetoothPeripheralDataMessage;
        }
        var bleDataList = []
        if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.bleMessageType = "tire"
            for (var i = 2;i+10 <= bleData.length;i+=10){
                var bleTireData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 1.22 + 0.01 * voltageTmp;
                }
                var airPressureTmp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var airPressure;
                if(airPressureTmp == 255){
                    airPressure = -999;
                }else{
                    airPressure = 1.572 * 2 * airPressureTmp;
                }
                var airTempTmp = bleData[i + 8] < 0 ? bleData[i + 8] + 256 : bleData[i + 8];
                var airTemp;
                if(airTempTmp == 255){
                    airTemp = -999;
                }else{
                    airTemp = airTempTmp - 55;
                }
                bleTireData.mac =mac
                bleTireData.voltage =voltage
                bleTireData.airPressure =airPressure
                bleTireData.airTemp =airTemp
                var alarm = bleData[i + 9];
                if(alarm == -1){
                    alarm = 0;
                }
                bleTireData.status = alarm
                bleDataList.push(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.bleMessageType = "sos"
            var bleAlertData = {}
            var macArray = ByteUtils.arrayOfRange(bleData, 2, 8);
            var mac = ByteUtils.bytes2HexString(macArray, 0);
            var voltageStr = ByteUtils.bytes2HexString(bleData,8).substring(0, 2);
            var voltage = parseFloat(voltageStr) / 10;

            var alertByte = bleData[9];
            var alert = alertByte == 0x01 ? "low_battery" : "sos";

            bleAlertData.setAlertType = alert
            bleAlertData.altitude = altitude
            bleAlertData.azimuth = azimuth
            bleAlertData.innerVoltage = voltage
            bleAlertData.isHistoryData = isHisData
            bleAlertData.latitude = latitude
            bleAlertData.latlngValid = latlngValid
            bleAlertData.longitude = longitude
            bleAlertData.mac = mac
            bleAlertData.speed = speedf
            bleAlertData.is_4g_lbs = is_4g_lbs
            bleAlertData.is_2g_lbs = is_2g_lbs
            bleAlertData.mcc_2g = mcc_2g
            bleAlertData.mnc_2g = mnc_2g
            bleAlertData.lac_2g_1 = lac_2g_1
            bleAlertData.ci_2g_1 = ci_2g_1
            bleAlertData.lac_2g_2 = lac_2g_2
            bleAlertData.ci_2g_2 = ci_2g_2
            bleAlertData.lac_2g_3 = lac_2g_3
            bleAlertData.ci_2g_3 = ci_2g_3
            bleAlertData.mcc_4g = mcc_4g
            bleAlertData.mnc_4g = mnc_4g
            bleAlertData.ci_4g = ci_4g
            bleAlertData.earfcn_4g_1 = earfcn_4g_1
            bleAlertData.pcid_4g_1 = pcid_4g_1
            bleAlertData.earfcn_4g_2 = earfcn_4g_2
            bleAlertData.pcid_4g_2 = pcid_4g_2
            bleDataList.push(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.bleMessageType = "driver"
            var bleDriverSignInData = {}
            var macArray = ByteUtils.arrayOfRange(bleData, 2,8);
            var mac = ByteUtils.bytes2HexString(macArray, 0);
            var voltageStr = ByteUtils.bytes2HexString(bleData,8).substring(0, 2);
            var voltage = parseFloat(voltageStr) / 10;

            var alertByte = bleData[9];
            var alert = alertByte == 0x01 ? "low_battery" : "driver"

            bleDriverSignInData.setAlert =alert
            bleDriverSignInData.altitude =altitude
            bleDriverSignInData.azimuth =azimuth
            bleDriverSignInData.voltage =voltage
            bleDriverSignInData.isHistoryData = isHisData
            bleDriverSignInData.latitude =latitude
            bleDriverSignInData.latlngValid =latlngValid
            bleDriverSignInData.longitude =longitude
            bleDriverSignInData.mac =mac
            bleDriverSignInData.speed = speedf
            bleDriverSignInData.is_4g_lbs =is_4g_lbs
            bleDriverSignInData.is_2g_lbs =is_2g_lbs
            bleDriverSignInData.mcc_2g =mcc_2g
            bleDriverSignInData.mnc_2g =mnc_2g
            bleDriverSignInData.lac_2g_1 =lac_2g_1
            bleDriverSignInData.ci_2g_1 =ci_2g_1
            bleDriverSignInData.lac_2g_2 =lac_2g_2
            bleDriverSignInData.ci_2g_2 =ci_2g_2
            bleDriverSignInData.lac_2g_3 =lac_2g_3
            bleDriverSignInData.ci_2g_3 =ci_2g_3
            bleDriverSignInData.mcc_4g =mcc_4g
            bleDriverSignInData.mnc_4g =mnc_4g
            bleDriverSignInData.ci_4g =ci_4g
            bleDriverSignInData.earfcn_4g_1 =earfcn_4g_1
            bleDriverSignInData.pcid_4g_1 =pcid_4g_1
            bleDriverSignInData.earfcn_4g_2 =earfcn_4g_2
            bleDriverSignInData.pcid_4g_2 =pcid_4g_2
            bleDataList.push(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.bleMessageType = "temp"
            for (var i = 2;i +15 <= bleData.length;i+=15){
                var bleTempData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                if(mac.startsWith("0000")){
                    mac = mac.substring(4,12);
                }
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6]
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var humidityTemp = ByteUtils.byteToShort(bleData,i+10);
                var humidity;
                if(humidityTemp == 65535){
                    humidity = -999;
                }else{
                    humidity = parseFloat((humidityTemp * 0.01).toFixed(2));
                }
                var lightTemp = ByteUtils.byteToShort(bleData,i+12);
                var lightIntensity ;
                if(lightTemp == 65535){
                    lightIntensity = -999;
                }else{
                    lightIntensity = lightTemp & 0x0001;
                }
                var rssiTemp = bleData[i + 14] < 0 ? bleData[i + 14] + 256 : bleData[i + 14];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleTempData.rssi =rssi
                bleTempData.mac =mac
                bleTempData.lightIntensity =lightIntensity
                bleTempData.humidity =humidity
                bleTempData.voltage =voltage
                bleTempData.batteryPercent =batteryPercent
                bleTempData.temp =temperature
                bleDataList.push(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.bleMessageType = "door"
            for (var i = 2;i+12 <= bleData.length;i+=12){
                var bleDoorData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ?  bleData[i + 6] + 256 :  bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var doorStatus = bleData[i+10] < 0 ? bleData[i+10] + 256 : bleData[i+10];
                var online = 1;
                if(doorStatus == 255){
                    doorStatus = -999;
                    online = 0;
                }

                var rssiTemp = bleData[i + 11] < 0 ? bleData[i + 11] + 256 : bleData[i + 11];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleDoorData.rssi =rssi
                bleDoorData.mac =mac
                bleDoorData.online =online
                bleDoorData.doorStatus =doorStatus
                bleDoorData.voltage =voltage
                bleDoorData.batteryPercent =batteryPercent
                bleDoorData.temp =temperature
                bleDataList.push(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.bleMessageType = "ctrl"
            for (var i = 2;i+12 <= bleData.length;i+=12){
                var bleCtrlData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed());
                }
                var batteryPercentTemp = bleData[i + 7] < 0 ? bleData[i + 7] + 256 : bleData[i + 7];
                var batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+8);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var CtrlStatus =bleData[i+10] < 0 ? bleData[i+10] + 256 : bleData[i+10];
                var online = 1;
                if(CtrlStatus == 255){
                    CtrlStatus = -999;
                    online = 0;
                }

                var rssiTemp = bleData[i + 11] < 0 ? bleData[i + 11] + 256 : bleData[i + 11];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleCtrlData.setRssi = rssi
                bleCtrlData.setMac = mac
                bleCtrlData.setOnline = online
                bleCtrlData.setCtrlStatus = CtrlStatus
                bleCtrlData.voltage = voltage
                bleCtrlData.setBatteryPercent = batteryPercent
                bleCtrlData.temp = temperature
                bleDataList.push(bleCtrlData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x07){
            bluetoothPeripheralDataMessage.bleMessageType = "fuel"
            for (var i = 2;i+15 <= bleData.length;i+=15){
                var bleFuelData = {}
                var macArray = ByteUtils.arrayOfRange(bleData, i + 0, i + 6);
                var mac = ByteUtils.bytes2HexString(macArray, 0);
                var voltageTmp = bleData[i + 6] < 0 ? bleData[i + 6] + 256 : bleData[i + 6];
                var voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = parseFloat((2 + 0.01 * voltageTmp).toFixed(2));
                }
                var valueTemp = ByteUtils.byteToShort(bleData,i+7);
                var value;
                if(valueTemp == 65535){
                    value = -999;
                }else{
                    value = valueTemp;
                }
                var temperatureTemp = ByteUtils.byteToShort(bleData,i+9);
                var tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                var temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = parseFloat(((temperatureTemp & 0x7fff) * 0.01 * tempPositive).toFixed(2));
                }
                var status =bleData[i+13] < 0 ? bleData[i+13] + 256 : bleData[i+13];
                var online = 1;
                if(status == 255){
                    status = 0;
                    online = 0;
                }
                var rssiTemp = bleData[i + 14] < 0 ? bleData[i + 14] + 256 : bleData[i + 14];
                var rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleFuelData.rssi = rssi
                bleFuelData.mac = mac
                bleFuelData.online = online
                bleFuelData.status = status
                bleFuelData.voltage = voltage
                bleFuelData.value = value
                bleFuelData.temp = temperature
                bleDataList.push(bleFuelData);
            }
        }
        bluetoothPeripheralDataMessage.bleDataList = bleDataList
        return bluetoothPeripheralDataMessage;
    },
    parseSecondDataMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var message = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"location",
        }
        var data = ByteUtils.arrayOfRange(bytes, 15, bytes.length);
        var samplingIntervalAccOn = ByteUtils.byteToShort(data, 0);
        var samplingIntervalAccOff = ByteUtils.byteToShort(data, 2);
        var angleCompensation = (data[4] & 0xff);
        var distanceCompensation = ByteUtils.byteToShort(data, 5);
        var limit = ByteUtils.byteToShort(data, 7);
        var speedLimit = ((0x7F80 & limit)) >> 7;
        if ((0x8000 & limit) != 0) {
            speedLimit *= 1.609344;
        }
        var networkSignal = limit & 0x7F;
        var isGpsWorking = (data[9] & 0x20) == 0x00;
        var isHistoryData = (data[9] & 0x80) != 0x00;
        var satelliteNumber = data[9] & 0x1F;
        var gSensorSensitivity = (data[10] & 0xF0) >> 4;
        var isManagerConfigured1 = (data[10] & 0x01) != 0x00;
        var isManagerConfigured2 = (data[10] & 0x02) != 0x00;
        var isManagerConfigured3 = (data[10] & 0x04) != 0x00;
        var isManagerConfigured4 = (data[10] & 0x08) != 0x00;
        var antitheftedStatus = (data[11] & 0x10) != 0x00 ? 1 : 0;
        var heartbeatInterval = data[12] & 0x00FF;
        var relayStatus = data[13] & 0x3F;
        var rlyMode =  data[13] & 0xCF;
        var smsLanguageType = data[13] & 0xF;
        var isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);

        var dragThreshold = ByteUtils.byteToShort(data, 14);

        var iop =   ByteUtils.byteToShort(data, 16);
        var iopIgnition = (iop & this.MASK_IGNITION) == this.MASK_IGNITION;
        var iopPowerCutOff = (iop & this.MASK_POWER_CUT) == this.MASK_POWER_CUT;
        var iopACOn = (iop & this.MASK_AC) == this.MASK_AC;
        var input1 = (iop & 0x100) == 0x100 ? 1 : 0;
        var input2 = (iop & 0x2000) == 0x2000 ? 1 : 0;
        var input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
        var input4 = (iop & 0x800) == 0x800 ? 1 : 0;
        var input5 = (iop & 0x400) == 0x400 ? 1 : 0;
        var input6 = (iop & 0x200) == 0x200 ? 1 : 0;
        var output1 = (data[18] & 0x20) == 0x20 ? 1 : 0;
        var output2 = (data[18] & 0x10) == 0x10 ? 1 : 0;
        var output3 = (data[18] & 0x8) == 0x8 ? 1 : 0;
        var output12V = (data[18] & 0x20) == 0x20 ;
        var outputVout = (data[18] & 0x40) == 0x40;
        var accdetSettingStatus = (iop & 0x1) ==  0x1  ? 1 : 0;
        var str = ByteUtils.bytes2HexString(data, 20);
        var analoginput = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput  = parseFloat( str.substring(0, 2) + "." + str.substring(2, 4))
            } catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput = -999;
        }

        str = ByteUtils.bytes2HexString(data, 22);
        var analoginput2 = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput2  = parseFloat( str.substring(0, 2) + "." + str.substring(2, 4))
            }catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput2 = -999;
        }

        str = ByteUtils.bytes2HexString(data, 24);
        var analoginput3 = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput3  = parseFloat( str.substring(0, 2) + "." + str.substring(2, 4))
            }catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput3 = -999;
        }

        str = ByteUtils.bytes2HexString(data, 26);
        var analoginput4 = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput4  = parseFloat( str.substring(0, 2) + "." + str.substring(2, 4))
            }catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput4 = -999;
        }

        str = ByteUtils.bytes2HexString(data, 28);
        var analoginput5 = 0;
        if (!str.toLowerCase() == "ffff"){
            try {
                analoginput5  = parseFloat( str.substring(0, 2) + "." + str.substring(2, 4))
            }catch (e){
//            e.printStackTrace();
            }
        }else{
            analoginput5 = -999;
        }

        var alarmByte = data[30];
        var originalAlarmCode = alarmByte;
        var externalPowerReduceValue = (data[31] & 0x11);
        var isSendSmsAlarmToManagerPhone = (data[31] & 0x20) == 0x20;
        var isSendSmsAlarmWhenDigitalInput2Change = (data[31] & 0x10) == 0x10;
        var jammerDetectionStatus = (data[31] & 0xC);
        var isAlarmData = bytes[2] == 0x14;
        var mileage =  ByteUtils.byteToLong(data, 32);
        var batteryBytes = [data[36]]
        var batteryStr = ByteUtils.bytes2HexString(batteryBytes, 0);
        var batteryCharge = parseInt(batteryStr)
        if (0 == batteryCharge) {
            batteryCharge = 100;
        }
        var gmt0 = ByteUtils.getGTM0Date(data, 37);
        var latlngValid = (data[9] & 0x40) != 0x00;
        var latlngData = ByteUtils.arrayOfRange(data, 43, 59);
        if (ByteUtils.arrayEquals(latlngData,this.latlngInvalidData)){
            latlngValid = false;
        }
        var altitude = latlngValid? ByteUtils.bytes2Float(data, 43) : 0.0;
        var latitude = latlngValid ? ByteUtils.bytes2Float(data, 51) : 0.0;
        var longitude = latlngValid ? ByteUtils.bytes2Float(data, 47) : 0.0;
        var speedf = 0.0;
        if (latlngValid){
            try{
                if (latlngValid) {
                    var bytesSpeed = ByteUtils.arrayOfRange(data, 55, 57);
                    var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
                    speedf = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
                }
            }catch (e){

            }
        }
        var azimuth = latlngValid ? ByteUtils.byteToShort(data, 57) : 0;
        var is_4g_lbs = false;
        var mcc_4g = null;
        var mnc_4g = null;
        var ci_4g = null;
        var earfcn_4g_1 = null;
        var pcid_4g_1 = null;
        var earfcn_4g_2 = null;
        var pcid_4g_2 = null;
        var is_2g_lbs = false;
        var mcc_2g = null;
        var mnc_2g = null;
        var lac_2g_1 = null;
        var ci_2g_1 = null;
        var lac_2g_2 = null;
        var ci_2g_2 = null;
        var lac_2g_3 = null;
        var ci_2g_3 = null;
        if (!latlngValid){
            var lbsByte = data[43];
            if ((lbsByte & 0x8) == 0x8){
                is_2g_lbs = true;
            }else{
                is_4g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(data,43);
            mnc_2g = ByteUtils.byteToShort(data,45);
            lac_2g_1 = ByteUtils.byteToShort(data,47);
            ci_2g_1 = ByteUtils.byteToShort(data,49);
            lac_2g_2 = ByteUtils.byteToShort(data,51);
            ci_2g_2 = ByteUtils.byteToShort(data,53);
            lac_2g_3 = ByteUtils.byteToShort(data,55);
            ci_2g_3 = ByteUtils.byteToShort(data,57);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(data,43);
            mnc_4g = ByteUtils.byteToShort(data,45);
            ci_4g = ByteUtils.byteToLong(data, 47);
            earfcn_4g_1 = ByteUtils.byteToShort(data, 51);
            pcid_4g_1 = ByteUtils.byteToShort(data, 53);
            earfcn_4g_2 = ByteUtils.byteToShort(data, 55);
            pcid_4g_2 = ByteUtils.byteToShort(data,57);
        }
        var batteryVoltage = 0;
        var batteryVoltageBytes = ByteUtils.arrayOfRange(data, 59, 61);
        var batteryVoltageStr = ByteUtils.bytes2HexString(batteryVoltageBytes, 0);
        batteryVoltage = parseFloat(batteryVoltageStr) / 100.0
        var externalPowerVoltage = 0 ;
        var externalPowerVoltageBytes = ByteUtils.arrayOfRange(data, 61, 63);
        var externalPowerVoltageStr = ByteUtils.bytes2HexString(externalPowerVoltageBytes, 0);
        externalPowerVoltage = parseFloat(externalPowerVoltageStr) / 100.0
        var rpm = 0;
        var rpmBytes = ByteUtils.arrayOfRange(data, 63, 65);
        rpm = ByteUtils.byteToShort(rpmBytes,0);
        rpm = ByteUtils.byteToShort(rpmBytes,0);
        if (rpm == 32768){
            rpm = -999;
        }
        var isSmartUploadSupport = (data[65] & 0x8) == 0x8;
        var supportChangeBattery =  (data[66] & 0x8) == 0x8;
        var deviceTemp = -999;
        if (data.length >= 69 && data[68] != 0xff){
            deviceTemp = (data[68] & 0x7F) * ((data[68] & 0x80) == 0x80 ? -1 : 1);
        }
        message.isAlarmData = isAlarmData
        message.protocolHeadType = bytes[2]
        message.networkSignal = networkSignal
        message.samplingIntervalAccOn = samplingIntervalAccOn
        message.samplingIntervalAccOff = samplingIntervalAccOff
        message.angleCompensation = angleCompensation
        message.distanceCompensation = distanceCompensation
        message.overSpeedLimit = speedLimit
        message.isGpsWorking = isGpsWorking
        message.isHistoryData = isHistoryData
        message.satelliteNumber = satelliteNumber
        message.gSensorSensitivity = gSensorSensitivity
        message.isManagerConfigured1 = isManagerConfigured1
        message.isManagerConfigured2 = isManagerConfigured2
        message.isManagerConfigured3 = isManagerConfigured3
        message.isManagerConfigured4 = isManagerConfigured4
        message.antitheftedStatus = antitheftedStatus
        message.heartbeatInterval = heartbeatInterval
        message.relayStatus = relayStatus
        message.rlyMode = rlyMode
        message.smsLanguageType = smsLanguageType
        message.isRelayWaiting = isRelayWaiting
        message.dragThreshold = dragThreshold
        message.iop = iop
        message.iopIgnition = iopIgnition
        message.iopPowerCutOff = iopPowerCutOff
        message.iopACOn = iopACOn
        message.analoginput = analoginput
        message.analoginput2 = analoginput2
        message.originalAlarmCode = originalAlarmCode
        message.mileage = mileage
        message.output12V = output12V
        message.output1 = output1
        message.output2 = output2
        message.output3 = output3
        message.input1 = input1
        message.input2 = input2
        message.input3 = input3
        message.input4 = input4
        message.outputVout = outputVout
        message.analoginput3 = analoginput3
        message.accdetSettingStatus = accdetSettingStatus
        message.rpm = rpm
        message.deviceTemp = deviceTemp
        message.batteryCharge = batteryCharge
        message.date =  gmt0
        message.latlngValid = latlngValid
        message.altitude = altitude
        message.latitude = latitude
        message.longitude = longitude
        if (message.latlngValid) {
            message.speed = speedf
        } else {
            message.speed = 0
        }
        message.azimuth =azimuth
        message.externalPowerVoltage =externalPowerVoltage
        message.batteryVoltage =batteryVoltage
        message.input5 =input5
        message.input6 =input6
        message.analoginput4 =analoginput4
        message.analoginput5 =analoginput5
        message.isSmartUploadSupport =isSmartUploadSupport
        message.supportChangeBattery =supportChangeBattery
        message.is_4g_lbs =is_4g_lbs
        message.is_2g_lbs =is_2g_lbs
        message.mcc_2g =mcc_2g
        message.mnc_2g =mnc_2g
        message.lac_2g_1 =lac_2g_1
        message.ci_2g_1 =ci_2g_1
        message.lac_2g_2 =lac_2g_2
        message.ci_2g_2 =ci_2g_2
        message.lac_2g_3 =lac_2g_3
        message.ci_2g_3 =ci_2g_3
        message.mcc_4g =mcc_4g
        message.mnc_4g =mnc_4g
        message.ci_4g =ci_4g
        message.earfcn_4g_1 =earfcn_4g_1
        message.pcid_4g_1 =pcid_4g_1
        message.earfcn_4g_2 =earfcn_4g_2
        message.pcid_4g_2 =pcid_4g_2
        message.externalPowerReduceValue =externalPowerReduceValue
        message.isSendSmsAlarmWhenDigitalInput2Change =isSendSmsAlarmWhenDigitalInput2Change
        message.isSendSmsAlarmToManagerPhone =isSendSmsAlarmToManagerPhone
        message.jammerDetectionStatus =jammerDetectionStatus
        return message;
    },
    parseInteractMessage:function (bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var data = ByteUtils.arrayOfRange(bytes,16,bytes.length)
        var content = ByteUtils.charArrayToStr(data,"ascii")
        var configMessage = {
            serialNo:serialNo,
            messageType:"config",
            imei:imei,
            srcBytes:bytes,
            content:content,
        }
        return configMessage
    },
}

module.exports = Decoder;
