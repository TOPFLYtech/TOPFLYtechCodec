package com.topflytech.tftble.data;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BluetoothPermissionHelper {
    private static final int REQUEST_ENABLE_BT = 10;
    private static final int REQUEST_OPEN_LOCATION_SERVICE = 6;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_ADVERTISE = 4;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
    private static final int REQUEST_CODE_BLUETOOTH = 6;
    private Activity activity;
    private BluetoothPermissionCallback callback;

    public BluetoothPermissionHelper(Activity activity, BluetoothPermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {// || ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED

//                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
//                            Manifest.permission.BLUETOOTH)) {
////                        Toast.makeText(context,"Need to open ")
//                    }
//                    ActivityCompat.requestPermissions(activity,
//                            new String[]{Manifest.permission.BLUETOOTH},
//                            REQUEST_CODE_BLUETOOTH);
//                } else
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_SCAN)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_SCAN);
                } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }

                else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_FINE_LOCATION);
                } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                } else {
                    // 已经获得权限，执行蓝牙操作
                    checkLocationServiceAndStartBluetoothOperations();
                }
            } else {
                // 已经获得权限，执行蓝牙操作
                checkLocationServiceAndStartBluetoothOperations();
            }
        }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 到 Android 11
            if (   ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_FINE_LOCATION);
                } else if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                } else {
                    // 已经获得权限，执行蓝牙操作
                    checkLocationServiceAndStartBluetoothOperations();
                }
            }else {
                // 已经获得权限，执行蓝牙操作
                checkLocationServiceAndStartBluetoothOperations();
            }

        }else {
            // Android 5.1 及以下，不需要权限，直接执行蓝牙操作
            checkLocationServiceAndStartBluetoothOperations();
        }
    }

    public void startCheckBlePermission(){
        BluetoothAdapter bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothadapter != null && !bluetoothadapter.isEnabled()) {
            // 提示用户打开蓝牙

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
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

        } else {
            // 检查并请求蓝牙权限
            checkAndRequestPermissions();
        }
    }


    private void checkLocationServiceAndStartBluetoothOperations() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsEnabled || isNetworkEnabled) {
            // 定位服务已开启，执行蓝牙操作
            startBluetoothOperations();
        } else {
            // 提示用户开启定位服务
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivityForResult(intent, REQUEST_OPEN_LOCATION_SERVICE);

        }
    }

    private void startBluetoothOperations() {
        callback.onPermissionsGranted();
    }



    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，执行蓝牙操作
                checkAndRequestPermissions();
            } else {
                // 权限被拒绝，提示用户
                // 在此处处理权限被拒绝的情况
            }
        } else if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，执行蓝牙操作
                checkAndRequestPermissions();
            } else {
                // 权限被拒绝，提示用户
                // 在此处处理权限被拒绝的情况
            }
        } else if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  ) {
                // 权限被授予，执行蓝牙操作
                BluetoothAdapter bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothadapter != null && !bluetoothadapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                checkAndRequestPermissions();
            } else {
                // 权限被拒绝，提示用户
                // 在此处处理权限被拒绝的情况
            }

        }
        else if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  ) {
                // 权限被授予，执行蓝牙操作
                checkAndRequestPermissions();
            } else {
                // 权限被拒绝，提示用户
                // 在此处处理权限被拒绝的情况
            }
        }else if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                // 权限被授予，执行蓝牙操作
                checkAndRequestPermissions();
            } else {
                // 权限被拒绝，提示用户
                // 在此处处理权限被拒绝的情况
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
            
                checkAndRequestPermissions();
            } else {
                // 用户拒绝 
                // 在此处处理蓝牙未启用的情况
            }
        } else if (requestCode == REQUEST_OPEN_LOCATION_SERVICE) {
            if (resultCode == Activity.RESULT_OK) {
            
//                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//
//                }
//                mBluetoothAdapter.stopLeScan(startSearchCallback);
//                mBluetoothAdapter.startLeScan(startSearchCallback);
                callback.onLocationStart();
            } else {
                // 用户拒绝 
                // 在此处处理蓝牙未启用的情况
            }

        }
    }

    public interface BluetoothPermissionCallback {
        void onPermissionsGranted();
        void onLocationStart();
    }
}
