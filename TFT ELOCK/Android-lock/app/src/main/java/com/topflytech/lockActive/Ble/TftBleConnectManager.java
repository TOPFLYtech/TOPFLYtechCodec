package com.topflytech.lockActive.Ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.LogFileHelper;
import com.topflytech.lockActive.data.MyByteUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TftBleConnectManager {
    public static final int BLE_STATUS_OF_CLOSE = -1;
    public static final int BLE_STATUS_OF_DISCONNECT = 0;
    public static final int BLE_STATUS_OF_CONNECTING = 1;
    public static final int BLE_STATUS_OF_CONNECT_SUCC = 2;
    public static final int BLE_STATUS_OF_SCANNING = 3;
    private boolean enterUpgrade = false;
    private static TftBleConnectManager instance = null;


    private UUID curServiceId = BleDeviceData.unlockServiceId;
    private UUID curWriteUUID = BleDeviceData.unlockWriteUUID;
    private UUID curNotifyUUID = BleDeviceData.unlockNotifyUUID;

    private final static Object lock = new Object();
    public static TftBleConnectManager getInstance(){
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new TftBleConnectManager();

                }
            }
        }
        return instance;
    }


    public void setIsParentLockReadHisData(){
        curServiceId = BleDeviceData.readDataServiceId;
        curWriteUUID = BleDeviceData.readDataWriteUUID;
        curNotifyUUID = BleDeviceData.readDataNotifyUUID;
    }

    public boolean isEnterUpgrade() {
        return enterUpgrade;
    }

    public void setEnterUpgrade(boolean enterUpgrade) {
        this.enterUpgrade = enterUpgrade;
    }

    private Context context;
    public void init(Context context){
        this.context = context;
        new Thread(sendMsgThread).start();
    }
    private Date lastCheckStatusDate;
    private long getStatusTimeout = 4000;
    private LinkedBlockingDeque<byte[]> sendMsgQueue = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<byte[]> sendMsgMultiQueue = new LinkedBlockingDeque<>();
    boolean connectSucc = false;
    private boolean isCanSendMsg = true;
    private boolean isDeviceReady = false;
    boolean onThisView = false;
    private String mac;
    private boolean isSubLock = false;

    private boolean isNeedCheckDeviceReady = true;
    private boolean isNeedGetLockStatus = true;
    private BluetoothDevice bleDevice;
    private BluetoothGatt bluetoothGatt;

    private BluetoothGattCharacteristic unclockNotifyCharacteristic;
    private BluetoothGattCharacteristic unclockWriteCharacteristic;
    private BluetoothGattCharacteristic upgradeCmdReadWriteCharacteristic;
    private BluetoothGattCharacteristic upgradeDataWriteCharacteristic;
    public boolean getIsCanSendMsg() {
        return isCanSendMsg;
    }

    public boolean getIsDeviceReady() {
        return isDeviceReady;
    }

    public boolean isNeedGetLockStatus() {
        return isNeedGetLockStatus;
    }

    public void setNeedGetLockStatus(boolean needGetLockStatus) {
        isNeedGetLockStatus = needGetLockStatus;
    }

    public boolean isNeedCheckDeviceReady() {
        return isNeedCheckDeviceReady;
    }

    public void setNeedCheckDeviceReady(boolean needCheckDeviceReady) {
        isNeedCheckDeviceReady = needCheckDeviceReady;
    }

    public void setOnThisView(boolean onThisView) {
        this.onThisView = onThisView;
    }

    private Runnable sendMsgThread = new Runnable() {
        @Override
        public void run() {
            try {
                while (onThisView){
                    if ((isNeedCheckDeviceReady || isNeedGetLockStatus) && ( (sendMsgQueue.size() == 0 && sendMsgMultiQueue.size() == 0) || connectSucc == false || isDeviceReady == false)){
                        if(!isDeviceReady){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(isNeedCheckDeviceReady || isNeedGetLockStatus){
                                synchronized (lock){
                                    isCanSendMsg = false;
                                }
                            }
                            sendCheckDeviceReadyCmd();
//                            context.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                }
//                            });

                        }else{
                            Date now = new Date();
                            if (lastCheckStatusDate == null || now.getTime() - lastCheckStatusDate.getTime() > getStatusTimeout){
                                getLockStatus();
                            }
                            Thread.sleep(1000);
                        }
                        continue;
                    }
                    if (isCanSendMsg){
                        if (sendMsgQueue.size() != 0){
                            synchronized (lock){
                                if (isCanSendMsg){
                                    isCanSendMsg = false;
                                    byte[] needSendBytes = sendMsgQueue.poll();
                                    if (needSendBytes != null){
                                        sendContent(needSendBytes);
                                    }
                                }
                            }

                        }else{
                            while (sendMsgMultiQueue.size() != 0){
                                byte[] needSendBytes = sendMsgMultiQueue.poll();
                                if (needSendBytes != null){
                                    sendContent(needSendBytes);
                                }
                            }
                        }

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void sendCheckDeviceReadyCmd() {
        if(!isNeedCheckDeviceReady){
            return;
        }
        byte[] deviceReady = BleDeviceData.deviceReadyHead;
        sendContent(deviceReady);
    }
    public void getLockStatus(){
        if(enterUpgrade || !isNeedGetLockStatus){
            return;
        }
        byte[] needSendBytes = BleDeviceData.getLockStatusHead;
        if(isSubLock){
            needSendBytes = BleDeviceData.getSubLockStatusHead;
        }
        lastCheckStatusDate = new Date();
        sendContent(needSendBytes);
    }

    public void writeArrayContent(ArrayList<byte[]> writeContentList) {
        if (writeContentList.size() > 1){
            for (int i = 0; i < writeContentList.size(); i++) {
                sendMsgMultiQueue.offer(writeContentList.get(i));
            }
        }else{
            sendMsgQueue.offer(writeContentList.get(0));
        }

    }

    private void selfLog(String log){
        LogFileHelper.getInstance(context).writeIntoFile(log);
        Log.e("tft_eclock_log", log);
    }


    public void writeContent(final byte[] content){
        if(!connectSucc){
            return;
        }
        if (content != null && content.length > 0){
            sendMsgQueue.offer(content);
//            selfLog("write:" + MyByteUtils.bytes2HexString(content,0));
//            if(bluetoothGatt != null){
//                unclockWriteCharacteristic.setValue(content);
//                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                }
//                bluetoothGatt.writeCharacteristic(unclockWriteCharacteristic);
//                synchronized (lock){
//                    isCanSendMsg = true;
//                }
//            }
        }
    }

    private void sendContent(final byte[] content){
        if(!connectSucc){
            return;
        }
        if (content != null && content.length > 0){
            selfLog("write:" + MyByteUtils.bytes2HexString(content,0));
            if(bluetoothGatt != null){
                unclockWriteCharacteristic.setValue(content);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                }
                bluetoothGatt.writeCharacteristic(unclockWriteCharacteristic);

            }
        }
    }



    public void setDeviceReady(boolean deviceReady) {
        isDeviceReady = deviceReady;
    }




    private HashMap<String,BleStatusCallback> bleNotifyCallbackMap = new HashMap<>();

    public void setCallback(String activityName,BleStatusCallback callback) {
        bleNotifyCallbackMap.put(activityName,callback);
    }

    public void removeCallback(String activityName){
        bleNotifyCallbackMap.remove(activityName);
    }

    private void notifyCallback(byte[] value) {
        for (String activityName :
                bleNotifyCallbackMap.keySet()) {
            BleStatusCallback bleNotifyCallback = bleNotifyCallbackMap.get(activityName);
            if (bleNotifyCallback != null) {
                bleNotifyCallback.onNotifyValue(value);
            }
        }
    }
    private HashMap<String,BleStatusCallback> bleNotifyUpgradeCallbackMap = new HashMap<>();
    public void setUpgradeCallback(String activityName,BleStatusCallback callback) {
        bleNotifyUpgradeCallbackMap.put(activityName,callback);
    }

    public void removeUpgradeCallback(String activityName){
        bleNotifyUpgradeCallbackMap.remove(activityName);
    }
    private void notifyUpgradeCallback(byte[] value) {
        for (String activityName :
                bleNotifyUpgradeCallbackMap.keySet()) {
            BleStatusCallback bleNotifyCallback = bleNotifyUpgradeCallbackMap.get(activityName);
            if (bleNotifyCallback != null) {
                bleNotifyCallback.onUpgradeNotifyValue(value);
            }
        }
    }
    private void connectStatusCallback(int connectStatus){
        for (String activityName :
                bleNotifyCallbackMap.keySet()) {
            BleStatusCallback bleNotifyCallback = bleNotifyCallbackMap.get(activityName);
            if (bleNotifyCallback != null) {
                bleNotifyCallback.onBleStatusCallback(connectStatus);
            }
        }
    }
    public void disconnect(){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }

        if(bluetoothGatt != null){
            if (unclockNotifyCharacteristic != null) {
                // 启用通知
                setCharacteristicNotification(bluetoothGatt, unclockNotifyCharacteristic, false);
            }
            if(upgradeCmdReadWriteCharacteristic != null){
                setCharacteristicNotification(bluetoothGatt, upgradeCmdReadWriteCharacteristic, false);
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        bleDevice = null;
        sendMsgMultiQueue.clear();
        sendMsgQueue.clear();

        unclockNotifyCharacteristic = null;
        unclockWriteCharacteristic = null;
        upgradeCmdReadWriteCharacteristic = null;
        upgradeDataWriteCharacteristic = null;
        enterUpgrade = false;
        connectSucc = false;
        isDeviceReady = false;
    }

    public boolean isConnectSucc() {
        return connectSucc;
    }

    public void connect(String deviceMac,boolean isSubLock){
        if(mac != null && !deviceMac.equals(mac)){
            disconnect();
        }
        enterUpgrade = false;
        mac = deviceMac;
        this.isSubLock = isSubLock;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // 设备不支持蓝牙
            return;
        }
        bleDevice = bluetoothAdapter.getRemoteDevice(mac);
        if (bleDevice == null) {
            connectStatusCallback(BLE_STATUS_OF_DISCONNECT);
            return;
        }
        bleDevice.connectGatt(context, false, bluetoothGattCallback);
    }
    private boolean isNotificationEnabled( BluetoothGattDescriptor cccdDescriptor) {
        // 读取描述符的值
        byte[] value = cccdDescriptor.getValue();

        // 检查描述符值是否设置为通知或指示
        return value != null && value.length >= 2 &&
                (value[0] == 0x01 || value[0] == 0x02);
    }
    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


        }
        UUID descriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 通知描述符的UUID
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUUID);
        if(descriptor != null){
            if(isNotificationEnabled(descriptor)){
                return;
            }
        }
        boolean enableSucc = gatt.setCharacteristicNotification(characteristic, enabled);
        Log.e("TFTBleService","setCharacteristicNotification:" + enableSucc + ";" + characteristic.getUuid());
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean writeSucc =  gatt.writeDescriptor(descriptor);
            Log.e("TFTBleService","writeDescriptor:" + writeSucc + ";" + characteristic.getUuid());
            String deviceAddress = gatt.getDevice().getAddress();

        }
    }
    private static final int REQUEST_MTU_SIZE = 30;
    private int negotiatedMtu = 23; // 默认MTU
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("TFTBleService", "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                String deviceAddress = gatt.getDevice().getAddress();
                // 处理连接成功
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                String deviceAddress = gatt.getDevice().getAddress();
                // 处理断开连接
                 connectStatusCallback(BLE_STATUS_OF_DISCONNECT);
                connectSucc = false;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//
//                    }
//                    boolean mtuRequested = gatt.requestMtu(REQUEST_MTU_SIZE);
//                    if (mtuRequested) {
//                        Log.i("tft_eclock_log", "Requested MTU size: " + REQUEST_MTU_SIZE);
//                    } else {
//                        Log.w("tft_eclock_log", "Could not initiate MTU request.");
//                        // 如果MTU请求启动失败，你可能需要按当前MTU处理数据写入
//                        // proceedToWriteCharacteristicWithCurrentMtu(characteristic);
//                    }
//                } else {
//                    // API < 21, 无法请求MTU，使用默认MTU (23)
//                    Log.i("tft_eclock_log", "MTU request not supported on this API level. Using default MTU: " + negotiatedMtu);
//                    // proceedToWriteCharacteristicWithCurrentMtu(characteristic);
//                }
                String deviceAddress = gatt.getDevice().getAddress();
                BluetoothGattService service = gatt.getService(curServiceId);
                if (service != null) {
                    unclockNotifyCharacteristic = service.getCharacteristic(curNotifyUUID);
                    if (unclockNotifyCharacteristic != null) {
                        // 启用通知
                        setCharacteristicNotification(gatt, unclockNotifyCharacteristic, true);
                    }
                    unclockWriteCharacteristic = service.getCharacteristic(curWriteUUID);
                }
                if (isSubLock) {
                    BluetoothGattService upgradeService = gatt.getService(BleDeviceData.upgradeDataServiceId);
                    if(upgradeService != null){
                        upgradeCmdReadWriteCharacteristic  = upgradeService.getCharacteristic(BleDeviceData.upgradeDataWriteNotifyUUID);
                        upgradeDataWriteCharacteristic = upgradeService.getCharacteristic(BleDeviceData.upgradePackageDataWriteUUID);
                    }

                }

            } else {
                disconnect();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("TFTBleService", "onCharacteristicRead");
            // 通过gatt对象来识别设备
            String deviceAddress = gatt.getDevice().getAddress();
            // 处理特征值
            byte[] data = characteristic.getValue();
            Log.e("BluetoothNotify", MyByteUtils.bytes2HexString(data, 0));
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("TFTBleService", "onCharacteristicWrite");
            // 通过gatt对象来识别设备
            String deviceAddress = gatt.getDevice().getAddress();
            byte[] data = characteristic.getValue();
            synchronized (lock){
                isCanSendMsg = true;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e("TFTBleService", "onCharacteristicChanged");
            UUID characteristicUUID = characteristic.getUuid();
            UUID serviceUUID = characteristic.getService().getUuid();
            String deviceAddress = gatt.getDevice().getAddress();

            if (characteristicUUID.equals(curNotifyUUID) && serviceUUID.equals(curServiceId)) {
                // 处理特定UUID的特征值通知
                byte[] data = characteristic.getValue();
                selfLog("resp:" + MyByteUtils.bytes2HexString(data, 0));

                notifyCallback(data);
            }
            if(characteristicUUID.equals(BleDeviceData.upgradeDataWriteNotifyUUID) && serviceUUID.equals(BleDeviceData.upgradeDataServiceId)){
                byte[] data = characteristic.getValue();
                selfLog("upgrade resp:" + MyByteUtils.bytes2HexString(data, 0));
                notifyUpgradeCallback(data);

            }
        }

        @Override
        public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
            super.onCharacteristicChanged(gatt, characteristic, value);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            super.onDescriptorRead(gatt, descriptor, status, value);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("TFTBleService", "onDescriptorWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(descriptor.getCharacteristic().getUuid().equals(curNotifyUUID)){
                    connectSucc = true;
                    connectStatusCallback(BLE_STATUS_OF_CONNECT_SUCC);
                    if(upgradeCmdReadWriteCharacteristic != null){
                        setCharacteristicNotification(gatt, upgradeCmdReadWriteCharacteristic, true);
                    }
                }
            } else {
                disconnect();
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onServiceChanged(@NonNull BluetoothGatt gatt) {
            super.onServiceChanged(gatt);
        }
    };
    public void writeUpgradePackageDataArray( byte[] content) {
        if(!isConnectSucc()){
            return;
        }
        selfLog("upgrade package write:" + MyByteUtils.bytes2HexString(content,0));
        if(bluetoothGatt != null){
            upgradeDataWriteCharacteristic.setValue(content);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            bluetoothGatt.writeCharacteristic(upgradeDataWriteCharacteristic);
            synchronized (lock){
                isCanSendMsg = true;
            }
        }
    }

    public void writeUpgradeCmdDataArray( byte[] content) {
        if(!isConnectSucc()){
            return;
        }
        selfLog("upgrade cmd write:" + MyByteUtils.bytes2HexString(content,0));
        if(bluetoothGatt != null){
            upgradeCmdReadWriteCharacteristic.setValue(content);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            bluetoothGatt.writeCharacteristic(upgradeCmdReadWriteCharacteristic);
            synchronized (lock){
                isCanSendMsg = true;
            }
        }
    }

}
