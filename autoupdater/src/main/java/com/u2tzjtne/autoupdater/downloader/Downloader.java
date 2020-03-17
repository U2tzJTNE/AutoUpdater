package com.u2tzjtne.autoupdater.downloader;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.u2tzjtne.autoupdater.InstallUtils;
import com.u2tzjtne.autoupdater.R;
import com.u2tzjtne.autoupdater.permission.EasyPermission;
import com.u2tzjtne.autoupdater.permission.GrantResult;
import com.u2tzjtne.autoupdater.permission.NextAction;
import com.u2tzjtne.autoupdater.permission.Permission;
import com.u2tzjtne.autoupdater.permission.PermissionRequestListener;
import com.u2tzjtne.autoupdater.permission.RequestPermissionRationalListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author u2tzjtne
 */
public class Downloader implements Application.ActivityLifecycleCallbacks {

    private String fileName;
    private String filePath;
    private String dirName;
    private String title;
    private String downloadUrl;
    private Activity context;
    private DownloadManager downloadManager;
    private long mTaskId;
    private boolean showNotification = false;
    private boolean allowedOverRoaming = false;
    private boolean overlayDownload = false;
    private DownloadReceiver downloadReceiver;
    private DownloadObserver downloadObserver;

    private DownloadFailedReceiver downloadFailedReceiver = new DownloadFailedReceiver();


    private Downloader(Activity context) {
        this.context = context;
        //注册全局生命周期回调
        context.getApplication().registerActivityLifecycleCallbacks(this);
    }

    private void download() {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }

        if (TextUtils.isEmpty(downloadUrl)) {
            throw new NullPointerException("downloadUrl must not be null");
        }

        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }

        if (downloadManager != null && isDownloading(downloadUrl)) {
            LogUtils.debug("downloading...");
            return;
        }

        String fileAbsPath;
        if (TextUtils.isEmpty(filePath) && TextUtils.isEmpty(dirName)) {
            fileAbsPath = getFileAbsPath(Environment.DIRECTORY_DOWNLOADS, fileName);
        } else if (!TextUtils.isEmpty(dirName)) {
            fileAbsPath = getFileAbsPath(dirName, fileName);
        } else {
            fileAbsPath = filePath + File.separator + fileName;
        }

        if (TextUtils.isEmpty(fileAbsPath)) {
            return;
        }
        File downloadFile = new File(fileAbsPath);
        if (downloadFile.exists()) {
            if (overlayDownload) {
                if (!downloadFile.delete()) {
                    LogUtils.debug("file delete failed!");
                    return;
                }
            } else {
                //文件已存在 直接安装
                InstallUtils.install(context, downloadFile);
                return;
            }
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager
                .Request.NETWORK_WIFI);

        request.setAllowedOverRoaming(allowedOverRoaming);

        request.setTitle(TextUtils.isEmpty(title) ? fileName : title);

        request.setNotificationVisibility(showNotification ? DownloadManager.Request.VISIBILITY_VISIBLE
                : DownloadManager.Request.VISIBILITY_HIDDEN);
        if (TextUtils.isEmpty(fileName)) {
            fileName = Utils.getFileNameForUrl(downloadUrl);
        }

        //设置下载路径
        request.setDestinationUri(Uri.fromFile(downloadFile));

        //将下载请求加入下载队列
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等
        Utils.showToast(context, context.getString(R.string.downloading));
        mTaskId = downloadManager.enqueue(request);
        if (downloadFailedReceiver != null) {
            context.registerReceiver(downloadFailedReceiver,
                    new IntentFilter(DownloadFailedReceiver.TAG));
        }
    }

    private boolean isDownloading(String url) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).equals(url)) {
                LogUtils.debug("downloading...");
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    private String getFileAbsPath(String dirType, String subPath) {
        File file = Environment.getExternalStoragePublicDirectory(dirType);
        if (file == null) {
            throw new IllegalStateException("Failed to get external storage public directory");
        } else if (file.exists()) {
            if (!file.isDirectory()) {
                throw new IllegalStateException(file.getAbsolutePath() +
                        " already exists and is not a directory");
            }
        } else {
            if (!file.mkdirs()) {
                throw new IllegalStateException("Unable to create directory: " +
                        file.getAbsolutePath());
            }
        }
        if (subPath == null) {
            throw new NullPointerException("subPath cannot be null");
        }

        return file.getAbsolutePath() + "/" + subPath;
    }

    /**
     * 注册下载完成的监听
     */
    public void registerDownloadReceiver() {
        if (downloadReceiver == null) {
            downloadReceiver = new DownloadReceiver();
        }
        context.registerReceiver(downloadReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 解绑下载完成的监听
     */
    public void unRegisterDownloadReceiver() {
        if (downloadReceiver != null) {
            context.unregisterReceiver(downloadReceiver);
        }
    }

    private ArrayList<ProgressListener> listeners;

    /**
     * 添加下载进度回调
     */
    public void addProgressListener(ProgressListener progressListener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (!listeners.contains(progressListener)) {
            listeners.add(progressListener);
        }
        if (downloadObserver == null && handler != null && downloadManager != null) {
            downloadObserver = new DownloadObserver(handler, downloadManager, mTaskId);
            context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"),
                    true, downloadObserver);
        }
    }

    /**
     * 移除下载进度回调
     */
    public void removeProgressListener(ProgressListener progressListener) {
        if (!listeners.contains(progressListener)) {
            throw new NullPointerException("this progressListener not attch Downloader");
        }
        listeners.remove(progressListener);
        if (listeners.isEmpty() && downloadObserver != null) {
            context.getContentResolver().unregisterContentObserver(downloadObserver);
        }
    }


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            long cutBytes = data.getLong(DownloadObserver.CURBYTES);
            long totalBytes = data.getLong(DownloadObserver.TOTALBYTES);
            int progress = data.getInt(DownloadObserver.PROGRESS);
            if (listeners != null && !listeners.isEmpty()) {
                for (ProgressListener listener : listeners) {
                    listener.onProgressChange(totalBytes, cutBytes, progress);
                }
            }
            return false;
        }
    });


    public static class Builder {

        private Downloader mDownloader;

        public Builder(Activity context) {
            synchronized (Downloader.class) {
                if (mDownloader == null) {
                    synchronized (Downloader.class) {
                        mDownloader = new Downloader(context);
                    }
                }
            }
        }

        /**
         * 设置下载下来的文件名
         *
         * @param fileName 文件的名字
         * @return
         */
        public Builder setFileName(String fileName) {
            mDownloader.fileName = fileName;
            return this;
        }

        /**
         * 设置下载路径
         *
         * @param filePath 自定义的全路径
         * @return
         */
        public Builder setFilePath(String filePath) {
            mDownloader.filePath = filePath;
            return this;
        }

        /**
         * 设置下载目录
         *
         * @param dirName sd卡的文件夹名字
         * @return
         */
        public Builder setDirName(String dirName) {
            mDownloader.dirName = dirName;
            return this;
        }

        /**
         * 设置下载的链接地址
         *
         * @param downloadUrl 下载链接
         * @return
         */
        public Builder setDownloadUrl(String downloadUrl) {
            mDownloader.downloadUrl = downloadUrl;
            return this;
        }

        /**
         * 通知栏显示的标题
         *
         * @param title 标题
         * @return
         */
        public Builder setNotificationTitle(String title) {
            mDownloader.title = title;
            return this;
        }

        /**
         * 隐藏通知栏
         *
         * @return
         */
        public Builder showNotification(boolean showNotification) {
            mDownloader.showNotification = showNotification;
            return this;
        }

        /**
         * 覆盖下载
         *
         * @return
         */
        public Builder overlayDownload() {
            mDownloader.overlayDownload = true;
            return this;
        }

        public Builder addProgressListener(ProgressListener listener) {
            if (listener != null) {
                mDownloader.addProgressListener(listener);
            }
            return this;
        }

        public Builder registerDownloadReceiver() {
            mDownloader.registerDownloadReceiver();
            return this;
        }

        /**
         * 是否为debug模式，会输出很多log信息
         *
         * @return
         */
        public Builder debug() {
            LogUtils.isDebug = true;
            return this;
        }

        /**
         * 允许漫游网络可下载
         *
         * @return
         */
        public Builder allowedOverRoaming() {
            mDownloader.allowedOverRoaming = true;
            return this;
        }

        /**
         * 开始下载
         *
         * @return
         */
        public Downloader start() {
            mDownloader.checkPermissions();
            return mDownloader;
        }

    }

    private void checkPermissions() {
        EasyPermission.with(context)
                .addPermissions(Permission.Group.STORAGE)//存储
                .request(new PermissionRequestListener() {
                    @Override
                    public void onGrant(Map<String, GrantResult> result) {
                        //权限申请返回
                        boolean allGrant = true;
                        for (GrantResult grantResult : result.values()) {
                            if (grantResult == GrantResult.DENIED) {
                                allGrant = false;
                                break;
                            }
                        }
                        if (allGrant) {
                            download();
                        } else {
                            Utils.showToast(context, "拒绝了授权");
                        }
                    }

                    @Override
                    public void onCancel(String stopPermission) {
                        //在addRequestPermissionRationaleHandler的处理函数里面调用了NextAction.next(NextActionType.STOP,就会中断申请过程，直接回调到这里来
                        Utils.showToast(context, "取消了授权");
                    }
                });
    }


    /**
     * 下载进度监听器
     */
    public interface ProgressListener {
        /**
         * 下载进度变化时回调
         *
         * @param totalBytes 下载总字节数
         * @param curBytes   当前下载的字节数
         * @param progress   当前的进度
         */
        void onProgressChange(long totalBytes, long curBytes, int progress);
    }

    /**
     * 下载失败的广播
     */
    public class DownloadFailedReceiver extends BroadcastReceiver {

        public static final String TAG = "DownloadFailedReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.debug("开始重新下载");
            download();
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LogUtils.debug("onActivityCreated: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LogUtils.debug("onActivityStarted: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        LogUtils.debug("onActivityResumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LogUtils.debug("onActivityPaused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LogUtils.debug("onActivityStopped: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        LogUtils.debug("onActivitySaveInstanceState: " + activity.getClass().getSimpleName());
    }

    /**
     * @param activity 当前即将销毁的Activity
     */
    @Override
    public void onActivityDestroyed(Activity activity) {
        LogUtils.debug("onActivityDestroyed: " + activity.getClass().getSimpleName());
        //为了放指内存泄漏 在Activity 销毁时解除广播
        try {
            if (activity.getClass().getSimpleName().equals(context.getClass().getSimpleName())) {
                activity.unregisterReceiver(downloadFailedReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
