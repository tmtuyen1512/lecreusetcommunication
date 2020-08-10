package com.delfi.xmobile.app.lecreusetcommunication.model;


import java.io.Serializable;


public class HistoryOrderModel extends BaseItem implements Serializable {
    public int Id;

    public String orderNo;
    public String deliveryDate;
    public String createDate;
    public int lineCount;
    public int status;
    public String orderType;

    public String itemNumber;
    public long quantity;
    public String dateTime;
    public String colliSize;
    public String itemDescription;
    public String EAN;
    public String positionNo;

    public String itemNoOrEAN;

    public String sentDate;
    public String outputFileName;
    public int recreateCount;


    @Override
    public int getId() {
        return Id;
    }
}
