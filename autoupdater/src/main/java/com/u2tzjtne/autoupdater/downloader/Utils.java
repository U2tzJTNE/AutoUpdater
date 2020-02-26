package com.u2tzjtne.autoupdater.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.info.aegis.webapp.App;
import com.info.aegis.webapp.activity.ImageActivity;
import com.info.aegis.webapp.activity.PDFActivity;
import com.info.aegis.webapp.util.FileUtils;
import com.info.aegis.webapp.util.ToastUtils;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import wendu.dsbridge.CompletionHandler;

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

    public static void installApk(Context context, Uri uri) {
        File file = new File(Objects.requireNonNull(uri.getPath()));
        if (!file.exists()) {
            LogUtils.debug("file not exists");
            return;
        }
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            String packageName = context.getPackageName();
            LogUtils.debug("packageName==" + packageName);
            Uri providerUri = FileProvider
                    .getUriForFile(context, packageName + ".fileProvider", file);
            LogUtils.debug("providerUri==" + providerUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(providerUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        context.startActivity(intent);
    }

    public static void openFile(final Uri uri) {
        if (uri == null) {
            return;
        }
        Uri decodedUri = Uri.parse(Uri.decode(uri.toString()));
        if (decodedUri == null) {
            return;
        }
        String fileUrl = decodedUri.toString();
        LogUtils.debug("fileUrl: " + fileUrl);
        int index = fileUrl.lastIndexOf(".");
        if (index < 0) {
            return;
        }
        String ext = fileUrl.substring(index).toLowerCase(Locale.US);
        LogUtils.debug("ext: " + ext);
        if (TextUtils.isEmpty(ext)) {
            return;
        }
        if (ext.equals(".pdf") || ext.equals(".PDF")) {
            Intent intent = new Intent(App.getInstance(), PDFActivity.class);
            intent.putExtra("path", uri.getPath());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
            return;
        }

        if (ext.equals(".jpg") || ext.equals(".png") || ext.equals(".JPG") || ext.equals(".PNG")) {
            Intent intent = new Intent(App.getInstance(), ImageActivity.class);
            intent.putExtra("path", fileUrl);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
            return;
        }
        StrictMode.VmPolicy defaultVmPolicy = null;
        boolean isNeedMatch = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
        try {
            if (isNeedMatch) {
                defaultVmPolicy = StrictMode.getVmPolicy();
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String temp = ext.substring(1);
            String mime = mimeTypeMap.getMimeTypeFromExtension(temp);
            mime = TextUtils.isEmpty(mime) ? "" : mime;
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(decodedUri, mime);
            Objects.requireNonNull(App.getInstance()).startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.s("无法打开后缀名为" + ext + "的文件！");
        } finally {
            if (isNeedMatch) {
                StrictMode.setVmPolicy(defaultVmPolicy);
            }
        }
    }


    public static void openFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "fileName is null or empty");
            return;
        }
        int index = fileName.lastIndexOf(".");
        if (index < 0) {
            return;
        }
        String ext = fileName.substring(index).toLowerCase(Locale.US);
        LogUtils.debug("ext: " + ext);
        if (TextUtils.isEmpty(ext)) {
            return;
        }
        if (ext.equals(".pdf") || ext.equals(".PDF")) {
            Intent intent = new Intent(App.getInstance(), PDFActivity.class);
            intent.putExtra("fileName", fileName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
            return;
        }
        if (ext.equals(".jpg") || ext.equals(".png") || ext.equals(".JPG") || ext.equals(".PNG")) {
            Intent intent = new Intent(App.getInstance(), ImageActivity.class);
            intent.putExtra("fileName", fileName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
        }
    }

    public static void deleteAllFiles(Context context, CompletionHandler<Boolean> handler) {
        handler.complete(FileUtils.deleteAllInDir(context.getExternalCacheDir()));
    }

    public static void deleteAllFilesForDate(Context context, Long date, CompletionHandler<Boolean> handler) {
        try {
            File dir = context.getExternalCacheDir();
            if (dir != null) {
                for (File file : Objects.requireNonNull(dir.listFiles())) {
                    if (file.isFile() && file.lastModified() <= date) {
                        file.delete();
                    }
                }
                handler.complete(true);
            } else {
                handler.complete(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.complete(false);
        }
    }

    public static void isFileExist(Context context, String fileName, CompletionHandler<Boolean> handler) {
        String filePath = context.getExternalCacheDir() + "/" + fileName;
        handler.complete(FileUtils.isFileExist(filePath));
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
