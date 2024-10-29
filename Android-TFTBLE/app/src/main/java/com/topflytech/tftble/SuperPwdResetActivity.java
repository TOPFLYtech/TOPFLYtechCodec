package com.topflytech.tftble;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.BleHisData;
import com.topflytech.tftble.data.DfuService;
import com.topflytech.tftble.data.DownloadFileManager;
import com.topflytech.tftble.data.LogFileHelper;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.OpenAPI;
import com.topflytech.tftble.data.SingleClickListener;
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
public class SuperPwdResetActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    private BluetoothClient mClient;
    private String mac;
    private String deviceType;
    private String id;
    private String software;
    private String confirmPwd;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog connectWaitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;

    EditText pwdEdit;
    boolean connectSucc = false;

    boolean onThisView = false;


    private boolean pwdErrorWarning = false;
     private boolean isWaitResponse = false;
    private LinkedBlockingQueue<WriteSensorObj> waitingSendMsgQueue = new LinkedBlockingQueue<>();
    private boolean isSendMsgThreadRunning = true;

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


        mClient = new BluetoothClient(SuperPwdResetActivity.this);
        initUI();
        Thread dealSendMsgThread = new Thread(dealSendMsgRunnable);
        dealSendMsgThread.start();
    }

    private Runnable dealSendMsgRunnable = new Runnable() {
        @Override
        public void run() {
            while (isSendMsgThreadRunning){
                try {
                    if(isWaitResponse){
                        Thread.sleep(100);
                        continue;
                    }
                    if(waitingSendMsgQueue.size() > 0){
                        WriteSensorObj writeSensorObj = waitingSendMsgQueue.poll();
                        if(mClient != null && connectSucc){
                            LogFileHelper.getInstance(SuperPwdResetActivity.this).writeIntoFile(MyUtils.bytes2HexString(writeSensorObj.getContent(),0));
                            Log.e("myLog",MyUtils.bytes2HexString(writeSensorObj.getContent(),0));
                            isWaitResponse = true;
                            mClient.writeNoRsp(mac, serviceId, writeSensorObj.getCurUUID(), writeSensorObj.getContent(), new BleWriteResponse() {
                                @Override
                                public void onResponse(int code) {
                                    if (code == REQUEST_SUCCESS) {

                                    }
                                }
                            }); 
                        }
                    }else{
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void showWaitingDlg(String warning){
        if(!onThisView){
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

    private void connectFailTimeoutShow(long tick){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 要执行的操作
                if(!connectSucc && onThisView){
                    if(connectWaitingCancelDlg != null){
                        connectWaitingCancelDlg.hide();
                    }
                    Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
                }
            }
        }, tick);
    }

    private void doReconnect(){
        if(connectWaitingCancelDlg != null){
            connectWaitingCancelDlg.hide();
        }
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
        mClient.disconnect(mac);
        connectSucc = false;
        showConnectWaitingCancelDlg(getResources().getString(R.string.reconnecting));

        connectDeviceBle();
    }

    private void writePwd(String pwd){
        writeStrData(MyUtils.controlFunc.get("password").get("write"),pwd,uuid);
    }

    private Button btnEditPwd,btnResetFactory;
    private void initUI(){

        btnEditPwd = (Button)findViewById(R.id.btn_edit_pwd);
        btnEditPwd.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if(!connectSucc){
                    Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_reset_password_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmRelayDlg.hide();
                        writePwd("654321");
                    }
                });
                confirmRelayDlg.show();
            }
        });
        btnResetFactory = (Button)findViewById(R.id.btn_reset_factory);
        btnResetFactory.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if(!connectSucc){
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


    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {

            } else if (status == STATUS_DISCONNECTED) {
                if (onThisView){
                    if(connectSucc || curTryConnectCount >= tryConnectCount){
                        if(connectWaitingCancelDlg != null){
                            connectWaitingCancelDlg.hide();
                        }
                        Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
                    }
                }
                curTryConnectCount++;
                connectSucc = false;
                reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
            }
        }
    };





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
    
    BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile data) {
            if (code == REQUEST_SUCCESS) {
                Log.e("bleConnect","connected");
                mClient.notify(mac, serviceId, uuid, bleNotifyResponse); 
            }else{
                new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                if(waitingDlg != null){
                    waitingDlg.hide();
                }
            }
        }
    };
    BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            reconnectBtn.setImageResource(R.mipmap.ic_refresh);
            Log.e("myLog","resp:" + MyUtils.bytes2HexString(value,0));
            isWaitResponse = false;
            LogFileHelper.getInstance(SuperPwdResetActivity.this).writeIntoFile("resp:" + MyUtils.bytes2HexString(value,0));
            if(value.length > 1){
                int status = value[0];
                int type = value[1] & 0xff;
                if (status == 0){
                    if(type == MyUtils.controlFunc.get("password").get("write")){
                        Toast.makeText(SuperPwdResetActivity.this,R.string.password_has_been_reset,Toast.LENGTH_SHORT).show();
                        finish();
                    }else if(type == MyUtils.controlFunc.get("resetFactory").get("write")){
                        Toast.makeText(SuperPwdResetActivity.this,R.string.factory_reset_succ,Toast.LENGTH_SHORT).show();
                        finish();
                        //read all params
                    }
                }else if (status == 1){
                    if(waitingDlg != null){
                        waitingDlg.hide();
                    }
                    if(!pwdErrorWarning){
                        Toast.makeText(SuperPwdResetActivity.this,R.string.super_password_is_error,Toast.LENGTH_SHORT).show();
                        pwdErrorWarning = true;
                        showPwdDlg();
                    }
                } else {
                    if(waitingDlg != null){
                        waitingDlg.hide();
                    }
                    Toast.makeText(SuperPwdResetActivity.this,R.string.error_please_try_again,Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                if(!connectSucc){
                    connectWaitingCancelDlg.hide();
                    showPwdDlg();
                }
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

            }else{
                new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                connectWaitingCancelDlg.hide();
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
            if(waitingDlg != null){
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
        mClient.connect(mac, conectOptions, bleConnectResponse);
        connectFailTimeoutShow(60000);
        mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
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
        mClient.disconnect(mac);
        connectSucc = false;
    }




    private void fixTime(){
        Date now = new Date();
        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("time").get("write"),value,"topfly",uuid);

    }




    private void readData(int cmd,UUID curUUID){
        if(!connectSucc){
            Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
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
            Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
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
    private void writeArrayData(int cmd,byte[] realContent,String inPwd,UUID curUUID){
        if(!connectSucc){
            Toast.makeText(SuperPwdResetActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] content = MyUtils.getInterActiveCmd(inPwd,cmd,realContent);
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


    private void writeResetFactory(){
        writeArrayData(MyUtils.controlFunc.get("resetFactory").get("write"),null,uuid);
    }

}
