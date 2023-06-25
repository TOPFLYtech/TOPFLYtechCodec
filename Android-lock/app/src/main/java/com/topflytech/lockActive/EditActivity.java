package com.topflytech.lockActive;

import static com.inuker.bluetooth.library.Code.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.UniqueIDTool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class EditActivity extends AppCompatActivity {
    public static boolean isDebug = false;
    public static boolean isOnlyActiveNetwork = false;

    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    private BluetoothClient mClient;
    boolean onThisView = false;
    private String mac;
    private String id;
    private String deviceName;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;
    boolean connectSucc = false;
    private boolean isCanSendMsg = true;
    private final static Object lock = new Object();
    private boolean isDeviceReady = false;


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
    private Button btnClearLog;
    private Button btnRefreshStatus;
    private String uniqueID;

    private byte[] deviceReadyHead = new byte[]{0x20,0x00,0x01};
    private byte[] getLockStatusHead = new byte[]{0x20,0x00,0x1D};
    private byte[] unlockHead = new byte[]{0x60,0x07,(byte)0xDA};
    private byte[] lockHead = new byte[]{0x60,0x07,(byte)0xDB};
    private byte[] activeNetworkHead = new byte[]{0x60,0x07,(byte)0xDC};
    private byte[] uploadStatusHead = new byte[]{0x30,(byte)0xA0,0x29};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        onThisView = true;
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        id = intent.getStringExtra("id");
        deviceName = intent.getStringExtra("deviceName");
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
                showPwdDlg();
            }

        });
        btnLock = (Button)findViewById(R.id.btn_lock);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] needSendBytes = getCmdContent(lockHead,new byte[]{0x00},true);
                writeContent(needSendBytes);
            }
        });
        btnActiveNetwork = (Button)findViewById(R.id.btn_active_network);
        btnActiveNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] needSendBytes = getCmdContent(activeNetworkHead,new byte[]{0x00},true);
                writeContent(needSendBytes);
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
                getLockStatus();
            }
        });
        mClient = new BluetoothClient(EditActivity.this);

        new Thread(sendMsgThread).start();
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
                    byte[] needSendBytes = getCmdContent(unlockHead,pwd.getBytes(),true);
                    writeContent(needSendBytes);
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

    private void getLockStatus(){
        byte[] needSendBytes = getLockStatusHead;
        writeContent(needSendBytes);
    }
    private Date lastCheckStatusDate;
    private long getStatusTimeout = 4000;
    private LinkedBlockingDeque<byte[]> sendMsgQueue = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<byte[]> sendMsgMultiQueue = new LinkedBlockingDeque<>();
    private Runnable sendMsgThread = new Runnable() {
        @Override
        public void run() {
            try {
                while (onThisView){
                    if ((sendMsgQueue.size() == 0 && sendMsgMultiQueue.size() == 0) || connectSucc == false || isDeviceReady == false){
                        if(!isDeviceReady){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            synchronized (lock){
                                isCanSendMsg = false;
                            }
                            sendCheckDeviceReadyCmd();
//                            EditActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//
//                                }
//                            });

                        }else{
                            Date now = new Date();
                            if (lastCheckStatusDate == null || now.getTime() - lastCheckStatusDate.getTime() > getStatusTimeout){
                                if(!isDebug){
                                    getLockStatus();
                                }
                            }
                            Thread.sleep(500);
                        }
                        continue;
                    }
                    if (isCanSendMsg){
                        if (sendMsgQueue.size() != 0){
                            synchronized (lock){
                                if (isCanSendMsg){
                                    isCanSendMsg = false;
                                    byte[] needSendBytes = sendMsgQueue.poll();
                                    if (needSendBytes != null){
                                        writeContent(needSendBytes);
                                    }
                                }
                            }
                            EditActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    showProgressBar();
                                }
                            });

                        }else{
                            while (sendMsgMultiQueue.size() != 0){
                                byte[] needSendBytes = sendMsgMultiQueue.poll();
                                if (needSendBytes != null){
                                    writeContent(needSendBytes);
                                }
                            }
                        }

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

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
                reconnectBtn.setImageResource(R.mipmap.ic_refresh);
                mClient.disconnect(mac);
                connectSucc = false;
                isDeviceReady = false;
                isCanSendMsg = false;
                showWaitingDlg(getResources().getString(R.string.reconnecting));
                mClient.connect(mac,bleConnectResponse);
                mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
        mClient.disconnect(mac);
        connectSucc = false;
        isDeviceReady = false;
        isCanSendMsg = false;
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
                isDeviceReady = false;
                isCanSendMsg = false;
                showDetailMsg(getString(R.string.disconnect_from_device));
                reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
            }
        }
    };

    BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile data) {
            if (code == REQUEST_SUCCESS) {
                mClient.notify(mac, BleDeviceData.unlockServiceId, BleDeviceData.unlockNotifyUUID, bleNotifyResponse);
            }else{
                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                waitingDlg.hide();
            }
        }
    };




    private void parseResp(byte[] respContent){
        int i = 0;
        while (i + 3 <= respContent.length){
            byte[] head = new byte[]{respContent[i],respContent[i+1],respContent[i+2]};
            if (Arrays.equals(head,deviceReadyHead)){
                if ( i + 6 <= respContent.length){
                    int status = respContent[i + 5];
                    if (status == 0x01){
                        isDeviceReady = true;
                        getLockStatus();
                        EditActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                waitingCancelDlg.hide();
                            }
                        });
                    }
                }
                i+=6;

            }else if (Arrays.equals(head,getLockStatusHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    parseLockType(lockType);
                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingCancelDlg.hide();
                        }
                    });
                }
                i+=5;
            }else if (Arrays.equals(head,unlockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingCancelDlg.hide();
                        }
                    });
                    parseLockType(lockType);
                }
                i+=5;
            }else if (Arrays.equals(head,lockHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingCancelDlg.hide();
                        }
                    });
                    parseLockType(lockType);
                }
                i+=5;
            }else if (Arrays.equals(head,activeNetworkHead)){
                if ( i + 5 <= respContent.length){
                    int status = respContent[i + 4];
                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingCancelDlg.hide();
                        }
                    });
                    showDetailMsg(getString(R.string.activeNetworkCmdSend));
                }
                i+=5;
            }else if (Arrays.equals(head,uploadStatusHead)){
                if ( i + 5 <= respContent.length){
                    int lockType = respContent[i + 4];
                    parseLockType(lockType);
                    setLockRefreshTime();
                    EditActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            waitingCancelDlg.hide();
                        }
                    });
                }
                i+=5;
            }else{
                i+=1;
            }
        }
    }

    BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            reconnectBtn.setImageResource(R.mipmap.ic_refresh);
            synchronized (lock){
                isCanSendMsg = true;
            }
            Log.e("writeContent","resp:" + MyByteUtils.bytes2HexString(value,0));
            addLog("resp:" + MyByteUtils.bytes2HexString(value,0));
            parseResp(value);
//            ArrayList<BleRespData> dataList = Utils.parseRespContent(value);
//            for (BleRespData bleRespData : dataList){
//                Log.e("writeContent","code:" + bleRespData.getControlCode());
//                if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
//                    parseReadResp(bleRespData);
//                }
////                else if(bleRespData.getType() == BleRespData.WRITE_TYPE){
////                    parseWriteResp(bleRespData);
////                }
//                else{
//                    Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType());
//                }
//            }
        }

        @Override
        public void onResponse(int code) {
            if (code == REQUEST_SUCCESS) {
                connectSucc = true;
                isCanSendMsg = true;
                waitingDlg.hide();
                if (!isDeviceReady){
                    showWaitingCancelDlg(getString(R.string.waitingDeviceReady));
                    sendCheckDeviceReadyCmd();
                }
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
                new SweetAlertDialog(EditActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getResources().getString(R.string.warning))
                        .setContentText(getResources().getString(R.string.connect_fail))
                        .show();
                waitingDlg.hide();
                reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                mClient.disconnect(mac);
            }
        }
    };


    private void addLog(final String log){
        EditActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.setText(tvLog.getText().toString() + log + "\r\n");
            }
        });
    }


    private void sendCheckDeviceReadyCmd() {
        byte[] deviceReady = deviceReadyHead;
        writeContent(deviceReady);
    }

    private void writeContent(final byte[] content){
        if(!connectSucc){
//            EditActivity.this.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(EditActivity.this,R.string.disconnect_please_connect_manually,Toast.LENGTH_SHORT).show();
//                }
//            });
            return;
        }
        if (content != null && content.length > 0){
            Log.e("writeContent",MyByteUtils.bytes2HexString(content,0));
            addLog("write:"+MyByteUtils.bytes2HexString(content,0));
            mClient.write(mac, BleDeviceData.unlockServiceId, BleDeviceData.unlockWriteUUID, content, new BleWriteResponse() {
                @Override
                public void onResponse(int code) {
                    if (code == REQUEST_SUCCESS) {

                    }else{
                        synchronized (lock){
                            isCanSendMsg = true;
                        }
                    }
                }
            });
        }
    }
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
                lockType == 0x54
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
        }else if (lockType == 0xff){
            showDetailMsg(getString(R.string.pwd_error));
        }
    }

    private void showDetailMsg(String msg){
        tvLockStatus.setText(msg);
    }

}