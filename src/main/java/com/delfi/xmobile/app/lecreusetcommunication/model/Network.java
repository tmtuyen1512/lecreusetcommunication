package com.delfi.xmobile.app.lecreusetcommunication.model;

import java.io.Serializable;

/**
 * Created by USER on 06/05/2019.
 */
public class Network implements Serializable {
    public boolean Ethernet;

    public Boolean WifiEnabled;
    public String SSID;
    public String WifiPassword;
    public String WifiPassword2;

    public String EAPType;
    public String Encryption;

    //for enterprise wifi
    public String Username;
    public String UserPassword;

    public Boolean DHCP;

    //for static ip
    public String StaticTerminalIp;
    public String StaticTerminalGateway;
    public String StaticTerminalSubnet;
    public String StaticTerminalDns;
    //Added from Base_0.0.008
    public String StaticTerminalDns2;


    //for manual
    public String CCXFeatures;
    public String RoamPeriod;
    public String RoamTrigger;
    public String RoamDelta;
    public Boolean NoCredsPrompt;
    public String RadioMode;

    //Flowchart_Ver1000_Rev1022
    public String CAcertificate;
}
