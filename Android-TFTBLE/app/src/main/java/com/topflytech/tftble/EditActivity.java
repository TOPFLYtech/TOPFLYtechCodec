package com.topflytech.tftble;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.BleHisData;
import com.topflytech.tftble.data.DfuService;
import com.topflytech.tftble.data.DownloadFileManager;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.OpenAPI;
import com.topflytech.tftble.view.SwitchButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;

import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

public class EditActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    private BluetoothClient mClient;
    private String mac;
    private String deviceType;
    private String id;
    private String software;
    private String confirmPwd,newPwd;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;
    private String deviceName;
    EditText pwdEdit;
    boolean connectSucc = false;
    float tempAlarmUp,tempAlarmDown;
    int humidityAlarmUp,humidityAlarmDown;
    boolean humidityAlarmUpOpen,humidityAlarmDownOpen,tempAlarmUpOpen,tempAlarmDownOpen;

    int saveCount = 0,saveAlarmCount = 0,saveInterval=0;
    boolean isSaveRecordStatus = false;
    int serverVersion = -1;
    String upgradeLink;
    boolean onThisView = false;
    private OptionsPickerView pvTransmittedPower;
    private OptionsPickerView pvSaveInterval;
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

    private long startTimestamp,endTimestamp;
    private int transmittedPower;
    private boolean pwdErrorWarning = false;
    private TextView relayStatusTV;
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
        mClient = new BluetoothClient(EditActivity.this);
        initUI();
    }

    private void showWaitingDlg(String warning){
        waitingDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
        }
        waitingDlg.show();
    }

    private void showWaitingCancelDlg(String warning){
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

    private void showPwdDlg(){
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
        if(MyUtils.isDebug){
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
        TextView tvHead = (TextView)findViewById(R.id.edit_command_title);
        tvHead.setText(id);
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
                reconnectBtn.setImageResource(R.mipmap.ic_refresh);
                mClient.disconnect(mac);
                connectSucc = false;
                showWaitingDlg(getResources().getString(R.string.reconnecting));
                mClient.connect(mac,bleConnectResponse);
                mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
            }
        });
    }
    private ArrayList<String> transmittedPowerList = new ArrayList<String>(){{
        add("4");
        add("0");
        add("-4");
        add("-8");
        add("-12");
        add("-16");
        add("-20");
    }};

    private ArrayList<String> saveIntervalList = new ArrayList<>();
    private TextView deviceNameTV,modelTV,hardwareTV,softwareTV,broadcastCycleTV,saveRecordIntervalTV,
        tempHighAlarmTV,tempLowAlarmTV,humidityHighAlarmTV,humidityLowAlarmTV,saveCountTV,alarmCountTV,transmittedPowerTV;
    private SwitchButton ledSwitch,relaySwitch;
    private LinearLayout saveRecordIntervalLL,humidityHighAlarmLL,humidityLowAlarmLL,recordControlLL,relayLL,transmittedPowerLL,transmittedPowerLineLL,
            saveRecordIntervaLineLL,humidityHighAlarmLineLL,humidityLowAlarmLineLL, recordControlLineLL,relayLineLL,
            readSaveCountLL,readSaveCountLineLL,readAlarmLL,readAlarmLineLL, clearRecordLineLL, clearRecordLL;
    private Button btnEditDeviceName,btnEditPwd,btnEditBroadcastCycle,btnEditHighTemp,btnEditLowTemp,btnEditHumidityHigh,
            btnEditHumidityLow,btnEditSaveRecordInterval, btnRefreshSaveCount,btnReadSaveCount,btnStartRecord,btnStopRecord,
            btnClearRecord,btnResetFactory,btnUpgrade,btnRefreshAlarmCount,btnReadAlarmCount,btnEditTransmittedPower,btnDebugUpgrade;
    private void initUI(){
        relayStatusTV = (TextView)findViewById(R.id.tx_relay_status);
        deviceNameTV = (TextView)findViewById(R.id.tx_device_name);
        modelTV = (TextView)findViewById(R.id.tx_device_model);
        hardwareTV = (TextView)findViewById(R.id.tx_hardware);
        softwareTV = (TextView)findViewById(R.id.tx_software);
        broadcastCycleTV = (TextView)findViewById(R.id.tx_broadcast_cycle);
        saveRecordIntervalTV = (TextView)findViewById(R.id.tx_save_record_interval);
        tempHighAlarmTV = (TextView)findViewById(R.id.tx_temp_high_alarm);
        tempLowAlarmTV = (TextView)findViewById(R.id.tx_temp_low_alarm);
        humidityHighAlarmTV = (TextView)findViewById(R.id.tx_humidity_high_alarm);
        humidityLowAlarmTV = (TextView)findViewById(R.id.tx_humidity_low_alarm);
        saveCountTV = (TextView)findViewById(R.id.tx_save_count);
        alarmCountTV = (TextView)findViewById(R.id.tx_alarm_count);
        transmittedPowerTV = (TextView)findViewById(R.id.tx_transmitted_power);
        ledSwitch = (SwitchButton)findViewById(R.id.switch_led);
        ledSwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                writeLedOpenStatus();
            }
        });
        relaySwitch = (SwitchButton)findViewById(R.id.switch_relay);
        relaySwitch.setOnSwitchChangeListener(new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if(MyUtils.isDebug){
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
                confirmRelayDlg.show();

            }
        });
        btnEditDeviceName = (Button)findViewById(R.id.btn_edit_device_name);
        btnEditDeviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        if(deviceName.length() < 8){
                            sweetAlertDialog.hide();
                            writeDeviceName(deviceName);
                        }else{
                            Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editDeviceNameDlg.show();
            }
        });
        btnEditPwd = (Button)findViewById(R.id.btn_edit_pwd);
        btnEditPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( EditActivity.this, EditPwdActivity.class);
                intent.putExtra("oldPwd",confirmPwd);
                startActivityForResult(intent,REQUEST_CHANGE_PWD);
            }
        });
        btnEditBroadcastCycle = (Button)findViewById(R.id.btn_edit_broadcast_cycle);
        btnEditBroadcastCycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SweetAlertDialog editBroadcastCycleDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                editBroadcastCycleDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                editBroadcastCycleDlg.setTitleText(getResources().getString(R.string.enter_broadcast_cycle));
                editBroadcastCycleDlg.setCancelable(true);
                editBroadcastCycleDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                editBroadcastCycleDlg.setCancelText(getResources().getString(R.string.cancel));
                editBroadcastCycleDlg.setConfirmText(getResources().getString(R.string.confirm));
                editBroadcastCycleDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        String broadcastCycleValue = sweetAlertDialog.getInputText();
                        if(broadcastCycleValue.length() > 0){
                            int broadcastCycle = Float.valueOf(broadcastCycleValue).intValue();
                            if (broadcastCycle >= 5 && broadcastCycle <= 1800){
                                writeBroadcastData(broadcastCycle);
                                sweetAlertDialog.hide();
                            }else{
                                Toast.makeText(EditActivity.this,R.string.broadcast_input_error,Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                editBroadcastCycleDlg.show();
            }
        });
        btnEditHighTemp = (Button)findViewById(R.id.btn_edit_temp_high_alarm);
        btnEditHighTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue",String.valueOf(tempAlarmUp));
                intent.putExtra("lowValue",String.valueOf(tempAlarmDown));
                intent.putExtra("highValueOpen",tempAlarmUpOpen);
                intent.putExtra("lowValueOpen",tempAlarmDownOpen);
                intent.putExtra("editType","temp");
                startActivityForResult(intent,REQUEST_CHANGE_TEMP);
            }
        });
        btnEditLowTemp = (Button)findViewById(R.id.btn_edit_temp_low_alarm);
        btnEditLowTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue",String.valueOf(tempAlarmUp));
                intent.putExtra("lowValue",String.valueOf(tempAlarmDown));
                intent.putExtra("highValueOpen",tempAlarmUpOpen);
                intent.putExtra("lowValueOpen",tempAlarmDownOpen);
                intent.putExtra("editType","temp");
                startActivityForResult(intent,REQUEST_CHANGE_TEMP);
            }
        });
        btnEditHumidityHigh = (Button)findViewById(R.id.btn_edit_humidity_high_alarm);
        btnEditHumidityHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue",String.valueOf(humidityAlarmUp));
                intent.putExtra("lowValue",String.valueOf(humidityAlarmDown));
                intent.putExtra("highValueOpen",humidityAlarmUpOpen);
                intent.putExtra("lowValueOpen",humidityAlarmDownOpen);
                intent.putExtra("editType","humidity");
                startActivityForResult(intent,REQUEST_CHANGE_HUMIDITY);
            }
        });
        btnEditHumidityLow = (Button)findViewById(R.id.btn_edit_humidity_low_alarm);
        btnEditHumidityLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( EditActivity.this, EditRangeValueActivity.class);
                intent.putExtra("highValue",String.valueOf(humidityAlarmUp));
                intent.putExtra("lowValue",String.valueOf(humidityAlarmDown));
                intent.putExtra("highValueOpen",humidityAlarmUpOpen);
                intent.putExtra("lowValueOpen",humidityAlarmDownOpen);
                intent.putExtra("editType","humidity");
                startActivityForResult(intent,REQUEST_CHANGE_HUMIDITY);
            }
        });
        saveIntervalList.clear();
        for(int i = 6;i <= 60;i++){
            saveIntervalList.add(String.valueOf(i * 10));
        }
        pvSaveInterval = new OptionsPickerBuilder(EditActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是三个级别的选中位置
//                String tx = options1Items.get(options1).getPickerViewText()
//                        + options2Items.get(options1).get(option2)
//                        + options3Items.get(options1).get(option2).get(options3).getPickerViewText();
//                tvOptions.setText(tx);
                String selected = saveIntervalList.get(options1);
                writeSaveRecordIntervalData(Integer.valueOf(selected));
            }
        }).build();
        pvSaveInterval.setPicker(saveIntervalList);
        btnEditSaveRecordInterval = (Button)findViewById(R.id.btn_edit_save_record_interval);
        btnEditSaveRecordInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                pvSaveInterval.show();
            }
        });
        btnRefreshSaveCount = (Button)findViewById(R.id.btn_refresh_save_count);
        btnRefreshSaveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWaitingDlg(getResources().getString(R.string.waiting));
                readSaveCount();
            }
        });
        btnRefreshAlarmCount = (Button)findViewById(R.id.btn_refresh_alarm_count);
        btnRefreshAlarmCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWaitingDlg(getResources().getString(R.string.waiting));
                readSaveCount();
            }
        });
        btnReadSaveCount = (Button)findViewById(R.id.btn_read_save_count);
        btnReadSaveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHistory = false;
                Intent intent = new Intent(EditActivity.this,HistorySelectActivity.class);
                intent.putExtra("mac",mac);
                intent.putExtra("deviceType",deviceType);
                intent.putExtra("id",id);
                intent.putExtra("reportType","history");
                startActivityForResult(intent,REQUEST_READ_HISTORY_TIME);
            }
        });
        btnReadAlarmCount = (Button)findViewById(R.id.btn_read_alarm_count);
        btnReadAlarmCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startHistory = false;
                Intent intent = new Intent(EditActivity.this,HistorySelectActivity.class);
                intent.putExtra("mac",mac);
                intent.putExtra("deviceType",deviceType);
                intent.putExtra("id",id);
                intent.putExtra("reportType","alarm");
                startActivityForResult(intent,REQUEST_READ_ALARM_TIME);
            }
        });
        btnStartRecord = (Button)findViewById(R.id.btn_start_record);
        btnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecord();
            }
        });
        btnStopRecord = (Button)findViewById(R.id.btn_stop_record);
        btnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord();
            }
        });
        btnClearRecord = (Button)findViewById(R.id.btn_clear_record);
        btnClearRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearRecord();
            }
        });
        btnResetFactory = (Button)findViewById(R.id.btn_reset_factory);
        btnResetFactory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_reset_factory_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        writeResetFactory();
                    }
                });
                confirmRelayDlg.show();

            }
        });
        btnUpgrade = (Button)findViewById(R.id.btn_upgrade);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        pvTransmittedPower = new OptionsPickerBuilder(EditActivity.this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
                //返回的分别是三个级别的选中位置
//                String tx = options1Items.get(options1).getPickerViewText()
//                        + options2Items.get(options1).get(option2)
//                        + options3Items.get(options1).get(option2).get(options3).getPickerViewText();
//                tvOptions.setText(tx);
                String selected = transmittedPowerList.get(options1);
                writeTransmittedPower(Integer.valueOf(selected));
            }
        }).build();
        pvTransmittedPower.setPicker(transmittedPowerList);

        btnEditTransmittedPower = (Button)findViewById(R.id.btn_edit_transmitted_power);
        btnEditTransmittedPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTransmittedPower.setSelectOptions(0);
                for(int i = 0;i < transmittedPowerList.size();i++){
                    String item  = transmittedPowerList.get(i);
                    if(String.valueOf(transmittedPower).equals(item)){
                        pvTransmittedPower.setSelectOptions(i);
                        break;
                    }
                }
                pvTransmittedPower.show();
            }
        });
        btnDebugUpgrade = (Button) findViewById(R.id.btn_debug_upgrade);
        if (MyUtils.isDebug){
            btnDebugUpgrade.setVisibility(View.VISIBLE);
        }
        btnDebugUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog urlDlg = new SweetAlertDialog(EditActivity.this, SweetAlertDialog.INPUT_TYPE);
                urlDlg.setTitleText(getResources().getString(R.string.input_debug_upgrade_url));
                urlDlg.setCancelable(true);
                urlDlg.setCancelText(getResources().getString(R.string.cancel));
                urlDlg.setConfirmText(getResources().getString(R.string.confirm));
                urlDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if (sweetAlertDialog.getInputText() != null && sweetAlertDialog.getInputText().trim() != null){
                            sweetAlertDialog.hide();
                            mClient.disconnect(mac);
                            connectSucc = false;
                            showWaitingDlg(getResources().getString(R.string.prepare_upgrade));
                            DownloadFileManager.instance().geetDebugUpdateFileUrl(EditActivity.this, sweetAlertDialog.getInputText().trim(), new DownloadFileManager.Callback() {
                                @Override
                                public void callback(StatusCode code, String result) {
                                    if(StatusCode.OK == code){
                                        updateDFU(mac,deviceName,result);
                                    }else{
                                        waitingDlg.hide();
                                        Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_LONG);
                                    }
                                }
                            });
                        }

                    }
                });
                urlDlg.show();
            }
        });
        getServerUpgradeInfo();
        initDiffUI();
    }

    private void upgradeDevice(){
        mClient.disconnect(mac);
        connectSucc = false;
        DownloadFileManager.instance().geetUpdateFileUrl(EditActivity.this, upgradeLink, serverVersion, deviceType, new DownloadFileManager.Callback() {
            @Override
            public void callback(StatusCode code, String result) {
                if(StatusCode.OK == code){
                    updateDFU(mac,deviceName,result);
                }else{
                    waitingDlg.hide();
                    Toast.makeText(EditActivity.this,R.string.download_file_fail,Toast.LENGTH_LONG);
                }
            }
        });
    }

    private DfuServiceController dfuServiceController;
    private void updateDFU(String mac,String name,String path){
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
                if (onThisView){
                    Toast.makeText(EditActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
                }
                connectSucc = false;
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
            Log.e("BluetoothUtils","onDfuCompleted");
            upgradeStatus = "completed";
            Toast.makeText(EditActivity.this,R.string.upgrade_succ,Toast.LENGTH_LONG);
            waitingDlg.setTitleText(getResources().getString(R.string.reconnecting));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mClient.connect(mac,bleConnectResponse);
            mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            Log.e("BluetoothUtils","onDfuAborted");
            upgradeStatus = "aborted";
            waitingDlg.hide();
            Toast.makeText(EditActivity.this,R.string.upgrade_aborted,Toast.LENGTH_LONG);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            Log.e("BluetoothUtils","onError");
            upgradeStatus = "error";
            upgradeErrorMsg = message;
            waitingDlg.hide();
            Toast.makeText(EditActivity.this,message,Toast.LENGTH_LONG);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
//            Log.e("BluetoothUtils","onProgressChanged:"+percent);
            progressPercent = percent;
            upgradeStatus = "progressChanged";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.processing)+ ":" + percent);
            }

        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.e("BluetoothUtils","onDfuProcessStarted");
            upgradeStatus = "processStarted";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_process_start));
            }

        }

        @Override
        public void onDeviceConnecting(String deviceAddress) {
            Log.e("BluetoothUtils","onDeviceConnecting");
            upgradeStatus = "deviceConnecting";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_device_connecting));
            }

        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            Log.e("BluetoothUtils","onDeviceDisconnecting");
            upgradeStatus = "deviceDisconnected";

        }
    };
    private void getServerUpgradeInfo() {
        String deviceTypeParam = deviceType;
        if(deviceType.equals("S02") && Integer.valueOf(software) <= 8){
            deviceTypeParam = "S01";
        }
        if(!MyUtils.isNetworkConnected(EditActivity.this)){
            Toast.makeText(EditActivity.this,R.string.network_permission_fail,Toast.LENGTH_SHORT).show();
            return;
        }
        OpenAPI.instance().getServerVersion(deviceTypeParam,new OpenAPI.Callback() {
            @Override
            public void callback(StatusCode code, String result) {
                if(code == StatusCode.OK){
                    JSONObject resObj = JSONObject.parseObject(result);
                    int jsonCode = resObj.getInteger("code");
                    if(jsonCode == 0){
                        JSONObject jsonData  = resObj.getJSONObject("data");
                        if(jsonData != null){
                            String serverVersionStr = jsonData.getString("version");
                            if(serverVersionStr!= null && !serverVersionStr.isEmpty()){
                                serverVersion = Integer.valueOf(serverVersionStr.replaceAll("V","").replaceAll("v",""));
                            }
                            upgradeLink = jsonData.getString("link");
                            showUpgradeBtn();
                        }
                    }else{
                        Toast.makeText(EditActivity.this,R.string.get_upgrade_info_fail,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    Toast.makeText(EditActivity.this,R.string.get_upgrade_info_fail,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void showUpgradeBtn(){
        Integer softwareInt = Integer.valueOf(software);
        if(serverVersion != -1 && serverVersion != softwareInt){
            btnUpgrade.setVisibility(View.VISIBLE);
        }else{
            btnUpgrade.setVisibility(View.INVISIBLE);
        }
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
        saveRecordIntervalLL.setVisibility(View.GONE);
        humidityLowAlarmLL.setVisibility(View.GONE);
        humidityHighAlarmLL.setVisibility(View.GONE);
        recordControlLL.setVisibility(View.GONE);
        relayLL.setVisibility(View.GONE);
        clearRecordLL.setVisibility(View.GONE);
        saveRecordIntervaLineLL = (LinearLayout)findViewById(R.id.line_line_save_record_interval);
        humidityLowAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_humidity_low_alarm);
        humidityHighAlarmLineLL = (LinearLayout)findViewById(R.id.line_line_humidity_high_alarm);
        recordControlLineLL = (LinearLayout)findViewById(R.id.line_line_record_control);
        relayLineLL = (LinearLayout)findViewById(R.id.line_line_relay);
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
        readSaveCountLineLL.setVisibility(View.GONE);
        readSaveCountLL.setVisibility(View.GONE);
        readAlarmLL.setVisibility(View.GONE);
        readAlarmLineLL.setVisibility(View.GONE);
        if(Integer.valueOf(software) >= 13){
            transmittedPowerLL.setVisibility(View.VISIBLE);
            transmittedPowerLineLL.setVisibility(View.VISIBLE);
        }else{
            transmittedPowerLL.setVisibility(View.GONE);
            transmittedPowerLineLL.setVisibility(View.GONE);
        }
        if (deviceType.equals("S04")){
            if(Integer.valueOf(software) > 10){
                recordControlLL.setVisibility(View.VISIBLE);
                recordControlLineLL.setVisibility(View.VISIBLE);
                readAlarmLL.setVisibility(View.VISIBLE);
                readAlarmLineLL.setVisibility(View.VISIBLE);
                clearRecordLL.setVisibility(View.VISIBLE);
                clearRecordLineLL.setVisibility(View.VISIBLE);
            }
        }else if (deviceType.equals("S05")){
            relayLL.setVisibility(View.VISIBLE);
            relayLineLL.setVisibility(View.VISIBLE);
        }else{
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

            humidityLowAlarmLL.setVisibility(View.VISIBLE);
            humidityHighAlarmLL.setVisibility(View.VISIBLE);
            humidityLowAlarmLineLL.setVisibility(View.VISIBLE);
            humidityHighAlarmLineLL.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onThisView = true;
        if(requestCode == REQUEST_CHANGE_PWD && resultCode == RESPONSE_CHANGE_PWD){
            newPwd = data.getStringExtra("newPwd");
            if(newPwd != null && newPwd.length() == 6){
                writePwd(newPwd);
            }
        }else if(requestCode == REQUEST_CHANGE_TEMP && resultCode == RESPONSE_CHANGE_TEMP){
            int highValue = data.getIntExtra("highValue",4095);
            int lowValue = data.getIntExtra("lowValue",4095);
            writeTempAlarmData(highValue,lowValue);
        }else if(requestCode == REQUEST_CHANGE_HUMIDITY && resultCode == RESPONSE_CHANGE_HUMIDITY){
            int highValue = data.getIntExtra("highValue",4095);
            int lowValue = data.getIntExtra("lowValue",4095);
            writeHumidityAlarmData(highValue,lowValue);
        }else if(requestCode == REQUEST_READ_HISTORY_TIME && resultCode == RESPONSE_READ_HISTORY_TIME){
            if (startHistory){
                return;
            }
            historyIndex = 0;
            startHistory = true;
            originHistoryList.clear();
            startTimestamp = data.getLongExtra("startDate",0);
            endTimestamp = data.getLongExtra("endDate",0);
            showWaitingCancelDlg(getResources().getString(R.string.loading));
            readHistory();
        }else if(requestCode == REQUEST_READ_ALARM_TIME && resultCode == RESPONSE_READ_ALARM_TIME){
            if (startHistory){
                return;
            }
            startHistory = true;
            showWaitingCancelDlg(getResources().getString(R.string.loading));
            startTimestamp = data.getLongExtra("startDate",0);
            endTimestamp = data.getLongExtra("endDate",0);
            readAlarm();
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
            if(pwd.length() == 6){
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
                fixTime();
            }else{
                Toast.makeText(EditActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();

            }
        }
    };

    private UUID serviceId = UUID.fromString("27760001-999C-4D6A-9FC4-C7272BE10900");
    private UUID uuid = UUID.fromString("27763561-999C-4D6A-9FC4-C7272BE10900");

    BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile data) {
            if (code == REQUEST_SUCCESS) {
                mClient.notify(mac, serviceId, uuid, bleNotifyResponse);
            }else{
                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                waitingDlg.hide();
            }
        }
    };
    BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            reconnectBtn.setImageResource(R.mipmap.ic_refresh);
            Log.e("myLog",MyUtils.bytes2HexString(value,0));
            if(value.length > 1){
                int status = value[0];
                int type = value[1];
                if (status == 0){
                    if(type == MyUtils.controlFunc.get("deviceName").get("read")
                        || type == MyUtils.controlFunc.get("deviceName").get("write")){
                        readDeviceNameParse(value);
                    }else if(type == MyUtils.controlFunc.get("password").get("write")){
                        Toast.makeText(EditActivity.this,R.string.password_has_been_updated,Toast.LENGTH_SHORT).show();
                        confirmPwd = newPwd;
                    }else if(type == MyUtils.controlFunc.get("broadcastCycle").get("read")
                            || type == MyUtils.controlFunc.get("broadcastCycle").get("write")){
                        readBroadcastCycleResp(value);
                    }else if(type == MyUtils.controlFunc.get("transmittedPower").get("read")
                            || type == MyUtils.controlFunc.get("transmittedPower").get("write")){
                        readTransmittedPowerResp(value);
                    }else if(type == MyUtils.controlFunc.get("saveCount").get("read")){
                        readSaveCountResp(value);
                        waitingDlg.hide();
                    }else if(type == MyUtils.controlFunc.get("readOriginData").get("read")){
                        readHistoryResp(value);
                    }else if(type == MyUtils.controlFunc.get("readAlarm").get("read")){
                        readAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("readNextAlarm").get("read")){
                        readNextAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("saveInterval").get("read")
                            || type == MyUtils.controlFunc.get("saveInterval").get("write")){
                        readSaveIntervalResp(value);
                    }else if(type == MyUtils.controlFunc.get("time").get("read")
                            || type == MyUtils.controlFunc.get("time").get("write")){
//
                    }else if(type == MyUtils.controlFunc.get("firmware").get("read")){
                        readVersionResp(value);
                    }else if(type == MyUtils.controlFunc.get("humidityAlarm").get("read")
                            || type == MyUtils.controlFunc.get("humidityAlarm").get("write")){
                        readHumidityAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("tempAlarm").get("read")
                            || type == MyUtils.controlFunc.get("tempAlarm").get("write")){
                        readTempAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("tempAlarm").get("read")
                            || type == MyUtils.controlFunc.get("tempAlarm").get("write")){
                        readTempAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("startRecord").get("write")){
                        startRecordResp(value);
                    }else if(type == MyUtils.controlFunc.get("stopRecord").get("write")){
                        stopRecordResp(value);
                    }else if(type == MyUtils.controlFunc.get("clearRecord").get("write")){
                        clearRecordResp(value);
                    }else if(type == MyUtils.controlFunc.get("ledOpen").get("read")
                            || type == MyUtils.controlFunc.get("ledOpen").get("write")){
                        readLedOpenStatusResp(value);
                    }else if(type == MyUtils.controlFunc.get("relay").get("read")
                            || type == MyUtils.controlFunc.get("relay").get("write")){
                        readRelayStatusResp(value);
                    }else if(type == MyUtils.controlFunc.get("resetFactory").get("write")){
                        Toast.makeText(EditActivity.this,R.string.factory_reset_succ,Toast.LENGTH_LONG).show();
                        showPwdDlg();
                        //read all params
                    }
                }else if (status == 1){
                    waitingDlg.hide();
                    if(!pwdErrorWarning){
                        Toast.makeText(EditActivity.this,R.string.password_is_error,Toast.LENGTH_SHORT).show();
                        pwdErrorWarning = true;
                        showPwdDlg();
                    }
                }else if (status == 7){
                    if(type == MyUtils.controlFunc.get("readAlarm").get("read")){
                        readAlarmResp(value);
                    }else if(type == MyUtils.controlFunc.get("readNextAlarm").get("read")){
                        readNextAlarmResp(value);
                    }
                }else {
                    waitingDlg.hide();
                    Toast.makeText(EditActivity.this,R.string.error_please_try_again,Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                connectSucc = true;
//                byte[] readDeviceName = {54, 53, 52, 51, 50, 49, 103, 36};
//                mClient.write(mac, serviceId, uuid, readDeviceName, new BleWriteResponse() {
//                    @Override
//                    public void onResponse(int code) {
//                        if (code == REQUEST_SUCCESS) {
//
//                        }
//                    }
//                });
                waitingDlg.hide();
                showPwdDlg();
            }else{
                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                waitingDlg.hide();
                mClient.disconnect(mac);
            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        pwdErrorWarning = false;
        onThisView = true;
        if(!connectSucc){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showWaitingDlg(getResources().getString(R.string.connecting));
            mClient.connect(mac, bleConnectResponse);
            mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onThisView = false;
        pwdErrorWarning = false;
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


        mClient.disconnect(mac);
        connectSucc = false;
    }

    private void readDeviceName(){
        if(!isCurrentDeviceTypeFunc("deviceName")){
            return;
        }
        readData(MyUtils.controlFunc.get("deviceName").get("read"));
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
        readData(MyUtils.controlFunc.get("transmittedPower").get("read"));
    }

    private void readTransmittedPowerResp(byte[] resp){
        this.transmittedPower = resp[2];
        transmittedPowerTV.setText(String.format("%d dBm",this.transmittedPower));
    }
    private void readBroadcastCycle(){
        if(!isCurrentDeviceTypeFunc("broadcastCycle")){
            return;
        }
        readData(MyUtils.controlFunc.get("broadcastCycle").get("read"));
    }

    private void readBroadcastCycleResp(byte[] resp){
       int broadcast = (resp[2] << 8) + (resp[3]& 0xff);
       broadcastCycleTV.setText(String.valueOf(broadcast) + "s");
    }

    private void readSaveCount(){
        if(!isCurrentDeviceTypeFunc("saveCount")){
            return;
        }
        readData(MyUtils.controlFunc.get("saveCount").get("read"));
    }

    private void readSaveCountResp(byte[] resp){
        this.isSaveRecordStatus = resp[2] == 0 ? false : true;
        this.saveCount = (resp[3] << 8) + (resp[4]& 0xff);
        this.saveAlarmCount = (resp[5] << 8) + (resp[6]& 0xff);
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
        writeArrayData(MyUtils.controlFunc.get("readAlarm").get("read"),value);
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
        writeArrayData(MyUtils.controlFunc.get("readNextAlarm").get("read"),endDateValue);
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
        writeArrayData(MyUtils.controlFunc.get("readOriginData").get("read"),value);
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
                        ArrayList<BleHisData> bleHisDataList = MyUtils.parseS02BleHisData(byteDataArray);
                        allBleHisData.addAll(bleHisDataList);
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
        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("startRecord").get("write"),value);
    }

    private void startRecordResp(byte[] resp){
        readSaveCount();
    }

    private void stopRecord(){
        if(!isCurrentDeviceTypeFunc("stopRecord")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("stopRecord").get("write"),null);
    }
    private void stopRecordResp(byte[] resp){
        readSaveCount();
    }

    private void clearRecord(){
        if(!isCurrentDeviceTypeFunc("clearRecord")){
            return;
        }
        writeArrayData(MyUtils.controlFunc.get("clearRecord").get("write"),null);
    }
    private void clearRecordResp(byte[] resp){
        readSaveCount();
    }

    private void readSaveInterval(){
        if(!isCurrentDeviceTypeFunc("saveInterval")){
            return;
        }
        readData(MyUtils.controlFunc.get("saveInterval").get("read"));
    }

    private void readSaveIntervalResp(byte[] resp){
        this.saveInterval = (resp[2] << 8) + (resp[3] & 0xff);
        saveRecordIntervalTV.setText(String.valueOf(saveInterval) + "s");

    }


    private void fixTime(){
        Date now = new Date();
        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("time").get("write"),value);

    }

    private void readVersion(){
        if(!isCurrentDeviceTypeFunc("firmware")){
            return;
        }
        readData(MyUtils.controlFunc.get("firmware").get("read"));
    }
    private void readVersionResp(byte[] resp){
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
        showUpgradeBtn();
    }
    private void readHumidityAlarm(){
        if(!isCurrentDeviceTypeFunc("humidityAlarm")){
            return;
        }
        readData(MyUtils.controlFunc.get("humidityAlarm").get("read"));
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
        readData(MyUtils.controlFunc.get("tempAlarm").get("read"));
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
            tempHighAlarmTV.setText(String.format("%.1f %s",tempAlarmUp, BleDeviceData.getCurTempUnit(EditActivity.this)));
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
            tempLowAlarmTV.setText(String.format("%.1f %s",tempAlarmDown,BleDeviceData.getCurTempUnit(EditActivity.this)));
        }
    }

    private void readLedOpenStatus(){
        if(!isCurrentDeviceTypeFunc("ledOpen")){
            return;
        }
        readData(MyUtils.controlFunc.get("ledOpen").get("read"));
    }
    private void readLedOpenStatusResp(byte[] resp){
        if (resp[2] == 0){
            ledSwitch.setSwitchStatus(false);
        }else{
            ledSwitch.setSwitchStatus(true);
        }
    }

    private void readRelayStatus(){
        if(!isCurrentDeviceTypeFunc("relay")){
            return;
        }
        readData(MyUtils.controlFunc.get("relay").get("read"));
    }
    private void readRelayStatusResp(byte[] resp){
        if (resp[2] == 0){
            relayStatusTV.setText("NC");
            relaySwitch.setSwitchStatus(false);
        }else{
            relayStatusTV.setText("NO");
            relaySwitch.setSwitchStatus(true);
        }
    }

    private void readData(int cmd){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,null);
        Log.e("myLog",MyUtils.bytes2HexString(content,0));

        mClient.writeNoRsp(mac, serviceId, uuid, content, new BleWriteResponse() {
            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {

                }
            }
        });
    }


    private void writeStrData(int cmd,String dataStr){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] data = dataStr.getBytes();
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,data);
        Log.e("myLog",MyUtils.bytes2HexString(content,0));
        if (content != null && content.length > 0){
            mClient.writeNoRsp(mac, serviceId, uuid, content, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {

                    }
                }
            });
        }

    }
    private void writeArrayData(int cmd,byte[] realContent){
        if(!connectSucc){
            Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(confirmPwd,cmd,realContent);
        if (content != null && content.length > 0){
            Log.e("myLog",MyUtils.bytes2HexString(content,0));
            mClient.writeNoRsp(mac, serviceId, uuid, content, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {

                    }
                }
            });
        }

    }

    private void writePwd(String pwd){
        writeStrData(MyUtils.controlFunc.get("password").get("write"),pwd);
    }

    private void  writeDeviceName(String name){
        writeStrData(MyUtils.controlFunc.get("deviceName").get("write"), name);
    }

    private void writeTransmittedPower(int value){
        byte[] data = new byte[]{(byte)value};
        writeArrayData(MyUtils.controlFunc.get("transmittedPower").get("write"),data);
    }
    private void writeBroadcastData(int value){
        byte[] data = MyUtils.short2Bytes(value);
        writeArrayData(MyUtils.controlFunc.get("broadcastCycle").get("write"),data);
    }
    private void writeSaveRecordIntervalData(int value){
        byte[] data = MyUtils.short2Bytes(value);
        writeArrayData(MyUtils.controlFunc.get("saveInterval").get("write"),data);
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
        writeArrayData(MyUtils.controlFunc.get("humidityAlarm").get("write"),data);
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
        writeArrayData(MyUtils.controlFunc.get("tempAlarm").get("write"),data);
    }

    private void writeLedOpenStatus(){
        byte[] data;
        if(ledSwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("ledOpen").get("write"),data);
    }
    private void writeRelayStatus(){
        byte[] data;
        if(relaySwitch.getSwitchStatus()){
            data = new byte[]{1};
        }else{
            data = new byte[]{0};
        }
        writeArrayData(MyUtils.controlFunc.get("relay").get("write"),data);
    }
    private void writeResetFactory(){
        writeArrayData(MyUtils.controlFunc.get("resetFactory").get("write"),null);
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
                    || funcName.equals("stopRecord") || funcName.equals("clearRecord")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData")) {
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
                    || funcName.equals("tempAlarm") || funcName.equals("ledOpen") || funcName.equals("deviceName") || funcName.equals("relay")) {
                return true;
            }else{
                return false;
            }
        }else{
            if(funcName.equals("transmittedPower")){
                if(Integer.valueOf(software) >= 13){
                    return true;
                }else{
                    return false;
                }
            }
            if(Integer.valueOf(software) < 11 && (funcName.equals("saveInterval") ||funcName.equals("readHistory")
                    || funcName.equals("saveCount") || funcName.equals("readAlarm") || funcName.equals("readOriginData")  || funcName.equals("startRecord"))){
                return false;
            }
            if (funcName.equals("relay")){
                return false;
            }else{
                return true;
            }
        }
    }



}
