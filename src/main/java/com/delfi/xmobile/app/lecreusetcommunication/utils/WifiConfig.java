package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;

/**
 * Created by USER on 06/12/2019.
 */
public class WifiConfig {

    private Context context;

    public WifiConfig(Context ctx) {
        this.context = ctx;
    }

    public enum Type {
        WEP, WPA
    }
}
