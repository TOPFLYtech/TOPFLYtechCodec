package com.topflytech.codec;

import com.topflytech.codec.entities.*;
import jdk.nashorn.internal.runtime.Debug;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;



/**
 * The type Decoder.
 */
public class ObdDecoder {


    private static final int HEADER_LENGTH = 3;


    private static final byte[] SIGNUP =      {0x26, 0x26, 0x01};

    private static final byte[] DATA =       {0x26, 0x26, 0x02};

    private static final byte[] HEARTBEAT =  {0x26, 0x26, 0x03};

    private static final byte[] ALARM =      {0x26, 0x26, 0x04};

    private static final byte[] CONFIG =          {0x26, 0x26, (byte)0x81};
    private static final byte[] GPS_DRIVER_BEHAVIOR = {0x26, 0x26, (byte)0x05};
    private static final byte[] ACCELERATION_DRIVER_BEHAVIOR =       {0x26, 0x26, (byte)0x06};
    private static final byte[] ACCELERATION_ALARM =       {0x26, 0x26, (byte)0x07};
    private static final byte[] BLUETOOTH_MAC =       {0x26, 0x26, (byte)0x08};
    private static final byte[] OBD_DATA =       {0x26, 0x26, (byte)0x09};
    private static final byte[] BLUETOOTH_DATA =       {0x26, 0x26, (byte)0x10};
    private static final byte[] NETWORK_INFO_DATA = {0x26, 0x26, (byte)0x11};
    private static final byte[] BLUETOOTH_SECOND_DATA =  {0x26, 0x26, (byte)0x12};
    private static final byte[] latlngInvalidData = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};

    private static final byte[] obdHead = {0x55,(byte)0xAA};
    private static final byte[] LOCATION_DATA_WITH_SENSOR =  {0x26, 0x26, (byte)0x16};
    private static final byte[] LOCATION_ALARM_WITH_SENSOR =  {0x26, 0x26, (byte)0x18};
    private static final byte[] WIFI_DATA =  {0x26, 0x26, (byte)0x15};

    private static final byte[] INDEFINITE_LOCATION_DATA =  {0x26, 0x26, (byte)0x62};
    private static final byte[] INDEFINITE_LOCATION_ALARM_DATA =  {0x26, 0x26, (byte)0x64};
    private int encryptType = 0;
    private String aesKey;
    private static final long MASK_IGNITION =   0x4000;
    private static final long MASK_POWER_CUT =  0x8000;
    private static final long MASK_AC       =   0x2000;
    private static final long MASK_RELAY = 0x400;
    /**
     * Instantiates a new Decoder.
     *
     * @param messageEncryptType The message encrypt type .Use the value of MessageEncryptType.
     * @param aesKey             The aes key.If you do not use AES encryption, the value can be empty.
     */
    public ObdDecoder(int messageEncryptType, String aesKey){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
    }
    private TopflytechByteBuf decoderBuf = new TopflytechByteBuf();
    public ObdDecoder(int messageEncryptType,String aesKey,int buffSize){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
        this.decoderBuf = new TopflytechByteBuf(buffSize);
    }

    private boolean matchNewCryptData(byte[] bytes){
        assert bytes.length >= HEADER_LENGTH : "command match: length is not 3!";
        return  Arrays.equals(INDEFINITE_LOCATION_DATA,bytes)
                || Arrays.equals(INDEFINITE_LOCATION_ALARM_DATA,bytes);
    }

    private static boolean match(byte[] bytes) {
        assert bytes.length >= HEADER_LENGTH : "command match: length is not 3!";
        return     Arrays.equals(SIGNUP, bytes)
                || Arrays.equals(HEARTBEAT, bytes)
                || Arrays.equals(DATA, bytes)
                || Arrays.equals(ALARM, bytes)
                || Arrays.equals(CONFIG, bytes)
                || Arrays.equals(GPS_DRIVER_BEHAVIOR, bytes)
                || Arrays.equals(ACCELERATION_DRIVER_BEHAVIOR, bytes)
                || Arrays.equals(ACCELERATION_ALARM, bytes)
                || Arrays.equals(BLUETOOTH_MAC, bytes)
                || Arrays.equals(OBD_DATA, bytes)
                || Arrays.equals(BLUETOOTH_DATA, bytes)
                || Arrays.equals(BLUETOOTH_SECOND_DATA, bytes)
                || Arrays.equals(NETWORK_INFO_DATA,bytes)
                || Arrays.equals(LOCATION_DATA_WITH_SENSOR,bytes)
                || Arrays.equals(LOCATION_ALARM_WITH_SENSOR,bytes)
                || Arrays.equals(WIFI_DATA,bytes)
                || Arrays.equals(INDEFINITE_LOCATION_DATA,bytes)
                || Arrays.equals(INDEFINITE_LOCATION_ALARM_DATA,bytes)
                ;
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
                if(matchNewCryptData(bytes)){
                    if(decoderBuf.getReadableBytes() <= 12){
                        decoderBuf.resetReaderIndex();
                        callback.receiveErrorMessage("Error Message");
                        return;
                    }
                    decoderBuf.skipBytes(10);
                    byte[] msgInfoBytes = decoderBuf.readBytes(2);
                    byte msgInfo = msgInfoBytes[1];
                    int msgEncryptType =  MessageEncryptType.NONE;
                    if((msgInfo & 0x30) == 0 ){
                        msgEncryptType = MessageEncryptType.NONE;
                    }else if((msgInfo & 0x10) == 0x10){
                        msgEncryptType = MessageEncryptType.MD5;
                    }else if((msgInfo & 0x20) == 0x20){
                        msgEncryptType = MessageEncryptType.AES;
                    }
                    if(msgEncryptType == MessageEncryptType.AES){
                        packageLength = Crypto.getAesInMsgLength(packageLength);
                    }else if(msgEncryptType == MessageEncryptType.MD5){
                        packageLength = packageLength + 8;
                    }
                    decoderBuf.resetReaderIndex();
                    if(packageLength <= 0){
                        callback.receiveErrorMessage("Error Message");
                        return;
                    }
                    if (packageLength > decoderBuf.getReadableBytes()){
                        callback.receiveErrorMessage("Error Message");
                        return;
                    }
                    byte[] data = decoderBuf.readBytes(packageLength);
                    byte checkSum = msgInfoBytes[0];
                    byte[] realData = Crypto.decryptEncryptKeyInData(data, msgEncryptType, aesKey);
                    if (realData != null){
                        byte[] calCheckSumBytes = Arrays.copyOfRange(realData,16,realData.length);
                        byte crc8Value = BytesUtils.tftCrc8(calCheckSumBytes);
                        if(checkSum != crc8Value){
                            decoderBuf.skipBytes(1);
                            callback.receiveErrorMessage("Error Message");
                            return;
                        }

                        try {
                            build(data,realData,callback);
                        }catch (ParseException e){
                            callback.receiveErrorMessage(e.getMessage());
                        }
                    }
                }else{
                    if (encryptType == MessageEncryptType.MD5){
                        packageLength = packageLength + 8;
                    }else if(encryptType == MessageEncryptType.AES){
                        packageLength = Crypto.getAesLength(packageLength);
                    }
                    decoderBuf.resetReaderIndex();
                    if(packageLength <= 0){
                        callback.receiveErrorMessage("Error Message");
                        return;
                    }
                    if (packageLength > decoderBuf.getReadableBytes()){
                        callback.receiveErrorMessage("Error Message");
                        return;
                    }
                    byte[] data = decoderBuf.readBytes(packageLength);
                    byte[] realData = Crypto.decryptData(data, encryptType, aesKey);
                    if (realData != null){
                        try {
                            build(data,realData,callback);
                        }catch (ParseException e){
                            callback.receiveErrorMessage(e.getMessage());
                        }
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

                if(matchNewCryptData(bytes)){
                    if(decoderBuf.getReadableBytes() <= 12){
                        decoderBuf.resetReaderIndex();
                        break;
                    }
                    decoderBuf.skipBytes(10);
                    byte[] msgInfoBytes = decoderBuf.readBytes(2);
                    byte msgInfo = msgInfoBytes[1];
                    int msgEncryptType =  MessageEncryptType.NONE;
                    if((msgInfo & 0x30) == 0 ){
                        msgEncryptType = MessageEncryptType.NONE;
                    }else if((msgInfo & 0x10) == 0x10){
                        msgEncryptType = MessageEncryptType.MD5;
                    }else if((msgInfo & 0x20) == 0x20){
                        msgEncryptType = MessageEncryptType.AES;
                    }
                    if(msgEncryptType == MessageEncryptType.AES){
                        packageLength = Crypto.getAesInMsgLength(packageLength);
                    }else if(msgEncryptType == MessageEncryptType.MD5){
                        packageLength = packageLength + 8;
                    }
                    decoderBuf.resetReaderIndex();
                    if(packageLength <= 0){
                        break;
                    }
                    if (packageLength > decoderBuf.getReadableBytes()){
                        break;
                    }
                    byte[] data = decoderBuf.readBytes(packageLength);
                    byte checkSum = msgInfoBytes[0];
                    byte[] realData = Crypto.decryptEncryptKeyInData(data, msgEncryptType, aesKey);
                    if (realData != null){
                        byte[] calCheckSumBytes = Arrays.copyOfRange(realData,16,realData.length);
                        byte crc8Value = BytesUtils.tftCrc8(calCheckSumBytes);
                        if(checkSum != crc8Value){
                            decoderBuf.skipBytes(1);
                            break;
                        }
                        try {
                            Message message = build(realData);
                            if (message != null){
                                message.setOrignBytes(data);
                                messages.add(message);
                            }
                        }catch (ParseException e){
                            System.out.println(e.getMessage());
                        }
                    }
                }else{
                    if (encryptType == MessageEncryptType.MD5){
                        packageLength = packageLength + 8;
                    }else if(encryptType == MessageEncryptType.AES){
                        packageLength = Crypto.getAesLength(packageLength);
                    }
                    decoderBuf.resetReaderIndex();
                    if(packageLength <= 0){
                        decoderBuf.skipBytes(5);
                        break;
                    }
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
                }
            }else{
                decoderBuf.skipBytes(1);
            }
        }
        return messages;
    }



    private Message build(byte[] bytes) throws ParseException{
        if (bytes != null && bytes.length > HEADER_LENGTH
                && ((bytes[0] == 0x26 && bytes[1] == 0x26))) {
            switch (bytes[2]) {
                case 0x01:
                    SignInMessage signInMessage = parseLoginMessage(bytes);
                    return signInMessage;
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x02:
                case 0x04:
                case 0x16:
                case 0x18:
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
                    ObdMessage obdMessage = parseObdMessage(bytes);
                    return obdMessage;
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataMessage;
                case 0x11:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    return networkInfoMessage;
                case 0x15:
                    WifiMessage  wifiMessage = parseWifiMessage(bytes);
                    return wifiMessage;
                case 0x12:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataSecondMessage;
                case 0x62:
                case 0x64:
                    Message indefiniteLocationMessage = DecoderHelper.parseLocationMessage(bytes);
                    return indefiniteLocationMessage;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    return message;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }
        return null;
    }



    private WifiMessage parseWifiMessage(byte[] bytes) {
        WifiMessage wifiMessage = new WifiMessage();
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        Date date = TimeUtils.getGTM0Date(bytes, 15);
        String selfMac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 21, 27), 0);
        String ap1Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 27, 33), 0);
        int ap1Rssi = (int)bytes[33];
        String ap2Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes,34,40),0);
        int ap2Rssi = (int)bytes[40];
        String ap3Mac =  BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes,41,47),0);
        int ap3Rssi = (int)bytes[47];
        wifiMessage.setOrignBytes(bytes);
        wifiMessage.setImei(imei);
        wifiMessage.setDate(date);
        wifiMessage.setSerialNo(serialNo);
        wifiMessage.setSelfMac(selfMac.toUpperCase());
        wifiMessage.setAp1Mac(ap1Mac.toUpperCase());
        wifiMessage.setAp1RSSI(ap1Rssi);
        wifiMessage.setAp2Mac(ap2Mac.toUpperCase());
        wifiMessage.setAp2RSSI(ap2Rssi);
        wifiMessage.setAp3Mac(ap3Mac.toUpperCase());
        wifiMessage.setAp3RSSI(ap3Rssi);
        return wifiMessage;
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


    private void build(byte[] srcData,byte[] bytes,Callback callback) throws ParseException {
        if (bytes != null && bytes.length > HEADER_LENGTH
                && ((bytes[0] == 0x26 && bytes[1] == 0x26))) {
            switch (bytes[2]) {
                case 0x01:
                    SignInMessage signInMessage = parseLoginMessage(bytes);
                    signInMessage.setOrignBytes(srcData);
                    callback.receiveSignInMessage(signInMessage);
                    break;
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    heartbeatMessage.setOrignBytes(srcData);
                    callback.receiveHeartbeatMessage(heartbeatMessage);
                    break;
                case 0x02:
                case 0x04:
                case 0x16:
                case 0x18:
                    LocationMessage locationMessage = parseDataMessage(bytes);
                    locationMessage.setOrignBytes(srcData);
                    if (locationMessage instanceof LocationAlarmMessage){
                        callback.receiveAlarmMessage((LocationAlarmMessage)locationMessage);
                    }else if (locationMessage instanceof LocationInfoMessage) {
                        callback.receiveLocationInfoMessage((LocationInfoMessage) locationMessage);
                    }
                    break;
                case 0x05:
                    GpsDriverBehaviorMessage gpsDriverBehaviorMessage = parseGpsDriverBehaviorMessage(bytes);
                    gpsDriverBehaviorMessage.setOrignBytes(srcData);
                    callback.receiveGpsDriverBehaviorMessage(gpsDriverBehaviorMessage);
                    break;
                case 0x06:
                    AccelerationDriverBehaviorMessage accelerationDriverBehaviorMessage = parseAccelerationDriverBehaviorMessage(bytes);
                    accelerationDriverBehaviorMessage.setOrignBytes(srcData);
                    callback.receiveAccelerationDriverBehaviorMessage(accelerationDriverBehaviorMessage);
                    break;
                case 0x07:
                    AccidentAccelerationMessage accidentAccelerationMessage = parseAccelerationAlarmMessage(bytes);
                    accidentAccelerationMessage.setOrignBytes(srcData);
                    callback.receiveAccidentAccelerationMessage(accidentAccelerationMessage);
                    break;
                case 0x09:
                    ObdMessage obdMessage = parseObdMessage(bytes);
                    obdMessage.setOrignBytes(srcData);
                    callback.receiveObdMessage(obdMessage);
                    break;
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    bluetoothPeripheralDataMessage.setOrignBytes(srcData);
                    callback.receiveBluetoothDataMessage(bluetoothPeripheralDataMessage);
                    break;
                case 0x12:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataSecondMessage = parseSecondBluetoothDataMessage(bytes);
                    bluetoothPeripheralDataSecondMessage.setOrignBytes(srcData);
                    callback.receiveBluetoothDataMessage(bluetoothPeripheralDataSecondMessage);
                    break;
                case 0x11:
                    NetworkInfoMessage networkInfoMessage = parseNetworkInfoMessage(bytes);
                    networkInfoMessage.setOrignBytes(srcData);
                    callback.receiveNetworkInfoMessage(networkInfoMessage);
                    break;
                case 0x15:
                    WifiMessage wifiMessage = parseWifiMessage(bytes);
                    wifiMessage.setOrignBytes(srcData);
                    callback.receiveWifiMessage(wifiMessage);
                case 0x62:
                case 0x64:
                    Message indefiniteLocationMessage = DecoderHelper.parseLocationMessage(bytes);
                    indefiniteLocationMessage.setOrignBytes(srcData);
                    if(indefiniteLocationMessage instanceof DebugMessage){
                        callback.receiveDebugMessage((DebugMessage) indefiniteLocationMessage);
                    }else{
                        if (indefiniteLocationMessage instanceof LocationAlarmMessage){
                            callback.receiveAlarmMessage((LocationAlarmMessage)indefiniteLocationMessage);
                        }else if (indefiniteLocationMessage instanceof LocationInfoMessage) {
                            callback.receiveLocationInfoMessage((LocationInfoMessage) indefiniteLocationMessage);
                        }
                    }
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    message.setOrignBytes(srcData);
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
        bluetoothPeripheralDataMessage.setIsHistoryData((bytes[15] & 0x80) != 0x00);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        bluetoothPeripheralDataMessage.setSerialNo(serialNo);
//        bluetoothPeripheralDataMessage.setIsNeedResp(isNeedResp);
        bluetoothPeripheralDataMessage.setImei(imei);
        boolean latlngValid = (bytes[22] & 0x40) == 0x40;
        boolean isHisData = (bytes[22] & 0x80) == 0x80;
        bluetoothPeripheralDataMessage.setLatlngValid(latlngValid);
        bluetoothPeripheralDataMessage.setIsHistoryData(isHisData);
        double altitude = latlngValid? BytesUtils.bytes2Float(bytes, 23) : 0.0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(bytes, 27) : 0.0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(bytes, 31) : 0.0;
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
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long eci_4g = null;
        Integer tac = null;
        Integer pcid_4g_1 = null;
        Integer pcid_4g_2 = null;
        Integer pcid_4g_3 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = bytes[23];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bytes,23);
            mnc_2g = BytesUtils.bytes2Short(bytes,25);
            lac_2g_1 = BytesUtils.bytes2Short(bytes,27);
            ci_2g_1 = BytesUtils.bytes2Short(bytes,29);
            lac_2g_2 = BytesUtils.bytes2Short(bytes,31);
            ci_2g_2 = BytesUtils.bytes2Short(bytes,33);
            lac_2g_3 = BytesUtils.bytes2Short(bytes,35);
            ci_2g_3 = BytesUtils.bytes2Short(bytes,37);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bytes,23) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(bytes,25);
            eci_4g = BytesUtils.unsigned4BytesToInt(bytes, 27);
            tac = BytesUtils.bytes2Short(bytes, 31);
            pcid_4g_1 = BytesUtils.bytes2Short(bytes, 33);
            pcid_4g_2 = BytesUtils.bytes2Short(bytes, 35);
            pcid_4g_3 = BytesUtils.bytes2Short(bytes,37);
        }
        bluetoothPeripheralDataMessage.setLatitude(latitude);
        bluetoothPeripheralDataMessage.setLongitude(longitude);
        bluetoothPeripheralDataMessage.setAzimuth(azimuth);
        bluetoothPeripheralDataMessage.setSpeed(speedf);
        bluetoothPeripheralDataMessage.setAltitude(altitude);
        bluetoothPeripheralDataMessage.setIsHadLocationInfo(true);
        bluetoothPeripheralDataMessage.setIs_4g_lbs(is_4g_lbs);
        bluetoothPeripheralDataMessage.setIs_2g_lbs(is_2g_lbs);
        bluetoothPeripheralDataMessage.setMcc_2g(mcc_2g);
        bluetoothPeripheralDataMessage.setMnc_2g(mnc_2g);
        bluetoothPeripheralDataMessage.setLac_2g_1(lac_2g_1);
        bluetoothPeripheralDataMessage.setCi_2g_1(ci_2g_1);
        bluetoothPeripheralDataMessage.setLac_2g_2(lac_2g_2);
        bluetoothPeripheralDataMessage.setCi_2g_2(ci_2g_2);
        bluetoothPeripheralDataMessage.setLac_2g_3(lac_2g_3);
        bluetoothPeripheralDataMessage.setCi_2g_3(ci_2g_3);
        bluetoothPeripheralDataMessage.setMcc_4g(mcc_4g);
        bluetoothPeripheralDataMessage.setMnc_4g(mnc_4g);
        bluetoothPeripheralDataMessage.setEci_4g(eci_4g);
        bluetoothPeripheralDataMessage.setTac(tac);
        bluetoothPeripheralDataMessage.setPcid_4g_1(pcid_4g_1);
        bluetoothPeripheralDataMessage.setPcid_4g_2(pcid_4g_2);
        bluetoothPeripheralDataMessage.setPcid_4g_3(pcid_4g_3);
        byte[] bleData = Arrays.copyOfRange(bytes,39,bytes.length);
        if (bleData.length <= 2){
            System.out.println("Error len ble Data:" +BytesUtils.bytes2HexString(bytes,0));
            return bluetoothPeripheralDataMessage;
        }
        List<BleData> bleDataList = new ArrayList<BleData>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if(bleData[0] == 0x00 && bleData[1] == 0x00){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_MIX);
            int positionIndex = 2;
            while (positionIndex + 2 < bleData.length ){
                if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x01){
                    positionIndex+=2;
                    if(positionIndex + 10 <= bleData.length){
                        BleTireData bleTireData = getBleTireData(bleData, positionIndex);
                        bleDataList.add(bleTireData);
                        positionIndex += 10;
                        continue;
                    }else{
                        break;
                    }

                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x02){
                    positionIndex+=2;
                    if(positionIndex + 8 <= bleData.length){
                        BleAlertData bleAlertData = getBleAlertDataWithoutLocation(imei, latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                        bleDataList.add(bleAlertData);
                        positionIndex += 8;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x03){
                    positionIndex+=2;
                    if(positionIndex + 8 <= bleData.length){
                        BleDriverSignInData bleDriverSignInData = getBleDriverSignInDataWithoutLocation(imei, latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
                        bleDataList.add(bleDriverSignInData);
                        positionIndex += 8;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x04){
                    positionIndex+=2;
                    if(positionIndex + 15 <= bleData.length){
                        BleTempData bleTempData = getBleTempData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleTempData);
                        positionIndex += 15;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x05){
                    positionIndex+=2;
                    if(positionIndex + 12 <= bleData.length){
                        BleDoorData bleDoorData = getBleDoorData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleDoorData);
                        positionIndex += 12;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x06){
                    positionIndex+=2;
                    if(positionIndex + 12 <= bleData.length){
                        BleCtrlData bleCtrlData = getBleCtrlData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleCtrlData);
                        positionIndex += 12;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x07){
                    positionIndex+=2;
                    if(positionIndex + 15 <= bleData.length){
                        BleFuelData bleFuelData = getBleFuelData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleFuelData);
                        positionIndex += 15;
                        continue;
                    }else{
                        break;
                    }
                }else{
                    break;
                }
            }

        }
        else if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE);
            for (int i = 2;i+10 <= bleData.length;i+=10){
                BleTireData bleTireData = getBleTireData(bleData, i);
                bleDataList.add(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS);
            BleAlertData bleAlertData = getBleAlertDataWithoutLocation(imei, latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
            bleDataList.add(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER);
            BleDriverSignInData bleDriverSignInData = getBleDriverSignInDataWithoutLocation(imei, latlngValid, isHisData, altitude, longitude, latitude, azimuth, speedf, is_4g_lbs, mcc_4g, mnc_4g, eci_4g, tac, pcid_4g_1, pcid_4g_2, pcid_4g_3, is_2g_lbs, mcc_2g, mnc_2g, lac_2g_1, ci_2g_1, lac_2g_2, ci_2g_2, lac_2g_3, ci_2g_3, bleData);
            bleDataList.add(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP);
            for (int i = 2;i+15 <= bleData.length;i+=15){
                BleTempData bleTempData = getBleTempData(bleData, i, decimalFormat);
                bleDataList.add(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR);
            for (int i = 2;i+12 <= bleData.length;i+=12){
                BleDoorData bleDoorData = getBleDoorData(bleData, i, decimalFormat);
                bleDataList.add(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL);
            for (int i = 2;i+12 <= bleData.length;i+=12){
                BleCtrlData bleCtrlData = getBleCtrlData(bleData, i, decimalFormat);
                bleDataList.add(bleCtrlData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x07){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL);
            for (int i = 2;i +15 <= bleData.length;i+=15){
                BleFuelData bleFuelData = getBleFuelData(bleData, i, decimalFormat);
                bleDataList.add(bleFuelData);
            }
        }
        bluetoothPeripheralDataMessage.setBleDataList(bleDataList);
        return bluetoothPeripheralDataMessage;
    }

    private BleDriverSignInData getBleDriverSignInDataWithoutLocation(String imei, boolean latlngValid, boolean isHisData, double altitude, double longitude, double latitude, int azimuth, Float speedf, Boolean is_4g_lbs, Integer mcc_4g, Integer mnc_4g, Long eci_4g, Integer tac, Integer pcid_4g_1, Integer pcid_4g_2, Integer pcid_4g_3, Boolean is_2g_lbs, Integer mcc_2g, Integer mnc_2g, Integer lac_2g_1, Integer ci_2g_1, Integer lac_2g_2, Integer ci_2g_2, Integer lac_2g_3, Integer ci_2g_3, byte[] bleData) {
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
        bleDriverSignInData.setIs_4g_lbs(is_4g_lbs);
        bleDriverSignInData.setIs_2g_lbs(is_2g_lbs);
        bleDriverSignInData.setMcc_2g(mcc_2g);
        bleDriverSignInData.setMnc_2g(mnc_2g);
        bleDriverSignInData.setLac_2g_1(lac_2g_1);
        bleDriverSignInData.setCi_2g_1(ci_2g_1);
        bleDriverSignInData.setLac_2g_2(lac_2g_2);
        bleDriverSignInData.setCi_2g_2(ci_2g_2);
        bleDriverSignInData.setLac_2g_3(lac_2g_3);
        bleDriverSignInData.setCi_2g_3(ci_2g_3);
        bleDriverSignInData.setMcc_4g(mcc_4g);
        bleDriverSignInData.setMnc_4g(mnc_4g);
        bleDriverSignInData.setEci_4g(eci_4g);
        bleDriverSignInData.setTac(tac);
        bleDriverSignInData.setPcid_4g_1(pcid_4g_1);
        bleDriverSignInData.setPcid_4g_2(pcid_4g_2);
        bleDriverSignInData.setPcid_4g_3(pcid_4g_3);
        return bleDriverSignInData;
    }

    private BleAlertData getBleAlertDataWithoutLocation(String imei, boolean latlngValid, boolean isHisData, double altitude, double longitude, double latitude, int azimuth, Float speedf, Boolean is_4g_lbs, Integer mcc_4g, Integer mnc_4g, Long eci_4g, Integer tac, Integer pcid_4g_1, Integer pcid_4g_2, Integer pcid_4g_3, Boolean is_2g_lbs, Integer mcc_2g, Integer mnc_2g, Integer lac_2g_1, Integer ci_2g_1, Integer lac_2g_2, Integer ci_2g_2, Integer lac_2g_3, Integer ci_2g_3, byte[] bleData) {
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
        bleAlertData.setIs_4g_lbs(is_4g_lbs);
        bleAlertData.setIs_2g_lbs(is_2g_lbs);
        bleAlertData.setMcc_2g(mcc_2g);
        bleAlertData.setMnc_2g(mnc_2g);
        bleAlertData.setLac_2g_1(lac_2g_1);
        bleAlertData.setCi_2g_1(ci_2g_1);
        bleAlertData.setLac_2g_2(lac_2g_2);
        bleAlertData.setCi_2g_2(ci_2g_2);
        bleAlertData.setLac_2g_3(lac_2g_3);
        bleAlertData.setCi_2g_3(ci_2g_3);
        bleAlertData.setMcc_4g(mcc_4g);
        bleAlertData.setMnc_4g(mnc_4g);
        bleAlertData.setEci_4g(eci_4g);
        bleAlertData.setTac(tac);
        bleAlertData.setPcid_4g_1(pcid_4g_1);
        bleAlertData.setPcid_4g_2(pcid_4g_2);
        bleAlertData.setPcid_4g_3(pcid_4g_3);
        return bleAlertData;
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
        if (bleData.length <= 0){
            System.out.println("Error len ble Data:" +BytesUtils.bytes2HexString(bytes,0));
            return bluetoothPeripheralDataMessage;
        }
        List<BleData> bleDataList = new ArrayList<BleData>();
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        if(bleData[0] == 0x00 && bleData[1] == 0x00){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_MIX);
            int positionIndex = 2;
            while (positionIndex + 2 < bleData.length ){
                if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x01){
                    positionIndex+=2;
                    if(positionIndex + 10 <= bleData.length){
                        BleTireData bleTireData = getBleTireData(bleData, positionIndex);
                        bleDataList.add(bleTireData);
                        positionIndex += 10;
                        continue;
                    }else{
                        break;
                    }

                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x02){
                    positionIndex+=2;
                    if(positionIndex + 25 <= bleData.length){
                        BleAlertData bleAlertData = getBleAlertData(imei, bleData);
                        bleDataList.add(bleAlertData);
                        positionIndex += 25;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x03){
                    positionIndex+=2;
                    if(positionIndex + 25 <= bleData.length){
                        BleDriverSignInData bleDriverSignInData = getBleDriverSignInData(imei, bleData);
                        bleDataList.add(bleDriverSignInData);
                        positionIndex += 25;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x04){
                    positionIndex+=2;
                    if(positionIndex + 15 <= bleData.length){
                        BleTempData bleTempData = getBleTempData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleTempData);
                        positionIndex += 15;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x05){
                    positionIndex+=2;
                    if(positionIndex + 12 <= bleData.length){
                        BleDoorData bleDoorData = getBleDoorData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleDoorData);
                        positionIndex += 12;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x06){
                    positionIndex+=2;
                    if(positionIndex + 12 <= bleData.length){
                        BleCtrlData bleCtrlData = getBleCtrlData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleCtrlData);
                        positionIndex += 12;
                        continue;
                    }else{
                        break;
                    }
                }else if(bleData[positionIndex] == 0x00 && bleData[positionIndex + 1] == 0x07){
                    positionIndex+=2;
                    if(positionIndex + 15 <= bleData.length){
                        BleFuelData bleFuelData = getBleFuelData(bleData, positionIndex, decimalFormat);
                        bleDataList.add(bleFuelData);
                        positionIndex += 15;
                        continue;
                    }else{
                        break;
                    }
                }else{
                    break;
                }
            }

        }
        else if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE);
            for (int i = 2;i+10 <= bleData.length;i+=10){
                BleTireData bleTireData = getBleTireData(bleData, i);
                bleDataList.add(bleTireData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x02){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_SOS);
            BleAlertData bleAlertData = getBleAlertData(imei, bleData);
            bleDataList.add(bleAlertData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x03){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DRIVER);
            BleDriverSignInData bleDriverSignInData = getBleDriverSignInData(imei, bleData);
            bleDataList.add(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP);
            for (int i = 2;i +15 <= bleData.length;i+=15){
                BleTempData bleTempData = getBleTempData(bleData, i, decimalFormat);
                bleDataList.add(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR);
            for (int i = 2;i+12 <= bleData.length;i+=12){
                BleDoorData bleDoorData = getBleDoorData(bleData, i, decimalFormat);
                bleDataList.add(bleDoorData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x06){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_CTRL);
            for (int i = 2;i+12 <= bleData.length;i+=12){
                BleCtrlData bleCtrlData = getBleCtrlData(bleData, i, decimalFormat);
                bleDataList.add(bleCtrlData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x07) {
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_FUEL);
            for (int i = 2;i+15 <= bleData.length;i+=15){
                BleFuelData bleFuelData = getBleFuelData(bleData, i, decimalFormat);
                bleDataList.add(bleFuelData);
            }
        }
        bluetoothPeripheralDataMessage.setBleDataList(bleDataList);
        return bluetoothPeripheralDataMessage;
    }

    private BleFuelData getBleFuelData(byte[] bleData, int i, DecimalFormat decimalFormat) {
        BleFuelData bleFuelData = new BleFuelData();
        byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
        String mac = BytesUtils.bytes2HexString(macArray, 0);
        int voltageTmp = (int) bleData[i + 6] < 0 ? (int) bleData[i + 6] + 256 : (int) bleData[i + 6];
        float voltage;
        if (voltageTmp == 255) {
            voltage = -999;
        } else {
            voltage = 2 + 0.01f * voltageTmp;
        }
        int valueTemp = BytesUtils.bytes2Short(bleData, i + 7);
        int value;
        if (valueTemp == 65535) {
            value = -999;
        } else {
            value = valueTemp;
        }
        int temperatureTemp = BytesUtils.bytes2Short(bleData, i + 9);
        int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
        float temperature;
        if (temperatureTemp == 65535) {
            temperature = -999;
        } else {
            temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
        }
        int status =(int) bleData[i +13] < 0 ? (int) bleData[i +13] + 256 : (int) bleData[i +13];
        int online = 1;
        if(status == 255){
            status = 0;
            online = 0;
        }
        int rssiTemp = (int) bleData[i + 14] < 0 ? (int) bleData[i + 14] + 256 : (int) bleData[i + 14];
        int rssi;
        if (rssiTemp == 255) {
            rssi = -999;
        } else {
            rssi = rssiTemp - 128;
        }
        bleFuelData.setRssi(rssi);
        bleFuelData.setMac(mac);
        bleFuelData.setOnline(online);
        bleFuelData.setAlarm(status);
        bleFuelData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
        bleFuelData.setValue(value);
        bleFuelData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
        return bleFuelData;
    }

    private BleCtrlData getBleCtrlData(byte[] bleData, int i, DecimalFormat decimalFormat) {
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
        int temperatureTemp = BytesUtils.bytes2Short(bleData, i +8);
        int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
        float temperature;
        if(temperatureTemp == 65535){
            temperature = -999;
        }else{
            temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
        }
        int CtrlStatus = (int) bleData[i +10] < 0 ? (int) bleData[i +10] + 256 : (int) bleData[i +10];
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
        return bleCtrlData;
    }

    private BleDoorData getBleDoorData(byte[] bleData, int i, DecimalFormat decimalFormat) {
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
        int temperatureTemp = BytesUtils.bytes2Short(bleData, i +8);
        int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
        float temperature;
        if(temperatureTemp == 65535){
            temperature = -999;
        }else{
            temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
        }
        int doorStatus = (int) bleData[i +10] < 0 ? (int) bleData[i +10] + 256 : (int) bleData[i +10];
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
        return bleDoorData;
    }

    private BleTempData getBleTempData(byte[] bleData, int i, DecimalFormat decimalFormat) {
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
        int temperatureTemp = BytesUtils.bytes2Short(bleData, i +8);
        int tempPositive = (temperatureTemp & 0x8000) == 0 ? 1 : -1;
        float temperature;
        if(temperatureTemp == 65535){
            temperature = -999;
        }else{
            temperature = (temperatureTemp & 0x7fff) * 0.01f * tempPositive;
        }
        int humidityTemp = BytesUtils.bytes2Short(bleData, i +10);
        float humidity;
        if(humidityTemp == 65535){
            humidity = -999;
        }else{
            humidity = humidityTemp * 0.01f;
        }
        int lightTemp = BytesUtils.bytes2Short(bleData, i +12);
        int lightIntensity ;
        if(lightTemp == 65535){
            lightIntensity = -999;
        }else{
            lightIntensity = lightTemp & 0x0001;
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
        bleTempData.setHumidity(Float.valueOf(decimalFormat.format(humidity)));
        bleTempData.setVoltage(Float.valueOf(decimalFormat.format(voltage)));
        bleTempData.setBatteryPercent(batteryPercent);
        bleTempData.setTemp(Float.valueOf(decimalFormat.format(temperature)));
        return bleTempData;
    }

    private BleDriverSignInData getBleDriverSignInData(String imei, byte[] bleData) {
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
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long eci_4g = null;
        Integer tac = null;
        Integer pcid_4g_1 = null;
        Integer pcid_4g_2 = null;
        Integer pcid_4g_3 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = bleData[11];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bleData,11);
            mnc_2g = BytesUtils.bytes2Short(bleData,13);
            lac_2g_1 = BytesUtils.bytes2Short(bleData,15);
            ci_2g_1 = BytesUtils.bytes2Short(bleData,17);
            lac_2g_2 = BytesUtils.bytes2Short(bleData,19);
            ci_2g_2 = BytesUtils.bytes2Short(bleData,21);
            lac_2g_3 = BytesUtils.bytes2Short(bleData,23);
            ci_2g_3 = BytesUtils.bytes2Short(bleData,25);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bleData,11) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(bleData,13);
            eci_4g = BytesUtils.unsigned4BytesToInt(bleData, 15);
            tac = BytesUtils.bytes2Short(bleData, 19);
            pcid_4g_1 = BytesUtils.bytes2Short(bleData, 21);
            pcid_4g_2 = BytesUtils.bytes2Short(bleData, 23);
            pcid_4g_3 = BytesUtils.bytes2Short(bleData,25);
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
        bleDriverSignInData.setIs_4g_lbs(is_4g_lbs);
        bleDriverSignInData.setIs_2g_lbs(is_2g_lbs);
        bleDriverSignInData.setMcc_2g(mcc_2g);
        bleDriverSignInData.setMnc_2g(mnc_2g);
        bleDriverSignInData.setLac_2g_1(lac_2g_1);
        bleDriverSignInData.setCi_2g_1(ci_2g_1);
        bleDriverSignInData.setLac_2g_2(lac_2g_2);
        bleDriverSignInData.setCi_2g_2(ci_2g_2);
        bleDriverSignInData.setLac_2g_3(lac_2g_3);
        bleDriverSignInData.setCi_2g_3(ci_2g_3);
        bleDriverSignInData.setMcc_4g(mcc_4g);
        bleDriverSignInData.setMnc_4g(mnc_4g);
        bleDriverSignInData.setEci_4g(eci_4g);
        bleDriverSignInData.setTac(tac);
        bleDriverSignInData.setPcid_4g_1(pcid_4g_1);
        bleDriverSignInData.setPcid_4g_2(pcid_4g_2);
        bleDriverSignInData.setPcid_4g_3(pcid_4g_3);
        return bleDriverSignInData;
    }

    private BleAlertData getBleAlertData(String imei, byte[] bleData) {
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
        int azimuth = latlngValid ? BytesUtils.bytes2Short(bleData, 25) : 0;
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long eci_4g = null;
        Integer tac = null;
        Integer pcid_4g_1 = null;
        Integer pcid_4g_2 = null;
        Integer pcid_4g_3 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = bleData[11];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bleData,11);
            mnc_2g = BytesUtils.bytes2Short(bleData,13);
            lac_2g_1 = BytesUtils.bytes2Short(bleData,15);
            ci_2g_1 = BytesUtils.bytes2Short(bleData,17);
            lac_2g_2 = BytesUtils.bytes2Short(bleData,19);
            ci_2g_2 = BytesUtils.bytes2Short(bleData,21);
            lac_2g_3 = BytesUtils.bytes2Short(bleData,23);
            ci_2g_3 = BytesUtils.bytes2Short(bleData,25);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bleData,11) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(bleData,13);
            eci_4g = BytesUtils.unsigned4BytesToInt(bleData, 15);
            tac = BytesUtils.bytes2Short(bleData, 19);
            pcid_4g_1 = BytesUtils.bytes2Short(bleData, 21);
            pcid_4g_2 = BytesUtils.bytes2Short(bleData, 23);
            pcid_4g_3 = BytesUtils.bytes2Short(bleData,25);
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
        bleAlertData.setIs_4g_lbs(is_4g_lbs);
        bleAlertData.setIs_2g_lbs(is_2g_lbs);
        bleAlertData.setMcc_2g(mcc_2g);
        bleAlertData.setMnc_2g(mnc_2g);
        bleAlertData.setLac_2g_1(lac_2g_1);
        bleAlertData.setCi_2g_1(ci_2g_1);
        bleAlertData.setLac_2g_2(lac_2g_2);
        bleAlertData.setCi_2g_2(ci_2g_2);
        bleAlertData.setLac_2g_3(lac_2g_3);
        bleAlertData.setCi_2g_3(ci_2g_3);
        bleAlertData.setMcc_4g(mcc_4g);
        bleAlertData.setMnc_4g(mnc_4g);
        bleAlertData.setEci_4g(eci_4g);
        bleAlertData.setTac(tac);
        bleAlertData.setPcid_4g_1(pcid_4g_1);
        bleAlertData.setPcid_4g_2(pcid_4g_2);
        bleAlertData.setPcid_4g_3(pcid_4g_3);
        return bleAlertData;
    }

    private BleTireData getBleTireData(byte[] bleData, int i) {
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
        return bleTireData;
    }

    private SignInMessage parseLoginMessage(byte[] bytes) throws ParseException {
        if (bytes.length == 25){
            int serialNo = BytesUtils.bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.decode(bytes, 7);
            String str = BytesUtils.bytes2HexString(bytes, 15);
            if (20 == str.length()) {
                String software = String.format("V%d.%d.%d", (bytes[15] & 0xf0) >> 4, bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4);
                String firmware = String.format("V%d.%d.%d", bytes[16] & 0xf, (bytes[17] & 0xf0) >> 4, bytes[17] & 0xf);
                String platform = String.format("%s", str.substring(6, 10));
                String hardware = String.format("%d.%d", (bytes[20] & 0xf0) >> 4, bytes[20] & 0xf);
                int obdV1 = (int)bytes[21];
                int obdV2 = (int)bytes[22];
                int obdV3 = (int)bytes[23];
                if(obdV1 < 0){
                    obdV1 += 256;
                }
                if(obdV2 < 0){
                    obdV2 += 256;
                }
                if(obdV3 < 0){
                    obdV3 += 256;
                }
                String obdSoftware = String.format("V%d.%d.%d", obdV1, obdV2, obdV3);
                String obdHardware = String.format("%d.%d", (bytes[24] & 0xf0) >> 4, bytes[24] & 0xf);
                SignInMessage signInMessage = new SignInMessage();
//                boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
                signInMessage.setSerialNo(serialNo);
//                signInMessage.setIsNeedResp(isNeedResp);
                signInMessage.setImei(imei);
                signInMessage.setSoftware(software);
                signInMessage.setFirmware(firmware);
                signInMessage.setPlatform(platform);
                signInMessage.setHardware(hardware);
                signInMessage.setOrignBytes(bytes);
                signInMessage.setObdHardware(obdHardware);
                signInMessage.setObdSoftware(obdSoftware);
                return signInMessage;
            }else{
                throw new ParseException("Error login message",0);
            }
        }else if(bytes.length == 27){
            int serialNo = BytesUtils.bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.decode(bytes, 7);
            String software = String.format("V%d.%d.%d", bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4, bytes[16] & 0xf);
            String str = BytesUtils.bytes2HexString(bytes, 17);
            String platform = str.substring(0, 6);
            String firmware = String.format("V%s.%s.%s.%s", str.substring(6, 7), str.substring(7, 8), str.substring(8, 9), str.substring(9, 10));
            String hardware = String.format("%s.%s", str.substring(10, 11), str.substring(11, 12));
            int obdV1 = (int)bytes[23];
            int obdV2 = (int)bytes[24];
            int obdV3 = (int)bytes[25];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            String obdSoftware = String.format("V%d.%d.%d", obdV1, obdV2, obdV3);
            String obdHardware = String.format("%d.%d", (bytes[26] & 0xf0) >> 4, bytes[26] & 0xf);
            SignInMessage signInMessage = new SignInMessage();
//            boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
            signInMessage.setSerialNo(serialNo);
//            signInMessage.setIsNeedResp(isNeedResp);
            signInMessage.setImei(imei);
            signInMessage.setSoftware(software);
            signInMessage.setFirmware(firmware);
            signInMessage.setPlatform(platform);
            signInMessage.setHardware(hardware);
            signInMessage.setOrignBytes(bytes);
            signInMessage.setObdHardware(obdHardware);
            signInMessage.setObdSoftware(obdSoftware);
            return signInMessage;
        }else if(bytes.length >= 35){
            int serialNo = BytesUtils.bytes2Short(bytes, 5);
            String imei = BytesUtils.IMEI.decode(bytes, 7);
            int model = BytesUtils.bytes2Short(bytes,15);
            String str = BytesUtils.bytes2HexString(bytes, 17);
            String software = String.format("V%s.%s.%s.%s", str.substring(0, 1), str.substring(1, 2), str.substring(2, 3), str.substring(3, 4));
            String platform = str.substring(4, 10);
            String firmware = String.format("V%s.%s.%s.%s", str.substring(10, 11), str.substring(11, 12), str.substring(12, 13), str.substring(13, 14));
            String hardware = String.format("%s.%s", str.substring(14, 15), str.substring(15, 16));
            int obdV1 = (int)bytes[25];
            int obdV2 = (int)bytes[26];
            int obdV3 = (int)bytes[27];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            String obdSoftware = String.format("V%d.%d.%d", obdV1, obdV2, obdV3);
            obdV1 = (int)bytes[28];
            obdV2 = (int)bytes[29];
            obdV3 = (int)bytes[30];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            String obdBootVersion = String.format("V%d.%d.%d", obdV1, obdV2, obdV3);
            obdV1 = (int)bytes[31];
            obdV2 = (int)bytes[32];
            obdV3 = (int)bytes[33];
            if(obdV1 < 0){
                obdV1 += 256;
            }
            if(obdV2 < 0){
                obdV2 += 256;
            }
            if(obdV3 < 0){
                obdV3 += 256;
            }
            String obdDataVersion = String.format("V%d.%d.%d", obdV1, obdV2, obdV3);
            String obdHardware = String.format("%d.%d", (bytes[34] & 0xf0) >> 4, bytes[34] & 0xf);
            SignInMessage signInMessage = new SignInMessage();
//            boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
            signInMessage.setSerialNo(serialNo);
//            signInMessage.setIsNeedResp(isNeedResp);
            signInMessage.setImei(imei);
            signInMessage.setSoftware(software);
            signInMessage.setFirmware(firmware);
            signInMessage.setPlatform(platform);
            signInMessage.setHardware(hardware);
            signInMessage.setOrignBytes(bytes);
            signInMessage.setObdHardware(obdHardware);
            signInMessage.setObdSoftware(obdSoftware);
            signInMessage.setObdDataVersion(obdDataVersion);
            signInMessage.setObdBootVersion(obdBootVersion);
            return signInMessage;
        }else{
            return  null;
        }

    }
    private HeartbeatMessage parseHeartbeat(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        HeartbeatMessage heartbeatMessage = new HeartbeatMessage();
        heartbeatMessage.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        heartbeatMessage.setSerialNo(serialNo);
//        heartbeatMessage.setIsNeedResp(isNeedResp);
        heartbeatMessage.setImei(imei);
        return heartbeatMessage;
    }



    private Message parseInteractMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        byte protocol = bytes[15];
        byte[] data = Arrays.copyOfRange(bytes, 16, bytes.length);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
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

    private ObdMessage parseObdMessage(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        Date date = TimeUtils.getGTM0Date(bytes, 15);

        ObdMessage obdData = new ObdMessage();
        obdData.setImei(imei);
        obdData.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        obdData.setSerialNo(serialNo);
//        obdData.setIsNeedResp(isNeedResp);
        obdData.setDate(date);
        byte[] obdBytes = Arrays.copyOfRange(bytes,21,bytes.length);

        byte[] head = new byte[2];
        head[0] = obdBytes[0];
        head[1] = obdBytes[1];
        if(Arrays.equals(head,obdHead)){
            obdBytes[2] = (byte)(obdBytes[2] & 0x0F);//去除高位
            int length = BytesUtils.bytes2Short(obdBytes,2);
            if(length > 0){
                try{
                    byte[] data = Arrays.copyOfRange(obdBytes,4,4+length);
                    if(data[0] == 0x41 && data[1] == 0x04 && data.length > 3){
                        obdData.setMessageType(ObdMessage.CLEAR_ERROR_CODE_MESSAGE);
                        obdData.setClearErrorCodeSuccess(data[2] == 0x01);
                    }else if(data[0] == 0x41 && data[1] == 0x05 && data.length > 2){
                        byte[] vinData = Arrays.copyOfRange(data,2,data.length - 1);
                        boolean dataValid = false;
                        for(byte item : vinData){
                            if((item & 0xFF) != 0xFF){
                                dataValid = true;
                            }
                        }
                        if(vinData.length > 0 && dataValid){
                            obdData.setMessageType(ObdMessage.VIN_MESSAGE);
                            obdData.setVin(new String(vinData));
                        }
                    }else if(data[0] == 0x41 && (data[1] == 0x03 || data[1] == 0x0A)){
                        int errorCode = data[2];
                        byte[] errorDataByte = Arrays.copyOfRange(data,3,data.length - 1);
                        String errorDataStr = BytesUtils.bytes2HexString(errorDataByte,0);
                        if(errorDataStr != null){
                            String errorDataSum = "";
                            for(int i = 0 ;i+6 <= errorDataStr.length();i+=6){
                                String errorDataItem = errorDataStr.substring(i,i+6);
                                String srcFlag = errorDataItem.substring(0,1);
                                String errorDataCode =  getObdErrorFlag(srcFlag) + errorDataItem.substring(1,4);
                                if(!errorDataSum.contains(errorDataCode)){
                                    if(i != 0){
                                        errorDataSum += ";";
                                    }
                                    errorDataSum += errorDataCode;
                                }
                                if(i+6 >= errorDataStr.length()){
                                    break;
                                }
                            }
                            obdData.setMessageType(ObdMessage.ERROR_CODE_MESSAGE);
                            obdData.setErrorCode(getObdErrorCode(errorCode));
                            obdData.setErrorData(errorDataSum);
                        }
                    }
                }catch (Exception e){
                    System.out.println("OBD Data error :" + BytesUtils.bytes2HexString(bytes,0));
                    e.printStackTrace();
                }
            }
        }
        return obdData;
    }

    private String getObdErrorCode(int errorCode){
        if(errorCode == 0){
            return "J1979";
        }else if(errorCode == 1){
            return "J1939";
        }
        return "";
    }

    private String getObdErrorFlag(String srcFlag){
        byte[] data = BytesUtils.hexString2Bytes(srcFlag);
        if(data[0] >= 0 && data[0] < 4){
            return "P" + String.valueOf(data[0]);
        }else if(data[0] >= 4 && data[0] < 8){
            return "C" + String.valueOf(data[0] - 4);
        }else if(data[0] >= 8 && data[0] < 12){
            return "B" + String.valueOf(data[0] - 8);
        }else{
            return "U" + String.valueOf(data[0] - 12);
        }
    }




    private GpsDriverBehaviorMessage parseGpsDriverBehaviorMessage(byte[] bytes) {
        int length = BytesUtils.bytes2Short(bytes, 3);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        GpsDriverBehaviorMessage gpsDriverBehaviorMessage = new GpsDriverBehaviorMessage();
        int behaviorType = (int)bytes[15];
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        gpsDriverBehaviorMessage.setSerialNo(serialNo);
//        gpsDriverBehaviorMessage.setIsNeedResp(isNeedResp);
        gpsDriverBehaviorMessage.setImei(imei);
        gpsDriverBehaviorMessage.setBehaviorType(behaviorType);
        gpsDriverBehaviorMessage.setOrignBytes(bytes);
        Date startDate = TimeUtils.getGTM0Date(bytes, 16);
        gpsDriverBehaviorMessage.setStartDate(startDate);
        float startAltitude = BytesUtils.bytes2Float(bytes, 22);
        if(Float.compare(startAltitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setStartAltitude(startAltitude);
        }

        float startLongitude = BytesUtils.bytes2Float(bytes, 26);
        if(Float.compare(startLongitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setStartLongitude(startLongitude);
        }

        float startLatitude = BytesUtils.bytes2Float(bytes, 30);
        if(Float.compare(startLatitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setStartLatitude(startLatitude);
        }

        byte[] bytesSpeed = Arrays.copyOfRange(bytes, 34, 36);
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        Float speedf = 0f;
        if (!strSp.toLowerCase().equals("ffff")){
            speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        }
        gpsDriverBehaviorMessage.setStartSpeed(speedf);
        int azimuth = BytesUtils.bytes2Short(bytes, 36);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.setStartAzimuth(azimuth);

        int rpm = BytesUtils.bytes2Short(bytes, 38);
        if(rpm == 65535){
            rpm = -999;
        }
        gpsDriverBehaviorMessage.setStartRpm(rpm);


        Date endDate = TimeUtils.getGTM0Date(bytes, 40);
        gpsDriverBehaviorMessage.setEndDate(endDate);
        float endAltitude = BytesUtils.bytes2Float(bytes, 46);
        if(Float.compare(endAltitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setEndAltitude(endAltitude);
        }

        float endLongitude = BytesUtils.bytes2Float(bytes, 50);
        if(Float.compare(endLongitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setEndLongitude(endLongitude);
        }

        float endLatitude = BytesUtils.bytes2Float(bytes, 54);
        if(Float.compare(endLatitude,Float.NaN) != 0){
            gpsDriverBehaviorMessage.setEndLatitude(endLatitude);
        }

        bytesSpeed = Arrays.copyOfRange(bytes, 58, 60);
        strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        speedf = 0f;
        if (!strSp.toLowerCase().equals("ffff")){
            speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
        }
        gpsDriverBehaviorMessage.setEndSpeed(speedf);
        azimuth = BytesUtils.bytes2Short(bytes, 60);
        if (azimuth == 255){
            azimuth = 0;
        }
        gpsDriverBehaviorMessage.setEndAzimuth(azimuth);
        rpm = BytesUtils.bytes2Short(bytes, 62);
        if(rpm == 65535){
            rpm = -999;
        }
        gpsDriverBehaviorMessage.setEndRpm(rpm);
        return gpsDriverBehaviorMessage;
    }

    private AccidentAccelerationMessage parseAccelerationAlarmMessage(byte[] bytes) {
        int length = BytesUtils.bytes2Short(bytes, 3);
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        AccidentAccelerationMessage accidentAccelerationMessage = new AccidentAccelerationMessage();

        accidentAccelerationMessage.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        accidentAccelerationMessage.setSerialNo(serialNo);
//        accidentAccelerationMessage.setIsNeedResp(isNeedResp);
        accidentAccelerationMessage.setImei(imei);
        int behaviorType = (int)bytes[15];
        accidentAccelerationMessage.setBehaviorType(behaviorType);
        int dataLength = length - 16;
        int dataCount = dataLength / 30;
        int beginIndex = 16;
        List<AccelerationData> accidentAccelerationList = new ArrayList<AccelerationData>();
        for (int i = 0 ; i < dataCount;i++){
            int curParseIndex = beginIndex + i * 30;
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
        acceleration.setDate(TimeUtils.getGTM0Date(bytes, curParseIndex));
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

        float altitude = latlngValid ? BytesUtils.bytes2Float(bytes, curParseIndex + 12) : 0;
        if(Float.compare(altitude,Float.NaN) != 0){
            acceleration.setAltitude(altitude);
        }
        float longitude = latlngValid ? BytesUtils.bytes2Float(bytes, curParseIndex + 16) : 0;
        if(Float.compare(longitude,Float.NaN) != 0){
            acceleration.setLongitude(longitude);
        }
        float latitude = latlngValid ? BytesUtils.bytes2Float(bytes, curParseIndex + 20) : 0;
        if(Float.compare(latitude,Float.NaN) != 0){
            acceleration.setLatitude(latitude);
        }

        byte[] bytesSpeed = Arrays.copyOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        float speedf = 0;
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        if (!strSp.toLowerCase().equals("ffff")){
            try {
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }catch (Exception e){
                System.out.println("GPS Acceleration Speed Error ; imei is :" + imei);
            }
        }
        acceleration.setSpeed(speedf);
        int azimuth = 0;
        try {
            azimuth = BytesUtils.bytes2Short(bytes, curParseIndex + 26);
            if (azimuth == 255){
                azimuth = 0;
            }
        }catch (Exception e){
            System.out.println("GPS Acceleration azimuth Error ; imei is :" + imei + ":" + BytesUtils.bytes2HexString(bytes,0));
        }
        acceleration.setAzimuth(azimuth);
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long ci_4g = null;
        Integer earfcn_4g_1 = null;
        Integer pcid_4g_1 = null;
        Integer earfcn_4g_2 = null;
        Integer pcid_4g_2 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = bytes[curParseIndex + 12];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bytes,curParseIndex + 12);
            mnc_2g = BytesUtils.bytes2Short(bytes,curParseIndex + 14);
            lac_2g_1 = BytesUtils.bytes2Short(bytes,curParseIndex + 16);
            ci_2g_1 = BytesUtils.bytes2Short(bytes,curParseIndex + 18);
            lac_2g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 20);
            ci_2g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 22);
            lac_2g_3 = BytesUtils.bytes2Short(bytes,curParseIndex + 24);
            ci_2g_3 = BytesUtils.bytes2Short(bytes,curParseIndex + 26);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bytes,curParseIndex + 12) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(bytes,curParseIndex + 14);
            ci_4g = BytesUtils.unsigned4BytesToInt(bytes, curParseIndex + 16);
            earfcn_4g_1 = BytesUtils.bytes2Short(bytes, curParseIndex + 20);
            pcid_4g_1 = BytesUtils.bytes2Short(bytes, curParseIndex + 22);
            earfcn_4g_2 = BytesUtils.bytes2Short(bytes, curParseIndex + 24);
            pcid_4g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 26);
        }
        int rpm = BytesUtils.bytes2Short(bytes, curParseIndex + 28);
        if(rpm == 65535){
            rpm = -999;
        }
        acceleration.setRpm(rpm);
        acceleration.setIs_4g_lbs(is_4g_lbs);
        acceleration.setIs_2g_lbs(is_2g_lbs);
        acceleration.setMcc_2g(mcc_2g);
        acceleration.setMnc_2g(mnc_2g);
        acceleration.setLac_2g_1(lac_2g_1);
        acceleration.setCi_2g_1(ci_2g_1);
        acceleration.setLac_2g_2(lac_2g_2);
        acceleration.setCi_2g_2(ci_2g_2);
        acceleration.setLac_2g_3(lac_2g_3);
        acceleration.setCi_2g_3(ci_2g_3);
        acceleration.setMcc_4g(mcc_4g);
        acceleration.setMnc_4g(mnc_4g);
        acceleration.setCi_4g(ci_4g);
        acceleration.setEarfcn_4g_1(earfcn_4g_1);
        acceleration.setPcid_4g_1(pcid_4g_1);
        acceleration.setEarfcn_4g_2(earfcn_4g_2);
        acceleration.setPcid_4g_2(pcid_4g_2);
        return acceleration;
    }
    private AccelerationData getAlarmAccelerationData(byte[] bytes, String imei, int curParseIndex) {
        AccelerationData acceleration = new AccelerationData();
        acceleration.setImei(imei);
        acceleration.setDate(TimeUtils.getGTM0Date(bytes, curParseIndex));
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

        float altitude = BytesUtils.bytes2Float(bytes, curParseIndex + 12);
        if(Float.compare(altitude,Float.NaN) != 0){
            acceleration.setAltitude(altitude);
        }
        float longitude = BytesUtils.bytes2Float(bytes, curParseIndex + 16);
        if(Float.compare(longitude,Float.NaN) != 0){
            acceleration.setLongitude(longitude);
        }
        float latitude = BytesUtils.bytes2Float(bytes, curParseIndex + 20);
        if(Float.compare(latitude,Float.NaN) != 0){
            acceleration.setLatitude(latitude);
        }

        byte[] bytesSpeed = Arrays.copyOfRange(bytes, curParseIndex + 24, curParseIndex + 26);
        float speedf = 0;
        String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
        if (!strSp.toLowerCase().equals("ffff")){
            try {
                speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
            }catch (Exception e){
                System.out.println("GPS Acceleration Speed Error ; imei is :" + imei);
            }
        }
        acceleration.setSpeed(speedf);
        int azimuth = 0;
        try {
            azimuth = BytesUtils.bytes2Short(bytes, curParseIndex + 26);
            if (azimuth == 255){
                azimuth = 0;
            }
        }catch (Exception e){
            System.out.println("GPS Acceleration azimuth Error ; imei is :" + imei + ":" + BytesUtils.bytes2HexString(bytes,0));
        }
        acceleration.setAzimuth(azimuth);
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long ci_4g = null;
        Integer earfcn_4g_1 = null;
        Integer pcid_4g_1 = null;
        Integer earfcn_4g_2 = null;
        Integer pcid_4g_2 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = bytes[curParseIndex + 12];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bytes,curParseIndex + 12);
            mnc_2g = BytesUtils.bytes2Short(bytes,curParseIndex + 14);
            lac_2g_1 = BytesUtils.bytes2Short(bytes,curParseIndex + 16);
            ci_2g_1 = BytesUtils.bytes2Short(bytes,curParseIndex + 18);
            lac_2g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 20);
            ci_2g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 22);
            lac_2g_3 = BytesUtils.bytes2Short(bytes,curParseIndex + 24);
            ci_2g_3 = BytesUtils.bytes2Short(bytes,curParseIndex + 26);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bytes,curParseIndex + 12) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(bytes,curParseIndex + 14);
            ci_4g = BytesUtils.unsigned4BytesToInt(bytes, curParseIndex + 16);
            earfcn_4g_1 = BytesUtils.bytes2Short(bytes, curParseIndex + 20);
            pcid_4g_1 = BytesUtils.bytes2Short(bytes, curParseIndex + 22);
            earfcn_4g_2 = BytesUtils.bytes2Short(bytes, curParseIndex + 24);
            pcid_4g_2 = BytesUtils.bytes2Short(bytes,curParseIndex + 26);
        }
        int rpm = BytesUtils.bytes2Short(bytes, curParseIndex + 28);
        if(rpm == 65535){
            rpm = -999;
        }
        acceleration.setRpm(rpm);
        acceleration.setIs_4g_lbs(is_4g_lbs);
        acceleration.setIs_2g_lbs(is_2g_lbs);
        acceleration.setMcc_2g(mcc_2g);
        acceleration.setMnc_2g(mnc_2g);
        acceleration.setLac_2g_1(lac_2g_1);
        acceleration.setCi_2g_1(ci_2g_1);
        acceleration.setLac_2g_2(lac_2g_2);
        acceleration.setCi_2g_2(ci_2g_2);
        acceleration.setLac_2g_3(lac_2g_3);
        acceleration.setCi_2g_3(ci_2g_3);
        acceleration.setMcc_4g(mcc_4g);
        acceleration.setMnc_4g(mnc_4g);
        acceleration.setCi_4g(ci_4g);
        acceleration.setEarfcn_4g_1(earfcn_4g_1);
        acceleration.setPcid_4g_1(pcid_4g_1);
        acceleration.setEarfcn_4g_2(earfcn_4g_2);
        acceleration.setPcid_4g_2(pcid_4g_2);
        return acceleration;
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
        int relayStatus = data[13] & 0x3F;
        int rlyMode =  data[13] & 0xCF;
        int smsLanguageType = data[13] & 0xF;
        int ignitionSource = data[13] & 0xf;

        boolean isRelayWaiting = ((data[13] & 0xC0) != 0x00) && ((data[13] & 0x80) == 0x00);
        int dragThreshold = BytesUtils.bytes2Short(data, 14);
        long iop = (long) BytesUtils.bytes2Short(data, 16);
        boolean iopIgnition = (iop & MASK_IGNITION) == MASK_IGNITION;
        boolean iopPowerCutOff = (iop & MASK_POWER_CUT) == MASK_POWER_CUT;
        boolean iopACOn = (iop & MASK_AC) == MASK_AC;
        boolean iopRelay =(iop & MASK_RELAY) == MASK_RELAY;
        int input1 = iopIgnition ? 1 : 0;
        int input2 = iopACOn ? 1 : 0;
        int output1 = (iop & 0x0400) == 0x0400 ? 1 : 0;
        int speakerStatus = (iop & 0x40) ==  0x40  ? 1 : 0;
        int rs232PowerOf5V = (iop & 0x20) ==  0x20  ? 1 : 0;
        int hasThirdPartyObd = (iop & 0x10) ==  0x10  ? 1 : 0;
        int exPowerConsumpStatus = 0;
        if ((iop & 0x06) == 0x02){
            exPowerConsumpStatus = 2;
        }else if ((iop & 0x06) == 0x04){
            exPowerConsumpStatus = 1;
        }else{
            exPowerConsumpStatus = 0;
        }
        byte alarmByte = data[18];
        int originalAlarmCode = (int) alarmByte;
        if(originalAlarmCode < 0){
            originalAlarmCode += 256;
        }
        int externalPowerReduceValue = (data[19] & 0x11);
        int mileageSource  = (data[19] & 0x02) == 0x02 ? 1 : 0;
        boolean isSendSmsAlarmToManagerPhone = (data[19] & 0x20) == 0x20;
        boolean isSendSmsAlarmWhenDigitalInput2Change = (data[19] & 0x10) == 0x10;
        int jammerDetectionStatus = (data[19] & 0xC);
        boolean isAlarmData = command[2] == 0x04  || command[2] == 0x18;
        long mileage = BytesUtils.unsigned4BytesToInt(data, 20);
        byte[] batteryBytes = new byte[]{data[24]};
        String batteryStr = BytesUtils.bytes2HexString(batteryBytes, 0);
        Date gmt0 = TimeUtils.getGTM0Date(data, 25);
        boolean latlngValid = (data[9] & 0x40) != 0x00;
        byte[] latlngData = Arrays.copyOfRange(data,31,47);
        if (Arrays.equals(latlngData,latlngInvalidData)){
            latlngValid = false;
        }
        double altitude = latlngValid? BytesUtils.bytes2Float(data, 31) : 0.0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(data, 39) : 0.0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(data, 35) : 0.0;
        int azimuth = latlngValid ? BytesUtils.bytes2Short(data, 45) : 0;
        Boolean is_4g_lbs = false;
        Integer mcc_4g = null;
        Integer mnc_4g = null;
        Long eci_4g = null;
        Integer tac = null;
        Integer pcid_4g_1 = null;
        Integer pcid_4g_2 = null;
        Integer pcid_4g_3 = null;
        Boolean is_2g_lbs = false;
        Integer mcc_2g = null;
        Integer mnc_2g = null;
        Integer lac_2g_1 = null;
        Integer ci_2g_1 = null;
        Integer lac_2g_2 = null;
        Integer ci_2g_2 = null;
        Integer lac_2g_3 = null;
        Integer ci_2g_3 = null;
        if (!latlngValid){
            byte lbsByte = data[31];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(data,31);
            mnc_2g = BytesUtils.bytes2Short(data,33);
            lac_2g_1 = BytesUtils.bytes2Short(data,35);
            ci_2g_1 = BytesUtils.bytes2Short(data,37);
            lac_2g_2 = BytesUtils.bytes2Short(data,39);
            ci_2g_2 = BytesUtils.bytes2Short(data,41);
            lac_2g_3 = BytesUtils.bytes2Short(data,43);
            ci_2g_3 = BytesUtils.bytes2Short(data,45);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(data,31) & 0x7FFF;
            mnc_4g = BytesUtils.bytes2Short(data,33);
            eci_4g = BytesUtils.unsigned4BytesToInt(data, 35);
            tac = BytesUtils.bytes2Short(data, 39);
            pcid_4g_1 = BytesUtils.bytes2Short(data, 41);
            pcid_4g_2 = BytesUtils.bytes2Short(data, 43);
            pcid_4g_3 = BytesUtils.bytes2Short(data,45);
        }
        Float externalPowerVoltage = 0f;
        byte[] externalPowerVoltageBytes = Arrays.copyOfRange(data, 47, 49);
        String externalPowerVoltageStr = BytesUtils.bytes2HexString(externalPowerVoltageBytes, 0);
        try{
            externalPowerVoltage = Float.valueOf(externalPowerVoltageStr) / 100.0f;
        }catch (Exception e){
            System.out.println("externalPowerVoltage error, imei = " + imei + " externalPowerVoltage :" + externalPowerVoltageStr);
        }
        Float speedf = 0.0f;
        try{
            byte[] bytesSpeed = Arrays.copyOfRange(data, 49, 51);
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

        long accumulatingFuelConsumption = BytesUtils.unsigned4BytesToInt(data, 51);
        if(accumulatingFuelConsumption == 4294967295l){
            accumulatingFuelConsumption = -999;
        }
        long instantFuelConsumption =  BytesUtils.unsigned4BytesToInt(data, 55);
        if(instantFuelConsumption == 4294967295l){
            instantFuelConsumption = -999;
        }
        int rpm = BytesUtils.bytes2Short(data, 59);
        if(rpm == 65535){
            rpm = -999;
        }
        int airInput = (int)data[61] < 0 ? (int)data[61] + 256 : (int)data[61];
        if(airInput == 255){
            airInput = -999;
        }
        int airPressure = (int)data[62] < 0 ? (int)data[62] + 256 : (int)data[62];
        if(airPressure == 255){
            airPressure = -999;
        }
        int coolingFluidTemp = (int)data[63] < 0 ? (int)data[63] + 256 : (int)data[63];
        if(coolingFluidTemp == 255){
            coolingFluidTemp = -999;
        }else{
            coolingFluidTemp = coolingFluidTemp - 40;
        }
        int airInflowTemp = (int)data[64] < 0 ? (int)data[64] + 256 : (int)data[64];
        if(airInflowTemp == 255){
            airInflowTemp = -999;
        }else {
            airInflowTemp = airInflowTemp - 40;
        }
        int engineLoad = (int)data[65] < 0 ? (int)data[65] + 256 : (int)data[65];
        if(engineLoad == 255){
            engineLoad = -999;
        }
        int throttlePosition = (int)data[66] < 0 ? (int)data[66] + 256 : (int)data[66];
        if(throttlePosition == 255){
            throttlePosition = -999;
        }
        int remainFuelRate = data[67] & 0x7f;
        int remainFuelUnit = (data[67] & 0x80) == 0x80 ? 1 : 0;
        if(data[67] == -1){ // == 0xff
            remainFuelRate = -999;
            remainFuelUnit = -999;
        }


        LocationMessage message;
        if (isAlarmData){
            message = new LocationAlarmMessage();
            message.setProtocolHeadType(0x04);
        }else {
            message = new LocationInfoMessage();
            message.setProtocolHeadType(0x02);
        }
        int protocolHead = bytes[2];
        if ((protocolHead == 0x16 || protocolHead == 0x18) && data.length >= 80){
            byte[] axisXByte = Arrays.copyOfRange(data,68,70);
            BigInteger axisXB = new BigInteger(axisXByte);
            byte[] axisYByte = Arrays.copyOfRange(data,70,72);
            BigInteger axisYB = new BigInteger(axisYByte);
            byte[] axisZByte = Arrays.copyOfRange(data,72,74);
            BigInteger axisZB = new BigInteger(axisZByte);
            int axisX = axisXB.shortValue();
            int axisY = axisYB.shortValue();
            int axisZ =  axisZB.shortValue();
            byte[] gyroscopeAxisXByte = Arrays.copyOfRange(data,74,76);
            BigInteger gyroscopeAxisXB = new BigInteger(gyroscopeAxisXByte);
            byte[] gyroscopeAxisYByte = Arrays.copyOfRange(data,76,78);
            BigInteger gyroscopeAxisYB = new BigInteger(gyroscopeAxisYByte);
            byte[] gyroscopeAxisZByte = Arrays.copyOfRange(data, 78, 80);
            BigInteger gyroscopeAxisZB = new BigInteger(gyroscopeAxisZByte);
            int gyroscopeAxisX =  gyroscopeAxisXB.shortValue();
            int gyroscopeAxisY =  gyroscopeAxisYB.shortValue();
            int gyroscopeAxisZ =  gyroscopeAxisZB.shortValue();
            message.setAxisX(axisX);
            message.setAxisY(axisY);
            message.setAxisZ(axisZ);
            message.setGyroscopeAxisX(gyroscopeAxisX);
            message.setGyroscopeAxisY(gyroscopeAxisY);
            message.setGyroscopeAxisZ(gyroscopeAxisZ);

        }
        message.setOrignBytes(bytes);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        message.setSerialNo(serialNo);
//        message.setIsNeedResp(isNeedResp);
        message.setImei(imei);
        message.setProtocolHeadType(protocolHead);
        message.setOutput1(output1);
        message.setInput1(input1);
        message.setInput2(input2);
        message.setNetworkSignal(networkSignal);
        message.setSamplingIntervalAccOn((long)samplingIntervalAccOn);
        message.setSamplingIntervalAccOff((long)samplingIntervalAccOff);
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
        message.setRelayStatus(relayStatus);
        message.setRlyMode(rlyMode);
        message.setSmsLanguageType(smsLanguageType);
        message.setIsRelayWaiting(isRelayWaiting);
        message.setDragThreshold(dragThreshold);
        message.setIOP(iop);
        message.setIopIgnition(iopIgnition);
        message.setExternalPowerReduceStatus(externalPowerReduceValue);
        message.setIopPowerCutOff(iopPowerCutOff);
        message.setIopACOn(iopACOn);
        message.setOriginalAlarmCode(originalAlarmCode);
        message.setSpeakerStatus(speakerStatus);
        message.setRs232PowerOf5V(rs232PowerOf5V);
        message.setMileage(mileage);
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
        message.setSpeed(speedf);
        message.setAzimuth(azimuth);
        message.setExternalPowerVoltage(externalPowerVoltage);
        message.setAccumulatingFuelConsumption(accumulatingFuelConsumption);
        message.setInstantFuelConsumption(instantFuelConsumption);
        message.setRpm(rpm);
        message.setAirInflowTemp(airInflowTemp);
        message.setAirInput(airInput);
        message.setAirPressure(airPressure);
        message.setCoolingFluidTemp(coolingFluidTemp);
        message.setEngineLoad(engineLoad);
        message.setThrottlePosition(throttlePosition);
        message.setRemainFuelRate(remainFuelRate);
        message.setRemainFuelUnit(remainFuelUnit);
        message.setIs_4g_lbs(is_4g_lbs);
        message.setIs_2g_lbs(is_2g_lbs);
        message.setMcc_2g(mcc_2g);
        message.setMnc_2g(mnc_2g);
        message.setLac_2g_1(lac_2g_1);
        message.setCi_2g_1(ci_2g_1);
        message.setLac_2g_2(lac_2g_2);
        message.setCi_2g_2(ci_2g_2);
        message.setLac_2g_3(lac_2g_3);
        message.setCi_2g_3(ci_2g_3);
        message.setMcc_4g(mcc_4g);
        message.setMnc_4g(mnc_4g);
        message.setEci_4g(eci_4g);
        message.setTac(tac);
        message.setPcid_4g_1(pcid_4g_1);
        message.setPcid_4g_2(pcid_4g_2);
        message.setPcid_4g_3(pcid_4g_3);
        message.setIsSendSmsAlarmWhenDigtalInput2Change(isSendSmsAlarmWhenDigitalInput2Change);
        message.setIsSendSmsAlarmToManagerPhone(isSendSmsAlarmToManagerPhone);
        message.setJammerDetectionStatus(jammerDetectionStatus);
        message.setIgnitionSource(ignitionSource);
        message.setExPowerConsumpStatus(exPowerConsumpStatus);
        message.setHasThirdPartyObd(hasThirdPartyObd);
        message.setMileageSource(mileageSource);
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
         * @param ussdMessage the USSD message.
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

        void receiveObdMessage(ObdMessage obdMessage);

        void receiveDebugMessage(DebugMessage debugMessage);
        void receiveWifiMessage(WifiMessage wifiMessage);
    }
}
