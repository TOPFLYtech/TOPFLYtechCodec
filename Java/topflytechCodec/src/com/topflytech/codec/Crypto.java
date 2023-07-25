package com.topflytech.codec;

import com.topflytech.codec.entities.MessageEncryptType;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by admin on 2017/5/10.
 */
public class Crypto {
    public static byte[] MD5(byte[] data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(data);
            byte[] pathMd5 = Arrays.copyOfRange(digest, 4, 12);
            return pathMd5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String ivParam = "topflytech201205";

    public static byte[] aesEncrypt(byte[] src, String key) throws Exception {
        if (key == null) {
            System.out.print("Key is null");
            return null;
        }
        byte[] raw = key.getBytes();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] arrBTmp = md.digest(raw);
        SecretKeySpec skeySpec = new SecretKeySpec(arrBTmp, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        IvParameterSpec iv = new IvParameterSpec(ivParam.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(src);
        return encrypted;
    }

    public static byte[] aesDecrypt(byte[] src, String key) throws Exception {
        try {
            if (key == null) {
                System.out.print("Key为空null");
                return null;
            }
            byte[] raw = key.getBytes("ASCII");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] arrBTmp = md.digest(raw);
            SecretKeySpec skeySpec = new SecretKeySpec(arrBTmp, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParam.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            try {
                byte[] original = cipher.doFinal(src);
                return original;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }
    public static int getAesLength(int packageLength) {
        if (packageLength <= 15){
            return packageLength;
        }
        return ((packageLength - 15) / 16 + 1) * 16 + 15;
    }
    public static byte[] decryptData(byte[] data, int encryptType, String aesKey){
        if (encryptType == MessageEncryptType.MD5){
            byte[] realData = Arrays.copyOfRange(data, 0, data.length - 8);
            byte[] md5Data = Arrays.copyOfRange(data, data.length - 8, data.length);
            byte[] pathMd5 = Crypto.MD5(realData);
            if (pathMd5 == null){
                return null;
            }
            if (!Arrays.equals(pathMd5,md5Data)){
                return null;
            }
            return realData;
        }else if(encryptType == MessageEncryptType.AES){
            byte[] head = Arrays.copyOfRange(data,0,15);
            byte[] aesData = Arrays.copyOfRange(data,15,data.length);
            if (aesData == null || aesData.length == 0){
                return data;
            }
            byte[] realData = null;
            try {
                realData = Crypto.aesDecrypt(aesData, aesKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (realData == null){
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                try {
                    outputStream.write(head);
                    outputStream.write(realData);
                    return outputStream.toByteArray();
                } finally {
                    outputStream.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }else {
            return data;
        }
    }
}
