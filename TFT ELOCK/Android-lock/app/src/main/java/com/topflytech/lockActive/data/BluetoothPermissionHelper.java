package com.topflytech.lockActive.data;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.topflytech.lockActive.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothPermissionHelper {
    private static final int REQUEST_ENABLE_BT = 10;
    private static final int REQUEST_OPEN_LOCATION_SERVICE = 6;
    private static final int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 100;
    private static final int REQUEST_CODE_LOCATION_PERMISSIONS = 101;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
    
    private Activity activity;
    private BluetoothPermissionCallback callback;
    private boolean isRequestingPermissions = false;

    public BluetoothPermissionHelper(Activity activity, BluetoothPermissionCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    /**
     * 启动蓝牙权限检查流程
     * 1. 检查蓝牙是否开启
     * 2. 检查并请求必要的权限
     * 3. 检查定位服务是否开启
     */
    public void startCheckBlePermission() {
        if (isRequestingPermissions) {
            return;
        }
        
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showErrorDialog(activity.getString(R.string.ble_not_support));
            return;
        }
        
        // 检查蓝牙是否开启
        if (!bluetoothAdapter.isEnabled()) {
            requestEnableBluetooth();
            return;
        }
        
        // 检查并请求权限
        checkAndRequestPermissions();
    }

    /**
     * 请求用户开启蓝牙
     */
    private void requestEnableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要 BLUETOOTH_CONNECT 权限才能请求开启蓝牙
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestBluetoothConnectPermission();
                return;
            }
        }
        
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /**
     * 请求 BLUETOOTH_CONNECT 权限（用于 Android 12+）
     */
    private void requestBluetoothConnectPermission() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_request_title)
                .setMessage(R.string.permission_bluetooth_connect_desc)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 检查并请求所有必要的权限
     */
    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要新的蓝牙权限
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        
        // 位置权限（所有 Android 版本都需要用于蓝牙扫描）
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        
        if (!permissionsToRequest.isEmpty()) {
            // 显示权限说明对话框
            showPermissionRationaleDialog(permissionsToRequest);
        } else {
            // 所有权限都已获得，检查定位服务
            checkLocationServiceAndStartBluetoothOperations();
        }
    }

    /**
     * 显示权限说明对话框
     */
    private void showPermissionRationaleDialog(List<String> permissions) {
        String message = buildPermissionRationaleMessage(permissions);
        
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_request_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    isRequestingPermissions = true;
                    ActivityCompat.requestPermissions(activity, 
                            permissions.toArray(new String[0]), 
                            REQUEST_CODE_BLUETOOTH_PERMISSIONS);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 构建权限说明文本
     */
    private String buildPermissionRationaleMessage(List<String> permissions) {
        StringBuilder sb = new StringBuilder();
        sb.append(activity.getString(R.string.permission_bluetooth_general_desc)).append("\n\n");
        
        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.BLUETOOTH_SCAN:
                    sb.append("• ").append(activity.getString(R.string.permission_bluetooth_scan_desc)).append("\n");
                    break;
                case Manifest.permission.BLUETOOTH_CONNECT:
                    sb.append("• ").append(activity.getString(R.string.permission_bluetooth_connect_desc)).append("\n");
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                case Manifest.permission.ACCESS_COARSE_LOCATION:
                    sb.append("• ").append(activity.getString(R.string.permission_location_desc)).append("\n");
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 检查定位服务并启动蓝牙操作
     */
    private void checkLocationServiceAndStartBluetoothOperations() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsEnabled || isNetworkEnabled) {
            // 定位服务已开启，执行蓝牙操作
            startBluetoothOperations();
        } else {
            // 提示用户开启定位服务
            showLocationServiceDialog();
        }
    }

    /**
     * 显示定位服务开启提示对话框
     */
    private void showLocationServiceDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.location_service_title)
                .setMessage(R.string.location_service_desc)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivityForResult(intent, REQUEST_OPEN_LOCATION_SERVICE);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 启动蓝牙操作
     */
    private void startBluetoothOperations() {
        isRequestingPermissions = false;
        callback.onPermissionsGranted();
    }

    /**
     * 显示错误对话框
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /**
     * 显示权限被拒绝的引导对话框
     */
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_denied_title)
                .setMessage(R.string.permission_denied_desc)
                .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                    // 跳转到应用设置页面
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    callback.onPermissionsDenied();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 处理权限请求结果
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        isRequestingPermissions = false;
        
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS || 
            requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                
                if (allGranted) {
                    // 所有权限被授予，继续检查流程
                    startCheckBlePermission();
                } else {
                    // 有权限被拒绝
                    if (shouldShowPermissionRationale()) {
                        // 可以再次显示说明并请求
                        showPermissionDeniedDialog();
                    } else {
                        // 用户选择了"不再询问"
                        showPermissionDeniedDialog();
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndRequestPermissions();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    /**
     * 检查是否应该显示权限说明
     */
    private boolean shouldShowPermissionRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN) ||
                   ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT);
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * 处理 Activity 结果
     */
    public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // 蓝牙已开启，继续检查权限
                checkAndRequestPermissions();
            } else {
                // 用户拒绝开启蓝牙
                callback.onPermissionsDenied();
            }
        } else if (requestCode == REQUEST_OPEN_LOCATION_SERVICE) {
            // 从定位服务设置返回，重新检查
            checkLocationServiceAndStartBluetoothOperations();
        }
    }

    /**
     * 蓝牙权限回调接口
     */
    public interface BluetoothPermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
        void onLocationStart();
    }
}
