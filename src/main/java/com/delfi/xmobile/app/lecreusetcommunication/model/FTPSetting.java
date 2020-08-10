package com.delfi.xmobile.app.lecreusetcommunication.model;

import android.view.View;

import com.delfi.xmobile.app.lecreusetcommunication.utils.DoInsertBase;

import java.io.Serializable;
import java.util.List;

public class FTPSetting implements Serializable {
    public String ServerIP;
    public String ServerPort;
    public String ServerUser;
    public String ServerPassword;
    public String FTPType;
    public boolean IsImplicit;
    public String ServerConfigDirectory;
    public String DestinationDirectory;
    public List<FilesTo> FilesTo;
    public boolean IsPolicyConfig;
    public String CommunicationLabel;
    public String MainTask;
    public boolean IgnoreSameFileWhenDownload;
    public boolean IsBackupServer;

    public FTPSetting SettingsBackup;

    public View _View;

    public boolean isFTP;

    public List<DoInsertBase> doInsertBases;

    public static class FilesTo implements Serializable{
        public String Label;
        public String Name;
        public String DisplayName;
        public View fView;
        public boolean isTransfer;
        public String message;
        public boolean isSameFile;

        public FilesTo(String fileName){
            this.Name = fileName;
            this.DisplayName = fileName;
        }

        public FilesTo(String fileName, String displayName){
            this.Name = fileName;
            this.DisplayName = displayName;
        }

        public FilesTo(FilesTo copy) {
            this.Label = copy.Label;
            this.Name = copy.Name;
            this.DisplayName = copy.DisplayName;
            this.fView = copy.fView;
            this.isTransfer = copy.isTransfer;
            this.message = copy.message;
        }

        @Override
        public boolean equals(Object obj) {
            boolean a = obj instanceof FilesTo && this.Name.equals(((FTPSetting.FilesTo) obj).Name);
            return a;
        }
    }

}
