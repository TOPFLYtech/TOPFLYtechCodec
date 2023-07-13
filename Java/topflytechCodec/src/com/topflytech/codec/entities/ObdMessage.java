package com.topflytech.codec.entities;
 
import java.util.Date;

/**
 * Created by admin on 2017/10/20.
 */
public class ObdMessage extends Message{
    /**
     * Gets date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }



    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorData() {
        return errorData;
    }

    public void setErrorData(String errorData) {
        this.errorData = errorData;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public boolean isClearErrorCodeSuccess() {
        return clearErrorCodeSuccess;
    }

    public void setClearErrorCodeSuccess(boolean clearErrorCodeSuccess) {
        this.clearErrorCodeSuccess = clearErrorCodeSuccess;
    }
    private String errorCode;
    private String errorData ;
    private Date date;


    private int messageType;
    private String vin;
    private boolean clearErrorCodeSuccess = false;

    public static int ERROR_CODE_MESSAGE = 0;
    public static int VIN_MESSAGE = 1;
    public static int CLEAR_ERROR_CODE_MESSAGE = 2;


}
