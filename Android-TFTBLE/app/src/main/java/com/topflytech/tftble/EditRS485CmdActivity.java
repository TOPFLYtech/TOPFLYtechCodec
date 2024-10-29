package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topflytech.tftble.data.MyUtils;
import com.topflytech.tftble.data.SingleClickListener;

public class EditRS485CmdActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private EditText etCmd;
    private Button btnConfirm;
    private CheckBox cbHexData,cbAddLineEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd_edit);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.send_data);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        cbHexData = (CheckBox)findViewById(R.id.cb_hex_select);
        cbHexData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("rs485_send_data_use_hex",status);
                editor.commit();
            }
        });
        SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
        boolean isUseHex = sharedPreferences.getBoolean("rs485_send_data_use_hex",true);
        boolean isAddLineEnd = sharedPreferences.getBoolean("rs485_send_data_add_line_end",false);
        cbHexData.setChecked(isUseHex);
        cbAddLineEnd = (CheckBox)findViewById(R.id.cb_add_line_end);
        cbAddLineEnd.setChecked(isAddLineEnd);
        cbAddLineEnd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                SharedPreferences sharedPreferences= getSharedPreferences("data", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("rs485_send_data_add_line_end",status);
                editor.commit();
            }
        });
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setVisibility(View.INVISIBLE);
        backButton.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                finish();
            }
        });
        etCmd = (EditText)findViewById(R.id.et_cmd);
        btnConfirm = (Button)findViewById(R.id.btn_send_cmd_confirm);
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String cmd = etCmd.getText().toString().trim();
                if(cmd.length() <= 0){
                    Toast.makeText(EditRS485CmdActivity.this,R.string.fixInput,Toast.LENGTH_SHORT).show();
                    return;
                }
                String submitCmd = "";
                if(cbHexData.isChecked()){
                    int checkLen = 400;
                    if(cbAddLineEnd.isChecked()){
                        checkLen = checkLen - 4;
                    }
                    if(cmd.length() > checkLen){
                        Toast.makeText(EditRS485CmdActivity.this,R.string.sendCmdInvalidLen,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for(char c : cmd.toCharArray()){
                        if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') ){

                        }else{
                            Toast.makeText(EditRS485CmdActivity.this,R.string.invalidChar,Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if(cmd.length() % 2 != 0){
                        Toast.makeText(EditRS485CmdActivity.this,R.string.hex_data_len_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitCmd = cmd;

                }else{
                    int checkLen = 200;
                    if(cbAddLineEnd.isChecked()){
                        checkLen = checkLen - 2;
                    }
                    if(cmd.length() > checkLen){
                        Toast.makeText(EditRS485CmdActivity.this,R.string.sendCmdInvalidLen,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitCmd = MyUtils.bytes2HexString(cmd.getBytes(),0);
                }
                if(cbAddLineEnd.isChecked()){
                    submitCmd = submitCmd + "0d0a";
                }
                Intent intent = new Intent();
                intent.putExtra("cmd", submitCmd);
                setResult(EditActivity.RESPONSE_RS485_SEND_DATA,intent);
                finish();
            }
        });
    }
}