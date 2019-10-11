package com.topflytech.codec.entities;

/**
 * Created by admin on 2017/10/9.
 */
public class BleTireData extends BleData{
    /**
     * Gets bluetooth mac.
     *
     * @return the mac
     */
    public String getMac() {
        return mac;
    }

    /**
     * Sets mac.
     *
     * @param mac the mac
     */
    public void setMac(String mac) {
        this.mac = mac;
    }

    /**
     * Gets tire pressure sensor inner battery voltage.
     *
     * @return the voltage
     */
    public double getVoltage() {
        return voltage;
    }

    /**
     * Sets voltage.
     *
     * @param voltage the voltage
     */
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    /**
     * Gets air pressure.The unit is kPa.
     *
     * @return the air pressure
     */
    public double getAirPressure() {
        return airPressure;
    }

    /**
     * Sets air pressure.
     *
     * @param airPressure the air pressure
     */
    public void setAirPressure(double airPressure) {
        this.airPressure = airPressure;
    }

    /**
     * Gets air temp.The unit is degrees Celsius.
     *
     * @return the air temp
     */
    public int getAirTemp() {
        return airTemp;
    }

    /**
     * Sets air temp.
     *
     * @param airTemp the air temp
     */
    public void setAirTemp(int airTemp) {
        this.airTemp = airTemp;
    }

    private String mac;
    private double voltage = 0;
    private double airPressure = 0;
    private int airTemp = 0;
    private int status = 0;
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
