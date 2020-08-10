package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.delfi.xmobile.app.lecreusetcommunication.ModuleApp;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryInventoryModel;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryOrderModel;
import com.delfi.xmobile.app.lecreusetcommunication.model.HistoryShelfMarkModel;
import com.delfi.xmobile.lib.lecreusetbase.utils.DateTime;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.sqlite.DbHelper;
import com.delfi.xmobile.lib.xcore.sqlite.QueryOption;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by USER on 05/25/2020.
 */
public class HistoryHelper {

    public HistoryHelper() {
    }

    @Nullable
    public static List getItems(Class tableCls, String mFileName) {
        LinkedHashMap clause = new LinkedHashMap();
        clause.put("outputFileName", mFileName);
        try {
            return DbHelper.getInstance(ModuleApp.getInstance())
                    .getListAsObject(tableCls, new QueryOption("outputFileName=?", clause));

        } catch (Exception e) {
            logError(e);
        }

        return null;
    }

    public static void updateInventoryItems(@NonNull List<HistoryInventoryModel> items) {

        for (HistoryInventoryModel history : items) {
            history.sentDate = DateTime.getCurrentTime();
            try {
                DbHelper.getInstance(ModuleApp.getInstance()).update(history);

            } catch (Exception e) {
                logError(e);
            }
        }
    }

    public static void updateOrderItems(@NonNull List<HistoryOrderModel> items) {

        for (HistoryOrderModel history : items) {
            history.sentDate = DateTime.getCurrentTime();
            try {
                DbHelper.getInstance(ModuleApp.getInstance()).update(history);

            } catch (Exception e) {
                logError(e);
            }
        }
    }

    public static void updateShelfMarkItems(@NonNull List<HistoryShelfMarkModel> items) {

        for (HistoryShelfMarkModel history : items) {
            history.sentDate = DateTime.getCurrentTime();
            try {
                DbHelper.getInstance(ModuleApp.getInstance()).update(history);

            } catch (Exception e) {
                logError(e);
            }
        }
    }

    private static void logError(Exception e) {
        e.printStackTrace();
        Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
    }
}
