package com.topflytech.bleAntiLost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.bleAntiLost.data.BleDeviceData;
import com.topflytech.bleAntiLost.data.BleStatusCallback;
import com.topflytech.bleAntiLost.data.MyUtils;
import com.topflytech.bleAntiLost.data.PositiveIntegerFilter;
import com.topflytech.bleAntiLost.data.TFTBleService;
import com.topflytech.bleAntiLost.view.SwitchButton;


import org.opencv.OpenCV;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity  implements BleStatusCallback {
    //    BluetoothClient mClient;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_ADVERTISE = 4;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String serviceId = "27760001-999C-4D6A-9FC4-C7272BE10900";
    private String uuid = "27763561-999C-4D6A-9FC4-C7272BE10900";
    private ActionBar actionBar;
    private ImageView refreshButton;
    private ImageView fuzzySearchBtn;
    private ImageButton scanBtn;
    private ImageView imgDevice,imgWarningTips;
    private TextView tvConnectStatus,tvSignal,tvConnectLog;//tvTwoWayNotification;
    private EditText etImei,etNotificationDuration,etNotificationCount,etMobileNotificationDuration,etMobileNotificationCount;
    private String connectImei;
    private Switch sbBleConnect,sbTwoWayAntiLost,sbSearchMode;
    private CheckBox cbSound,cbShock;
    private Button btnConfirm;
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    private static final int REQUEST_CODE_NOTIFICATIONS = 100;
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATIONS);
        }
    }
    private void initUIControl(){
        tvConnectStatus = (TextView) findViewById(R.id.tv_connect_status);
        tvSignal = (TextView) findViewById(R.id.tv_signal);
        tvConnectLog = (TextView) findViewById(R.id.tv_connect_log);
        tvConnectLog.setVisibility(View.INVISIBLE);
//        tvTwoWayNotification = (TextView) findViewById(R.id.tv_two_way_notification);
        imgDevice = (ImageView)findViewById(R.id.img_device);
        imgWarningTips = (ImageView)findViewById(R.id.img_warning_tips);
        imgWarningTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,R.string.mobile_warning_set_tips,Toast.LENGTH_LONG).show();
//                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
//                confirmRelayDlg.setTitleText(getResources().getString(R.string.mobile_warning_set_tips));
//                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
//                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        sweetAlertDialog.hide();
//                    }
//                });
//                confirmRelayDlg.show();
            }
        });
        etImei = (EditText) findViewById(R.id.et_imei);
        etImei.setFilters(new InputFilter[]{new PositiveIntegerFilter()});

        etNotificationDuration = (EditText) findViewById(R.id.et_notification_duration);
        etNotificationCount = (EditText) findViewById(R.id.et_notification_count);
        etMobileNotificationDuration = (EditText) findViewById(R.id.et_mobile_notification_duration);
        etMobileNotificationCount = (EditText) findViewById(R.id.et_mobile_notification_count);
        etNotificationDuration.setFilters(new InputFilter[]{new PositiveIntegerFilter()});
        etNotificationCount.setFilters(new InputFilter[]{new PositiveIntegerFilter()});
        etMobileNotificationDuration.setFilters(new InputFilter[]{new PositiveIntegerFilter()});
        etNotificationDuration.setFilters(new InputFilter[]{new PositiveIntegerFilter()});


        sbBleConnect = (Switch) findViewById(R.id.switch_ble_connect);
        sbBleConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("MainActivity",sbBleConnect.isChecked() ? "1":"0");
                if(sbBleConnect.isChecked()){
                    String imei = etImei.getText().toString();
                    if(imei.isEmpty() || imei.trim().length() != 15){
                        Toast.makeText(MainActivity.this,R.string.imei_check_error,Toast.LENGTH_SHORT).show();
                        sbBleConnect.setChecked(!sbBleConnect.isChecked());
                        return;
                    }
                    connectImei = imei.trim();
                    // try to connect
                    if(tftBleService != null && tftBleService.getConnectStatus(connectImei) == TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
                        tftBleService.disconnectAll();
                        tftBleService.stopScan();
                        return;
                    }

                    SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, MainActivity.this.MODE_PRIVATE).edit();
                    editor.putString("enterImei",imei);
                    editor.apply();
                    connectDevice(imei);
                }else{
                    tftBleService.disconnectAll();
                    tftBleService.stopScan();
                }

            }
        });

//        sbBleConnect.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
//            @Override
//            public void onSwitchChanged(boolean open) {
//                if(tftBleService != null && tftBleService.getConnectStatus() == TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
//                    tftBleService.disConnect();
//                    tftBleService.stopScan();
//                    return;
//                }
//                String imei = etImei.getText().toString();
//                if(imei.isEmpty() || imei.trim().length() != 15){
//                    Toast.makeText(MainActivity.this,R.string.imei_check_error,Toast.LENGTH_SHORT).show();
//                    sbBleConnect.setSwitchStatus(!sbBleConnect.getSwitchStatus());
//                    return;
//                }
//                SharedPreferences.Editor editor = MainActivity.this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, MainActivity.this.MODE_PRIVATE).edit();
//                editor.putString("enterImei",imei);
//                editor.apply();
//                connectDevice(imei);
//            }
//        });
        sbTwoWayAntiLost = (Switch) findViewById(R.id.switch_two_way_anti_lost);
        sbTwoWayAntiLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String notificationDuration = etNotificationDuration.getText().toString();
                if(notificationDuration.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.notification_duration_check,Toast.LENGTH_SHORT).show();
                    sbTwoWayAntiLost.setChecked(!sbTwoWayAntiLost.isChecked());
                    return;
                }
                String notificationCount = etNotificationCount.getText().toString();
                if(notificationCount.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.notification_count_check,Toast.LENGTH_SHORT).show();
                    sbTwoWayAntiLost.setChecked(!sbTwoWayAntiLost.isChecked());
                    return;
                }
                if(tftBleService != null){
                    if(tftBleService.getConnectStatus(connectImei) != TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
                        Toast.makeText(MainActivity.this,R.string.need_connect_ble_first,Toast.LENGTH_SHORT).show();
                        sbTwoWayAntiLost.setChecked(!sbTwoWayAntiLost.isChecked());
                        return;
                    }
                    try {
                        int notificationDurationInt = Integer.valueOf(notificationDuration);
                        int notificationCountInt = Integer.valueOf(notificationCount);
                        int twoWayAntiLostInt = sbTwoWayAntiLost.isChecked() ? 1 : 0;
                        tftBleService.setAntiLostBleStatus(connectImei,twoWayAntiLostInt,notificationDurationInt,notificationCountInt);
                    }catch (Exception ex){

                    }
                }
            }
        });
        sbSearchMode = (Switch) findViewById(R.id.switch_search_mode);
        sbSearchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tftBleService != null){
                    if(tftBleService.getConnectStatus(connectImei) != TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
                        sbSearchMode.setChecked(!sbSearchMode.isChecked());
                        Toast.makeText(MainActivity.this,R.string.need_connect_ble_first,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tftBleService.setAntiLostBleSearchMode(connectImei,sbSearchMode.isChecked() ? 1 : 0);
                }
            }
        });

        cbSound= (CheckBox) findViewById(R.id.cb_sound);
        cbShock= (CheckBox) findViewById(R.id.cb_shock);

        btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(tftBleService != null){
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        tftBleService.doLostMobileWarning();
//                    }
//                }
                String notificationDuration = etNotificationDuration.getText().toString();
                if(notificationDuration.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.notification_duration_check,Toast.LENGTH_SHORT).show();
                    return;
                }
                String notificationCount = etNotificationCount.getText().toString();
                if(notificationCount.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.notification_count_check,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tftBleService != null){
                    if(tftBleService.getConnectStatus(connectImei) != TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
                        Toast.makeText(MainActivity.this,R.string.need_connect_ble_first,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        int notificationDurationInt = Integer.valueOf(notificationDuration);
                        int notificationCountInt = Integer.valueOf(notificationCount);
                        if(notificationCountInt == 0 || notificationDurationInt == 0 || notificationCountInt > 255 || notificationDurationInt >255){
                            Toast.makeText(MainActivity.this,R.string.device_invalid_set_value_warning,Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int twoWayAntiLostInt = sbTwoWayAntiLost.isChecked() ? 1 : 0;
                        tftBleService.setAntiLostBleStatus(connectImei,twoWayAntiLostInt,notificationDurationInt,notificationCountInt);
                    }catch (Exception ex){

                    }
                }
                notificationDuration = etMobileNotificationDuration.getText().toString();
                if(notificationDuration.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.mobile_notification_duration_check,Toast.LENGTH_SHORT).show();
                    sbTwoWayAntiLost.setChecked(!sbTwoWayAntiLost.isChecked());
                    return;
                }
                notificationCount = etMobileNotificationCount.getText().toString();
                if(notificationCount.isEmpty()){
                    Toast.makeText(MainActivity.this,R.string.mobile_notification_count_check,Toast.LENGTH_SHORT).show();
                    sbTwoWayAntiLost.setChecked(!sbTwoWayAntiLost.isChecked());
                    return;
                }
                try{
                    int notificationDurationInt = Integer.valueOf(notificationDuration);
                    int notificationCountInt = Integer.valueOf(notificationCount);
                    if(notificationCountInt == 0 || notificationDurationInt == 0 || notificationCountInt > 255 || notificationDurationInt >255){
                        Toast.makeText(MainActivity.this,R.string.mobile_invalid_set_value_warning,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(tftBleService != null){
                        tftBleService.setMobileNotificationConfig(cbSound.isChecked(),cbShock.isChecked(),notificationDurationInt,notificationCountInt);
                    }
                    Toast.makeText(MainActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
                }catch (Exception e){

                }

            }
        });
        sbBleConnect.setChecked(false);
//        sbBleConnect.setSwitchStatus(false);
        sbTwoWayAntiLost.setChecked(false);
        sbSearchMode.setChecked(false);
        SharedPreferences editor = this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, this.MODE_PRIVATE);
        int soundWarning = editor.getInt("soundWarning",0);
        int shockWarning = editor.getInt("shockWarning",0);
        int notificationDuration = editor.getInt("notificationDuration",0);
        int notificationCount = editor.getInt("notificationCount",0);
        String enterImei = editor.getString("enterImei","");
        etImei.setText(enterImei);
        etImei.setText("869487060198073");
        connectImei = etImei.getText().toString();
        cbShock.setChecked(shockWarning == 1);
        cbSound.setChecked(soundWarning == 1);
        etMobileNotificationCount.setText(String.valueOf(notificationCount));
        etMobileNotificationDuration.setText(String.valueOf(notificationDuration));

        scanBtn = (ImageButton) findViewById(R.id.btn_scan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeChatQRCodeActivity.class);
                startActivityForResult(intent, REQUEST_SCAN_QRCODE);
            }
        });
    }

    private void connectDevice(String imei) {
        if(tftBleService != null){
            tvConnectLog.setText(R.string.connecting);
            tftBleService.connect(imei);
            if(tftBleService.getConnectStatus(imei) == TFTBleService.BLE_STATUS_OF_CONNECTING){
                return;
            }
            if(tftBleService.getConnectStatus(imei) != TFTBleService.BLE_STATUS_OF_SCANNING){
                tftBleService.startScan();
            }
        }
    }

    private TFTBleService tftBleService;
    private boolean mIsBound = false;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TFTBleService.LocalBinder binder = (TFTBleService.LocalBinder) service;
            tftBleService = binder.getService();
            mIsBound = true;
            tftBleService.setCallback(MainActivity.this); // 设置回调
            tftBleService.initializeBluetooth();
            SharedPreferences editor = MainActivity.this.getSharedPreferences(TFTBleService.BLE_NOTIFY_VALUE, MainActivity.this.MODE_PRIVATE);
            String connectImeis = editor.getString("connectImei","");
            if(connectImeis != null && !connectImeis.isEmpty()){
                String[] imeiItems = connectImeis.split(";");
                etImei.setText(imeiItems[0]);
                connectDevice(imeiItems[0]);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tftBleService = null;
            mIsBound = false;
        }
    };

    SweetAlertDialog waitingCancelDlg;

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private boolean isFuzzySearchStatus = false, isOnView = false;
    private String fuzzyKey = "";
    private ArrayList<ArrayList<byte[]>> orignHistoryList = new ArrayList<ArrayList<byte[]>>();

    public static final int REQUEST_SCAN_QRCODE = 1;
    public static final int RESPONSE_SCAN_QRCODE = 1;
    private Date dataNotifyDate;
    private Date enterDate;

    private int clickCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化OpenCV
        OpenCV.initAsync(MainActivity.this);
        if (!hasNotificationPermission()) {
            requestNotificationPermission();
        }
        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(MainActivity.this);

        CrashReport.initCrashReport(getApplicationContext());
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.main_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount >= 10) {
                    MyUtils.isDebug = true;
                    // 停止点击事件
                    customView.setOnClickListener(null);
                }else if(clickCount > 6){
                    Toast.makeText(MainActivity.this,"再点击" + (10 - clickCount) + "次，打开Debug功能",Toast.LENGTH_SHORT).show();
                }

                if(clickCount == 1){
                    // 在 20 秒后检查点击次数
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!MyUtils.isDebug) {
                                // 20 秒内未达到点击次数要求
                                // 进行相应的处理逻辑
                                clickCount = 0;
                            }
                        }
                    }, 20000); // 20 秒
                }
            }
        });


        enterDate = new Date();
        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothadapter = bluetoothManager.getAdapter();
        if (bluetoothadapter == null) {
            SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
            confirmRelayDlg.setTitleText(getResources().getString(R.string.ble_not_support));
            confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
            confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                }
            });
            confirmRelayDlg.show();
        } else {

            if (bluetoothadapter.isEnabled()) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        //判断是否需要向用户解释为什么需要申请该权限
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                        }
                        //请求权限
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_CODE_BLUETOOTH_CONNECT);
                    }
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                } else {
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                }

            }



            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                //判断是否具有权限
                boolean hadReqeustLocationPermission = false;
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    hadReqeustLocationPermission  = true;
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && !hadReqeustLocationPermission) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_FINE_LOCATION);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_SCAN)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_ADVERTISE)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_ADVERTISE},
                            REQUEST_CODE_BLUETOOTH_ADVERTISE);
                }
            }
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
            if (bluetoothadapter.isOffloadedScanBatchingSupported()) {
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                //设置为0以立即通知结果
                builder.setReportDelay(0L);
            }



        }
        Intent intent = new Intent(this, TFTBleService.class);
        this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        initUIControl();
    }

    @Override
    protected void onDestroy() {
        tftBleService.disconnectAll();
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    SweetAlertDialog waitingDlg;
    private void showWaitingDlg(String warning){

        waitingDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
            waitingDlg.show();
        }
    }

    private void showWaitingCancelDlg(String warning) {
        waitingCancelDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingCancelDlg.showCancelButton(true);
        waitingCancelDlg.setTitleText("");
        waitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
        waitingCancelDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.hide();
            }
        });
        if (warning != null && !warning.isEmpty()) {
            waitingCancelDlg.setTitleText(warning);
        }
        waitingCancelDlg.show();
    }


    public void reStartApp() {
        try {
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(MainActivity.this.getBaseContext().getPackageName());
            intent.putExtra("REBOOT", "reboot");
            PendingIntent restartIntent = PendingIntent.getActivity(MainActivity.this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION || requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            Log.e("permission","permission:" + requestCode);
            SweetAlertDialog restartWarning = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
            restartWarning.setTitleText("");
            restartWarning.setContentText(getResources().getString(R.string.restart_app_warning));
            restartWarning.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                    reStartApp();
                }
            });
            restartWarning.show();
        } else if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT || requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothadapter = bluetoothManager.getAdapter();
            if (bluetoothadapter != null) {
                if (bluetoothadapter.isEnabled()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                            return;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            return;
                        }
                    }

//                        mBluetoothAdapter.startLeScan(startSearchCallback);

//                    showWaitingCancelDlg("");
                } else {
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                }
            }

        }else if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("NotificationPermission", "Notification permission granted");
            } else {
                Log.e("NotificationPermission", "Notification permission denied");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_QRCODE && resultCode == RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            etImei.setText(value);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        isOnView = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnView = false;
    }


    @Override
    public void onNotifyValue(String imei,byte[] value) {
        if(!imei.equals(connectImei)){
            return;
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value.length > 1) {

                    int status = value[0];
                    int type = value[1] & 0xff;
                    if (status == 0) {
                        if(type == MyUtils.controlFunc.get("antiLostConn").get("read")
                                || type == MyUtils.controlFunc.get("antiLostConn").get("write")
                                ||type == MyUtils.controlFunc.get("configParam").get("read")
                                || type == MyUtils.controlFunc.get("configParam").get("write")){
                            if(value.length >=5){
                                int twoWayAntiLost = value[2] & 0xff;
                                int singleVibrationDurationTime = value[3] & 0xff;
                                int repeatTime = value[4] & 0xff;
                                etNotificationCount.setText(String.valueOf(repeatTime));
                                etNotificationDuration.setText(String.valueOf(singleVibrationDurationTime));
                                sbTwoWayAntiLost.setChecked(twoWayAntiLost == 1);
                                if(singleVibrationDurationTime == 0 && repeatTime == 0){
                                    tftBleService.setDefaultDeviceWarningValue(imei,twoWayAntiLost);
                                }
                            }else{
                                tftBleService.setDefaultDeviceWarningValue(imei,0);
                            }
                        } else if(type == MyUtils.controlFunc.get("searchMode").get("read")
                                || type == MyUtils.controlFunc.get("searchMode").get("write")){
                            int mode = value[2] & 0xff;
                            sbSearchMode.setChecked(mode == 1);
                        }else if(type == MyUtils.controlFunc.get("silentMode").get("read")
                                || type == MyUtils.controlFunc.get("silentMode").get("write")){
                            int mode = value[2] & 0xff;
                        }else if( type == MyUtils.controlFunc.get("activelyDisconnect").get("write")){
                            tftBleService.doActivelyDisconnectCb(imei);
                        }
                    } else if (status == 1) {
                        //password_is_error
                        Toast.makeText(MainActivity.this,R.string.password_is_error,Toast.LENGTH_SHORT).show();
                    } else {
                        //error_please_try_again
                        Toast.makeText(MainActivity.this,R.string.error_please_try_again,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    public void onBleStatusCallback(String imei,int connectStatus) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!imei.equals(connectImei)){
                    return;
                }
                if(connectStatus == TFTBleService.BLE_STATUS_OF_CONNECT_SUCC){
                    Log.e("MainActivity","ble connect succ");
                    sbBleConnect.setChecked(true);
                    tvConnectLog.setText("");
                    String model = tftBleService.getCurConnectDeviceModel();
                    if(model.equals(BleDeviceData.MODEL_V0V_X10)){
                        imgDevice.setImageResource(R.mipmap.device_vovx10);
                    }else{
                        imgDevice.setImageResource(R.mipmap.device_k100);
                    }
                    tvConnectStatus.setText(R.string.connect_succ);
                    etImei.setText(imei);
                }else if(connectStatus == TFTBleService.BLE_STATUS_OF_CLOSE){
                    sbBleConnect.setChecked(false);
                    tvConnectLog.setText("");
                    tvConnectStatus.setText(R.string.ble_close);
                }else if(connectStatus == TFTBleService.BLE_STATUS_OF_DISCONNECT){
//                    Log.e("MainActivity","ble disconnect");
                    sbBleConnect.setChecked(false);
                    tvConnectLog.setText("");
                    tvConnectStatus.setText(R.string.disconnected);
                }else if(connectStatus == TFTBleService.BLE_STATUS_OF_CONNECTING){
                    sbBleConnect.setChecked(true);
                    tvConnectStatus.setText(R.string.connecting);
                }else if(connectStatus == TFTBleService.BLE_STATUS_OF_SCANNING){
                    sbBleConnect.setChecked(true);
                    tvConnectStatus.setText(R.string.scanning);
                }
            }
        });

    }

    @Override
    public void onRssiCallback(String imei,int rssi) {
        if(!imei.equals(connectImei)){
            return;
        }
        tvSignal.setText(rssi + " dBm");
    }
}
