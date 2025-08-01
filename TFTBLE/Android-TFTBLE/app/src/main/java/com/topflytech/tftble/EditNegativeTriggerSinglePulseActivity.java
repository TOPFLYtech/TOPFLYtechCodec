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

public class EditNegativeTriggerSinglePulseActivity extends AppCompatActivity {
 
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;

    private EditText etPort,etStartLevel, etPulseWidthTime;
    private Button btnConfirm;
    private int port;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_negative_trigger_single_pulse);
        initActionbar();
        etPort = (EditText)findViewById(R.id.et_port);
        etStartLevel = (EditText)findViewById(R.id.et_start_level) ;
        etPulseWidthTime = (EditText)findViewById(R.id.et_pulse_width_time) ;
        btnConfirm = (Button) findViewById(R.id.btn_relay_pulse_confirm);
        port = getIntent().getIntExtra("port",-1);
        if(port != -1){
            etPort.setText(String.valueOf(port));
        }
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String startLevel = etStartLevel.getText().toString();
                String pulseWidthTime = etPulseWidthTime.getText().toString();
                if(startLevel == null || startLevel.trim().length() == 0 ||
                        pulseWidthTime == null || pulseWidthTime.trim().length() == 0 ){
                    Toast.makeText(EditNegativeTriggerSinglePulseActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!startLevel.trim().equals("1") && !startLevel.trim().equals("0")){
                    Toast.makeText(EditNegativeTriggerSinglePulseActivity.this,R.string.start_level_format_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    int startLevelInt = Integer.valueOf(startLevel);
                    int pulseWidthTimeInt = Integer.valueOf(pulseWidthTime) ;

                    if (pulseWidthTimeInt < 100 || pulseWidthTimeInt > 6553500){
                        Toast.makeText(EditNegativeTriggerSinglePulseActivity.this,R.string.negative_trigger_pulse_input_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("startLevel",startLevelInt);
                    intent.putExtra("pulseWidthTime",pulseWidthTimeInt/ 100);
                    intent.putExtra("port",port);
                    setResult(EditActivity.RESPONSE_EDIT_NEGATIVE_TRIGGER_SINGLE_PULSE,intent);
                    finish();
                }catch (Exception e){
                    Toast.makeText(EditNegativeTriggerSinglePulseActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();

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