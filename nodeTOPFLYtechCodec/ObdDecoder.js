var CryptoTool = require("./CryptoTool")
var ByteUtils = require("./ByteUtils")
const TopflytechByteBuf = require("./TopflytechByteBuf")

var ObdDecoder = {
    HEADER_LENGTH:3,
    SIGNUP:[0x26, 0x26, 0x01],
    DATA:[0x26, 0x26, 0x02],
    HEARTBEAT:[0x26, 0x26, 0x03],
    ALARM:[0x26, 0x26, 0x04],
    CONFIG:[0x26, 0x26, 0x81],
    GPS_DRIVER_BEHAVIOR:[0x26, 0x26, 0x05],
    ACCELERATION_DRIVER_BEHAVIOR:[0x26, 0x26, 0x06],
    ACCELERATION_ALARM:[0x26, 0x26, 0x07],
    BLUETOOTH_MAC:[0x25, 0x25, 0x08],
    OBD_DATA:[0x26,0x26,0x09],
    BLUETOOTH_DATA:[0x26, 0x26, 0x10],
    NETWORK_INFO_DATA:[0x26, 0x26, 0x11],
    BLUETOOTH_SECOND_DATA:[0x26, 0x26, 0x12],
    latlngInvalidData:[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,
        0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF,0xFF],
    obdHead:[0x55,0xAA],
    LOCATION_DATA_WITH_SENSOR:[0x26, 0x26, 0x16],
    LOCATION_ALARM_WITH_SENSOR:[0x26, 0x26, 0x18],
    WIFI_DATA :  [0x26, 0x26, 0x15],
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
            || ByteUtils.arrayEquals(this.GPS_DRIVER_BEHAVIOR, bytes)
            || ByteUtils.arrayEquals(this.ACCELERATION_DRIVER_BEHAVIOR, bytes)
            || ByteUtils.arrayEquals(this.ACCELERATION_ALARM, bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_MAC, bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_DATA, bytes)
            || ByteUtils.arrayEquals(this.NETWORK_INFO_DATA,bytes)
            || ByteUtils.arrayEquals(this.BLUETOOTH_SECOND_DATA,bytes)
            || ByteUtils.arrayEquals(this.OBD_DATA,bytes)
            || ByteUtils.arrayEquals(this.LOCATION_DATA_WITH_SENSOR,bytes)
            || ByteUtils.arrayEquals(this.LOCATION_ALARM_WITH_SENSOR,bytes)
            || ByteUtils.arrayEquals(this.WIFI_DATA,bytes)
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
            && ((bytes[0] == 0x26 && bytes[1] == 0x26))) {
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
                    var obdMessage = this.parseObdMessage(bytes);
                    return obdMessage;
                case 0x10:
                    var bluetoothPeripheralDataMessage = this.parseBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataMessage;
                case 0x11:
                    var networkInfoMessage = this.parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case 0x12:
                    var bluetoothPeripheralDataSecondMessage = this.parseSecondBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataSecondMessage;
                case 0x15:
                        var  wifiMessage = this.parseWifiMessage(bytes);
                        return wifiMessage;
                case 0x81:
                    var message =  this.parseInteractMessage(bytes);
                    return message;
                default:
                    return null;
            }
        }
        return null;
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
        if (strSp.toLowerCase() !== "ffff"){
            startSpeed = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
        }
        gpsDriverBehaviorMessage.startSpeed = startSpeed;
        var azimuth = ByteUtils.byteToShort(bytes, 36);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.startAzimuth = azimuth;

        var startRpm = ByteUtils.byteToShort(bytes,38);
        if (startRpm == 65535){
            startRpm = -999;
        }
        gpsDriverBehaviorMessage.startRpm = startRpm;

        var endDate = ByteUtils.getGTM0Date(bytes, 40);
        gpsDriverBehaviorMessage.endDate = endDate;
        var endAltitude = ByteUtils.bytes2Float(bytes, 46);
        gpsDriverBehaviorMessage.endAltitude = endAltitude

        var endLongitude = ByteUtils.bytes2Float(bytes, 50);
        gpsDriverBehaviorMessage.endLongitude = endLongitude

        var endLatitude = ByteUtils.bytes2Float(bytes, 54);
        gpsDriverBehaviorMessage.endLatitude = endLatitude

        bytesSpeed = ByteUtils.arrayOfRange(bytes, 58, 60);
        var endSpeed = 0;
        strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if (strSp.toLowerCase() !== "ffff"){
            endSpeed = parseFloat(strSp.substring(0, 3) + "." + strSp.substring(3, strSp.length));
        }
        gpsDriverBehaviorMessage.endSpeed = endSpeed
        azimuth = ByteUtils.byteToShort(bytes, 60);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.endAzimuth = azimuth
        var endRpm = ByteUtils.byteToShort(bytes,62);
        if (endRpm == 65535){
            endRpm = -999;
        }
        gpsDriverBehaviorMessage.endRpm = endRpm;
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
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
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
            mcc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 12) & 0x7FFF;
            mnc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 14);
            ci_4g = ByteUtils.byteToLong(bytes, curParseIndex + 16);
            earfcn_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 20);
            pcid_4g_1 = ByteUtils.byteToShort(bytes, curParseIndex + 22);
            earfcn_4g_2 = ByteUtils.byteToShort(bytes, curParseIndex + 24);
            pcid_4g_2 = ByteUtils.byteToShort(bytes,curParseIndex + 26);
        }
        var rpm = ByteUtils.byteToShort(bytes, curParseIndex + 28);
        if(rpm == 65535){
            rpm = -999;
        }
        acceleration.rpm=rpm;
        acceleration.is_4g_lbs = is_4g_lbs
        acceleration.is_2g_lbs = is_2g_lbs
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
        var dataCount = dataLength / 30;
        var beginIndex = 16;
        var accidentAccelerationList = []
        for (var i = 0 ; i < dataCount;i++){
            var curParseIndex = beginIndex + i * 30;
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
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
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
            mcc_4g = ByteUtils.byteToShort(bytes,curParseIndex + 12) & 0x7FFF;
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
        var rpm = ByteUtils.byteToShort(bytes, curParseIndex + 28)
        if(rpm == 65535){
            rpm = -999;
        }
        acceleration.rpm=rpm
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
            var bytesSpeed = ByteUtils.arrayOfRange(bleData, 23, 25);
            var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
            if(strSp.indexOf("f") != -1){
                speedf = -1;
            }else {
                speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
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
                if ((lbsByte & 0x80) == 0x80){
                    is_4g_lbs = true;
                }else{
                    is_2g_lbs = true;
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
                mcc_4g = ByteUtils.byteToShort(bleData,11) & 0x7FFF;
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
            var bytesSpeed = ByteUtils.arrayOfRange(bleData, 23, 25);
            var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
            if(strSp.indexOf("f") != -1){
                speedf = -1;
            }else {
                speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
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
                if ((lbsByte & 0x80) == 0x80){
                    is_4g_lbs = true;
                }else{
                    is_2g_lbs = true;
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
                mcc_4g = ByteUtils.byteToShort(bleData,11) & 0x7FFF;
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
        var bytesSpeed = ByteUtils.arrayOfRange(bytes, 35, 37);
        var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if(strSp.indexOf("f") != -1){
            speedf = -999
        }else {
            speedf = parseFloat(strSp.substring(0, 3) +"."+ strSp.substring(3, strSp.length));
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
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
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
            mcc_4g = ByteUtils.byteToShort(bytes,23) & 0x7FFF;
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
    parseLoginMessage:function(bytes){
        if (bytes.length == 25){
            var serialNo = ByteUtils.byteToShort(bytes,5);
            var imei = ByteUtils.IMEI.decode(bytes,7)
            var str = ByteUtils.bytes2HexString(bytes,15)
            if (20 == str.length) {
                var software = "V" + ((bytes[15] & 0xf0) >> 4) + "." + (bytes[15] & 0xf) + "." + ((bytes[16] & 0xf0) >> 4)
                var firmware = "V" + (bytes[16] & 0xf) + "." + ((bytes[17] & 0xf0) >> 4) + "." + (bytes[17] & 0xf)
                var platform = str.substring(6, 10)
                var hardware = ((bytes[20] & 0xf0) >> 4) + "." + (bytes[20] & 0xf)
                var obdV1 = bytes[21];
                var obdV2 = bytes[22];
                var obdV3 = bytes[23];
                if(obdV1 < 0){
                    obdV1 += 256;
                }
                if(obdV2 < 0){
                    obdV2 += 256;
                }
                if(obdV3 < 0){
                    obdV3 += 256;
                }
                var obdSoftware = "V" + obdV1 + "." + obdV2 + "." + obdV3
                var obdHardware = ((bytes[24] & 0xf0) >> 4) + "." + (bytes[24] & 0xf)
                var signInMessage = {
                    serialNo:serialNo,
                    imei:imei,
                    software:software,
                    firmware:firmware,
                    platform:platform,
                    hardware:hardware,
                    srcBytes:bytes,
                    obdSoftware:obdSoftware,
                    obdHardware:obdHardware,
                    messageType:"signIn",
                }
                return signInMessage
            }
            return null
        }else if(bytes.length == 27){
            var serialNo = ByteUtils.byteToShort(bytes,5);
            var imei = ByteUtils.IMEI.decode(bytes,7)
            var software = "V" + (bytes[15] & 0xf) + "." + ((bytes[16] & 0xf0) >> 4) + "." + (bytes[16] & 0xf)
            var str = ByteUtils.bytes2HexString(bytes, 17);
            var platform = str.substring(0, 6);
            var firmware = "V" + str.substring(6, 7) + "." + str.substring(7, 8) + "." + str.substring(8, 9) + "." + str.substring(9, 10)
            var hardware = str.substring(10, 11) + "." + str.substring(11, 12)
            var obdV1 = bytes[23];
            var obdV2 = bytes[24];
            var obdV3 = bytes[25];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            var obdSoftware = "V" + obdV1 + "." + obdV2 + "." + obdV3
            var obdHardware = ((bytes[26] & 0xf0) >> 4) + "." + (bytes[26] & 0xf)
            var signInMessage = {
                serialNo:serialNo,
                imei:imei,
                software:software,
                firmware:firmware,
                platform:platform,
                hardware:hardware,
                srcBytes:bytes,
                obdSoftware:obdSoftware,
                obdHardware:obdHardware,
                messageType:"signIn",
            }
            return signInMessage
        }else if(bytes.length >= 35){
            var serialNo = ByteUtils.byteToShort(bytes,5);
            var imei = ByteUtils.IMEI.decode(bytes,7)
            var model = BytesUtils.bytes2Short(bytes,15);
            var str = BytesUtils.bytes2HexString(bytes, 17);
            var software = "V" + str.substring(0, 1) + "." + str.substring(1, 2) + "." + str.substring(2, 3) + "." + str.substring(3, 4)
            var platform = str.substring(4, 10);
            var firmware = "V" + str.substring(10, 11) + "." + str.substring(11, 12)+ "." + str.substring(12, 13)+ "." + str.substring(13, 14)
            var hardware = "V" + str.substring(14, 15) + "." + str.substring(15, 16)
            var obdV1 = bytes[25];
            var obdV2 = bytes[26];
            var obdV3 = bytes[27];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            var obdSoftware = "V" + obdV1 + "." + obdV2 + "." + obdV3
            obdV1 = bytes[28];
            obdV2 = bytes[29];
            obdV3 = bytes[30];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            var obdBootVersion = "V" + obdV1 + "." + obdV2 + "." + obdV3
            obdV1 = bytes[31];
            obdV2 = bytes[32];
            obdV3 = bytes[33];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            var obdDataVersion = "V" + obdV1 + "." + obdV2 + "." + obdV3
            var obdHardware = "V" + ((bytes[34] & 0xf0) >> 4) + "." + (bytes[34] & 0xf)
            var signInMessage = {
                serialNo:serialNo,
                imei:imei,
                software:software,
                firmware:firmware,
                platform:platform,
                hardware:hardware,
                srcBytes:bytes,
                obdSoftware:obdSoftware,
                obdHardware:obdHardware,
                messageType:"signIn",
                model:model,
                obdDataVersion:obdBootVersion,
                obdBootVersion:obdBootVersion,
            }
            return signInMessage;
        }
        return null
    },
    parseDataMessage:function(bytes){
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
        var speed = ((0x7F80 & limit)) >> 7;
        if ((0x8000 & limit) != 0) {
            speed *= 1.609344;
        }
        var networkSignal = limit & 0x7F;
        var isGpsWorking = (data[9] & 0x20) == 0x00;
        var isHistoryData = (data[9] & 0x80) != 0x00;
        var satelliteCount = data[9] & 0x1F;
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
        var ignitionSource = data[13] & 0xf;

        var isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);
        var dragThreshold = ByteUtils.byteToShort(data, 14);
        var iop = ByteUtils.byteToShort(data, 16);
        var iopIgnition = (iop & this.MASK_IGNITION) == this.MASK_IGNITION;
        var iopPowerCutOff = (iop & this.MASK_POWER_CUT) == this.MASK_POWER_CUT;
        var iopACOn = (iop & this.MASK_AC) == this.MASK_AC;
        var iopRelay =(iop & this.MASK_RELAY) == this.MASK_RELAY;
        var input1 = iopIgnition ? 1 : 0;
        var input2 = iopACOn ? 1 : 0;
        var output1 = (iop & 0x0400) == 0x0400 ? 1 : 0;
        var speakerStatus = (iop & 0x40) ==  0x40  ? 1 : 0;
        var rs232PowerOf5V = (iop & 0x20) ==  0x20  ? 1 : 0;
        var hasThirdPartyObd = (iop & 0x10) ==  0x10  ? 1 : 0;
        var exPowerConsumpStatus = 0;
        if ((iop & 0x03) == 0x01){
            exPowerConsumpStatus = 2;
        }else if ((iop & 0x03) == 0x02){
            exPowerConsumpStatus = 1;
        }else{
            exPowerConsumpStatus = 0;
        }
        var alarmByte = data[18];
        var originalAlarmCode = alarmByte;
        var externalPowerReduceValue = (data[19] & 0x11);
        var isSendSmsAlarmToManagerPhone = (data[19] & 0x20) == 0x20;
        var isSendSmsAlarmWhenDigitalInput2Change = (data[19] & 0x10) == 0x10;
        var jammerDetectionStatus = (data[19] & 0xC);
        var mileageSource  = (data[19] & 0x02) == 0x02 ? "GPS" : "ECU";
        var isAlarmData = bytes[2] == 0x04  || bytes[2] == 0x18;
        var mileage = ByteUtils.byteToLong(data, 20);
        var batteryBytes = [data[24]]
        var batteryStr = ByteUtils.bytes2HexString(batteryBytes, 0);
        var batteryCharge = parseInt(batteryStr)
        var gmt0 = ByteUtils.getGTM0Date(data, 25);
        var latlngValid = (data[9] & 0x40) != 0x00;
        var latlngData = ByteUtils.arrayOfRange(data,31,47);
        if (ByteUtils.arrayEquals(latlngData,this.latlngInvalidData)){
            latlngValid = false;
        }
        var altitude = latlngValid? ByteUtils.bytes2Float(data, 31) : 0.0;
        var latitude = latlngValid ? ByteUtils.bytes2Float(data, 39) : 0.0;
        var longitude = latlngValid ? ByteUtils.bytes2Float(data, 35) : 0.0;
        var azimuth = latlngValid ? ByteUtils.byteToShort(data, 45) : 0;
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
            var lbsByte = data[31];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = ByteUtils.byteToShort(data,31);
            mnc_2g = ByteUtils.byteToShort(data,33);
            lac_2g_1 = ByteUtils.byteToShort(data,35);
            ci_2g_1 = ByteUtils.byteToShort(data,37);
            lac_2g_2 = ByteUtils.byteToShort(data,39);
            ci_2g_2 = ByteUtils.byteToShort(data,41);
            lac_2g_3 = ByteUtils.byteToShort(data,43);
            ci_2g_3 = ByteUtils.byteToShort(data,45);
        }
        if (is_4g_lbs){
            mcc_4g = ByteUtils.byteToShort(data,31) & 0x7FFF;
            mnc_4g = ByteUtils.byteToShort(data,33);
            ci_4g = ByteUtils.byteToLong(data, 35);
            earfcn_4g_1 = ByteUtils.byteToShort(data, 39);
            pcid_4g_1 = ByteUtils.byteToShort(data, 41);
            earfcn_4g_2 = ByteUtils.byteToShort(data, 43);
            pcid_4g_2 = ByteUtils.byteToShort(data,45);
        }
        var externalPowerVoltage = 0;
        var externalPowerVoltageBytes = ByteUtils.arrayOfRange(data, 47, 49);
        var externalPowerVoltageStr = ByteUtils.bytes2HexString(externalPowerVoltageBytes, 0);
        externalPowerVoltage = parseFloat(externalPowerVoltageStr) / 100.0;
        var speedf = 0.0;
        var bytesSpeed = ByteUtils.arrayOfRange(data, 49, 51);
        var strSp = ByteUtils.bytes2HexString(bytesSpeed, 0);
        if(strSp.indexOf("f") != -1){
            speedf = -1;
        }else {
            speedf = parseFloat(strSp.substring(0, 3) + "."+ strSp.substring(3, strSp.length));
        }

        var accumulatingFuelConsumption = ByteUtils.byteToLong(data, 51);
        if(accumulatingFuelConsumption == 4294967295){
            accumulatingFuelConsumption = -999;
        }
        var instantFuelConsumption =  ByteUtils.byteToLong(data, 55);
        if(instantFuelConsumption == 4294967295){
            instantFuelConsumption = -999;
        }
        var rpm = ByteUtils.byteToShort(data, 59);
        if(rpm == 65535){
            rpm = -999;
        }
        var airInput = data[61] < 0 ? data[61] + 256 : data[61];
        if(airInput == 255){
            airInput = -999;
        }
        var airPressure = data[62] < 0 ? data[62] + 256 : data[62];
        if(airPressure == 255){
            airPressure = -999;
        }
        var coolingFluidTemp = data[63] < 0 ? data[63] + 256 : data[63];
        if(coolingFluidTemp == 255){
            coolingFluidTemp = -999;
        }else{
            coolingFluidTemp = coolingFluidTemp - 40;
        }
        var airInflowTemp = data[64] < 0 ? data[64] + 256 : data[64];
        if(airInflowTemp == 255){
            airInflowTemp = -999;
        }else {
            airInflowTemp = airInflowTemp - 40;
        }
        var engineLoad = data[65] < 0 ? data[65] + 256 : data[65];
        if(engineLoad == 255){
            engineLoad = -999;
        }
        var throttlePosition = data[66] < 0 ? data[66] + 256 : data[66];
        if(throttlePosition == 255){
            throttlePosition = -999;
        }
        var remainFuelRate = data[67] & 0x7f;
        if(data[67] == 255){
            remainFuelRate = -999;
        }
        var remainFuelUnit = (data[67] & 0x80) == 0x80 ? "L" : "%";
        var protocolHead = bytes[2];
        message.protocolHeadType = protocolHead
        if ((protocolHead == 0x16 || protocolHead == 0x18) && data.length >= 80){

            var axisX = ByteUtils.byteToShort(data,68);
            if (axisX > 32767){
                axisX = axisX - 65536
            }
            var axisY = ByteUtils.byteToShort(data,70);
            if (axisY > 32767){
                axisY = axisY - 65536
            }
            var axisZ = ByteUtils.byteToShort(data,72);
            if (axisZ > 32767){
                axisZ = axisZ - 65536
            }
            var gyroscopeAxisX = ByteUtils.byteToShort(data,74);
            if (gyroscopeAxisX > 32767){
                gyroscopeAxisX = gyroscopeAxisX - 65536
            }
            var gyroscopeAxisY = ByteUtils.byteToShort(data,76);
            if (gyroscopeAxisY > 32767){
                gyroscopeAxisY = gyroscopeAxisY - 65536
            }
            var gyroscopeAxisZ = ByteUtils.byteToShort(data,78);
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
        message.isAlarmData = isAlarmData
        message.protocolHeadType = protocolHead
        message.output1 =output1
        message.input1 =input1
        message.input2 =input2
        message.networkSignal =networkSignal
        message.samplingIntervalAccOn =samplingIntervalAccOn
        message.samplingIntervalAccOff =samplingIntervalAccOff
        message.angleCompensation =angleCompensation
        message.distanceCompensation =distanceCompensation
        message.overSpeedLimit=speed
        message.isGpsWorking =isGpsWorking
        message.isHistoryData =isHistoryData
        message.satelliteCount =satelliteCount
        message.gSensorSensitivity =gSensorSensitivity
        message.isManagerConfigured1 =isManagerConfigured1
        message.isManagerConfigured2 =isManagerConfigured2
        message.isManagerConfigured3 =isManagerConfigured3
        message.isManagerConfigured4 =isManagerConfigured4
        message.antitheftedStatus =antitheftedStatus
        message.heartbeatInterval =heartbeatInterval
        message.relayStatus =relayStatus
        message.rlyMode =rlyMode
        message.smsLanguageType =smsLanguageType
        message.isRelayWaiting =isRelayWaiting
        message.dragThreshold =dragThreshold
        message.iop =iop
        message.iopIgnition =iopIgnition
        message.externalPowerReduceValue =externalPowerReduceValue
        message.iopPowerCutOff =iopPowerCutOff
        message.iopACOn =iopACOn
        message.originalAlarmCode =originalAlarmCode
        message.speakerStatus =speakerStatus
        message.rs232PowerOf5V =rs232PowerOf5V
        message.mileage =mileage
        message.batteryCharge=batteryCharge
        message.date = gmt0
        message.latlngValid = latlngValid
        message.altitude = altitude
        message.latitude = latitude
        message.longitude = longitude
        message.speed = speedf
        message.azimuth = azimuth
        message.externalPowerVoltage = externalPowerVoltage
        message.accumulatingFuelConsumption = accumulatingFuelConsumption
        message.instantFuelConsumption = instantFuelConsumption
        message.rpm = rpm
        message.airInflowTemp = airInflowTemp
        message.airInput = airInput
        message.airPressure = airPressure
        message.coolingFluidTemp = coolingFluidTemp
        message.engineLoad = engineLoad
        message.throttlePosition = throttlePosition
        message.remainFuelRate = remainFuelRate
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
        message.ignitionSource = ignitionSource
        message.exPowerConsumpStatus = exPowerConsumpStatus
        message.hasThirdPartyObd = hasThirdPartyObd
        message.remainFuelUnit = remainFuelUnit
        message.mileageSource = mileageSource
        return message;
    },
    parseObdMessage:function(bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var date = ByteUtils.getGTM0Date(bytes, 15);
        var obdData = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"obdData",
            date:date,
        }
        var obdBytes = ByteUtils.arrayOfRange(bytes,21,bytes.length);

        var head = [2]
        head[0] = obdBytes[0];
        head[1] = obdBytes[1];
        if(ByteUtils.arrayEquals(head,this.obdHead)){
            obdBytes[2] = obdBytes[2] & 0x0F
            var length = ByteUtils.byteToShort(obdBytes,2);
            if(length > 0){
                try{
                    var data = ByteUtils.arrayOfRange(obdBytes,4,4+length);
                    if((data[0] & 0x41) == 0x41 && data[1] == 0x04 && data.length > 3){
                        obdData.obdMessageType = "clear_error_code"
                        obdData.clearErrorCodeSuccess = data[2] == 0x01
                    }else if((data[0] & 0x41) == 0x41 && data[1] == 0x05 && data.length > 2){
                        var vinData = ByteUtils.arrayOfRange(data,2,data.length - 1);
                        var dataValid = false;
                        for(var item in vinData){
                            if((item & 0xFF) != 0xFF){
                                dataValid = true;
                            }
                        }
                        if(vinData.length > 0 && dataValid){
                            obdData.obdMessageType = "vin"
                            obdData.vin = ByteUtils.bin2String(vinData);
                        }
                    }else if((data[0] & 0x41) == 0x41 && (data[1] == 0x03 || data[1] == 0x0A)){
                        var errorCode = data[2];
                        var errorDataByte = ByteUtils.arrayOfRange(data,3,data.length - 1);
                        var errorDataStr = ByteUtils.bytes2HexString(errorDataByte,0);
                        if(errorDataStr != null){
                            var errorDataSum = "";
                            for(var i = 0 ;i+6 <= errorDataStr.length;i+=6){
                                var errorDataItem = errorDataStr.substring(i,i+6);
                                var srcFlag = errorDataItem.substring(0,1);
                                var errorDataCode =  this.getObdErrorFlag(srcFlag) + errorDataItem.substring(1,4);
                                if(!errorDataSum.contains(errorDataCode)){
                                    if(i != 0){
                                        errorDataSum += ";";
                                    }
                                    errorDataSum += errorDataCode;
                                }
                                if(i+6 >= errorDataStr.length){
                                    break;
                                }
                            }
                            obdData.obdMessageType = "error_code"
                            obdData.errorCode = this.getObdErrorCode(errorCode)
                            obdData.errorData = errorDataSum
                        }
                    }
                }catch (e){
                    console.log(e)
                }
            }
        }
        return obdData;
    },
    getObdErrorCode:function(errorCode){
        if(errorCode == 0){
            return "J1979";
        }else if(errorCode == 1){
            return "J1939";
        }
        return "";
    },
    getObdErrorFlag:function (srcFlag){
        var data = ByteUtils.hexStringToByte(srcFlag);
        if(data[0] >= 0 && data[0] < 4){
            return "P" + data[0]
        }else if(data[0] >= 4 && data[0] < 8){
            return "C" + (data[0] - 4)
        }else if(data[0] >= 8 && data[0] < 12){
            return "B" + (data[0] - 8)
        }else{
            return "U" + data[0] - 12
        }
    },
    parseWifiMessage:function(bytes){
        var serialNo = ByteUtils.byteToShort(bytes,5);
        var imei = ByteUtils.IMEI.decode(bytes,7)
        var wifiMessage = {
            serialNo:serialNo,
            imei:imei,
            srcBytes:bytes,
            messageType:"wifi",
        }
        var date = ByteUtils.getGTM0Date(bytes, 15);
        var selfMac =  ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes, 21, 27), 0);
        var ap1Mac =  ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes, 27, 33), 0);
        var ap1Rssi = bytes[33];
        var ap2Mac =  ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes,34,40),0);
        var ap2Rssi = bytes[40];
        var ap3Mac =  ByteUtils.bytes2HexString(ByteUtils.arrayOfRange(bytes,41,47),0);
        var ap3Rssi = bytes[47];
        wifiMessage.setDate = date
        wifiMessage.selfMac = selfMac.toUpperCase()
        wifiMessage.ap1Mac = ap1Mac.toUpperCase()
        wifiMessage.ap1Rssi = ap1Rssi
        wifiMessage.ap2Mac = ap2Mac.toUpperCase()
        wifiMessage.ap2Rssi = ap2Rssi
        wifiMessage.ap3Mac = ap3Mac.toUpperCase()
        wifiMessage.ap3Rssi = ap3Rssi
        return wifiMessage;
    },
}

module.exports = ObdDecoder;
