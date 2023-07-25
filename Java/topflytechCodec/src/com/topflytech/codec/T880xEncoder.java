package com.topflytech.codec;
import com.topflytech.codec.entities.MessageEncryptType;

import java.io.IOException;

/**
 * Old Device Encoder like 8806,8803PRO
 */
public class T880xEncoder {
    /**
     * Get sign in msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getSignInMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x23, 0x23, 0x01};
        return Encoder.getSignInMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE,null);
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
    public static byte[] getHeartbeatMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException {
        byte[] command = {0x23, 0x23, 0x03};
        return Encoder.getHeartbeatMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE,null);
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
    public static byte[] getLocationMsgReply(String imei,boolean needSerialNo,int serialNo) throws IOException{
        byte[] command = {0x23, 0x23, 0x02};
        return Encoder.getLocationMsgReply(imei, needSerialNo, serialNo, command, MessageEncryptType.NONE,null);
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
    public static byte[] getLocationAlarmMsgReply(String imei,boolean needSerialNo,int serialNo,int sourceAlarmCode) throws IOException {
        byte[] command = {0x23, 0x23, 0x04};
        return Encoder.getLocationAlarmMsgReply(imei, needSerialNo, serialNo, sourceAlarmCode, command, MessageEncryptType.NONE,null);
    }


    /**
     * Get config setting msg byte [ ].
     *
     * @param imei    the imei
     * @param content the config content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getConfigSettingMsg(String imei,String content) throws IOException {
        byte[] command = {0x23, 0x23, (byte)0x81};
        return Encoder.getConfigSettingMsg(imei, content.trim(), command, MessageEncryptType.NONE,null);
    }

    /**
     * Get brocast sms msg byte [ ].
     *
     * @param imei    the imei
     * @param content the content
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getBrocastSmsMsg(String imei,String content) throws IOException {
        byte[] command = {0x23, 0x23, (byte)0x81};
        return Encoder.getBrocastSmsMsg(imei, content.trim(), command, MessageEncryptType.NONE,null);
    }

}
