package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.text.TextUtils;

/**
 * Created by USER on 11/07/2019.
 */
public class WifiSubnetHelper {

    public static int getPrefixLength(String subnet) {
        try {
            if (!TextUtils.isEmpty(subnet)) {
                String[] parts = subnet.split("\\.");

                int count = 0;
                for (String part : parts) {
                    int decimal = Integer.parseInt(part);
                    count += countSetBits(decimal);
                }
                return count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 24;
    }

    public static int countSetBits(int n) {
        int count = 0;
        while (n > 0) {
            count += n & 1;
            n >>= 1;
        }
        return count;
    }
}
