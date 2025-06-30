package com.topflytech.lockActive.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.topflytech.lockActive.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class BleDeviceData {

    public static UUID unlockServiceId = UUID.fromString("27760001-999c-4d6a-9fc4-c7272be10900");
    public static UUID unlockWriteUUID = UUID.fromString("27760003-999c-4d6a-9fc4-c7272be10900");
    public static UUID unlockNotifyUUID = UUID.fromString("27760003-999c-4d6a-9fc4-c7272be10900");


    public static UUID readDataServiceId = UUID.fromString("27760001-999d-4d6a-9fc4-c7272be10900");
    public static UUID readDataWriteUUID = UUID.fromString("27760002-999d-4d6a-9fc4-c7272be10900");
    public static UUID readDataNotifyUUID = UUID.fromString("27760003-999d-4d6a-9fc4-c7272be10900");



    public static UUID upgradeDataServiceId = UUID.fromString("27770001-999c-4d6a-9fc4-c7272be10900");
    public static UUID upgradePackageDataWriteUUID = UUID.fromString("27770003-999c-4d6a-9fc4-c7272be10900");
    public static UUID upgradeDataWriteNotifyUUID = UUID.fromString("27770002-999c-4d6a-9fc4-c7272be10900");


    public static byte[]
            deviceReadyHead = new byte[]{0x20,0x00,0x01};
    public static byte[] getLockStatusHead = new byte[]{0x20,0x00,0x1D};
    public static byte[] getSubLockStatusHead = new byte[]{0x20,0x00,0x1F};
    public static byte[] unlockHead = new byte[]{0x60,0x07,(byte)0xDA};
    public static byte[] lockHead = new byte[]{0x60,0x07,(byte)0xDB};
    public static byte[] activeNetworkHead = new byte[]{0x60,0x07,(byte)0xDC};
    public static byte[] uploadStatusHead = new byte[]{0x30,(byte)0xA0,0x29};

    public static final int func_id_of_unlock = 2010;
    public static final int func_id_of_lock = 2011;
    public static final int func_id_of_activate_network = 2012;
    public static final int func_id_of_timer = 12046;
    public static final int func_id_of_ip1 = 12031;
    public static final int func_id_of_ip2 = 12032;
    public static final int func_id_of_change_unclock_pwd = 2013;
    public static final int func_id_of_apn_addr = 12035;
    public static final int func_id_of_apn_username = 12036;
    public static final int func_id_of_apn_pwd = 12037;
    public static final int func_id_of_ble_pwd_change = 2016;

    public static final int func_id_of_sub_lock_version = 33;

    public static final int func_id_of_sub_lock_boot_version = 34;
    public static final int func_id_of_reboot = 2003;
    public static final int func_id_of_sub_lock_shutdown = 2005;
    public static final int func_id_of_sub_lock_factory_reset = 2006;
    public static final int func_id_of_sub_lock_unclock = 2019;
    public static final int func_id_of_sub_lock_lock = 2020;
    public static final int func_id_of_sub_lock_device_name = 4033;
    public static final int func_id_of_sub_lock_broadcast_interval = 4043;
    public static final int func_id_of_sub_lock_long_range = 4035;
    public static final int func_id_of_sub_lock_ble_transmitted_power = 4036;
    public static final int func_id_of_sub_lock_led = 4037;
    public static final int func_id_of_sub_lock_buzzer = 4038;
    public static final int func_id_of_sub_lock_datetime = 4044;
    public static final int func_id_of_sub_lock_device_id = 4041;
    public static final int func_id_of_open_sub_lock = 2019;
    public static final int func_id_of_sub_lock_alarm_open_set = 4040;
    public static final int func_id_of_sub_lock_temp_alarm_set = 4042;

    public static final int func_id_of_reset_default = 2026;
    public static final int func_id_of_clear_his_data = 2025;
    public static final int func_id_of_read_sub_lock_his_data = 1017;

    public static final int CFG_RESTORE_NONE = 0;
    public static final int CFG_RESTORE_IP = 1;
    public static final int CFG_RESTORE_APN = 2;
    public static final int CFG_RESTORE_TIMER = 3;
    public static final int CFG_RESTORE_NFCIDLIST = 4;
    public static final int CFG_RESTORE_SUBLOCKLIST = 5;


    private String deviceName;
    private String mac;
    private String imei;
    private String rssi;
    private Date date;
    private byte[] srcData;
    private String id;
    private String model;
    private String software;
    private String hardware;
    private float voltage;
    private String deviceId;
    private boolean isExpand = false;

    private boolean isSubLock = false;
    private boolean isCharging = false;
    private boolean isChargingOverVoltage = false;
    private boolean isLowPower = false;
    private boolean isHighTemp = false;
    private boolean isLowTemp = false;
    private boolean isOpenLockCover = false;
    private boolean isOpenBackCover = false;
    private boolean isGpsPosition = false;
    private boolean isGpsJamming = false;
    private Float solarVoltage;


    
    private int batteryPercent;
    private Float temp;
    private int lockType;

    private String nfcId;
    private String macId;
    private int subLockAlarm;

    public int getSubLockAlarm() {
        return subLockAlarm;
    }

    public void setSubLockAlarm(int subLockAlarm) {
        this.subLockAlarm = subLockAlarm;
    }

    public String getMacId() {
        return macId;
    }

    public void setMacId(String macId) {
        this.macId = macId;
    }

    public String getNfcId() {
        return nfcId;
    }

    public void setNfcId(String nfcId) {
        this.nfcId = nfcId;
    }

    public int getLockType() {
        return lockType;
    }

    public void setLockType(int lockType) {
        this.lockType = lockType;
    }

    public void parseSubLockStatus(int lockStatus){
        boolean isCharging = (lockStatus & 0x01) == 0x01;
        boolean isChargingOverVoltage = (lockStatus & 0x02) == 0x02;
        boolean isLowPower = (lockStatus & 0x04) == 0x04;
        boolean isHighTemp = (lockStatus & 0x08) == 0x08;
        boolean isLowTemp = (lockStatus & 0x10) == 0x10;
        boolean isOpenLockCover = (lockStatus & 0x20) == 0x20;
        boolean isOpenBackCover = (lockStatus & 0x40) == 0x40;
        boolean isGpsPosition = (lockStatus & 0x80) == 0x80;
        boolean isGpsJamming = (lockStatus & 0x100) == 0x100;
        this.setCharging(isCharging);
        this.setChargingOverVoltage(isChargingOverVoltage);
        this.setLowPower(isLowPower);
        this.setHighTemp(isHighTemp);
        this.setLowTemp(isLowTemp);
        this.setOpenLockCover(isOpenLockCover);
        this.setOpenBackCover(isOpenBackCover);
        this.setGpsPosition(isGpsPosition);
        this.setGpsJamming(isGpsJamming);
    }

    public boolean isSubLockDeviceIdValidAndNoneZero(){
        if(deviceId != null){
            try{
                long deviceIdInt = Long.valueOf(deviceId);
                return deviceIdInt != 0;
            }catch (Exception e){

            }
            return false;
        }else{
            return false;
        }
    }
    public static boolean isParentLockDeviceIdValid(String deviceId){
        if(deviceId != null){
            try{
                String temp = deviceId.toUpperCase().replaceAll("F","").replaceAll("f","");
                return temp.length() != 0;
            }catch (Exception e){

            }
            return false;
        }else{
            return false;
        }
    }

    public String getAlarm(Context context) {
        StringBuilder resp = new StringBuilder();

        if (isCharging) {
            resp.append(context.getResources().getString(R.string.charging)).append(";");
        }
        if (isChargingOverVoltage) {
            resp.append(context.getResources().getString(R.string.is_charging_over_voltage)).append(";");
        }
        if (isLowPower) {
            resp.append(context.getResources().getString(R.string.battery_low)).append(";");
        }
        if (isHighTemp) {
            resp.append(context.getResources().getString(R.string.alarm_high_temperature)).append(";");
        }
        if (isLowTemp) {
            resp.append(context.getResources().getString(R.string.alarm_low_temperature)).append(";");
        }
        if (isOpenLockCover) {
            resp.append(context.getResources().getString(R.string.alarm_lock_storage_open)).append(";");
        }
        if (isOpenBackCover) {
            resp.append(context.getResources().getString(R.string.alarm_rear_cover_open)).append(";");
        }

        // 如果没有警报，返回空字符串
        return resp.length() > 0 ? resp.toString() : "";
    }




    public int getBatteryPercent() {
        return batteryPercent;
    }

    public void setBatteryPercent(int batteryPercent) {
        this.batteryPercent = batteryPercent;
    }

    public final static String MODEL_OF_SGX120B01 = "SolarGuardX 120 B01";
    public final static String MODEL_OF_SGX110 = "SolarGuardX 110";
    public final static String MODEL_OF_TC019 = "SolarGuardX 200";
    public static final String MODEL_OF_TC015 = "SolarGuardX 100";

    public static boolean isSupportConfig(String model,String version,String deviceId){
        if(model.equals(MODEL_OF_SGX120B01)){
            return true;
        }else if(model.equals(MODEL_OF_TC015)){
            return false;
        }else if(deviceId != null && deviceId.trim().length() > 0){
            int versionInt = Integer.valueOf(version.replaceAll("V","").replaceAll("v","").replaceAll("\\.",""));
            if(versionInt >= 105){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public static boolean isSupportClearHisAndResetDefault(String model,String version){
        if(model.equals(MODEL_OF_SGX120B01)){
            return false;
        }else if(model.equals(MODEL_OF_TC015)){
            return false;
        }else if(model.equals(MODEL_OF_TC019)){
            int versionInt = Integer.valueOf(version.replaceAll("V","").replaceAll("v","").replaceAll("\\.",""));
            if(versionInt >= 116){
                return true;
            }else{
                return false;
            }
        } else{
            int versionInt = Integer.valueOf(version.replaceAll("V","").replaceAll("v","").replaceAll("\\.",""));
            if(versionInt >= 106){
                return true;
            }else{
                return false;
            }
        }
    }

    public static boolean isSubLockDevice(String model){
        if(model.equals(MODEL_OF_SGX120B01)){
            return true;
        }
        return false;
    }
    public boolean isGpsPosition() {
        return isGpsPosition;
    }

    public void setGpsPosition(boolean gpsPosition) {
        isGpsPosition = gpsPosition;
    }

    public boolean isGpsJamming() {
        return isGpsJamming;
    }

    public void setGpsJamming(boolean gpsJamming) {
        isGpsJamming = gpsJamming;
    }

    public boolean isSubLock() {
        return isSubLock;
    }

    public void setSubLock(boolean subLock) {
        isSubLock = subLock;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }

    public boolean isChargingOverVoltage() {
        return isChargingOverVoltage;
    }

    public void setChargingOverVoltage(boolean chargingOverVoltage) {
        isChargingOverVoltage = chargingOverVoltage;
    }

    public boolean isLowPower() {
        return isLowPower;
    }

    public void setLowPower(boolean lowPower) {
        isLowPower = lowPower;
    }

    public boolean isHighTemp() {
        return isHighTemp;
    }

    public void setHighTemp(boolean highTemp) {
        isHighTemp = highTemp;
    }

    public boolean isLowTemp() {
        return isLowTemp;
    }

    public void setLowTemp(boolean lowTemp) {
        isLowTemp = lowTemp;
    }

    public boolean isOpenLockCover() {
        return isOpenLockCover;
    }

    public void setOpenLockCover(boolean openLockCover) {
        isOpenLockCover = openLockCover;
    }

    public boolean isOpenBackCover() {
        return isOpenBackCover;
    }

    public void setOpenBackCover(boolean openBackCover) {
        isOpenBackCover = openBackCover;
    }

    public Float getSolarVoltage() {
        return solarVoltage;
    }

    public void setSolarVoltage(Float solarVoltage) {
        this.solarVoltage = solarVoltage;
    }

    public Float getTemp() {
        return temp;
    }

    public void setTemp(Float temp) {
        this.temp = temp;
    }

    public boolean isSupportReadHis() {
        return supportReadHis;
    }

    public void setSupportReadHis(boolean supportReadHis) {
        this.supportReadHis = supportReadHis;
    }

    private boolean supportReadHis = true;

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public float getVoltage() {
        return voltage;
    }

    public void setVoltage(float voltage) {
        this.voltage = voltage;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }



    public static HashMap<Byte ,byte[]> parseRawData(byte[] rawData){
        HashMap<Byte ,byte[]> result = new HashMap<>();
        int index = 0;
        while (index < rawData.length){
            int len = rawData[index];
            if (index + len + 1 <= rawData.length && len > 2){
                byte type = rawData[index + 1];
                byte[] itemData = Arrays.copyOfRange(rawData,index+2,index + len  + 1);
                if(type == 0x16){
                    if (itemData[0] == (byte)0xaf && itemData[1] == (byte)0xde){
                        result.put((byte)0xFE,itemData);
                    }else if (itemData[0] == (byte)0xaf && itemData[1] == (byte)0xbe){
                        result.put((byte)0xfd,itemData);
                    }else if (itemData[0] == (byte)0xaf && itemData[1] == (byte)0xae){
                        result.put((byte)0xae,itemData);
                    }else if (itemData[0] == (byte)0xaa && itemData[1] == (byte)0xfe){
                        result.put((byte)0xaa,itemData);
                    }else{
                        result.put(type,itemData);
                    }
                }else{
                    result.put(type,itemData);
                }
            }
            index += len + 1;
        }
        return result;
    }

    public static String getBatteryPercent(float voltage){

        if(MyByteUtils.isShowVirtualData){
            return getVirtualBatteryPercent(voltage) + "%";
        }
        return getRealBatteryPercent(voltage) + "%";
    }

    private static int getRealBatteryPercent(float voltage){
        if(voltage >= 4.1f){
            return 100;
        }else if(voltage >= 3.8f){
            return (int)(60+(voltage * 1000-3800)*40/(4100-3800));
        }else if(voltage >= 3.45f){
            return (int)(10+(voltage * 1000-3450 )*50/(3800-3450));
        }else if(voltage >= 3.2f){
            return (int)(1+(voltage * 1000-3200 )*9/(3450-3200));
        }else  {
            return 1;
        }
    }
    private static int getVirtualBatteryPercent(float voltage){
        if(voltage >= 4.10f){
            return 100;
        }else if(voltage >= 3.65f){
            return 95;
        }else if(voltage >= 3.2f){
            return (int)(1+(voltage * 1000-3200 )*94/( 3650-3200));
        }else  {
            return 1;
        }
    }

    public static String parseModel(byte protocolByte){
        if(protocolByte == (byte)0x62){
            return MODEL_OF_TC015;
        }else if(protocolByte == (byte)0x65 || protocolByte == (byte)0x7b || protocolByte == (byte)0x7c){
            return MODEL_OF_TC019;
        }else if(protocolByte == 119 || protocolByte == 120 || protocolByte == 121|| protocolByte == 122){
            return MODEL_OF_SGX110;
        }else{
            return "";
        }
    }

    public static boolean isSupportReadHistory(BleDeviceData bleDeviceData){
        if(bleDeviceData.getModel().equals(MODEL_OF_TC019) || bleDeviceData.getModel().equals(MODEL_OF_SGX110)){
            return bleDeviceData.supportReadHis;
        }else if(bleDeviceData.getModel().equals(MODEL_OF_SGX120B01)){
            String formatSoftware = bleDeviceData.getSoftware().replace("V","").replaceAll("v","").replaceAll("\\.","");
            int result = formatSoftware.compareToIgnoreCase("1002");
            if(result >= 0){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
    private static final String PREFS_NAME = "BleSubLockInfo";
    public static void saveSubLockBindMap(Context context, String imei, List<String> subLockList){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "sub_locks_" + imei;
        String parentLock = prefs.getString("parent_locks", "");
        if(parentLock == null || parentLock.isEmpty()){
            parentLock = imei;
        }else{
            if(!parentLock.contains(imei)){
                parentLock = parentLock + "," + imei;
            }
        }
        SharedPreferences.Editor editor = prefs.edit();
        String joined = TextUtils.join(",", subLockList);
        editor.putString(key, joined);
        editor.putString("parent_locks", parentLock);
        editor.apply();
    }
    public static List<String> getSubLockBindMap(Context context, String imei){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "sub_locks_" + imei;
        String joined = prefs.getString(key, "");
        if(joined == null || joined.isEmpty()){
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(joined.split(",")));
    }

    public static List<String> getHadSubLockImeis(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String joined = prefs.getString("parent_locks", "");
        if (joined == null || joined.isEmpty()) {
            return new ArrayList<>();
        }
        HashSet<String> set = new HashSet<>(Arrays.asList(joined.split(",")));
        return new ArrayList<>(set);
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
    public static void switchCurTempUnit(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tempUnit = preferences.getString("tempUnit","0");
        if(tempUnit != null && tempUnit.equals("0")){
            preferences.edit().putString("tempUnit","1").commit();
        }else{
            preferences.edit().putString("tempUnit","0").commit();
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
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


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
        this.srcData = srcData;
    }


}
