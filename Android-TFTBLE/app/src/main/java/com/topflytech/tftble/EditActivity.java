package com.topflytech.tftble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSONObject;
//import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
//import com.bigkoo.pickerview.view.OptionsPickerView;
import com.github.gzuliyujiang.wheelpicker.OptionPicker;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.topflytech.tftble.data.A001SoftwareUpgradeManager;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.BleHisData;
import com.topflytech.tftble.data.DfuService;
import com.topflytech.tftble.data.DownloadFileManager;
import com.topflytech.tftble.data.LogFileHelper;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.OpenAPI;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.data.SingleOptionSelectClickListener;
import com.topflytech.tftble.data.WriteSensorObj;
import com.topflytech.tftble.view.SwitchButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import cn.pedant.SweetAlert.SweetAlertDialog;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;

import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class EditActivity extends AppCompatActivity implements A001SoftwareUpgradeManager.UpgradeStatusCallback {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    //    private BluetoothClient mClient;
    private String mac;
    private String deviceType;
    private String id;
    private String software;
    private String confirmPwd, newPwd;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog connectWaitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;
    private String deviceName;
    EditText pwdEdit;
    boolean connectSucc = false;
    float tempAlarmUp, tempAlarmDown;
    int humidityAlarmUp, humidityAlarmDown;
    boolean humidityAlarmUpOpen, humidityAlarmDownOpen, tempAlarmUpOpen, tempAlarmDownOpen;

    long saveCount = 0, saveAlarmCount = 0, saveInterval = 0;
    boolean isSaveRecordStatus = false;
    String serverVersion = null;
    String betaServerVersion = null;
    String upgradeLink;
    String betaUpgradeLink;
    boolean onThisView = false;
    private OptionPicker pvTransmittedPower;
    private OptionPicker pvSaveInterval;
    private OptionPicker pvDinStatusEvent;
    private OptionPicker pvPortSelect;
    private OptionPicker pvOneWireWorkMode;
    private OptionPicker pvRs385BaudRate;
    private OptionPicker pvBroadcastType;
    private OptionPicker pvBroadcastCycle;
    private OptionPicker pvRelayType;
    public static final int REQUEST_CHANGE_PWD = 1;
    public static final int RESPONSE_CHANGE_PWD = 1;

    public static final int REQUEST_CHANGE_TEMP = 2;
    public static final int RESPONSE_CHANGE_TEMP = 2;

    public static final int REQUEST_CHANGE_HUMIDITY = 3;
    public static final int RESPONSE_CHANGE_HUMIDITY = 3;

    public static final int REQUEST_READ_HISTORY_TIME = 4;
    public static final int RESPONSE_READ_HISTORY_TIME = 4;

    public static final int REQUEST_READ_ALARM_TIME = 5;
    public static final int RESPONSE_READ_ALARM_TIME = 5;
    public static final int REQUEST_CHANGE_CONNECT_PWD = 6;
    public static final int RESPONSE_CHANGE_CONNECT_PWD = 6;
    public static final int REQUEST_CHANGE_DOUT_STATUS = 7;
    public static final int RESPONSE_CHANGE_DOUT_STATUS = 7;
    public static final int REQUEST_CHANGE_POSITIVE_NEGATIVE_WARNING = 8;
    public static final int RESPONSE_CHANGE_POSITIVE_NEGATIVE_WARNING = 8;
    public static final int REQUEST_RS485_SEND_DATA = 9;
    public static final int RESPONSE_RS485_SEND_DATA = 9;
    public static final int REQUEST_SEND_INSTRUCTION_SEQUENCE = 10;
    public static final int RESPONSE_SEND_INSTRUCTION_SEQUENCE = 10;

    public static final int REQUEST_EDIT_PULSE_DELAY = 11;
    public static final int RESPONSE_EDIT_PULSE_DELAY = 11;

    public static final int REQUEST_EDIT_SECOND_PULSE_DELAY = 12;
    public static final int RESPONSE_EDIT_SECOND_PULSE_DELAY = 12;


    private int getExtSensorTypeNextCtrl = -1;
    public static final int GET_EXT_SENSOR_TYPE_NEXT_DO_GET_HIS_DATA = 1;
    public static final int GET_EXT_SENSOR_TYPE_NEXT_DO_START_RECORD = 2;

    private long startTimestamp, endTimestamp;
    private int transmittedPower;
    private boolean pwdErrorWarning = false;
    private TextView relayStatusTV;
    private boolean relayFlashEnable = false;
    private ArrayList<String> portList;
    private boolean isWaitResponse = false;
    private LinkedBlockingQueue<WriteSensorObj> waitingSendMsgQueue = new LinkedBlockingQueue<>();
    private boolean isSendMsgThreadRunning = true;
    private ArrayList<String> relayTypeList;
    private UUID serviceId = UUID.fromString("27760001-999C-4D6A-9FC4-C7272BE10900");
    private UUID uuid = UUID.fromString("27763561-999C-4D6A-9FC4-C7272BE10900");
    private BluetoothDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic cmdReadWriteCharacteristic;
    private boolean isA001Protocol = false;

    private void checkUUID() {
        if (deviceType.equals("A001") || deviceType.equals("A002")) {
            serviceId = A001SoftwareUpgradeManager.serviceId;
            uuid = A001SoftwareUpgradeManager.cmdWriteNotifyUUID;
            isA001Protocol = true;
        }
    }

    private A001SoftwareUpgradeManager a001UpgradeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        onThisView = true;
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        software = intent.getStringExtra("software");
        deviceType = intent.getStringExtra("deviceType");
        id = intent.getStringExtra("id");
        initActionbar();
        if (deviceType.equals("S07")) {
            broadcastTypeList.set(0, "Eddystone T-button");
            broadcastTypeList.add("Eddystone UID");
            transmittedPowerList.remove(0);
        } else if (deviceType.equals("S08")) {
            broadcastTypeList.set(0, "Eddystone T-sense");
            broadcastTypeList.add("Eddystone UID");
        } else if (deviceType.equals("S10")) {
            broadcastTypeList.set(0, "Eddystone T-one");
            broadcastTypeList.add("Eddystone UID");
        } else if (deviceType.equals("S02") || deviceType.equals("S04") || deviceType.equals("S05")) {
            transmittedPowerList.remove(0);
        }
        checkUUID();
        a001UpgradeManager = new A001SoftwareUpgradeManager(this, this);
//        mClient = new BluetoothClient(EditActivity.this);
        portList = new ArrayList<String>() {{
            add(getResources().getString(R.string.port) + " 0");
            add(getResources().getString(R.string.port) + " 1");
            add(getResources().getString(R.string.port) + " 2");
        }};
        dinStatusEventList = new ArrayList<String>() {{
            add(getResources().getString(R.string.close));
            add(getResources().getString(R.string.din_status_event_rising_edge));
            add(getResources().getString(R.string.din_status_event_falling_edge));
            add(getResources().getString(R.string.din_status_event_bilateral_margin));
        }};
        relayTypeList = new ArrayList<String>() {{
            add(getResources().getString(R.string.cycle_switching));
            add(getResources().getString(R.string.relay_delay));
            add(getResources().getString(R.string.dynamic_pulse));
            add(getResources().getString(R.string.relay_pulse));
        }};
        oneWireWorkModeList = new ArrayList<String>() {{
            add(getResources().getString(R.string.close));
            add(getResources().getString(R.string.conventional_pull_up));
            add(getResources().getString(R.string.strong_pull_up));
        }};
        initUI();
        Thread dealSendMsgThread = new Thread(dealSendMsgRunnable);
        dealSendMsgThread.start();
    }

    private Runnable dealSendMsgRunnable = new Runnable() {
        @Override
        public void run() {
            while (isSendMsgThreadRunning) {
                try {
                    if (isWaitResponse) {
                        Thread.sleep(100);
                        continue;
                    }
                    if (waitingSendMsgQueue.size() > 0) {
                        WriteSensorObj writeSensorObj = waitingSendMsgQueue.poll();
                        if (bluetoothGatt != null && connectSucc) {
                            LogFileHelper.getInstance(EditActivity.this).writeIntoFile(MyUtils.bytes2HexString(writeSensorObj.getContent(), 0));
                            Log.e("tftble_log", MyUtils.bytes2HexString(writeSensorObj.getContent(), 0));
                            isWaitResponse = true;
//                            mClient.writeNoRsp(mac, serviceId, writeSensorObj.getCurUUID(), writeSensorObj.getContent(), new BleWriteResponse() {
//                                @Override
//                                public void onResponse(int code) {
//                                    if (code == REQUEST_SUCCESS) {
//
//                                    }
//                                }
//                            });
                            cmdReadWriteCharacteristic.setValue(writeSensorObj.getContent());
                            if (ActivityCompat.checkSelfPermission(EditActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            }
                            bluetoothGatt.writeCharacteristic(cmdReadWriteCharacteristic);
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void showWaitingDlg(String warning) {
        if (!onThisView) {
            return;
        }
        waitingDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()) {
            waitingDlg.setTitleText(warning);
            waitingDlg.show();
        }
    }

    private void showConnectWaitingCancelDlg(String warning) {
        connectWaitingCancelDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        connectWaitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        connectWaitingCancelDlg.showCancelButton(true);
        connectWaitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
        connectWaitingCancelDlg.setCancelClickListener(sweetPwdCancelClick);
        if (warning != null && !warning.isEmpty()) {
            connectWaitingCancelDlg.setTitleText(warning);
        }
        connectWaitingCancelDlg.show();
    }

    private void showWaitingCancelDlg(String warning) {
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
        if (warning != null && !warning.isEmpty()) {
            waitingCancelDlg.setTitleText(warning);
        }
        waitingCancelDlg.show();
    }

    private void showPwdDlg() {
        sweetPwdDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
        sweetPwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        sweetPwdDlg.setTitleText(getResources().getString(R.string.input_device_password));
        sweetPwdDlg.setCancelable(true);
        sweetPwdDlg.setCancelText(getResources().getString(R.string.cancel));
        sweetPwdDlg.setConfirmText(getResources().getString(R.string.confirm));
        sweetPwdDlg.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        sweetPwdDlg.setConfirmClickListener(sweetPwdConfirmClick);
        sweetPwdDlg.setCancelClickListener(sweetPwdCancelClick);
        sweetPwdDlg.setInputText("");
        if (MyUtils.isDebug) {
            sweetPwdDlg.setInputText("654321");
        }
        sweetPwdDlg.show();
    }

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.edit_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        TextView tvHead = (TextView) findViewById(R.id.edit_command_title);
        tvHead.setText(id);
        backButton = (ImageView) findViewById(R.id.command_list_bar_back_id);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        reconnectBtn = (ImageView) findViewById(R.id.command_edit_reconnect);
        reconnectBtn.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                doReconnect();
            }
        });
    }

    private void connectFailTimeoutShow(long tick) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要执行的操作
                if (!connectSucc && onThisView) {
                    if (connectWaitingCancelDlg != null) {
                        connectWaitingCancelDlg.hide();
                    }
                    if (waitingDlg != null) {
                        waitingDlg.hide();
                    }
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                }
            }
        }, tick);
    }

    private void doReconnect() {
        if (connectWaitingCancelDlg != null) {
            connectWaitingCancelDlg.hide();
        }
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
//        mClient.disconnect(mac);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        connectSucc = false;
        a001UpgradeManager.stopUpgrade();
        if (waitingDlg != null) {
            waitingDlg.hide();
        }
        showConnectWaitingCancelDlg(getResources().getString(R.string.reconnecting));

        connectDeviceBle();
    }

    private ArrayList<String> transmittedPowerList = new ArrayList<String>() {{
        add("8");
        add("4");
        add("0");
        add("-4");
        add("-8");
        add("-12");
        add("-16");
        add("-20");
    }};

    private ArrayList<String> dinStatusEventList;

    private ArrayList<String> oneWireWorkModeList;

    private ArrayList<String> rs485BaudRateList = new ArrayList<String>() {{
        add("1200");
        add("2400");
        add("4800");
        add("9600");
        add("14400");
        add("19200");
        add("28800");
        add("31250");
        add("38400");
        add("56000");
        add("57600");
        add("76800");
        add("115200");
        add("230400");
        add("250000");
    }};

    private ArrayList<String> broadcastTypeList = new ArrayList<String>() {{
        add("Eddystone");
        add("Beacon");
    }};

    private ArrayList<String> broadcastCycleList = new ArrayList<String>() {{
        add("5s");
        add("10s");
        add("15s");
        add("20s");
        add("25s");
        add("30s");
    }};


    private boolean relayStatus = false;
    private ArrayList<String> saveIntervalList = new ArrayList<>();
    private TextView deviceNameTV, modelTV, hardwareTV, softwareTV, broadcastCycleTV, saveRecordIntervalTV,
            tempHighAlarmTV, tempLowAlarmTV, humidityHighAlarmTV, humidityLowAlarmTV, saveCountTV, alarmCountTV, transmittedPowerTV, dinVoltageTV, dinStatusEventTV,
            dinStatusEventTypeTV, oneWireWorkModeTV, rs485BaudRateTV, broadcastTypeTV, gSensorSensitivityTV, gSensorDetectionIntervalTV, gSensorDetectionDurationTV,
            beaconMajorSetTV, beaconMinorSetTV, eddystoneNidSetTV, eddystoneBidSetTV, btnTriggerTimeTV;
    private SwitchButton ledSwitch, relaySwitch, lightSensorEnableSwitch, rs485EnableSwitch, longRangeEnableSwitch, gSensorEnableSwitch, doorEnableSwitch;
    private LinearLayout saveRecordIntervalLL, humidityHighAlarmLL, humidityLowAlarmLL, recordControlLL, relayLL, relayFlashingLL, transmittedPowerLL, transmittedPowerLineLL,
            saveRecordIntervaLineLL, humidityHighAlarmLineLL, humidityLowAlarmLineLL, recordControlLineLL, relayLineLL, relayFlashingLineLL,
            readSaveCountLL, readSaveCountLineLL, readAlarmLL, readAlarmLineLL, clearRecordLineLL, clearRecordLL, lightSensorEnableLineLL, lightSensorEnableLL,
            readDinVoltageLL, readDinVoltageLineLL, dinStatusEventLL, dinStatusEventLineLL, readDinStatusEventTypeLL, readDinStatusEventTypeLineLL,
            doutStatusLL, doutStatusLineLL, readAinVoltageLL, readAinVoltageLineLL, setPositiveNegativeWarningLL, setPositiveNegativeWarningLineLL,
            getOneWireDeviceLL, getOneWireDeviceLineLL, sendCmdSequenceLL, sendCmdSequenceLineLL,
            sequentialLL, sequentialLineLL, sendDataLL, sendDataLineLL, rs485BaudRateLL, rs485BaudRateLineLL, rs485EnableLL, rs485EnableLineLL,
            oneWireWorkModeLL, oneWireWorkModeLineLL, longRangeLL, longRangeLineLL, broadcastTypeLL, broadcastTypeLineLL, gSensorEnableLL, gSensorEnableLineLL,
            tempLowAlarmLL, tempLowAlarmLineLL, tempHighAlarmLL, tempHighAlarmLineLL, ledLL, ledLineLL, broadcastCycleLL, broadcastCycleLineLL, shutdownLL, shutdownLineLL, doorEnableLL, doorEnableLineLL, gSensorDetectionDurationLL, gSensorDetectionDurationLineLL, gSensorDetectionIntervalLL, gSensorDetectionIntervalLineLL,
            gSensorSensitivityLL, gSensorSensitivityLineLL, beaconMajorSetLL, beaconMajorSetLineLL, beaconMinorSetLL, beaconMinorSetLineLL,
            eddystoneNidSetLL, eddystoneNidSetLineLL, eddystoneBidSetLL, eddystoneBidSetLineLL, btnTriggerTimeLL, btnTriggerTimeLineLL;
    private Button btnEditDeviceName, btnEditPwd, btnEditBroadcastCycle, btnEditHighTemp, btnEditLowTemp, btnEditHumidityHigh,
            btnEditHumidityLow, btnEditSaveRecordInterval, btnRefreshSaveCount, btnReadSaveCount, btnStartRecord, btnStopRecord,
            btnClearRecord, btnResetFactory, btnUpgrade, btnBetaUpgrade, btnRefreshAlarmCount, btnReadAlarmCount, btnEditTransmittedPower, btnDebugUpgrade, btnConnectPwd, btnDinStatusEvent,
            btnShowAinVoltage, btnPositiveNegativeWarning, btnGeoOneWireDevice, btnOneWireWorkMode, btnRs485BaudRate, btnBroadcastType, btnEditDoutStatus,
            btnDinVoltageRefresh, btnSendCmdData, btnSendInstructionSequence, btnShutdown, btnGSensorSensitivity, btnGSensorDetectionInterval, btnGSensorDetectionDuration,
            btnBeaconMajorSet, btnBeaconMinorSet, btnEddystoneNidSet, btnEddystoneBidSet, btnSetBtnTriggerTime, btnFlashingRelay;

    private void initUI() {
        relayStatusTV = (TextView) findViewById(R.id.tx_relay_status);
        deviceNameTV = (TextView) findViewById(R.id.tx_device_name);
        modelTV = (TextView) findViewById(R.id.tx_device_model);
        hardwareTV = (TextView) findViewById(R.id.tx_hardware);
        softwareTV = (TextView) findViewById(R.id.tx_software);
        broadcastCycleTV = (TextView) findViewById(R.id.tx_broadcast_cycle);
        saveRecordIntervalTV = (TextView) findViewById(R.id.tx_save_record_interval);
        tempHighAlarmTV = (TextView) findViewById(R.id.tx_temp_high_alarm);
        tempLowAlarmTV = (TextView) findViewById(R.id.tx_temp_low_alarm);
        humidityHighAlarmTV = (TextView) findViewById(R.id.tx_humidity_high_alarm);
        humidityLowAlarmTV = (TextView) findViewById(R.id.tx_humidity_low_alarm);
        saveCountTV = (TextView) findViewById(R.id.tx_save_count);
        alarmCountTV = (TextView) findViewById(R.id.tx_alarm_count);
        transmittedPowerTV = (TextView) findViewById(R.id.tx_transmitted_power);
        ledSwitch = (SwitchButton) findViewById(R.id.switch_led);
        ledSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    ledSwitch.setSwitchStatus(!ledSwitch.getSwitchStatus());
                    return;
                }
                writeLedOpenStatus();
            }
        });
        lightSensorEnableSwitch = (SwitchButton) findViewById(R.id.switch_light_sensor);
        lightSensorEnableSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    lightSensorEnableSwitch.setSwitchStatus(!lightSensorEnableSwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeLightSensorEnableStatus();
            }
        });
        relaySwitch = (SwitchButton) findViewById(R.id.switch_relay);
        relaySwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    relaySwitch.setSwitchStatus(!relaySwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (MyUtils.isDebug) {
                    writeRelayStatus();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_relay_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        writeRelayStatus();
                    }
                });
                confirmRelayDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        relaySwitch.setSwitchStatus(!relaySwitch.getSwitchStatus());
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnFlashingRelay = (Button) findViewById(R.id.btn_flashing_relay);
        btnFlashingRelay.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                pvRelayType.show();
            }
        }); 
        pvRelayType = new OptionPicker(this);
        pvRelayType.setData(relayTypeList);
        pvRelayType.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                //返回的分别是三个级别的选中位置
                if (position == 0) {
                    if (MyUtils.isDebug) {
                        writeFlashingRelayStatus();
                        return;
                    }
                    SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                    confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                    confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_regular_relay_warning));
                    confirmRelayDlg.setCancelable(true);
                    confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                    confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                    confirmRelayDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.hide();
                        }
                    });
                    confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.hide();
                            writeFlashingRelayStatus();
                        }
                    });
                    confirmRelayDlg.show();
                } else if (position == 1) {
                    SweetAlertDialog confirmDelayRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                    confirmDelayRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                    confirmDelayRelayDlg.setTitleText(getResources().getString(R.string.confirm_delay_relay_warning));
                    confirmDelayRelayDlg.setCancelable(true);
                    confirmDelayRelayDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                    confirmDelayRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                    confirmDelayRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                    confirmDelayRelayDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.hide();
                        }
                    });
                    confirmDelayRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            String delayValue = sweetAlertDialog.getInputText();
                            if (delayValue.length() > 0) {
                                int delayInt = Float.valueOf(delayValue).intValue();
                                if (delayInt >= 1 && delayInt <= 600) {
                                    writeDelayRelayStatus(delayInt);
                                    sweetAlertDialog.hide();
//                                    relayStatusTV.setText(R.string.relay_delay);
                                } else {
                                    Toast.makeText(EditActivity.this, R.string.relay_delay_error_warning, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    confirmDelayRelayDlg.show();
                } else if (position == 2) {
                    relaySwitch.setSwitchStatus(!relaySwitch.getSwitchStatus());
                    Intent intent = new Intent(EditActivity.this, EditPulseDelayActivity.class);
                    startActivityForResult(intent, REQUEST_EDIT_PULSE_DELAY);
                }else if(position == 3){
                    relaySwitch.setSwitchStatus(!relaySwitch.getSwitchStatus());
                    Intent intent = new Intent(EditActivity.this, EditSecondPulseDelayActivity.class);
                    startActivityForResult(intent, REQUEST_EDIT_SECOND_PULSE_DELAY);
                }
            }
        });
        pvRelayType.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                relaySwitch.setSwitchStatus(!relaySwitch.getSwitchStatus());
            }
        });
        btnEditDeviceName = (Button) findViewById(R.id.btn_edit_device_name);
        btnEditDeviceName.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
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
                        if (deviceName.length() >= 3 && deviceName.length() <= 8) {
                            sweetAlertDialog.hide();
                            writeDeviceName(deviceName);
                        } else {
                            Toast.makeText(EditActivity.this, R.string.device_name_len_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editDeviceNameDlg.show();
            }
        });
        btnEditPwd = (Button) findViewById(R.id.btn_edit_pwd);
        btnEditPwd.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditPwdActivity.class);
                intent.putExtra("oldPwd", confirmPwd);
                startActivityForResult(intent, REQUEST_CHANGE_PWD);
            }
        });
        pvBroadcastCycle = new OptionPicker(this);
        pvBroadcastCycle.setData(broadcastCycleList);
        pvBroadcastCycle.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                //返回的分别是三个级别的选中位置
                int broadcastCycle = 5 * (position + 1);
                writeBroadcastData(broadcastCycle);
            }
        });
        btnEditBroadcastCycle = (Button) findViewById(R.id.btn_edit_broadcast_cycle);
        btnEditBroadcastCycle.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
//                pvBroadcastCycle.setDefaultPosition(0);
//                if (broadcastCycleTV.getText().toString().equals("10s")) {
//                    pvBroadcastCycle.setDefaultPosition(1);
//                }
                pvBroadcastCycle.show();

            }
        });
        btnEditHighTemp = (Button) findViewById(R.id.btn_edit_temp_high_alarm);
        btnEditHighTemp.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue", BleDeviceData.getCurTemp(EditActivity.this, tempAlarmUp));
                intent.putExtra("lowValue", BleDeviceData.getCurTemp(EditActivity.this, tempAlarmDown));
                intent.putExtra("highValueOpen", tempAlarmUpOpen);
                intent.putExtra("lowValueOpen", tempAlarmDownOpen);
                intent.putExtra("editType", "temp");
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent, REQUEST_CHANGE_TEMP);
            }
        });
        btnEditLowTemp = (Button) findViewById(R.id.btn_edit_temp_low_alarm);
        btnEditLowTemp.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue", BleDeviceData.getCurTemp(EditActivity.this, tempAlarmUp));
                intent.putExtra("lowValue", BleDeviceData.getCurTemp(EditActivity.this, tempAlarmDown));
                intent.putExtra("highValueOpen", tempAlarmUpOpen);
                intent.putExtra("lowValueOpen", tempAlarmDownOpen);
                intent.putExtra("editType", "temp");
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent, REQUEST_CHANGE_TEMP);
            }
        });
        btnEditHumidityHigh = (Button) findViewById(R.id.btn_edit_humidity_high_alarm);
        btnEditHumidityHigh.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue", String.valueOf(humidityAlarmUp));
                intent.putExtra("lowValue", String.valueOf(humidityAlarmDown));
                intent.putExtra("highValueOpen", humidityAlarmUpOpen);
                intent.putExtra("lowValueOpen", humidityAlarmDownOpen);
                intent.putExtra("editType", "humidity");
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent, REQUEST_CHANGE_HUMIDITY);
            }
        });
        btnEditHumidityLow = (Button) findViewById(R.id.btn_edit_humidity_low_alarm);
        btnEditHumidityLow.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue", String.valueOf(humidityAlarmUp));
                intent.putExtra("lowValue", String.valueOf(humidityAlarmDown));
                intent.putExtra("highValueOpen", humidityAlarmUpOpen);
                intent.putExtra("lowValueOpen", humidityAlarmDownOpen);
                intent.putExtra("editType", "humidity");
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent, REQUEST_CHANGE_HUMIDITY);
            }
        });
        saveIntervalList.clear();
        for (int i = 6; i <= 60; i++) {
            saveIntervalList.add(String.valueOf(i * 10));
        }
        pvSaveInterval = new OptionPicker(this);
        pvSaveInterval.setData(saveIntervalList);
        pvSaveInterval.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                String selected = saveIntervalList.get(position);
                writeSaveRecordIntervalData(Integer.valueOf(selected));
            }
        });
        btnEditSaveRecordInterval = (Button) findViewById(R.id.btn_edit_save_record_interval);
        btnEditSaveRecordInterval.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
//                SweetAlertDialog editSaveRecordIntervalDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
//                editSaveRecordIntervalDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
//                editSaveRecordIntervalDlg.setTitleText(getResources().getString(R.string.enter_save_record_interval));
//                editSaveRecordIntervalDlg.setCancelable(true);
//                editSaveRecordIntervalDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
//                editSaveRecordIntervalDlg.setCancelText(getResources().getString(R.string.cancel));
//                editSaveRecordIntervalDlg.setConfirmText(getResources().getString(R.string.confirm));
//                editSaveRecordIntervalDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                    @Override
//                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                        String saveRecordIntervalValue = sweetAlertDialog.getInputText();
//                        if(saveRecordIntervalValue.length() > 0){
//                            int saveRecordInterval = Float.valueOf(saveRecordIntervalValue).intValue();
//                            if (saveRecordInterval >= 10){
//                                writeSaveRecordIntervalData(saveRecordInterval);
//                                sweetAlertDialog.hide();
//                            }else{
//                                Toast.makeText(EditActivity.this,R.string.save_record_interval_input_error,Toast.LENGTH_SHORT).show();
//                            }
//                        }else{
//                            Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                editSaveRecordIntervalDlg.show();
                pvSaveInterval.setDefaultPosition(0);
                String curItem = saveRecordIntervalTV.getText().toString().replace("s", "");
                for (int i = 0; i < saveIntervalList.size(); i++) {
                    if (curItem.equals(saveIntervalList.get(i))) {
                        pvSaveInterval.setDefaultPosition(i);
                    }
                }

                pvSaveInterval.show();
            }
        });
        btnRefreshSaveCount = (Button) findViewById(R.id.btn_refresh_save_count);
        btnRefreshSaveCount.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                showWaitingDlg(getResources().getString(R.string.waiting));
                readSaveCount();
            }
        });
        btnRefreshAlarmCount = (Button) findViewById(R.id.btn_refresh_alarm_count);
        btnRefreshAlarmCount.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                showWaitingDlg(getResources().getString(R.string.waiting));
                readSaveCount();
            }
        });
        btnReadSaveCount = (Button) findViewById(R.id.btn_read_save_count);
        btnReadSaveCount.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (deviceType.equals("S10")) {
                    getExtSensorTypeNextCtrl = GET_EXT_SENSOR_TYPE_NEXT_DO_GET_HIS_DATA;
                    readExtSensorType();
                } else {
                    startHistory = false;
                    Intent intent = new Intent(EditActivity.this, HistorySelectActivity.class);
                    intent.putExtra("mac", mac);
                    intent.putExtra("deviceType", deviceType);
                    intent.putExtra("id", id);
                    intent.putExtra("reportType", "history");
                    startActivityForResult(intent, REQUEST_READ_HISTORY_TIME);
                }

            }
        });
        btnReadAlarmCount = (Button) findViewById(R.id.btn_read_alarm_count);
        btnReadAlarmCount.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                startHistory = false;
                Intent intent = new Intent(EditActivity.this, HistorySelectActivity.class);
                intent.putExtra("mac", mac);
                intent.putExtra("deviceType", deviceType);
                intent.putExtra("id", id);
                intent.putExtra("reportType", "alarm");
                startActivityForResult(intent, REQUEST_READ_ALARM_TIME);
            }
        });
        btnStartRecord = (Button) findViewById(R.id.btn_start_record);
        btnStartRecord.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (deviceType.equals("S10")) {
                    getExtSensorTypeNextCtrl = GET_EXT_SENSOR_TYPE_NEXT_DO_START_RECORD;
                    readExtSensorType();
                } else {
                    startRecord();
                }
            }
        });
        btnStopRecord = (Button) findViewById(R.id.btn_stop_record);
        btnStopRecord.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                stopRecord();
            }
        });
        btnClearRecord = (Button) findViewById(R.id.btn_clear_record);
        btnClearRecord.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                clearRecord();
            }
        });
        btnResetFactory = (Button) findViewById(R.id.btn_reset_factory);
        btnResetFactory.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
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
                        writeResetFactory();
                    }
                });
                confirmRelayDlg.show();

            }
        });
        btnUpgrade = (Button) findViewById(R.id.btn_upgrade);
        btnUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
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
        btnBetaUpgrade = (Button) findViewById(R.id.btn_beta_upgrade);
        btnBetaUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
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
        pvTransmittedPower = new OptionPicker(this);
        pvTransmittedPower.setData(transmittedPowerList);
        pvTransmittedPower.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                String selected = transmittedPowerList.get(position);
                writeTransmittedPower(Integer.valueOf(selected));
            }
        });

        btnEditTransmittedPower = (Button) findViewById(R.id.btn_edit_transmitted_power);
        btnEditTransmittedPower.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                pvTransmittedPower.setDefaultPosition(0);
                for (int i = 0; i < transmittedPowerList.size(); i++) {
                    String item = transmittedPowerList.get(i);
                    if (String.valueOf(transmittedPower).equals(item)) {
                        pvTransmittedPower.setDefaultPosition(i);
                        break;
                    }
                }
                pvTransmittedPower.show();
            }
        });
        btnDebugUpgrade = (Button) findViewById(R.id.btn_debug_upgrade);
        if (MyUtils.isDebug) {
            btnDebugUpgrade.setVisibility(View.VISIBLE);
        }
        btnDebugUpgrade.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (!connectSucc) {
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
        ImageView flashRelayTipsImg = (ImageView) findViewById(R.id.image_flashing_relay_tips);
        flashRelayTipsImg.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle(R.string.warning);

                // 创建一个可滚动的布局
                ScrollView scrollView = new ScrollView(EditActivity.this);
                TextView textView = new TextView(EditActivity.this);
                textView.setPadding(20, 20, 20, 20); // 设置内边距
                textView.setTextSize(16f); // 设置文本大小
                textView.setText(R.string.flashing_relay_tips);
                textView.setMovementMethod(ScrollingMovementMethod.getInstance()); // 允许文本滚动
                scrollView.addView(textView);

                // 将滚动视图设置为对话框的内容视图
                builder.setView(scrollView);

                // 添加按钮，如确定/取消按钮
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 关闭对话框
                    }
                });

                // 显示对话框
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
//        btnConnectPwd = (Button)findViewById(R.id.btn_edit_connect_pwd) ;
//        btnConnectPwd.setOnClickListener(new SingleClickListener() {
//            @Override
//            public void onSingleClick(View view) {
//                Intent intent = new Intent( EditActivity.this, EditConnectPwdActivity.class);
//                startActivityForResult(intent,REQUEST_CHANGE_CONNECT_PWD);
//            }
//        });
        dinVoltageTV = (TextView) findViewById(R.id.tx_din_voltage);
        dinStatusEventTV = (TextView) findViewById(R.id.tx_din_status_event);
        pvDinStatusEvent = new OptionPicker(this);
        pvDinStatusEvent.setData(dinStatusEventList);
        pvDinStatusEvent.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeDinStatusEvent(position);
            }
        });
        btnDinStatusEvent = (Button) findViewById(R.id.btn_edit_din_status_event);
        btnDinStatusEvent.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                pvDinStatusEvent.setDefaultPosition(0);
                int index = dinStatusEventList.indexOf(dinStatusEventTV.getText().toString());
                if (index >= 0 && index < dinStatusEventList.size()) {
                    pvDinStatusEvent.setDefaultPosition(index);
                }
                pvDinStatusEvent.show();
            }
        });
        btnEditDoutStatus = (Button) findViewById(R.id.btn_edit_dout_status);
        btnEditDoutStatus.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                dout0 = null;
                dout1 = null;
                showWaitingCancelDlg(getString(R.string.waiting));
                readDoutStatus(0);
                readDoutStatus(1);
            }
        });
        dinStatusEventTypeTV = (TextView) findViewById(R.id.tx_din_status_event_type);
        btnShowAinVoltage = (Button) findViewById(R.id.btn_edit_ain_voltage);
        btnShowAinVoltage.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                ain1 = null;
                ain2 = null;
                ain3 = null;
                vin = null;
                readAinStatus(0);
                readAinStatus(1);
                readAinStatus(2);
                readVinVoltage();
            }
        });
        pvPortSelect = new OptionPicker(this);
        pvPortSelect.setData(portList);
        pvPortSelect.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                readPositiveNegativeWarning(position);
            }
        });
        btnPositiveNegativeWarning = (Button) findViewById(R.id.btn_edit_positive_negative_warning);
        btnPositiveNegativeWarning.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                pvPortSelect.show();
            }
        });
        btnGeoOneWireDevice = (Button) findViewById(R.id.btn_edit_one_wire_device);
        btnGeoOneWireDevice.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                readOneWireDevice();
            }
        });
        oneWireWorkModeTV = (TextView) findViewById(R.id.tx_one_wire_work_mode);
        btnOneWireWorkMode = (Button) findViewById(R.id.btn_edit_one_wire_work_mode);
        pvOneWireWorkMode = new OptionPicker(this);
        pvOneWireWorkMode.setData(oneWireWorkModeList);
        pvOneWireWorkMode.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                //返回的分别是三个级别的选中位置
                writeOneWireWorkMode(position);
            }
        });
        btnOneWireWorkMode.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                int value = oneWireWorkModeList.indexOf(oneWireWorkModeTV.getText().toString().trim());
                if (value >= 0) {
                    pvOneWireWorkMode.setDefaultPosition(value);
                }
                pvOneWireWorkMode.show();
            }
        });
        rs485BaudRateTV = (TextView) findViewById(R.id.tx_rs485_baud_rate);
        pvRs385BaudRate = new OptionPicker(this);
        pvRs385BaudRate.setData(rs485BaudRateList);
        pvRs385BaudRate.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                //返回的分别是三个级别的选中位置
                writeRs485BaudRate(Integer.valueOf(rs485BaudRateList.get(position)));
            }
        });
        btnRs485BaudRate = (Button) findViewById(R.id.btn_edit_rs485_baud_rate);
        btnRs485BaudRate.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                int value = rs485BaudRateList.indexOf(rs485BaudRateTV.getText().toString().trim());
                if (value >= 0) {
                    pvRs385BaudRate.setDefaultPosition(value);
                }
                pvRs385BaudRate.show();
            }
        });
        rs485EnableSwitch = (SwitchButton) findViewById(R.id.switch_rs485_enable);
        rs485EnableSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    rs485EnableSwitch.setSwitchStatus(!rs485EnableSwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeRs485EnableStatus();
            }
        });
        broadcastTypeTV = (TextView) findViewById(R.id.tx_broadcast_type);
        btnBroadcastType = (Button) findViewById(R.id.btn_edit_broadcast_type);
        pvBroadcastType = new OptionPicker(this);
        pvBroadcastType.setData(broadcastTypeList);
        pvBroadcastType.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                //返回的分别是三个级别的选中位置
                writeBroadcastType(position);
            }
        });
        btnBroadcastType.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                int value = broadcastTypeList.indexOf(broadcastTypeTV.getText().toString().trim());
                if (value >= 0) {
                    pvBroadcastType.setDefaultPosition(value);
                }
                pvBroadcastType.show();
            }
        });
        longRangeEnableSwitch = (SwitchButton) findViewById(R.id.switch_long_range);
        longRangeEnableSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    longRangeEnableSwitch.setSwitchStatus(!longRangeEnableSwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeLongRangeEnableStatus();
            }
        });
        gSensorEnableSwitch = (SwitchButton) findViewById(R.id.switch_gsensor_enable);
        gSensorEnableSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    gSensorEnableSwitch.setSwitchStatus(!gSensorEnableSwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeGSensorEnableStatus();
            }
        });
        btnDinVoltageRefresh = (Button) findViewById(R.id.btn_edit_din_voltage_refresh);
        btnDinVoltageRefresh.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                readDinVoltage();
            }
        });
        btnSendCmdData = (Button) findViewById(R.id.btn_edit_send_data);
        btnSendCmdData.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditRS485CmdActivity.class);
                startActivityForResult(intent, REQUEST_RS485_SEND_DATA);
            }
        });
        btnSendInstructionSequence = (Button) findViewById(R.id.btn_edit_send_instruction_sequence);
        btnSendInstructionSequence.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this, EditInstructionSequenceActivity.class);
                startActivityForResult(intent, REQUEST_SEND_INSTRUCTION_SEQUENCE);
            }
        });
        btnShutdown = (Button) findViewById(R.id.btn_shutdown);
        btnShutdown.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
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
                        writeShutdown();
                    }
                });
                confirmRelayDlg.show();
            }
        });
        doorEnableSwitch = (SwitchButton) findViewById(R.id.switch_door_enable);
        doorEnableSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if (!connectSucc) {
                    doorEnableSwitch.setSwitchStatus(!doorEnableSwitch.getSwitchStatus());
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                writeDoorEnableStatus();
            }
        });
        gSensorSensitivityTV = (TextView) findViewById(R.id.tx_gsensor_sensitivity);
        btnGSensorSensitivity = (Button) findViewById(R.id.btn_edit_gsensor_sensitivity);
        btnGSensorSensitivity.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editGSensorSensitivityDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editGSensorSensitivityDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editGSensorSensitivityDlg.setTitleText(getResources().getString(R.string.gsensor_sensitivity));
                editGSensorSensitivityDlg.setCancelable(true);
                editGSensorSensitivityDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editGSensorSensitivityDlg.setCancelText(getResources().getString(R.string.cancel));
                editGSensorSensitivityDlg.setConfirmText(getResources().getString(R.string.confirm));
                editGSensorSensitivityDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveGSensorSensitivityValue = sweetAlertDialog.getInputText();
                        if (saveGSensorSensitivityValue.length() > 0) {
                            int gSensorSensitivity = Float.valueOf(saveGSensorSensitivityValue).intValue();
                            if (gSensorSensitivity >= 20 && gSensorSensitivity <= 64) {
                                writeGSensorSensitivity(gSensorSensitivity);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.gsensor_sensitivity_error_warning, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editGSensorSensitivityDlg.show();

            }
        });
        gSensorDetectionDurationTV = (TextView) findViewById(R.id.tx_gsensor_detection_duration);
        btnGSensorDetectionDuration = (Button) findViewById(R.id.btn_edit_gsensor_detection_duration);
        btnGSensorDetectionDuration.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editGSensorDetectionDurationDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editGSensorDetectionDurationDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editGSensorDetectionDurationDlg.setTitleText(getResources().getString(R.string.gsensor_detection_duration));
                editGSensorDetectionDurationDlg.setCancelable(true);
                editGSensorDetectionDurationDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editGSensorDetectionDurationDlg.setCancelText(getResources().getString(R.string.cancel));
                editGSensorDetectionDurationDlg.setConfirmText(getResources().getString(R.string.confirm));
                editGSensorDetectionDurationDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveGSensorDetectionDurationValue = sweetAlertDialog.getInputText();
                        if (saveGSensorDetectionDurationValue.length() > 0) {
                            int gSensorDetectionDuration = Float.valueOf(saveGSensorDetectionDurationValue).intValue();
                            if (gSensorDetectionDuration >= 3 && gSensorDetectionDuration <= 10) {
                                writeGSensorDetectionDuration(gSensorDetectionDuration);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.gsensor_detection_duration_error_warning, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editGSensorDetectionDurationDlg.show();

            }
        });
        gSensorDetectionIntervalTV = (TextView) findViewById(R.id.tx_gsensor_detection_interval);
        btnGSensorDetectionInterval = (Button) findViewById(R.id.btn_edit_gsensor_detection_interval);
        btnGSensorDetectionInterval.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editGSensorDetectionIntervalDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editGSensorDetectionIntervalDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editGSensorDetectionIntervalDlg.setTitleText(getResources().getString(R.string.gsensor_detection_interval));
                editGSensorDetectionIntervalDlg.setCancelable(true);
                editGSensorDetectionIntervalDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editGSensorDetectionIntervalDlg.setCancelText(getResources().getString(R.string.cancel));
                editGSensorDetectionIntervalDlg.setConfirmText(getResources().getString(R.string.confirm));
                editGSensorDetectionIntervalDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveGSensorDetectionIntervalValue = sweetAlertDialog.getInputText();
                        if (saveGSensorDetectionIntervalValue.length() > 0) {
                            int gSensorDetectionInterval = Float.valueOf(saveGSensorDetectionIntervalValue).intValue();
                            if (gSensorDetectionInterval >= 2 && gSensorDetectionInterval <= 180) {
                                writeGSensorDetectionInterval(gSensorDetectionInterval);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.gsensor_detection_interval_error_warning, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editGSensorDetectionIntervalDlg.show();

            }
        });

        btnBeaconMajorSet = (Button) findViewById(R.id.btn_edit_beacon_major_set);
        btnBeaconMinorSet = (Button) findViewById(R.id.btn_edit_beacon_minor_set);
        btnEddystoneNidSet = (Button) findViewById(R.id.btn_edit_eddystone_nid_set);
        btnEddystoneBidSet = (Button) findViewById(R.id.btn_edit_eddystone_bid_set);
        btnSetBtnTriggerTime = (Button) findViewById(R.id.btn_edit_btn_trigger_time);
        beaconMajorSetTV = (TextView) findViewById(R.id.tx_beacon_major_set);
        beaconMinorSetTV = (TextView) findViewById(R.id.tx_beacon_minor_set);
        eddystoneNidSetTV = (TextView) findViewById(R.id.tx_eddystone_nid_set);
        eddystoneBidSetTV = (TextView) findViewById(R.id.tx_eddystone_bid_set);
        btnTriggerTimeTV = (TextView) findViewById(R.id.tx_btn_trigger_time);
        btnBeaconMajorSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editBeaconMajorSetDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editBeaconMajorSetDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editBeaconMajorSetDlg.setTitleText(getResources().getString(R.string.beacon_major_set));
                editBeaconMajorSetDlg.setCancelable(true);
                editBeaconMajorSetDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editBeaconMajorSetDlg.setCancelText(getResources().getString(R.string.cancel));
                editBeaconMajorSetDlg.setConfirmText(getResources().getString(R.string.confirm));
                editBeaconMajorSetDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveBeaconMajorSetValue = sweetAlertDialog.getInputText();
                        if (saveBeaconMajorSetValue.length() > 0) {
                            int beaconMajorSet = Float.valueOf(saveBeaconMajorSetValue).intValue();
                            if (beaconMajorSet >= 0 && beaconMajorSet <= 65535) {
                                writeBeaconMajorSet(beaconMajorSet);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editBeaconMajorSetDlg.show();
            }
        });
        btnBeaconMinorSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editBeaconMinorSetDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editBeaconMinorSetDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editBeaconMinorSetDlg.setTitleText(getResources().getString(R.string.beacon_minor_set));
                editBeaconMinorSetDlg.setCancelable(true);
                editBeaconMinorSetDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editBeaconMinorSetDlg.setCancelText(getResources().getString(R.string.cancel));
                editBeaconMinorSetDlg.setConfirmText(getResources().getString(R.string.confirm));
                editBeaconMinorSetDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveBeaconMinorSetValue = sweetAlertDialog.getInputText();
                        if (saveBeaconMinorSetValue.length() > 0) {
                            int beaconMinorSet = Float.valueOf(saveBeaconMinorSetValue).intValue();
                            if (beaconMinorSet >= 0 && beaconMinorSet <= 65535) {
                                writeBeaconMinorSet(beaconMinorSet);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editBeaconMinorSetDlg.show();
            }
        });
        btnEddystoneNidSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editEddystoneNidSetDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editEddystoneNidSetDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editEddystoneNidSetDlg.setTitleText(getResources().getString(R.string.eddystone_nid_set));
                editEddystoneNidSetDlg.setCancelable(true);
                editEddystoneNidSetDlg.setInputType(InputType.TYPE_CLASS_TEXT);
                editEddystoneNidSetDlg.setInputText("0x");
                editEddystoneNidSetDlg.setCancelText(getResources().getString(R.string.cancel));
                editEddystoneNidSetDlg.setConfirmText(getResources().getString(R.string.confirm));
                editEddystoneNidSetDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveEddystoneNidSetValue = sweetAlertDialog.getInputText().trim();
                        saveEddystoneNidSetValue = MyUtils.replaceAll(saveEddystoneNidSetValue, "0x", "");
                        saveEddystoneNidSetValue = MyUtils.replaceAll(saveEddystoneNidSetValue, "0X", "");
                        saveEddystoneNidSetValue = MyUtils.replaceAll(saveEddystoneNidSetValue, " ", "");
                        for (char c : saveEddystoneNidSetValue.toCharArray()) {
                            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {

                            } else {
                                Toast.makeText(EditActivity.this, R.string.invalidChar, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        if (saveEddystoneNidSetValue.length() == 20) {
                            writeEddystoneNidSet(saveEddystoneNidSetValue);
                            sweetAlertDialog.hide();
                        } else {
                            Toast.makeText(EditActivity.this, R.string.eddystone_nid_set_len_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editEddystoneNidSetDlg.show();
            }
        });
        btnEddystoneBidSet.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editEddystoneBidSetDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editEddystoneBidSetDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editEddystoneBidSetDlg.setTitleText(getResources().getString(R.string.eddystone_bid_set));
                editEddystoneBidSetDlg.setCancelable(true);
                editEddystoneBidSetDlg.setInputText("0x");
                editEddystoneBidSetDlg.setInputType(InputType.TYPE_CLASS_TEXT);
                editEddystoneBidSetDlg.setCancelText(getResources().getString(R.string.cancel));
                editEddystoneBidSetDlg.setConfirmText(getResources().getString(R.string.confirm));
                editEddystoneBidSetDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveEddystoneBidSetValue = sweetAlertDialog.getInputText().trim();
                        saveEddystoneBidSetValue = MyUtils.replaceAll(saveEddystoneBidSetValue, "0x", "");
                        saveEddystoneBidSetValue = MyUtils.replaceAll(saveEddystoneBidSetValue, "0X", "");
                        saveEddystoneBidSetValue = MyUtils.replaceAll(saveEddystoneBidSetValue, " ", "");
                        for (char c : saveEddystoneBidSetValue.toCharArray()) {
                            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {

                            } else {
                                Toast.makeText(EditActivity.this, R.string.invalidChar, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        if (saveEddystoneBidSetValue.length() == 12) {
                            writeEddystoneBidSet(saveEddystoneBidSetValue);
                            sweetAlertDialog.hide();
                        } else {
                            Toast.makeText(EditActivity.this, R.string.eddystone_bid_set_len_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editEddystoneBidSetDlg.show();
            }
        });
        btnSetBtnTriggerTime.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!connectSucc) {
                    Toast.makeText(EditActivity.this, R.string.disconnect_please_connect_manually, Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog editBtnTriggerTimeDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editBtnTriggerTimeDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editBtnTriggerTimeDlg.setTitleText(getResources().getString(R.string.btn_trigger_time_desc));
                editBtnTriggerTimeDlg.setCancelable(true);
                editBtnTriggerTimeDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editBtnTriggerTimeDlg.setCancelText(getResources().getString(R.string.cancel));
                editBtnTriggerTimeDlg.setConfirmText(getResources().getString(R.string.confirm));
                editBtnTriggerTimeDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String saveBtnTriggerTimeValue = sweetAlertDialog.getInputText();
                        if (saveBtnTriggerTimeValue.length() > 0) {
                            int btnTriggerTime = Float.valueOf(saveBtnTriggerTimeValue).intValue();
                            if (btnTriggerTime >= 0 && btnTriggerTime <= 4000) {
                                writeBtnTriggerTime(btnTriggerTime / 100);
                                sweetAlertDialog.hide();
                            } else {
                                Toast.makeText(EditActivity.this, R.string.btn_trigger_time_input_error, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editBtnTriggerTimeDlg.show();
            }
        });
        getServerUpgradeInfo();
        initDiffUI();
    }

    private void upgradeDevice() {
        DownloadFileManager.instance().geetUpdateFileUrl(EditActivity.this, upgradeLink, serverVersion, deviceType, new DownloadFileManager.Callback() {
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
        DownloadFileManager.instance().geetUpdateFileUrl(EditActivity.this, betaUpgradeLink, betaServerVersion, deviceType, new DownloadFileManager.Callback() {
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
        if (A001SoftwareUpgradeManager.deviceTypeList.contains(deviceType)) {
            a001UpgradeManager.startUpgrade(mac, name, path, bluetoothGatt);
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            connectSucc = false;
            a001UpgradeManager.stopUpgrade();
            updateDFU(mac, name, path);
        }
    }

    private DfuServiceController dfuServiceController;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDFU(String mac, String name, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this);
        }
        DfuServiceListenerHelper.registerProgressListener(EditActivity.this, dfuProgressListener);
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac)
                .setDeviceName(name)
                .setForceDfu(true)
                .setKeepBond(true)
                .setPacketsReceiptNotificationsEnabled(true);
        starter.setZip(path);
        // We can use the controller to pause, resume or abort the DFU process.
        dfuServiceController = starter.start(EditActivity.this, DfuService.class);
    }

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {

            } else if (status == STATUS_DISCONNECTED) {
                if (onThisView) {
                    if (connectSucc || curTryConnectCount >= tryConnectCount) {
                        if (connectWaitingCancelDlg != null) {
                            connectWaitingCancelDlg.hide();
                        }
                        if (waitingDlg != null) {
                            waitingDlg.hide();
                        }
                        Toast.makeText(EditActivity.this, R.string.disconnect_from_device, Toast.LENGTH_SHORT).show();
                    }
                }
                curTryConnectCount++;
                connectSucc = false;
                a001UpgradeManager.stopUpgrade();
                reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
            }
        }
    };
    private static int progressPercent = 0;
    private static String upgradeErrorMsg = "";
    private static String upgradeStatus = "";

    private DfuProgressListener dfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDfuCompleted(String deviceAddress) {
            Log.e("BluetoothUtils", "onDfuCompleted");
            upgradeSucc();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            Log.e("BluetoothUtils", "onDfuAborted");
            upgradeStatus = "aborted";
            if (waitingDlg != null) {
                waitingDlg.hide();
            }
            Toast.makeText(EditActivity.this, R.string.upgrade_aborted, Toast.LENGTH_LONG);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            Log.e("BluetoothUtils", "onError");
            upgradeStatus = "error";
            upgradeErrorMsg = message;
            if (waitingDlg != null) {
                waitingDlg.hide();
            }
            Toast.makeText(EditActivity.this, message, Toast.LENGTH_LONG);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
//            Log.e("BluetoothUtils","onProgressChanged:"+percent);
            progressPercent = percent;
            upgradeStatus = "progressChanged";
            if (waitingDlg != null) {
                waitingDlg.setTitleText(getResources().getString(R.string.processing) + ":" + percent);
            }

        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.e("BluetoothUtils", "onDfuProcessStarted");
            upgradeStatus = "processStarted";
            if (waitingDlg != null) {
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_process_start));
            }

        }

        @Override
        public void onDeviceConnecting(String deviceAddress) {
            Log.e("BluetoothUtils", "onDeviceConnecting");
            upgradeStatus = "deviceConnecting";
            if (waitingDlg != null) {
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_device_connecting));
            }

        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            Log.e("BluetoothUtils", "onDeviceDisconnecting");
            upgradeStatus = "deviceDisconnected";

        }
    };

    private void upgradeSucc() {
        upgradeStatus = "completed";
        Toast.makeText(EditActivity.this, R.string.upgrade_succ, Toast.LENGTH_LONG);
        waitingDlg.setTitleText(getResources().getString(R.string.reconnecting));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectDeviceBle();
    }

    private void getServerUpgradeInfo() {
        String deviceTypeParam = deviceType;
        if (deviceType.equals("S02") && Integer.valueOf(software) <= 8) {
            deviceTypeParam = "S01";
        }
        if (!MyUtils.isNetworkConnected(EditActivity.this)) {
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onThisView = true;
        if (requestCode == REQUEST_CHANGE_PWD && resultCode == RESPONSE_CHANGE_PWD) {
            newPwd = data.getStringExtra("newPwd");
            if (newPwd != null && newPwd.length() == 6) {
                writePwd(newPwd);
            }
        } else if (requestCode == REQUEST_CHANGE_TEMP && resultCode == RESPONSE_CHANGE_TEMP) {
            int highValue = data.getIntExtra("highValue", 4095);
            int lowValue = data.getIntExtra("lowValue", 4095);
            writeTempAlarmData(highValue, lowValue);
        } else if (requestCode == REQUEST_CHANGE_HUMIDITY && resultCode == RESPONSE_CHANGE_HUMIDITY) {
            int highValue = data.getIntExtra("highValue", 4095);
            int lowValue = data.getIntExtra("lowValue", 4095);
            writeHumidityAlarmData(highValue, lowValue);
        } else if (requestCode == REQUEST_READ_HISTORY_TIME && resultCode == RESPONSE_READ_HISTORY_TIME) {
            if (startHistory) {
                return;
            }
            historyIndex = 0;
            startHistory = true;
            originHistoryList.clear();
            startTimestamp = data.getLongExtra("startDate", 0);
            endTimestamp = data.getLongExtra("endDate", 0);
            showWaitingCancelDlg(getResources().getString(R.string.loading));
            readHistory();
        } else if (requestCode == REQUEST_READ_ALARM_TIME && resultCode == RESPONSE_READ_ALARM_TIME) {
            if (startHistory) {
                return;
            }
            startHistory = true;
            showWaitingCancelDlg(getResources().getString(R.string.loading));
            startTimestamp = data.getLongExtra("startDate", 0);
            endTimestamp = data.getLongExtra("endDate", 0);
            readAlarm();
        }
//        else if(requestCode == REQUEST_CHANGE_CONNECT_PWD && resultCode == RESPONSE_CHANGE_CONNECT_PWD){
//            String connectPwd = data.getStringExtra("newPwd");
//            if(connectPwd != null && connectPwd.length() == 6){
//                writeConnectPwd(connectPwd);
//            }
//        }
        else if (requestCode == REQUEST_CHANGE_DOUT_STATUS && resultCode == RESPONSE_CHANGE_DOUT_STATUS) {
            String dout0 = data.getStringExtra("dout0");
            String dout1 = data.getStringExtra("dout1");
            if (dout0 != null) {
                writeDoutStatus(0, Integer.valueOf(dout0));
            }
            if (dout1 != null) {
                writeDoutStatus(1, Integer.valueOf(dout1));
            }
        } else if (requestCode == REQUEST_CHANGE_POSITIVE_NEGATIVE_WARNING && resultCode == RESPONSE_CHANGE_POSITIVE_NEGATIVE_WARNING) {
            Integer lowVoltage = data.getIntExtra("lowVoltage", -1);
            Integer highVoltage = data.getIntExtra("highVoltage", -1);
            Integer port = data.getIntExtra("port", -1);
            Integer mode = data.getIntExtra("mode", -1);
            Integer ditheringIntervalHigh = data.getIntExtra("ditheringIntervalHigh", -1);
            Integer ditheringIntervalLow = data.getIntExtra("ditheringIntervalLow", -1);
            Integer samplingInterval = data.getIntExtra("samplingInterval", -1);
            writePositiveNegativeWaning(port, mode, highVoltage,
                    lowVoltage, samplingInterval, ditheringIntervalHigh, ditheringIntervalLow);
        } else if (requestCode == REQUEST_RS485_SEND_DATA && resultCode == RESPONSE_RS485_SEND_DATA) {
            String cmd = data.getStringExtra("cmd");
            writeRS485SendData(cmd);
        } else if (requestCode == REQUEST_SEND_INSTRUCTION_SEQUENCE && resultCode == RESPONSE_SEND_INSTRUCTION_SEQUENCE) {
            String cmd = data.getStringExtra("cmd");
            writeSendInstructionSequence(cmd);
        } else if (requestCode == REQUEST_EDIT_PULSE_DELAY && resultCode == RESPONSE_EDIT_PULSE_DELAY) {
            Integer cycleTime = data.getIntExtra("cycleTime", -1);
            Integer initEnableTime = data.getIntExtra("initEnableTime", -1);
            Integer toggleTime = data.getIntExtra("toggleTime", -1);
            Integer recoverTime = data.getIntExtra("recoverTime", -1);
            writePulseRelayStatus(cycleTime, initEnableTime, toggleTime, recoverTime);
        }else if (requestCode == REQUEST_EDIT_SECOND_PULSE_DELAY && resultCode == RESPONSE_EDIT_SECOND_PULSE_DELAY) {
            Integer startLevel = data.getIntExtra("startLevel", -1);
            Integer highLevelPulseWidthTime = data.getIntExtra("highLevelPulseWidthTime", -1);
            Integer lowLevelPulseWidthTime = data.getIntExtra("lowLevelPulseWidthTime", -1);
            Integer pulseCount = data.getIntExtra("pulseCount", -1);
            writeSecondPulseRelayStatus(startLevel, highLevelPulseWidthTime, lowLevelPulseWidthTime, pulseCount);
        }
    }

    SweetAlertDialog.OnSweetClickListener sweetPwdCancelClick = new SweetAlertDialog.OnSweetClickListener() {
        @Override
        public void onClick(SweetAlertDialog sweetAlertDialog) {
            finish();
        }
    };
    SweetAlertDialog.OnSweetClickListener sweetPwdConfirmClick = new SweetAlertDialog.OnSweetClickListener() {
        @Override
        public void onClick(SweetAlertDialog sweetAlertDialog) {
            String pwd = sweetPwdDlg.getInputText();
            pwdErrorWarning = false;
            if (pwd.length() == 6) {
                sweetPwdDlg.hide();
                confirmPwd = pwd;
                readTransmittedPower();
                readBroadcastCycle();
                readDeviceName();
                readVersion();
                readHumidityAlarm();
                readTempAlarm();
                readLedOpenStatus();
                readRelayStatus();
                readSaveCount();
                readSaveInterval();
                readLightSensorOpen();
                readRs485Enable();
                readOneWireWorkMode();
                readRs485BaudRate();
                readBroadcastType();
                readLongRangeEnable();
                readGSensorEnable();
                readDinStatusEvent();
                readDinVoltage();
                readBtnTriggerTime();
                if (!deviceType.equals("S08")) {
                    readDoorEnableStatus();
                }
                fixTime();
            } else {
                Toast.makeText(EditActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();

            }
        }
    };
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
//                Log.e("BluetoothNotify", MyUtils.bytes2HexString(data, 0));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseNotifyData(data);
                    }
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("TFTBleService", "onCharacteristicWrite");
            // 通过gatt对象来识别设备
            String deviceAddress = gatt.getDevice().getAddress();
            byte[] data = characteristic.getValue();

            // 处理特征值写入
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.e("TFTBleService", "onConnectionStateChange");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        String deviceAddress = gatt.getDevice().getAddress();
                        // 处理连接成功
                        if (ActivityCompat.checkSelfPermission(EditActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        }

                        gatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        String deviceAddress = gatt.getDevice().getAddress();
                        // 处理断开连接
                        if (onThisView) {
                            if (connectSucc || curTryConnectCount >= tryConnectCount) {
                                if (connectWaitingCancelDlg != null) {
                                    connectWaitingCancelDlg.hide();
                                }
                                if (waitingDlg != null) {
                                    waitingDlg.hide();
                                }
                                Toast.makeText(EditActivity.this, R.string.disconnect_from_device, Toast.LENGTH_SHORT).show();
                            }
                        }
                        curTryConnectCount++;
                        connectSucc = false;
                        a001UpgradeManager.stopUpgrade();
                        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                    }
                }
            });
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String deviceAddress = gatt.getDevice().getAddress();

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
                        setCharacteristicNotification(gatt, characteristic, true);
                    }
                    if (isA001Protocol) {
                        BluetoothGattCharacteristic dataWriteCharacteristic = service.getCharacteristic(A001SoftwareUpgradeManager.dataWriteUUID);
                        if (dataWriteCharacteristic != null) {
                            a001UpgradeManager.setDataWriteCharacteristic(dataWriteCharacteristic);
                        }
                    }

                }
            } else {
                // 处理服务发现失败
                if (ActivityCompat.checkSelfPermission(EditActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.disconnect();
                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                if (connectWaitingCancelDlg != null) {
                    connectWaitingCancelDlg.hide();
                }
                if (waitingDlg != null) {
                    waitingDlg.hide();
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e("TFTBleService", "onDescriptorWrite");
            super.onDescriptorWrite(gatt, descriptor, status);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        String deviceAddress = gatt.getDevice().getAddress();
                        if (connectWaitingCancelDlg != null) {
                            connectWaitingCancelDlg.hide();
                        }
                        if (waitingDlg != null) {
                            waitingDlg.hide();
                        }
                        if (!connectSucc) {
                            showPwdDlg();
                        }

                        connectSucc = true;


                    } else {
                        if (ActivityCompat.checkSelfPermission(EditActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        }
                        gatt.disconnect();
                        new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getResources().getString(R.string.warning))
                                .setContentText(getResources().getString(R.string.connect_fail))
                                .show();
                        if (connectWaitingCancelDlg != null) {
                            connectWaitingCancelDlg.hide();
                        }
                        if (waitingDlg != null) {
                            waitingDlg.hide();
                        }
                    }
                }
            });

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
            if (ActivityCompat.checkSelfPermission(EditActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


            }
            gatt.setCharacteristicNotification(characteristic, enabled);

            UUID descriptorUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"); // 通知描述符的UUID
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUUID);
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                String deviceAddress = gatt.getDevice().getAddress();
                bluetoothGatt = gatt;
                cmdReadWriteCharacteristic = characteristic;
                a001UpgradeManager.setCmdReadWriteCharacteristic(cmdReadWriteCharacteristic);
//                String imei = macImeiMap.get(deviceAddress);
//                if (imei != null) {
//                    imeiWriteObj.put(imei, characteristic);
//                    saveConnectImeis();
//
//                    connectStatusCallback(imei);
//                }
//                isConnectingBle = false;
//                cancelDoWarning();

            }
        }
    };


//    BleConnectResponse bleConnectResponse = new BleConnectResponse() {
//        @Override
//        public void onResponse(int code, BleGattProfile data) {
//            if (code == REQUEST_SUCCESS) {
//                Log.e("bleConnect", "connected");
//                mClient.notify(mac, serviceId, uuid, bleNotifyResponse);
//            } else {
//                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
//                        .setTitleText(getResources().getString(R.string.warning))
//                        .setContentText(getResources().getString(R.string.connect_fail))
//                        .show();
//                if (waitingDlg != null) {
//                    waitingDlg.hide();
//                }
//            }
//        }
//    };
//    BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
//        @Override
//        public void onNotify(UUID service, UUID character, byte[] value) {
//            parseNotifyData(value);
//        }
//
//        @Override
//        public void onResponse(int code) {
//            if (code == REQUEST_SUCCESS) {
//                if (connectWaitingCancelDlg != null) {
//                    connectWaitingCancelDlg.hide();
//                }
//                if (waitingDlg != null) {
//                    waitingDlg.hide();
//                }
//                if (!connectSucc) {
//                    showPwdDlg();
//                }
//
//                connectSucc = true;
////                byte[] readDeviceName = {54, 53, 52, 51, 50, 49, 103, 36};
////                mClient.write(mac, serviceId, uuid, readDeviceName, new BleWriteResponse() {
////                    @Override
////                    public void onResponse(int code) {
////                        if (code == REQUEST_SUCCESS) {
////
////                        }
////                    }
////                });
//
//            } else {
//                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
//                        .setTitleText(getResources().getString(R.string.warning))
//                        .setContentText(getResources().getString(R.string.connect_fail))
//                        .show();
//                if (connectWaitingCancelDlg != null) {
//                    connectWaitingCancelDlg.hide();
//                }
//                if (waitingDlg != null) {
//                    waitingDlg.hide();
//                }
//                mClient.disconnect(mac);
//            }
//        }
//    };

    private void parseNotifyData(byte[] value) {
        reconnectBtn.setImageResource(R.mipmap.ic_refresh);
        Log.e("tftble_log", "resp:" + MyUtils.bytes2HexString(value, 0));
        isWaitResponse = false;
        LogFileHelper.getInstance(EditActivity.this).writeIntoFile("resp:" + MyUtils.bytes2HexString(value, 0));
        if (a001UpgradeManager.getStartUpgrade()) {
            a001UpgradeManager.receiveCmdResp(value);
            return;
        }
        if (value.length > 1) {
            int status = value[0];
            int type = value[1] & 0xff;
            if (status == 0) {
                if (type == MyUtils.controlFunc.get("deviceName").get("read")
                        || type == MyUtils.controlFunc.get("deviceName").get("write")) {
                    readDeviceNameParse(value);
                } else if (type == MyUtils.controlFunc.get("password").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.password_has_been_updated, Toast.LENGTH_SHORT).show();
                    confirmPwd = newPwd;
                } else if (type == MyUtils.controlFunc.get("broadcastCycle").get("read")
                        || type == MyUtils.controlFunc.get("broadcastCycle").get("write")) {
                    readBroadcastCycleResp(value);
                } else if (type == MyUtils.controlFunc.get("transmittedPower").get("read")
                        || type == MyUtils.controlFunc.get("transmittedPower").get("write")) {
                    readTransmittedPowerResp(value);
                } else if (type == MyUtils.controlFunc.get("saveCount").get("read")) {
                    readSaveCountResp(value);
                    if (waitingDlg != null) {
                        waitingDlg.hide();
                    }
                } else if (type == MyUtils.controlFunc.get("readOriginData").get("read")) {
                    readHistoryResp(value);
                } else if (type == MyUtils.controlFunc.get("readAlarm").get("read")) {
                    readAlarmResp(value);
                } else if (type == MyUtils.controlFunc.get("readNextAlarm").get("read")) {
                    readNextAlarmResp(value);
                } else if (type == MyUtils.controlFunc.get("saveInterval").get("read")
                        || type == MyUtils.controlFunc.get("saveInterval").get("write")) {
                    readSaveIntervalResp(value);
                } else if (type == MyUtils.controlFunc.get("time").get("read")
                        || type == MyUtils.controlFunc.get("time").get("write")) {
//
                } else if (type == MyUtils.controlFunc.get("firmware").get("read")) {
                    readVersionResp(value);
                } else if (type == MyUtils.controlFunc.get("humidityAlarm").get("read")
                        || type == MyUtils.controlFunc.get("humidityAlarm").get("write")) {
                    readHumidityAlarmResp(value);
                } else if (type == MyUtils.controlFunc.get("tempAlarm").get("read")
                        || type == MyUtils.controlFunc.get("tempAlarm").get("write")) {
                    readTempAlarmResp(value);
                } else if (type == MyUtils.controlFunc.get("startRecord").get("write")) {
                    startRecordResp(value);
                } else if (type == MyUtils.controlFunc.get("stopRecord").get("write")) {
                    stopRecordResp(value);
                } else if (type == MyUtils.controlFunc.get("clearRecord").get("write")) {
                    clearRecordResp(value);
                } else if (type == MyUtils.controlFunc.get("ledOpen").get("read")
                        || type == MyUtils.controlFunc.get("ledOpen").get("write")) {
                    readLedOpenStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("relay").get("read")
                        || type == MyUtils.controlFunc.get("relay").get("write")) {
                    readRelayStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("resetFactory").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.factory_reset_succ, Toast.LENGTH_LONG).show();
                    doReconnect();
                    //read all params
                } else if (type == MyUtils.controlFunc.get("lightSensorOpen").get("read")
                        || type == MyUtils.controlFunc.get("lightSensorOpen").get("write")) {
                    readLightSensorOpenStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("readDinVoltage").get("read")) {
                    readDinVoltageResp(value);
                } else if (type == MyUtils.controlFunc.get("dinStatusEvent").get("read")
                        || type == MyUtils.controlFunc.get("dinStatusEvent").get("write")) {
                    readDinStatusEventResp(value);
                } else if (type == MyUtils.controlFunc.get("readDinStatusEventType").get("read")) {
                    readDinStatusEventTypeResp(value);
                } else if (type == MyUtils.controlFunc.get("doutStatus").get("read")) {
                    readDoutStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("doutStatus").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else if (type == MyUtils.controlFunc.get("readAinVoltage").get("read")) {
                    readAinVoltageResp(value);
                } else if (type == MyUtils.controlFunc.get("readVinVoltage").get("read")) {
                    readVinVoltageResp(value);
                } else if (type == MyUtils.controlFunc.get("setPositiveNegativeWarning").get("read")) {
                    readPositiveNegativeWarningResp(value);
                } else if (type == MyUtils.controlFunc.get("setPositiveNegativeWarning").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else if (type == MyUtils.controlFunc.get("getOneWireDevice").get("read")) {
                    readOneWireDeviceResp(value);
                } else if (type == MyUtils.controlFunc.get("oneWireWorkMode").get("read")
                        || type == MyUtils.controlFunc.get("oneWireWorkMode").get("write")) {
                    readOneWireWorkModeResp(value);
                } else if (type == MyUtils.controlFunc.get("rs485BaudRate").get("read")
                        || type == MyUtils.controlFunc.get("rs485BaudRate").get("write")) {
                    readRs485BaudRateResp(value);
                } else if (type == MyUtils.controlFunc.get("rs485Enable").get("read")
                        || type == MyUtils.controlFunc.get("rs485Enable").get("write")) {
                    readRs485EnableStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("broadcastType").get("read")
                        || type == MyUtils.controlFunc.get("broadcastType").get("write")) {
                    readBroadcastTypeResp(value);
                } else if (type == MyUtils.controlFunc.get("readExtSensorType").get("read")) {
                    readExtSensorTypeResp(value);
                } else if (type == MyUtils.controlFunc.get("longRangeEnable").get("read") ||
                        type == MyUtils.controlFunc.get("longRangeEnable").get("write")) {
                    readLongRangeEnableStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("gSensorEnable").get("read")
                        || type == MyUtils.controlFunc.get("gSensorEnable").get("write")) {
                    readGSensorEnableStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("sendCmdSequence").get("write")) {
                    readSendInstructionSequenceResp(value);
                } else if (type == MyUtils.controlFunc.get("rs485SendData").get("read")) {
                    readRS485SendDataResp(value);
                } else if (type == MyUtils.controlFunc.get("rs485SendData").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else if (type == MyUtils.controlFunc.get("shutdown").get("write")) {
                    Toast.makeText(EditActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                } else if (type == MyUtils.controlFunc.get("getAinEvent").get("read")) {
                    readAinEventResp(value);
                } else if (type == MyUtils.controlFunc.get("doorEnable").get("read")
                        || type == MyUtils.controlFunc.get("doorEnable").get("write")) {
                    readDoorEnableStatusResp(value);
                } else if (type == MyUtils.controlFunc.get("gSensorDetectionDuration").get("read")
                        || type == MyUtils.controlFunc.get("gSensorDetectionDuration").get("write")) {
                    readGSensorDetectionDurationResp(value);
                } else if (type == MyUtils.controlFunc.get("gSensorSensitivity").get("read")
                        || type == MyUtils.controlFunc.get("gSensorSensitivity").get("write")) {
                    readGSensorSensitivityResp(value);
                } else if (type == MyUtils.controlFunc.get("gSensorDetectionInterval").get("read")
                        || type == MyUtils.controlFunc.get("gSensorDetectionInterval").get("write")) {
                    readGSensorDetectionIntervalResp(value);
                } else if (type == MyUtils.controlFunc.get("beaconMajorSet").get("read")
                        || type == MyUtils.controlFunc.get("beaconMajorSet").get("write")) {
                    readBeaconMajorSetResp(value);
                } else if (type == MyUtils.controlFunc.get("beaconMinorSet").get("read")
                        || type == MyUtils.controlFunc.get("beaconMinorSet").get("write")) {
                    readBeaconMinorSetResp(value);
                } else if (type == MyUtils.controlFunc.get("eddystoneNIDSet").get("read")
                        || type == MyUtils.controlFunc.get("eddystoneNIDSet").get("write")) {
                    readEddystoneNidSetResp(value);
                } else if (type == MyUtils.controlFunc.get("eddystoneBIDSet").get("read")
                        || type == MyUtils.controlFunc.get("eddystoneBIDSet").get("write")) {
                    readEddystoneBidSetResp(value);
                } else if (type == MyUtils.controlFunc.get("btnTriggerTime").get("read")
                        || type == MyUtils.controlFunc.get("btnTriggerTime").get("write")) {
                    readBtnTriggerTimeResp(value);
                }
            } else if (status == 1) {
                if (waitingDlg != null) {
                    waitingDlg.hide();
                }
                if (!pwdErrorWarning) {
                    Toast.makeText(EditActivity.this, R.string.password_is_error, Toast.LENGTH_SHORT).show();
                    pwdErrorWarning = true;
                    showPwdDlg();
                }
            } else if (status == 7) {
                if (type == MyUtils.controlFunc.get("readAlarm").get("read")) {
                    readAlarmResp(value);
                } else if (type == MyUtils.controlFunc.get("readNextAlarm").get("read")) {
                    readNextAlarmResp(value);
                }
            } else {
                if (waitingDlg != null) {
                    waitingDlg.hide();
                }
                Toast.makeText(EditActivity.this, R.string.error_please_try_again, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        pwdErrorWarning = false;
        onThisView = true;
        if (!connectSucc) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (waitingDlg != null) {
                waitingDlg.hide();
                waitingDlg.dismiss();
                waitingDlg = null;
            }
            showConnectWaitingCancelDlg(getResources().getString(R.string.connecting));
            connectDeviceBle();
        }
    }

    private int tryConnectCount = 5;
    private int curTryConnectCount = 0;
    BleConnectOptions conectOptions = new BleConnectOptions.Builder().setConnectRetry(tryConnectCount).setConnectTimeout(6000).setServiceDiscoverTimeout(60000).build();

    private void connectDeviceBle() {
        curTryConnectCount = 0;
//        mClient.connect(mac, conectOptions, bleConnectResponse);
        connectFailTimeoutShow(60000);
//        mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        if (bleDevice == null) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                // 设备不支持蓝牙
                return;
            }
            bleDevice = bluetoothAdapter.getRemoteDevice(mac);
            if (bleDevice == null) {
                Toast.makeText(EditActivity.this, R.string.error_please_try_again, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bleDevice.connectGatt(this, false, bluetoothGattCallback);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        a001UpgradeManager.stopUpgrade();
        a001UpgradeManager.stopService();
        isSendMsgThreadRunning = false;
//        mClient.disconnect(mac);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }
        if(bluetoothGatt != null ){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        connectSucc = false;
        onThisView = false;
        pwdErrorWarning = false;
        try{
            if(waitingDlg != null){
                waitingDlg.hide();
                waitingDlg.dismiss();
                waitingDlg = null;
            }
            if(sweetPwdDlg != null){
                sweetPwdDlg.hide();
                sweetPwdDlg.dismiss();
                sweetPwdDlg = null;
            }
            if(waitingCancelDlg != null){
                waitingCancelDlg.hide();
                waitingCancelDlg.dismiss();
                waitingCancelDlg = null;
            }
            if(connectWaitingCancelDlg != null){
                connectWaitingCancelDlg.hide();
                connectWaitingCancelDlg.dismiss();
                connectWaitingCancelDlg = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readDeviceName(){
        if(!isCurrentDeviceTypeFunc("deviceName")){
            return;
        }
        readData(MyUtils.controlFunc.get("deviceName").get("read"),uuid);
    }

    private void readDeviceNameParse(byte[] resp){
        byte[] realContent = Arrays.copyOfRange(resp,2,resp.length - 1);
        deviceName = new String(realContent);
        deviceNameTV.setText(deviceName);
    }


    private void readTransmittedPower(){
        if(!isCurrentDeviceTypeFunc("transmittedPower")){
            return;
        }
        readData(MyUtils.controlFunc.get("transmittedPower").get("read"),uuid);
    }

    private void readTransmittedPowerResp(byte[] resp){
        this.transmittedPower = (int)resp[2];
        transmittedPowerTV.setText(String.format("%d dBm",this.transmittedPower));
    }
    private void readBroadcastCycle(){
        if(!isCurrentDeviceTypeFunc("broadcastCycle")){
            return;
        }
        readData(MyUtils.controlFunc.get("broadcastCycle").get("read"),uuid);
    }

    private void readDinStatusEventResp(byte[] resp){
        dinStatusEventTV.setText(dinStatusEventList.get((int)resp[2]));
    }
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private void readDinStatusEventTypeResp(byte[] resp){
        if((int)resp[2] == 1){
            dinStatusEventTypeTV.setText("0 -> 1 " + dateFormat.format(new Date()));
        }else if((int)resp[2] == 2){
            dinStatusEventTypeTV.setText("1 -> 0 " + dateFormat.format(new Date()));
        }

    }
    private Integer dout0 =null, dout1 =null;
    private void readDoutStatusResp(byte[] resp){
        if(resp[2] == 0){
            dout0 = (int)resp[3];
        }else if(resp[2] == 1){
            dout1 = (int)resp[3];
        }
        if(dout0 != null && dout1 != null){
            Intent intent = new Intent(EditActivity.this,EditDoutOutputStatusActivity.class);
            intent.putExtra("oldDout0", dout0);
            intent.putExtra("oldDout1", dout1);
            if(waitingCancelDlg != null){
                waitingCancelDlg.hide();
            }
            startActivityForResult(intent,REQUEST_CHANGE_DOUT_STATUS);
        }
    }

    private void readAinEventResp(byte[] resp){
        String warningMsg = "";
        int voltage = MyUtils.bytes2Short(resp,4);
        warningMsg +=   String.format("AIN%d:%.2fV;\r\n ",(int)resp[2],voltage / 100.0f);
        if((int)resp[3] == 1){
            warningMsg += "0 -> 1 " + dateFormat.format(new Date());
        }else if((int)resp[3] == 2){
            warningMsg += "1 -> 0 " + dateFormat.format(new Date());
        }
        SweetAlertDialog warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
        warningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        warningDlg.setTitleText(getResources().getString(R.string.ain_positive_negative_warning));
        warningDlg.setContentText(warningMsg);
        warningDlg.setCancelable(false);
        warningDlg.setConfirmText(getResources().getString(R.string.confirm));
        warningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                warningDlg.hide();
            }
        });
        warningDlg.show();
    }

    private Integer ain1=null,ain2=null,ain3=null,vin=null;
    private void readAinVoltageResp(byte[] resp){
        if(resp[2] == 0){
            ain1 = MyUtils.bytes2Short(resp,3);
        }else if(resp[2] == 1){
            ain2 = MyUtils.bytes2Short(resp,3);
        }else if(resp[2] == 2){
            ain3 = MyUtils.bytes2Short(resp,3);
        }
        showAinVinDlg();
    }

    private void showAinVinDlg(){
        if(ain1 != null && ain2 != null && ain3 != null && vin != null){
            String warningMsg = String.format("VIN:%.2fV;\nAIN0:%.2fV;\r\n AIN1:%.2fV;\r\n AIN2:%.2fV; ",vin / 100.0f,ain1 / 100.0f,ain2/100.0f,ain3/100.0f);
            SweetAlertDialog warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
            warningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
            warningDlg.setTitleText(getResources().getString(R.string.ainVoltage));
            warningDlg.setContentText(warningMsg);
            warningDlg.setCancelable(false);
            warningDlg.setConfirmText(getResources().getString(R.string.confirm));
            warningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    warningDlg.hide();
                }
            });
            warningDlg.show();
        }
    }

    private void readVinVoltageResp(byte[] resp){
        vin = MyUtils.bytes2Short(resp,2);
        showAinVinDlg();
    }
    private void readOneWireWorkModeResp(byte[] resp){
        int value = resp[2];
        if(value < oneWireWorkModeList.size()){
            oneWireWorkModeTV.setText(oneWireWorkModeList.get(value));
        }

    }
    private void readRs485BaudRateResp(byte[] resp){
        int value = MyUtils.bytes2Integer(resp,2);
        rs485BaudRateTV.setText(String.valueOf(value));
    }
    private int extSensorType = 0;
    private void readExtSensorTypeResp(byte[] resp){
        int value = resp[2];
        extSensorType = value;
        if(value == 1){
            if(getExtSensorTypeNextCtrl == GET_EXT_SENSOR_TYPE_NEXT_DO_GET_HIS_DATA){
                startHistory = false;
                getExtSensorTypeNextCtrl = -1;
                Intent intent = new Intent(EditActivity.this,HistorySelectActivity.class);
                intent.putExtra("mac",mac);
                intent.putExtra("deviceType",deviceType);
                intent.putExtra("id",id);
                intent.putExtra("reportType","history");
                startActivityForResult(intent,REQUEST_READ_HISTORY_TIME);
            }else if(getExtSensorTypeNextCtrl == GET_EXT_SENSOR_TYPE_NEXT_DO_START_RECORD){
                getExtSensorTypeNextCtrl = -1;
                startRecord();
            }

            }else{
            Toast.makeText(EditActivity.this,R.string.not_find_ext_sensor,Toast.LENGTH_SHORT).show();
        }
    }

    private void readBroadcastTypeResp(byte[] resp){
        int value = resp[2];
        if(value < broadcastTypeList.size()){
            broadcastTypeTV.setText(broadcastTypeList.get(value));
//            if(deviceType.equals("S07") ){
//               if(value == 0){
//                   longRangeLL.setVisibility(View.GONE);
//                   longRangeLineLL.setVisibility(View.GONE);
//               }else{
//                   longRangeLL.setVisibility(View.VISIBLE);
//                   longRangeLineLL.setVisibility(View.VISIBLE);
//               }
//            }
            beaconMajorSetLL.setVisibility(View.GONE);
            beaconMajorSetLineLL.setVisibility(View.GONE);
            beaconMinorSetLL.setVisibility(View.GONE);
            beaconMinorSetLineLL.setVisibility(View.GONE);
            eddystoneNidSetLL.setVisibility(View.GONE);
            eddystoneNidSetLineLL.setVisibility(View.GONE);
            eddystoneBidSetLL.setVisibility(View.GONE);
            eddystoneBidSetLineLL.setVisibility(View.GONE);

            if(deviceType.equals("S07") || deviceType.equals("S08") || deviceType.equals("S10")  ){
                if(value == 1){
                    beaconMajorSetLL.setVisibility(View.VISIBLE);
                    beaconMajorSetLineLL.setVisibility(View.VISIBLE);
                    beaconMinorSetLL.setVisibility(View.VISIBLE);
                    beaconMinorSetLineLL.setVisibility(View.VISIBLE);
                    readBeaconMajorSet();
                    readBeaconMinorSet();
                }
                if(value == 2){
                    eddystoneNidSetLL.setVisibility(View.VISIBLE);
                    eddystoneNidSetLineLL.setVisibility(View.VISIBLE);
                    eddystoneBidSetLL.setVisibility(View.VISIBLE);
                    eddystoneBidSetLineLL.setVisibility(View.VISIBLE);
                    readEddystoneBidSet();
                    readEddystoneNidSet();
                }
            }
            if(deviceType.equals("S07")){
                if(value == 0){
                    broadcastCycleLL.setVisibility(View.GONE);
                    broadcastCycleLineLL.setVisibility(View.GONE);
                }else{
                    readBroadcastCycle();
                    broadcastCycleLL.setVisibility(View.VISIBLE);
                    broadcastCycleLineLL.setVisibility(View.VISIBLE);
                }
            }
            if(deviceType.equals("S08")){
                if(value == 0){
                    doorEnableLineLL.setVisibility(View.VISIBLE);
                    doorEnableLL.setVisibility(View.VISIBLE);
                    readDoorEnableStatus();
                }else{
                    doorEnableLineLL.setVisibility(View.GONE);
                    doorEnableLL.setVisibility(View.GONE);
                }
            }
        }
    }

    private void readOneWireDeviceResp(byte[] resp){
        int deviceCount = resp[2];
        String warningMsg = "";
        if(deviceCount > 0){
            for(int i = 0;i < deviceCount;i++){
                if(i * 8 + 8 + 3 >= resp.length){
                    break;
                }
                byte[] curDevice = Arrays.copyOfRange(resp,i * 8 + 3,i * 8 + 8 + 3);
                String deviceDesc = "ROM" + (i + 1) + ":" + MyUtils.bytes2HexString(curDevice,0).toUpperCase();
                warningMsg += deviceDesc;
                if(i != deviceCount - 1){
                    warningMsg += "\r\n";
                }
            }
        }else{
            warningMsg = getResources().getString(R.string.noDevice);
        }
        SweetAlertDialog warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
        warningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        warningDlg.setTitleText(getResources().getString(R.string.one_wire_device));
        warningDlg.setContentText(warningMsg);
        warningDlg.setCancelable(false);
        warningDlg.setConfirmText(getResources().getString(R.string.confirm));
        warningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                warningDlg.hide();
            }
        });
        warningDlg.show();
    }

    private void readPositiveNegativeWarningResp(byte[] resp){
        int port = resp[2];
        int mode = resp[3];
        int highVoltage = MyUtils.bytes2Short(resp,4);
        int lowVoltage = MyUtils.bytes2Short(resp,6);
        int samplingInterval = MyUtils.bytes2Short(resp,8);
        int ditheringIntervalHigh = resp[10] & 0xff;
        int ditheringIntervalLow = resp[11] & 0xff;
        Intent intent = new Intent(EditActivity.this,EditPositiveNegativeWarningActivity.class);
        intent.putExtra("port",port);
        intent.putExtra("mode",mode);
        intent.putExtra("highVoltage",highVoltage);
        intent.putExtra("lowVoltage",lowVoltage);
        intent.putExtra("samplingInterval",samplingInterval);
        intent.putExtra("ditheringIntervalHigh",ditheringIntervalHigh);
        intent.putExtra("ditheringIntervalLow",ditheringIntervalLow);
        startActivityForResult(intent,REQUEST_CHANGE_POSITIVE_NEGATIVE_WARNING);
    }

    private void readDinVoltageResp(byte[] resp){
        dinVoltageTV.setText(String.valueOf((int)resp[2]));
    }

    private void readBroadcastCycleResp(byte[] resp){
        if (deviceType.equals("S02") || deviceType.equals("S05") || deviceType.equals("S04")){
            int broadcast = (resp[2] << 8) + (resp[3]& 0xff);
            broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
        }else{
            if(deviceType.equals("S07") || deviceType.equals("S08")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1006");
                if(result >= 0){
                    int broadcast = (resp[2] << 8) + (resp[3]& 0xff);
                    broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
                }else{
                    int broadcast = resp[2]& 0xff;
                    broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
                }
            }else if(deviceType.equals("S10")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1005");
                if(result >= 0){
                    int broadcast = (resp[2] << 8) + (resp[3]& 0xff);
                    broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
                }else{
                    int broadcast = resp[2]& 0xff;
                    broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
                }
            }else{
                int broadcast = resp[2]& 0xff;
                broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
            }

        }
    }

    private void readSaveCount(){
        if(!isCurrentDeviceTypeFunc("saveCount")){
            return;
        }
        readData(MyUtils.controlFunc.get("saveCount").get("read"),uuid);
    }

    private void readSaveCountResp(byte[] resp){
        this.isSaveRecordStatus = resp[2] == 0 ? false : true;
        this.saveCount = ((resp[3] & 0xff) << 8)+ (resp[4]& 0xff);
        this.saveAlarmCount = ((resp[5]& 0xff) << 8) + (resp[6]& 0xff);
        saveCountTV.setText(String.valueOf(saveCount));
        alarmCountTV.setText(String.valueOf(saveAlarmCount));
        if(this.isSaveRecordStatus){
            this.btnStartRecord.setVisibility(View.GONE);
            this.btnStopRecord.setVisibility(View.VISIBLE);
        }else{
            this.btnStartRecord.setVisibility(View.VISIBLE);
            this.btnStopRecord.setVisibility(View.GONE);
        }
    }


    private void readAlarm(){
        if(!isCurrentDeviceTypeFunc("readAlarm")){
            return;
        }
        allBleHisData.clear();
        byte[] startDateValue = MyUtils.unSignedInt2Bytes(startTimestamp /1000);
        byte[] endDateValue = MyUtils.unSignedInt2Bytes(endTimestamp /1000);
        byte[] value = new byte[8];
        for(int i = 0;i < 4;i++){
            value[i] = startDateValue[i];
        } for(int i = 0;i < 4;i++){
            value[i+4] = endDateValue[i];
        }
        writeArrayData(MyUtils.controlFunc.get("readAlarm").get("read"),value,uuid);
    }



    private long parseAlarmData(byte[] realResp){
        if(realResp == null || realResp.length == 0){
            return 0;
        }
        long newTime=0;
        for(int i = 0;i < realResp.length ;i+=10){
            long timestamp = MyUtils.bytes2Integer(realResp,i+0);
            if(timestamp > newTime){
                newTime = timestamp;
            }
            int battery = realResp[i+4] & 0xff;
            int tempSource = MyUtils.bytes2Short(realResp,i+5);
            float temp = (tempSource & 0x7fff) * ((tempSource & 0x8000) == 0x8000 ? -1 : 1) / 100.0f;
            int humidity = realResp[i+7] & 0xff;
            int doorStatus = realResp[i+8] & 0xff;
            BleHisData bleHisData = new BleHisData();
            bleHisData.setBattery(battery);
            bleHisData.setProp(doorStatus);
            bleHisData.setTemp(temp);
            bleHisData.setDate(new Date(timestamp*1000));
            bleHisData.setHumidity(humidity);
            bleHisData.setAlarm(realResp[i+9]);
            allBleHisData.add(bleHisData);
        }
        String percent = String.format("%.2f%%",((newTime * 1000 - this.startTimestamp) / 1.0f / (this.endTimestamp - this.startTimestamp)  * 100));
        waitingCancelDlg.setTitleText(percent);
        return newTime;
    }



    private void readAlarmResp(byte[] resp){
        byte[] realResp = Arrays.copyOfRange(resp,2,resp.length - 1);
        long time = parseAlarmData(realResp);
        if(time != 0){
            readNextAlarm();
        }else{
            readAlarmSucc();
        }
    }


    private void readNextAlarm(){
        if(!isCurrentDeviceTypeFunc("readAlarm")){
            return;
        }
        byte[] endDateValue = MyUtils.unSignedInt2Bytes(endTimestamp /1000);
        writeArrayData(MyUtils.controlFunc.get("readNextAlarm").get("read"),endDateValue,uuid);
    }

    private void readNextAlarmResp(byte[] resp){
        byte[] realResp = Arrays.copyOfRange(resp,2,resp.length - 1);
        long time = parseAlarmData(realResp);
        if(time != 0){
            readNextAlarm();
        }else{
            readAlarmSucc();
        }
    }

    private void readAlarmSucc() {
        waitingCancelDlg.hide();
        if(allBleHisData.size() == 0){
            Toast.makeText(EditActivity.this,R.string.not_find_data,Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(EditActivity.this, HistoryReportActivity.class);
        intent.putExtra("mac", mac);
        intent.putExtra("deviceType", deviceType);
        intent.putExtra("id", id);
        intent.putExtra("reportType", "alarm");
        intent.putExtra("tempAlarmUp",tempAlarmUp);
        intent.putExtra("tempAlarmDown",tempAlarmDown);
        intent.putExtra("humidityAlarmUp",humidityAlarmUp);
        intent.putExtra("humidityAlarmDown",humidityAlarmDown);
        intent.putExtra("deviceName",deviceName);
        intent.putExtra("startDate",startTimestamp);
        intent.putExtra("endDate",endTimestamp);
        startActivity(intent);
    }

    private boolean startHistory = false;
    private int historyIndex = 0;
    private void readHistory(){
        if(!isCurrentDeviceTypeFunc("readOriginData") || historyIndex > 11){
            return;
        }
        byte[] value = new byte[]{(byte)historyIndex};
        writeArrayData(MyUtils.controlFunc.get("readOriginData").get("read"),value,uuid);
        historyIndex++;
    }

    public static ArrayList<ArrayList<byte[]>> originHistoryList = new ArrayList<ArrayList<byte[]>>();
    public static  ArrayList<BleHisData> allBleHisData = new ArrayList<>();
    private ArrayList<byte[]> curHistoryList;
    private void readHistoryResp(byte[] resp){
        if(curHistoryList == null){
            curHistoryList = new ArrayList<>();
        }
        if (curHistoryList.size() < 17){
            byte[] realData = Arrays.copyOfRange(resp,2,resp.length - 1);
            boolean isExist = false;
            for (byte[] item : curHistoryList){
                if(Arrays.equals(item,realData)){
                    isExist = true;
                    break;
                }
            }
            if(!isExist){
                System.out.println(MyUtils.bytes2HexString(realData,0));
                curHistoryList.add(realData);
            }
            float percent = (originHistoryList.size() * 17 + curHistoryList.size()) / 204.0f;
            waitingCancelDlg.setTitleText(String.format("%.2f%%",percent* 100));
        }
        if(curHistoryList.size() == 17){
            originHistoryList.add(curHistoryList);
            float percent = (originHistoryList.size() * 17) / 204.0f;
            waitingCancelDlg.setTitleText(String.format("%.2f%%",percent* 100));
            curHistoryList = new ArrayList<>();
            if (historyIndex < 12){
                readHistory();
            }else{
                ArrayList<byte[]> mergeData = MyUtils.mergeOriginHisData(originHistoryList);
                allBleHisData.clear();
                for(byte[] byteDataArray : mergeData){
                    boolean dataCorrect = MyUtils.checkOriginHisDataCrc(byteDataArray);
                    if(dataCorrect){
                        if(deviceType.equals("S10")){
                            if(extSensorType == 1){
                                ArrayList<BleHisData> bleHisDataList = MyUtils.parseS10BleGX112HisData(byteDataArray);
                                allBleHisData.addAll(bleHisDataList);
                            }
                        }else{
                            ArrayList<BleHisData> bleHisDataList = MyUtils.parseS02BleHisData(byteDataArray);
                            allBleHisData.addAll(bleHisDataList);
                        }
                    }else{
//                        Toast.makeText(EditActivity.this,"Data not correct",Toast.LENGTH_LONG).show();
                    }
                }
                waitingCancelDlg.hide();
                ArrayList<BleHisData> bleHisDataList = new ArrayList<>();
                for(BleHisData bleHisData : allBleHisData){
                    if(bleHisData.getDate().getTime() <= endTimestamp && bleHisData.getDate().getTime() >= startTimestamp){
                        bleHisDataList.add(bleHisData);
                    }
                }
                allBleHisData.clear();
                allBleHisData.addAll(bleHisDataList);
                if(allBleHisData.size() == 0){
                    Toast.makeText(EditActivity.this,R.string.not_find_data,Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(EditActivity.this,HistoryReportActivity.class);
                intent.putExtra("mac",mac);
                intent.putExtra("deviceType",deviceType);
                intent.putExtra("id",id);
                intent.putExtra("reportType","history");
                intent.putExtra("tempAlarmUp",tempAlarmUp);
                intent.putExtra("tempAlarmDown",tempAlarmDown);
                intent.putExtra("humidityAlarmUp",humidityAlarmUp);
                intent.putExtra("humidityAlarmDown",humidityAlarmDown);
                intent.putExtra("deviceName",deviceName);
                intent.putExtra("startDate",startTimestamp);
                intent.putExtra("endDate",endTimestamp);
                startActivity(intent);
            }
        }
    }



    private void startRecord(){
        if(!isCurrentDeviceTypeFunc("startRecord")){
            return;
        }
        Date now = new Date();
//        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("startRecord").get("write"),null,uuid);
    }

    private void startRecordResp(byte[] resp){
        readSaveCount();
    }

    private void readDoutStatus(int port){
        if(!isCurrentDeviceTypeFunc("doutStatus")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("doutStatus").get("read"),new byte[]{(byte)port},uuid);
    }


    private void readPositiveNegativeWarning(int port){
        if(!isCurrentDeviceTypeFunc("setPositiveNegativeWarning")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("setPositiveNegativeWarning").get("read"),new byte[]{(byte)port}, uuid);
    }
    private void  readOneWireDevice(){
        if(!isCurrentDeviceTypeFunc("getOneWireDevice")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("getOneWireDevice").get("read"),null,uuid);
    }

    private void readBroadcastType(){
        if(!isCurrentDeviceTypeFunc("broadcastType")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("broadcastType").get("read"),null,uuid);
    }


    private void readExtSensorType(){
        if(!isCurrentDeviceTypeFunc("readExtSensorType")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("readExtSensorType").get("read"),null,uuid);
    }

    private void readBeaconMajorSet(){
        if(!isCurrentDeviceTypeFunc("beaconMajorSet")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("beaconMajorSet").get("read"),null,uuid);
    }
    private void readBeaconMinorSet(){
        if(!isCurrentDeviceTypeFunc("beaconMinorSet")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("beaconMinorSet").get("read"),null,uuid);
    }
    private void readEddystoneNidSet(){
        if(!isCurrentDeviceTypeFunc("eddystoneNIDSet")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("eddystoneNIDSet").get("read"),null,uuid);
    }
    private void readEddystoneBidSet(){
        if(!isCurrentDeviceTypeFunc("eddystoneBIDSet")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("eddystoneBIDSet").get("read"),null,uuid);
    }
    private void readBeaconMajorSetResp(byte[] resp){
        int value = MyUtils.bytes2Short(resp,2);
        beaconMajorSetTV.setText(String.valueOf(value));
    }
    private void readBeaconMinorSetResp(byte[] resp){
        int value = MyUtils.bytes2Short(resp,2);
        beaconMinorSetTV.setText(String.valueOf(value));
    }
    private void readEddystoneNidSetResp(byte[] resp){
        byte[] content = Arrays.copyOfRange(resp,2,resp.length - 1);
        eddystoneNidSetTV.setText("0x" + MyUtils.bytes2HexString(content,0));
    }
    private void readEddystoneBidSetResp(byte[] resp){
        byte[] content = Arrays.copyOfRange(resp,2,resp.length - 1);
        eddystoneBidSetTV.setText("0x" + MyUtils.bytes2HexString(content,0));
    }

    private void readRs485BaudRate(){
        if(!isCurrentDeviceTypeFunc("rs485BaudRate")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("rs485BaudRate").get("read"),null, uuid);
    }

    private void readOneWireWorkMode(){
        if(!isCurrentDeviceTypeFunc("oneWireWorkMode")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("oneWireWorkMode").get("read"),null,uuid);
    }

    private void readDinVoltage(){
        if(!isCurrentDeviceTypeFunc("readDinVoltage")){
            return;
        }
        readData(MyUtils.controlFunc.get("readDinVoltage").get("read"), uuid);
    }

    private void readDinVoltage(int port){
        if(!isCurrentDeviceTypeFunc("readDinVoltage")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("readDinVoltage").get("read"),new byte[]{(byte)port},uuid);
    }

    private void readVinVoltage(){
        if(!isCurrentDeviceTypeFunc("readVinVoltage")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("readVinVoltage").get("read"),null,uuid);
    }

    private void readAinStatus(int port){
        if(!isCurrentDeviceTypeFunc("readAinVoltage")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("readAinVoltage").get("read"),new byte[]{(byte)port},uuid);
    }
    private void stopRecord(){
        if(!isCurrentDeviceTypeFunc("stopRecord")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("stopRecord").get("write"),null,uuid);
    }
    private void stopRecordResp(byte[] resp){
        readSaveCount();
    }

    private void clearRecord(){
        if(!isCurrentDeviceTypeFunc("clearRecord")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("clearRecord").get("write"),null,uuid);
    }
    private void clearRecordResp(byte[] resp){
        readSaveCount();
    }

    private void readSaveInterval(){
        if(!isCurrentDeviceTypeFunc("saveInterval")){
            return;
        }
        readData(MyUtils.controlFunc.get("saveInterval").get("read"),uuid);
    }

    private void readSaveIntervalResp(byte[] resp){
        this.saveInterval = (resp[2] << 8) + (resp[3] & 0xff);
        saveRecordIntervalTV.setText(String.valueOf(saveInterval) + "s");
    }


    private void fixTime(){
        if(!isCurrentDeviceTypeFunc("time")){
            return;
        }
        Date now = new Date();
        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("time").get("write"),value,uuid);

    }

    private void readVersion(){
        if(!isCurrentDeviceTypeFunc("firmware")){
            return;
        }
        readData(MyUtils.controlFunc.get("firmware").get("read"),uuid);
    }
    private void readVersionResp(byte[] resp){
        if(resp.length == 6){
            if (resp[2] == 2){
                modelTV.setText("TSTH1-B");
            }else if (resp[2] == 4){
                modelTV.setText("TSDT1-B");
            }else if (resp[2] == 5){
                modelTV.setText("TSR1-B");
            }
            String hardware = MyUtils.parseHardwareVersion(resp[3]);
            hardwareTV.setText("V" + hardware);
            software = String.valueOf(resp[4]);
            softwareTV.setText("V"+software);
        }else if(resp.length == 7 || resp.length == 8){
            if (resp[2] == 0x07){
                modelTV.setText("T-button");
            }else if (resp[2] == 0x08){
                modelTV.setText("T-sense");
            }else if (resp[2] == 0x09){
                modelTV.setText("T-hub");
            }else if (resp[2] == 0x0a){
                modelTV.setText("T-one");
            }
            String hardware = MyUtils.parseHardwareVersion(resp[3]);
            hardwareTV.setText("V" + hardware);
            software = MyUtils.parseSoftwareVersion(resp,4);
            softwareTV.setText("V"+software);
        }

        showUpgradeBtn();
        showBetaUpgradeBtn();
    }
    private void readHumidityAlarm(){
        if(!isCurrentDeviceTypeFunc("humidityAlarm")){
            return;
        }
        readData(MyUtils.controlFunc.get("humidityAlarm").get("read"),uuid);
    }
    private void readHumidityAlarmResp(byte[] resp){
        int humidityAlarmUpTemp = (resp[2] << 8) + (resp[3]& 0xff);
        int humidityAlarmDownTemp = (resp[4] << 8) + (resp[5]& 0xff);
        if (humidityAlarmUpTemp == 4095){
            humidityAlarmUp = 4095;
            humidityAlarmUpOpen = false;
            humidityHighAlarmTV.setText("");
        }else{
            humidityAlarmUp = (humidityAlarmUpTemp & 0xfff);
            humidityAlarmUpOpen = true;
            humidityHighAlarmTV.setText(String.valueOf(humidityAlarmUp));
        }
        if (humidityAlarmDownTemp == 4095){
            humidityAlarmDown = 4095;
            humidityAlarmDownOpen = false;
            humidityLowAlarmTV.setText("");
        }else{
            humidityAlarmDown = (humidityAlarmDownTemp & 0xfff);
            humidityAlarmDownOpen = true;
            humidityLowAlarmTV.setText(String.valueOf(humidityAlarmDown));
        }
    }
    private void readTempAlarm(){
        if(!isCurrentDeviceTypeFunc("tempAlarm")){
            return;
        }
        readData(MyUtils.controlFunc.get("tempAlarm").get("read"),uuid);
    }
    private void readTempAlarmResp(byte[] resp){
        int tempAlarmUpTemp = (resp[2] << 8) + (resp[3]& 0xff);
        int tempAlarmDownTemp = (resp[4] << 8) + (resp[5]& 0xff);
        if (tempAlarmUpTemp == 4095){
            tempAlarmUp = 4095;
            tempAlarmUpOpen = false;
            tempHighAlarmTV.setText("");
        }else{
            tempAlarmUpOpen = true;
            if ((tempAlarmUpTemp & 0x8000) == 0x8000){
                tempAlarmUp = (tempAlarmUpTemp & 0xfff) * -1 * 0.1f;
            }else{
                tempAlarmUp = (tempAlarmUpTemp & 0xfff) * 0.1f;
            }
            tempHighAlarmTV.setText(String.format("%s %s",BleDeviceData.getCurTemp(EditActivity.this,tempAlarmUp), BleDeviceData.getCurTempUnit(EditActivity.this)));
        }
        if (tempAlarmDownTemp == 4095){
            tempAlarmDown = 4095;
            tempAlarmDownOpen = false;
            tempLowAlarmTV.setText("");
        }else{
            if ((tempAlarmDownTemp & 0x8000) == 0x8000){
                tempAlarmDown = (tempAlarmDownTemp & 0xfff) * -1 * 0.1f;
            }else{
                tempAlarmDown = (tempAlarmDownTemp & 0xfff) * 0.1f;
            }

            tempAlarmDownOpen = true;
            tempLowAlarmTV.setText(String.format("%s %s",BleDeviceData.getCurTemp(EditActivity.this,tempAlarmDown),BleDeviceData.getCurTempUnit(EditActivity.this)));
        }
    }

    private void readBtnTriggerTime(){
        if(!isCurrentDeviceTypeFunc("btnTriggerTime")){
            return;
        }
        readData(MyUtils.controlFunc.get("btnTriggerTime").get("read"),uuid);
    }

    private void readBtnTriggerTimeResp(byte[] resp){
        btnTriggerTimeTV.setText(String.valueOf((resp[2]& 0xff) * 100));
    }

    private void readLedOpenStatus(){
        if(!isCurrentDeviceTypeFunc("ledOpen")){
            return;
        }
        readData(MyUtils.controlFunc.get("ledOpen").get("read"),uuid);
    }

    private void readLedOpenStatusResp(byte[] resp){
        if (resp[2] == 0){
            ledSwitch.setSwitchStatus(false);
        }else{
            ledSwitch.setSwitchStatus(true);
        }
    }

    private void readDoorEnableStatus(){
        if(!isCurrentDeviceTypeFunc("doorEnable")){
            return;
        }
        readData(MyUtils.controlFunc.get("doorEnable").get("read"),uuid);
    }

    private void readDoorEnableStatusResp(byte[] resp){
        if (resp[2] == 0){
            doorEnableSwitch.setSwitchStatus(false);
        }else{
            doorEnableSwitch.setSwitchStatus(true);
        }
    }


    private void readRelayStatus(){
        if(!isCurrentDeviceTypeFunc("relay")){
            return;
        }
        readData(MyUtils.controlFunc.get("relay").get("read"),uuid);
    }
    private void readRelayStatusResp(byte[] resp){
        if (resp[2] == 0){
            relayStatusTV.setText("NC");
            relayStatus = false;
            relaySwitch.setSwitchStatus(false);

        }else{
            relayStatusTV.setText("NO");
            relayStatus = true;
            relaySwitch.setSwitchStatus(true);
        }
        if(resp.length >= 5){
            relayFlashEnable = resp[3] == 0x01;
        }

    }
    private void readLightSensorOpen(){
        if(!isCurrentDeviceTypeFunc("lightSensorOpen")){
            return;
        }
        readData(MyUtils.controlFunc.get("lightSensorOpen").get("read"),uuid);
    }
    private void readLightSensorOpenStatusResp(byte[] resp){
        if (resp[2] == 0){
            lightSensorEnableSwitch.setSwitchStatus(false);
        }else{
            lightSensorEnableSwitch.setSwitchStatus(true);
        }
    }


    private void readDinStatusEvent(){
        if(!isCurrentDeviceTypeFunc("dinStatusEvent")){
            return;
        }
        readData(MyUtils.controlFunc.get("dinStatusEvent").get("read"), uuid);
    }
    private void readRs485Enable(){
        if(!isCurrentDeviceTypeFunc("rs485Enable")){
            return;
        }
        readData(MyUtils.controlFunc.get("rs485Enable").get("read"), uuid);
    }
    private void readRs485EnableStatusResp(byte[] resp){
        if (resp[2] == 0){
            rs485EnableSwitch.setSwitchStatus(false);
        }else{
            rs485EnableSwitch.setSwitchStatus(true);
        }
    }

    private void readLongRangeEnable(){
        if(!isCurrentDeviceTypeFunc("longRangeEnable")){
            return;
        }
        readData(MyUtils.controlFunc.get("longRangeEnable").get("read"),uuid);
    }
    private void readLongRangeEnableStatusResp(byte[] resp){
        if (resp[2] == 0){
            longRangeEnableSwitch.setSwitchStatus(false);
        }else{
            longRangeEnableSwitch.setSwitchStatus(true);
        }
    }

    private void readGSensorEnable(){
        if(!isCurrentDeviceTypeFunc("gSensorEnable")){
            return;
        }
        readData(MyUtils.controlFunc.get("gSensorEnable").get("read"),uuid);
    }
    private void readGSensorEnableStatusResp(byte[] resp){
        gSensorDetectionDurationLL.setVisibility(View.GONE);
        gSensorDetectionDurationLineLL.setVisibility(View.GONE);
        gSensorDetectionIntervalLL.setVisibility(View.GONE);
        gSensorDetectionIntervalLineLL.setVisibility(View.GONE);
        gSensorSensitivityLL.setVisibility(View.GONE);
        gSensorSensitivityLineLL.setVisibility(View.GONE);
        if (resp[2] == 0){
            gSensorEnableSwitch.setSwitchStatus(false);
        }else{
            gSensorEnableSwitch.setSwitchStatus(true);
            if(deviceType.equals("S08")){
                gSensorDetectionDurationLL.setVisibility(View.VISIBLE);
                gSensorDetectionDurationLineLL.setVisibility(View.VISIBLE);
                gSensorDetectionIntervalLL.setVisibility(View.VISIBLE);
                gSensorDetectionIntervalLineLL.setVisibility(View.VISIBLE);
                gSensorSensitivityLL.setVisibility(View.VISIBLE);
                gSensorSensitivityLineLL.setVisibility(View.VISIBLE);
                readGSensorDetectionInterval();
                readGSensorSensitivity();
                readGSensorDetectionDuration();
            }
        }
    }
    private void readGSensorDetectionInterval(){
        if(!isCurrentDeviceTypeFunc("gSensorDetectionInterval")){
            return;
        }
        readData(MyUtils.controlFunc.get("gSensorDetectionInterval").get("read"),uuid);
    }
    private void readGSensorSensitivity(){
        if(!isCurrentDeviceTypeFunc("gSensorSensitivity")){
            return;
        }
        readData(MyUtils.controlFunc.get("gSensorSensitivity").get("read"),uuid);
    }
    private void readGSensorSensitivityResp(byte[] resp){
        this.saveInterval = resp[2]& 0xff ;
        gSensorSensitivityTV.setText(String.valueOf(resp[2]& 0xff ));
    }
    private void readGSensorDetectionIntervalResp(byte[] resp){
        this.saveInterval = resp[2]& 0xff ;
        gSensorDetectionIntervalTV.setText(String.valueOf(resp[2]& 0xff) );
    }
    private void readGSensorDetectionDurationResp(byte[] resp){
        this.saveInterval = resp[2]& 0xff ;
        gSensorDetectionDurationTV.setText(String.valueOf(resp[2]& 0xff ));
    }
    private void readGSensorDetectionDuration(){
        if(!isCurrentDeviceTypeFunc("gSensorDetectionDuration")){
            return;
        }
        readData(MyUtils.controlFunc.get("gSensorDetectionDuration").get("read"),uuid);
    }

    private void readSendInstructionSequenceResp(byte[] value){
        String warningMsg = MyUtils.bytes2HexString(value,2);
        if(warningMsg.length() > 2){
            warningMsg = warningMsg.substring(0,warningMsg.length() - 2);
        }else{
            warningMsg = "Error msg";
        }
        SweetAlertDialog warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
        warningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        warningDlg.setTitleText(getResources().getString(R.string.receiveInstructionSequence));
        warningDlg.setContentText(warningMsg);
        warningDlg.setCancelable(false);
        warningDlg.setConfirmText(getResources().getString(R.string.confirm));
        String finalWarningMsg = warningMsg;
        warningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ClipboardManager cm = (ClipboardManager) EditActivity.this.getSystemService(EditActivity.this.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(finalWarningMsg);
                Toast.makeText(EditActivity.this, R.string.copyToClipboard, Toast.LENGTH_SHORT).show();
                warningDlg.hide();
            }
        });
        warningDlg.show();
    }


    private int rs485DataErrorCount = 0;
    private Date lastCheckRs485DataTime = null;
    private Date lastRs485DataErrorShow = null;
    private void readRS485SendDataResp(byte[] value){
        String warningMsg;
        byte status = value[2];
        SweetAlertDialog warningDlg  ;
        boolean isErrorMsg = false;
        if(status == 0){
            SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
            boolean isUseHex = sharedPreferences.getBoolean("rs485_send_data_use_hex",true);
            if(value.length > 3){
                byte[] content = Arrays.copyOfRange(value,3,value.length - 1);
                if(isUseHex){
                    warningMsg = MyUtils.bytes2HexString(content,0);
                }else{
                    warningMsg = new String(content);
                }
            }else{
                warningMsg = "Error msg";
            }
            warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
            rs485DataErrorCount = 0;
        }else{
            isErrorMsg = true;
            warningDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE);
            if((status & 0x01) == 0x01){
                warningMsg = "Receive buffer overflow";
            }else if((status & 0x02) == 0x02){
                warningMsg = "Overrun error";
            }else if((status & 0x04) == 0x04){
                warningMsg = "Parity error";
            }else if((status & 0x08) == 0x08){
                warningMsg = "Framing error occurred";
            }else if((status & 0x10) == 0x10){
                warningMsg = "Break condition";
            }else{
                warningMsg = "Unknown error";
            }
            if((status & 0x04) == 0x04 || (status & 0x08) == 0x08 || (status & 0x10) == 0x10){
                if(lastCheckRs485DataTime == null){
                    lastCheckRs485DataTime = new Date();
                    rs485DataErrorCount = 1;
                }else{
                    Date now = new Date();
                    if(now.getTime() - lastCheckRs485DataTime.getTime() > 3000){
                        rs485DataErrorCount = 1;
                        lastCheckRs485DataTime = new Date();
                    }else{
                        rs485DataErrorCount++;
                    }
                }
                if(rs485DataErrorCount > 6){
                    rs485DataErrorCount = 0;
                    Toast.makeText(EditActivity.this,R.string.rs485_baud_set_error_warning,Toast.LENGTH_LONG).show();
                }
            }

        }
        if(isErrorMsg){
            if(lastRs485DataErrorShow == null){
                lastRs485DataErrorShow = new Date();
            }else{
                Date now = new Date();
                if(now.getTime() - lastRs485DataErrorShow.getTime() > 3000){
                    lastRs485DataErrorShow = new Date();
                }else{
                    return;
                }
            }
        }
        warningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        warningDlg.setTitleText(getResources().getString(R.string.receiveData));
        warningDlg.setContentText(warningMsg);
        warningDlg.setCancelable(false);
        warningDlg.setConfirmText(getResources().getString(R.string.confirm));
        String finalWarningMsg = warningMsg;
        warningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                ClipboardManager cm = (ClipboardManager) EditActivity.this.getSystemService(EditActivity.this.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(finalWarningMsg);
                Toast.makeText(EditActivity.this, R.string.copyToClipboard, Toast.LENGTH_SHORT).show();
                warningDlg.hide();
            }
        });
        warningDlg.show();
    }

    private void readData(int cmd,UUID curUUID){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,null);

        waitingSendMsgQueue.offer(new WriteSensorObj(curUUID,content));
//        mClient.writeNoRsp(mac, serviceId, curUUID, content, new BleWriteResponse() {
//            @Override
//            public void onResponse(int code) {
//                if (code == REQUEST_SUCCESS) {
//
//                }
//            }
//        });
    }


    private void writeStrData(int cmd,String dataStr,UUID curUUID){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] data = dataStr.getBytes();
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,data);

        if (content != null && content.length > 0){
            waitingSendMsgQueue.offer(new WriteSensorObj(curUUID,content));
//            mClient.writeNoRsp(mac, serviceId, curUUID, content, new BleWriteResponse() {
//                @Override
//                public void onResponse(int code) {
//                    if (code == REQUEST_SUCCESS) {
//
//                    }
//                }
//            });
        }

    }
    private void writeArrayData(int cmd,byte[] realContent,UUID curUUID){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,realContent);
        if (content != null && content.length > 0){

            waitingSendMsgQueue.offer(new WriteSensorObj(curUUID,content));
//            mClient.writeNoRsp(mac, serviceId, curUUID, content, new BleWriteResponse() {
//                @Override
//                public void onResponse(int code) {
//                    if (code == REQUEST_SUCCESS) {
//
//                    }
//                }
//            });
        }

    }

    private void writePwd(String pwd){
        writeStrData(MyUtils.controlFunc.get("password").get("write"),pwd,uuid);
    }

    private void writeConnectPwd(String pwd){
        writeStrData(MyUtils.controlFunc.get("connectPwd").get("write"),pwd,uuid);
    }

    private void writeRS485SendData(String cmd){
        byte[] data = MyUtils.hexString2Bytes(cmd);
        writeArrayData(MyUtils.controlFunc.get("rs485SendData").get("write"),data,uuid);
    }

    private void writeSendInstructionSequence(String cmd){
        byte[] data = MyUtils.hexString2Bytes(cmd);
        writeArrayData(MyUtils.controlFunc.get("sendCmdSequence").get("write"),data,uuid);
    }

    private void writePositiveNegativeWaning(int port,int mode,int highVoltage,int lowVoltage,int samplingInterval,int ditheringIntervalHigh,int ditheringIntervalLow){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[]{};
        try {
            outputStream.write(port);
            outputStream.write(mode);
            outputStream.write(MyUtils.short2Bytes(highVoltage));
            outputStream.write(MyUtils.short2Bytes(lowVoltage));
            outputStream.write(MyUtils.short2Bytes(samplingInterval));
            outputStream.write(ditheringIntervalHigh);
            outputStream.write(ditheringIntervalLow);
            data = outputStream.toByteArray();
        }catch (Exception e){

        }
        writeArrayData(MyUtils.controlFunc.get("setPositiveNegativeWarning").get("write"),data,uuid);
    }

    private void writeBroadcastType(int broadcastType){
        byte[] data = new byte[]{(byte)broadcastType};
        writeArrayData(MyUtils.controlFunc.get("broadcastType").get("write"),data,uuid);
    }

    private void writeRs485BaudRate(int baudRate){
        byte[] data = MyUtils.unSignedInt2Bytes(baudRate);
        writeArrayData(MyUtils.controlFunc.get("rs485BaudRate").get("write"),data, uuid);
    }

    private void  writeOneWireWorkMode(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("oneWireWorkMode").get("write"),data,uuid);
    }

    private void writeDoutStatus(int port,int value){
        byte[] data = new byte[]{(byte)port,(byte)value};
        writeArrayData(MyUtils.controlFunc.get("doutStatus").get("write"),data,uuid);
    }

    private void  writeDeviceName(String name){
        writeStrData(MyUtils.controlFunc.get("deviceName").get("write"), name,uuid);
    }


    private void writeDinStatusEvent(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("dinStatusEvent").get("write"),data,uuid);
    }

    private void writeTransmittedPower(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("transmittedPower").get("write"),data,uuid);
    }
    private void writeBroadcastData(int value){
        byte[]  data  ;
        if (deviceType.equals("S02") || deviceType.equals("S05") || deviceType.equals("S04")){
            data = MyUtils.short2Bytes(value);
        }else{
            if(deviceType.equals("S07") || deviceType.equals("S08")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");

                int result = formatSoftware.compareToIgnoreCase("1006");
                if(result >= 0){
                    data = MyUtils.short2Bytes(value);
                }else{
                    data = new byte[]{(byte)value};
                }
            }else if(deviceType.equals("S10")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1005");
                if(result >= 0){
                    data = MyUtils.short2Bytes(value);
                }else{
                    data = new byte[]{(byte)value};
                }
            }else{
                data = new byte[]{(byte)value};
            }
        }
        writeArrayData(MyUtils.controlFunc.get("broadcastCycle").get("write"),data,uuid);
    }
    private void writeSaveRecordIntervalData(int value){
        byte[] data = MyUtils.short2Bytes(value);
        writeArrayData(MyUtils.controlFunc.get("saveInterval").get("write"),data,uuid);
    }

    private void writeGSensorSensitivity(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("gSensorSensitivity").get("write"),data,uuid);
    }
    private void writeGSensorDetectionDuration(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("gSensorDetectionDuration").get("write"),data,uuid);
    }
    private void writeGSensorDetectionInterval(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("gSensorDetectionInterval").get("write"),data,uuid);
    }

    private void writeBeaconMajorSet(int beaconMajorSet){
        byte[] data = MyUtils.short2Bytes(beaconMajorSet);
        writeArrayData(MyUtils.controlFunc.get("beaconMajorSet").get("write"),data,uuid);
    }
    private void writeBeaconMinorSet(int beaconMinorSet){
        byte[] data = MyUtils.short2Bytes(beaconMinorSet);
        writeArrayData(MyUtils.controlFunc.get("beaconMinorSet").get("write"),data,uuid);
    }
    private void writeEddystoneNidSet(String saveEddystoneNidSetValue){
        byte[] data = MyUtils.hexString2Bytes(saveEddystoneNidSetValue);
        writeArrayData(MyUtils.controlFunc.get("eddystoneNIDSet").get("write"),data,uuid);
    }
    private void writeEddystoneBidSet(String saveEddystoneBidSetValue){
        byte[] data = MyUtils.hexString2Bytes(saveEddystoneBidSetValue);
        writeArrayData(MyUtils.controlFunc.get("eddystoneBIDSet").get("write"),data,uuid);
    }

    private void writeBtnTriggerTime(int btnTriggerTime){
        byte[] data = new byte[]{(byte)btnTriggerTime};
        writeArrayData(MyUtils.controlFunc.get("btnTriggerTime").get("write"),data,uuid);
    }

    private void writeHumidityAlarmData(int upValue,int downValue){
        int convertUpValue = upValue;
        if(upValue < 0){
            convertUpValue = (-1 * upValue) | 0x8000;
        }
        int convertDownValue = downValue;
        if(downValue < 0){
            convertDownValue = (-1 * downValue) | 0x8000;
        }
        byte[] upData = MyUtils.short2Bytes(convertUpValue);
        byte[] downData = MyUtils.short2Bytes(convertDownValue);
        byte[] data = new byte[4];
        data[0] = upData[0];
        data[1] = upData[1];
        data[2] = downData[0];
        data[3] = downData[1];
        writeArrayData(MyUtils.controlFunc.get("humidityAlarm").get("write"),data,uuid);
    }

    private void writeTempAlarmData(int upValue,int downValue){
        int convertUpValue = upValue;
        if(upValue < 0){
            convertUpValue = (-1 * upValue) | 0x8000;
        }
        int convertDownValue = downValue;
        if(downValue < 0){
            convertDownValue = (-1 * downValue) | 0x8000;
        }
        byte[] upData = MyUtils.short2Bytes(convertUpValue);
        byte[] downData = MyUtils.short2Bytes(convertDownValue);
        byte[] data = new byte[4];
        data[0] = upData[0];
        data[1] = upData[1];
        data[2] = downData[0];
        data[3] = downData[1];
        writeArrayData(MyUtils.controlFunc.get("tempAlarm").get("write"),data,uuid);
    }

    private void writeLightSensorEnableStatus(){
        byte[] data;
        if(lightSensorEnableSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("lightSensorOpen").get("write"),data,uuid);
    }

    private void writeRs485EnableStatus(){
        byte[] data;
        if(rs485EnableSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("rs485Enable").get("write"),data, uuid);
    }

    private void writeLongRangeEnableStatus(){
        byte[] data;
        if(longRangeEnableSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("longRangeEnable").get("write"),data,uuid);
    }

    private void writeGSensorEnableStatus(){
        byte[] data;
        if(gSensorEnableSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("gSensorEnable").get("write"),data,uuid);
    }


    private void writeDoorEnableStatus(){
        byte[] data;
        if(doorEnableSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("doorEnable").get("write"),data,uuid);
    }

    private void writeLedOpenStatus(){
        byte[] data;
        if(ledSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("ledOpen").get("write"),data,uuid);
    }
    private void writeRelayStatus(){
        byte[] data;
        if(!relayStatus){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("relay").get("write"),data,uuid);
    }

    private void writeDelayRelayStatus(int delayTime){
        byte[] data = new byte[3];
        if(!relayStatus){
            data[0] = 0x01;
        }else{
            data[0] = 0x00;
        }
        byte[] relayTimeBytes = MyUtils.short2Bytes(delayTime);
        data[1] = relayTimeBytes[0];
        data[2] = relayTimeBytes[1];
        writeArrayData(MyUtils.controlFunc.get("relay").get("write"),data,uuid);
    }

    private void writeFlashingRelayStatus(){
        byte[] data = new byte[2];
        data[0] = 0x02;
        data[1] =(relayFlashEnable ?  (byte)0x00 :  (byte)0x01);
        writeArrayData(MyUtils.controlFunc.get("relay").get("write"),data,uuid);
    }

    private void writePulseRelayStatus(int cycleTime,int initEnableTime,int toggleTime,int recoverTime){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(0x03);
            outputStream.write(MyUtils.short2Bytes(cycleTime));
            outputStream.write(MyUtils.short2Bytes(initEnableTime));
            outputStream.write(MyUtils.short2Bytes(toggleTime));
            outputStream.write(recoverTime);
            writeArrayData(MyUtils.controlFunc.get("relay").get("write"),outputStream.toByteArray(),uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSecondPulseRelayStatus(int starLevel,int highLevelPulseWidthTime,int lowLevelPulseWidthTime,int PulseCount){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(0x04);
            outputStream.write(starLevel);
            outputStream.write(highLevelPulseWidthTime);
            outputStream.write(lowLevelPulseWidthTime);
            outputStream.write(MyUtils.short2Bytes(PulseCount));
            writeArrayData(MyUtils.controlFunc.get("relay").get("write"),outputStream.toByteArray(),uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeShutdown(){
        writeArrayData(MyUtils.controlFunc.get("shutdown").get("write"),null,uuid);
    }

    private void writeResetFactory(){
        writeArrayData(MyUtils.controlFunc.get("resetFactory").get("write"),null,uuid);
    }

    private void initDiffUI(){
        saveRecordIntervalLL = (LinearLayout)findViewById(R.id.line_save_record_interval);
        humidityLowAlarmLL = (LinearLayout)findViewById(R.id.line_humidity_low_alarm);
        humidityHighAlarmLL = (LinearLayout)findViewById(R.id.line_humidity_high_alarm);
        recordControlLL = (LinearLayout)findViewById(R.id.line_record_control);
        clearRecordLL = (LinearLayout)findViewById(R.id.line_clear_record);
        transmittedPowerLL = (LinearLayout)findViewById(R.id.line_transmitted_power);
        transmittedPowerLineLL = (LinearLayout)findViewById(R.id.line_line_transmitted_power);
        relayLL = (LinearLayout)findViewById(R.id.line_relay);
        relayLineLL = (LinearLayout)findViewById(R.id.line_line_relay);
        relayFlashingLL = (LinearLayout)findViewById(R.id.line_flashing_relay);
        lightSensorEnableLL = (LinearLayout)findViewById(R.id.line_light_sensor);
        saveRecordIntervalLL.setVisibility(View.GONE);
        humidityLowAlarmLL.setVisibility(View.GONE);
        humidityHighAlarmLL.setVisibility(View.GONE);
        recordControlLL.setVisibility(View.GONE);
        relayLL.setVisibility(View.GONE);
        relayFlashingLL.setVisibility(View.GONE);
        clearRecordLL.setVisibility(View.GONE);
        saveRecordIntervaLineLL = (LinearLayout)findViewById(R.id.line_line_save_record_interval);
        humidityLowAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_humidity_low_alarm);
        humidityHighAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_humidity_high_alarm);
        recordControlLineLL = (LinearLayout)findViewById(R.id.line_line_record_control);
        relayFlashingLineLL = (LinearLayout)findViewById(R.id.line_line_flashing_relay);
        lightSensorEnableLineLL = (LinearLayout)findViewById(R.id.line_line_light_sensor);
        readSaveCountLL = (LinearLayout)findViewById(R.id.line_save_count);
        readSaveCountLineLL = (LinearLayout)findViewById(R.id.line_line_save_count);
        readAlarmLL  = (LinearLayout)findViewById(R.id.line_alarm_count);
        readAlarmLineLL  = (LinearLayout)findViewById(R.id.line_line_alarm_count);
        clearRecordLineLL = (LinearLayout)findViewById(R.id.line_line_clear_record) ;
        clearRecordLineLL.setVisibility(View.GONE);
        saveRecordIntervaLineLL.setVisibility(View.GONE);
        humidityLowAlarmLineLL.setVisibility(View.GONE);
        humidityHighAlarmLineLL.setVisibility(View.GONE);
        recordControlLineLL.setVisibility(View.GONE);
        relayLineLL.setVisibility(View.GONE);
        relayFlashingLineLL.setVisibility(View.GONE);
        readSaveCountLineLL.setVisibility(View.GONE);
        readSaveCountLL.setVisibility(View.GONE);
        readAlarmLL.setVisibility(View.GONE);
        readAlarmLineLL.setVisibility(View.GONE);
        lightSensorEnableLineLL.setVisibility(View.GONE);
        lightSensorEnableLL.setVisibility(View.GONE);
        transmittedPowerLL.setVisibility(View.GONE);
        transmittedPowerLineLL.setVisibility(View.GONE);


        readDinVoltageLL = (LinearLayout)findViewById(R.id.line_din_voltage);
        readDinVoltageLineLL = (LinearLayout)findViewById(R.id.line_line_din_voltage);
        dinStatusEventLL = (LinearLayout)findViewById(R.id.line_din_status_event);
        dinStatusEventLineLL = (LinearLayout)findViewById(R.id.line_line_din_status_event);
        readDinStatusEventTypeLL = (LinearLayout)findViewById(R.id.line_din_status_event_type);
        readDinStatusEventTypeLineLL = (LinearLayout)findViewById(R.id.line_line_din_status_event_type);
        doutStatusLL = (LinearLayout)findViewById(R.id.line_dout_status);
        doutStatusLineLL = (LinearLayout)findViewById(R.id.line_line_dout_status);
        readAinVoltageLL = (LinearLayout)findViewById(R.id.line_ain_voltage);
        readAinVoltageLineLL = (LinearLayout)findViewById(R.id.line_line_ain_voltage);
        setPositiveNegativeWarningLL = (LinearLayout)findViewById(R.id.line_positive_negative_warning);
        setPositiveNegativeWarningLineLL = (LinearLayout)findViewById(R.id.line_line_positive_negative_warning);
        getOneWireDeviceLL = (LinearLayout)findViewById(R.id.line_one_wire_device);
        getOneWireDeviceLineLL = (LinearLayout)findViewById(R.id.line_line_one_wire_device);
        sendCmdSequenceLL = (LinearLayout)findViewById(R.id.line_send_instruction_sequence);
        sendCmdSequenceLineLL = (LinearLayout)findViewById(R.id.line_line_send_instruction_sequence);
        sequentialLL = (LinearLayout)findViewById(R.id.line_sequential);
        sequentialLineLL = (LinearLayout)findViewById(R.id.line_line_sequential);
        sendDataLL = (LinearLayout)findViewById(R.id.line_send_data);
        sendDataLineLL = (LinearLayout)findViewById(R.id.line_line_send_data);
        rs485BaudRateLL = (LinearLayout)findViewById(R.id.line_rs485_baud_rate);
        rs485BaudRateLineLL = (LinearLayout)findViewById(R.id.line_line_rs485_baud_rate);
        rs485EnableLL = (LinearLayout)findViewById(R.id.line_rs485_enable);
        rs485EnableLineLL = (LinearLayout)findViewById(R.id.line_line_rs485_enable);
        oneWireWorkModeLL = (LinearLayout)findViewById(R.id.line_one_wire_work_mode);
        oneWireWorkModeLineLL = (LinearLayout)findViewById(R.id.line_line_one_wire_work_mode);

        readDinVoltageLL.setVisibility(View.GONE);
        readDinVoltageLineLL.setVisibility(View.GONE);
        dinStatusEventLL.setVisibility(View.GONE);
        dinStatusEventLineLL.setVisibility(View.GONE);
        readDinStatusEventTypeLL.setVisibility(View.GONE);
        readDinStatusEventTypeLineLL.setVisibility(View.GONE);
        doutStatusLL.setVisibility(View.GONE);
        doutStatusLineLL.setVisibility(View.GONE);
        readAinVoltageLL.setVisibility(View.GONE);
        readAinVoltageLineLL.setVisibility(View.GONE);
        setPositiveNegativeWarningLL.setVisibility(View.GONE);
        setPositiveNegativeWarningLineLL.setVisibility(View.GONE);
        getOneWireDeviceLL.setVisibility(View.GONE);
        getOneWireDeviceLineLL.setVisibility(View.GONE);
        sendCmdSequenceLL.setVisibility(View.GONE);
        sendCmdSequenceLineLL.setVisibility(View.GONE);
        sequentialLL.setVisibility(View.GONE);
        sequentialLineLL.setVisibility(View.GONE);
        sendDataLL.setVisibility(View.GONE);
        sendDataLineLL.setVisibility(View.GONE);
        rs485BaudRateLL.setVisibility(View.GONE);
        rs485BaudRateLineLL.setVisibility(View.GONE);
        rs485EnableLL.setVisibility(View.GONE);
        rs485EnableLineLL.setVisibility(View.GONE);
        oneWireWorkModeLL.setVisibility(View.GONE);
        oneWireWorkModeLineLL.setVisibility(View.GONE);


        longRangeLL = (LinearLayout)findViewById(R.id.line_long_range);
        longRangeLineLL = (LinearLayout)findViewById(R.id.line_line_long_range);
        broadcastTypeLL = (LinearLayout)findViewById(R.id.line_broadcast_type);
        broadcastTypeLineLL = (LinearLayout)findViewById(R.id.line_line_broadcast_type);
        gSensorEnableLL = (LinearLayout)findViewById(R.id.line_gsensor_enable);
        gSensorEnableLineLL = (LinearLayout)findViewById(R.id.line_line_gsensor_enable);
        longRangeLL.setVisibility(View.GONE);
        longRangeLineLL.setVisibility(View.GONE);
        broadcastTypeLL.setVisibility(View.GONE);
        broadcastTypeLineLL.setVisibility(View.GONE);
        gSensorEnableLL.setVisibility(View.GONE);
        gSensorEnableLineLL.setVisibility(View.GONE);

        tempLowAlarmLL = (LinearLayout)findViewById(R.id.line_temp_low_alarm);
        tempLowAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_temp_low_alarm);
        tempHighAlarmLL = (LinearLayout)findViewById(R.id.line_temp_high_alarm);
        tempHighAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_temp_high_alarm);
        tempLowAlarmLL.setVisibility(View.GONE);
        tempLowAlarmLineLL.setVisibility(View.GONE);
        tempHighAlarmLL.setVisibility(View.GONE);
        tempHighAlarmLineLL.setVisibility(View.GONE);
        ledLL = (LinearLayout)findViewById(R.id.line_led);
        ledLineLL = (LinearLayout)findViewById(R.id.line_line_led);

        broadcastCycleLL = (LinearLayout)findViewById(R.id.line_broadcast_cycle);
        broadcastCycleLineLL = (LinearLayout)findViewById(R.id.line_line_broadcast_cycle);
        shutdownLL = (LinearLayout)findViewById(R.id.line_shutdown);
        shutdownLineLL = (LinearLayout)findViewById(R.id.line_line_shutdown);
        shutdownLL.setVisibility(View.GONE);
        shutdownLineLL.setVisibility(View.GONE);
        doorEnableLL = (LinearLayout)findViewById(R.id.line_door_enable);
        doorEnableLineLL = (LinearLayout)findViewById(R.id.line_line_door_enable);
        doorEnableLL.setVisibility(View.GONE);
        doorEnableLineLL.setVisibility(View.GONE);


        gSensorDetectionDurationLL = (LinearLayout)findViewById(R.id.line_gsensor_detection_duration);
        gSensorDetectionDurationLL.setVisibility(View.GONE);
        gSensorDetectionDurationLineLL = (LinearLayout)findViewById(R.id.line_line_gsensor_detection_duration);
        gSensorDetectionDurationLineLL.setVisibility(View.GONE);
        gSensorDetectionIntervalLL = (LinearLayout)findViewById(R.id.line_gsensor_detection_interval);
        gSensorDetectionIntervalLL.setVisibility(View.GONE);
        gSensorDetectionIntervalLineLL = (LinearLayout)findViewById(R.id.line_line_gsensor_detection_interval);
        gSensorDetectionIntervalLineLL.setVisibility(View.GONE);
        gSensorSensitivityLL = (LinearLayout)findViewById(R.id.line_gsensor_sensitivity);
        gSensorSensitivityLL.setVisibility(View.GONE);
        gSensorSensitivityLineLL = (LinearLayout)findViewById(R.id.line_line_gsensor_sensitivity);
        gSensorSensitivityLineLL.setVisibility(View.GONE);


        beaconMajorSetLL=(LinearLayout)findViewById(R.id.line_beacon_major_set);
        beaconMajorSetLineLL=(LinearLayout)findViewById(R.id.line_line_beacon_major_set);
        beaconMinorSetLL=(LinearLayout)findViewById(R.id.line_beacon_minor_set);
        beaconMinorSetLineLL=(LinearLayout)findViewById(R.id.line_line_beacon_minor_set);
        eddystoneNidSetLL=(LinearLayout)findViewById(R.id.line_eddystone_nid_set);
        eddystoneNidSetLineLL=(LinearLayout)findViewById(R.id.line_line_eddystone_nid_set);
        eddystoneBidSetLL=(LinearLayout)findViewById(R.id.line_eddystone_bid_set);
        eddystoneBidSetLineLL=(LinearLayout)findViewById(R.id.line_line_eddystone_bid_set);
        btnTriggerTimeLL=(LinearLayout)findViewById(R.id.line_btn_trigger_time);
        btnTriggerTimeLineLL=(LinearLayout)findViewById(R.id.line_line_btn_trigger_time);;
        beaconMajorSetLL.setVisibility(View.GONE);
        beaconMajorSetLineLL.setVisibility(View.GONE);
        beaconMinorSetLL.setVisibility(View.GONE);
        beaconMinorSetLineLL.setVisibility(View.GONE);
        eddystoneNidSetLL.setVisibility(View.GONE);
        eddystoneNidSetLineLL.setVisibility(View.GONE);
        eddystoneBidSetLL.setVisibility(View.GONE);
        eddystoneBidSetLineLL.setVisibility(View.GONE);
        btnTriggerTimeLL.setVisibility(View.GONE);
        btnTriggerTimeLineLL.setVisibility(View.GONE);
        if (deviceType.equals("S04")){
            if(Integer.valueOf(software) >= 13){
                transmittedPowerLL.setVisibility(View.VISIBLE);
                transmittedPowerLineLL.setVisibility(View.VISIBLE);
            }
//            if(Integer.valueOf(software) > 10){
//                recordControlLL.setVisibility(View.VISIBLE);
//                recordControlLineLL.setVisibility(View.VISIBLE);
//                readAlarmLL.setVisibility(View.VISIBLE);
//                readAlarmLineLL.setVisibility(View.VISIBLE);
//                clearRecordLL.setVisibility(View.VISIBLE);
//                clearRecordLineLL.setVisibility(View.VISIBLE);
//            }
            tempLowAlarmLL.setVisibility(View.VISIBLE);
            tempLowAlarmLineLL.setVisibility(View.VISIBLE);
            tempHighAlarmLL.setVisibility(View.VISIBLE);
            tempHighAlarmLineLL.setVisibility(View.VISIBLE);
        }else if (deviceType.equals("S05")){
            if(Integer.valueOf(software) >= 13){
                transmittedPowerLL.setVisibility(View.VISIBLE);
                transmittedPowerLineLL.setVisibility(View.VISIBLE);
            }
            relayLL.setVisibility(View.VISIBLE);
            relayLineLL.setVisibility(View.VISIBLE);
            if(Integer.valueOf(software) >= 17){
                relayFlashingLL.setVisibility(View.VISIBLE);
                relayFlashingLineLL.setVisibility(View.VISIBLE);
            }
            tempLowAlarmLL.setVisibility(View.VISIBLE);
            tempLowAlarmLineLL.setVisibility(View.VISIBLE);
            tempHighAlarmLL.setVisibility(View.VISIBLE);
            tempHighAlarmLineLL.setVisibility(View.VISIBLE);
        }else if (deviceType.equals("S02")){
            if(Integer.valueOf(software) >= 13){
                transmittedPowerLL.setVisibility(View.VISIBLE);
                transmittedPowerLineLL.setVisibility(View.VISIBLE);
            }
            if(Integer.valueOf(software) > 10){
                recordControlLineLL.setVisibility(View.VISIBLE);
                recordControlLL.setVisibility(View.VISIBLE);
                saveRecordIntervaLineLL.setVisibility(View.VISIBLE);
                saveRecordIntervalLL.setVisibility(View.VISIBLE);
                readSaveCountLineLL.setVisibility(View.VISIBLE);
                readSaveCountLL.setVisibility(View.VISIBLE);
                clearRecordLL.setVisibility(View.VISIBLE);
                clearRecordLineLL.setVisibility(View.VISIBLE);
            }

            if(Integer.valueOf(software) >= 20){
                lightSensorEnableLineLL.setVisibility(View.VISIBLE);
                lightSensorEnableLL.setVisibility(View.VISIBLE);
            }
            humidityLowAlarmLL.setVisibility(View.VISIBLE);
            humidityHighAlarmLL.setVisibility(View.VISIBLE);
            humidityLowAlarmLineLL.setVisibility(View.VISIBLE);
            humidityHighAlarmLineLL.setVisibility(View.VISIBLE);
            tempLowAlarmLL.setVisibility(View.VISIBLE);
            tempLowAlarmLineLL.setVisibility(View.VISIBLE);
            tempHighAlarmLL.setVisibility(View.VISIBLE);
            tempHighAlarmLineLL.setVisibility(View.VISIBLE);
        }else if(deviceType.equals("S09")){
            transmittedPowerLL.setVisibility(View.VISIBLE);
            transmittedPowerLineLL.setVisibility(View.VISIBLE);
            longRangeLL.setVisibility(View.VISIBLE);
            longRangeLineLL.setVisibility(View.VISIBLE);

            readDinVoltageLL.setVisibility(View.VISIBLE);
            readDinVoltageLineLL.setVisibility(View.VISIBLE);
            dinStatusEventLL.setVisibility(View.VISIBLE);
            dinStatusEventLineLL.setVisibility(View.VISIBLE);
            readDinStatusEventTypeLL.setVisibility(View.VISIBLE);
            readDinStatusEventTypeLineLL.setVisibility(View.VISIBLE);
            doutStatusLL.setVisibility(View.VISIBLE);
            doutStatusLineLL.setVisibility(View.VISIBLE);
            readAinVoltageLL.setVisibility(View.VISIBLE);
            readAinVoltageLineLL.setVisibility(View.VISIBLE);
            setPositiveNegativeWarningLL.setVisibility(View.VISIBLE);
            setPositiveNegativeWarningLineLL.setVisibility(View.VISIBLE);
            getOneWireDeviceLL.setVisibility(View.VISIBLE);
            getOneWireDeviceLineLL.setVisibility(View.VISIBLE);
            sendCmdSequenceLL.setVisibility(View.VISIBLE);
            sendCmdSequenceLineLL.setVisibility(View.VISIBLE);
//            sequentialLL.setVisibility(View.VISIBLE);
//            sequentialLineLL.setVisibility(View.VISIBLE);
            sendDataLL.setVisibility(View.VISIBLE);
            sendDataLineLL.setVisibility(View.VISIBLE);
            rs485BaudRateLL.setVisibility(View.VISIBLE);
            rs485BaudRateLineLL.setVisibility(View.VISIBLE);
            rs485EnableLL.setVisibility(View.VISIBLE);
            rs485EnableLineLL.setVisibility(View.VISIBLE);
            oneWireWorkModeLL.setVisibility(View.VISIBLE);
            oneWireWorkModeLineLL.setVisibility(View.VISIBLE);
            broadcastCycleLL.setVisibility(View.GONE);
            broadcastCycleLineLL.setVisibility(View.GONE);

        }else if (deviceType.equals("S08")){
            transmittedPowerLL.setVisibility(View.VISIBLE);
            transmittedPowerLineLL.setVisibility(View.VISIBLE);
            recordControlLineLL.setVisibility(View.VISIBLE);
            recordControlLL.setVisibility(View.VISIBLE);
            saveRecordIntervaLineLL.setVisibility(View.VISIBLE);
            saveRecordIntervalLL.setVisibility(View.VISIBLE);
            readSaveCountLineLL.setVisibility(View.VISIBLE);
            readSaveCountLL.setVisibility(View.VISIBLE);
            clearRecordLL.setVisibility(View.VISIBLE);
            clearRecordLineLL.setVisibility(View.VISIBLE);

            humidityLowAlarmLL.setVisibility(View.GONE);
            humidityHighAlarmLL.setVisibility(View.GONE);
            humidityLowAlarmLineLL.setVisibility(View.GONE);
            humidityHighAlarmLineLL.setVisibility(View.GONE);

            broadcastTypeLL.setVisibility(View.VISIBLE);
            broadcastTypeLineLL.setVisibility(View.VISIBLE);

            tempLowAlarmLL.setVisibility(View.VISIBLE);
            tempLowAlarmLineLL.setVisibility(View.VISIBLE);
            tempHighAlarmLL.setVisibility(View.VISIBLE);
            tempHighAlarmLineLL.setVisibility(View.VISIBLE);
            shutdownLL.setVisibility(View.VISIBLE);
            shutdownLineLL.setVisibility(View.VISIBLE);
//            doorEnableLL.setVisibility(View.VISIBLE);
//            doorEnableLineLL.setVisibility(View.VISIBLE);
        }
        else if (deviceType.equals("S10")){
            transmittedPowerLL.setVisibility(View.VISIBLE);
            transmittedPowerLineLL.setVisibility(View.VISIBLE);
            recordControlLineLL.setVisibility(View.VISIBLE);
            recordControlLL.setVisibility(View.VISIBLE);
            saveRecordIntervaLineLL.setVisibility(View.VISIBLE);
            saveRecordIntervalLL.setVisibility(View.VISIBLE);
            readSaveCountLineLL.setVisibility(View.VISIBLE);
            readSaveCountLL.setVisibility(View.VISIBLE);
            clearRecordLL.setVisibility(View.VISIBLE);
            clearRecordLineLL.setVisibility(View.VISIBLE);

            humidityLowAlarmLL.setVisibility(View.GONE);
            humidityHighAlarmLL.setVisibility(View.GONE);
            humidityLowAlarmLineLL.setVisibility(View.GONE);
            humidityHighAlarmLineLL.setVisibility(View.GONE);

            broadcastTypeLL.setVisibility(View.VISIBLE);
            broadcastTypeLineLL.setVisibility(View.VISIBLE);
            gSensorEnableLL.setVisibility(View.GONE);
            gSensorEnableLineLL.setVisibility(View.GONE);
            tempLowAlarmLL.setVisibility(View.VISIBLE);
            tempLowAlarmLineLL.setVisibility(View.VISIBLE);
            tempHighAlarmLL.setVisibility(View.VISIBLE);
            tempHighAlarmLineLL.setVisibility(View.VISIBLE);
            shutdownLL.setVisibility(View.VISIBLE);
            shutdownLineLL.setVisibility(View.VISIBLE);

        }
        else if (deviceType.equals("S07")){
            transmittedPowerLL.setVisibility(View.VISIBLE);
            transmittedPowerLineLL.setVisibility(View.VISIBLE);
            humidityLowAlarmLL.setVisibility(View.GONE);
            humidityHighAlarmLL.setVisibility(View.GONE);
            humidityLowAlarmLineLL.setVisibility(View.GONE);
            humidityHighAlarmLineLL.setVisibility(View.GONE);
            broadcastTypeLL.setVisibility(View.VISIBLE);
            broadcastTypeLineLL.setVisibility(View.VISIBLE);
            ledLL.setVisibility(View.GONE);
            ledLineLL.setVisibility(View.GONE);
            String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
            int result = "1005".compareToIgnoreCase(formatSoftware);
            if(result > 0){
                btnTriggerTimeLL.setVisibility(View.GONE);
                btnTriggerTimeLineLL.setVisibility(View.GONE);
            }else{
                btnTriggerTimeLL.setVisibility(View.VISIBLE);
                btnTriggerTimeLineLL.setVisibility(View.VISIBLE);
            }

        }
    }

    private boolean isCurrentDeviceTypeFunc(String funcName){
        if (deviceType.equals("S04")){
            if(funcName.equals("transmittedPower")){
                if(Integer.valueOf(software) >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if (funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("startRecord")
                    || funcName.equals("stopRecord") || funcName.equals("clearRecord") || funcName.equals("time")) {
                if(Integer.valueOf(software) < 11 && (funcName.equals("saveInterval") || funcName.equals("saveCount")
                        || funcName.equals("readAlarm") || funcName.equals("readOriginData") || funcName.equals("startRecord"))){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }else  if (deviceType.equals("S05")){
            if(funcName.equals("transmittedPower")){
                if(Integer.valueOf(software) >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if (funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("relay")
                    || funcName.equals("time")) {
                return true;
            }else{
                return false;
            }
        }else if (deviceType.equals("S02")){
            if(funcName.equals("transmittedPower")){
                if(Integer.valueOf(software) >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if(funcName.equals("lightSensorOpen")){
                if(Integer.valueOf(software) >= 20){
                    return true;
                }else{
                    return false;
                }
            }
            if(Integer.valueOf(software) < 11 && (funcName.equals("saveInterval") ||funcName.equals("readHistory")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData")  || funcName.equals("startRecord"))){
                return false;
            }
            if (funcName.equals("saveInterval") || funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("startRecord")
                    || funcName.equals("stopRecord") || funcName.equals("clearRecord") ||funcName.equals("readHistory") || funcName.equals("humidityAlarm")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData") || funcName.equals("transmittedPower")
                    || funcName.equals("time")){
                return true;
            }else{
                return false;
            }
        }else if (deviceType.equals("S09")){
            if (funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory")
                    || funcName.equals("transmittedPower") || funcName.equals("ledOpen") || funcName.equals("deviceName")
                    || funcName.equals("readDinVoltage") || funcName.equals("dinStatusEvent") || funcName.equals("doutStatus")|| funcName.equals("readVinVoltage")
                    || funcName.equals("readAinVoltage") || funcName.equals("setPositiveNegativeWarning") || funcName.equals("ainPositiveNegativeWarning") || funcName.equals("getOneWireDevice")
                    || funcName.equals("sendCmdSequence") || funcName.equals("sequential") || funcName.equals("oneWireWorkMode") || funcName.equals("rs485SendData")
                    || funcName.equals("rs485BaudRate") || funcName.equals("rs485Enable")  || funcName.equals("getAinEvent")|| funcName.equals("longRangeEnable")) {
                return true;
            }else{
                return false;
            }
        }else if (deviceType.equals("S08")){
            if(funcName.equals("saveInterval") || funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("startRecord")
                    || funcName.equals("stopRecord") || funcName.equals("clearRecord")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData") || funcName.equals("transmittedPower")
                    || funcName.equals("broadcastType")  || funcName.equals("readHistory")
                    || funcName.equals("time")    || funcName.equals("shutdown") || funcName.equals("doorEnable")
                    || funcName.equals("gSensorSensitivity") || funcName.equals("gSensorDetectionDuration") || funcName.equals("gSensorDetectionInterval")
                    || funcName.equals("beaconMajorSet") || funcName.equals("beaconMinorSet") || funcName.equals("eddystoneNIDSet") || funcName.equals("eddystoneBIDSet") ){
                return true;
            }
            return false;
        }else if (deviceType.equals("S10")){
            if(funcName.equals("saveInterval") || funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("startRecord")
                    || funcName.equals("stopRecord") || funcName.equals("clearRecord")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData") || funcName.equals("transmittedPower")
                    || funcName.equals("broadcastType") ||funcName.equals("readHistory")
                    || funcName.equals("time")   || funcName.equals("shutdown") || funcName.equals("readExtSensorType")
                    || funcName.equals("beaconMajorSet") || funcName.equals("beaconMinorSet") || funcName.equals("eddystoneNIDSet") || funcName.equals("eddystoneBIDSet")
            ){
                return true;
            }
            return false;
        }
        else if (deviceType.equals("S07")){
            if(funcName.equals("btnTriggerTime")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = "1005".compareToIgnoreCase(formatSoftware);
                if(result > 0){
                    return false;
                }else{
                    return true;
                }
            }
            if(funcName.equals("time")){
                String formatSoftware = software.replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1006");
                if(result >= 0){
                    return true;
                }else{
                    return false;
                }
            }
            if(funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory") || funcName.equals("broadcastCycle")
                    ||  funcName.equals("transmittedPower") || funcName.equals("deviceName")
                    || funcName.equals("broadcastType")
                    || funcName.equals("beaconMajorSet") || funcName.equals("beaconMinorSet") || funcName.equals("eddystoneNIDSet") || funcName.equals("eddystoneBIDSet") ){
                return true;
            }
            return false;
        }else if (deviceType.equals("A001")){
            if (funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory")  || funcName.equals("deviceName") ) {
                return true;
            }else{
                return false;
            }
        }else if (deviceType.equals("A002")){
            if (funcName.equals("firmware") || funcName.equals("password") || funcName.equals("resetFactory")  || funcName.equals("deviceName") ) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }



    @Override
    public void onUpgradeStatus(int status, float percent) {
        if(status == A001SoftwareUpgradeManager.STATUS_OF_FINE_NOT_FIND){
            if(connectWaitingCancelDlg != null){
                connectWaitingCancelDlg.hide();
            }
            if(waitingDlg != null){
                waitingDlg.hide();
            }
            Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_SHORT).show();
        }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_UNKNOWN_ERROR){
            if(connectWaitingCancelDlg != null){
                connectWaitingCancelDlg.hide();
            }
            if(waitingDlg != null){
                waitingDlg.hide();
            }
            Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_SHORT).show();
        }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_ERROR_UPGRADE_FILE){
            if(connectWaitingCancelDlg != null){
                connectWaitingCancelDlg.hide();
            }
            if(waitingDlg != null){
                waitingDlg.hide();
            }
            Toast.makeText(EditActivity.this,R.string.error_upgrade_file,Toast.LENGTH_SHORT).show();
        }else if(status == A001SoftwareUpgradeManager.STATUS_OF_UPGRADE_WRITE_SUCC){
            upgradeSucc();
        }else{
            upgradeStatus = "progressChanged";
            if(waitingDlg != null){
                if(percent > 100){
                    percent = 100;
                }
                waitingDlg.setTitleText(getResources().getString(R.string.processing)+ ":" + String.format("%.2f%%",percent));
            }
        }
    }
}
