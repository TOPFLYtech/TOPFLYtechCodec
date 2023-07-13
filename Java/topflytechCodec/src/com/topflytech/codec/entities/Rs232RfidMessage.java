package com.topflytech.codec.entities;
 
/**
 * Created by admin on 2017/11/27.
 */
public class Rs232RfidMessage extends Rs232DeviceMessage {
    private String rfid;

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }
}
