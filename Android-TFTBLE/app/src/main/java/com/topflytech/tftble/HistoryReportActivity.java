package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.IFormat;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.table.PageTableData;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.BleHisData;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.view.popmenu.DropPopMenu;
import com.topflytech.tftble.view.popmenu.MenuItem;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class HistoryReportActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private LineChart tempChart,humidityChart;
    private Button btnNextPage,btnPrePage;
//    protected Typeface tfRegular;
//    protected Typeface tfLight;
    private SmartTable<BleHisData> table;
    private PageTableData<BleHisData> tableData;
    private ArrayList<ArrayList<byte[]>> orignHistoryList = new ArrayList<ArrayList<byte[]>>();
    private ArrayList<BleHisData> showBleHisData = new ArrayList<>();
    private final SimpleDateFormat tableDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private String deviceType="S02",deviceName="TH121",mac="221322112312",reportType,id;
    private ActionBar actionBar;
    private ImageView backButton;
    private DropPopMenu mDropPopMenu;
    private ImageView rightButton;
    private TextView tvHead;
    private Spinner pageSpinner;
    private PageSpinnerAdapter pageSpinnerAdapter;
    private ArrayList<String> pageSelectList = new ArrayList<>();
    private int pageSize = 15;
    private float tempAlarmUp,tempAlarmDown,humidityAlarmUp,humidityAlarmDown;

    private LinearLayout llS02Summary,llS04Summary,llTempChart,llHumidityChart;
    private TextView txDeviceName,txBleId,txReportCreateTime,txReportBeginTime,txReportEndTime,txBatteryBegin,txBatteryEnd,
        txReportProp,txOpenCount,txCloseCount,txS02TempHead,txS02HumidityHead,txS02TempStart,txS02HumidityStart,txS02TempEnd,txS02HumidityEnd,
            txS02TempMaxLimit,txS02HumidityMaxLimit,txS02TempMinLimit,txS02HumidityMinLimit,txS02TempAverage,txS02HumidityAverage,
            txS02TempMax,txS02HumidityMax,txS02TempMin,txS02HumidityMin,txS02TempOverHighCount,txS02TempOverHighTime,txS02HumidityOverHighCount,
            txS02HumidityOverHighTime,txS02TempOverLowCount,txS02TempOverLowTime,txS02HumidityOverLowCount,txS02HumidityOverLowTime,txS04TempHead,txS04TempStart,txS04TempEnd,
            txS04TempMaxLimit,txS04TempMinLimit,txS04TempAverage,txDeviceModel,
            txS04TempMax,txS04TempMin,txS04TempOverHighCount,txS04TempOverHighTime,
            txS04TempOverLowCount,txS04TempOverLowTime;
    private ImageView imgOpenCount,imgCloseCount;
    private long startDate,endDate,propOpenCount,propCloseCount;
    private int startBattery,endBattery;
    private String beginTemp,endTemp;
    private float beginHumidity,endHumidity,averageTemp,averageHumidity,maxTemp,minTemp,maxHumidity,minHumidity,
            overMaxTempLimitCount,overMaxTempLimitTime,overMinTempLimitCount,overMinTempLimitTime,
            overMaxHumidityLimitCount,overMaxHumidityLimitTime,overMinHumidityLimitCount,overMinHumidityLimitTime;
    private SweetAlertDialog waitingDlg;
    private SharedPreferences preferences;
    private  FontSelector selector;
    boolean isHadValidTempData = false;
    boolean isHadValidHumidityData = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_report);
        @SuppressLint("ResourceType")
        String yaHeiFontName = getResources().getString(R.raw.msyhl);
        yaHeiFontName += ",1";
        selector = new FontSelector();
        selector.addFont(FontFactory.getFont(FontFactory.TIMES_ROMAN, 16));
        selector.addFont(FontFactory.getFont(yaHeiFontName, BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED));

        Intent intent = getIntent();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mac = intent.getStringExtra("mac");
        reportType = intent.getStringExtra("reportType");
        deviceType = intent.getStringExtra("deviceType");
        tempAlarmUp = intent.getFloatExtra("tempAlarmUp",4095.0f);
        tempAlarmDown = intent.getFloatExtra("tempAlarmDown",4095.0f);
        humidityAlarmUp = intent.getFloatExtra("humidityAlarmUp",4095.0f);
        humidityAlarmDown = intent.getFloatExtra("humidityAlarmDown",4095.0f);
        deviceName = intent.getStringExtra("deviceName");
        id = intent.getStringExtra("id");
        startDate = intent.getLongExtra("startDate",0);
        endDate = intent.getLongExtra("endDate",0);
        initActionbar();
//        Button btnSavePic = findViewById(R.id.btn_save_pic);
//        btnSavePic.setOnClickListener(new SingleClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ContextCompat.checkSelfPermission(HistoryReportActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    saveToGallery();
//                } else {
//                    requestStoragePermission(tempChart);
//                }
//            }
//        });
        checkWritePermission();
        initData();
        initUI();
        initTempChart();
        initHumidityChart();
        initTable();

        for(BleHisData bleHisData : showBleHisData){
            if(bleHisData.getTemp() != -999){
                isHadValidTempData = true;
            }
            if(bleHisData.getHumidity() != -999){
                isHadValidHumidityData = true;
            }
        }
        if(!isHadValidHumidityData){
            llHumidityChart.setVisibility(View.GONE);
        }
        if(!isHadValidTempData){
            llTempChart.setVisibility(View.GONE);
        }
    }
    private void showWaitingDlg(String warning){
        waitingDlg = new SweetAlertDialog(HistoryReportActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        waitingDlg.setTitleText(getResources().getString(R.string.waiting));
        if (warning != null && !warning.isEmpty()){
            waitingDlg.setTitleText(warning);
        }
        waitingDlg.show();
    }

    private void initVirtualData(){
        Date now = new Date();
        mac = "ABCDEF123456";
        reportType = "temp";
        deviceType = "S02";
        tempAlarmUp = 4095;
        tempAlarmDown = 4095;
        humidityAlarmUp = 4095;
        humidityAlarmDown = 4095;
        deviceName = "thsld";
        id = "ABCDEF123456";
        startDate = now.getTime();
        endDate = now.getTime();
        for(int i = 0;i < 30;i++){
            BleHisData bleHisData = new BleHisData();
            bleHisData.setTemp(25);
            bleHisData.setHumidity(30);
            bleHisData.setBattery(100);
            bleHisData.setDate(new Date(now.getTime() + i * 3000));
            showBleHisData.add(bleHisData);
        }
    }

    private void initData() {
        orignHistoryList.clear();
        showBleHisData.clear();
        showBleHisData = EditActivity.allBleHisData;
//        initVirtualData();
        BleHisData beginItem = showBleHisData.get(0);
        BleHisData endItem = showBleHisData.get(showBleHisData.size() - 1);
//        startDate = beginItem.getDate().getTime();
        startBattery = beginItem.getBattery();
        beginTemp = BleDeviceData.getCurTemp(HistoryReportActivity.this,beginItem.getTemp());
        beginHumidity = beginItem.getHumidity();
//        endDate = endItem.getDate().getTime();
        endBattery = endItem.getBattery();
        endTemp = BleDeviceData.getCurTemp(HistoryReportActivity.this,endItem.getTemp());
        endHumidity = endItem.getHumidity();
        propCloseCount = 0;
        propOpenCount = 0;
        float tempSum = 0,humiditySum = 0;
        maxTemp = 0;minTemp = 0;maxHumidity = 0;minHumidity = 0;
        overMaxTempLimitCount = 0;overMaxTempLimitTime = 0;overMinTempLimitCount = 0;overMinTempLimitTime = 0;
        overMaxHumidityLimitCount = 0;overMaxHumidityLimitTime = 0;overMinHumidityLimitCount = 0;overMinHumidityLimitTime = 0;
        boolean beginTempUp = false,beginTempDown = false,beginHumidityUp = false,beginHumidityDown = false;
        long startTempCalDate = 0,endTempCalDate = 0,startHumidityCalDate = 0,endHumidityCalDate = 0;
        for(BleHisData bleHisData:showBleHisData){
            if(bleHisData.getTemp() != -999){
                tempSum += bleHisData.getTemp();
            }
            if(bleHisData.getHumidity() != -999){
                humiditySum += bleHisData.getHumidity();
            }
            if(bleHisData.getProp() == 1){
                propOpenCount ++;
            }else{
                propCloseCount++;
            }
            if (bleHisData.getTemp() != -999 && bleHisData.getTemp() > maxTemp){
                maxTemp = bleHisData.getTemp();
            }
            if(bleHisData.getTemp() != -999 && bleHisData.getTemp() < minTemp){
                minTemp = bleHisData.getTemp();
            }
            if(bleHisData.getHumidity() > maxHumidity  && bleHisData.getHumidity() != -999){
                maxHumidity = bleHisData.getHumidity();
            }
            if(bleHisData.getHumidity() < minHumidity  && bleHisData.getHumidity() != -999){
                minHumidity = bleHisData.getHumidity();
            }
            if(tempAlarmUp != 4095 && bleHisData.getTemp() > tempAlarmUp && bleHisData.getTemp() != -999){
                if (!beginTempUp){
                    beginTempUp = true;
                    startTempCalDate = bleHisData.getDate().getTime();
                }
                overMaxTempLimitCount++;
            }else{
                if (beginTempUp){
                    beginTempUp = false;
                    endTempCalDate = bleHisData.getDate().getTime();
                    overMaxTempLimitTime += endTempCalDate - startTempCalDate;
                }
            }
            if(tempAlarmDown != 4095 && bleHisData.getTemp() < tempAlarmDown && bleHisData.getTemp() != -999){
                if (!beginTempDown){
                    beginTempDown = true;
                    startTempCalDate = bleHisData.getDate().getTime();
                }
                overMinTempLimitCount++;
            }else{
                if (beginTempDown){
                    beginTempDown = false;
                    endTempCalDate = bleHisData.getDate().getTime();
                    overMinTempLimitTime += endTempCalDate - startTempCalDate;
                }
            }
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                if(humidityAlarmUp != 4095 && bleHisData.getHumidity() > humidityAlarmUp && bleHisData.getHumidity() != -999){
                    if (!beginHumidityUp){
                        beginHumidityUp = true;
                        startHumidityCalDate = bleHisData.getDate().getTime();
                    }
                    overMaxHumidityLimitCount++;
                }else{
                    if (beginHumidityUp){
                        beginHumidityUp = false;
                        endHumidityCalDate = bleHisData.getDate().getTime();
                        overMaxHumidityLimitTime += endHumidityCalDate - startHumidityCalDate;
                    }
                }
                if(humidityAlarmDown != 4095 && bleHisData.getHumidity() < humidityAlarmDown  && bleHisData.getHumidity() != -999){
                    if (!beginHumidityDown){
                        beginHumidityDown = true;
                        startHumidityCalDate = bleHisData.getDate().getTime();
                    }
                    overMinHumidityLimitCount++;
                }else{
                    if (beginHumidityDown){
                        beginHumidityDown = false;
                        endHumidityCalDate = bleHisData.getDate().getTime();
                        overMinHumidityLimitTime += endHumidityCalDate - startHumidityCalDate;
                    }
                }
            }
        }
        averageHumidity = humiditySum / showBleHisData.size();
        averageTemp = tempSum / showBleHisData.size();
    }

    private void initUI(){
        txDeviceName = (TextView)findViewById(R.id.tx_device_name);
        txBleId = (TextView)findViewById(R.id.tx_ble_id);
        txReportCreateTime = (TextView)findViewById(R.id.tx_report_create_time);
        txReportBeginTime = (TextView)findViewById(R.id.tx_report_begin_time);
        txReportEndTime = (TextView)findViewById(R.id.tx_report_end_time);
        txBatteryBegin = (TextView)findViewById(R.id.tx_battery_begin);
        txBatteryEnd = (TextView)findViewById(R.id.tx_battery_end);
        txReportProp = (TextView)findViewById(R.id.tx_report_prop);
        txOpenCount = (TextView)findViewById(R.id.tx_open_count);
        txCloseCount = (TextView)findViewById(R.id.tx_close_count);
        txS02TempHead = (TextView)findViewById(R.id.tx_s02_temp_head);
        txS02HumidityHead = (TextView)findViewById(R.id.tx_s02_humidity_head);
        txS02TempStart = (TextView)findViewById(R.id.tx_s02_temp_start);
        txS02HumidityStart = (TextView)findViewById(R.id.tx_s02_humidity_start);
        txS02TempEnd = (TextView)findViewById(R.id.tx_s02_temp_end);
        txS02HumidityEnd = (TextView)findViewById(R.id.tx_s02_humidity_end);
        txS02TempMaxLimit = (TextView)findViewById(R.id.tx_s02_temp_max_limit);
        txS02HumidityMaxLimit = (TextView)findViewById(R.id.tx_s02_humidity_max_limit);
        txS02TempMinLimit = (TextView)findViewById(R.id.tx_s02_temp_min_limit);
        txS02HumidityMinLimit = (TextView)findViewById(R.id.tx_s02_humidity_min_limit);
        txS02TempAverage = (TextView)findViewById(R.id.tx_s02_temp_average);
        txS02HumidityAverage = (TextView)findViewById(R.id.tx_s02_humidity_average);
        txS02TempMax = (TextView)findViewById(R.id.tx_s02_temp_max);
        txS02HumidityMax = (TextView)findViewById(R.id.tx_s02_humidity_max);
        txS02TempMin = (TextView)findViewById(R.id.tx_s02_temp_min);
        txS02HumidityMin = (TextView)findViewById(R.id.tx_s02_humidity_min);
        txS02TempOverHighCount = (TextView)findViewById(R.id.tx_s02_temp_over_high_count);
        txS02TempOverHighTime = (TextView)findViewById(R.id.tx_s02_temp_over_high_time);
        txS02HumidityOverHighCount = (TextView)findViewById(R.id.tx_s02_humidity_over_high_count);
        txS02HumidityOverHighTime = (TextView)findViewById(R.id.tx_s02_humidity_over_high_time);
        txS02TempOverLowCount = (TextView)findViewById(R.id.tx_s02_temp_over_low_count);
        txS02TempOverLowTime = (TextView)findViewById(R.id.tx_s02_temp_over_low_time);
        txS02HumidityOverLowCount = (TextView)findViewById(R.id.tx_s02_humidity_over_low_count);
        txS02HumidityOverLowTime = (TextView)findViewById(R.id.tx_s02_humidity_over_low_time);
        txS04TempHead = (TextView)findViewById(R.id.tx_s04_temp_head);
        txS04TempStart = (TextView)findViewById(R.id.tx_s04_temp_start);
        txS04TempEnd = (TextView)findViewById(R.id.tx_s04_temp_end);
        txS04TempMaxLimit = (TextView)findViewById(R.id.tx_s04_temp_max_limit);
        txS04TempMinLimit = (TextView)findViewById(R.id.tx_s04_temp_min_limit);
        txS04TempAverage = (TextView)findViewById(R.id.tx_s04_temp_average);
        txS04TempMax = (TextView)findViewById(R.id.tx_s04_temp_max);
        txS04TempMin = (TextView)findViewById(R.id.tx_s04_temp_min);
        txS04TempOverHighCount = (TextView)findViewById(R.id.tx_s04_temp_over_high_count);
        txS04TempOverHighTime = (TextView)findViewById(R.id.tx_s04_temp_over_high_time);
        txS04TempOverLowCount = (TextView)findViewById(R.id.tx_s04_temp_over_low_count);
        txS04TempOverLowTime = (TextView)findViewById(R.id.tx_s04_temp_over_low_time);
        llTempChart = (LinearLayout)findViewById(R.id.ll_temp_chart);
        llHumidityChart = (LinearLayout)findViewById(R.id.ll_humidity_chart);
        llS02Summary = (LinearLayout)findViewById(R.id.ll_s02_summary);
        llS04Summary = (LinearLayout)findViewById(R.id.ll_s04_summary);
        imgOpenCount = (ImageView)findViewById(R.id.img_open_count);
        imgCloseCount = (ImageView)findViewById(R.id.img_close_count);
        txDeviceModel = (TextView)findViewById(R.id.tx_ble_model);
        if(deviceType.equals("S04") || deviceType.equals("S08")){
            llS02Summary.setVisibility(View.GONE);
            llS04Summary.setVisibility(View.VISIBLE);
            llHumidityChart.setVisibility(View.GONE);
        }else{
            llS02Summary.setVisibility(View.VISIBLE);
            llS04Summary.setVisibility(View.GONE);
            llHumidityChart.setVisibility(View.VISIBLE);
        }

        txDeviceName.setText(deviceName);
        txBleId.setText(id);
        txReportCreateTime.setText(tableDateFormat.format(new Date()));
        txReportBeginTime.setText(tableDateFormat.format(new Date(startDate)));
        txReportEndTime.setText(tableDateFormat.format(new Date(endDate)));
        txBatteryBegin.setText(getResources().getString(R.string.begin)+startBattery + "%");
        txBatteryEnd.setText(getResources().getString(R.string.end)+endBattery+"%");
        if(deviceType.equals("S02") || deviceType.equals("S10")){
            txReportProp.setText(R.string.light);
            imgOpenCount.setImageResource(R.mipmap.ic_light_open);
            imgCloseCount.setImageResource(R.mipmap.ic_light_close);
            if(deviceType.equals("S10")){
                txDeviceModel.setText("T-one");
            }else{
                txDeviceModel.setText("TSTH1-B");
            }
        }else{
            txReportProp.setText(R.string.door);
            imgOpenCount.setImageResource(R.mipmap.ic_door_open);
            imgCloseCount.setImageResource(R.mipmap.ic_door_close);
            if(deviceType.equals("S08")){
                txDeviceModel.setText("T-sense");
            }else{
                txDeviceModel.setText("TSDT1-B");
            }
        }
        txOpenCount.setText(String.valueOf(propOpenCount));
        txCloseCount.setText(String.valueOf(propCloseCount));
        txS02TempHead.setText(getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")");
        txS02HumidityHead.setText(getResources().getString(R.string.table_head_humidity));
        txS02TempStart.setText(beginTemp);
        if(beginHumidity == -999){
            txS02HumidityStart.setText("-");
        }else{
            txS02HumidityStart.setText(String.valueOf(beginHumidity));
        }

        txS02TempEnd.setText(endTemp);
        if(endHumidity == -999){
            txS02HumidityEnd.setText("-");
        }else{
            txS02HumidityEnd.setText(String.valueOf(endHumidity));
        }

        if(tempAlarmUp != 4095){
            txS02TempMaxLimit.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,tempAlarmUp));
        }else{
            txS02TempMaxLimit.setText("-");
        }
        if(tempAlarmDown != 4095){
            txS02TempMinLimit.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,tempAlarmDown));
        }else{
            txS02TempMinLimit.setText("-");
        }
        if(humidityAlarmUp != 4095){
            txS02HumidityMaxLimit.setText(String.valueOf(humidityAlarmUp));
        }else{
            txS02HumidityMaxLimit.setText("-");
        }
        if(humidityAlarmDown != 4095){
            txS02HumidityMinLimit.setText(String.valueOf(humidityAlarmDown));
        }else{
            txS02HumidityMinLimit.setText("-");
        }
        txS02TempAverage.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,averageTemp));
        txS02HumidityAverage.setText(String.format("%.2f",averageHumidity));
        txS02TempMax.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,maxTemp));
        txS02TempMin.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,minTemp));
        txS02HumidityMax.setText(String.format("%.2f",maxHumidity));
        txS02HumidityMin.setText(String.format("%.2f",minHumidity));
        txS02TempOverHighCount.setText(String.valueOf(overMaxTempLimitCount));
        txS02TempOverHighTime.setText(calDiffTime((long)overMaxTempLimitTime));
        txS02HumidityOverHighCount.setText(String.valueOf(overMaxHumidityLimitCount));
        txS02HumidityOverHighTime.setText(calDiffTime((long)overMaxHumidityLimitTime));
        txS02TempOverLowCount.setText(String.valueOf(overMinTempLimitCount));
        txS02TempOverLowTime.setText(calDiffTime((long)overMinTempLimitTime));
        txS02HumidityOverLowCount.setText(String.valueOf(overMinHumidityLimitCount));
        txS02HumidityOverLowTime.setText(calDiffTime((long)overMinHumidityLimitTime));
        txS04TempHead.setText(getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")");
        txS04TempStart.setText(beginTemp);
        txS04TempEnd.setText(endTemp);
        if(tempAlarmUp != 4095){
            txS04TempMaxLimit.setText(String.valueOf(tempAlarmUp));
        }else{
            txS04TempMaxLimit.setText("-");
        }
        if(tempAlarmDown != 4095){
            txS04TempMinLimit.setText(String.valueOf(tempAlarmDown));
        }else{
            txS04TempMinLimit.setText("-");
        }

        txS04TempAverage.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,averageTemp));
        txS04TempMax.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,maxTemp));
        txS04TempMin.setText(BleDeviceData.getCurTemp(HistoryReportActivity.this,minTemp));
        txS04TempOverHighCount.setText(String.valueOf(overMaxTempLimitCount));
        txS04TempOverHighTime.setText(calDiffTime((long)overMaxTempLimitTime));
        txS04TempOverLowCount.setText(String.valueOf(overMinTempLimitCount));
        txS04TempOverLowTime.setText(calDiffTime((long)overMinTempLimitTime));

    }



    private String calDiffTime(long timeDiff){
        long allSecond = timeDiff / 1000;
        long minute = allSecond / 60;
        long hour = minute / 60;
        return String.format("%02d:%02d:%02d",hour,minute % 60,allSecond % 60);
    }


    protected boolean saveToGallery(Chart chart, String name) {
//        if (chart.saveToGallery(name, 70))
//            return true;
//        else
//            return false;
        if (saveToGallery(chart,name, 70))
            return true;
        else
            return false;
    }
    private boolean saveToGallery(Chart chart, String fileName, int quality) {
        String fileDescription = "MPAndroidChart-Library Save";
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        if (quality < 0 || quality > 100) {
            quality = 50;
        }

        long currentTime = System.currentTimeMillis();
        File file = new File(getDownDirs());
        if (!file.exists() && !file.mkdirs()) {
            return false;
        } else {
            String mimeType = "";
            switch (format) {
                case PNG:
                    mimeType = "image/png";
                    if (!fileName.endsWith(".png")) {
                        fileName = fileName + ".png";
                    }
                    break;
                case WEBP:
                    mimeType = "image/webp";
                    if (!fileName.endsWith(".webp")) {
                        fileName = fileName + ".webp";
                    }
                    break;
                case JPEG:
                default:
                    mimeType = "image/jpeg";
                    if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
                        fileName = fileName + ".jpg";
                    }
            }



            try {
                String filePath = file.getAbsolutePath() + "/" + fileName;
                FileOutputStream out = null;
                out = new FileOutputStream(filePath);
                Bitmap b = chart.getChartBitmap();
                b.compress(format, quality, out);
                out.flush();
                out.close();
                long size = (new File(filePath)).length();
//                ContentValues values = new ContentValues(8);
//                values.put("title", fileName);
//                values.put("_display_name", fileName);
//                values.put("date_added", currentTime);
//                values.put("mime_type", mimeType);
//                values.put("description", fileDescription);
//                values.put("orientation", 0);
//                values.put("_data", filePath);
//                values.put("_size", size);
//                return HistoryReportActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null;
                return true;
            } catch (IOException var16) {
                var16.printStackTrace();
                return false;
            }catch (Exception e) {
                e.printStackTrace();
                return false;
            }


        }
    }


    private String getSaveName(String fileType){
        String timeExt = txReportCreateTime.getText().toString() + "-" + txReportBeginTime.getText().toString() + "-" + txReportEndTime.getText().toString();
        if(fileType.equals("pdf")){
            return id + "-" + timeExt + ".pdf";
        }else if(fileType.equals("temp")){
            return id + "-temp-" + timeExt + ".png";
        }else if(fileType.equals("humidity")){
            return id + "-humidity-" + timeExt + ".png";
        }else if(fileType.equals("xls")){
            return id + "-" + timeExt + ".xls";
        }else if(fileType.equals("csv")){
            return id + "-" + timeExt + ".csv";
        }
        return "";
    }

    private void delCacheFile(){
        String path = getDownDirs();
        File file = new File(path);
        for(File childFile : file.listFiles()){
            childFile.delete();
        }
    }

    private String getSavePath(String fileType){
        String saveName = getSaveName(fileType);
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/files/";
        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        if(fileType.equals("pdf")){
            return path + saveName;
//            return Environment.getExternalStorageDirectory()+ File.separator + saveName;
        }else if(fileType.equals("temp")){
            return Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DCIM
                    +File.separator+ saveName;
        }else if(fileType.equals("humidity")){
            return Environment.getExternalStorageDirectory()
                    + File.separator + Environment.DIRECTORY_DCIM
                    +File.separator+ saveName;
        }else if(fileType.equals("xls")){
//            return Environment.getExternalStorageDirectory()+ File.separator + saveName;
            return path + saveName;
        }else if(fileType.equals("csv")){
//            return Environment.getExternalStorageDirectory()+ File.separator + saveName;
            return path + saveName;
        }
        return "";
    }


    private void initTempChart() {
        {   // // Chart Style // //
            tempChart = findViewById(R.id.temp_chart);
            // background color
            tempChart.setBackgroundColor(Color.WHITE);
            // disable description text
            tempChart.getDescription().setEnabled(false);
            // enable touch gestures
            tempChart.setTouchEnabled(true);

            // set listeners
            tempChart.setOnChartValueSelectedListener(this);
            tempChart.setDrawGridBackground(false);

            tempChart.setDragEnabled(false);
            tempChart.setScaleEnabled(false);
             tempChart.setScaleXEnabled(false);
             tempChart.setScaleYEnabled(false);

            SweetAlertDialog gotoDetailChartDlg = new SweetAlertDialog(HistoryReportActivity.this, SweetAlertDialog.WARNING_TYPE);
            gotoDetailChartDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
            gotoDetailChartDlg.setTitleText(getResources().getString(R.string.show_detail_chart));
            gotoDetailChartDlg.setCancelable(true);
            gotoDetailChartDlg.setCancelText(getResources().getString(R.string.cancel));
            gotoDetailChartDlg.setConfirmText(getResources().getString(R.string.confirm));
            gotoDetailChartDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                    Intent intent = new Intent(HistoryReportActivity.this, ChartDetailActivity.class);
                    intent.putExtra("mac", mac);
                    intent.putExtra("deviceType", deviceType);
                    intent.putExtra("id", id);
                    intent.putExtra("reportType", reportType);
                    intent.putExtra("tempAlarmUp",tempAlarmUp);
                    intent.putExtra("tempAlarmDown",tempAlarmDown);
                    intent.putExtra("humidityAlarmUp",humidityAlarmUp);
                    intent.putExtra("humidityAlarmDown",humidityAlarmDown);
                    startActivity(intent);

                }
            });
            gotoDetailChartDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                }
            });
         tempChart.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
//                 gotoDetailChartDlg.show();
                 return true;
             }
         });

            tempChart.setPinchZoom(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = tempChart.getXAxis();
            ValueFormatter formatter = new ValueFormatter() {
                private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);
                private final SimpleDateFormat mFormat2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                @Override
                public String getFormattedValue(float value) {

                    //设置 xAxis.setGranularity(1);后 value是从0开始的，每次加1，
                    int v = (int) value;
                    if (v <= showBleHisData.size() && v >= 0) {
                        String result = "";
                        if(v == 0){
                            Date date = showBleHisData.get(v).getDate();
                            result = mFormat.format(date);
                        }else if(v >= 1){
                            Date preDate = showBleHisData.get(v - 1).getDate();
                            Date now = showBleHisData.get(v).getDate();
                            if(preDate.getDay() != now.getDay()){
                                result = mFormat.format(now);
                            }else{
                                result = mFormat.format(now);
                            }
                        }

                        return result;
                    } else {
                        return null;
                    }
                }
            };
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter( formatter);
            xAxis.setLabelCount(3);
            xAxis.setEnabled(true);

            // vertical grid lines
//            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setDrawAxisLine(true);
        }
        // add data

        YAxis leftAxis = tempChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.line_color1));


        if(tempAlarmUp != 4095){
            LimitLine limitLineHighTemp = new LimitLine(tempAlarmUp, getResources().getString(R.string.high_temp_alarm));
            limitLineHighTemp.setLineWidth(4f);
            limitLineHighTemp.setLineColor(getResources().getColor(R.color.line_color1));
            limitLineHighTemp.enableDashedLine(10f, 10f, 0f);
            limitLineHighTemp.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
            limitLineHighTemp.setTextSize(10f);
            leftAxis.addLimitLine(limitLineHighTemp);

        }
        if(tempAlarmDown != 4095){
            LimitLine limitLineLowTemp = new LimitLine(tempAlarmDown, getResources().getString(R.string.low_temp_alarm));
            limitLineLowTemp.setLineWidth(4f);
            limitLineLowTemp.setLineColor(getResources().getColor(R.color.line_color1));
            limitLineLowTemp.enableDashedLine(10f, 10f, 0f);
            limitLineLowTemp.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            limitLineLowTemp.setTextSize(10f);
            leftAxis.addLimitLine(limitLineLowTemp);
        }

        // draw points over time
        tempChart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend legend = tempChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        setTempData();
    }

    private void initHumidityChart() {
        {   // // Chart Style // //
            humidityChart = findViewById(R.id.humidity_chart);
            // background color
            humidityChart.setBackgroundColor(Color.WHITE);
            // disable description text
            humidityChart.getDescription().setEnabled(false);
            // enable touch gestures
            humidityChart.setTouchEnabled(true);
            // set listeners
            humidityChart.setOnChartValueSelectedListener(this);
            humidityChart.setDrawGridBackground(false);

            humidityChart.setDragEnabled(false);
            humidityChart.setScaleEnabled(false);
            humidityChart.setScaleXEnabled(false);
            humidityChart.setScaleYEnabled(false);
            SweetAlertDialog gotoDetailChartDlg = new SweetAlertDialog(HistoryReportActivity.this, SweetAlertDialog.WARNING_TYPE);
            gotoDetailChartDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
            gotoDetailChartDlg.setTitleText(getResources().getString(R.string.show_detail_chart));
            gotoDetailChartDlg.setCancelable(true);
            gotoDetailChartDlg.setCancelText(getResources().getString(R.string.cancel));
            gotoDetailChartDlg.setConfirmText(getResources().getString(R.string.confirm));
            gotoDetailChartDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                    Intent intent = new Intent(HistoryReportActivity.this, ChartDetailActivity.class);
                    intent.putExtra("mac", mac);
                    intent.putExtra("deviceType", deviceType);
                    intent.putExtra("id", id);
                    intent.putExtra("reportType", reportType);
                    intent.putExtra("tempAlarmUp",tempAlarmUp);
                    intent.putExtra("tempAlarmDown",tempAlarmDown);
                    intent.putExtra("humidityAlarmUp",humidityAlarmUp);
                    intent.putExtra("humidityAlarmDown",humidityAlarmDown);
                    startActivity(intent);

                }
            });
            gotoDetailChartDlg.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.hide();
                }
            });
            humidityChart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    gotoDetailChartDlg.show();
                    return true;
                }
            });

            humidityChart.setPinchZoom(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = humidityChart.getXAxis();
            ValueFormatter formatter = new ValueFormatter() {
                private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);
                private final SimpleDateFormat mFormat2 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                @Override
                public String getFormattedValue(float value) {

                    //设置 xAxis.setGranularity(1);后 value是从0开始的，每次加1，
                    int v = (int) value;
                    if (v <= showBleHisData.size() && v >= 0) {
                        String result = "";
                        if(v == 0){
                            Date date = showBleHisData.get(v).getDate();
                            result = mFormat.format(date);
                        }else if(v >= 1){
                            Date preDate = showBleHisData.get(v - 1).getDate();
                            Date now = showBleHisData.get(v).getDate();
                            if(preDate.getDay() != now.getDay()){
                                result = mFormat.format(now);
                            }else{
                                result = mFormat.format(now);
                            }
                        }

                        return result;
                    } else {
                        return null;
                    }
                }
            };
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setValueFormatter( formatter);
            xAxis.setEnabled(true);
            xAxis.setLabelCount(3);
            // vertical grid lines
//            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setDrawAxisLine(true);
        }
        // add data

        YAxis leftAxis = humidityChart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.line_color2));


        if(humidityAlarmUp != 4095){
            LimitLine limitLineHighHumidity = new LimitLine(humidityAlarmUp, getResources().getString(R.string.high_humidity_alarm));
            limitLineHighHumidity.setLineWidth(4f);
            limitLineHighHumidity.setLineColor(getResources().getColor(R.color.line_color2));
            limitLineHighHumidity.enableDashedLine(10f, 10f, 0f);
            limitLineHighHumidity.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            limitLineHighHumidity.setTextSize(10f);
            leftAxis.addLimitLine(limitLineHighHumidity);

        }
        if(humidityAlarmDown != 4095){
            LimitLine limitLineLowHumidity = new LimitLine(humidityAlarmDown, getResources().getString(R.string.low_humidity_alarm));
            limitLineLowHumidity.setLineWidth(4f);
            limitLineLowHumidity.setLineColor(getResources().getColor(R.color.line_color2));
            limitLineLowHumidity.enableDashedLine(10f, 10f, 0f);
            limitLineLowHumidity.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            limitLineLowHumidity.setTextSize(10f);
            leftAxis.addLimitLine(limitLineLowHumidity);
        }

        // draw points over time
        humidityChart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend legend = humidityChart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.CIRCLE);

        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
        setHumidityData();
    }

    private void initTable() {
        int pageCount = showBleHisData.size() / pageSize;
        if (showBleHisData.size() % pageSize > 0){
            pageCount++;
        }
        pageSelectList.clear();
        for(int i = 0;i < pageCount;i++){
            pageSelectList.add(String.valueOf(i+1));
        }
        table = (SmartTable<BleHisData>) findViewById(R.id.table);
        table.getConfig().setShowYSequence(false);
        IFormat<Date> dateFormat =  new IFormat<Date>() {
            @Override
            public String format(Date date) {
                return tableDateFormat.format(date);
            }
        };
        final Column<Date> dateColumn = new Column<>(getResources().getString(R.string.table_head_date), "date",dateFormat);
        IFormat<Integer> batteryFormat =  new IFormat<Integer>() {
            @Override
            public String format(Integer value) {
                return value + "%";
            }
        };
        final Column<Integer> batteryColumn = new Column<>(getResources().getString(R.string.table_head_battery), "battery",batteryFormat);
        IFormat<Float> tempFormat =  new IFormat<Float>() {
            @Override
            public String format(Float value) {
                return BleDeviceData.getCurTemp(HistoryReportActivity.this,value) + BleDeviceData.getCurTempUnit(HistoryReportActivity.this);
            }
        };
        final Column<Float> tempColumn = new Column<>(getResources().getString(R.string.table_head_temp), "temp",tempFormat);
        IFormat<Float> humidityFormat =  new IFormat<Float>() {
            @Override
            public String format(Float value) {
                if(value == -999){
                    return "-";
                }
                return String.format("%.0f",value);
            }
        };
        final Column<Float> humidityColumn = new Column<>(getResources().getString(R.string.table_head_humidity), "humidity",humidityFormat);
        String propDesc = getResources().getString(R.string.table_head_light);
        if(deviceType.equals("S04") || deviceType.equals("S08")){
            propDesc = getResources().getString(R.string.table_head_door);
        }
        IFormat<Integer> propFormat =  new IFormat<Integer>() {
            @Override
            public String format(Integer value) {
                if(deviceType.equals("S02")  || deviceType.equals("S10")){
                    if(value == 1){
                        return getResources().getString(R.string.prop_light);
                    }else{
                        return getResources().getString(R.string.prop_dark);
                    }
                }else if(deviceType.equals("S04") || deviceType.equals("S08")){
                    if(value == 1){
                        return getResources().getString(R.string.prop_door_open);
                    }else{
                        return getResources().getString(R.string.prop_door_close);
                    }
                }else{
                    return "";
                }
            }
        };
        final Column<Integer> propColumn = new Column<>(propDesc, "prop",propFormat);
        if(reportType.equals("alarm")){
            IFormat<Byte> alarmFormat = new IFormat<Byte>() {
                @Override
                public String format(Byte aByte) {
                    return BleDeviceData.getWarnDesc(HistoryReportActivity.this,deviceType,aByte);
                }
            };
            final Column<Byte> alarmColumn = new Column<>(getResources().getString(R.string.table_head_warn), "alarm",alarmFormat);
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                tableData = new PageTableData<>("",showBleHisData,dateColumn,batteryColumn,tempColumn,humidityColumn,propColumn,alarmColumn);
            }else{
                tableData = new PageTableData<>("",showBleHisData,dateColumn,batteryColumn,tempColumn,propColumn,alarmColumn);
            }
        }else{
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                tableData = new PageTableData<>("",showBleHisData,dateColumn,batteryColumn,tempColumn,humidityColumn,propColumn);
            }else{
                tableData = new PageTableData<>("",showBleHisData,dateColumn,batteryColumn,tempColumn,propColumn);
            }
        }

        table.getConfig().setContentCellBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
            @Override
            public int getBackGroundColor(CellInfo cellInfo) {
                if(cellInfo.row %2 ==0) {
                    return ContextCompat.getColor(HistoryReportActivity.this, R.color.content_bg);
                }
                return TableConfig.INVALID_COLOR;
            }
        });
        table.getConfig().setTableTitleStyle(new FontStyle(this,22,getResources().getColor(R.color.arc1)));
        tableData.setPageSize(pageSize);
        table.setTableData(tableData);
        initSpanner();
        btnNextPage = (Button)findViewById(R.id.btn_next_page);
        btnPrePage = (Button)findViewById(R.id.btn_pre_page);
        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tableData.getCurrentPage() != tableData.getTotalPage() - 1){
                    pageSpinner.setSelection(tableData.getCurrentPage()+1);
                }

            }
        });
        btnPrePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tableData.getCurrentPage() != 0){
                    pageSpinner.setSelection(tableData.getCurrentPage()-1);
                }
            }
        });
    }


    private void setTempData() {

        ArrayList<Entry> tempValues = new ArrayList<>();

        for (int i = 0; i < showBleHisData.size(); i++) {
            BleHisData bleHisData = showBleHisData.get(i);
            float val = bleHisData.getTemp();
            if(bleHisData.getTemp() != -999){
                tempValues.add(new Entry(i, val, getResources().getDrawable(R.mipmap.star)));
            }
        }

        LineDataSet tempSet;
        if (tempChart.getData() != null &&
                tempChart.getData().getDataSetCount() > 0) {
            tempSet = (LineDataSet) tempChart.getData().getDataSetByIndex(0);
            tempSet.setValues(tempValues);
            tempChart.getData().notifyDataChanged();
            tempChart.notifyDataSetChanged();
        } else{
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            tempSet = new LineDataSet(tempValues, "Temperature");
            tempSet.setDrawIcons(false);
//            tempSet.enableDashedLine(10f, 5f, 0f);
            tempSet.setColor(getResources().getColor(R.color.line_color1));
            tempSet.setCircleColor(getResources().getColor(R.color.line_color1));
            tempSet.setLineWidth(1f);
            tempSet.setCircleRadius(2f);

            tempSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            tempSet.setDrawCircleHole(false);
            tempSet.setFormLineWidth(1f);

//            tempSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            tempSet.setFormSize(15.f);
            tempSet.setValueTextSize(9f);
            tempSet.setDrawValues(false);
//            tempSet.enableDashedHighlightLine(10f, 5f, 0f);
            tempSet.setDrawFilled(true);
            tempSet.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return tempChart.getAxisLeft().getAxisMinimum();
                }
            });

            dataSets.add(tempSet);
            LineData data = new LineData(dataSets);
            tempChart.setData(data);
        }
    }

    private void setHumidityData() {
        ArrayList<Entry> humidityValues = new ArrayList<>();
        LineDataSet humiditySet;
        for (int i = 0; i < showBleHisData.size(); i++) {
            BleHisData bleHisData = showBleHisData.get(i);
            if(bleHisData.getHumidity() != -999){
                float val = bleHisData.getHumidity();
                humidityValues.add(new Entry(i, val, getResources().getDrawable(R.mipmap.star)));
            }
        }
        if (humidityChart.getData() != null &&
                humidityChart.getData().getDataSetCount() > 0) {
            humiditySet = (LineDataSet) humidityChart.getData().getDataSetByIndex(0);
            humiditySet.setValues(humidityValues);
            humidityChart.getData().notifyDataChanged();
            humidityChart.notifyDataSetChanged();
        } else{
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            humiditySet = new LineDataSet(humidityValues, getResources().getString(R.string.table_head_humidity));
            humiditySet.setDrawIcons(false);
//            humiditySet.enableDashedLine(10f, 5f, 0f);
            humiditySet.setColor(getResources().getColor(R.color.line_color2));
            humiditySet.setCircleColor(getResources().getColor(R.color.line_color2));
            humiditySet.setLineWidth(1f);
            humiditySet.setCircleRadius(3f);
            humiditySet.setDrawCircleHole(false);
            humiditySet.setFormLineWidth(1f);
//            humiditySet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            humiditySet.setFormSize(15.f);
            humiditySet.setValueTextSize(9f);
            humiditySet.setDrawValues(false);
            humiditySet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//            humiditySet.enableDashedHighlightLine(10f, 5f, 0f);
            humiditySet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            humiditySet.setDrawFilled(true);
            humiditySet.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return humidityChart.getAxisLeft().getAxisMinimum();
                }
            });

            dataSets.add(humiditySet);
            LineData data = new LineData(dataSets);

            humidityChart.setData(data);
        }


    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }


    private void initSpanner(){
        pageSpinner = (Spinner)findViewById(R.id.page_spinner);
        pageSpinnerAdapter = new PageSpinnerAdapter();
        pageSpinner.setAdapter(pageSpinnerAdapter);
        pageSpinner.setSelection(0,true);
        pageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int pageSelect = i;
                tableData.setCurrentPage(pageSelect);
                table.notifyDataChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    class PageSpinnerAdapter extends BaseAdapter implements SpinnerAdapter{
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
                convertView = View.inflate(HistoryReportActivity.this, R.layout.spinner_simple_daropdown_item, null);
            }
            String value = (String)getItem(position);
            ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = View.inflate(HistoryReportActivity.this, R.layout.spinner_simple_daropdown_item, null);
            }
            String value = (String)getItem(position);
            ((TextView) convertView.findViewById(R.id.main_title)).setText(value);
            return convertView;
        }
    }

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.report_view_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setImageResource(R.mipmap.ic_category);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        mDropPopMenu = new DropPopMenu(HistoryReportActivity.this);
        mDropPopMenu.setBackgroundColor(R.color.colorPrimary);
        mDropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                if(menuItem.getItemId() == 1){
                    emailSetting();
                }else if(menuItem.getItemId() == 2){
                    sendPdf();
                }else if(menuItem.getItemId() == 3){
                    sendExcel();
                }else if(menuItem.getItemId() == 4){
                    sendCSV();
                }else if(menuItem.getItemId() == 5){
                    savePdf();
                }else if(menuItem.getItemId() == 6){
                    saveExcel();
                }else if(menuItem.getItemId() == 7){
                    saveCsv();
                }
            }
        });
        mDropPopMenu.setMenuList(getMenuList());
        rightButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                    mDropPopMenu.show(view);
            }
        });

    }
    private String getDownDirs(){
//        File dir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File tftbleDir = new File(dir,"TFTBLE");
        if (!tftbleDir.exists()) {
            tftbleDir.mkdirs();
        }
        return tftbleDir.getAbsolutePath();
    }

    private void saveCsv() {
        ArrayList<ArrayList<String>> csvData = getCsvData();
        String fileName = getDownDirs() +File.separator+ getSaveName("csv");
        showWaitingDlg("");
        if(exportCsv(fileName,csvData)){
             Toast.makeText(HistoryReportActivity.this,R.string.saveToDownloadsSucc,Toast.LENGTH_LONG).show();
        }else{
             Toast.makeText(HistoryReportActivity.this,R.string.error_please_try_again,Toast.LENGTH_LONG).show();
        }
        waitingDlg.hide();
    }

    private void saveExcel() {
        String fileName = getDownDirs() +File.separator+ getSaveName("xls");
        showWaitingDlg("");
        if(exportExcel(fileName)){
            Toast.makeText(HistoryReportActivity.this,R.string.saveToDownloadsSucc,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(HistoryReportActivity.this,R.string.error_please_try_again,Toast.LENGTH_LONG).show();
        }
        waitingDlg.hide();
    }

    private void savePdf() {
        String fileName = getDownDirs() +File.separator+ getSaveName("pdf");
        showWaitingDlg("");
        createTextPDF(fileName);
        Toast.makeText(HistoryReportActivity.this,R.string.saveToDownloadsSucc,Toast.LENGTH_LONG).show();
        waitingDlg.hide();
    }

    private void emailSetting() {
        Intent intent = new Intent(HistoryReportActivity.this,SmtpSettingActivity.class);
        startActivity(intent);
    }
    String smtpServer,smtpPort,email,smtpPwd,senderName,recvEmail;
    private void getSmtpConfig(){
        smtpServer = preferences.getString("smtpServer","");
        smtpPort = preferences.getString("smtpPort","");
        email = preferences.getString("email","");
        senderName = preferences.getString("senderName","");
        smtpPwd = preferences.getString("smtpPwd","");
        recvEmail = preferences.getString("recvEmail","");
    }
    public interface Callback {
        enum StatusCode {
            OK,
            ERROR
        }

        void callback(StatusCode code);
    }
    private void sendEmailFile(String fileType ,final Callback callback){
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", smtpServer);
        int port = Integer.valueOf(smtpPort);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.timeout","60000");
        properties.put("mail.smtp.connectiontimeout", "60000");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        if (port == 465) {
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.ssl.enable", "true");
        }
        if(port == 587){
            properties.put("mail.smtp.starttls.enable","true");
        }
        // 获取默认session对象
        final Session session = Session.getDefaultInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // 登陆邮件发送服务器的用户名和密码
                        return new PasswordAuthentication(email,smtpPwd);
                    }
                });

        MimeBodyPart attach = null;
        File file = new File(getSavePath(fileType));
        if (file.exists()){
            attach = new MimeBodyPart();
            DataHandler dh = new DataHandler(new FileDataSource(file));
            try {
                attach.setDataHandler(dh);
                attach.setFileName(dh.getName());
            } catch (MessagingException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }


        // 创建默认的 MimeMessage 对象
        // 发送消息
        final String filename = getSaveName(fileType);
        final MimeBodyPart finalAttach = attach;
        new AsyncTask<String,Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(String... strings) {
                try {
                    String[] recvMailList = recvEmail.split(";");
                    for(String recvMail : recvMailList){
                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(email));
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recvMail));
                        MimeMultipart mp = new MimeMultipart();
                        MimeBodyPart text = new MimeBodyPart();
                        text.setContent("<h4>"+filename+"</h4>", "text/html;charset=UTF-8");
                        mp.addBodyPart(text);
                        mp.addBodyPart(finalAttach);
                        mp.setSubType("mixed");

                        message.setSubject(filename);
                        message.setContent(mp);
                        message.saveChanges();
                        Transport.send(message);
                    }
                    callback.callback(Callback.StatusCode.OK);
                } catch (MessagingException e) {
                    callback.callback(Callback.StatusCode.ERROR);
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    private void sendPdf(){
        try{
            delCacheFile();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // 设置邮件格式
            String fileName = getDownDirs() +File.separator+ getSaveName("pdf");
            createTextPDF(fileName);
            File file = new File(fileName);
            Uri fileUri;
            if(Build.VERSION.SDK_INT < 24){
                fileUri = Uri.fromFile(file);
            }else{
                fileUri = FileProvider.getUriForFile(
                        this,
                        "com.topflytech.tftble.fileprovider",
                        file);
            }
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent.createChooser(intent, "Choose Email Client");
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
//        Boolean isSmtpConfigSucc = preferences.getBoolean("smtpConfig",false);
//        if(isSmtpConfigSucc){
//            showWaitingDlg("");
//            getSmtpConfig();
//            createTextPDF(getSavePath("pdf"));
//            sendEmailFile("pdf", new Callback() {
//                @Override
//                public void callback(StatusCode code) {
//                    waitingDlgHide();
//                    Looper.prepare();
//                    if(code == StatusCode.OK){
//                        Toast.makeText(HistoryReportActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(HistoryReportActivity.this,R.string.smtp_send_fail,Toast.LENGTH_SHORT).show();
//                    }
//                    Looper.loop();
//                    delCacheFile();
//                }
//            });
//        }else{
//            Toast.makeText(HistoryReportActivity.this,R.string.need_config_smtp,Toast.LENGTH_SHORT).show();
//            emailSetting();
//        }

    }




    private boolean checkWritePermission(){
        int permissionCheck = ContextCompat.checkSelfPermission(HistoryReportActivity.this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        }else {
            ActivityCompat.requestPermissions(HistoryReportActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        return false;
    }



    public void createTextPDF(String realPath){
        // 创建文件及相关目录
        Document document = new Document();
        try {
            saveToGallery(tempChart, getSaveName("temp"));
            if(deviceType.equals("S02") || (deviceType.equals("S10") && isHadValidHumidityData)){
                saveToGallery(humidityChart, getSaveName("humidity"));
            }
            File file = new File(realPath);
            if (!file.exists()) {
                file.createNewFile();
            }

            // 创建PdfWriter对象
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(realPath));
            // 设置每行的间距
            writer.setInitialLeading(30);

            // 打开文档
            document.open();

            PdfPTable reportSumTable = new PdfPTable(3);
            reportSumTable.setSpacingBefore(10f);
            reportSumTable.setSpacingAfter(10f);
            PdfPCell nameDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.ble_name))));
            reportSumTable.addCell(nameDescCell);
            PdfPCell nameCell = new PdfPCell(new Paragraph(selector.process(deviceName)));
            nameCell.setColspan(2);
            reportSumTable.addCell(nameCell);

            PdfPCell idDescCell = new PdfPCell(new Paragraph(getResources().getString(R.string.ble_id)));
            reportSumTable.addCell(idDescCell);
            PdfPCell idCell = new PdfPCell(new Paragraph(id));
            idCell.setColspan(2);
            reportSumTable.addCell(idCell);

            PdfPCell modelDescCell = new PdfPCell(new Paragraph(getResources().getString(R.string.device_model)));
            reportSumTable.addCell(modelDescCell);
            PdfPCell modelCell = new PdfPCell(new Paragraph(txDeviceModel.getText().toString()));
            modelCell.setColspan(2);
            reportSumTable.addCell(modelCell);

            PdfPCell createReportTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.report_create_time))));
            reportSumTable.addCell(createReportTimeDescCell);
            PdfPCell createReportTimeCell = new PdfPCell(new Paragraph(txReportCreateTime.getText().toString()));
            createReportTimeCell.setColspan(2);
            reportSumTable.addCell(createReportTimeCell);

            PdfPCell reportTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.report_time))));
            reportSumTable.addCell(reportTimeDescCell);
            PdfPCell beginTimeCell = new PdfPCell(new Paragraph(txReportBeginTime.getText().toString()));
            reportSumTable.addCell(beginTimeCell);
            PdfPCell endTimeCell = new PdfPCell(new Paragraph(txReportEndTime.getText().toString()));
            reportSumTable.addCell(endTimeCell);

            PdfPCell batteryDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.battery))));
            reportSumTable.addCell(batteryDescCell);
            PdfPCell beginBatteryCell = new PdfPCell(new Paragraph(txBatteryBegin.getText().toString()));
            reportSumTable.addCell(beginBatteryCell);
            PdfPCell endBatteryCell = new PdfPCell(new Paragraph(txBatteryEnd.getText().toString()));
            reportSumTable.addCell(endBatteryCell);

            PdfPCell lightDescCell = new PdfPCell(new Paragraph(selector.process(txReportProp.getText().toString())));
            reportSumTable.addCell(lightDescCell);
            PdfPCell lightOpenCell = new PdfPCell(getMultiImageText(txOpenCount.getText().toString(),"ic_light_open.png",true));
            reportSumTable.addCell(lightOpenCell);
            PdfPCell lightCloseCell = new PdfPCell(getMultiImageText(txCloseCount.getText().toString(),"ic_light_close.png",true));
            reportSumTable.addCell(lightCloseCell);
            document.add(reportSumTable);

            if(deviceType.equals("S02")  || deviceType.equals("S10") ){
                PdfPTable detailSumTable = new PdfPTable(3);
                detailSumTable.setSpacingBefore(10f);
                detailSumTable.setSpacingAfter(10f);

                PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
                detailSumTable.addCell(emptyCell);
                String temHeadStr = getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")";
                PdfPCell tempHeadCell = new PdfPCell(new Paragraph(selector.process(temHeadStr)));
                detailSumTable.addCell(tempHeadCell);
                PdfPCell humidityHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_humidity))));
                detailSumTable.addCell(humidityHeadCell);

                PdfPCell startValueDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.start_value))));
                detailSumTable.addCell(startValueDescCell);
                PdfPCell startTempCell = new PdfPCell(new Paragraph(txS02TempStart.getText().toString()));
                detailSumTable.addCell(startTempCell);
                PdfPCell startHumidityCell = new PdfPCell(new Paragraph(txS02HumidityStart.getText().toString()));
                detailSumTable.addCell(startHumidityCell);

                PdfPCell endValueDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.end_value))));
                detailSumTable.addCell(endValueDescCell);
                PdfPCell endTempCell = new PdfPCell(new Paragraph(txS02TempEnd.getText().toString()));
                detailSumTable.addCell(endTempCell);
                PdfPCell endHumidityCell = new PdfPCell(new Paragraph(txS02HumidityEnd.getText().toString()));
                detailSumTable.addCell(endHumidityCell);

                PdfPCell highAlarmDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.max_limit))));
                detailSumTable.addCell(highAlarmDescCell);
                PdfPCell highTempAlarmCell = new PdfPCell(new Paragraph(txS02TempMaxLimit.getText().toString()));
                detailSumTable.addCell(highTempAlarmCell);
                PdfPCell highHumidityAlarmCell = new PdfPCell(new Paragraph(txS02HumidityMaxLimit.getText().toString()));
                detailSumTable.addCell(highHumidityAlarmCell);

                PdfPCell lowAlarmDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.min_limit))));
                detailSumTable.addCell(lowAlarmDescCell);
                PdfPCell lowTempAlarmCell = new PdfPCell(new Paragraph(txS02TempMinLimit.getText().toString()));
                detailSumTable.addCell(lowTempAlarmCell);
                PdfPCell lowHumidityAlarmCell = new PdfPCell(new Paragraph(txS02HumidityMinLimit.getText().toString()));
                detailSumTable.addCell(lowHumidityAlarmCell);

                PdfPCell averageDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.average))));
                detailSumTable.addCell(averageDescCell);
                PdfPCell averageTempCell = new PdfPCell(new Paragraph(txS02TempAverage.getText().toString()));
                detailSumTable.addCell(averageTempCell);
                PdfPCell averageHumidityCell = new PdfPCell(new Paragraph(txS02HumidityAverage.getText().toString()));
                detailSumTable.addCell(averageHumidityCell);

                PdfPCell maxDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.max))));
                detailSumTable.addCell(maxDescCell);
                PdfPCell maxTempCell = new PdfPCell(new Paragraph(txS02TempMax.getText().toString()));
                detailSumTable.addCell(maxTempCell);
                PdfPCell maxHumidityCell = new PdfPCell(new Paragraph(txS02HumidityMax.getText().toString()));
                detailSumTable.addCell(maxHumidityCell);


                PdfPCell minDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.min))));
                detailSumTable.addCell(minDescCell);
                PdfPCell minTempCell = new PdfPCell(new Paragraph(txS02TempMin.getText().toString()));
                detailSumTable.addCell(minTempCell);
                PdfPCell minHumidityCell = new PdfPCell(new Paragraph(txS02HumidityMin.getText().toString()));
                detailSumTable.addCell(minHumidityCell);

                PdfPCell overHighAlarmCountDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_high_count))));
                detailSumTable.addCell(overHighAlarmCountDescCell);
                PdfPCell overHighAlarmCountTempCell = new PdfPCell(new Paragraph(txS02TempOverHighCount.getText().toString()));
                detailSumTable.addCell(overHighAlarmCountTempCell);
                PdfPCell overHighAlarmCountHumidityCell = new PdfPCell(new Paragraph(txS02HumidityOverHighCount.getText().toString()));
                detailSumTable.addCell(overHighAlarmCountHumidityCell);

                PdfPCell overHighAlarmTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_high_time))));
                detailSumTable.addCell(overHighAlarmTimeDescCell);
                PdfPCell overHighAlarmTimeTempCell = new PdfPCell(new Paragraph(txS02TempOverHighTime.getText().toString()));
                detailSumTable.addCell(overHighAlarmTimeTempCell);
                PdfPCell overHighAlarmTimeHumidityCell = new PdfPCell(new Paragraph(txS02HumidityOverHighTime.getText().toString()));
                detailSumTable.addCell(overHighAlarmTimeHumidityCell);

                PdfPCell overLowAlarmCountDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_low_count))));
                detailSumTable.addCell(overLowAlarmCountDescCell);
                PdfPCell overLowAlarmCountTempCell = new PdfPCell(new Paragraph(txS02TempOverLowCount.getText().toString()));
                detailSumTable.addCell(overLowAlarmCountTempCell);
                PdfPCell overLowAlarmCountHumidityCell = new PdfPCell(new Paragraph(txS02HumidityOverLowCount.getText().toString()));
                detailSumTable.addCell(overLowAlarmCountHumidityCell);

                PdfPCell overLowAlarmTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_low_time))));
                detailSumTable.addCell(overLowAlarmTimeDescCell);
                PdfPCell overLowAlarmTimeTempCell = new PdfPCell(new Paragraph(txS02TempOverLowTime.getText().toString()));
                detailSumTable.addCell(overLowAlarmTimeTempCell);
                PdfPCell overLowAlarmTimeHumidityCell = new PdfPCell(new Paragraph(txS02HumidityOverLowTime.getText().toString()));
                detailSumTable.addCell(overLowAlarmTimeHumidityCell);

                document.add(detailSumTable);
                try{
                    Image imgTemp = Image.getInstance(getDownDirs() +File.separator+ getSaveName("temp"));
                    if (imgTemp != null){
                        document.newPage();
                        //设置图片缩放到A4纸的大小
                        imgTemp.scaleToFit(PageSize.A4.getWidth() , PageSize.A4.getHeight());
                        //设置图片的显示位置（居中）
                        imgTemp.setAbsolutePosition((PageSize.A4.getWidth() - imgTemp.getScaledWidth()) / 2, (PageSize.A4.getHeight() - imgTemp.getScaledHeight()) / 2);
                        document.add(imgTemp);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                try{
                    if(isHadValidHumidityData){
                        Image imgHumidity = Image.getInstance(getDownDirs() +File.separator+ getSaveName("humidity"));
                        if(imgHumidity != null){
                            document.newPage();
                            //设置图片缩放到A4纸的大小
                            imgHumidity.scaleToFit(PageSize.A4.getWidth() , PageSize.A4.getHeight());
                            //设置图片的显示位置（居中）
                            imgHumidity.setAbsolutePosition((PageSize.A4.getWidth() - imgHumidity.getScaledWidth()) / 2, (PageSize.A4.getHeight() - imgHumidity.getScaledHeight()) / 2);
                            document.add(imgHumidity);
                            document.newPage();
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }



            }else{
                PdfPTable detailSumTable = new PdfPTable(2);
                detailSumTable.setSpacingBefore(10f);
                detailSumTable.setSpacingAfter(10f);

                PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
                detailSumTable.addCell(emptyCell);

                String temHeadStr = getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")";
                PdfPCell tempHeadCell = new PdfPCell(new Paragraph(selector.process(temHeadStr)));
                detailSumTable.addCell(tempHeadCell);


                PdfPCell startValueDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.start_value))));
                detailSumTable.addCell(startValueDescCell);
                PdfPCell startTempCell = new PdfPCell(new Paragraph(txS04TempStart.getText().toString()));
                detailSumTable.addCell(startTempCell);

                PdfPCell endValueDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.end_value))));
                detailSumTable.addCell(endValueDescCell);
                PdfPCell endTempCell = new PdfPCell(new Paragraph(txS04TempEnd.getText().toString()));
                detailSumTable.addCell(endTempCell);

                PdfPCell highAlarmDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.max_limit))));
                detailSumTable.addCell(highAlarmDescCell);
                PdfPCell highTempAlarmCell = new PdfPCell(new Paragraph(txS04TempMaxLimit.getText().toString()));
                detailSumTable.addCell(highTempAlarmCell);

                PdfPCell lowAlarmDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.min_limit))));
                detailSumTable.addCell(lowAlarmDescCell);
                PdfPCell lowTempAlarmCell = new PdfPCell(new Paragraph(txS04TempMinLimit.getText().toString()));
                detailSumTable.addCell(lowTempAlarmCell);

                PdfPCell averageDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.average))));
                detailSumTable.addCell(averageDescCell);
                PdfPCell averageTempCell = new PdfPCell(new Paragraph(txS04TempAverage.getText().toString()));
                detailSumTable.addCell(averageTempCell);

                PdfPCell maxDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.max))));
                detailSumTable.addCell(maxDescCell);
                PdfPCell maxTempCell = new PdfPCell(new Paragraph(txS04TempMax.getText().toString()));
                detailSumTable.addCell(maxTempCell);


                PdfPCell minDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.min))));
                detailSumTable.addCell(minDescCell);
                PdfPCell minTempCell = new PdfPCell(new Paragraph(txS04TempMin.getText().toString()));
                detailSumTable.addCell(minTempCell);

                PdfPCell overHighAlarmCountDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_high_count))));
                detailSumTable.addCell(overHighAlarmCountDescCell);
                PdfPCell overHighAlarmCountTempCell = new PdfPCell(new Paragraph(txS04TempOverHighCount.getText().toString()));
                detailSumTable.addCell(overHighAlarmCountTempCell);

                PdfPCell overHighAlarmTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_high_time))));
                detailSumTable.addCell(overHighAlarmTimeDescCell);
                PdfPCell overHighAlarmTimeTempCell = new PdfPCell(new Paragraph(txS04TempOverHighTime.getText().toString()));
                detailSumTable.addCell(overHighAlarmTimeTempCell);

                PdfPCell overLowAlarmCountDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_low_count))));
                detailSumTable.addCell(overLowAlarmCountDescCell);
                PdfPCell overLowAlarmCountTempCell = new PdfPCell(new Paragraph(txS04TempOverLowCount.getText().toString()));
                detailSumTable.addCell(overLowAlarmCountTempCell);

                PdfPCell overLowAlarmTimeDescCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.over_low_time))));
                detailSumTable.addCell(overLowAlarmTimeDescCell);
                PdfPCell overLowAlarmTimeTempCell = new PdfPCell(new Paragraph(txS04TempOverLowTime.getText().toString()));
                detailSumTable.addCell(overLowAlarmTimeTempCell);

                document.add(detailSumTable);
                Image imgTemp = Image.getInstance(getDownDirs() +File.separator+ getSaveName("temp"));
                if (imgTemp != null){
                    document.newPage();
                    //设置图片缩放到A4纸的大小
                    imgTemp.scaleToFit(PageSize.A4.getWidth() , PageSize.A4.getHeight());
                    //设置图片的显示位置（居中）
                    imgTemp.setAbsolutePosition((PageSize.A4.getWidth() - imgTemp.getScaledWidth()) / 2, (PageSize.A4.getHeight() - imgTemp.getScaledHeight()) / 2);
                    document.add(imgTemp);
                }
            }
            int detailColumnCount = 6;
            if(reportType.equals("alarm")){
                if(deviceType.equals("S02") || deviceType.equals("S10")){
                    detailColumnCount = 6;
                }else{
                    detailColumnCount = 5;
                }
            }else{
                if(deviceType.equals("S02") || deviceType.equals("S10")){
                    detailColumnCount = 5;
                }else{
                    detailColumnCount = 4;
                }
            }
            document.newPage();
            PdfPTable detailTable = new PdfPTable(detailColumnCount);
            PdfPCell tableDateHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_date))));
            detailTable.addCell(tableDateHeadCell);
            PdfPCell tableBatteryHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_battery))));
            detailTable.addCell(tableBatteryHeadCell);
            PdfPCell tableTempHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_temp))));
            detailTable.addCell(tableTempHeadCell);
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                PdfPCell tableHumidityHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_humidity))));
                detailTable.addCell(tableHumidityHeadCell);
                PdfPCell tablePropHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_light))));
                detailTable.addCell(tablePropHeadCell);

            }else{
                PdfPCell tablePropHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_door))));
                detailTable.addCell(tablePropHeadCell);
            }
            if(reportType.equals("alarm")){
                PdfPCell tableAlarmHeadCell = new PdfPCell(new Paragraph(selector.process(getResources().getString(R.string.table_head_warn))));
                detailTable.addCell(tableAlarmHeadCell);
            }
            for(BleHisData bleHisData :showBleHisData){
                PdfPCell tableDateValueCell = new PdfPCell(new Paragraph(tableDateFormat.format(bleHisData.getDate())));
                detailTable.addCell(tableDateValueCell);
                PdfPCell tableBatteryValueCell = new PdfPCell(new Paragraph(bleHisData.getBattery() + "%"));
                detailTable.addCell(tableBatteryValueCell);
                PdfPCell tableTempValueCell = new PdfPCell(new Paragraph(BleDeviceData.getCurTemp(HistoryReportActivity.this,bleHisData.getTemp())));
                detailTable.addCell(tableTempValueCell);
                if(deviceType.equals("S02") || deviceType.equals("S10")){
                    String humidityStr = "-";
                    if(bleHisData.getHumidity() != -999){
                        humidityStr = String.format("%.0f",bleHisData.getHumidity());
                    }
                    PdfPCell tableHumidityValueCell = new PdfPCell(new Paragraph(humidityStr));
                    detailTable.addCell(tableHumidityValueCell);
                    String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_light) : getResources().getString(R.string.prop_dark);
                    PdfPCell tablePropValueCell = new PdfPCell(new Paragraph(selector.process(propValue)));
                    detailTable.addCell(tablePropValueCell);
                }else{
                    String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_door_open) : getResources().getString(R.string.prop_door_close);
                    PdfPCell tablePropValueCell = new PdfPCell(new Paragraph(selector.process(propValue)));
                    detailTable.addCell(tablePropValueCell);
                }
                if(reportType.equals("alarm")){
                    PdfPCell tableAlarmValueCell = new PdfPCell(new Paragraph(BleDeviceData.getWarnDesc(HistoryReportActivity.this,deviceType,bleHisData.getAlarm())));
                    detailTable.addCell(tableAlarmValueCell);
                }
            }
            document.add(detailTable);

            // low level
//            PdfContentByte cb = writer.getDirectContent();
//            cb.fill();
//            cb.sanityCheck();
//            Toast.makeText(HistoryReportActivity.this,"output to pdf succ",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭文档
            document.close();
        }

    }

    PdfPTable getMultiImageText(String text,String filename,boolean isImageBefore){
        PdfPTable datatable = new PdfPTable(2);
        int CellWidth[] = {20,20};
        try {
            datatable.setWidths(CellWidth);
            datatable.getDefaultCell().setBorder(1);
            datatable.setWidthPercentage(100);
            PdfPCell pCTitle = new PdfPCell(new Paragraph(text));
            pCTitle.setColspan(1);
            pCTitle.setBorder(1);
            pCTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
            pCTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
            AssetManager am = getResources().getAssets();
            InputStream is = am.open(filename);
            byte[] readBytes = new byte[1024 * 1024 * 2];
            int readLength = is.read(readBytes);
            byte[] imageByte = Arrays.copyOfRange(readBytes,0,readLength);
            PdfPCell pImage = new PdfPCell(Image.getInstance(imageByte));
            pImage.setColspan(1);
            pImage.setPadding(5);
            pImage.setBorder(1);
            pImage.setHorizontalAlignment(Element.ALIGN_CENTER);
            pImage.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if(isImageBefore){
                datatable.addCell(pImage);
                datatable.addCell(pCTitle);
            }else{
                datatable.addCell(pCTitle);
                datatable.addCell(pImage);
            }


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return datatable;
    }

    private void sendExcel(){
        try{
            delCacheFile();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // 设置邮件格式
            String fileName = getDownDirs() +File.separator+ getSaveName("xls");
            exportExcel(fileName);
            File file = new File(fileName);
            Uri fileUri;
            if(Build.VERSION.SDK_INT < 24){
                fileUri = Uri.fromFile(file);
            }else{
                fileUri = FileProvider.getUriForFile(
                        this,
                        "com.topflytech.tftble.fileprovider",
                        file);
            }
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent.createChooser(intent, "Choose Email Client");
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
//        Boolean isSmtpConfigSucc = preferences.getBoolean("smtpConfig",false);
//        if(isSmtpConfigSucc){
//            getSmtpConfig();
//            exportExcel(getSavePath("xls"));
//            showWaitingDlg("");
//            sendEmailFile("xls", new Callback() {
//                @Override
//                public void callback(StatusCode code) {
//                    waitingDlgHide();
//                    Looper.prepare();
//                    if(code == StatusCode.OK){
//                        Toast.makeText(HistoryReportActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(HistoryReportActivity.this,R.string.smtp_send_fail,Toast.LENGTH_SHORT).show();
//                    }
//                    Looper.loop();
//                    delCacheFile();
//                }
//            });
//        }else{
//            Toast.makeText(HistoryReportActivity.this,R.string.need_config_smtp,Toast.LENGTH_SHORT).show();
//            emailSetting();
//        }
    }

    private ArrayList<ArrayList<String>> getCsvData(){
        ArrayList<ArrayList<String>> result = new  ArrayList<ArrayList<String>>();
        ArrayList<String> head = new ArrayList<>();
        head.add(getResources().getString(R.string.table_head_date));
        head.add(getResources().getString(R.string.table_head_battery));
        head.add(getResources().getString(R.string.table_head_temp));
        if(deviceType.equals("S02") || deviceType.equals("S10")){
            head.add(getResources().getString(R.string.table_head_humidity));
            head.add(getResources().getString(R.string.table_head_light));
        }else{
            head.add(getResources().getString(R.string.table_head_door));
        }
        if(reportType.equals("alarm")){
            head.add(getResources().getString(R.string.table_head_warn));
        }
        result.add(head);
        for(BleHisData bleHisData :showBleHisData){
            ArrayList<String> line = new ArrayList<>();
            line.add(tableDateFormat.format(bleHisData.getDate()));
            line.add(bleHisData.getBattery() + "%");
            line.add(BleDeviceData.getCurTemp(HistoryReportActivity.this,bleHisData.getTemp()) + BleDeviceData.getCurTempUnit(HistoryReportActivity.this));
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                String humidityStr = "-";
                if(bleHisData.getHumidity() != -999){
                    humidityStr = String.format("%.0f",bleHisData.getHumidity());
                }
                line.add(humidityStr);
                String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_light) : getResources().getString(R.string.prop_dark);
                line.add(propValue);
            }else{
                String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_door_open) : getResources().getString(R.string.prop_door_close);
                line.add(propValue);
            }
            if(reportType.equals("alarm")){
                line.add(BleDeviceData.getWarnDesc(HistoryReportActivity.this,deviceType,bleHisData.getAlarm()));
            }
            result.add(line);
        }
        return result;
    }
    public boolean exportCsv(String path, ArrayList<ArrayList<String>> dataList){
        File file =new File(path);
        boolean isSucess=false;
        FileOutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;
        try {
            out = new FileOutputStream(file);
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(dataList!=null && !dataList.isEmpty()){
                for(ArrayList<String> line : dataList){
                    for(int i = 0;i < line.size();i++){
                        bw.append(line.get(i));
                        if(i != line.size() - 1){
                            bw.append(",");
                        }
                    }
                    bw.append("\r\n");
                }
            }
            isSucess=true;
        } catch (Exception e) {
            isSucess=false;
        }finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return isSucess;
    }

    private void waitingDlgHide(){
        HistoryReportActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                waitingDlg.hide();
            }
        });
    }
    private void sendCSV(){
        try{
            delCacheFile();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // 设置邮件格式
            ArrayList<ArrayList<String>> csvData = getCsvData();
            String fileName = getDownDirs() +File.separator+ getSaveName("csv");
            exportCsv(fileName,csvData);
            File file = new File(fileName);
            Uri fileUri;
            if(Build.VERSION.SDK_INT < 24){
                fileUri = Uri.fromFile(file);
            }else{
                fileUri = FileProvider.getUriForFile(
                        this,
                        "com.topflytech.tftble.fileprovider",
                        file);
            }
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent.createChooser(intent, "Choose Email Client");
            startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
//        Boolean isSmtpConfigSucc = preferences.getBoolean("smtpConfig",false);
//        if(isSmtpConfigSucc){
//            getSmtpConfig();
//            ArrayList<ArrayList<String>> csvData = getCsvData();
//            exportCsv(getSavePath("csv"),csvData);
//            showWaitingDlg("");
//            sendEmailFile("csv", new Callback() {
//                @Override
//                public void callback(StatusCode code) {
//                    waitingDlgHide();
//                    Looper.prepare();
//                    if(code == StatusCode.OK){
//                        Toast.makeText(HistoryReportActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
//                    }else{
//                        Toast.makeText(HistoryReportActivity.this,R.string.smtp_send_fail,Toast.LENGTH_SHORT).show();
//                    }
//                    Looper.loop();
//                    delCacheFile();
//                }
//            });
//        }else{
//            Toast.makeText(HistoryReportActivity.this,R.string.need_config_smtp,Toast.LENGTH_SHORT).show();
//            emailSetting();
//        }
    }


    private List<MenuItem> getMenuList() {
        List<MenuItem> list = new ArrayList<>();
//        list.add(new MenuItem(1, getResources().getString(R.string.email_setting)));
        list.add(new MenuItem(2, getResources().getString(R.string.send_pdf)));
        list.add(new MenuItem(3, getResources().getString(R.string.send_excel)));
        list.add(new MenuItem(4, getResources().getString(R.string.send_csv)));
//        list.add(new MenuItem(5, getResources().getString(R.string.savePdfToDownload)));
//        list.add(new MenuItem(6, getResources().getString(R.string.saveExcelToDownload)));
//        list.add(new MenuItem(7, getResources().getString(R.string.saveCsvToDownload)));
        return list;
    }


    public boolean exportExcel(String filePath){
        try {
            saveToGallery(tempChart, getSaveName("temp"));
            if(deviceType.equals("S02") || (deviceType.equals("S10") && isHadValidHumidityData)){
                saveToGallery(humidityChart, getSaveName("humidity"));
            }
            OutputStream os = new FileOutputStream(filePath);
            // 创建Excel工作簿
            WritableWorkbook mWritableWorkbook = Workbook.createWorkbook(os);
            // 创建Sheet表
            WritableSheet mWritableSheet =  mWritableWorkbook.createSheet("sheet1", 0);
            int rowIndex = 0;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.ble_name));
            addExcelString(mWritableSheet,1,rowIndex,deviceName);
            mWritableSheet.mergeCells( 1 , rowIndex , 2 , rowIndex );

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.ble_id));
            addExcelString(mWritableSheet,1,rowIndex,id);
            mWritableSheet.mergeCells( 1 , rowIndex , 2 , rowIndex );

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.device_model));
            addExcelString(mWritableSheet,1,rowIndex,txDeviceModel.getText().toString());
            mWritableSheet.mergeCells( 1 , rowIndex , 2 , rowIndex );

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.report_create_time));
            addExcelString(mWritableSheet,1,rowIndex,txReportCreateTime.getText().toString());
            mWritableSheet.mergeCells( 1 , rowIndex , 2 , rowIndex );

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.report_time));
            addExcelString(mWritableSheet,1,rowIndex,txReportBeginTime.getText().toString());
            addExcelString(mWritableSheet,2,rowIndex,txReportEndTime.getText().toString());

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.battery));
            addExcelString(mWritableSheet,1,rowIndex,txBatteryBegin.getText().toString());
            addExcelString(mWritableSheet,2,rowIndex,txBatteryEnd.getText().toString());

            rowIndex++;
            addExcelString(mWritableSheet,0,rowIndex,txReportProp.getText().toString());
            addExcelString(mWritableSheet,1,rowIndex,"↑:" + txOpenCount.getText().toString());
            addExcelString(mWritableSheet,2,rowIndex,"↓:" + txCloseCount.getText().toString());
            rowIndex++;

            if(deviceType.equals("S02") || deviceType.equals("S10")){
                rowIndex++;
                addExcelString(mWritableSheet,1,rowIndex,getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")");
                addExcelString(mWritableSheet,2,rowIndex,getResources().getString(R.string.table_head_humidity));

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.start_value));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempStart.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityStart.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.end_value));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempEnd.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityEnd.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.max_limit));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMaxLimit.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityMaxLimit.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.min_limit));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMinLimit.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityMinLimit.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.average));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempAverage.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityAverage.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.max));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMax.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityMax.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_high_count));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverHighCount.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityOverHighCount.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_high_time));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverHighTime.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityOverHighTime.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_low_count));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverLowCount.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityOverLowCount.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_low_time));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverLowTime.getText().toString());
                addExcelString(mWritableSheet,2,rowIndex,txS02HumidityOverLowTime.getText().toString());

                rowIndex++;
                rowIndex++;
                addExcelImage(mWritableSheet,0,rowIndex,"temp");
                rowIndex+=20;
                addExcelImage(mWritableSheet,0,rowIndex,"humidity");
                rowIndex+=20;
            }else{
                rowIndex++;
                addExcelString(mWritableSheet,1,rowIndex,getResources().getString(R.string.table_head_temp) + "(" + BleDeviceData.getCurTempUnit(HistoryReportActivity.this) + ")");


                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.start_value));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempStart.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.end_value));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempEnd.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.max_limit));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMaxLimit.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.min_limit));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMinLimit.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.average));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempAverage.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.max));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempMax.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_high_count));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverHighCount.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_high_time));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverHighTime.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_low_count));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverLowCount.getText().toString());

                rowIndex++;
                addExcelString(mWritableSheet,0,rowIndex,getResources().getString(R.string.over_low_time));
                addExcelString(mWritableSheet,1,rowIndex,txS02TempOverLowTime.getText().toString());

                rowIndex++;
                rowIndex++;
                addExcelImage(mWritableSheet,0,rowIndex,"temp");
                rowIndex+=20;
            }
            rowIndex++;
            int colIndex = 0;
            addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_date));
            colIndex++;
            addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_battery));
            colIndex++;
            addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_temp));
            if(deviceType.equals("S02") || deviceType.equals("S10")){
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_humidity));
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_light));
            }else{
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_door));
            }
            if(reportType.equals("alarm")){
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,getResources().getString(R.string.table_head_warn));
            }

            for(BleHisData bleHisData :showBleHisData){
                rowIndex++;
                colIndex = 0;
                addExcelString(mWritableSheet,colIndex,rowIndex,tableDateFormat.format(bleHisData.getDate()));
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,bleHisData.getBattery() + "%");
                colIndex++;
                addExcelString(mWritableSheet,colIndex,rowIndex,BleDeviceData.getCurTemp(HistoryReportActivity.this,bleHisData.getTemp()) + BleDeviceData.getCurTempUnit(HistoryReportActivity.this));

                if(deviceType.equals("S02") || deviceType.equals("S10")){
                    colIndex++;
                    String humidityStr = "-";
                    if(bleHisData.getHumidity() != -999){
                        humidityStr = String.format("%.0f",bleHisData.getHumidity());
                    }
                    addExcelString(mWritableSheet,colIndex,rowIndex,humidityStr);
                    colIndex++;
                    String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_light) : getResources().getString(R.string.prop_dark);
                    addExcelString(mWritableSheet,colIndex,rowIndex,propValue);
                }else{
                    colIndex++;
                    String propValue = bleHisData.getProp() == 1 ? getResources().getString(R.string.prop_door_open) : getResources().getString(R.string.prop_door_close);
                    addExcelString(mWritableSheet,colIndex,rowIndex,propValue);
                }

                if(reportType.equals("alarm")){
                    colIndex++;
                    addExcelString(mWritableSheet,colIndex,rowIndex,BleDeviceData.getWarnDesc(HistoryReportActivity.this,deviceType,bleHisData.getAlarm()));
                }
            }

            // 写入数据
            mWritableWorkbook.write();
            // 关闭文件
            mWritableWorkbook.close();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;

    }
    public void addExcelString(WritableSheet mWritableSheet,int col, int row, String text) throws WriteException {
        if (null == mWritableSheet) return;
        Label label = new Label(col, row, text);
        mWritableSheet.addCell(label);
    }

    public void addExcelImage(WritableSheet mWritableSheet,int col,int row,String fileType){
        String picPath = getDownDirs() +File.separator+ getSaveName(fileType);
        File imageFile = new File(picPath);
        if(imageFile.exists()){
            Bitmap input = BitmapFactory.decodeFile(picPath);
            mWritableSheet.addImage(new WritableImage(col,row,5,
                    18,imageFile));
        }
    }
}
