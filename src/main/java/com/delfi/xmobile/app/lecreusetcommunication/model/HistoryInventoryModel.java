package com.delfi.xmobile.app.lecreusetcommunication.model;


import java.io.Serializable;


public class HistoryInventoryModel extends BaseItem implements Serializable {
    public int Id;
    public String initials;
    public String location;
    public String EAN;
    public String itemNo;
    public String description;
    public long quantity;
    public String positionNr;
    public String dateTime;
    public String price;
    public String itemGroup;

    public String inventoryNo;
    public String createDate;
    public int lineCount;

    public String sentDate;
    public String outputFileName;
    public int recreateCount;

    @Override
    public int getId() {
        return Id;
    }
}
