package com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiConfigurationCreator;
import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiSubnetHelper;
import com.delfi.xmobile.lib.lecreusetbase.utils.NetworkUtil;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.preethzcodez.wifiadvancedlib.IPSettings;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 11/14/2019.
 */
public class WifiStaticPresenter extends BaseWifiPresenter {

    public WifiStaticPresenter(@NonNull Activity context) {
        super(context);
    }

    @Override
    public void applyNetwork(@NonNull Network network) {
        try {
            WifiConfiguration wifiConfig = null;

            switch (convertType(network.Encryption)) {
                case WEP:
                    wifiConfig = WifiConfigurationCreator.generateWEPNetworkConfiguration(
                            network.SSID, network.WifiPassword);
                    break;
                case WPA:
                    wifiConfig = WifiConfigurationCreator.generateWPA2NetworkConfiguration(
                            network.SSID, network.WifiPassword);
                    break;
            }

            String staticTerminalIp = network.StaticTerminalIp;
            String staticTerminalGateway = network.StaticTerminalGateway;
            int prefixLength = WifiSubnetHelper.getPrefixLength(network.StaticTerminalSubnet);

            InetAddress[] dns_servers = getDnsAddress(network);

            final WifiConfiguration newWifiConfig = IPSettings.setStaticIpConfiguration(
                    wifiConfig,
                    InetAddress.getByName(staticTerminalIp),
                    prefixLength,
                    InetAddress.getByName(staticTerminalGateway),
                    dns_servers
            );

            int count = 0;
            do {
                try {
                    count++;
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, "[USE STATIC IP] - count: " + count, null));
                    tryToConnect(network, newWifiConfig, count);
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!NetworkUtil.checkConnectedToServer() && count < MAX_RETRY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private InetAddress[] getDnsAddress(@NonNull Network network) throws UnknownHostException {
        List<String> dns = new ArrayList<>();
        if (!TextUtils.isEmpty(network.StaticTerminalDns)) {
            dns.add(network.StaticTerminalDns);
        }
        if (!TextUtils.isEmpty(network.StaticTerminalDns2)) {
            dns.add(network.StaticTerminalDns2);
        }
        if (dns.isEmpty()) {
            dns.add("8.8.8.8");
            dns.add("8.8.4.4");
        }

        InetAddress[] dns_servers;
        if (dns.size() == 2) {
            dns_servers = new InetAddress[]{
                    InetAddress.getByName(dns.get(0)),
                    InetAddress.getByName(dns.get(1)),
            };
        } else {
            dns_servers = new InetAddress[]{
                    InetAddress.getByName(dns.get(0))
            };
        }
        return dns_servers;
    }
}
