package com.u2tzjtne.autoupdater;

import android.content.Context;
import android.telephony.mbms.DownloadProgressListener;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.u2tzjtne.autoupdater.downloader.Utils;

/**
 * @author u2tzjtne
 */
public class AutoUpdater {

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public static class Builder {
        Context mContext;
        String mApkPath;
        String mDownloadPath;
        String mNotificationTitle;
        boolean mShowNotification = true;
        DownloadProgressListener mDownloadProgressListener;

        Builder(Context context) {
            mContext = context;
            mDownloadPath = mContext.getCacheDir().getAbsolutePath();
        }

        public Builder setApkPath(String apkPath) {
            mApkPath = apkPath;
            return this;
        }

        public Builder setNotificationTitle(String notificationTitle) {
            mNotificationTitle = notificationTitle;
            return this;
        }

        public Builder isShowNotification(boolean showNotification) {
            mShowNotification = showNotification;
            return this;
        }

        public Builder setDownloadProgressListener(DownloadProgressListener listener) {
            mDownloadProgressListener = listener;
            return this;
        }

        public void start() {
           //进行相应的操作
            if (TextUtils.isEmpty(mApkPath)){
                mApkPath = mContext.getCacheDir().getAbsolutePath();
            }
            if (Utils.isExternalPath(mContext,mApkPath)){
                //TODO 请求存储权限
                
            }
            if (Utils.isDownloadPath(mApkPath)){
                //TODO 下载apk
            }

        }
    }
}
