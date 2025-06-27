package com.topflytech.lockActive.deviceConfigSetting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topflytech.lockActive.EditActivity;
import com.topflytech.lockActive.R;

public class AlarmOpenSetActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private CheckBox checkboxChargingAlarm;
    private CheckBox checkboxOvervoltageAlarm;
    private CheckBox checkboxLowBatteryAlarm;
    private CheckBox checkboxHighTemperatureAlarm;
    private CheckBox checkboxLowTemperatureAlarm;
    private CheckBox checkboxLockOpenAlarm;
    private CheckBox checkboxBackCoverOpenAlarm;
    private CheckBox checkboxGpsLocationAlarm;
    private CheckBox checkboxGpsInterferenceAlarm;
    private CheckBox checkboxLockEventAlarm;
    private TextView byteValueText;

    private int alarmOpenSet = 0;
    private void initActionBar(){
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.alarm_set );
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setImageResource(R.mipmap.ic_submit);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        rightButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("alarmOpenSet", alarmOpenSet);
                setResult(EditActivity.RESPONSE_CHANGE_ALARM_OPEN_SET,intent);
                finish();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_open_set);
        initActionBar();
        checkboxChargingAlarm = findViewById(R.id.checkbox_charging_alarm);
        checkboxOvervoltageAlarm = findViewById(R.id.checkbox_overvoltage_alarm);
        checkboxLowBatteryAlarm = findViewById(R.id.checkbox_low_battery_alarm);
        checkboxHighTemperatureAlarm = findViewById(R.id.checkbox_high_temperature_alarm);
        checkboxLowTemperatureAlarm = findViewById(R.id.checkbox_low_temperature_alarm);
        checkboxLockOpenAlarm = findViewById(R.id.checkbox_lock_open_alarm);
        checkboxBackCoverOpenAlarm = findViewById(R.id.checkbox_back_cover_open_alarm);
        checkboxGpsLocationAlarm = findViewById(R.id.checkbox_gps_location_alarm);
        checkboxGpsInterferenceAlarm = findViewById(R.id.checkbox_gps_interference_alarm);
        checkboxLockEventAlarm = findViewById(R.id.checkbox_lock_event_alarm);
        byteValueText = findViewById(R.id.byte_value_text);
        alarmOpenSet = getIntent().getIntExtra("alarmOpenSet", 0);
        byteValueText.setVisibility(View.GONE);
        initializeCheckboxes();
        setupCheckboxListeners();
    }
    private void initializeCheckboxes() {
        checkboxChargingAlarm.setChecked((alarmOpenSet & (1 << 0)) != 0);
        checkboxOvervoltageAlarm.setChecked((alarmOpenSet & (1 << 1)) != 0);
        checkboxLowBatteryAlarm.setChecked((alarmOpenSet & (1 << 2)) != 0);
        checkboxHighTemperatureAlarm.setChecked((alarmOpenSet & (1 << 3)) != 0);
        checkboxLowTemperatureAlarm.setChecked((alarmOpenSet & (1 << 4)) != 0);
        checkboxLockOpenAlarm.setChecked((alarmOpenSet & (1 << 5)) != 0);
        checkboxBackCoverOpenAlarm.setChecked((alarmOpenSet & (1 << 6)) != 0);
        checkboxGpsLocationAlarm.setChecked((alarmOpenSet & (1 << 7)) != 0);
        checkboxGpsInterferenceAlarm.setChecked((alarmOpenSet & (1 << 8)) != 0);
        checkboxLockEventAlarm.setChecked((alarmOpenSet & (1 << 9)) != 0);
    }
    private void setupCheckboxListeners() {
        checkboxChargingAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(0, isChecked);
            }
        });

        checkboxOvervoltageAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(1, isChecked);
            }
        });

        checkboxLowBatteryAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(2, isChecked);
            }
        });

        checkboxHighTemperatureAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(3, isChecked);
            }
        });

        checkboxLowTemperatureAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(4, isChecked);
            }
        });

        checkboxLockOpenAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(5, isChecked);
            }
        });

        checkboxBackCoverOpenAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(6, isChecked);
            }
        });

        checkboxGpsLocationAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(7, isChecked);
            }
        });

        checkboxGpsInterferenceAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(8, isChecked);
            }
        });

        checkboxLockEventAlarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateByteValue(9, isChecked);
            }
        });
    }

    private void updateByteValue(int bitPosition, boolean isChecked) {
        if (isChecked) {
            alarmOpenSet |= (1 << bitPosition);
        } else {
            alarmOpenSet &= ~(1 << bitPosition);
        }
        byteValueText.setText("Byte Value: " + alarmOpenSet);
    }
}