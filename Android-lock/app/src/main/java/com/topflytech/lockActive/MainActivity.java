package com.topflytech.lockActive;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.UniqueIDTool;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView refreshButton;
    private ImageView fuzzySearchBtn;
    private ImageView mainMenuBtn;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    SweetAlertDialog waitingCancelDlg;
    private ListView mListView;
    private ItemAdapter mListAdapter;
    private LinearLayout llFuzzySearch;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private boolean isFuzzySearchStatus = false,isOnView = false;
    private String fuzzyKey = "";
    public static final int REQUEST_SCAN_QRCODE= 1;
    public static final int RESPONSE_SCAN_QRCODE = 1;
    private Date dataNotifyDate;
    private Date enterDate;
    private EditText searchEditText;
    private ImageButton scanBtn;
    private Date lastRecvDate;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static ArrayList<BleDeviceData> allBleDeviceDataList = new ArrayList<BleDeviceData>();
    public static ArrayList<BleDeviceData> showBleDeviceDataList = new ArrayList<BleDeviceData>();
    private ConcurrentHashMap<String, Integer> allMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private ConcurrentHashMap<String, Integer> showMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private String uniqueID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext());
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.main_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        enterDate = new Date();

        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothadapter = bluetoothManager.getAdapter();
        if (bluetoothadapter == null) {
            SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
            confirmRelayDlg.setTitleText(getResources().getString(R.string.ble_not_support));
            confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
            confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                }
            });
            confirmRelayDlg.show();
        }else{
            if(bluetoothadapter.isEnabled()){

            }else{
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivity(enabler);
            }
            refreshButton = (ImageView) findViewById(R.id.main_left_refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBluetoothAdapter.stopLeScan(startSearchCallback);
                    allBleDeviceDataList.clear();
                    allMacIndex.clear();
                    showBleDeviceDataList.clear();
                    showMacIndex.clear();
                    mListAdapter.notifyDataSetChanged();
                    if (bluetoothadapter.isEnabled()){
                        mBluetoothAdapter.startLeScan(startSearchCallback);
                        showWaitingCancelDlg("");
                    }else{
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        MainActivity.this.startActivity(enabler);
                    }

                }
            });
            searchEditText = (EditText)findViewById(R.id.et_fuzzy_search);
            llFuzzySearch = (LinearLayout)findViewById(R.id.ll_fuzzy_search);
            scanBtn = (ImageButton)findViewById(R.id.btn_scan);
            scanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,ScanActivity.class);
                    startActivityForResult(intent,REQUEST_SCAN_QRCODE);
                }
            });
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    fuzzyKey = editable.toString().trim();
                    refreshShowItem();
                    if(fuzzyKey.length() == 15){
                        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
                            if (bleDeviceData.getImei().equals(fuzzyKey)){
                                Intent intent = new Intent( MainActivity.this, EditActivity.class);
                                intent.putExtra("mac",bleDeviceData.getMac());
                                intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                                intent.putExtra("id",bleDeviceData.getId());
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                }
            });
            fuzzySearchBtn = (ImageView) findViewById(R.id.main_right_fuzzy_search);
            fuzzySearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!isFuzzySearchStatus){
                        llFuzzySearch.setVisibility(View.VISIBLE);
                    }else{
                        fuzzyKey = "";
                        refreshShowItem();
                        llFuzzySearch.setVisibility(View.GONE);
                    }
                    isFuzzySearchStatus = !isFuzzySearchStatus;

                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
                //判断是否具有权限
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_CODE_ACCESS_COARSE_LOCATION);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_ACCESS_FINE_LOCATION);
                }
            }
            mListView = (ListView) findViewById(R.id.listView);
            mListView.setDividerHeight(0);
            mListAdapter = new ItemAdapter();
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(MainActivity.this.getWindow().getDecorView().getWindowToken(), 0);
                }
            });

            service.scheduleWithFixedDelay(checkBleSearchStatus, 1, 1, TimeUnit.SECONDS);
        }
        uniqueID = UniqueIDTool.getUniqueID();
        Log.e("BluetoothUtils","my UUID:" + uniqueID);
    }

    private void showWaitingCancelDlg(String warning){
        waitingCancelDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingCancelDlg.showCancelButton(true);
        waitingCancelDlg.setTitleText("");
        waitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
        waitingCancelDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.hide();
            }
        });
        if (warning != null && !warning.isEmpty()){
            waitingCancelDlg.setTitleText(warning);
        }
        waitingCancelDlg.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SCAN_QRCODE && resultCode == RESPONSE_SCAN_QRCODE){
            String value = data.getStringExtra("value");
            searchEditText.setText(value);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION || requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION){
            reStartApp();
        }
    }


    public void reStartApp()
    {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(MainActivity.this.getBaseContext().getPackageName());
        intent.putExtra("REBOOT","reboot");
        PendingIntent restartIntent = PendingIntent.getActivity(MainActivity.this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }




    private void refreshShowItem(){
        showBleDeviceDataList.clear();
        showMacIndex.clear();
        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
//            if(fuzzyKey == null || fuzzyKey.isEmpty() || bleDeviceData.getId().contains(fuzzyKey.toUpperCase())
//                    || (bleDeviceData.getDeviceName()!=null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
//                showBleDeviceDataList.add(bleDeviceData);
//                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
//            }
            if (fuzzyKey == null || fuzzyKey.isEmpty()){
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            }else{
                if((bleDeviceData.getImei() != null && bleDeviceData.getImei().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName()!= null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
                    showBleDeviceDataList.add(bleDeviceData);
                    showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
                }
            }
        }
        mListAdapter.notifyDataSetChanged();
    }

    private Runnable checkBleSearchStatus = new Runnable() {
        @Override
        public void run() {
            if(!isOnView){
                return;
            }
            Date checkDate = lastRecvDate;
            if(lastRecvDate == null){
                checkDate = enterDate;
            }

//            Log.e("BluetoothUtils","checkBleSearchStatus");
            Date now = new Date();
            if(now.getTime() - checkDate.getTime() > 45000){
                //need restart ble search
                Log.e("BluetoothUtils","restart search");
                mBluetoothAdapter.stopLeScan(startSearchCallback);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkDate = now;
                mBluetoothAdapter.startLeScan(startSearchCallback);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (waitingCancelDlg != null){
            waitingCancelDlg.hide();
            waitingCancelDlg.dismiss();
            waitingCancelDlg = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mBluetoothAdapter != null){
            mBluetoothAdapter.startLeScan(startSearchCallback);
        }
        isOnView = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnView = false;
    }
    private void updateShowItem(BleDeviceData bleDeviceData){
        if (bleDeviceData == null){
            return;
        }
        if(showMacIndex.get(bleDeviceData.getMac()) != null && showBleDeviceDataList.size() > 0){
            showBleDeviceDataList.set(showMacIndex.get(bleDeviceData.getMac()),bleDeviceData);
        }else{
            if (fuzzyKey == null || fuzzyKey.isEmpty()){
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            }else{
                if((bleDeviceData.getImei() != null && bleDeviceData.getImei().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName()!= null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
                    showBleDeviceDataList.add(bleDeviceData);
                    showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
                }
            }
        }
    }
    private void updateAllShowItem(){
        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
            updateShowItem(bleDeviceData);
        }
    }

    private final BluetoothAdapter.LeScanCallback startSearchCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String id = device.getAddress().replaceAll(":","").toUpperCase();
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress());
            lastRecvDate = new Date();
            HashMap<Byte,byte[]> rawDataList = BleDeviceData.parseRawData(scanRecord);
            if (rawDataList.containsKey((byte)0xfe) || rawDataList.containsKey((byte)0xfd)){
                byte[] versionInfo = rawDataList.get((byte)0xfe);
                byte[] imeiInfo = rawDataList.get((byte)0xfd);
                String imei = null;
                if (imeiInfo != null){
                    byte[] imeiByte = Arrays.copyOfRange(imeiInfo,3,imeiInfo.length);
                    imei = new String(imeiByte);
                }
                String software = null;
                String hardware = null;
                String model = null;
                String voltageStr = null;
                byte protocolByte = 0x00;
                if (versionInfo != null && versionInfo.length >= 7){
                    protocolByte = versionInfo[2];
                    String versionStr = MyByteUtils.bytes2HexString(versionInfo,3);
                    hardware = String.format("V%s.%s",versionStr.substring(0,1),versionStr.substring(1,2));
                    software =  String.format("V%d",Integer.valueOf(versionStr.substring(2,6)));;
                    voltageStr = String.format("%s.%s",versionStr.substring(6,7),versionStr.substring(7,8));
                    model = BleDeviceData.parseModel(protocolByte);
                }
                if (protocolByte != (byte)0x62) {
                    return;
                }
                Integer index = allMacIndex.get(device.getAddress());
                if(index != null && allBleDeviceDataList.size() > index){
                    BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
                    bleDeviceData.setRssi(rssi + "dBm");
                    bleDeviceData.setSrcData(scanRecord);
                    bleDeviceData.setDate(new Date());
                    bleDeviceData.setId(id);
                    if (hardware != null){
                        bleDeviceData.setHardware(hardware);
                    }
                    if (software != null){
                        bleDeviceData.setSoftware(software);
                    }
                    if (model != null){
                        bleDeviceData.setModel(model);
                    }
                    if (imei != null){
                        bleDeviceData.setImei(imei);
                    }
                    if (voltageStr != null){
                        bleDeviceData.setVoltage(Float.valueOf(voltageStr));
                    }
                    bleDeviceData.setDeviceName(device.getName());

//                        updateShowItem(bleDeviceData);
                }else{
                    BleDeviceData bleDeviceData = new BleDeviceData();
                    bleDeviceData.setRssi(rssi + "dBm");
                    bleDeviceData.setSrcData(scanRecord);
                    bleDeviceData.setDate(new Date());
                    bleDeviceData.setMac(device.getAddress());
                    bleDeviceData.setId(id);
                    if (imei != null){
                        bleDeviceData.setImei(imei);
                    }
                    if (hardware != null){
                        bleDeviceData.setHardware(hardware);
                    }
                    if (software != null){
                        bleDeviceData.setSoftware(software);
                    }
                    if (model != null){
                        bleDeviceData.setModel(model);
                    }
                    if (voltageStr != null){
                        bleDeviceData.setVoltage(Float.valueOf(voltageStr));
                    }
                    bleDeviceData.setDeviceName(device.getName());
                    allBleDeviceDataList.add(bleDeviceData);
                    index = allBleDeviceDataList.size() - 1;
                    allMacIndex.put(device.getAddress(),index);
                }
                if(dataNotifyDate == null || lastRecvDate.getTime() - dataNotifyDate.getTime() > 1000){
                    updateAllShowItem();
                    dataNotifyDate = lastRecvDate;
                    mListAdapter.notifyDataSetChanged();
                }
                if(waitingCancelDlg != null && waitingCancelDlg.isShowing()){
                    waitingCancelDlg.hide();
                }
            }
        }

    };
    View.OnClickListener configDeviceClick = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            mBluetoothAdapter.stopLeScan(startSearchCallback);
            final String mac = (String)view.getTag();
            Log.e("Click", "config device " + mac);
            Integer index = allMacIndex.get(mac);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                Intent intent = new Intent( MainActivity.this, EditActivity.class);
                intent.putExtra("mac",mac);
                intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                intent.putExtra("id",bleDeviceData.getId());
                startActivity(intent);
            }

        }
    };

    class ItemAdapter extends BaseAdapter {

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getCount() {
            return showBleDeviceDataList.size();
        }

        @Override
        public Object getItem(int i) {
            return showBleDeviceDataList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BleDeviceData bleDeviceData = (BleDeviceData)getItem(position);
            if(null == convertView ){
                convertView = View.inflate(getApplicationContext(),R.layout.ble_detail_item,null);
                DeviceViewHolder holder = new DeviceViewHolder();
//                holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                holder.imeiTextView = (TextView)convertView.findViewById(R.id.tx_imei);
                holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_model);
                holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                convertView.setTag(holder);
            }

            DeviceViewHolder holder = (DeviceViewHolder) convertView.getTag();
            holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
//            holder.idTextView.setText(bleDeviceData.getId());
            holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
            holder.rssiTextView.setText(String.valueOf(bleDeviceData.getRssi()));
            holder.imeiTextView.setText(bleDeviceData.getImei());
            holder.softwareTextView.setText(bleDeviceData.getSoftware());
            holder.hardwareTextView.setText(bleDeviceData.getHardware());
            holder.modelTextView.setText(bleDeviceData.getModel());
            holder.configBtn.setTag(bleDeviceData.getMac());
            holder.configBtn.setOnClickListener(configDeviceClick);
            holder.configBtn.setText(getString(R.string.unlock));
            return convertView;

        }
        class DeviceViewHolder {
            TextView deviceNameTextView,  idTextView, dateTextView, rssiTextView , imeiTextView , softwareTextView , hardwareTextView , modelTextView ;
            Button configBtn;
        }
    }
}