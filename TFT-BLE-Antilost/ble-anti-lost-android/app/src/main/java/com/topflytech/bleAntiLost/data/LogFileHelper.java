package com.topflytech.bleAntiLost.data;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogFileHelper {
    private static LogFileHelper instance = null;
    private final static Object lock = new Object();
    private Context context;
    private String logPath;
    private String logName = "tftbleLog.txt";
    public static LogFileHelper getInstance(Context context){
        if (instance == null){
            synchronized (lock){
                if (instance == null){
                    instance = new LogFileHelper();
                }
            }
        }
        instance.context = context;
        ContextWrapper cw = new ContextWrapper(context);
        File dir = cw.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        instance.logPath = dir.getAbsolutePath() + "//" + instance.logName;
        return instance;
    }

    private final SimpleDateFormat tableDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    public void writeIntoFile(String log){
        if(!MyUtils.isDebug){
            return;
        }
        String formatLog = tableDateFormat.format(new Date()) + " - " + log + "\r\n";
        boolean isSuccess = true;
        //再创建路径下的文件
        File file = null;
        boolean isAppend = true;
        try {
            file = new File(instance.logPath);
            if(!file.exists()){
                file.createNewFile();
            }
            if(file.length() > 5000000){
                isAppend = false;
            }
        } catch (IOException e) {
            isSuccess = false;
            //e.printStackTrace();
        }
        //将logs写入文件
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, isAppend);
            fileWriter.write(formatLog);
            fileWriter.flush();
        } catch (IOException e) {
            isSuccess = false;
            //e.printStackTrace();
        } finally{
            try {
                fileWriter.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

    }

}
