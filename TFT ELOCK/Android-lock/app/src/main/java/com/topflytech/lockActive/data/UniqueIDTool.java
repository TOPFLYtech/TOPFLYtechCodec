package com.topflytech.lockActive.data;

import android.media.MediaDrm;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UniqueIDTool {
    private final static String[] hexArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String getMediaDrmID() {
        UUID wideVineUuid = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
        try {
            MediaDrm wvDrm = new MediaDrm(wideVineUuid);
            byte[] wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);
            return android.util.Base64.encodeToString(wideVineId, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }

    }

    private static int hexCharToInt(byte c){
        int value = (int)c;
        if(value >= 97){
            return value - 87;
        }else if(value >= 65){
            return value - 55;
        }else{
            return value - 48;
        }
    }

    public static String getUniqueID(){
        String hardwareInfo = android.os.Build.BOARD + android.os.Build.BRAND + android.os.Build.DEVICE + android.os.Build.DISPLAY
                + android.os.Build.HOST + android.os.Build.ID + android.os.Build.MANUFACTURER + android.os.Build.MODEL
                + android.os.Build.PRODUCT + android.os.Build.TAGS + android.os.Build.TYPE +android.os. Build.USER + Build.FINGERPRINT;
        String mediaDrmId = getMediaDrmID();
//        Log.e("BluetoothUtils","my mediaDrmId:" + mediaDrmId);
        if(mediaDrmId != null){
            hardwareInfo += mediaDrmId;
        }
        String md5 = UniqueIDTool.CalcMD5(hardwareInfo);
        byte[] originByte = md5.getBytes();
        int[] values = new int[6];
        for(int i = 0;i < originByte.length;i++){
            int intValue = hexCharToInt(originByte[i]);
            values[i % 6] += intValue;
        }
        values[0] += 10;//The value of the first plus the Android flag a is 10,ios flag i is 17;
        byte[] byteValue = new byte[6];
        for(int i = 0;i < 6;i++){
            byteValue[i] = (byte)values[i];
        }
        return MyByteUtils.bytes2HexString(byteValue,0);
    }

    public static String CalcMD5(String originString) {
        try {
            //创建具有MD5算法的信息摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            //使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
            byte[] bytes = md.digest(originString.getBytes());
            //将得到的字节数组变成字符串返回
            String s = byteArrayToHex(bytes);
            return s.toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String byteArrayToHex(byte[] b){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(byteToHex(b[i]));
        }
        return sb.toString();
    }

    public static String byteToHex(byte b) {
        int n = b;
        if (n < 0)
            n = n + 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexArray[d1]+hexArray[d2];
    }
}


