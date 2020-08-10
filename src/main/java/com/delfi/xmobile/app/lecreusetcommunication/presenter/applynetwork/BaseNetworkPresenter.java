package com.delfi.xmobile.app.lecreusetcommunication.presenter.applynetwork;

import android.app.Activity;
import android.support.annotation.NonNull;


/**
 * Created by USER on 11/14/2019.
 */
public abstract class BaseNetworkPresenter implements INetworkPresenter {
    protected static final int MAX_RETRY = 3;

    protected Activity context;

    public BaseNetworkPresenter(@NonNull Activity context) {
        this.context = context;
    }
}
