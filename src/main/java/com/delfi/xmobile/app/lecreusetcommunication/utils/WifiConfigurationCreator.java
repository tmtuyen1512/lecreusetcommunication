package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.net.wifi.WifiConfiguration;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by USER on 10/31/2019.
 */
public class WifiConfigurationCreator {
    public static final String QUOTE = "\"";

    @NonNull
    public static final WifiConfiguration generateOpenNetworkConfiguration(@NonNull String ssid) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = convertToQuotedString(ssid);
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // Allowed auth algorithms
        wifiConfiguration.allowedAuthAlgorithms.clear();

        // Allowed protocols
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        // Allowed Group Ciphers
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        // Allowed Pairwise Ciphers
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        return wifiConfiguration;
    }

    @NonNull
    public static final WifiConfiguration generateWEPNetworkConfiguration(@NonNull String ssid, @NonNull String password) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = convertToQuotedString(ssid);
        wifiConfiguration.wepKeys[0] = QUOTE + password + QUOTE;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // Deprecated due to security issues with WPA networks, should use WPA2 instead
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA); // WPA network protocol

        /*
         * Allowed Auth Algorithms
         */

        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        // Deprecated due to shared key authentication requiring static WEP keys
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

        /*
         * Allowed Protocols
         */
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        /*
         * Allowed Group Ciphers
         */

        // Deprecated because of WEP
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        /*
         * Allowed Pairwise Ciphers
         */

        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        // Deprecated WPA algorithm, RSN and WPA2 should be used instead
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

        return wifiConfiguration;
    }

    @NonNull
    public static final WifiConfiguration generateWPA2NetworkConfiguration(@NonNull String ssid, @NonNull String password) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = convertToQuotedString(ssid);
        wifiConfiguration.preSharedKey = QUOTE + password + QUOTE;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfiguration.status = WifiConfiguration.Status.ENABLED;

        // Allowed protocols
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        // Allowed Group Ciphers
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);

        // Allowed Pairwise Ciphers
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        return wifiConfiguration;
    }

    @NonNull
    public static String convertToQuotedString(@NonNull String string) {
        if (TextUtils.isEmpty(string))
            return "";

        final int lastPos = string.length() - 1;
        if (lastPos < 0 || (string.charAt(0) == '"' && string.charAt(lastPos) == '"'))
            return string;

        return "\"" + string + "\"";
    }
}
