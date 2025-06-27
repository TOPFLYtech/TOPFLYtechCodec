package com.topflytech.lockActive;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.topflytech.lockActive.Ble.A001SoftwareUpgradeManager;
import com.topflytech.lockActive.Ble.TftBleConnectManager;
import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BleRespData;
import com.topflytech.lockActive.data.DownloadFileManager;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.OpenAPI;
import com.topflytech.lockActive.data.UniqueIDTool;
import com.topflytech.lockActive.data.Utils;
import com.topflytech.lockActive.deviceConfigSetting.AccessPwdChangeActivity;
import com.topflytech.lockActive.deviceConfigSetting.AlarmOpenSetActivity;
import com.topflytech.lockActive.deviceConfigSetting.IpEditActivity;
import com.topflytech.lockActive.deviceConfigSetting.LockPwdChangeActivity;
import com.topflytech.lockActive.deviceConfigSetting.RFIDActivity;
import com.topflytech.lockActive.deviceConfigSetting.SingleClickListener;
import com.topflytech.lockActive.deviceConfigSetting.SubLockIdActivity;
import com.topflytech.lockActive.deviceConfigSetting.TempAlarmSetActivity;
import com.topflytech.lockActive.deviceConfigSetting.TimerActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditActivity extends AppCompatActivity {
    public static boolean isDebug = false;
    public static boolean isOnlyActiveNetwork = false;

    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    boolean onThisView = false;
    private String mac;
    private String id;
    private String deviceName;
    SweetAlertDialog waitingDlg,warningDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;
    SweetAlertDialog sweetBlePwdDlg;


    private ImageView imgLockStatus;
    private TextView tvLockStatus;
    private Button btnOpenLock;
    private Button btnLock;
    private Button btnActiveNetwork;
    private TextView tvLog;
    private TextView tvLockRefreshTime;
    private TextView tvUniqueIdTv;

    private LinearLayout lnShowLog;
    private LinearLayout lnLock;
    private LinearLayout lnUnlock;
    private LinearLayout lnActiveNetwork;
    private Button btnClearLog;
    private Button btnRefreshStatus;
    private String uniqueID;
    private String serverVersion = null;
    private String betaServerVersion = null;
    private String upgradeLink;
    private String betaUpgradeLink;
    private String software;
    private String deviceId;
    private String imei;


    private String blePwd = "";
    private List<Integer> parentLockInitViewFuncs = new ArrayList<Integer>(){{
        add(BleDeviceData.func_id_of_timer);
        add(BleDeviceData.func_id_of_ip1);
        add(BleDeviceData.func_id_of_ip2);
        add(BleDeviceData.func_id_of_apn_addr);
        add(BleDeviceData.func_id_of_apn_username);
        add(BleDeviceData.func_id_of_apn_pwd);
    }};
    private List<Integer> subLockInitViewFuncs = new ArrayList<Integer>(){{
        add(BleDeviceData.func_id_of_sub_lock_version);
        add(BleDeviceData.func_id_of_sub_lock_boot_version);
        add(BleDeviceData.func_id_of_sub_lock_device_name);
        add(BleDeviceData.func_id_of_sub_lock_broadcast_interval);
        add(BleDeviceData.func_id_of_sub_lock_long_range);
        add(BleDeviceData.func_id_of_sub_lock_ble_transmitted_power);
        add(BleDeviceData.func_id_of_sub_lock_led);
        add(BleDeviceData.func_id_of_sub_lock_buzzer);
        add(BleDeviceData.func_id_of_sub_lock_device_id);
        add(BleDeviceData.func_id_of_sub_lock_alarm_open_set);
        add(BleDeviceData.func_id_of_sub_lock_temp_alarm_set);

    }};

    private String model = "";

    private int accOnValue = -1,angleValue = -1,distanceValue = -1;
    private long accOffValue = -1;

    public static final int REQUEST_CHANGE_UNCLOCK_PWD = 1;
    public static final int RESPONSE_CHANGE_UNCLOCK_PWD = 1;
    public static final int REQUEST_CHANGE_TIMER = 2;
    public static final int RESPONSE_CHANGE_TIMER = 2;
    public static final int REQUEST_CHANGE_IP = 3;
    public static final int RESPONSE_CHANGE_IP = 3;
    public static final int REQUEST_CHANGE_ACCESS_PWD = 4;
    public static final int RESPONSE_CHANGE_ACCESS_PWD = 4;

    public static final int REQUEST_CHANGE_TEMP_ALARM = 5;
    public static final int RESPONSE_CHANGE_TEMP_ALARM = 5;
    public static final int REQUEST_CHANGE_ALARM_OPEN_SET = 6;
    public static final int RESPONSE_CHANGE_ALARM_OPEN_SET = 6;
    private BleStatusCallback bleStatusCallback = new BleStatusCallback() {
        @Override
        public void onNotifyValue(byte[] value) {
            if(!onThisView){
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setLockRefreshTime();
                    parseResp(value);
                }
            });
        }

        @Override
        public void onBleStatusCallback(int connectStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!onThisView){
                        return;
                    }
                    if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC) {
                        reconnectBtn.setImageResource(R.mipmap.ic_refresh);
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        showWaitingCancelDlg(getString(R.string.waitingDeviceReady));
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CLOSE) {
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_DISCONNECT) {
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                        showWarningDlg(getResources().getString(R.string.connect_fail));
                        TftBleConnectManager.getInstance().setEnterUpgrade(false);
                        a001UpgradeManager.stopUpgrade();
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CONNECTING) {

                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_SCANNING) {

                    }
                }
            });

        }

        @Override
        public void onRssiCallback(int rssi) {

        }

        @Override
        public void onUpgradeNotifyValue(byte[] value) {

        }
    };

    private void showWarningDlg(String warning){
        if(warningDlg != null){
            if(warning != null){
                warningDlg.setContentText(warning);
                warningDlg.show();
            }
            return;
        }
        warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getResources().getString(R.string.warning));
        if(warning != null){
            warningDlg.setContentText(warning);
        }
        warningDlg.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        onThisView = true;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        id = intent.getStringExtra("id");
        deviceName = intent.getStringExtra("deviceName");
        model = intent.getStringExtra("model");
        software = intent.getStringExtra("software");
        deviceId = intent.getStringExtra("deviceId");
        imei = intent.getStringExtra("imei");
        initActionbar();
        imgLockStatus = (ImageView)findViewById(R.id.img_lock_status);
        tvLockStatus = (TextView)findViewById(R.id.tx_lock_status);
        tvLog = (TextView)findViewById(R.id.tx_log);
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLockRefreshTime = (TextView)findViewById(R.id.tv_lock_refresh_time);
        tvUniqueIdTv = (TextView)findViewById(R.id.tv_unique_id);
        uniqueID = UniqueIDTool.getUniqueID();
        tvUniqueIdTv.setText(uniqueID);
        btnOpenLock = (Button)findViewById(R.id.btn_unlock);
        lnShowLog = (LinearLayout)findViewById(R.id.ln_log);
        if (isDebug){
            lnShowLog.setVisibility(View.VISIBLE);
        }else{
            lnShowLog.setVisibility(View.INVISIBLE);
        }
        lnLock = (LinearLayout)findViewById(R.id.ln_lock);
        lnActiveNetwork = (LinearLayout)findViewById(R.id.ln_active_network);
        lnUnlock = (LinearLayout)findViewById(R.id.ln_unlock);
        if(isOnlyActiveNetwork){
            lnLock.setVisibility(View.GONE);
            lnUnlock.setVisibility(View.GONE);
        }else{
            if(isDebug){
                lnLock.setVisibility(View.VISIBLE);
            }else{
                lnLock.setVisibility(View.GONE);
            }
            lnUnlock.setVisibility(View.VISIBLE);
        }
        btnOpenLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnclockPwdDlg();
            }

        });
        btnLock = (Button)findViewById(R.id.btn_lock);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] needSendBytes = getCmdContent(BleDeviceData.lockHead,new byte[]{0x00},true);
                TftBleConnectManager.getInstance().writeContent(needSendBytes);
            }
        });
        btnActiveNetwork = (Button)findViewById(R.id.btn_active_network);
        btnActiveNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] needSendBytes = getCmdContent(BleDeviceData.activeNetworkHead,new byte[]{0x00},true);
                TftBleConnectManager.getInstance().writeContent(needSendBytes);
            }
        });
        btnClearLog = (Button)findViewById(R.id.btn_clear_log);
        btnClearLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvLog.setText("");
            }
        });
        btnRefreshStatus = (Button)findViewById(R.id.btn_refresh_status);
        btnRefreshStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TftBleConnectManager.getInstance().getLockStatus();
            }
        });
        initConfigUi();

        TftBleConnectManager.getInstance().init(EditActivity.this);
        TftBleConnectManager.getInstance().setNeedCheckDeviceReady(true);
        TftBleConnectManager.getInstance().setNeedGetLockStatus(false);
        TftBleConnectManager.getInstance().setCallback("EditActivity",bleStatusCallback);
        a001UpgradeManager = new A001SoftwareUpgradeManager(this,upgradeStatusCallback);
    }
    private static int progressPercent = 0;
    private static String upgradeErrorMsg = "";
    private static String upgradeStatus = "";
    private A001SoftwareUpgradeManager.UpgradeStatusCallback upgradeStatusCallback = new A001SoftwareUpgradeManager.UpgradeStatusCallback() {
        @Override
        public void onUpgradeStatus(int status, float percent) {
            if(percent > 100){
                percent = 100;
            }
            final float percentTemp = percent;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(status == A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND){
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_SHORT).show();
                        TftBleConnectManager.getInstance().setEnterUpgrade(false);
                    }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR){
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_SHORT).show();
                        TftBleConnectManager.getInstance().setEnterUpgrade(false);
                    }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE){
                        if(waitingCancelDlg != null){
                            waitingCancelDlg.hide();
                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        Toast.makeText(EditActivity.this,R.string.error_upgrade_file,Toast.LENGTH_SHORT).show();
                        TftBleConnectManager.getInstance().setEnterUpgrade(false);
                    }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_SUCC){
                        upgradeSucc();
                    }else{
                        upgradeStatus = "progressChanged";
                        if(waitingDlg != null){

                            if(a001UpgradeManager.getStartUpgrade()){
                                waitingDlg.setTitleText(getResources().getString(R.string.processing)+ ":" + String.format("%.1f%%",percentTemp));
                            }
                        }
                    }
                }
            });

        }
    };
    private void upgradeSucc() {
        TftBleConnectManager.getInstance().setEnterUpgrade(false);
        upgradeStatus = "completed";
        Toast.makeText(EditActivity.this, R.string.upgrade_succ, Toast.LENGTH_LONG);
//        showWaitingDlg(getResources().getString(R.string.reconnecting));
//        TftBleConnectManager.getInstance().disconnect();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
        finish();
    }

    private List<Integer> getInitViewFuncs(String model){
        if(BleDeviceData.isSubLockDevice(model)){
            return subLockInitViewFuncs;
        }else{
            return parentLockInitViewFuncs;
        }
    }

    private A001SoftwareUpgradeManager a001UpgradeManager;
    private TextView ip1Tv,port1Tv,ip2Tv,port2Tv,timerTv,apnAddrTv,apnUsernameTv,apnPwdTv,versionTv,deviceNameTv,broadcastIntervalTv,
        transmittedPowerTv,deviceIdTv,modelTv,hardwareTv,tempAlarmTv,bootVersionTv;
    private Button ip1Btn,port1Btn,ip2Btn,port2Btn,timerBtn,rfidBtn,changeUnclockPwdBtn,apnAddrBtn,apnUsernameBtn,apnPwdBtn,changeAccessPwdBtn,subLockBtn,
        btnUpgrade,btnReboot,btnFactoryReset,btnShutdown,btnDeviceName,btnBroadcastInterval,btnTransmittedPower,btnDeviceId,btnBetaUpgrade,btnDebugUpgrade,
        btnTempAlarmSet,btnAlarmOpenSet,btnClearHisData,btnResetDefault;
    private LinearLayout llTimer,llSubLock,llIp1,llIp2,llApn,llVersion,llReboot,llShutdown,llFactoryReset,llTempAlarmSet,llAlarmOpenSet,
            llDeviceName,llBroadcastInterval, lllongRange,llTransmittedPower,llLed,llBuzzer,llDeviceId,llModel,llHardware,llBootVersion,llClearHisData,llResetDefault;
    private Switch ledSw,buzzerSw,longrangeSw;
    private ScrollView scrollview;

    private float tempAlarmHigh,tempAlarmLow;
    private int alarmOpenValue;
    private void initDiffUI(){
        llTimer.setVisibility(View.GONE);
        llSubLock.setVisibility(View.GONE);
        llIp1.setVisibility(View.GONE);
        llIp2.setVisibility(View.GONE);
        llApn.setVisibility(View.GONE);
        llVersion.setVisibility(View.GONE);
        llReboot.setVisibility(View.GONE);
        llShutdown.setVisibility(View.GONE);
        llFactoryReset.setVisibility(View.GONE);
        llTempAlarmSet.setVisibility(View.GONE);
        llAlarmOpenSet.setVisibility(View.GONE);
        llDeviceName.setVisibility(View.GONE);
        llBroadcastInterval.setVisibility(View.GONE);
        lllongRange.setVisibility(View.GONE);
        llTransmittedPower.setVisibility(View.GONE);
        llLed.setVisibility(View.GONE);
        llBuzzer.setVisibility(View.GONE);
        llDeviceId.setVisibility(View.GONE);
        llModel.setVisibility(View.GONE);
        llHardware.setVisibility(View.GONE);
        llBootVersion.setVisibility(View.GONE);
        llResetDefault.setVisibility(View.GONE);
        llClearHisData.setVisibility(View.GONE);
        if(!BleDeviceData.isSupportConfig(model,software,deviceId)){
            scrollview.setVisibility(View.GONE);
            return;
        }
        scrollview.setVisibility(View.VISIBLE);
        if(BleDeviceData.isSubLockDevice(model)){
            if(Utils.isDebug){
                llBootVersion.setVisibility(View.VISIBLE);
            }
            llTimer.setVisibility(View.GONE);
            llSubLock.setVisibility(View.GONE);
            llIp1.setVisibility(View.GONE);
            llIp2.setVisibility(View.GONE);
            llApn.setVisibility(View.GONE);
            btnActiveNetwork.setVisibility(View.GONE);
            lnActiveNetwork.setVisibility(View.GONE);

            llVersion.setVisibility(View.VISIBLE);
            llReboot.setVisibility(View.VISIBLE);
            llShutdown.setVisibility(View.VISIBLE);
            llFactoryReset.setVisibility(View.VISIBLE);
            llTempAlarmSet.setVisibility(View.VISIBLE);
            llAlarmOpenSet.setVisibility(View.VISIBLE);
            llDeviceName.setVisibility(View.VISIBLE);
            llBroadcastInterval.setVisibility(View.VISIBLE);
            lllongRange.setVisibility(View.VISIBLE);
            llTransmittedPower.setVisibility(View.VISIBLE);
            llLed.setVisibility(View.VISIBLE);
            llBuzzer.setVisibility(View.VISIBLE);
            llDeviceId.setVisibility(View.VISIBLE);
            llModel.setVisibility(View.VISIBLE);
            llHardware.setVisibility(View.VISIBLE);
        }else{
            llTimer.setVisibility(View.VISIBLE);
            llSubLock.setVisibility(View.VISIBLE);
            llIp1.setVisibility(View.VISIBLE);
            llIp2.setVisibility(View.VISIBLE);
            llApn.setVisibility(View.VISIBLE);
            btnActiveNetwork.setVisibility(View.VISIBLE);
            lnActiveNetwork.setVisibility(View.VISIBLE);

            llVersion.setVisibility(View.GONE);
            llReboot.setVisibility(View.GONE);
            llShutdown.setVisibility(View.GONE);
            llFactoryReset.setVisibility(View.GONE);
            llTempAlarmSet.setVisibility(View.GONE);
            llAlarmOpenSet.setVisibility(View.GONE);
            llDeviceName.setVisibility(View.GONE);
            llBroadcastInterval.setVisibility(View.GONE);
            lllongRange.setVisibility(View.GONE);
            llTransmittedPower.setVisibility(View.GONE);
            llLed.setVisibility(View.GONE);
            llBuzzer.setVisibility(View.GONE);
            llDeviceId.setVisibility(View.GONE);
            llModel.setVisibility(View.GONE);
            llHardware.setVisibility(View.GONE);

            if(BleDeviceData.isSupportClearHisAndResetDefault(model,software)){
                llResetDefault.setVisibility(View.VISIBLE);
                llClearHisData.setVisibility(View.VISIBLE);
                llReboot.setVisibility(View.VISIBLE);
            }else{
                llResetDefault.setVisibility(View.GONE);
                llClearHisData.setVisibility(View.GONE);
                llReboot.setVisibility(View.GONE);
            }

        }
    }
    private void initConfigUi(){
        scrollview = (ScrollView)findViewById(R.id.scrollview);
        llTimer = (LinearLayout)findViewById(R.id.ll_timer);
        llSubLock = (LinearLayout)findViewById(R.id.ll_sub_lock);
        llIp1 = (LinearLayout)findViewById(R.id.ll_ip1);
        llIp2 = (LinearLayout)findViewById(R.id.ll_ip2);
        llApn = (LinearLayout)findViewById(R.id.ll_apn);

        llVersion = (LinearLayout)findViewById(R.id.ll_version);
        llReboot = (LinearLayout)findViewById(R.id.ll_reboot);
        llShutdown = (LinearLayout)findViewById(R.id.ll_shutdown);
        llFactoryReset = (LinearLayout)findViewById(R.id.ll_factory_reset);
        llTempAlarmSet = (LinearLayout)findViewById(R.id.ll_temp_alarm_set);
        llAlarmOpenSet = (LinearLayout)findViewById(R.id.ll_alarm_set);
        llDeviceName = (LinearLayout)findViewById(R.id.ll_device_name);
        llBroadcastInterval = (LinearLayout)findViewById(R.id.ll_broadcast_interval);
        lllongRange = (LinearLayout)findViewById(R.id.ll_longrange);
        llTransmittedPower = (LinearLayout)findViewById(R.id.ll_transmitted_power);
        llLed = (LinearLayout)findViewById(R.id.ll_led);
        llBuzzer = (LinearLayout)findViewById(R.id.ll_buzzer);
        llDeviceId = (LinearLayout)findViewById(R.id.ll_device_id);
        llModel = (LinearLayout)findViewById(R.id.ll_model);
        llHardware = (LinearLayout)findViewById(R.id.ll_hardware);
        llBootVersion = (LinearLayout)findViewById(R.id.ll_boot_version);
        llResetDefault = (LinearLayout)findViewById(R.id.ll_reset_default);
        llClearHisData = (LinearLayout)findViewById(R.id.ll_clear_his_data);

        initDiffUI();
        ip1Tv = (TextView)findViewById(R.id.tx_device_ip1);
        ip2Tv = (TextView)findViewById(R.id.tx_device_ip2);
        port1Tv = (TextView)findViewById(R.id.tx_device_port1);
        port2Tv = (TextView)findViewById(R.id.tx_device_port2);
        timerTv = (TextView)findViewById(R.id.tx_device_timer);
        ip1Btn = (Button)findViewById(R.id.btn_edit_device_ip1);
        port1Btn = (Button)findViewById(R.id.btn_edit_device_port1);
        apnAddrTv = (TextView)findViewById(R.id.tx_device_apn_addr);
        apnUsernameTv = (TextView)findViewById(R.id.tx_device_apn_username);
        apnPwdTv = (TextView)findViewById(R.id.tx_device_apn_pwd);

        versionTv = (TextView)findViewById(R.id.tx_device_version);
        deviceNameTv = (TextView)findViewById(R.id.tx_device_name);
        broadcastIntervalTv = (TextView)findViewById(R.id.tx_device_broadcast_interval);
        transmittedPowerTv = (TextView)findViewById(R.id.tx_device_transmitted_power);
        deviceIdTv = (TextView)findViewById(R.id.tx_device_id);
        tempAlarmTv = (TextView)findViewById(R.id.tx_temp_alarm_set);
        modelTv = (TextView)findViewById(R.id.tx_device_model);
        hardwareTv = (TextView)findViewById(R.id.tx_device_hardware);
        ledSw = (Switch)findViewById(R.id.sw_device_led);
        buzzerSw = (Switch)findViewById(R.id.sw_device_buzzer);
        longrangeSw = (Switch)findViewById(R.id.sw_longrange);
        bootVersionTv= (TextView)findViewById(R.id.tx_boot_version);
        longrangeSw.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    longrangeSw.setChecked(!longrangeSw.isChecked());
                    return;
                }
                byte[] content = new byte[]{longrangeSw.isChecked() ? (byte)0x01 : (byte)0x00};
                ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_long_range,content,blePwd);
                TftBleConnectManager.getInstance().writeArrayContent(cmd);
            }
        });
        ledSw.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    ledSw.setChecked(!ledSw.isChecked());
                    return;
                }
                byte[] content = new byte[]{ledSw.isChecked() ? (byte)0x01 : (byte)0x00};
                ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_led,content,blePwd);
                TftBleConnectManager.getInstance().writeArrayContent(cmd);
            }
        });
        buzzerSw.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    buzzerSw.setChecked(!buzzerSw.isChecked());
                    return;
                }
                byte[] content = new byte[]{buzzerSw.isChecked() ? (byte)0x01 : (byte)0x00};
                ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_buzzer,content,blePwd);
                TftBleConnectManager.getInstance().writeArrayContent(cmd);
            }
        });

        apnAddrBtn = (Button)findViewById(R.id.btn_edit_device_apn_addr);
        apnUsernameBtn = (Button)findViewById(R.id.btn_edit_device_apn_username);
        apnPwdBtn = (Button)findViewById(R.id.btn_edit_device_apn_pwd);
        apnAddrBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editApnAddrDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editApnAddrDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editApnAddrDlg.setTitleText(getResources().getString(R.string.apn_addr));
                editApnAddrDlg.setCancelable(true);
                editApnAddrDlg.setCancelText(getResources().getString(R.string.cancel));
                editApnAddrDlg.setConfirmText(getResources().getString(R.string.confirm));
                editApnAddrDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String apnAddr = sweetAlertDialog.getInputText();
                        if (apnAddr.length() >= 0 && apnAddr.length() <= 49) {
                            byte[] content = apnAddr.trim().getBytes();
                            if(content.length == 0){
                                content = new byte[]{0x00};
                            }
                            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_apn_addr,content,blePwd);
                            TftBleConnectManager.getInstance().writeArrayContent(cmd);
                            sweetAlertDialog.dismissWithAnimation();
                        }else{
                            Toast.makeText(EditActivity.this,R.string.apn_addr_len_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editApnAddrDlg.show();
            }
        });
        apnUsernameBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editApnUsernameDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editApnUsernameDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editApnUsernameDlg.setTitleText(getResources().getString(R.string.apn_username));
                editApnUsernameDlg.setCancelable(true);
                editApnUsernameDlg.setCancelText(getResources().getString(R.string.cancel));
                editApnUsernameDlg.setConfirmText(getResources().getString(R.string.confirm));
                editApnUsernameDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String apnUsername = sweetAlertDialog.getInputText();
                        if (apnUsername.length() >= 0 && apnUsername.length() <= 49) {
                            byte[] content = apnUsername.trim().getBytes();
                            if(content.length == 0){
                                content = new byte[]{0x00};
                            }
                            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_apn_username,content,blePwd);
                            TftBleConnectManager.getInstance().writeArrayContent(cmd);
                            sweetAlertDialog.dismissWithAnimation();
                        }else{
                            Toast.makeText(EditActivity.this,R.string.apn_username_len_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editApnUsernameDlg.show();
            }
        });
        apnPwdBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editApnPwdDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editApnPwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editApnPwdDlg.setTitleText(getResources().getString(R.string.apn_pwd));
                editApnPwdDlg.setCancelable(true);
                editApnPwdDlg.setCancelText(getResources().getString(R.string.cancel));
                editApnPwdDlg.setConfirmText(getResources().getString(R.string.confirm));
                editApnPwdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String apnPwd = sweetAlertDialog.getInputText();
                        if (apnPwd.length() >= 0 && apnPwd.length() <= 49) {
                            byte[] content = apnPwd.trim().getBytes();
                            if(content.length == 0){
                                content = new byte[]{0x00};
                            }
                            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_apn_pwd,content,blePwd);
                            TftBleConnectManager.getInstance().writeArrayContent(cmd);
                            sweetAlertDialog.dismissWithAnimation();
                        }else{
                            Toast.makeText(EditActivity.this,R.string.apn_pwd_len_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editApnPwdDlg.show();
            }
        });
        SingleClickListener ip1EditClick = new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, IpEditActivity.class);
                intent.putExtra("domain", ip1Tv.getText());
                intent.putExtra("port", port1Tv.getText());
                intent.putExtra("ipType", 1);
                startActivityForResult(intent, REQUEST_CHANGE_IP);
            }
        };
        ip1Btn.setOnClickListener(ip1EditClick);
        port1Btn.setOnClickListener(ip1EditClick);
        ip2Btn = (Button)findViewById(R.id.btn_edit_device_ip2);
        port2Btn = (Button)findViewById(R.id.btn_edit_device_port2);
        SingleClickListener ip2EditClick = new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, IpEditActivity.class);
                intent.putExtra("domain", ip2Tv.getText());
                intent.putExtra("port", port2Tv.getText());
                intent.putExtra("ipType", 2);
                startActivityForResult(intent, REQUEST_CHANGE_IP);
            }
        };
        ip2Btn.setOnClickListener(ip2EditClick);
        port2Btn.setOnClickListener(ip2EditClick);
        timerBtn = (Button)findViewById(R.id.btn_edit_timer);
        timerBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, TimerActivity.class);
                intent.putExtra("accOn", accOnValue);
                intent.putExtra("accOff", accOffValue);
                intent.putExtra("angle", angleValue);
                intent.putExtra("distance", distanceValue);
                startActivityForResult(intent, REQUEST_CHANGE_TIMER);
            }
        });
        rfidBtn = (Button)findViewById(R.id.btn_edit_rfid);
        rfidBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, RFIDActivity.class);
                intent.putExtra("blePwd", blePwd);
                startActivity(intent);
            }
        });
        subLockBtn = (Button)findViewById(R.id.btn_edit_sub_lock_id);
        subLockBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, SubLockIdActivity.class);
                intent.putExtra("blePwd", blePwd);
                intent.putExtra("imei", imei);
                startActivity(intent);
            }
        });
        changeUnclockPwdBtn = (Button)findViewById(R.id.btn_edit_device_unclock_pwd);
        changeUnclockPwdBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, LockPwdChangeActivity.class);
                startActivityForResult(intent, REQUEST_CHANGE_UNCLOCK_PWD);
            }
        });
        changeAccessPwdBtn = (Button)findViewById(R.id.btn_edit_device_access_pwd);
        changeAccessPwdBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, AccessPwdChangeActivity.class);
                intent.putExtra("oldPwd",blePwd);
                startActivityForResult(intent, REQUEST_CHANGE_ACCESS_PWD);
            }
        });
        btnUpgrade = (Button)findViewById(R.id.btn_edit_device_version);
        btnBetaUpgrade = (Button)findViewById(R.id.btn_beta_upgrade);
        btnDebugUpgrade = (Button)findViewById(R.id.btn_debug_upgrade);
        btnTempAlarmSet = (Button)findViewById(R.id.btn_edit_temp_alarm_set);
        btnAlarmOpenSet  = (Button)findViewById(R.id.btn_edit_alarm_set);
        btnReboot = (Button)findViewById(R.id.btn_reboot);
        btnFactoryReset = (Button)findViewById(R.id.btn_factory_reset);
        btnShutdown = (Button)findViewById(R.id.btn_shutdown);
        btnDeviceName = (Button)findViewById(R.id.btn_edit_device_name);
        btnBroadcastInterval = (Button)findViewById(R.id.btn_edit_device_broadcast_interval);
        btnTransmittedPower = (Button)findViewById(R.id.btn_edit_device_transmitted_power);
        btnDeviceId = (Button)findViewById(R.id.btn_edit_device_id);
        btnDeviceId.setVisibility(View.GONE);
        if(Utils.isDebug){
            btnDebugUpgrade.setVisibility(View.VISIBLE);
        }else{
            btnDebugUpgrade.setVisibility(View.GONE);
        }
        btnUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmUpgradeDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmUpgradeDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmUpgradeDlg.setTitleText(getResources().getString(R.string.upgrade_confirm));
                confirmUpgradeDlg.setCancelable(true);
                confirmUpgradeDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmUpgradeDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmUpgradeDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        showWaitingDlg(getResources().getString(R.string.prepare_upgrade));
                        upgradeDevice();

                    }
                });
                confirmUpgradeDlg.show();
            }
        });
        btnDebugUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog urlDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                urlDlg.setTitleText(getResources().getString(R.string.input_debug_upgrade_url));
                urlDlg.setCancelable(true);
                urlDlg.setCancelText(getResources().getString(R.string.cancel));
                urlDlg.setConfirmText(getResources().getString(R.string.confirm));
                urlDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (sweetAlertDialog.getInputText() != null && sweetAlertDialog.getInputText().trim() != null) {
                            sweetAlertDialog.hide();
                            showWaitingDlg(getResources().getString(R.string.prepare_upgrade));
                            DownloadFileManager.instance().geetDebugUpdateFileUrl(EditActivity.this, sweetAlertDialog.getInputText().trim(), new DownloadFileManager.Callback() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void callback(StatusCode code, String result) {
                                    if (StatusCode.OK == code) {
                                        updateDeviceSoftware(mac, deviceName, result);
                                    } else {
                                        if (waitingDlg != null) {
                                            waitingDlg.hide();
                                        }
                                        Toast.makeText(EditActivity.this, R.string.download_file_fail, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                    }
                });
                urlDlg.show();
            }
        });
        btnBetaUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmUpgradeDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmUpgradeDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmUpgradeDlg.setTitleText(getResources().getString(R.string.upgrade_beta_confirm));
                confirmUpgradeDlg.setCancelable(true);
                confirmUpgradeDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmUpgradeDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmUpgradeDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        showWaitingDlg(getResources().getString(R.string.prepare_upgrade));
                        upgradeBetaDevice();

                    }
                });
                confirmUpgradeDlg.show();
            }
        });
        btnReboot.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRebootDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRebootDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRebootDlg.setTitleText(getResources().getString(R.string.confirm_reboot_warning));
                confirmRebootDlg.setCancelable(true);
                confirmRebootDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRebootDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRebootDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRebootDlg.hide();
                        byte[] content = new byte[]{0x00};
                        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_reboot,content,blePwd);
                        TftBleConnectManager.getInstance().writeArrayContent(cmd);
                    }
                });
                confirmRebootDlg.show();
            }
        });
        btnFactoryReset.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_reset_factory_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRelayDlg.hide();
                        byte[] content = new byte[]{0x00};
                        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_factory_reset,content,blePwd);
                        TftBleConnectManager.getInstance().writeArrayContent(cmd);
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnShutdown.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_shutdown_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRelayDlg.hide();
                        byte[] content = new byte[]{0x00};
                        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_shutdown,content,blePwd);
                        TftBleConnectManager.getInstance().writeArrayContent(cmd);
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnDeviceName.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editDeviceNameDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editDeviceNameDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editDeviceNameDlg.setTitleText(getResources().getString(R.string.enter_device_name));
                editDeviceNameDlg.setCancelable(true);
                editDeviceNameDlg.setCancelText(getResources().getString(R.string.cancel));
                editDeviceNameDlg.setConfirmText(getResources().getString(R.string.confirm));
                editDeviceNameDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String deviceName = sweetAlertDialog.getInputText();
                        if (deviceName.length() >= 3 && deviceName.length() <= 16) {
                            sweetAlertDialog.hide();
                            byte[] content = deviceName.getBytes();
                            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_device_name,content,blePwd);
                            TftBleConnectManager.getInstance().writeArrayContent(cmd);
                        } else {
                            Toast.makeText(EditActivity.this, R.string.device_name_len_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editDeviceNameDlg.show();
            }
        });
        btnBroadcastInterval.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editBroadcastIntervalDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editBroadcastIntervalDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editBroadcastIntervalDlg.setTitleText(getResources().getString(R.string.broadcast_interval));
                editBroadcastIntervalDlg.setCancelable(true);
                editBroadcastIntervalDlg.setCancelText(getResources().getString(R.string.cancel));
                editBroadcastIntervalDlg.setConfirmText(getResources().getString(R.string.confirm));
                editBroadcastIntervalDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String broadcastInterval = sweetAlertDialog.getInputText();
                        if(broadcastInterval == null || broadcastInterval.trim().isEmpty()){
                            Toast.makeText(EditActivity.this, R.string.fix_input, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            int broadcastIntervalInt = Integer.parseInt(broadcastInterval);
                            if (broadcastIntervalInt  >= 1 && broadcastIntervalInt  <= 10) {
                                sweetAlertDialog.hide();
                                byte[] content = new byte[]{(byte) broadcastIntervalInt};
                                ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_broadcast_interval,content,blePwd);
                                TftBleConnectManager.getInstance().writeArrayContent(cmd);
                            } else {
                                Toast.makeText(EditActivity.this, R.string.broadcast_interval_range, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(EditActivity.this, R.string.broadcast_interval_range, Toast.LENGTH_SHORT).show();

                        }

                    }
                });
                editBroadcastIntervalDlg.show();
            }
        });

        btnTransmittedPower.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editTransmittedPowerDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editTransmittedPowerDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editTransmittedPowerDlg.setTitleText(getResources().getString(R.string.transmitted_power));
                editTransmittedPowerDlg.setCancelable(true);
                editTransmittedPowerDlg.setCancelText(getResources().getString(R.string.cancel));
                editTransmittedPowerDlg.setConfirmText(getResources().getString(R.string.confirm));
                editTransmittedPowerDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String transmittedPower = sweetAlertDialog.getInputText();
                        if(transmittedPower == null || transmittedPower.trim().isEmpty() || !Utils.isNumeric(sweetAlertDialog.getInputText())){
                            Toast.makeText(EditActivity.this, R.string.fix_input, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try{
                            int TransmittedPowerInt = Integer.parseInt(transmittedPower);
                            if (TransmittedPowerInt  >= -20 && TransmittedPowerInt  <= 10) {
                                sweetAlertDialog.hide();
                                byte[] content = new byte[]{(byte) TransmittedPowerInt};
                                ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_ble_transmitted_power,content,blePwd);
                                TftBleConnectManager.getInstance().writeArrayContent(cmd);
                            } else {
                                Toast.makeText(EditActivity.this, R.string.transmitted_power_range, Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Toast.makeText(EditActivity.this, R.string.transmitted_power_range, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                editTransmittedPowerDlg.show();
            }
        });
        btnTempAlarmSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(EditActivity.this, TempAlarmSetActivity.class);
                intent.putExtra("tempHigh", tempAlarmHigh);
                intent.putExtra("tempLow", tempAlarmLow);
                startActivityForResult(intent,REQUEST_CHANGE_TEMP_ALARM);
            }
        });
        btnAlarmOpenSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(EditActivity.this, AlarmOpenSetActivity.class);
                intent.putExtra("alarmOpenSet", alarmOpenValue); // 示例字节值
                startActivityForResult(intent,REQUEST_CHANGE_ALARM_OPEN_SET);
            }
        });
        btnDeviceId.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editDeviceIdDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editDeviceIdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editDeviceIdDlg.setTitleText(getResources().getString(R.string.device_id));
                editDeviceIdDlg.setCancelable(true);
                editDeviceIdDlg.setCancelText(getResources().getString(R.string.cancel));
                editDeviceIdDlg.setConfirmText(getResources().getString(R.string.confirm));
                editDeviceIdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String deviceId = sweetAlertDialog.getInputText();
                        if (isValidDeviceId(deviceId)) {
                            sweetAlertDialog.hide();
                            byte[] content = MyByteUtils.hexString2Bytes(deviceId);
                            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_device_id,content,blePwd);
                            TftBleConnectManager.getInstance().writeArrayContent(cmd);
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editDeviceIdDlg.show();
            }
        });
        btnClearHisData = findViewById(R.id.btn_clear_his_data);
        btnClearHisData.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmClearHisDataDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmClearHisDataDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmClearHisDataDlg.setTitleText(getResources().getString(R.string.confirm_clear_his_data_warning));
                confirmClearHisDataDlg.setCancelable(true);
                confirmClearHisDataDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmClearHisDataDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmClearHisDataDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmClearHisDataDlg.hide();
                        byte[] content = new byte[]{0x00};
                        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_clear_his_data,content,blePwd);
                        TftBleConnectManager.getInstance().writeArrayContent(cmd);
                    }
                });
                confirmClearHisDataDlg.show();
            }
        });
        btnResetDefault = findViewById(R.id.btn_reset_default);
        btnResetDefault.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmResetDefaultDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmResetDefaultDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmResetDefaultDlg.setTitleText(getResources().getString(R.string.confirm_reset_default_warning));
                confirmResetDefaultDlg.setCancelable(true);
                confirmResetDefaultDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmResetDefaultDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmResetDefaultDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmResetDefaultDlg.hide();
                        waitCheckResetDefaultValueItems.clear();
                        waitCheckResetDefaultValueItems.add(BleDeviceData.CFG_RESTORE_IP);
                        waitCheckResetDefaultValueItems.add(BleDeviceData.CFG_RESTORE_APN);
                        waitCheckResetDefaultValueItems.add(BleDeviceData.CFG_RESTORE_TIMER);
                        waitCheckResetDefaultValueItems.add(BleDeviceData.CFG_RESTORE_NFCIDLIST);
                        waitCheckResetDefaultValueItems.add(BleDeviceData.CFG_RESTORE_SUBLOCKLIST);
                        doResetDefaultValue();

                    }
                });
                confirmResetDefaultDlg.show();
            }
        });
    }
    private void doResetDefaultValue(){
        Integer resetItem = waitCheckResetDefaultValueItems.poll();
        if(resetItem == null){
            return;
        }
        showWaitingCancelDlg(getResources().getString(R.string.waiting));
        byte[] content = new byte[]{(byte)resetItem.intValue()};
        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_reset_default,content,blePwd);
        TftBleConnectManager.getInstance().writeArrayContent(cmd);
    }
    private LinkedList<Integer> waitCheckResetDefaultValueItems = new LinkedList<Integer>();
    private boolean isValidDeviceId(String deviceId) {
        // Check if the ID is a 10-digit hexadecimal string
        return Pattern.matches("[0-9A-Fa-f]{10}", deviceId);
    }
    private void showUpgradeBtn() {
        if (serverVersion != null && !serverVersion.equals(software)) {
            btnUpgrade.setVisibility(View.VISIBLE);
        } else {
            btnUpgrade.setVisibility(View.INVISIBLE);
        }
    }
    private void showBetaUpgradeBtn() {
        if (betaServerVersion != null && software != null) {
            String formatBetaVersion = betaServerVersion.replace("V", "").replaceAll("v", "").replaceAll("\\.", "");
            String formatSoftware = software.replace("V", "").replaceAll("v", "").replaceAll("\\.", "");
            int result = formatBetaVersion.compareToIgnoreCase(formatSoftware);
            if (result > 0) {
                btnBetaUpgrade.setVisibility(View.VISIBLE);
            } else {
                btnBetaUpgrade.setVisibility(View.INVISIBLE);
            }
        }
    }
    private void getServerUpgradeInfo() {
        if(!BleDeviceData.isSubLockDevice(model)){
            return;
        }
        String deviceTypeParam = BleDeviceData.MODEL_OF_SGX120B01;
        if (!Utils.isNetworkConnected(EditActivity.this)) {
            Toast.makeText(EditActivity.this, R.string.network_permission_fail, Toast.LENGTH_SHORT).show();
            return;
        }
        OpenAPI.instance().getServerVersion(deviceTypeParam, new OpenAPI.Callback() {
            @Override
            public void callback(StatusCode code, String result) {
                if (code == StatusCode.OK) {
                    JSONObject resObj = JSONObject.parseObject(result);
                    int jsonCode = resObj.getInteger("code");
                    if (jsonCode == 0) {
                        JSONObject jsonData = resObj.getJSONObject("data");
                        if (jsonData != null) {
                            String serverVersionStr = jsonData.getString("version");
                            if (serverVersionStr != null && !serverVersionStr.isEmpty()) {
                                serverVersion = serverVersionStr.replaceAll("V", "").replaceAll("v", "");
                            }
                            upgradeLink = jsonData.getString("link");
                            showUpgradeBtn();
                        }
                    } else {
                        Toast.makeText(EditActivity.this, R.string.get_upgrade_info_fail, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(EditActivity.this, R.string.get_upgrade_info_fail, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        OpenAPI.instance().getBetaServerVersion(deviceTypeParam, id, new OpenAPI.Callback() {
            @Override
            public void callback(StatusCode code, String result) {
                if (code == StatusCode.OK) {
                    JSONObject resObj = JSONObject.parseObject(result);
                    int jsonCode = resObj.getInteger("code");
                    if (jsonCode == 0) {
                        JSONObject jsonData = resObj.getJSONObject("data");
                        if (jsonData != null) {
                            String serverVersionStr = jsonData.getString("version");
                            if (serverVersionStr != null && !serverVersionStr.isEmpty()) {
                                betaServerVersion = serverVersionStr.replaceAll("V", "").replaceAll("v", "");
                            }
                            betaUpgradeLink = jsonData.getString("link");
                            showBetaUpgradeBtn();
                        }
                    }
                }
            }
        });
    }

    private void upgradeDevice() {
        String deviceTypeParam = "SGX120 B01";
        DownloadFileManager.instance().getUpdateFileUrl(EditActivity.this, upgradeLink, serverVersion, deviceTypeParam, new DownloadFileManager.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void callback(StatusCode code, String result) {
                if (StatusCode.OK == code) {
                    updateDeviceSoftware(mac, deviceName, result);
                } else {
                    if (waitingDlg != null) {
                        waitingDlg.hide();
                    }
                    Toast.makeText(EditActivity.this, R.string.download_file_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void upgradeBetaDevice() {
        if(!BleDeviceData.isSubLockDevice(model)){
            return;
        }
        String deviceTypeParam = "SGX120 B01";
        DownloadFileManager.instance().getUpdateFileUrl(EditActivity.this, betaUpgradeLink, betaServerVersion, deviceTypeParam, new DownloadFileManager.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void callback(StatusCode code, String result) {
                if (StatusCode.OK == code) {
                    updateDeviceSoftware(mac, deviceName, result);
                } else {
                    if (waitingDlg != null) {
                        waitingDlg.hide();
                    }
                    Toast.makeText(EditActivity.this, R.string.download_file_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDeviceSoftware(String mac, String name, String path) {
        TftBleConnectManager.getInstance().setEnterUpgrade(true);
        a001UpgradeManager.startUpgrade(path);
    }
    private void fixTime(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Date now = new Date();
            outputStream.write(0x00);
            outputStream.write(MyByteUtils.unSignedInt2Bytes(now.getTime() / 1000));
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_datetime,content,null);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void changeUnclockPwd(String oldPwd, String pwd){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(oldPwd.getBytes());
            outputStream.write(pwd.getBytes());
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_change_unclock_pwd,content,this.blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void writeAlarmOpenSet(int alarmOpenSet) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(MyByteUtils.short2Bytes(alarmOpenSet));
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_alarm_open_set,content,blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void writeTempAlarm(int tempHigh, int tempLow) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(MyByteUtils.convertSignedIntToBigEndian(tempHigh));
            outputStream.write(MyByteUtils.convertSignedIntToBigEndian(tempLow));
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_temp_alarm_set,content,blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void writeTimer(int accOn,long accOff,int angle,int distance){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(MyByteUtils.short2Bytes(accOn));
            outputStream.write(MyByteUtils.unSignedInt2Bytes(accOff));
            outputStream.write(angle);
            outputStream.write(MyByteUtils.short2Bytes(distance));
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_timer,content,blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeDomain(int ipType,String domain,String port){
        int func = BleDeviceData.func_id_of_ip1;
        if(ipType == 2){
            func = BleDeviceData.func_id_of_ip2;
        }
        boolean isIpModeBool = true;
        isIpModeBool = Utils.isIpMode(domain);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(MyByteUtils.short2Bytes(Integer.valueOf(port)));
            outputStream.write(isIpModeBool ? 0x01 : 0x00);
            outputStream.write(Utils.getDomainByte(isIpModeBool,domain));
            ArrayList<byte[]> arrayList = Utils.getWriteCmdContent(func,outputStream.toByteArray(),blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(arrayList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String confirmPwd, newPwd;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onThisView = true;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);
        if (requestCode == REQUEST_CHANGE_UNCLOCK_PWD && resultCode == RESPONSE_CHANGE_UNCLOCK_PWD) {
            newPwd = data.getStringExtra("newPwd");
            String oldPwd = data.getStringExtra("oldPwd");
            if (newPwd != null && oldPwd != null && newPwd.length() == 6  && oldPwd.length() == 6) {
                changeUnclockPwd(oldPwd,newPwd);
            }
        }else if (requestCode == REQUEST_CHANGE_ACCESS_PWD && resultCode == RESPONSE_CHANGE_ACCESS_PWD) {
            newPwd = data.getStringExtra("newPwd");
            if (newPwd != null  && newPwd.length() == 6  ) {
                changeBlePwd(this.blePwd,newPwd);
            }
        }else  if (requestCode == REQUEST_CHANGE_TIMER && resultCode == RESPONSE_CHANGE_TIMER) {
            int accOn = data.getIntExtra("accOn",-1);
            long accOff = data.getLongExtra("accOff",-1);
            int angle = data.getIntExtra("angle",-1);
            int distance = data.getIntExtra("distance",-1);
            if(accOn != -1 && accOff != -1 && angle != -1 && distance != -1){
                writeTimer(accOn,accOff,angle,distance);
            }else {
                Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == REQUEST_CHANGE_IP && resultCode == RESPONSE_CHANGE_IP) {
           String domain = data.getStringExtra("domain");
            String port = data.getStringExtra("port");
            int ipType = data.getIntExtra("ipType",-1);
            writeDomain(ipType,domain,port);
        }
        else if (requestCode == REQUEST_CHANGE_ALARM_OPEN_SET && resultCode == RESPONSE_CHANGE_ALARM_OPEN_SET) {
            int alarmOpenSet = data.getIntExtra("alarmOpenSet",-1);
            if(alarmOpenSet != -1){
                writeAlarmOpenSet(alarmOpenSet);
            }else {
                Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_CHANGE_TEMP_ALARM && resultCode == RESPONSE_CHANGE_TEMP_ALARM) {
            int tempHigh = data.getIntExtra("tempHigh",-1);
            int tempLow = data.getIntExtra("tempLow",-1);
            if(tempHigh != -1 && tempLow != -1){
                writeTempAlarm(tempHigh,tempLow);
            }else {
                Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
            }
        }
    }




    private void showWaitingDlg(String warning){
        if(!onThisView){
            return;
        }
        if(waitingDlg != null){
            if (warning != null && !warning.isEmpty()){
                waitingDlg.setTitleText(warning);
            }
            waitingDlg.show();
            return;
        }
        waitingDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
        }
        waitingDlg.show();
    }

    private void showWaitingCancelDlg(String warning){
        if(!onThisView){
            return;
        }
        if(waitingCancelDlg != null){
            if (warning != null && !warning.isEmpty()){
                waitingCancelDlg.setTitleText(warning);
            }
            waitingCancelDlg.show();
            return;
        }
        waitingCancelDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingCancelDlg.showCancelButton(true);
        waitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
        waitingCancelDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.hide();
                sweetAlertDialog.dismiss();
            }
        });
        if (warning != null && !warning.isEmpty()){
            waitingCancelDlg.setTitleText(warning);
        }
        waitingCancelDlg.show();
    }


    private void showBlePwdDlg(){
        if(!BleDeviceData.isSupportConfig(model,software,deviceId)){
            return;
        }
        if(sweetBlePwdDlg != null && sweetBlePwdDlg.isShowing()){
            return;
        }
        sweetBlePwdDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
        sweetBlePwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        sweetBlePwdDlg.setTitleText(getResources().getString(R.string.input_ble_pwd));
        sweetBlePwdDlg.setCancelable(true);
        sweetBlePwdDlg.setCancelText(getResources().getString(R.string.cancel));
        sweetBlePwdDlg.setConfirmText(getResources().getString(R.string.confirm));
        sweetBlePwdDlg.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        sweetBlePwdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String pwd = sweetBlePwdDlg.getInputText();
                if (pwd.length() == 6){
                    sweetBlePwdDlg.hide();
                    blePwd = pwd;
                    changeBlePwd(pwd,pwd);
                }else{
                    Toast.makeText(EditActivity.this,R.string.pwd_format_error,Toast.LENGTH_SHORT).show();
                }

            }
        });
        sweetBlePwdDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.hide();
                sweetAlertDialog.dismiss();
                finish();
            }
        });
        sweetBlePwdDlg.setInputText("");
        if(onThisView){
            sweetBlePwdDlg.show();
        }
    }

    private void changeBlePwd(String oldPwd,String newPwd) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(oldPwd.getBytes());
            outputStream.write(newPwd.getBytes());
            this.newPwd = newPwd;
            ArrayList<byte[]> arrayList = Utils.getWriteCmdContent(BleDeviceData.func_id_of_ble_pwd_change,outputStream.toByteArray(),null);
            TftBleConnectManager.getInstance().writeArrayContent(arrayList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showUnclockPwdDlg(){
        sweetPwdDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
        sweetPwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        sweetPwdDlg.setTitleText(getResources().getString(R.string.input_ble_open_lock_pwd));
        sweetPwdDlg.setCancelable(true);
        sweetPwdDlg.setCancelText(getResources().getString(R.string.cancel));
        sweetPwdDlg.setConfirmText(getResources().getString(R.string.confirm));
        sweetPwdDlg.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        sweetPwdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String pwd = sweetPwdDlg.getInputText();
                if (pwd.length() == 6){
                    sweetPwdDlg.hide();
                    if(BleDeviceData.isSubLockDevice(model)){
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        try {
                            outputStream.write(0x02);
                            outputStream.write(pwd.getBytes());
                            outputStream.write(MyByteUtils.hexString2Bytes(uniqueID));
                            ArrayList<byte[]> arrayList = Utils.getWriteCmdContent(BleDeviceData.func_id_of_open_sub_lock,outputStream.toByteArray(),null);
                            TftBleConnectManager.getInstance().writeArrayContent(arrayList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }else{
                        byte[] needSendBytes = getCmdContent(BleDeviceData.unlockHead,pwd.getBytes(),true);
                        TftBleConnectManager.getInstance().writeContent(needSendBytes);
                    }

                }else{
                    Toast.makeText(EditActivity.this,R.string.pwd_format_error,Toast.LENGTH_SHORT).show();
                }

            }
        });
        sweetPwdDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.hide();
                sweetAlertDialog.dismiss();
            }
        });
        sweetPwdDlg.setInputText("");
        sweetPwdDlg.show();
    }

    private byte[] getCmdContent(byte[] head,byte[] content,boolean isNeedUniqueID){
        if (content == null){
            content = new byte[]{};
        }
        int len  = content.length;
        if(isNeedUniqueID){
            len += 6;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(head);
            outputStream.write(len);
            outputStream.write(content);
            if(isNeedUniqueID){
                outputStream.write(MyByteUtils.hexString2Bytes(uniqueID));
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }




    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.edit_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        TextView tvHead = (TextView)findViewById(R.id.edit_command_title);
        if (deviceName != null){
            tvHead.setText(deviceName);
        }
        backButton = (ImageView) findViewById(R.id.command_list_bar_back_id);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        reconnectBtn = (ImageView) findViewById(R.id.command_edit_reconnect);
        reconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doReconnect();
            }
        });
    }

    private void doReconnect() {
        reconnectBtn.setImageResource(R.mipmap.ic_refresh);
        TftBleConnectManager.getInstance().disconnect();
        showWaitingDlg(getResources().getString(R.string.reconnecting));
        TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
    }

    @Override
    protected void onStart() {
        super.onStart();
        onThisView = true;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);
        if(!TftBleConnectManager.getInstance().isConnectSucc()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showWaitingDlg(getResources().getString(R.string.connecting));
            TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onThisView = false;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);
        if(waitingDlg != null){
            waitingDlg.hide();
            waitingDlg.dismiss();
            waitingDlg = null;
        }
        if(waitingCancelDlg != null){
            waitingCancelDlg.hide();
            waitingCancelDlg.dismiss();
            waitingCancelDlg = null;
        }
        if(sweetPwdDlg != null){
            sweetPwdDlg.hide();
            sweetPwdDlg.dismiss();
            sweetPwdDlg = null;
        }
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
        TftBleConnectManager.getInstance().disconnect();
        TftBleConnectManager.getInstance().removeCallback("EditActivity");
    }





    private void readOtherStatus(){
        if(!BleDeviceData.isSupportConfig(model,software,deviceId)){
            return;
        }
        List<Integer> curInitViewFuncs = getInitViewFuncs(model);
        for(int i = 0; i < curInitViewFuncs.size(); i++){
            byte[] cmd = Utils.getReadCmdContent(curInitViewFuncs.get(i),null);
            TftBleConnectManager.getInstance().writeArrayContent(new ArrayList<byte[]>(){{add(cmd);}});
        }
    }




    private void parseResp(byte[] respContent){
        int i = 0;
        while (i + 3 <= respContent.length){
            byte[] head = new byte[]{respContent[i],respContent[i+1],respContent[i+2]};
            if (Arrays.equals(head,BleDeviceData.deviceReadyHead)){
                if ( i + 6 <= respContent.length){
                    int status = respContent[i + 5];
                    if (status == 0x01){
                        TftBleConnectManager.getInstance().setDeviceReady(true);
                        TftBleConnectManager.getInstance().getLockStatus();
                        readOtherStatus();
                        showBlePwdDlg();
                        EditActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(waitingCancelDlg != null){
                                    waitingCancelDlg.hide();
                                }
                            }
                        });
                    }
                }
                i+=6;

            }else if (Arrays.equals(head,BleDeviceData.getLockStatusHead) || Arrays.equals(head,BleDeviceData.getSubLockStatusHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    parseLockType(lockType);
//                    setLockRefreshTime();
//                    EditActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(waitingCancelDlg != null){
//                                waitingCancelDlg.hide();
//                            }
//
//                        }
//                    });
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.unlockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
//                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.hide();
                            }
                        }
                    });
                    parseLockType(lockType);
                }else{
                    int errorCode = respContent[i+3] & 0x7f;
                    if(errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                        Toast.makeText(EditActivity.this,R.string.pwd_error,Toast.LENGTH_SHORT).show();
                    }
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.lockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
//                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.hide();
                            }

                        }
                    });
                    parseLockType(lockType);
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.activeNetworkHead)){
                if ( i + 5 <= respContent.length){
                    int status = respContent[i + 4];
//                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.hide();
                            }

                        }
                    });
                    showDetailMsg(getString(R.string.activeNetworkCmdSend));
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.uploadStatusHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    parseLockType(lockType);
//                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.hide();
                            }

                        }
                    });
                }
                i+=5;
            }else{
                ArrayList<BleRespData> dataList = Utils.parseRespContent(respContent);
                for (BleRespData bleRespData : dataList){
                    Log.e("writeContent","code:" + bleRespData.getControlCode());
                    if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
                        parseReadResp(bleRespData);
                    }
                    else{
                        if(bleRespData.getErrorCode() == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                            if(bleRespData.getControlCode() == BleDeviceData.func_id_of_unlock
                            || bleRespData.getControlCode() == BleDeviceData.func_id_of_sub_lock_unclock){
                                Toast.makeText(EditActivity.this,R.string.unlock_pwd_error,Toast.LENGTH_SHORT).show();
                                showUnclockPwdDlg();
                            }else if(bleRespData.getControlCode() == BleDeviceData.func_id_of_change_unclock_pwd){
                                Toast.makeText(EditActivity.this,R.string.old_unlock_error,Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(EditActivity.this,R.string.ble_pwd_error,Toast.LENGTH_SHORT).show();
                                showBlePwdDlg();
                            }

                        }else{
                            Toast.makeText(EditActivity.this,R.string.fail,Toast.LENGTH_SHORT).show();
                        }

                        if(bleRespData.getControlCode() == BleDeviceData.func_id_of_sub_lock_led){
                            ledSw.setChecked(!ledSw.isChecked());
                        }else if(bleRespData.getControlCode() == BleDeviceData.func_id_of_sub_lock_buzzer){
                            buzzerSw.setChecked(!buzzerSw.isChecked());
                        }else if(bleRespData.getControlCode() == BleDeviceData.func_id_of_sub_lock_long_range){
                            longrangeSw.setChecked(!longrangeSw.isChecked());
                        }else if (bleRespData.getControlCode() == BleDeviceData.func_id_of_reset_default){
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.dismiss();
                            }
                        }

                        Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType() + "-" + bleRespData.getErrorCode());
                    }
                }
                i+= respContent.length;
            }
        }
    }

    private void parseReadResp(BleRespData bleRespData){

        int code = bleRespData.getControlCode();
        String cmdStr = Utils.controlFunc.get(bleRespData.getControlCode());
        String labelValue = "";
        if (code == 1){
            if (bleRespData.getData()[1] == 0x01){
                TftBleConnectManager.getInstance().setDeviceReady(true);

            }
        }
        else if(code == BleDeviceData.func_id_of_ip1){
            parseServerConfig(bleRespData, code,cmdStr);
        }else if(code == BleDeviceData.func_id_of_ip2){
            parseServerConfig(bleRespData, code,cmdStr);
        }else if(bleRespData.getControlCode() == BleDeviceData.func_id_of_open_sub_lock){
            int lockType = bleRespData.getData()[0];
//                    setLockRefreshTime();
            EditActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(waitingCancelDlg != null){
                        waitingCancelDlg.hide();
                    }

                }
            });
            parseLockType(lockType);
        }
        else if(code == BleDeviceData.func_id_of_apn_addr){
            byte[] respData = getMultiRespData(bleRespData,code);
            if (respData != null){
                if(respData.length == 1 && respData[0] == 0x00){
                    apnAddrTv.setText("");
                }else{
                    labelValue = new String(respData);//APN addr
                    apnAddrTv.setText(labelValue);
                }

            }
        }else if(code == BleDeviceData.func_id_of_apn_username){
            byte[] respData = getMultiRespData(bleRespData,code);
            if (respData != null){
                if(respData.length == 1 && respData[0] == 0x00){
                    apnUsernameTv.setText("");
                }else{
                    labelValue = new String(respData);//APN
                    apnUsernameTv.setText(labelValue);
                }
            }
        }else if(code == BleDeviceData.func_id_of_apn_pwd){
            byte[] respData = getMultiRespData(bleRespData,code);
            if (respData != null){
                if(respData.length == 1 && respData[0] == 0x00){
                    apnPwdTv.setText("");
                }else{
                    labelValue = new String(respData);//APN
                    apnPwdTv.setText(labelValue);
                }
            }
        }else if(code == 12038){
            if (bleRespData.getData()[0] == 0x01){
                labelValue = "TCP";
            }else if (bleRespData.getData()[0] == 0x02){
                labelValue = "UDP";
            }else if (bleRespData.getData()[0] == 0x03){
                labelValue = "MQTT";
            }
        }else if(code == BleDeviceData.func_id_of_timer){
//            setTimerLabelValue(bleRespData,code,cmdStr);//TIMER
            accOnValue = MyByteUtils.bytes2Short(bleRespData.getData(),0);
            accOffValue = MyByteUtils.unsigned4BytesToInt(bleRespData.getData(),2);
            angleValue = bleRespData.getData()[6] & 0xff;
            distanceValue = MyByteUtils.bytes2Short(bleRespData.getData(),7);
            timerTv.setText(accOnValue + ":" + accOffValue + ":" + angleValue + ":" + distanceValue);
        } else if(code == BleDeviceData.func_id_of_ble_pwd_change){
            this.blePwd = newPwd;
            if(BleDeviceData.isSubLockDevice(model)){
                fixTime();
            }
            Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
        }else if(code == BleDeviceData.func_id_of_change_unclock_pwd){
            Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
        }else if(code == BleDeviceData.func_id_of_sub_lock_buzzer){
            buzzerSw.setChecked(bleRespData.getData()[0] == 0x01);
        }else if(code == BleDeviceData.func_id_of_sub_lock_led){
            ledSw.setChecked(bleRespData.getData()[0] == 0x01);
        }else if(code == BleDeviceData.func_id_of_sub_lock_device_name){
            byte[] respData = getMultiRespData(bleRespData,code);
            if(respData != null){
                deviceNameTv.setText(new String(respData));
            }
        }else if(code == BleDeviceData.func_id_of_sub_lock_long_range){
            longrangeSw.setChecked(bleRespData.getData()[0] == 0x01);
        }else if(code == BleDeviceData.func_id_of_sub_lock_broadcast_interval){
            broadcastIntervalTv.setText(String.valueOf(bleRespData.getData()[0]) + " s") ;
        }else if(code == BleDeviceData.func_id_of_sub_lock_ble_transmitted_power){
            transmittedPowerTv.setText(String.valueOf(bleRespData.getData()[0]) + " dBm") ;
        }else if(code == BleDeviceData.func_id_of_sub_lock_version){
            byte[] respData = bleRespData.getData();
            if(respData.length < 5){
                return;
            }
            if(respData[0] == 0x0b){
                model = BleDeviceData.MODEL_OF_SGX120B01;
            }else{
                model = "";
            }
            modelTv.setText(model);
            String hardware = MyByteUtils.parseHardwareVersion(respData[1]);
            software = MyByteUtils.parseSoftwareVersion(respData,2);
            hardwareTv.setText(hardware);
            versionTv.setText(software);
            initDiffUI();
            getServerUpgradeInfo();
        }else if(code == BleDeviceData.func_id_of_sub_lock_boot_version){
            byte[] respData = bleRespData.getData();
            if (respData.length >= 2){
                bootVersionTv.setText((int)respData[0] +" " + new String(respData).substring(1,2));
            }else{
                bootVersionTv.setText(String.valueOf((int)respData[0]));
            }
        }else if(code == BleDeviceData.func_id_of_sub_lock_device_id){
            byte[] respData = getMultiRespData(bleRespData,code);
            if (respData != null){
                deviceIdTv.setText(MyByteUtils.bytes2HexString(respData,0));
            }
        }else if(code == BleDeviceData.func_id_of_sub_lock_temp_alarm_set){
            tempAlarmHigh = MyByteUtils.convertBigEndianToSignedInt(bleRespData.getData(),0);
            tempAlarmLow = MyByteUtils.convertBigEndianToSignedInt(bleRespData.getData(),2);
            String value = BleDeviceData.getCurTemp(EditActivity.this,tempAlarmLow / 100)  +
                    BleDeviceData.getCurTempUnit(EditActivity.this)
                    + " ~ " + BleDeviceData.getCurTemp(EditActivity.this,tempAlarmHigh / 100) +
                    BleDeviceData.getCurTempUnit(EditActivity.this);
            tempAlarmTv.setText(value);
            if(bleRespData.getType() ==  BleRespData.WRITE_TYPE){
                Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
            }
        }else if(code == BleDeviceData.func_id_of_sub_lock_alarm_open_set){
            alarmOpenValue = MyByteUtils.bytes2Short(bleRespData.getData(),0);
            if(bleRespData.getType() ==  BleRespData.WRITE_TYPE){
                Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
            }
        }else if (code == BleDeviceData.func_id_of_sub_lock_factory_reset) {
            Toast.makeText(EditActivity.this, R.string.factory_reset_succ, Toast.LENGTH_LONG).show();
            doReconnect();
            //read all params
        }else if (code == BleDeviceData.func_id_of_clear_his_data){
            Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
        }else if (code == BleDeviceData.func_id_of_reset_default){
            int resetItem = bleRespData.getData()[0];
            if(waitCheckResetDefaultValueItems.size() == 0){
                Toast.makeText(EditActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
                if(waitingCancelDlg != null){
                    waitingCancelDlg.dismiss();
                }
                readOtherStatus();
            }else{
                doResetDefaultValue();

            }
        }

        Log.e("show","code:" + code);


    }

    private HashMap<Integer,LinkedBlockingDeque<BleRespData>> multiRespDataMap = new HashMap<>();
    private byte[] getMultiRespData(BleRespData bleRespData, int code){
        if (bleRespData.isEnd()){
            LinkedBlockingDeque<BleRespData> queue = multiRespDataMap.get(code);
            ArrayList<BleRespData> dataList = new ArrayList<>();
            if (queue == null){
                dataList.add(bleRespData);
            }else{
                try {
                    BleRespData queueItem = queue.poll();
                    while (queueItem != null){
                        dataList.add(queueItem);
                        queueItem = queue.poll();
                    }
                    dataList.add(bleRespData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            byte[] respData = Utils.getAllDataBytes(dataList);
            return respData;
        }else{
            LinkedBlockingDeque<BleRespData> queue = multiRespDataMap.get(code);
            if (queue == null){
                queue = new LinkedBlockingDeque<>();
                multiRespDataMap.put(code,queue);
            }
            queue.offer(bleRespData);
        }
        return null;
    }

    private void parseServerConfig(BleRespData bleRespData, int code,String cmdStr) {
        byte[] respData = getMultiRespData(bleRespData,code);
        if (respData != null){
            int port = MyByteUtils.bytes2Short(respData,0);
            byte ipType = respData[2];
            byte[] domainByte = Arrays.copyOfRange(respData,3,respData.length);
            String domain = "";
            if (ipType == 0x01){
                int addr1 = (int)respData[3] >= 0 ? (int)respData[3] : (int)respData[3] + 256;
                int addr2 = (int)respData[4] >= 0 ? (int)respData[4] : (int)respData[4] + 256;
                int addr3 = (int)respData[5] >= 0 ? (int)respData[5] : (int)respData[5] + 256;
                int addr4 = (int)respData[6] >= 0 ? (int)respData[6] : (int)respData[6] + 256;
                domain = addr1  + "." + addr2 + "." + addr3 + "." + addr4;
            }else{
                domain = new String(domainByte);
            }
            String ipCmdStr = code + "_0";
            String portCmdStr = code+ "_1";
            if(code == BleDeviceData.func_id_of_ip1){
                ip1Tv.setText(domain);
                port1Tv.setText(String.valueOf(port));
            } else if (code == BleDeviceData.func_id_of_ip2) {
                ip2Tv.setText(domain);
                port2Tv.setText(String.valueOf(port));
            }
//            TextView ipLabel = showValueTV.get(ipCmdStr);
//            TextView portLabel = showValueTV.get(portCmdStr);
//            if (ipLabel != null){
//                ipLabel.setText(domain);
//            }
//            String portStr = String.valueOf(port);
//            if (portLabel != null){
//                portLabel.setText(portStr);
//            }
//            stringFuncValueMap.put(code + "_0",domain);
//            stringFuncValueMap.put(code+ "_1",portStr);
        }
    }




    private void addLog(final String log){
        EditActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.setText(tvLog.getText().toString() + log + "\r\n");
            }
        });
    }



    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date lastCheckStatusDate;
    private void setLockRefreshTime(){
        lastCheckStatusDate = new Date();
        tvLockRefreshTime.setText(dateFormat.format(lastCheckStatusDate));
    }


    private boolean isDeviceLockErrorState(int code){
        boolean isError = false;
        if (code == 0x03 ||
                code == 0x14 ||
                code == 0x24 ||
                code == 0x34 ||
                code == 0x44 ||
                code == 0x54) {
            isError = true;
        }
        return isError;
    }

    private boolean isDeviceLock(int lockType){
        boolean isLock = false;
        if (lockType == 0x00 ||
                lockType == 0x02 ||
                lockType == 0x05 ||
                lockType == 0x07 ||
                lockType == 0x12 ||
                lockType == 0x13 ||
                lockType == 0x14 ||
                lockType == 0x22 ||
                lockType == 0x23 ||
                lockType == 0x24 ||
                lockType == 0x32 ||
                lockType == 0x33 ||
                lockType == 0x34 ||
                lockType == 0x42 ||
                lockType == 0x43 ||
                lockType == 0x44 ||
                lockType == 0x52 ||
                lockType == 0x53 ||
                lockType == 0x54 ||
                lockType == 0x17 ||
                lockType == 0x27 ||
                lockType == 0x37 ||
                lockType == 0x47 ||
                lockType == 0x57
        ) {
            isLock = true;
        }
        return isLock;
    }

    private boolean isDeviceLockThreadTrimming(int lockType){
        if (
                lockType == 0x01
                        || lockType == 0x16
                        || lockType == 0x26
                        || lockType == 0x36
                        || lockType == 0x46
                        || lockType == 0x56
        ) {
            return true;
        } else {
            return false;
        }
    }

    private void setImgLockStatus(int lockType){
        if (lockType == 0xff || lockType == 0x04 || lockType == 0x09){
            return;
        }
        if (lockType < 0){
            lockType = lockType + 256;
        }
        boolean isLock = isDeviceLock(lockType);
        boolean isTrimming = isDeviceLockThreadTrimming(lockType);
        boolean isLockError = isDeviceLockErrorState(lockType);
        if (isTrimming){
            imgLockStatus.setImageResource(R.mipmap.ic_suocut);
        }else if(isLock){
            if(isLockError){
                imgLockStatus.setImageResource(R.mipmap.ic_lock_error);
            }else{
                imgLockStatus.setImageResource(R.mipmap.ic_lock);
            }
        }else{
            if(isLockError){
                imgLockStatus.setImageResource(R.mipmap.ic_unlock_error);
            }else{
                imgLockStatus.setImageResource(R.mipmap.ic_unlock);
            }
        }
    }

    private void parseLockType(int lockType){
        if (lockType < 0){
            lockType = lockType + 256;
        }
        setImgLockStatus(lockType);
        if (lockType == 0x00){
            showDetailMsg(getString(R.string.lock_status_00));
        }else if (lockType == 0x01){
            showDetailMsg(getString(R.string.lock_status_01));
        }else if (lockType == 0x03){
            showDetailMsg(getString(R.string.lock_status_03));
        }else if (lockType == 0x04){
            showDetailMsg(getString(R.string.lock_status_04));
        }else if (lockType == 0x09){
            showDetailMsg(getString(R.string.lock_status_09));
        }else if (lockType == 0x05){
            showDetailMsg(getString(R.string.lock_status_05));
        }else if (lockType == 0x06){
            showDetailMsg(getString(R.string.lock_status_06));
        }else if (lockType == 0x07){
            showDetailMsg(getString(R.string.lock_status_07));
        }else if (lockType == 0x08){
            showDetailMsg(getString(R.string.lock_status_08));
        }else if (lockType == 0x11){
            showDetailMsg(getString(R.string.lock_status_11));
        }else if (lockType == 0x12){
            showDetailMsg(getString(R.string.lock_status_12));
        }else if (lockType == 0x13){
            showDetailMsg(getString(R.string.lock_status_13));
        }else if (lockType == 0x14){
            showDetailMsg(getString(R.string.lock_status_14));
        }else if (lockType == 0x15){
            showDetailMsg(getString(R.string.lock_status_15));
        }else if (lockType == 0x16){
            showDetailMsg(getString(R.string.lock_status_16));
        }else if (lockType == 0x17){
            showDetailMsg(getString(R.string.lock_status_17));
        }
        else if (lockType == 0x21){
            showDetailMsg(getString(R.string.lock_status_21));
        }else if (lockType == 0x22){
            showDetailMsg(getString(R.string.lock_status_22));
        }else if (lockType == 0x23){
            showDetailMsg(getString(R.string.lock_status_23));
        }else if (lockType == 0x24){
            showDetailMsg(getString(R.string.lock_status_24));
        }else if (lockType == 0x25){
            showDetailMsg(getString(R.string.lock_status_25));
        }else if (lockType == 0x26){
            showDetailMsg(getString(R.string.lock_status_26));
        }else if (lockType == 0x27){
            showDetailMsg(getString(R.string.lock_status_27));
        }
        else if (lockType == 0x31){
            showDetailMsg(getString(R.string.lock_status_31));
        }else if (lockType == 0x32){
            showDetailMsg(getString(R.string.lock_status_32));
        }else if (lockType == 0x33){
            showDetailMsg(getString(R.string.lock_status_33));
        }else if (lockType == 0x34){
            showDetailMsg(getString(R.string.lock_status_34));
        }else if (lockType == 0x35){
            showDetailMsg(getString(R.string.lock_status_35));
        }else if (lockType == 0x36){
            showDetailMsg(getString(R.string.lock_status_36));
        }else if (lockType == 0x37){
            showDetailMsg(getString(R.string.lock_status_37));
        }
        else if (lockType == 0x41){
            showDetailMsg(getString(R.string.lock_status_41));
        }else if (lockType == 0x42){
            showDetailMsg(getString(R.string.lock_status_42));
        }else if (lockType == 0x43){
            showDetailMsg(getString(R.string.lock_status_43));
        }else if (lockType == 0x44){
            showDetailMsg(getString(R.string.lock_status_44));
        }else if (lockType == 0x45){
            showDetailMsg(getString(R.string.lock_status_45));
        }else if (lockType == 0x46){
            showDetailMsg(getString(R.string.lock_status_46));
        }else if (lockType == 0x47){
            showDetailMsg(getString(R.string.lock_status_47));
        }
        else if (lockType == 0x51){
            showDetailMsg(getString(R.string.lock_status_51));
        }else if (lockType == 0x52){
            showDetailMsg(getString(R.string.lock_status_52));
        }else if (lockType == 0x53){
            showDetailMsg(getString(R.string.lock_status_53));
        }else if (lockType == 0x54){
            showDetailMsg(getString(R.string.lock_status_54));
        }else if (lockType == 0x55){
            showDetailMsg(getString(R.string.lock_status_55));
        }else if (lockType == 0x56){
            showDetailMsg(getString(R.string.lock_status_56));
        }else if (lockType == 0x57){
            showDetailMsg(getString(R.string.lock_status_57));
        }else if (lockType == 0xff){
            showDetailMsg(getString(R.string.pwd_error));
        }
    }

    private void showDetailMsg(String msg){
        tvLockStatus.setText(msg);
    }

}