package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;


import com.delfi.xmobile.app.lecreusetcommunication.BuildConfig;
import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.lib.featuremanager.model.XmobileBundle;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.google.gson.Gson;

import net.wequick.small.Small;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by USER on 06/10/2020.
 */
public class IntegrationSafetyCheck {

    private Context context;

    public IntegrationSafetyCheck(@NonNull Context context) {
        this.context = context;
    }

    private Context getContext() {
        return context;
    }

    public void doubleCheckLibrariesVersion(@NonNull final IResult callback) {
        final String BASE_PACKAGE = "com.delfi.xmobile.lib.lecreusetbase";
        final String CORE_PACKAGE = "com.delfi.xmobile.lib.xcore";

        final List<XmobileBundle> needUpdateBundleList = new ArrayList<>();
        final StringBuilder needUpdateMessage = new StringBuilder();

        if (getVersionCode(BASE_PACKAGE) < BuildConfig.REQUIRED_BASE) {
            final XmobileBundle latestBase = getLatestVersionOnServer(BASE_PACKAGE);
            needUpdateBundleList.add(getActualLatestBase(latestBase));
            needUpdateMessage.append("REITAN BASE\n");
            needUpdateMessage.append(String.format(Locale.getDefault(), "Minimum version required: %d " +
                            "\nCurrent version on device: %d\n",
                    BuildConfig.REQUIRED_BASE, getVersionCode(BASE_PACKAGE)));
        }

        if (getVersionCode(CORE_PACKAGE) < BuildConfig.REQUIRED_CORE) {
            final XmobileBundle latestCore = getLatestVersionOnServer(CORE_PACKAGE);
            needUpdateBundleList.add(getActualLatestCore(latestCore));
            needUpdateMessage.append("\n");
            needUpdateMessage.append("REITAN CORE\n");
            needUpdateMessage.append(String.format(Locale.getDefault(), "Minimum version required: %d " +
                            "\nCurrent version on device: %d",
                    BuildConfig.REQUIRED_CORE, getVersionCode(CORE_PACKAGE)));
        }

        if (needUpdateBundleList.isEmpty()) {
            callback.onPassed();
            return;
        }

        LibrariesUpdateDialog.cleanAll();
        LibrariesUpdateDialog.showRequired(getContext(), "Upgrade Libraries Required", "The current libraries were outdated and must be updated.\n" +
                "Do you want to get an update?", new LibrariesUpdateDialog.IDialogRequiredLibsListener() {
            @Override
            public void onDetails() {

                LibrariesUpdateDialog.showDetails(getContext(), "Upgrade Libraries Information",
                        needUpdateMessage.toString(), new LibrariesUpdateDialog.IDialogInfoLibsListener() {

                            @Override
                            public void onCancel() {
                                callback.onCanceled();
                            }

                            @Override
                            public void onUpdate() {
                                callback.onDownloadMissing(needUpdateBundleList);
                            }
                        });
            }

            @Override
            public void onUpdate() {
                callback.onDownloadMissing(needUpdateBundleList);
            }
        });
    }

    @NonNull
    private XmobileBundle getActualLatestBase(XmobileBundle latestServer) {
        XmobileBundle latest;

        if (latestServer == null) {

            latest = new XmobileBundle();
            latest.name = "base";
            latest.uri = "base";
            latest.pkg = "com.delfi.xmobile.lib.lecreusetbase";
            latest.moduleType = 10;

            latest.vcode = BuildConfig.REQUIRED_BASE;
            latest.vname = BuildConfig.REQUIRED_BASE_NAME;
            latest.url = BuildConfig.REQUIRED_BASE_URL;

        } else if (latestServer.vcode < BuildConfig.REQUIRED_BASE) {
            Gson gson = new Gson();
            String latestStr = gson.toJson(latestServer);

            latest = gson.fromJson(latestStr, XmobileBundle.class);
            latest.vcode = BuildConfig.REQUIRED_BASE;
            latest.vname = BuildConfig.REQUIRED_BASE_NAME;
            latest.url = BuildConfig.REQUIRED_BASE_URL;

        } else {
            latest = latestServer;
        }

        Timber.d("ActualLatestBase: " + new Gson().toJson(latest));

        return latest;
    }

    @NonNull
    private XmobileBundle getActualLatestCore(XmobileBundle latestServer) {
        XmobileBundle latest;

        if (latestServer == null) {
            latest = new XmobileBundle();
            latest.name = "xcore";
            latest.uri = "xcore";
            latest.pkg = "com.delfi.xmobile.lib.xcore";

            latest.vcode = BuildConfig.REQUIRED_CORE;
            latest.vname = BuildConfig.REQUIRED_CORE_NAME;
            latest.url = BuildConfig.REQUIRED_CORE_URL;

        } else if (latestServer.vcode < BuildConfig.REQUIRED_CORE) {
            Gson gson = new Gson();
            String latestStr = gson.toJson(latestServer);

            latest = gson.fromJson(latestStr, XmobileBundle.class);
            latest.vcode = BuildConfig.REQUIRED_CORE;
            latest.vname = BuildConfig.REQUIRED_CORE_NAME;
            latest.url = BuildConfig.REQUIRED_CORE_URL;

        } else {
            latest = latestServer;
        }

        Timber.d("ActualLatestCore: " + new Gson().toJson(latest));

        return latest;
    }

    public interface IResult {
        void onPassed();

        void onCanceled();

        void onDownloadMissing(List<XmobileBundle> bundleList);
    }

    @Nullable
    private XmobileBundle getLatestVersionOnServer(@NonNull String packageName) {

        try {
            List<XmobileBundle> bundles = AutoUpdateIO.readModuleList();
            if (bundles != null) {
                for (XmobileBundle bundle : bundles) {

                    if (packageName.equals(bundle.pkg)) {
                        return getLatestVersionOnServer(bundle);
                    }
                }
            }

        } catch (Exception e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG, e.getMessage(), e));
        }

        return null;
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

    protected int getVersionCode(String bundleName) {
        try {
            return Small.getBundle(bundleName).getVersionCode();
        } catch (Exception e) {
            return 0;
        }
    }

    private static class LibrariesUpdateDialog {
        private static Dialog mRequiredInstance;
        private static Dialog mDetailsInstance;

        static void showRequired(Context context, String title, String message, final IDialogRequiredLibsListener callback) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(View.inflate(context.getApplicationContext(), R.layout.dialog_required_libs, null));

            Button btnDetails = dialog.findViewById(R.id.btnDetails);
            Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

            TextView tvTitle = dialog.findViewById(R.id.header);
            TextView tvMsg = dialog.findViewById(R.id.message);

            tvTitle.setText(title);
            tvMsg.setText(message);

            btnDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) callback.onDetails();
                    dialog.dismiss();
                }
            });
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) callback.onUpdate();
                    dialog.dismiss();
                }
            });

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return true;
                }
            });

            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawableResource(android.R.color.transparent);
            }
            mRequiredInstance = dialog;
            dialog.show();
        }

        static void showDetails(Context context, String title, String message, final IDialogInfoLibsListener callback) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(View.inflate(context.getApplicationContext(), R.layout.dialog_info_libs, null));

            Button btnCancel = dialog.findViewById(R.id.btnCancel);
            Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

            TextView tvTitle = dialog.findViewById(R.id.header);
            TextView tvMsg = dialog.findViewById(R.id.message);

            tvTitle.setText(title);
            tvMsg.setText(message);

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) callback.onCancel();
                    dialog.dismiss();
                }
            });
            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null) callback.onUpdate();
                    dialog.dismiss();
                }
            });

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return true;
                }
            });

            Window w = dialog.getWindow();
            if (w != null) {
                w.setBackgroundDrawableResource(android.R.color.transparent);
            }
            mDetailsInstance = dialog;
            dialog.show();
        }

        static synchronized void cleanAll() {

            if (mRequiredInstance != null) {
                mRequiredInstance.dismiss();
                mRequiredInstance = null;
            }

            if (mDetailsInstance != null) {
                mDetailsInstance.dismiss();
                mDetailsInstance = null;
            }
        }

        public interface IDialogRequiredLibsListener {
            void onDetails();

            void onUpdate();
        }

        public interface IDialogInfoLibsListener {
            void onCancel();

            void onUpdate();
        }
    }
}
