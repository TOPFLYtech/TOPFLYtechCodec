package com.topflytech.codec.entities.PTT;
 
import com.topflytech.codec.entities.Message;

public class VoiceMessage  extends Message {
    private int encodeType;
    private byte[] voiceData;

    public int getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(int encodeType) {
        this.encodeType = encodeType;
    }

    public byte[] getVoiceData() {
        return voiceData;
    }

    public void setVoiceData(byte[] voiceData) {
        this.voiceData = voiceData;
    }
}
