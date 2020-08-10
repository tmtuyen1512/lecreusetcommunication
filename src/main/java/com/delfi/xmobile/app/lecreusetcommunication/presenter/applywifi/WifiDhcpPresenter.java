package com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.lib.lecreusetbase.utils.NetworkUtil;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

/**
 * Created by USER on 11/14/2019.
 */
public class WifiDhcpPresenter extends BaseWifiPresenter {

    public WifiDhcpPresenter(@NonNull Activity context) {
        super(context);
    }

    @Override
    public void applyNetwork(@NonNull Network network) {
        int count = 0;
        do {
            try {
                count++;
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE DHCP] - count: " + count, null));
                tryToConnect(network, null, count);
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!NetworkUtil.checkConnectedToServer() && count < MAX_RETRY);
    }
}
