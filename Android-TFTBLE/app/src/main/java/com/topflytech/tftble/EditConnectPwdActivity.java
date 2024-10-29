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


public class EditConnectPwdActivity extends AppCompatActivity {

    private EditText etNewPwd,edRepeatPwd;
    private Button btnConfirm;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_connect_pwd);
        etNewPwd = (EditText)findViewById(R.id.et_connect_new_pwd);
        edRepeatPwd = (EditText)findViewById(R.id.et_connect_repeat_pwd);
        btnConfirm = (Button)findViewById(R.id.btn_connect_pwd_submit);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.edit_connect_pwd_view_title);
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
        btnConfirm.setOnClickListener(new SingleClickListener() {
            @Override
            public void onSingleClick(View view) {

                if( etNewPwd.getText() == null || etNewPwd.getText().toString().trim().length() != 6 ||
                        edRepeatPwd.getText() == null || edRepeatPwd.getText().toString().trim().length() != 6
                    ){
                    Toast.makeText(EditConnectPwdActivity.this,R.string.change_pwd_input_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                String newPwd = etNewPwd.getText().toString().trim();
                String repeatPwd = edRepeatPwd.getText().toString().trim();

                if(!newPwd.equals(repeatPwd)){
                    Toast.makeText(EditConnectPwdActivity.this,R.string.repeat_pwd_not_match,Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("newPwd",newPwd);
                setResult(EditActivity.RESPONSE_CHANGE_CONNECT_PWD,intent);
                finish();
            }
        });
    }
}
