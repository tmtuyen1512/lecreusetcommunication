package com.delfi.xmobile.app.lecreusetcommunication.ftpcom;

import android.content.Context;

import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;

public class DoCommunication {

    private BaseCommunication baseCommunication;

    public DoCommunication(Context activity, FTPSetting config, FTPCommListener transferListener){
        if(config.FTPType != null && config.FTPType.length() > 0 && config.FTPType.toLowerCase().equals("sftp")){
            baseCommunication = new SFTPComm(activity, config, transferListener);
        }
        else
            baseCommunication = new FTPComm(activity, config, transferListener);
    }
    public void startCommunication(TypeComm typeComm){
        baseCommunication.startCommunication(typeComm);
    }

    public boolean isExistsError(){
        return baseCommunication.isExistsError();
    }

    public void disconnect(){
        baseCommunication.disconnect();
    }

    public String testConnection() {
        return baseCommunication.testConnection();
    }
}
