package com.topflytech.tftble;

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

import com.topflytech.tftble.data.SingleClickListener;

public class EditSecondPulseDelayActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;

    private EditText etStartLevel,etHighLevelPulseWidthTime,etLowLevelPulseWidthTime,etPulseCount;
    private Button btnConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_second_pulse_delay);
        initActionbar();
        etStartLevel = (EditText)findViewById(R.id.et_start_level) ;
        etHighLevelPulseWidthTime = (EditText)findViewById(R.id.et_high_level_pulse_width_time) ;
        etLowLevelPulseWidthTime = (EditText)findViewById(R.id.et_low_level_pulse_width_time) ;
        etPulseCount = (EditText)findViewById(R.id.et_pulse_count) ;
        btnConfirm = (Button) findViewById(R.id.btn_relay_pulse_confirm);
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String startLevel = etStartLevel.getText().toString();
                String highLevelPulseWidthTime = etHighLevelPulseWidthTime.getText().toString();
                String lowLevelPulseWidthTime = etLowLevelPulseWidthTime.getText().toString();
                String pulseCount = etPulseCount.getText().toString();
                if(startLevel == null || startLevel.trim().length() == 0 ||
                        highLevelPulseWidthTime == null || highLevelPulseWidthTime.trim().length() == 0||
                        lowLevelPulseWidthTime == null || lowLevelPulseWidthTime.trim().length() == 0||
                        pulseCount == null || pulseCount.trim().length() == 0){
                    Toast.makeText(EditSecondPulseDelayActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!startLevel.trim().equals("1") && !startLevel.trim().equals("0")){
                    Toast.makeText(EditSecondPulseDelayActivity.this,R.string.start_level_format_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                int startLevelInt = Integer.valueOf(startLevel);
                int highLevelPulseWidthTimeInt = Integer.valueOf(highLevelPulseWidthTime) ;
                int lowLevelPulseWidthTimeInt = Integer.valueOf(lowLevelPulseWidthTime) ;
                int pulseCountInt = Integer.valueOf(pulseCount);

                if (highLevelPulseWidthTimeInt < 100 || highLevelPulseWidthTimeInt > 25500){
                    Toast.makeText(EditSecondPulseDelayActivity.this,R.string.high_level_pulse_width_time_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (lowLevelPulseWidthTimeInt < 100 || lowLevelPulseWidthTimeInt > 25500){
                    Toast.makeText(EditSecondPulseDelayActivity.this,R.string.low_level_pulse_width_time_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (pulseCountInt < 0 || pulseCountInt > 65535){
                    Toast.makeText(EditSecondPulseDelayActivity.this,R.string.pulse_count_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("startLevel",startLevelInt);
                intent.putExtra("highLevelPulseWidthTime",highLevelPulseWidthTimeInt/ 100);
                intent.putExtra("lowLevelPulseWidthTime",lowLevelPulseWidthTimeInt/ 100) ;
                intent.putExtra("pulseCount",pulseCountInt);
                setResult(EditActivity.RESPONSE_EDIT_SECOND_PULSE_DELAY,intent);
                finish();
            }
        });

    }
    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.relay_pulse);
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