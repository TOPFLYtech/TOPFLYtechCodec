package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topflytech.tftble.data.SingleClickListener;

public class EditPulseDelayActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;

    private EditText etCycleTime,etInitEnableTime,etToggleTime,etRecoverTime;
    private Button btnConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pulse_delay);
        initActionbar();
        etCycleTime = (EditText)findViewById(R.id.et_cycle_time) ;
        etInitEnableTime = (EditText)findViewById(R.id.et_init_enable_time) ;
        etToggleTime = (EditText)findViewById(R.id.et_toggle_time) ;
        etRecoverTime = (EditText)findViewById(R.id.et_recover_time) ;
        btnConfirm = (Button) findViewById(R.id.btn_relay_pulse_confirm);
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String cycleTime = etCycleTime.getText().toString();
                String initEnableTime = etInitEnableTime.getText().toString();
                String toggleTime = etToggleTime.getText().toString();
                String recoverTime = etRecoverTime.getText().toString();
                if(cycleTime == null || cycleTime.trim().length() == 0 ||
                        initEnableTime == null || initEnableTime.trim().length() == 0||
                        toggleTime == null || toggleTime.trim().length() == 0||
                        recoverTime == null || recoverTime.trim().length() == 0){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Integer.valueOf(cycleTime) % 100 != 0){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.cycle_time_format_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Integer.valueOf(initEnableTime) % 10 != 0){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.init_enable_time_format_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                int cycleTimeInt = Integer.valueOf(cycleTime) / 100 * 100;
                int initEnableTimeInt = Integer.valueOf(initEnableTime) / 10 * 10;
                int toggleTimeInt = Integer.valueOf(toggleTime);
                int recoverTimeInt = Integer.valueOf(recoverTime);
                if (cycleTimeInt <= 0 || cycleTimeInt > 65500){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.cycle_time_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (initEnableTimeInt <= 0 || initEnableTimeInt > 65500){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.init_enable_time_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (toggleTimeInt <= 0 || toggleTimeInt > 65535){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.toggle_time_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                if (recoverTimeInt < 0 || recoverTimeInt > 255){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.recover_time_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cycleTimeInt < initEnableTimeInt){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.cycle_time_error_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(toggleTimeInt != 0 && (cycleTimeInt - initEnableTimeInt) / toggleTimeInt <= 20 ){
                    Toast.makeText(EditPulseDelayActivity.this,R.string.pulse_Delay_multi_warning,Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("cycleTime",cycleTimeInt);
                intent.putExtra("initEnableTime",initEnableTimeInt);
                intent.putExtra("toggleTime",toggleTimeInt);
                intent.putExtra("recoverTime",recoverTimeInt);
                setResult(EditActivity.RESPONSE_EDIT_PULSE_DELAY,intent);
                finish();
            }
        });

    }
    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.dynamic_pulse);
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