package com.u2tzjtne.autoupdater;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.List;

/**
 * 辅助服务自动安装APP，该服务在单独进程中允许
 *
 * @author u2tzjtne
 */
public class AutoInstallService extends AccessibilityService {
    private static final String TAG = AutoInstallService.class.getSimpleName();
    @Override
    protected void onServiceConnected() {
        Log.i(TAG, "onServiceConnected: ");
        Toast.makeText(this, getString(R.string.aby_label) + "开启了", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        Toast.makeText(this, getString(R.string.aby_label) + "停止了，请重新开启", Toast.LENGTH_LONG).show();
        // 服务停止，重新进入系统设置界面
        AccessibilityUtils.jumpToSetting(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event==null||event.getPackageName()==null){
            return;
        }
        Log.d(TAG, "当前包名：" + event.getPackageName().toString());
        //不写完整包名，是因为某些手机(如小米)安装器包名是自定义的
        if (event.getPackageName().toString().contains("packageinstaller")) {
            /*
             模拟点击->自动安装，只验证了小米5s plus(MIUI 9.8.4.26)、小米Redmi 5A(MIUI 9.2)、华为mate 10
             其它品牌手机可能还要适配，适配最可恶的就是出现安装广告按钮，误点安装其它垃圾APP（典型就是小米安装后广告推荐按钮，华为安装开始官方安装）
            */
            //当前窗口根节点
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode == null) {
                return;
            }
            Log.i(TAG, "rootNode: " + rootNode);
            if (isNotAD(rootNode)) {
                //一起执行：安装->下一步->打开,以防意外漏掉节点
                findTxtClick(rootNode, "安装");
            }
            findTxtClick(rootNode, "打开");
            rootNode.recycle();
        }

    }

    /**
     * 查找安装,并模拟点击(findAccessibilityNodeInfosByText判断逻辑是contains而非equals)
     *
     * @param nodeInfo
     * @param txt
     */
    private void findTxtClick(AccessibilityNodeInfo nodeInfo, String txt) {
        List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(txt);
        if (nodes == null || nodes.isEmpty()) {
            return;
        }
        Log.i(TAG, "findTxtClick: " + txt + ", " + nodes.size() + ", " + nodes);
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isEnabled() && node.isClickable() && ("android.widget.Button".contentEquals(node.getClassName())
                    // 兼容华为安装界面的复选框
                    || "android.widget.CheckBox".contentEquals(node.getClassName())
            )) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 排除广告[安装]按钮
     * TODO 现在只适配了小米和华为
     *
     * @param rootNode
     * @return
     */
    private boolean isNotAD(AccessibilityNodeInfo rootNode) {
        //小米
        return isNotFind(rootNode, "还喜欢")
                //华为
                && isNotFind(rootNode, "官方安装");
    }

    private boolean isNotFind(AccessibilityNodeInfo rootNode, String txt) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(txt);
        return nodes == null || nodes.isEmpty();
    }

    @Override
    public void onInterrupt() {
    }
}