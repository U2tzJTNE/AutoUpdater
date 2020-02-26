package com.u2tzjtne.autoupdater.downloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

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
        //网络中断时结束当前下载
        if (!NetworkUtils.isConnected() && downId >= 0) {
            DownloadManager dManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dManager != null) {
                LogUtils.debug("网络断开");
                dManager.remove(downId);
            } else {
                LogUtils.debug("DownloadManager is null");
            }
        } else {
            LogUtils.debug("downId: " + downId);
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
                    String downloadFileUrl = c
                            .getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    LogUtils.debug("downloadFileUrl: " + downloadFileUrl);
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
