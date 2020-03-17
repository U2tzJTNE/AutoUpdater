package com.u2tzjtne.autoupdater.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.u2tzjtne.autoupdater.AutoUpdater;
import com.u2tzjtne.autoupdater.downloader.Downloader;

import java.util.Objects;

/**
 * @author u2tzjtne
 */
public class MainActivity extends AppCompatActivity {

    private TextView mTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTipView = findViewById(R.id.tv_tip);
    }

    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btn_local:
                AutoUpdater.with(this)
                        .setApkPath("")
                        .start();
                break;
            case R.id.btn_net:
                AutoUpdater.with(this)
                        .setApkPath("http://www.u2tzjtne.cn:10080/files/Debug_home_2.0.23.apk")
                        .setDownloadDir(Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath())
                        .setDownloadFileName("App.apk")
                        .setDownloadProgressListener(new Downloader.ProgressListener() {
                            @Override
                            public void onProgressChange(long totalBytes, long curBytes, int progress) {
                                mTipView.setText("下载进度：" + progress);
                            }
                        })
                        .isShowNotification(true)
                        .setNotificationTitle("正在下载更新...")
                        .start();
                break;
            default:
                break;
        }
    }
}
