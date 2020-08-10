package com.delfi.xmobile.app.lecreusetcommunication.model;

import org.simpleframework.xml.Element;

import java.util.List;

/**
 * Created by USER on 06/28/2019.
 */
public class OrderOutput {

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
        @Element(required = false, name = "ShelfFrontEdges")
        public Orders Orders;
    }

    public static class Orders {
        @Element(required = false, name = "ShelfFrontEdge")
        public Order Order;
    }

    public static class Order {
        @Element(required = false, name = "OrderHeader")
        public OrderHeader OrderHeader;

        @Element(required = false, name = "OrderLines")
        public OrderLines OrderLines;
    }

    public static class OrderLines {
        @Element(required = false, name = "OrderLine")
        public List<OrderLine> OrderLine;
    }

    public static class OrderLine {

        @Element(required = false, name = "PositionNo")
        public String PositionNo;

        @Element(required = false, name = "PartnerItemNo")
        public String PartnerItemNo;

        @Element(required = false, name = "EAN")
        public String BarCode;

        @Element(required = false, name = "itemNumber")
        public String RDIItemNo;

        @Element(required = false, name = "Description")
        public String Description;

        @Element(required = false, name = "Location")
        public String Location;

        @Element(required = false, name = "Quantity")
        public String Quantity;

        @Element(required = false, name = "QuantityPerUnit")
        public String QuantityPerUnit;

        @Element(required = false, name = "CampaignNo")
        public String CampaignNo;

        @Element(required = false, name = "ScanningDateTime")
        public String ScanningDateTime;
    }

    public static class OrderHeader {
        @Element(required = false, name = "StoreNo")
        public String StoreNo;

        @Element(required = false, name = "StoreGLN")
        public String StoreGLN;

        @Element(required = false, name = "CustomerNo")
        public String CustomerNo;

        @Element(required = false, name = "DeliveryDayMethod")
        public String DeliveryDayMethod;

        @Element(required = false, name = "RequestedDeliveryDate")
        public String RequestedDeliveryDate;

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

