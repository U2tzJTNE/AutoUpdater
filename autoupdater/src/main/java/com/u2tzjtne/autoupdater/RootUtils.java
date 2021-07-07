package com.u2tzjtne.autoupdater;


import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author u2tzjtne
 */
public class RootUtils {
    private static final String TAG = "RootUtils";
    public static boolean isRoot() {
        PrintWriter printWriter;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            printWriter = new PrintWriter(process.getOutputStream());
            printWriter.flush();
            printWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * root下执行cmd的返回值
     */
    private static boolean returnResult(int value) {
        switch (value) {
            case 0:
                // 代表成功
                return true;
            case 1:
                // 失败
                return false;
            default:
                // 未知情况
                return false;
        }
    }

    /**
     * root下静默安装
     */
    private static boolean rootSilentInstallApk(String apkPath) {
        PrintWriter printWriter;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            printWriter = new PrintWriter(process.getOutputStream());
            printWriter.println("pm install -r " + apkPath);
            printWriter.flush();
            printWriter.close();
            //execLinuxCommand();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 通过Linux延时执行重启
     */
    private static void execLinuxCommand() {
        //TODO 补充自己项目的activity路径
        String activityPath = "xxxx.xxxx.xxx/xxxx.GuideActivity";
        //usleep 后面是微秒  1000微妙=1ms
        String cmd = "usleep 30000000; am start -n " + activityPath;
        //Runtime对象
        Runtime runtime = Runtime.getRuntime();
        try {
            Process localProcess = runtime.exec("su");
            OutputStream localOutputStream = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream(localOutputStream);
            localDataOutputStream.writeBytes(cmd);
            localDataOutputStream.flush();
            Log.d(TAG,"设备准备重启");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rootInstallApk(File file) {
        if (!isRoot()) {
            Log.e(TAG,"系统尚无Root权限");
            return;
        }
        Log.d(TAG,"系统有Root权限");
        if (!file.exists()) {
            Log.e(TAG,"尚无APK文件");
            return;
        }
        //静默安装
        if (rootSilentInstallApk(file.getAbsolutePath())) {
            Log.d(TAG,"静默安装成功");
        } else {
            Log.e(TAG,"静默安装失败！！！");
        }
    }
}
