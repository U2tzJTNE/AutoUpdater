package com.u2tzjtne.autoupdater.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.u2tzjtne.autoupdater.AutoUpdater;
import com.u2tzjtne.autoupdater.downloader.Downloader;

/**
 * @author u2tzjtne
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                        .setApkPath("")
                        .isShowNotification(true)
                        .setDownloadProgressListener(new Downloader.ProgressListener() {
                            @Override
                            public void onProgressChange(long totalBytes, long curBytes, int progress) {

                            }
                        })
                        .setNotificationTitle("")
                        .start();
                break;
            default:
                break;
        }
    }
}
