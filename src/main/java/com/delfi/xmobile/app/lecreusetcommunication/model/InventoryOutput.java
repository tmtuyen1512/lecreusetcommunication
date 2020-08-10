package com.delfi.xmobile.app.lecreusetcommunication.model;

import org.simpleframework.xml.Element;

import java.util.List;

/**
 * Created by USER on 06/28/2019.
 */
public class InventoryOutput {

    @Element(required = false, name = "RDIDOC")
    public RDIDOC RDIDOC;

    public static class RDIDOC {
        @Element(required = false, name = "Header")
        public Header Header;

        @Element(required = false, name = "Body")
        public Body Body;
    }

    public static class Header {
        @Element(required = false, name = "From")
        public String From;

        @Element(required = false, name = "To")
        public String To;

        @Element(required = false, name = "SendingSystem")
        public SendingSystem SendingSystem;

        @Element(required = false, name = "Created")
        public String Created;

        @Element(required = false, name = "Test")
        public String Test;
    }

    public static class Body {
        @Element(required = false, name = "InventoryStatus")
        public InventoryStatus InventoryStatus;
    }

    public static class InventoryStatus {
        @Element(required = false, name = "Status")
        public Status Status;
    }

    public static class Status {
        @Element(required = false, name = "StatusHeader")
        public StatusHeader StatusHeader;

        @Element(required = false, name = "StatusLines")
        public StatusLines StatusLines;
    }

    public static class StatusLines {
        @Element(required = false, name = "StatusLine")
        public List<StatusLine> StatusLine;
    }

    public static class StatusLine {

        @Element(required = false, name = "PositionNo")
        public String PositionNo;

        @Element(required = false, name = "Initials")
        public String Initials;

        @Element(required = false, name = "BarCode")
        public String BarCode;

        @Element(required = false, name = "RDIItemNo")
        public String RDIItemNo;

        /*@Element(required = false, name = "Description")
        public String Description;*/

        @Element(required = false, name = "Location")
        public String Location;

        @Element(required = false, name = "Quantity")
        public String Quantity;

        @Element(required = false, name = "Family")
        public String Family;

        @Element(required = false, name = "Price")
        public String Price;

        @Element(required = false, name = "ScanningDateTime")
        public String ScanningDateTime;
    }

    public static class StatusHeader {
        @Element(required = false, name = "StoreNo")
        public String StoreNo;

        @Element(required = false, name = "StoreGLN")
        public String StoreGLN;

        @Element(required = false, name = "CustomerNo")
        public String CustomerNo;

        @Element(required = false, name = "CreatingDateTime")
        public String CreatingDateTime;

        @Element(required = false, name = "ConfirmedDateTime")
        public String ConfirmedDateTime;

        @Element(required = false, name = "SourceOfOrigin")
        public String SourceOfOrigin;

        @Element(required = false, name = "NumberOfLines")
        public String NumberOfLines;
    }

    public static class SendingSystem {
        @Element(required = false, name = "Name")
        public String Name;

        @Element(required = false, name = "Version")
        public String Version;

        @Element(required = false, name = "SerialNo")
        public String SerialNo;
    }
}

