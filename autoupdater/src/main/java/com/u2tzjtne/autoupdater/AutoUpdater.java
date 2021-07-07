package com.u2tzjtne.autoupdater;

import android.app.Activity;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.u2tzjtne.autoupdater.downloader.Downloader;
import com.u2tzjtne.autoupdater.downloader.Utils;

/**
 * @author u2tzjtne
 */
public class AutoUpdater {

    public static Builder with(@NonNull Activity context) {
        return new Builder(context);
    }

    public static class Builder {
        Activity mContext;
        String mApkPath;
        String mDownloadDir;
        String mDownloadFileName;
        String mNotificationTitle;
        boolean mShowNotification;
        Downloader.ProgressListener mDownloadProgressListener;

        Builder(Activity context) {
            mContext = context;
        }

        public Builder setApkPath(String apkPath) {
            mApkPath = apkPath;
            return this;
        }

        public Builder setDownloadDir(String downloadDir) {
            mDownloadDir = downloadDir;
            return this;
        }

        public Builder setDownloadFileName(String downloadFileName) {
            mDownloadFileName = downloadFileName;
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

        public Builder setDownloadProgressListener(Downloader.ProgressListener listener) {
            mDownloadProgressListener = listener;
            return this;
        }

        /**
         * 初始化自动安装
         */
        private void initAutoInstall() {
            if (!RootUtils.isRoot()){
                // "未知来源"设置
                InstallUtils.checkSetting(mContext);
                // "辅助功能"设置
                AccessibilityUtils.checkSetting(mContext, AutoInstallService.class);
            }
        }

        public void start() {
            initAutoInstall();
            //进行相应的操作
            if (TextUtils.isEmpty(mApkPath)) {
                return;
            }
            if (Utils.isDownloadPath(mApkPath)) {
                //下载apk
                if (TextUtils.isEmpty(mDownloadDir)) {
                    mDownloadDir = Environment.DIRECTORY_DOWNLOADS;
                }
                if (TextUtils.isEmpty(mDownloadFileName)) {
                    mDownloadFileName = Utils.getFileNameForUrl(mApkPath);
                }
                if (TextUtils.isEmpty(mNotificationTitle)) {
                    mNotificationTitle = Utils.getFileNameForUrl(mApkPath);
                }
                Downloader.Builder builder = new Downloader.Builder(mContext);
                builder.setDownloadUrl(mApkPath)
                        .setFileName(mDownloadFileName)
                        .setDirName(mDownloadDir)
                        .showNotification(mShowNotification)
                        .setNotificationTitle(mNotificationTitle)
                        .addProgressListener(mDownloadProgressListener)
                        .registerDownloadReceiver()
                        .overlayDownload()
                        .start();
            } else {
                //安装
                InstallUtils.install(mContext, mApkPath);
            }
        }
    }
}
