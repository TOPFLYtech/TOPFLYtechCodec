package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.topflytech.tftble.data.DateSpinnerAdapter;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HistorySelectActivity extends AppCompatActivity implements  DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private boolean selectEndDate = false;
    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";
    private TextView startDateText;
    private TextView endDateText;

    private LinearLayout startDateSpan;
    private LinearLayout endDateSpan;
    private DateSpinnerAdapter mDateSpinnerAdapter;
    private Spinner dateSpinner;
    final Calendar startCalendar = Calendar.getInstance();
    final Calendar endCalendar = Calendar.getInstance();
    private String mac,software,deviceType,reportType,id;
    private Button btnSelectDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_select);
        Intent intent = getIntent();
        mac = intent.getStringExtra("mac");
        id = intent.getStringExtra("id");
        software = intent.getStringExtra("software");
        deviceType = intent.getStringExtra("deviceType");
        reportType = intent.getStringExtra("reportType");
        btnSelectDate = (Button)findViewById(R.id.btn_select_date_submit);
        btnSelectDate.setOnClickListener(submitClick);
        initActionbar();
    }

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.select_date_view_title);
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
        initDateTexts();
        initDateTimePickers();
        initSpinner();
    }
    View.OnClickListener submitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH : mm");
            String textFromDate = startDateText.getText().toString();
            String textToDate = endDateText.getText().toString();
            try {
                final Date startDate = dateFormat.parse(textFromDate);
                final Date endDate = dateFormat.parse(textToDate);

                if(startDate.getTime() > endDate.getTime()){
                    Toast.makeText(HistorySelectActivity.this,R.string.str_alert_start_date_must_less_than_end_date,Toast.LENGTH_SHORT).show();
                    return ;
                }
                if(reportType.equals("history")){
                    Intent intent = new Intent();
                    intent.putExtra("startDate",startDate.getTime());
                    intent.putExtra("endDate",endDate.getTime());
                    setResult(EditActivity.RESPONSE_READ_HISTORY_TIME,intent);
                    finish();
                }else if(reportType.equals("alarm")){
                    Intent intent = new Intent();
                    intent.putExtra("startDate",startDate.getTime());
                    intent.putExtra("endDate",endDate.getTime());
                    setResult(EditActivity.RESPONSE_READ_ALARM_TIME,intent);
                    finish();
                }
//                presentReviewActivity(mCurrentImei, startDate, endDate);
            } catch (ParseException e) {
                e.printStackTrace();
                SweetAlertDialog wrongDateWarningDlg = new SweetAlertDialog(HistorySelectActivity.this, SweetAlertDialog.ERROR_TYPE);
                wrongDateWarningDlg.getProgressHelper().setBarColor(Color.parseColor("#18c2d6"));
                wrongDateWarningDlg.setTitleText(getResources().getString(R.string.wrong_date_warning));
                wrongDateWarningDlg.setCancelable(false);
                wrongDateWarningDlg.setConfirmText(getResources().getString(R.string.confirm));
                wrongDateWarningDlg.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                     sweetAlertDialog.hide();
                    }
                });
                wrongDateWarningDlg.show();
            }
        }
    };
    private void initDateTexts() {
        startDateText = (TextView) findViewById(R.id.text_start_date);
        endDateText = (TextView) findViewById(R.id.text_end_date);
        startDateSpan = (LinearLayout)findViewById(R.id.span_start_date);
        endDateSpan = (LinearLayout)findViewById(R.id.span_end_date);
        startDateSpan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectEndDate = false;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String textFromDate = startDateText.getText().toString();
                try {
                    Date startDateTmp = dateFormat.parse(textFromDate);
                    Calendar calendarTmp = Calendar.getInstance();
                    calendarTmp.setTime(startDateTmp);
                    datePickerDialog.initialize(HistorySelectActivity.this, calendarTmp.get(Calendar.YEAR), calendarTmp.get(Calendar.MONTH), calendarTmp.get(Calendar.DAY_OF_MONTH));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
            }
        });

        endDateSpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectEndDate = true;
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String textFromDate = endDateText.getText().toString();
                try {
                    Date endDateTmp = dateFormat.parse(textFromDate);
                    Calendar calendarTmp = Calendar.getInstance();
                    calendarTmp.setTime(endDateTmp);
                    datePickerDialog.initialize(HistorySelectActivity.this, calendarTmp.get(Calendar.YEAR), calendarTmp.get(Calendar.MONTH), calendarTmp.get(Calendar.DAY_OF_MONTH));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);
            }
        });

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH : mm");
        startDateText.setText(dateFormat.format(DateSpinnerAdapter.getStartDate(0)));
        endDateText.setText(dateFormat.format(DateSpinnerAdapter.getEndDate(0)));
    }

    private void initDateTimePickers() {
        datePickerDialog= DatePickerDialog.newInstance(HistorySelectActivity.this, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));
        timePickerDialog= TimePickerDialog.newInstance(HistorySelectActivity.this, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), true, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE));
//        datePickerDialog.setVibrate(true);

        datePickerDialog.setYearRange(2014, 2028);
//        datePickerDialog.setCloseOnSingleTapDay(true);
//        timePickerDialog.setVibrate(true);
//        timePickerDialog.setCloseOnSingleTapMinute(false);
        datePickerDialog.setOnDateSetListener(this);
        timePickerDialog.setOnTimeSetListener(this);
    }

    private void initSpinner() {


        mDateSpinnerAdapter = new DateSpinnerAdapter(HistorySelectActivity.this);
        dateSpinner = (Spinner)findViewById(R.id.date_spinner);
        dateSpinner.setAdapter(mDateSpinnerAdapter);
        dateSpinner.setSelection(0,true);

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Date startDate = DateSpinnerAdapter.getStartDate(position);
                Date endDate = DateSpinnerAdapter.getEndDate(position);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH : mm");
                startDateText.setText(dateFormat.format(startDate));
                endDateText.setText(dateFormat.format(endDate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        try {
            startCalendar.set(year, monthOfYear, dayOfMonth);
            endCalendar.set(yearEnd, monthOfYearEnd, dayOfMonthEnd);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH : mm");
            String startTextFromDate = startDateText.getText().toString();
            String endTextFromDate = endDateText.getText().toString();;

            Date startDateTmp = dateFormat.parse(startTextFromDate);
            Calendar startCalendarTmp = Calendar.getInstance();
            startCalendarTmp.setTime(startDateTmp);
            Date endDateTmp = dateFormat.parse(endTextFromDate);
            Calendar endCalendarTmp = Calendar.getInstance();
            endCalendarTmp.setTime(endDateTmp);
            timePickerDialog.initialize(HistorySelectActivity.this, startCalendarTmp.get(Calendar.HOUR_OF_DAY), startCalendarTmp.get(Calendar.MINUTE), endCalendarTmp.get(Calendar.HOUR_OF_DAY),endCalendarTmp.get(Calendar.MINUTE),true);
        }catch (Exception e){
        }

        timePickerDialog.show(getFragmentManager(), TIMEPICKER_TAG);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        startCalendar.set(Calendar.MINUTE, minute);
        endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDayEnd);
        endCalendar.set(Calendar.MINUTE, minuteEnd);
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH : mm");
        try {
            ((TextView) findViewById(R.id.text_end_date)).setText(dateFormat.format(endCalendar.getTime()), TextView.BufferType.EDITABLE);
            findViewById(R.id.text_end_date).setTag(startCalendar.getTime());
            ((TextView) findViewById(R.id.text_start_date)).setText(dateFormat.format(startCalendar.getTime()), TextView.BufferType.EDITABLE);
            findViewById(R.id.text_start_date).setTag(startCalendar.getTime());

        } catch (Exception e) {
        }
    }
}
