package com.topflytech.bleAntiLost.data;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.topflytech.bleAntiLost.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TFTBleService extends Service {
    private static final String TAG = "TFTBleService";
    private UUID serviceId = UUID.fromString("27760001-999C-4D6A-9FC4-C7272BE10900");
    private UUID uuid = UUID.fromString("27763561-999C-4D6A-9FC4-C7272BE10900");
    private final IBinder mBinder = new LocalBinder();
    private BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothLeScanner mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
    private ScanSettings mScanSettings;
    //    private BluetoothClient mClient;
    private boolean needSound = false;
    private boolean needShock = false;
    private int warningDuration = 0;
    private int warningCount = 0;
    private boolean isCurWarning = false;
    private Date lastRecvDate;
    private String notificationTitle = "TFT BLE Anti Lost";
    private String notificationContent;
    public static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    public static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    public static final int REQUEST_CODE_BLUETOOTH_ADVERTISE = 4;
    public static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
    private int defaultDeviceWarningDuration = 3;
    private int defaultDeviceWarningCount = 5;
    private ConcurrentHashMap<String, String> macImeiMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BluetoothGatt> imeiBleClientMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> imeiConnectStatusMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BluetoothGattCharacteristic> imeiWriteObj = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Date> disconnectImeiMap = new ConcurrentHashMap<>();
    private ArrayList<String> breakConnectImeiList = new ArrayList<>();
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("TFTBleService", "onCharacteristicRead");
            // 通过gatt对象来识别设备
            String deviceAddress = gatt.getDevice().getAddress();
            // 处理特征值
            byte[] data = characteristic.getValue();
            Log.e("BluetoothNotify", MyUtils.bytes2HexString(data, 0));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e("TFTBleService", "onCharacteristicChanged");
            UUID characteristicUUID = characteristic.getUuid();
            UUID serviceUUID = characteristic.getService().getUuid();
            String deviceAddress = gatt.getDevice().getAddress();

            if (characteristicUUID.equals(uuid) && serviceUUID.equals(serviceUUID)) {
                // 处理特定UUID的特征值通知
                byte[] data = characteristic.getValue();
                Log.e("BluetoothNotify", MyUtils.bytes2HexString(data, 0));
                String imei = macImeiMap.get(deviceAddress);
                notifyCallback(imei, data);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("TFTBleService", "onCharacteristicWrite");
            // 通过gatt对象来识别设备
            String deviceAddress = gatt.getDevice().getAddress();

            // 处理特征值写入
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            isConnectingBle = false;
            Log.e("TFTBleService", "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                String deviceAddress = gatt.getDevice().getAddress();
                // 处理连接成功
                if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                String imei = macImeiMap.get(deviceAddress);
                imeiBleClientMap.put(imei, gatt);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                String deviceAddress = gatt.getDevice().getAddress();
                // 处理断开连接
                String imei = macImeiMap.get(deviceAddress);
                if (imei != null) {
                    Integer connectStatus = imeiConnectStatusMap.get(imei);
                    if (connectStatus != null && connectStatus == BLE_STATUS_OF_CONNECT_SUCC) {
                        breakConnectImeiList.add(imei);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            doLostMobileWarning();
                        }
                    }
                    allDeviceMap.remove(imei);
                    imeiConnectStatusMap.put(imei, BLE_STATUS_OF_DISCONNECT);
                    connectStatusCallback(imei);

                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String deviceAddress = gatt.getDevice().getAddress();
                String imei = macImeiMap.get(deviceAddress);
                if (imei != null) {
                    if (bleNotifyCallback != null) {
                        bleNotifyCallback.onRssiCallback(imei, rssi);
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e("TFTBleService", "onServicesDiscovered");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String deviceAddress = gatt.getDevice().getAddress();
                BluetoothGattService service = gatt.getService(serviceId);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
                    if (characteristic != null) {
                        // 启用通知

                        String imei = macImeiMap.get(deviceAddress);
                        if (imei != null) {
                            imeiConnectStatusMap.put(imei, BLE_STATUS_OF_CONNECT_SUCC);
                            imeiBleClientMap.put(imei, gatt);
                            breakConnectImeiList.remove(imei);
                            if (breakConnectImeiList.size() == 0) {
                                isCurWarning = false;
                            }
                            setCharacteristicNotification(gatt, characteristic, true);
                        }
                    }
                }
            } else {
                // 处理服务发现失败
                if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.disconnect();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e("TFTBleService", "onDescriptorWrite");
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String deviceAddress = gatt.getDevice().getAddress();
                String imei = macImeiMap.get(deviceAddress);
                if (imei != null) {
                    antiLostConnConfig(imei, 1);
                    getAntiLostBleSearchMode(imei);
                }

            } else {
                if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.disconnect();
            }

        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            Log.e("TFTBleService", "onDescriptorRead");
            super.onDescriptorRead(gatt, descriptor, status, value);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.e("TFTBleService", "onReliableWriteCompleted");
            super.onReliableWriteCompleted(gatt, status);
        }

        private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
            if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


            }
            gatt.setCharacteristicNotification(characteristic, enabled);

            UUID descriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 通知描述符的UUID
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUUID);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                String deviceAddress = gatt.getDevice().getAddress();
                String imei = macImeiMap.get(deviceAddress);
                if (imei != null) {
                    imeiWriteObj.put(imei, characteristic);
                    saveConnectImeis();

                    connectStatusCallback(imei);
                }
                isConnectingBle = false;
                cancelDoWarning();

            }
        }
    };

    public static void requestBlePermission(Activity activity) {
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothadapter = bluetoothManager.getAdapter();
        if (bluetoothadapter == null) {
            Toast.makeText(activity, "bluetoothadapter is null", Toast.LENGTH_LONG).show();
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivity(enabler);
            } else {
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivity(enabler);
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                            return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    return;
                }
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_BLUETOOTH_CONNECT);
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                //判断是否具有权限
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_FINE_LOCATION);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_SCAN)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_ADVERTISE)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_ADVERTISE},
                            REQUEST_CODE_BLUETOOTH_ADVERTISE);
                }
            }

        }
    }

    private void setNotificationWarning(String title, String content) {
        this.notificationTitle = title;
        this.notificationContent = content;
    }

    public class LocalBinder extends Binder {
        public TFTBleService getService() {
            return TFTBleService.this;
        }
    }

    private Date enterDate;
    private NotificationCompat.Builder notificationBuilder;
    NotificationManager notificationManager;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver notificationCancelClickResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 在这里处理接收到的结果
            Log.e("YourActivity", "Notification cancel click");
            cancelDoWarning();
        }
    };

    private void cancelDoWarning() {
        isCurWarning = false;
        if (vibrator != null) {
            vibrator.cancel();
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }

    Vibrator vibrator;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
//        mClient = new BluetoothClient(this);
        initNotification();
        notificationContent = getResources().getString(R.string.lost_connect);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(notificationCancelClickResultReceiver, new IntentFilter(NotificationBroadcastReceiver.cancelClickAction));

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(bluetoothReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(bluetoothReceiver, filter);
        }

    }

    private boolean isInitNotification = false;

    private void initNotification() {
        if (isInitNotification) {
            return;
        }
        isInitNotification = true;
        createNotificationChannel();
        Notification notification = buildForegroundNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(REMAIN_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE); // 或 FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK 等，根据实际场景选择
        } else {
            startForeground(REMAIN_NOTIFICATION_ID, notification);
        }
    }

    private Notification buildForegroundNotification() {
//        Intent notificationIntent = new Intent(this, MainActivity.class); // 替换为你的Activity
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE |  PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setContentText("")
//                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // 在这里添加任何额外的通知配置

        return notificationBuilder.build();
    }


    public void initializeBluetooth() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        ScanSettings.Builder builder = new ScanSettings.Builder()
                //设置高功耗模式
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        //android 6.0添加设置回调类型、匹配模式等
        if (Build.VERSION.SDK_INT >= 23) {
            //定义回调类型
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
            builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);

        }

        if (Build.VERSION.SDK_INT >= 26) {
            builder.setLegacy(false);
        }
        //芯片组支持批处理芯片上的扫描
        if (mBluetoothAdapter.isOffloadedScanBatchingSupported()) {
            //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
            //设置为0以立即通知结果
            builder.setReportDelay(0L);
        }
        mScanSettings = builder.build();

        SharedPreferences editor = this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, this.MODE_PRIVATE);

        int soundWarning = editor.getInt("soundWarning", 0);
        int shockWarning = editor.getInt("shockWarning", 0);
        int notificationDuration = editor.getInt("notificationDuration", 0);
        int notificationCount = editor.getInt("notificationCount", 0);
        this.needSound = soundWarning == 1;
        this.needShock = shockWarning == 1;
        this.warningDuration = notificationDuration;
        this.warningCount = notificationCount;
        Thread workerThread = new Thread(checkBleSearchStatus);
        workerThread.start();
    }

    public void setMobileNotificationConfig(boolean needSound, boolean needShock, int notificationDuration, int notificationCount) {
        try {
            int soundWarning = needSound ? 1 : 0;
            int shockWarning = needShock ? 1 : 0;
            SharedPreferences.Editor editor = this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, this.MODE_PRIVATE).edit();
            editor.putInt("soundWarning", soundWarning);
            editor.putInt("shockWarning", shockWarning);
            editor.putInt("notificationDuration", notificationDuration);
            editor.putInt("notificationCount", notificationCount);
            editor.apply();
            this.needShock = needShock;
            this.needSound = needSound;
            this.warningCount = notificationCount;
            this.warningDuration = notificationDuration;
        } catch (Exception e) {

        }
    }

    private boolean isRunning = true;
    private Runnable checkBleSearchStatus = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    Date checkDate = lastRecvDate;
                    if (lastRecvDate == null) {
                        checkDate = enterDate;
                    }


                    if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    }
                    boolean isHadReadRssi = false;
                    for (String imei : imeiConnectStatusMap.keySet()) {
                        Integer status = imeiConnectStatusMap.get(imei);
                        if (status != null && status == BLE_STATUS_OF_CONNECT_SUCC) {
                            BluetoothGatt gatt = imeiBleClientMap.get(imei);
                            if (gatt != null)
                                gatt.readRemoteRssi();
                            isHadReadRssi = true;
                        }
                    }
                    if (isHadReadRssi) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    Date now = new Date();
                    ArrayList<String> removeImeiList = new ArrayList<>();
                    for (String imei :
                            disconnectImeiMap.keySet()) {
                        Date date = disconnectImeiMap.get(imei);
                        if (now.getTime() - date.getTime() > 10000) {
                            removeImeiList.add(imei);
                        }
                    }
                    for (String imei : removeImeiList) {
                        doActivelyDisconnectCb(imei);
                    }
                    for (String imei :
                            imeiConnectStatusMap.keySet()) {
                        int status = imeiConnectStatusMap.get(imei);
                        if (status == BLE_STATUS_OF_CONNECTING || status == BLE_STATUS_OF_DISCONNECT) {
                            isScanning = true;
                            isNeedConnectDevice = true;
                            break;
                        }
                    }
                    if (!isBleEnable) {
                        if (mBluetoothAdapter.isEnabled()) {
                            isBleEnable = true;
                            for (String imei :
                                    imeiConnectStatusMap.keySet()) {
                                connectStatusCallback(imei);
                            }
                            if (isConnectingBle || isScanning) {
                                try {
                                    if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                                    }
                                    mBLEScanner.stopScan(startScanCallback);
                                } catch (Exception e) {

                                }
                                try {
                                    Thread.sleep(2000);
                                    startScan();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                checkDate = now;
                            }
                        }
                    }
//            Log.e("BluetoothUtils","checkBleSearchStatus");

                    if (isScanning && checkDate != null && now.getTime() - checkDate.getTime() > 15000) {
                        //need restart ble search
                        lastRecvDate = now;
                        if (!mBluetoothAdapter.isEnabled()) {
                            isBleEnable = false;
                        } else {
                            isBleEnable = true;
                        }
                        for (String imei :
                                imeiConnectStatusMap.keySet()) {
                            connectStatusCallback(imei);
                        }
                        Log.e("BluetoothUtils", "restart search");
                        if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                        }
                        try {
                            mBLEScanner.stopScan(startScanCallback);
                        } catch (Exception e) {

                        }
                        try {
                            Thread.sleep(2000);
                            startScan();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        checkDate = now;
//                mBluetoothAdapter.startLeScan(startSearchCallback);

                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {

                }

            }

        }
    };

    public void stopScan() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
        isScanning = false;
        for (String imei :
                imeiConnectStatusMap.keySet()) {
            connectStatusCallback(imei);
        }

        mBLEScanner.stopScan(startScanCallback);
    }

    // Start scanning for BLE devices
    public void startScan() {

        isScanning = true;
        for (String imei :
                imeiConnectStatusMap.keySet()) {
            connectStatusCallback(imei);
        }
        if (enterDate == null) {
            enterDate = new Date();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            return;
        }
        if (mBLEScanner == null) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }


        mBLEScanner.startScan(null, mScanSettings, startScanCallback);
    }

    public void testScan() {
        if (isScanning) {
            stopScan();
        } else {
            startScan();
        }
    }

    private ArrayList<String> supportAntiLostModels = new ArrayList<String>() {{
        add(BleDeviceData.MODEL_KNIGHTX_100);
        add(BleDeviceData.MODEL_KNIGHTX_300);
        add(BleDeviceData.MODEL_V0V_X10);
    }};
    HashMap<String, BleDeviceData> allDeviceMap = new HashMap<>();
    private ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (ActivityCompat.checkSelfPermission(TFTBleService.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            }
            BluetoothDevice device = result.getDevice();

            int rssi = result.getRssi();
            byte[] scanRecord = result.getScanRecord().getBytes();
            String data = MyUtils.bytes2HexString(scanRecord, 0);
            if (result.getScanRecord() == null || result.getScanRecord().getDeviceName() == null || result.getScanRecord().getDeviceName().trim().length() == 0) {
                return;
            }
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
            lastRecvDate = new Date();
//            if(device.getAddress() != null && device.getAddress().contains("CD:5E")){
//                Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  "+ result.getScanRecord().getDeviceName() + "  " + device.getAddress() + "," + data);
//            }else{
//            }
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  "+ result.getScanRecord().getDeviceName() + "  " + device.getAddress() + "," + data);

            String id = device.getAddress().replaceAll(":", "").toUpperCase();
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress());
            lastRecvDate = new Date();
            HashMap<Byte, byte[]> rawDataList = BleDeviceData.parseRawData(scanRecord);
            if (rawDataList.containsKey((byte) 0xfe) || rawDataList.containsKey((byte) 0xfd)) {
                byte[] versionInfo = rawDataList.get((byte) 0xfe);
                byte[] imeiInfo = rawDataList.get((byte) 0xfd);
                String imei = null;
                if (imeiInfo != null) {
                    byte[] imeiByte = Arrays.copyOfRange(imeiInfo, 3, imeiInfo.length);
                    imei = new String(imeiByte);
                }
                String software = null;
                String hardware = null;
                String protocol = null;
                String model = null;
                if (versionInfo != null) {
                    byte protocolByte = versionInfo[2];
                    String versionStr = MyUtils.bytes2HexString(versionInfo, 3);
                    hardware = String.format("V%s.%s", versionStr.substring(0, 1), versionStr.substring(1, 2));
                    software = versionStr.substring(2, versionStr.length());
                    protocol = BleDeviceData.parseProtocol(protocolByte);
                    model = BleDeviceData.parseModel(protocolByte);
//                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + ";" + protocolByte);
                }

                if (allDeviceMap.containsKey(imei)) {
                    BleDeviceData bleDeviceData = allDeviceMap.get(imei);
                    bleDeviceData.setRssi(rssi + "dBm");
                    bleDeviceData.setSrcData(scanRecord);
                    bleDeviceData.setDate(new Date());
                    bleDeviceData.setId(id);
                    bleDeviceData.setBleDevice(device);
                    if (protocol != null) {
                        bleDeviceData.setProtocol(protocol);
                    }
                    if (hardware != null) {
                        bleDeviceData.setHardware(hardware);
                    }
                    if (software != null) {
                        bleDeviceData.setSoftware(software);
                    }
                    if (model != null) {
                        bleDeviceData.setModel(model);
                    }
                    if (imei != null) {
                        bleDeviceData.setImei(imei);
                    }
                    Log.e("BluetoothUtils", "MainActivity.onDeviceFounded " + bleDeviceData.getImei() + ";" + bleDeviceData.getDeviceName());
                    bleDeviceData.setDeviceName(device.getName());
                } else {
                    if (!supportAntiLostModels.contains(model)) {
                        return;
                    }
                    BleDeviceData bleDeviceData = new BleDeviceData();
                    bleDeviceData.setRssi(rssi + "dBm");
                    bleDeviceData.setSrcData(scanRecord);
                    bleDeviceData.setDate(new Date());
                    bleDeviceData.setMac(device.getAddress());
                    bleDeviceData.setId(id);
                    bleDeviceData.setBleDevice(device);
                    if (protocol != null) {
                        bleDeviceData.setProtocol(protocol);
                    }
                    if (imei != null) {
                        bleDeviceData.setImei(imei);
                    }
                    if (hardware != null) {
                        bleDeviceData.setHardware(hardware);
                    }
                    if (software != null) {
                        bleDeviceData.setSoftware(software);
                    }
                    if (model != null) {
                        bleDeviceData.setModel(model);
                    }
                    bleDeviceData.setDeviceName(device.getName());
                    Log.e("BluetoothUtils", "MainActivity.onDeviceFounded " + bleDeviceData.getImei() + ";" + bleDeviceData.getDeviceName() + ";" + bleDeviceData.getModel());
                    allDeviceMap.put(bleDeviceData.getImei(), bleDeviceData);
                }
                if (imeiConnectStatusMap.containsKey(imei)) {
                    Log.e("BluetoothUtils", "MainActivity.onDeviceFounded " + imei + " " + device.getName() + "  " + device.getAddress());
                    checkNeedConnect(imei);
                }
            }

        }
    };
    private boolean isNeedConnectDevice = false;


    // Connect to a specific Bluetooth device
    public void connect(String imei) {
        initNotification();
        if (!mBluetoothAdapter.isEnabled()) {
            isBleEnable = false;
        } else {
            isBleEnable = true;
        }

        if (!isBleEnable) {
            return;
        }
        imeiConnectStatusMap.put(imei, BLE_STATUS_OF_SCANNING);
        connectStatusCallback(imei);
        isNeedConnectDevice = true;
        checkNeedConnect(imei);
    }

    private void checkNeedConnect(String imei) {
        if (!isNeedConnectDevice) {
            return;
        }
        if (allDeviceMap.containsKey(imei)) {
            connectDeviceBle(imei);
        }
    }


    public void doActivelyDisconnectCb(String imei) {
        Log.e("BluetoothUtils", "doActivelyDisconnectCb:" + imei);
        disconnectImeiMap.remove(imei);
        isConnectingBle = false;
        isNeedConnectDevice = false;
        imeiConnectStatusMap.put(imei, BLE_STATUS_OF_DISCONNECT);
        connectStatusCallback(imei);
        imeiConnectStatusMap.remove(imei);
        allDeviceMap.remove(imei);
        if (imeiBleClientMap.containsKey(imei)) {
            BluetoothGatt bluetoothDevice = imeiBleClientMap.get(imei);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            }
            String address = bluetoothDevice.getDevice().getAddress();
            macImeiMap.remove(address);
            bluetoothDevice.disconnect();
            bluetoothDevice.close();
            imeiBleClientMap.remove(imei);
            imeiWriteObj.remove(imei);
        }
        saveConnectImeis();
    }

    private void saveConnectImeis() {
        SharedPreferences.Editor editor = TFTBleService.this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, TFTBleService.this.MODE_PRIVATE).edit();
        String imeis = "";
        for (String imei : imeiConnectStatusMap.keySet()) {
            imeis += imei + ";";
        }
        if (imeis.length() > 0) {
            imeis = imeis.substring(0, imeis.length() - 1);
            editor.putString("connectImei", imeis);
        } else {
            editor.remove("connectImei");
        }
        editor.apply();
    }

    private BleDeviceData connectBleDeviceData;
    //    boolean connectSucc = false;
    boolean isBleEnable = false;
    boolean isConnectingBle = false;
    boolean isWaitResponse = false;
    ;
    boolean isScanning = false;
    String mac = "";
    private int curRssi = 0;
    private int tryConnectCount = 5;
    private int curTryConnectCount = 0;
    private String confirmPwd = "654321";
    private boolean isSendMsgThreadRunning = true;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "Bluetooth is turned off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "Bluetooth is turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "Bluetooth is turned on");
                        for (String imei :
                                imeiConnectStatusMap.keySet()) {
                            int status = imeiConnectStatusMap.get(imei);
                            if (status == BLE_STATUS_OF_CONNECTING || status == BLE_STATUS_OF_DISCONNECT) {
                                isScanning = true;
                                isNeedConnectDevice = true;
                                break;
                            }
                        }
                        if (isScanning) {
                            startScan();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "Bluetooth is turning on");
                        break;
                }
            }
        }
    };

    private void connectDeviceBle(String imei) {
//        if (isConnectingBle) {
//            return;
//        }
        stopScan();
        isConnectingBle = true;
//        curTryConnectCount = 0;
        this.imeiConnectStatusMap.put(imei, BLE_STATUS_OF_CONNECTING);
        connectStatusCallback(imei);
        if (allDeviceMap.containsKey(imei)) {
            BleDeviceData bleDeviceData = allDeviceMap.get(imei);
            this.macImeiMap.put(bleDeviceData.getMac(), imei);
            BluetoothDevice bleDevice = bleDeviceData.getBleDevice();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


            }
            Log.e("TFTBleService", "connect device:" + imei);

            bleDevice.connectGatt(this, true, bluetoothGattCallback);

        }
    }


    private BleStatusCallback bleNotifyCallback;

    public void setCallback(BleStatusCallback callback) {
        bleNotifyCallback = callback;
    }

    private void notifyCallback(String imei, byte[] value) {
        if (bleNotifyCallback != null) {
            bleNotifyCallback.onNotifyValue(imei, value);
        }
    }

    private void connectStatusCallback(String imei) {
        if (bleNotifyCallback != null) {
            int connectStatus = getConnectStatus(imei);
            if (connectStatus == TFTBleService.BLE_STATUS_OF_CONNECT_SUCC) {
                updateConnectStatusInNotification(getResources().getString(R.string.connect_succ));
            } else if (connectStatus == TFTBleService.BLE_STATUS_OF_CLOSE) {
                updateConnectStatusInNotification(getResources().getString(R.string.ble_close));
            } else if (connectStatus == TFTBleService.BLE_STATUS_OF_DISCONNECT) {
                updateConnectStatusInNotification(getResources().getString(R.string.disconnected));
            } else if (connectStatus == TFTBleService.BLE_STATUS_OF_CONNECTING) {
                updateConnectStatusInNotification(getResources().getString(R.string.connecting));
            } else if (connectStatus == TFTBleService.BLE_STATUS_OF_SCANNING) {
                updateConnectStatusInNotification(getResources().getString(R.string.scanning));
            }
            bleNotifyCallback.onBleStatusCallback(imei, connectStatus);
        }
    }

    public int getConnectStatus(String imei) {
        if (!isBleEnable) {
            return BLE_STATUS_OF_CLOSE;
        }
        Integer status = imeiConnectStatusMap.get(imei);
        if (status == null) {
            return BLE_STATUS_OF_DISCONNECT;
        }
        if (status == BLE_STATUS_OF_CONNECT_SUCC || status == BLE_STATUS_OF_CONNECTING) {
            return status;
        }
        if (isScanning) {
            return BLE_STATUS_OF_SCANNING;
        }
        return BLE_STATUS_OF_DISCONNECT;
    }


    private void readData(String imei, int cmd, UUID curUUID) {
        Integer status = imeiConnectStatusMap.get(imei);
        if (status == null || status != BLE_STATUS_OF_CONNECT_SUCC) {
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd, cmd, null);

        if (imeiBleClientMap.containsKey(imei)) {
            BluetoothGatt gatt = imeiBleClientMap.get(imei);
            BluetoothGattCharacteristic characteristic = imeiWriteObj.get(imei);
            if (characteristic != null && gatt != null) {
                characteristic.setValue(content);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.writeCharacteristic(characteristic);
            }
        }

    }

    private void writeStrData(String imei, int cmd, String dataStr, UUID curUUID) {
        Integer status = imeiConnectStatusMap.get(imei);
        if (status == null || status != BLE_STATUS_OF_CONNECT_SUCC) {
            return;
        }
        byte[] data = dataStr.getBytes();
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd, cmd, data);
        Log.e("BluetoothUtils", "write:" + MyUtils.bytes2HexString(content, 0));
        if (content != null && content.length > 0) {

            if (imeiBleClientMap.containsKey(imei)) {
                BluetoothGatt gatt = imeiBleClientMap.get(imei);
                BluetoothGattCharacteristic characteristic = imeiWriteObj.get(imei);
                if (characteristic != null && gatt != null) {
                    characteristic.setValue(content);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


                    }
                    gatt.writeCharacteristic(characteristic);
                }
            }
        }

    }

    private void writeArrayData(String imei, int cmd, byte[] realContent, UUID curUUID) {
        Integer status = imeiConnectStatusMap.get(imei);
        if (status == null || status != BLE_STATUS_OF_CONNECT_SUCC) {
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd, cmd, realContent);
        if (content != null && content.length > 0) {
            Log.e("BluetoothUtils", "write:" + MyUtils.bytes2HexString(content, 0));

            if (imeiBleClientMap.containsKey(imei)) {
                BluetoothGatt gatt = imeiBleClientMap.get(imei);
                BluetoothGattCharacteristic characteristic = imeiWriteObj.get(imei);
                if (characteristic != null && gatt != null) {
                    characteristic.setValue(content);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                    }
                    gatt.writeCharacteristic(characteristic);
                }
            }
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After unbinding, clean up resources if appropriate.
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        if (mediaPlayer != null) {
            mediaPlayer.release(); // 服务销毁时释放资源
        }
        disconnectAll();
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        stopForeground(true);
    }

    public void disconnectAll() {
        isConnectingBle = false;
        for (String imei : imeiBleClientMap.keySet()) {
            Integer status = imeiConnectStatusMap.get(imei);
            if (status != null && status == BLE_STATUS_OF_CONNECT_SUCC) {
                doActivelyDisconnect(imei);
            }
            disconnectImeiMap.put(imei, new Date());
            imeiConnectStatusMap.remove(imei);
        }
        saveConnectImeis();
        cancelForegroundNotification();
    }

    public void disconnect(String imei) {
        isConnectingBle = false;
        Integer status = imeiConnectStatusMap.get(imei);
        if (status != null && status == BLE_STATUS_OF_CONNECT_SUCC) {
            doActivelyDisconnect(imei);
        }
        disconnectImeiMap.put(imei, new Date());
        imeiConnectStatusMap.remove(imei);
        saveConnectImeis();
        cancelForegroundNotification();
    }

    public void antiLostConnConfig(String imei, int open) {
        writeArrayData(imei, MyUtils.controlFunc.get("antiLostConn").get("write"), new byte[]{(byte) open}, uuid);
    }

    public void doActivelyDisconnect(String imei) {
        writeArrayData(imei, MyUtils.controlFunc.get("activelyDisconnect").get("write"), new byte[]{}, uuid);
    }

    public void setAntiLostBleStatus(String imei, int twoWayAntiLost, int singleVibrationDurationTime, int repeatTime) {
        writeArrayData(imei, MyUtils.controlFunc.get("configParam").get("write"), new byte[]{(byte) twoWayAntiLost, (byte) singleVibrationDurationTime, (byte) repeatTime}, uuid);
    }

    public void setAntiLostBleSearchMode(String imei, int searchMode) {
        writeArrayData(imei, MyUtils.controlFunc.get("searchMode").get("write"), new byte[]{(byte) searchMode}, uuid);
    }

    public void setAntiLostBleSilentMode(String imei, int silentMode) {
        writeArrayData(imei, MyUtils.controlFunc.get("silentMode").get("write"), new byte[]{(byte) silentMode}, uuid);
    }

    public void getAntiLostBleSearchMode(String imei) {
        writeArrayData(imei, MyUtils.controlFunc.get("searchMode").get("read"), new byte[]{}, uuid);
    }

    public void getAntiLostBleSilentMode(String imei) {
        writeArrayData(imei, MyUtils.controlFunc.get("silentMode").get("read"), new byte[]{}, uuid);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void checkAntiLostIfNeedWarning(int bleStatus) {
        SharedPreferences editor = this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, this.MODE_PRIVATE);
        int setConnectAntiLostBle = editor.getInt("setConnectAntiLostBle", 0);
        if (setConnectAntiLostBle == 0) {
            return;
        }
        int soundWarning = editor.getInt("soundWarning", 0);
        int shockWarning = editor.getInt("shockWarning", 0);
        int notificationDuration = editor.getInt("notificationDuration", 0);
        int notificationCount = editor.getInt("notificationCount", 0);
        long notificationStartTime = editor.getLong("notificationStartTime", 0);
        long notificationEndTime = editor.getLong("notificationEndTime", 0);
        boolean isNeedCheckValidTime = false;
        if (notificationEndTime != 0) {
            if (notificationStartTime != 0) {
                isNeedCheckValidTime = true;
            }
            if (notificationStartTime == 0 && notificationEndTime != 1439000) {
                isNeedCheckValidTime = true;
            }
            if (notificationEndTime <= notificationStartTime) {
                isNeedCheckValidTime = false;
            }
        }
        if (isNeedCheckValidTime) {
            long currentTimeMillis = LocalTime.now().toNanoOfDay() / 1_000_000; // 当前时间的毫秒数（转换为当天的时分秒毫秒数）
            LocalTime startTime = LocalTime.ofNanoOfDay(notificationStartTime * 1_000_000);
            LocalTime endTime = LocalTime.ofNanoOfDay(notificationEndTime * 1_000_000);
            LocalTime currentTime = LocalTime.ofNanoOfDay(currentTimeMillis * 1_000_000);
            if (startTime.isBefore(currentTime) || startTime.equals(currentTime)) {
                if (endTime.isAfter(currentTime) || endTime.equals(currentTime)) {
                    System.out.println("当前时间在指定的范围内");
                } else {
                    return;
                }
            } else {
                return;
            }
        }

    }


    public static final int BLE_STATUS_OF_CLOSE = -1;
    public static final int BLE_STATUS_OF_DISCONNECT = 0;
    public static final int BLE_STATUS_OF_CONNECTING = 1;
    public static final int BLE_STATUS_OF_CONNECT_SUCC = 2;
    public static final int BLE_STATUS_OF_SCANNING = 3;
    public static final String BLE_NOTIFY_VALUE = "ble_notify";


    private static final String CHANNEL_ID = "tft_ble_channel_id";
    private int REMAIN_NOTIFICATION_ID = 1;
    private int NOTIFICATION_ID = 2;
    public int soundRepeatCountTemp = 5;
    private int soundWarningDuration = 9;
    private static final int defaultSoundWarningDuration = 9;
    private int curWarningNotificationCount = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        scheduleNotificationRepeat();
        // 返回START_STICKY保证服务不会轻易被系统杀死
        return START_STICKY;
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Default Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false); // 可根据需求开启震动
            channel.setSound(null, null); // 使用默认声音，若要自定义声音则设置对应的Uri

            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void scheduleNotificationRepeat() {
        int repeatCount = warningCount;
        sendNotification();
        for (int i = 0; i < repeatCount; i++) {
            int delay =  (i + 1) * (warningDuration + 3) * 1000 ; // 转换为毫秒
            new Handler(Looper.getMainLooper()).postDelayed(this::sendNotification, delay);
        }
    }

    private MediaPlayer mediaPlayer;

    private void initMediaPlayer() {
        int soundResourceId = R.raw.laba;
        FileDescriptor fd = getResources().openRawResourceFd(soundResourceId).getFileDescriptor();
        mediaPlayer = new MediaPlayer();
        try {
//            mediaPlayer.setDataSource(fd);
            mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.laba));
            mediaPlayer.prepare(); // 预加载音频文件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void makeMobileWarning() {
        if (breakConnectImeiList.size() == 0) {
            return;
        }
        if (isCurWarning) {
            return;
        }
        if (!needSound && !needShock) {
            isCurWarning = true;
            scheduleNotificationRepeat();
            return;
        }
        isCurWarning = true;
        curWarningNotificationCount = 0;
        soundRepeatCountTemp = warningCount;
        if (warningDuration < defaultSoundWarningDuration) {
            soundWarningDuration = defaultSoundWarningDuration;
        } else {
            soundWarningDuration = warningDuration;
        }
        if (needSound) {
            playAudioAndSendNotification();
        } else {
            scheduleNotificationRepeat();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void doLostMobileWarning() {
        new Handler(Looper.getMainLooper()).postDelayed(this::makeMobileWarning, 7000);

    }

    private void mobileVibrator() {
        if (!needShock) {
            return;
        }
        if (vibrator != null && vibrator.hasVibrator()) { // 确保设备支持震动
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0及以上版本可以使用震动特征
                VibrationEffect effect = VibrationEffect.createOneShot(warningDuration * 1000, VibrationEffect.EFFECT_HEAVY_CLICK);
                vibrator.vibrate(effect);
            } else {
                // 之前的版本使用经典方法
                vibrator.vibrate(warningDuration * 1000); // 参数是震动持续时间，单位是毫秒
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playAudioAndSendNotification() {
        if (!isCurWarning) {
            return;
        }
        try {
            sendNotification();
            if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                initMediaPlayer();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> {
                    Log.e("Media", "reset");
                    mp.reset(); // 重置MediaPlayer以便下次播放

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("Media", "start delay");
        new Handler(Looper.getMainLooper()).postDelayed(this::playNextAudio, soundWarningDuration * 1000 + 3000);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playNextAudio() {
        // 根据需求判断是否继续播放，此处假设播放五次
        if (--soundRepeatCountTemp > 0) {
            playAudioAndSendNotification();

        } else {
            isCurWarning = false;
            Log.e("Media", "stop self");
            stopSelf(); // 所有音频播放完毕后，停止服务
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
        if (!isCurWarning) {
            return;
        }
        curWarningNotificationCount++;
        if (curWarningNotificationCount >= warningCount) {
            isCurWarning = false;
        }
        mobileVibrator();
        Log.e("TFTBLE SERVICE", "sendNotification");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// 创建自定义视图
        RemoteViews customContentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        customContentView.setTextViewText(R.id.notification_title, notificationTitle);
        customContentView.setTextViewText(R.id.notification_text, notificationContent);
// 设置通知的基本属性
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(customContentView)
                .setCustomBigContentView(customContentView) // 如果支持大通知
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

// 设置按钮的点击事件
        Intent buttonIntent = new Intent(this, NotificationBroadcastReceiver.class);
        buttonIntent.setAction("ACTION_BUTTON_CLICKED");
        PendingIntent buttonPendingIntent = PendingIntent.getBroadcast(this, 0, buttonIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        customContentView.setOnClickPendingIntent(R.id.notification_button, buttonPendingIntent);

// 发送通知
//        NOTIFICATION_ID = NOTIFICATION_ID+1;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                notificationContent,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(false); // 可根据需求开启震动
        channel.setSound(null, null); // 使用默认声音，若要自定义声音则设置对应的Uri

        notificationManager.createNotificationChannel(channel);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());


    }

    private void cancelForegroundNotification() {
        boolean isAllDisconnect = true;
        for (String imei : imeiConnectStatusMap.keySet()) {
            int status = imeiConnectStatusMap.get(imei);
            if (status == BLE_STATUS_OF_CONNECT_SUCC) {
                isAllDisconnect = false;
                break;
            }
        }
        if (isAllDisconnect) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_REMOVE);
            } else {
                stopForeground(true); // 移除通知
            }
            if (notificationManager != null) {
                notificationManager.cancel(REMAIN_NOTIFICATION_ID);
            }
            isInitNotification = false;
        }
    }

    private void updateConnectStatusInNotification(String connectStr) {
//        Intent notificationIntent = new Intent(this, MainActivity.class); // 替换为你的Activity
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(connectStr)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(contentIntent)
                .setAutoCancel(true); // 用户点击后自动取消
        builder.setSound(null);

        Notification notification = builder.build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

        }
        NotificationManagerCompat.from(this).notify(REMAIN_NOTIFICATION_ID, notification);
    }

    public String getCurConnectDeviceModel(){
        if(connectBleDeviceData != null){
            return connectBleDeviceData.getModel();
        }
        return "";
    }

    public void setDefaultDeviceWarningValue(String imei,int twoWayAntiLost){
        setAntiLostBleStatus(imei,twoWayAntiLost,defaultDeviceWarningDuration,defaultDeviceWarningCount);
    }

}
