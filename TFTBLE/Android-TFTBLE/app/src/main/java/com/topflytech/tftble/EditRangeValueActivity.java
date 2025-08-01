package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topflytech.tftble.data.BleDeviceData;
import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.view.SwitchButton;

public class EditRangeValueActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private EditText etHigh,etLow;
    private SwitchButton switchHigh,switchLow;
    private Button btnConfirm;

    private String editType;
    private String highValue,lowValue;
    private boolean highValueOpen,lowValueOpen;
    private String deviceType;

    private int extSensorType = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_range_value);
        initActionbar();
        etHigh = (EditText)findViewById(R.id.et_high);
        etLow = (EditText)findViewById(R.id.et_low);
        btnConfirm = (Button)findViewById(R.id.btn_pwd_submit);
        switchHigh = (SwitchButton)findViewById(R.id.btn_switch_high);
        switchLow = (SwitchButton)findViewById(R.id.btn_switch_low);
        Intent intent = getIntent();
        editType = intent.getStringExtra("editType");
        highValue = intent.getStringExtra("highValue");
        lowValue = intent.getStringExtra("lowValue");
        highValueOpen = intent.getBooleanExtra("highValueOpen",false);
        lowValueOpen = intent.getBooleanExtra("lowValueOpen",false);
        deviceType = intent.getStringExtra("deviceType");
        extSensorType = intent.getIntExtra("extSensorType",0);
        switchLow.setSwitchStatus(lowValueOpen);
        switchHigh.setSwitchStatus(highValueOpen);
        if(highValueOpen){
            etHigh.setText(highValue);
        }
        if(lowValueOpen){
            etLow.setText(lowValue);
        }
        if(editType.equals("temp")){
            tvHead.setText(getResources().getString(R.string.temp_alarm) + "(" + BleDeviceData.getCurTempUnit(EditRangeValueActivity.this) +")");
            etHigh.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etLow.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }else if(editType.equals("humidity")){
            tvHead.setText(R.string.humidity_alarm);
            etHigh.setInputType(InputType.TYPE_CLASS_NUMBER);
            etLow.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        SwitchButton.OnSwitchChangeListener sbClick = new SwitchButton.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(boolean open) {
                if(!switchHigh.getSwitchStatus() && !switchLow.getSwitchStatus()){
                    etHigh.setText("");
                    etLow.setText("");
                }
            }
        };
        switchHigh.setOnSwitchChangeListener(sbClick);
        switchLow.setOnSwitchChangeListener(sbClick);
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if(editType.equals("temp")){
                    if(!submitTempAlarm()){
                        return;
                    }
                }else if(editType.equals("humidity")){
                    if(!submitHumidityAlarm()){
                        return;
                    }
                }
                finish();
            }
        });
    }

    private boolean submitTempAlarm(){
        String highValue = etHigh.getText() != null ? etHigh.getText().toString().trim() : "";
        String lowValue = etLow.getText() != null ? etLow.getText().toString().trim() : "";
        boolean highValueOpen = switchHigh.getSwitchStatus();
        boolean lowValueOpen = switchLow.getSwitchStatus();
//        if ((lowValue.length() == 0 && highValueOpen) || (highValue.length() == 0 && lowValueOpen)){
//            Toast.makeText(EditRangeValueActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
//            return false;
//        }
        if (!highValueOpen && highValue.length() > 0){
            Toast.makeText(EditRangeValueActivity.this,R.string.high_temp_warning,Toast.LENGTH_SHORT).show();
            return false;
        }
        if (highValueOpen && highValue.length() == 0){
            Toast.makeText(EditRangeValueActivity.this,R.string.high_temp_warning2,Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!lowValueOpen && lowValue.length() > 0){
            Toast.makeText(EditRangeValueActivity.this,R.string.low_temp_warning,Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lowValueOpen && lowValue.length() == 0){
            Toast.makeText(EditRangeValueActivity.this,R.string.low_temp_warning2,Toast.LENGTH_SHORT).show();
            return false;
        }
        int saveHighValue,saveLowValue;
        if (!highValueOpen || highValue.length() == 0){
            saveHighValue = 4095;
        }else{
            try{
                saveHighValue = Float.valueOf(Float.valueOf(BleDeviceData.getSourceTemp(EditRangeValueActivity.this,Float.valueOf(highValue) ) ) * 10).intValue();
            }catch (Exception ex){
                Toast.makeText(EditRangeValueActivity.this,R.string.value_must_be_number,Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (!lowValueOpen  || lowValue.length() == 0){
            saveLowValue = 4095;
        }else{
            try{
                saveLowValue = Float.valueOf(Float.valueOf(BleDeviceData.getSourceTemp(EditRangeValueActivity.this,Float.valueOf(lowValue) ) ) * 10).intValue();
            }catch (Exception ex){
                Toast.makeText(EditRangeValueActivity.this,R.string.value_must_be_number,Toast.LENGTH_SHORT).show();
                return false;
            }

        }

        if(saveHighValue <= saveLowValue && saveLowValue != 4095 && saveHighValue != 4095){
            Toast.makeText(EditRangeValueActivity.this,R.string.high_must_great_than_low,Toast.LENGTH_SHORT).show();
            return false;
        }
        if(deviceType.equals("S08")){
            if ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 800) ||
                    (saveLowValue != 4095 && saveLowValue <= -300)){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EditRangeValueActivity.this);
                String tempUnit = preferences.getString("tempUnit","0");
                if(tempUnit.equals("0")){
                    Toast.makeText(EditRangeValueActivity.this,R.string.s08_temp_range_error_warning,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditRangeValueActivity.this,R.string.s08_temp_range_error_warning1,Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }else if(deviceType.equals("S10")){
            if(extSensorType == 1){
                if ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 1500) ||
                        (saveLowValue != 4095 && saveLowValue <= -550)){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EditRangeValueActivity.this);
                    String tempUnit = preferences.getString("tempUnit","0");
                    if(tempUnit.equals("0")){
                        Toast.makeText(EditRangeValueActivity.this,R.string.s10_temp_range_error_warning,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(EditRangeValueActivity.this,R.string.s10_temp_range_error_warning1,Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            }else if (extSensorType == 2 || extSensorType == 3){
                if ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 1300) ||
                        (saveLowValue != 4095 && saveLowValue <= -450)){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EditRangeValueActivity.this);
                    String tempUnit = preferences.getString("tempUnit","0");
                    if(tempUnit.equals("0")){
                        Toast.makeText(EditRangeValueActivity.this,R.string.s10_gxts03_temp_range_error_warning,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(EditRangeValueActivity.this,R.string.s10_gxts03_temp_range_error_warning1,Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            }

        }else{
            if ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 1000) ||
                    (saveLowValue != 4095 && saveLowValue <= -400)){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EditRangeValueActivity.this);
                String tempUnit = preferences.getString("tempUnit","0");
                if(tempUnit.equals("0")){
                    Toast.makeText(EditRangeValueActivity.this,R.string.temp_range_error_warning,Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(EditRangeValueActivity.this,R.string.temp_range_error_warning1,Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }

        Intent intent = new Intent();
        intent.putExtra("highValue",saveHighValue);
        intent.putExtra("lowValue",saveLowValue);
        setResult(EditActivity.RESPONSE_CHANGE_TEMP,intent);
        return true;
    }

    private boolean submitHumidityAlarm(){
        String highValue = etHigh.getText() != null ? etHigh.getText().toString().trim() : "";
        String lowValue = etLow.getText() != null ? etLow.getText().toString().trim() : "";
        boolean highValueOpen = switchHigh.getSwitchStatus();
        boolean lowValueOpen = switchLow.getSwitchStatus();
//        if ((lowValue.length() == 0 && highValueOpen) || (highValue.length() == 0 && lowValueOpen)){
//            Toast.makeText(EditRangeValueActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
//            return false;
//        }
        try{
            if (!highValueOpen && highValue.length() > 0){
                Toast.makeText(EditRangeValueActivity.this,R.string.high_humidity_warning,Toast.LENGTH_SHORT).show();
                return false;
            }
            if (highValueOpen && highValue.length() == 0){
                Toast.makeText(EditRangeValueActivity.this,R.string.high_humidity_warning2,Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!lowValueOpen && lowValue.length() > 0){
                Toast.makeText(EditRangeValueActivity.this,R.string.low_humidity_warning,Toast.LENGTH_SHORT).show();
                return false;
            }

            if (lowValueOpen && lowValue.length() == 0){
                Toast.makeText(EditRangeValueActivity.this,R.string.low_humidity_warning2,Toast.LENGTH_SHORT).show();
                return false;
            }
            int saveHighValue,saveLowValue;
            if (!highValueOpen  || highValue.length() == 0){
                saveHighValue = 4095;
            }else{
                try{
                    saveHighValue = Integer.valueOf(highValue);
                }catch (Exception ex){
                    Toast.makeText(EditRangeValueActivity.this,R.string.value_must_be_number,Toast.LENGTH_SHORT).show();
                    return false;
                }

            }
            if (!lowValueOpen  || lowValue.length() == 0){
                saveLowValue = 4095;
            }else{
                try{
                    saveLowValue = Integer.valueOf(lowValue);
                }catch (Exception ex){
                    Toast.makeText(EditRangeValueActivity.this,R.string.value_must_be_number,Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            if(saveHighValue <= saveLowValue && saveLowValue != 4095 && saveHighValue != 4095){
                Toast.makeText(EditRangeValueActivity.this,R.string.high_must_great_than_low,Toast.LENGTH_SHORT).show();
                return false;
            }
            if  ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue >= 100) ||
                    (saveLowValue != 4095 && saveLowValue < 0)){
                Toast.makeText(EditRangeValueActivity.this,R.string.opt_of_range_warning,Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent();
            intent.putExtra("highValue",saveHighValue);
            intent.putExtra("lowValue",saveLowValue);
            setResult(EditActivity.RESPONSE_CHANGE_HUMIDITY,intent);

        }catch (Exception e){
            Toast.makeText(EditRangeValueActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setVisibility(View.INVISIBLE);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
    }
}
