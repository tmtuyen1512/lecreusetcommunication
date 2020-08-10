package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.delfi.xmobile.lib.featuremanager.model.XmobileBundle;
import com.delfi.xmobile.lib.featuremanager.utils.Utils;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Created by USER on 05/14/2019.
 */
public class AutoUpdateIO {

    private AutoUpdateIO() {
    }

    @Nullable
    public static List<XmobileBundle> readModuleList() {
        try {
            File file = new File(Constant.IMPORT_PATH, "module_list.json");
            String content = readTextFile(file);
            return Utils.getXmoduleBundle(content);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.DEBUG,
                    "readModuleList: " + e.getMessage(), null));
        }
        return null;
    }

    private static String readTextFile(@NonNull File file) {
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.VERBOSE,
                    "readTextFile: " + e.getMessage(), null));
            e.printStackTrace();
        }
        return text.toString();
    }
}
