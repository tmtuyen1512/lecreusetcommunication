package com.delfi.xmobile.app.lecreusetcommunication.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.delfi.xmobile.app.lecreusetcommunication.BuildConfig;
import com.delfi.xmobile.app.lecreusetcommunication.model.FTPSetting;
import com.delfi.xmobile.app.lecreusetcommunication.model.InventoryModel;
import com.delfi.xmobile.lib.xcore.common.SharedManager;
import com.delfi.xmobile.lib.xcore.sqlite.DbCore;
import com.delfi.xmobile.lib.xcore.sqlite.DbHelper;
import com.delfi.xmobile.lib.xcore.sqlite.SharedPre;

import java.util.ArrayList;
import java.util.List;


public class DBUtil {

    private final SyncDBListener messageListener;
    private final Context context;
    private int total = 0;
    private SQLiteDatabase db;
    private FTPSetting ftp;

    private List<Class> entries = new ArrayList<Class>() {{
        add(DBItemGroup.class);
        add(DBItemInfo.class);
        add(DBItemReference.class);
        add(DBItemPackageReference.class);
        add(DBPartnerItemCatalog.class);
        add(DBPartnerItemReference.class);
        add(InventoryModel.class);
    }};


    public DBUtil(Context context, FTPSetting ftp, SyncDBListener messageListener){
        this.context = context;
        SharedPre config = new SharedPre(context);
        String db_name = config.getString("___db_name___");
        int db_version = config.getInt("___db_version___");
        int module_version = config.getInt(BuildConfig.APPLICATION_ID);

        if(db_name == null || db_name.length() == 0)
            db_name = "reitan.db";
        if(db_version == 0)
            db_version = 1;
        if(module_version < BuildConfig.VERSION_CODE) {
            db_version += 1;
            config.putInt(BuildConfig.APPLICATION_ID, BuildConfig.VERSION_CODE);
        }
        new DbHelper(context, db_name, db_version, entries);
        DbCore core = new DbCore(context, db_name, db_version, entries);
        db = core.getWritableDatabase();
        this.messageListener = messageListener;
        this.ftp = ftp;
    }

    public void startSync(){

        messageListener.onStart(ftp._View);
        CommLogHandle.getInstance().logMessage(LogType.I, ftp.MainTask + " [" + ftp.CommunicationLabel + "]","Buiding database", null);
        boolean error = false;

        for(DoInsertBase doInsertBase : ftp.doInsertBases){
            doInsertBase.release();
            if(SharedManager.getInstance(context).getBoolean(doInsertBase.fileName + "_SAMEFILE"))
                this.messageListener.onHideTask(doInsertBase.view);
        }
        for(DoInsertBase doInsertBase : ftp.doInsertBases){
            if(!SharedManager.getInstance(context).getBoolean(doInsertBase.fileName + "_SAMEFILE"))
                doInsertBase.init(this.context, this.db, this.messageListener);
        }
        messageListener.initViewDone(ftp._View);
        for(DoInsertBase doInsertBase : ftp.doInsertBases){
            if(!SharedManager.getInstance(context).getBoolean(doInsertBase.fileName + "_SAMEFILE")) {
                doInsertBase.doInsert();
                if (!doInsertBase.isSuccess)
                    error = true;
            }
            else {
                doInsertBase.isSameFile = true;
            }
        }

        CommLogHandle.getInstance().logMessage(LogType.I, ftp.MainTask + " [" + ftp.CommunicationLabel + "]","Completed buiding database", null);
        messageListener.onDone(ftp._View, error);

        //setMessage("Shrinking database", true);
        //db.execSQL("VACUUM;");
        db.close();

        Log.e("Sync", "End thread");

    }

    /**
     * Varegruppe.dat
     * ItemGroup[20] Description[30]
     */
    public class DBItemGroup {

        public int Id;
        public String itemGroup;
        public String itemGroupLabel;
    }

    /**
     * RDIVareKartotek.dat
     * ItemNumber[20] ItemDescription[30] ItemPrice [10]
     */
    public class DBItemInfo {

        public int Id;
        public String itemNumber;
        public String itemDescription;
        public String itemPrice;
    }

    /**
     * RDIVareReference.dat
     * EAN[20] ItemNumber[20] ColliSize[10]
     */
    public class DBItemReference {

        public int Id;
        public String EAN;
        public String itemNumber;
        public String colliSize;

    }

    /**
     * RDIVareKolliReference.dat
     * ItemNumber[20] ColliSize[10] EAN [20] IsStandard [1]
     */
    public class DBItemPackageReference {

        public int Id;
        public String EAN;
        public String itemNumber;
        public String colliSize;
        public String isStandard;
    }

    /**
     * PartnerVareKartotek.dat
     * 4 editions
     */
    public class DBPartnerItemCatalog{
        public int Id;
        public String itemNumber;
        public String description;
        public String secondaryPrice;
        public float expectedQtyInStock;
        public String average_Week1;
        public String average_Week2;
        public String average_Week3;
        public String average_Week4;
        public String average_Week5;
        public String average_Week6;
        public String average_Week7;
        public String average_Week8;
        public String dayOrWeek;
        public String nameOfDay;
        public String timestamp;
        public String timestamp2;
        public String profit;
    }

    /**
     * PartnerVareReference.dat
     * EAN[20] ItemNumber[20] ColliSize[10]
     */
    public class DBPartnerItemReference {
        public int Id;
        public String EAN;
        public String itemNumber;
        public String colliSize;
    }
}
