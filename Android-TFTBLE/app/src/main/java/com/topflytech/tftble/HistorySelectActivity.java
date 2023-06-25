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
import com.loper7.date_time_picker.dialog.CardDatePickerDialog;
import com.topflytech.tftble.data.DateSpinnerAdapter;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class HistorySelectActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private boolean selectEndDate = false;
    private TextView startDateText;
    private TextView endDateText;
    private String dateFormatStr = "yyyy-MM-dd HH:mm:ss";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr, Locale.ENGLISH);
    private LinearLayout startDateSpan;
    private LinearLayout endDateSpan;
    private DateSpinnerAdapter mDateSpinnerAdapter;
    private Spinner dateSpinner;
    private Button btnSelectDate;
    private String mac,software,deviceType,reportType,id;
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
        initSpinner();
    }
    View.OnClickListener submitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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
                CardDatePickerDialog datePickerDialog = new CardDatePickerDialog.Builder(HistorySelectActivity.this)
                        .setTitle(getString(R.string.start_date))
                        .setOnCancel(getString(R.string.cancel),null)
                        .showBackNow(false)
                        .showDateLabel(false)
                        .showFocusDateInfo(false)
                        .setLabelText("","","","","","")
                        .setOnChoose(getString(R.string.confirm), new Function1<Long, Unit>() {
                            @Override
                            public Unit invoke(Long aLong) {
                                startDateText.setText(dateFormat.format(new Date(aLong)));
                                return null;
                            }
                        })
                        .build();
                datePickerDialog.show();

            }
        });

        endDateSpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardDatePickerDialog datePickerDialog = new CardDatePickerDialog.Builder(HistorySelectActivity.this)
                        .setTitle(getString(R.string.end_date))
                        .setOnCancel(getString(R.string.cancel),null)
                        .showBackNow(false)
                        .showDateLabel(false)
                        .showFocusDateInfo(false)
                        .setLabelText("","","","","","")
                        .setOnChoose(getString(R.string.confirm), new Function1<Long, Unit>() {
                            @Override
                            public Unit invoke(Long aLong) {
                                endDateText.setText(dateFormat.format(new Date(aLong)));
                                return null;
                            }
                        })
                        .build();
                datePickerDialog.show();
            }
        });

        startDateText.setText(dateFormat.format(DateSpinnerAdapter.getStartDate(0)));
        endDateText.setText(dateFormat.format(DateSpinnerAdapter.getEndDate(0)));
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
                DateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
                startDateText.setText(dateFormat.format(startDate));
                endDateText.setText(dateFormat.format(endDate));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
