package com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiConfig;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.ConnectionScanResultsListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.WifiConnectionCallback;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.WifiConnectionReceiver;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiScan.ScanResultsListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiScan.WifiScanCallback;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiScan.WifiScanReceiver;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiState.WifiStateCallback;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiState.WifiStateListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiState.WifiStateReceiver;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiWps.ConnectionWpsListener;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.cleanPreviousConfiguration;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.connectToWifi;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.connectWps;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.matchScanResult;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.matchScanResultBssid;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.matchScanResultSsid;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.reenableAllHotspots;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.registerReceiver;
import static com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.ConnectorUtils.unregisterReceiver;

public final class WifiUtils implements WifiConnectorBuilder,
        WifiConnectorBuilder.WifiUtilsBuilder,
        WifiConnectorBuilder.WifiSuccessListener,
        WifiConnectorBuilder.WifiWpsSuccessListener {
    @NonNull
    private final WifiManager mWifiManager;
    @NonNull
    private final Context mContext;
    private static boolean mEnableLog;
    private long mWpsTimeoutMillis = 30000;
    private long mTimeoutMillis = 30000;
    @NonNull
    private static final String TAG = WifiUtils.class.getSimpleName();
    //@NonNull private static final WifiUtils INSTANCE = new WifiUtils();
    @NonNull
    private final WifiStateReceiver mWifiStateReceiver;
    @NonNull
    private final WifiConnectionReceiver mWifiConnectionReceiver;
    @NonNull
    private final WifiScanReceiver mWifiScanReceiver;
    @Nullable
    private String mSsid;
    @Nullable
    private String mBssid;
    @Nullable
    private String mPassword;
    @Nullable
    private ScanResult mSingleScanResult;
    @Nullable
    private ScanResultsListener mScanResultsListener;
    @Nullable
    private ConnectionScanResultsListener mConnectionScanResultsListener;
    @Nullable
    private ConnectionSuccessListener mConnectionSuccessListener;
    @Nullable
    private WifiStateListener mWifiStateListener;
    @Nullable
    private ConnectionWpsListener mConnectionWpsListener;
    @Nullable
    private WifiConfig.Type mSecurityType;
    @Nullable
    private WifiConfiguration mStaticWifiConfiguration;

    @NonNull
    private final WifiStateCallback mWifiStateCallback = new WifiStateCallback() {
        @Override
        public void onWifiEnabled() {
            wifiLog("WIFI ENABLED...");
            unregisterReceiver(mContext, mWifiStateReceiver);
            if(mWifiStateListener != null)
                mWifiStateListener.isSuccess(true);

            if (mScanResultsListener != null || mPassword != null) {
                wifiLog("START SCANNING....");
                if (mWifiManager.startScan())
                    registerReceiver(mContext, mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                else {
                    if(mScanResultsListener != null)
                        mScanResultsListener.onScanResults(new ArrayList<ScanResult>());
                    if(mConnectionWpsListener != null)
                        mConnectionWpsListener.isSuccessful(false);
                    mWifiConnectionCallback.errorConnect();
                    wifiLog("ERROR COULDN'T SCAN");
                }
            }
        }
    };

    @NonNull
    private final WifiScanCallback mWifiScanResultsCallback = new WifiScanCallback() {
        @Override
        public void onScanResultsReady() {
            wifiLog("GOT SCAN RESULTS");
            unregisterReceiver(mContext, mWifiScanReceiver);

            final List<ScanResult> scanResultList = mWifiManager.getScanResults();
            if(mScanResultsListener != null)
                mScanResultsListener.onScanResults(scanResultList);
            if(mConnectionScanResultsListener != null)
                mConnectionScanResultsListener.onConnectWithScanResult(scanResultList);
            if (mConnectionWpsListener != null && mBssid != null && mPassword != null) {
                mSingleScanResult = matchScanResultBssid(mBssid, scanResultList);
                if (mSingleScanResult != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    connectWps(mWifiManager, mSingleScanResult, mPassword, mWpsTimeoutMillis, mConnectionWpsListener);
                else {
                    if (mSingleScanResult == null)
                        wifiLog("Couldn't find network. Possibly out of range");
                    mConnectionWpsListener.isSuccessful(false);
                }
                return;
            }

            if (mSsid != null) {
                if (mBssid != null)
                    mSingleScanResult = matchScanResult(mSsid, mBssid, scanResultList);
                else
                    mSingleScanResult = matchScanResultSsid(mSsid, scanResultList);
            }
            if (mSingleScanResult != null && mPassword != null) {
                if(mSecurityType != null && mSecurityType != WifiConfig.Type.WPA)
                    mSingleScanResult.capabilities = mSecurityType.name();

                boolean isSuccessConnect;
                if (mStaticWifiConfiguration != null){
                    isSuccessConnect = connectToWifi(mContext, mWifiManager, mSingleScanResult, mStaticWifiConfiguration);
                } else {
                    isSuccessConnect = connectToWifi(mContext, mWifiManager, mSingleScanResult, mPassword);
                }

                if (isSuccessConnect) {
                    registerReceiver(mContext, mWifiConnectionReceiver.activateTimeoutHandler(mSingleScanResult),
                            new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                    registerReceiver(mContext, mWifiConnectionReceiver,
                            new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                } else
                    mWifiConnectionCallback.errorConnect();
            } else
                mWifiConnectionCallback.errorConnect();
        }
    };

    @NonNull
    private final WifiConnectionCallback mWifiConnectionCallback = new WifiConnectionCallback() {
        @Override
        public void successfulConnect() {
            wifiLog("CONNECTED SUCCESSFULLY");
            unregisterReceiver(mContext, mWifiConnectionReceiver);
            //reenableAllHotspots(mWifiManager);
            if(mConnectionSuccessListener != null)
                mConnectionSuccessListener.isSuccessful(true);
        }

        @Override
        public void errorConnect() {
            unregisterReceiver(mContext, mWifiConnectionReceiver);
            reenableAllHotspots(mWifiManager);
            //if (mSingleScanResult != null)
            //cleanPreviousConfiguration(mWifiManager, mSingleScanResult);

            if(mConnectionSuccessListener != null)
                mConnectionSuccessListener.isSuccessful(false);
        }
    };

    private WifiUtils(@NonNull Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null)
            throw new RuntimeException("WifiManager is not supposed to be null");
        mWifiStateReceiver = new WifiStateReceiver(mWifiStateCallback);
        mWifiScanReceiver = new WifiScanReceiver(mWifiScanResultsCallback);
        mWifiConnectionReceiver = new WifiConnectionReceiver(mWifiConnectionCallback, mWifiManager, mTimeoutMillis);
    }

    public static WifiUtilsBuilder withContext(@NonNull final Context context) {
        return new WifiUtils(context);
    }

    public static void wifiLog(final String text) {
        if (mEnableLog) {
            Log.d("TAG", "WifiLog: " + text);
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.INFO, "WifiLog: " + text ,null));
        }
    }

    public static void enableLog(final boolean enabled) {
        mEnableLog = enabled;
    }

    @Override
    public void enableWifi(@Nullable final WifiStateListener wifiStateListener) {
        mWifiStateListener = wifiStateListener;
        if (mWifiManager.isWifiEnabled())
            mWifiStateCallback.onWifiEnabled();
        else {
            if (mWifiManager.setWifiEnabled(true))
                registerReceiver(mContext, mWifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            else {
                if(wifiStateListener != null)
                    wifiStateListener.isSuccess(false);
                if(mScanResultsListener != null)
                    mScanResultsListener.onScanResults(new ArrayList<ScanResult>());
                if(mConnectionWpsListener != null)
                    mConnectionWpsListener.isSuccessful(false);
                mWifiConnectionCallback.errorConnect();
                wifiLog("COULDN'T ENABLE WIFI");
            }
        }
    }

    @Override
    public void enableWifi() {
        enableWifi(null);
    }

    @NonNull
    @Override
    public WifiConnectorBuilder scanWifi(final ScanResultsListener scanResultsListener) {
        mScanResultsListener = scanResultsListener;
        return this;
    }

    @NonNull
    @Override
    public WifiConnectorBuilder.WifiSuccessListener connectWith(@NonNull final String ssid, @NonNull final String password) {
        mSsid = ssid;
        mPassword = password;
        return this;
    }

    @NonNull
    @Override
    public WifiConnectorBuilder.WifiSuccessListener connectWith(@NonNull final String ssid, @NonNull final String bssid, @NonNull final String password) {
        mSsid = ssid;
        mBssid = bssid;
        mPassword = password;
        return this;
    }

    @NonNull
    @Override
    public WifiConnectorBuilder.WifiSuccessListener connectWithScanResult(@NonNull final String password,
                                                                          @Nullable final ConnectionScanResultsListener connectionScanResultsListener) {
        mConnectionScanResultsListener = connectionScanResultsListener;
        mPassword = password;
        return this;
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WifiWpsSuccessListener connectWithWps(@NonNull final String bssid, @NonNull final String password) {
        mBssid = bssid;
        mPassword = password;
        return this;
    }

    @Override
    public void cancelAutoConnect() {
        unregisterReceiver(mContext, mWifiStateReceiver);
        unregisterReceiver(mContext, mWifiScanReceiver);
        unregisterReceiver(mContext, mWifiConnectionReceiver);
        if(mSingleScanResult != null)
            cleanPreviousConfiguration(mWifiManager, mSingleScanResult);
        reenableAllHotspots(mWifiManager);
    }

    @Override
    public WifiConnectorBuilder securityType(WifiConfig.Type type) {
        mSecurityType = type;
        return this;
    }

    @Override
    public WifiConnectorBuilder setStaticWifiConfiguration(WifiConfiguration wifiConfiguration) {
        mStaticWifiConfiguration = wifiConfiguration;
        return this;
    }

    @NonNull
    @Override
    public WifiConnectorBuilder.WifiSuccessListener setTimeout(final long timeOutMillis) {
        mTimeoutMillis = timeOutMillis;
        mWifiConnectionReceiver.setTimeout(timeOutMillis);
        return this;
    }

    @NonNull
    @Override
    public WifiWpsSuccessListener setWpsTimeout(final long timeOutMillis) {
        mWpsTimeoutMillis = timeOutMillis;
        return this;
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WifiConnectorBuilder onConnectionWpsResult(@Nullable final ConnectionWpsListener successListener) {
        mConnectionWpsListener = successListener;
        return this;
    }


    @NonNull
    @Override
    public WifiConnectorBuilder onConnectionResult(@Nullable final ConnectionSuccessListener successListener) {
        mConnectionSuccessListener = successListener;
        return this;
    }

    @Override
    public void start() {
        unregisterReceiver(mContext, mWifiStateReceiver);
        unregisterReceiver(mContext, mWifiScanReceiver);
        unregisterReceiver(mContext, mWifiConnectionReceiver);
        enableWifi(null);
    }

    @Override
    public void disableWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
            unregisterReceiver(mContext, mWifiStateReceiver);
            unregisterReceiver(mContext, mWifiScanReceiver);
            unregisterReceiver(mContext, mWifiConnectionReceiver);
        }
        wifiLog("WiFi Disabled");
    }
}
