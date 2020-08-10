package com.delfi.xmobile.app.lecreusetcommunication.model;

import java.io.Serializable;

public class HistoryShelfMarkModel extends BaseItem implements Serializable {
    public int Id;
    public String shelfMarkNo;
    public String createDate;
    public String EAN;
    public String itemNumber;
    public String shelfFrontEdgeType;
    public String ScanningDateTime;
    public String itemDescription;

    public int lineCount;

    public String sentDate;
    public String outputFileName;
    public int recreateCount;

    @Override
    public int getId() {
        return Id;
    }
}
