package com.topflytech.codec;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PTTEncoder {
    private int encryptType = 0;
    private String aesKey;
    public PTTEncoder(int messageEncryptType,String aesKey){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
    }
    public  byte[] getHeartbeatMsgReply(String imei,int serialNo) throws IOException {
        byte[] command = {0x28, 0x28, 0x03};
        return Encoder.getHeartbeatMsgReply(imei, true, serialNo, command, encryptType, aesKey);
    }

    public byte[] getTalkStartMsgReply(String imei,int serialNo,int status) throws IOException {
        byte[] command = {0x28, 0x28, 0x04};
        byte[] content = {(byte)status};
        return Encoder.getNormalMsgReply(imei,serialNo,command,content,encryptType,aesKey);
    }

    public byte[] getTalkEndMsgReply(String imei,int serialNo,int status) throws IOException {
        byte[] command = {0x28, 0x28, 0x05};
        byte[] content = {(byte)status};
        return Encoder.getNormalMsgReply(imei,serialNo,command,content,encryptType,aesKey);
    }

    public byte[] getVoiceData(String imei ,int serialNo,int encodeType,byte[] voiceData) throws IOException{
        byte[] command = {0x28, 0x28, 0x06};
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write(encodeType);
        contentStream.write(BytesUtils.short2Bytes(voiceData.length));
        contentStream.write(voiceData);
        byte[] content = contentStream.toByteArray();
        return Encoder.getNormalMsgReply(imei,serialNo,command,content,encryptType,aesKey);
    }

    public byte[] getListenStartData(String imei,int serialNo) throws IOException {
        byte[] command = {0x28, 0x28, 0x07};
        return Encoder.getNormalMsgReply(imei,serialNo,command,new byte[]{},encryptType,aesKey);
    }
    public byte[] getListenEndData(String imei,int serialNo) throws IOException {
        byte[] command = {0x28, 0x28, 0x08};
        return Encoder.getNormalMsgReply(imei,serialNo,command,new byte[]{},encryptType,aesKey);
    }
}
