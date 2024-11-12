package com.topflytech.codec.entities;

import java.util.Date;

/**
 * Created by admin on 2018/9/21.
 */
public class NetworkInfoMessage extends Message {
    public String getNetworkOperator() {
        return networkOperator;
    }

    public void setNetworkOperator(String networkOperator) {
        this.networkOperator = networkOperator;
    }

    public String getAccessTechnology() {
        return accessTechnology;
    }

    public void setAccessTechnology(String accessTechnology) {
        this.accessTechnology = accessTechnology;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getWifiMac() {
        return wifiMac;
    }

    public void setWifiMac(String wifiMac) {
        this.wifiMac = wifiMac;
    }

    public String getWifiSsid() {
        return wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    private String networkOperator;
    private String accessTechnology;
    private String band;
    private String imsi;
    private Date date;
    private String iccid;
    private String wifiSsid;
    private String wifiMac;

}
