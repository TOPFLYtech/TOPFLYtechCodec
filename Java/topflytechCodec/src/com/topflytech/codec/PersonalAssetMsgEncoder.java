package com.topflytech.codec;
 
import java.io.IOException;

/**
 * Created by admin on 2019/9/10.
 */
public class PersonalAssetMsgEncoder {
    private int encryptType = 0;
    private String aesKey;

    /**
     * Instantiates a new T880xPlusEncoder.
     *
     * @param messageEncryptType The message encrypt type .Use the value of MessageEncryptType.
     * @param aesKey             The aes key.If you do not use AES encryption, the value can be empty.
     */
    public PersonalAssetMsgEncoder(int messageEncryptType, String aesKey){
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
        byte[] command = {0x27, 0x27, 0x01};
        return Encoder.getSignInMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
    }


    public  byte[] getWifiMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x27, 0x27, 0x15};
        return Encoder.getWifiMsgReply(imei, needSerialNo, serialNo, command, encryptType, aesKey);
    }

//    public  byte[] getLockMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
//        byte[] command = {0x27, 0x27, 0x14};
//        return Encoder.getWifiMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
//    }

    public  byte[] getLockMsgReply(String imei,boolean needSerialNo,int serialNo,int protocolHeadType) throws IOException {
        byte[] command = {0x27, 0x27, (byte)protocolHeadType};
        return Encoder.getWifiMsgReply(imei,needSerialNo,serialNo,command,encryptType,aesKey);
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
        byte[] command = {0x27, 0x27, 0x03};
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
        byte[] command = {0x27, 0x27, (byte)protocolHeadType};
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
        byte[] command = {0x27, 0x27, (byte)protocolHeadType};
        return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, encryptType, aesKey);
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
        byte[] command = {0x27, 0x27, (byte)0x81};
        return Encoder.getBrocastSmsMsg(imei, content.trim(), command, encryptType, aesKey);
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
        byte[] command = {0x27, 0x27, (byte)0x81};
        return Encoder.getUSSDMsg(imei, content.trim(), command, encryptType, aesKey);
    }

    public byte[] getNetworkMsgReply(String imei,int serialNo)throws IOException {
        byte[] command = {0x27, 0x27, 0x05};
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
        byte[] command = {0x27, 0x27, (byte)0x81};
        return Encoder.getConfigSettingMsg(imei, content.trim(), command, encryptType, aesKey);
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
        byte[] command = {0x27, 0x27, (byte)protocolHeadType};
        return Encoder.getBluetoothPeripheralDataMsgReply(imei, serialNo, command, encryptType, aesKey);
    }


    public  byte[] getInnerGeoDataMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x27, 0x27, (byte)0x20};
        return Encoder.getNormalMsgReply(imei, serialNo, command,new byte[]{} ,encryptType, aesKey);
    }
    public  byte[] getWifiWithDeviceInfoReply(String imei,int serialNo,int alarmCode,int protocolHeadType) throws IOException {
        byte[] command = {0x27, 0x27, (byte)protocolHeadType};
        byte[] content;
        if(alarmCode != 0){
            content = new byte[]{(byte)alarmCode};
        }else{
            content = new byte[]{};
        }
        return Encoder.getNormalMsgReply(imei, serialNo, command,content ,encryptType, aesKey);
    }

    public  byte[] getDeviceTempCollectionMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x27, 0x27, 0x26};
        byte[] content = new byte[]{};
        return Encoder.getNormalMsgReply(imei, serialNo, command,content ,encryptType, aesKey);
    }

}
