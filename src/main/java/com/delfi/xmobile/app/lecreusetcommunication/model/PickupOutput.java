package com.delfi.xmobile.app.lecreusetcommunication.model;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.Element;

import java.util.List;

/**
 * Created by USER on 06/28/2019.
 */
public class PickupOutput {

    @Element(name = "RDIDOC")
    public RDIDOC RDIDOC;

    public static class RDIDOC {

        @Element(name = "Header")
        @SerializedName("Header")
        public Header Header; //The tag order of output file depend on variable alphabet

        @Element(name = "Body")
        @SerializedName("Body")
        public Body Body; //The tag order of output file depend on variable alphabet
    }

    public static class Header {
        @Element(name = "From")
        public String From;

        @Element(name = "To")
        public String To;

        @Element(name = "SendingSystem")
        public SendingSystem SendingSystem;

        @Element(name = "Created")
        public String Created;

        @Element(name = "Test")
        public String Test;
    }

    public static class Body {
        @Element(name = "Pickups")
        public Picks Pickups;
    }

    public static class Picks {
        @Element(name = "Pickup")
        public Pick Pickup;
    }

    public static class Pick {
        @Element(name = "PickupHeader")
        public PickupHeader PickupHeader;

        @Element(name = "PickupLines")
        public PickupLines PickupLines;
    }

    public static class PickupLines {
        @Element(name = "PickupLine")
        public List<PickupLine> PickupLine;
    }

    public static class PickupLine {

        @Element(name = "PositionNo")
        public String PositionNo;

        @Element(name = "PickupType")
        public String PickupType;

        @Element(name = "PickupTypeNumber")
        public int PickupTypeNumber;

        @Element(name = "Initials")
        public String Initials;

        @Element(name = "Barcode")
        public String Barcode;

        @Element(name = "ItemNumber")
        public String ItemNumber;

        @Element(name = "ItemGroup")
        public String ItemGroup;

        @Element(name = "Quantity")
        public String Quantity;

        @Element(name = "Price")
        public String Price;

        @Element(name = "Date")
        public String Date;
    }

    public static class PickupHeader {
        @Element(name = "StoreNo")
        public String StoreNo;

        @Element(name = "StoreGLN")
        public String StoreGLN;

        @Element(name = "CustomerNo")
        public String CustomerNo;

        @Element(name = "CreatingDateTime")
        public String CreatingDateTime;

        @Element(name = "ConfirmedDateTime")
        public String ConfirmedDateTime;

        @Element(name = "SourceOfOrigin")
        public String SourceOfOrigin;

        @Element(name = "NumberOfLines")
        public String NumberOfLines;
    }

    public static class SendingSystem {
        @Element(name = "Name")
        public String Name;

        @Element(name = "Version")
        public String Version;

        @Element(name = "SerialNo")
        public String SerialNo;
    }
}

