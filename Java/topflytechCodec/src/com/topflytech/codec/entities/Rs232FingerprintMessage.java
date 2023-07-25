package com.topflytech.codec.entities;

/**
 * Created by admin on 2017/12/15.
 */
public class Rs232FingerprintMessage extends Rs232DeviceMessage{
    private int fingerprintId;
    private String data;
    private int fingerprintType;
    private int status;
    private int fingerprintDataIndex;
    private int remarkId;
    public static int FINGERPRINT_TYPE_OF_NONE = 0;
    public static int FINGERPRINT_TYPE_OF_CLOUND_REGISTER = 1;
    public static int FINGERPRINT_TYPE_OF_PATCH = 2;
    public static int FINGERPRINT_TYPE_OF_DELETE = 3;
    public static int FINGERPRINT_TYPE_GET_TEMPLATE = 4;
    public static int FINGERPRINT_TYPE_WRITE_TEMPLATE = 5;
    public static int FINGERPRINT_TYPE_OF_ALL_CLEAR = 6;
    public static int FINGERPRINT_TYPE_OF_SET_PERMISSION = 7;
    public static int FINGERPRINT_TYPE_OF_GET_PERMISSION = 8;
    public static int FINGERPRINT_TYPE_OF_GET_EMPTY_ID = 9;
    public static int FINGERPRINT_TYPE_OF_SET_PATCH_PERMISSION = 10;
    public static int FINGERPRINT_TYPE_OF_SET_DEVICE_ID = 11;
    public static int FINGERPRINT_TYPE_OF_REGISTER = 11;
    public static int FINGERPRITN_MSG_STATUS_SUCC = 0;
    public static int FINGERPRINT_MSG_STATUS_ERROR = 1;


    public int getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(int remarkId) {
        this.remarkId = remarkId;
    }


    public int getFingerprintDataIndex() {
        return fingerprintDataIndex;
    }

    public void setFingerprintDataIndex(int fingerprintDataIndex) {
        this.fingerprintDataIndex = fingerprintDataIndex;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFingerprintType() {
        return fingerprintType;
    }

    public void setFingerprintType(int fingerprintType) {
        this.fingerprintType = fingerprintType;
    }


    public int getFingerprintId() {
        return fingerprintId;
    }

    public void setFingerprintId(int fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
