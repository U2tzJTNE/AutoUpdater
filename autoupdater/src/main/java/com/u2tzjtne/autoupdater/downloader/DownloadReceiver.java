package com.u2tzjtne.autoupdater.downloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.u2tzjtne.autoupdater.InstallUtils;

/**
 * @author u2tzjtne
 */
public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        long downId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        //下载完成或点击通知栏
        if (TextUtils.equals(intent.getAction(), (DownloadManager.ACTION_DOWNLOAD_COMPLETE)) ||
                TextUtils.equals(intent.getAction(), (DownloadManager.ACTION_NOTIFICATION_CLICKED))) {
            queryFileUri(context, downId);
        }
    }

    private void queryFileUri(Context context, long downloadId) {
        DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        if (dManager == null) {
            return;
        }
        Cursor c = dManager.query(query);
        if (c != null && c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PENDING:
                    LogUtils.debug("STATUS_PENDING");
                    break;
                case DownloadManager.STATUS_PAUSED:
                    LogUtils.debug("STATUS_PAUSED");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    LogUtils.debug("STATUS_RUNNING");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    LogUtils.debug("STATUS_SUCCESSFUL");
                    String downloadFilePath ="";
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        int fileUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        String fileUri = c.getString(fileUriIdx);
                        if (fileUri != null) {
                            downloadFilePath = Uri.parse(fileUri).getPath();
                        }
                    } else {
                        //过时的方式：DownloadManager.COLUMN_LOCAL_FILENAME
                        int fileNameIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        downloadFilePath = c.getString(fileNameIdx);
                    }
                    //安装apk
                    if (TextUtils.isEmpty(downloadFilePath)) {
                        return;
                    }
                    LogUtils.debug("downloadFilePath: " + downloadFilePath);
                    InstallUtils.install(context, downloadFilePath);
                    break;
                case DownloadManager.STATUS_FAILED:
                    LogUtils.debug("STATUS_FAILED");
                    Utils.showToast(context, "下载失败");
                    context.sendBroadcast(new Intent(Downloader.DownloadFailedReceiver.TAG));
                    break;
                default:
                    break;
            }
            c.close();
        }
    }
}
