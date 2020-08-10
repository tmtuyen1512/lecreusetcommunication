package com.delfi.xmobile.app.lecreusetcommunication.view;

import android.content.Intent;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration;
import com.delfi.xmobile.app.lecreusetcommunication.model.BarcodeConfig;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConfigIO;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConstComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DoInsertBase;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.ItemGroupInsert;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.ItemInfoInsert;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.ItemPackageReferenceInsert;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.ItemReferenceInsert;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.PartnerItemCatalogInsert;
import com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils.PartnerItemReferenceInsert;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.lecreusetbase.utils.IScreenEventError;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.BaseActivity;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.common.FullScreenDialog;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.common.settings.ConfigurationObject;
import com.delfi.xmobile.lib.xcore.common.settings.HandleConfiguationIO;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.sqlite.SharedPre;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseReceiveActivity extends BaseActivity {

    protected List<FTPSetting> settingList = new ArrayList<>();
    protected boolean isMidnightReceiveData;

    protected void initReceive() {

        File f = new File(FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML), Constant.APPLICATION_CONFIGURATION_XML.replace("/", ""));
        if (!f.exists()) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_not_found),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }


        BarcodeConfig barcodeConfig = ConfigIO.readBarcodeConfigXml(this);
        if (barcodeConfig != null && barcodeConfig.Config != null
                && barcodeConfig.Config.Generelt != null && barcodeConfig.Config.Generelt.StoreID != null
                && barcodeConfig.Config.Generelt.StoreID.length() > 0) {

        } else {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }


        ApplicationConfiguration AppConfig = ConfigIO.readAppConfigXml(this, FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML));
        if (AppConfig == null || AppConfig.Configuration == null || AppConfig.Configuration.Global == null) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }
        ApplicationConfiguration.Configuration.Global.Databases databases = AppConfig.Configuration.Global.Databases;

        if (databases == null || databases.RDIDatabase == null) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_database_config),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        } else {
            try {
                initRDIDatabase(databases, barcodeConfig.Config.Generelt.StoreID);
            } catch (Exception e) {
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_database_config),
                        new IScreenEventError() {
                            @Override
                            public void onOk() {
                                finish();
                            }
                        });
                return;
            }

        }

        if (databases == null || databases.PartnerDatabase == null) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_partner_database_config),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        } else {
            try {
                initPartnerDatabase(databases, barcodeConfig.Config.Generelt.StoreID);
            } catch (Exception e) {
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_partner_database_config),
                        new IScreenEventError() {
                            @Override
                            public void onOk() {
                                finish();
                            }
                        });
                return;
            }

        }

        doInsertDB();

        ApplicationConfiguration.Configuration.Global.Policies policies = AppConfig.Configuration.Global.Policies;
        if (policies == null) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_policy_config),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        } else {
            try {
                initPolicies(policies, barcodeConfig.Config.Generelt.StoreID);
            } catch (Exception e) {
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_policy_config),
                        new IScreenEventError() {
                            @Override
                            public void onOk() {
                                finish();
                            }
                        });
                return;
            }
        }

        try {
            for (ApplicationConfiguration.Configuration.Menuer.Menu menu : AppConfig.Configuration.Menuer.Menu) {
                if ("Opsamling".equalsIgnoreCase(menu.Name) && menu.IsVisible) {
                    initPickConfig(databases, barcodeConfig.Config.Generelt.StoreID);
                    break;
                }
            }

        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_database_config),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }

        ApplicationConfiguration.Configuration.Global.AppConfig appConfig = AppConfig.Configuration.Global.AppConfig;

        boolean isAutoUpdate = appConfig == null || appConfig.EnableAutoUpdate == null //Default, if the tag is not found
                || appConfig.EnableAutoUpdate;

        if (isAutoUpdate) {
            try {
                initAutoUpdate();
            } catch (Exception e) {
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
                FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_read_autoupdate_config),
                        new IScreenEventError() {
                            @Override
                            public void onOk() {
                                finish();
                            }
                        });
                return;
            }
        }

        FileUtil.deleteAll("/Import/Varekartotek/");

        startCommunicationActivity();
    }

    protected void startCommunicationActivity() {
        Intent intent = new Intent(this, CommunicationActivity.class);
        intent.putExtra(CommunicationActivity.KEY_EXTRA_SETTINGS, (Serializable) settingList);
        intent.putExtra(CommunicationActivity.KEY_EXTRA_TYPE, TypeComm.RECEIVE);
        intent.putExtra(CommunicationActivity.KEY_EXTRA_ISMIDNIGHT_RECEIVEDATA, isMidnightReceiveData);
        startActivity(intent);
        finish();
    }

    protected void initAutoUpdate() {
        FTPSetting ftp = new FTPSetting();
        ConfigurationObject config = HandleConfiguationIO.readConfig(this);

        ftp.ServerIP = config.FTPHost;
        ftp.ServerPort = config.FTPPort;
        ftp.ServerUser = config.FTPUserName;
        ftp.ServerPassword = config.FTPPassword;
        ftp.ServerConfigDirectory = config.FTPRootPath + "/" + SharedManager.getInstance(this).getString(SharedPre.KEY_XMOBILE_APPID);
        ftp.FTPType = config.communicationType;
        try {
            if(com.delfi.xmobile.lib.xcore.BuildConfig.VERSION_CODE >= 20)
                ftp.IsImplicit = config.isImplicitSSL;
        }
        catch (Exception e){

        }
        ftp.DestinationDirectory = "/Import/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo("module_list.json", getResources().getString(R.string.module_list)));
        ftp.CommunicationLabel = getResources().getString(R.string.auto_update);
        ftp.isFTP = true;
        ftp.MainTask = "AutoUpdate";
        settingList.add(ftp);
    }

    protected void initPolicies(ApplicationConfiguration.Configuration.Global.Policies policies, String storeID) {
        FTPSetting ftp = new FTPSetting();
        ftp.ServerIP = ConfigIO.calculateIPAddress(storeID, policies.PolicyServerIp);
        ftp.ServerPort = policies.PolicyServerPort;
        ftp.ServerUser = policies.PolicyServerUser;
        ftp.ServerPassword = policies.PolicyServerPassword;
        ftp.ServerConfigDirectory = policies.PolicyServerConfigDirectory.replace("\\", "/");
        ftp.FTPType = policies.FTPType;
        ftp.IsImplicit = policies.IsImplicit;
        ftp.DestinationDirectory = "/Import/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo(Constant.APPLICATION_CONFIGURATION_XML.replace("/", "")));
        ftp.CommunicationLabel = getResources().getString(R.string.configuration);
        ftp.isFTP = true;
        ftp.MainTask = "Receive_Config";

        settingList.add(ftp);
    }

    protected void initPartnerDatabase(ApplicationConfiguration.Configuration.Global.Databases databases, String storeID) {
        FTPSetting ftp = new FTPSetting();
        ftp.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.PartnerDatabase.DatabaseFilesPrimary);
        ftp.ServerPort = databases.PartnerDatabase.DatabaseFilesPrimaryPort;
        ftp.ServerUser = databases.PartnerDatabase.DatabaseFilesPrimaryUser;
        ftp.ServerPassword = databases.PartnerDatabase.DatabaseFilesPrimaryPassword;
        ftp.ServerConfigDirectory = databases.PartnerDatabase.DatabaseFilesPrimaryDirectory.toLowerCase().replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
        ftp.FTPType = databases.PartnerDatabase.FTPType;
        ftp.IsImplicit = databases.PartnerDatabase.IsImplicit;
        ftp.DestinationDirectory = "/Import/Varekartotek/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.PartnerVarekartotek_Filename));
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.PartnerVareReference_Filename));
        ftp.CommunicationLabel = "PartnerDatabase";
        ftp.isFTP = true;
        ftp.MainTask = "Receive_Data";

        if (databases.PartnerDatabase.DatabaseFilesSecondary == null || databases.PartnerDatabase.DatabaseFilesSecondary.length() == 0) {

        } else {
            FTPSetting ftpBak = new FTPSetting();
            ftpBak.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.PartnerDatabase.DatabaseFilesSecondary);
            ftpBak.ServerPort = databases.PartnerDatabase.DatabaseFilesSecondaryPort;
            ftpBak.ServerUser = databases.PartnerDatabase.DatabaseFilesSecondaryUser;
            ftpBak.ServerPassword = databases.PartnerDatabase.DatabaseFilesSecondaryPassword;
            ftpBak.ServerConfigDirectory = databases.PartnerDatabase.DatabaseFilesSecondaryDirectory.toLowerCase().replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
            ftpBak.FTPType = databases.PartnerDatabase.FTPType;
            ftpBak.IsImplicit = databases.PartnerDatabase.IsImplicit;
            ftpBak.DestinationDirectory = "/Import/Varekartotek/";
            ftpBak.FilesTo = new ArrayList<>();
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.PartnerVarekartotek_Filename));
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.PartnerVareReference_Filename));
            ftpBak.CommunicationLabel = "PartnerDatabase - Backup";
            ftpBak.IsBackupServer = true;
            ftpBak.MainTask = "Receive_Data";
            ftpBak.isFTP = true;
            ftp.SettingsBackup = ftpBak;
        }

        settingList.add(ftp);
    }

    protected void initRDIDatabase(ApplicationConfiguration.Configuration.Global.Databases databases, String storeID) {
        FTPSetting ftp = new FTPSetting();
        ftp.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.RDIDatabase.DatabaseFilesPrimary);
        ftp.ServerPort = databases.RDIDatabase.DatabaseFilesPrimaryPort;
        ftp.ServerUser = databases.RDIDatabase.DatabaseFilesPrimaryUser;
        ftp.ServerPassword = databases.RDIDatabase.DatabaseFilesPrimaryPassword;
        ftp.ServerConfigDirectory = databases.RDIDatabase.DatabaseFilesPrimaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
        ftp.FTPType = databases.RDIDatabase.FTPType;
        ftp.IsImplicit = databases.RDIDatabase.IsImplicit;
        ftp.DestinationDirectory = "/Import/Varekartotek/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.Varegruppe_Filename));
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVarekartotek_Filename));
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVarekolliReference_Filename));
        ftp.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVareReference_Filename));
        ftp.CommunicationLabel = "RDIDatabase";
        ftp.isFTP = true;
        ftp.MainTask = "Receive_Data";

        if (databases.RDIDatabase.DatabaseFilesSecondary == null || databases.RDIDatabase.DatabaseFilesSecondary.length() == 0) {

        } else {
            FTPSetting ftpBak = new FTPSetting();
            ftpBak.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.RDIDatabase.DatabaseFilesSecondary);
            ftpBak.ServerPort = databases.RDIDatabase.DatabaseFilesSecondaryPort;
            ftpBak.ServerUser = databases.RDIDatabase.DatabaseFilesSecondaryUser;
            ftpBak.ServerPassword = databases.RDIDatabase.DatabaseFilesSecondaryPassword;
            ftpBak.ServerConfigDirectory = databases.RDIDatabase.DatabaseFilesSecondaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
            ftpBak.FTPType = databases.RDIDatabase.FTPType;
            ftpBak.IsImplicit = databases.RDIDatabase.IsImplicit;
            ftpBak.DestinationDirectory = "/Import/Varekartotek/";
            ftpBak.FilesTo = new ArrayList<>();
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.Varegruppe_Filename));
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVarekartotek_Filename));
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVarekolliReference_Filename));
            ftpBak.FilesTo.add(new FTPSetting.FilesTo(ConstComm.RDIVareReference_Filename));
            ftpBak.CommunicationLabel = "RDIDatabase - Backup";
            ftpBak.IsBackupServer = true;
            ftpBak.MainTask = "Receive_Data";
            ftpBak.isFTP = true;
            ftp.SettingsBackup = ftpBak;
        }
        settingList.add(ftp);
    }

    protected void initPickConfig(ApplicationConfiguration.Configuration.Global.Databases databases, String storeID) {
        FTPSetting ftp = new FTPSetting();
        ftp.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.RDIDatabase.DatabaseFilesPrimary);
        ftp.ServerPort = databases.RDIDatabase.DatabaseFilesPrimaryPort;
        ftp.ServerUser = databases.RDIDatabase.DatabaseFilesPrimaryUser;
        ftp.ServerPassword = databases.RDIDatabase.DatabaseFilesPrimaryPassword;
        ftp.ServerConfigDirectory = databases.RDIDatabase.DatabaseFilesPrimaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
        ftp.FTPType = databases.RDIDatabase.FTPType;
        ftp.DestinationDirectory = "/Import/";
        ftp.FilesTo = new ArrayList<>();
        ftp.FilesTo.add(new FTPSetting.FilesTo("Pickconfig.xml"));
        ftp.CommunicationLabel = "Pickconfig";
        ftp.isFTP = true;
        ftp.MainTask = "Receive_Data";

        if (databases.RDIDatabase.DatabaseFilesSecondary == null || databases.RDIDatabase.DatabaseFilesSecondary.length() == 0) {

        } else {
            FTPSetting ftpBak = new FTPSetting();
            ftpBak.ServerIP = ConfigIO.calculateIPAddress(storeID, databases.RDIDatabase.DatabaseFilesSecondary);
            ftpBak.ServerPort = databases.RDIDatabase.DatabaseFilesSecondaryPort;
            ftpBak.ServerUser = databases.RDIDatabase.DatabaseFilesSecondaryUser;
            ftpBak.ServerPassword = databases.RDIDatabase.DatabaseFilesSecondaryPassword;
            ftpBak.ServerConfigDirectory = databases.RDIDatabase.DatabaseFilesSecondaryDirectory.replace("\\", "/").replace("$storeid$", storeID == null ? "$storeid$" : storeID);
            ftpBak.FTPType = databases.RDIDatabase.FTPType;
            ftpBak.DestinationDirectory = "/Import/";
            ftpBak.FilesTo = new ArrayList<>();
            ftpBak.FilesTo.add(new FTPSetting.FilesTo("Pickconfig.xml"));
            ftpBak.CommunicationLabel = "Pickconfig - Backup";
            ftpBak.IsBackupServer = true;
            ftpBak.MainTask = "Receive_Data";
            ftpBak.isFTP = true;
            ftp.SettingsBackup = ftpBak;
        }
        settingList.add(ftp);
    }

    protected void doInsertDB() {
        FTPSetting ftp = new FTPSetting();
        ftp.CommunicationLabel = getResources().getString(R.string.saving_catalogs_to_database);
        ftp.isFTP = false;
        ftp.MainTask = "Update_Database";
        ftp.FilesTo = new ArrayList<>();
        ftp.doInsertBases = new ArrayList<>();

        DoInsertBase itemGroup = new ItemGroupInsert();
        DoInsertBase itemInfo = new ItemInfoInsert();
        DoInsertBase itemPackageReference = new ItemPackageReferenceInsert();
        DoInsertBase itemReference = new ItemReferenceInsert();
        DoInsertBase partnerItemCatalog = new PartnerItemCatalogInsert();
        DoInsertBase partnerItemReference = new PartnerItemReferenceInsert();

        ftp.doInsertBases.add(itemGroup);
        ftp.doInsertBases.add(itemInfo);
        ftp.doInsertBases.add(itemPackageReference);
        ftp.doInsertBases.add(itemReference);
        ftp.doInsertBases.add(partnerItemCatalog);
        ftp.doInsertBases.add(partnerItemReference);

        settingList.add(ftp);
    }
}
