package com.topflytech.lockActive.deviceConfigSetting;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topflytech.lockActive.Ble.TftBleConnectManager;
import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.EditActivity;
import com.topflytech.lockActive.MainActivity;
import com.topflytech.lockActive.R;
import com.topflytech.lockActive.WeChatQRCodeActivity;
import com.topflytech.lockActive.WeChatQRCodeActivity;
import com.topflytech.lockActive.data.BleRespData;
import com.topflytech.lockActive.data.CustomMenuItem;
import com.topflytech.lockActive.data.CustomPopMenu;
import com.topflytech.lockActive.data.CustomPopupMenuAdapter;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RFIDActivity extends AppCompatActivity {
    private ListView lvRfids;
    private ArrayList<String> rfidList;
    private HashSet<String> rfidSet;
    private ArrayAdapter<String> adapter;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private BleStatusCallback bleStatusCallback = new BleStatusCallback() {
        @Override
        public void onNotifyValue(byte[] value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parseResp(value);
                }
            });
        }


        @Override
        public void onBleStatusCallback(int connectStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC) {

                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CLOSE) {
                        Toast.makeText(RFIDActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_DISCONNECT) {
                        Toast.makeText(RFIDActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
                        finish();
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
    private void parseResp(byte[] value) {
        ArrayList<BleRespData> dataList = Utils.parseRespContent(value);
        for (BleRespData bleRespData : dataList){
            Log.e("writeContent","code:" + bleRespData.getControlCode());
            if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
                parseReadResp(bleRespData);
            }
            else{
                if(bleRespData.getErrorCode() == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                    Toast.makeText(RFIDActivity.this,R.string.ble_pwd_error,Toast.LENGTH_SHORT).show();
                    finish();
                }
                if(bleRespData.getControlCode() == func_id_of_read_rfid ||
                        bleRespData.getControlCode() == func_id_of_add_rfid ||
                        bleRespData.getControlCode() == func_id_of_delete_rfid){
                    Toast.makeText(RFIDActivity.this,R.string.error_retry,Toast.LENGTH_SHORT).show();
                    finish();
                }
                Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType() + "-" + bleRespData.getErrorCode());
            }
        }
    }

    private final int func_id_of_read_rfid = 1016;
    private final int func_id_of_add_rfid = 2014;
    private final int func_id_of_delete_rfid = 2015;
    private void parseReadResp(BleRespData bleRespData){

        int code = bleRespData.getControlCode();
        if(code == func_id_of_read_rfid){
            byte[] respData = getMultiRespData(bleRespData,code);
            if(respData == null || respData.length == 0){
                return;
            }
            ArrayList<String> curReadRfidList = new ArrayList<>();
            int allLen = respData[0];
            if(allLen < 0){
                allLen += 256;
            }
            int i = 1;
            if(i + allLen > respData.length){
                return;
            }
            while (i + 1 < respData.length){
                int len = respData[i];
                if(len < 0){
                    len += 256;
                }
                if(i + 1 + len <= respData.length){
                    byte[] id = Arrays.copyOfRange(respData,i+1,i+1+len);
                    String idStr = MyByteUtils.bytes2HexString(id,0);
                    curReadRfidList.add(idStr.toUpperCase());
                    i+= 1 + len;
                }else{
                    break;
                }
            }
            rfidSet.addAll(curReadRfidList);
            if(curReadRfidList.size() < 10){
                rfidList.addAll(rfidSet);
                waitingCancelDlg.dismiss();
                adapter.notifyDataSetChanged();
            }else{
                readRfid(rfidSet.size()+1);
            }
        }else if(code == func_id_of_add_rfid){
            rfidList.add(curDealRfid);
            adapter.notifyDataSetChanged();
            waitingCancelDlg.dismiss();
        }
        else if(code == func_id_of_delete_rfid){
            rfidList.remove(curDealRfid.toUpperCase());
            rfidList.remove(curDealRfid.toLowerCase());
            waitingCancelDlg.dismiss();
            adapter.notifyDataSetChanged();
        }
        Log.e("show","code:" + code);


    }
    SweetAlertDialog waitingCancelDlg;
    private void showWaitingCancelDlg(String warning){
        waitingCancelDlg = new SweetAlertDialog(RFIDActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
    private void readRfid(int index){
        byte[] content = new byte[3];
        byte[] indexByte = MyByteUtils.short2Bytes(index);
        content[0] = indexByte[0];
        content[1] = indexByte[1];
        content[2] = 0x0A;
        if(index == 1){
            rfidSet.clear();
        }
        byte[] cmd = Utils.getReadCmdContent(func_id_of_read_rfid,content);
        TftBleConnectManager.getInstance().writeArrayContent(new ArrayList<byte[]>(){{add(cmd);}});
    }

    private String curDealRfid;
    private void addRfid(String rfid){
        if(rfidList.contains(rfid.toUpperCase())){
            Toast.makeText(RFIDActivity.this,R.string.rfid_exists,Toast.LENGTH_SHORT).show();
            return;
        }
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        curDealRfid = rfid.toUpperCase();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] rfidByte = MyByteUtils.hexString2Bytes(curDealRfid);

        try {
            outputStream.write(rfidByte.length);
            outputStream.write(rfidByte);
            ArrayList< byte[]> cmd = Utils.getWriteCmdContent(func_id_of_add_rfid,outputStream.toByteArray(),blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void deleteRfid(String rfid){
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        curDealRfid = rfid;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] rfidByte = MyByteUtils.hexString2Bytes(curDealRfid);

        try {
            outputStream.write(rfidByte.length);
            outputStream.write(rfidByte);
            ArrayList< byte[]> cmd = Utils.getWriteCmdContent(func_id_of_delete_rfid,outputStream.toByteArray(),blePwd);
            TftBleConnectManager.getInstance().writeArrayContent(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<Integer, LinkedBlockingDeque<BleRespData>> multiRespDataMap = new HashMap<>();
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

    private void initActionBar(){
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText("RFID");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        rightButton.setImageResource(R.mipmap.ic_list);
        rightButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                showCustomPopupMenu(v);
            }
        });

    }
    private int scanType = 0;
    private final int SCAN_TYPE_OF_ADD = 0;
    private final int SCAN_TYPE_OF_DELETE = 1;
    private void showCustomPopupMenu(View anchorView) {
        // Create a list of menu items
        List<CustomMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new CustomMenuItem(getString(R.string.add),R.mipmap.ic_add));
        menuItems.add(new CustomMenuItem(getString(R.string.add),R.mipmap.ic_scan));
        menuItems.add(new CustomMenuItem(getString(R.string.delete),R.mipmap.ic_scan));
        CustomPopupMenuAdapter.OnMenuItemClickListener itemClickListener = new CustomPopupMenuAdapter.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(CustomMenuItem menuItem) {
                if(menuItem.getIconResId() == R.mipmap.ic_add){
                    showAddRfidDialog("");
                }else if(menuItem.getIconResId() == R.mipmap.ic_scan && menuItem.getTitle().equals(getString(R.string.add))){
                    scanType = SCAN_TYPE_OF_ADD;
                    Intent intent = new Intent(RFIDActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, MainActivity.REQUEST_SCAN_QRCODE);
                }else if(menuItem.getIconResId() == R.mipmap.ic_scan && menuItem.getTitle().equals(getString(R.string.delete))){
                    scanType = SCAN_TYPE_OF_DELETE;
                    Intent intent = new Intent(RFIDActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, MainActivity.REQUEST_SCAN_QRCODE);
                }
            }

        };
        CustomPopMenu customPopMenu = new CustomPopMenu(RFIDActivity.this,itemClickListener);
        customPopMenu.show(anchorView, menuItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MainActivity.REQUEST_SCAN_QRCODE && resultCode == MainActivity.RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            if(scanType == SCAN_TYPE_OF_ADD){
                showAddRfidDialog(value);
            }else  if(scanType == SCAN_TYPE_OF_DELETE){
                showDeleteRfidDlg(value);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TftBleConnectManager.getInstance().removeCallback("RFIDActivity");
    }
    String blePwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        TftBleConnectManager.getInstance().setCallback("RFIDActivity",bleStatusCallback);
        setContentView(R.layout.activity_rfid);
        blePwd = getIntent().getStringExtra("blePwd");
        lvRfids = findViewById(R.id.lv_rfids);
        rfidList = new ArrayList<>();
        rfidSet = new HashSet<>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_rfid, rfidList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_rfid, parent, false);
                }

                TextView tvRfid = convertView.findViewById(R.id.tv_rfid);
                Button btnDelete = convertView.findViewById(R.id.btn_delete);

                final String rfid = getItem(position);
                tvRfid.setText(rfid);
                btnDelete.setTag(rfid);
                btnDelete.setOnClickListener(new SingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        String curRfid = (String)v.getTag();
                        showDeleteRfidDlg(curRfid);
                    }
                });

                return convertView;
            }
        };

        lvRfids.setAdapter(adapter);
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        readRfid(1);
    }
    private void showDeleteRfidDlg(String curRfid) {
        if(!rfidList.contains(curRfid.toUpperCase()) && !rfidList.contains(curRfid.toLowerCase())){
            Toast.makeText(RFIDActivity.this,R.string.not_find_rfid,Toast.LENGTH_SHORT).show();
            return;
        }
        SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(RFIDActivity.this, SweetAlertDialog.NORMAL_TYPE);
        confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_to_delete_rfid) +" " + curRfid + "?");
        confirmRelayDlg.setCancelable(true);
        confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
        confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
        confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                deleteRfid(curRfid);
            }
        });
        confirmRelayDlg.show();
    }

    private void showAddRfidDialog(String defaultStr) {
        SweetAlertDialog addNewRfidDlg = new SweetAlertDialog(this, SweetAlertDialog.INPUT_TYPE);
        addNewRfidDlg.setTitleText(getResources().getString(R.string.enter_rfid));
        addNewRfidDlg.setCancelable(true);
        addNewRfidDlg.setCancelText(getResources().getString(R.string.cancel));
        addNewRfidDlg.setConfirmText(getResources().getString(R.string.confirm));
        if(defaultStr != null && defaultStr.trim().length() > 0){
            addNewRfidDlg.setInputText(defaultStr);
        }
        addNewRfidDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String rfid = sweetAlertDialog.getInputText();
                if (isValidRfid(rfid)) {
                    sweetAlertDialog.dismiss();
                    addRfid(rfid);
                } else {
                    Toast.makeText(RFIDActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        addNewRfidDlg.show();
    }
    private boolean isValidRfid(String rfid) {
        // Check if the RFID is either an 8-digit or a 10-digit hexadecimal string
        return Pattern.matches("[0-9A-Fa-f]{8}|[0-9A-Fa-f]{10}", rfid);
    }
}