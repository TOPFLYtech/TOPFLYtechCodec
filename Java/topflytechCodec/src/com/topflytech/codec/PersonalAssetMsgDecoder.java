package com.topflytech.codec;

import com.topflytech.codec.entities.*;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2019/9/10.
 */
public class PersonalAssetMsgDecoder {
    private static final int HEADER_LENGTH = 3;

    private static final byte[] SIGNUP =      {0x27, 0x27, 0x01};

    private static final byte[] DATA =       {0x27, 0x27, 0x02};

    private static final byte[] HEARTBEAT =  {0x27, 0x27, 0x03};

    private static final byte[] ALARM =      {0x27, 0x27, 0x04};
    private static final byte[] CONFIG =          {0x27, 0x27, (byte)0x81};
    private static final byte[] NETWORK_INFO_DATA = {0x27, 0x27, 0x05};

    private int encryptType = 0;
    private String aesKey;
    public PersonalAssetMsgDecoder(int messageEncryptType,String aesKey){
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
                || Arrays.equals(CONFIG,bytes)
                || Arrays.equals(NETWORK_INFO_DATA,bytes);
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
                data = Crypto.decryptData(data, encryptType, aesKey);
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
                && (bytes[0] == 0x27 && bytes[1] == 0x27)) {
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
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    return message;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }
        return null;
    }

    private LocationMessage parseDataMessage(byte[] data) {
        int serialNo = BytesUtils.bytes2Short(data, 5);
        String imei = BytesUtils.IMEI.decode(data, 7);
        Date date = TimeUtils.getGTM0Date(data, 17);
        boolean isGpsWorking = (data[15] & 0x20) == 0x00;
        boolean isHistoryData = (data[15] & 0x80) != 0x00;
        boolean latlngValid = (data[15] & 0x40) == 0x40;
        int satelliteNumber = data[15] & 0x1F;
        double altitude = latlngValid ? BytesUtils.bytes2Float(data,23) : 0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(data,27) : 0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(data,31) : 0;
        Float speedf = 0.0f;
        try{
            if (latlngValid) {
                byte[] bytesSpeed = Arrays.copyOfRange(data, 35, 37);
                String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        int azimuth = latlngValid ? BytesUtils.bytes2Short(data, 37) : 0;
        int axisXDirect = (data[39] & 0x80) == 0x80 ? 1 : -1;
        float axisX = ((data[39] & 0x7F & 0xff) + (((data[40] & 0xf0) >> 4) & 0xff) /10.0f) * axisXDirect;

        int axisYDirect = (data[40] & 0x08) == 0x08 ? 1 : -1;
        float axisY = (((((data[40] & 0x07) << 4) & 0xff) + (((data[41] & 0xf0) >> 4) & 0xff)) + (data[41] & 0x0F & 0xff)/10.0f)* axisYDirect;

        int axisZDirect = (data[42] & 0x80) == 0x80 ? 1 : -1;
        float axisZ = ((data[42] & 0x7F & 0xff) + (((data[43] & 0xf0) >> 4) & 0xff) /10.0f) * axisZDirect;

        byte[] batteryPercentBytes = new byte[]{data[44]};
        String batteryPercentStr = BytesUtils.bytes2HexString(batteryPercentBytes, 0);
        int batteryPercent = 100;
        if(batteryPercentStr.toLowerCase().equals("ff")){
            batteryPercent = 100;
        }else{
            try{
                batteryPercent = Integer.parseInt(batteryPercentStr);
                if (0 == batteryPercent) {
                    batteryPercent = 100;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        float deviceTemp = (data[45] & 0x7F) * ((data[45] & 0x80) == 0x80 ? -1 : 1);
        byte[] lightSensorBytes = new byte[]{data[46]};
        String lightSensorStr = BytesUtils.bytes2HexString(lightSensorBytes, 0);
        float lightSensor = 0;
        if(lightSensorStr.toLowerCase().equals("ff")){
            lightSensor = -1;
        }else{
            try{
                lightSensor = Integer.parseInt(lightSensorStr) / 10.0f;
            }catch (Exception e){
                e.printStackTrace();            }

        }

        byte[] batteryVoltageBytes = new byte[]{data[47]};
        String batteryVoltageStr = BytesUtils.bytes2HexString(batteryVoltageBytes, 0);
        float batteryVoltage = 0;
        if(batteryVoltageStr.toLowerCase().equals("ff")){
            batteryVoltage = -1;
        }else{
            try{
                batteryVoltage = Integer.parseInt(batteryVoltageStr) / 10.0f;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        byte[] solarVoltageBytes = new byte[]{data[48]};
        String solarVoltageStr = BytesUtils.bytes2HexString(solarVoltageBytes, 0);
        float solarVoltage = 0;
        if(solarVoltageStr.toLowerCase().equals("ff")){
            solarVoltage = -1;
        }else{
            try{
                solarVoltage = Integer.parseInt(solarVoltageStr) / 10.0f;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        long mileage = BytesUtils.unsigned4BytesToInt(data, 49);
        int status = BytesUtils.bytes2Short(data, 53);
        int network = (status & 0x7F0) >> 4;
        int accOnInterval = BytesUtils.bytes2Short(data, 55);
        int accOffInterval = BytesUtils.bytes2Integer(data, 57);
        int angleCompensation = (int) data[61];
        int distanceCompensation = BytesUtils.bytes2Short(data, 62);
        int heartbeatInterval = (int) data[64];
        boolean isUsbCharging = (status & 0x8000) == 0x8000;
        boolean isSolarCharging = (status & 0x8) == 0x8;
        boolean iopIgnition = (status & 0x4) == 0x4;
        byte alarmByte = data[16];
        int originalAlarmCode = (int) alarmByte;
        byte[] command = Arrays.copyOf(data, HEADER_LENGTH);
        boolean isAlarmData = command[2] == 0x04;
        LocationMessage locationMessage;
        if (isAlarmData){
            locationMessage = new LocationAlarmMessage();
        }else {
            locationMessage = new LocationInfoMessage();
        }
        locationMessage.setOrignBytes(data);
        locationMessage.setSerialNo(serialNo);
        locationMessage.setNetworkSignal(network);
        locationMessage.setImei(imei);
        locationMessage.setIsSolarCharging(isSolarCharging);
        locationMessage.setIsUsbCharging(isUsbCharging);
        locationMessage.setSamplingIntervalAccOn(accOnInterval);
        locationMessage.setSamplingIntervalAccOff(accOffInterval);
        locationMessage.setAngleCompensation(angleCompensation);
        locationMessage.setDistanceCompensation(distanceCompensation);
        locationMessage.setGpsWorking(isGpsWorking);
        locationMessage.setIsHistoryData(isHistoryData);
        locationMessage.setSatelliteNumber(satelliteNumber);
        locationMessage.setHeartbeatInterval(heartbeatInterval);
        locationMessage.setOriginalAlarmCode(originalAlarmCode);
        locationMessage.setAlarm(getEvent(alarmByte));
        locationMessage.setMileage(mileage);
        locationMessage.setBatteryCharge(batteryPercent);
        locationMessage.setDate(date);
        locationMessage.setLatlngValid(latlngValid);
        locationMessage.setAltitude(altitude);
        locationMessage.setLatitude(latitude);
        locationMessage.setLongitude(longitude);
        if(locationMessage.isLatlngValid()) {
            locationMessage.setSpeed(speedf);
        } else {
            locationMessage.setSpeed(0.0f);
        }
        locationMessage.setAzimuth(azimuth);
        locationMessage.setAxisX(axisX);
        locationMessage.setAxisY(axisY);
        locationMessage.setAxisZ(axisZ);
        locationMessage.setDeviceTemp(deviceTemp);
        locationMessage.setLightSensor(lightSensor);
        locationMessage.setBatteryVoltage(batteryVoltage);
        locationMessage.setSolarVoltage(solarVoltage);
        return locationMessage;
    }
    private static int getEvent(byte alarmCodeByte) {
        if(alarmCodeByte == 0x01){
            return Event.ALARM_DEVICE_REMOVE;
        }else if(alarmCodeByte == 0x02){
            return Event.ALARM_DEVICE_CASE_OPEN;
        }else if(alarmCodeByte == 0x03){
            return Event.ALARM_SOS;
        }else if(alarmCodeByte == 0x04){
            return Event.ALARM_BOX_OPEN;
        }else if(alarmCodeByte == 0x05){
            return Event.ALARM_FALL_DOWN;
        }else if(alarmCodeByte == 0x06){
            return Event.ALARM_LOW_BATTERY;
        }else if(alarmCodeByte == 0x07){
            return Event.ALARM_BATTERY_POWER_RECOVER;
        }else if(alarmCodeByte == 0x08){
            return Event.ALARM_INNER_TEMP_HIGH;
        }else if(alarmCodeByte == 0x09){
            return Event.ALARM_MOVE;
        }else if(alarmCodeByte == 0x10){
            return Event.ALARM_COLLISION;
        }else if(alarmCodeByte == 0x11){
            return Event.ALARM_INCLINE;
        }else if(alarmCodeByte == 0x12){
            return Event.ALARM_USB_RECHARGE_START;
        }else if(alarmCodeByte == 0x13){
            return Event.ALARM_USB_RECHARGE_END;
        }else if(alarmCodeByte == 0x14){
            return Event.ALARM_GEOFENCE_IN;
        }else if(alarmCodeByte == 0x15){
            return Event.ALARM_GEOFENCE_OUT;
        }else if(alarmCodeByte == 0x16){
            return Event.IGNITION;
        }else if(alarmCodeByte == 0x17){
            return Event.PARKING;
        }else if(alarmCodeByte == 0x18){
            return Event.IDLE_START;
        }else if(alarmCodeByte == 0x19){
            return Event.IDLE_END;
        }else if(alarmCodeByte == 0x20){
            return Event.ADDRESS_REQUESTED;
        }else if(alarmCodeByte == 0x21){
            return Event.ALARM_DEVICE_MOUNTED;
        }else if(alarmCodeByte == 0x22){
            return Event.ALARM_DEVICE_CASE_CLOSED;
        }else if(alarmCodeByte == 0x23){
            return Event.ALARM_BOX_CLOSED;
        }else if(alarmCodeByte == 0x24){
            return Event.ALARM_FALL_DOWN_REC;
        }else if(alarmCodeByte == 0x25){
            return Event.ALARM_INNER_TEMP_HIGH_REC;
        }else if(alarmCodeByte == 0x26){
            return Event.ALARM_MOVE_REC;
        }else if(alarmCodeByte == 0x27){
            return Event.ALARM_COLLISION_REC;
        }else if(alarmCodeByte == 0x28){
            return Event.ALARM_INCLINE_REC;
        }else if(alarmCodeByte == 0x29){
            return Event.ALARM_POWER_ON;
        }else if(alarmCodeByte == 0x30){
            return Event.ALARM_INNER_TEMP_LOW;
        }else if(alarmCodeByte == 0x31){
            return Event.ALARM_INNER_TEMP_LOW_REC;
        }
        return Event.NONE;
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
                case 0x05:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    callback.receiveNetworkInfoMessage(networkInfoMessage);
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    if (message instanceof ConfigMessage) {
                        callback.receiveConfigMessage((ConfigMessage)message);
                    }else if(message instanceof ForwardMessage){
                        callback.receiveForwardMessage((ForwardMessage) message);
                    }else if(message instanceof USSDMessage){
                        callback.receiveUSSDMessage((USSDMessage)message);
                    }
                default:
                    throw new ParseException("The message type error!",0);
            }
        }

    }

    private SignInMessage parseLoginMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        String software = String.format("V%d.%d.%d",  bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4, bytes[16] & 0xf);
        String firmware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 20, 22), 0);
        firmware = String.format("V%s.%s.%s", firmware.substring(0,1), firmware.substring(1, 2), firmware.substring(2,4));
        String hardware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 22, 23), 0);
        hardware = String.format("V%s.%s", hardware.substring(0,1), hardware.substring(1, 2));
        SignInMessage signInMessage = new SignInMessage();
        signInMessage.setSerialNo(serialNo);
        signInMessage.setImei(imei);
        signInMessage.setSoftware(software);
        signInMessage.setFirmware(firmware);
        signInMessage.setHareware(hardware);
        signInMessage.setOrignBytes(bytes);
        return signInMessage;
    }
    private HeartbeatMessage parseHeartbeat(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
        heartbeatMessage.setOrignBytes(bytes);
        heartbeatMessage.setSerialNo(serialNo);
        heartbeatMessage.setImei(imei);
        return heartbeatMessage;
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
        networkInfoMessage.setSerialNo(serialNo);
        networkInfoMessage.setImei(imei);
        networkInfoMessage.setOrignBytes(bytes);
        networkInfoMessage.setDate(gmt0);
        networkInfoMessage.setAccessTechnology(accessTechnology);
        networkInfoMessage.setNetworkOperator(networkOperator);
        networkInfoMessage.setBand(band);

        return networkInfoMessage;
    }

    private Message parseInteractMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
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
                return configMessage;
            }
            case 0x03:{
                ForwardMessage forwardMessage = new ForwardMessage();
                messageData = new String(data, Charset.forName("UTF-16LE"));
                forwardMessage.setContent(messageData);
                forwardMessage.setOrignBytes(bytes);
                forwardMessage.setImei(imei);
                forwardMessage.setSerialNo(serialNo);
                return forwardMessage;
            }case 0x05:{
                USSDMessage ussdMessage = new USSDMessage();
                messageData = new String(data, Charset.forName("UTF-16LE"));
                ussdMessage.setContent(messageData);
                ussdMessage.setOrignBytes(bytes);
                ussdMessage.setImei(imei);
                ussdMessage.setSerialNo(serialNo);
                return ussdMessage;
            }
            default:
            {
                System.out.println("Error config message:" + BytesUtils.bytes2HexString(bytes,0));
                return null;
            }
        }

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

        void receiveNetworkInfoMessage(NetworkInfoMessage networkInfoMessage);
        /**
         * Receive error message.
         *
         * @param msg the msg
         */
        void receiveErrorMessage(String msg);
    }
}
