package com.topflytech.codec;
 
import com.topflytech.codec.entities.*;
import com.topflytech.codec.entities.PTT.TalkEndMessage;
import com.topflytech.codec.entities.PTT.TalkStartMessage;
import com.topflytech.codec.entities.PTT.VoiceMessage;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PTTDecoder {
    private static final int HEADER_LENGTH = 3;
    private static final byte[] HEARTBEAT =  {0x28, 0x28, 0x03};
    private static final byte[] TALK_START =  {0x28, 0x28, 0x04};
    private static final byte[] TALK_END =  {0x28, 0x28, 0x05};
    private static final byte[] VOICE_DATA =  {0x28, 0x28, 0x06};
    private int encryptType = 0;
    private String aesKey;

    public PTTDecoder(int messageEncryptType,String aesKey){
        assert messageEncryptType < 0 || messageEncryptType >= 3 : "Message encrypt type error!";
        this.encryptType = messageEncryptType;
        this.aesKey = aesKey;
    }

    private static boolean match(byte[] bytes) {
        assert bytes.length >= HEADER_LENGTH : "command match: length is not 3!";
        return Arrays.equals(TALK_START, bytes)
                || Arrays.equals(HEARTBEAT, bytes)
                || Arrays.equals(TALK_END, bytes)
                || Arrays.equals(VOICE_DATA, bytes)
              ;
    }
    public void decode(byte[] buf, Callback callback) {
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
        if (bytes != null && bytes.length > HEADER_LENGTH ) {
            switch (bytes[2]) {
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    return heartbeatMessage;
                case 0x04:
                    TalkStartMessage talkStartMessage = parseTalkStartMessage(bytes);
                    return talkStartMessage;
                case 0x05:
                    TalkEndMessage talkEndMessage = parseTalkEndMessage(bytes);
                    return talkEndMessage;
                case 0x06:
                    VoiceMessage voiceMessage = parseVoiceMessage(bytes);
                    return voiceMessage;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }
        return null;
    }
    public void build(byte[] bytes, Callback callback) throws ParseException {
        if (bytes != null && bytes.length > HEADER_LENGTH ) {
            switch (bytes[2]) {
                case 0x03:
                    HeartbeatMessage heartbeatMessage = parseHeartbeat(bytes);
                    callback.receiveHeartbeatMessage(heartbeatMessage);
                    break;
                case 0x04:
                    TalkStartMessage talkStartMessage = parseTalkStartMessage(bytes);
                    callback.receiveTalkStartMessage(talkStartMessage);
                    break;
                case 0x05:
                    TalkEndMessage talkEndMessage = parseTalkEndMessage(bytes);
                    callback.receiveTalkEndMessage(talkEndMessage);
                    break;
                case 0x06:
                    VoiceMessage voiceMessage = parseVoiceMessage(bytes);
                    callback.receiveVoiceMessage(voiceMessage);
                    break;
                default:
                    throw new ParseException("The message type error!",0);
            }
        }

    }
    private VoiceMessage parseVoiceMessage(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.setOrignBytes(bytes);
        voiceMessage.setSerialNo(serialNo);
        voiceMessage.setImei(imei);
        voiceMessage.setEncodeType(bytes[15]);
        int voiceLen = BytesUtils.bytes2Short(bytes,16);
        if(bytes.length >= 18 + voiceLen){
            byte[] voiceData = Arrays.copyOfRange(bytes,18,18 + voiceLen);
            voiceMessage.setVoiceData(voiceData);
        }
        return voiceMessage;
    }

    private TalkEndMessage parseTalkEndMessage(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        TalkEndMessage talkEndMessage = new TalkEndMessage();
        talkEndMessage.setOrignBytes(bytes);
        talkEndMessage.setSerialNo(serialNo);
        talkEndMessage.setImei(imei);
        return talkEndMessage;
    }

    private TalkStartMessage parseTalkStartMessage(byte[] bytes) {
        int serialNo = BytesUtils.bytes2Short(bytes, 5);
        String imei = BytesUtils.IMEI.decode(bytes, 7);
        TalkStartMessage talkStartMessage = new TalkStartMessage();
        talkStartMessage.setOrignBytes(bytes);
        talkStartMessage.setSerialNo(serialNo);
        talkStartMessage.setImei(imei);
        return talkStartMessage;
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


    public interface Callback{
        void receiveHeartbeatMessage(HeartbeatMessage heartbeatMessage);
        void receiveVoiceMessage(VoiceMessage voiceMessage);
        void receiveTalkStartMessage(TalkStartMessage talkStartMessage);
        void receiveTalkEndMessage(TalkEndMessage talkEndMessage);
        void receiveErrorMessage(String msg);
    }

}
