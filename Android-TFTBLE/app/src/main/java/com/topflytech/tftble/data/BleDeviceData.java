package com.topflytech.tftble.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.topflytech.tftble.R;

import java.util.Arrays;
import java.util.Date;

public class BleDeviceData {
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public byte[] getSrcData() {
        return srcData;
    }

    public void setSrcData(byte[] srcData) {
        this.srcData = Arrays.copyOf(srcData, srcData.length);
    }
    public boolean isNormalDevice() {
        return normalDevice;
    }

    public void setNormalDevice(boolean normalDevice) {
        this.normalDevice = normalDevice;
    }
    private String deviceName;
    private String deviceType;
    private String modelName;
    private String mac;
    private String hardware;
    private String software;
    private String battery;
    private float sourceTemp;
    private String humidity;
    private String deviceProp;
    private byte warn;
    private String id;
    private String rssi;
    private Date date;
    private String hexData;
    private byte[] srcData;
    private boolean normalDevice = true;
    private float tirePressure;
    private byte status;

    public float getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(float tirePressure) {
        this.tirePressure = tirePressure;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getWarn() {
        return warn;
    }

    public void setWarn(byte warn) {
        this.warn = warn;
    }


    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public float getSourceTemp() {
        return sourceTemp;
    }

    public void setSourceTemp(float sourceTemp) {
        this.sourceTemp = sourceTemp;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getDeviceProp() {
        return deviceProp;
    }

    public void setDeviceProp(String deviceProp) {
        this.deviceProp = deviceProp;
    }


    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }


    public String getHexData() {
        return hexData;
    }

    public void setHexData(String hexData) {
        this.hexData = hexData;
    }




    public static String getCurTemp(Context context, float sourceTemp){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            return String.format("%.2f",sourceTemp);
        }else{
            return String.format("%.2f",sourceTemp * 1.8 + 32);
        }
    }
    public static String getSourceTemp(Context context, float curTemp){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            return String.format("%.2f",curTemp);
        }else{
            return String.format("%.2f",((curTemp - 32) / 1.8));
        }
    }

    public static String getCurTempUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            return  "℃";
        }else{
            return "℉";
        }
    }
    public static String getNextTempUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            return  "℉";
        }else{
            return "℃";
        }
    }

    public static void switchCurTempUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
             preferences.edit().putString("tempUnit","1").commit();
        }else{
            preferences.edit().putString("tempUnit","0").commit();
        }
    }
    public static String getCurPressure(Context context, float srcTirePressure){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("pressureUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            return String.format("%.2f",srcTirePressure);
        }else{
            return String.format("%.2f",srcTirePressure / 0.1450377);
        }
    }

    public static String getCurPressureUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pressureUnit = preferences.getString("pressureUnit","0");
        if(pressureUnit != null && pressureUnit.equals("0")){
            return  "Kpa";
        }else{
            return "Psi";
        }
    }
    public static String getNextPressureUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pressureUnit = preferences.getString("pressureUnit","0");
        if(pressureUnit != null && pressureUnit.equals("0")){
            return "Psi";
        }else{
            return  "Kpa";
        }
    }

    public static void switchCurPressureUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pressureUnit = preferences.getString("pressureUnit","0");
        if(pressureUnit != null && pressureUnit.equals("0")){
            preferences.edit().putString("pressureUnit","1").commit();
        }else{
            preferences.edit().putString("pressureUnit","0").commit();
        }
    }


    public static String getWarnDesc(Context context,String deviceType,byte warnByte){
        if(deviceType.equals("S02")){
            String warnStr = "";
            if ((warnByte & 0x01) == 0x01) {
                warnStr += context.getResources().getString(R.string.device_high_temp_warning);
            }
            if ((warnByte & 0x02) == 0x02) {
                warnStr += context.getResources().getString(R.string.device_high_humidity_warning);
            }
            if ((warnByte & 0x10) == 0x10) {
                warnStr += context.getResources().getString(R.string.device_low_temp_warning);
            }
            if ((warnByte & 0x20) == 0x20) {
                warnStr += context.getResources().getString(R.string.device_low_humidity_warning);
            }
            if ((warnByte & 0x40) == 0x40) {
                warnStr += context.getResources().getString(R.string.device_low_voltage_warning);
            }
            if ((warnByte & 0x80) == 0x80) {
                warnStr += context.getResources().getString(R.string.device_light_change_warning);
            }
            return warnStr;
        }else if(deviceType.equals("S04")){
            String warnStr = "";
            if ((warnByte & 0x01) == 0x01) {
                warnStr += context.getResources().getString(R.string.device_high_temp_warning);
            }
            if ((warnByte & 0x10) == 0x10) {
                warnStr += context.getResources().getString(R.string.device_low_temp_warning);
            }
            if ((warnByte & 0x40) == 0x40) {
                warnStr += context.getResources().getString(R.string.device_low_voltage_warning);
            }
            if ((warnByte & 0x80) == 0x80) {
                warnStr += context.getResources().getString(R.string.device_door_status_change_warning);
            }
            return warnStr;
        }else if(deviceType.equals("S05")){
            String warnStr = "";
            if ((warnByte & 0x01) == 0x01) {
                warnStr += context.getResources().getString(R.string.device_high_temp_warning);
            }
            if ((warnByte & 0x10) == 0x10) {
                warnStr += context.getResources().getString(R.string.device_low_temp_warning);
            }
            if ((warnByte & 0x40) == 0x40) {
                warnStr += context.getResources().getString(R.string.device_low_voltage_warning);
            }
            return warnStr;
        }else if(deviceType.equals("tire")){
            if (warnByte == 0x00) {
                return context.getResources().getString(R.string.normal);
            }else if (warnByte == 0x01) {
                return context.getResources().getString(R.string.leak);
            }else if (warnByte == 0x02) {
                return context.getResources().getString(R.string.inflation);
            }else if (warnByte == 0x03) {
                return context.getResources().getString(R.string.startUp);
            }else if (warnByte == 0x04) {
                return context.getResources().getString(R.string.power_on);
            }else if (warnByte == 0x05) {
                return context.getResources().getString(R.string.awaken);
            }
        }
        return "";
    }

    public void parseData(Context context){
        if (deviceType != null && deviceType.equals("tire")){
            byte voltageByte = srcData[11];
            byte pressureByte = srcData[12];
            byte tempByte = srcData[13];
            byte statusByte = srcData[14];
            if ((int)voltageByte < 0){
                battery = String.format("%.2f V",1.22 + ((int)voltageByte + 256) * 0.01);
            }else{
                battery = String.format("%.2f V",1.22 + (int)voltageByte * 0.01)  + "V";
            }
            if ((int)pressureByte < 0){
                tirePressure = 1.572f * 2 *((int)pressureByte + 256);
            }else{
                tirePressure = 1.572f * 2 * (int)pressureByte;
            }
            if((int)tempByte < 0){
                sourceTemp = (int)tempByte + 256 - 55;
            }else{
                sourceTemp = (int)tempByte - 55;
            }
            status = statusByte;
            this.modelName = "TPMS";
        }else{
            byte deviceTypeByte = srcData[5];
            String realData = hexData.substring(28).toLowerCase();
            String invalidContent = "ffffffffff";
            byte hardwareByte = srcData[6];
            this.hardware = MyUtils.parseHardwareVersion(hardwareByte);
            byte softwareByte = srcData[7];
            software = String.valueOf((int)softwareByte);
            id = hexData.substring(16, 28).toUpperCase();
            if(realData.startsWith(invalidContent)){
                deviceType = "errorDevice";
                if(deviceTypeByte == 0x02){
                    this.modelName = "Error TSTH1-B";
                }else if(deviceTypeByte == 0x04){
                    this.modelName = "Error TSDT1-B";
                }else if(deviceTypeByte == 0x05){
                    this.modelName = "Error TSR1-B";
                }
            }else{
                byte batteryByte = srcData[14];
                float realBatteryValue = batteryByte & 0x7f;
                if ((batteryByte & 0x80) == 0x80) {
                    realBatteryValue = realBatteryValue / 100.0f + 2;
                    battery = String.format("%.2f V",realBatteryValue);
                } else {
                    battery = String.format("%.0f%%",realBatteryValue);
                }
                if(deviceTypeByte == 0x02){
                    this.deviceType = "S02";
                    this.modelName = "TSTH1-B";
                    int tempSrc =  MyUtils.bytes2Short(srcData,15);
                    int temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1);
                    sourceTemp = temp /100.0f;
                    byte humidityByte = srcData[17];
                    byte lightByte = srcData[19];
                    byte warnByte = srcData[20];
                    humidity = String.valueOf((int)humidityByte);
                    deviceProp = lightByte == 0x01 ? context.getResources().getString(R.string.prop_light) : context.getResources().getString(R.string.prop_dark);
                    warn = warnByte;

                }else if(deviceTypeByte == 0x04){
                    this.deviceType = "S04";
                    this.modelName = "TSDT1-B";
                    int tempSrc =  MyUtils.bytes2Short(srcData,15);
                    int temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1);
                    sourceTemp = temp /100.0f;
                    byte doorByte = srcData[17];
                    byte warnByte = srcData[18];
                    deviceProp = doorByte == 0x01 ? context.getResources().getString(R.string.prop_door_open) : context.getResources().getString(R.string.prop_door_close);
                    warn = warnByte;

                }else if(deviceTypeByte == 0x05){
                    this.deviceType = "S05";
                    this.modelName = "TSR1-B";
                    int tempSrc =  MyUtils.bytes2Short(srcData,15);
                    int temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1);
                    sourceTemp = temp /100.0f;
                    byte relayByte = srcData[17];
                    byte warnByte = srcData[18];
                    deviceProp = relayByte == 0x01 ? context.getResources().getString(R.string.yes) : context.getResources().getString(R.string.no);
                    warn = warnByte;

                }
            }
        }


    }

}
