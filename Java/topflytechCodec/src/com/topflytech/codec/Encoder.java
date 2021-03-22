package com.topflytech.codec;

import com.topflytech.codec.entities.MessageEncryptType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


/**
 * The type Encoder.
 */
public class Encoder {
    /**
     * Get sign in msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getSignInMsgReply(String imei, boolean needSerialNo, int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei,needSerialNo,serialNo, command, content.getBytes(),0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }


    public static byte[] getWifiMsgReply(String imei, boolean needSerialNo, int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei,needSerialNo,serialNo, command, content.getBytes(),0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get heartbeat msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getHeartbeatMsgReply(String imei,boolean needSerialNo,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, needSerialNo, serialNo, command, content.getBytes(),0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get location msg reply byte [ ].
     *
     * @param imei         the imei
     * @param needSerialNo the need serial no
     * @param serialNo     the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getLocationMsgReply(String imei,boolean needSerialNo,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException{
        String content = "";
        byte[] data = encode(imei, needSerialNo, serialNo, command, content.getBytes(),0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get location alarm msg reply byte [ ].
     *
     * @param imei            the imei
     * @param needSerialNo    the need serial no
     * @param serialNo        the serial no
     * @param sourceAlarmCode the source alarm code
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getLocationAlarmMsgReply(String imei,boolean needSerialNo,int serialNo,int sourceAlarmCode,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] data = encode(imei, needSerialNo, serialNo, command, new byte[]{(byte)sourceAlarmCode}, 0x10);
        return encrypt(data,messageEncryptType,aesKey);
    }


    /**
     * Get gps driver behavoir msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getGpsDriverBehavoirMsgReply(String imei,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get acceleration driver behavior msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getAccelerationDriverBehaviorMsgReply(String imei, int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get acceleration alarm msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getAccelerationAlarmMsgReply(String imei,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }



    /**
     * Get bluetooth peripheral data msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getBluetoothPeripheralDataMsgReply(String imei, int serialNo, byte[] command, int messageEncryptType, String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get RS232 msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getRS232MsgReply(String imei,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }
    /**
     * Get Obd msg reply byte [ ].
     *
     * @param imei     the imei
     * @param serialNo the serial no
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getObdMsgReply(String imei,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get config setting msg byte [ ].
     *
     * @param imei    the imei
     * @param content the config content
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getConfigSettingMsg(String imei,String content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] data = encode(imei, false, 1, command, (byte) 1, content.getBytes());
        return encrypt(data,messageEncryptType,aesKey);
    }

    public static byte[] getConfigSettingMsg(String imei,byte[] content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] data = encode(imei, false, 1, command, (byte) 1, content);
        return encrypt(data,messageEncryptType,aesKey);
    }
    public static byte[] getObdConfigSettingMsg(String imei,byte[] content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        int length = 15 + content.length;
        byte[] data = encode(imei, false, 1, command,  content,length);
        return encrypt(data,messageEncryptType,aesKey);
    }

    public static byte[] get82ConfigSettingMsg(String imei,byte[] content,byte[] command,int protocolType,int messageEncryptType,String aesKey) throws IOException {
        int length = 16 + content.length;
        byte[] data = encode(imei, false, 1, command, (byte) protocolType, content,length);
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get brocast sms msg byte [ ].
     *
     * @param imei    the imei
     * @param content the content,also you can use sms command
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getBrocastSmsMsg(String imei,String content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] data = encode(imei, false, 1, command, (byte) 2, content.getBytes("UTF-16LE"));
        return encrypt(data,messageEncryptType,aesKey);
    }

    /**
     * Get forward sms msg byte [ ].
     *
     * @param imei    the imei
     * @param number  the forward msg number
     * @param content the content,also you can use sms command
     * @param command     the command
     * @param messageEncryptType message encrypt type
     * @param aesKey    the aes key
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] getForwardSmsMsg(String imei,String number,String content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] numberBytes = number.getBytes("UTF-16LE");
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write(numberBytes);
        for (int i = numberBytes.length / 2 - 1;i < 20;i++){
            contentStream.write(new byte[]{0x00, 0x00});
        }
        contentStream.write(content.getBytes("UTF-16LE"));
        byte[] data = encode(imei, false, 1, command, (byte) 3, contentStream.toByteArray());
        return encrypt(data,messageEncryptType,aesKey);
    }

    public static byte[] getUSSDMsg(String imei,String content,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        byte[] data = encode(imei, false, 1, command, (byte) 5, content.getBytes());
        return encrypt(data,messageEncryptType,aesKey);
    }

    public static byte[] getNetworkMsgReply(String imei,int serialNo,byte[] command,int messageEncryptType,String aesKey) throws IOException {
        String content = "";
        byte[] data = encode(imei, true, serialNo, command, content.getBytes(), 0x0F);
        return encrypt(data,messageEncryptType,aesKey);
    }

    private static byte[] encode(String imei, boolean useSerialNo, Integer serialNo, byte[] command, byte protocol, byte[] content,int length) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(command);
            outputStream.write(new byte[]{0x00,  (byte)length});
            if (useSerialNo) {
                outputStream.write(BytesUtils.short2Bytes(serialNo));
            } else {
                outputStream.write(new byte[]{0x00, 0x01});
            }
            outputStream.write(encodeImei(imei));
            outputStream.write(protocol);
            outputStream.write(content, 0, content.length);
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }

    private static byte[] encode(String imei, boolean useSerialNo, Integer serialNo, byte[] command, byte protocol, byte[] content) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(command);
            int packageSize = Math.min(0x10, Short.MAX_VALUE);
            outputStream.write(new byte[]{0x00, (byte) packageSize});
            if (useSerialNo) {
                outputStream.write(BytesUtils.short2Bytes(serialNo));
            } else {
                outputStream.write(new byte[]{0x00, 0x01});
            }
            outputStream.write(encodeImei(imei));
            outputStream.write(protocol);
            outputStream.write(content, 0, content.length);
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }


    private static byte[] encodeImei(String imei) {
        assert imei != null && 15 == imei.length() : "invalid imei length!";
        return BytesUtils.hexString2Bytes("0" + imei);
    }

    private static byte[] encode(String imei, boolean useSerialNo, Integer serialNo, byte[] command, byte[] content, int length) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(command);
            outputStream.write(new byte[]{0x00,  (byte)length});
            if (useSerialNo) {
                outputStream.write(BytesUtils.short2Bytes(serialNo));
            } else {
                outputStream.write(new byte[]{0x00, 0x01});
            }
            outputStream.write(encodeImei(imei));
            outputStream.write(content, 0, content.length);
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }


    private static byte[] encrypt(byte[] data,int messageEncryptType,String aesKey) throws IOException {
        if (messageEncryptType == MessageEncryptType.MD5){
            byte[] md5 = Crypto.MD5(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(data);
                outputStream.write(md5);
                return outputStream.toByteArray();
            } finally {
                outputStream.close();
            }
        }else if(messageEncryptType == MessageEncryptType.AES){
            if (aesKey == null){
                return null;
            }
            byte[] head = Arrays.copyOfRange(data,0,15);
            byte[] realData = Arrays.copyOfRange(data,15,data.length);
            if (realData == null || realData.length == 0){
                return data;
            }
            byte[] aesData = null;
            try {
                aesData = Crypto.aesEncrypt(realData,aesKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (aesData == null){
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(head);
                outputStream.write(aesData);
                return outputStream.toByteArray();
            } finally {
                outputStream.close();
            }
        }else{
            return data;
        }
    }
}
