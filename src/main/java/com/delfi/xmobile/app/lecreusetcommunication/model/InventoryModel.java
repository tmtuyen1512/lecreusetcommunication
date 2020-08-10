package com.delfi.xmobile.app.lecreusetcommunication.model;


import android.annotation.SuppressLint;


public class InventoryModel {
    public int Id;
    public String initials;
    public String location;
    public String EAN;
    public String itemNo;
    public String description;
    public double quantity;
    public String positionNr;
    public String dateTime;
    public String price;
    public String itemGroup;

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        //Initials¤Location¤
        //EAN¤ItemNumber¤Description¤Quantity¤PositionNr¤Date¤Price¤ItemGroup

        if (quantity % 1.0 != 0)
            return String.format("%s¤%s¤%s¤%s¤%s¤%s¤%s¤%s¤%s¤%s",
                    initials, location, EAN, itemNo, description,
                    quantity, positionNr, dateTime, price, itemGroup);
        else
            return String.format("%s¤%s¤%s¤%s¤%s¤%.0f¤%s¤%s¤%s¤%s",
                    initials, location, EAN, itemNo, description,
                    quantity, positionNr, dateTime, price, itemGroup);
    }
}
