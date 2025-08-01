package com.topflytech.codec.entities;


/**
 * Message is the base class for all decoded messages
 */
public abstract class Message {
    /**
     * Gets imei.
     *
     * @return the imei
     */
    public String getImei() {
        return imei;
    }

    /**
     * Sets imei.
     *
     * @param imei the imei
     */
    public void setImei(String imei) {
        this.imei = imei;
    }

    /**
     * Gets serial number of the message,The serial number is counted on the device
     *
     * @return the serial no
     */
    public int getSerialNo() {
        return serialNo;
    }

    /**
     * Sets serial number.
     *
     * @param serialNo the serial no
     */
    public void setSerialNo(int serialNo) {
        this.serialNo = serialNo;
    }

    /**
     * Get original bytes byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getOrignBytes() {
        return orignBytes;
    }

    /**
     * Sets original bytes.
     *
     * @param orignBytes the orign bytes
     */
    public void setOrignBytes(byte[] orignBytes) {
        this.orignBytes = orignBytes;
    }
    public boolean isNeedResp() {
        return isNeedResp;
    }

    public void setIsNeedResp(boolean isNeedResp) {
        this.isNeedResp = isNeedResp;
    }
    public int getProtocolHeadType() {
        return protocolHeadType;
    }

    public void setProtocolHeadType(int protocolHeadType) {
        this.protocolHeadType = protocolHeadType;
    }

    public int getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    private String imei;
    private int serialNo;
    private byte[] orignBytes;
    private boolean isNeedResp = true;
    private int protocolHeadType;
    private int encryptType = MessageEncryptType.NONE;
}
