package com.topflytech.lockActive.data;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class BleDeviceData {
    public static UUID serviceId = UUID.fromString("27760001-999c-4d6a-9fc4-c7272be10900");
    public static UUID writeUUID = UUID.fromString("27760003-999c-4d6a-9fc4-c7272be10900");
    public static UUID notifyUUID = UUID.fromString("27760003-999c-4d6a-9fc4-c7272be10900");
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

    public static String parseModel(byte protocolByte){
        if(protocolByte == (byte)0x62){
            return "SolarGuardX 100";
        }else{
            return "";
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
