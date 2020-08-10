package com.delfi.xmobile.app.lecreusetcommunication.ftpcom;

import android.content.Context;

import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;

import java.io.File;

public abstract class BaseCommunication {

    protected Context activity;
    protected FTPSetting config;
    protected FTPCommListener transferListener;
    protected String rootPath;
    protected boolean existsError;

    public BaseCommunication(Context activity, FTPSetting config, FTPCommListener transferListener){
        this.activity = activity;
        this.config = config;
        this.transferListener = transferListener;
        this.existsError = false;
        this.rootPath = Constant.getExternal() + "/" + config.DestinationDirectory;
    }

    public abstract void startCommunication(TypeComm typeComm);

    public abstract boolean isExistsError();

    public abstract void disconnect();

    public abstract String testConnection();

    protected String getPrefixLog() {
        return config.MainTask + " [" + config.CommunicationLabel + "]";
    }

    protected boolean isExistFile(String filePath){
        try {
            File f = new File(filePath);
            return f.exists();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
