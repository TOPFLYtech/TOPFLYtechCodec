package com.topflytech.codec.entities;
 
import java.util.Date;
import java.util.List;

/**
 * The type RS232 message.Protocol number is 25 25 09.
 */
public class RS232Message extends Message{
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

    /**
     * Is vehicle ignition boolean.
     *
     * @return the boolean
     */
    public boolean isIgnition() {
        return isIgnition;
    }

    /**
     * Sets ignition.
     *
     * @param ignition the ignition
     */
    public void setIgnition(boolean ignition) {
        this.isIgnition = ignition;
    }

    private Date date;
    private boolean isIgnition;


    public List<Rs232DeviceMessage> getRs232DeviceMessageList() {
        return rs232DeviceMessageList;
    }

    public void setRs232DeviceMessageList(List<Rs232DeviceMessage> rs232DeviceMessageList) {
        this.rs232DeviceMessageList = rs232DeviceMessageList;
    }

    public int getRs232DataType() {
        return rs232DataType;
    }

    public void setRs232DataType(int rs232DataType) {
        this.rs232DataType = rs232DataType;
    }

    private int rs232DataType;
    private List<Rs232DeviceMessage> rs232DeviceMessageList;

    public final static int OTHER_DEVICE_DATA = 0;
    public final static int TIRE_DATA = 1;
    public final static int RDID_DATA = 3;
    public final static int FINGERPRINT_DATA = 2;
    public final static int CAPACITOR_FUEL_DATA = 4;
    public final static int ULTRASONIC_FUEL_DATA = 5;

}
