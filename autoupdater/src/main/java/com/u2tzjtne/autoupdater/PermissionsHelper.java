package com.u2tzjtne.autoupdater;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Random;

/**
 * @author u2tzjtne
 */
public class PermissionsHelper extends Fragment {
    private SparseArray<PermissionListener> mCallbacks = new SparseArray<>();
    private Random mCodeGenerator = new Random();

    public interface PermissionListener {
        void onGranted();

        void onDenied();
    }

    public static PermissionsHelper newInstance() {
        return new PermissionsHelper();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置为 true，表示 configuration change 的时候，fragment 实例不会背重新创建
        setRetainInstance(true);
    }

    public void requestPermissions(PermissionListener callback, @NonNull String... permissions) {
        int requestCode = makeRequestCode();
        mCallbacks.put(requestCode, callback);
        requestPermissions(permissions, requestCode);
    }

    /**
     * 随机生成唯一的requestCode，最多尝试10次
     *
     * @return
     */
    private int makeRequestCode() {
        int requestCode;
        int tryCount = 0;
        do {
            requestCode = mCodeGenerator.nextInt(0x0000FFFF);
            tryCount++;
        } while (mCallbacks.indexOfKey(requestCode) >= 0 && tryCount < 10);
        return requestCode;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handlePermissionCallBack(requestCode, grantResults);
    }

    private void handlePermissionCallBack(int requestCode, @NonNull int[] grantResults) {
        PermissionListener callback = mCallbacks.get(requestCode);
        mCallbacks.remove(requestCode);

        if (callback == null) {
            return;
        }

        boolean allGranted = false;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
            allGranted = true;
        }
        if (allGranted) {
            callback.onGranted();
        } else {
            callback.onDenied();
        }
    }
}
