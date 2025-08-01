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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.topflytech.tftble.data.A001SoftwareUpgradeManager;
import com.topflytech.tftble.data.LogFileHelper;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.OpenAPI;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.data.WriteSensorObj;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

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
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog connectWaitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;

    EditText pwdEdit;
    boolean connectSucc = false;

    boolean onThisView = false;

    private BluetoothDevice bleDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic cmdReadWriteCharacteristic;
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
                        if(bluetoothGatt != null && connectSucc){
                            LogFileHelper.getInstance(SuperPwdResetActivity.this).writeIntoFile(MyUtils.bytes2HexString(writeSensorObj.getContent(),0));
                            Log.e("myLog",MyUtils.bytes2HexString(writeSensorObj.getContent(),0));
                            isWaitResponse = true;
                            cmdReadWriteCharacteristic.setValue(writeSensorObj.getContent());
                            if (ActivityCompat.checkSelfPermission(SuperPwdResetActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            }
                            bluetoothGatt.writeCharacteristic(cmdReadWriteCharacteristic);

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        connectSucc = false;
        if (waitingDlg != null) {
            waitingDlg.hide();
        }
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
     private void connectDeviceBle() {
        curTryConnectCount = 0;
        connectFailTimeoutShow(60000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }
         BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (bluetoothAdapter == null) {
             // 设备不支持蓝牙
             return;
         }
         bleDevice = bluetoothAdapter.getRemoteDevice(mac);
         if (bleDevice == null) {
             Toast.makeText(SuperPwdResetActivity.this, R.string.error_please_try_again, Toast.LENGTH_SHORT).show();
             return;
         }
        bleDevice.connectGatt(this, false, bluetoothGattCallback);
    }
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
                        if (ActivityCompat.checkSelfPermission(SuperPwdResetActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

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
                                Toast.makeText(SuperPwdResetActivity.this, R.string.disconnect_from_device, Toast.LENGTH_SHORT).show();
                            }
                        }
                        curTryConnectCount++;
                        connectSucc = false;
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
                }
            } else {
                // 处理服务发现失败
                if (ActivityCompat.checkSelfPermission(SuperPwdResetActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                gatt.disconnect();
                new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.ERROR_TYPE)
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
                        if (ActivityCompat.checkSelfPermission(SuperPwdResetActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        }
                        gatt.disconnect();
                        new SweetAlertDialog(SuperPwdResetActivity.this, SweetAlertDialog.ERROR_TYPE)
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


    };
    private void setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (ActivityCompat.checkSelfPermission(SuperPwdResetActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {


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
    private void parseNotifyData(byte[] value) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return;
        }
        if(bluetoothGatt != null ){
            if (cmdReadWriteCharacteristic != null) {
                // 启用通知
                setCharacteristicNotification(bluetoothGatt, cmdReadWriteCharacteristic, false);
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        connectSucc = false;
        onThisView = false;
    }




    private void fixTime(){
        Date now = new Date();
        byte[] value = MyUtils.unSignedInt2Bytes(now.getTime() / 1000);
        writeArrayData(MyUtils.controlFunc.get("time").get("write"),value,"topfly",uuid);

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
