package com.topflytech.codec.entities;


import java.util.Date;
import java.util.List;

public class DeviceTempCollectionMessage extends Message{

    private int interval;
    private List<Float> tempList;
    private Date date;
    private int type = 1;//1:temp

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<Float> getTempList() {
        return tempList;
    }

    public void setTempList(List<Float> tempList) {
        this.tempList = tempList;
    }

}
