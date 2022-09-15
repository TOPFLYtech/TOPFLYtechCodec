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
    private static final byte[] BLUETOOTH_DATA =       {0x27, 0x27, (byte)0x10};
    private static final byte[] WIFI_DATA =  {0x27, 0x27, (byte)0x15};
    private static final byte[] LOCK_DATA =  {0x27, 0x27, (byte)0x17};
    private static final byte[] latlngInvalidData = {(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
            (byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
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
                || Arrays.equals(BLUETOOTH_DATA,bytes)
                || Arrays.equals(WIFI_DATA,bytes)
                || Arrays.equals(LOCK_DATA,bytes)
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
                if(packageLength <= 0){
                    callback.receiveErrorMessage("Error Message");
                    return;
                }
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
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    return bluetoothPeripheralDataMessage;
                case 0x17:
                    LockMessage lockMessage = parseLockMessage(bytes);
                    return lockMessage;
                case 0x15:
                    WifiMessage  wifiMessage = parseWifiMessage(bytes);
                    return wifiMessage;
                case (byte)0x81:
                    Message message =  parseInteractMessage(bytes);
                    return message;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }
        return null;
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
        if(bleData[0] == 0x00 && bleData[1] == 0x01){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TIRE);
            for (int i = 2;i+10 <= bleData.length;i+=10){
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
            if (latlngValid){
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
            }
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
                mcc_4g = BytesUtils.bytes2Short(bleData,11);
                mnc_4g = BytesUtils.bytes2Short(bleData,13);
                ci_4g = BytesUtils.unsigned4BytesToInt(bleData, 15);
                earfcn_4g_1 = BytesUtils.bytes2Short(bleData, 19);
                pcid_4g_1 = BytesUtils.bytes2Short(bleData, 21);
                earfcn_4g_2 = BytesUtils.bytes2Short(bleData, 23);
                pcid_4g_2 = BytesUtils.bytes2Short(bleData,25);
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
            bleAlertData.setCi_4g(ci_4g);
            bleAlertData.setEarfcn_4g_1(earfcn_4g_1);
            bleAlertData.setPcid_4g_1(pcid_4g_1);
            bleAlertData.setEarfcn_4g_2(earfcn_4g_2);
            bleAlertData.setPcid_4g_2(pcid_4g_2);
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
            if (latlngValid){
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
            }
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
                byte lbsByte = bleData[11];
                if ((lbsByte & 0x8) == 0x8){
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
                mcc_4g = BytesUtils.bytes2Short(bleData,11);
                mnc_4g = BytesUtils.bytes2Short(bleData,13);
                ci_4g = BytesUtils.unsigned4BytesToInt(bleData, 15);
                earfcn_4g_1 = BytesUtils.bytes2Short(bleData, 19);
                pcid_4g_1 = BytesUtils.bytes2Short(bleData, 21);
                earfcn_4g_2 = BytesUtils.bytes2Short(bleData, 23);
                pcid_4g_2 = BytesUtils.bytes2Short(bleData,25);
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
            bleDriverSignInData.setCi_4g(ci_4g);
            bleDriverSignInData.setEarfcn_4g_1(earfcn_4g_1);
            bleDriverSignInData.setPcid_4g_1(pcid_4g_1);
            bleDriverSignInData.setEarfcn_4g_2(earfcn_4g_2);
            bleDriverSignInData.setPcid_4g_2(pcid_4g_2);
            bleDataList.add(bleDriverSignInData);
        }else if (bleData[0] == 0x00 && bleData[1] == 0x04){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_TEMP);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i+15 <= bleData.length;i+=15){
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
                bleDataList.add(bleTempData);
            }
        }else if (bleData[0] == 0x00 && bleData[1] == 0x05){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_DOOR);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            for (int i = 2;i+12 <= bleData.length;i+=12){
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
            for (int i = 2;i+12 <= bleData.length;i+=12){
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
                int CtrlStatus = (int) bleData[i+10] < 0 ? (int) bleData[i+10] + 256 : (int) bleData[i+10];
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
            for (int i = 2;i+15 <= bleData.length;i+=15){
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
        }else if (bleData[0] == 0x00 && bleData[1] == 0x0d){
            bluetoothPeripheralDataMessage.setMessageType(BluetoothPeripheralDataMessage.MESSAGE_TYPE_Customer2397);
            int i = 2;
            while (i + 8 < bleData.length){
                BleCustomer2397SensorData bleCustomer2397SensorData = new BleCustomer2397SensorData();
                byte[] macArray = Arrays.copyOfRange(bleData, i + 0, i + 6);
                String mac = BytesUtils.bytes2HexString(macArray, 0);
                i+=6;
                i+=1;
                int rawDataLen = bleData[i] < 0 ? bleData[i] + 256 : bleData[i];
                if(i + rawDataLen >= bleData.length || rawDataLen < 1){
                    break;
                }
                i += 1;
                byte[] rawData = Arrays.copyOfRange(bleData,i,i+rawDataLen-1);
                i += rawDataLen - 1;
                int rssiTemp = (int) bleData[i] < 0 ? (int) bleData[i] + 256 : (int) bleData[i];
                int rssi;
                if(rssiTemp == 255){
                    rssi = -999;
                }else{
                    rssi = rssiTemp - 128;
                }
                i += 1;
                bleCustomer2397SensorData.setRawData(rawData);
                bleCustomer2397SensorData.setMac(mac);
                bleCustomer2397SensorData.setRssi(rssi);
                bleDataList.add(bleCustomer2397SensorData);
            }
        }
        bluetoothPeripheralDataMessage.setBleDataList(bleDataList);
        return bluetoothPeripheralDataMessage;
    }

    private LocationMessage parseDataMessage(byte[] data) {
        int serialNo = BytesUtils.bytes2Short(data, 5);
        String imei = BytesUtils.IMEI.decode(data, 7);
        Date date = TimeUtils.getGTM0Date(data, 17);
        boolean isGpsWorking = (data[15] & 0x20) == 0x00;
        boolean isHistoryData = (data[15] & 0x80) != 0x00;
        boolean latlngValid = (data[15] & 0x40) == 0x40;
        int satelliteNumber = data[15] & 0x1F;
        double altitude = latlngValid ? BytesUtils.bytes2Float(data, 23) : 0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(data,27) : 0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(data,31) : 0;
        Float speedf = 0.0f;
        if (latlngValid){
            try{
                if (latlngValid) {
                    byte[] bytesSpeed = Arrays.copyOfRange(data, 35, 37);
                    String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                    if(!strSp.toLowerCase().equals("ffff")){
                        speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
                    }
                }
            }catch (Exception e){
//                e.printStackTrace();
            }
        }
        int azimuth = latlngValid ? BytesUtils.bytes2Short(data, 37) : 0;

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
            byte lbsByte = data[23];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(data,23);
            mnc_2g = BytesUtils.bytes2Short(data,25);
            lac_2g_1 = BytesUtils.bytes2Short(data,27);
            ci_2g_1 = BytesUtils.bytes2Short(data,29);
            lac_2g_2 = BytesUtils.bytes2Short(data,31);
            ci_2g_2 = BytesUtils.bytes2Short(data,33);
            lac_2g_3 = BytesUtils.bytes2Short(data,35);
            ci_2g_3 = BytesUtils.bytes2Short(data,37);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(data,23);
            mnc_4g = BytesUtils.bytes2Short(data,25);
            ci_4g = BytesUtils.unsigned4BytesToInt(data, 27);
            earfcn_4g_1 = BytesUtils.bytes2Short(data, 31);
            pcid_4g_1 = BytesUtils.bytes2Short(data, 33);
            earfcn_4g_2 = BytesUtils.bytes2Short(data, 35);
            pcid_4g_2 = BytesUtils.bytes2Short(data,37);
        }

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
//                e.printStackTrace();
            }
        }
        float deviceTemp = -999;
        if ( data[45] != 0xff){
            deviceTemp = (data[45] & 0x7F) * ((data[45] & 0x80) == 0x80 ? -1 : 1);
        }
        byte[] lightSensorBytes = new byte[]{data[46]};
        String lightSensorStr = BytesUtils.bytes2HexString(lightSensorBytes, 0);
        float lightSensor = 0;
        if(lightSensorStr.toLowerCase().equals("ff")){
            lightSensor = -999;
        }else{
            try{
                lightSensor = Integer.parseInt(lightSensorStr) / 10.0f;
            }catch (Exception e){
//                e.printStackTrace();
            }

        }

        byte[] batteryVoltageBytes = new byte[]{data[47]};
        String batteryVoltageStr = BytesUtils.bytes2HexString(batteryVoltageBytes, 0);
        float batteryVoltage = 0;
        if(batteryVoltageStr.toLowerCase().equals("ff")){
            batteryVoltage = -999;
        }else{
            try{
                batteryVoltage = Integer.parseInt(batteryVoltageStr) / 10.0f;
            }catch (Exception e){
//                e.printStackTrace();
            }
        }
        byte[] solarVoltageBytes = new byte[]{data[48]};
        String solarVoltageStr = BytesUtils.bytes2HexString(solarVoltageBytes, 0);
        float solarVoltage = 0;
        if(solarVoltageStr.toLowerCase().equals("ff")){
            solarVoltage = -999;
        }else{
            try{
                solarVoltage = Integer.parseInt(solarVoltageStr) / 10.0f;
            }catch (Exception e){
//                e.printStackTrace();
            }
        }

        long mileage = BytesUtils.unsigned4BytesToInt(data, 49);
        int status = BytesUtils.bytes2Short(data, 53);
        int network = (status & 0x7F0) >> 4;
        int accOnInterval = BytesUtils.bytes2Short(data, 55);
        int accOffInterval = BytesUtils.bytes2Integer(data, 57);
        int angleCompensation = (int) (data[61] & 0xff);
        if (angleCompensation < 0){
            angleCompensation += 256;
        }
        int distanceCompensation = BytesUtils.bytes2Short(data, 62);
        int heartbeatInterval = (int) data[64];
        boolean isUsbCharging = (status & 0x8000) == 0x8000;
        boolean isSolarCharging = (status & 0x8) == 0x8;
        boolean iopIgnition = (status & 0x4) == 0x4;
        byte alarmByte = data[16];
        int originalAlarmCode = (int) alarmByte;
        if(originalAlarmCode < 0){
            originalAlarmCode += 256;
        }
        byte[] command = Arrays.copyOf(data, HEADER_LENGTH);
        boolean isAlarmData = command[2] == 0x04;
        byte status1 = data[54];
        String smartPowerOpenStatus = "close";
        if((status1 & 0x01) == 0x01){
            smartPowerOpenStatus = "open";
        }
        byte status2 = data[66];
        boolean isLockSim = (status2 & 0x80) == 0x80;
        boolean isLockDevice = (status2 & 0x40) == 0x40;
        boolean AGPSEphemerisDataDownloadSettingStatus = (status2 & 0x20) == 0x10;
        boolean gSensorSettingStatus = (status2 & 0x10) == 0x10;
        boolean frontSensorSettingStatus = (status2 & 0x08) == 0x08;
        boolean deviceRemoveAlarmSettingStatus = (status2 & 0x04) == 0x04;
        boolean openCaseAlarmSettingStatus = (status2 & 0x02) == 0x02;
        boolean deviceInternalTempReadingANdUploadingSettingStatus = (status2 & 0x01) == 0x01;
        byte status3 = data[67];
        String smartPowerSettingStatus = "disable";
        if((status3 & 0x80) == 0x80){
            smartPowerSettingStatus = "enable";
        }
        Integer lockType = null;
        if (data.length >= 71){
            lockType = (int)data[70];
        }
        LocationMessage locationMessage;
        if (isAlarmData){
            locationMessage = new LocationAlarmMessage();
            locationMessage.setProtocolHeadType(0x04);
        }else {
            locationMessage = new LocationInfoMessage();
            locationMessage.setProtocolHeadType(0x02);
        }
        locationMessage.setOrignBytes(data);
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        locationMessage.setSerialNo(serialNo);
//        locationMessage.setIsNeedResp(isNeedResp);
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
        locationMessage.setMileage(mileage);
        locationMessage.setIopIgnition(iopIgnition);
        locationMessage.setIOP(locationMessage.isIopIgnition() ? 0x4000l : 0x0000l);
        locationMessage.setBatteryCharge(batteryPercent);
        locationMessage.setDate(date);
        locationMessage.setLatlngValid(latlngValid);
        locationMessage.setAltitude(altitude);
        locationMessage.setLatitude(latitude);
        locationMessage.setLongitude(longitude);
        locationMessage.setIsLockSim(isLockSim);
        locationMessage.setIsLockDevice(isLockDevice);
        locationMessage.setAGPSEphemerisDataDownloadSettingStatus(AGPSEphemerisDataDownloadSettingStatus);
        locationMessage.setgSensorSettingStatus(gSensorSettingStatus);
        locationMessage.setFrontSensorSettingStatus(frontSensorSettingStatus);
        locationMessage.setDeviceRemoveAlarmSettingStatus(deviceRemoveAlarmSettingStatus);
        locationMessage.setOpenCaseAlarmSettingStatus(openCaseAlarmSettingStatus);
        locationMessage.setDeviceInternalTempReadingANdUploadingSettingStatus(deviceInternalTempReadingANdUploadingSettingStatus);
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
        locationMessage.setSmartPowerSettingStatus(smartPowerSettingStatus);
        locationMessage.setSmartPowerOpenStatus(smartPowerOpenStatus);
        locationMessage.setIs_4g_lbs(is_4g_lbs);
        locationMessage.setIs_2g_lbs(is_2g_lbs);
        locationMessage.setMcc_2g(mcc_2g);
        locationMessage.setMnc_2g(mnc_2g);
        locationMessage.setLac_2g_1(lac_2g_1);
        locationMessage.setCi_2g_1(ci_2g_1);
        locationMessage.setLac_2g_2(lac_2g_2);
        locationMessage.setCi_2g_2(ci_2g_2);
        locationMessage.setLac_2g_3(lac_2g_3);
        locationMessage.setCi_2g_3(ci_2g_3);
        locationMessage.setMcc_4g(mcc_4g);
        locationMessage.setMnc_4g(mnc_4g);
        locationMessage.setCi_4g(ci_4g);
        locationMessage.setEarfcn_4g_1(earfcn_4g_1);
        locationMessage.setPcid_4g_1(pcid_4g_1);
        locationMessage.setEarfcn_4g_2(earfcn_4g_2);
        locationMessage.setPcid_4g_2(pcid_4g_2);
        locationMessage.setLockType(lockType);
        return locationMessage;
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
                case 0x10:
                    BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage = parseBluetoothDataMessage(bytes);
                    callback.receiveBluetoothDataMessage(bluetoothPeripheralDataMessage);
                case 0x17:
                    LockMessage lockMessage = parseLockMessage(bytes);
                    callback.receiveLockMessage(lockMessage);
                case 0x15:
                    WifiMessage wifiMessage = parseWifiMessage(bytes);
                    callback.receiveWifiMessage(wifiMessage);
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

    private LockMessage parseLockMessage(byte[] bytes) {
        LockMessage lockMessage = new LockMessage();
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        Date date = TimeUtils.getGTM0Date(bytes, 15);
        boolean latlngValid = (bytes[21] & 0x40) != 0x00;
        boolean isGpsWorking = (bytes[21] & 0x20) == 0x00;
        boolean isHistoryData = (bytes[21] & 0x80) != 0x00;
        int satelliteNumber = bytes[21] & 0x1F;
        byte[] latlngData = Arrays.copyOfRange(bytes,22,38);
        if (Arrays.equals(latlngData,latlngInvalidData)){
            latlngValid = false;
        }
        double altitude = latlngValid ? BytesUtils.bytes2Float(bytes, 22) : 0;
        double longitude = latlngValid ? BytesUtils.bytes2Float(bytes,26) : 0;
        double latitude = latlngValid ? BytesUtils.bytes2Float(bytes,30) : 0;
        Float speedf = 0.0f;
        if (latlngValid){
            try{
                if (latlngValid) {
                    byte[] bytesSpeed = Arrays.copyOfRange(bytes, 34, 36);
                    String strSp = BytesUtils.bytes2HexString(bytesSpeed, 0);
                    if(!strSp.toLowerCase().equals("ffff")){
                        speedf = Float.parseFloat(String.format("%d.%d", Integer.parseInt(strSp.substring(0, 3)), Integer.parseInt(strSp.substring(3, strSp.length()))));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        int azimuth = latlngValid ? BytesUtils.bytes2Short(bytes, 36) : 0;
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
            byte lbsByte = bytes[22];
            if ((lbsByte & 0x80) == 0x80){
                is_4g_lbs = true;
            }else{
                is_2g_lbs = true;
            }
        }
        if (is_2g_lbs){
            mcc_2g = BytesUtils.bytes2Short(bytes,22);
            mnc_2g = BytesUtils.bytes2Short(bytes,24);
            lac_2g_1 = BytesUtils.bytes2Short(bytes,26);
            ci_2g_1 = BytesUtils.bytes2Short(bytes,28);
            lac_2g_2 = BytesUtils.bytes2Short(bytes,30);
            ci_2g_2 = BytesUtils.bytes2Short(bytes,32);
            lac_2g_3 = BytesUtils.bytes2Short(bytes,34);
            ci_2g_3 = BytesUtils.bytes2Short(bytes,36);
        }
        if (is_4g_lbs){
            mcc_4g = BytesUtils.bytes2Short(bytes,22);
            mnc_4g = BytesUtils.bytes2Short(bytes,24);
            ci_4g = BytesUtils.unsigned4BytesToInt(bytes, 26);
            earfcn_4g_1 = BytesUtils.bytes2Short(bytes, 30);
            pcid_4g_1 = BytesUtils.bytes2Short(bytes, 32);
            earfcn_4g_2 = BytesUtils.bytes2Short(bytes, 34);
            pcid_4g_2 = BytesUtils.bytes2Short(bytes,36);
        }
        int lockType = bytes[38] & 0xff;
        if(lockType < 0){
            lockType += 256;
        }
        int idLen = (bytes[39] & 0xff) * 2;
        String idStr = BytesUtils.bytes2HexString(bytes,40);
        String id = idStr;
        if (idStr.length() > idLen){
            id = idStr.substring(0,idLen);
        }
        id = id.toUpperCase();
        lockMessage.setProtocolHeadType(bytes[2]);
        lockMessage.setSerialNo(serialNo);
        lockMessage.setImei(imei);
        lockMessage.setDate(date);
        lockMessage.setOrignBytes(bytes);
        lockMessage.setLatlngValid(latlngValid);
        lockMessage.setAltitude(altitude);
        lockMessage.setLongitude(longitude);
        lockMessage.setLatitude(latitude);
        lockMessage.setLatlngValid(latlngValid);
        lockMessage.setSpeed(speedf);
        lockMessage.setAzimuth(azimuth);
        lockMessage.setLockType(lockType);
        lockMessage.setLockId(id);

        lockMessage.setHistoryData(isHistoryData);
        lockMessage.setSatelliteNumber(satelliteNumber);
        lockMessage.setGpsWorking(isGpsWorking);
        lockMessage.setIs_4g_lbs(is_4g_lbs);
        lockMessage.setIs_2g_lbs(is_2g_lbs);
        lockMessage.setMcc_2g(mcc_2g);
        lockMessage.setMnc_2g(mnc_2g);
        lockMessage.setLac_2g_1(lac_2g_1);
        lockMessage.setCi_2g_1(ci_2g_1);
        lockMessage.setLac_2g_2(lac_2g_2);
        lockMessage.setCi_2g_2(ci_2g_2);
        lockMessage.setLac_2g_3(lac_2g_3);
        lockMessage.setCi_2g_3(ci_2g_3);
        lockMessage.setMcc_4g(mcc_4g);
        lockMessage.setMnc_4g(mnc_4g);
        lockMessage.setCi_4g(ci_4g);
        lockMessage.setEarfcn_4g_1(earfcn_4g_1);
        lockMessage.setPcid_4g_1(pcid_4g_1);
        lockMessage.setEarfcn_4g_2(earfcn_4g_2);
        lockMessage.setPcid_4g_2(pcid_4g_2);

        return lockMessage;
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

    private SignInMessage parseLoginMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        String software = String.format("V%d.%d.%d",  bytes[15] & 0xf, (bytes[16] & 0xf0) >> 4, bytes[16] & 0xf);
        String firmware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 20, 22), 0);
        firmware = String.format("V%s.%s.%s.%s", firmware.substring(0,1), firmware.substring(1, 2), firmware.substring(2,3),firmware.substring(3,4));
        String hardware = BytesUtils.bytes2HexString(Arrays.copyOfRange(bytes, 22, 23), 0);
        hardware = String.format("V%s.%s", hardware.substring(0,1), hardware.substring(1, 2));
        SignInMessage signInMessage = new SignInMessage();
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
        signInMessage.setSerialNo(serialNo);
//        signInMessage.setIsNeedResp(isNeedResp);
        signInMessage.setImei(imei);
        signInMessage.setSoftware(software);
        signInMessage.setFirmware(firmware);
        signInMessage.setHardware(hardware);
        signInMessage.setOrignBytes(bytes);
        return signInMessage;
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
        try{
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
        }catch (Exception e){
            System.out.println("error NetworkInfo:" + imei + ";src:" + BytesUtils.bytes2HexString(bytes,0));
            return null;
        }
    }

    private Message parseInteractMessage(byte[] bytes) throws ParseException {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        byte protocol = bytes[15];
        byte[] data = Arrays.copyOfRange(bytes, 16, bytes.length);
        String messageData;
//        boolean isNeedResp = (serialNo & 0x8000) != 0x8000;
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

        /**
         * receive bluetooth data message
         * @param bluetoothPeripheralDataMessage the bluetooth data message
         */
        void receiveBluetoothDataMessage(BluetoothPeripheralDataMessage bluetoothPeripheralDataMessage);

        void receiveNetworkInfoMessage(NetworkInfoMessage networkInfoMessage);

        void receiveWifiMessage(WifiMessage wifiMessage);

        void receiveLockMessage(LockMessage lockMessage);
        /**
         * Receive error message.
         *
         * @param msg the msg
         */
        void receiveErrorMessage(String msg);
    }
}
