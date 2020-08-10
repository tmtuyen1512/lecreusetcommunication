package com.delfi.xmobile.app.lecreusetcommunication.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.delfi.xmobile.app.lecreusetcommunication.BuildConfig;
import com.delfi.xmobile.app.lecreusetcommunication.ModuleApp;
import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.ftpcom.DoCommunication;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration.Configuration.Global.NetworkInformation;
import com.delfi.xmobile.app.lecreusetcommunication.model.BarcodeConfig;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.Generelt;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryInventoryModel;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryOrderModel;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryShelfMarkModel;
import com.delfi.xmobile.app.lecreusetcommunication.model.Network;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applyethernet.EthernetPresenter;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applynetwork.BaseNetworkPresenter;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi.WifiDhcpPresenter;
import com.delfi.xmobile.app.lecreusetcommunication.presenter.applywifi.WifiEnterprisePresenter;
import com.delfi.xmobile.app.lecreusetcommunication.utils.AutoUpdateIO;
import com.delfi.xmobile.app.lecreusetcommunication.utils.CommLogHandle;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConfigIO;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConstComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DBUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DoInsertBase;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FTPCommListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.HistoryHelper;
import com.delfi.xmobile.app.lecreusetcommunication.utils.LogType;
import com.delfi.xmobile.app.lecreusetcommunication.utils.NetworkUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.SyncDBListener;
import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiConfig;
import com.delfi.xmobile.app.lecreusetcommunication.utils.wakeup.AppVisibilityDetector;
import com.delfi.xmobile.app.lecreusetcommunication.view.adapter.MessageAdapter;
import com.delfi.xmobile.lib.featuremanager.FeatureManager;
import com.delfi.xmobile.lib.featuremanager.UpgradeManager;
import com.delfi.xmobile.lib.featuremanager.model.XmobileBundle;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.lecreusetbase.utils.IDialogEventError;
import com.delfi.xmobile.lib.lecreusetbase.utils.IScreenEventError;
import com.delfi.xmobile.lib.lecreusetbase.utils.IScreenEventSuccess;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.BaseActivity;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.common.FullScreenDialog;
import com.delfi.xmobile.lib.xcore.common.DeviceApplicationInformation;
import com.delfi.xmobile.lib.xcore.common.DeviceInformation;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.common.SoundManager;
import com.delfi.xmobile.lib.xcore.communication.handler.EnumMessageType;
import com.delfi.xmobile.lib.xcore.communication.handler.TransferEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.scanner.DelfiScannerHandler;
import com.delfi.xmobile.lib.xcore.sqlite.SharedPre;
import com.delfi.xmobile.lib.xcore.validation.validate.IValidationPresenter;
import com.delfi.xmobile.lib.xcore.validation.validate.IValidationView;
import com.delfi.xmobile.lib.xcore.validation.validate.ValidationPresenterImpl;

import net.wequick.small.Small;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import device.sdk.Control;
import timber.log.Timber;

public class CommunicationActivity extends BaseActivity {
    public static final String KEY_EXTRA_SETTINGS = "FTPSettingList";
    public static final String KEY_EXTRA_TYPE = "TypeComm";
    public static final String KEY_EXTRA_ISRECEIVECONFIG = "IsReceiveConfig";
    public static final String KEY_EXTRA_ISMIDNIGHT_RECEIVEDATA = "MidnightReceiveData";
    public static final String KEY_EXTRA_ISNEWSTOREID = "isNewStoreId";

    public static final int REQUEST_INSTALL_AFTER_DOWNLOAD = 101;
    public static final int REQUEST_INSTALL_ON_UPGRADE = 102;

    @BindView(R.id.llItem) LinearLayout llItem;
    @BindView(R.id.llDialog) LinearLayout llDialog;
    @BindView(R.id.tvDialog) TextView tvDialog;
    @BindView(R.id.mainTitle) TextView mainTitle;
    @BindView(R.id.btnSyncComm) Button btnRetry;
    @BindView(R.id.tvVersion) TextView tvVersion;
    @BindView(R.id.imgConnect) pl.droidsonroids.gif.GifImageView imgConnect;
    @BindView(R.id.imgValidate) pl.droidsonroids.gif.GifImageView imgValidate;
    @BindView(R.id.imgLogin) pl.droidsonroids.gif.GifImageView imgLogin;
    @BindView(R.id.imgDone) pl.droidsonroids.gif.GifImageView imgDone;
    @BindView(R.id.llErrorInternet) LinearLayout llErrorInternet;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.llMessage) LinearLayout llMessage;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.tvMessage) TextView tvMessage;
    @BindView(R.id.tvExtMessage) TextView tvExtMessage;
    @BindView(R.id.tvContinue) TextView tvContinue;
    @BindView(R.id.llValidate) LinearLayout llValidate;
    @BindView(R.id.llValidateMsg) LinearLayout llValidateMsg;
    @BindView(R.id.tvValidateMsg) TextView tvValidateMsg;
    @BindView(R.id.llLoginMsg) LinearLayout llLoginMsg;
    @BindView(R.id.tvLoginMsg) TextView tvLoginMsg;

    private List<FTPSetting> settingList;
    private TypeComm typeComm;
    private DoCommunication comm;
    private boolean isReceiveConfig;
    private boolean isNewStoreId;
    private boolean isMidnightReceiveData;
    private boolean isRunning;
    private boolean pressToClose;
    private String fileCorrupted;
    private IValidationPresenter presenter;
    private String validateDate;
    private FTPSetting.FilesTo validateMessage;
    private FTPSetting.FilesTo validateLogin;

    List<XmobileBundle> xmobileBundles = new ArrayList<>();

    public static final String QUOTE = "\"";
    private String previousSSID = "";
    private static final int MAX_RETRY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_v4);
        ButterKnife.bind(this);
        AppVisibilityDetector.init(getApplication(), appVisibilityCallback);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        llItem.removeAllViews();
        llDialog.setVisibility(View.GONE);
        llMessage.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        imgConnect.setImageResource(R.drawable.ic_radio_button_off);

        settingList = (List<FTPSetting>) getIntent().getSerializableExtra(KEY_EXTRA_SETTINGS);
        typeComm = (TypeComm) getIntent().getSerializableExtra(KEY_EXTRA_TYPE);
        isReceiveConfig = getIntent().getBooleanExtra(KEY_EXTRA_ISRECEIVECONFIG, false);
        isNewStoreId = getIntent().getBooleanExtra(KEY_EXTRA_ISNEWSTOREID, false);
        isMidnightReceiveData = getIntent().getBooleanExtra(KEY_EXTRA_ISMIDNIGHT_RECEIVEDATA, false);

        if (settingList == null || settingList.size() == 0) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.no_ftp_settings_found), new IScreenEventError() {
                @Override
                public void onOk() {
                    finish();
                }
            });

        } else {

            startCommunication();
        }
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    if (typeComm == TypeComm.RECEIVE && !pressToClose) {
                        updateData();
                    } else {
                        finish();
                    }
                }
            }
        });

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    llMessage.setVisibility(View.GONE);
                    startCommunication();
                }
            }
        });

        if (typeComm == TypeComm.SEND)
            mainTitle.setText(R.string.send_data);
        else
            mainTitle.setText(R.string.receive_data);

        tvVersion.setText(BuildConfig.VERSION_NAME);
    }

    private void startCommunication() {
        llItem.removeAllViews();
        for (FTPSetting f : settingList) {
            for (FTPSetting.FilesTo i : f.FilesTo) {
                i.isTransfer = false;
                i.message = null;
            }
            if (f.SettingsBackup != null && f.SettingsBackup.FilesTo.size() > 0) {
                for (FTPSetting.FilesTo bak : f.SettingsBackup.FilesTo) {
                    bak.isTransfer = false;
                    bak.message = null;
                }
            }
        }
        imgValidate.setImageResource(R.drawable.ic_radio_button_off);
        imgLogin.setImageResource(R.drawable.ic_radio_button_off);
        imgDone.setImageResource(R.drawable.ic_radio_button_off);
        initView();
        imgConnect.setImageResource(R.mipmap.ic_loading);
        llErrorInternet.setVisibility(View.GONE);
        llLoginMsg.setVisibility(View.GONE);
        llValidate.setVisibility(View.GONE);
        llValidateMsg.setVisibility(View.GONE);
        tvValidateMsg.setText("");
        btnRetry.setVisibility(View.GONE);
        pressToClose = false;
        validateMessage = null;
        fileCorrupted = "";

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new Thread(new Runnable() {
            @Override
            public void run() {

                checkConnection();
            }
        }).start();

        previousSSID = unquoted(getCurrentNetworkSSID());
    }

    @NonNull
    private String unquoted(@NonNull String ssid) {
        return ssid.replaceAll(QUOTE, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DeviceInformation.isPM66() || DeviceInformation.isPM85()) {
            try {
                DelfiScannerHandler.getInstance().disableScanner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (DeviceInformation.isPM66() || DeviceInformation.isPM85()) {
            try {
                DelfiScannerHandler.getInstance().enableScanner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkConnection() {
        int count = 0;
        isRunning = true;


        while ((!NetworkUtil.isNetworkConnected(CommunicationActivity.this) || !NetworkUtil.checkConnectedToServer()) && count < MAX_RETRY) {
            count += 1;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (!NetworkUtil.isNetworkConnected(CommunicationActivity.this) || !NetworkUtil.checkConnectedToServer()) {
                    Log.e("checkConnectedToServer", "No connected");
                    imgConnect.setImageResource(R.drawable.ic_error_comm);
                    llErrorInternet.setVisibility(View.VISIBLE);

                    List<FTPSetting.FilesTo> list = new ArrayList<>();
                    FTPSetting.FilesTo item = new FTPSetting.FilesTo(getResources().getString(R.string.check_connection));
                    item.message = getResources().getString(R.string.error_no_internet_connection);
                    item.isTransfer = false;
                    list.add(item);
                    showErrorMessage(list);
                } else {
                    validateAppID();
                }
            }
        });


    }

    private void showErrorMessage(List<FTPSetting.FilesTo> list) {
        MessageAdapter adapter = new MessageAdapter(list);
        recyclerView.setAdapter(adapter);
        tvMessage.setVisibility(View.GONE);
        tvExtMessage.setVisibility(View.GONE);
        isRunning = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnRetry.setVisibility(View.VISIBLE);
                llMessage.setVisibility(View.VISIBLE);
                pressToClose = true;
            }
        }, 1000);
    }

    @SuppressLint("StaticFieldLeak")
    private void validateAppID() {
        presenter = new ValidationPresenterImpl(new IValidationView() {
            @Override
            public void showProgressDialog(boolean show) {

            }

            @Override
            public void showToastMessage(int idMessage) {

            }

            @Override
            public Context getContext() {
                return CommunicationActivity.this;
            }

            @Override
            public void onValidateSuccess() {
                imgValidate.setImageResource(R.drawable.ic_check);
                imgLogin.setImageResource(R.mipmap.ic_loading);
                SharedManager.getInstance(CommunicationActivity.this).putString("ValidateDate", validateDate);
                CommLogHandle.getInstance().logMessage(LogType.I, "ValidateLicense", "Validation success", null);
                validateMessage = new FTPSetting.FilesTo(getResources().getString(R.string.validating_license));
                validateMessage.isTransfer = true;
                new AsyncCommTest().execute();
            }

            @Override
            public void onValidateError(String message) {
                imgValidate.setImageResource(R.drawable.ic_error_comm);
                llValidateMsg.setVisibility(View.VISIBLE);
                tvValidateMsg.setText(message);
                CommLogHandle.getInstance().logMessage(LogType.I, "ValidateLicense", "Validation error: " + message, null);

                List<FTPSetting.FilesTo> list = new ArrayList<>();
                FTPSetting.FilesTo item = new FTPSetting.FilesTo(getResources().getString(R.string.validating_license));
                item.message = message;
                item.isTransfer = false;
                list.add(item);
                showErrorMessage(list);
            }
        });

        String appId = SharedManager.getInstance(this).getString(SharedPre.KEY_XMOBILE_APPID);
        //SharedManager.getInstance(this).putString(SharedPre.KEY_XMOBILE_APPID, "REIXFV");

        final String finalAppId = appId;

        new AsyncTask<Void, String, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                CommLogHandle.getInstance().logMessage(LogType.I, "", "\n-------------------------------------------------", null);
                CommLogHandle.getInstance().logMessage(LogType.I, "ValidateLicense", "Checking first communication per day", null);
            }

            @Override
            protected String doInBackground(Void... voids) {
                String networkTime = NetworkUtil.getCurrentNetworkTime();
                if (networkTime == null) {
                    networkTime = NetworkUtil.getCurrentLocalTime(); //always validation if cannot get time from google
                }
                return networkTime;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Log.e("networkTime", s);
                imgConnect.setImageResource(R.drawable.ic_check);

                String date = SharedManager.getInstance(CommunicationActivity.this).getString("ValidateDate");

                CommLogHandle.getInstance().logMessage(LogType.I, "ValidateLicense", String.format("Compare day %s <> %s", date, s), null);

                if (!date.equals(s)) {
                    validateDate = s;
                    llValidate.setVisibility(View.VISIBLE);
                    imgValidate.setImageResource(R.mipmap.ic_loading);

                    CommLogHandle.getInstance().logMessage(LogType.I, "ValidateLicense", "Starting validation", null);
                    presenter.authorize(finalAppId);
                } else {
                    llValidate.setVisibility(View.GONE);
                    new AsyncCommTest().execute();
                }
            }
        }.execute();

    }

    private void initView() {
        int i = 0;
        for (FTPSetting f : settingList) {
            addView(f, i);
            i += 1;
        }
    }

    private void addView(FTPSetting f, int index) {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_process_comm_v4, null);
        TextView tvLabel = view.findViewById(R.id.tvLabel);
        pl.droidsonroids.gif.GifImageView img = view.findViewById(R.id.img);
        LinearLayout llDetail = view.findViewById(R.id.llDetail);
        tvLabel.setText(f.isFTP ? (typeComm == TypeComm.SEND ? getResources().getString(R.string.sending_files_for) : getResources().getString(R.string.receiving_files_for)) + f.CommunicationLabel : f.CommunicationLabel);
        if (tvLabel.getText().toString().toLowerCase().equals("Modtager filer for auto opdatering".toLowerCase()))
            tvLabel.setText("Modtager opdateringer"); //fixed Modtager opdateringer
        img.setImageResource(R.drawable.ic_radio_button_off);
        llDetail.removeAllViews();
        f._View = view;
        if (f.FilesTo != null) {
            addViewDetail(llDetail, f.FilesTo);
        }
        if (f.doInsertBases != null) {
            for (DoInsertBase db : f.doInsertBases) {
                LayoutInflater inflaterD = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View viewD = inflaterD.inflate(R.layout.item_process_comm_detail, null);
                TextView tvLabelDetail = viewD.findViewById(R.id.tvLabelDetail);
                TextView tvType = viewD.findViewById(R.id.tvType);
                TextView tvDetail = viewD.findViewById(R.id.tvDetail);
                ProgressBar progressBar = viewD.findViewById(R.id.progressBarComm);
                tvLabelDetail.setText(db.fileName);
                tvType.setText("");
                tvDetail.setText(getResources().getString(R.string.calculating));
                progressBar.setVisibility(View.GONE);
                llDetail.setVisibility(View.GONE);
                llDetail.addView(viewD);
                db.view = viewD;
            }
        }

        llItem.addView(view, index);
    }

    private void addViewDetail(LinearLayout llDetail, List<FTPSetting.FilesTo> files) {
        for (FTPSetting.FilesTo file : files) {
            LayoutInflater inflaterD = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewD = inflaterD.inflate(R.layout.item_process_comm_detail, null);
            TextView tvLabelDetail = viewD.findViewById(R.id.tvLabelDetail);
            TextView tvType = viewD.findViewById(R.id.tvType);
            TextView tvDetail = viewD.findViewById(R.id.tvDetail);
            ProgressBar progressBar = viewD.findViewById(R.id.progressBarComm);

            //tvLabelDetail.setText(file.Name);
            tvLabelDetail.setText(file.DisplayName);

            tvType.setText(typeComm == TypeComm.SEND ? getResources().getString(R.string.uploading) : getResources().getString(R.string.downloading));
            progressBar.setVisibility(View.GONE);
            viewD.setVisibility(View.GONE);
            llDetail.addView(viewD);
            file.fView = viewD;
        }
    }


    private class AsyncCommTest extends AsyncTask<Void, String, FTPSetting.FilesTo> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imgLogin.setImageResource(R.mipmap.ic_loading);
        }

        @Override
        protected FTPSetting.FilesTo doInBackground(Void... voids) {
            FTPSetting ftpOld = null;
            for (final FTPSetting ftp : settingList) {
                if (ftp.isFTP) {
                    if (ftpOld == null || !TextUtils.equals(ftp.ServerIP, ftpOld.ServerIP) || !TextUtils.equals(ftp.ServerPort, ftpOld.ServerPort)
                            || !TextUtils.equals(ftp.ServerUser, ftpOld.ServerUser) || !TextUtils.equals(ftp.ServerPassword, ftpOld.ServerPassword)) {
                        comm = new DoCommunication(CommunicationActivity.this, ftp, transferListener);
                        String result = comm.testConnection();
                        if (result != null) { //fail connect
                            if(ftp.SettingsBackup != null && (!TextUtils.equals(ftp.ServerIP, ftp.SettingsBackup.ServerIP) || !TextUtils.equals(ftp.ServerPort, ftp.SettingsBackup.ServerPort)
                                    || !TextUtils.equals(ftp.ServerUser, ftp.SettingsBackup.ServerUser) || !TextUtils.equals(ftp.ServerPassword, ftp.SettingsBackup.ServerPassword))){
                                comm = new DoCommunication(CommunicationActivity.this, ftp.SettingsBackup, transferListener);
                                result = comm.testConnection();
                                if(result != null){
                                    if(ftp.CommunicationLabel != null && ftp.CommunicationLabel.contains("Pickconfig"))
                                        continue; //pickconfig use RDIDatabase settings, when test connection fail, allow continue
                                    FTPSetting.FilesTo rs = new FTPSetting.FilesTo("");
                                    rs.message = result;
                                    rs.Label = ftp.CommunicationLabel;
                                    return rs;
                                }
                            }
                            else {
                                if(ftp.CommunicationLabel != null && ftp.CommunicationLabel.contains("Pickconfig"))
                                    continue; //pickconfig use RDIDatabase settings, when test connection fail, allow continue
                                FTPSetting.FilesTo rs = new FTPSetting.FilesTo("");
                                rs.message = result;
                                rs.Label = ftp.CommunicationLabel;
                                return rs;
                            }
                        }

                    }
                    ftpOld = ftp;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(FTPSetting.FilesTo result) {

            if (result != null) {
                imgLogin.setImageResource(R.drawable.ic_error_comm);
                llLoginMsg.setVisibility(View.VISIBLE);
                tvLoginMsg.setText(result.message);

                List<FTPSetting.FilesTo> list = new ArrayList<>();
                result.Name = getResources().getString(R.string.login_ftp_label);
                //item.message = result;
                //item.isTransfer = false;
                list.add(result);
                showErrorMessage(list);
            } else {
                validateLogin = new FTPSetting.FilesTo(getResources().getString(R.string.login_ftp_label));
                validateLogin.isTransfer = true;
                imgLogin.setImageResource(R.drawable.ic_check);
                llLoginMsg.setVisibility(View.GONE);

                new AsyncComm(CommunicationActivity.this, settingList).execute();
            }
        }
    }

    private class AsyncComm extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final List<FTPSetting> FTPSettings;

        public AsyncComm(Context context, List<FTPSetting> FTPSettings) {
            this.context = context;
            this.FTPSettings = FTPSettings;
            isRunning = true;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            int i = 0;
            boolean exists = false;
            for (final FTPSetting ftp : FTPSettings) {
                if (ftp.isFTP) {
                    comm = new DoCommunication(context, ftp, transferListener);
                    try {
                        comm.startCommunication(typeComm);
                        if (comm.isExistsError())
                            exists = true;
                        if (comm.isExistsError() && ftp.SettingsBackup != null) {
                            exists = false;
                            comm.disconnect();

                            //Need to copy properties from FTPServer to FTPServer backup
                            ftp.SettingsBackup.doInsertBases = ftp.doInsertBases;
                            ftp.SettingsBackup.FilesTo = new ArrayList<>();
                            for (FTPSetting.FilesTo f : ftp.FilesTo) {
                                if (!f.isTransfer) {
                                    FTPSetting.FilesTo bkFile = new FTPSetting.FilesTo(f);
                                    bkFile.message = null;
                                    bkFile.isTransfer = false;
                                    ftp.SettingsBackup.FilesTo.add(bkFile);
                                }
                            }


                            final int finalI = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addView(ftp.SettingsBackup, finalI + 1);
                                }
                            });
                            Thread.sleep(1000);
                            i += 1;
                            comm = new DoCommunication(context, ftp.SettingsBackup, transferListener);
                            comm.startCommunication(typeComm);
                            if (comm.isExistsError())
                                exists = true;
                        }
                        if (ftp.CommunicationLabel.equals(getResources().getString(R.string.auto_update)) && !comm.isExistsError()) {
                            boolean isFirstTime = !SharedManager.getInstance(CommunicationActivity.this).getBoolean("HAS_COMMUNICATED_BEFORE");
                            if (existsModuleFile()) {
                                List<String> SOFiles = getListFilesSO(isFirstTime);
                                if (SOFiles.size() > 0) {
                                    FTPSetting.FilesTo tmp = ftp.FilesTo.get(0);
                                    ftp.FilesTo.remove(0);

                                    List<FTPSetting.FilesTo> list = new ArrayList<>();
                                    for (String file : SOFiles) {
                                        FTPSetting.FilesTo f = new FTPSetting.FilesTo(file, makeUserFriendlyName(file));
                                        if (!ftp.FilesTo.contains(f)) {
                                            list.add(f);
                                        }
                                    }
                                    if (list.size() > 0) {
                                        ftp.FilesTo.clear();
                                        ftp.FilesTo.addAll(list);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                addViewDetail((LinearLayout) ftp._View.findViewById(R.id.llDetail), ftp.FilesTo);
                                            }
                                        });
                                        Thread.sleep(1000);

                                        comm.startCommunication(typeComm);
                                    }
                                    ftp.FilesTo.add(0, tmp);
                                }
                            }
                            SharedManager.getInstance(CommunicationActivity.this).putBoolean("HAS_COMMUNICATED_BEFORE", true);
                        }
                        comm.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                    }
                } else {
                    DBUtil dbUtils = new DBUtil(CommunicationActivity.this, ftp, syncDBListener);
                    dbUtils.startSync();
                }
                i += 1;

            }

            return exists;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            imgDone.setImageResource(R.drawable.ic_check);
            isRunning = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            int totalSucess = 0;
            int totalAll = 0;
            int totalUpToDate = 0;
            List<FTPSetting.FilesTo> list = new ArrayList<>();

            if (validateMessage != null)
                list.add(validateMessage);

            if (validateLogin != null)
                list.add(validateLogin);

            for (FTPSetting f : settingList) {
                if (f.FilesTo != null) {
                    for (FTPSetting.FilesTo i : f.FilesTo) {
                        i.Label = f.CommunicationLabel;
                        list.add(i);
                        if (i.isSameFile) {
                            totalUpToDate += 1;
                        }
                        if (i.isTransfer && !i.isSameFile) {
                            totalSucess += 1;
                        }
                        if (!i.isTransfer && f.SettingsBackup != null && f.SettingsBackup.FilesTo.size() > 0) {
                            for (FTPSetting.FilesTo bak : f.SettingsBackup.FilesTo) {
                                if (bak.Name.equals(i.Name)) {
                                    bak.Label = f.SettingsBackup.CommunicationLabel;
                                    list.add(bak);
                                    if (bak.isTransfer && !bak.isSameFile)
                                        totalSucess += 1;
                                    break;
                                }
                            }
                        }
                        totalAll += 1;
                    }
                }
                if (f.doInsertBases != null) {
                    for (DoInsertBase db : f.doInsertBases) {
                        if (!db.isSameFile) {
                            FTPSetting.FilesTo filesTo = new FTPSetting.FilesTo(db.fileName);
                            filesTo.isTransfer = db.isSuccess();
                            filesTo.Label = f.CommunicationLabel;
                            if (db.getErrorLines().size() > 0) {
                                filesTo.isTransfer = false;
                                fileCorrupted += db.fileName + "\n";
                                filesTo.message = getResources().getString(R.string.one_or_more_lines_are_invalid_in_the_file);
                            }
                            list.add(filesTo);
                        }
                    }
                }
            }

            MessageAdapter adapter = new MessageAdapter(list);
            recyclerView.setAdapter(adapter);
            tvMessage.setVisibility(View.VISIBLE);

            if (typeComm == TypeComm.RECEIVE) {
                tvMessage.setText(String.format(getResources().getString(R.string.new_files_are_received), totalSucess));

                tvExtMessage.setVisibility(View.VISIBLE);
                tvExtMessage.setText(String.format(getResources().getString(R.string.new_files_are_up_to_date), totalUpToDate));
            } else {
                tvMessage.setText(String.format(getResources().getString(R.string.new_files_are_sent), totalSucess));
                tvExtMessage.setVisibility(View.GONE);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnRetry.setVisibility(View.VISIBLE);
                    llMessage.setVisibility(View.VISIBLE);

                    if (isMidnightReceiveData) {
                        tvContinue.performClick();
                    }
                }
            }, 1500);

            Log.e("onPostExecute", "HERE: running: " + isRunning);
            if (typeComm == TypeComm.RECEIVE && !isReceiveConfig)
                SharedManager.getInstance(CommunicationActivity.this).putLong(ConstComm.LastTimeSync, System.currentTimeMillis());
        }
    }

    private boolean existsModuleFile() {
        //check exists file module_list.json
        File file = new File(Constant.getExternal(), "/Import/module_list.json");
        return file.exists();
    }

    /**
     * @return list file so to update modules
     */
    private List<String> getListFilesSO(boolean isFirstTime) {
        List<String> list = new ArrayList<>();
        try {
            List<XmobileBundle> bundles = AutoUpdateIO.readModuleList();
            if (bundles != null) {
                xmobileBundles.clear();

                for (XmobileBundle bundle : bundles) {

                    //Do not download update modules if the modules are disabled from configuration.
                    if (isBundleSkipByConfig(bundle)) {
                        continue;
                    }

                    XmobileBundle latestOnServer = getLatestVersionOnServer(bundle);

                    //Handle APK auto update: skip on first time
                    if (isAppAPK(bundle) && isFirstTime) {
                        continue;
                    }

                    //Handle APK auto update: Only except new .apk version
                    if (isAppAPK(bundle) && !isHasNewerApkVersion(latestOnServer)) {
                        continue;
                    }

                    //Handle library modules: Only except new .so version
                    if (isModuleLibrary(bundle) && !isNewerVersionCode(latestOnServer.vcode, getVersionCodeOnDevice(bundle))) {
                        continue;
                    }

                    //Handle others plugs-in modules auto update: Except both upgrade & downgrade, do not install itself
                    if (isSameVersionCode(latestOnServer.vcode, getVersionCodeOnDevice(bundle))) {
                        continue;
                    }

                    String fileName = latestOnServer.url.replace("/", "");
                    list.add(fileName);
                    if (!isAppAPK(bundle)) { //xmobileBundles is list of .so files, .apk file is excluded
                        xmobileBundles.add(latestOnServer);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, e.getMessage(), e));
        }
        return list;
    }

    @NonNull
    private XmobileBundle getLatestVersionOnServer(XmobileBundle bundle) {
        XmobileBundle latestVer = bundle;

        if (latestVer.vcode == null) {
            latestVer.vcode = 0;
        }

        if (bundle.versions != null) {
            for (XmobileBundle version : bundle.versions) {

                if (version.vcode > latestVer.vcode) {
                    latestVer = version;
                }
            }
        }
        return latestVer;
    }

    private String makeUserFriendlyName(@NonNull String fileName) {
        try {
            if (fileName.contains("-release.apk")) {
                fileName = fileName.replace("com.delfi.xmobile_", "APK ");
                fileName = fileName.replace("-release.apk", "");

            } else {
                fileName = fileName.replace("libcom_delfi_xmobile_app_", "");
                fileName = fileName.replace("libcom_delfi_xmobile_lib_", "");
                fileName = fileName.replace("reitan", "");
                fileName = fileName.replace("_", " v");

                if (fileName.length() > 3) {
                    fileName = fileName.substring(0, fileName.length() - 3);
                }
            }
            if (!fileName.isEmpty()) {
                fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
            }

        } catch (Exception e) {/*ignore*/}

        return fileName;
    }

    public static boolean isSameVersionCode(int onServerVCode, int onDeviceVCode) {
        int lenghServerVC = String.valueOf(onServerVCode).length();
        int lenghDeviceVC = String.valueOf(onDeviceVCode).length();

        //If server is version code on short form
        if (lenghDeviceVC - lenghServerVC >= 2) {
            int shortOnDevice = Integer.valueOf(String.valueOf(onDeviceVCode).substring(0, lenghServerVC));
            return onServerVCode == shortOnDevice;

        } else {
            return onServerVCode == onDeviceVCode;
        }
    }

    public static boolean isNewerVersionCode(int onServerVCode, int onDeviceVCode) {
        int lenghServerVC = String.valueOf(onServerVCode).length();
        int lenghDeviceVC = String.valueOf(onDeviceVCode).length();

        //If server is version code on short form
        if (lenghDeviceVC - lenghServerVC >= 2) {
            int shortOnDevice = Integer.valueOf(String.valueOf(onDeviceVCode).substring(0, lenghServerVC));
            return onServerVCode > shortOnDevice;

        } else {
            return onServerVCode > onDeviceVCode;
        }
    }

    private boolean isHasNewerApkVersion(XmobileBundle latestOnServer) {
        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.INFO,
                String.format("[AutoUpdate - APK] Current apk version is %s, " +
                        "Latest apk version on server %s", Small.getLaunchingHostVersionCode(), latestOnServer.vcode), null));

        if (latestOnServer.vcode > Small.getLaunchingHostVersionCode()) {

            SharedManager.getInstance(this).putBoolean("UpdateApk.RequestInstall", true);
            SharedManager.getInstance(this).putString("UpdateApk.Url", getBundleDownloadFileName(latestOnServer));

            Log.w("TAG", String.format("Found new APK (%s - %s)", latestOnServer.vname, latestOnServer.vcode));
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.INFO,
                    String.format("[AutoUpdate - APK] Found new APK (%s - %s)", latestOnServer.vname, latestOnServer.vcode), null));
            return true;
        }

        return false;
    }

    private boolean isAppAPK(XmobileBundle bundle) {
        return getPackageName().equals(bundle.pkg) && bundle.moduleType == 4;
    }

    private boolean isModuleLibrary(XmobileBundle bundle) {

        //CORE
        if ("com.delfi.xmobile.lib.xcore".equals(bundle.pkg)) {
            return true;
        }

        //BASE
        if ("com.delfi.xmobile.lib.lecreusetbase".equals(bundle.pkg)) {
            return true;
        }

        //FEATURE MANAGER
        if ("com.delfi.xmobile.lib.featuremanager".equals(bundle.pkg)) {
            return true;
        }

        return false;
    }

    protected int getVersionCodeOnDevice(XmobileBundle bundle) {
        if (isAppAPK(bundle)) {
            return Small.getLaunchingHostVersionCode();
        }
        return getVersionCode(bundle);
    }

    protected int getVersionCode(XmobileBundle bundle) {
        try {
            return Small.getBundle(bundle.pkg).getVersionCode();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getBundleDownloadFileName(XmobileBundle u) {
        if (u == null) {
            return "";
        }
        if (u.url != null && u.url.contains("/")) {
            int beginIndex = u.url.lastIndexOf("/") + 1;
            return u.url.substring(beginIndex);
        } else {
            return u.url;
        }
    }

    private boolean isBundleSkipByConfig(XmobileBundle bundle) {
        List<String> skipList = getSkipPackageList();
        return skipList.contains(bundle.pkg);
    }

    @NonNull
    private List<String> getSkipPackageList() {
        List<String> skipList = new ArrayList<>();

        ApplicationConfiguration AppConfig = ConfigIO.readAppConfigXml(this, FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML));
        if (AppConfig == null || AppConfig.Configuration == null || AppConfig.Configuration.Global == null || AppConfig.Configuration.Menuer == null) {
            return skipList;
        }

        for (ApplicationConfiguration.Configuration.Menuer.Menu menu : AppConfig.Configuration.Menuer.Menu) {
            if ("Status".equalsIgnoreCase(menu.Name) && !menu.IsVisible) {
                skipList.add("com.delfi.xmobile.app.lecreusetinventory");

            } else if ("Ordre".equalsIgnoreCase(menu.Name) && !menu.IsVisible) {
                skipList.add("com.delfi.xmobile.app.lecreusetorder");

            } else if ("Opsamling".equalsIgnoreCase(menu.Name) && !menu.IsVisible) {
                skipList.add("com.delfi.xmobile.app.lecreusetpick");

            } else if ("Hyldeforkanter".equalsIgnoreCase(menu.Name) && !menu.IsVisible) {
                skipList.add("com.delfi.xmobile.app.lecreusetshelfmark");
            }
        }

        return skipList;
    }

    private FTPCommListener transferListener = new FTPCommListener() {
        pl.droidsonroids.gif.GifImageView img;
        LinearLayout llError;
        TextView tvError;
        TextView tvType;
        TextView tvDetail;
        ProgressBar progressBar;

        @Override
        public Context getContext() {
            return CommunicationActivity.this;
        }

        @Override
        public void onStartCommunication(final View view, final TransferEventArgs args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args.getMessageType() == EnumMessageType.CONNECT) {
                        img = view.findViewById(R.id.img);
                        llError = view.findViewById(R.id.llError);
                        tvError = view.findViewById(R.id.tvError);

                        img.setImageResource(R.mipmap.ic_loading);

                        scrollView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        }, 0);
                    }

                }
            });
        }

        @Override
        public void onStartDetail(final View fView, TransferEventArgs transferEventArgs) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fView.setVisibility(View.VISIBLE);
                    tvDetail = fView.findViewById(R.id.tvDetail);
                    tvType = fView.findViewById(R.id.tvType);
                    progressBar = fView.findViewById(R.id.progressBarComm);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });

        }

        @Override
        public void onSetProgress(View view, final TransferEventArgs args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tvPercent.setText(args.getMessage());
                    tvDetail.setText(args.getMessage());
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress((int) args.getTotal());
                }
            });
        }

        @Override
        public void onErrorStep(View view, final TransferEventArgs args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvError.setText(args.getMessage());
                    llError.setVisibility(View.VISIBLE);
                    img.setImageResource(R.drawable.ic_error_comm);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });
        }

        @Override
        public void onDoneStep(View view, final TransferEventArgs args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvType.setText(args.getMessage());
                    if (args.getState() == EnumMessageType.FAIL)
                        tvType.setTextColor(getResources().getColor(R.color.red));
                    else
                        tvType.setTextColor(getResources().getColor(R.color.green));
                    progressBar.setVisibility(View.GONE);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });


            if (typeComm == TypeComm.SEND && args.getState() == EnumMessageType.DONE) {
                updateSentDateHistoryItems(args);
            }
        }

        @Override
        public void onSetDetail(View view, final TransferEventArgs args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //tvDetail.setText(args.getMessage());
                }
            });
        }

        @Override
        public void onDoneCommunication(View view, final boolean existsError) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*llItem.setBackgroundResource(R.drawable.drawable_comm_done);
                    tvDetail.setVisibility(View.GONE);*/
                    if (!existsError)
                        img.setImageResource(R.drawable.ic_check);
                    else
                        img.setImageResource(R.drawable.ic_error_comm);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });
        }
    };

    private void updateSentDateHistoryItems(TransferEventArgs args) {
        List<HistoryInventoryModel> inventoryList = HistoryHelper.getItems(HistoryInventoryModel.class, args.getFileName());
        if (inventoryList != null && !inventoryList.isEmpty()) {
            HistoryHelper.updateInventoryItems(inventoryList);
            return;
        }

        List<HistoryOrderModel> orderList = HistoryHelper.getItems(HistoryOrderModel.class, args.getFileName());
        if (orderList != null && !orderList.isEmpty()) {
            HistoryHelper.updateOrderItems(orderList);
            return;
        }

        List<HistoryShelfMarkModel> shelfmarkList = HistoryHelper.getItems(HistoryShelfMarkModel.class, args.getFileName());
        if (shelfmarkList != null && !shelfmarkList.isEmpty()) {
            HistoryHelper.updateShelfMarkItems(shelfmarkList);
            return;
        }

    }

    private void updateData() {
        isRunning = true;
        llDialog.setVisibility(View.VISIBLE);
        SharedManager.getInstance(CommunicationActivity.this).putBoolean("NETWORK_SETTINGS_IN_PROGRESS", true);
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean result = updateConfig();
                if (result) {
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llDialog.setVisibility(View.GONE);
                            SharedManager.getInstance(CommunicationActivity.this).putBoolean("NETWORK_SETTINGS_IN_PROGRESS", false);

                            String msg = getResources().getString(R.string.data_has_been_received);
                            IScreenEventSuccess callback = new IScreenEventSuccess() {
                                @Override
                                public void onOk() {
                                    onSuccessDialog_btnOKClicked();
                                }
                            };
                            if (fileCorrupted.length() == 0) {

                                if (isMidnightReceiveData) {
                                    onSuccessDialog_btnOKClicked();
                                } else {
                                    FullScreenDialog.getInstance().showSuccess(CommunicationActivity.this, msg, callback);
                                }

                            } else {
                                msg = getResources().getString(R.string.files_orrupted) + ":\n" + fileCorrupted
                                        + "\n" + getResources().getString(R.string.please_contact_your_system_administrator);
                                showWarning(CommunicationActivity.this, msg, callback);
                            }
                        }
                    });
                }
                isRunning = false;

            }
        }).start();
    }

    private void onSuccessDialog_btnOKClicked() {
        boolean isFirstExecuted = SharedManager.getInstance(CommunicationActivity.this).getBoolean("isFirstExecuted");
        if (!isFirstExecuted || isNewStoreId) {
            setResult(RESULT_OK);
            SharedManager.getInstance(CommunicationActivity.this).putBoolean("isFirstExecuted", true);
        } else {
            boolean isXmobileSetupCompleted = SharedManager.getInstance(CommunicationActivity.this).getBoolean("isXmobileSetupCompleted");

            if (!isXmobileSetupCompleted) {
                SharedManager.getInstance(CommunicationActivity.this).putBoolean("isXmobileSetupCompleted", true);
                if (Is_Has_SO_Files_Downloaded()) {
                    upgradeApp();
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UpgradeManager.restartApp(CommunicationActivity.this);
                        }
                    }, 500);
                }
                return;
            }
        }

        //Upgrade app if has .so file downloaded from communication progress,
        //in the case of existing new apk update, skip for now, do it later on startup after restart app
        if (Is_Has_SO_Files_Downloaded()) {
            upgradeApp();
            return;
        }

        //Update APK if receive request from communication progress
        if (SharedManager.getInstance(this).getBoolean("UpdateApk.RequestInstall")) {
            attemptUpdateApk(REQUEST_INSTALL_AFTER_DOWNLOAD);
        }

        //finish progress on the other cases
        finish();
    }

    private boolean attemptUpdateApk(int requestCode) {
        SharedManager.getInstance(this).putBoolean("UpdateApk.RequestInstall", false);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File apkFile = new File(DeviceApplicationInformation.getImportDirectory() + "/"
                + SharedManager.getInstance(this).getString("UpdateApk.Url"));

        Log.w("TAG", String.format("Received request update apk %s", apkFile.getAbsolutePath()));
        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.INFO,
                String.format("[AutoUpdate - APK] Received request update apk %s", apkFile.getAbsolutePath()), null));

        if (validateApkFile(apkFile)) {
            Intent promptInstall = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
            startActivityForResult(promptInstall, requestCode);
            return true;
        }

        return false;
    }

    private boolean validateApkFile(File apkFile) {
        final PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
        if (info == null) {
            Timber.e("Invalid install file");
            return false;
        }
        if (!getPackageName().equals(info.packageName)) {
            Timber.e("Invalid package name");
            return false;
        }
        int newVersionCode = info.versionCode;
        int oldVersionCode = 0;
        try {
            oldVersionCode = Small.getLaunchingHostVersionCode();
        } catch (Exception e) {
        }
        Timber.e("Version current (%s) , new (%s)", oldVersionCode, newVersionCode);
        if (oldVersionCode < newVersionCode) {
            return true;
        }
        return false;
    }

    private boolean Is_Has_SO_Files_Downloaded() {
        return xmobileBundles != null && xmobileBundles.size() > 0;
    }

    protected void upgradeApp() {
        FeatureManager.upgradeBundle(this, xmobileBundles, onUpgradeListener);
    }

    private UpgradeManager.OnUpgradeListener onUpgradeListener = new UpgradeManager.OnUpgradeListener() {
        @Override
        public void onUpgradeFinish(boolean succeed) {
            if (succeed) {
                if (SharedManager.getInstance(CommunicationActivity.this).getBoolean("UpdateApk.RequestInstall")) {
                    if (attemptUpdateApk(REQUEST_INSTALL_ON_UPGRADE)) {
                        System.exit(0);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        return;
                    }
                }

                UpgradeManager.restartApp(CommunicationActivity.this);

            } else {
                mShowDialogError(getResources().getString(R.string.error_while_updating_application), new IDialogEventError() {
                    @Override
                    public void onOk() {
                        UpgradeManager.restartApp(CommunicationActivity.this);
                    }
                });
            }
        }

        @Override
        public void onStatusChange(XmobileBundle bundle, String status) {
            if (status.equals("error")) {

            }
        }
    };

    private boolean updateConfig() {

        boolean configuration = false;
        for (FTPSetting f : settingList) {
            if (f.CommunicationLabel.equals(getResources().getString(R.string.configuration))) {
                configuration = true;
            }
        }
        if (!configuration)
            return true;

        File file = new File(FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML) + "/Tmp/", Constant.APPLICATION_CONFIGURATION_XML);
        Log.v("updateConfig", file.getPath());
        if (file.exists()) {
            ApplicationConfiguration appConfig = ConfigIO.readAppConfigXml(this, file.getParent());
            BarcodeConfig config = ConfigIO.readBarcodeConfigXml(this);
            if (config == null)
                config = new BarcodeConfig();
            if (config.Config == null)
                config.Config = new BarcodeConfig.Config();
            if (config.Config.Generelt == null)
                config.Config.Generelt = new Generelt();
            if (config.Config.Network == null)
                config.Config.Network = new Network();

            if (appConfig == null || appConfig.Configuration == null || appConfig.Configuration.Global == null
                    || appConfig.Configuration.Global.Policies == null || appConfig.Configuration.Global.Policies.PolicyServerIp == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llDialog.setVisibility(View.GONE);
                        SharedManager.getInstance(CommunicationActivity.this).putBoolean("NETWORK_SETTINGS_IN_PROGRESS", false);

                        FullScreenDialog.getInstance().showError(CommunicationActivity.this, getResources().getString(R.string.error_file_app_config_read_error),
                                new IScreenEventError() {
                                    @Override
                                    public void onOk() {
                                        finish();
                                    }
                                });
                    }
                });

                return false;
            }
            try {
                config.Config.Generelt.PolicyServerIp = appConfig.Configuration.Global.Policies.PolicyServerIp;
                config.Config.Generelt.PolicyServerPort = appConfig.Configuration.Global.Policies.PolicyServerPort;
                config.Config.Generelt.PolicyServerUser = appConfig.Configuration.Global.Policies.PolicyServerUser;
                config.Config.Generelt.PolicyServerPassword = appConfig.Configuration.Global.Policies.PolicyServerPassword;
                config.Config.Generelt.PolicyServerConfigDirectory = appConfig.Configuration.Global.Policies.PolicyServerConfigDirectory;
                config.Config.Generelt.FTPType = appConfig.Configuration.Global.Policies.FTPType;
                config.Config.Generelt.IsImplicit = appConfig.Configuration.Global.Policies.IsImplicit;

                if (appConfig.Configuration.Global.NetworkInformation != null) {

                    //check value changed or not before override
                    checkExtendedWifiChanged(config.Config.Network,
                            appConfig.Configuration.Global.NetworkInformation);


                    config.Config.Network.SSID = appConfig.Configuration.Global.NetworkInformation.SSID;
                    config.Config.Network.WifiPassword = appConfig.Configuration.Global.NetworkInformation.WifiPassword;
                    config.Config.Network.WifiPassword2 = appConfig.Configuration.Global.NetworkInformation.WifiPassword2;
                    config.Config.Network.Encryption = appConfig.Configuration.Global.NetworkInformation.Encryption;
                    config.Config.Network.EAPType = appConfig.Configuration.Global.NetworkInformation.EAPType;
                    config.Config.Network.Username = appConfig.Configuration.Global.NetworkInformation.Username;
                    config.Config.Network.UserPassword = appConfig.Configuration.Global.NetworkInformation.UserPassword;
                    config.Config.Network.CCXFeatures = appConfig.Configuration.Global.NetworkInformation.CCXFeatures;
                    config.Config.Network.RoamPeriod = appConfig.Configuration.Global.NetworkInformation.RoamPeriod;
                    config.Config.Network.RoamTrigger = appConfig.Configuration.Global.NetworkInformation.RoamTrigger;
                    config.Config.Network.RoamDelta = appConfig.Configuration.Global.NetworkInformation.RoamDelta;
                    config.Config.Network.NoCredsPrompt = Boolean.parseBoolean(appConfig.Configuration.Global.NetworkInformation.NoCredsPrompt);
                    config.Config.Network.RadioMode = appConfig.Configuration.Global.NetworkInformation.RadioMode;

                    Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG,
                            "[Apply Network Settings][Cloud Config] Network Information: " + appConfig.Configuration.Global.NetworkInformation.toString(), null));

                    if (Boolean.FALSE.equals(appConfig.Configuration.Global.NetworkInformation.DHCP)) {
                        config.Config.Network.DHCP = false;
                        config.Config.Network.StaticTerminalIp = appConfig.Configuration.Global.NetworkInformation.StaticTerminalIp;
                        config.Config.Network.StaticTerminalGateway = appConfig.Configuration.Global.NetworkInformation.StaticTerminalGateway;
                        config.Config.Network.StaticTerminalSubnet = appConfig.Configuration.Global.NetworkInformation.StaticTerminalSubnet;
                        config.Config.Network.StaticTerminalDns = appConfig.Configuration.Global.NetworkInformation.StaticTerminalDns;
                        config.Config.Network.StaticTerminalDns2 = appConfig.Configuration.Global.NetworkInformation.StaticTerminalDns2;

                    } else {
                        config.Config.Network.DHCP = true;
                    }

                    applyNetworkSettings(config.Config.Network);
                }
                applyStandbyTimeout(appConfig);

                File f = new File(FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML), Constant.APPLICATION_CONFIGURATION_XML.replace("/", ""));
                FileUtil.getInstance().move(file, f);

                ConfigIO.createBarcodeConfigXml(this, config);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llDialog.setVisibility(View.GONE);
                        SharedManager.getInstance(CommunicationActivity.this).putBoolean("NETWORK_SETTINGS_IN_PROGRESS", false);

                        FullScreenDialog.getInstance().showError(CommunicationActivity.this, getResources().getString(R.string.error_file_app_config_read_error),
                                new IScreenEventError() {
                                    @Override
                                    public void onOk() {
                                        finish();
                                    }
                                });
                    }
                });
                return false;
            }
        } else {
            boolean sameFile = SharedManager.getInstance(this).getBoolean(Constant.APPLICATION_CONFIGURATION_XML.replace("/", "") + "_SAMEFILE");
            if (sameFile)
                return true;
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FullScreenDialog.getInstance().showError(CommunicationActivity.this, getResources().getString(R.string.error_file_app_config_not_received),
                                new IScreenEventError() {
                                    @Override
                                    public void onOk() {
                                        finish();
                                    }
                                });
                    }
                });
                return false;
            }
        }
    }

    private void checkExtendedWifiChanged(@NonNull Network network, @NonNull NetworkInformation networkInformation) {
        boolean isRoamTriggerChanged = isChanged(network.RoamTrigger, networkInformation.RoamTrigger);
        boolean isRoamPeriodChanged = isChanged(network.RoamPeriod, networkInformation.RoamPeriod);
        boolean isCCXFeaturesChanged = isChanged(network.CCXFeatures, networkInformation.CCXFeatures);

        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.INFO,
                String.format("[Extended Wifi Settings][Cloud Config] - RoamTriggerChanged: %s, RoamPeriodChanged: %s, CCXFeaturesChanged: %s",
                        isRoamTriggerChanged, isRoamPeriodChanged, isCCXFeaturesChanged), null));

        boolean isChanged = isRoamTriggerChanged || isRoamPeriodChanged || isCCXFeaturesChanged;
        SharedManager.getInstance(this).putBoolean("ExtendedWifiChanged", isChanged);
    }

    private boolean isChanged(String old, String current) {
        if (old == null) old = "";

        if (TextUtils.isEmpty(current)) {
            return false;
        }

        return !old.equalsIgnoreCase(current);
    }

    @SuppressLint("StaticFieldLeak")
    private void applyNetworkSettings(@NonNull final Network network) {
        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG,
                "[Apply Network Settings][Cloud Config] Network: " + network.toString(), null));

        final BaseNetworkPresenter presenter = getNetworkPresenter(network);
        if (presenter != null) {
            presenter.applyNetwork(network);
        }
    }

    private BaseNetworkPresenter getNetworkPresenter(@NonNull Network network) {

        //use custom wifi settings
        if (Boolean.TRUE.equals(network.WifiEnabled)) {

            if (!TextUtils.isEmpty(network.Username) && !TextUtils.isEmpty(network.UserPassword)) {
                WifiEnterprisePresenter presenter = new WifiEnterprisePresenter(this);
                presenter.setPreviousSSID(previousSSID);

                return presenter;

            } else if (network.DHCP == null || network.DHCP) {
                WifiDhcpPresenter presenter = new WifiDhcpPresenter(this);
                presenter.setPreviousSSID(previousSSID);

                return presenter;

            } else {
                //FIXME: For Static-IP use Ethernet by default
                //return new WifiStaticPresenter(this);
                return new EthernetPresenter(this);
            }
        }

        //use wifi default
        return null;
    }

    private void applyStandbyTimeout(ApplicationConfiguration appConfig) {
        try {
            if (appConfig != null && appConfig.Configuration != null &&
                    appConfig.Configuration.Global != null &&
                    appConfig.Configuration.Global.DeviceSettings != null) {

                int time = (int) (appConfig.Configuration.Global.DeviceSettings.StandbyTimeout * 60 * 1000); //milli-seconds
                //Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
                Control.getInstance().setScreenOffTimeout(time);

                Logger.getInstance().logMessage(new LogEventArgs(
                        LogLevel.INFO, "[Apply Settings][Communication] apply stand by Timeout " + time, null));
            }

        } catch (Exception | Error e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(
                    LogLevel.WARNING, "[Apply Settings][Communication] apply stand by Timeout unsuccessful: " + e.getMessage(), null));
        }
    }

    private SyncDBListener syncDBListener = new SyncDBListener() {

        pl.droidsonroids.gif.GifImageView img;
        TextView tvType, tvDetail;
        ProgressBar progressBar;

        @Override
        public void onStart(final View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img = view.findViewById(R.id.img);
                    LinearLayout llDetail = view.findViewById(R.id.llDetail);
                    llDetail.setVisibility(View.VISIBLE);
                    img.setImageResource(R.mipmap.ic_loading);

                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });

        }

        @Override
        public void onError(View view, String message) {

        }

        @Override
        public void onDone(View view, final boolean existsError) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!existsError)
                        img.setImageResource(R.drawable.ic_check);
                    else
                        img.setImageResource(R.drawable.ic_error_comm);
                    scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);
                }
            });
        }

        @Override
        public void initView(final View view, final int totalRecords) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
                    tvType = view.findViewById(R.id.tvType);
                    tvDetail = view.findViewById(R.id.tvDetail);
                    progressBar = view.findViewById(R.id.progressBarComm);

                    tvDetail.setText(String.format(getResources().getString(R.string.lines), totalRecords));
                    if (totalRecords > 0) {
                        tvType.setText(R.string.waiting);
                        tvType.setTextColor(getResources().getColor(R.color.dark_grey));
                    } else {
                        tvType.setText(getResources().getString(R.string.file_not_found));
                        tvType.setTextColor(getResources().getColor(R.color.red));
                    }
                }
            });
        }

        @Override
        public void initViewDone(final View view) {

        }

        @Override
        public void onStartDetail(final View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvType = view.findViewById(R.id.tvType);
                    tvDetail = view.findViewById(R.id.tvDetail);
                    progressBar = view.findViewById(R.id.progressBarComm);

                    tvType.setText(R.string.processing);
                    tvType.setTextColor(getResources().getColor(R.color.colorPrimary));
                    /*scrollView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 0);*/
                }
            });
        }

        @Override
        public void onProgressDetail(View view, final String message, final int percent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDetail.setText(message);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(percent);
                }
            });
        }

        @Override
        public void onDoneDetail(View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvType.setText(R.string.success);
                    tvType.setTextColor(getResources().getColor(R.color.green));
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onErrorDetail(View view, final String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvType.setText(message);
                    tvType.setTextColor(getResources().getColor(R.color.red));
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public void onMessageDetail(View view, String message) {

        }

        @Override
        public void onHideTask(final View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.GONE);
                }
            });
        }
    };

    private WifiInfo getCurrentNetworkInfo() {
        final ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (cm != null && wifiManager != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {  //wifi
                return wifiManager.getConnectionInfo();
            }
        }

        return null;
    }

    private String getCurrentNetworkSSID() {
        WifiInfo wifiInfo = getCurrentNetworkInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return "";
    }

    private int getCurrentNetworkID() {
        WifiInfo wifiInfo = getCurrentNetworkInfo();
        if (wifiInfo != null) {
            return wifiInfo.getNetworkId();
        }
        return -1;
    }

    private WifiConfig.Type convertType(String encryption) {

        if (encryption != null && encryption.contains("WEP")) {
            return WifiConfig.Type.WEP;
        }

        if (encryption != null && encryption.contains("WPA")) {
            return WifiConfig.Type.WPA;
        }

        //default
        return WifiConfig.Type.WPA;
    }

    @Override
    public void onBackPressed() {
        if (!isRunning) {
            if (llMessage.getVisibility() == View.VISIBLE) {
                //llMessage.setVisibility(View.GONE);
                return;
            }

            super.onBackPressed();
        }
    }

    public void showWarning(Context context, String message, final IScreenEventSuccess callback) {
        final Dialog dialog = new Dialog(context, R.style.Fullscreen_DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(View.inflate(context.getApplicationContext(), R.layout.fragment_screen_warning, null));

        TextView editText = dialog.findViewById(R.id.message);
        editText.setText(message);

        Button btnOK = dialog.findViewById(R.id.btnOK);


        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onOk();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog1, int keyCode, KeyEvent mEvent) {

                if (mEvent.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK)) {
                    if (callback != null)
                        callback.onOk();
                    dialog1.dismiss();
                }

                return false;
            }
        });
        try {
            dialog.getWindow().getAttributes().windowAnimations = R.style.Fullscreen_DialogTheme;
        } catch (Exception e) {
            //ignore
        }
        dialog.show();

        SoundManager.getInstance().PlayOK(context.getApplicationContext());
    }

    private AppVisibilityDetector.AppVisibilityCallback appVisibilityCallback = new AppVisibilityDetector.AppVisibilityCallback() {
        @Override
        public void onAppGotoForeground() {
            //app is from background to foreground
            Log.e("RunningAppProcessInfo", "app is from background to foreground");
            ModuleApp.IsRunBackground = false;
        }

        @Override
        public void onAppGotoBackground() {
            //app is from foreground to background
            Log.e("RunningAppProcessInfo", "app is from foreground to background");
            ModuleApp.IsRunBackground = true;
            //if(messageListener != null)
            //messageListener.onMessage("Sleep");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (ModuleApp.IsRunBackground && isRunning) {
                        try {
                            //Log.i("Here", "here 1");
                            Thread.sleep(TimeUnit.SECONDS.toMillis(3));

                            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                            if (!pm.isScreenOn()) {

                            }
                            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                                    PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlarmManager");

                            wakeLock.acquire();


                            wakeLock.release();
                            //Log.e("Here", "here 2");
                        } catch (InterruptedException e) {
                            Log.i("WakeLockService", e.getMessage());
                        }
                    }

                }
            }).start();
        }
    };

    public void mShowDialogError(String mess, final IDialogEventError callback) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(View.inflate(getApplicationContext(), R.layout.dialog_error, null));
        TextView editText = dialog.findViewById(R.id.message);
        Button buttonOk = dialog.findViewById(R.id.buttonOk);

        editText.setText(mess);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onOk();
                dialog.dismiss();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog1, int keyCode, KeyEvent mEvent) {

                if (mEvent.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK)) {
                    if (callback != null)
                        callback.onOk();
                    dialog1.dismiss();
                }

                return false;
            }
        });
        dialog.show();

        SoundManager.getInstance().PlayError(getApplicationContext());
    }
}
