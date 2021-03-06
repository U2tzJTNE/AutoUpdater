package com.u2tzjtne.autoupdater;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;


import androidx.core.content.FileProvider;

import java.io.File;

/**
 * 安装相关工具
 *
 * @author u2tzjtne
 */
public class InstallUtils {
    /**
     * 检查系统设置，并显示设置对话框
     */
    public static void checkSetting(final Context cxt) {
        if (isSettingOpen(cxt)) {
            return;
        }
        new AlertDialog.Builder(cxt)
                .setTitle(R.string.unknow_setting_title)
                .setMessage(R.string.unknow_setting_msg)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        jumpToInstallSetting(cxt);
                    }
                }).show();
    }

    /**
     * 检查系统设置：是否允许安装来自未知来源的应用
     */
    private static boolean isSettingOpen(Context cxt) {
        boolean canInstall;
        // Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canInstall = cxt.getPackageManager().canRequestPackageInstalls();
        } else {
            canInstall = Settings.Secure.getInt(cxt.getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0) == 1;
        }
        return canInstall;
    }

    /**
     * 跳转到系统设置：允许安装来自未知来源的应用
     */
    private static void jumpToInstallSetting(Context cxt) {
        // Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cxt.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + cxt.getPackageName())));
        } else {
            cxt.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        }
    }

    /**
     * 安装APK
     *
     * @param apkFile APK文件的本地路径
     */
    public static void install(Context cxt, File apkFile) {
        //判断有没有Root权限
        if (RootUtils.isRoot()){
            RootUtils.rootInstallApk(apkFile);
        }else {
            //唤醒屏幕,以便辅助功能模拟用户点击"安装"
            AccessibilityUtils.wakeUpScreen(cxt);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Android 7.0以上不允许Uri包含File实际路径，需要借助FileProvider生成Uri（或者调低targetSdkVersion小于Android 7.0欺骗系统）
                    uri = FileProvider.getUriForFile(cxt, cxt.getPackageName() + ".fileProvider", apkFile);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    uri = Uri.fromFile(apkFile);
                }
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                cxt.startActivity(intent);
            } catch (Throwable e) {
                Toast.makeText(cxt, "安装失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 安装APK
     *
     * @param apkPath APK文件的本地路径
     */
    public static void install(Context cxt, String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return;
        }
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            return;
        }
        install(cxt, apkFile);
    }
}