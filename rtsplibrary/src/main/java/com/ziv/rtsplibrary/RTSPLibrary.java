package com.ziv.rtsplibrary;

import android.content.Context;
import android.content.Intent;

import com.ziv.rtsplibrary.rtsp.RtspServer;
import com.ziv.rtsplibrary.utils.NetworkUtil;
import com.ziv.rtsplibrary.utils.RunState;
import com.ziv.rtsplibrary.utils.ToastUtil;
import com.ziv.rtsplibrary.utils.ui.PermissionActivity;

public class RTSPLibrary {
    private volatile static boolean mIsRun = false;

    private RTSPLibrary() {
    }

    private static class RTSPLibrary$ {
        private static final RTSPLibrary INSTANCE = new RTSPLibrary();
    }

    public static RTSPLibrary getInstance() {
        return RTSPLibrary$.INSTANCE;
    }

    public void start(Context context) {
        try {
            // Context applicationContext = context.getApplicationContext();
            Intent intent = new Intent();
            intent.setClass(context, PermissionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtil.showLong(context, "PermissionActivity未找到");
        }
    }

    public String getRtspAddress(Context context) {
        Context applicationContext = context.getApplicationContext();
        return NetworkUtil.displayIpAddress(applicationContext);
    }

    public void release(Context context) {
        try {
            Intent rtspServerIntent = new Intent(context, RtspServer.class);
            context.stopService(rtspServerIntent);
        } catch (Exception e) {
            ToastUtil.showLong(context, "PermissionActivity未找到");
        }
    }
}
