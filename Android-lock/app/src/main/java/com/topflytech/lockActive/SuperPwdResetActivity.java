package com.topflytech.lockActive;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.Ble.TftBleConnectManager;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BleRespData;
import com.topflytech.lockActive.data.LogFileHelper;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.Utils;
import com.topflytech.lockActive.deviceConfigSetting.SingleClickListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SuperPwdResetActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    private String mac;
    private String deviceType;
    private String id;
    private String software;
    private String confirmPwd;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog warningDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog connectWaitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;

    EditText pwdEdit;

    boolean onThisView = false;
    private String model = "";

    private boolean pwdErrorWarning = false;
    private boolean isWaitResponse = false;
    private boolean isSendMsgThreadRunning = true;


    private BleStatusCallback bleStatusCallback = new BleStatusCallback(){
        @Override
        public void onNotifyValue(byte[] value) {
            if(!onThisView){
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        if(TftBleConnectManager.getInstance().isNeedCheckDeviceReady()){
                            showWaitingCancelDlg(getString(R.string.waitingDeviceReady));
                        }else{
                            if(connectWaitingCancelDlg != null){
                                connectWaitingCancelDlg.hide();
                            }
                            showPwdDlg();
                        }
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CLOSE) {

                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_DISCONNECT) {
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                        showWarningDlg(getResources().getString(R.string.connect_fail));


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
        warningDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getResources().getString(R.string.warning));
        if(warning != null){
            warningDlg.setContentText(warning);
        }
        warningDlg.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_pwd_reset);
        onThisView = true;
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        software = intent.getStringExtra("software");
        deviceType = intent.getStringExtra("deviceType");
        id = intent.getStringExtra("id");
        initActionbar();
        TftBleConnectManager.getInstance().setOnThisView(onThisView); 
        model = intent.getStringExtra("model");
        TftBleConnectManager.getInstance().init(SuperPwdResetActivity.this);
        TftBleConnectManager.getInstance().setNeedCheckDeviceReady(false);
        TftBleConnectManager.getInstance().setNeedGetLockStatus(false);
        TftBleConnectManager.getInstance().setCallback("SuperPwdResetActivity",bleStatusCallback);

        initUI();

    }



    private void showWaitingDlg(String warning){
        if(!onThisView){
            return;
        }
        if(waitingDlg != null){
            if (warning != null && !warning.isEmpty()){
                waitingDlg.setTitleText(warning);
                waitingDlg.show();
            }
            return;
        }
        waitingDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
            waitingDlg.show();
        }
    }
    private void showConnectWaitingCancelDlg(String warning){
        connectWaitingCancelDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        connectWaitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        connectWaitingCancelDlg.showCancelButton(true);
        connectWaitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
        connectWaitingCancelDlg.setCancelClickListener(sweetPwdCancelClick);
        if (warning != null && !warning.isEmpty()){
            connectWaitingCancelDlg.setTitleText(warning);
        }
        connectWaitingCancelDlg.show();
    }
    private void showWaitingCancelDlg(String warning){
        if(waitingCancelDlg != null){
            if(warning != null){
                waitingCancelDlg.setContentText(warning);
                waitingCancelDlg.show();
            }
            return;
        }
        waitingCancelDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
        if(sweetPwdDlg != null){
            sweetPwdDlg.setInputText("");
            sweetPwdDlg.show();
            return;
        }
        sweetPwdDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.INPUT_TYPE);
        sweetPwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        sweetPwdDlg.setTitleText(getResources().getString(R.string.input_super_device_password));
        sweetPwdDlg.setCancelable(true);
        sweetPwdDlg.setCancelText(getResources().getString(R.string.cancel));
        sweetPwdDlg.setConfirmText(getResources().getString(R.string.confirm));
        sweetPwdDlg.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        sweetPwdDlg.setConfirmClickListener(sweetPwdConfirmClick);
        sweetPwdDlg.setCancelClickListener(sweetPwdCancelClick);
        sweetPwdDlg.setInputText("");
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



    private void doReconnect(){
        if(connectWaitingCancelDlg != null){
            connectWaitingCancelDlg.hide();
        }
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
        TftBleConnectManager.getInstance().disconnect();

        showConnectWaitingCancelDlg(getResources().getString(R.string.reconnecting));

        TftBleConnectManager.getInstance().connect(mac, BleDeviceData.isSubLockDevice(model));
    }
    private void changeUnclockPwd(String pwd){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(confirmPwd.getBytes());
            outputStream.write(pwd.getBytes());
            byte[] content = outputStream.toByteArray();
            ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_change_unclock_pwd,content,this.confirmPwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void changeBlePwd(String pwd){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(confirmPwd.getBytes());
            outputStream.write(pwd.getBytes());
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

    private Button btnEditBlePwd,btnResetFactory,btnEditUnclockPwd;
    private void initUI(){

        btnEditBlePwd = (Button)findViewById(R.id.btn_edit_ble_pwd);
        btnEditBlePwd.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_reset_ble_password_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRelayDlg.hide();
                        changeBlePwd("654321");
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnEditUnclockPwd = (Button)findViewById(R.id.btn_edit_unclock_pwd);
        btnEditUnclockPwd.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_reset_unclock_password_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRelayDlg.hide();
                        changeUnclockPwd("654321");
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnResetFactory = (Button)findViewById(R.id.btn_reset_factory);
        btnResetFactory.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                    Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.NORMAL_TYPE);
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onThisView = true;

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
                fixTime();
            }else{
                Toast.makeText(SuperPwdResetActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();

            }
        }
    };

    private UUID serviceId = UUID.fromString("27760001-999C-4D6A-9FC4-C7272BE10900");
    private UUID uuid = UUID.fromString("27763561-999C-4D6A-9FC4-C7272BE10900");




    private void parseResp(byte[] value) {
        reconnectBtn.setImageResource(R.mipmap.ic_refresh);
        LogFileHelper.getInstance(SuperPwdResetActivity.this).writeIntoFile("resp:" + MyByteUtils.bytes2HexString(value,0));
        ArrayList<BleRespData> dataList = Utils.parseRespContent(value);
        for (BleRespData bleRespData : dataList){
            Log.e("writeContent","code:" + bleRespData.getControlCode());
            if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
                int code = bleRespData.getControlCode();
                String cmdStr = Utils.controlFunc.get(bleRespData.getControlCode());
                String labelValue = "";
                if (code == 1){
                    if (bleRespData.getData()[1] == 0x01){
                        TftBleConnectManager.getInstance().setDeviceReady(true);

                    }
                }
                else if(code == BleDeviceData.func_id_of_ble_pwd_change){
                    Toast.makeText(SuperPwdResetActivity.this,R.string.password_has_been_reset,Toast.LENGTH_SHORT).show();
                    finish();
                }else if(code == BleDeviceData.func_id_of_sub_lock_factory_reset){
                    Toast.makeText(SuperPwdResetActivity.this,R.string.factory_reset_succ,Toast.LENGTH_SHORT).show();
                    finish();
                    //read all params
                }else if(code == BleDeviceData.func_id_of_change_unclock_pwd){
                    Toast.makeText(SuperPwdResetActivity.this,R.string.password_has_been_reset,Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            else{
                if(bleRespData.getErrorCode() == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                    if(waitingDlg != null){
                        waitingDlg.hide();
                    }
                    if(!pwdErrorWarning){
                        Toast.makeText(SuperPwdResetActivity.this,R.string.super_password_is_error,Toast.LENGTH_SHORT).show();
                        pwdErrorWarning = true;
                        showPwdDlg();
                    }

                }else{
                    if(waitingDlg != null){
                        waitingDlg.hide();
                    }
                    Toast.makeText(SuperPwdResetActivity.this,R.string.error_please_try_again,Toast.LENGTH_SHORT).show();
                }


                Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType() + "-" + bleRespData.getErrorCode());
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        pwdErrorWarning = false;

        onThisView = true;
        TftBleConnectManager.getInstance().setOnThisView(onThisView);
        if(!TftBleConnectManager.getInstance().isConnectSucc()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(waitingDlg != null){
                waitingDlg.hide();
                waitingDlg.dismiss();
                waitingDlg = null;
            }
            showConnectWaitingCancelDlg(getResources().getString(R.string.connecting));
            TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        isSendMsgThreadRunning = false;
        TftBleConnectManager.getInstance().disconnect();
        TftBleConnectManager.getInstance().removeCallback("SuperPwdResetActivity");

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

    private void writeResetFactory(){
        byte[] content = new byte[]{0x00};
        ArrayList<byte[]> cmd = Utils.getWriteCmdContent(BleDeviceData.func_id_of_sub_lock_factory_reset,content,confirmPwd);
        TftBleConnectManager.getInstance().writeArrayContent(cmd);
    }

}
