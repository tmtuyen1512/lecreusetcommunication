package com.delfi.xmobile.app.lecreusetcommunication.ftpcom;

import android.content.Context;
import android.util.Log;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.ftpcom.sftp.SFTPProgress;
import com.delfi.xmobile.app.lecreusetcommunication.ftpcom.sftp.SUserInfo;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.CommLogHandle;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.LogType;
import com.delfi.xmobile.app.lecreusetcommunication.utils.NetworkUtil;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.xcore.common.MediaScannerHelper;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.communication.ftpcom.FileOperationsHelper;
import com.delfi.xmobile.lib.xcore.communication.ftpcom.ServerFileInfo;
import com.delfi.xmobile.lib.xcore.communication.ftpcom.StringHelper;
import com.delfi.xmobile.lib.xcore.communication.handler.EnumMessageType;
import com.delfi.xmobile.lib.xcore.communication.handler.TransferEventArgs;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class SFTPComm extends BaseCommunication {

    private Session mSession;
    private ChannelSftp channelSftp;

    public SFTPComm(Context activity, FTPSetting config, FTPCommListener transferListener) {
        super(activity, config, transferListener);
    }

    @Override
    public void startCommunication(TypeComm typeComm) {
        this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.PROCESSING, "", activity.getResources().getString(R.string.starting_communication), 0));
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Start " + typeComm.name() + " " + config.CommunicationLabel, null);
        try {
            if (!NetworkUtil.isNetworkConnected(activity)) {
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.error_no_internet_connection), 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "No internet connection", null);
                return;
            } else {
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Connect to server " + config.ServerIP + ", Port " + config.ServerPort, null);
                if (connect()){

                }
                else {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Could not connect to server: " + config.ServerIP, null);
                    this.existsError = true;
                    this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.could_not_connect_to_server), 0));
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.existsError = true;
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), e.getMessage(), e);
            if (e instanceof org.apache.commons.net.ftp.FTPConnectionClosedException)
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.could_not_connect_to_server), 0));

            else
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", e.getMessage(), 0));
            return;
        }

        Log.i("FTPComm|INFO", "Starting SYNC");


        if (typeComm == TypeComm.SEND) {
            try {
                sendFilesToServer();
            } catch (Exception e) {
                e.printStackTrace();
                this.existsError = true;
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while sending files to server.", e);
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.an_error_occurred_while_sending_files_to_server), 0));
            }
        }


        if (typeComm == TypeComm.RECEIVE) {
            try {
                receiveFilesFromServer();
            } catch (Exception e) {
                e.printStackTrace();
                this.existsError = true;
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while receiving files to server.", e);
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.an_error_occurred_while_receiving_files_to_server), 0));
            }
        }
    }

    private boolean connect() {
        if (mSession == null || !mSession.isConnected()) {

            JSch jsch = new JSch();
            mSession = null;
            try {
                mSession = jsch.getSession(config.ServerUser, config.ServerIP, Integer.parseInt(config.ServerPort));

                SUserInfo userInfo = new SUserInfo(config.ServerIP, Integer.parseInt(config.ServerPort), config.ServerUser, config.ServerPassword);
                mSession.setUserInfo(userInfo);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
                mSession.setConfig("PreferredAuthentications", "password");

                mSession.connect(120000);

                if (mSession.isConnected()) {
                    Channel channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect(120000);
                    this.channelSftp = (ChannelSftp) channel;
                    return true;
                } else {

                    return false;
                }
            } catch (Exception ex) {
                Log.e("SFTP", "Exception:" + ex.getMessage());
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while connecting to server.", ex);
                return false;
            }
        }
        else if(mSession.isConnected())
            return true;
        return false;
    }


    private void sendFilesToServer() {
        this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.START, "", activity.getResources().getString(R.string.starting_to_send_file_to_server), 0));
        boolean val = createDirectoryIfNotExists(channelSftp, "", config.ServerConfigDirectory);
        if (!val) {
            this.existsError = true;
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote upload path " + config.ServerConfigDirectory, null);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", config.ServerConfigDirectory + " " + activity.getResources().getString(R.string.directory_not_found), 0));
            for (FTPSetting.FilesTo f : config.FilesTo) {
                //f.message = "Failed to set path " + config.ServerConfigDirectory;
                if (config.IsBackupServer){
                    f.message = String.format("Could not locate %s", config.ServerConfigDirectory);
                } else {
                    f.message = String.format("Could not locate %s trying backup..", config.ServerConfigDirectory);
                }
            }
            return;
        }
        try {
            channelSftp.cd(config.ServerConfigDirectory);
        } catch (Exception e) {
            this.existsError = true;
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote upload path " + config.ServerConfigDirectory, null);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", config.ServerConfigDirectory + " " + activity.getResources().getString(R.string.directory_not_found), 0));
            for (FTPSetting.FilesTo f : config.FilesTo) {
                //f.message = "Failed to set path " + config.ServerConfigDirectory;
                if (config.IsBackupServer){
                    f.message = String.format("Could not locate %s", config.ServerConfigDirectory);
                } else {
                    f.message = String.format("Could not locate %s trying backup..", config.ServerConfigDirectory);
                }
            }
            return;
        }

        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Get files from " + Constant.EXPORT_PATH + "/" + config.DestinationDirectory + ". Files found: " + config.FilesTo.size(), null);

        int filesent = 0;

        for (FTPSetting.FilesTo f : config.FilesTo) {
            this.transferListener.onStartDetail(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.PROCESSING, f.Name, "100%", 0));
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Begin to upload: " + f.Name, null);

            String path = Constant.EXPORT_PATH + "/" + config.DestinationDirectory + "/" + f.Name;
            try {
                File file = new File(path);
                if (!file.exists()) {
                    this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.file_not_found), 0));
                    f.message = activity.getResources().getString(R.string.file_not_found);
                    return;
                }

                SFTPProgress spm = new SFTPProgress(f.fView, transferListener);
                channelSftp.put(path, f.Name, spm, ChannelSftp.OVERWRITE);
                f.isTransfer = true;
                filesent += 1;

                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), file.getName() + " is sent. Filesize: " + StringHelper.calculateSize(file.length()), null);
                if (!file.delete()) {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Unable to delete file " + file.getName(), new Exception());
                    this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.unable_to_delete_file), 0));
                } else {
                    new MediaScannerHelper(this.activity, file);
                    this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.DONE, file.getName(), activity.getResources().getString(R.string.success), 0));
                }

            }
            catch (Exception e){
                existsError = true;
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " isn't sent", null);
                this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_success), 0));
            }

        }

        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Total files sent: " + filesent + "/" + config.FilesTo.size(), null);
    }

    private void receiveFilesFromServer() {
        this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.START, "", activity.getResources().getString(R.string.starting_to_receive_file_from_server), 0));
        if (!createDirectoryIfNotExists(channelSftp, "", config.ServerConfigDirectory)) {
            this.existsError = true;
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.failed_to_set_remote_download_path), 0));
            return;
        }

        String path = config.ServerConfigDirectory;
        Vector<ChannelSftp.LsEntry> mRemoteFiles = getListContent(channelSftp, path);
        if (mRemoteFiles == null || mRemoteFiles.size() == 0) {
            this.existsError = true;
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.no_files_to_receive_from_server), 0));
            return;
        } else {
            Vector<ChannelSftp.LsEntry> listFiles = new Vector<>();
            for (FTPSetting.FilesTo f : config.FilesTo) {
                for (ChannelSftp.LsEntry e : mRemoteFiles) {
                    if (!e.getAttrs().isDir() && f.Name.toLowerCase().equals(e.getFilename().toLowerCase()))
                        listFiles.add(e);
                }
            }
            if (listFiles.size() == 0) {
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.no_files_to_receive_from_server), 0));
                return;
            }

            int fileReceived = 0;

            for (FTPSetting.FilesTo f : config.FilesTo) {
                this.transferListener.onStartDetail(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, f.Name, "100%", 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Begin to download: " + f.Name, null);

                boolean existsFile = false;

                for (ChannelSftp.LsEntry e : listFiles) {
                    if(f.Name.toLowerCase().equals(e.getFilename().toLowerCase())) {
                        existsFile = true;
                        if(FileOperationsHelper.getInstance(activity).isNewFileFoundSFTP(e) || e.getFilename().contains(".so")  || (!isExistFile(rootPath + "/" + e.getFilename()) && (e.getFilename().equals("ApplicationConfiguration.xml") || e.getFilename().equals("module_list.json") || e.getFilename().equals("Pickconfig.xml")))) {
                            Log.v("receiveFilesFromServer", "File " + e.getFilename() + "; " + e.getAttrs().toString());
                            SFTPProgress spm = new SFTPProgress(f.fView, transferListener);

                            try {
                                String srcPath = path + "/" + e.getFilename();

                                String out = rootPath + "/" + e.getFilename();
                                if (e.getFilename().equals(Constant.APPLICATION_CONFIGURATION_XML.replace("/", "")))
                                    out = rootPath + "/Tmp/" + e.getFilename();

                                channelSftp.get(srcPath, out, spm, ChannelSftp.OVERWRITE);
                                fileReceived += 1;
                                f.isTransfer = true;
                                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " is received. Filesize: " + StringHelper.calculateSize(e.getAttrs().getSize()), null);
                                SharedManager.getInstance(activity).putString(f.Name, rootPath);

                                this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.received), 0));

                                try {
                                    ServerFileInfo serverFileInfo = new ServerFileInfo(e.getFilename(), e.getAttrs().getSize(), 0, new Date((long) e.getAttrs().getATime() * 1000L));
                                    FileOperationsHelper.getInstance(activity).handleWriteReceivedFile(serverFileInfo);
                                } catch (Exception ex) {
                                    //Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, ex.getMessage(), ex));
                                    ex.printStackTrace();
                                }
                                try {
                                    //Set last modified date as FTP last modified time for downloaded file.
                                    File localFile = new File(out);
                                    localFile.setLastModified((new Date((long) e.getAttrs().getATime() * 1000L)).getTime());
                                } catch (Exception ex) {
                                    //Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, ex.getMessage(), ex));
                                    ex.printStackTrace();
                                }
                                SharedManager.getInstance(activity).putBoolean(f.Name + "_SAMEFILE", false);

                            } catch (Exception je) {
                                je.printStackTrace();
                                existsError = true;
                                this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_receive), 0));
                            }
                        }
                        else {
                            f.isTransfer = true;
                            f.isSameFile = true;
                            this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, f.Name, activity.getResources().getString(R.string.skip), 0));
                            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Skip file " + f.Name, null);

                            SharedManager.getInstance(activity).putBoolean(f.Name + "_SAMEFILE", true);
                        }
                    }
                }

                if (!existsFile) {
                    File file = new File(rootPath + "/" + f);
                    if (file.exists())
                        file.delete();
                    existsError = true;
                    this.transferListener.onStartDetail(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, f.Name, "100%", 0));
                    this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.no_file_to_receive), 0));
                    CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " " + activity.getResources().getString(R.string.file_not_found), null);
                }
            }
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), activity.getResources().getString(R.string.total_files_received) + ": " + fileReceived + "/" + config.FilesTo.size(), null);
        }
    }

    @Override
    public boolean isExistsError() {
        return this.existsError;
    }

    @Override
    public void disconnect() {

        try {
            if (mSession != null) {
                mSession.disconnect();
            }
            if(channelSftp != null)
                channelSftp.disconnect();
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerIP + " disconnected", null);
            Log.i("FTPCommm", "Disconnected");
        } catch (Exception ex) {
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), config.ServerIP + " disconnect failed", ex);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.DISCONNECT, EnumMessageType.FAIL, "", ex.getMessage(), 0));
            ex.printStackTrace();
        }
        this.transferListener.onDoneCommunication(config._View, existsError);
    }

    @Override
    public String testConnection() {
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Test connection to server " + config.ServerIP + ", Port " + config.ServerPort, null);
        if(connect()){
            if (mSession != null) {
                mSession.disconnect();
            }
            if(channelSftp != null)
                channelSftp.disconnect();
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerIP + " disconnected", null);
            return null;
        }
        else {
            return activity.getResources().getString(R.string.could_not_connect_to_server) + " " + config.ServerIP + ": " + config.ServerPort;
        }
    }

    private boolean createDirectoryIfNotExists(ChannelSftp channelSftp, String root, String folderPath) {
        if (root.indexOf("/") != 0)
            root = "/" + root;
        try {
            Vector<ChannelSftp.LsEntry> v = getListContent(channelSftp, root);
            if (v == null)
                return false;
            boolean exists;
            if (folderPath.length() > 0) {
                String[] rs = folderPath.replaceFirst("/", "").split("/");
                for (int i = 0; i < rs.length; i++) {
                    exists = false;
                    for (ChannelSftp.LsEntry entry : v)
                        if (entry.getFilename().toLowerCase().equals(rs[i].toLowerCase()) && entry.getAttrs().isDir())
                            exists = true;
                    if (root.equals("/"))
                        root = root + rs[i];
                    else
                        root = root + "/" + rs[i];
                    if (!exists) {
                        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "'" + root + "' folder not found", null);
                        try {
                            Log.e("root", root);
                            channelSftp.mkdir(root);
                            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "'" + root + "' folder was created", null);
                        } catch (Exception e) {
                            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Error while mkdir " + "'" + root + "' folder.\n", null);
                            return false;
                        }
                    }
                    v = getListContent(channelSftp, root);
                    if (v == null)
                        return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Error while creating " + folderPath + " path\n" + e.getMessage(), e);
            return false;
        }
        return true;
    }

    private Vector<ChannelSftp.LsEntry> getListContent(ChannelSftp channelSftp, String path) {
        try {
            Vector<ChannelSftp.LsEntry> v = channelSftp.ls(path);
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Get list file dir " + path + ", " + (v != null ? v.size() + "" : "Null"), null);
            return v;
        } catch (Exception e) {
            e.printStackTrace();
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Error get list file in " + "'" + path + "' folder.\n", e);
        }
        return null;
    }
}
