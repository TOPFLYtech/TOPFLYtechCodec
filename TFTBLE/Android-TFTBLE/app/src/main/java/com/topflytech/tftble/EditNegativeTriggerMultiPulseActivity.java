package com.topflytech.tftble;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.topflytech.tftble.data.SingleClickListener;

public class EditNegativeTriggerMultiPulseActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;

    private EditText etPort,etStartLevel,etHighLevelPulseWidthTime,etLowLevelPulseWidthTime,etPulseCount;
    private Button btnConfirm;
    private int port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_negative_trigger_multi_pulse);
        initActionbar();
        etPort = (EditText)findViewById(R.id.et_port);
        etStartLevel = (EditText)findViewById(R.id.et_start_level) ;
        etHighLevelPulseWidthTime = (EditText)findViewById(R.id.et_high_level_pulse_width_time) ;
        etLowLevelPulseWidthTime = (EditText)findViewById(R.id.et_low_level_pulse_width_time) ;
        etPulseCount = (EditText)findViewById(R.id.et_pulse_count) ;
        btnConfirm = (Button) findViewById(R.id.btn_relay_pulse_confirm);
        port = getIntent().getIntExtra("port",-1);
        if(port != -1){
            etPort.setText(String.valueOf(port));
        }
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
                    Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!startLevel.trim().equals("1") && !startLevel.trim().equals("0")){
                    Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.start_level_format_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    int startLevelInt = Integer.valueOf(startLevel);
                    int highLevelPulseWidthTimeInt = Integer.valueOf(highLevelPulseWidthTime) ;
                    int lowLevelPulseWidthTimeInt = Integer.valueOf(lowLevelPulseWidthTime) ;
                    int pulseCountInt = Integer.valueOf(pulseCount);

                    if (highLevelPulseWidthTimeInt < 100 || highLevelPulseWidthTimeInt > 25500){
                        Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.high_level_pulse_width_time_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (lowLevelPulseWidthTimeInt < 100 || lowLevelPulseWidthTimeInt > 25500){
                        Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.low_level_pulse_width_time_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (pulseCountInt < 0 || pulseCountInt > 65535){
                        Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.pulse_count_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("startLevel",startLevelInt);
                    intent.putExtra("highLevelPulseWidthTime",highLevelPulseWidthTimeInt/ 100);
                    intent.putExtra("lowLevelPulseWidthTime",lowLevelPulseWidthTimeInt/ 100) ;
                    intent.putExtra("pulseCount",pulseCountInt);
                    intent.putExtra("port",port);
                    setResult(EditActivity.RESPONSE_EDIT_NEGATIVE_TRIGGER_MULTI_PULSE,intent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(EditNegativeTriggerMultiPulseActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                }

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