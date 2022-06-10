package com.topflytech.tftble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SmtpSettingActivity extends AppCompatActivity {
    private ActionBar actionBar;
    private ImageView backButton;
    private ImageView rightButton;
    private TextView tvHead;
    SweetAlertDialog waitingDlg;
    private EditText etSmtpServer,etSmtpPort,etAccount,etPwd,etName,etRecvEmail;
    private Button btnSave;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smtp_setting);
        initActionbar();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        etSmtpServer = (EditText)findViewById(R.id.et_smtp_server);
        etSmtpPort = (EditText)findViewById(R.id.et_smtp_port);
        etAccount = (EditText)findViewById(R.id.et_smtp_email);
        etPwd = (EditText)findViewById(R.id.et_smtp_password);
        etName = (EditText)findViewById(R.id.et_smtp_senders_name);
        btnSave = (Button)findViewById(R.id.btn_smtp_submit);
        etRecvEmail = (EditText)findViewById(R.id.et_recv_email);
        waitingDlg = new SweetAlertDialog(SmtpSettingActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        waitingDlg.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        waitingDlg.setCancelable(false);
        waitingDlg.setTitleText(getResources().getString(R.string.waiting));
        initParams();
        btnSave.setOnClickListener(submitClick);
    }
    View.OnClickListener submitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(etSmtpServer.getText() != null && etSmtpServer.getText().toString().length() > 0
                && etSmtpPort.getText() != null && etSmtpPort.getText().toString().length() > 0
                    && etAccount.getText() != null && etAccount.getText().toString().length() > 0
                    && etPwd.getText() != null && etPwd.getText().toString().length() > 0
                    && etName.getText() != null && etName.getText().toString().length() > 0
                    && etRecvEmail.getText() != null && etRecvEmail.getText().toString().length() > 0){
                waitingDlg.show();
                javaMailSendSimpleEmail(new Callback() {
                    @Override
                    public void callback(StatusCode code) {
                        SmtpSettingActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                waitingDlg.hide();
                            }
                        });

                        if(code == StatusCode.OK){
                            SharedPreferences.Editor preferenceEdit = preferences.edit();
                            preferenceEdit.putBoolean("smtpConfig",true);
                            preferenceEdit.putString("smtpServer",etSmtpServer.getText().toString().trim());
                            preferenceEdit.putString("smtpPort",etSmtpPort.getText().toString().trim());
                            preferenceEdit.putString("senderName",etName.getText().toString().trim());
                            preferenceEdit.putString("email",etAccount.getText().toString().trim());
                            preferenceEdit.putString("smtpPwd",etPwd.getText().toString().trim());
                            preferenceEdit.putString("recvEmail",etRecvEmail.getText().toString().trim()).commit();
                            Looper.prepare();
                            Toast.makeText(SmtpSettingActivity.this,R.string.success,Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            finish();
                        }else {
                            Looper.prepare();
                            Toast.makeText(SmtpSettingActivity.this, R.string.smtp_send_fail, Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                });
            }else{
                Toast.makeText(SmtpSettingActivity.this,R.string.input_error,Toast.LENGTH_SHORT).show();

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(waitingDlg != null){
            waitingDlg.hide();
            waitingDlg.dismiss();
            waitingDlg = null;
        }
    }

    private void initParams(){
        Boolean isSmtpConfigSucc = preferences.getBoolean("smtpConfig",false);
        if(isSmtpConfigSucc){
            String smtpServer = preferences.getString("smtpServer","");
            String port = preferences.getString("smtpPort","");
            String email = preferences.getString("email","");
            String senderName = preferences.getString("senderName","");
            String pwd = preferences.getString("smtpPwd","");
            String recvEmail = preferences.getString("recvEmail","");
            etSmtpPort.setText(port);
            etSmtpServer.setText(smtpServer);
            etPwd.setText(pwd);
            etName.setText(senderName);
            etAccount.setText(email);
            etRecvEmail.setText(recvEmail);
        }
    }

    public interface Callback {
        enum StatusCode {
            OK,
            ERROR
        }

        void callback(StatusCode code);
    }
    public void javaMailSendSimpleEmail(final Callback callback){
        final String to = etAccount.getText().toString().trim();
        final String from = etAccount.getText().toString().trim();
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", etSmtpServer.getText().toString().trim());
        int port = Integer.valueOf(etSmtpPort.getText().toString().trim());
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.timeout","60000");
        properties.put("mail.smtp.connectiontimeout", "60000");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        if (port == 465) {
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.ssl.enable", "true");
        }
        if(port == 587){
            properties.put("mail.smtp.starttls.enable","true");
        }
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        // 登陆邮件发送服务器的用户名和密码
                        return new PasswordAuthentication(etAccount.getText().toString().trim(), etPwd.getText().toString().trim());
                    }
                });

        // 创建默认的 MimeMessage 对象
        final MimeMessage message = new MimeMessage(session);


        // 发送消息
        new AsyncTask<String,Void, JSONObject>(){

            @Override
            protected JSONObject doInBackground(String... strings) {
                try {
                    message.setFrom(new InternetAddress(from));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    message.setSubject("TFTBLE Test");
                    message.setText("TFTBLE Test");
                    Transport.send(message);
                    callback.callback(Callback.StatusCode.OK);
                } catch (MessagingException e) {
                    callback.callback(Callback.StatusCode.ERROR);
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private void initActionbar() {
        actionBar = getSupportActionBar();
        View customView = this.getLayoutInflater().inflate(R.layout.custom_activity_bar, null);
        tvHead = customView.findViewById(R.id.custom_head_title);
        tvHead.setText(R.string.email_setting);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(customView);
        backButton = (ImageView) customView.findViewById(R.id.command_list_bar_back_id);
        rightButton =(ImageView) customView.findViewById(R.id.img_btn_right);
        rightButton.setVisibility(View.INVISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
