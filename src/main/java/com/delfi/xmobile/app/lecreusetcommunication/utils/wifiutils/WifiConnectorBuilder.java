package com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils;

import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiConfig;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.ConnectionScanResultsListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiScan.ScanResultsListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiState.WifiStateListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiWps.ConnectionWpsListener;

public interface WifiConnectorBuilder {
    void start();

    interface WifiUtilsBuilder {
        void enableWifi(WifiStateListener wifiStateListener);

        void enableWifi();

        void disableWifi();

        @NonNull
        WifiConnectorBuilder scanWifi(@Nullable ScanResultsListener scanResultsListener);

        @NonNull
        WifiSuccessListener connectWith(@NonNull String ssid, @NonNull String password);

        @NonNull
        WifiSuccessListener connectWith(@NonNull String ssid, @NonNull String bssid, @NonNull String password);

        @NonNull
        WifiSuccessListener connectWithScanResult(@NonNull String password, @Nullable ConnectionScanResultsListener connectionScanResultsListener);

        @NonNull
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        WifiWpsSuccessListener connectWithWps(@NonNull String bssid, @NonNull String password);

        void cancelAutoConnect();

    }

    interface WifiSuccessListener {
        @NonNull
        WifiSuccessListener setTimeout(long timeOutMillis);

        @NonNull
        WifiConnectorBuilder onConnectionResult(@Nullable ConnectionSuccessListener successListener);
    }

    interface WifiWpsSuccessListener {
        @NonNull
        WifiWpsSuccessListener setWpsTimeout(long timeOutMillis);

        @NonNull
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        WifiConnectorBuilder onConnectionWpsResult(@Nullable ConnectionWpsListener successListener);
    }

    WifiConnectorBuilder securityType(WifiConfig.Type type);

    WifiConnectorBuilder setStaticWifiConfiguration(WifiConfiguration wifiConfiguration);
}
