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

public class EditInstructionSequenceActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private EditText etCmd;
    private Button btnConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_sequence_edit);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.send_instruction_sequence);
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
        etCmd = (EditText)findViewById(R.id.et_instruction_sequence);
        btnConfirm = (Button)findViewById(R.id.btn_send_instruction_sequence_confirm);
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                String cmd = etCmd.getText().toString().trim();
                if(cmd.length() <= 0){
                    Toast.makeText(EditInstructionSequenceActivity.this,R.string.fixInput,Toast.LENGTH_SHORT).show();
                    return;
                }
                if(cmd.length() > 400){
                    Toast.makeText(EditInstructionSequenceActivity.this,R.string.sendCmdInvalidLen,Toast.LENGTH_SHORT).show();
                    return;
                }
                for(char c : cmd.toCharArray()){
                    if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') ){

                    }else{
                        Toast.makeText(EditInstructionSequenceActivity.this,R.string.invalidChar,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Intent intent = new Intent();
                intent.putExtra("cmd",cmd);
                setResult(EditActivity.RESPONSE_SEND_INSTRUCTION_SEQUENCE,intent);
                finish();
            }
        });
    }
}