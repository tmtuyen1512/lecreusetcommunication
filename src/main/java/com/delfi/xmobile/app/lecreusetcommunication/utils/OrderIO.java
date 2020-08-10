package com.delfi.xmobile.app.lecreusetcommunication.utils;

import com.delfi.xmobile.app.lecreusetcommunication.model.OrderOutput;
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
public class OrderIO {

    private OrderIO() {
    }

    public static int readOrder(String path) {

        File file = new File(path);

        OrderOutput order = readOrderOutputXml(file);
        try {
            if(order != null)
                return order.RDIDOC.Body.Orders.Order.OrderLines.OrderLine.size();
        }
        catch (Exception e){
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }

    private static OrderOutput readOrderOutputXml(File file) {
        String content = ConfigIO.readTextFile(file);
        content = content.replace("<ns0:RDIDOC xmlns:ns0=\"http://schemas.reitandistribution.dk/Partner/2010/01/Services\">", "<RDIDOC>")
                .replace("</ns0:RDIDOC>", "</RDIDOC>");

        try {
            XmlToJson xmlToJson = new XmlToJson.Builder(content)
                    .forceList("/RDIDOC/Body/Orders/Order/OrderLines/OrderLine")
                    .build();
            JSONObject jsObj = new JSONObject(xmlToJson.toString());

            return new Gson().fromJson(jsObj.toString(), OrderOutput.class);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return null;
    }
}
