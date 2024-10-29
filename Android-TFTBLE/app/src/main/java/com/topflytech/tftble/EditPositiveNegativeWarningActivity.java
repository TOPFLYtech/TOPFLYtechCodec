package com.topflytech.tftble;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.gzuliyujiang.wheelpicker.OptionPicker;
import com.topflytech.tftble.data.SingleClickListener;
import com.topflytech.tftble.data.SingleOptionSelectClickListener;

import java.util.ArrayList;


public class EditPositiveNegativeWarningActivity extends AppCompatActivity {

    private EditText etPort, etMode, etHighVoltage,etLowVoltage,etSamplingInterval, etDitheringIntervalHigh, etDitheringIntervalLow;
    private Button btnConfirm;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private Integer highVoltage,lowVoltage,port, mode,samplingInterval, ditheringIntervalHigh, ditheringIntervalLow;
    private OptionPicker pvMode;
    private ArrayList<String> modeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modeList = new ArrayList<String>(){{
            add(getResources().getString(R.string.close));
            add(getResources().getString(R.string.open));
        }};
        setContentView(R.layout.activity_edit_positive_negative_warning);
        etPort = (EditText)findViewById(R.id.et_port);
        etMode = (EditText)findViewById(R.id.et_mode);
        etHighVoltage = (EditText)findViewById(R.id.et_high_voltage);
        etLowVoltage = (EditText)findViewById(R.id.et_low_voltage);
        etSamplingInterval = (EditText)findViewById(R.id.et_sampling_interval);
        etDitheringIntervalHigh = (EditText)findViewById(R.id.et_dithering_interval_high);
        etDitheringIntervalLow = (EditText)findViewById(R.id.et_dithering_interval_low);
        port = getIntent().getIntExtra("port",-1);
        if(port != -1){
            etPort.setText(String.valueOf(port));
        }
        mode = getIntent().getIntExtra("mode",-1);
        if(mode != -1){
            etMode.setText(modeList.get(mode));
        }
        highVoltage = getIntent().getIntExtra("highVoltage",-1);
        if(highVoltage != -1){
            etHighVoltage.setText(String.format("%.2f",highVoltage / 100.0f));
        }
        lowVoltage = getIntent().getIntExtra("lowVoltage",-1);
        if(lowVoltage != -1){
            etLowVoltage.setText(String.format("%.2f",lowVoltage / 100.0f));
        }
        samplingInterval = getIntent().getIntExtra("samplingInterval",-1);
        if(samplingInterval != -1){
            etSamplingInterval.setText(String.valueOf(samplingInterval));
        }
        ditheringIntervalHigh = getIntent().getIntExtra("ditheringIntervalHigh",-1);
        if(ditheringIntervalHigh != -1){
            etDitheringIntervalHigh.setText(String.valueOf(ditheringIntervalHigh));
        }
        ditheringIntervalLow = getIntent().getIntExtra("ditheringIntervalLow",-1);
        if(ditheringIntervalLow != -1){
            etDitheringIntervalLow.setText(String.valueOf(ditheringIntervalLow));
        }
        btnConfirm = (Button)findViewById(R.id.btn_positive_neagtive_warning_submit);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.positive_negative_warning);
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
        pvMode = new OptionPicker(this);
        pvMode.setData(modeList);
        pvMode.setOnOptionPickedListener(new SingleOptionSelectClickListener() {
            @Override
            public void onSingleOptionClick(int position, Object item) {
                mode = position;
                etMode.setText(modeList.get(Integer.valueOf(mode)));
            }
        });
        etMode.setFocusable(false);
        etMode.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
//                InputMethodManager imm = (InputMethodManager) EditPositiveNegativeWarningActivity.this.getSystemService(EditPositiveNegativeWarningActivity.this.INPUT_METHOD_SERVICE);
//                // 隐藏软键盘
//                imm.hideSoftInputFromWindow(EditPositiveNegativeWarningActivity.this.getWindow().getDecorView().getWindowToken(), 0);
                pvMode.setDefaultPosition(Integer.valueOf(mode));
                pvMode.show();
            }
        });
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String port = etPort.getText().toString().trim();
                String lowVoltage = etLowVoltage.getText().toString().trim();
                String highVoltage = etHighVoltage.getText().toString().trim();
                String ditheringIntervalHigh = etDitheringIntervalHigh.getText().toString().trim();
                String ditheringIntervalLow = etDitheringIntervalLow.getText().toString().trim();
                String samplingInterval = etSamplingInterval.getText().toString().trim();
                if( highVoltage.length() <= 0 || lowVoltage.length() <= 0 || ditheringIntervalHigh.length() <= 0 ||
                        ditheringIntervalLow.length() <= 0 || samplingInterval.length() <= 0
                    ){
                    Toast.makeText(EditPositiveNegativeWarningActivity.this,R.string.fixInput,Toast.LENGTH_SHORT).show();
                    return;
                }
                int samplingIntervalInt = Integer.valueOf(samplingInterval);
                if(samplingIntervalInt < 0 || samplingIntervalInt > 65535){
                    Toast.makeText(EditPositiveNegativeWarningActivity.this,R.string.sampling_interval_error_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                float lowVoltageFloat = Float.valueOf(lowVoltage);
                float highVoltageFloat = Float.valueOf(highVoltage);
                if (highVoltageFloat - lowVoltageFloat < 0.5){
                    Toast.makeText(EditPositiveNegativeWarningActivity.this,R.string.high_voltage_low_voltage_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(highVoltageFloat < 0 || highVoltageFloat > 32 || lowVoltageFloat < 0 || lowVoltageFloat > 32){
                    Toast.makeText(EditPositiveNegativeWarningActivity.this,R.string.voltage_range_error_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                lowVoltageFloat = lowVoltageFloat*100;
                highVoltageFloat = highVoltageFloat*100;
                int ditheringIntervalHighInt = Integer.valueOf(ditheringIntervalHigh);
                int ditheringIntervalLowInt = Integer.valueOf(ditheringIntervalLow);
                if(ditheringIntervalHighInt < 0 || ditheringIntervalHighInt > 255 || ditheringIntervalLowInt < 0 || ditheringIntervalLowInt > 255  ){
                    Toast.makeText(EditPositiveNegativeWarningActivity.this,R.string.dithering_interval_error_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("port", Integer.valueOf(port));
                intent.putExtra("mode",mode);
                intent.putExtra("lowVoltage",(int)lowVoltageFloat);
                intent.putExtra("highVoltage",(int)highVoltageFloat);
                intent.putExtra("ditheringIntervalHigh",ditheringIntervalHighInt);
                intent.putExtra("ditheringIntervalLow",ditheringIntervalLowInt);
                intent.putExtra("samplingInterval",samplingIntervalInt);


                setResult(EditActivity.RESPONSE_CHANGE_POSITIVE_NEGATIVE_WARNING,intent);
                finish();
            }
        });
    }
}
