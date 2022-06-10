package com.topflytech.tftble.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.topflytech.tftble.MainActivity;

import java.io.File;

public class DownloadFileManager {
    private static class SingletonHolder {
        static DownloadFileManager downloadFileManager = new DownloadFileManager();
    }
    public static DownloadFileManager instance() {
        return SingletonHolder.downloadFileManager;
    }

    public interface Callback {
        enum StatusCode {
            OK,
            ERROR
        }

        void callback(StatusCode code, String result);
    }

    public void geetDebugUpdateFileUrl(Context context,String serverUpgradeLink,  final Callback callback){
        String diskFile = "dfu_app_debug_upgrade.zip";
        String path = context.getFilesDir().getPath() + "/";
        final String relativePath = path + diskFile;
        delFile(path +"dfu_app_debug_upgrade.zip");
        FileDownloader.setup(context);
        delFile(relativePath);
        FileDownloader.getImpl().create(serverUpgradeLink).setPath(relativePath).setListener(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                System.out.println("pending");
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                System.out.println("progress");
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                System.out.println("complete");
                callback.callback(Callback.StatusCode.OK,relativePath);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                System.out.println("paused");
                callback.callback(Callback.StatusCode.ERROR,null);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                e.printStackTrace();
                System.out.println("error");
                callback.callback(Callback.StatusCode.ERROR,null);
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                System.out.println("warn");
                callback.callback(Callback.StatusCode.ERROR,null);
            }
        }).start();

    }
    private static String versionExt = "bleVersion";
    public void geetUpdateFileUrl(Context context,String serverUpgradeLink, int serverUpgradeVersion, String deviceType, final Callback callback){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String bleVersionStr = sharedPreferences.getString(deviceType+versionExt,"");
        int bleVersion = 0;
        if(!bleVersionStr.isEmpty()){
            bleVersion = Integer.valueOf(bleVersionStr);
        }
        String diskFile = serverUpgradeVersion + "_" + deviceType + "_" + versionExt + ".zip";
        String path = context.getFilesDir().getPath() + "/";
        final String relativePath = path + diskFile;
        if(bleVersion != serverUpgradeVersion){
            delFile(path + bleVersion + "_" + deviceType + "_"+versionExt+".zip");
            FileDownloader.setup(context);
            delFile(relativePath);
            FileDownloader.getImpl().create(serverUpgradeLink).setPath(relativePath).setListener(new FileDownloadListener() {
                @Override
                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    System.out.println("pending");
                }

                @Override
                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    System.out.println("progress");
                }

                @Override
                protected void completed(BaseDownloadTask task) {
                    System.out.println("complete");
                    callback.callback(Callback.StatusCode.OK,relativePath);
                }

                @Override
                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    System.out.println("paused");
                    callback.callback(Callback.StatusCode.ERROR,null);
                }

                @Override
                protected void error(BaseDownloadTask task, Throwable e) {
                    e.printStackTrace();
                    System.out.println("error");
                    callback.callback(Callback.StatusCode.ERROR,null);
                }

                @Override
                protected void warn(BaseDownloadTask task) {
                    System.out.println("warn");
                    callback.callback(Callback.StatusCode.ERROR,null);
                }
            }).start();
        }else{
            callback.callback(Callback.StatusCode.OK,relativePath);
        }

    }

    private void delFile(String filename){
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
