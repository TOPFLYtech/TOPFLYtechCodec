package com.topflytech.lockActive.data;

public class BleRespData {
    private int type;//0:read 1:write
    public static final int READ_TYPE  = 0;
    public static final int WRITE_TYPE  = 1;
    public static final int ERROR_TYPE = 2;
    private int index;
    private int controlCode;
    private byte[] data;
    private boolean isEnd;
    public boolean isEnd() {
        return isEnd;
    }
    private int errorCode;

    public final static int ERROR_CODE_OF_PWD_ERROR = 1;
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }


    public int getControlCode() {
        return controlCode;
    }

    public void setControlCode(int controlCode) {
        this.controlCode = controlCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
