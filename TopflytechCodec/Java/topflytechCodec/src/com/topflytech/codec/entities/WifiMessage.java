package com.topflytech.codec.entities;

import java.util.Date;

/**
 * Created by admin on 2021/3/11.
 */
public class WifiMessage  extends Message{
    private Date date;
    private String selfMac;
    private String ap1Mac;
    private int ap1RSSI;
    private String ap2Mac;
    private int ap2RSSI;
    private String ap3Mac;
    private int ap3RSSI;


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSelfMac() {
        return selfMac;
    }

    public void setSelfMac(String selfMac) {
        this.selfMac = selfMac;
    }

    public String getAp1Mac() {
        return ap1Mac;
    }

    public void setAp1Mac(String ap1Mac) {
        this.ap1Mac = ap1Mac;
    }

    public int getAp1RSSI() {
        return ap1RSSI;
    }

    public void setAp1RSSI(int ap1RSSI) {
        this.ap1RSSI = ap1RSSI;
    }

    public String getAp2Mac() {
        return ap2Mac;
    }

    public void setAp2Mac(String ap2Mac) {
        this.ap2Mac = ap2Mac;
    }

    public int getAp2RSSI() {
        return ap2RSSI;
    }

    public void setAp2RSSI(int ap2RSSI) {
        this.ap2RSSI = ap2RSSI;
    }

    public String getAp3Mac() {
        return ap3Mac;
    }

    public void setAp3Mac(String ap3Mac) {
        this.ap3Mac = ap3Mac;
    }

    public int getAp3RSSI() {
        return ap3RSSI;
    }

    public void setAp3RSSI(int ap3RSSI) {
        this.ap3RSSI = ap3RSSI;
    }


}
