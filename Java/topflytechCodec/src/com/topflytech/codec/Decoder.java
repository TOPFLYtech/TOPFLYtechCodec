package com.topflytech.codec;

import com.topflytech.codec.entities.*;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * The type Decoder.
 */
public class Decoder {


    private static final int HEADER_LENGTH = 3;

    private static final byte[] SIGNUP =      {0x23, 0x23, 0x01};

    private static final byte[] DATA =       {0x23, 0x23, 0x02};

    private static final byte[] HEARTBEAT =  {0x23, 0x23, 0x03};

    private static final byte[] ALARM =      {0x23, 0x23, 0x04};
    private static final byte[] CONFIG =          {0x23, 0x23, (byte)0x81};

    private static final byte[] SIGNUP_880XPlUS =      {0x25, 0x25, 0x01};

    private static final byte[] DATA_880XPlUS =       {0x25, 0x25, 0x02};

    private static final byte[] HEARTBEAT_880XPlUS =  {0x25, 0x25, 0x03};

    private static final byte[] ALARM_880XPlUS =      {0x25, 0x25, 0x04};

    private static final byte[] CONFIG_880XPlUS =          {0x25, 0x25, (byte)0x81};
    private static final byte[] GPS_DRIVER_BEHAVIOR = {0x25, 0x25, (byte)0x05};
    private static final byte[] ACCELERATION_DRIVER_BEHAVIOR =       {0x25, 0x25, (byte)0x06};
    private static final byte[] ACCELERATION_ALARM =       {0x25, 0x25, (byte)0x07};
    private static final byte[] BLUETOOTH_MAC =       {0x25, 0x25, (byte)0x08};
    private static final byte[] BLUETOOTH_DATA =       {0x25, 0x25, (byte)0x10};
    private static final byte[] NETWORK_INFO_DATA = {0x25, 0x25, (byte)0x11};
    private static final byte[] BLUETOOTH_SECOND_DATA =  {0x25, 0x25, (byte)0x12};
    private static final byte[] LOCATION_SECOND_DATA =      {0x25, 0x25, (byte)0x13};
    private static final byte[] ALARM_SECOND_DATA =      {0x25, 0x25, (byte)0x14};
    private static final byte[] RS232 = {0x25, 0x25, (byte)0x09};


    private static final byte[] latlngInvalidData = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
                                                     (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

    private static final byte[] rs232TireHead = {(byte)0x00,(byte)0x01};
    private static final byte[] rs232RfidHead = {(byte)0x00,(byte)0x03};
    private static final byte[] rs232FingerprintHead = {(byte)0x00,(byte)0x02,(byte)0x6C,(byte)0x62,(byte)0x63};
    private static final byte[] rs232CapacitorFuelHead = {(byte)0x00,(byte)0x04};
    private static final byte[] rs232UltrasonicFuelHead = {(byte)0x00,(byte)0x05};
    private int encryptType = 0;
    private String aesKey;


    private static final long MASK_IGNITION =   0x4000;
    private static final long MASK_POWER_CUT =  0x8000;
    private static final long MASK_AC       =   0x2000;
    private static final long IOP_RS232_DEVICE_VALID       =   0x20;
    private static final long MASK_RELAY = 0x400;
    /**
     * Instantiates a new Decoder.
     *
     * @param messageEncryptType The message encrypt type .Use the value of MessageEncryptType.
     * @param aesKey             The aes key.If you do not use AES encryption, the value can be empty.
     */
    public Decoder(int messageEncryptType,String aesKey){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
    }

    private static boolean match(byte[] bytes) {
        assert bytes.length >= HEADER_LENGTH : "command match: length is not 3!";
        return Arrays.equals(SIGNUP, bytes)
                || Arrays.equals(HEARTBEAT, bytes)
                || Arrays.equals(DATA, bytes)
                || Arrays.equals(ALARM, bytes)
                || Arrays.equals(CONFIG, bytes)
                || Arrays.equals(SIGNUP_880XPlUS, bytes)
                || Arrays.equals(HEARTBEAT_880XPlUS, bytes)
                || Arrays.equals(DATA_880XPlUS, bytes)
                || Arrays.equals(ALARM_880XPlUS, bytes)
                || Arrays.equals(CONFIG_880XPlUS, bytes)
                || Arrays.equals(GPS_DRIVER_BEHAVIOR, bytes)
                || Arrays.equals(ACCELERATION_DRIVER_BEHAVIOR, bytes)
                || Arrays.equals(ACCELERATION_ALARM, bytes)
                || Arrays.equals(BLUETOOTH_MAC, bytes)
                || Arrays.equals(BLUETOOTH_DATA, bytes)
                || Arrays.equals(NETWORK_INFO_DATA,bytes)
                || Arrays.equals(BLUETOOTH_SECOND_DATA, bytes)
                || Arrays.equals(LOCATION_SECOND_DATA, bytes)
                || Arrays.equals(ALARM_SECOND_DATA, bytes)
                || Arrays.equals(RS232,bytes);
    }


    /**
     * Decode.  decoder.You can get the message one by one from the callback.
     *
     * @param buf      the buf
     * @param callback the callback
     */
    public void decode(byte[] buf,Callback callback) {
        decoderBuf.putBuf(buf);
        if (decoderBuf.getReadableBytes() < (HEADER_LENGTH + 2)){
            callback.receiveErrorMessage("Error Message");
            return;
        }
        boolean foundHead = false;
        byte[] bytes = new byte[3];
        while (decoderBuf.getReadableBytes() > 5){
            decoderBuf.markReaderIndex();
            bytes[0] = decoderBuf.getByte(0);
            bytes[1] = decoderBuf.getByte(1);
            bytes[2] = decoderBuf.getByte(2);
            if (match(bytes)){
                foundHead = true;
                decoderBuf.skipBytes(HEADER_LENGTH);
                byte[] lengthBytes = decoderBuf.readBytes(2);
                int packageLength = BytesUtils.bytes2Short(lengthBytes, 0);
                if (encryptType == MessageEncryptType.MD5){
                    packageLength = packageLength + 8;
                }else if(encryptType == MessageEncryptType.AES){
                    packageLength = Crypto.getAesLength(packageLength);
                }
                decoderBuf.resetReaderIndex();
                if (packageLength > decoderBuf.getReadableBytes()){
                    callback.receiveErrorMessage("Error Message");
                    return;
                }
                byte[] data = decoderBuf.readBytes(packageLength);
                data =  Crypto.decryptData(data, encryptType, aesKey);
                if (data != null){
                    try {
                        build(data,callback);
                    }catch (ParseException e){
                        callback.receiveErrorMessage(e.getMessage());
                    }
                }
            }else{
                decoderBuf.skipBytes(1);
            }
        }
        if (foundHead == false){
            callback.receiveErrorMessage("Error Message");
        }
    }



    private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();

    /**
     * Docode list. You can get all message at once.
     *
     * @param buf the buf
     * @return the list
     */
    public List<Message> decode(byte[] buf){
        decoderBuf.putBuf(buf);
        List<Message> messages = new ArrayList<Message>();
        if (decoderBuf.getReadableBytes()  < (HEADER_LENGTH + 2)){
//            System.out.println("Error Message,Topflytech Codec Msg Length Error");
            return messages;
        }
        byte[] bytes = new byte[3];
        while (decoderBuf.getReadableBytes() > 5){
            decoderBuf.markReaderIndex();
            bytes[0] = decoderBuf.getByte(0);
            bytes[1] = decoderBuf.getByte(1);
            bytes[2] = decoderBuf.getByte(2);
            if (match(bytes)){
                decoderBuf.skipBytes(HEADER_LENGTH);
                byte[] lengthBytes = decoderBuf.readBytes(2);
                int packageLength = BytesUtils.bytes2Short(lengthBytes, 0);
                if (encryptType == MessageEncryptType.MD5){
                    packageLength = packageLength + 8;
                }else if(encryptType == MessageEncryptType.AES){
                    packageLength = Crypto.getAesLength(packageLength);
                }
                decoderBuf.resetReaderIndex();
                if (packageLength > decoderBuf.getReadableBytes()){
                    break;
                }
                byte[] data = decoderBuf.readBytes(packageLength);
                data = Crypto.decryptData(data, encryptType, aesKey);
                if (data != null){
                    try {
                        Message message = build(data);
                        if (message != null){
                            messages.add(message);
                        }
                    }catch (ParseException e){
                        System.out.println(e.getMessage());
                    }
                }
            }else{
                decoderBuf.skipBytes(1);
            }
        }
        return messages;
    }



    public Message build(byte[] bytes) throws ParseException{
        if (bytes != null && bytes.length > HEADER_LENGTH
                && ((bytes[0] == 0x23 && bytes[1] == 0x23) || (bytes[0] == 0x25 && bytes[1] == 0x25))) {
            switch (bytes[2]) {
                case 0x01:
                    SignInMessage signInMessage = parseLoginMessage(bytes);
                    return signInMessage;
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x02:
                case 0x04:
                    LocationMessage locationMessage = parseDataMessage(bytes);
                    return locationMessage;
                case 0x05:
                    GpsDriverBehaviorMessage gpsDriverBehaviorMessage = parseGpsDriverBehaviorMessage(bytes);
                    return gpsDriverBehaviorMessage;
                case 0x06:
                    AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = parseAccelerationDriverBehaviorMessage(bytes);
                    return accelerationDriverBehaviorMessage;
                case 0x07:
                    AccidentAccelerationMessage accidentAccelerationMessage = parseAccelerationAlarmMessage(bytes);
                    return accidentAccelerationMessage;
                case 0x09:
                    RS232Message rs232Message = parseRs232Message(bytes);
                    return rs232Message;
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataMessage;
                case 0x11:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case 0x12:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataSecondMessage;
                case 0x13:
                case 0x14:
                    LocationMessage locationSecondMessage = parseSecondDataMessage(bytes);
                    return locationSecondMessage;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    return message;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }
        return null;
    }
    public void build(byte[] bytes,Callback callback) throws ParseException {
        if (bytes != null && bytes.length > HEADER_LENGTH
                && ((bytes[0] == 0x23 && bytes[1] == 0x23) || (bytes[0] == 0x25 && bytes[1] == 0x25))) {
            switch (bytes[2]) {
                case 0x01:
                    SignInMessage signInMessage = parseLoginMessage(bytes);
                    callback.receiveSignInMessage(signInMessage);
                    break;
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    callback.receiveHeartbeatMessage(heartbeatMessage);
                    break;
                case 0x02:
                case 0x04:
                    LocationMessage locationMessage = parseDataMessage(bytes);
                    if (locationMessage instanceof LocationAlarmMessage){
                        callback.receiveAlarmMessage((LocationAlarmMessage)locationMessage);
                    }else if (locationMessage instanceof LocationInfoMessage) {
                        callback.receiveLocationInfoMessage((LocationInfoMessage) locationMessage);
                    }
                    break;
                case 0x13:
                case 0x14:
                    LocationMessage locationSecondMessage = parseSecondDataMessage(bytes);
                    if (locationSecondMessage instanceof LocationAlarmMessage){
                        callback.receiveAlarmMessage((LocationAlarmMessage)locationSecondMessage);
                    }else if (locationSecondMessage instanceof LocationInfoMessage) {
                        callback.receiveLocationInfoMessage((LocationInfoMessage) locationSecondMessage);
                    }
                    break;
                case 0x05:
                    GpsDriverBehaviorMessage gpsDriverBehaviorMessage = parseGpsDriverBehaviorMessage(bytes);
                    callback.receiveGpsDriverBehaviorMessage(gpsDriverBehaviorMessage);
                    break;
                case 0x06:
                    AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = parseAccelerationDriverBehaviorMessage(bytes);
                    callback.receiveAccelerationDriverBehaviorMessage(accelerationDriverBehaviorMessage);
                    break;
                case 0x07:
                    AccidentAccelerationMessage accidentAccelerationMessage = parseAccelerationAlarmMessage(bytes);
                    callback.receiveAccidentAccelerationMessage(accidentAccelerationMessage);
                    break;
                case 0x09:
                    RS232Message rs232Message = parseRs232Message(bytes);
                    callback.receiveRS232Message(rs232Message);
                    break;
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    callback.receiveBluetoothDataMessage(bluetoothPeripheralDataMessage);
                    break;
                case 0x12:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                    callback.receiveBluetoothDataMessage(bluetoothPeripheralDataSecondMessage);
                    break;
                case 0x11:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    callback.receiveNetworkInfoMessage(networkInfoMessage);
                    break;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    if (message instanceof ConfigMessage) {
                        callback.receiveConfigMessage((ConfigMessage)message);
                    }else if(message instanceof ForwardMessage){
                        callback.receiveForwardMessage((ForwardMessage) message);
                    }else if(message instanceof USSDMessage){
                        callback.receiveUSSDMessage((USSDMessage)message);
                    }
                    break;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }

    }

    private BluetoothPeripheralDataMessage parseBluetoothDataMessage(byte[] bytes) {
        BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = new BluetoothPeripheralDataMessage();
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        if ((bytes[21] & 0x01) == 0x01){
            bluetoothPeripheralDataMessage.setIsIgnition(true);
        }else {
            bluetoothPeripheralDataMessage.setIsIgnition(false);
        }
        bluetoothPeripheralDataMessage.setProtocolHeadType(0x10);
        bluetoothPeripheralDataMessage.setDate(TimeUtils.getGTM0Date(bytes, 15));
        bluetoothPeripheralDataMessage.setOrignBytes(bytes);
//        bluetoothPeripheralDataMessage.setIsHistoryData((bytes[15] & 0x80) != 0x00);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        bluetoothPeripheralDataMessage.setSerialNo(serialNo);
//        bluetoothPeripheralDataMessage.setIsNeedResp(isNeedResp);
        bluetoothPeripheralDataMessage.setImei(imei);
        byte[] bleData = Arrays.copyOfRange(bytes,22,bytes.length);
        List<BleData> bleDataList = new ArrayList<BleData>();
        if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE);
            for (int i = 2;i < bleData.length;i+=10){
                BleTireData bleTireData = new BleTireData();
                byte[] macArray = Arrays.copyOfRange(bleData, i, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                double voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 1.22 + 0.01 * voltageTmp;
                }
                int airPressureTmp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                double airPressure;
                if(airPressureTmp == 255){
                    airPressure = -999;
                }else{
                    airPressure = 1.572 * 2 * airPressureTmp;
                }
                int airTempTmp = (int) bleData[i + 8] < 0 ? (int) bleData[i + 8] + 256 : (int) bleData[i + 8];
                int airTemp;
                if(airTempTmp == 255){
                    airTemp = -999;
                }else{
                    airTemp = airTempTmp - 55;
                }
//            boolean isTireLeaks = (bleData[i+5] == 0x01);
                bleTireData.setMac(mac);
                bleTireData.setVoltage(voltage);
                bleTireData.setAirPressure(airPressure);
                bleTireData.setAirTemp(airTemp);
//            bleTireData.setIsTireLeaks(isTireLeaks);
                int alarm = (int) bleData[i + 9];
                if(alarm == -1){
                    alarm = 0;
                }
                bleTireData.setStatus(alarm);
                bleDataList.add(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS);
            BleAlertData bleAlertData = new BleAlertData();
            byte[] macArray = Arrays.copyOfRange(bleData, 2, 8);
            String mac = BytesUtils.bytes2HexString(macArray, 0);
            String voltageStr = BytesUtils.bytes2HexString(bleData,8).substring(0, 2);
            float voltage = 0;
            try {
                voltage = Float.valueOf(voltageStr) / 10;
            }catch (Exception e){
                System.out.println("imei :" + imei + " ble sos voltage error : " + voltageStr);
            }
            byte alertByte = bleData[9];
            int alert = alertByte == 0x01 ? BleAlertData.ALERT_TYPE_LOW_BATTERY : BleAlertData.ALERT_TYPE_SOS;
            boolean isHistoryData = (bleData[10] & 0x80) != 0x00;
            boolean latlngValid = (bleData[10] & 0x40) != 0x00;
            int satelliteNumber = bleData[10] & 0x1F;
            double altitude = latlngValid? BytesUtils.bytes2Float(bleData, 11) : 0.0;
            double longitude = latlngValid ? BytesUtils.bytes2Float(bleData, 15) : 0.0;
            double latitude = latlngValid ? BytesUtils.bytes2Float(bleData, 19) : 0.0;
            int azimuth = latlngValid ? BytesUtils.bytes2Short(bleData, 25) : 0;
            Float speedf = 0.0f;
            try{
                byte[] bytesSpeed = Arrays.copyOfRange(bleData, 23, 25);
                String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                if(strSp.contains("f")){
                    speedf = -1f;
                }else {
                    speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
                }
            }catch (Exception e){
                System.out.println("Imei : " + imei);
                e.printStackTrace();
            }
            bleAlertData.setAlertType(alert);
            bleAlertData.setAltitude(altitude);
            bleAlertData.setAzimuth(azimuth);
            bleAlertData.setInnerVoltage(voltage);
            bleAlertData.setIsHistoryData(isHistoryData);
            bleAlertData.setLatitude(latitude);
            bleAlertData.setLatlngValid(latlngValid);
            bleAlertData.setSatelliteCount(satelliteNumber);
            bleAlertData.setLongitude(longitude);
            bleAlertData.setMac(mac);
            bleAlertData.setSpeed(speedf);
            bleDataList.add(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER);
            BleDriverSignInData bleDriverSignInData = new BleDriverSignInData();
            byte[] macArray = Arrays.copyOfRange(bleData, 2,8);
            String mac = BytesUtils.bytes2HexString(macArray, 0);
            String voltageStr = BytesUtils.bytes2HexString(bleData,8).substring(0, 2);
            float voltage = 0;
            try {
                voltage = Float.valueOf(voltageStr) / 10;
            }catch (Exception e){
                System.out.println("imei :" + imei + " ble sos voltage error : " + voltageStr);
            }
            byte alertByte = bleData[9];
            int alert = alertByte == 0x01 ? BleDriverSignInData.ALERT_TYPE_LOW_BATTERY : BleDriverSignInData.ALERT_TYPE_DRIVER;
            boolean isHistoryData = (bleData[10] & 0x80) != 0x00;
            boolean latlngValid = (bleData[10] & 0x40) != 0x00;
            int satelliteNumber = bleData[10] & 0x1F;
            double altitude = latlngValid? BytesUtils.bytes2Float(bleData, 11) : 0.0;
            double longitude = latlngValid ? BytesUtils.bytes2Float(bleData, 15) : 0.0;
            double latitude = latlngValid ? BytesUtils.bytes2Float(bleData, 19) : 0.0;
            int azimuth = latlngValid ? BytesUtils.bytes2Short(bleData, 25) : 0;
            Float speedf = 0.0f;
            try{
                byte[] bytesSpeed = Arrays.copyOfRange(bleData, 23, 25);
                String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                if(strSp.contains("f")){
                    speedf = -1f;
                }else {
                    speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
                }
            }catch (Exception e){
                System.out.println("Imei : " + imei);
                e.printStackTrace();
            }
            bleDriverSignInData.setAlert(alert);
            bleDriverSignInData.setAltitude(altitude);
            bleDriverSignInData.setAzimuth(azimuth);
            bleDriverSignInData.setVoltage(voltage);
            bleDriverSignInData.setIsHistoryData(isHistoryData);
            bleDriverSignInData.setLatitude(latitude);
            bleDriverSignInData.setLatlngValid(latlngValid);
            bleDriverSignInData.setSatelliteCount(satelliteNumber);
            bleDriverSignInData.setLongitude(longitude);
            bleDriverSignInData.setMac(mac);
            bleDriverSignInData.setSpeed(speedf);
            bleDataList.add(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=15){
                BleTempData bleTempData = new BleTempData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                if(mac.startsWith("0000")){
                    mac = mac.substring(4,12);
                }
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int humidityTemp = BytesUtils.bytes2Short(bleData,i+10);
                float humidity;
                if(humidityTemp == 65535){
                    humidity = -999;
                }else{
                    humidity = humidityTemp * 0.01f;
                }
                int lightTemp = BytesUtils.bytes2Short(bleData,i+12);
                boolean isOpenBox = false;
                int lightIntensity ;
                if(lightTemp == 65535){
                    lightIntensity = -999;
                }else{
                    lightIntensity = lightTemp & 0xfff;
                    isOpenBox = (0x8000 & lightTemp) == 0x8000;
                }
                int rssiTemp = (int) bleData[i + 14] < 0 ? (int) bleData[i + 14] + 256 : (int) bleData[i + 14];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleTempData.setRssi(rssi);
                bleTempData.setMac(mac);
                bleTempData.setLightIntensity(lightIntensity);
                bleTempData.setIsOpenBox(isOpenBox);
                bleTempData.setHumidity(Float.valueOf(decimalFormat.format(humidity)));
                bleTempData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleTempData.setBatteryPercent(batteryPercent);
                bleTempData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=12){
                BleDoorData bleDoorData = new BleDoorData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int doorStatus = (int) bleData[i+10] < 0 ? (int) bleData[i+10] + 256 : (int) bleData[i+10];
                int online = 1;
                if(doorStatus == 255){
                    doorStatus = -999;
                    online = 0;
                }

                int rssiTemp = (int) bleData[i + 11] < 0 ? (int) bleData[i + 11] + 256 : (int) bleData[i + 11];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleDoorData.setRssi(rssi);
                bleDoorData.setMac(mac);
                bleDoorData.setOnline(online);
                bleDoorData.setDoorStatus(doorStatus);
                bleDoorData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleDoorData.setBatteryPercent(batteryPercent);
                bleDoorData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=12){
                BleCtrlData bleCtrlData = new BleCtrlData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int CtrlStatus =(int) bleData[i+10] < 0 ? (int) bleData[i+10] + 256 : (int) bleData[i+10];
                int online = 1;
                if(CtrlStatus == 255){
                    CtrlStatus = -999;
                    online = 0;
                }

                int rssiTemp = (int) bleData[i + 11] < 0 ? (int) bleData[i + 11] + 256 : (int) bleData[i + 11];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleCtrlData.setRssi(rssi);
                bleCtrlData.setMac(mac);
                bleCtrlData.setOnline(online);
                bleCtrlData.setCtrlStatus(CtrlStatus);
                bleCtrlData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleCtrlData.setBatteryPercent(batteryPercent);
                bleCtrlData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleCtrlData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x07){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=15){
                BleFuelData bleFuelData = new BleFuelData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int valueTemp = BytesUtils.bytes2Short(bleData,i+7);
                int value;
                if(valueTemp == 65535){
                    value = -999;
                }else{
                    value = valueTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+9);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int status =(int) bleData[i+13] < 0 ? (int) bleData[i+13] + 256 : (int) bleData[i+13];
                int online = 1;
                if(status == 255){
                    status = 0;
                    online = 0;
                }
                int rssiTemp = (int) bleData[i + 14] < 0 ? (int) bleData[i + 14] + 256 : (int) bleData[i + 14];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleFuelData.setRssi(rssi);
                bleFuelData.setMac(mac);
                bleFuelData.setOnline(online);
                bleFuelData.setAlarm(status);
                bleFuelData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleFuelData.setValue(value);
                bleFuelData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleFuelData);
            }
        }
        bluetoothPeripheralDataMessage.setBleDataList(bleDataList);
        return bluetoothPeripheralDataMessage;
    }

    private BluetoothPeripheralDataMessage parseSecondBluetoothDataMessage(byte[] bytes) {
        BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = new BluetoothPeripheralDataMessage();
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        if ((bytes[21] & 0x01) == 0x01){
            bluetoothPeripheralDataMessage.setIsIgnition(true);
        }else {
            bluetoothPeripheralDataMessage.setIsIgnition(false);
        }
        bluetoothPeripheralDataMessage.setProtocolHeadType(0x12);
        bluetoothPeripheralDataMessage.setDate(TimeUtils.getGTM0Date(bytes, 15));
        bluetoothPeripheralDataMessage.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        bluetoothPeripheralDataMessage.setSerialNo(serialNo);
//        bluetoothPeripheralDataMessage.setIsNeedResp(isNeedResp);
        bluetoothPeripheralDataMessage.setImei(imei);
        boolean latlngValid = (bytes[22] & 0xBF) != 0xBF;
        boolean isHisData = (bytes[22] & 0x7F) != 0x7F;
        bluetoothPeripheralDataMessage.setLatlngValid(latlngValid);
        bluetoothPeripheralDataMessage.setIsHistoryData(isHisData);
        double altitude = latlngValid? BytesUtils.bytes2Float(bytes, 23) : 0.0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(bytes, 27) : 0.0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(bytes, 31) : 0.0;
        int azimuth = latlngValid ? BytesUtils.bytes2Short(bytes, 37) : 0;
        Float speedf = 0.0f;
        try{
            byte[] bytesSpeed = Arrays.copyOfRange(bytes, 35, 37);
            String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
            if(strSp.contains("f")){
                speedf = -1f;
            }else {
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }
        }catch (Exception e){
            System.out.println("Imei : " + imei);
            e.printStackTrace();
        }
        bluetoothPeripheralDataMessage.setLatitude(latitude);
        bluetoothPeripheralDataMessage.setLongitude(longitude);
        bluetoothPeripheralDataMessage.setAzimuth(azimuth);
        bluetoothPeripheralDataMessage.setSpeed(speedf);
        bluetoothPeripheralDataMessage.setAltitude(altitude);
        bluetoothPeripheralDataMessage.setIsHadLocationInfo(true);
        byte[] bleData = Arrays.copyOfRange(bytes,39,bytes.length);
        List<BleData> bleDataList = new ArrayList<BleData>();
        if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE);
            for (int i = 2;i < bleData.length;i+=10){
                BleTireData bleTireData = new BleTireData();
                byte[] macArray = Arrays.copyOfRange(bleData, i, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                double voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 1.22 + 0.01 * voltageTmp;
                }
                int airPressureTmp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                double airPressure;
                if(airPressureTmp == 255){
                    airPressure = -999;
                }else{
                    airPressure = 1.572 * 2 * airPressureTmp;
                }
                int airTempTmp = (int) bleData[i + 8] < 0 ? (int) bleData[i + 8] + 256 : (int) bleData[i + 8];
                int airTemp;
                if(airTempTmp == 255){
                    airTemp = -999;
                }else{
                    airTemp = airTempTmp - 55;
                }
//            boolean isTireLeaks = (bleData[i+5] == 0x01);
                bleTireData.setMac(mac);
                bleTireData.setVoltage(voltage);
                bleTireData.setAirPressure(airPressure);
                bleTireData.setAirTemp(airTemp);
//            bleTireData.setIsTireLeaks(isTireLeaks);
                int alarm = (int) bleData[i + 9];
                if(alarm == -1){
                    alarm = 0;
                }
                bleTireData.setStatus(alarm);
                bleDataList.add(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS);
            BleAlertData bleAlertData = new BleAlertData();
            byte[] macArray = Arrays.copyOfRange(bleData, 2, 8);
            String mac = BytesUtils.bytes2HexString(macArray, 0);
            String voltageStr = BytesUtils.bytes2HexString(bleData,8).substring(0, 2);
            float voltage = 0;
            try {
                voltage = Float.valueOf(voltageStr) / 10;
            }catch (Exception e){
                System.out.println("imei :" + imei + " ble sos voltage error : " + voltageStr);
            }
            byte alertByte = bleData[9];
            int alert = alertByte == 0x01 ? BleAlertData.ALERT_TYPE_LOW_BATTERY : BleAlertData.ALERT_TYPE_SOS;

            bleAlertData.setAlertType(alert);
            bleAlertData.setAltitude(altitude);
            bleAlertData.setAzimuth(azimuth);
            bleAlertData.setInnerVoltage(voltage);
            bleAlertData.setIsHistoryData(isHisData);
            bleAlertData.setLatitude(latitude);
            bleAlertData.setLatlngValid(latlngValid);
            bleAlertData.setLongitude(longitude);
            bleAlertData.setMac(mac);
            bleAlertData.setSpeed(speedf);
            bleDataList.add(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER);
            BleDriverSignInData bleDriverSignInData = new BleDriverSignInData();
            byte[] macArray = Arrays.copyOfRange(bleData, 2,8);
            String mac = BytesUtils.bytes2HexString(macArray, 0);
            String voltageStr = BytesUtils.bytes2HexString(bleData,8).substring(0, 2);
            float voltage = 0;
            try {
                voltage = Float.valueOf(voltageStr) / 10;
            }catch (Exception e){
                System.out.println("imei :" + imei + " ble sos voltage error : " + voltageStr);
            }
            byte alertByte = bleData[9];
            int alert = alertByte == 0x01 ? BleDriverSignInData.ALERT_TYPE_LOW_BATTERY : BleDriverSignInData.ALERT_TYPE_DRIVER;

            bleDriverSignInData.setAlert(alert);
            bleDriverSignInData.setAltitude(altitude);
            bleDriverSignInData.setAzimuth(azimuth);
            bleDriverSignInData.setVoltage(voltage);
            bleDriverSignInData.setIsHistoryData(isHisData);
            bleDriverSignInData.setLatitude(latitude);
            bleDriverSignInData.setLatlngValid(latlngValid);
            bleDriverSignInData.setLongitude(longitude);
            bleDriverSignInData.setMac(mac);
            bleDriverSignInData.setSpeed(speedf);
            bleDataList.add(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=15){
                BleTempData bleTempData = new BleTempData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                if(mac.startsWith("0000")){
                    mac = mac.substring(4,12);
                }
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int humidityTemp = BytesUtils.bytes2Short(bleData,i+10);
                float humidity;
                if(humidityTemp == 65535){
                    humidity = -999;
                }else{
                    humidity = humidityTemp * 0.01f;
                }
                int lightTemp = BytesUtils.bytes2Short(bleData,i+12);
                boolean isOpenBox = false;
                int lightIntensity ;
                if(lightTemp == 65535){
                    lightIntensity = -999;
                }else{
                    lightIntensity = lightTemp & 0xfff;
                    isOpenBox = (0x8000 & lightTemp) == 0x8000;
                }
                int rssiTemp = (int) bleData[i + 14] < 0 ? (int) bleData[i + 14] + 256 : (int) bleData[i + 14];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleTempData.setRssi(rssi);
                bleTempData.setMac(mac);
                bleTempData.setLightIntensity(lightIntensity);
                bleTempData.setIsOpenBox(isOpenBox);
                bleTempData.setHumidity(Float.valueOf(decimalFormat.format(humidity)));
                bleTempData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleTempData.setBatteryPercent(batteryPercent);
                bleTempData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=12){
                BleDoorData bleDoorData = new BleDoorData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int doorStatus = (int) bleData[i+10] < 0 ? (int) bleData[i+10] + 256 : (int) bleData[i+10];
                int online = 1;
                if(doorStatus == 255){
                    doorStatus = -999;
                    online = 0;
                }

                int rssiTemp = (int) bleData[i + 11] < 0 ? (int) bleData[i + 11] + 256 : (int) bleData[i + 11];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleDoorData.setRssi(rssi);
                bleDoorData.setMac(mac);
                bleDoorData.setOnline(online);
                bleDoorData.setDoorStatus(doorStatus);
                bleDoorData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleDoorData.setBatteryPercent(batteryPercent);
                bleDoorData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i < bleData.length;i+=12){
                BleCtrlData bleCtrlData = new BleCtrlData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
                float voltage;
                if(voltageTmp == 255){
                    voltage = -999;
                }else{
                    voltage = 2 + 0.01f * voltageTmp;
                }
                int batteryPercentTemp = (int) bleData[i + 7] < 0 ? (int) bleData[i + 7] + 256 : (int) bleData[i + 7];
                int batteryPercent;
                if(batteryPercentTemp == 255){
                    batteryPercent = -999;
                }else{
                    batteryPercent = batteryPercentTemp;
                }
                int temperatureTemp = BytesUtils.bytes2Short(bleData,i+8);
                int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
                float temperature;
                if(temperatureTemp == 65535){
                    temperature = -999;
                }else{
                    temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
                }
                int CtrlStatus =(int) bleData[i+10] < 0 ? (int) bleData[i+10] + 256 : (int) bleData[i+10];
                int online = 1;
                if(CtrlStatus == 255){
                    CtrlStatus = -999;
                    online = 0;
                }

                int rssiTemp = (int) bleData[i + 11] < 0 ? (int) bleData[i + 11] + 256 : (int) bleData[i + 11];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                bleCtrlData.setRssi(rssi);
                bleCtrlData.setMac(mac);
                bleCtrlData.setOnline(online);
                bleCtrlData.setCtrlStatus(CtrlStatus);
                bleCtrlData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
                bleCtrlData.setBatteryPercent(batteryPercent);
                bleCtrlData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
                bleDataList.add(bleCtrlData);
            }
        }
        bluetoothPeripheralDataMessage.setBleDataList(bleDataList);
        return bluetoothPeripheralDataMessage;
    }

    private SignInMessage parseLoginMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        String str = BytesUtils.bytes2HexString(bytes, 15);
        if (12 == str.length()) {
            String software = String.format("V%d.%d.%d", (bytes[15] & 0xf0) >> 4, bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4);
            String firmware = String.format("V%d.%d.%d", bytes[16] & 0xf, (bytes[17] & 0xf0) >> 4, bytes[17] & 0xf);
            String platform = String.format("%s", str.substring(6, 10));
            String hardware = String.format("%d.%d", (bytes[20] & 0xf0) >> 4, bytes[20] & 0xf);
            SignInMessage signInMessage = new SignInMessage();
            signInMessage.setSerialNo(serialNo);
            signInMessage.setImei(imei);
            signInMessage.setSoftware(software);
            signInMessage.setFirmware(firmware);
            signInMessage.setPlatform(platform);
            signInMessage.setHardware(hardware);
            signInMessage.setOrignBytes(bytes);
//            signInMessage.setIsNeedResp(isNeedResp);
            return signInMessage;
        }else if(16 == str.length()){
            String software = String.format("V%d.%d.%d",  bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4, bytes[16] & 0xf);
            String firmware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 20, 22), 0);
            firmware = String.format("V%s.%s.%s.%s", firmware.substring(0,1), firmware.substring(1, 2), firmware.substring(2,3),firmware.substring(3,4));
            String hardware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 22, 23), 0);
            hardware = String.format("V%s.%s", hardware.substring(0,1), hardware.substring(1, 2));
            SignInMessage signInMessage = new SignInMessage();
            signInMessage.setSerialNo(serialNo);
            signInMessage.setImei(imei);
            signInMessage.setSoftware(software);
            signInMessage.setFirmware(firmware);
            signInMessage.setHardware(hardware);
            signInMessage.setOrignBytes(bytes);
//            signInMessage.setIsNeedResp(isNeedResp);
            return signInMessage;
        }else{
            throw new ParseException("Error login message",0);
        }
    }
    private HeartbeatMessage parseHeartbeat(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
        heartbeatMessage.setOrignBytes(bytes);
        heartbeatMessage.setSerialNo(serialNo);
        heartbeatMessage.setImei(imei);
//        heartbeatMessage.setIsNeedResp(isNeedResp);
        return heartbeatMessage;
    }



    private Message parseInteractMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        byte protocol = bytes[15];
        byte[] data = Arrays.copyOfRange(bytes, 16, bytes.length);
        String messageData;
        switch (protocol) {
            case 0x01:{
                ConfigMessage configMessage = new ConfigMessage();
                messageData = new String(data, Charset.forName("UTF-16LE"));
                configMessage.setConfigResultContent(messageData);
                configMessage.setOrignBytes(bytes);
                configMessage.setImei(imei);
                configMessage.setSerialNo(serialNo);
//                configMessage.setIsNeedResp(isNeedResp);
                return configMessage;
            }
            case 0x03:{
                ForwardMessage forwardMessage = new ForwardMessage();
                messageData = new String(data, Charset.forName("UTF-16LE"));
                forwardMessage.setContent(messageData);
                forwardMessage.setOrignBytes(bytes);
                forwardMessage.setImei(imei);
                forwardMessage.setSerialNo(serialNo);
//                forwardMessage.setIsNeedResp(isNeedResp);
                return forwardMessage;
            }case 0x05:{
                USSDMessage ussdMessage = new USSDMessage();
                messageData = new String(data, Charset.forName("UTF-16LE"));
                ussdMessage.setContent(messageData);
                ussdMessage.setOrignBytes(bytes);
                ussdMessage.setImei(imei);
                ussdMessage.setSerialNo(serialNo);
//                ussdMessage.setIsNeedResp(isNeedResp);
                return ussdMessage;
            }
            default:
            {
                System.out.println("Error config message:" + BytesUtils.bytes2HexString(bytes,0));
                return null;
            }
        }

    }

    private RS232Message parseRs232Message(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        Date date = TimeUtils.getGTM0Date(bytes, 15);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        boolean isIgnition = (bytes[21] == 0x01);
        byte[] data = Arrays.copyOfRange(bytes, 22, bytes.length);
        RS232Message rs232Message = new RS232Message();
        rs232Message.setImei(imei);
        rs232Message.setOrignBytes(bytes);
        rs232Message.setSerialNo(serialNo);
        rs232Message.setDate(date);
        rs232Message.setIgnition(isIgnition);
//        rs232Message.setIsNeedResp(isNeedResp);
        if(data.length < 2){
            return rs232Message;
        }
        byte[] rs232Head = {data[0],data[1]};
        byte[] fingerprintHead = null;
        if(data.length > 4 ){
            fingerprintHead = new byte[]{data[0],data[1],data[2],data[3],data[4]};
        }
        List<Rs232DeviceMessage> messageList = new ArrayList<Rs232DeviceMessage>();
        if(Arrays.equals(rs232Head,rs232TireHead)){
            rs232Message.setRs232DataType(RS232Message.TIRE_DATA);
            int dataCount = (data.length -2 ) / 7;
            int curIndex = 2;
            for(int i = 0;i < dataCount;i++){
                curIndex = i * 7 + 2;
                Rs232TireMessage rs232TireMessage = new Rs232TireMessage();
                int airPressureTmp = (int) data[curIndex + 4];
                if(airPressureTmp < 0){
                    airPressureTmp += 256;
                }
                double airPressure = airPressureTmp * 1.572 * 2;
                rs232TireMessage.setAirPressure(airPressure);
                int airTempTmp = (int) data[curIndex + 5];
                if(airTempTmp < 0){
                    airTempTmp += 256;
                }
                int airTemp = airTempTmp - 55;
                rs232TireMessage.setAirTemp(airTemp);
                int statusTmp = (int) data[curIndex + 6];
                if(statusTmp < 0){
                    statusTmp += 256;
                }
                rs232TireMessage.setStatus(statusTmp);
                int voltageTmp = (int) data[curIndex + 3];
                if(voltageTmp < 0){
                    voltageTmp += 256;
                }
                double voltage = 0.01 * voltageTmp + 1.22;
                rs232TireMessage.setVoltage(voltage);
                byte[] sensorIdByte = {data[curIndex],data[curIndex + 1],data[curIndex + 2]};
                rs232TireMessage.setSensorId(BytesUtils.bytes2HexString(sensorIdByte,0));
                messageList.add(rs232TireMessage);
            }
        } else if (Arrays.equals(rs232Head,rs232RfidHead)){
            rs232Message.setRs232DataType(RS232Message.RDID_DATA);
            int dataCount = data.length / 10;
            int curIndex = 0;
            for(int i = 0;i < dataCount;i++){
                curIndex = i * 10;
                Rs232RfidMessage rs232RfidMessage = new Rs232RfidMessage();
                byte[] rfidByte = {data[curIndex + 2],data[curIndex + 3],data[curIndex + 4],data[curIndex + 5],
                        data[curIndex + 6],data[curIndex + 7],data[curIndex + 8],data[curIndex + 9]};
                rs232RfidMessage.setRfid(new String(rfidByte));
                messageList.add(rs232RfidMessage);
            }
        }else if(fingerprintHead != null && Arrays.equals(fingerprintHead,rs232FingerprintHead)){
            rs232Message.setRs232DataType(RS232Message.FINGERPRINT_DATA);
            Rs232FingerprintMessage rs232FingerprintMessage = new Rs232FingerprintMessage();
            if(data[6] == 0x00){
                rs232FingerprintMessage.setStatus(Rs232FingerprintMessage.FINGERPRITN_MSG_STATUS_SUCC);
            }else{
                rs232FingerprintMessage.setStatus(Rs232FingerprintMessage.FINGERPRINT_MSG_STATUS_ERROR);
            }
            if(data[5] == 0x25){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_CLOUND_REGISTER);
                int dataIndex = (int)data[8];
                rs232FingerprintMessage.setFingerprintDataIndex(dataIndex);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
                if(data.length > 10){
                    rs232FingerprintMessage.setData(BytesUtils.bytes2HexString(data,10));
                }
            }else if(data[5]== 0x71){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_PATCH);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0x73){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_DELETE);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0x78){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_WRITE_TEMPLATE);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
                int remarkId = (int)data[8];
                if(remarkId < 0){
                    remarkId += 256;
                }
                rs232FingerprintMessage.setRemarkId(remarkId);
            }else if(data[5] == 0xA6){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PERMISSION);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0xA7){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_PERMISSION);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0x54){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_ALL_CLEAR);
            }else if(data[5] == 0x74){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_GET_EMPTY_ID);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0xA5){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION);
            }else if(data[5] == 0xA3){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_REGISTER);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
            }else if(data[5] == 0x77){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_GET_TEMPLATE);
                int dataIndex = (int)data[8];
                rs232FingerprintMessage.setFingerprintDataIndex(dataIndex);
                int fingerprintId = (int)data[7];
                if(fingerprintId < 0){
                    fingerprintId += 256;
                }
                rs232FingerprintMessage.setFingerprintId(fingerprintId);
                rs232FingerprintMessage.setData(BytesUtils.bytes2HexString(data, 10));
            }else if(data[5] == 0x59){
                rs232FingerprintMessage.setFingerprintType(Rs232FingerprintMessage.FINGERPRINT_TYPE_OF_SET_DEVICE_ID);
            }
            messageList.add(rs232FingerprintMessage);
        } else if(Arrays.equals(rs232Head,rs232CapacitorFuelHead)){
            rs232Message.setRs232DataType(RS232Message.CAPACITOR_FUEL_DATA);
            Rs232FuelMessage rs232FuelMessage = new Rs232FuelMessage();
            byte[] fuelData = Arrays.copyOfRange(data,2,6);
            if(fuelData.length > 0){
                String fuelStr = new String(fuelData);
                try {
                    if(!fuelStr.trim().equals("")){
                        rs232FuelMessage.setFuelPercent((Float.valueOf(fuelStr)) / 100);
                    }
                }catch (Exception e){
                    System.out.println("empty fuel:" + fuelStr + " " + BytesUtils.bytes2HexString(fuelData,0));
                }
            }
            rs232FuelMessage.setAlarm((int)(data[6]));
            messageList.add(rs232FuelMessage);
        }else if(Arrays.equals(rs232Head,rs232UltrasonicFuelHead)){
            rs232Message.setRs232DataType(RS232Message.ULTRASONIC_FUEL_DATA);
            Rs232FuelMessage rs232FuelMessage = new Rs232FuelMessage();
            byte[] curHeightData = Arrays.copyOfRange(data, 2, 4);
            String curHeightStr = BytesUtils.bytes2HexString(curHeightData, 0);
            if(curHeightStr.toLowerCase().equals("ffff")){
                rs232FuelMessage.setCurLiquidHeight(-999f);
            }else{
                rs232FuelMessage.setCurLiquidHeight(Float.valueOf(curHeightStr) / 10);
            }
            byte[] tempData = Arrays.copyOfRange(data,4,6);
            String tempStr = BytesUtils.bytes2HexString(tempData, 0);
            if(tempStr.toLowerCase().equals("ffff")){
                rs232FuelMessage.setTemp(-999f);
            }else{
                rs232FuelMessage.setTemp(Integer.valueOf(tempStr.substring(1,4)) / 10.0f);
                if(tempStr.substring(0,1).equals("1")){
                    rs232FuelMessage.setTemp(-1 * rs232FuelMessage.getTemp());
                }
            }
            byte[] fullHeightData = Arrays.copyOfRange(data,6,8);
            String fullHeightStr = BytesUtils.bytes2HexString(fullHeightData, 0);
            if(fullHeightStr.toLowerCase().equals("ffff")){
                rs232FuelMessage.setFullLiquidHeight(-999f);
            }else{
                rs232FuelMessage.setFullLiquidHeight(Float.valueOf(fullHeightStr ));
            }
            rs232FuelMessage.setLiquidType((int)data[8] + 1);
            rs232FuelMessage.setAlarm((int)(data[9]));
            messageList.add(rs232FuelMessage);
        }else{
            Rs232DeviceMessage rs232DeviceMessage = new Rs232DeviceMessage();
            rs232DeviceMessage.setRS232Data(data);
            messageList.add(rs232DeviceMessage);
            rs232Message.setRs232DataType(RS232Message.OTHER_DEVICE_DATA);
        }
        rs232Message.setRs232DeviceMessageList(messageList);
        return rs232Message;
    }


    private GpsDriverBehaviorMessage parseGpsDriverBehaviorMessage(byte[] bytes) {
        int length = BytesUtils.bytes2Short(bytes, 3);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        GpsDriverBehaviorMessage gpsDriverBehaviorMessage = new GpsDriverBehaviorMessage();
        int behaviorType = (int)bytes[15];
        gpsDriverBehaviorMessage.setSerialNo(serialNo);
//        gpsDriverBehaviorMessage.setIsNeedResp(isNeedResp);
        gpsDriverBehaviorMessage.setImei(imei);
        gpsDriverBehaviorMessage.setBehaviorType(behaviorType);
        gpsDriverBehaviorMessage.setOrignBytes(bytes);
        Date startDate = TimeUtils.getGTM0Date(bytes, 16);
        gpsDriverBehaviorMessage.setStartDate(startDate);
        gpsDriverBehaviorMessage.setStartAltitude(BytesUtils.bytes2Float(bytes, 22));
        gpsDriverBehaviorMessage.setStartLongitude(BytesUtils.bytes2Float(bytes, 26));
        gpsDriverBehaviorMessage.setStartLatitude(BytesUtils.bytes2Float(bytes,30));
        byte[] bytesSpeed = Arrays.copyOfRange(bytes, 34, 36);
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        Float speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        gpsDriverBehaviorMessage.setStartSpeed(speedf);
        int azimuth = BytesUtils.bytes2Short(bytes, 36);
        gpsDriverBehaviorMessage.setStartAzimuth(azimuth);

        Date endDate = TimeUtils.getGTM0Date(bytes, 38);
        gpsDriverBehaviorMessage.setEndDate(endDate);
        gpsDriverBehaviorMessage.setEndAltitude(BytesUtils.bytes2Float(bytes, 44));
        gpsDriverBehaviorMessage.setEndLongitude(BytesUtils.bytes2Float(bytes, 48));
        gpsDriverBehaviorMessage.setEndLatitude(BytesUtils.bytes2Float(bytes, 52));
        bytesSpeed = Arrays.copyOfRange(bytes, 56, 58);
        strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        gpsDriverBehaviorMessage.setEndSpeed(speedf);
        azimuth = BytesUtils.bytes2Short(bytes, 58);
        gpsDriverBehaviorMessage.setEndAzimuth(azimuth);
        return gpsDriverBehaviorMessage;
    }

    private AccidentAccelerationMessage parseAccelerationAlarmMessage(byte[] bytes) {
        int length = BytesUtils.bytes2Short(bytes, 3);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        AccidentAccelerationMessage accidentAccelerationMessage = new AccidentAccelerationMessage();
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        accidentAccelerationMessage.setOrignBytes(bytes);
        accidentAccelerationMessage.setSerialNo(serialNo);
//        accidentAccelerationMessage.setIsNeedResp(isNeedResp);
        accidentAccelerationMessage.setImei(imei);
        int behaviorType = (int)bytes[15];
        accidentAccelerationMessage.setBehaviorType(behaviorType);
        int dataLength = length - 16;
        int dataCount = dataLength / 28;
        int beginIndex = 16;
        List<AccelerationData> accidentAccelerationList = new ArrayList<AccelerationData>();
        for (int i = 0 ; i < dataCount;i++){
            int curParseIndex = beginIndex + i * 28;
            AccelerationData accidentAcceleration = getAlarmAccelerationData(bytes,imei,curParseIndex);
            accidentAccelerationList.add(accidentAcceleration);
        }
        accidentAccelerationMessage.setAccelerationList(accidentAccelerationList);
        return accidentAccelerationMessage;
    }

    private AccelerationDriverBehaviorMessage parseAccelerationDriverBehaviorMessage(byte[] bytes) {
        int counter = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        AccelerationDriverBehaviorMessage message = new AccelerationDriverBehaviorMessage();
        int length = BytesUtils.bytes2Short(bytes, 3);
        int behavior = (int)bytes[15];
//        boolean isNeedResp = (counter & 0x8000) != 0x8000;
        message.setSerialNo(counter);
//        message.setIsNeedResp(isNeedResp);
        message.setImei(imei);
        message.setOrignBytes(bytes);
        message.setBehaviorType(behavior);

        int beginIndex = 16;
        AccelerationData acceleration = getAccelerationData(bytes, imei, beginIndex);
        message.setAccelerationData(acceleration);
        return message;
    }

    private AccelerationData getAccelerationData(byte[] bytes, String imei, int curParseIndex) {
        AccelerationData acceleration = new AccelerationData();
        acceleration.setImei(imei);
        try{
            acceleration.setDate(TimeUtils.getGTM0Date(bytes, curParseIndex));
        }catch (Exception e){
            acceleration.setDate(new Date());
            System.out.println("AccelerationData Error: imei " + imei + "data" + BytesUtils.bytes2HexString(bytes,curParseIndex));
        }
        boolean isGpsWorking = (bytes[curParseIndex + 6] & 0x20) == 0x00;
        boolean isHistoryData = (bytes[curParseIndex + 6] & 0x80) != 0x00;
        int satelliteNumber = bytes[curParseIndex + 6] & 0x1F;
        boolean latlngValid = (bytes[curParseIndex + 6] & 0x40) != 0x00;
        acceleration.setIsHistoryData(isHistoryData);
        acceleration.setGpsWorking(isGpsWorking);
        acceleration.setSatelliteNumber(satelliteNumber);
        acceleration.setLatlngValid(latlngValid);
        int axisXDirect = (bytes[curParseIndex + 7] & 0x80) == 0x80 ? 1 : -1;
        float axisX = ((bytes[curParseIndex + 7] & 0x7F & 0xff) + (((bytes[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0f) * axisXDirect;
        acceleration.setAxisX(axisX);
        int axisYDirect = (bytes[curParseIndex + 8] & 0x08) == 0x08 ? 1 : -1;
        float axisY = (((((bytes[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((bytes[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (bytes[curParseIndex + 9] & 0x0F & 0xff)/10.0f)* axisYDirect;
        acceleration.setAxisY(axisY);
        int axisZDirect = (bytes[curParseIndex + 10] & 0x80) == 0x80 ? 1 : -1;
        float axisZ = ((bytes[curParseIndex + 10] & 0x7F & 0xff) + (((bytes[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0f) * axisZDirect;
        acceleration.setAxisZ(axisZ);

        acceleration.setAltitude(BytesUtils.bytes2Float(bytes, curParseIndex + 12));
        acceleration.setLongitude(BytesUtils.bytes2Float(bytes, curParseIndex + 16));
        acceleration.setLatitude(BytesUtils.bytes2Float(bytes, curParseIndex + 20));
        byte[] bytesSpeed = Arrays.copyOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        float speedf = 0;
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        try {
            speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        }catch (Exception e){
            System.out.println("GPS Acceleration Speed Error ; imei is :" + imei);
        }
        acceleration.setSpeed(speedf);
        int azimuth = 0;
        try {
            azimuth = BytesUtils.bytes2Short(bytes, curParseIndex + 26);
        }catch (Exception e){
            System.out.println("GPS Acceleration azimuth Error ; imei is :" + imei + ":" + BytesUtils.bytes2HexString(bytes,0));
        }
        acceleration.setAzimuth(azimuth);
        if(bytes.length > 44){
            int gyroscopeAxisXDirect = (bytes[curParseIndex + 28] & 0x80) == 0x80 ? 1 : -1;
            float gyroscopeAxisX = (((bytes[curParseIndex + 28] & 0x7F & 0xff) << 4) + ((bytes[curParseIndex + 29] & 0xf0) >> 4)) * gyroscopeAxisXDirect;
            acceleration.setGyroscopeAxisX(gyroscopeAxisX);
            int gyroscopeAxisYDirect = (bytes[curParseIndex + 29] & 0x08) == 0x08 ? 1 : -1;
            float gyroscopeAxisY = ((((bytes[curParseIndex + 29] & 0x07) << 4) & 0xff) + (bytes[curParseIndex + 30] &  0xff))* gyroscopeAxisYDirect;
            acceleration.setGyroscopeAxisY(gyroscopeAxisY);
            int gyroscopeAxisZDirect = (bytes[curParseIndex + 31] & 0x80) == 0x80 ? 1 : -1;
            float gyroscopeAxisZ = (((bytes[curParseIndex + 31] & 0x7F & 0xff)<< 4) + ((bytes[curParseIndex + 32] & 0xf0) >> 4)) * gyroscopeAxisZDirect;
            acceleration.setGyroscopeAxisZ(gyroscopeAxisZ);
        }
        return acceleration;
    }

    private AccelerationData getAlarmAccelerationData(byte[] bytes, String imei, int curParseIndex) {
        AccelerationData acceleration = new AccelerationData();
        acceleration.setImei(imei);
        try{
            acceleration.setDate(TimeUtils.getGTM0Date(bytes, curParseIndex));
        }catch (Exception e){
            acceleration.setDate(new Date());
            System.out.println("AccelerationData Error: imei " + imei + "data" + BytesUtils.bytes2HexString(bytes,curParseIndex));
        }
        boolean isGpsWorking = (bytes[curParseIndex + 6] & 0x20) == 0x00;
        boolean isHistoryData = (bytes[curParseIndex + 6] & 0x80) != 0x00;
        int satelliteNumber = bytes[curParseIndex + 6] & 0x1F;
        boolean latlngValid = (bytes[curParseIndex + 6] & 0x40) != 0x00;
        acceleration.setIsHistoryData(isHistoryData);
        acceleration.setGpsWorking(isGpsWorking);
        acceleration.setSatelliteNumber(satelliteNumber);
        acceleration.setLatlngValid(latlngValid);
        int axisXDirect = (bytes[curParseIndex + 7] & 0x80) == 0x80 ? 1 : -1;
        float axisX = ((bytes[curParseIndex + 7] & 0x7F & 0xff) + (((bytes[curParseIndex + 8] & 0xf0) >> 4) & 0xff) /10.0f) * axisXDirect;
        acceleration.setAxisX(axisX);
        int axisYDirect = (bytes[curParseIndex + 8] & 0x08) == 0x08 ? 1 : -1;
        float axisY = (((((bytes[curParseIndex + 8] & 0x07) << 4) & 0xff) + (((bytes[curParseIndex + 9] & 0xf0) >> 4) & 0xff)) + (bytes[curParseIndex + 9] & 0x0F & 0xff)/10.0f)* axisYDirect;
        acceleration.setAxisY(axisY);
        int axisZDirect = (bytes[curParseIndex + 10] & 0x80) == 0x80 ? 1 : -1;
        float axisZ = ((bytes[curParseIndex + 10] & 0x7F & 0xff) + (((bytes[curParseIndex + 11] & 0xf0) >> 4) & 0xff) /10.0f) * axisZDirect;
        acceleration.setAxisZ(axisZ);

        acceleration.setAltitude(BytesUtils.bytes2Float(bytes, curParseIndex + 12));
        acceleration.setLongitude(BytesUtils.bytes2Float(bytes, curParseIndex + 16));
        acceleration.setLatitude(BytesUtils.bytes2Float(bytes, curParseIndex + 20));
        byte[] bytesSpeed = Arrays.copyOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        float speedf = 0;
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        try {
            speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        }catch (Exception e){
            System.out.println("GPS Acceleration Speed Error ; imei is :" + imei);
        }
        acceleration.setSpeed(speedf);
        int azimuth = 0;
        try {
            azimuth = BytesUtils.bytes2Short(bytes, curParseIndex + 26);
        }catch (Exception e){
            System.out.println("GPS Acceleration azimuth Error ; imei is :" + imei + ":" + BytesUtils.bytes2HexString(bytes,0));
        }
        acceleration.setAzimuth(azimuth);

        return acceleration;
    }
    private NetworkInfoMessage parseNetworkInfoMessage(byte[] bytes) {
        NetworkInfoMessage networkInfoMessage = new NetworkInfoMessage();
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        Date gmt0 = null;
        try{
            gmt0 = TimeUtils.getGTM0Date(bytes, 15);
        }catch (Exception e){
            System.out.println("Date Error:" + imei + " data:" + BytesUtils.bytes2HexString(bytes,0));
            return null;
        }
        int networkOperatorLen = bytes[21];
        int networkOperatorStartIndex = 22;
        byte[] networkOperatorByte = Arrays.copyOfRange(bytes, networkOperatorStartIndex, networkOperatorStartIndex + networkOperatorLen);
        String networkOperator = new String(networkOperatorByte, Charset.forName("UTF-16LE"));
        int accessTechnologyLen = bytes[networkOperatorStartIndex + networkOperatorLen];
        int accessTechnologyStartIndex = networkOperatorStartIndex + networkOperatorLen + 1;
        byte[] accessTechnologyByte = Arrays.copyOfRange(bytes, accessTechnologyStartIndex,accessTechnologyStartIndex + accessTechnologyLen);
        String accessTechnology = new String(accessTechnologyByte);
        int bandLen = bytes[accessTechnologyStartIndex + accessTechnologyLen];
        int bandStartIndex = accessTechnologyStartIndex + accessTechnologyLen + 1;
        byte[] bandLenByte = Arrays.copyOfRange(bytes, bandStartIndex,bandStartIndex + bandLen);
        String band = new String(bandLenByte);
        int msgLen = BytesUtils.bytes2Short(bytes,3);
        if(msgLen > bandStartIndex + bandLen ){
            int IMSILen = bytes[bandStartIndex + bandLen];
            int IMSIStartIndex = bandStartIndex + bandLen + 1;
            byte[] IMSILenByte = Arrays.copyOfRange(bytes,IMSIStartIndex,IMSIStartIndex + IMSILen);
            String IMSI = new String(IMSILenByte);
            networkInfoMessage.setImsi(IMSI);
            if(msgLen > IMSIStartIndex + IMSILen){
                int iccidLen = bytes[IMSIStartIndex + IMSILen];
                int iccidStartIndex = IMSIStartIndex + IMSILen + 1;
                byte[] iccidLenByte = Arrays.copyOfRange(bytes,iccidStartIndex,iccidStartIndex + iccidLen);
                String iccid = new String(iccidLenByte);
                networkInfoMessage.setIccid(iccid);
            }
        }
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        networkInfoMessage.setSerialNo(serialNo);
//        networkInfoMessage.setIsNeedResp(isNeedResp);
        networkInfoMessage.setImei(imei);
        networkInfoMessage.setOrignBytes(bytes);
        networkInfoMessage.setDate(gmt0);
        networkInfoMessage.setAccessTechnology(accessTechnology);
        networkInfoMessage.setNetworkOperator(networkOperator);
        networkInfoMessage.setBand(band);

        return networkInfoMessage;
    }
    private LocationMessage parseDataMessage(byte[] bytes) throws ParseException {
        byte[] command = Arrays.copyOf(bytes, HEADER_LENGTH);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        byte[] data = Arrays.copyOfRange(bytes, 15, bytes.length);
        int samplingIntervalAccOn = BytesUtils.bytes2Short(data, 0);
        int samplingIntervalAccOff = BytesUtils.bytes2Short(data, 2);
        int angleCompensation = (int) (data[4] & 0xff);
        int distanceCompensation = BytesUtils.bytes2Short(data, 5);
        short limit = (short)BytesUtils.bytes2Short(data, 7);
        int speed = ((0x7F80 & limit)) >> 7;
        if ((0x8000 & limit) != 0) {
            speed *= 1.609344;
        }
        int networkSignal = limit & 0x7F;
        boolean isGpsWorking = (data[9] & 0x20) == 0x00;
        boolean isHistoryData = (data[9] & 0x80) != 0x00;
        int satelliteNumber = data[9] & 0x1F;
        int gSensorSensitivity = (data[10] & 0xF0) >> 4;
        boolean isManagerConfigured1 = (data[10] & 0x01) != 0x00;
        boolean isManagerConfigured2 = (data[10] & 0x02) != 0x00;
        boolean isManagerConfigured3 = (data[10] & 0x04) != 0x00;
        boolean isManagerConfigured4 = (data[10] & 0x08) != 0x00;
        int antitheftedStatus = (data[11] & 0x10) != 0x00 ? 1 : 0;
        int heartbeatInterval = data[12] & 0x00FF;
        boolean isRelayWorking = (data[13] & 0xC0) == 0xC0;
        int relayStatus = isRelayWorking ? 1 : 0;
        boolean isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);

        int dragThreshold = BytesUtils.bytes2Short(data, 14);

        long iop = (long) BytesUtils.bytes2Short(data, 16);
        boolean iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
        boolean iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
        boolean iopACOn = (iop & MASK_AC) == MASK_AC;
        boolean iopRs232DeviceValid = (iop & IOP_RS232_DEVICE_VALID) != IOP_RS232_DEVICE_VALID;
        boolean iopRelay =(iop & MASK_RELAY) == MASK_RELAY;
        int input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
        int input4 = (iop & 0x800) == 0x800 ? 1 : 0;
        int output2 = (iop & 0x200) == 0x200 ? 1 : 0;
        int output3 = (iop & 0x100) == 0x100 ? 1 : 0;
        boolean output12V = (iop & 0x10) == 0x10;
        boolean outputVout = (iop & 0x8) == 0x8;
        String str = BytesUtils.bytes2HexString(data, 18);
        float analoginput = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput  = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            } catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput = -999;
        }

        str = BytesUtils.bytes2HexString(data, 20);
        float analoginput2 = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput2 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            }catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput2 = -999;
        }


        byte alarmByte = data[22];
        int originalAlarmCode = (int) alarmByte;
        boolean isAlarmData = command[2] == 0x04;
        long mileage =  BytesUtils.unsigned4BytesToInt(data, 24);
        byte[] batteryBytes = new byte[]{data[28]};
        String batteryStr = BytesUtils.bytes2HexString(batteryBytes, 0);
        Date gmt0 = null;
        try{
            gmt0 = TimeUtils.getGTM0Date(data, 29);
        }catch (Exception e){
            System.out.println("Date Error:" + imei + " data:" + BytesUtils.bytes2HexString(bytes,0));
            return null;
        }
        boolean latlngValid = (data[9] & 0x40) != 0x00;
        byte[] latlngData = Arrays.copyOfRange(data,35,51);
        if (Arrays.equals(latlngData,latlngInvalidData)){
            latlngValid = false;
        }
        double altitude = latlngValid? BytesUtils.bytes2Float(data, 35) : 0.0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(data, 43) : 0.0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(data, 39) : 0.0;
        Float speedf = 0.0f;
        try{
            if (latlngValid) {
                byte[] bytesSpeed = Arrays.copyOfRange(data, 47, 49);
                String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }
        }catch (Exception e){
            System.out.println("Imei : " + imei);
            e.printStackTrace();
        }
        int azimuth = latlngValid ? BytesUtils.bytes2Short(data, 49) : 0;

        Float externalPowerVoltage = 0f;
        if (data.length >=53) {
            byte[] externalPowerVoltageBytes = Arrays.copyOfRange(data, 51, 53);
            String externalPowerVoltageStr = BytesUtils.bytes2HexString(externalPowerVoltageBytes, 0);
            try{
                externalPowerVoltage = Float.valueOf(externalPowerVoltageStr) / 100.0f;
            }catch (Exception e){
                System.out.println("externalPowerVoltage error, imei = " + imei + " externalPowerVoltage :" + externalPowerVoltageStr);
            }
        }
        float analogInput3 = 0;
        int rpm = 0;
        if(data.length >= 55){
            byte adc2Din = data[53];
            byte[] analogInput3Bytes = Arrays.copyOfRange(data,54,56);
            str = BytesUtils.bytes2HexString(analogInput3Bytes, 0);

            if (!str.toLowerCase().equals("ffff")){
                analogInput3 = -999;
            }else{
                try {
                    analogInput3 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
                }catch (Exception e){
//            e.printStackTrace();
                }
            }


            byte[] rpmBytes = Arrays.copyOfRange(data, 56, 58);
            rpm = BytesUtils.bytes2Short(rpmBytes,0);

        }
        LocationMessage message;
        if (isAlarmData){
            message = new LocationAlarmMessage();
            message.setProtocolHeadType(0x4);
        }else {
            message = new LocationInfoMessage();
            message.setProtocolHeadType(0x2);
        }
        message.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        message.setSerialNo(serialNo);
//        message.setIsNeedResp(isNeedResp);
        message.setImei(imei);
        message.setNetworkSignal(networkSignal);
        message.setSamplingIntervalAccOn(samplingIntervalAccOn);
        message.setSamplingIntervalAccOff(samplingIntervalAccOff);
        message.setAngleCompensation(angleCompensation);
        message.setDistanceCompensation(distanceCompensation);
        message.setOverSpeedLimit((int) speed);// 统一单位为 km/h
        message.setGpsWorking(isGpsWorking);
        message.setIsHistoryData(isHistoryData);
        message.setSatelliteNumber(satelliteNumber);
        message.setgSensorSensitivity(gSensorSensitivity);
        message.setIsManagerConfigured1(isManagerConfigured1);
        message.setIsManagerConfigured2(isManagerConfigured2);
        message.setIsManagerConfigured3(isManagerConfigured3);
        message.setIsManagerConfigured4(isManagerConfigured4);
        message.setAntitheftedStatus(antitheftedStatus);
        message.setHeartbeatInterval(heartbeatInterval);
        message.setRelayStatus(iopRelay ? 1 : 0);
        message.setIsRelayWaiting(isRelayWaiting);
        message.setDragThreshold(dragThreshold);
        message.setIOP(iop);
        message.setIopIgnition(iopIgnition);
        message.setIopPowerCutOff(iopPowerCutOff);
        message.setIopACOn(iopACOn);
        message.setRs232DeviceValid(iopRs232DeviceValid);
        message.setAnalogInput1(analoginput);
        message.setAnalogInput2(analoginput2);
        message.setOriginalAlarmCode(originalAlarmCode);
        message.setAlarm(Event.getEvent(alarmByte));
        message.setMileage(mileage);
        message.setOutput12V(output12V);
        message.setOutput2(output2);
        message.setOutput3(output3);
        message.setInput3(input3);
        message.setInput4(input4);
        message.setOutputVout(outputVout);
        message.setAnalogInput3(analogInput3);
        message.setRpm(rpm);
        try{
            int charge = Integer.parseInt(batteryStr);
            if (0 == charge) {
                charge = 100;
            }
            message.setBatteryCharge(charge);
        }catch (Exception e){
            throw new ParseException("parse battery error !!! imei :" + imei + "error str :" + batteryStr,0);
        }
        message.setDate(gmt0);
        message.setLatlngValid(latlngValid);
        message.setAltitude(altitude);
        message.setLatitude(latitude);
        message.setLongitude(longitude);
        if(message.isLatlngValid()) {
            message.setSpeed(speedf);
        } else {
            message.setSpeed(0.0f);
        }
        message.setAzimuth(azimuth);
        message.setExternalPowerVoltage(externalPowerVoltage);

        return message;
    }
    private LocationMessage parseSecondDataMessage(byte[] bytes) throws ParseException {
        byte[] command = Arrays.copyOf(bytes, HEADER_LENGTH);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        byte[] data = Arrays.copyOfRange(bytes, 15, bytes.length);
        int samplingIntervalAccOn = BytesUtils.bytes2Short(data, 0);
        int samplingIntervalAccOff = BytesUtils.bytes2Short(data, 2);
        int angleCompensation = (int) (data[4] & 0xff);
        int distanceCompensation = BytesUtils.bytes2Short(data, 5);
        short limit = (short)BytesUtils.bytes2Short(data, 7);
        int speed = ((0x7F80 & limit)) >> 7;
        if ((0x8000 & limit) != 0) {
            speed *= 1.609344;
        }
        int networkSignal = limit & 0x7F;
        boolean isGpsWorking = (data[9] & 0x20) == 0x00;
        boolean isHistoryData = (data[9] & 0x80) != 0x00;
        int satelliteNumber = data[9] & 0x1F;
        int gSensorSensitivity = (data[10] & 0xF0) >> 4;
        boolean isManagerConfigured1 = (data[10] & 0x01) != 0x00;
        boolean isManagerConfigured2 = (data[10] & 0x02) != 0x00;
        boolean isManagerConfigured3 = (data[10] & 0x04) != 0x00;
        boolean isManagerConfigured4 = (data[10] & 0x08) != 0x00;
        int antitheftedStatus = (data[11] & 0x10) != 0x00 ? 1 : 0;
        int heartbeatInterval = data[12] & 0x00FF;
        boolean isRelayWorking = (data[13] & 0xC0) == 0xC0;
        int relayStatus = isRelayWorking ? 1 : 0;
        boolean isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);

        int dragThreshold = BytesUtils.bytes2Short(data, 14);

        long iop = (long) BytesUtils.bytes2Short(data, 16);
        boolean iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
        boolean iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
        boolean iopACOn = (iop & MASK_AC) == MASK_AC;

        int input3 = (iop & 0x1000) == 0x1000 ? 1 : 0;
        int input4 = (iop & 0x800) == 0x800 ? 1 : 0;
        int input5 = (iop & 0x400) == 0x400 ? 1 : 0;
        int input6 = (iop & 0x200) == 0x200 ? 1 : 0;
        int output1 = (data[18] & 0x10) == 0x10 ? 1 : 0;
        int output2 = (data[18] & 0x8) == 0x8 ? 1 : 0;
        int output3 = (data[18] & 0x4) == 0x4 ? 1 : 0;
        boolean output12V = (data[18] & 0x20) == 0x20 ;
        boolean outputVout = (data[18] & 0x40) == 0x40;
        String str = BytesUtils.bytes2HexString(data, 20);
        float analoginput = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput  = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            } catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput = -999;
        }

        str = BytesUtils.bytes2HexString(data, 22);
        float analoginput2 = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput2 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            }catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput2 = -999;
        }

        str = BytesUtils.bytes2HexString(data, 24);
        float analoginput3 = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput3 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            }catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput3 = -999;
        }

        str = BytesUtils.bytes2HexString(data, 26);
        float analoginput4 = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput4 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            }catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput4 = -999;
        }

        str = BytesUtils.bytes2HexString(data, 28);
        float analoginput5 = 0;
        if (!str.toLowerCase().equals("ffff")){
            try {
                analoginput5 = Float.parseFloat(String.format("%d.%s", Integer.parseInt(str.substring(0, 2)),str.substring(2, 4)));
            }catch (Exception e){
//            e.printStackTrace();
            }
        }else{
            analoginput5 = -999;
        }

        byte alarmByte = data[30];
        int originalAlarmCode = (int) alarmByte;
        boolean isAlarmData = command[2] == 0x14;
        long mileage =  BytesUtils.unsigned4BytesToInt(data, 32);
        byte[] batteryBytes = new byte[]{data[36]};
        String batteryStr = BytesUtils.bytes2HexString(batteryBytes, 0);
        Date gmt0 = null;
        try{
            gmt0 = TimeUtils.getGTM0Date(data, 37);
        }catch (Exception e){
            System.out.println("Date Error:" + imei + " data:" + BytesUtils.bytes2HexString(bytes,0));
            return null;
        }
        boolean latlngValid = (data[9] & 0x40) != 0x00;
        byte[] latlngData = Arrays.copyOfRange(data, 43, 59);
        if (Arrays.equals(latlngData,latlngInvalidData)){
            latlngValid = false;
        }
        double altitude = latlngValid? BytesUtils.bytes2Float(data, 43) : 0.0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(data, 51) : 0.0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(data, 47) : 0.0;
        Float speedf = 0.0f;
        try{
            if (latlngValid) {
                byte[] bytesSpeed = Arrays.copyOfRange(data, 55, 57);
                String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }
        }catch (Exception e){
            System.out.println("Imei : " + imei);
            e.printStackTrace();
        }
        int azimuth = latlngValid ? BytesUtils.bytes2Short(data, 57) : 0;

        Float batteryVoltage = 0f;
        byte[] batteryVoltageBytes = Arrays.copyOfRange(data, 59, 61);
        String batteryVoltageStr = BytesUtils.bytes2HexString(batteryVoltageBytes, 0);
        try{
            batteryVoltage = Float.valueOf(batteryVoltageStr) / 100.0f;
        }catch (Exception e){
            System.out.println("batteryVoltage error, imei = " + imei + " batteryVoltage :" + batteryVoltageStr);
        }
        Float externalPowerVoltage = 0f;
        byte[] externalPowerVoltageBytes = Arrays.copyOfRange(data, 61, 63);
        String externalPowerVoltageStr = BytesUtils.bytes2HexString(externalPowerVoltageBytes, 0);
        try{
            externalPowerVoltage = Float.valueOf(externalPowerVoltageStr) / 100.0f;
        }catch (Exception e){
            System.out.println("externalPowerVoltage error, imei = " + imei + " externalPowerVoltage :" + externalPowerVoltageStr);
        }
        int rpm = 0;
        byte[] rpmBytes = Arrays.copyOfRange(data, 63, 65);
        rpm = BytesUtils.bytes2Short(rpmBytes,0);
        boolean isSmartUploadSupport = (data[65] & 0x8) == 0x8;
        boolean supportChangeBattery =  (data[66] & 0x8) == 0x8;
        float deviceTemp = -999;
        if (data.length >= 69){
            deviceTemp = (data[68] & 0x7F) * ((data[68] & 0x80) == 0x80 ? -1 : 1);
        }
        LocationMessage message;
        if (isAlarmData){
            message = new LocationAlarmMessage();
            message.setProtocolHeadType(0x14);
        }else {
            message = new LocationInfoMessage();
            message.setProtocolHeadType(0x13);
        }
        message.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        message.setSerialNo(serialNo);
//        message.setIsNeedResp(isNeedResp);
        message.setImei(imei);
        message.setNetworkSignal(networkSignal);
        message.setSamplingIntervalAccOn(samplingIntervalAccOn);
        message.setSamplingIntervalAccOff(samplingIntervalAccOff);
        message.setAngleCompensation(angleCompensation);
        message.setDistanceCompensation(distanceCompensation);
        message.setOverSpeedLimit((int) speed);// 统一单位为 km/h
        message.setGpsWorking(isGpsWorking);
        message.setIsHistoryData(isHistoryData);
        message.setSatelliteNumber(satelliteNumber);
        message.setgSensorSensitivity(gSensorSensitivity);
        message.setIsManagerConfigured1(isManagerConfigured1);
        message.setIsManagerConfigured2(isManagerConfigured2);
        message.setIsManagerConfigured3(isManagerConfigured3);
        message.setIsManagerConfigured4(isManagerConfigured4);
        message.setAntitheftedStatus(antitheftedStatus);
        message.setHeartbeatInterval(heartbeatInterval);
        message.setRelayStatus(output1);
        message.setIsRelayWaiting(isRelayWaiting);
        message.setDragThreshold(dragThreshold);
        message.setIOP(iop);
        message.setIopIgnition(iopIgnition);
        message.setIopPowerCutOff(iopPowerCutOff);
        message.setIopACOn(iopACOn);
        message.setAnalogInput1(analoginput);
        message.setAnalogInput2(analoginput2);
        message.setOriginalAlarmCode(originalAlarmCode);
        message.setAlarm(Event.getEvent(alarmByte));
        message.setMileage(mileage);
        message.setOutput12V(output12V);
        message.setOutput2(output2);
        message.setOutput3(output3);
        message.setInput3(input3);
        message.setInput4(input4);
        message.setOutputVout(outputVout);
        message.setAnalogInput3(analoginput3);
        message.setRpm(rpm);
        message.setDeviceTemp(deviceTemp);
        try{
            int charge = Integer.parseInt(batteryStr);
            if (0 == charge) {
                charge = 100;
            }
            message.setBatteryCharge(charge);
        }catch (Exception e){
            throw new ParseException("parse battery error !!! imei :" + imei + "error str :" + batteryStr,0);
        }
        message.setDate(gmt0);
        message.setLatlngValid(latlngValid);
        message.setAltitude(altitude);
        message.setLatitude(latitude);
        message.setLongitude(longitude);
        if(message.isLatlngValid()) {
            message.setSpeed(speedf);
        } else {
            message.setSpeed(0.0f);
        }
        message.setAzimuth(azimuth);
        message.setExternalPowerVoltage(externalPowerVoltage);
        message.setBatteryVoltage(batteryVoltage);
        message.setInput5(input5);
        message.setInput6(input6);
        message.setAnalogInput4(analoginput4);
        message.setAnalogInput5(analoginput5);
        message.setIsSmartUploadSupport(isSmartUploadSupport);
        message.setSupportChangeBattery(supportChangeBattery);
        return message;
    }

    /**
     * The interface Callback.
     */
    public interface Callback{
        /**
         * Receive sign in message.
         *
         * @param signInMessage the sign in message
         */
        void receiveSignInMessage(SignInMessage signInMessage);

        /**
         * Receive heartbeat message.
         *
         * @param heartbeatMessage the heartbeat message
         */
        void receiveHeartbeatMessage(HeartbeatMessage heartbeatMessage);

        /**
         * Receive location info message.
         *
         * @param locationInfoMessage the location info message
         */
        void receiveLocationInfoMessage(LocationInfoMessage locationInfoMessage);

        /**
         * Receive alarm message.
         *
         * @param alarmMessage the alarm message
         */
        void receiveAlarmMessage(LocationAlarmMessage alarmMessage);


        /**
         * Receive gps driver behavior message.
         *
         * @param gpsDriverBehaviorMessage the gps driver behavior message
         */
        void receiveGpsDriverBehaviorMessage(GpsDriverBehaviorMessage gpsDriverBehaviorMessage);

        /**
         * Receive acceleration driver behavior message.
         *
         * @param accelerationDriverBehaviorMessage the acceleration driver behavior message
         */
        void receiveAccelerationDriverBehaviorMessage(AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage);

        /**
         * Receive accident acceleration message.
         *
         * @param accidentAccelerationMessage the accident acceleration message
         */
        void receiveAccidentAccelerationMessage(AccidentAccelerationMessage accidentAccelerationMessage);

        /**
         * Receive config message.
         *
         * @param configMessage the config message
         */
        void receiveConfigMessage(ConfigMessage configMessage);

        /**
         * Receive forward message.
         * @param forwardMessage the forward message.
         */
        void receiveForwardMessage(ForwardMessage forwardMessage);


        /**
         * Receive USSD message.
         * @param ussdMessage the USSD message
         */
        void receiveUSSDMessage(USSDMessage ussdMessage);


        /**
         * receive RS232 message
         * @param rs232Message the RS232 message
         */
        void receiveRS232Message(RS232Message rs232Message);
        /**
         * receive bluetooth data message
         * @param bluetoothPeripheralDataMessage the bluetooth data message
         */
        void receiveBluetoothDataMessage(BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage);

        void receiveNetworkInfoMessage(NetworkInfoMessage networkInfoMessage);
        /**
         * Receive error message.
         *
         * @param msg the msg
         */
        void receiveErrorMessage(String msg);
        void receiveDebugMessage(DebugMessage debugMessage);
    }
}
