package com.delfi.xmobile.app.lecreusetcommunication.utils.dbutils;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.delfi.xmobile.app.lecreusetcommunication.R;
import com.delfi.xmobile.app.lecreusetcommunication.utils.CommLogHandle;
import com.delfi.xmobile.app.lecreusetcommunication.utils.ConstComm;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DBUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.DoInsertBase;
import com.delfi.xmobile.app.lecreusetcommunication.utils.FileUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.LogType;
import com.delfi.xmobile.lib.xcore.logger.LogEventArgs;
import com.delfi.xmobile.lib.xcore.logger.LogLevel;
import com.delfi.xmobile.lib.xcore.logger.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class ItemReferenceInsert extends DoInsertBase implements Serializable {

    public ItemReferenceInsert() {
        fileName = ConstComm.RDIVareReference_Filename;
        tableName = DBUtil.DBItemReference.class.getSimpleName();
        FIX_RECORD_LENGTH = 50;
    }

    @Override
    public void addDBItem(SQLiteDatabase db, File f){
        try {

            CommLogHandle.getInstance().logMessage(LogType.I, label,"Populating " + fileName + " with " + totalRecords + " items", null);

            for(int i = 0; i< arr.size(); i++) {
                db.beginTransaction();

                doDBItemReference(db, i, arr.get(i), 0, 99);

                db.setTransactionSuccessful();
                db.endTransaction();
            }
            if(errorLines.size() == 0)
                dbListener.onDoneDetail(view);
            else {
                isSuccess = false;
                dbListener.onErrorDetail(view, context.getResources().getString(R.string.invalid_format_line));
                CommLogHandle.getInstance().logMessage(LogType.I, label, tableName + " - Invalid format lines: " + errorLines.toString(), null);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            dbListener.onErrorDetail(view,"Error: " + e.getMessage());
            db.endTransaction();
            isSuccess = false;
            Logger.getInstance().logMessage(new LogEventArgs(LogLevel.ERROR, tableName + ": " + e.getMessage(), e));
        }
    }

    private void doDBItemReference(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doDBItemReference(db, index, arr, to + 1, to + 100);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == FIX_RECORD_LENGTH) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?)");
            }
            else {
                errorLines.add(index * FileUtil.BLOCK + i + 1);
            }
        }
        if(valuesBuilder.length() == 0){
            errorLines.add(0);
            dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
            return;
        }

        SQLiteStatement stmt = db.compileStatement(
                "Insert into "+tableName+"(EAN, itemNumber, colliSize) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);

            if(s.length == FIX_RECORD_LENGTH) {
                bindValue(stmt,k + 1, s, 0, 20);
                bindValue(stmt,k + 2, s, 20, 20);
                bindValue(stmt,k + 3, s, 40, 10);

                k += 3;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }
}
