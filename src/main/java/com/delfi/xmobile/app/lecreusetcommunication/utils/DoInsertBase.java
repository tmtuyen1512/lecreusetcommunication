package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.view.View;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class DoInsertBase implements Serializable {

    protected Context context;
    protected SyncDBListener dbListener;
    public View view;
    public String fileName;
    protected String tableName;
    protected boolean isSuccess;
    protected List<Integer> errorLines;
    protected String label = "Update_Database [Saving catalogs to database]";
    protected int inserted;
    protected int totalRecords;
    protected ArrayList<ArrayList<char[]>> arr;
    protected File file;
    protected int FIX_RECORD_LENGTH;
    protected StringBuilder errorLog;
    public boolean isSameFile;

    protected SQLiteDatabase db;

    public void init(Context context, SQLiteDatabase db, SyncDBListener mdbListener){
        this.context = context;
        this.db = db;
        this.dbListener = mdbListener;

        arr = readRecords();
        totalRecords = 0;
        inserted = 0;
        for(int i = 0; i< arr.size(); i++) {
            totalRecords += arr.get(i).size();
        }

        dbListener.initView(view, totalRecords);

    }

    public void doInsert(){

        dbListener.onStartDetail(view);
        errorLog = new StringBuilder();
        errorLines = new ArrayList<>();
        CommLogHandle.getInstance().logMessage(LogType.I, label, "Retrieving items from " + fileName, null);

        if(arr != null && arr.size() > 0){
            isSuccess = true;

            db.delete(tableName, null, null);
            db.execSQL("UPDATE sqlite_sequence set seq = 0 where name = '"+tableName+"'");

            addDBItem(db, file);
            if(errorLog.length() > 0)
                Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, errorLog.toString(), null));
            file.delete();
            arr = null;
        }
        else {
            dbListener.onErrorDetail(view, context.getResources().getString(R.string.file_not_found));
            CommLogHandle.getInstance().logMessage(LogType.I, label, fileName + " not found", null);
            isSuccess = false;
        }
    }

    protected abstract void addDBItem(SQLiteDatabase db, File f);

    private ArrayList<ArrayList<char[]>> readRecords(){
        file = new File(FileUtil.getRootPath(context, fileName) + "/" + fileName);
        try {
            if(file != null && file.exists())
                return FileUtil.getInstance().readListChar(file);
            else
                return new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, "readRecords: " + file.getName() + ": " + e.getMessage(), e));
        }
        return new ArrayList<>();
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public List<Integer> getErrorLines() {
        return errorLines;
    }

    protected void bindValue(SQLiteStatement stmt, int index, char[] line, int offset, int count){
        try {
            if(offset + count <= line.length)
                stmt.bindString(index, String.valueOf(line, offset, count).trim());
            else
                stmt.bindString(index, "");
        }
        catch (Exception e){
            stmt.bindString(index, "");
            //e.printStackTrace();
            errorLog.append(fileName+ ": " + e.getMessage() + "\n");
        }
    }

    public void release() {
        file = null;
        arr = null;
    }
}
