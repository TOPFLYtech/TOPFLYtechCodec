package com.topflytech.tftble;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.topflytech.tftble.data.SingleClickListener;


public class EditDoutOutputStatusActivity extends AppCompatActivity {

    private EditText etDout0, edDout1;
    private Button btnConfirm;
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    private int oldDout0, oldDout1;

    private InputFilter filter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            // 允许删除字符
            if (TextUtils.isEmpty(source)) {
                return null;
            }

            // 检查新输入的字符是否都是0或1
            String input = source.toString();
            if (input.matches("[01]+")) {
                return null;
            }

            // 不允许输入其他字符
            return "";
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dout_output_status);
        etDout0 = (EditText)findViewById(R.id.et_dout0);
        edDout1 = (EditText)findViewById(R.id.et_dout1);
        etDout0.setFilters(new InputFilter[]{filter});
        edDout1.setFilters(new InputFilter[]{filter});
        oldDout0 = getIntent().getIntExtra("oldDout0",-1);
        if(oldDout0 != -1){
            etDout0.setText(String.valueOf(oldDout0));
        }
        oldDout1 = getIntent().getIntExtra("oldDout1",-1);
        if(oldDout1 != -1){
            edDout1.setText(String.valueOf(oldDout1));
        }
        btnConfirm = (Button)findViewById(R.id.btn_dout_output_confirm);
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.doutStatus);
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

                if( etDout0.getText() == null || !(etDout0.getText().toString().trim().equals("0") || etDout0.getText().toString().trim().equals("1")) ||
                        edDout1.getText() == null || !(edDout1.getText().toString().trim().equals("0") || edDout1.getText().toString().trim().equals("1"))
                    ){
                    Toast.makeText(EditDoutOutputStatusActivity.this,R.string.dout_value_error,Toast.LENGTH_SHORT).show();
                    return;
                }
                String dout0 = etDout0.getText().toString().trim();
                String dout1 = edDout1.getText().toString().trim();

                Intent intent = new Intent();
                intent.putExtra("dout0",dout0);
                intent.putExtra("dout1",dout1);
                setResult(EditActivity.RESPONSE_CHANGE_DOUT_STATUS,intent);
                finish();
            }
        });
    }
}
