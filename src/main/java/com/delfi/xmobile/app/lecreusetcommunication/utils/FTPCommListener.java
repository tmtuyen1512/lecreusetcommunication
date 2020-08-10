package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;
import android.view.View;

import com.delfi.xmobile.lib.xcore.communication.handler.TransferEventArgs;

/**
 * Interface that must be implemented by any object wishing to Listen
 * to the Jsch connection status (either connected or disconnected).
 * Created by Jon Hough 7/31/14.
 */
public interface FTPCommListener {

    Context getContext();

    void onStartCommunication(View view, TransferEventArgs args);
    void onStartDetail(View fView, TransferEventArgs transferEventArgs);
    void onSetProgress(View view, TransferEventArgs args);
    void onErrorStep(View view, TransferEventArgs args);
    void onDoneStep(View view, TransferEventArgs args);

    void onSetDetail(View view, TransferEventArgs args);

    void onDoneCommunication(View view, boolean existsError);

}
