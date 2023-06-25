package com.topflytech.tftble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.LogFileHelper;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.view.MarqueeTextView;


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


public class MainActivity extends AppCompatActivity {
    //    BluetoothClient mClient;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_ADVERTISE = 4;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
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
    BluetoothLeScanner mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
    SweetAlertDialog waitingCancelDlg;
    private ListView mListView;
    private ItemAdapter mListAdapter;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private boolean isFuzzySearchStatus = false, isOnView = false;
    private String fuzzyKey = "";
    private ArrayList<ArrayList<byte[]>> orignHistoryList = new ArrayList<ArrayList<byte[]>>();
    private long startDate, endDate, propOpenCount, propCloseCount;
    private int startBattery, endBattery;
    private String beginTemp, endTemp;
    private float beginHumidity, endHumidity, averageTemp, averageHumidity, maxTemp, minTemp, maxHumidity, minHumidity,
            overMaxTempLimitCount, overMaxTempLimitTime, overMinTempLimitCount, overMinTempLimitTime,
            overMaxHumidityLimitCount, overMaxHumidityLimitTime, overMinHumidityLimitCount, overMinHumidityLimitTime;
    private LinearLayout llFuzzySearch;
    public static final int REQUEST_SCAN_QRCODE = 1;
    public static final int RESPONSE_SCAN_QRCODE = 1;
    private Date dataNotifyDate;
    private Date enterDate;
    private ScanSettings mScanSettings;

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
        } else {

            if (bluetoothadapter.isEnabled()) {

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                            Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        //判断是否需要向用户解释为什么需要申请该权限
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                        }
                        //请求权限
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                REQUEST_CODE_BLUETOOTH_CONNECT);
                    }
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                } else {
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                }

            }
            refreshButton = (ImageView) findViewById(R.id.main_left_refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                        return;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            return;
                        }
                    }
//                    mBluetoothAdapter.stopLeScan(startSearchCallback);
                    try {
                        mBLEScanner.stopScan(new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {
                                super.onScanResult(callbackType, result);
                            }
                        });
                    } catch (Exception e) {

                    }

                    allBleDeviceDataList.clear();
                    allMacIndex.clear();
                    showBleDeviceDataList.clear();
                    showMacIndex.clear();
                    mListAdapter.notifyDataSetChanged();
                    if (bluetoothadapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
//                            return;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                return;
                            }
                        }

//                        mBluetoothAdapter.startLeScan(startSearchCallback);
                        if (mBLEScanner == null) {
                            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        }
                        mBLEScanner.startScan(null, mScanSettings, startScanCallback);
                        showWaitingCancelDlg("");
                    } else {
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        MainActivity.this.startActivity(enabler);
                    }
                }
            });
            searchEditText = (EditText) findViewById(R.id.et_fuzzy_search);
            llFuzzySearch = (LinearLayout) findViewById(R.id.ll_fuzzy_search);
            scanBtn = (ImageButton) findViewById(R.id.btn_scan);
            scanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QRCODE);
                }
            });
            searchEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 显示软键盘
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchEditText, 0);
                    searchEditText.setFocusable(true);
                    searchEditText.requestFocus();

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

                    if (!isFuzzySearchStatus) {
                        llFuzzySearch.setVisibility(View.VISIBLE);
                    } else {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_CONNECT)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_SCAN)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            REQUEST_CODE_BLUETOOTH_CONNECT);
                }
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    //判断是否需要向用户解释为什么需要申请该权限
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.BLUETOOTH_ADVERTISE)) {
//                        Toast.makeText(context,"Need to open ")
                    }
                    //请求权限
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.BLUETOOTH_ADVERTISE},
                            REQUEST_CODE_BLUETOOTH_ADVERTISE);
                }
            }
            ScanSettings.Builder builder = new ScanSettings.Builder()
                    //设置高功耗模式
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            //android 6.0添加设置回调类型、匹配模式等
            if (Build.VERSION.SDK_INT >= 23) {
                //定义回调类型
                builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
                //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
                builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);

            }

            if (Build.VERSION.SDK_INT >= 26) {
                builder.setLegacy(false);
            }
//芯片组支持批处理芯片上的扫描
            if (bluetoothadapter.isOffloadedScanBatchingSupported()) {
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                //设置为0以立即通知结果
                builder.setReportDelay(0L);
            }
            mScanSettings = builder.build();

            mListView = (ListView) findViewById(R.id.listView);
            mListView.setDividerHeight(0);
            mListAdapter = new ItemAdapter();
            mListView.setAdapter(mListAdapter);

            service.scheduleWithFixedDelay(checkBleSearchStatus, 1, 1, TimeUnit.SECONDS);
        }
    }



    private void showWaitingCancelDlg(String warning) {
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
        if (warning != null && !warning.isEmpty()) {
            waitingCancelDlg.setTitleText(warning);
        }
        waitingCancelDlg.show();
    }


    public void reStartApp() {
        try {
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(MainActivity.this.getBaseContext().getPackageName());
            intent.putExtra("REBOOT", "reboot");
            PendingIntent restartIntent = PendingIntent.getActivity(MainActivity.this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION || requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            reStartApp();
        } else if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT || requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothadapter = bluetoothManager.getAdapter();
            if (bluetoothadapter != null) {
                if (bluetoothadapter.isEnabled()) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                            return;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            return;
                        }
                    }

//                        mBluetoothAdapter.startLeScan(startSearchCallback);
                    mBLEScanner.startScan(null, mScanSettings, startScanCallback);
                    showWaitingCancelDlg("");
                } else {
                    Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enabler);
                }
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_QRCODE && resultCode == RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            searchEditText.setText(value);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
//                return;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    return;
//                }
            }
//            mBluetoothAdapter.startLeScan(startSearchCallback);
            if (mBLEScanner == null) {
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            if (mBLEScanner != null) {
                mBLEScanner.startScan(null, mScanSettings, startScanCallback);
            }

        }
        isOnView = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnView = false;
    }

    private void updateShowItem(BleDeviceData bleDeviceData) {
        if (bleDeviceData == null) {
            return;
        }
        if (showMacIndex.get(bleDeviceData.getMac()) != null && showBleDeviceDataList.size() > 0) {
            Integer index = showMacIndex.get(bleDeviceData.getMac());
            if (showBleDeviceDataList.size() <= index) {
                return;
            }
            BleDeviceData oldBleDeviceData = showBleDeviceDataList.get(index);
            if (oldBleDeviceData != null) {
                if (oldBleDeviceData.getDate().getTime() < bleDeviceData.getDate().getTime()) {
                    showBleDeviceDataList.set(index, bleDeviceData);
                }
            } else {
                showBleDeviceDataList.set(index, bleDeviceData);
            }
        } else {
            if (fuzzyKey == null || fuzzyKey.isEmpty()) {
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            } else {
                if ((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName() != null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))) {
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

    private void updateAllShowItem() {
        for (BleDeviceData bleDeviceData : allBleDeviceDataList) {
            updateShowItem(bleDeviceData);
        }
//        if (fuzzyKey != null && !fuzzyKey.isEmpty()) {
//            if(showBleDeviceDataList.size() == 1){
//                BleDeviceData bleDeviceData = showBleDeviceDataList.get(0);
//                connectDevice(bleDeviceData);
//            }
//        }
    }

    private void refreshShowItem() {
        Log.e("aaa","refreshShowItem");
        showBleDeviceDataList.clear();
        showMacIndex.clear();
        for (BleDeviceData bleDeviceData : allBleDeviceDataList) {
            if (fuzzyKey == null || fuzzyKey.isEmpty()) {
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
            } else {
                if ((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName() != null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))) {
                    showBleDeviceDataList.add(bleDeviceData);
                    showMacIndex.put(bleDeviceData.getMac(), showBleDeviceDataList.size() - 1);
                }
            }
        }
//        if(showBleDeviceDataList.size() == 0 && mListAdapter.isEmpty()){
//
//        }else{
//            mListAdapter.notifyDataSetChanged();
//        }
        mListAdapter.notifyDataSetChanged();
    }

    private Runnable checkBleSearchStatus = new Runnable() {
        @Override
        public void run() {
            if (!isOnView) {
                return;
            }
            Date checkDate = lastRecvDate;
            if (lastRecvDate == null) {
                checkDate = enterDate;
            }

//            Log.e("BluetoothUtils","checkBleSearchStatus");
            Date now = new Date();
            if (now.getTime() - checkDate.getTime() > 45000) {
                //need restart ble search
                Log.e("BluetoothUtils", "restart search");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    return;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return;
                    }
                }
//                mBluetoothAdapter.stopLeScan(startSearchCallback);
                try {
                    mBLEScanner.stopScan(new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            super.onScanResult(callbackType, result);
                        }
                    });
                } catch (Exception e) {

                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkDate = now;
//                mBluetoothAdapter.startLeScan(startSearchCallback);
                mBLEScanner.startScan(null, mScanSettings, startScanCallback);
            }
        }
    };

    private ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            }
            BluetoothDevice device = result.getDevice();

            int rssi = result.getRssi();
            byte[] scanRecord = result.getScanRecord().getBytes();
            String data = MyUtils.bytes2HexString(scanRecord, 0);
            if(result.getScanRecord() == null || result.getScanRecord().getDeviceName() == null || result.getScanRecord().getDeviceName().trim().length() == 0){
                return;
            }
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
            lastRecvDate = new Date();
            if(device.getAddress() != null && device.getAddress().contains("F9:44")){
                Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  "+ result.getScanRecord().getDeviceName() + "  " + device.getAddress() + "," + data);
            }
            if (data != null) {
                String head = data.substring(2, 8);
                String tireHead = "";
                if(data.length() > 20)
                {
                    tireHead = data.substring(16, 20).toLowerCase();
                }

                if (head.toLowerCase().equals("16ffbf") || tireHead.equals("ffac") || data.toLowerCase().startsWith("020106")) {
                    String id = device.getAddress().replaceAll(":", "").toUpperCase();
//                    if(!(id.equals("D104A3CC6E55") || id.contains("6676")|| id.contains("E625"))){
//                        return;
//                    }
                    if(MyUtils.isOpenBroadcastLog){
                        LogFileHelper.getInstance(MainActivity.this).writeIntoFile("onDeviceFounded:" + result.getScanRecord().getDeviceName() + "," + device.getAddress() + "," + data);
                    }
                    Integer index = allMacIndex.get(device.getAddress());
                    if (index != null && allBleDeviceDataList.size() > index) {
                        BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setRssiInt(rssi);
                        bleDeviceData.setHexData(data);
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setDeviceName(result.getScanRecord().getDeviceName());
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        }
                        if (data.toLowerCase().startsWith("020106") && !tireHead.equals("ffac")){
                            try {
                                if(device.getName() != null && device.getAddress().contains("F9:44")){
                                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  "+ result.getScanRecord().getDeviceName() + "  " + device.getAddress() + "," + data);
                                }
                                bleDeviceData.parseS0789Data(MainActivity.this);
                                if(bleDeviceData.getDeviceType().equals("errorDevice")){
                                    return;
                                }
//                                Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            if (tireHead.equals("ffac")) {
                                bleDeviceData.setDeviceType("tire");
                                bleDeviceData.setId(device.getAddress().replaceAll(":", ""));
                            }

                            try {
                                bleDeviceData.parseData(MainActivity.this);
                            } catch (Exception e) {
//                                e.printStackTrace();
                            }
                        }

                    } else {
                        BleDeviceData bleDeviceData = new BleDeviceData();
                        if (tireHead.equals("ffac")) {
                            bleDeviceData.setDeviceType("tire");
                        }
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setRssiInt(rssi);
                        bleDeviceData.setHexData(data);
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setMac(device.getAddress());
                        bleDeviceData.setDeviceName(result.getScanRecord().getDeviceName());
                        if (data.toLowerCase().startsWith("020106") && !tireHead.equals("ffac")){
                            try {
                                if(device.getName() != null && device.getAddress().contains("F9:44")){
                                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
                                }
                                bleDeviceData.parseS0789Data(MainActivity.this);
                                if(bleDeviceData.getDeviceType().equals("errorDevice")){
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                bleDeviceData.parseData(MainActivity.this);
                            } catch (Exception e) {
//                            e.printStackTrace();
                            }
                        }
                        allBleDeviceDataList.add(bleDeviceData);
                        index = allBleDeviceDataList.size() - 1;
                        allMacIndex.put(device.getAddress(), index);
                    }
                    if (dataNotifyDate == null || lastRecvDate.getTime() - dataNotifyDate.getTime() > 1000) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAllShowItem();

                            }
                        });
                        dataNotifyDate = lastRecvDate;
                        if(showBleDeviceDataList.size() == 0 && mListAdapter.isEmpty()){

                        }else{
                            mListAdapter.notifyDataSetChanged();
                        }

                    }
                    if (waitingCancelDlg != null) {
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

            final String mac = (String)view.getTag();
            Log.e("Click", "config device " + mac);
            Integer index = allMacIndex.get(mac);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                connectDevice(bleDeviceData);
            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
        }
    };

    private void connectDevice(BleDeviceData bleDeviceData){
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {


            }
            mBLEScanner.stopScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }
            });
        }catch (Exception e){

        }
        Intent intent = new Intent( MainActivity.this, EditActivity.class);
        intent.putExtra("mac",bleDeviceData.getMac());
        intent.putExtra("deviceType",bleDeviceData.getDeviceType());
        intent.putExtra("id",bleDeviceData.getId());
        intent.putExtra("software",bleDeviceData.getSoftware());
        startActivity(intent);
    }
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
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S07")){
                if(null == convertView || convertView.getTag() instanceof S07ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s07_detail_item,null);
                    S07ViewHolder holder = new S07ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.batteryPercentTextView = (TextView)convertView.findViewById(R.id.tx_battery_percent);
                    holder.flagTextView = (TextView)convertView.findViewById(R.id.tx_flag);
                    holder.alarmTextView = (MarqueeTextView) convertView.findViewById(R.id.tx_alarm);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    holder.broadcastTypeTextview = (TextView)convertView.findViewById(R.id.tx_broadcast_type);
                    holder.minorLine = (LinearLayout)convertView.findViewById(R.id.line_minor);
                    holder.majorLine = (LinearLayout)convertView.findViewById(R.id.line_major);
                    holder.bidTextView = (TextView)convertView.findViewById(R.id.tx_bid);
                    holder.nidTextView = (TextView)convertView.findViewById(R.id.tx_nid);
                    holder.majorTextView = (TextView)convertView.findViewById(R.id.tx_major);
                    holder.minorTextView = (TextView)convertView.findViewById(R.id.tx_minor);

                    holder.bidLine = (LinearLayout)convertView.findViewById(R.id.line_bid);
                    holder.nidLine = (LinearLayout)convertView.findViewById(R.id.line_nid);
                    holder.minorLine = (LinearLayout)convertView.findViewById(R.id.line_minor);
                    holder.majorLine = (LinearLayout)convertView.findViewById(R.id.line_major);
                    holder.batteryLine = (LinearLayout)convertView.findViewById(R.id.line_battery);
                    holder.batteryPercentLine = (LinearLayout)convertView.findViewById(R.id.line_battery_percent);
                    holder.warnLine = (LinearLayout)convertView.findViewById(R.id.line_warn);
                    holder.flagLine = (LinearLayout)convertView.findViewById(R.id.line_flag);

                    convertView.setTag(holder);
                }

                S07ViewHolder holder = (S07ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.batteryPercentTextView.setText(bleDeviceData.getBatteryPercent());
                holder.flagTextView.setText(bleDeviceData.getFlag());
                holder.majorTextView.setText(bleDeviceData.getMajor());
                holder.minorTextView.setText(bleDeviceData.getMinor());
                holder.bidTextView.setText(bleDeviceData.getBid());
                holder.nidTextView.setText(bleDeviceData.getNid());
                if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.alarmTextView.setText("-");
                }else{
                    holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                }
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                holder.broadcastTypeTextview.setText(bleDeviceData.getBroadcastType());

                holder.minorLine.setVisibility(View.GONE);
                holder.majorLine.setVisibility(View.GONE);
                holder.bidLine.setVisibility(View.GONE);
                holder.nidLine.setVisibility(View.GONE);
                holder.batteryLine.setVisibility(View.GONE);
                holder.batteryPercentLine.setVisibility(View.GONE);
                holder.warnLine.setVisibility(View.GONE);
                holder.flagLine.setVisibility(View.GONE);
                if(bleDeviceData.getBroadcastType().equals("Eddystone UID")){
                    holder.bidLine.setVisibility(View.VISIBLE);
                    holder.nidLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Long range")){
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                    holder.flagLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.minorLine.setVisibility(View.VISIBLE);
                    holder.majorLine.setVisibility(View.VISIBLE);
                }else{
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                    holder.flagLine.setVisibility(View.VISIBLE);
                }

                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S08")){
                if(null == convertView || convertView.getTag() instanceof S08ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s08_detail_item,null);
                    S08ViewHolder holder = new S08ViewHolder();
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
                    holder.batteryPercentTextView = (TextView)convertView.findViewById(R.id.tx_battery_percent);
                    holder.doorTextView = (TextView)convertView.findViewById(R.id.tx_door);
                    holder.broadcastTypeTextview = (TextView)convertView.findViewById(R.id.tx_broadcast_type);
                    holder.moveTextView = (TextView)convertView.findViewById(R.id.tx_move_prop);
                    holder.moveDetectionTextView = (TextView)convertView.findViewById(R.id.tx_move_detection_prop);
                    holder.stopDetectionTextView = (TextView)convertView.findViewById(R.id.tx_stop_detection_prop);
                    holder.pitchTextView = (TextView)convertView.findViewById(R.id.tx_pitch_prop);
                    holder.rollTextView = (TextView)convertView.findViewById(R.id.tx_roll_prop);
                    holder.bidTextView = (TextView)convertView.findViewById(R.id.tx_bid);
                    holder.nidTextView = (TextView)convertView.findViewById(R.id.tx_nid);
                    holder.majorTextView = (TextView)convertView.findViewById(R.id.tx_major);
                    holder.minorTextView = (TextView)convertView.findViewById(R.id.tx_minor);

                    holder.doorLine = (LinearLayout)convertView.findViewById(R.id.line_door);
                    holder.bidLine = (LinearLayout)convertView.findViewById(R.id.line_bid);
                    holder.nidLine = (LinearLayout)convertView.findViewById(R.id.line_nid);
                    holder.batteryLine = (LinearLayout)convertView.findViewById(R.id.line_battery);
                    holder.batteryPercentLine = (LinearLayout)convertView.findViewById(R.id.line_battery_percent);
                    holder.tempLine = (LinearLayout)convertView.findViewById(R.id.line_temp);
                    holder.lightLine = (LinearLayout)convertView.findViewById(R.id.line_light);
                    holder.moveLine = (LinearLayout)convertView.findViewById(R.id.line_move);
                    holder.moveDetectionLine = (LinearLayout)convertView.findViewById(R.id.line_move_detection);
                    holder.stopDetectionLine = (LinearLayout)convertView.findViewById(R.id.line_stop_detection);
                    holder.pitchLine = (LinearLayout)convertView.findViewById(R.id.line_pitch);
                    holder.rollLine = (LinearLayout)convertView.findViewById(R.id.line_roll);
                    holder.warnLine = (LinearLayout)convertView.findViewById(R.id.line_warn);
                    holder.minorLine = (LinearLayout)convertView.findViewById(R.id.line_minor);
                    holder.majorLine = (LinearLayout)convertView.findViewById(R.id.line_major);

                    convertView.setTag(holder);
                }

                S08ViewHolder holder = (S08ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.tempTextView.setText("-");
                    holder.alarmTextView.setText("-");
                }else{
                    holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                    holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                }
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.humidityTextView.setText(bleDeviceData.getHumidity() + "%");
                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                holder.batteryPercentTextView.setText(bleDeviceData.getBatteryPercent());
                holder.doorTextView.setText(bleDeviceData.getDoorStatus());
                holder.broadcastTypeTextview.setText(bleDeviceData.getBroadcastType());
                holder.moveTextView.setText(bleDeviceData.getMove());
                holder.moveDetectionTextView.setText(bleDeviceData.getMoveDetection());
                holder.stopDetectionTextView.setText(bleDeviceData.getStopDetection());
                holder.pitchTextView.setText(bleDeviceData.getPitchAngle());
                holder.rollTextView.setText(bleDeviceData.getRollAngle());
                holder.bidTextView.setText(bleDeviceData.getBid());
                holder.nidTextView.setText(bleDeviceData.getNid());
                holder.majorTextView.setText(bleDeviceData.getMajor());
                holder.minorTextView.setText(bleDeviceData.getMinor());
                if(MyUtils.isDebug){
                    holder.qrcodeLL.setVisibility(View.VISIBLE);
                    holder.qrCodeBtn.setTag(bleDeviceData.getId());
                    holder.qrCodeBtn.setOnClickListener(showQrCodeClick);
                }
                holder.doorLine.setVisibility(View.GONE);
                holder.bidLine.setVisibility(View.GONE);
                holder.nidLine.setVisibility(View.GONE);
                holder.batteryLine.setVisibility(View.GONE);
                holder.batteryPercentLine.setVisibility(View.GONE);
                holder.tempLine.setVisibility(View.GONE);
                holder.lightLine.setVisibility(View.GONE);
                holder.moveLine.setVisibility(View.GONE);
                holder.moveDetectionLine.setVisibility(View.GONE);
                holder.stopDetectionLine.setVisibility(View.GONE);
                holder.pitchLine.setVisibility(View.GONE);
                holder.rollLine.setVisibility(View.GONE);
                holder.warnLine.setVisibility(View.GONE);
                holder.minorLine.setVisibility(View.GONE);
                holder.majorLine.setVisibility(View.GONE);
                if(bleDeviceData.getBroadcastType().equals("Eddystone UID")){
                    holder.bidLine.setVisibility(View.VISIBLE);
                    holder.nidLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Long range")){
                    holder.doorLine.setVisibility(View.VISIBLE);
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.tempLine.setVisibility(View.VISIBLE);
                    holder.lightLine.setVisibility(View.VISIBLE);
                    if(!bleDeviceData.getMove().equals("-")){
                        holder.moveLine.setVisibility(View.VISIBLE);
                        if(bleDeviceData.getMoveStatus() != 2){
                            holder.moveDetectionLine.setVisibility(View.VISIBLE);
                            holder.stopDetectionLine.setVisibility(View.VISIBLE);
                            holder.pitchLine.setVisibility(View.VISIBLE);
                            holder.rollLine.setVisibility(View.VISIBLE);
                        }
                    }

                    holder.warnLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.minorLine.setVisibility(View.VISIBLE);
                    holder.majorLine.setVisibility(View.VISIBLE);
                }else{
                    holder.doorLine.setVisibility(View.VISIBLE);
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.tempLine.setVisibility(View.VISIBLE);
                    holder.lightLine.setVisibility(View.VISIBLE);
                    if(!bleDeviceData.getMove().equals("-")){
                        holder.moveLine.setVisibility(View.VISIBLE);
                        if(bleDeviceData.getMoveStatus() != 2){
                            holder.moveDetectionLine.setVisibility(View.VISIBLE);
                            holder.stopDetectionLine.setVisibility(View.VISIBLE);
                            holder.pitchLine.setVisibility(View.VISIBLE);
                            holder.rollLine.setVisibility(View.VISIBLE);
                        }
                    }
                    holder.warnLine.setVisibility(View.VISIBLE);
                }
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S10")){
                if(null == convertView || convertView.getTag() instanceof S10ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s10_detail_item,null);
                    S10ViewHolder holder = new S10ViewHolder();
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
                    holder.batteryPercentTextView = (TextView)convertView.findViewById(R.id.tx_battery_percent);
                    holder.broadcastTypeTextview = (TextView)convertView.findViewById(R.id.tx_broadcast_type);
                    holder.bidTextView = (TextView)convertView.findViewById(R.id.tx_bid);
                    holder.nidTextView = (TextView)convertView.findViewById(R.id.tx_nid);
                    holder.majorTextView = (TextView)convertView.findViewById(R.id.tx_major);
                    holder.minorTextView = (TextView)convertView.findViewById(R.id.tx_minor);

                    holder.bidLine = (LinearLayout)convertView.findViewById(R.id.line_bid);
                    holder.nidLine = (LinearLayout)convertView.findViewById(R.id.line_nid);
                    holder.batteryLine = (LinearLayout)convertView.findViewById(R.id.line_battery);
                    holder.batteryPercentLine = (LinearLayout)convertView.findViewById(R.id.line_battery_percent);
                    holder.tempLine = (LinearLayout)convertView.findViewById(R.id.line_temp);
                    holder.lightLine = (LinearLayout)convertView.findViewById(R.id.line_light);

                    holder.warnLine = (LinearLayout)convertView.findViewById(R.id.line_warn);
                    holder.minorLine = (LinearLayout)convertView.findViewById(R.id.line_minor);
                    holder.majorLine = (LinearLayout)convertView.findViewById(R.id.line_major);
                    holder.humidityLine = (LinearLayout)convertView.findViewById(R.id.line_humidity);
                    convertView.setTag(holder);
                }

                S10ViewHolder holder = (S10ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.tempTextView.setText("-");
                    holder.alarmTextView.setText("-");
                }else{
                    holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getSourceTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                    holder.alarmTextView.setText(BleDeviceData.getWarnDesc(MainActivity.this,bleDeviceData.getDeviceType(),bleDeviceData.getWarn()));
                }
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.humidityTextView.setText(bleDeviceData.getHumidity() + "%");
                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
                holder.switchTempBtn.setOnClickListener(switchTempUnitClick);
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                holder.batteryPercentTextView.setText(bleDeviceData.getBatteryPercent());
                holder.broadcastTypeTextview.setText(bleDeviceData.getBroadcastType());
                holder.bidTextView.setText(bleDeviceData.getBid());
                holder.nidTextView.setText(bleDeviceData.getNid());
                holder.majorTextView.setText(bleDeviceData.getMajor());
                holder.minorTextView.setText(bleDeviceData.getMinor());
                if(MyUtils.isDebug){
                    holder.qrcodeLL.setVisibility(View.VISIBLE);
                    holder.qrCodeBtn.setTag(bleDeviceData.getId());
                    holder.qrCodeBtn.setOnClickListener(showQrCodeClick);
                }
                holder.bidLine.setVisibility(View.GONE);
                holder.nidLine.setVisibility(View.GONE);
                holder.batteryLine.setVisibility(View.GONE);
                holder.batteryPercentLine.setVisibility(View.GONE);
                holder.tempLine.setVisibility(View.GONE);
                holder.lightLine.setVisibility(View.GONE);
                holder.warnLine.setVisibility(View.GONE);
                holder.minorLine.setVisibility(View.GONE);
                holder.majorLine.setVisibility(View.GONE);
                holder.humidityLine.setVisibility(View.GONE);
                if(bleDeviceData.getBroadcastType().equals("Eddystone UID")){
                    holder.bidLine.setVisibility(View.VISIBLE);
                    holder.nidLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Long range")){
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.tempLine.setVisibility(View.VISIBLE);
                    holder.humidityLine.setVisibility(View.VISIBLE);
                    holder.lightLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.minorLine.setVisibility(View.VISIBLE);
                    holder.majorLine.setVisibility(View.VISIBLE);
                }else{
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.tempLine.setVisibility(View.VISIBLE);
                    holder.humidityLine.setVisibility(View.VISIBLE);
                    holder.lightLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                }
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("S09")){
                if(null == convertView  || convertView.getTag() instanceof S09ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_s09_detail_item,null);
                    S09ViewHolder holder = new S09ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    convertView.setTag(holder);
                }

                S09ViewHolder holder = (S09ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
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
        class S08ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,humidityTextView,devicePropTextView,doorTextView,batteryPercentTextView,broadcastTypeTextview,
                    moveTextView,moveDetectionTextView,stopDetectionTextView,pitchTextView,rollTextView,bidTextView,nidTextView,
                    majorTextView,minorTextView;
            Button switchTempBtn,configBtn,qrCodeBtn;
            LinearLayout qrcodeLL;
            LinearLayout doorLine,bidLine,nidLine,batteryLine,batteryPercentLine,tempLine,lightLine,moveLine,moveDetectionLine,
                    stopDetectionLine,pitchLine,rollLine,warnLine,majorLine,minorLine;
            MarqueeTextView alarmTextView;
        }
        class S07ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,batteryPercentTextView,flagTextView,broadcastTypeTextview,bidTextView,nidTextView,majorTextView,minorTextView;
            MarqueeTextView alarmTextView;
            LinearLayout  bidLine,nidLine,majorLine,minorLine,batteryLine,batteryPercentLine,warnLine,flagLine;
            Button configBtn;

        }
        class S09ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView;
            Button configBtn;
        }
        class S10ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,humidityTextView,devicePropTextView,batteryPercentTextView,broadcastTypeTextview,
                    bidTextView,nidTextView, majorTextView,minorTextView;;
            Button switchTempBtn,configBtn,qrCodeBtn;
            LinearLayout bidLine,nidLine,batteryLine,batteryPercentLine,tempLine,lightLine,warnLine,majorLine,minorLine,humidityLine;
            LinearLayout qrcodeLL;
            MarqueeTextView alarmTextView;
        }
        class TireViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,
                    batteryTextView,tireTextView,tempTextView;
            Button switchTempBtn,switchPressureBtn;
            MarqueeTextView statusTextView;
        }
    }
}
