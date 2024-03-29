package com.topflytech.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *  New Device Encoder like 8806+,8806+r
 */
public class T880xPlusEncoder {
    private int encryptType = 0;
    private String aesKey;

    /**
     * Instantiates a new T880xPlusEncoder.
     *
     * @param messageEncryptType The message encrypt type .Use the value of MessageEncryptType.
     * @param aesKey             The aes key.If you do not use AES encryption, the value can be empty.
     */
    public T880xPlusEncoder(int messageEncryptType,String aesKey){
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
        byte[] command = {0x25, 0x25, 0x01};
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
        byte[] command = {0x25, 0x25, 0x03};
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
    public  byte[] getLocationMsgReply(String imei,boolean needSerialNo,int serialNo,int protocolHeadType) throws IOException{
        byte[] command = {0x25, 0x25, (byte)protocolHeadType};
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
    public  byte[] getLocationAlarmMsgReply(String imei,boolean needSerialNo,int serialNo,int sourceAlarmCode,int protocolHeadType) throws IOException {
        byte[] command = {0x25, 0x25, (byte)protocolHeadType};
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
        byte[] command = {0x25, 0x25, 0x05};
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
        byte[] command = {0x25, 0x25, 0x06};
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
        byte[] command = {0x25, 0x25, 0x07};
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
        byte[] command = {0x25, 0x25, (byte)protocolHeadType};
        return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
    }

    /**
     * Get RS232 msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getRS232MsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x25, 0x25, 0x09};
        return Encoder.getRS232MsgReply(imei, serialNo, command, encryptType, aesKey);
    }

    public byte[] getNetworkMsgReply(String imei,int serialNo)throws IOException {
        byte[] command = {0x25, 0x25, 0x11};
        return Encoder.getNetworkMsgReply(imei, serialNo, command, encryptType, aesKey);
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
        byte[] command = {0x25, 0x25, (byte)0x81};
        return Encoder.getConfigSettingMsg(imei, content.trim(), command, encryptType, aesKey);
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
        byte[] command = {0x25, 0x25, (byte)0x81};
        return Encoder.getBrocastSmsMsg(imei, content.trim(), command, encryptType, aesKey);
    }


    /**
     * get forward sms msg byte [].
     * @param imei    the imei
     * @param phoneNumb the phone number need send to
     * @param content the content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getForwardMsg(String imei,String phoneNumb,String content) throws IOException{
        byte[] command = {0x25, 0x25, (byte)0x81};
        return Encoder.getForwardSmsMsg(imei, phoneNumb, content.trim(), command, encryptType, aesKey);
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
        byte[] command = {0x25, 0x25, (byte)0x81};
        return Encoder.getUSSDMsg(imei, content.trim(), command, encryptType, aesKey);
    }



    /**
     * Get RS232 config setting msg byte [ ].
     *
     * @param imei    the imei
     * @param content the config content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public  byte[] getRS232ConfigSettingMsg(String imei,String content) throws IOException {
        byte[] command = {0x25, 0x25, (byte)0x82};
        return Encoder.getConfigSettingMsg(imei, content.trim(), command, encryptType, aesKey);
    }

    public byte[] getRS232ConfigSettingMsg(String imei,byte[] content,int protocolType) throws IOException {
        byte[] command = {0x25, 0x25, (byte)0x82};
        return Encoder.get82ConfigSettingMsg(imei, content, command, protocolType, encryptType, aesKey);
    }

    public  byte[] getWifiMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x25, 0x25, 0x15};
        return Encoder.getWifiMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }

    public  byte[] getRs485MsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x25, 0x25, 0x21};
        return Encoder.getRs485MsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }
    public  byte[] getOneWireMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x25, 0x25, 0x23};
        return Encoder.getOneWireMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }

    public  byte[] getObdMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x25, 0x25, 0x22};
        return Encoder.getObdMsgReply(imei,  serialNo, command, encryptType, aesKey);
    }
}
