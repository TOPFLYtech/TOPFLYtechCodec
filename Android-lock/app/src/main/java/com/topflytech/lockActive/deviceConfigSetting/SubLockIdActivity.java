package com.topflytech.lockActive.deviceConfigSetting;

import android.content.Intent;
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

import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.Ble.TftBleConnectManager;
import com.topflytech.lockActive.MainActivity;
import com.topflytech.lockActive.R;
import com.topflytech.lockActive.WeChatQRCodeActivity;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BleRespData;
import com.topflytech.lockActive.data.CustomMenuItem;
import com.topflytech.lockActive.data.CustomPopMenu;
import com.topflytech.lockActive.data.CustomPopupMenuAdapter;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SubLockIdActivity extends AppCompatActivity {
    private ListView lvSubLockIds;
    private ArrayList<String> subLockIdList;
    private HashSet<String> subLockIdSet;
    private ArrayAdapter<String> adapter;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private String imei;
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
                        Toast.makeText(SubLockIdActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
                        saveSubLockIdMap();
                        finish();
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_DISCONNECT) {
                        Toast.makeText(SubLockIdActivity.this,R.string.disconnect_from_device,Toast.LENGTH_SHORT).show();
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

    // 重写返回键监听
    @Override
    public void onBackPressed() {
        saveSubLockIdMap();
        super.onBackPressed();
    }
    private void parseResp(byte[] value) {
        ArrayList<BleRespData> dataList = Utils.parseRespContent(value);
        for (BleRespData bleRespData : dataList){
            Log.e("writeContent","code:" + bleRespData.getControlCode());
            if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
                parseReadResp(bleRespData);
            }
            else{
                if(bleRespData.getErrorCode() == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                    Toast.makeText(SubLockIdActivity.this,R.string.ble_pwd_error,Toast.LENGTH_SHORT).show();
                    saveSubLockIdMap();
                    finish();
                }
                if(bleRespData.getControlCode() == func_id_of_read_sub_lock_id ||
                        bleRespData.getControlCode() == func_id_of_add_sub_lock_id ||
                        bleRespData.getControlCode() == func_id_of_delete_sub_lock_id){
                    Toast.makeText(SubLockIdActivity.this,R.string.error_retry,Toast.LENGTH_SHORT).show();
                    saveSubLockIdMap();
                    finish();
                }
                Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType() + "-" + bleRespData.getErrorCode());
            }
        }
    }

    private final int func_id_of_read_sub_lock_id = 1018;
    private final int func_id_of_add_sub_lock_id = 2017;
    private final int func_id_of_delete_sub_lock_id = 2018;
    private void parseReadResp(BleRespData bleRespData){
        int code = bleRespData.getControlCode();
        if(code == func_id_of_read_sub_lock_id){
            byte[] respData = getMultiRespData(bleRespData,code);
            if(respData == null || respData.length == 0){
                return;
            }
            ArrayList<String> curReadSubLockIdList = new ArrayList<>();
            int i = 0;
            while (i < respData.length){
                int len = 6;
                if(i + len <= respData.length){
                    byte[] id = Arrays.copyOfRange(respData,i,i+len);
                    String idStr = MyByteUtils.bytes2HexString(id,0);
                    curReadSubLockIdList.add(idStr.toUpperCase());
                    i+= len;
                }else{
                    break;
                }
            }
            subLockIdSet.addAll(curReadSubLockIdList);
            if(curReadSubLockIdList.size() < 10){
                subLockIdList.addAll(subLockIdSet);
                subLockIdSet.clear();
                waitingCancelDlg.dismiss();
                adapter.notifyDataSetChanged();
            }else{
                readSubLockId(subLockIdSet.size() + 1);
            }
        }else if(code == func_id_of_add_sub_lock_id){
            subLockIdList.add(curDealSubLockId.toUpperCase());
            adapter.notifyDataSetChanged();
            waitingCancelDlg.dismiss();
        }
        else if(code == func_id_of_delete_sub_lock_id){
            subLockIdList.remove(curDealSubLockId.toUpperCase());
            subLockIdList.remove(curDealSubLockId.toLowerCase());
            waitingCancelDlg.dismiss();
            adapter.notifyDataSetChanged();
        }
        Log.e("show","code:" + code);


    }
    SweetAlertDialog waitingCancelDlg;
    private void showWaitingCancelDlg(String warning){
        waitingCancelDlg = new SweetAlertDialog(SubLockIdActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
    private void readSubLockId(int index){
        byte[] content = new byte[2];
        content[0] = (byte)index;
        content[1] = 0x0A;
        if(index == 1){
            subLockIdSet.clear();
        }
        byte[] cmd = Utils.getReadCmdContent(func_id_of_read_sub_lock_id,content);
        TftBleConnectManager.getInstance().writeArrayContent(new ArrayList<byte[]>(){{add(cmd);}});
    }

    private String curDealSubLockId;
    private void addSubLockId(String subLockId){
        if(subLockIdList.contains(subLockId.toUpperCase())){
            Toast.makeText(SubLockIdActivity.this,R.string.sub_lock_exists,Toast.LENGTH_SHORT).show();
            return;
        }
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        curDealSubLockId = subLockId.toUpperCase();
        ArrayList< byte[]> cmd = Utils.getWriteCmdContent(func_id_of_add_sub_lock_id,MyByteUtils.hexString2Bytes(curDealSubLockId),blePwd);
        TftBleConnectManager.getInstance().writeArrayContent(cmd);
    }
    private void deleteSubLockId(String subLockId){
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        curDealSubLockId = subLockId;
        ArrayList< byte[]> cmd = Utils.getWriteCmdContent(func_id_of_delete_sub_lock_id,MyByteUtils.hexString2Bytes(curDealSubLockId),blePwd);
        TftBleConnectManager.getInstance().writeArrayContent(cmd);
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
        tvHead.setText(R.string.sub_lock_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                saveSubLockIdMap();
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

    private void saveSubLockIdMap() {
        if(imei != null && !imei.isEmpty()){
            BleDeviceData.saveSubLockBindMap(SubLockIdActivity.this,imei,subLockIdList);
        }
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
                    showAddSubLockIdDialog("");
                }else if(menuItem.getIconResId() == R.mipmap.ic_scan && menuItem.getTitle().equals(getString(R.string.add))){
                    scanType = SCAN_TYPE_OF_ADD;
                    Intent intent = new Intent(SubLockIdActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, MainActivity.REQUEST_SCAN_QRCODE);
                }else if(menuItem.getIconResId() == R.mipmap.ic_scan && menuItem.getTitle().equals(getString(R.string.delete))){
                    scanType = SCAN_TYPE_OF_DELETE;
                    Intent intent = new Intent(SubLockIdActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, MainActivity.REQUEST_SCAN_QRCODE);
                }
            }

        };
        CustomPopMenu customPopMenu = new CustomPopMenu(SubLockIdActivity.this,itemClickListener);
        customPopMenu.show(anchorView, menuItems);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MainActivity.REQUEST_SCAN_QRCODE && resultCode == MainActivity.RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            if(scanType == SCAN_TYPE_OF_ADD){
                showAddSubLockIdDialog(value);
            }else  if(scanType == SCAN_TYPE_OF_DELETE){
                showDeleteSubLockIdDlg(value);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TftBleConnectManager.getInstance().removeCallback("SubLockIdActivity");
    }
    String blePwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        TftBleConnectManager.getInstance().setCallback("SubLockIdActivity",bleStatusCallback);
        setContentView(R.layout.activity_sub_lock_id);
        blePwd = getIntent().getStringExtra("blePwd");
        imei = getIntent().getStringExtra("imei");
        lvSubLockIds = findViewById(R.id.lv_sub_lock_ids);
        subLockIdList = new ArrayList<>();
        subLockIdSet = new HashSet<>();
        adapter = new ArrayAdapter<String>(this, R.layout.item_rfid, subLockIdList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_rfid, parent, false);
                }

                TextView tvSubLockId = convertView.findViewById(R.id.tv_rfid);
                Button btnDelete = convertView.findViewById(R.id.btn_delete);

                final String subLockId = getItem(position);
                tvSubLockId.setText(subLockId);
                btnDelete.setTag(subLockId);
                btnDelete.setOnClickListener(new SingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        String curSubLockId = (String)v.getTag();
                        showDeleteSubLockIdDlg(curSubLockId);
                    }
                });

                return convertView;
            }
        };

        lvSubLockIds.setAdapter(adapter);
        showWaitingCancelDlg(getResources().getString(R.string.waiting) );
        readSubLockId(1);
    }
    private void showDeleteSubLockIdDlg(String cursubLockId) {
        if(!subLockIdList.contains(cursubLockId.toUpperCase()) && !subLockIdList.contains(cursubLockId.toLowerCase())){
            Toast.makeText(SubLockIdActivity.this,R.string.not_find_sub_lock_id,Toast.LENGTH_SHORT).show();
            return;
        }
        SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(SubLockIdActivity.this, SweetAlertDialog.NORMAL_TYPE);
        confirmRelayDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        confirmRelayDlg.setTitleText(getResources().getString(R.string.confirm_to_delete_sub_lock_id) +" " + cursubLockId + "?");
        confirmRelayDlg.setCancelable(true);
        confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
        confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
        confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                deleteSubLockId(cursubLockId);
            }
        });
        confirmRelayDlg.show();
    }

    private void showAddSubLockIdDialog(String defaultStr) {
        SweetAlertDialog addNewSubLockIdDlg = new SweetAlertDialog(this, SweetAlertDialog.INPUT_TYPE);
        addNewSubLockIdDlg.setTitleText(getResources().getString(R.string.enter_sub_lock_id));
        addNewSubLockIdDlg.setCancelable(true);
        addNewSubLockIdDlg.setCancelText(getResources().getString(R.string.cancel));
        addNewSubLockIdDlg.setConfirmText(getResources().getString(R.string.confirm));
        if(defaultStr != null && defaultStr.trim().length() > 0){
            addNewSubLockIdDlg.setInputText(defaultStr);
        }
        addNewSubLockIdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String subLockId = sweetAlertDialog.getInputText();
                if (isValidSubLockId(subLockId)) {
                    sweetAlertDialog.dismiss();
                    addSubLockId(subLockId);
                } else {
                    Toast.makeText(SubLockIdActivity.this, R.string.input_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        addNewSubLockIdDlg.show();
    }
    private boolean isValidSubLockId(String subLockId) {
        // Check if the ID is a 10-digit hexadecimal string
        return Pattern.matches("[0-9A-Fa-f]{12}", subLockId);
    }
}