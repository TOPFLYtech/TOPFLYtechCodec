package com.topflytech.tftble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.fonts.Font;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.view.MarqueeTextView;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class MainActivity extends AppCompatActivity {
//    BluetoothClient mClient;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static ArrayList<BleDeviceData> allBleDeviceDataList = new ArrayList<BleDeviceData>();
    public static ArrayList<BleDeviceData> showBleDeviceDataList = new ArrayList<BleDeviceData>();
    private ConcurrentHashMap<String, Integer> allMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private ConcurrentHashMap<String, Integer> showMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private String serviceId = "27760001-999C-4D6A-9FC4-C7272BE10900";
    private String uuid = "27763561-999C-4D6A-9FC4-C7272BE10900";
    private ActionBar actionBar;
    private ImageView refreshButton;
    private ImageView fuzzySearchBtn;
    private EditText searchEditText;
    private ImageButton scanBtn;
    private Date lastRecvDate;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    SweetAlertDialog waitingCancelDlg;
    private ListView mListView;
    private ItemAdapter mListAdapter;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private boolean isFuzzySearchStatus = false,isOnView = false;
    private String fuzzyKey = "";
    private ArrayList<ArrayList<byte[]>> orignHistoryList = new ArrayList<ArrayList<byte[]>>();
    private long startDate,endDate,propOpenCount,propCloseCount;
    private int startBattery,endBattery;
    private String beginTemp,endTemp;
    private float beginHumidity,endHumidity,averageTemp,averageHumidity,maxTemp,minTemp,maxHumidity,minHumidity,
            overMaxTempLimitCount,overMaxTempLimitTime,overMinTempLimitCount,overMinTempLimitTime,
            overMaxHumidityLimitCount,overMaxHumidityLimitTime,overMinHumidityLimitCount,overMinHumidityLimitTime;
    private LinearLayout llFuzzySearch;
    public static final int REQUEST_SCAN_QRCODE= 1;
    public static final int RESPONSE_SCAN_QRCODE = 1;
    private Date dataNotifyDate;
    private Date enterDate;
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
                }
            });
            fuzzySearchBtn = (ImageView) findViewById(R.id.main_right_fuzzy_search);
            fuzzySearchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                orignHistoryList.clear();
//                for(String jsonStr : MyUtils.srcHisData){
//                    JSONArray srcItemArray = (JSONArray)JSONArray.parse(jsonStr);
//                    ArrayList<byte[]> curHistoryList = new ArrayList<>();
//                    for(int i = 0;i < srcItemArray.size();i++){
//                        String onRecordItem = srcItemArray.getString(i);
//                        curHistoryList.add(MyUtils.hexString2Bytes(onRecordItem));
//                    }
//                    orignHistoryList.add(curHistoryList);
//                }
//                ArrayList<byte[]> mergeData = MyUtils.mergeOriginHisData(orignHistoryList);
//                ArrayList<BleHisData> showBleHisData = new ArrayList<>();
//                for(byte[] byteDataArray : mergeData){
//                    boolean dataCorrect = MyUtils.checkOriginHisDataCrc(byteDataArray);
//                    if(dataCorrect){
//                        ArrayList<BleHisData> bleHisDataList = MyUtils.parseS02BleHisData(byteDataArray);
//                        showBleHisData.addAll(bleHisDataList);
//                        System.out.print("succ");
//                    }else{
//                        Toast.makeText(MainActivity.this,"Data not correct",Toast.LENGTH_LONG).show();
//                    }
//                }


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
//        mClient = new BluetoothClient(MainActivity.this);
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

            service.scheduleWithFixedDelay(checkBleSearchStatus, 1, 1, TimeUnit.SECONDS);
        }


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


    public void reStartApp()
   {
       Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(MainActivity.this.getBaseContext().getPackageName());
       intent.putExtra("REBOOT","reboot");
       PendingIntent restartIntent = PendingIntent.getActivity(MainActivity.this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
       AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
       mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
       android.os.Process.killProcess(android.os.Process.myPid());
   }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION || requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION){
            reStartApp();
        } 
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
            BleDeviceData oldBleDeviceData = showBleDeviceDataList.get(showMacIndex.get(bleDeviceData.getMac()));
            if(oldBleDeviceData != null){
                if (oldBleDeviceData.getDate().getTime() < bleDeviceData.getDate().getTime()){
                    showBleDeviceDataList.set(showMacIndex.get(bleDeviceData.getMac()),bleDeviceData);
                }
            }else{
                showBleDeviceDataList.set(showMacIndex.get(bleDeviceData.getMac()),bleDeviceData);
            }
        }else{
            if (fuzzyKey == null || fuzzyKey.isEmpty()){
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            }else{
                if((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName()!= null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
                    showBleDeviceDataList.add(bleDeviceData);
                    showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
                }
            }
//            if(fuzzyKey == null || fuzzyKey.isEmpty() ||bleDeviceData.getId() == null || bleDeviceData.getId().contains(fuzzyKey.toUpperCase())
//                    || bleDeviceData.getDeviceName()== null || bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase())){
//                showBleDeviceDataList.add(bleDeviceData);
//                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
//            }
        }
    }

    private void updateAllShowItem(){
        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
            updateShowItem(bleDeviceData);
        }
    }

    private void refreshShowItem(){
        showBleDeviceDataList.clear();
        showMacIndex.clear();
        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
            if (fuzzyKey == null || fuzzyKey.isEmpty()){
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            }else{
                if((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
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

    private final BluetoothAdapter.LeScanCallback startSearchCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress());
//            if(MyUtils.isDebug){
//                if(rssi < -55 || rssi > 0){
//                    return;
//                }
//
//            }
            String data = MyUtils.bytes2HexString(scanRecord, 0);
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
            lastRecvDate = new Date();
            if (data != null){
                String head = data.substring(2,8);
                String tireHead = data.substring(16,20).toLowerCase();
                if(head.toLowerCase().equals("16ffbf") || tireHead.equals("ffac")){
                    String id = device.getAddress().replaceAll(":","").toUpperCase();
//                    if(!(id.equals("D104A3CC6E55") || id.contains("6676")|| id.contains("E625"))){
//                        return;
//                    }
                    Integer index = allMacIndex.get(device.getAddress());
                    if(index != null && allBleDeviceDataList.size() > index){
                        BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
                        if(tireHead.equals("ffac")){
                            bleDeviceData.setDeviceType("tire");
                            bleDeviceData.setId(device.getAddress().replaceAll(":",""));
                        }
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setHexData(data);
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setDeviceName(device.getName());
                        try {
                            bleDeviceData.parseData(MainActivity.this);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
//                        updateShowItem(bleDeviceData);
                    }else{
                        BleDeviceData bleDeviceData = new BleDeviceData();
                        if(tireHead.equals("ffac")){
                            bleDeviceData.setDeviceType("tire");
                        }
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setHexData(data);
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setMac(device.getAddress());
                        bleDeviceData.setDeviceName(device.getName());
                        try {
                            bleDeviceData.parseData(MainActivity.this);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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
//                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + " data:" + data);
                }
            }
        }

    };
    View.OnClickListener switchPressureUnitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e("Click", "switch pressure");
            BleDeviceData.switchCurPressureUnit(MainActivity.this);
            mListAdapter.notifyDataSetChanged();
        }
    };
    View.OnClickListener switchTempUnitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.e("Click", "switch temp");
            BleDeviceData.switchCurTempUnit(MainActivity.this);
            mListAdapter.notifyDataSetChanged();
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
                intent.putExtra("deviceType",bleDeviceData.getDeviceType());
                intent.putExtra("id",bleDeviceData.getId());
                intent.putExtra("software",bleDeviceData.getSoftware());
                startActivity(intent);
            }

        }
    };
    View.OnClickListener showQrCodeClick = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            final String id = (String)view.getTag();
            SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
            Bitmap  bitmap =    QRCodeEncoder.syncEncodeQRCode(id,200);
            pDialog.setCustomImageBitmap(bitmap) ;
            pDialog.setTitleText("");
            pDialog.setCancelable(false);
            pDialog.show();

        }
    };
    class ItemAdapter extends BaseAdapter{

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return 4;
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
            if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S02")){
                if(null == convertView || convertView.getTag() instanceof S02ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s02_detail_item,null);
                    S02ViewHolder holder = new S02ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.tempTextView = (TextView)convertView.findViewById(R.id.tx_temp);
                    holder.humidityTextView = (TextView)convertView.findViewById(R.id.tx_humidity);
                    holder.devicePropTextView = (TextView)convertView.findViewById(R.id.tx_device_prop);
                    holder.alarmTextView = (MarqueeTextView) convertView.findViewById(R.id.tx_alarm);
                    holder.switchTempBtn = (Button)convertView.findViewById(R.id.btn_temp_switch);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    holder.qrcodeLL = (LinearLayout) convertView.findViewById(R.id.ll_show_qrcode);
                    holder.qrCodeBtn = (Button)convertView.findViewById(R.id.btn_show_qrcode);
                    convertView.setTag(holder);
                }

                S02ViewHolder holder = (S02ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.humidityTextView.setText(bleDeviceData.getHumidity() + "%");
                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
                holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                if(MyUtils.isDebug){
                    holder.qrcodeLL.setVisibility(View.VISIBLE);
                    holder.qrCodeBtn.setTag(bleDeviceData.getId());
                    holder.qrCodeBtn.setOnClickListener(showQrCodeClick);
                }
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S04")){
                if(null == convertView  || convertView.getTag() instanceof S04ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s04_detail_item,null);
                    S04ViewHolder holder = new S04ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.tempTextView = (TextView)convertView.findViewById(R.id.tx_temp);
                    holder.devicePropTextView = (TextView)convertView.findViewById(R.id.tx_device_prop);
                    holder.alarmTextView = (MarqueeTextView) convertView.findViewById(R.id.tx_alarm);
                    holder.switchTempBtn = (Button)convertView.findViewById(R.id.btn_temp_switch);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    holder.qrcodeLL = (LinearLayout) convertView.findViewById(R.id.ll_show_qrcode);
                    holder.qrCodeBtn = (Button)convertView.findViewById(R.id.btn_show_qrcode);
                    convertView.setTag(holder);
                }

                S04ViewHolder holder = (S04ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
                holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                if(MyUtils.isDebug){
                    holder.qrcodeLL.setVisibility(View.VISIBLE);
                    holder.qrCodeBtn.setTag(bleDeviceData.getId());
                    holder.qrCodeBtn.setOnClickListener(showQrCodeClick);
                }
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S05")){
                if(null == convertView || convertView.getTag() instanceof S05ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s05_detail_item,null);
                    S05ViewHolder holder = new S05ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.tempTextView = (TextView)convertView.findViewById(R.id.tx_temp);
                    holder.devicePropTextView = (TextView)convertView.findViewById(R.id.tx_device_prop);
                    holder.alarmTextView = (MarqueeTextView) convertView.findViewById(R.id.tx_alarm);
                    holder.switchTempBtn = (Button)convertView.findViewById(R.id.btn_temp_switch);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    holder.qrcodeLL = (LinearLayout) convertView.findViewById(R.id.ll_show_qrcode);
                    holder.qrCodeBtn = (Button)convertView.findViewById(R.id.btn_show_qrcode);
                    convertView.setTag(holder);
                }

                S05ViewHolder holder = (S05ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
                holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                if(MyUtils.isDebug){
                    holder.qrcodeLL.setVisibility(View.VISIBLE);
                    holder.qrCodeBtn.setTag(bleDeviceData.getId());
                    holder.qrCodeBtn.setOnClickListener(showQrCodeClick);
                }
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("tire")){
                if(null == convertView || convertView.getTag() instanceof TireViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_tire_detail_item,null);
                    TireViewHolder holder = new TireViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.tempTextView = (TextView)convertView.findViewById(R.id.tx_temp);
                    holder.tireTextView = (TextView)convertView.findViewById(R.id.tx_device_tire);
                    holder.statusTextView = (MarqueeTextView) convertView.findViewById(R.id.tx_status);
                    holder.switchTempBtn = (Button)convertView.findViewById(R.id.btn_temp_switch);
                    holder.switchPressureBtn = (Button)convertView.findViewById(R.id.btn_pressure_switch);
                    convertView.setTag(holder);
                }

                TireViewHolder holder = (TireViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.switchPressureBtn.setText(BleDeviceData.getNextPressureUnit(MainActivity.this));
                holder.tireTextView.setText(BleDeviceData.getCurPressure(MainActivity.this,bleDeviceData.getTirePressure()) + BleDeviceData.getCurPressureUnit(MainActivity.this));
                holder.statusTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getStatus()));
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.switchPressureBtn.setOnClickListener(switchPressureUnitClick);
                return convertView;
            }else{
                if(null == convertView || convertView.getTag() instanceof ErrorDeviceViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_invalid_detail_item,null);
                    ErrorDeviceViewHolder holder = new ErrorDeviceViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    convertView.setTag(holder);
                }

                ErrorDeviceViewHolder holder = (ErrorDeviceViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(String.valueOf(bleDeviceData.getRssi()));
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                return convertView;
            }

        }
        class S02ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,humidityTextView,devicePropTextView;
            Button switchTempBtn,configBtn,qrCodeBtn;
            LinearLayout qrcodeLL;
            MarqueeTextView alarmTextView;
        }
        class S04ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,devicePropTextView;
            Button switchTempBtn,configBtn,qrCodeBtn;
            LinearLayout qrcodeLL;
            MarqueeTextView alarmTextView;
        }
        class S05ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
            batteryTextView,tempTextView,devicePropTextView;
            Button switchTempBtn,configBtn,qrCodeBtn;
            LinearLayout qrcodeLL;
            MarqueeTextView alarmTextView;
        }
        class ErrorDeviceViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView;
        }

        class TireViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,
                    batteryTextView,tireTextView,tempTextView;
            Button switchTempBtn,switchPressureBtn;
            MarqueeTextView statusTextView;
        }
    }
}
