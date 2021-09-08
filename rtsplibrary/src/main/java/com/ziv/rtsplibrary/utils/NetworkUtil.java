package com.ziv.rtsplibrary.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.ziv.rtsplibrary.config.Constant;

import java.util.Locale;

public class NetworkUtil {
    private static int DEFAULT_RTSP_PORT = Constant.DEFAULT_RTSP_PORT;

    public static void changeDefaultPort(int port) {
        DEFAULT_RTSP_PORT = port;
    }

    public static String displayIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        StringBuilder ipaddress = new StringBuilder();
        if (info != null && info.getNetworkId() > -1) {
            int i = info.getIpAddress();
            String ip = String.format(Locale.ENGLISH, "%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff, i >> 16 & 0xff, i >> 24 & 0xff);
            ipaddress.append("rtsp://").append(ip).append(":").append(DEFAULT_RTSP_PORT);
        }
        return ipaddress.toString();
    }
}
