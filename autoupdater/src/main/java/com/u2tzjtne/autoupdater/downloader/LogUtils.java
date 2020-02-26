package com.u2tzjtne.autoupdater.downloader;

import android.util.Log;

import com.info.aegis.webapp.BuildConfig;


/**
 * @author u2tzjtne
 */
class LogUtils {

    private static final String TAG = "Downloader";
    static boolean isDebug = BuildConfig.DEBUG;

    static void debug(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

}
