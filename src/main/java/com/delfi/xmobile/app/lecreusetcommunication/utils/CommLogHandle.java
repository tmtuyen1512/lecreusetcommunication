package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.os.Build;
import android.os.Environment;

import com.delfi.xmobile.lib.xcore.logger.FileUtils;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import net.wequick.small.Small;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class CommLogHandle {

    private static CommLogHandle instance;

    private static final String LINE_SEPARATOR = "\n";
    private static long logMaxDays = 5;

    private String filename = "CommunicationLog.log";
    private long logMaxSize = 5 * 1024 * 1024; //5MB

    public static CommLogHandle getInstance() {
        if(instance == null)
            instance = new CommLogHandle();
        return instance;
    }

    private CommLogHandle() {

    }

    public void logMessage(LogType type, String label, String message, final Exception e) {

        String fileTimestamps = new SimpleDateFormat("ddMMyyyy", Locale.UK).format(new Date());

        File logDir = new File(Environment.getExternalStorageDirectory() + "/log");
        if (!logDir.exists())
            logDir.mkdir();
        File file = new File(logDir, filename);

        boolean needHeader = false;
        if (file.exists()) {
            if (file.length() == 0) {
                needHeader = true;
            } else if (file.length() > logMaxSize) {

                try {
                    clearLogFile(file.getParent());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                //copy file
                File newFile = new File(logDir, filename.split("\\.")[0] + "_" + fileTimestamps + ".log");
                FileUtils.copyFileUsingStream(file, newFile);
                needHeader = true;
            }
        } else {
            needHeader = true;
        }

        final StringBuilder logReport = new StringBuilder();

        if (needHeader) {
            addFileHeader(file);
        }

        DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ", Locale.UK);
        String current = dateFormatter.format(new Date());

        logReport.append(current).append("  ");
        logReport.append(type.toString()).append("/");
        logReport.append(label).append(": ");
        logReport.append(message);

        //write file
        //Log.i("logMessage", logReport.toString());
        try {
            FileUtils.writeFile(file.getPath(),logReport.toString(), LINE_SEPARATOR, FileUtils.APPEND_BOTTOM);
            if(e != null)
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void addFileHeader(File file) {
        StringBuilder deviceInfo = new StringBuilder();
        deviceInfo.append("Brand: ").append(Build.BRAND).append(LINE_SEPARATOR);
        deviceInfo.append("Device: ").append(Build.DEVICE).append(LINE_SEPARATOR);
        deviceInfo.append("Model: ").append(Build.MODEL).append(LINE_SEPARATOR);
        deviceInfo.append("Id: ").append(Build.ID).append(LINE_SEPARATOR);
        deviceInfo.append("Product: ").append(Build.PRODUCT).append(LINE_SEPARATOR);
        deviceInfo.append("SDK: ").append(Build.VERSION.SDK).append(LINE_SEPARATOR);
        deviceInfo.append("Release: ").append(Build.VERSION.RELEASE).append(LINE_SEPARATOR);
        deviceInfo.append("Incremental: ").append(Build.VERSION.INCREMENTAL).append(LINE_SEPARATOR);
        deviceInfo.append(getVersionLog());

        deviceInfo.append(LINE_SEPARATOR);
        deviceInfo.append(LINE_SEPARATOR);

        FileUtils.writeFile(file.getPath(), deviceInfo.toString(), LINE_SEPARATOR, FileUtils.OVER_WRITE_FILE);
    }

    private String getVersionLog() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("App version:");
        Map<String, Integer> bundleVersionMap = Small.getBundleVersions();
        for (Map.Entry me : bundleVersionMap.entrySet()) {
            String pkg = (String) me.getKey();
            if (pkg != null) {
                String bundleName = pkg.substring(pkg.lastIndexOf(".") + 1);
                stringBuilder.append("\t" + bundleName + "(" + me.getValue() + ")");
            }
        }
        return stringBuilder.toString();
    }

    public void clearLogFile(String logDirectory) throws Exception {
        File logDir = new File(logDirectory);
        File[] listFiles = logDir.listFiles();
        if (logDir.exists() && listFiles != null) {
            for (int i = 0; i < listFiles.length; i++) {
                File aLogFile = listFiles[i];
                if (aLogFile.getName().startsWith(filename.split("\\.")[0])
                        && aLogFile.getName().length() > filename.length()) {
                    aLogFile.delete();
                }

            }
        }

    }
}
