package com.topflytech.codec.entities;

import java.util.ArrayList;
import java.util.Date;

public class InnerGeoDataMessage extends Message{
    private Date date;
    private ArrayList<InnerGeofence> geoList = new ArrayList<>();
    public int getLockGeofenceEnable() {
        return lockGeofenceEnable;
    }

    public void setLockGeofenceEnable(int lockGeofenceEnable) {
        this.lockGeofenceEnable = lockGeofenceEnable;
    }

    private int lockGeofenceEnable;
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<InnerGeofence> getGeoList() {
        return geoList;
    }

    public void setGeoList(ArrayList<InnerGeofence> geoList) {
        this.geoList = geoList;
    }

    public void addGeoPoint(InnerGeofence geo){
        this.geoList.add(geo);
    }
}
