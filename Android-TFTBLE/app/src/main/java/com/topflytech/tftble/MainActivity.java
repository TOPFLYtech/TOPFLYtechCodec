package com.topflytech.tftble;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.github.gzuliyujiang.wheelpicker.OptionPicker;
import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.BluetoothPermissionHelper;
import com.topflytech.tftble.data.DfuService;
import com.topflytech.tftble.data.DownloadFileManager;
import com.topflytech.tftble.data.LogFileHelper;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.OpenAPI;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.data.SingleOptionSelectClickListener;
import com.topflytech.tftble.view.MarqueeTextView;


import org.opencv.OpenCV;

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
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


public class MainActivity extends AppCompatActivity implements BluetoothPermissionHelper.BluetoothPermissionCallback {
    //    BluetoothClient mClient;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 2;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 3;
    private static final int REQUEST_CODE_BLUETOOTH_ADVERTISE = 4;
    private static final int REQUEST_CODE_BLUETOOTH_CONNECT = 5;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static ArrayList<BleDeviceData> allBleDeviceDataList = new ArrayList<BleDeviceData>();
    public static ArrayList<BleDeviceData> showBleDeviceDataList = new ArrayList<BleDeviceData>();
//    private ConcurrentHashMap<String, Integer> allMacIndex = new ConcurrentHashMap<String, Integer>(32);
//    private ConcurrentHashMap<String, Integer> showMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private String serviceId = "27760001-999C-4D6A-9FC4-C7272BE10900";
    private String uuid = "27763561-999C-4D6A-9FC4-C7272BE10900";
    private ActionBar actionBar;
    private ImageView refreshButton;
    private ImageView fuzzySearchBtn;
    private ImageView infoBtn;
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
    public static final int REQUEST_SCAN_QRCODE = 99;
    public static final int RESPONSE_SCAN_QRCODE = 99;
    private Date dataNotifyDate;
    private Date enterDate;
    private ScanSettings mScanSettings;
    private ArrayList<String> modelList = new ArrayList<String>(){
        {
            add("TSTH1-B");
            add("TSDT1-B");
            add("TSR1-B");
            add("T-button");
            add("T-sense");
            add("T-hub");
            add("T-one");
        }
    };
    private ArrayList<String> deviceTypeList = new ArrayList<String>(){
        {
            add("S02");
            add("S04");
            add("S05");
            add("S07");
            add("S08");
            add("S09");
            add("S10");
        }
    };
    private OptionPicker pvModel;
    private int clickCount = 0;
    private BluetoothPermissionHelper bluetoothPermissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化OpenCV
        OpenCV.initAsync(MainActivity.this);
        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(MainActivity.this);
        bluetoothPermissionHelper = new BluetoothPermissionHelper(this, this);
        CrashReport.initCrashReport(getApplicationContext());
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.main_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount >= 10) {
                    MyUtils.isDebug = true;
                    // 停止点击事件
                    customView.setOnClickListener(null);
                }else if(clickCount > 6){
                    Toast.makeText(MainActivity.this,"再点击" + (10 - clickCount) + "次，打开Debug功能",Toast.LENGTH_SHORT).show();
                }

                if(clickCount == 1){
                    // 在 20 秒后检查点击次数
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!MyUtils.isDebug) {
                                // 20 秒内未达到点击次数要求
                                // 进行相应的处理逻辑
                                clickCount = 0;
                            }
                        }
                    }, 20000); // 20 秒
                }
            }
        });


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
            refreshButton = (ImageView) findViewById(R.id.main_left_refresh);
            refreshButton.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                    }
//                    mBluetoothAdapter.stopLeScan(startSearchCallback);
                    try {
                        mBLEScanner.stopScan(startScanCallback);
                    } catch (Exception e) {

                    }

                    allBleDeviceDataList.clear();
//                    allMacIndex.clear();
                    showBleDeviceDataList.clear();
//                    showMacIndex.clear();
                    mListAdapter.notifyDataSetChanged();
                    if (bluetoothadapter.isEnabled()) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                        }

//                        mBluetoothAdapter.startLeScan(startSearchCallback);
                        if (mBLEScanner == null) {
                            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        }

                        initScanSettings(mBluetoothAdapter);
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
            scanBtn.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent intent = new Intent(MainActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QRCODE);
                }
            });
            searchEditText.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View view) {
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
            infoBtn = (ImageView) findViewById(R.id.main_bar_info);
            infoBtn.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    try {
                        PackageManager packageManager = MainActivity.this.getPackageManager();
                        PackageInfo packageInfo = packageManager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                        Toast.makeText(MainActivity.this,"V"+packageInfo.versionName,Toast.LENGTH_SHORT).show();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();

                    }
                }
            });
            fuzzySearchBtn = (ImageView) findViewById(R.id.main_right_fuzzy_search);
            fuzzySearchBtn.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View view) {

                    if (!isFuzzySearchStatus) {
                        llFuzzySearch.setVisibility(View.VISIBLE);
                    } else {
                        fuzzyKey = "";
                        searchEditText.setText(fuzzyKey);
                        refreshShowItem();
                        llFuzzySearch.setVisibility(View.GONE);
                    }
                    isFuzzySearchStatus = !isFuzzySearchStatus;


                }
            });
//        mClient = new BluetoothClient(MainActivity.this);

            initScanSettings(bluetoothadapter);

            mListView = (ListView) findViewById(R.id.listView);
            mListView.setDividerHeight(0);
            mListAdapter = new ItemAdapter();
            mListView.setAdapter(mListAdapter);

            service.scheduleWithFixedDelay(checkBleSearchStatus, 1, 1, TimeUnit.SECONDS);
        }

        pvModel = new OptionPicker(this);
        pvModel.setData(modelList);
        pvModel.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                //返回的分别是三个级别的选中位置
                String modelName = modelList.get(position);
                String deviceType = deviceTypeList.get(position);
                SweetAlertDialog confirmUpgradeDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                confirmUpgradeDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                confirmUpgradeDlg.setTitleText(getResources().getString(R.string.upgrade_device_to) + " " + modelName);
                confirmUpgradeDlg.setCancelable(true);
                confirmUpgradeDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmUpgradeDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmUpgradeDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        confirmUpgradeDlg.hide();
                        upgradeDfuDevice(deviceType);

                    }
                });
                confirmUpgradeDlg.show();
            }
        });


        LinearLayout rootView = findViewById(R.id.root_view);

        // 添加点击事件监听器
        rootView.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                hideKeyboard();
            }
        });
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // 隐藏键盘的方法
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void initScanSettings(BluetoothAdapter bluetoothadapter) {
        if(mScanSettings == null){
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
        }
    }

    SweetAlertDialog waitingDlg;
    private void showWaitingDlg(String warning){

        waitingDlg = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
            waitingDlg.show();
        }
    }
    private BleDeviceData selectDfuDevice;
    private void upgradeDfuDevice(String deviceType){
        if(!MyUtils.isNetworkConnected(MainActivity.this)){
            Toast.makeText(MainActivity.this,R.string.network_permission_fail,Toast.LENGTH_SHORT).show();
            return;
        }
        if(selectDfuDevice == null){
            Toast.makeText(MainActivity.this,R.string.error_please_try_again,Toast.LENGTH_SHORT).show();
            return;
        }
        showWaitingDlg(getResources().getString(R.string.waiting));
        OpenAPI.instance().getServerVersion(deviceType,new OpenAPI.Callback() {
            @Override
            public void callback(StatusCode code, String result) {
                if(code == StatusCode.OK){
                    JSONObject resObj = JSONObject.parseObject(result);
                    int jsonCode = resObj.getInteger("code");
                    if(jsonCode == 0){
                        JSONObject jsonData  = resObj.getJSONObject("data");
                        if(jsonData != null){

                            String upgradeLink = jsonData.getString("link");
                            DownloadFileManager.instance().geetUpdateFileUrl(MainActivity.this, upgradeLink, "", deviceType, new DownloadFileManager.Callback() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void callback(StatusCode code, String result) {
                                    if(StatusCode.OK == code){
                                        updateDFU(selectDfuDevice.getMac(),selectDfuDevice.getDeviceName(),result);
                                    }else{
                                        if(waitingCancelDlg != null){
                                            waitingCancelDlg.hide();
                                        }
                                        selectDfuDevice = null;
                                        Toast.makeText(MainActivity.this,R.string.download_file_fail,Toast.LENGTH_LONG);
                                    }
                                }
                            });
                        }
                    }else{
                        selectDfuDevice = null;
                        Toast.makeText(MainActivity.this,R.string.get_upgrade_info_fail,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }else{
                    selectDfuDevice = null;
                    Toast.makeText(MainActivity.this,R.string.get_upgrade_info_fail,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
    private DfuServiceController dfuServiceController;
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDFU(String mac, String name, String path){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DfuServiceInitiator.createDfuNotificationChannel(this);
        }
        DfuServiceListenerHelper.registerProgressListener(MainActivity.this, dfuProgressListener);
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac)
                .setDeviceName(name)
                .setForceDfu(true)
                .setKeepBond(true)
                .setPacketsReceiptNotificationsEnabled(true);
        starter.setZip(path);
        // We can use the controller to pause, resume or abort the DFU process.
        dfuServiceController = starter.start(MainActivity.this, DfuService.class);
    }

    private static int progressPercent = 0;
    private static String upgradeErrorMsg = "";
    private static String upgradeStatus = "";

    private DfuProgressListener dfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDfuCompleted(String deviceAddress) {
            Log.e("BluetoothUtils","onDfuCompleted");
            upgradeStatus = "completed";
            Toast.makeText(MainActivity.this,R.string.upgrade_succ,Toast.LENGTH_LONG);
            waitingDlg.hide();
            for(int i = 0;i < showBleDeviceDataList.size();i++){
                BleDeviceData item = showBleDeviceDataList.get(i);
                if(item.getMac().equals(selectDfuDevice.getMac())){
                    showBleDeviceDataList.remove(i);
                    break;
                }
            }
            for(int i = 0;i < allBleDeviceDataList.size();i++){
                BleDeviceData item = allBleDeviceDataList.get(i);
                if(item.getMac().equals(selectDfuDevice.getMac())){
                    allBleDeviceDataList.remove(i);
                    break;
                }
            }
            mListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            Log.e("BluetoothUtils","onDfuAborted");
            upgradeStatus = "aborted";
            waitingDlg.hide();
            Toast.makeText(MainActivity.this,R.string.upgrade_aborted,Toast.LENGTH_LONG);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            Log.e("BluetoothUtils","onError");
            upgradeStatus = "error";
            upgradeErrorMsg = message;
            waitingDlg.hide();
            Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG);
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
//            Log.e("BluetoothUtils","onProgressChanged:"+percent);
            progressPercent = percent;
            upgradeStatus = "progressChanged";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.processing)+ ":" + percent);
            }

        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            Log.e("BluetoothUtils","onDfuProcessStarted");
            upgradeStatus = "processStarted";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_process_start));
            }

        }

        @Override
        public void onDeviceConnecting(String deviceAddress) {
            Log.e("BluetoothUtils","onDeviceConnecting");
            upgradeStatus = "deviceConnecting";
            if(waitingDlg != null){
                waitingDlg.setTitleText(getResources().getString(R.string.upgrade_device_connecting));
            }

        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            Log.e("BluetoothUtils","onDeviceDisconnecting");
            upgradeStatus = "deviceDisconnected";

        }
    };

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
        bluetoothPermissionHelper.onRequestPermissionsResult(requestCode,grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_QRCODE && resultCode == RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            searchEditText.setText(value);
        }
        bluetoothPermissionHelper.onActivityResult(requestCode,resultCode);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }

//            mBluetoothAdapter.startLeScan(startSearchCallback);
            if (mBLEScanner == null) {
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            if (mBLEScanner != null) {
                initScanSettings(mBluetoothAdapter);
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
        boolean findItem = false;
        for(int i = 0;i < showBleDeviceDataList.size();i++){
            BleDeviceData item = showBleDeviceDataList.get(i);
            if(item.getMac().equals(bleDeviceData.getMac())){
                showBleDeviceDataList.set(i,bleDeviceData);
                findItem = true;
                break;
            }
        }
        if(!findItem){
            if (fuzzyKey == null || fuzzyKey.isEmpty()) {
                showBleDeviceDataList.add(bleDeviceData);
            } else {
                if ((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName() != null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))) {
                    showBleDeviceDataList.add(bleDeviceData);
                }
            }
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
        for (BleDeviceData bleDeviceData : allBleDeviceDataList) {
            if (fuzzyKey == null || fuzzyKey.isEmpty()) {
                showBleDeviceDataList.add(bleDeviceData);
            } else {
                if ((bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                        || (bleDeviceData.getDeviceName() != null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))) {
                    showBleDeviceDataList.add(bleDeviceData);
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
                enterDate = new Date();
                Log.e("BluetoothUtils", "restart search");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                }
//                mBluetoothAdapter.stopLeScan(startSearchCallback);
                try {
                    mBLEScanner.stopScan(startScanCallback);
                } catch (Exception e) {

                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkDate = now;
//                mBluetoothAdapter.startLeScan(startSearchCallback);
                initScanSettings(mBluetoothAdapter);
                mBLEScanner.startScan(null, mScanSettings, startScanCallback);
            }
        }
    };

    private ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            try{
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                BluetoothDevice device = result.getDevice();

                int rssi = result.getRssi();
                byte[] scanRecord = result.getScanRecord().getBytes();
                String data = MyUtils.bytes2HexString(scanRecord, 0);
                if(result.getScanRecord() == null || result.getScanRecord().getDeviceName() == null  ){
                    return;
                }
//            Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + "," + data);
                lastRecvDate = new Date();
                if(device.getAddress() != null && device.getAddress().contains("01:EB")){
//                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  "+ result.getScanRecord().getDeviceName() + "  " + device.getAddress() + "," + data);

                }else{
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
                        boolean findItem = false;
                        BleDeviceData existItem = null;
                        for(int i = 0;i < allBleDeviceDataList.size();i++){
                            BleDeviceData item = allBleDeviceDataList.get(i);
                            if(item.getMac().equals(device.getAddress())){
                                existItem = item;
                                findItem = true;
                                break;
                            }
                        }
                        if (findItem && existItem != null) {
                            BleDeviceData bleDeviceData = existItem;
                            bleDeviceData.setRssi(rssi + "dBm");
                            bleDeviceData.setRssiInt(rssi);
                            bleDeviceData.setHexData(data);
                            bleDeviceData.setSrcData(scanRecord);
                            bleDeviceData.setDate(new Date());

                            bleDeviceData.setDeviceName(result.getScanRecord().getDeviceName());
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                            }
                            if(result.getScanRecord().getDeviceName().toLowerCase().contains("dfu") && data.toLowerCase().startsWith("020106030259fe")){
                                bleDeviceData.setDeviceType("dfuDevice");
                                bleDeviceData.setId(device.getAddress().replaceAll(":", ""));
                            }else if (data.toLowerCase().startsWith("020106") && !tireHead.equals("ffac")){
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
                            if(result.getScanRecord().getDeviceName().toLowerCase().contains("dfu")  && data.toLowerCase().startsWith("020106030259fe")){
                                bleDeviceData.setDeviceType("dfuDevice");
                                bleDeviceData.setId(device.getAddress().replaceAll(":", ""));
                            }else if (data.toLowerCase().startsWith("020106") && !tireHead.equals("ffac")){
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
                            findItem = false;
                            for(int i = 0;i < allBleDeviceDataList.size();i++){
                                BleDeviceData item = allBleDeviceDataList.get(i);
                                if(item.getMac().equals(device.getAddress())){
                                    findItem = true;
                                    allBleDeviceDataList.set(i,bleDeviceData);
                                    break;
                                }
                            }
                            if (!findItem) {
                                allBleDeviceDataList.add(bleDeviceData);
                            }
                        }
                        int refreshTime = 1000;
                        if(allBleDeviceDataList.size() > 30){
                            refreshTime = 2200;
                        }else if(allBleDeviceDataList.size() > 20){
                            refreshTime = 1600;
                        }
                        if (dataNotifyDate == null || lastRecvDate.getTime() - dataNotifyDate.getTime() > refreshTime) {
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
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };


    View.OnClickListener switchPressureUnitClick = new SingleClickListener() {
        @Override
        public void onSingleClick(View view) {
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
    View.OnClickListener configDeviceClick = new SingleClickListener() {
        @Override
        public void onSingleClick(final View view) {
            final String mac = (String)view.getTag();
            Log.e("Click", "config device " + mac);

            for(int i = 0;i < allBleDeviceDataList.size();i++){
                BleDeviceData item = allBleDeviceDataList.get(i);
                if(item.getMac().equals(mac)){
                    connectDevice(item);
                    break;
                }
            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
        }
    };

    View.OnClickListener resetDevicePwdClick = new SingleClickListener() {
        @Override
        public void onSingleClick(final View view) {

            final String mac = (String)view.getTag();
            Log.e("Click", "config device " + mac);

            for(int i = 0;i < allBleDeviceDataList.size();i++){
                BleDeviceData item = allBleDeviceDataList.get(i);
                if(item.getMac().equals(mac)){
                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        }
                        mBLEScanner.stopScan(startScanCallback);
                    }catch (Exception e){

                    }
                    Intent intent = new Intent( MainActivity.this, SuperPwdResetActivity.class);
                    intent.putExtra("mac",item.getMac());
                    intent.putExtra("deviceType",item.getDeviceType());
                    intent.putExtra("id",item.getId());
                    intent.putExtra("software",item.getSoftware());
                    startActivity(intent);
                    break;
                }
            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
        }
    };

    View.OnClickListener upgradeDfuErrorClick = new SingleClickListener() {
        @Override
        public void onSingleClick(final View view) {

            final String mac = (String)view.getTag();
            Log.e("Click", "upgradeDfuError device " + mac);
            for(int i = 0;i < allBleDeviceDataList.size();i++){
                BleDeviceData item = allBleDeviceDataList.get(i);
                if(item.getMac().equals(mac)){
                    selectDfuDevice = item;
                    pvModel.setDefaultPosition(0);
                    pvModel.show();
                }
            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
        }
    };

    private void connectDevice(BleDeviceData bleDeviceData){
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {


            }
            mBLEScanner.stopScan(startScanCallback);
        }catch (Exception e){

        }
        Intent intent = new Intent( MainActivity.this, EditActivity.class);
        intent.putExtra("mac",bleDeviceData.getMac());
        intent.putExtra("deviceType",bleDeviceData.getDeviceType());
        intent.putExtra("id",bleDeviceData.getId());
        intent.putExtra("software",bleDeviceData.getSoftware());
        intent.putExtra("hardware",bleDeviceData.getHardware());
        intent.putExtra("extSensorType",bleDeviceData.getExtSensorType());
        startActivity(intent);
    }
    View.OnClickListener showQrCodeClick = new SingleClickListener() {
        @Override
        public void onSingleClick(final View view) {
            final String id = (String)view.getTag();
            SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
            Bitmap  bitmap =    QRCodeEncoder.syncEncodeQRCode(id,200);
            pDialog.setCustomImageBitmap(bitmap) ;
            pDialog.setTitleText("");
            pDialog.setCancelable(false);
            pDialog.show();

        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        bluetoothPermissionHelper.startCheckBlePermission();
    }
    @Override
    public void onPermissionsGranted() {

        startBluetoothOperations();
    }
    private void startBluetoothOperations() {

        // 这里放置蓝牙扫描和连接的代码
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
        //            mBluetoothAdapter.startLeScan(startSearchCallback);
        if (mBLEScanner == null) {
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        if (mBLEScanner != null) {
            initScanSettings(mBluetoothAdapter);
            mBLEScanner.startScan(null, mScanSettings, startScanCallback);
        }
        if(allBleDeviceDataList.size() == 0){
            showWaitingCancelDlg("");
        }

    }
    @Override
    public void onLocationStart() {

        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
            if (mBLEScanner == null) {
                mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            if (mBLEScanner != null) {
                mBLEScanner.stopScan(startScanCallback);
                initScanSettings(mBluetoothAdapter);
                mBLEScanner.startScan(null, mScanSettings, startScanCallback);
            }
        } catch (Exception e) {

        }

    }


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
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                if(bleDeviceData.getSoftware().compareTo("23") >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
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
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                if(bleDeviceData.getSoftware().compareTo("22") >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
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
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                if(bleDeviceData.getSoftware().compareTo("17") >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
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
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
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
                String formatSoftware = bleDeviceData.getSoftware().replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1006");
                if(result >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
                }
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
//                    holder.devicePropTextView = (TextView)convertView.findViewById(R.id.tx_device_prop);
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
//                    holder.lightLine = (LinearLayout)convertView.findViewById(R.id.line_light);
                    holder.moveLine = (LinearLayout)convertView.findViewById(R.id.line_move);
                    holder.moveDetectionLine = (LinearLayout)convertView.findViewById(R.id.line_move_detection);
                    holder.stopDetectionLine = (LinearLayout)convertView.findViewById(R.id.line_stop_detection);
                    holder.pitchLine = (LinearLayout)convertView.findViewById(R.id.line_pitch);
                    holder.rollLine = (LinearLayout)convertView.findViewById(R.id.line_roll);
                    holder.warnLine = (LinearLayout)convertView.findViewById(R.id.line_warn);
                    holder.minorLine = (LinearLayout)convertView.findViewById(R.id.line_minor);
                    holder.majorLine = (LinearLayout)convertView.findViewById(R.id.line_major);
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                String formatSoftware = bleDeviceData.getSoftware().replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1006");
                if(result >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
                }
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
                holder.humidityTextView.setText(bleDeviceData.getHumidity() + "%");
//                holder.devicePropTextView.setText(bleDeviceData.getDeviceProp());
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
//                holder.lightLine.setVisibility(View.GONE);
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
//                    holder.lightLine.setVisibility(View.VISIBLE);
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
//                    holder.lightLine.setVisibility(View.VISIBLE);
                    if(!bleDeviceData.getMove().equals("-")){
                        holder.moveLine.setVisibility(View.VISIBLE);
                        holder.moveDetectionLine.setVisibility(View.VISIBLE);
                        holder.stopDetectionLine.setVisibility(View.VISIBLE);
                        holder.pitchLine.setVisibility(View.VISIBLE);
                        holder.rollLine.setVisibility(View.VISIBLE);
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
                    holder.extSensorTextview = (TextView)convertView.findViewById(R.id.tx_ext_sensor_type);

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
                    holder.extSensorLine = (LinearLayout)convertView.findViewById(R.id.line_ext_sensor);
                    holder.resetPwdLL = (LinearLayout) convertView.findViewById(R.id.ll_reset_pwd);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
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
                holder.extSensorTextview.setText(bleDeviceData.getExtSensorName(MainActivity.this));
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
                holder.extSensorLine.setVisibility(View.GONE);
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                String formatSoftware = bleDeviceData.getSoftware().replace("V","").replaceAll("v","").replaceAll("\\.","");
                int result = formatSoftware.compareToIgnoreCase("1005");
                if(result >= 0){
                    holder.resetPwdLL.setVisibility(View.VISIBLE);
                }else{
                    holder.resetPwdLL.setVisibility(View.GONE);
                }
                if(bleDeviceData.getBroadcastType().equals("Eddystone UID")){
                    holder.bidLine.setVisibility(View.VISIBLE);
                    holder.nidLine.setVisibility(View.VISIBLE);
                }else  if(bleDeviceData.getBroadcastType().equals("Long range")){
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                    holder.extSensorLine.setVisibility(View.VISIBLE);


                    if(bleDeviceData.getExtSensorType() == 1 || bleDeviceData.getExtSensorType() == 2 || bleDeviceData.getExtSensorType() == 3){
                        holder.tempLine.setVisibility(View.VISIBLE);
                    }
                }else  if(bleDeviceData.getBroadcastType().equals("Beacon")){
                    holder.minorLine.setVisibility(View.VISIBLE);
                    holder.majorLine.setVisibility(View.VISIBLE);
                }else{
                    holder.batteryLine.setVisibility(View.VISIBLE);
                    holder.batteryPercentLine.setVisibility(View.VISIBLE);
                    holder.warnLine.setVisibility(View.VISIBLE);
                    holder.extSensorLine.setVisibility(View.VISIBLE);

                    if(bleDeviceData.getExtSensorType() == 1 || bleDeviceData.getExtSensorType() == 2 || bleDeviceData.getExtSensorType() == 3){
                        holder.tempLine.setVisibility(View.VISIBLE);
                    }
                }
                if(bleDeviceData.isS10SupportHumidity()){
                    holder.humidityLine.setVisibility(View.VISIBLE);
                }else{
                    holder.humidityLine.setVisibility(View.GONE);
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
                    holder.input0TextView = (TextView)convertView.findViewById(R.id.tx_input0);
                    holder.output0TextView = (TextView)convertView.findViewById(R.id.tx_output0);
                    holder.output1TextView = (TextView)convertView.findViewById(R.id.tx_output1);
                    holder.analog0TextView = (TextView)convertView.findViewById(R.id.tx_analog_input_0);
                    holder.analog1TextView = (TextView)convertView.findViewById(R.id.tx_analog_input_1);
                    holder.analog2TextView = (TextView)convertView.findViewById(R.id.tx_analog_input_2);
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
                holder.input0TextView.setText(bleDeviceData.getInput0());
                holder.output0TextView.setText(bleDeviceData.getOutput0());
                holder.output1TextView.setText(bleDeviceData.getOutput1());
                holder.analog0TextView.setText(bleDeviceData.getAnalog0());
                holder.analog1TextView.setText(bleDeviceData.getAnalog1());
                holder.analog2TextView.setText(bleDeviceData.getAnalog2());
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("A001")){
                if(null == convertView  || convertView.getTag() instanceof A001ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_a001_detail_item,null);
                    A001ViewHolder holder = new A001ViewHolder();
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

                A001ViewHolder holder = (A001ViewHolder) convertView.getTag();
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
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("A002")){
                if(null == convertView  || convertView.getTag() instanceof A002ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_a002_detail_item,null);
                    A002ViewHolder holder = new A002ViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.hardwareTextView = (TextView)convertView.findViewById(R.id.tx_hardware);
                    holder.softwareTextView = (TextView)convertView.findViewById(R.id.tx_software);
                    holder.configBtn = (Button) convertView.findViewById(R.id.btn_config);
                    holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                    holder.inputTextView = (TextView)convertView.findViewById(R.id.tx_input);
                    holder.relayTextView = (TextView)convertView.findViewById(R.id.tx_relay);
                    holder.negativeTrigger1TextView = (TextView)convertView.findViewById(R.id.tx_negative_trigger_one);
                    holder.negativeTrigger2TextView = (TextView)convertView.findViewById(R.id.tx_negative_trigger_two);
                    holder.relayOutputTextView = (TextView)convertView.findViewById(R.id.tx_relay_output);
                    holder.resetPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);
                    convertView.setTag(holder);
                }

                A002ViewHolder holder = (A002ViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.modelTextView.setText(bleDeviceData.getModelName());
                holder.hardwareTextView.setText("V" + bleDeviceData.getHardware());
                holder.softwareTextView.setText("V" + bleDeviceData.getSoftware());
                holder.batteryTextView.setText(bleDeviceData.getBattery());
                holder.inputTextView.setText((bleDeviceData.getA002InputOutputStatus() & 0x80) == 0x80 ? "ON" : "OFF");
                holder.relayTextView.setText((bleDeviceData.getA002InputOutputStatus() & 0x40) == 0x40 ? "ON" : "OFF");
                holder.negativeTrigger1TextView.setText((bleDeviceData.getA002InputOutputStatus() & 0x20) == 0x20 ? "ON" : "OFF");
                holder.negativeTrigger2TextView.setText((bleDeviceData.getA002InputOutputStatus() & 0x10) == 0x10 ? "ON" : "OFF");
                holder.relayOutputTextView.setText((bleDeviceData.getA002InputOutputStatus() & 0x08) == 0x08 ? "ON" : "OFF");
                holder.configBtn.setTag(bleDeviceData.getMac());
                holder.configBtn.setOnClickListener(configDeviceClick);
                holder.resetPwdBtn.setTag(bleDeviceData.getMac());
                holder.resetPwdBtn.setOnClickListener(resetDevicePwdClick);
                return convertView;
            }else if(bleDeviceData.getDeviceType() != null && bleDeviceData.getDeviceType().equals("dfuDevice")){
                if(null == convertView  || convertView.getTag() instanceof S09ViewHolder == false){
                    convertView = View.inflate(getApplicationContext(),R.layout.ble_dfu_error_item,null);
                    DfuErrorDeviceViewHolder holder = new DfuErrorDeviceViewHolder();
                    holder.deviceNameTextView = (TextView)convertView.findViewById(R.id.tx_name);
                    holder.idTextView = (TextView)convertView.findViewById(R.id.tx_ble_id);
                    holder.dateTextView = (TextView)convertView.findViewById(R.id.tx_date);
                    holder.rssiTextView = (TextView)convertView.findViewById(R.id.tx_rssi);
                    holder.modelTextView = (TextView)convertView.findViewById(R.id.tx_device_model);
                    holder.btnUpgrade = (Button) convertView.findViewById(R.id.btn_config);
                    convertView.setTag(holder);
                }

                DfuErrorDeviceViewHolder holder = (DfuErrorDeviceViewHolder) convertView.getTag();
                holder.deviceNameTextView.setText(bleDeviceData.getDeviceName());
                holder.idTextView.setText(bleDeviceData.getId());
                holder.dateTextView.setText(dateFormat.format(bleDeviceData.getDate()));
                holder.rssiTextView.setText(bleDeviceData.getRssi());
                holder.btnUpgrade.setTag(bleDeviceData.getMac());
                holder.modelTextView.setText("Upgrade error");
                holder.btnUpgrade.setOnClickListener(upgradeDfuErrorClick);
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
            Button switchTempBtn,configBtn,qrCodeBtn,resetPwdBtn;
            LinearLayout qrcodeLL,resetPwdLL;
            MarqueeTextView alarmTextView;
        }
        class S04ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,devicePropTextView;
            Button switchTempBtn,configBtn,qrCodeBtn,resetPwdBtn;
            LinearLayout qrcodeLL,resetPwdLL;
            MarqueeTextView alarmTextView;
        }
        class S05ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
            batteryTextView,tempTextView,devicePropTextView;
            Button switchTempBtn,configBtn,qrCodeBtn,resetPwdBtn;
            LinearLayout qrcodeLL,resetPwdLL;
            MarqueeTextView alarmTextView;
        }
        class ErrorDeviceViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView;
        }
        class DfuErrorDeviceViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView;
            Button btnUpgrade;
        }
        class S08ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,humidityTextView,doorTextView,batteryPercentTextView,broadcastTypeTextview,
                    moveTextView,moveDetectionTextView,stopDetectionTextView,pitchTextView,rollTextView,bidTextView,nidTextView,//devicePropTextView,
                    majorTextView,minorTextView;
            Button switchTempBtn,configBtn,qrCodeBtn,resetPwdBtn;
            LinearLayout qrcodeLL,resetPwdLL;
            LinearLayout doorLine,bidLine,nidLine,batteryLine,batteryPercentLine,tempLine,moveLine,moveDetectionLine,//lightLine,
                    stopDetectionLine,pitchLine,rollLine,warnLine,majorLine,minorLine;
            MarqueeTextView alarmTextView;
        }
        class S07ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,batteryPercentTextView,flagTextView,broadcastTypeTextview,bidTextView,nidTextView,majorTextView,minorTextView;
            MarqueeTextView alarmTextView;
            LinearLayout  bidLine,nidLine,majorLine,minorLine,batteryLine,batteryPercentLine,warnLine,flagLine,resetPwdLL;
            Button configBtn,resetPwdBtn;

        }
        class S09ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                input0TextView,output0TextView,output1TextView,analog0TextView,analog1TextView,analog2TextView;
            Button configBtn;
        }
        class S10ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
                    batteryTextView,tempTextView,humidityTextView,devicePropTextView,batteryPercentTextView,broadcastTypeTextview,
                    bidTextView,nidTextView, majorTextView,minorTextView,extSensorTextview;
            Button switchTempBtn,configBtn,qrCodeBtn,resetPwdBtn;
            LinearLayout bidLine,nidLine,batteryLine,batteryPercentLine,tempLine,lightLine,warnLine,majorLine,minorLine,humidityLine,extSensorLine;
            LinearLayout qrcodeLL,resetPwdLL;
            MarqueeTextView alarmTextView;
        }
        class TireViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,
                    batteryTextView,tireTextView,tempTextView;
            Button switchTempBtn,switchPressureBtn;
            MarqueeTextView statusTextView;
        }

        class A001ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView ;
            Button configBtn;
        }
        class A002ViewHolder {
            TextView deviceNameTextView,  idTextView,  dateTextView, rssiTextView,modelTextView,hardwareTextView,softwareTextView,
            batteryTextView,inputTextView,relayTextView,negativeTrigger1TextView,negativeTrigger2TextView,relayOutputTextView;
            Button configBtn,resetPwdBtn;
        }
    }
}
