package com.delfi.xmobile.app.lecreusetcommunication.model;

import java.io.Serializable;

/**
 * Created by USER on 05/10/2019.
 */
public class BarcodeConfig implements Serializable {
    public Config Config;

    public BarcodeConfig() {
        this.Config = new Config();
    }

    public static class Config implements Serializable {
        public Generelt Generelt;
        public Network Network;
    }
}

