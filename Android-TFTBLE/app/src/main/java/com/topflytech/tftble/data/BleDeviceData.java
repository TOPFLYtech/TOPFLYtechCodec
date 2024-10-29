package com.topflytech.tftble.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.topflytech.tftble.R;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

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

    private int unitAttenuationOfS07 = 58;
    private int unitAttenuationOfS08 = 67;
    private int unitAttenuationOfS09 = 50;
    private int unitAttenuationOfS10 = 58;
    public String getRssi() {
        if(deviceType != null && deviceType.equals("S07")){
            return getDistByRSSI(rssiInt,unitAttenuationOfS07);
        }else if(deviceType != null && deviceType.equals("S08")){
            return getDistByRSSI(rssiInt,unitAttenuationOfS08);
        }else if(deviceType != null && deviceType.equals("S09")){
            return getDistByRSSI(rssiInt,unitAttenuationOfS09);
        }else if(deviceType != null && deviceType.equals("S10")){
            return getDistByRSSI(rssiInt,unitAttenuationOfS10);
        }
        return rssi;
    }
    private String getDistByRSSI(int rssi,int A){
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A) / ( 10 * 2.65);
        return String.format("%ddBm    ≈%.2fm",rssi,Math.pow(10,power));
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

    public int getRssiInt() {
        return rssiInt;
    }

    public void setRssiInt(int rssiInt) {
        this.rssiInt = rssiInt;
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
    private int rssiInt;
    private Date date;
    private String hexData;
    private byte[] srcData;
    private boolean normalDevice = true;
    private float tirePressure;
    private byte status;
    private String batteryPercent;
    private String flag;
    private String doorStatus;
    private String broadcastType;
    private String move;
    private String moveDetection;
    private String stopDetection;
    private String pitchAngle;
    private String rollAngle;
    private String nid;
    private String bid;
    private int moveStatus;
    private String major;
    private String minor;
    private int extSensorType;
    private String input0;
    private String output0;
    private String output1;
    private String analog0;
    private String analog1;
    private String analog2;

    public String getInput0() {
        return input0;
    }

    public void setInput0(String input0) {
        this.input0 = input0;
    }

    public String getOutput0() {
        return output0;
    }

    public void setOutput0(String output0) {
        this.output0 = output0;
    }

    public String getOutput1() {
        return output1;
    }

    public void setOutput1(String output1) {
        this.output1 = output1;
    }

    public String getAnalog0() {
        return analog0;
    }

    public void setAnalog0(String analog0) {
        this.analog0 = analog0;
    }

    public String getAnalog1() {
        return analog1;
    }

    public void setAnalog1(String analog1) {
        this.analog1 = analog1;
    }

    public String getAnalog2() {
        return analog2;
    }

    public void setAnalog2(String analog2) {
        this.analog2 = analog2;
    }

    public int getExtSensorType() {
        return extSensorType;
    }

    public String getExtSensorName(Context context){
        if (extSensorType == 1){
            return context.getResources().getString(R.string.temp_sensor_gx112);
        }else{
            return "";
        }
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    public int getMoveStatus() {
        return moveStatus;
    }

    public void setMoveStatus(int moveStatus) {
        this.moveStatus = moveStatus;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }


    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getMoveDetection() {
        return moveDetection;
    }

    public void setMoveDetection(String moveDetection) {
        this.moveDetection = moveDetection;
    }

    public String getStopDetection() {
        return stopDetection;
    }

    public void setStopDetection(String stopDetection) {
        this.stopDetection = stopDetection;
    }

    public String getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(String pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public String getRollAngle() {
        return rollAngle;
    }

    public void setRollAngle(String rollAngle) {
        this.rollAngle = rollAngle;
    }

    public String getBroadcastType() {
        return broadcastType;
    }

    public void setBroadcastType(String broadcastType) {
        this.broadcastType = broadcastType;
    }

    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }

    public String getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(String batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

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
        if(sourceTemp == -999){
            return "-";
        }
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
        if(deviceType.equals("S02") ){
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
        }else if(deviceType.equals("S07")){
            String warnStr = "";
            if ((warnByte & 0x40) == 0x40) {
                warnStr += context.getResources().getString(R.string.device_low_voltage_warning);
            }
            return warnStr;
        }else if(deviceType.equals("S08")){
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
                warnStr += context.getResources().getString(R.string.malfunction);
            }
            return warnStr;
        }else if(deviceType.equals("S10")){
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
                warnStr += context.getResources().getString(R.string.malfunction);
            }

            return warnStr;
        }
        return "";
    }

    private HashMap<String ,byte[]> parseRawData(byte[] rawData){
        HashMap<String ,byte[]> result = new HashMap<>();
        int index = 0;
        while (index < rawData.length){
            int len = rawData[index];
            if (index + len + 1 <= rawData.length && len > 2){
                byte type = rawData[index + 1];
                byte[] itemData = Arrays.copyOfRange(rawData,index+2,index + len  + 1);
                result.put(String.valueOf(type & 0xff),itemData);
            }
            index += len + 1;
        }
        return result;
    }

    public void parseS0789Data(Context context){
        HashMap<String ,byte[]> items = parseRawData(srcData);

        if(items.containsKey("22")){
            byte[] data = items.get("22");
            String hexData = MyUtils.bytes2HexString(data, 0);
            String head = hexData.substring(0,4);
            if (!(head.toLowerCase().equals("aafe") || head.toLowerCase().equals("0708") || head.toLowerCase().equals("3561"))){
                deviceType = "errorDevice";
                return;
            }
            if(data.length == 20 && data[2] == 0x00){
                this.nid = hexData.substring(8,28);
                this.bid = hexData.substring(28,40);
            }else{
                if(data[2] == 0x07){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-button";
                    this.deviceType = "S07";
                    this.broadcastType = "Beacon";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                    if(data.length < 19){

                    }else{
                        if(data[13] == 0x01){
                            this.broadcastType = "Long range";
                        } else{
                            this.broadcastType = "Eddystone T-button";
                        }
                        int batteryVoltage = MyUtils.bytes2Short(data,14);
                        battery = String.format("%.2f V",batteryVoltage / 1000.0f);
                        batteryPercent = (data[16] & 0xff) + "%";
                        byte warnByte = data[17];
                        warn = warnByte;
                        if(data[18] == 0x01){
                            flag = "SOS";
                        }else if(data[18] == 0x02){
                            flag = context.getResources().getString(R.string.identification);
                        }else{
                            flag = "";
                        }
                    }


                }else if(data[2] == 0x08){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-sense";
                    this.deviceType = "S08";
                    this.broadcastType = "Beacon";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.doorStatus = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                    if(data.length < 23){

                    }else{
                        Boolean isGensor = false;
                        if((data[13] & 0x01) == 0x01){
                            this.broadcastType = "Long range";
                        }else  {
                            this.broadcastType = "Eddystone T-sense";
                        }
                        if((data[13] & 0x02) == 0x02){
                            isGensor = true;
                        }else if((data[13] & 0x04) == 0x04){
                            //"One wire";
                        }
                        int batteryVoltage = MyUtils.bytes2Short(data,14);
                        battery = String.format("%.2f V",batteryVoltage / 1000.0f);
                        batteryPercent = (data[16] & 0xff) + "%";
                        int tempSrc =  MyUtils.bytes2Short(data,17);
                        int temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1);
                        sourceTemp = temp /100.0f;
                        if(data[20] == 0x00){
                            deviceProp = context.getResources().getString(R.string.normal);
                        }else{
                            deviceProp = context.getResources().getString(R.string.strong_light);
                        }
                        byte humidityByte = data[19];
                        byte doorByte = data[21];
                        if(doorByte == 0x02){
                            doorStatus = context.getResources().getString(R.string.disable);
                        }else{
                            doorStatus = doorByte == 0x01 ? context.getResources().getString(R.string.prop_door_open) : context.getResources().getString(R.string.prop_door_close);
                        }
                        if(isGensor){
                            this.moveStatus = (data[22] & 0xff) >> 6;
                            if(moveStatus == 0){
                                this.move = context.getResources().getString(R.string.move_static);
                            }else {
                                this.move = context.getResources().getString(R.string.move_move);
                            }
                            int moveInt = MyUtils.bytes2Short(data,22);
                            int stopInt  = MyUtils.bytes2Short(data,23);
                            this.moveDetection = String.valueOf((moveInt & 0x3FF8) >> 3);
                            this.stopDetection = String.valueOf(stopInt & 0x7ff);

                            this.pitchAngle =  String.valueOf((int)data[25]);
                            this.rollAngle = String.valueOf(MyUtils.byte2SignShort(data,26));
                            byte warnByte = data[28];
                            warn = warnByte;
                        }else{
                            byte warnByte = data[22];
                            warn = warnByte;
                        }
                    }


                }else if(data[2] == 0x09){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-hub";
                    this.deviceType = "S09";
                    this.input0 = "-";
                    this.output0 = "-";
                    this.output1 = "-";
                    this.analog0 = "-";
                    this.analog1 = "-";
                    this.analog2 = "-";
                    if(data.length >= 20){
                        if(data[13] != 0xff){
                            this.input0 = (data[13] & 0x80) == 0x80 ? context.getString(R.string.active) : context.getString(R.string.inactive);
                            this.output0 = (data[13] & 0x40) == 0x40 ? context.getString(R.string.active) : context.getString(R.string.inactive);
                            this.output1 = (data[13] & 0x20) == 0x20 ? context.getString(R.string.active) : context.getString(R.string.inactive);
                        }
                        if(!(data[14] == -1 && data[15] == -1)){
                            this.analog0 = String.format("%d.%dV",Integer.valueOf(hexData.substring(28,30)),Integer.valueOf(hexData.substring(30,32)));
                        }
                        if(!(data[16] == -1 && data[17] == -1)){
                            this.analog1 = String.format("%d.%dV",Integer.valueOf(hexData.substring(32,34)),Integer.valueOf(hexData.substring(34,36)));
                        }
                        if(!(data[18] == -1 && data[19] == -1)){
                            this.analog2 = String.format("%d.%dV",Integer.valueOf(hexData.substring(36,38)),Integer.valueOf(hexData.substring(38,40)));
                        }
                    }
                }else if(data[2] == 0x0a){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-one";
                    this.deviceType = "S10";
                    this.broadcastType = "Beacon";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.doorStatus = "-";
                    this.humidity = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                    this.extSensorType = 0;
                    if(data.length < 19){

                    }else{
                        Boolean isGensor = false;
                        if((data[13] & 0x01) == 0x01){
                            this.broadcastType = "Long range";
                        }else  {
                            this.broadcastType = "Eddystone T-one";
                        }
                        if((data[13] & 0x02) == 0x02){
                            isGensor = true;
                        }else if((data[13] & 0x04) == 0x04){
                            //"One wire";
                        }
                        int batteryVoltage = MyUtils.bytes2Short(data,14);
                        battery = String.format("%.2f V",batteryVoltage / 1000.0f);
                        batteryPercent = (data[16] & 0xff) + "%";
                        byte warnByte = data[17];
                        warn = warnByte;
                        byte lightByte = data[18];
                        byte extDeviceByte = data[19];
                        this.extSensorType = extDeviceByte >> 4;
                        int len = extDeviceByte & 0xf;
                        if(this.extSensorType == 1){
                            int tempSrc =  MyUtils.bytes2Short(data,20);
                            if(tempSrc == 65535){
                                sourceTemp = -999;
                            }else{
                                int temp = (tempSrc & 0x7fff) * ((tempSrc & 0x8000) == 0x8000 ? -1 : 1);
                                sourceTemp = temp /100.0f;
                            }
                        }
                    }

                }else if(data[2] ==  (byte)0xa1){
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "A001";
                    this.deviceType = "A001";
                }else if(data[2] ==  (byte)0xa2){
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "A001";
                    this.deviceType = "A002";
                }else{
                    deviceType = "errorDevice";
                }
            }

        }
        if(items.containsKey("255") == true){
            byte[] data = items.get("255");
            if(data.length == 13){
                String hexData = MyUtils.bytes2HexString(data, 0);
                String head = hexData.substring(0,4);
                if(data[2] == 0x07){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-button";
                    this.deviceType = "S07";
                    this.broadcastType = "Eddystone UID";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                }else if(data[2] == 0x08){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-sense";
                    this.deviceType = "S08";
                    this.broadcastType = "Eddystone UID";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.doorStatus = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                }else if(data[2] == 0x09){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-hub";
                    this.deviceType = "S09";
                }else if(data[2] == 0x0a){
                    if(data.length < 13){
                        return;
                    }
                    this.hardware = MyUtils.parseHardwareVersion(data[3]);
                    this.software = MyUtils.parseSoftwareVersion(data,4);
                    id = hexData.substring(14,26).toUpperCase();
                    this.modelName = "T-one";
                    this.deviceType = "S10";
                    this.broadcastType = "Eddystone UID";
                    this.flag = "-";
                    this.battery = "-";
                    this.batteryPercent = "-";
                    this.doorStatus = "-";
                    this.humidity = "-";
                    this.move = "-";
                    this.moveDetection = "-";
                    this.stopDetection = "-";
                    this.pitchAngle = "-";
                    this.rollAngle = "-";
                    this.deviceProp = "-";
                    this.major = "-";
                    this.minor = "-";
                }else{
                    deviceType = "errorDevice";
                }
            }else if(data.length == 25){
                this.major = String.valueOf(MyUtils.bytes2Short(data,20));
                this.minor = String.valueOf(MyUtils.bytes2Short(data,22));
            }
        }
        if(deviceType == null){
            deviceType = "errorDevice";
        }
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
                    byte lightStatusByte = srcData[18];
                    byte warnByte = srcData[20];
                    humidity = String.valueOf((int)humidityByte);
                    deviceProp = lightByte == 0x01 ? context.getResources().getString(R.string.prop_light) : context.getResources().getString(R.string.prop_dark);

                    if((lightStatusByte & 0x80) == 0x80){
                        deviceProp = context.getResources().getString(R.string.lightSenseDisable);
                    }
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
