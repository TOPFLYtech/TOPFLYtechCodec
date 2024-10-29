package com.topflytech.tftble;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.king.camera.scan.AnalyzeResult;
import com.king.camera.scan.analyze.Analyzer;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;
import com.king.wechat.qrcode.scanning.analyze.WeChatScanningAnalyzer;

import java.nio.file.Path;
import java.util.List;

public class WeChatQRCodeActivity extends WeChatCameraScanActivity {
    private String TAG =  "WeChatQRCodeActivity";
    @Override
    public void onScanResultCallback(@NonNull AnalyzeResult<List<String>> result) {
        if (result.getResult() != null) {
            // 停止分析
            getCameraScan().setAnalyzeImage(false);
            // 一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
            String text = result.getResult().get(0);
            Intent intent = new Intent();
            intent.putExtra("value",text);
            setResult(MainActivity.RESPONSE_SCAN_QRCODE,intent);
            finish();

        }
    }

    @Nullable
    @Override
    public Analyzer<List<String>> createAnalyzer() {
//        return super.createAnalyzer();
        return new WeChatScanningAnalyzer(true);
    }


    @Override
    public void onScanResultFailure() {
        super.onScanResultFailure();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
