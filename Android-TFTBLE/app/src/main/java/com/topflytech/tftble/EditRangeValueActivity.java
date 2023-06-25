package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


        switchLow.setSwitchStatus(lowValueOpen);
        switchHigh.setSwitchStatus(highValueOpen);
        if(highValueOpen){
            etHigh.setText(highValue);
        }
        if(lowValueOpen){
            etLow.setText(lowValue);
        }
        if(editType.equals("temp")){
            tvHead.setText(R.string.temp_alarm);
            etHigh.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            etLow.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }else if(editType.equals("humidity")){
            tvHead.setText(R.string.humidity_alarm);
            etHigh.setInputType(InputType.TYPE_CLASS_NUMBER);
            etLow.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            saveHighValue = Float.valueOf(Float.valueOf(highValue) * 10).intValue();
        }
        if (!lowValueOpen  || lowValue.length() == 0){
            saveLowValue = 4095;
        }else{
            saveLowValue = Float.valueOf(Float.valueOf(lowValue) * 10).intValue();
        }
        if ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue > 1000) ||
                (saveLowValue != 4095 && saveLowValue < -400)){
            Toast.makeText(EditRangeValueActivity.this,R.string.temp_range_error_warning,Toast.LENGTH_SHORT).show();
            return false;
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
            saveHighValue = Integer.valueOf(highValue);
        }
        if (!lowValueOpen  || lowValue.length() == 0){
            saveLowValue = 4095;
        }else{
            saveLowValue = Integer.valueOf(lowValue);
        }
        if  ((saveHighValue <= saveLowValue && saveLowValue != 4095) || (saveHighValue != 4095 && saveHighValue > 1000) ||
                (saveLowValue != 4095 && saveLowValue < 0)){
            Toast.makeText(EditRangeValueActivity.this,R.string.opt_of_range_warning,Toast.LENGTH_SHORT).show();
            return false;
        }
        Intent intent = new Intent();
        intent.putExtra("highValue",saveHighValue);
        intent.putExtra("lowValue",saveLowValue);
        setResult(EditActivity.RESPONSE_CHANGE_HUMIDITY,intent);
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
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
