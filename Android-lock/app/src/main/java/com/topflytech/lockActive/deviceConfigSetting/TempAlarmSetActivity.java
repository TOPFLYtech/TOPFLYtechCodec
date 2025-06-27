package com.topflytech.lockActive.deviceConfigSetting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topflytech.lockActive.EditActivity;
import com.topflytech.lockActive.R;
import com.topflytech.lockActive.data.BleDeviceData;

public class TempAlarmSetActivity extends AppCompatActivity {

    private EditText etTempHigh, etTempLow;
    private Button btnSubmit;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private void initActionBar(){
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.temp_alarm_set + "(" + BleDeviceData.getCurTempUnit(TempAlarmSetActivity.this) +")");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        rightButton.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_alarm_set);
        initActionBar();
        int tempHigh = getIntent().getIntExtra("tempHigh",-1);
        int tempLow = getIntent().getIntExtra("tempLow",-1);
        etTempHigh = findViewById(R.id.et_temp_high);
        etTempLow = findViewById(R.id.et_temp_low);
        btnSubmit = findViewById(R.id.btn_submit);
        if(tempHigh != -1){
            etTempHigh.setText(BleDeviceData.getCurTemp(TempAlarmSetActivity.this, tempHigh/100));
        }
        if(tempLow != -1){
            etTempLow.setText(BleDeviceData.getCurTemp(TempAlarmSetActivity.this, tempLow/100));
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitValues();
            }
        });
    }

    private void submitValues() {
        String tempHighStr = etTempHigh.getText().toString();
        String tempLowStr = etTempLow.getText().toString();

        if (tempHighStr.isEmpty() || tempLowStr.isEmpty()  ) {
            Toast.makeText(this, R.string.fix_input, Toast.LENGTH_SHORT).show();
            return;
        }

        float tempHigh = Float.parseFloat(tempHighStr);
        float tempLow = Float.parseFloat(tempLowStr);
        int tempHighCelsius = (int)Float.parseFloat(BleDeviceData.getSourceTemp(this, tempHigh * 100));
        int tempLowCelsius = (int)Float.parseFloat(BleDeviceData.getSourceTemp(this, tempLow*100));

        if (tempHighCelsius <= tempLowCelsius) {
            Toast.makeText(this, R.string.temp_high_low_warning, Toast.LENGTH_SHORT).show();
            return;
        }


        if (tempHighCelsius < -4000 || tempHighCelsius > 8500 || tempLowCelsius < -4000 || tempLowCelsius > 8500) {
            if(BleDeviceData.getCurTempUnit(TempAlarmSetActivity.this).equals("℃")){
                Toast.makeText(this, R.string.temp_range_warning_1, Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.temp_range_warning_2, Toast.LENGTH_SHORT).show();

            }
            return;
        }


        Intent intent = new Intent();
        intent.putExtra("tempHigh",tempHighCelsius);
        intent.putExtra("tempLow",tempLowCelsius);
        setResult(EditActivity.RESPONSE_CHANGE_TEMP_ALARM,intent);
        finish();
    }
}