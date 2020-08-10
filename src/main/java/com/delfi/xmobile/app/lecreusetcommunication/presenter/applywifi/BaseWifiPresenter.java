package com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applynetwork.BaseNetworkPresenter;
import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiConfig;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.WifiUtils;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;


/**
 * Created by USER on 11/14/2019.
 */
public abstract class BaseWifiPresenter extends BaseNetworkPresenter {
    protected String previousSSID = "";

    public BaseWifiPresenter(@NonNull Activity context) {
        super(context);
    }

    public void setPreviousSSID(@NonNull String previousSSID) {
        this.previousSSID = previousSSID;
    }

    protected void tryToConnect(final Network network, @Nullable final WifiConfiguration wifiConfiguration, final int count) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WifiUtils.enableLog(true);
                WifiUtils.withContext(context.getApplicationContext())
                        .connectWith(network.SSID, network.WifiPassword)
                        .onConnectionResult(new ConnectionSuccessListener() {
                            @Override
                            public void isSuccessful(boolean b) {
                                if (!b) {
                                    if (count == MAX_RETRY) {
                                        Toast.makeText(context, R.string.not_connect_to_wifi, Toast.LENGTH_LONG).show();
                                    }
                                } else if (!previousSSID.equalsIgnoreCase(network.SSID)) {
                                    Toast.makeText(context, R.string.new_wifi_connected, Toast.LENGTH_LONG).show();
                                }
                                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG,
                                        "Connection Result: " + b, null));
                            }
                        })
                        .setStaticWifiConfiguration(wifiConfiguration)
                        .securityType(convertType(network.Encryption))
                        .start();
            }
        });
    }

    /**
     * Follow arrays.xml
     * <p>
     * <string-array name="opt_encryption">
     * <item>WEP</item>
     * <item>WPA</item>
     * <item>WPA2</item>
     * <item>WEP EAP</item>
     * <item>WPA TKIP</item>
     * <item>WPA CCKM</item>
     * <item>WPA2 AES</item>
     * <item>WPA2 CCKM</item>
     * </string-array>
     */
    protected WifiConfig.Type convertType(String encryption) {

        if (encryption != null && encryption.contains("WEP")) {
            return WifiConfig.Type.WEP;
        }

        if (encryption != null && encryption.contains("WPA")) {
            return WifiConfig.Type.WPA;
        }

        //default
        return WifiConfig.Type.WPA;
    }
}
