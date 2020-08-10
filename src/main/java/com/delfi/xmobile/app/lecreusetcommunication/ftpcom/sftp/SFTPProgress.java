package com.delfi.xmobile.app.lecreusetcommunication.ftpcom.sftp;

import android.view.View;

import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;
import com.delfi.xmobile.lib.xcore.communication.ftpcom.StringHelper;
import com.delfi.xmobile.lib.xcore.communication.handler.EnumMessageType;
import com.delfi.xmobile.lib.xcore.communication.handler.TransferEventArgs;
import com.jcraft.jsch.SftpProgressMonitor;

public class SFTPProgress implements SftpProgressMonitor {
    private final FTPCommListener transferListener;
    private final View fView;
    long totalSize = 0;
    private String fileName;
    private long max = 0;
    private long count = 0;
    private long percent = 0;

    // If you need send something to the constructor, change this method
    public SFTPProgress(View fView, FTPCommListener listener) {
        this.fView = fView;
        this.transferListener = listener;
        this.totalSize = 0;
    }


    public void init(int op, String src, String dest, long max) {
        this.max = max;
        this.fileName = dest;
        this.count = 0;
        this.percent = 0;
        totalSize += max;
    }

    public boolean count(long bytes) {
        this.count += bytes;
        long percentNow = this.count * 100 / max;
        if (percentNow > this.percent) {
            this.percent = percentNow;

            this.transferListener.onSetProgress(fView, new TransferEventArgs(EnumMessageType.RECEIVE, EnumMessageType.PROCESSING, "", String.format("%s / %s", StringHelper.calculateSize(this.count), StringHelper.calculateSize(max)), (int) ((float) this.count / (float) max * 100)));
        }

        return (true);
    }

    public void end() {
    }
}
