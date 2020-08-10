package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.delfi.xmobile.lib.xcore.communication.handler.TransferEventArgs;
import com.delfi.xmobile.lib.xcore.communication.handler.TransferListener;

/**
 * Created by USER on 06/15/2020.
 */
public class SimpleTransferListener implements TransferListener {

    private Context context;

    public SimpleTransferListener(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void onStartStep(TransferEventArgs args) {

    }

    @Override
    public void onSetDetail(TransferEventArgs args) {

    }

    @Override
    public void onErrorStep(TransferEventArgs args) {

    }

    @Override
    public void onDoneStep(TransferEventArgs args) {

    }

    @Override
    public void onMessage(TransferEventArgs args) {

    }

    @Override
    public void onCommunicationDone() {

    }

    @Override
    public void onDownloadApkSuccess(String path) {

    }

    @Override
    public void onSetProgress(TransferEventArgs args) {

    }
}
