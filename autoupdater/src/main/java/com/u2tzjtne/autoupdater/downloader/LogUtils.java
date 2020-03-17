package com.u2tzjtne.autoupdater.downloader;

import android.util.Log;


/**
 * @author u2tzjtne
 */
public class LogUtils {

    private static final String TAG = "Downloader";
    public static boolean isDebug = true;

    public static void debug(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }
}
