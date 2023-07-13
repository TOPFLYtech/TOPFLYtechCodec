package com.topflytech.codec.entities;
 
/**
 * Created by admin on 2017/11/27.
 */
public class Rs232DeviceMessage {
    /**
     * Get RS232 data byte [ ].The data needs to be resolved according to the protocol of the RS232 device
     *
     * @return the byte [ ]
     */
    public byte[] getRS232Data() {
        return RS232Data;
    }

    /**
     * Sets RS232 data.
     *
     * @param RS232Data the rs 232 data
     */
    public void setRS232Data(byte[] RS232Data) {
        this.RS232Data = RS232Data;
    }
    private byte[] RS232Data;
}
