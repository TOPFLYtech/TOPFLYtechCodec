package com.topflytech.codec;

import java.io.IOException;

/**
 *  New Device Encoder like 8806+,8806+r
 */
public class T880xdEncoder {
    private int encryptType = 0;
    private String aesKey;

    /**
     * Instantiates a new T880xPlusEncoder.
     *
     * @param messageEncryptType The message encrypt type .Use the value of MessageEncryptType.
     * @param aesKey             The aes key.If you do not use AES encryption, the value can be empty.
     */
    public T880xdEncoder(int messageEncryptType, String aesKey){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
    }
    /**
     * Get sign in msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getSignInMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x01};
        return Encoder.getSignInMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    }

    /**
     * Get heartbeat msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getHeartbeatMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x03};
        return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }

    /**
     * Get location msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getLocationMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException{
        byte[] command = {0x26, 0x26, 0x02};
        return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }

    /**
     * Get location alarm msg reply byte [ ].
     *
     * @param imei            the imei
     * @param needSerialNo    the need serial no
     * @param serialNo        the serial no
     * @param sourceAlarmCode the source alarm code
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getLocationAlarmMsgReply(String imei,boolean needSerialNo,int serialNo,int sourceAlarmCode) throws IOException {
        byte[] command = {0x26, 0x26, 0x04};
        return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
    }


    /**
     * Get gps driver behavoir msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getGpsDriverBehaviorMsgReply(String imei, int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x05};
        return Encoder.getGpsDriverBehavoirMsgReply(imei, serialNo, command, encryptType, aesKey);
    }

    /**
     * Get acceleration driver behavior msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getAccelerationDriverBehaviorMsgReply(String imei, int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x06};
        return Encoder.getAccelerationDriverBehaviorMsgReply(imei, serialNo, command, encryptType, aesKey);
    }

    /**
     * Get acceleration alarm msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getAccelerationAlarmMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x07};
        return Encoder.getAccelerationAlarmMsgReply(imei, serialNo, command, encryptType, aesKey);
    }


    /**
     *  Get bluetooth peripheral data msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getBluetoothPeripheralMsgReply(String imei,int serialNo,int protocolHeadType) throws IOException {
        byte[] command = {0x26, 0x26, (byte)protocolHeadType};
        return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
    }

    public byte[] getNetworkMsgReply(String imei,int serialNo)throws IOException {
        byte[] command = {0x26, 0x26, 0x11};
        return Encoder.getNetworkMsgReply(imei, serialNo, command, encryptType, aesKey);
    }
    /**
     * Get Obd msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getObdMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x26, 0x26, 0x09};
        return Encoder.getObdMsgReply(imei, serialNo, command, encryptType, aesKey);
    }
    /**
     * Get config setting msg byte [ ].
     *
     * @param imei    the imei
     * @param content the config content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getConfigSettingMsg(String imei,String content) throws IOException {
        byte[] command = {0x26, 0x26, (byte)0x81};
        return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
    }

    /**
     * Get brocast sms msg byte [ ].
     *
     * @param imei    the imei
     * @param content the content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getBrocastSmsMsg(String imei,String content) throws IOException {
        byte[] command = {0x26, 0x26, (byte)0x81};
        return Encoder.getBrocastSmsMsg(imei, content, command, encryptType, aesKey);
    }


    /**
     * get forward sms msg byte [].
     * @param imei the imei
     * @param phoneNumb the phone number need send to
     * @param content the content
     * @return the byte[]
     * @throws IOException the io exception
     */
    public  byte[] getForwardMsg(String imei,String phoneNumb,String content) throws IOException{
        byte[] command = {0x26, 0x26, (byte)0x81};
        return Encoder.getForwardSmsMsg(imei, phoneNumb, content, command, encryptType, aesKey);
    }


    /**
     * Get USSD msg byte [ ].
     *
     * @param imei    the imei
     * @param content the content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getUSSDMsg(String imei,String content) throws IOException {
        byte[] command = {0x26, 0x26, (byte)0x81};
        return Encoder.getUSSDMsg(imei, content, command, encryptType, aesKey);
    }



    /**
     * Get Obd config setting msg byte [ ].
     *
     * @param imei    the imei
     * @param content the config content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public byte[] getObdConfigSettingMsg(String imei,String content) throws IOException {
        byte[] command = {0x26, 0x26, (byte)0x82};
        return Encoder.getConfigSettingMsg(imei, content, command, encryptType, aesKey);
    }


    public byte[] getClearObdErrorCodeMsg(String imei) throws IOException {
        byte[] content = {(byte)0x55,(byte)0xAA,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x04,(byte)0x06,(byte)0x0D,(byte)0x0A};
        byte[] command = {0x26, 0x26, (byte)0x82};
        return Encoder.getObdConfigSettingMsg(imei, content, command, encryptType, aesKey);
    }

    public byte[] getObdVinMsg(String imei) throws IOException {
        byte[] content = {(byte)0x55,(byte)0xAA,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x05,(byte)0x07,(byte)0x0D,(byte)0x0A};
        byte[] command = {0x26, 0x26, (byte)0x82};
        return Encoder.getObdConfigSettingMsg(imei, content, command, encryptType, aesKey);
    }

}
