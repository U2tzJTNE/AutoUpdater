package com.u2tzjtne.autoupdater.downloader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author u2tzjtne
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String getFileNameForUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("url is null");
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param context
     * @param path    文件路径
     * @return 是否是外部存储目录
     */
    public static boolean isExternalPath(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String packageName = getPackageName(context);
        String appPath = "/data/data/" + packageName;
        return !path.startsWith(appPath);
    }

    /**
     * 判断是否是下载路径
     *
     * @param path
     * @return
     */
    public static boolean isDownloadPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return path.startsWith("http://") ||
                path.startsWith("https://") ||
                path.startsWith("HTTP://") ||
                path.startsWith("HTTPS://");
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
