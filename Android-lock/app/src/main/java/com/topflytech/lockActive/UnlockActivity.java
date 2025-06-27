package com.topflytech.lockActive;


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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.topflytech.lockActive.Ble.A001SoftwareUpgradeManager;
import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.Ble.TftBleConnectManager;
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
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UnlockActivity extends AppCompatActivity {
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

    private String software;
    private String deviceId;
    private String imei;




    private String model = "";


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
            if(!onThisView){
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
        if(!onThisView){
            return;
        }
        if(warningDlg != null){
            if(warning != null){
                warningDlg.setContentText(warning);
                warningDlg.show();
            }
            return;
        }
        warningDlg = new SweetAlertDialog(UnlockActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getResources().getString(R.string.warning));
        if(warning != null){
            warningDlg.setContentText(warning);
        }
        warningDlg.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_edit);
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
        if(BleDeviceData.isSubLockDevice(model)){
            lnActiveNetwork.setVisibility(View.GONE);
        }else{
            lnActiveNetwork.setVisibility(View.VISIBLE);
        }
        TftBleConnectManager.getInstance().init(UnlockActivity.this);
        TftBleConnectManager.getInstance().setNeedCheckDeviceReady(true);
        TftBleConnectManager.getInstance().setNeedGetLockStatus(true);
        TftBleConnectManager.getInstance().setCallback("UnlockActivity",bleStatusCallback);

    }

    private boolean isValidDeviceId(String deviceId) {
        // Check if the ID is a 10-digit hexadecimal string
        return Pattern.matches("[0-9A-Fa-f]{10}", deviceId);
    }


    private void fixTime(){
        if(!BleDeviceData.isSubLockDevice(model)){
            return;
        }
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


    private String confirmPwd, newPwd;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onThisView = true;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);

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
        waitingDlg = new SweetAlertDialog(UnlockActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
        waitingCancelDlg = new SweetAlertDialog(UnlockActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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

    private void showUnclockPwdDlg(){
        if(!onThisView){
            return;
        }
        sweetPwdDlg = new SweetAlertDialog(UnlockActivity.this, SweetAlertDialog.INPUT_TYPE);
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
                    Toast.makeText(UnlockActivity.this,R.string.pwd_format_error,Toast.LENGTH_SHORT).show();
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




    private void parseResp(byte[] respContent){
        int i = 0;
        while (i + 3 <= respContent.length){
            byte[] head = new byte[]{respContent[i],respContent[i+1],respContent[i+2]};
            if (Arrays.equals(head,BleDeviceData.deviceReadyHead)){
                if ( i + 6 <= respContent.length){
                    int status = respContent[i + 5];
                    if (status == 0x01){
                        TftBleConnectManager.getInstance().setDeviceReady(true);
                        fixTime();
                        TftBleConnectManager.getInstance().getLockStatus();
                        UnlockActivity.this.runOnUiThread(new Runnable() {
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
                    UnlockActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(waitingCancelDlg != null){
                                waitingCancelDlg.hide();
                            }

                        }
                    });
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.unlockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    if((lockType & 0xff) == 0xff){
                        Toast.makeText(UnlockActivity.this,R.string.unlock_pwd_error,Toast.LENGTH_SHORT).show();
                        showUnclockPwdDlg();
                    }else{
                        setLockRefreshTime();
                        UnlockActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(waitingCancelDlg != null){
                                    waitingCancelDlg.hide();
                                }
                            }
                        });
                        parseLockType(lockType);
                    }
                }else{
                    int errorCode = respContent[i+3] & 0x7f;
                    if(errorCode == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                        Toast.makeText(UnlockActivity.this,R.string.unlock_pwd_error,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(UnlockActivity.this,R.string.fail,Toast.LENGTH_SHORT).show();
                    }
                }
                i+=5;
            }else if (Arrays.equals(head,BleDeviceData.lockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
//                    setLockRefreshTime();
                    UnlockActivity.this.runOnUiThread(new Runnable() {
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
                    UnlockActivity.this.runOnUiThread(new Runnable() {
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
                    UnlockActivity.this.runOnUiThread(new Runnable() {
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
                                Toast.makeText(UnlockActivity.this,R.string.unlock_pwd_error,Toast.LENGTH_SHORT).show();
                                showUnclockPwdDlg();
                            }else if(bleRespData.getControlCode() == BleDeviceData.func_id_of_change_unclock_pwd){
                                Toast.makeText(UnlockActivity.this,R.string.old_unlock_error,Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(UnlockActivity.this,R.string.ble_pwd_error,Toast.LENGTH_SHORT).show();

                            }

                        }else{
                            Toast.makeText(UnlockActivity.this,R.string.fail,Toast.LENGTH_SHORT).show();
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



    private void addLog(final String log){
        UnlockActivity.this.runOnUiThread(new Runnable() {
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
        if (lockType == 0xff || lockType == 0x04 || lockType == 0x09 || lockType == 0x0a){
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
        }else if (lockType == 0x0a){
            showDetailMsg(getString(R.string.lock_status_0a));
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