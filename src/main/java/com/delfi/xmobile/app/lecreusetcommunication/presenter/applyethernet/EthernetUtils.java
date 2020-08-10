package com.delfi.xmobile.app.lecreusetcommunication.presenter.applyethernet;

import android.util.Log;

import com.delfi.xmobile.app.lecreusetcommunication.model.Network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by USER on 11/18/2019.
 */
public class EthernetUtils {

    public static boolean doesEthExist() {
        List<String> list = getListOfNetworkInterfaces();
        return list.contains("eth0");
    }

    public static List<String> getListOfNetworkInterfaces() {

        List<String> list = new ArrayList<>();

        Enumeration<NetworkInterface> nets;

        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return list;
        }

        for (NetworkInterface netint : Collections.list(nets)) {
            list.add(netint.getName());
        }

        return list;

    }

    public static boolean isEthOn() {
        try {
            String line;
            boolean r = false;

            Process p = Runtime.getRuntime().exec("netcfg");

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {

                if (line.contains("eth0")) {
                    if (line.contains("UP")) {
                        r = true;
                    } else {
                        r = false;
                    }
                }
            }
            input.close();

            Log.e("TAG", "isEthOn: " + r);
            return r;

        } catch (IOException e) {
            Log.e("TAG", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void turnEthOn() {
        try {
            if (!isEthOn()) {
                Runtime.getRuntime().exec("ifconfig eth0 up");
            }

        } catch (IOException e) {
            Log.e("TAG", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void turnEthOff() {
        try {
            if (isEthOn()) {
                Runtime.getRuntime().exec("ifconfig eth0 down");
            }

        } catch (IOException e) {
            Log.e("TAG", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean connectEthernetNetwork(boolean isDHCP, Network network) {

        try {
            if (isDHCP) {
                Runtime.getRuntime().exec("ifconfig eth0 dhcp start");
            } else {
                Runtime.getRuntime().exec("ifconfig eth0 " + network.StaticTerminalIp + " netmask " + network.StaticTerminalSubnet + " gw " + network.StaticTerminalGateway);
            }

        } catch (IOException e) {
            Log.e("TAG", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
