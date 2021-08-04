package com.topflytech.codec;

import com.google.common.primitives.Bytes;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TimeUtils {

    /**
     * @param year   year (2013)
     * @param month  month (1-12)
     * @param day    day (1- 28/30/31)
     * @param hour   0 - 23
     * @param minute 0-59
     * @param second 0-59
     * @return 时间(基于GMT0)
     */
    public static Date GMT0(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    public static Date getGTM0Date(byte[] bytes,int startIndex) {
        byte[] dateData = Bytes.concat(new byte[]{0x20}, Arrays.copyOfRange(bytes, startIndex, startIndex + 6)); // 20130512122356
        String datetime = BytesUtils.bytes2HexString(dateData, 0);
        int year = Integer.parseInt(datetime.substring(0, 4));
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        if (year > curYear){
            year = year - 100;
        }
        int month = Integer.parseInt(datetime.substring(4, 6));
        int day = Integer.parseInt(datetime.substring(6, 8));
        int hour = Integer.parseInt(datetime.substring(8, 10));
        int minutes = Integer.parseInt(datetime.substring(10, 12));
        int seconds = Integer.parseInt(datetime.substring(12, 14));
        return GMT0(year, month, day, hour, minutes, seconds);
    }



}

