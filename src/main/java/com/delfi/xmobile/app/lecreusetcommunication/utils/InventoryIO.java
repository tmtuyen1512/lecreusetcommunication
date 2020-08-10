package com.delfi.xmobile.app.lecreusetcommunication.utils;

import com.delfi.xmobile.app.lecreusetcommunication.model.InventoryOutput;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.xmltojson.XmlToJson;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by USER on 05/14/2019.
 */
public class InventoryIO {

    private InventoryIO() {
    }

    public static int readInventory(String path) {

        File file = new File(path);

        InventoryOutput order = readOrderOutputXml(file);
        try {
            if(order != null)
                return order.RDIDOC.Body.InventoryStatus.Status.StatusLines.StatusLine.size();
        }
        catch (Exception e){
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }

    private static InventoryOutput readOrderOutputXml(File file) {
        String content = ConfigIO.readTextFile(file);
        content = content.replace("<ns0:RDIDOC xmlns:ns0=\"http://schemas.reitandistribution.dk/Partner/2010/01/Services\">", "<RDIDOC>")
                .replace("</ns0:RDIDOC>", "</RDIDOC>");

        try {
            XmlToJson xmlToJson = new XmlToJson.Builder(content)
                    .forceList("/RDIDOC/Body/InventoryStatus/Status/StatusLines/StatusLine")
                    .build();
            JSONObject jsObj = new JSONObject(xmlToJson.toString());

            return new Gson().fromJson(jsObj.toString(), InventoryOutput.class);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return null;
    }
}
