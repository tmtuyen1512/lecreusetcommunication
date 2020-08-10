package com.delfi.xmobile.app.lecreusetcommunication.view;

import android.os.Bundle;

import com.delfi.xmobile.app.lecreusetcommunication.R;

import butterknife.ButterKnife;

public class MidnightReceiveActivity extends BaseReceiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_comm);
        ButterKnife.bind(this);
        isMidnightReceiveData = true;

        initReceive();
    }
}
