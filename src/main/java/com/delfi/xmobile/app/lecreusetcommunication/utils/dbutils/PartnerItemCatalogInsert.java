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

public class PartnerItemCatalogInsert extends DoInsertBase implements Serializable {

    public PartnerItemCatalogInsert() {
        fileName = ConstComm.PartnerVarekartotek_Filename;
        tableName = DBUtil.DBPartnerItemCatalog.class.getSimpleName();
        FIX_RECORD_LENGTH = 113;
    }

    @Override
    public void addDBItem(SQLiteDatabase db, File f) {
        try {

            CommLogHandle.getInstance().logMessage(LogType.I, label,"Populating " + fileName + " with " + totalRecords + " items", null);

            for(int i = 0; i< arr.size(); i++) {
                db.beginTransaction();
                if(arr.get(0).get(0).length == 144) //not cal 2 timestamp column
                    doInsertDBPartnerItemCatalog_e4(db, i, arr.get(i), 0, 49);
                else if(arr.get(0).get(0).length == 131)
                    doInsertDBPartnerItemCatalog_e3(db, i, arr.get(i), 0, 49);
                else if(arr.get(0).get(0).length == 128)
                    doInsertDBPartnerItemCatalog_e2(db, i, arr.get(i), 0, 49);
                else if(arr.get(0).get(0).length == 121)
                    doInsertDBPartnerItemCatalog_e1(db, i, arr.get(i), 0, 49);
                else if(arr.get(0).get(0).length == 111)
                    doInsertDBPartnerItemCatalog_e0(db, i, arr.get(i), 0, 49);
                else
                    errorLines.add(1);

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

    private void doInsertDBPartnerItemCatalog_e4(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doInsertDBPartnerItemCatalog_e4(db, index, arr, to + 1, to + 50);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;
        //Log.e("doInsertDBItemPackageReference", from + " -> " + to);

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == 144) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
                "Insert into "+tableName+"(itemNumber, description, secondaryPrice, expectedQtyInStock, average_Week1, average_Week2, average_Week3, average_Week4, average_Week5, average_Week6, average_Week7, average_Week8, dayOrWeek, profit, nameOfDay, timestamp, timestamp2) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);
            if(s.length == 144) {
                bindValue(stmt, k + 1, s, 0, 20);    //itemNumber
                bindValue(stmt, k + 2, s, 20, 30);   //description
                bindValue(stmt, k + 3, s, 50, 10);   //secondaryPrice
                bindValue(stmt, k + 4, s, 60, 8);    //expectedQtyInStock
                bindValue(stmt, k + 5, s, 68, 5);    //average_Week1
                bindValue(stmt, k + 6, s, 73, 5);    //average_Week2
                bindValue(stmt, k + 7, s, 78, 5);    //average_Week3
                bindValue(stmt, k + 8, s, 83, 5);    //average_Week4
                bindValue(stmt, k + 9, s, 88, 5);    //average_Week5
                bindValue(stmt, k + 10, s, 93, 5);   //average_Week6
                bindValue(stmt, k + 11, s, 98, 5);   //average_Week7
                bindValue(stmt, k + 12, s, 103, 5);  //average_Week8
                bindValue(stmt, k + 13, s, 108, 6);  //dayOrWeek
                bindValue(stmt, k + 14, s, 114, 10); //profit
                bindValue(stmt, k + 15, s, 124, 10); //nameOfDay
                bindValue(stmt, k + 16, s, 134, 5);  //timestamp1
                bindValue(stmt, k + 17, s, 139, 5);  //timestamp2

                k += 17;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }

    private void doInsertDBPartnerItemCatalog_e3(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doInsertDBPartnerItemCatalog_e3(db, index, arr, to + 1, to + 50);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;
        //Log.e("doInsertDBItemPackageReference", from + " -> " + to);

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == 131) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
                "Insert into "+tableName+"(itemNumber, description, secondaryPrice, expectedQtyInStock, average_Week1, average_Week2, average_Week3, average_Week4, average_Week5, average_Week6, average_Week7, average_Week8, dayOrWeek, profit, timestamp, timestamp2) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);

            if(s.length == 131) {
                bindValue(stmt, k + 1, s, 0, 20);    //itemNumber
                bindValue(stmt, k + 2, s, 20, 30);   //description
                bindValue(stmt, k + 3, s, 50, 10);   //secondaryPrice
                bindValue(stmt, k + 4, s, 60, 8);    //expectedQtyInStock
                bindValue(stmt, k + 5, s, 68, 5);    //average_Week1
                bindValue(stmt, k + 6, s, 73, 5);    //average_Week2
                bindValue(stmt, k + 7, s, 78, 5);    //average_Week3
                bindValue(stmt, k + 8, s, 83, 5);    //average_Week4
                bindValue(stmt, k + 9, s, 88, 5);    //average_Week5
                bindValue(stmt, k + 10, s, 93, 5);   //average_Week6
                bindValue(stmt, k + 11, s, 98, 5);   //average_Week7
                bindValue(stmt, k + 12, s, 103, 5);  //average_Week8
                bindValue(stmt, k + 13, s, 108, 3);  //dayOrWeek
                bindValue(stmt, k + 14, s, 111, 10); //profit
                bindValue(stmt, k + 15, s, 121, 5);  //timestamp1
                bindValue(stmt, k + 16, s, 126, 5);  //timestamp2

                k += 16;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }

    private void doInsertDBPartnerItemCatalog_e2(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doInsertDBPartnerItemCatalog_e2(db, index, arr, to + 1, to + 50);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;
        //Log.e("doInsertDBItemPackageReference", from + " -> " + to);

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == 128) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            else  {
                errorLines.add(index * FileUtil.BLOCK + i + 1);
            }
        }

        if(valuesBuilder.length() == 0){
            errorLines.add(0);
            dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
            return;
        }

        SQLiteStatement stmt = db.compileStatement(
                "Insert into "+tableName+"(itemNumber, description, secondaryPrice, expectedQtyInStock, average_Week1, average_Week2, average_Week3, average_Week4, average_Week5, average_Week6, average_Week7, average_Week8, profit, timestamp, timestamp2) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);

            if(s.length == 128) {
                bindValue(stmt, k + 1, s, 0, 20);    //itemNumber
                bindValue(stmt, k + 2, s, 20, 30);   //description
                bindValue(stmt, k + 3, s, 50, 10);   //secondaryPrice
                bindValue(stmt, k + 4, s, 60, 8);    //expectedQtyInStock
                bindValue(stmt, k + 5, s, 68, 5);    //average_Week1
                bindValue(stmt, k + 6, s, 73, 5);    //average_Week2
                bindValue(stmt, k + 7, s, 78, 5);    //average_Week3
                bindValue(stmt, k + 8, s, 83, 5);    //average_Week4
                bindValue(stmt, k + 9, s, 88, 5);    //average_Week5
                bindValue(stmt, k + 10, s, 93, 5);   //average_Week6
                bindValue(stmt, k + 11, s, 98, 5);   //average_Week7
                bindValue(stmt, k + 12, s, 103, 5);  //average_Week8
                bindValue(stmt, k + 13, s, 108, 10); //profit
                bindValue(stmt, k + 14, s, 118, 5);  //timestamp1
                bindValue(stmt, k + 15, s, 123, 5);  //timestamp2

                k += 15;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }

    private void doInsertDBPartnerItemCatalog_e1(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doInsertDBPartnerItemCatalog_e1(db, index, arr, to + 1, to + 50);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;
        //Log.e("doInsertDBItemPackageReference", from + " -> " + to);

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == 121) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            else  {
                errorLines.add(index * FileUtil.BLOCK + i + 1);
            }
        }

        if(valuesBuilder.length() == 0){
            errorLines.add(0);
            dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
            return;
        }

        SQLiteStatement stmt = db.compileStatement(
                "Insert into "+tableName+"(itemNumber, description, secondaryPrice, expectedQtyInStock, average_Week1, average_Week2, average_Week3, average_Week4, average_Week5, average_Week6, average_Week7, average_Week8, dayOrWeek, timestamp, timestamp2) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);

            if(s.length == 121) {
                bindValue(stmt, k + 1, s, 0, 20);    //itemNumber
                bindValue(stmt, k + 2, s, 20, 30);   //description
                bindValue(stmt, k + 3, s, 50, 10);   //secondaryPrice
                bindValue(stmt, k + 4, s, 60, 8);    //expectedQtyInStock
                bindValue(stmt, k + 5, s, 68, 5);    //average_Week1
                bindValue(stmt, k + 6, s, 73, 5);    //average_Week2
                bindValue(stmt, k + 7, s, 78, 5);    //average_Week3
                bindValue(stmt, k + 8, s, 83, 5);    //average_Week4
                bindValue(stmt, k + 9, s, 88, 5);    //average_Week5
                bindValue(stmt, k + 10, s, 93, 5);   //average_Week6
                bindValue(stmt, k + 11, s, 98, 5);   //average_Week7
                bindValue(stmt, k + 12, s, 103, 5);  //average_Week8
                bindValue(stmt, k + 13, s, 108, 3);  //dayOrWeek
                bindValue(stmt, k + 14, s, 111, 5);  //timestamp1
                bindValue(stmt, k + 15, s, 116, 5);  //timestamp2

                k += 15;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }

    private void doInsertDBPartnerItemCatalog_e0(SQLiteDatabase db, int index, ArrayList<char[]> arr, int from, int to) {
        if (to < arr.size() - 1) {
            doInsertDBPartnerItemCatalog_e0(db, index, arr, to + 1, to + 50);
        }

        if(to > arr.size() - 1)
            to = arr.size() - 1;
        //Log.e("doInsertDBItemPackageReference", from + " -> " + to);

        StringBuilder valuesBuilder = new StringBuilder();
        for (int i = from; i <= to; i++) {
            if(arr.get(i).length == 111) {
                if (valuesBuilder.length() != 0) {
                    valuesBuilder.append(", ");
                }
                valuesBuilder.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            }
            else  {
                errorLines.add(index * FileUtil.BLOCK + i + 1);
            }
        }

        if(valuesBuilder.length() == 0){
            errorLines.add(0);
            dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
            return;
        }

        SQLiteStatement stmt = db.compileStatement(
                "Insert into "+tableName+"(itemNumber, description, secondaryPrice, expectedQtyInStock, average_Week1, average_Week2, average_Week3, average_Week4, average_Week5, average_Week6, average_Week7, average_Week8, dayOrWeek) VALUES "
                        + valuesBuilder.toString()
        );
        int k = 0;
        for (int i = from; i <= to; i++) {
            char[] s = arr.get(i);

            if(s.length == 111) {
                bindValue(stmt, k + 1, s, 0, 20);    //itemNumber
                bindValue(stmt, k + 2, s, 20, 30);   //description
                bindValue(stmt, k + 3, s, 50, 10);   //secondaryPrice
                bindValue(stmt, k + 4, s, 60, 8);    //expectedQtyInStock
                bindValue(stmt, k + 5, s, 68, 5);    //average_Week1
                bindValue(stmt, k + 6, s, 73, 5);    //average_Week2
                bindValue(stmt, k + 7, s, 78, 5);    //average_Week3
                bindValue(stmt, k + 8, s, 83, 5);    //average_Week4
                bindValue(stmt, k + 9, s, 88, 5);    //average_Week5
                bindValue(stmt, k + 10, s, 93, 5);   //average_Week6
                bindValue(stmt, k + 11, s, 98, 5);   //average_Week7
                bindValue(stmt, k + 12, s, 103, 5);  //average_Week8
                bindValue(stmt, k + 13, s, 108, 3);  //dayOrWeek

                k += 13;
                inserted += 1;
            }
        }

        stmt.executeInsert();
        stmt.clearBindings();

        dbListener.onProgressDetail(view, String.format(context.getResources().getString(R.string.line_progress), inserted, totalRecords), (int)((float)inserted/(float) totalRecords * 100));
    }
}
