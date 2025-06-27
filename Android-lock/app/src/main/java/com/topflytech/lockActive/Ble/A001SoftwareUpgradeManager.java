package com.topflytech.lockActive.Ble;

import android.app.Activity;
import com.topflytech.lockActive.data.MyByteUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class A001SoftwareUpgradeManager {
    private ArrayList<ArrayList<byte[]>> upgradeFileContentList = new ArrayList<>();
    private ArrayList<Integer> all4KPackageCrc = new ArrayList<>();
    private byte[] allBytes;
    private boolean isStop = false;
    private boolean isSendFirst = false;

    private int onePackLen = 245;

    private BleStatusCallback bleStatusCallback = new BleStatusCallback() {
        @Override
        public void onNotifyValue(byte[] value) {

        }

        @Override
        public void onBleStatusCallback(int connectStatus) {

        }

        @Override
        public void onRssiCallback(int rssi) {

        }

        @Override
        public void onUpgradeNotifyValue(byte[] value) {
            receiveCmdResp(value);
        }
    };

    public A001SoftwareUpgradeManager(Activity activity, UpgradeStatusCallback callback) {
        this.activity = activity;
        this.callback = callback;
        Thread workerThread = new Thread(checkTaskStatus);
        workerThread.start();
        TftBleConnectManager.getInstance().setUpgradeCallback("A001SoftwareUpgradeManager",bleStatusCallback);
    }

    private int curStep = 0;
    private int cur4KPackage = 0;
    private int curPackage = 0;
    private int restartFromHeadCount = 0;
    private int sendPackageErrorCount = 0;
    private byte[] deviceTypeHead;


    public interface UpgradeStatusCallback {
        void onUpgradeStatus(int status, float percent);
    }

    private UpgradeStatusCallback callback;
    private Activity activity;
    public static final int STATUS_OF_FINE_NOT_FIND = -1;
    public static final int STATUS_OF_UPGRADE_START = 0;
    public static final int STATUS_OF_UPGRADE_WRITE_ONE_BUFFER = 1;
    public static final int STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC = 2;
    public static final int STATUS_OF_UPGRADE_CHECK_ALL_CRC = 3;
    public static final int STATUS_OF_UPGRADE_WRITE_FLASH = 4;
    public static final int STATUS_OF_UPGRADE_WRITE_END = 5;
    public static final int STATUS_OF_UPGRADE_WRITE_SUCC = 6;
    public static final int STATUS_OF_UPGRADE_UNKNOWN_ERROR = 7;
    public static final int STATUS_OF_UPGRADE_WRITE_CUR_package = 8;
    public static final int STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE = 9;
    public HashMap<Integer, Integer> stepTimeoutMap = new HashMap<Integer, Integer>() {{
        put(STATUS_OF_UPGRADE_START, 10);
        put(STATUS_OF_UPGRADE_WRITE_ONE_BUFFER, 10);
        put(STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC, 12);
        put(STATUS_OF_UPGRADE_CHECK_ALL_CRC, 15);
        put(STATUS_OF_UPGRADE_WRITE_FLASH, 10);
        put(STATUS_OF_UPGRADE_WRITE_END, 10);
        put(STATUS_OF_UPGRADE_WRITE_SUCC, 10);
        put(STATUS_OF_UPGRADE_WRITE_CUR_package, 10);
    }};
    private Date lastWriteDate;
    private boolean isWaitResponse = false;


    private boolean checkRespTimeout() {
        if (lastWriteDate != null) {
            Date now = new Date();
            int timeout = 10;
            if (stepTimeoutMap.containsKey(curStep)) {
                timeout = stepTimeoutMap.get(curStep);
            }
            if (now.getTime() - lastWriteDate.getTime() > timeout * 1000) {
                return true;
            }
            return false;
        }
        return false;
    }

    private Runnable checkTaskStatus = new Runnable() {
        @Override
        public void run() {
            while (!isStop) {
                try {
                    if (!isStartUpgrade) {
                        Thread.sleep(1000);
                        continue;
                    }
                    if (isWaitResponse) {
                        if (checkRespTimeout()) {
                            doErrorCtrl(curStep);
                            isWaitResponse = false;
                        } else {
                            Thread.sleep(50);
                            continue;
                        }
                    }

                    if (curStep == STATUS_OF_UPGRADE_START) {
                        isWaitResponse = true;
                        if(deviceTypeHead != null && deviceTypeHead.length == 4){
                            writeArray( new byte[]{0x01,deviceTypeHead[0],deviceTypeHead[1],deviceTypeHead[2],deviceTypeHead[3]});
                        }else{
                            isStartUpgrade = false;
                            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR, -1);
                        }

                    } else if (curStep == STATUS_OF_UPGRADE_WRITE_ONE_BUFFER) {
//                        int curPackageOffset = curPackage * onePackLen;
//                        if(upgradeFileContentList.size() > cur4KPackage && upgradeFileContentList.get(cur4KPackage).size()  == curPackage){
//                            int allLen = 0;
//                            ArrayList<byte[]> send4KItem = upgradeFileContentList.get(cur4KPackage);
//                            for(byte[] item : send4KItem){
//                                allLen += item.length;
//                            }
//                            curPackageOffset = allLen;
//                        }
//                        byte[] offset = MyByteUtils.unSignedInt2Bytes(cur4KPackage * 4096 + curPackageOffset);
                        byte[] content = new byte[]{0x02, 0x00};
                        if (cur4KPackage == upgradeFileContentList.size() - 1 && curPackage == upgradeFileContentList.get(cur4KPackage).size() ) {
                            content[5] = 0x01;
                            writeArray( content);
                            isWaitResponse = true;
                        }else{
                            curStep = STATUS_OF_UPGRADE_WRITE_CUR_package;
                            continue;
                        }

                    } else if (curStep == STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC) {
                        if (upgradeFileContentList.size() > cur4KPackage) {
                            int crc = all4KPackageCrc.get(cur4KPackage);
                            byte[] content = new byte[]{0x03, (byte) crc};
                            writeArray( content);
                            isWaitResponse = true;
                        } else {
                            isStartUpgrade = false;
                            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR, -1);
                        }

                    } else if (curStep == STATUS_OF_UPGRADE_WRITE_FLASH) {
                        isWaitResponse = true;
                        writeArray( new byte[]{0x04});
                    } else if (curStep == STATUS_OF_UPGRADE_CHECK_ALL_CRC) {
                        int allCrc = MyByteUtils.calCrc(Arrays.copyOfRange(allBytes,4,allBytes.length), allBytes.length - 4);
                        byte[] content = new byte[]{0x05, (byte) allCrc};
                        writeArray( content);
                        isWaitResponse = true;
                    } else if (curStep == STATUS_OF_UPGRADE_WRITE_END) {
                        isWaitResponse = true;
                        writeArray( new byte[]{0x06});
                    } else if (curStep == STATUS_OF_UPGRADE_WRITE_CUR_package) {
                        if (upgradeFileContentList.size() > cur4KPackage) {
                            ArrayList<byte[]> curItem = upgradeFileContentList.get(cur4KPackage);
                            if (curItem.size() > curPackage) {
                                byte[] curWrite = curItem.get(curPackage);
                                writeDataArray( curWrite);
                                curStep = STATUS_OF_UPGRADE_WRITE_ONE_BUFFER;
                                curPackage++;
                            } else {
                                curStep = STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC;
                                isWaitResponse = false;
                                continue;
//                                callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR, -1);
                            }
                        } else {
                            isStartUpgrade = false;
                            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR, -1);
                        }
                    }
                    Thread.sleep(3);
                } catch (Exception e) {

                }
            }
        }
    };

    private void writeDataArray( byte[] content) {
        lastWriteDate = new Date();
        TftBleConnectManager.getInstance().writeUpgradePackageDataArray(content);
    }

    private void writeArray( byte[] content) {
        lastWriteDate = new Date();
        TftBleConnectManager.getInstance().writeUpgradeCmdDataArray(content);
    }

    private boolean isStartUpgrade = false;
    private String mac;
    private String name;



    public void startUpgrade( String path ){

        resetStatus();
        try{
             allBytes = readFileToByteArray(path);
            splitSrcData();
            isStartUpgrade = true;
        }catch (Exception e){
            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR,0);

        }
    }

    public void stopUpgrade(){
        isStartUpgrade = false;
    }

    public boolean getStartUpgrade(){
        return isStartUpgrade;
    }

    private void splitSrcData(){
        int allLen = allBytes.length;
        deviceTypeHead = Arrays.copyOfRange(allBytes,0,4);
        int index = 4;
        while(allBytes != null && index < allLen){
            if(index + 4096 <= allLen){
                byte[] curItem = Arrays.copyOfRange(allBytes,index,index+4096);
                parse4KBytes(curItem);
            }else{
                byte[] curItem = Arrays.copyOfRange(allBytes,index,allLen);
                parse4KBytes(curItem);
            }
            index += 4096;
        }
    }

    private void parse4KBytes(byte[] item){
        int index = 0;
        int allLen = item.length;
        ArrayList<byte[]> result = new ArrayList<>();
        while (index < allLen){
            if(index + onePackLen <= allLen){
                byte[] curItem = Arrays.copyOfRange(item,index,index+onePackLen);
                result.add(curItem);
            }else{
                byte[] curItem = Arrays.copyOfRange(item,index,allLen);
                result.add(curItem);
            }
            index += onePackLen;
        }
        int crc = MyByteUtils.calCrc(item,item.length);
        all4KPackageCrc.add(crc);
        upgradeFileContentList.add(result);
    }

    private byte[] readFileToByteArray(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
           callback.onUpgradeStatus(STATUS_OF_FINE_NOT_FIND,0);
            return null;
        }

        long fileLength = file.length();

        // Check if file length is larger than Integer.MAX_VALUE
        if (fileLength > Integer.MAX_VALUE) {
            callback.onUpgradeStatus(STATUS_OF_FINE_NOT_FIND,0);
            return null;
        }

        byte[] fileContent = new byte[(int) fileLength];
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            int bytesRead = 0;
            int offset = 0;

            while (offset < fileContent.length && (bytesRead = fis.read(fileContent, offset, fileContent.length - offset)) >= 0) {
                offset += bytesRead;
            }

            if (offset < fileContent.length) {
                callback.onUpgradeStatus(STATUS_OF_FINE_NOT_FIND,0);
                return null;
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fileContent;
    }

    public void resetStatus(){
        curStep = STATUS_OF_UPGRADE_START;
        curPackage = 0;
        cur4KPackage = 0;
        upgradeFileContentList.clear();
        all4KPackageCrc.clear();
        deviceTypeHead = null;
        allBytes = null;
        isSendFirst = false;
        sendPackageErrorCount = 0;
        restartFromHeadCount = 0;
    }

    private void returnToHeadRestartUpgrade(){
        curStep = STATUS_OF_UPGRADE_START;
        isSendFirst = false;
        curPackage = 0;
        cur4KPackage = 0;
        sendPackageErrorCount = 0;
        restartFromHeadCount++;
        if(restartFromHeadCount >= 3){
            isStartUpgrade = false;
            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR,-1);
        }
    }



    public void stopService(){
        isStop = true;
    }

    public void receiveCmdResp(byte[] data){
        if(!isStartUpgrade){
            return;
        }
        if(data.length < 2){
            callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR,0);
            return;
        }
        if(data[0] == (byte)0x81){
            if(data[1] == 0x01){
                updateProgress();
                curStep = STATUS_OF_UPGRADE_WRITE_ONE_BUFFER;
                isWaitResponse = false;
            }else if(data[1] == 0x02){
                isStartUpgrade = false;
                callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR,-1);
            }else{
                doErrorCtrl(STATUS_OF_UPGRADE_START);
            }

        }else if(data[0] == (byte)0x82){
            if(data[1] == 0x01){
                if(!isSendFirst){
                    isSendFirst = true;
                    curStep = STATUS_OF_UPGRADE_WRITE_CUR_package;
                }else{
                    if(upgradeFileContentList.size() > cur4KPackage){
                        ArrayList<byte[]> curItem = upgradeFileContentList.get(cur4KPackage);
                        if (curItem.size() > curPackage){
                            curStep = STATUS_OF_UPGRADE_WRITE_CUR_package;
                        }else{
                            curStep = STATUS_OF_UPGRADE_CHECK_ONE_BUFFER_CRC;
                        }
                    }
                }
                updateProgress();
                isWaitResponse = false;
            }else{
                //resend cur package
                curPackage--;
                if(curPackage < 0){
                    cur4KPackage--;
                    if(cur4KPackage < 0){
                        cur4KPackage = 0;
                    }
                }
                doErrorCtrl(STATUS_OF_UPGRADE_WRITE_ONE_BUFFER);

            }

        }else if(data[0] == (byte)0x83){
            if(data[1] == 0x01){
                cur4KPackage++;
                curPackage=0;
                curStep = STATUS_OF_UPGRADE_WRITE_FLASH;
                updateProgress();
                isWaitResponse = false;
            }else{
                curPackage=0;
                doErrorCtrl(STATUS_OF_UPGRADE_WRITE_ONE_BUFFER);
            }
        }else if(data[0] == (byte)0x84){
            if(data[1] == 0x01){
                if(upgradeFileContentList.size() > cur4KPackage){
                    curStep = STATUS_OF_UPGRADE_WRITE_ONE_BUFFER;
                    updateProgress();
                }else{
                    curStep = STATUS_OF_UPGRADE_CHECK_ALL_CRC;
                }
                isWaitResponse = false;
            }else{
                cur4KPackage--;
                curPackage=0;
                updateProgress();
                doErrorCtrl(STATUS_OF_UPGRADE_WRITE_ONE_BUFFER);
            }
        }else if(data[0] == (byte)0x85){
            if(data[1] == 0x01){
                curStep = STATUS_OF_UPGRADE_WRITE_END;
                updateProgress();
                isWaitResponse = false;
            }else{

                returnToHeadRestartUpgrade();
                isWaitResponse = false;
            }
        }else if(data[0] == (byte)0x86){
            if(data[1] == 0x01){
                curStep = STATUS_OF_UPGRADE_WRITE_SUCC;
                updateProgress();
            }else{
                returnToHeadRestartUpgrade();
                isWaitResponse = false;
            }
        }else{

        }
    }

    private void updateProgress(){
        int allPackage = 0;
        for(ArrayList<byte[]> item : upgradeFileContentList){
            allPackage += item.size();
        }
        float percent = (cur4KPackage * 17 + curPackage) * 1.0f  / allPackage *100;
        callback.onUpgradeStatus(curStep,percent);
    }

    private void doErrorCtrl(int nextStep) {
        sendPackageErrorCount++;
        if(sendPackageErrorCount > 10){
            restartFromHeadCount++;
            sendPackageErrorCount = 0;
            if(restartFromHeadCount >= 3){
                isStartUpgrade = false;
                callback.onUpgradeStatus(STATUS_OF_UPGRADE_UNKNOWN_ERROR,-1);
            }else{
                returnToHeadRestartUpgrade();
                isWaitResponse = false;
            }

        }else{
            curStep = nextStep ;
            isWaitResponse = false;
        }
    }

}
