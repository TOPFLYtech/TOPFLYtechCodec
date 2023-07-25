package com.topflytech.codec.entities;

/**
 * Created by admin on 2017/11/27.
 */
public class Rs232TireMessage extends Rs232DeviceMessage {

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(double airPressure) {
        this.airPressure = airPressure;
    }

    public int getAirTemp() {
        return airTemp;
    }

    public void setAirTemp(int airTemp) {
        this.airTemp = airTemp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private String sensorId;
    private double voltage = 0;
    private double airPressure = 0;
    private int airTemp = 0;
    private int status = 0;
}
