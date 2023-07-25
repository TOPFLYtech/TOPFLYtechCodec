package com.topflytech.codec.entities;

/**
 * Created by admin on 2017/11/27.
 */
public class Rs232FuelMessage extends Rs232DeviceMessage {


    public void setFuelPercent(Float fuelPercent) {
        this.fuelPercent = fuelPercent;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public Integer getLiquidType() {
        return liquidType;
    }

    public void setLiquidType(Integer liquidType) {
        this.liquidType = liquidType;
    }

    public Float getCurLiquidHeight() {
        return curLiquidHeight;
    }

    public void setCurLiquidHeight(Float curLiquidHeight) {
        this.curLiquidHeight = curLiquidHeight;
    }

    public Float getFullLiquidHeight() {
        return fullLiquidHeight;
    }

    public void setFullLiquidHeight(Float fullLiquidHeight) {
        this.fullLiquidHeight = fullLiquidHeight;
    }

    public Float getFuelPercent() {
        return fuelPercent;
    }
    public int getAlarm() {
        return alarm;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }
    private Float fuelPercent;
    private Float temp;
    private Integer liquidType;
    private Float curLiquidHeight;
    private Float fullLiquidHeight;



    private int alarm = 0;
    public final static int LIQUID_TYPE_OF_DIESEL = 1;
    public final static int LIQUID_TYPE_OF_PETROL = 2;
    public final static int LIQUID_TYPE_OF_WATER = 3;

    public final static int ALARM_NONE = 0;
    public final static int ALARM_FILL_TANK = 1;
    public final static int ALARM_FUEL_LEAK = 2;
}
