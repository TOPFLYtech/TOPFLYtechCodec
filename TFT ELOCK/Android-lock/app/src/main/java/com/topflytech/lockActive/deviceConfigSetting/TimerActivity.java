package com.topflytech.lockActive.deviceConfigSetting;


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

import com.topflytech.lockActive.EditActivity;
import com.topflytech.lockActive.R;

public class TimerActivity extends AppCompatActivity {

    private EditText etAccOn, etAccOff, etAngle, etDistance;
    private Button btnSubmit;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private void initActionBar(){
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.timer);
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
        setContentView(R.layout.activity_timer);
        initActionBar();
        int accOn = getIntent().getIntExtra("accOn",-1);
        long accOff = getIntent().getLongExtra("accOff",-1);
        int angle = getIntent().getIntExtra("angle",-1);
        int distance = getIntent().getIntExtra("distance",-1);
        etAccOn = findViewById(R.id.et_acc_on);
        etAccOff = findViewById(R.id.et_acc_off);
        etAngle = findViewById(R.id.et_angle);
        etDistance = findViewById(R.id.et_distance);
        btnSubmit = findViewById(R.id.btn_submit);
        if(accOn != -1){
            etAccOn.setText(String.valueOf(accOn));
        }
        if(accOff != -1){
            etAccOff.setText(String.valueOf(accOff));
        }
        if(angle != -1){
            etAngle.setText(String.valueOf(angle));
        }
        if(distance != -1){
            etDistance.setText(String.valueOf(distance));
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitValues();
            }
        });
    }

    private void submitValues() {
        String accOnStr = etAccOn.getText().toString();
        String accOffStr = etAccOff.getText().toString();
        String angleStr = etAngle.getText().toString();
        String distanceStr = etDistance.getText().toString();

        if (accOnStr.isEmpty() || accOffStr.isEmpty() || angleStr.isEmpty() || distanceStr.isEmpty()) {
            Toast.makeText(this, R.string.fix_input, Toast.LENGTH_SHORT).show();
            return;
        }

        int accOn = Integer.parseInt(accOnStr);
        long accOff = Long.parseLong(accOffStr);
        int angle = Integer.parseInt(angleStr);
        int distance = Integer.parseInt(distanceStr);

        if ((accOn < 5 || accOn > 65535) && accOn != 0) {
            Toast.makeText(this, R.string.acc_on_value_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        if (accOff < 0 || accOff > 2147483647L || (accOff > 0 && accOff < 1200)) {
            Toast.makeText(this, R.string.acc_off_value_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        if ((angle < 15 || angle > 180) && angle != 0) {
            Toast.makeText(this, R.string.angle_value_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        if ((distance < 100 || distance > 65535) && distance != 0) {
            Toast.makeText(this, R.string.distance_value_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("accOn",accOn);
        intent.putExtra("accOff",accOff);
        intent.putExtra("angle",angle);
        intent.putExtra("distance",distance);
        setResult(EditActivity.RESPONSE_CHANGE_TIMER,intent);
        finish();
    }
}



