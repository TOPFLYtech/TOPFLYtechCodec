package com.topflytech.lockActive;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.tencent.bugly.crashreport.CrashReport;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BluetoothPermissionHelper;
import com.topflytech.lockActive.data.CustomMenuItem;
import com.topflytech.lockActive.data.CustomPopMenu;
import com.topflytech.lockActive.data.CustomPopupMenuAdapter;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.UniqueIDTool;
import com.topflytech.lockActive.data.Utils;
import com.topflytech.lockActive.deviceConfigSetting.SingleClickListener;

import org.opencv.OpenCV;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity implements BluetoothPermissionHelper.BluetoothPermissionCallback {
    private ActionBar actionBar;
    private ImageView refreshButton;
    private ImageView rightMenuBtn;
    private ImageView favoriteBtn;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    BluetoothLeScanner mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
    BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    SweetAlertDialog waitingCancelDlg;
    private ListView mListView;
    private ItemAdapter mListAdapter;
    private LinearLayout llFuzzySearch;
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
    private boolean isFuzzySearchStatus = false, isOnView = false;
    private String fuzzyKey = "";
    public static final int REQUEST_SCAN_QRCODE = 99;
    public static final int RESPONSE_SCAN_QRCODE = 99;
    private Date dataNotifyDate;
    private Date enterDate;
    private EditText searchEditText;
    private ImageButton scanBtn;
    private ImageView btnCloseSearch;
    private Date lastRecvDate;
    ScanSettings scanSettings ;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static ArrayList<BleDeviceData> allBleDeviceDataList = new ArrayList<BleDeviceData>();
    public static ArrayList<BleDeviceData> showBleDeviceDataList = new ArrayList<BleDeviceData>();
    public ArrayList<String> favoriteMacs = new ArrayList<>();
    public boolean isShowFavorite = false;
    private ConcurrentHashMap<String, Integer> allMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private ConcurrentHashMap<String, Integer> showMacIndex = new ConcurrentHashMap<String, Integer>(32);
    private String uniqueID = "";

    private BluetoothAdapter bluetoothadapter;
    private BluetoothPermissionHelper bluetoothPermissionHelper;
//    private boolean isHadBlePermission = false;

    private void initFavoriteMacs() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("tfteclock", Context.MODE_PRIVATE);
        String favoriteMacStr = sharedPreferences.getString("favoriteMacs", null);
        favoriteMacs.clear();
        if (favoriteMacStr != null) {
            String[] imeiList = favoriteMacStr.split(";");
            for (String imei :
                    imeiList) {
                favoriteMacs.add(imei);
            }
        }
        this.isShowFavorite = sharedPreferences.getBoolean("isShowFavorite", false);
    }

    private void saveFavoriteMacs() {
        String combineStr = "";
        for (int i = 0; i < favoriteMacs.size(); i++) {
            combineStr += favoriteMacs.get(i);
            if (i != favoriteMacs.size() - 1) {
                combineStr += ";";
            }
        }
        SharedPreferences sharedPreferences = this.getSharedPreferences("tfteclock", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("favoriteMacs", combineStr);
        editor.putBoolean("isShowFavorite", isShowFavorite);
        editor.apply();
    }



    private int clickCount = 0;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化OpenCV
        OpenCV.initAsync(MainActivity.this);

        //初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(MainActivity.this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext());
        setContentView(R.layout.activity_main);
        initFavoriteMacs();
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.main_activity_bar, null);
        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount >= 10) {
                    Utils.isDebug = true;
                    // 停止点击事件
                    customView.setOnClickListener(null);
                } else if (clickCount > 6) {
                    Toast.makeText(MainActivity.this, "再点击" + (10 - clickCount) + "次，打开Debug功能", Toast.LENGTH_SHORT).show();
                }

                if (clickCount == 1) {
                    // 在 20 秒后检查点击次数
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!Utils.isDebug) {
                                // 20 秒内未达到点击次数要求
                                // 进行相应的处理逻辑
                                clickCount = 0;
                            }
                        }
                    }, 20000); // 20 秒
                }
            }
        });
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        enterDate = new Date();
        bluetoothPermissionHelper = new BluetoothPermissionHelper(this, this);
        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothadapter = bluetoothManager.getAdapter();
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
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent( MainActivity.this, ReadHisDataActivity.class);
//                    startActivity(intent);
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
//                    if(!isHadBlePermission){
//                        return;
//                    }
//                    mBluetoothAdapter.stopLeScan(startSearchCallback);
                    try {
                        mBLEScanner.stopScan(startScanCallback);
                    } catch (Exception e) {

                    }
                    allBleDeviceDataList.clear();
                    allMacIndex.clear();
                    showBleDeviceDataList.clear();
                    showMacIndex.clear();
                    mListAdapter.notifyDataSetChanged();
                    if (bluetoothadapter.isEnabled()) {
//                        if(!isHadBlePermission){
//                            return;
//                        }
//                        mBluetoothAdapter.startLeScan(startSearchCallback);
                        try {
                            mBLEScanner.startScan(null, scanSettings,startScanCallback);
//                            bluetoothLeScanner.startScan(null, scanSettings,startScanCallback);
                        } catch (Exception e) {

                        }
                        showWaitingCancelDlg("");
                    } else {
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        MainActivity.this.startActivity(enabler);
                    }
                }
            });
            searchEditText = (EditText) findViewById(R.id.et_fuzzy_search);
            llFuzzySearch = (LinearLayout) findViewById(R.id.ll_fuzzy_search);
            btnCloseSearch = (ImageView) findViewById(R.id.btn_close_search);
            btnCloseSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isFuzzySearchStatus) {
                        llFuzzySearch.setVisibility(View.VISIBLE);
                    } else {
                        fuzzyKey = "";
                        searchEditText.setText("");
                        refreshShowItem();
                        llFuzzySearch.setVisibility(View.GONE);
                    }
                    isFuzzySearchStatus = !isFuzzySearchStatus;
                }
            });
            scanBtn = (ImageButton) findViewById(R.id.btn_scan);
            scanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, WeChatQRCodeActivity.class);
                    startActivityForResult(intent, REQUEST_SCAN_QRCODE);
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
                    if (fuzzyKey.length() == 15) {
                        for (BleDeviceData bleDeviceData : allBleDeviceDataList) {
                            if (bleDeviceData.getImei().equals(fuzzyKey)) {
                                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                                intent.putExtra("mac", bleDeviceData.getMac());
                                intent.putExtra("deviceName", bleDeviceData.getDeviceName());
                                intent.putExtra("id", bleDeviceData.getId());
                                intent.putExtra("model", bleDeviceData.getModel());
                                intent.putExtra("software", bleDeviceData.getSoftware());
                                intent.putExtra("deviceId",bleDeviceData.getDeviceId());
                                intent.putExtra("imei",bleDeviceData.getImei());
                                startActivity(intent);
                                break;
                            }
                        }
                    }
                }
            });
            favoriteBtn = (ImageView) findViewById(R.id.main_bar_favorite);
            favoriteBtn.setImageResource(isShowFavorite ? R.mipmap.ic_show_favorite : R.mipmap.ic_hide_favorite);
            favoriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isShowFavorite = !isShowFavorite;
                    saveFavoriteMacs();
                    favoriteBtn.setImageResource(isShowFavorite ? R.mipmap.ic_show_favorite : R.mipmap.ic_hide_favorite);
                    showBleDeviceDataList.clear();
                    showMacIndex.clear();
                    updateAllShowItem();
                }
            });
            rightMenuBtn = (ImageView) findViewById(R.id.main_right_menu);
            rightMenuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCustomPopupMenu(view);
                }
            });
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
        Log.e("BluetoothUtils", "my UUID:" + uniqueID);
        LinearLayout rootView = findViewById(R.id.root_view);

        // 添加点击事件监听器
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });
        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                .build();

    }


    private void showCustomPopupMenu(View anchorView) {
        // Create a list of menu items
        List<CustomMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new CustomMenuItem(getString(R.string.search), R.mipmap.ic_search));
        menuItems.add(new CustomMenuItem(getString(R.string.about), R.mipmap.ic_info));
        CustomPopupMenuAdapter.OnMenuItemClickListener itemClickListener = new CustomPopupMenuAdapter.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(CustomMenuItem menuItem) {

                if (menuItem.getIconResId() == R.mipmap.ic_search) {
                    if (!isFuzzySearchStatus) {
                        llFuzzySearch.setVisibility(View.VISIBLE);
                    } else {
                        fuzzyKey = "";
                        searchEditText.setText("");
                        refreshShowItem();
                        llFuzzySearch.setVisibility(View.GONE);
                    }
                    isFuzzySearchStatus = !isFuzzySearchStatus;
                } else if (menuItem.getIconResId() == R.mipmap.ic_info) {
                    try {
                        PackageManager packageManager = MainActivity.this.getPackageManager();
                        PackageInfo packageInfo = packageManager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                        Toast.makeText(MainActivity.this, "V" + packageInfo.versionName, Toast.LENGTH_SHORT).show();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();

                    }
                }
            }

        };
        CustomPopMenu customPopMenu = new CustomPopMenu(MainActivity.this, itemClickListener);
        customPopMenu.show(anchorView, menuItems);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothPermissionHelper.startCheckBlePermission(MainActivity.this);
        initParentSubRelationMap();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_QRCODE && resultCode == RESPONSE_SCAN_QRCODE) {
            String value = data.getStringExtra("value");
            searchEditText.setText(value);
        }
        bluetoothPermissionHelper.onActivityResult(requestCode,resultCode);
    }

    private void startBluetoothOperations() {

        // 这里放置蓝牙扫描和连接的代码
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
//        if(!isHadBlePermission){
//            return;
//        }
//        mBluetoothAdapter.startLeScan(startSearchCallback);
        try {
            mBLEScanner.startScan(null, scanSettings,startScanCallback);
//            bluetoothLeScanner.startScan(null, scanSettings,startScanCallback);
        } catch (Exception e) {

        }
        showWaitingCancelDlg("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        bluetoothPermissionHelper.onRequestPermissionsResult(requestCode,grantResults);

    }


    public void reStartApp() {
        try {
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(MainActivity.this.getBaseContext().getPackageName());
            intent.putExtra("REBOOT", "reboot");
            PendingIntent restartIntent = PendingIntent.getActivity(MainActivity.this.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void refreshShowItem() {
        showBleDeviceDataList.clear();
        showMacIndex.clear();
        updateAllShowItem();
        mListAdapter.notifyDataSetChanged();
    }
    int checkSignalTimeoutCount = 0;
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
            checkSignalTimeoutCount++;
            Date now = new Date();
            if(checkSignalTimeoutCount % 3 == 0){
                int len = allBleDeviceDataList.size();
                boolean hadTimeout = false;
                for(int i = 0; i < len;i++){
                    if(allBleDeviceDataList.size() > i + 1){
                        BleDeviceData item = allBleDeviceDataList.get(i);
                        if(now.getTime() - item.getDate().getTime() > 30000 && now.getTime() - item.getDate().getTime() < 36000){
                            hadTimeout = true;
                            break;
                        }
                    }
                }
                if(hadTimeout){
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           mListAdapter.notifyDataSetChanged();
                       }
                   });
                }
            }
//            Log.e("BluetoothUtils","checkBleSearchStatus");

            if (now.getTime() - checkDate.getTime() > 45000) {
                //need restart ble search
                Log.e("BluetoothUtils", "restart search");
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                }
//                if(!isHadBlePermission){
//                    return;
//                }
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
                try {
                    mBLEScanner.startScan(null, scanSettings,startScanCallback);
//                    bluetoothLeScanner.startScan(null, scanSettings,startScanCallback);
                } catch (Exception e) {

                }
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
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
//            if(!isHadBlePermission){
//                return;
//            }
//            mBluetoothAdapter.startLeScan(startSearchCallback);
            try {
                mBLEScanner.startScan(null, scanSettings,startScanCallback);
//                bluetoothLeScanner.startScan(null, scanSettings,startScanCallback);
            } catch (Exception e) {

            }
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
        if(isShowFavorite){
            if(showMacIndex.get(bleDeviceData.getId()) != null && showBleDeviceDataList.size() > 0){
                showBleDeviceDataList.set(showMacIndex.get(bleDeviceData.getId()),bleDeviceData);
            }else{
                if (fuzzyKey == null || fuzzyKey.isEmpty()){
                    if(bleDeviceData.getImei() != null && favoriteMacs.contains(bleDeviceData.getId()) && !showMacIndex.containsKey(bleDeviceData.getId())){
                        showBleDeviceDataList.add(bleDeviceData);
                        showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
                    }
                }else{
                    if((bleDeviceData.getImei() != null && bleDeviceData.getImei().contains(fuzzyKey.toUpperCase()))
                            || (bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                            || (bleDeviceData.getDeviceName()!= null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
                        showBleDeviceDataList.add(bleDeviceData);
                        showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
                    }
                }
            }
        }else{
            if(showMacIndex.get(bleDeviceData.getId()) != null && showBleDeviceDataList.size() > 0){
                showBleDeviceDataList.set(showMacIndex.get(bleDeviceData.getId()),bleDeviceData);
            }else{
                if (fuzzyKey == null || fuzzyKey.isEmpty()){
                    updateParentSubRelationPosition(bleDeviceData);
                }else{
                    if((bleDeviceData.getImei() != null && bleDeviceData.getImei().contains(fuzzyKey.toUpperCase()))
                            || (bleDeviceData.getId() != null && bleDeviceData.getId().contains(fuzzyKey.toUpperCase()))
                            || (bleDeviceData.getDeviceName()!= null && bleDeviceData.getDeviceName().toUpperCase().contains(fuzzyKey.toUpperCase()))){
                        showBleDeviceDataList.add(bleDeviceData);
                        showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
                    }
                }
            }
        }

    }

    private HashMap<String,List<String>> bigLockSubLockMap = new HashMap<>();
    private HashMap<String,List<String>> subLockParentLockMap = new HashMap<>();

    private void initParentSubRelationMap(){
        bigLockSubLockMap.clear();
        subLockParentLockMap.clear();
        List<String> parentList = BleDeviceData.getHadSubLockImeis(MainActivity.this);
        for(String parent : parentList){
            List<String> subList = BleDeviceData.getSubLockBindMap(MainActivity.this,parent);
            if(subList != null && subList.size() > 0){
                bigLockSubLockMap.put(parent,subList);
                for(String sub : subList){
                    if(subLockParentLockMap.get(sub) == null){
                        subLockParentLockMap.put(sub,new ArrayList<>());
                    }
                    subLockParentLockMap.get(sub).add(parent);
                }
            }
        }
    }

    private void updateParentSubRelationPosition(BleDeviceData bleDeviceData){
        if(bleDeviceData.isSubLock()){
            String mac = bleDeviceData.getId() ;
            if(subLockParentLockMap.get(mac.toUpperCase()) != null || subLockParentLockMap.get(mac.toLowerCase()) != null){
                List<String> parentList = subLockParentLockMap.get(mac.toUpperCase());
                if(parentList == null){
                    parentList = subLockParentLockMap.get(mac.toLowerCase());
                }
                if(parentList.size() > 1){
                    showBleDeviceDataList.add(bleDeviceData);
                    showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
                }else{
                    String parentImei = parentList.get(0);
                    boolean isFindParent = false;
                    for(int i = 0;i < showBleDeviceDataList.size();i++){
                        if(showBleDeviceDataList.get(i).getImei().equals(parentImei)){
                            showBleDeviceDataList.add(i+1,bleDeviceData);
                            isFindParent = true;
                            break;
                        }
                    }
                    if(!isFindParent){
                        showBleDeviceDataList.add(bleDeviceData);
                    }
                    showMacIndex.clear();
                    for(int i = 0;i < showBleDeviceDataList.size();i++){
                        showMacIndex.put(showBleDeviceDataList.get(i).getId(), i);
                    }
                }
            }else{
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
            }
        }else{
            if(bigLockSubLockMap.get(bleDeviceData.getImei()) != null){
                List<String> subList = bigLockSubLockMap.get(bleDeviceData.getImei());
                List<BleDeviceData> subLockItems = new ArrayList<>();
                for(int i = 0;i < showBleDeviceDataList.size();i++){
                    BleDeviceData item = showBleDeviceDataList.get(i);
                    if(subList.contains(item.getId().replaceAll(":","").toUpperCase())
                            || subList.contains(item.getId().replaceAll(":","").toLowerCase())){
                        subLockItems.add(item);
                    }
                }
                if(subLockItems.size() > 0){
                    for (BleDeviceData item :
                            subLockItems) {
                        showBleDeviceDataList.remove(item);
                    }
                }
                showBleDeviceDataList.add(bleDeviceData);
                for (BleDeviceData item :
                        subLockItems) {
                    showBleDeviceDataList.add(item);
                }
                showMacIndex.clear();
                for(int i = 0;i < showBleDeviceDataList.size();i++){
                    showMacIndex.put(showBleDeviceDataList.get(i).getId(), i);
                }
            }else{
                showBleDeviceDataList.add(bleDeviceData);
                showMacIndex.put(bleDeviceData.getId(), showBleDeviceDataList.size() - 1);
            }
        }


    }

    private void updateAllShowItem(){
        for(BleDeviceData bleDeviceData : allBleDeviceDataList){
            updateShowItem(bleDeviceData);
        }
    }
    private ScanCallback startScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try{
                super.onScanResult(callbackType, result);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                }
                BluetoothDevice device = result.getDevice();

                int rssi = result.getRssi();
                byte[] scanRecord = result.getScanRecord().getBytes();
                String mac = device.getAddress();
                String id = mac.replaceAll(":","").toUpperCase();
                lastRecvDate = new Date();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

                }
//                if(!mac.contains("15:ED:6C") ){
////                    Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + mac + " " + MyByteUtils.bytes2HexString(scanRecord,0));
//                    return;
//                }
//                Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + mac + " " + MyByteUtils.bytes2HexString(scanRecord,0));

                HashMap<Byte,byte[]> rawDataList = BleDeviceData.parseRawData(scanRecord);
                if (rawDataList.containsKey((byte)0xfe) || rawDataList.containsKey((byte)0xfd)){


//                Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " + device.getAddress() + " " + MyByteUtils.bytes2HexString(scanRecord,0));
                    byte[] versionInfo = rawDataList.get((byte)0xfe);
                    byte[] imeiInfo = rawDataList.get((byte)0xfd);
                    byte[] deviceIdByte = rawDataList.get((byte)0xae);
                    String imei = null;
                    if (imeiInfo != null){
                        byte[] imeiByte = Arrays.copyOfRange(imeiInfo,3,imeiInfo.length);
                        imei = new String(imeiByte);
                    }
                    String software = null;
                    String hardware = null;
                    String model = null;
                    String voltageStr = null;
                    boolean isSupportReadHis = true;
                    byte protocolByte = 0x00;
                    if (versionInfo != null && versionInfo.length >= 7) {
                        protocolByte = versionInfo[2];
                    }
                    if (protocolByte != (byte)0x62 && protocolByte != (byte)0x65
                            &&  protocolByte != (byte)0x7b && protocolByte != (byte)0x7c
                            &&  protocolByte != 119 && protocolByte != 120
                            &&  protocolByte != 121 && protocolByte != 122) {
                        return;
                    }
                    if (versionInfo != null && versionInfo.length >= 7){
                        protocolByte = versionInfo[2];
                        String versionStr = MyByteUtils.bytes2HexString(versionInfo,3);
                        hardware = String.format("V%s.%s",versionStr.substring(0,1),versionStr.substring(1,2));
                        try {
                            software =  String.format("V%d",Integer.valueOf(versionStr.substring(2,6)));
                        }catch (Exception e){
                            software = "";
                            Log.e("error software",imei);
                            e.printStackTrace();
                        }
                        if(versionInfo.length == 9){
                            voltageStr = String.valueOf(Double.valueOf(String.format("%d.%d",(int)versionInfo[6],(int)versionInfo[7])) / 10);
                            isSupportReadHis = versionInfo[8] == 0x01;
                        }else{
                            if(versionInfo.length == 7){
                                voltageStr = String.format("%s.%s",versionStr.substring(6,7),versionStr.substring(7,8));
                            }else{
                                voltageStr = String.valueOf(Double.valueOf(String.format("%d.%d",(int)versionInfo[6],(int)versionInfo[7])) / 10);
                            }
                        }


                        model = BleDeviceData.parseModel(protocolByte);
                    }
                    String deviceId = null;
                    if (deviceIdByte != null && deviceIdByte.length >= 7){
                        deviceId = MyByteUtils.bytes2HexString(Arrays.copyOfRange(deviceIdByte,2,7),0);
                    }
                    Integer index = allMacIndex.get(id);
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
                        if(deviceId != null){
                            bleDeviceData.setDeviceId(deviceId);
                        }
                        bleDeviceData.setDeviceName(device.getName());
                        bleDeviceData.setSupportReadHis(isSupportReadHis);
//                        updateShowItem(bleDeviceData);
                    }else{
                        BleDeviceData bleDeviceData = new BleDeviceData();
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setMac(mac);
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
                        if(deviceId != null){
                            bleDeviceData.setDeviceId(deviceId);
                        }
                        bleDeviceData.setDeviceName(device.getName());
                        bleDeviceData.setSupportReadHis(isSupportReadHis);
                        allBleDeviceDataList.add(bleDeviceData);
                        index = allBleDeviceDataList.size() - 1;
                        allMacIndex.put(id,index);
                    }
                    if(dataNotifyDate == null || lastRecvDate.getTime() - dataNotifyDate.getTime() > 1000){

                        updateAllShowItem();
                        dataNotifyDate = lastRecvDate;
                        mListAdapter.notifyDataSetChanged();
                    }
                    if(waitingCancelDlg != null && waitingCancelDlg.isShowing()){
                        waitingCancelDlg.hide();
                    }
                }else if(rawDataList.containsKey((byte)0xaa)){
                    //sub lock
                    byte[] dataInfo = rawDataList.get((byte)0xaa);
                    if(dataInfo[2] != 0x0b){
                        return;
                    }
                    if(device.getName() != null && device.getName().contains("321")){
                        Log.e("BluetoothUtils","MainActivity.onDeviceFounded " + device.getName() + "  " +
                                mac + " " + MyByteUtils.bytes2HexString(scanRecord,0)
                                + " " + MyByteUtils.bytes2HexString(dataInfo,0));
                    }
                    String hardware = MyByteUtils.parseHardwareVersion(dataInfo[3]);
                    String software = MyByteUtils.parseSoftwareVersion(dataInfo,4);
                    byte lockType = dataInfo[7];
                    int lockStatus = MyByteUtils.bytes2Short(dataInfo,8);
                    if ((lockType & 0xff) == 0xff){
                        return;
                    }
                    int voltageTemp = MyByteUtils.bytes2Short(dataInfo,10);
                    float voltage = voltageTemp / 1000.0f;
                    int batteryPercent = (int)dataInfo[12];
                    if(batteryPercent < 0){
                        batteryPercent += 256;
                    }
                    int solarVoltageTemp = MyByteUtils.bytes2Short(dataInfo,13);
                    float solarVoltage = solarVoltageTemp / 1000.0f;
                    int temp = dataInfo[15] & 0xff;
                    if(dataInfo[15] == 0xff){
                        temp = -999;
                    }else{
                        if(temp < 0){
                            temp += 256;
                        }
                        temp = temp - 80;
                    }
                    String deviceId = MyByteUtils.bytes2HexString(dataInfo,16);
                    Integer index = allMacIndex.get(id);
                    if(index != null && allBleDeviceDataList.size() > index){
                        BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setId(id);
                        bleDeviceData.parseSubLockStatus(lockStatus);
                        bleDeviceData.setBatteryPercent(batteryPercent);
                        bleDeviceData.setTemp((float)temp);
                        bleDeviceData.setSolarVoltage(solarVoltage);
                        bleDeviceData.setVoltage(voltage);
                        bleDeviceData.setImei(deviceId);
                        bleDeviceData.setSubLock(true);
                        bleDeviceData.setLockType(lockType);
                        bleDeviceData.setModel(BleDeviceData.MODEL_OF_SGX120B01);
                        if (hardware != null){
                            bleDeviceData.setHardware(hardware);
                        }
                        if (software != null){
                            bleDeviceData.setSoftware(software);
                        }
                        if(deviceId != null){
                            bleDeviceData.setDeviceId(deviceId);
                        }
                        bleDeviceData.setDeviceName(device.getName());
//                        updateShowItem(bleDeviceData);
                    }else{
                        BleDeviceData bleDeviceData = new BleDeviceData();
                        bleDeviceData.setRssi(rssi + "dBm");
                        bleDeviceData.setSrcData(scanRecord);
                        bleDeviceData.setDate(new Date());
                        bleDeviceData.setMac(mac);
                        bleDeviceData.setId(id);
                        bleDeviceData.setBatteryPercent(batteryPercent);
                        bleDeviceData.parseSubLockStatus(lockStatus);
                        bleDeviceData.setSolarVoltage(solarVoltage);
                        bleDeviceData.setVoltage(voltage);
                        bleDeviceData.setTemp((float)temp);
                        bleDeviceData.setImei(deviceId);
                        bleDeviceData.setSubLock(true);
                        bleDeviceData.setLockType(lockType);
                        bleDeviceData.setModel(BleDeviceData.MODEL_OF_SGX120B01);
                        if (hardware != null){
                            bleDeviceData.setHardware(hardware);
                        }
                        if (software != null){
                            bleDeviceData.setSoftware(software);
                        }
                        if(deviceId != null){
                            bleDeviceData.setDeviceId(deviceId);
                        }
                        bleDeviceData.setDeviceName(device.getName());
                        bleDeviceData.setSupportReadHis(false);
                        allBleDeviceDataList.add(bleDeviceData);
                        index = allBleDeviceDataList.size() - 1;
                        allMacIndex.put(id,index);
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
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };
    SingleClickListener unlockDeviceClick = new SingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
//            if(!isHadBlePermission){
//                return;
//            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
            try {
                mBLEScanner.stopScan(startScanCallback);
            } catch (Exception e) {

            }
            final String id = (String)v.getTag();
            Log.e("Click", "config device " + id);
            Integer index = allMacIndex.get(id);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                Intent intent = new Intent( MainActivity.this, UnlockActivity.class);
                intent.putExtra("mac",bleDeviceData.getMac());
                intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                intent.putExtra("id",bleDeviceData.getId());
                intent.putExtra("model",bleDeviceData.getModel());
                intent.putExtra("software",bleDeviceData.getSoftware());
                intent.putExtra("deviceId",bleDeviceData.getDeviceId());
                intent.putExtra("imei",bleDeviceData.getImei());
                startActivity(intent);
            }
        }
    };
    SingleClickListener connectDeviceClick = new SingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
//            if(!isHadBlePermission){
//                return;
//            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
            try {
                mBLEScanner.stopScan(startScanCallback);
            } catch (Exception e) {

            }
            final String id = (String)v.getTag();
            Log.e("Click", "config device " + id);
            Integer index = allMacIndex.get(id);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                Intent intent = new Intent( MainActivity.this, EditActivity.class);
                intent.putExtra("mac",bleDeviceData.getMac());
                intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                intent.putExtra("id",bleDeviceData.getId());
                intent.putExtra("model",bleDeviceData.getModel());
                intent.putExtra("software",bleDeviceData.getSoftware());
                intent.putExtra("deviceId",bleDeviceData.getDeviceId());
                intent.putExtra("imei",bleDeviceData.getImei());
                startActivity(intent);
            }
        }
    };
    SingleClickListener resetDeviceClick = new SingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
//            if(!isHadBlePermission){
//                return;
//            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
            try {
                mBLEScanner.stopScan(startScanCallback);
            } catch (Exception e) {

            }
            final String id = (String)v.getTag();
            Log.e("Click", "config device " + id);
            Integer index = allMacIndex.get(id);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                Intent intent = new Intent( MainActivity.this, SuperPwdResetActivity.class);
                intent.putExtra("mac",bleDeviceData.getMac());
                intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                intent.putExtra("id",bleDeviceData.getId());
                intent.putExtra("model",bleDeviceData.getModel());
                intent.putExtra("software",bleDeviceData.getSoftware());
                intent.putExtra("deviceId",bleDeviceData.getDeviceId());
                intent.putExtra("imei",bleDeviceData.getImei());
                startActivity(intent);
            }
        }
    };


    SingleClickListener readHisDataClick = new SingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            }
//            if(!isHadBlePermission){
//                return;
//            }
//            mBluetoothAdapter.stopLeScan(startSearchCallback);
            try {
                mBLEScanner.stopScan(startScanCallback);
            } catch (Exception e) {

            }
            final String id = (String)v.getTag();
            Log.e("Click", "config device " + id);
            Integer index = allMacIndex.get(id);
            if (index == null || allBleDeviceDataList.size() <= 0){
                return;
            }
            BleDeviceData bleDeviceData = allBleDeviceDataList.get(index);
            if(bleDeviceData != null){
                if(bleDeviceData.isSubLock()){
                    Intent intent = new Intent( MainActivity.this, ReadSubLockHisDataActivity.class);
                    intent.putExtra("mac",bleDeviceData.getMac());
                    intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                    intent.putExtra("id",bleDeviceData.getId());
                    startActivity(intent);
                }else{
                    Intent intent = new Intent( MainActivity.this, ReadHisDataActivity.class);
                    intent.putExtra("mac",bleDeviceData.getMac());
                    intent.putExtra("deviceName",bleDeviceData.getDeviceName());
                    intent.putExtra("id",bleDeviceData.getId());
                    startActivity(intent);
                }
            }
        }
    };



    @Override
    public void onPermissionsGranted() {
//        isHadBlePermission = true;
        startBluetoothOperations();
    }

    @Override
    public void onLocationStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

        }
//        isHadBlePermission = true;
//        if(!isHadBlePermission){
//            return;
//        }
//        mBluetoothAdapter.stopLeScan(startSearchCallback);
        try {
            mBLEScanner.stopScan(startScanCallback);
        } catch (Exception e) {

        }
//        mBluetoothAdapter.startLeScan(startSearchCallback);
        try {
            mBLEScanner.startScan(null, scanSettings,startScanCallback);
//            bluetoothLeScanner.startScan(null, scanSettings,startScanCallback);
        } catch (Exception e) {

        }
    }

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
                convertView = View.inflate(getApplicationContext(),R.layout.ble_simple_detail_item,null);
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
                holder.readDataBtn = (Button) convertView.findViewById(R.id.btn_read_his_data);
                holder.deviceIdTextView = (TextView)convertView.findViewById(R.id.tx_device_id);
                holder.batteryTextView = (TextView)convertView.findViewById(R.id.tx_battery);
                holder.deviceIdLL = (LinearLayout)convertView.findViewById(R.id.ll_device_id);
                holder.readDataLL = (LinearLayout)convertView.findViewById(R.id.read_data_ll);
                holder.detailLL =  (LinearLayout)convertView.findViewById(R.id.detail_panel);
                holder.signalImg = (ImageView)convertView.findViewById(R.id.img_device_signal);
                holder.lockImg = (ImageView)convertView.findViewById(R.id.img_item_lock);
                holder.mainViewLL =  (LinearLayout)convertView.findViewById(R.id.main_view);
                holder.favoriteImgBtn = (ImageView)convertView.findViewById(R.id.img_item_favorite);
                holder.switchTempBtn = (Button)convertView.findViewById(R.id.btn_temp_sw);
                holder.solarVoltageLL =  (LinearLayout)convertView.findViewById(R.id.ll_solar_voltage);
                holder.tempLL =  (LinearLayout)convertView.findViewById(R.id.ll_temp);
                holder.solarVoltageTextView = (TextView)convertView.findViewById(R.id.tx_solar_voltage);
                holder.alarmLL = (LinearLayout)convertView.findViewById(R.id.ll_alarm);
                holder.alarmTextView = (TextView)convertView.findViewById(R.id.tx_alarm);
                holder.tempTextView = (TextView)convertView.findViewById(R.id.tx_temp);
                holder.imeiHeadTextView = (TextView)convertView.findViewById(R.id.tx_imei_head);
                holder.idHeadTextView = (TextView)convertView.findViewById(R.id.tx_device_id_head);
                holder.batteryVoltageTextView = (TextView)convertView.findViewById(R.id.tx_battery_voltage);
                holder.batteryVoltageLL = (LinearLayout) convertView.findViewById(R.id.ll_battery_voltage);
                holder.connectLL = (LinearLayout)convertView.findViewById(R.id.ll_connect);
                holder.connectBtn = (Button) convertView.findViewById(R.id.btn_connect);


                holder.parentLockLL =  (LinearLayout)convertView.findViewById(R.id.ll_parent_lock);
                holder.parentLockTextView = (TextView)convertView.findViewById(R.id.tx_parent_imeis);
                holder.parentLockExtendTextview = (TextView)convertView.findViewById(R.id.tx_parent_imeis_extend);
                holder.superPwdLL =  (LinearLayout)convertView.findViewById(R.id.ll_super_pwd);
                holder.superPwdBtn = (Button)convertView.findViewById(R.id.btn_reset_pwd);

                holder.parentLockWarningImg = (ImageView)convertView.findViewById(R.id.iv_parent_lock_hint);
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
            holder.configBtn.setTag(bleDeviceData.getId());
            holder.configBtn.setOnClickListener(unlockDeviceClick);
            holder.configBtn.setText(getString(R.string.unlock));
            holder.readDataBtn.setTag(bleDeviceData.getId());
            holder.readDataBtn.setOnClickListener(readHisDataClick);
            holder.connectLL.setVisibility(View.GONE);
            if(BleDeviceData.isSupportReadHistory(bleDeviceData)){
                holder.readDataLL.setVisibility(View.VISIBLE);
            }else{
                holder.readDataLL.setVisibility(View.GONE);
            }
            if(bleDeviceData.getDeviceId() != null  ){
                if(BleDeviceData.isSubLockDevice(bleDeviceData.getModel()) ){
                    if(bleDeviceData.isSubLockDeviceIdValidAndNoneZero()){
                        holder.deviceIdTextView.setText(bleDeviceData.getDeviceId());
                        holder.deviceIdLL.setVisibility(View.VISIBLE);
                    }else{
                        holder.deviceIdLL.setVisibility(View.GONE);
                    }
                }else{
                    if(BleDeviceData.isParentLockDeviceIdValid(bleDeviceData.getDeviceId())){
                        holder.deviceIdTextView.setText(bleDeviceData.getDeviceId());
                        holder.deviceIdLL.setVisibility(View.VISIBLE);
                    }else{
                        holder.deviceIdLL.setVisibility(View.GONE);
                    }
                }
            }else{
                holder.deviceIdLL.setVisibility(View.GONE);
            }
            if( BleDeviceData.isSupportConfig(bleDeviceData.getModel(),bleDeviceData.getSoftware(),bleDeviceData.getDeviceId())){
                holder.connectLL.setVisibility(View.VISIBLE);
            }else{
                holder.connectLL.setVisibility(View.GONE);
            }
            if(BleDeviceData.isSubLockDevice(bleDeviceData.getModel())){
                holder.tempTextView.setText(BleDeviceData.getCurTemp(MainActivity.this,bleDeviceData.getTemp()) + BleDeviceData.getCurTempUnit(MainActivity.this));
                holder.solarVoltageTextView.setText(String.valueOf(bleDeviceData.getSolarVoltage()) +" V");
                holder.batteryVoltageTextView.setText(String.valueOf(bleDeviceData.getVoltage()) +" V");
                holder.tempLL.setVisibility(View.VISIBLE);
                holder.solarVoltageLL.setVisibility(View.VISIBLE);
                holder.batteryVoltageLL.setVisibility(View.VISIBLE);
                holder.connectLL.setVisibility(View.VISIBLE);
                holder.alarmLL.setVisibility(View.VISIBLE);
                holder.batteryTextView.setText(String.valueOf(bleDeviceData.getBatteryPercent())+"%");
                holder.imeiHeadTextView.setText("MAC:");
                holder.alarmTextView.setText(bleDeviceData.getAlarm(MainActivity.this));
                holder.alarmTextView.setSelected(true);
                holder.imeiTextView.setText(bleDeviceData.getId());
                holder.parentLockLL.setVisibility(View.VISIBLE);
                holder.superPwdLL.setVisibility(View.VISIBLE);
                String mac = bleDeviceData.getId();
                if(subLockParentLockMap.get(mac.toUpperCase()) != null || subLockParentLockMap.get(mac.toLowerCase()) != null) {
                    List<String> parentList = subLockParentLockMap.get(mac.toUpperCase());
                    if (parentList == null) {
                        parentList = subLockParentLockMap.get(mac.toLowerCase());
                    }
                    if (parentList != null) {
                        if (parentList.size() > 1) {
                            holder.parentLockExtendTextview.setVisibility(View.VISIBLE);
                        } else {
                            holder.parentLockExtendTextview.setVisibility(View.GONE);
                        }
                    }
                    String parentLockStr = TextUtils.join(";", parentList);
                    holder.parentLockExtendTextview.setTag(parentLockStr);
                    holder.parentLockExtendTextview.setOnClickListener(new SingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            String tag = (String)v.getTag();
                            SweetAlertDialog alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                            alertDialog.setTitleText(getString(R.string.parent_lock));
                            alertDialog.setContentText(tag);
                            alertDialog.setCancelable(true);
                            alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                    });
                    holder.parentLockTextView.setText(parentList.get(0));
                }else{
                    holder.parentLockTextView.setText("");
                }
            }else{
                holder.imeiHeadTextView.setText("IMEI:");
                holder.tempLL.setVisibility(View.GONE);
                holder.solarVoltageLL.setVisibility(View.GONE);
                holder.alarmLL.setVisibility(View.GONE);
                holder.batteryVoltageLL.setVisibility(View.GONE);
                holder.parentLockLL.setVisibility(View.GONE);
                holder.superPwdLL.setVisibility(View.GONE);
                holder.batteryTextView.setText(BleDeviceData.getBatteryPercent(bleDeviceData.getVoltage()));
            }


            if(bleDeviceData.isExpand()){
                holder.detailLL.setVisibility(View.VISIBLE);
            }else{
                holder.detailLL.setVisibility(View.GONE);
            }
            Date now = new Date();
            if(now.getTime() - bleDeviceData.getDate().getTime() > 30000){
                holder.signalImg.setImageResource(R.mipmap.ic_no_signal);
            }else{
                holder.signalImg.setImageResource(R.mipmap.ic_full_signal);
            }
            if(bleDeviceData.isOpenBackCover()){
                holder.lockImg.setImageResource(R.mipmap.ic_openbox);
            }else{
                holder.lockImg.setImageResource(R.mipmap.ic_main_lock);
            }
            if(favoriteMacs.contains(bleDeviceData.getId())){
                holder.favoriteImgBtn.setImageResource(R.mipmap.ic_show_favorite);
            }else{
                holder.favoriteImgBtn.setImageResource(R.mipmap.ic_hide_favorite);
            }
            holder.favoriteImgBtn.setTag(bleDeviceData.getId());
            holder.parentLockWarningImg.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    SweetAlertDialog alertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE);
                    alertDialog.setTitleText(getString(R.string.warning));
                    alertDialog.setContentText(getString(R.string.sub_lock_relation_dependency));
                    alertDialog.setCancelable(true);
                    alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });
            holder.connectBtn.setTag(bleDeviceData.getId());
            holder.connectBtn.setOnClickListener(connectDeviceClick);
            holder.superPwdBtn.setTag(bleDeviceData.getId());
            holder.superPwdBtn.setOnClickListener(resetDeviceClick);
            holder.favoriteImgBtn.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    String mac = (String)v.getTag();
                    if(favoriteMacs.contains(mac)){
                        favoriteMacs.remove(mac);
                        showBleDeviceDataList.clear();
                        showMacIndex.clear();
                        updateAllShowItem();
                    }else{
                        favoriteMacs.add(mac);
                    }
                    saveFavoriteMacs();
                    mListAdapter.notifyDataSetChanged();
                }

            });
            holder.switchTempBtn.setText(BleDeviceData.getNextTempUnit(MainActivity.this));
            holder.switchTempBtn.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Log.e("Click", "switch temp");
                    BleDeviceData.switchCurTempUnit(MainActivity.this);
                    mListAdapter.notifyDataSetChanged();
                }
            });
            holder.mainViewLL.setTag(bleDeviceData.getId());
            holder.mainViewLL.setOnClickListener(new SingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    final String id = (String)v.getTag();
                    Log.e("Click", "expand device " + id);
                    Integer index = allMacIndex.get(id);
                    if (index == null || allBleDeviceDataList.size() <= 0){
                        return;
                    }
                    BleDeviceData item = allBleDeviceDataList.get(index);
                    item.setExpand(!item.isExpand());
                    mListAdapter.notifyDataSetChanged();
                }

            });
            return convertView;

        }
        class DeviceViewHolder {
            TextView deviceNameTextView,  idTextView, dateTextView, rssiTextView , imeiTextView , softwareTextView , hardwareTextView , modelTextView,
                deviceIdTextView,batteryTextView,solarVoltageTextView,batteryVoltageTextView,tempTextView,imeiHeadTextView,idHeadTextView,alarmTextView,
                parentLockTextView,parentLockExtendTextview;
            Button configBtn,readDataBtn,switchTempBtn,superPwdBtn,connectBtn;
            LinearLayout readDataLL,deviceIdLL,detailLL,mainViewLL,solarVoltageLL,tempLL,alarmLL,batteryVoltageLL,parentLockLL,superPwdLL,connectLL;
            ImageView signalImg,favoriteImgBtn,lockImg,parentLockWarningImg;
        }
    }
}