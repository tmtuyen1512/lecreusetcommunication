package com.delfi.xmobile.app.lecreusetcommunication.presenter.applyethernet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applynetwork.BaseNetworkPresenter;
import com.delfi.xmobile.lib.lecreusetbase.utils.NetworkUtil;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Created by USER on 11/14/2019.
 */
public class EthernetPresenter extends BaseNetworkPresenter {

    public EthernetPresenter(@NonNull Activity context) {
        super(context);
    }

    @Override
    public void applyNetwork(@NonNull Network network) {
        int count = 0;
        do {
            try {
                count++;
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[Ethernet] - count " + count, null));
                tryToConnect(network, count);
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!NetworkUtil.checkConnectedToServer() && count < MAX_RETRY);
    }


    private void tryToConnect(final Network network, final int count) {

        try {

            //enable ethernet if it exist
            if (EthernetUtils.doesEthExist()) {
                setEthEnabled(true);
            }

            Process p;

            //turn on if it off
            if (!EthernetUtils.isEthOn()) {
                p = Runtime.getRuntime().exec("ifconfig eth0 up");
                p.waitFor();
                LogOutput(p);
            }

            //connect to network
            if (network.DHCP == null || network.DHCP) {
                p = Runtime.getRuntime().exec("ifconfig eth0 dhcp start");
                p.waitFor();
                LogOutput(p);
            } else {
                p = Runtime.getRuntime().exec("ifconfig eth0 " + network.StaticTerminalIp + " netmask " + network.StaticTerminalSubnet + " gw " + network.StaticTerminalGateway);
                p.waitFor();
                LogOutput(p);
            }

        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[Ethernet] Connect Exception:" + e.getMessage(), e));
        } catch (Error e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[Ethernet] Connect Error:" + e.getMessage(), e));
        }
    }

    private void LogOutput(Process p) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            Log.i("TAG", line + "\n");
        }
    }

    @SuppressLint("WrongConstant")
    private void setEthEnabled(boolean enabled) {
        try {
            Object emInstance = context.getSystemService("ethernet");

            Class<?> emClass = Class.forName("android.net.ethernet.EthernetManager");
            Method methodSetEthEnabled = emClass.getMethod("setEthEnabled", Boolean.TYPE);
            methodSetEthEnabled.setAccessible(true);
            methodSetEthEnabled.invoke(emInstance, new Boolean(enabled));

        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[Ethernet] Enable Exception:" + e.getMessage(), e));
        } catch (Error e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "[Ethernet] Enable Error:" + e.getMessage(), e));
        }
    }
}
