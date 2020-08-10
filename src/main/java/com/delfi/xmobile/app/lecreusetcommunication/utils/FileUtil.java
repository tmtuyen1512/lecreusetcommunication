package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.delfi.xmobile.app.lecreusetcommunication.ModuleApp;
import com.delfi.xmobile.app.lecreusetcommunication.view.SendCommActivity;
import com.delfi.xmobile.lib.lecreusetbase.utils.Constant;
import com.delfi.xmobile.lib.xcore.common.DeviceApplicationInformation;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by hdadmin on 2/7/2017.
 */

public class FileUtil implements FileFilter {

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */

    public  static  final String TAG = FileUtil.class.getSimpleName();
    private static final String[] okFileExtensions =  new String[] {"jpg", "png", "gif","jpeg"};
    public static final String USER_BEEPS_PATH = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ScannerBeep");
    private static FileUtil instance ;
    public static String exported = "";
    public static String imported = "";
    //private static final String ENCODING = "windows-1252";

    public static final int BLOCK = 50000;

    public enum MODE{
        APPEND,
        OVERWRITE
    }

    public static FileUtil getInstance(){
        if (instance == null){
            instance = new FileUtil();
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }


    @Override
    public boolean accept(File file) {
        return false;
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().replace(":","");
            }
        } catch (Exception ex) {
        }
        return "020000000000";
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public void write(File file, String text, MODE mode) throws IOException {
        if(mode == MODE.OVERWRITE) {
            write(file, text);
        }
        else {
            String mEncoding = BinarySearch.getEncodingFile(file.getPath());
            append(file, text, mEncoding);

        }
    }

    public void write(File file, String[] text, MODE mode, String mEncoding) throws IOException {
        if(text.length == 0){
            delete(file);
            return;
        }

        if(mode == MODE.OVERWRITE) {
            write(file, text[0]);
            for (int i = 1; i < text.length; i++) {
                append(file, text[i], mEncoding);
            }
        }
        else {
            for (int i = 0; i < text.length; i++) {
                append(file, text[i], mEncoding);
            }
        }
    }

    public String[] read(File file) throws IOException {
        ArrayList<String> list = readArr(file);
        return list.toArray(new String[list.size()]);
    }

    public ArrayList<String> readArr(File file) throws IOException {
        ArrayList<String> text = new ArrayList<String>();
        if(!file.exists())
            return text;
        FileInputStream inputStream;
        try {
            String mEncoding = BinarySearch.getEncodingFile(file.getPath());
            inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream, mEncoding);
            BufferedReader bufferedReader = new BufferedReader(isr);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("\ufeff")) {
                    line = line.replace("\ufeff", "");
                }
                text.add(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return text;
    }

    private boolean delete(File file){
        try {
            return file.delete();
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private void write(File file, String text) throws IOException {
        FileOutputStream outputStream = null;
        try {
            addFolder(file);
            outputStream = new FileOutputStream(file);
            /*outputStream.write(text.getBytes());
            String line = "\r\n";
            outputStream.write(line.getBytes());
            outputStream.close();*/
            String mEncoding = BinarySearch.getEncodingFile(file.getPath());

            Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, mEncoding));
            out.write(text + "\r\n");

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean addFolder(File file){
        File f = new File(file.getParent());
        if(!f.exists())
            return f.mkdirs();
        else
            return true;
    }

    private void append(File file, String text, String mEncoding) throws IOException {
        FileOutputStream outputStream;
        try {
            addFolder(file);
            outputStream = new FileOutputStream(file, true);
            /*outputStream.write(text.getBytes());
            String line = "\r\n";
            outputStream.write(line.getBytes());
            outputStream.close();*/


            Writer out = new BufferedWriter(new OutputStreamWriter(outputStream, mEncoding));
            out.append(text);
            out.append("\r\n");
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void move(File from, File to) throws Exception {
        String[] arr1 = read(from);
        String mEncoding = BinarySearch.getEncodingFile(from.getPath());
        write(to, arr1, MODE.OVERWRITE, mEncoding);
        delete(from);
    }

    public ArrayList<ArrayList<char[]>> readListChar(File file) throws IOException {
        ArrayList<ArrayList<char[]>> values = new ArrayList<>();
        if(!file.exists())
            return values;
        FileInputStream inputStream;
        String charset = BinarySearch.getEncodingFile(file.getPath());
        try {
            inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream, charset);
            BufferedReader bufferedReader = new BufferedReader(isr);
            //char[] cbuf = new char[lengthLine];
            int k = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
            //while (bufferedReader.read(cbuf) != -1) {

                if(k == 0 || k % BLOCK == 0)
                    values.add(new ArrayList<char[]>());

                values.get(values.size() - 1).add(line.toCharArray());
                //values.get(values.size() - 1).add(line.replaceAll("(\\r|\\n)", "").toCharArray());
                k++;

            }

            bufferedReader.close();
            isr.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return values;
    }

    public ArrayList<ArrayList<byte[]>> readListByte(File file, int lengthLine) throws IOException {
        ArrayList<ArrayList<byte[]>> values = new ArrayList<>();
        if(!file.exists())
            return values;
        byte[] bytes;
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);

            int k = 0;
            while((k + 1) * lengthLine < bytes.length){
                if(k == 0 || k % BLOCK == 0)
                    values.add(new ArrayList<byte[]>());
                values.get(values.size() - 1).add(Arrays.copyOfRange(bytes, k * lengthLine, k*lengthLine + lengthLine));
                k++;
                //Log.i("ItemNumber", new String(values.get(values.size() - 1).get(values.get(values.size() - 1).size() - 1), "windows-1252"));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return values;
    }

    public static String getRootPath(Context context, String fileName){
        fileName = fileName.replace("/", "");
        String path = SharedManager.getInstance(context).getString(fileName);
        if(path != null && path.length() > 0)
            return path;
        return Constant.IMPORT_PATH + "/";
    }

    public static int readFilesCount(String path){
        try {
            ArrayList<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + path));
            return filesToUpload.size();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }

    public static void deleteAll(String path){
        try {
            ArrayList<String> files = DeviceApplicationInformation.getLocalFilesDir(new File(Environment.getExternalStorageDirectory() + "/" + path));
            for (String f : files){
                File file = new File(f);
                if(file.exists())
                    file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
    }

    public static int readRecordInventory(){
        try {
            ArrayList<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + SendCommActivity.InventoryDone));
            int count = 0;
            for(String file : filesToUpload)
                count += InventoryIO.readInventory(file);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }
    public static int readRecordOrder(){
        try {
            ArrayList<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + SendCommActivity.OrderDone));
            int count = 0;
            for(String file : filesToUpload)
                count += OrderIO.readOrder(file);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }


    public static int readRecordPickup() {
        try {
            List<String> filesToUpload = DeviceApplicationInformation.getLocalFilesDir(new File(Constant.EXPORT_PATH + "/" + SendCommActivity.PickupDone));
            int count = 0;
            for(String path : filesToUpload) {
                String filename = path.substring(path.lastIndexOf("/") + 1);
                Context context = ModuleApp.getInstance().getApplicationContext();
                Map<String, Integer> map = SharedManager.getInstance(context).getMap("files_pickup_done");
                count += map.get(filename);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, e.getMessage(), e));
        }
        return 0;
    }

    public static void cleanFolder(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) return;

            for (String child : children) {
                new File(dir, child).delete();
            }
        }
    }
}
