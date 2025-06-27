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

public class IpEditActivity extends AppCompatActivity {

    private EditText etDomain, etPort;
    private Button btnSubmit;

    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private int ipType;
    private void initActionBar(){
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.netowrk);
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
        setContentView(R.layout.activity_ip_edit);
        initActionBar();
        String domain = getIntent().getStringExtra("domain");
        String port = getIntent().getStringExtra("port");
        ipType = getIntent().getIntExtra("ipType",-1);
        if(ipType == 2){
            tvHead.setText(R.string.ip2);
        }else{
            tvHead.setText(R.string.ip1);
        }
        etDomain = findViewById(R.id.et_domain);
        etPort = findViewById(R.id.et_port);
        btnSubmit = findViewById(R.id.btn_submit);
        if(domain != null){
            etDomain.setText(domain);
        }
        if(port != null){
            etPort.setText(port);
        }
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitSettings();
            }
        });
    }

    private void submitSettings() {
        String domain = etDomain.getText().toString();
        String portStr = etPort.getText().toString();

        if (domain.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(this, R.string.fix_input, Toast.LENGTH_SHORT).show();
            return;
        }
        if(domain.trim().length() >= 50){
            Toast.makeText(this, R.string.domain_len_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.port_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        if (port < 0 || port > 65535) {
            Toast.makeText(this, R.string.port_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra("domain",domain);
        intent.putExtra("port",portStr);
        intent.putExtra("ipType",ipType);
        setResult(EditActivity.RESPONSE_CHANGE_IP,intent);
        finish();
    }
}



