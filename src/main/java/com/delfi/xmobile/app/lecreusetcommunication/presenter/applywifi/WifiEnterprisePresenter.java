package com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.lib.lecreusetbase.utils.NetworkUtil;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

/**
 * Created by USER on 11/14/2019.
 */
public class WifiEnterprisePresenter extends BaseWifiPresenter {
    public static final String QUOTE = "\"";

    public WifiEnterprisePresenter(@NonNull Activity context) {
        super(context);
        previousSSID = unquoted(getCurrentNetworkSSID());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void applyNetwork(@NonNull Network network) {

        //TODO begin DK snip code
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + network.SSID + "\"";

        config.priority = 1;

        WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        enterpriseConfig.setIdentity(network.Username);
        enterpriseConfig.setPassword(network.UserPassword);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
        enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Phase2.NONE);
        config.enterpriseConfig = enterpriseConfig;
        WifiManager myWifiManager = (WifiManager) context.getApplicationContext().getSystemService(context.WIFI_SERVICE);
        //TODO end DK snip code

        if (myWifiManager != null) {
            for (int i = 0; i < MAX_RETRY; i++) {
                try {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - manual connect: " + i, null));
                    tryToConnect(myWifiManager, config);
                    Thread.sleep(5000);
                    if (NetworkUtil.checkConnectedToServer()) return;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int count = 0;
        do {
            try {
                count++;
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - wifi utils connect: " + count, null));
                tryToConnect(network, config, count);
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!NetworkUtil.checkConnectedToServer() && count < MAX_RETRY);
    }

    private void tryToConnect(final WifiManager wifiManager, final WifiConfiguration wifiConfiguration) {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isFailed = false;
                boolean isWifiEnabled = wifiManager.setWifiEnabled(true);

                //Error on enable wifi
                if (!isWifiEnabled) {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[USE ENTERPRISE] - SetWifiEnabled returned false", null));
                    isFailed = true;
                }

                //Clean current connection if exist
                if (!previousSSID.equalsIgnoreCase(unquoted(wifiConfiguration.SSID))) {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - ChangeNetwork... From: " + previousSSID + " -> " + unquoted(wifiConfiguration.SSID), null));

                    int networkId = getCurrentNetworkID();
                    if (networkId != -1) {
                        boolean isRemoved = wifiManager.removeNetwork(networkId);
                        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - RemoveNetwork returned " + isRemoved, null));

                        if (!isRemoved) {
                            boolean isDisabled = wifiManager.disableNetwork(networkId);
                            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.WARNING, "[USE ENTERPRISE] - Can't remove network, try to disable network returned " + isDisabled, null));
                        }
                    }
                } else {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - UpdateNetwork... " + previousSSID, null));
                }

                int res = wifiManager.addNetwork(wifiConfiguration);

                if (res == -1) {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[USE ENTERPRISE] - AddNetwork returned false", null));
                    isFailed = true;
                    wifiManager.setWifiEnabled(false);
                }

                if (isFailed) {
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[USE ENTERPRISE] - Failed!", null));

                } else {
                    boolean isSave = wifiManager.saveConfiguration();
                    boolean isEnabled = wifiManager.enableNetwork(res, true);

                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE ENTERPRISE] - SaveConfiguration returned " + isSave + " - EnableNetwork returned " + isEnabled, null));
                }
            }
        });
    }

    private WifiInfo getCurrentNetworkInfo() {
        final ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (cm != null && wifiManager != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {  //wifi
                return wifiManager.getConnectionInfo();
            }
        }

        return null;
    }

    private String getCurrentNetworkSSID() {
        WifiInfo wifiInfo = getCurrentNetworkInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return "";
    }

    private int getCurrentNetworkID() {
        WifiInfo wifiInfo = getCurrentNetworkInfo();
        if (wifiInfo != null) {
            return wifiInfo.getNetworkId();
        }
        return -1;
    }

    @NonNull
    private String unquoted(@NonNull String ssid) {
        return ssid.replaceAll(QUOTE, "");
    }
}
