package com.u2tzjtne.autoupdater.downloader;

import android.app.DownloadManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * @author u2tzjtne
 */
public class DownloadObserver extends ContentObserver {

    private DownloadManager mDownloadManager;
    private Handler mHandler;
    private Bundle bundle = new Bundle();
    private DownloadManager.Query query;
    private Cursor cursor;

    static final String CURBYTES = "curBytes";
    static final String TOTALBYTES = "totalBytes";
    static final String PROGRESS = "progress";

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    DownloadObserver(Handler handler, DownloadManager downloadManager, long taskId) {
        super(handler);
        this.mHandler = handler;
        this.mDownloadManager = downloadManager;
        query = new DownloadManager.Query().setFilterById(taskId);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            cursor = mDownloadManager.query(query);
            if (cursor == null) {
                return;
            }
            cursor.moveToFirst();
            long curBytes = cursor
                    .getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            long totalBytes = cursor
                    .getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            int mProgress = (int) ((curBytes * 100) / totalBytes);
            LogUtils.debug("curBytes==" + curBytes);
            LogUtils.debug("totalBytes==" + totalBytes);
            LogUtils.debug("mProgress------->" + mProgress);
            Message message = mHandler.obtainMessage();
            bundle.putLong(CURBYTES, curBytes);
            bundle.putLong(TOTALBYTES, totalBytes);
            bundle.putInt(PROGRESS, mProgress);
            message.setData(bundle);
            mHandler.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
