package com.topflytech.lockActive;


import android.Manifest;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bin.david.form.data.table.PageTableData;
import com.topflytech.lockActive.Ble.BleStatusCallback;
import com.topflytech.lockActive.Ble.TftBleConnectManager;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BleDeviceData;
import com.topflytech.lockActive.data.BleRespData;
import com.topflytech.lockActive.data.MyByteUtils;
import com.topflytech.lockActive.data.TopflytechByteBuf;
import com.topflytech.lockActive.data.Utils;
import com.topflytech.lockActive.deviceConfigSetting.RFIDActivity;
import com.topflytech.lockActive.popmenu.DropPopMenu;
import com.topflytech.lockActive.popmenu.MenuItem;
import com.topflytech.lockActive.reportModel.ContentAdapter;
import com.topflytech.lockActive.reportModel.Entity;
import com.topflytech.lockActive.reportModel.TopTabAdpater;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class ReadSubLockHisDataActivity extends AppCompatActivity {
    public static boolean isDebug = true;
    public static boolean isOnlyActiveNetwork = false;
    public static int RESPONSE_READ_DATE_TIME = 1;
    public static int REQUEST_READ_DATE_TIME = 1;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView reconnectBtn;
    private ImageView menuBtn;
    private ImageView btnLightStatus;
    boolean onThisView = false;
    private String mac;
    private String id;
    private String deviceName;
    SweetAlertDialog waitingDlg;
    SweetAlertDialog waitingCancelDlg;
    SweetAlertDialog sweetPwdDlg;
    private boolean pwdErrorWarning = false;
    private final static Object lock = new Object();
    private DropPopMenu mDropPopMenu;

    private Long readStartDate, readEndDate;
    private String startDateStr, endDateStr;
    private ArrayList<BleDeviceData> showData = new ArrayList<BleDeviceData>();
    private ArrayList<BleDeviceData> allDatas = new ArrayList<>();
    private ArrayList<BleDeviceData> tempDatas = new ArrayList<>();
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private byte[] getDataHead = new byte[]{0x60, 0x07, (byte) 0xE0};
    private boolean isDataReady = false;
//    private SmartTable<BleDeviceData> table;
    private PageTableData<BleDeviceData> tableData;
    private Spinner pageSpinner;
    private PageSpinnerAdapter pageSpinnerAdapter;
    private ArrayList<String> pageSelectList = new ArrayList<>();
    private boolean isFirstGetData = true;
    private TopflytechByteBuf byteBuf = new TopflytechByteBuf();
//

    RecyclerView rvTabRight;
    LinearLayout llTopRoot;
    RecyclerView recyclerContent;
    private List<Entity> mEntities = new ArrayList<>();
    private List<String> topTabs = new ArrayList<>();
    ProgressBar pvWaiting;
    ImageButton btnNext,btnPre;
    TextView tvCurPage,tvAllPage;
    int curPage = 1,pageSize=15,allPageCount=0;
    private GestureDetector gestureDetector;
    private LinearLayout pageNumbView;
    private String curPwd = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_read_sub_lock_his_data);
        onThisView = true;
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        id = intent.getStringExtra("id");
        deviceName = intent.getStringExtra("deviceName");
        initActionbar();
        checkWritePermission();
        allDatas.clear();
        tempDatas.clear();
        TftBleConnectManager.getInstance().init(ReadSubLockHisDataActivity.this);
        TftBleConnectManager.getInstance().setOnThisView(true);
        TftBleConnectManager.getInstance().setNeedCheckDeviceReady(true);
        TftBleConnectManager.getInstance().setNeedGetLockStatus(false);
        TftBleConnectManager.getInstance().setCallback("ReadHisDataActivity",bleStatusCallback);
        new Thread(sendMsgThread).start();

//        makeSomeData();
        initTable();

        showPwdDlg(true);
//        String testStr = "2003f97b00070e682d4c66e600000000000000006d0e682d4c6af600804010326400486d0e682d58b1e6000000102d6400506b0e682d6513f600804010286400586b16682d818a35350040101c6400546b0106333813282d3016682d81933131804010176400546b0106333813282d300e682d81950000804010176400546b";
//        byte[] testByte = MyByteUtils.hexString2Bytes(testStr);
//        parseResp(testByte);
    }
    private BleStatusCallback bleStatusCallback = new BleStatusCallback() {
        @Override
        public void onNotifyValue(byte[] value) {
            if(!onThisView){
                return;
            }
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
                    if(!onThisView){
                        return;
                    }
                    if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CONNECT_SUCC) {
                        doAfterConnectSucc();
                        reconnectBtn.setImageResource(R.mipmap.ic_refresh);
                        if (!TftBleConnectManager.getInstance().getIsDeviceReady()) {
                            showWaitingCancelDlg(getString(R.string.waitingDeviceReady));

                        }
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_CLOSE) {

                    } else if (connectStatus == TftBleConnectManager.BLE_STATUS_OF_DISCONNECT) {
                        if(waitingDlg != null){
                            waitingDlg.hide();
                        }
                        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
                        new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getResources().getString(R.string.warning))
                                .setContentText(getResources().getString(R.string.connect_fail))
                                .show();
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


    private boolean isShowPwdDlg = false;
    private void showPwdDlg(boolean isNextShowDateDlg){
        if(isShowPwdDlg){
            return;
        }
        isShowPwdDlg = true;
        sweetPwdDlg = new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.INPUT_TYPE);
        sweetPwdDlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    // 在这里处理返回按钮被按下的逻辑
                    isShowPwdDlg = false;
                    finish();
                    return true; // 返回 true 表示已处理按键事件
                }
                return false; // 返回 false 表示未处理按键事件
            }
        });
        sweetPwdDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
        sweetPwdDlg.setTitleText(getResources().getString(R.string.input_ble_pwd));
        sweetPwdDlg.setCancelable(true);
        sweetPwdDlg.setCancelText(getResources().getString(R.string.cancel));
        sweetPwdDlg.setConfirmText(getResources().getString(R.string.confirm));
        sweetPwdDlg.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        sweetPwdDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String pwd = sweetPwdDlg.getInputText();
                pwdErrorWarning = false;
                if(pwd.length() == 6){
                    curPwd = pwd;
                    sweetPwdDlg.hide();
                    isShowPwdDlg = false;
                    if(isNextShowDateDlg){
                        showSelectDate();
                    }else{
                        afterSelectDateDoInView();
                    }
                }else{
                    Toast.makeText(ReadSubLockHisDataActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();

                }
            }
        });
        sweetPwdDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                isShowPwdDlg = false;
                finish();
            }
        });
        sweetPwdDlg.setInputText("");
        sweetPwdDlg.show();
    }



    ContentAdapter contentAdapter;
    private void showReadDataWaiting(boolean isShow){
        if(isShow){
            pvWaiting.setVisibility(View.VISIBLE);
        }else{
            pvWaiting.setVisibility(View.GONE);
        }
    }
    private void initTable() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        float scale = getResources().getDisplayMetrics().density;
        float rowHigh = 40 * scale + 0.5f;
        float headHigh = 80 * scale + 0.5f;
        int rowCount = (height - (int)headHigh) / (int)rowHigh - 1;
        pageSize = rowCount;
        ButterKnife.bind(this);
        //处理顶部标题部分
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ReadSubLockHisDataActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvTabRight = (RecyclerView)findViewById(R.id.rv_tab_right);
        llTopRoot = (LinearLayout)findViewById(R.id.ll_top_root);
        recyclerContent = (RecyclerView)findViewById(R.id.recycler_content);
        btnNext = (ImageButton)findViewById(R.id.btn_next);
        btnPre = (ImageButton)findViewById(R.id.btn_prev);
        tvCurPage = (TextView)findViewById(R.id.tv_current_page);
        tvAllPage = (TextView)findViewById(R.id.tv_count_page) ;
        pvWaiting = (ProgressBar)findViewById(R.id.pb_waiting);
        rvTabRight.setLayoutManager(linearLayoutManager);
        TopTabAdpater topTabAdpater = new TopTabAdpater(this);
        rvTabRight.setAdapter(topTabAdpater);
        topTabs.add(getString(R.string.table_head_date  ));
        topTabs.add(getString(R.string.table_head_alarm));
        topTabs.add(getString(R.string.table_head_lock_status ));
        topTabs.add(getString(R.string.table_head_device_status));
        topTabs.add(getString(R.string.solar_voltage));
        topTabs.add(getString(R.string.battery_voltage));
        topTabs.add(getString(R.string.table_head_battery));
        topTabs.add(getString(R.string.temp));
        topTabs.add(getString(R.string.unlock_id));
        topTabAdpater.setDatas(topTabs);
        //处理内容部分
        recyclerContent.setLayoutManager(new LinearLayoutManager(this));
        recyclerContent.setHasFixedSize(true);
        contentAdapter = new ContentAdapter(this,rvTabRight);
        recyclerContent.setAdapter(contentAdapter);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNextPage();
            }
        });
        btnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPrePage();
            }
        });
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 在这里响应滑动手势事件
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
//                    if (velocityX > 0) {
//                        Log.d("Gesture", "向右滑动");
//                    } else {
//                        Log.d("Gesture", "向左滑动");
//                    }
                } else {
                    if (velocityY > 0) {
                        Log.d("Gesture", "向下滑动");
                        doPrePage();
                    } else {
                        Log.d("Gesture", "向上滑动");
                        doNextPage();
                    }
                }
                return true;
            }
        });
        recyclerContent.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetector.onTouchEvent(e);
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        pageNumbView = (LinearLayout)findViewById(R.id.rv_page_number) ;
        pageNumbView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SweetAlertDialog sweetPageDlg = new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.INPUT_TYPE);
                sweetPageDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                sweetPageDlg.setTitleText(getResources().getString(R.string.jump_to));
                sweetPageDlg.setCancelable(true);
                sweetPageDlg.setCancelText(getResources().getString(R.string.cancel));
                sweetPageDlg.setConfirmText(getResources().getString(R.string.confirm));
                sweetPageDlg.setInputType(InputType.TYPE_CLASS_NUMBER);
                sweetPageDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        if(sweetAlertDialog.getInputText() == null || sweetAlertDialog.getInputText().trim().length() == 0){
                            Toast.makeText(ReadSubLockHisDataActivity.this,R.string.fix_input,Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            int page = Integer.valueOf(sweetAlertDialog.getInputText().trim());
                            if(page <= 0 || page > allPageCount){
                                Toast.makeText(ReadSubLockHisDataActivity.this,R.string.page_out_of_range,Toast.LENGTH_SHORT).show();
                                return;
                            }
                            curPage = page;
                            tvCurPage.setText(String.valueOf(page));
                            updatePageData(page);
                            sweetAlertDialog.hide();
                        }catch (Exception e){
                            Toast.makeText(ReadSubLockHisDataActivity.this,R.string.page_out_of_range,Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
                sweetPageDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                    }
                });
                sweetPageDlg.setInputText("");
                sweetPageDlg.show();
            }
        });
        reloadReportData();
    }

    private void doNextPage() {
        if(curPage >= allPageCount){
            return;
        }
        curPage++;
        tvCurPage.setText(String.valueOf(curPage));
        updatePageData(curPage);
    }

    private void doPrePage() {
        if(curPage <= 1){
            return;
        }
        curPage--;
        tvCurPage.setText(String.valueOf(curPage));
        updatePageData(curPage);
    }

    private boolean checkWritePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(ReadSubLockHisDataActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(ReadSubLockHisDataActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        return false;
    }




    class PageSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
        @Override
        public int getCount() {
            return pageSelectList.size();
        }

        @Override
        public Object getItem(int i) {
            return pageSelectList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = View.inflate(ReadSubLockHisDataActivity.this, R.layout.spinner_simple_daropdown_item, null);
            }
            String value = (String) getItem(position);
            ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = View.inflate(ReadSubLockHisDataActivity.this, R.layout.spinner_simple_daropdown_item, null);
            }
            String value = (String) getItem(position);
            ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
            return convertView;
        }
    }

    private void showWaitingDlg(String warning) {
        if (waitingDlg == null) {
            waitingDlg = new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            waitingDlg.setCancelable(false);

        }
        if (warning != null && !warning.isEmpty()) {
            waitingDlg.setTitleText(warning);
        }
        waitingDlg.show();
    }

    private void showWaitingCancelDlg(String warning) {
        if (waitingCancelDlg == null) {
            waitingCancelDlg = new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            waitingCancelDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            waitingCancelDlg.showCancelButton(true);
            waitingCancelDlg.setContentText("");
            waitingCancelDlg.setCancelText(getResources().getString(R.string.cancel));
            waitingCancelDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                    sweetAlertDialog.dismiss();
                    isReadData = false;
                    showReadDataWaiting(isReadData);
                    lastReceiveDataDate = null;
                    reloadReportData();
                }
            });
        }
        if (warning != null && !warning.isEmpty()) {
            waitingCancelDlg.setTitleText(warning);
        }
        if (waitingCancelDlg != null && onThisView) {
            waitingCancelDlg.show();
        }

    }


    private byte[] getReadHisCmd(Long startDate, boolean isGetNext) {
        byte[] startDateByte = MyByteUtils.unSignedInt2Bytes(startDate);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(0x00);
            if(!isGetNext){
                outputStream.write(0x00);
                outputStream.write(startDateByte);
            }else{
                outputStream.write(0xff);
            }
            byte[] content =  outputStream.toByteArray();
            return Utils.getReadCmdContent(BleDeviceData.func_id_of_read_sub_lock_his_data, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }

    private byte[] getCmdContent(byte[] head, byte[] content) {
        if (content == null) {
            content = new byte[]{};
        }
        int len = content.length;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(head);
            outputStream.write(len);
            outputStream.write(content);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }
    private String model = "";


    private Date lastCheckStatusDate;
    private boolean isReadData = false;
    private Date lastReceiveDataDate = null;
    private long getStatusTimeout = 4000;
    private LinkedBlockingDeque<byte[]> sendMsgQueue = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<byte[]> sendMsgMultiQueue = new LinkedBlockingDeque<>();
    private Runnable sendMsgThread = new Runnable() {
        @Override
        public void run() {
            try {
                while (onThisView) {
                    if(isReadData){
                        if(lastReceiveDataDate != null){
                            Date now = new Date();
                            if(now.getTime() - lastReceiveDataDate.getTime() > 60000){
                                Log.e("writeContent","timeout reset to read");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ReadSubLockHisDataActivity.this,R.string.read_data_timeout,Toast.LENGTH_LONG).show();
                                        if(waitingCancelDlg != null){
                                            waitingCancelDlg.hide();
                                        }
                                    }
                                });
                                lastReceiveDataDate = null;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    if (sendMsgQueue.size() != 0){
                        byte[] needSendBytes = sendMsgQueue.poll();
                        if (needSendBytes != null){
                            writeContent(needSendBytes);
                        }
                        ReadSubLockHisDataActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                    showProgressBar();
                            }
                        });
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Thread.sleep(3000);
                    }

                }
                Log.e("writeContent","sendMsgThread end");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.read_his_data_activity_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        TextView tvHead = (TextView) findViewById(R.id.edit_command_title);
        if (deviceName != null) {
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
                TftBleConnectManager.getInstance().disconnect();
                showWaitingDlg(getResources().getString(R.string.reconnecting));
                TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
            }
        });
        btnLightStatus =  (ImageView) findViewById(R.id.btn_light_status);
        btnLightStatus.setVisibility(View.GONE);
        btnLightStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLightWarning){
                    return;
                }
                SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(ReadSubLockHisDataActivity.this, SweetAlertDialog.NORMAL_TYPE);
                confirmRelayDlg.setTitleText(getResources().getString(R.string.cancel_light_warning));
                confirmRelayDlg.setCancelable(true);
                confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
                confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
                confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.hide();
                        sendCancelLightWarning();
                    }
                });
                confirmRelayDlg.show();
                return;
            }
        });
        menuBtn = (ImageView) findViewById(R.id.command_edit_menu);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDropPopMenu.show(view);
            }
        });
        mDropPopMenu = new DropPopMenu(ReadSubLockHisDataActivity.this);
        mDropPopMenu.setBackgroundColor(R.color.colorPrimary);
        mDropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                if (menuItem.getItemId() == 1) {
                    showSelectDate();
                } else if (menuItem.getItemId() == 2) {
//                    sendPdf();
                } else if (menuItem.getItemId() == 3) {
                    sendExcel();
                } else if (menuItem.getItemId() == 4) {
//                    sendCSV();
                }
//                else if(menuItem.getItemId() == 5){
//                    savePdf();
//                }else if(menuItem.getItemId() == 6){
//                    saveExcel();
//                }else if(menuItem.getItemId() == 7){
//                    saveCsv();
//                }
            }


        });
        mDropPopMenu.setMenuList(getMenuList());
    }

    private String getDownDirs() {
//        File dir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File tftbleDir = new File(dir, "TFTBLE");
        if (!tftbleDir.exists()) {
            tftbleDir.mkdirs();
        }
        return tftbleDir.getAbsolutePath();
    }

    private void delCacheFile() {
        String path = getDownDirs();
        File file = new File(path);
        for (File childFile : file.listFiles()) {
            childFile.delete();
        }
    }

    private void sendExcel() {
        try {
            if(allDatas.size() == 0){
                Toast.makeText(ReadSubLockHisDataActivity.this,R.string.please_repair_data_first,Toast.LENGTH_SHORT).show();
                return;
            }
            delCacheFile();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // 设置邮件格式
            String name = "TFT_ELOCK_DATA";
            if (readStartDate != null && readEndDate != null && readStartDate != -1 && readEndDate != -1) {
                name += "-" + startDateStr + "-" + endDateStr;
            }
            String fileName = getDownDirs() + File.separator + name + ".xls";
            exportExcel(fileName);
            File file = new File(fileName);
            Uri fileUri;
            if (Build.VERSION.SDK_INT < 24) {
                fileUri = Uri.fromFile(file);
            } else {
                fileUri = FileProvider.getUriForFile(
                        this,
                        "com.topflytech.lockActive.fileprovider",
                        file);
            }
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent.createChooser(intent, "Choose Email Client");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addExcelString(WritableSheet mWritableSheet, int col, int row, String text) throws WriteException {
        if (null == mWritableSheet) return;
        Label label = new Label(col, row, text);
        mWritableSheet.addCell(label);
    }

    public boolean exportExcel(String filePath) {
        try {
            OutputStream os = new FileOutputStream(filePath);
            // 创建Excel工作簿
            WritableWorkbook mWritableWorkbook = Workbook.createWorkbook(os);
            // 创建Sheet表
            int rowIndex = 0;
            WritableSheet mWritableSheet = mWritableWorkbook.createSheet("sheet1", 0);
            addExcelString(mWritableSheet, 0, rowIndex, getResources().getString(R.string.table_head_date));
            addExcelString(mWritableSheet, 1, rowIndex, getResources().getString(R.string.table_head_alarm));
            addExcelString(mWritableSheet, 2, rowIndex, getResources().getString(R.string.table_head_lock_status));
            addExcelString(mWritableSheet, 3, rowIndex, getResources().getString(R.string.table_head_device_status));
            addExcelString(mWritableSheet, 4, rowIndex, getResources().getString(R.string.solar_voltage));
            addExcelString(mWritableSheet, 5, rowIndex, getResources().getString(R.string.battery_voltage));
            addExcelString(mWritableSheet, 6, rowIndex, getResources().getString(R.string.table_head_battery));
            addExcelString(mWritableSheet, 7, rowIndex, getResources().getString(R.string.temp));
            addExcelString(mWritableSheet, 8, rowIndex, getResources().getString(R.string.unlock_id));
            rowIndex++;
            for (int i = 0; i < allDatas.size(); i++) {
                BleDeviceData message = allDatas.get(i);
                addExcelString(mWritableSheet, 0, rowIndex + i, dateFormat.format(message.getDate()));
                addExcelString(mWritableSheet, 1, rowIndex + i, parseSubLockAlarm(message.getSubLockAlarm()));
                addExcelString(mWritableSheet, 2, rowIndex + i, parseLockType(message.getLockType()));
                addExcelString(mWritableSheet, 3, rowIndex + i, String.valueOf(message.getAlarm(ReadSubLockHisDataActivity.this)));
                addExcelString(mWritableSheet, 4, rowIndex + i, String.format("%.2f", message.getSolarVoltage()));
                addExcelString(mWritableSheet, 5, rowIndex + i, String.format("%.2f", message.getVoltage()));
                addExcelString(mWritableSheet, 6, rowIndex + i, String.valueOf(message.getBatteryPercent()) + "%");
                addExcelString(mWritableSheet, 7, rowIndex + i, BleDeviceData.getCurTemp(ReadSubLockHisDataActivity.this,message.getTemp()) + BleDeviceData.getCurTempUnit(ReadSubLockHisDataActivity.this));
                addExcelString(mWritableSheet, 8, rowIndex + i, message.getNfcId() != null && message.getNfcId().trim().length() > 0 ? message.getNfcId() : message.getMacId());
            }
            // 写入数据
            mWritableWorkbook.write();
            // 关闭文件
            mWritableWorkbook.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void showSelectDate() {
        Intent intent = new Intent(ReadSubLockHisDataActivity.this, DateSelectActivity.class);
        startActivityForResult(intent, REQUEST_READ_DATE_TIME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_READ_DATE_TIME && resultCode == RESPONSE_READ_DATE_TIME) {
            readStartDate = data.getLongExtra("startDate", -1);
            readEndDate = data.getLongExtra("endDate", -1);
            if (readStartDate != null && readEndDate != null && readStartDate != -1 && readEndDate != -1) {
                startDateStr = dateFormat.format(new Date(readStartDate * 1000));
                endDateStr = dateFormat.format(new Date(readEndDate * 1000));
            }
            afterSelectDateDoInView();
        }
    }

    private void afterSelectDateDoInView() {
        if(isFirstGetData){
            isFirstGetData = false;
            connectDevice();
        }else{
            if (!TftBleConnectManager.getInstance().isConnectSucc()) {
                return;
            }

            doAfterConnectSucc();
//            if(TftBleConnectManager.getInstance().getIsDeviceReady()){
//                showWaitingCancelDlg(getString(R.string.waitingDeviceReady));
//            }else{
//                showWaitingCancelDlg(getString(R.string.reading_data));
//            }
//            sendGetData(true,false);
            changeBlePwd(curPwd,curPwd);
        }
    }

    private List<MenuItem> getMenuList() {
        List<MenuItem> list = new ArrayList<>();
        list.add(new MenuItem(1, getResources().getString(R.string.choose_date)));
//        list.add(new MenuItem(2, getResources().getString(R.string.send_pdf)));
        list.add(new MenuItem(3, getResources().getString(R.string.send_excel)));
//        list.add(new MenuItem(4, getResources().getString(R.string.send_csv)));
//        list.add(new MenuItem(5, getResources().getString(R.string.savePdfToDownload)));
//        list.add(new MenuItem(6, getResources().getString(R.string.saveExcelToDownload)));
//        list.add(new MenuItem(7, getResources().getString(R.string.saveCsvToDownload)));
        return list;
    }

    @Override
    protected void onStart() {
        super.onStart();
        onThisView = true;

    }

    private void connectDevice(){
        if (!TftBleConnectManager.getInstance().isConnectSucc()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            showWaitingDlg(getResources().getString(R.string.connecting));
            Log.e("writeContent","try to connect");
            TftBleConnectManager.getInstance().connect(mac,BleDeviceData.isSubLockDevice(model));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (waitingDlg != null) {
            waitingDlg.hide();
            waitingDlg.dismiss();
            waitingDlg = null;
        }
        if (waitingCancelDlg != null) {
            waitingCancelDlg.hide();
            waitingCancelDlg.dismiss();
            waitingCancelDlg = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onThisView = false;
        if (waitingDlg != null) {
            waitingDlg.hide();
            waitingDlg.dismiss();
            waitingDlg = null;
        }
        if (waitingCancelDlg != null) {
            waitingCancelDlg.hide();
            waitingCancelDlg.dismiss();
            waitingCancelDlg = null;
        }
        if (sweetPwdDlg != null) {
            sweetPwdDlg.hide();
            sweetPwdDlg.dismiss();
            sweetPwdDlg = null;
        }
        reconnectBtn.setImageResource(R.mipmap.ic_disconnect);
        TftBleConnectManager.getInstance().disconnect();
        TftBleConnectManager.getInstance().removeCallback("EditActivity");
    }


    private void doAfterConnectSucc() {
        allDatas.clear();
        tempDatas.clear();
        curPage = 1;
        allPageCount = 1;
        tvCurPage.setText(String.valueOf(curPage));
        tvAllPage.setText("1");
        reloadReportData();
    }


    private void sendCancelLightWarning(){
        byte[] headCmd = new byte[]{0x60,0x07,(byte)0xE1};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(headCmd);
            outputStream.write(7);
            if(curPwd != null){
                outputStream.write(curPwd.getBytes());
            }
            outputStream.write(0x01);
            byte[] cmd = outputStream.toByteArray();
            writeContent(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void readLightStatus(){
        byte[] headCmd = new byte[]{0x60,0x07,(byte)0xE1};
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(headCmd);
            outputStream.write(6);
            if(curPwd != null){
                outputStream.write(curPwd.getBytes());
            }
            byte[] cmd = outputStream.toByteArray();
            writeContent(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void changeBlePwd(String oldPwd,String newPwd) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(oldPwd.getBytes());
            outputStream.write(newPwd.getBytes());
            ArrayList<byte[]> arrayList = Utils.getWriteCmdContent(BleDeviceData.func_id_of_ble_pwd_change,outputStream.toByteArray(),null);
            TftBleConnectManager.getInstance().writeArrayContent(arrayList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private TopflytechByteBuf bleByteBuf = new TopflytechByteBuf();
    private boolean isLightWarning = false;
    private void parseResp(byte[] respContent) {
        byte[] head = new byte[]{respContent[0],respContent[1],respContent[2]};
        if (Arrays.equals(head,BleDeviceData.deviceReadyHead)){
            int status = respContent[5];
            if (status == 0x01){
                if (!TftBleConnectManager.getInstance().getIsDeviceReady()){
//                    sendGetData(false,false);
                    changeBlePwd(curPwd,curPwd);
                }
                TftBleConnectManager.getInstance().setDeviceReady(true);
            }
        }else{
            ArrayList<BleRespData> dataList = Utils.parseRespContent(respContent);
            for (BleRespData bleRespData : dataList){
                Log.e("writeContent","code:" + bleRespData.getControlCode());
                if (bleRespData.getType() == BleRespData.READ_TYPE || bleRespData.getType() == BleRespData.WRITE_TYPE){
                    parseReadResp(bleRespData);
                }
                else{
                    if(bleRespData.getErrorCode() == BleRespData.ERROR_CODE_OF_PWD_ERROR){
                        Toast.makeText(ReadSubLockHisDataActivity.this,R.string.ble_pwd_error,Toast.LENGTH_SHORT).show();
                        showPwdDlg(false);
                    }
                    Log.e("writeContent","control error:" + bleRespData.getControlCode() + "-" + bleRespData.getType() + "-" + bleRespData.getErrorCode());
                }
            }
        }


//        if(respContent[0] == 0x60 &&  respContent[1] == 0x07 && respContent[2] == (byte)0xE1 ){
//            if(respContent.length >= 5){
//                if(respContent[4] == 0x01){
//                    isLightWarning = false;
//                    btnLightStatus.setImageResource(R.mipmap.light_blue);
//                }else if(respContent[4] == 0x02){
//                    isLightWarning = true;
//                    btnLightStatus.setImageResource(R.mipmap.light_red);
//                }else if((respContent[4] & 0xff) == 0xff ){
//                    Toast.makeText(ReadSubLockHisDataActivity.this, R.string.pwd_error, Toast.LENGTH_LONG).show();
//                    showPwdDlg(false);
//                }
//            }
//            return;
//        }
//        bleByteBuf.putBuf(respContent);
//        byte[] bytes = new byte[3];
//        while (bleByteBuf.getReadableBytes() > 5){
//            bleByteBuf.markReaderIndex();
//            bytes[0] = bleByteBuf.getByte(0);
//            bytes[1] = bleByteBuf.getByte(1);
//            bytes[2] = bleByteBuf.getByte(2);
//            if (bytes[0] == 0x60 && bytes[1] == 0x07 &&
//                    bytes[2] ==  (byte)0xE0){
//                bleByteBuf.skipBytes(3);
//                byte[] lengthBytes = bleByteBuf.readBytes(2);
//                int packageLength = MyByteUtils.bytes2Short(lengthBytes, 0);
//                bleByteBuf.resetReaderIndex();
//                if(packageLength <= 0){
//                    return;
//                }
//                if (packageLength + 5 > bleByteBuf.getReadableBytes() ){
//                    return;
//                }
//                bleByteBuf.skipBytes(5);
//
//                byte[] data = bleByteBuf.readBytes(packageLength);
//                if (packageLength == 1) {
//                    byte status = data[0];
//                    //deal status
//                    if (status == 0x01) {
//                        TftBleConnectManager.getInstance().setDeviceReady(false);
//                        sendGetData(true,false);
//                    } else if (status == 0x00) {
//                        if (waitingCancelDlg != null) {
//                            waitingCancelDlg.hide();
//                        }
//                        //is No date
//                        isDataReady = true;
//                        Log.e("writeContent","parseResp");
//                        isReadData = false;
//                        showReadDataWaiting(isReadData);
//                        lastReceiveDataDate = null;
//                        // init report
//                        reloadReportData();
//                    } else if (status == 0x02) {
//                        TftBleConnectManager.getInstance().setDeviceReady(true);
//                        if (waitingCancelDlg != null) {
//                            waitingCancelDlg.hide();
//                        }
//                        Toast.makeText(ReadSubLockHisDataActivity.this, R.string.pwd_error, Toast.LENGTH_LONG).show();
//                        showPwdDlg(false);
//                    } else if ((status & 0xff) == 0xff) {
//                        TftBleConnectManager.getInstance().setDeviceReady(true);
//                        if (waitingCancelDlg != null) {
//                            waitingCancelDlg.hide();
//                        }
//                        Toast.makeText(ReadSubLockHisDataActivity.this, R.string.func_not_open, Toast.LENGTH_SHORT).show();
////                        SweetAlertDialog confirmRelayDlg = new SweetAlertDialog(ReadHisDataActivity.this, SweetAlertDialog.NORMAL_TYPE);
////                        confirmRelayDlg.setTitleText(getResources().getString(R.string.open_save_record_warning));
////                        confirmRelayDlg.setCancelable(true);
////                        confirmRelayDlg.setCancelText(getResources().getString(R.string.cancel));
////                        confirmRelayDlg.setConfirmText(getResources().getString(R.string.confirm));
////                        confirmRelayDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
////                            @Override
////                            public void onClick(SweetAlertDialog sweetAlertDialog) {
////                                sweetAlertDialog.hide();
////                                sendOpenFuncCmd();
////                            }
////                        });
////                        confirmRelayDlg.show();
//                    }
//                } else {
//                    isReadData = true;
//                    showReadDataWaiting(isReadData);
//                    lastReceiveDataDate = new Date();
//                    TftBleConnectManager.getInstance().setDeviceReady(true);
////                    showWaitingCancelDlg(getString(R.string.reading_data));
//                    //check msg receive all
//                    try {
//                        dealReceiveData(data);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }else{
//                bleByteBuf.skipBytes(1);
//            }
//        }


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
    private void parseReadResp(BleRespData bleRespData) {
        if(bleRespData.getControlCode() == BleDeviceData.func_id_of_read_sub_lock_his_data){
            byte[] respData = getMultiRespData(bleRespData,BleDeviceData.func_id_of_read_sub_lock_his_data);
            if(respData == null || respData.length == 0){
                return;
            }
//            int len = respData[0];
//            if(respData.length < len || respData.length < 3){
//                return;
//            }
            boolean isReadEnd = respData[0] == 0x00;
            int dataCount = respData[1];
            int readIndex = 2;
            for(int i = 0; i < dataCount; i++){
                if(readIndex >= respData.length){
                    break;
                }
                BleDeviceData bleDeviceData = new BleDeviceData();
                int curPackageLen = respData[readIndex];
                readIndex++;
                int curPackageIndex = readIndex;
                if(curPackageIndex + curPackageLen > respData.length){
                    break;
                }
                readIndex += curPackageLen;
                long timestamp = MyByteUtils.unsigned4BytesToInt(respData,curPackageIndex);
                curPackageIndex += 4;
                int alarm = respData[curPackageIndex];
                curPackageIndex++;
                int lockType = respData[curPackageIndex];
                curPackageIndex++;
                int deviceStatus = MyByteUtils.bytes2Short(respData,curPackageIndex);
                curPackageIndex += 2;
                int voltageTemp = MyByteUtils.bytes2Short(respData,curPackageIndex);
                float voltage = voltageTemp / 1000.0f;
                curPackageIndex += 2;
                int batteryPercent = (int)respData[curPackageIndex];
                if(batteryPercent < 0){
                    batteryPercent += 256;
                }
                curPackageIndex += 1;
                int solarVoltageTemp = MyByteUtils.bytes2Short(respData,curPackageIndex);
                curPackageIndex += 2;
                float solarVoltage = solarVoltageTemp / 1000.0f;
                int temp = respData[curPackageIndex] & 0xff;
                if(respData[curPackageIndex] == 0xff){
                    temp = -999;
                }else{
                    if(temp < 0){
                        temp += 256;
                    }
                    temp = temp - 80;
                }
                curPackageIndex++;
                if(curPackageIndex < readIndex){
                    int type = respData[curPackageIndex];
                    curPackageIndex++;
                    if(type == 0x01){
                        int carLen = respData[curPackageIndex];
                        if(curPackageIndex + 1 + carLen <= readIndex){
                            String mac = MyByteUtils.bytes2HexString(Arrays.copyOfRange(respData,curPackageIndex + 1,curPackageIndex+carLen + 1),0);
                            bleDeviceData.setMacId(mac.toUpperCase());
                            curPackageIndex += carLen + 1;
                        }
                    }else if(type == 0x00){
                        int carLen = respData[curPackageIndex];
                        if(curPackageIndex + 1 + carLen <= readIndex){
                            String nfc = MyByteUtils.bytes2HexString(Arrays.copyOfRange(respData,curPackageIndex + 1,curPackageIndex+carLen + 1),0);
                            bleDeviceData.setNfcId(nfc.toUpperCase());
                            curPackageIndex += carLen + 1;
                        }
                    }
                }
                if(curPackageIndex < readIndex){
                    int type = respData[curPackageIndex];
                    curPackageIndex++;
                    if(type == 0x01){
                        int carLen = respData[curPackageIndex];
                        if(curPackageIndex + 1 + carLen <= readIndex){
                            String mac = MyByteUtils.bytes2HexString(Arrays.copyOfRange(respData,curPackageIndex + 1,curPackageIndex+carLen + 1),0);
                            bleDeviceData.setMacId(mac.toUpperCase());
                            curPackageIndex += carLen + 1;
                        }
                    }else if(type == 0x00){
                        int carLen = respData[curPackageIndex];
                        if(curPackageIndex + 1 + carLen <= readIndex){
                            String nfc = MyByteUtils.bytes2HexString(Arrays.copyOfRange(respData,curPackageIndex + 1,curPackageIndex+carLen + 1),0);
                            bleDeviceData.setNfcId(nfc.toUpperCase());
                            curPackageIndex += carLen + 1;
                        }
                    }
                }

                bleDeviceData.setId(id);
                bleDeviceData.parseSubLockStatus(deviceStatus);
                bleDeviceData.setBatteryPercent(batteryPercent);
                bleDeviceData.setTemp((float)temp);
                bleDeviceData.setSolarVoltage(solarVoltage);
                bleDeviceData.setVoltage(voltage);
                bleDeviceData.setSubLock(true);
                bleDeviceData.setLockType(lockType);
                bleDeviceData.setModel(BleDeviceData.MODEL_OF_SGX120B01);
                bleDeviceData.setDate(new Date(timestamp * 1000));
                if(readEndDate !=null && timestamp >readEndDate){
                    isReadEnd = true;
                    break;
                }
                bleDeviceData.setSubLockAlarm(alarm);
                tempDatas.add(bleDeviceData);
            }
            if(waitingCancelDlg != null){
                waitingCancelDlg.setContentText(getResources().getString(R.string.receive_count) + (tempDatas.size()+ allDatas.size()));
            }
            if(!isReadEnd){
                sendGetData(false,true);
            } else{
                isReadData = false;
                showReadDataWaiting(isReadData);
                if (waitingCancelDlg != null) {
                    waitingCancelDlg.hide();
                }
                reloadReportData();
            }

            if(tempDatas.size() > 10){
                reloadReportData();
            }
        }else if (bleRespData.getControlCode() == BleDeviceData.func_id_of_ble_pwd_change){
            sendGetData(false, false);
        }
    }


    private void sendGetData(boolean checkDeviceReady,boolean isGetNext) {
        if (readStartDate != null && readEndDate != null && readStartDate != -1 && readEndDate != -1) {
            byte[] cmd = getReadHisCmd(readStartDate,isGetNext);
            if(checkDeviceReady){
//                sendMsgQueue.offer(cmd);
                TftBleConnectManager.getInstance().sendCheckDeviceReadyCmd();
            }else{
                isReadData = true;
                showReadDataWaiting(isReadData);
                lastReceiveDataDate = null;
                writeContent(cmd);
            }
        }
    }
    private void updatePageData(int pageNumb){
        if(allDatas.size() < (pageNumb - 1) * pageSize){
            return;
        }
        int startIndex = (pageNumb - 1) * pageSize;
        if(startIndex <= 0){
            startIndex = 0;
        }
        int endIndex = pageNumb * pageSize;
        if(endIndex > allDatas.size()){
            endIndex = allDatas.size();
        }
        mEntities.clear();
        for(int i = startIndex;i < endIndex;i++){
            Entity entity = new Entity();
            ArrayList<String> rightMoveDatas = new ArrayList<>();
            BleDeviceData message = allDatas.get(i);
            rightMoveDatas.add( dateFormat.format(message.getDate()));
            rightMoveDatas.add(parseSubLockAlarm(message.getSubLockAlarm()));
            rightMoveDatas.add(parseLockType(message.getLockType()));
            rightMoveDatas.add(String.valueOf(message.getAlarm(ReadSubLockHisDataActivity.this)));
            rightMoveDatas.add(String.format("%.2f", message.getSolarVoltage()));
            rightMoveDatas.add(String.format("%.2f", message.getVoltage()));
            rightMoveDatas.add(String.valueOf(message.getBatteryPercent()) + "%");
            rightMoveDatas.add(BleDeviceData.getCurTemp(ReadSubLockHisDataActivity.this,message.getTemp()) + BleDeviceData.getCurTempUnit(ReadSubLockHisDataActivity.this));
            rightMoveDatas.add( message.getNfcId() != null && message.getNfcId().trim().length() > 0 ? message.getNfcId() : message.getMacId());
            entity.setRightDatas(rightMoveDatas);
            mEntities.add(entity);
        }
        contentAdapter.setDatas(mEntities);
    }
    private void reloadReportData() {
        try{
            Log.e("writeContent","reloadReportData:" + allDatas.size());
            allDatas.addAll(tempDatas);
            tempDatas.clear();
            if(allPageCount == curPage){
                updatePageData(curPage);
            }
            allPageCount = allDatas.size() % pageSize == 0 ? allDatas.size() / pageSize : allDatas.size() / pageSize + 1;
            tvAllPage.setText(String.valueOf(allPageCount));

            if(mEntities.size() == 0){
                updatePageData(curPage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private final SimpleDateFormat tableDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);




    private String getAlarmDesc(Integer alarmCode) {
        int alarmCodeHex = Integer.valueOf(Integer.toHexString(alarmCode));
        switch (alarmCodeHex) {
            case 1:
                return getString(R.string.alarm_1);
            case 2:
                return getString(R.string.alarm_2);
            case 3:
                return getString(R.string.alarm_3);
            case 4:
                return getString(R.string.alarm_4);
            case 5:
                return getString(R.string.alarm_5);
            case 6:
                return getString(R.string.alarm_6);
            case 7:
                return getString(R.string.alarm_7);
            case 8:
                return getString(R.string.alarm_8);
            case 9:
                return getString(R.string.alarm_9);
            case 10:
                return getString(R.string.alarm_10);
            case 11:
                return getString(R.string.alarm_11);
            case 12:
                return getString(R.string.alarm_12);
            case 13:
                return getString(R.string.alarm_13);
            case 14:
                return getString(R.string.alarm_14);
            case 15:
                return getString(R.string.alarm_15);
            case 16:
                return getString(R.string.alarm_16);
            case 17:
                return getString(R.string.alarm_17);
            case 18:
                return getString(R.string.alarm_18);
            case 19:
                return getString(R.string.alarm_19);
            case 20:
                return getString(R.string.alarm_20);
            case 21:
                return getString(R.string.alarm_21);
            case 22:
                return getString(R.string.alarm_22);
            case 23:
                return getString(R.string.alarm_23);
            case 24:
                return getString(R.string.alarm_24);
            case 25:
                return getString(R.string.alarm_25);
            case 26:
                return getString(R.string.alarm_26);
            case 27:
                return getString(R.string.alarm_27);
            case 28:
                return getString(R.string.alarm_28);
            case 29:
                return getString(R.string.alarm_29);
            case 30:
                return getString(R.string.alarm_30);
            case 31:
                return getString(R.string.alarm_31);
            case 32:
                return getString(R.string.alarm_32);
            case 66:
                return getString(R.string.alarm_66);
            case 67:
                return getString(R.string.alarm_67);
            case 70:
                return getString(R.string.alarm_70);
            case 71:
                return getString(R.string.alarm_71);
            case 72:
                return getString(R.string.alarm_72);
            case 74:
                return getString(R.string.alarm_74);
            case 77:
                return getString(R.string.alarm_77);
            default:
                return "";
        }
    }


    private void writeContent(final byte[] content) {
        TftBleConnectManager.getInstance().writeContent(content);

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

    public String parseSubLockAlarm(int alarmCode){
        if (alarmCode < 0){
            alarmCode = alarmCode + 256;
        }
        if (alarmCode == 0x00){
            return getString(R.string.lock_status_00);
        }else if (alarmCode == 0x01){
            return getString(R.string.lock_status_01);
        }else if (alarmCode == 0x03){
            return getString(R.string.lock_status_03);
        }else if (alarmCode == 0x04){
            return getString(R.string.lock_status_04);
        }else if (alarmCode == 0x09){
            return getString(R.string.lock_status_09);
        }else if (alarmCode == 0x0a){
            return getString(R.string.lock_status_0a);
        }else if (alarmCode == 0x05){
            return getString(R.string.lock_status_05);
        }else if (alarmCode == 0x06){
            return getString(R.string.lock_status_06);
        }else if (alarmCode == 0x07){
            return getString(R.string.lock_status_07);
        }else if (alarmCode == 0x08){
            return getString(R.string.lock_status_08);
        }else if (alarmCode == 0x11){
            return getString(R.string.lock_status_11);
        }else if (alarmCode == 0x12){
            return getString(R.string.lock_status_12);
        }else if (alarmCode == 0x13){
            return getString(R.string.lock_status_13);
        }else if (alarmCode == 0x14){
            return getString(R.string.lock_status_14);
        }else if (alarmCode == 0x15){
            return getString(R.string.lock_status_15);
        }else if (alarmCode == 0x16){
            return getString(R.string.lock_status_16);
        }else if (alarmCode == 0x17){
            return getString(R.string.lock_status_17);
        }
        else if (alarmCode == 0x21){
            return getString(R.string.lock_status_21);
        }else if (alarmCode == 0x22){
            return getString(R.string.lock_status_22);
        }else if (alarmCode == 0x23){
            return getString(R.string.lock_status_23);
        }else if (alarmCode == 0x24){
            return getString(R.string.lock_status_24);
        }else if (alarmCode == 0x25){
            return getString(R.string.lock_status_25);
        }else if (alarmCode == 0x26){
            return getString(R.string.lock_status_26);
        }else if (alarmCode == 0x27){
            return getString(R.string.lock_status_27);
        }
        else if (alarmCode == 0x31){
            return getString(R.string.lock_status_31);
        }else if (alarmCode == 0x32){
            return getString(R.string.lock_status_32);
        }else if (alarmCode == 0x33){
            return getString(R.string.lock_status_33);
        }else if (alarmCode == 0x34){
            return getString(R.string.lock_status_34);
        }else if (alarmCode == 0x35){
            return getString(R.string.lock_status_35);
        }else if (alarmCode == 0x36){
            return getString(R.string.lock_status_36);
        }else if (alarmCode == 0x37){
            return getString(R.string.lock_status_37);
        }
        else if (alarmCode == 0x41){
            return getString(R.string.lock_status_41);
        }else if (alarmCode == 0x42){
            return getString(R.string.lock_status_42);
        }else if (alarmCode == 0x43){
            return getString(R.string.lock_status_43);
        }else if (alarmCode == 0x44){
            return getString(R.string.lock_status_44);
        }else if (alarmCode == 0x45){
            return getString(R.string.lock_status_45);
        }else if (alarmCode == 0x46){
            return getString(R.string.lock_status_46);
        }else if (alarmCode == 0x47){
            return getString(R.string.lock_status_47);
        }
        else if (alarmCode == 0x51){
            return getString(R.string.lock_status_51);
        }else if (alarmCode == 0x52){
            return getString(R.string.lock_status_52);
        }else if (alarmCode == 0x53){
            return getString(R.string.lock_status_53);
        }else if (alarmCode == 0x54){
            return getString(R.string.lock_status_54);
        }else if (alarmCode == 0x55){
            return getString(R.string.lock_status_55);
        }else if (alarmCode == 0x56){
            return getString(R.string.lock_status_56);
        }else if (alarmCode == 0x57){
            return getString(R.string.lock_status_57);
        }else if (alarmCode == 0xff){
            return getString(R.string.pwd_error);
        }
        else if (alarmCode == 0xe0){
            return getString(R.string.charge_end);
        }else if (alarmCode == 0xe1){
            return getString(R.string.charge_over_voltage_recover);
        }else if (alarmCode == 0xe2){
            return getString(R.string.battery_lower_recover);
        }else if (alarmCode == 0xe3){
            return getString(R.string.high_temp_recover);
        }else if (alarmCode == 0xe4){
            return getString(R.string.low_temp_recover);
        }else if (alarmCode == 0xe5){
            return getString(R.string.lock_open_recover);
        }else if (alarmCode == 0xe6){
            return getString(R.string.back_cover_open_recover);
        }else if (alarmCode == 0xe7){
            return getString(R.string.gps_location_lost);
        }else if (alarmCode == 0xe8){
            return getString(R.string.gps_interference_recover);
        }
        else if (alarmCode == 0xf0){
            return getString(R.string.charge_start);
        }else if (alarmCode == 0xf1){
            return getString(R.string.charge_over_voltage);
        }else if (alarmCode == 0xf2){
            return getString(R.string.battery_lower);
        }else if (alarmCode == 0xf3){
            return getString(R.string.high_temp);
        }else if (alarmCode == 0xf4){
            return getString(R.string.low_temp);
        }else if (alarmCode == 0xf5){
            return getString(R.string.lock_open);
        }else if (alarmCode == 0xf6){
            return getString(R.string.back_cover_open);
        }else if (alarmCode == 0xf7){
            return getString(R.string.gps_location);
        }else if (alarmCode == 0xf8){
            return getString(R.string.gps_interference);
        }
        return "";
    }

    private String parseLockType(int lockType){
        if (lockType < 0){
            lockType = lockType + 256;
        }
        if (lockType == 0x00){
            return getString(R.string.lock_status_00);
        }else if (lockType == 0x01){
            return getString(R.string.lock_status_01);
        }else if (lockType == 0x03){
            return getString(R.string.lock_status_03);
        }else if (lockType == 0x04){
            return getString(R.string.lock_status_04);
        }else if (lockType == 0x09){
            return getString(R.string.lock_status_09);
        }else if (lockType == 0x0a){
            return getString(R.string.lock_status_0a);
        }else if (lockType == 0x05){
            return getString(R.string.lock_status_05);
        }else if (lockType == 0x06){
            return getString(R.string.lock_status_06);
        }else if (lockType == 0x07){
            return getString(R.string.lock_status_07);
        }else if (lockType == 0x08){
            return getString(R.string.lock_status_08);
        }else if (lockType == 0x11){
            return getString(R.string.lock_status_11);
        }else if (lockType == 0x12){
            return getString(R.string.lock_status_12);
        }else if (lockType == 0x13){
            return getString(R.string.lock_status_13);
        }else if (lockType == 0x14){
            return getString(R.string.lock_status_14);
        }else if (lockType == 0x15){
            return getString(R.string.lock_status_15);
        }else if (lockType == 0x16){
            return getString(R.string.lock_status_16);
        }else if (lockType == 0x17){
            return getString(R.string.lock_status_17);
        }
        else if (lockType == 0x21){
            return getString(R.string.lock_status_21);
        }else if (lockType == 0x22){
            return getString(R.string.lock_status_22);
        }else if (lockType == 0x23){
            return getString(R.string.lock_status_23);
        }else if (lockType == 0x24){
            return getString(R.string.lock_status_24);
        }else if (lockType == 0x25){
            return getString(R.string.lock_status_25);
        }else if (lockType == 0x26){
            return getString(R.string.lock_status_26);
        }else if (lockType == 0x27){
            return getString(R.string.lock_status_27);
        }
        else if (lockType == 0x31){
            return getString(R.string.lock_status_31);
        }else if (lockType == 0x32){
            return getString(R.string.lock_status_32);
        }else if (lockType == 0x33){
            return getString(R.string.lock_status_33);
        }else if (lockType == 0x34){
            return getString(R.string.lock_status_34);
        }else if (lockType == 0x35){
            return getString(R.string.lock_status_35);
        }else if (lockType == 0x36){
            return getString(R.string.lock_status_36);
        }else if (lockType == 0x37){
            return getString(R.string.lock_status_37);
        }
        else if (lockType == 0x41){
            return getString(R.string.lock_status_41);
        }else if (lockType == 0x42){
            return getString(R.string.lock_status_42);
        }else if (lockType == 0x43){
            return getString(R.string.lock_status_43);
        }else if (lockType == 0x44){
            return getString(R.string.lock_status_44);
        }else if (lockType == 0x45){
            return getString(R.string.lock_status_45);
        }else if (lockType == 0x46){
            return getString(R.string.lock_status_46);
        }else if (lockType == 0x47){
            return getString(R.string.lock_status_47);
        }
        else if (lockType == 0x51){
            return getString(R.string.lock_status_51);
        }else if (lockType == 0x52){
            return getString(R.string.lock_status_52);
        }else if (lockType == 0x53){
            return getString(R.string.lock_status_53);
        }else if (lockType == 0x54){
            return getString(R.string.lock_status_54);
        }else if (lockType == 0x55){
            return getString(R.string.lock_status_55);
        }else if (lockType == 0x56){
            return getString(R.string.lock_status_56);
        }else if (lockType == 0x57){
            return getString(R.string.lock_status_57);
        }else if (lockType == 0xff){
            return getString(R.string.pwd_error);
        }
        return "";
    }


}