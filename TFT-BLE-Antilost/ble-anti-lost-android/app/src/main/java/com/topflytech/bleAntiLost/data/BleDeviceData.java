package com.topflytech.bleAntiLost.data;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class BleDeviceData {
    private String deviceName;
    private String mac;
    private String imei;
    private String rssi;
    private Date date;
    private byte[] srcData;
    private String id;
    private String protocol;
    private String model;
    private String software;
    private String hardware;
    private BluetoothDevice bleDevice;

    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public static String parseProtocol(byte protocolByte){
        if (protocolByte == (byte)0x44 || protocolByte == (byte)0x45 || protocolByte == (byte)0x46){
            return "tc008";
        }else if(protocolByte == (byte)0x4D){
            return "tc009";
        }else if(protocolByte == (byte)0x52){
            return "tc010";
        }else if(protocolByte == (byte)0x58){
            return "tc011";
        }else if(protocolByte == (byte)0x62){
            return "tc015";
        }else{
            return "";
        }
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
    public static final String MODEL_KNIGHTX_100 = "KnightX 100";
    public static final String MODEL_KNIGHTX_300 = "KnightX 300";
    public static final String MODEL_V0V_X10 = "VOV X10";
    public static String parseModel(byte protocolByte){
        if (protocolByte == (byte)0x44){
            return "TLW2-12BL";
        }else if (protocolByte == (byte)0x4D){
            return "TLP2-SFB";
        }else if(protocolByte == (byte)0x62){
            return "tc015";
        }else if(protocolByte == (byte)0x68){
            return MODEL_KNIGHTX_100;
        }else if(protocolByte == (byte)0x6a){
            return MODEL_KNIGHTX_300;
        }else if(protocolByte == (byte)0x75){
            return MODEL_V0V_X10;
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
