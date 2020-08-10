package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.model.ApplicationConfiguration;
import com.delfi.xmobile.app.lecreusetcommunication.model.BarcodeConfig;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;
import com.delfi.xmobile.lib.xcore.xmltojson.JsonToXml;
import com.delfi.xmobile.lib.xcore.xmltojson.XmlToJson;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by USER on 06/14/2019.
 */
public class ConfigIO {

    private ConfigIO() {
    }


    @Nullable
    public static BarcodeConfig readBarcodeConfigXml(@NonNull Context context) {
        File file = new File(context.getFilesDir(), Constant.BARCODE_CONFIGURATION_XML);
        String content = readTextFile(file);

        try {
            XmlToJson xmlToJson = new XmlToJson.Builder(content).build();
            JSONObject jsObj = new JSONObject(xmlToJson.toString());

            return new Gson().fromJson(jsObj.toString(), BarcodeConfig.class);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return null;
    }


    @Nullable
    public static ApplicationConfiguration readAppConfigXml(@NonNull Context context, String rootPath) {
        File file = new File(rootPath, Constant.APPLICATION_CONFIGURATION_XML);
        String content;

        if (file.exists()) {
            content = readTextFile(file);
        } else {
            return null;
        }

        try {
            content = lowercaseTagXml(context, content); //#6302 Autoupdate enabling from ApplicationConfiguration

            XmlToJson xmlToJson = new XmlToJson.Builder(content).build();
            JSONObject jsObj = new JSONObject(xmlToJson.toString());

            ApplicationConfiguration configuration =
                    new Gson().fromJson(jsObj.toString(), ApplicationConfiguration.class);

            return configuration;

        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return null;
    }

    private static String lowercaseTagXml(Context context, String content) throws TransformerException {
        Source xmlSource = new StreamSource(new StringReader(content));
        Source xsltSource = new StreamSource(context.getResources().openRawResource(R.raw.lowercase));
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        StringWriter output = new StringWriter();
        StreamResult result = new StreamResult(output);
        trans.transform(xmlSource, result);

        return output.toString();
    }

    public static boolean createBarcodeConfigXml(@NonNull Context context, BarcodeConfig config) {

        File file = new File(context.getFilesDir(),
                Constant.BARCODE_CONFIGURATION_XML);

        return writeXml(file, config);
    }

    public static String readTextFile(@NonNull File file) {
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            String mEncoding = BinarySearch.getEncodingFile(file.getPath());
            FileInputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, mEncoding);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("\ufeff")) {
                    line = line.replace("\ufeff", "");
                }
                text.append(line);
                text.append('\n');
            }
            br.close();
            isr.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private static boolean writeXml(File file, Object obj) {
        FileOutputStream outputStream = null;

        try {
            String jsString = new Gson().toJson(obj);
            JSONObject jsObj = new JSONObject(jsString);
            JsonToXml jsXml = new JsonToXml.Builder(jsObj).build();

            String text = jsXml.toFormattedString();

            outputStream = new FileOutputStream(file);
            outputStream.write(text.getBytes());
            outputStream.write(String.valueOf("\r\n").getBytes());
            outputStream.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private static String readAssetsFile(@NonNull Context context, String fileName) {
        //Read text from file
        StringBuilder text = new StringBuilder();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName)));

            // do reading, usually loop until end of file reading
            String line;
            while ((line = reader.readLine()) != null) {
                //process line
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return text.toString();
    }

    public static String calculateIPAddress(String storeId, String server){
        if(server == null || server.length() == 0)
            return "";
        if(server.toUpperCase().equals("AUTOCALC")) {
            int mStoreId = Integer.parseInt(storeId);
            StringBuilder sb = new StringBuilder("10").append(".");
            sb.append(((calculateIPOctet(mStoreId)) / 128) + 32).append(".");
            sb.append((((calculateIPOctet(mStoreId)) * 2) % 256)).append(".");
            sb.append("32");
            return sb.toString();
        }
        else
            return server;
    }

    private static int calculateIPOctet(int storeid)
    {
        if (storeid > 699)
        {
            return storeid - 700;
        }
        else
        {
            return storeid;
        }
    }
}
