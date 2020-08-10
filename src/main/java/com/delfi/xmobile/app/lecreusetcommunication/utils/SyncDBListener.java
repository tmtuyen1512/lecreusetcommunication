package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.view.View;

public interface SyncDBListener {
    void onStart(View view);
    void onError(View view, String message);
    void onDone(View view, boolean existsError);

    void initView(View view, int totalRecords);
    void initViewDone(View view);

    void onStartDetail(View view);
    void onProgressDetail(View view, String message, int percent);
    void onDoneDetail(View view);
    void onErrorDetail(View view, String message);
    void onMessageDetail(View view, String message);

    void onHideTask(View view);
}
