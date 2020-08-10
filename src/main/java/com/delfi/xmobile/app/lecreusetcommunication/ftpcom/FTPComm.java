package com.delfi.xmobile.app.lecreusetcommunication.ftpcom;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.CommLogHandle;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
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

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FTPComm extends BaseCommunication{
    FTPClient ftpClient;
    private Object connectedThreadLock = new Object();
    private boolean connected = false;
    private boolean networkFailed = false;

    private List<FTPFile> listOfFtpFilesToReceive;

    /**
     * Constructor with port option
     */

    public FTPComm(Context activity, FTPSetting config, FTPCommListener transferListener) {
        super(activity, config, transferListener);

        this.ftpClient = new FTPClient();
        if(config.FTPType != null && config.FTPType.toLowerCase().equals("ftps"))
            this.ftpClient = new SSLSessionReuseFTPSClient(config.IsImplicit);

        this.ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                //Log.i("Sent", event.getMessage());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                Log.i("Received", event.getMessage());
            }
        });
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
                if (!connect(activity))
                    return;
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

        //sendLogFileToServer();

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

    /**
     * Tells you whether the FTPComm is connected or not.
     *
     * @return whether the FTPComm is connected or not.
     **/
    public boolean isConnected() {
        synchronized (connectedThreadLock) {
            return connected;
        }
    }

    public boolean connect(final Context activity) {
        if (this.activity == null)
            this.activity = activity;
        synchronized (connectedThreadLock) {
            try {
                if (!NetworkUtil.isNetworkConnected(activity)) {
                    networkFailed = true;
                }
                this.transferListener.onSetDetail(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.PROCESSING, "", activity.getResources().getString(R.string.connecting_to_server), 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Connect to server " + config.ServerIP + ", Port " + config.ServerPort, null);
                try {
                    if(!ftpClient.isConnected())
                        ftpClient.connect(config.ServerIP, Integer.parseInt(config.ServerPort));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Could not connect to server " + ex.getMessage(), ex);
                    this.existsError = true;
                    this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.could_not_connect_to_server), 0));
                    return false;
                }
                //Log.i("FTPComm|INFO", ftpClient.getReplyString());
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerIP + " connected", null);
                this.transferListener.onSetDetail(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.PROCESSING, "", activity.getResources().getString(R.string.waiting_for_authentication), 0));

                if(config.FTPType != null && config.FTPType.toLowerCase().equals("ftps")) {
                    ((SSLSessionReuseFTPSClient)ftpClient).execPBSZ(0);
                    ((SSLSessionReuseFTPSClient)ftpClient).execPROT("P");
                }
                ftpClient.enterLocalPassiveMode();

                try {
                    CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Login with " + config.ServerUser + "/******", null);
                    if (!ftpClient.login(config.ServerUser, config.ServerPassword)) {
                        this.existsError = true;
                        this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.username_or_password_incorrect), 0));
                        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Login failed with " + config.ServerUser + " and password *****", null);
                        return false;
                    }
                    CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Login success", null);
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                } catch (Exception e) {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), e.getMessage(), e);
                    this.existsError = true;
                    this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", e.getMessage(), 0));
                    return false;
                }
                //Log.i("FTPComm|INFO", ftpClient.getReplyString());
                connected = true;

                //this.transferListener.onDoneStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.DONE, "", "Connected to server", 0));
            } catch (Exception e) {
                Log.e("FTPComm|connect", e.toString());
                connected = false;
                e.printStackTrace();
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), e.getMessage(), e);
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", e.getMessage(), 0));
                return false;
            }
        }

        try {
            Thread.sleep(5);  //multi-threaded voodoo. Give the AsyncTask 5 ms to get started and get the lock.
        } catch (InterruptedException e) {
            // do nothing.
        }
        synchronized (connectedThreadLock) {
            if (!connected) {
                if (networkFailed) {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "No internet connection!", null);
                    this.existsError = true;
                    this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.error_no_internet_connection), 0));
                    return false;
                } else {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "It was not possible to connect to the server", null);
                    this.existsError = true;
                    this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.CONNECT, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.it_was_not_possible_to_connect_to_the_server), 0));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String testConnection() {
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Start test connection to " + config.ServerIP + ", Port " + config.ServerPort, null);
        try {
            if (!NetworkUtil.isNetworkConnected(activity)) {
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "No internet connection", null);
                return "No internet connection";
            }
            ftpClient.connect(config.ServerIP, Integer.parseInt(config.ServerPort));
            Log.i("FTPComm|INFO", ftpClient.getReplyString());
            ftpClient.enterLocalPassiveMode();

            boolean result = ftpClient.login(config.ServerUser, config.ServerPassword);
            if (!result) {
                String rs = ftpClient.getReplyString();
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Login fail: " + rs, null);
                return rs;
            }
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Test connection successfully", null);
            return null;
        } catch (Exception e) {
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), e.getMessage(), null);
            e.printStackTrace();
            return "Could not connect to server (" + e.getMessage() + ")";
        }
    }

    /**
     * Ftp upload file
     *
     * @param filename Given file name
     */
    private boolean uploadLogSync(String filename) {
        /*DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.UK);
        String current = dateFormatter.format(new Date());
        String RemotePath = serverSettings.logRemotePath + "/" + "Logger_" + current + ".txt";
        Log.i("uploadSync", "\nUploading: " + filename + "\nTo: " + RemotePath);
        try {
            
			InputStream is = new FileInputStream(filename);
			try {
                if(!ftpClient.storeFile(RemotePath, is)){
                    this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "An error occurred while sending file " + filename + " to the server\nReplyCode: " + ftpClient.getReply() + "\nReplyString: " + ftpClient.getReplyString(), 0));
                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "An error occurred while sending file \" + filename + \" to the server", new Exception("ReplyCode: " + getReplyCode() + "\nReplyString: " + getReplyString())));
                }

            }
            catch (Exception e){
                this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "An error occurred while sending file " + filename + " to the server", 0));
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "An error occurred while sending file \" + filename + \" to the server", e));
                return false;
            }							
		} catch (Exception e) {
            Log.e("uploadLogSync", e.toString());
			this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "An error occurred while open file " + filename + " on the local", 0));
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "Upload log file failed.", e));
            return false;
        }*/
        return true;
    }


    private boolean setRemotePath(String currentDir) throws IOException {
        String dirToList = currentDir;
        if (!currentDir.equals("") && !currentDir.contains("/")) {
            dirToList += "/" + currentDir;
        }
        return ftpClient.changeWorkingDirectory(dirToList);
    }

    /**
     * Sends all client logs to server
     */

    public void sendLogFileToServer() throws Exception {
        /*this.transferListener.onStartCommunication(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.START, "", "Begin sending log files.", 0));
        File logFile = new File(DeviceApplicationInformation.getLogDirectory(), "Logger.txt");
        if (!logFile.exists()) {
            this.transferListener.onDoneStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.DONE, "", "No log files to send to server", 0));
            return;
        }
        try {
            if (!setRemotePath(serverSettings.logRemotePath)) {
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "Failed to set delfi_logs remote path", new Exception(getReplyCode() + "\n" + getReplyString())));
                this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "Failed to set delfi_logs remote path\nReplyCode: " + getReplyCode() + "\nReplyString: " + getReplyString(), 0));
                return;
            }
        } catch (Exception ex) {
            Log.e("FTPComm|sync", "Failed to set delfi_logs remote path");
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "Failed to set delfi_logs remote path", ex));
            this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "Failed to set delfi_logs remote path", 0));
            ex.printStackTrace();
            syncing = false;
            return;
        }

        this.transferListener.onSetDetail(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.PROCESSING, "", "File Transfer started\nSending log file" + " ("
                + 1 + "/" + 1 + ")\n"
                + logFile.getName() + "\n" + "Filesize" + ": "
                + StringHelper.calculateSize(logFile.length()), (int) logFile.length()));

        int filesent = 0;
        try {
            if (uploadLogSync(logFile.getAbsolutePath())) {
                filesent++;
                this.transferListener.onSetProgress(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.PROCESSING, logFile.getName(), "", logFile.length()));
            }
        } catch (Exception e) {

            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "An error occurred while sending file" + ": \n" + logFile.getName() + "\n" + "To the server", e));
            this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "An error occurred while sending file" + ": \n" + logFile.getName() + "\n" + "To the server", 0));

            return;
        }
        try {
            if (config.deleteLogFileAfterTransfer) {
                logFile.delete();
            }
        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "Unable to delete log file " + logFile.getName(), e));
            this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "Unable to delete log file " + logFile.getName(), 0));
            e.printStackTrace();
        }
        if (filesent > 0) {
            this.transferListener.onDoneStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.DONE, "", "File Transfer Completed" + "\n(" + filesent + "/" + 1 + ") " + "File sent\n", 0));
        } else {
            this.transferListener.onErrorStep(new TransferEventArgs(EnumMessageType.SEND_LOG_FILES, EnumMessageType.FAIL, "", "File Transfer Completed" + "\n(" + filesent + "/" + 1 + ") " + "File sent\n", 0));

        }*/
    }

    public void sendFilesToServer() throws Exception {
        this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.START, "", activity.getResources().getString(R.string.starting_to_send_file_to_server), 0));

        /*ArrayList<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + config.DestinationDirectory));
        if (filesToUpload.size() < 1) {
            //this.transferListener.onDoneStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.DONE, "", "No files to send to server", 0));
            return;
        }*/

        try {
            if (!setRemotePath(config.ServerConfigDirectory)) {
                this.existsError = true;
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote upload path " + config.ServerConfigDirectory, new Exception(getReplyCode() + "\n" + getReplyString()));
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", config.ServerConfigDirectory + " " + activity.getResources().getString(R.string.directory_not_found), 0));
                for (FTPSetting.FilesTo f : config.FilesTo){
                    //f.message = "Failed to set path " + config.ServerConfigDirectory;
                    if (config.IsBackupServer){
                        f.message = String.format("Could not locate %s", config.ServerConfigDirectory);
                    } else {
                        f.message = String.format("Could not locate %s trying backup..", config.ServerConfigDirectory);
                    }
                }
                return;
            }
        } catch (Exception ex) {
            this.existsError = true;
            Log.e("FTPComm|sync", "Failed to set remote upload path");
            ex.printStackTrace();
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote upload path", ex);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.failed_to_set_remote_upload_path), 0));
            return;
        }
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Get files from " + Constant.EXPORT_PATH + "/" + config.DestinationDirectory + ". Files found: " + config.FilesTo.size(), null);

        int filesent = 0;

        for (FTPSetting.FilesTo f : config.FilesTo) {
            this.transferListener.onStartDetail(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.PROCESSING, f.Name, "100%", 0));
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Begin to upload: " + f.Name, null);

            String path = Constant.EXPORT_PATH + "/" + config.DestinationDirectory + "/" + f.Name;
            if (SendFile(path, f)) {
                f.isTransfer = true;
                filesent += 1;
            } else {
                existsError = true;
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " isn't sent", null);
            }
        }

        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Total files sent: " + filesent + "/" + config.FilesTo.size(), null);
    }

    private boolean SendFile(String path, final FTPSetting.FilesTo f) throws Exception {
        final File file = new File(path);
        if (!file.exists()) {
            this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.file_not_found), 0));
            f.message = activity.getResources().getString(R.string.file_not_found);
            return false;
        }


        //this.transferListener.onSetDetail(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.PROCESSING, "", "Sending file: " + file.getName(), 0));
        CopyStreamAdapter streamListener = new CopyStreamAdapter() {

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                //this method will be called everytime some bytes are transferred

                int percent = (int) (totalBytesTransferred * 100 / file.length());
                //Log.i("bytesTransferred", percent + "");
                transferListener.onSetProgress(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, file.getName(), String.format("%s / %s", StringHelper.calculateSize(totalBytesTransferred), StringHelper.calculateSize(file.length())), (int) ((float) totalBytesTransferred / (float) file.length() * 100)));
            }

        };
        ftpClient.setCopyStreamListener(streamListener);

        String filename = file.getAbsolutePath();
        String RemotePath = config.ServerConfigDirectory + "/" + filename.substring(filename.lastIndexOf("/"));

        try {
            InputStream is = new FileInputStream(filename);

            {
                try {
                    boolean failed = ftpClient.storeFile(RemotePath, is);
                    if (!failed) {
                        CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while sending file: " + file.getName() + ". " + getReplyCode() + ": " + getReplyString(), null);
                        this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_success), 0));
                        return false;
                    }
                    is.close();
                } catch (Exception e) {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while sending file " + file.getName() + ". " + getReplyCode() + ": " + getReplyString(), e);
                    e.printStackTrace();
                    if (e instanceof java.net.SocketException) {
                        this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_success), 0));
                    } else {
                        this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_success), 0));
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e("uploadSync", e.toString());
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Upload file failed.", e);
            this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.can_not_open_to_file), 0));
            return false;
        }
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), file.getName() + " is sent. Filesize: " + StringHelper.calculateSize(file.length()), null);

        try {
            if (true) {
                if (!file.delete()) {
                    CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Unable to delete file " + file.getName(), new Exception());
                    this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.unable_to_delete_file), 0));
                    //return false;
                } else {
                    new MediaScannerHelper(this.activity, file);
                }
            }

        } catch (Exception e) {
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Unable to delete file " + file.getName(), e);
            this.transferListener.onDoneStep(config._View, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.unable_to_delete_file), 0));
            e.printStackTrace();
            //return false;
        }
        this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.SEND_FILES, EnumMessageType.DONE, file.getName(), activity.getResources().getString(R.string.success), 0));

        return true;
    }

    /**
     * Downloads newest input files from server
     */
    public void receiveFilesFromServer() throws Exception {
        this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.START, "", activity.getResources().getString(R.string.starting_to_receive_file_from_server), 0));

        try {

            if (!setRemotePath(config.ServerConfigDirectory)) {
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote download path " + config.ServerConfigDirectory, new Exception(getReplyCode() + "\n" + getReplyString()));
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", config.ServerConfigDirectory + " " + activity.getResources().getString(R.string.directory_not_found), 0));
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
        } catch (Exception ex) {
            Log.e("FTPComm|sync", "Failed to set remote download path");
            ex.printStackTrace();
            this.existsError = true;
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "Failed to set remote download path", ex);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.failed_to_set_remote_download_path), 0));
            return;
        }
        try {
            listOfFtpFilesToReceive = getAllFilesFromServer();
            if (listOfFtpFilesToReceive == null || listOfFtpFilesToReceive.size() == 0) {
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.no_files_to_receive_from_server), 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerConfigDirectory + ": No file found", null);
                return;
            }
            int existsFile = 0;
            for (FTPSetting.FilesTo f : config.FilesTo) {
                for (FTPFile ftpFile : listOfFtpFilesToReceive) {
                    if (f.Name.toLowerCase().equals(ftpFile.getName().toLowerCase())) {
                        existsFile += 1;
                    }
                }
            }
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), activity.getResources().getString(R.string.get_files_from) + " " + config.ServerConfigDirectory + ". " + activity.getResources().getString(R.string.files_found) + " " + existsFile, null);

            if (existsFile == 0) {
                this.existsError = true;
                this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.no_files_to_receive_from_server), 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerConfigDirectory + ": " + activity.getResources().getString(R.string.no_files_to_receive_from_server), null);
                return;
            }
        } catch (Exception e) {
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "It was not possible to get the filelist from server: " + e.getMessage(), e);
            this.existsError = true;
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.it_was_not_possible_to_get_the_filelist_from_server), 0));
            return;
        }

        int fileReceived = 0;

        for (FTPSetting.FilesTo f : config.FilesTo) {
            this.transferListener.onStartDetail(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, f.Name, "100%", 0));
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), "Begin to download: " + f.Name, null);

            boolean existsFile = false;
            for (FTPFile ftpFile : listOfFtpFilesToReceive) {
                if (f.Name.toLowerCase().equals(ftpFile.getName().toLowerCase())) {
                    existsFile = true;
                    if(FileOperationsHelper.getInstance(this.activity).isNewFileFound(ftpFile) || ftpFile.getName().contains(".so") || (!isExistFile(rootPath + "/" + ftpFile.getName()) && (ftpFile.getName().equals("ApplicationConfiguration.xml") || ftpFile.getName().equals("module_list.json") || ftpFile.getName().equals("Pickconfig.xml")))) {

                        if (receiveFile(ftpFile, f.fView)) {
                            f.isTransfer = true;
                            fileReceived += 1;
                            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " is received. Filesize: " + StringHelper.calculateSize(ftpFile.getSize()), null);
                            SharedManager.getInstance(activity).putString(f.Name, rootPath);
                            SharedManager.getInstance(activity).putBoolean(f.Name + "_SAMEFILE", false);
                        } else {
                            existsError = true;
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
                this.transferListener.onDoneStep(f.fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.no_file_to_receive), 0));
                CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), f.Name + " " + activity.getResources().getString(R.string.file_not_found), null);
            }
        }
        CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), activity.getResources().getString(R.string.total_files_received) + ": " + fileReceived + "/" + config.FilesTo.size(), null);
    }

    private boolean receiveFile(FTPFile file, View fView) throws Exception {

        String filename = rootPath + "/" + file.getName();
        if (file.getName().equals(Constant.APPLICATION_CONFIGURATION_XML.replace("/", "")))
            filename = rootPath + "/Tmp/" + file.getName();
        String remotePath = config.ServerConfigDirectory + "/" + file.getName();
        Log.i("downloadSync", "Downloading: " + file.getName() + "\nTo: " + filename);
        File localFile = new File(filename);

        try {
            //this.transferListener.onSetDetail(config._View, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, "", "Receiving file: " + file.getName(), 0));

            FileUtil.getInstance().addFolder(localFile);
            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(localFile));
            InputStream inputStream;// = ftpClient.retrieveFileStream(remotePath);
            try {
                inputStream = ftpClient.retrieveFileStream(remotePath);
            } catch (Exception e) {
                this.existsError = true;
                localFile.delete();
                if (e instanceof SocketException) {
                    this.transferListener.onDoneStep(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_receive), 0));
                } else {
                    this.transferListener.onDoneStep(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_receive), 0));
                }
                CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while read InputStream file:\n" + file.getName() + "\nOn the server", e);
                return false;
            }
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            long downloaded = 0;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream2.write(bytesArray, 0, bytesRead);
                downloaded += bytesRead;

                this.transferListener.onSetProgress(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, file.getName(), String.format("%s / %s", StringHelper.calculateSize(downloaded), StringHelper.calculateSize(file.getSize())), (int) ((float) downloaded / (float) file.getSize() * 100)));
            }
            ftpClient.completePendingCommand();
            outputStream2.close();
            inputStream.close();


            try {
                localFile.setLastModified(file.getTimestamp().getTimeInMillis());
            } catch (Exception e) {

            }
        } catch (Exception e) {
            this.existsError = true;
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), "An error occurred while receiving file " + file.getName() + ", " + getReplyCode() + ": " + getReplyString(), e);
            this.transferListener.onDoneStep(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.FAIL, "", activity.getResources().getString(R.string.not_receive), 0));

            localFile.delete();
            return false;
        }

        try {
            ServerFileInfo f = new ServerFileInfo(file.getName(), file.getSize(), 0, file.getTimestamp().getTime());
            FileOperationsHelper.getInstance(this.activity).handleWriteReceivedFile(f);
        } catch (Exception e) {

        }

        this.transferListener.onDoneStep(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.DONE, "", activity.getResources().getString(R.string.received), 0));
        return true;
    }

    private List<FTPFile> getAllFilesFromServer() throws Exception {
        try {
            ArrayList<FTPFile> globalFiles = new ArrayList<>();
            Collections.addAll(globalFiles, ftpClient.listFiles());

            ArrayList<FTPFile> tmpGlobalFiles = new ArrayList<>();
            if (globalFiles.size() == 0)
                return new ArrayList<>();

            for (FTPFile globalFile : globalFiles) {
                if (globalFile.isFile())
                    tmpGlobalFiles.add(globalFile);
            }

            List<FTPFile> returnLits = new ArrayList<>();
            returnLits.addAll(tmpGlobalFiles);
            return returnLits;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public void disconnect() {
        try {
            //this.transferListener.onStartCommunication(config._View, new TransferEventArgs(EnumMessageType.DISCONNECT, EnumMessageType.START, "", "Disconnecting", 0));
            ftpClient.disconnect();
            connected = false;
            CommLogHandle.getInstance().logMessage(LogType.I, getPrefixLog(), config.ServerIP + " disconnected", null);
            Log.i("FTPCommm", "Disconnected");
        } catch (Exception ex) {
            CommLogHandle.getInstance().logMessage(LogType.E, getPrefixLog(), config.ServerIP + " disconnect failed", ex);
            this.transferListener.onErrorStep(config._View, new TransferEventArgs(EnumMessageType.DISCONNECT, EnumMessageType.FAIL, "", ex.getMessage(), 0));
            ex.printStackTrace();
        }
        this.transferListener.onDoneCommunication(config._View, existsError);
    }

    private int getReplyCode() {
        return ftpClient.getReplyCode();
    }

    private String getReplyString() {
        return ftpClient.getReplyString();
    }

    @Override
    public boolean isExistsError() {
        return existsError;
    }
}
