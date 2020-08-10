package com.delfi.xmobile.app.lecreusetcommunication.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.delfi.xmobile.app.lecreusetcommunication.BuildConfig;
import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration;
import com.delfi.xmobile.app.lecreusetcommunication.model.BarcodeConfig;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConfigIO;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConstComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DBUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.IntegrationSafetyCheck;
import com.delfi.xmobile.app.lecreusetcommunication.utils.SimpleTransferListener;
import com.delfi.xmobile.lib.featuremanager.FeatureManager;
import com.delfi.xmobile.lib.featuremanager.UpgradeManager;
import com.delfi.xmobile.lib.featuremanager.model.XmobileBundle;
import com.delfi.xmobile.lib.featuremanager.utils.AppConfig;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.lecreusetbase.utils.IDialogEventError;
import com.delfi.xmobile.lib.lecreusetbase.utils.IDialogEventInfo;
import com.delfi.xmobile.lib.lecreusetbase.utils.IScreenEventError;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.BaseActivity;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.common.FullScreenDialog;
import com.delfi.xmobile.lib.xcore.common.DeviceApplicationInformation;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.common.settings.ConfigurationObject;
import com.delfi.xmobile.lib.xcore.common.settings.HandleConfiguationIO;
import com.delfi.xmobile.lib.xcore.communication.ftpcom.communication.XFTPConnection;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.sqlite.DbHelper;
import com.delfi.xmobile.lib.xcore.sqlite.QueryOption;
import com.delfi.xmobile.lib.xcore.sqlite.SharedPre;
import com.google.gson.Gson;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_MULTIPLE_PERMISSIONS = 0;
    private static final int REQUEST_CODE_COMMUNICATION = 1;
    private static final int REQUEST_ACTION_LOCATION_SOURCE_SETTINGS = 3;
    private static final int REQUEST_SYNC_DATA = 4;

    private boolean isReceiveConfig;
    private boolean isChangeAppID;
    private boolean isNewStoreId;
    private boolean isMidnightReceiveData;

    private boolean isSyncSendData;
    private boolean isSyncReceiveData;

    @BindView(R.id.llContent) RelativeLayout llContent;
    @BindView(R.id.btnSend) LinearLayout btnSend;
    @BindView(R.id.btnReceive) LinearLayout btnReceive;
    @BindView(R.id.btnRenewIP) LinearLayout btnRenewIP;
    @BindView(R.id.tvLastSync) TextView tvLastSync;

    @BindView(R.id.tvSendCount) TextView tvSendCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        llContent.setVisibility(View.GONE);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SendCommActivity.class);
                intent.putExtra(SendCommActivity.SEND_DATA_MENU, getIntent().getStringExtra(SendCommActivity.SEND_DATA_MENU));
                intent.putExtra(SendCommActivity.SEND_DATA_FILE, getIntent().getStringExtra(SendCommActivity.SEND_DATA_FILE));
                startActivityForResult(intent, REQUEST_SYNC_DATA);
            }
        });

        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReceiveCommActivity.class);
                startActivityForResult(intent, REQUEST_SYNC_DATA);
            }
        });

        /*btnRenewIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("RenewIP", NetworkUtil.renewDeviceIP(MainActivity.this) + "");
            }
        });*/

        //Set default to false
        isReceiveConfig = getIntent().getBooleanExtra("ReceiveConfig", false);
        isNewStoreId = getIntent().getBooleanExtra("NewStoreId", false);
        isChangeAppID = getIntent().getBooleanExtra("ChangeAppID", false);
        isMidnightReceiveData = getIntent().getBooleanExtra("MidnightReceiveData", false);

        isSyncSendData = getIntent().getBooleanExtra("SyncSendData", false);
        isSyncReceiveData = getIntent().getBooleanExtra("SyncReceiveData", false);

        if(checkPermission())
            checkSafeIntegration();
    }

    private void prepareCounter() {
        GetCounterTask task = new GetCounterTask();
        task.execute();

        long lastSync = SharedManager.getInstance(this).getLong(ConstComm.LastTimeSync, 0);
        if (lastSync != 0) {
            long current = System.currentTimeMillis();
            Date from = new Date(lastSync);
            Date to = new Date(current);
            long diff = to.getTime() - from.getTime();
            long diffMinutes = diff / (60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            long diffDays = diff / (24 * 60 * 60 * 1000);
            long diffWeek = diff / (7 * 24 * 60 * 60 * 1000);
            if (diffWeek > 0)
                tvLastSync.setText(String.format(getResources().getString(R.string.last_sync_week), diffWeek, diffWeek > 1 ? "s" : ""));
            else if (diffDays > 0)
                tvLastSync.setText(String.format(getResources().getString(R.string.last_sync_day), diffDays, diffDays > 1 ? "s" : ""));
            else if (diffHours > 0)
                tvLastSync.setText(String.format(getResources().getString(R.string.last_sync_hour), diffHours, diffHours > 1 ? "s" : ""));
            else if (diffMinutes > 0)
                tvLastSync.setText(String.format(getResources().getString(R.string.last_sync_minute), diffMinutes, diffMinutes > 1 ? "s" : ""));
            else
                tvLastSync.setText(String.format(getResources().getString(R.string.last_sync_second), diff / 1000));
        } else
            tvLastSync.setText(R.string.last_sync);
    }

    @SuppressLint("StaticFieldLeak")
    class GetCounterTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            mShowProgressDialog(true);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            int inventoryCount = FileUtil.readRecordInventory();
            int orderCount = FileUtil.readRecordOrder() + FileUtil.readFilesCount(SendCommActivity.ShelfMarkDone);
            int pickCount = FileUtil.readRecordPickup();

            return inventoryCount + orderCount + pickCount;
        }

        @Override
        protected void onPostExecute(Integer count) {
            mShowProgressDialog(false);

            if (count > 0) {
                tvSendCount.setText(String.format(getResources().getString(R.string.record_ready_send), count, count > 1 ? getResources().getString(R.string.multi) : getResources().getString(R.string.single)));
                tvSendCount.setTextColor(getResources().getColor(R.color.error));
            } else {
                tvSendCount.setText(R.string.all_record_has_been_sent);
                tvSendCount.setTextColor(getResources().getColor(R.color.label));
            }
        }
    }

    private void checkSafeIntegration() {
        IntegrationSafetyCheck safetyCheck = new IntegrationSafetyCheck(this);
        safetyCheck.doubleCheckLibrariesVersion(new IntegrationSafetyCheck.IResult() {
            @Override
            public void onPassed() {
                initConfig();
            }

            @Override
            public void onCanceled() {
                finish();
            }

            @Override
            public void onDownloadMissing(List<XmobileBundle> bundleList) {
                new DownloadMissingModelAsync(bundleList).execute();
            }
        });
        revertToOriginalAppId();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadMissingModelAsync extends AsyncTask<Void, Void, Boolean> {
        List<XmobileBundle> bundleList;
        boolean useBackupServer = false;

        public DownloadMissingModelAsync(List<XmobileBundle> bundleList) {
            this.bundleList = bundleList;
        }

        public DownloadMissingModelAsync(List<XmobileBundle> bundleList, boolean useBackupServer) {
            this.bundleList = bundleList;
            this.useBackupServer = useBackupServer;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mShowProgressDialog(true, getResources().getString(R.string.downloading));

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (bundleList == null || bundleList.size() == 0) {
                return false;
            }
            boolean isSuccess = true;
            try {
                String strSetup = new Gson().toJson(bundleList);
                Timber.w("strSetup: " + strSetup);

                ConfigurationObject config;
                if (useBackupServer) {
                    config = initBackupFtpConfig();
                    Timber.d("UseBackupServer: " + new Gson().toJson(config));

                } else {
                    config = HandleConfiguationIO.readConfig(MainActivity.this);
                    if (config == null) {
                        config = AppConfig.initDefaultFtpConfig(MainActivity.this);
                    }
                    Timber.i("UsePrimaryServer: " + new Gson().toJson(config));
                }

                XFTPConnection xftpConnection = new XFTPConnection(MainActivity.this, strSetup, config, new SimpleTransferListener(MainActivity.this));
                xftpConnection.startCommunication(MainActivity.this);
                Timber.w("Download finished      [Success]");
            } catch (Exception e) {
                e.printStackTrace();
                Timber.e("\nDownload Failure     [ERROR!]");
                isSuccess = false;
            }
            return isSuccess;
        }


        @NonNull
        private ConfigurationObject initBackupFtpConfig() {
            ConfigurationObject config = new ConfigurationObject();
            config.communicationType = "FTP";
            config.FTPHost = "delfivn.com";
            config.FTPPort = "21212";
            config.FTPUserName = "delfi_transfer";
            config.FTPPassword = "70222555";
            config.FTPRootPath = "/reitan/Production";
            config.versionName = BuildConfig.VERSION_NAME;
            config.autoCloseAfterTransfer = false;

            //change appId to backup appId on Backup server
            changeToBackupAppId();

            return config;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            mShowProgressDialog(false);

            if (useBackupServer) { //revert to original appId
                revertToOriginalAppId();
            }

            if (isSuccess) {
                FeatureManager.upgradeBundle(MainActivity.this, bundleList, new UpgradeManager.OnUpgradeListener() {
                    @Override
                    public void onUpgradeFinish(boolean succeed) {
                        mShowProgressDialog(false);
                        if (succeed) {
                            UpgradeManager.restartApp(MainActivity.this);

                        } else if (!useBackupServer) { //try in backup
                            new DownloadMissingModelAsync(bundleList, true).execute();

                        } else {
                            mShowDialogError(getString(R.string.msg_download_module_fail), new IDialogEventError() {
                                @Override
                                public void onOk() {
                                    finish();
                                }
                            });
                        }
                    }

                    @Override
                    public void onStatusChange(XmobileBundle bundle, String status) {
                        Log.i("TAG", "onStatusChange: status " + status);
                    }
                });
            } else {

                if (!useBackupServer) { //try in backup
                    new DownloadMissingModelAsync(bundleList, true).execute();

                } else {
                    mShowDialogError(getString(R.string.msg_download_module_fail), new IDialogEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
                }
            }
        }
    }

    private void changeToBackupAppId() {
        SharedPre pre = new SharedPre(this);
        pre.putString("original_key_xmobile_appid", pre.getString(SharedPre.KEY_XMOBILE_APPID));
        pre.putString(SharedPre.KEY_XMOBILE_APPID, "2124c03a-4118-d6c1-7bac-b3fde0120260");
    }

    private void revertToOriginalAppId() {
        SharedPre pre = new SharedPre(this);
        String currAppId = pre.getString(SharedPre.KEY_XMOBILE_APPID);
        String original = pre.getString("original_key_xmobile_appid");

        if ("2124c03a-4118-d6c1-7bac-b3fde0120260".equals(currAppId) && !TextUtils.isEmpty(original)) {
            pre.putString(SharedPre.KEY_XMOBILE_APPID, original);
            pre.putString("original_key_xmobile_appid", "");
        }
    }

    private void initConfig() {
        setLanguage();

        if (isReceiveConfig) {

            if (isNewStoreId) {
                clearDatabaseOnNewStoreId();
            }

            initReceiveConfig();

        } else if (isChangeAppID) {
            initChangeAppID();

        } else if (isMidnightReceiveData) {
            initMidnightReceiveData();

        } else if (isSyncSendData) {
            btnSend.performClick();

        } else if (isSyncReceiveData) {
            btnReceive.performClick();

        } else {
            llContent.setVisibility(View.VISIBLE);
            prepareCounter();
        }
    }

    private void clearDatabaseOnNewStoreId() {
        try {
            DbHelper.getInstance(this).delete(DBUtil.DBItemGroup.class, new QueryOption());
            DbHelper.getInstance(this).delete(DBUtil.DBItemInfo.class, new QueryOption());
            DbHelper.getInstance(this).delete(DBUtil.DBItemReference.class, new QueryOption());
            DbHelper.getInstance(this).delete(DBUtil.DBItemPackageReference.class, new QueryOption());
            DbHelper.getInstance(this).delete(DBUtil.DBPartnerItemCatalog.class, new QueryOption());
            DbHelper.getInstance(this).delete(DBUtil.DBPartnerItemReference.class, new QueryOption());

            //delete file info to prevent skip samefile
            File fileInfoPath = DeviceApplicationInformation.getFileInfoPath(this);
            fileInfoPath.delete();


        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.WARNING, "Could not clear database before receiving data " + e.getMessage(), null));
            e.printStackTrace();
        }
    }

    private void initReceiveConfig() {
        File file = new File(getFilesDir(), Constant.BARCODE_CONFIGURATION_XML);
        if (!file.exists()) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_retain_config_not_found),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }
        BarcodeConfig config = ConfigIO.readBarcodeConfigXml(this);
        if (config == null || config.Config.Generelt == null || config.Config.Generelt.StoreID == null) {
            Log.e("BarcodeConfig", "Read error");
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_retain_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
        } else {
            final FTPSetting ftp = new FTPSetting();
            ftp.ServerIP = ConfigIO.calculateIPAddress(config.Config.Generelt.StoreID, config.Config.Generelt.PolicyServerIp);
            ftp.ServerPort = config.Config.Generelt.PolicyServerPort;
            ftp.ServerUser = config.Config.Generelt.PolicyServerUser;
            ftp.ServerPassword = config.Config.Generelt.PolicyServerPassword;
            ftp.ServerConfigDirectory = config.Config.Generelt.PolicyServerConfigDirectory.replace("\\", "/");
            ftp.FTPType = config.Config.Generelt.FTPType;
            ftp.IsImplicit = config.Config.Generelt.IsImplicit;
            ftp.DestinationDirectory = "/Import/";
            ftp.FilesTo = new ArrayList<>();
            ftp.FilesTo.add(new FTPSetting.FilesTo(Constant.APPLICATION_CONFIGURATION_XML.replace("/", "")));

            ftp.IsPolicyConfig = true;
            ftp.CommunicationLabel = getResources().getString(R.string.configuration); //remember rename FTPComm receiveFilesFromServer function
            ftp.isFTP = true;
            ftp.MainTask = "Receive_Config";

            List<FTPSetting> settingList = new ArrayList<FTPSetting>() {
                {
                    add(ftp);
                }
            };

            ApplicationConfiguration AppConfig = ConfigIO.readAppConfigXml(this, FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML));
            if (AppConfig != null && AppConfig.Configuration != null && AppConfig.Configuration.Global != null && AppConfig.Configuration.Global.Databases != null  && AppConfig.Configuration.Global.Databases.RDIDatabase != null) {
                FTPSetting pick = new FTPSetting();
                ApplicationConfiguration.Configuration.Global.Databases.RDIDatabase databases = AppConfig.Configuration.Global.Databases.RDIDatabase;
                String storeID = config.Config.Generelt.StoreID;
                pick.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.DatabaseFilesPrimary);
                pick.ServerPort = databases.DatabaseFilesPrimaryPort;
                pick.ServerUser = databases.DatabaseFilesPrimaryUser;
                pick.ServerPassword = databases.DatabaseFilesPrimaryPassword;
                pick.ServerConfigDirectory = databases.DatabaseFilesPrimaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
                pick.FTPType = databases.FTPType;
                pick.DestinationDirectory = "/Import/";
                pick.FilesTo = new ArrayList<>();
                pick.FilesTo.add(new FTPSetting.FilesTo("Pickconfig.xml"));
                pick.CommunicationLabel = "Pickconfig";
                pick.isFTP = true;
                pick.MainTask = "Receive_Data";

                if (databases.DatabaseFilesSecondary == null || databases.DatabaseFilesSecondary.length() == 0) {

                } else {
                    FTPSetting ftpBak = new FTPSetting();
                    ftpBak.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.DatabaseFilesSecondary);
                    ftpBak.ServerPort = databases.DatabaseFilesSecondaryPort;
                    ftpBak.ServerUser = databases.DatabaseFilesSecondaryUser;
                    ftpBak.ServerPassword = databases.DatabaseFilesSecondaryPassword;
                    ftpBak.ServerConfigDirectory = databases.DatabaseFilesSecondaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
                    ftpBak.FTPType = databases.FTPType;
                    ftpBak.DestinationDirectory = "/Import/";
                    ftpBak.FilesTo = new ArrayList<>();
                    ftpBak.FilesTo.add(new FTPSetting.FilesTo("Pickconfig.xml"));
                    ftpBak.CommunicationLabel = "Pickconfig - Backup";
                    ftpBak.IsBackupServer = true;
                    ftpBak.MainTask = "Receive_Data";
                    ftpBak.isFTP = true;
                    pick.SettingsBackup = ftpBak;
                }
                settingList.add(pick);
            }


            FileUtil.deleteAll("/Import/Varekartotek/");

            Intent intent = new Intent(this, CommunicationActivity.class);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_SETTINGS, (Serializable) settingList);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_TYPE, TypeComm.RECEIVE);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_ISRECEIVECONFIG, true);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_ISNEWSTOREID, isNewStoreId);
            startActivityForResult(intent, REQUEST_CODE_COMMUNICATION);
        }
    }

    private void initChangeAppID() {
        List<FTPSetting> settingList = new ArrayList<>();
        FTPSetting ftp = new FTPSetting();
        ConfigurationObject config = HandleConfiguationIO.readConfig(this);

        ftp.ServerIP = config.FTPHost;
        ftp.ServerPort = config.FTPPort;
        ftp.ServerUser = config.FTPUserName;
        ftp.ServerPassword = config.FTPPassword;
        ftp.ServerConfigDirectory = config.FTPRootPath + "/" + SharedManager.getInstance(this).getString(SharedPre.KEY_XMOBILE_APPID);
        ftp.DestinationDirectory = "/Import/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo("module_list.json", getResources().getString(R.string.module_list)));
        ftp.CommunicationLabel = getResources().getString(R.string.auto_update);
        ftp.isFTP = true;
        ftp.MainTask = "AutoUpdate";

        settingList.add(ftp);

        Intent intent = new Intent(this, CommunicationActivity.class);
        intent.putExtra(CommunicationActivity.KEY_EXTRA_SETTINGS, (Serializable) settingList);
        intent.putExtra(CommunicationActivity.KEY_EXTRA_TYPE, TypeComm.RECEIVE);
        startActivity(intent);
        finish();
    }

    private void initMidnightReceiveData() {
        Intent intent = new Intent(MainActivity.this, MidnightReceiveActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLanguage() {
        String lang = SharedManager.getInstance(getBaseContext()).getString(Constant.Language);
        Log.e("setLanguage", lang);
        if (lang == null || lang.length() == 0) {
            lang = "da";
            SharedManager.getInstance(getBaseContext()).putString(Constant.Language, lang);
        }
        Locale locale = new Locale(lang);
        Configuration config = new Configuration();
        config.locale = locale;
        float fontScale = SharedManager.getInstance(getBaseContext()).getFloat("Font-Scale");
        if (fontScale != 0) {
            config.fontScale = fontScale;
        }
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        mCurrentLocale = locale;
    }

    private boolean checkPermission() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!network_enabled) {

            mShowDialogInfo(getResources().getString(R.string.msg_turn_on_location), getResources().getString(R.string.go_to_settings), new IDialogEventInfo() {
                @Override
                public void onOk() {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(myIntent, REQUEST_ACTION_LOCATION_SOURCE_SETTINGS);
                }
            });
            return false;
        }

        List<String> permissions = new ArrayList<>();
        int write_external_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_external_permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int access_network = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        int internet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int phone_state = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int ACCESS_WIFI_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int CHANGE_WIFI_STATE = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int WAKE_LOCK = ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK);
        int ACCESS_COARSE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (write_external_permission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (read_external_permission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (access_network != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (internet != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }
        if (phone_state != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ACCESS_WIFI_STATE != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (CHANGE_WIFI_STATE != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        }
        if (WAKE_LOCK != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WAKE_LOCK);
        }
        if (ACCESS_COARSE_LOCATION != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MULTIPLE_PERMISSIONS:
                if (checkPermission()) {
                    checkSafeIntegration();
                } else {
                    finish();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_COMMUNICATION && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, ReceiveCommActivity.class);
            startActivity(intent);
            finish();
        } else if (requestCode == REQUEST_ACTION_LOCATION_SOURCE_SETTINGS) {
            if (checkPermission())
                checkSafeIntegration();
            return;
        } else if (requestCode == REQUEST_SYNC_DATA) {
            if (isSyncSendData || isSyncReceiveData) {
                finish();
            }
        } else
            finish();
    }
}
