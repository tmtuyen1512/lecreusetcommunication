package com.delfi.xmobile.app.lecreusetcommunication.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration;
import com.delfi.xmobile.app.lecreusetcommunication.model.BarcodeConfig;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.TypeComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConfigIO;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.lecreusetbase.utils.IScreenEventError;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.BaseActivity;
import com.delfi.xmobile.lib.lecreusetbase.view.ui.common.FullScreenDialog;
import com.delfi.xmobile.lib.xcore.common.DeviceApplicationInformation;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendCommActivity extends BaseActivity {

    public static final String InventoryDone = "InventoryDone";
    public static final String OrderDone = "OrderDone";
    public static final String ShelfMarkDone = "ShelfMarkDone";
    public static final String PickupDone = "PickupDone";

    public static final String SEND_DATA_MENU = "SyncSendData_MenuKey";
    public static final String SEND_DATA_FILE = "SyncSendData_FilePath";

    private static final int REQUEST_COMMUNICATION_SEND = 301;

    @BindView(R.id.btnInventory) LinearLayout btnInventory;
    @BindView(R.id.btnOrder) LinearLayout btnOrder;
    @BindView(R.id.btnPickup) LinearLayout btnPickup;
    @BindView(R.id.btnShelfmark) LinearLayout btnShelfmark;
    @BindView(R.id.btnSendAll) Button btnSendAll;
    @BindView(R.id.tvInventoryCount) TextView tvInventoryCount;
    @BindView(R.id.tvOrderCount) TextView tvOrderCount;
    @BindView(R.id.tvPickCount) TextView tvPickCount;
    @BindView(R.id.tvShelfmarkCount) TextView tvShelfmarkCount;

    private List<FTPSetting> settingList = new ArrayList<>();

    private String mSpecificMenu;
    private String mSpecificFile;
    private boolean isSendImmediatelyFeature;
    private boolean isHoldMenuToSendFeature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_comm);
        ButterKnife.bind(this);

        mSpecificMenu = getIntent().getStringExtra(SEND_DATA_MENU);
        mSpecificFile = getIntent().getStringExtra(SEND_DATA_FILE);
        isSendImmediatelyFeature = (!TextUtils.isEmpty(mSpecificMenu) && !TextUtils.isEmpty(mSpecificFile));
        isHoldMenuToSendFeature = (!TextUtils.isEmpty(mSpecificMenu) && TextUtils.isEmpty(mSpecificFile));

        try {
            ApplicationConfiguration AppConfig = ConfigIO.readAppConfigXml(this, FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML));
            if (AppConfig == null || AppConfig.Configuration == null || AppConfig.Configuration.Global == null || AppConfig.Configuration.Menuer == null) {
                FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                        new IScreenEventError() {
                            @Override
                            public void onOk() {
                                finish();
                            }
                        });
                return;
            }

            for (ApplicationConfiguration.Configuration.Menuer.Menu menu : AppConfig.Configuration.Menuer.Menu) {
                if ("Status".equalsIgnoreCase(menu.Name) && menu.IsVisible) {
                    btnInventory.setVisibility(View.VISIBLE);

                } else if ("Ordre".equalsIgnoreCase(menu.Name) && menu.IsVisible) {
                    btnOrder.setVisibility(View.VISIBLE);

                } else if ("Opsamling".equalsIgnoreCase(menu.Name) && menu.IsVisible) {
                    btnPickup.setVisibility(View.VISIBLE);

                } else if ("Hyldeforkanter".equalsIgnoreCase(menu.Name) && menu.IsVisible) {
                    btnShelfmark.setVisibility(View.VISIBLE);
                }
            }

        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }

        btnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingList.clear();
                if(readFilesCount(InventoryDone) == 0){
                    Toast.makeText(SendCommActivity.this, getResources().getString(R.string.no_files_to_upload), Toast.LENGTH_LONG).show();
                    return;
                }
                initConfig(new String[] {"Status"} );
            }
        });
        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingList.clear();
                int order = readFilesCount(OrderDone);
                if(order == 0){
                    Toast.makeText(SendCommActivity.this, getResources().getString(R.string.no_files_to_upload), Toast.LENGTH_LONG).show();
                    return;
                }
                initConfig(new String[] {"Ordre"});
            }
        });

        btnPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingList.clear();
                if(readFilesCount(PickupDone) == 0){
                    Toast.makeText(SendCommActivity.this, getResources().getString(R.string.no_files_to_upload), Toast.LENGTH_LONG).show();
                    return;
                }
                initConfig(new String[] {"Opsamling"} );
            }
        });

        btnShelfmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingList.clear();
                if(readFilesCount(ShelfMarkDone) == 0){
                    Toast.makeText(SendCommActivity.this, getResources().getString(R.string.no_files_to_upload), Toast.LENGTH_LONG).show();
                    return;
                }
                initConfig(new String[] {"Hyldeforkanter"} );
            }
        });

        btnSendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingList.clear();
                int inv = readFilesCount(InventoryDone);
                int order = readFilesCount(OrderDone);
                int shelf = readFilesCount(ShelfMarkDone);
                int pick = readFilesCount(PickupDone);
                if(inv == 0 && order == 0 && shelf == 0 && pick == 0){
                    Toast.makeText(SendCommActivity.this, getResources().getString(R.string.no_files_to_upload), Toast.LENGTH_LONG).show();
                    return;
                }
                List<String> arrs = new ArrayList<>();
                if(inv > 0)
                    arrs.add("Status");
                if(order > 0)
                    arrs.add("Ordre");
                if(pick > 0)
                    arrs.add("Opsamling");
                if(shelf > 0)
                    arrs.add("Hyldeforkanter");
                initConfig(arrs.toArray(new String[0]) );
            }
        });

        if (isSendImmediatelyFeature || isHoldMenuToSendFeature){
            initConfig(new String[] {mSpecificMenu});
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        prepareCounter();
    }

    private void prepareCounter() {

        int invCount = readFilesCount(InventoryDone);
        tvInventoryCount.setText(String.valueOf(invCount));
        if(invCount > 0){
            tvInventoryCount.setBackground(getResources().getDrawable(R.drawable.drawable_red_box));
            tvInventoryCount.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            tvInventoryCount.setBackground(getResources().getDrawable(R.drawable.drawable_lightgrey_box));
            tvInventoryCount.setTextColor(getResources().getColor(R.color.dark_grey));
        }

        int orderCount = readFilesCount(OrderDone);
        tvOrderCount.setText(String.valueOf(orderCount));
        if(orderCount > 0){
            tvOrderCount.setText(String.valueOf(orderCount));
            tvOrderCount.setBackground(getResources().getDrawable(R.drawable.drawable_red_box));
            tvOrderCount.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            tvOrderCount.setBackground(getResources().getDrawable(R.drawable.drawable_lightgrey_box));
            tvOrderCount.setTextColor(getResources().getColor(R.color.dark_grey));
        }

        int pickCount = readFilesCount(PickupDone);
        tvPickCount.setText(String.valueOf(pickCount));
        if(pickCount > 0){
            tvPickCount.setBackground(getResources().getDrawable(R.drawable.drawable_red_box));
            tvPickCount.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            tvPickCount.setBackground(getResources().getDrawable(R.drawable.drawable_lightgrey_box));
            tvPickCount.setTextColor(getResources().getColor(R.color.dark_grey));
        }

        int shelfmarkCount = readFilesCount(ShelfMarkDone);
        tvShelfmarkCount.setText(String.valueOf(shelfmarkCount));
        if(shelfmarkCount > 0){
            tvShelfmarkCount.setBackground(getResources().getDrawable(R.drawable.drawable_red_box));
            tvShelfmarkCount.setTextColor(getResources().getColor(R.color.white));
        }
        else {
            tvShelfmarkCount.setBackground(getResources().getDrawable(R.drawable.drawable_lightgrey_box));
            tvShelfmarkCount.setTextColor(getResources().getColor(R.color.dark_grey));
        }
    }

    private void initConfig(String[] menus) {
        ApplicationConfiguration AppConfig = ConfigIO.readAppConfigXml(this, FileUtil.getRootPath(this, Constant.APPLICATION_CONFIGURATION_XML));
        if (AppConfig == null || AppConfig.Configuration == null || AppConfig.Configuration.Global == null || AppConfig.Configuration.Menuer == null) {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }

        ApplicationConfiguration.Configuration.Global.FTPServers ftpServers = AppConfig.Configuration.Global.FTPServers;
        ApplicationConfiguration.Configuration.Menuer menuer = AppConfig.Configuration.Menuer;

        if(ftpServers == null || ftpServers.FTPServer == null || ftpServers.FTPServer.size() == 0 ||
                menuer == null || menuer.Menu == null || menuer.Menu.size() == 0){
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
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

        }
        else {
            FullScreenDialog.getInstance().showError(this, getResources().getString(R.string.error_file_app_config_read_error),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
            return;
        }

        for(String menu : menus){
            for (ApplicationConfiguration.Configuration.Menuer.Menu m : menuer.Menu){
                if(menu.toLowerCase().equals(m.Name.toLowerCase())) {
                    initConfigMenu(m, ftpServers.FTPServer, barcodeConfig.Config.Generelt.StoreID);
                }
            }

        }
        if(settingList.size() > 0) {
            Intent intent = new Intent(this, CommunicationActivity.class);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_SETTINGS, (Serializable) settingList);
            intent.putExtra(CommunicationActivity.KEY_EXTRA_TYPE, TypeComm.SEND);
            startActivityForResult(intent, REQUEST_COMMUNICATION_SEND);
        }
    }

    private void initConfigMenu(ApplicationConfiguration.Configuration.Menuer.Menu menu, List<ApplicationConfiguration.Configuration.Global.FTPServers.FTPServer> ftpServer, String storeID) {
        FTPSetting ftp = new FTPSetting();
        ftp.isFTP = true;
        ftp.MainTask = "Send_Data";
        for(ApplicationConfiguration.Configuration.Global.FTPServers.FTPServer f : ftpServer){
            if(f.Key.equals(menu.FTPSentServer)){
                ftp.ServerIP = ConfigIO.calculateIPAddress(storeID, f.Server);
                ftp.ServerPort = f.Port;
                ftp.ServerUser = f.User;
                ftp.ServerPassword = f.Password;
                ftp.ServerConfigDirectory = menu.FTPSentDirectory.replace("\\", "/");
                ftp.FTPType = f.FTPType;
                ftp.IsImplicit = f.IsImplicit;
                ftp.FilesTo = new ArrayList<>();

                ftp.CommunicationLabel = menu.Name;
                if(menu.Name.equals("Status"))
                    ftp.DestinationDirectory = InventoryDone;
                if(menu.Name.equals("Ordre"))
                    ftp.DestinationDirectory = OrderDone;
                if(menu.Name.equals("Hyldeforkanter"))
                    ftp.DestinationDirectory = ShelfMarkDone;
                if(menu.Name.equals("Opsamling"))
                    ftp.DestinationDirectory = PickupDone;

                if (isSendImmediatelyFeature){
                    ftp.FilesTo.addAll(getFiles(mSpecificFile));
                } else {
                    ftp.FilesTo.addAll(getListFiles(ftp.DestinationDirectory));
                }
            }
            if(f.Key.equals(menu.FTPSentServer_Backup)){
                FTPSetting ftpBak = new FTPSetting();
                ftpBak.ServerIP = ConfigIO.calculateIPAddress(storeID, f.Server);
                ftpBak.ServerPort = f.Port;
                ftpBak.ServerUser = f.User;
                ftpBak.ServerPassword = f.Password;
                ftpBak.ServerConfigDirectory = menu.FTPSentDirectory_Backup.replace("\\", "/");
                ftpBak.FTPType = f.FTPType;
                ftpBak.IsImplicit = f.IsImplicit;
                ftpBak.FilesTo = new ArrayList<>();

                ftpBak.CommunicationLabel = menu.Name + " " + getResources().getString(R.string.backup);
                if(menu.Name.equals("Status"))
                    ftpBak.DestinationDirectory = InventoryDone;
                if(menu.Name.equals("Ordre"))
                    ftpBak.DestinationDirectory = OrderDone;
                if(menu.Name.equals("Hyldeforkanter"))
                    ftpBak.DestinationDirectory = ShelfMarkDone;
                if(menu.Name.equals("Opsamling"))
                    ftpBak.DestinationDirectory = PickupDone;


                if (isSendImmediatelyFeature){
                    ftpBak.FilesTo.addAll(getFiles(mSpecificFile));
                } else {
                    ftpBak.FilesTo.addAll(getListFiles(ftpBak.DestinationDirectory));
                }
                ftpBak.isFTP = true;

                ftp.SettingsBackup = ftpBak;
            }
        }
        if((ftp.ServerIP == null || ftp.ServerIP.length() == 0)&& ftp.SettingsBackup == null){
            FullScreenDialog.getInstance().showError(this, String.format(getResources().getString(R.string.error_read_ftp_send), menu.Name),
                    new IScreenEventError() {
                        @Override
                        public void onOk() {
                            finish();
                        }
                    });
        }
        else
            settingList.add(ftp);
    }

    private List<FTPSetting.FilesTo> getListFiles(String directory) {
        List<FTPSetting.FilesTo> items = new ArrayList<>();
        try {
            ArrayList<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + directory));
            for(String filePath : filesToUpload){
                File f = new File(filePath);
                items.add(new FTPSetting.FilesTo(f.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return items;
    }

    private List<FTPSetting.FilesTo> getFiles(String filePath) {
        List<FTPSetting.FilesTo> items = new ArrayList<>();
        try {
            File f = new File(filePath);
            items.add(new FTPSetting.FilesTo(f.getName()));

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return items;
    }

    private int readFilesCount(String path){
        return FileUtil.readFilesCount(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_COMMUNICATION_SEND){
            //FileUtil.cleanFolder(Constant.EXPORT_PATH + "/PickupImage");

            if (isSendImmediatelyFeature || isHoldMenuToSendFeature){
                finish();
            }
        }
    }
}
