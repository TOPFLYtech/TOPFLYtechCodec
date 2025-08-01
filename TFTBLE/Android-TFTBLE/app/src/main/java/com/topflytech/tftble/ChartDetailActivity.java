package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
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
import com.topflytech.tftble.data.BleHisData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChartDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private LineChart chart;
    private float tempAlarmUp,tempAlarmDown,humidityAlarmUp,humidityAlarmDown;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private ArrayList<ArrayList<byte[]>> orignHistoryList = new ArrayList<ArrayList<byte[]>>();
    private ArrayList<BleHisData> showBleHisData = new ArrayList<>();
    private String deviceType="S02",deviceName="TH121",mac="221322112312",reportType,id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_detail);
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        reportType = intent.getStringExtra("reportType");
        deviceType = intent.getStringExtra("deviceType");
        tempAlarmUp = intent.getFloatExtra("tempAlarmUp",4095);
        tempAlarmDown = intent.getFloatExtra("tempAlarmDown",4095);
        humidityAlarmUp = intent.getFloatExtra("humidityAlarmUp",4095);
        humidityAlarmDown = intent.getFloatExtra("humidityAlarmDown",4095);
        id = intent.getStringExtra("id");
        initActionbar();
        orignHistoryList.clear();
        showBleHisData.clear();
        showBleHisData = EditActivity.allBleHisData;
        initChart();

    }
    private void initChart() {
        {   // // Chart Style // //
            chart = findViewById(R.id.detail_chart);

            // background color
            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(true);

            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setScaleXEnabled(true);
            chart.setScaleYEnabled(true);
            chart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });

            chart.setPinchZoom(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();
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
                                result = mFormat2.format(now);
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

            // vertical grid lines
//            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setDrawAxisLine(true);
        }
        // add data

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(getResources().getColor(R.color.line_color1));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(getResources().getColor(R.color.line_color2));
        setData();

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

        if(humidityAlarmUp != 4095){
            LimitLine limitLineHighHumidity = new LimitLine(humidityAlarmUp, getResources().getString(R.string.high_humidity_alarm));
            limitLineHighHumidity.setLineWidth(4f);
            limitLineHighHumidity.setLineColor(getResources().getColor(R.color.line_color2));
            limitLineHighHumidity.enableDashedLine(10f, 10f, 0f);
            limitLineHighHumidity.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            limitLineHighHumidity.setTextSize(10f);
            rightAxis.addLimitLine(limitLineHighHumidity);

        }
        if(humidityAlarmDown != 4095){
            LimitLine limitLineLowHumidity = new LimitLine(humidityAlarmDown, getResources().getString(R.string.low_humidity_alarm));
            limitLineLowHumidity.setLineWidth(4f);
            limitLineLowHumidity.setLineColor(getResources().getColor(R.color.line_color2));
            limitLineLowHumidity.enableDashedLine(10f, 10f, 0f);
            limitLineLowHumidity.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            limitLineLowHumidity.setTextSize(10f);
            rightAxis.addLimitLine(limitLineLowHumidity);
        }

        // draw points over time
        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend legend = chart.getLegend();
        //设置显示类型，LINE CIRCLE SQUARE EMPTY 等等 多种方式，查看LegendForm 即可
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        //显示位置 左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //是否绘制在图表里面
        legend.setDrawInside(false);
    }
    private void setData() {

        ArrayList<Entry> tempValues = new ArrayList<>();

        for (int i = 0; i < showBleHisData.size(); i++) {
            BleHisData bleHisData = showBleHisData.get(i);
            float val = bleHisData.getTemp();
            tempValues.add(new Entry(i, val, getResources().getDrawable(R.mipmap.star)));
        }

        ArrayList<Entry> humidityValues = new ArrayList<>();
        LineDataSet tempSet,humiditySet;
        for (int i = 0; i < showBleHisData.size(); i++) {
            BleHisData bleHisData = showBleHisData.get(i);
            float val = bleHisData.getHumidity();
            humidityValues.add(new Entry(i, val, getResources().getDrawable(R.mipmap.star)));
        }
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            tempSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            humiditySet = (LineDataSet) chart.getData().getDataSetByIndex(1);
            tempSet.setValues(tempValues);
            humiditySet.setValues(humidityValues);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
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
//            tempSet.enableDashedHighlightLine(10f, 5f, 0f);
            tempSet.setDrawFilled(true);
            tempSet.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            dataSets.add(tempSet);
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
            humiditySet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//            humiditySet.enableDashedHighlightLine(10f, 5f, 0f);
            humiditySet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            humiditySet.setDrawFilled(true);
            humiditySet.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            dataSets.add(humiditySet);
            LineData data = new LineData(dataSets);

            chart.setData(data);
        }


    }
    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(id);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setVisibility(View.INVISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
