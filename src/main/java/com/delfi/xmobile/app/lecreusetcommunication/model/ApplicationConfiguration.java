package com.delfi.xmobile.app.lecreusetcommunication.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ApplicationConfiguration implements Serializable {

    @SerializedName("configuration")
    public Configuration Configuration;

    public static class Configuration implements Serializable {
        @SerializedName("global")
        public Global Global;

        @SerializedName("menuer")
        public Menuer Menuer;

        public static class Global implements Serializable {
            @SerializedName("appconfig")
            public AppConfig AppConfig;

            @SerializedName("policies")
            public Policies Policies;

            @SerializedName("devicelogfile")
            public DeviceLogFile DeviceLogFile;

            @SerializedName("deviceinfofile")
            public DeviceInfoFile DeviceInfoFile;

            @SerializedName("autoupdate")
            public AutoUpdate AutoUpdate;

            @SerializedName("networkinformation")
            public NetworkInformation NetworkInformation;

            @SerializedName("databases")
            public Databases Databases;

            @SerializedName("ftpservers")
            public FTPServers FTPServers;

            @SerializedName("devicesettings")
            public DeviceSettings DeviceSettings;


            public static class AppConfig implements Serializable {
                @SerializedName("enablealphakey")
                public Boolean EnableAlphaKey;

                @SerializedName("disablehomebutton")
                public Boolean DisableHomeButton;

                @SerializedName("enableautoupdate")
                public Boolean EnableAutoUpdate;

                @SerializedName("enablepasswordprotection")
                public Boolean EnablePasswordProtection;

                @SerializedName("showscanbutton")
                public Boolean ShowScanButton;
            }


            public static class Policies implements Serializable {
                @SerializedName("policyserverip")
                public String PolicyServerIp;

                @SerializedName("policyserverport")
                public String PolicyServerPort;

                @SerializedName("policyserveruser")
                public String PolicyServerUser;

                @SerializedName("policyserverpassword")
                public String PolicyServerPassword;

                @SerializedName("policyserverconfigdirectory")
                public String PolicyServerConfigDirectory;

                @SerializedName("ftptype")
                public String FTPType;

                @SerializedName("isimplicit")
                public boolean IsImplicit;
            }


            public static class DeviceLogFile implements Serializable {
                @SerializedName("devicelogfileserver")
                public String DeviceLogFileServer;

                @SerializedName("devicelogfileport")
                public String DeviceLogFilePort;

                @SerializedName("devicelogfileuser")
                public String DeviceLogFileUser;

                @SerializedName("devicelogfilepassword")
                public String DeviceLogFilePassword;

                @SerializedName("devicelogfiledirectory")
                public String DeviceLogFileDirectory;
            }


            public static class DeviceInfoFile implements Serializable {
                @SerializedName("deviceinfofileserver")
                public String DeviceInfoFileServer;

                @SerializedName("deviceinfofileport")
                public String DeviceInfoFilePort;

                @SerializedName("deviceinfofileuser")
                public String DeviceInfoFileUser;

                @SerializedName("deviceinfofilepassword")
                public String DeviceInfoFilePassword;

                @SerializedName("deviceinfofiledirectory")
                public String DeviceInfoFileDirectory;
            }


            public static class AutoUpdate implements Serializable {
                @SerializedName("autoupdateserver")
                public String AutoUpdateServer;

                @SerializedName("autoupdateserverport")
                public String AutoUpdateServerPort;

                @SerializedName("autoupdateserverdirectory")
                public String AutoUpdateServerDirectory;

                @SerializedName("autoupdateserveruser")
                public String AutoUpdateServerUser;

                @SerializedName("autoupdateserverpassword")
                public String AutoUpdateServerPassword;
            }


            public static class NetworkInformation implements Serializable {
                @SerializedName("ssid")
                public String SSID;

                @SerializedName("wifipassword")
                public String WifiPassword;

                @SerializedName("wifipassword2")
                public String WifiPassword2;

                @SerializedName("encryption")
                public String Encryption;

                @SerializedName("eaptype")
                public String EAPType;

                @SerializedName("username")
                public String Username;

                @SerializedName("userpassword")
                public String UserPassword;

                @SerializedName("ccxfeatures")
                public String CCXFeatures;

                @SerializedName("roamperiod")
                public String RoamPeriod;

                @SerializedName("roamtrigger")
                public String RoamTrigger;

                @SerializedName("roamdelta")
                public String RoamDelta;

                @SerializedName("nocredsprompt")
                public String NoCredsPrompt;

                @SerializedName("radiomode")
                public String RadioMode;

                @SerializedName("dhcp")
                public Boolean DHCP;

                @SerializedName("staticterminalip")
                public String StaticTerminalIp;

                @SerializedName("staticterminalgateway")
                public String StaticTerminalGateway;

                @SerializedName("staticterminalsubnet")
                public String StaticTerminalSubnet;

                @SerializedName("staticterminaldns")
                public String StaticTerminalDns;

                @SerializedName("staticterminaldns2")
                public String StaticTerminalDns2;

                @Override
                public String toString() {
                    final StringBuffer sb = new StringBuffer("{");
                    sb.append("SSID='").append(SSID).append('\'');
                    if (!TextUtils.isEmpty(WifiPassword))
                        sb.append("WifiPassword='").append("********").append('\'');
                    if (!TextUtils.isEmpty(WifiPassword2))
                        sb.append("WifiPassword2='").append("********").append('\'');

                    sb.append(", Encryption='").append(Encryption).append('\'');
                    sb.append(", EAPType='").append(EAPType).append('\'');
                    sb.append(", Username='").append(Username).append('\'');
                    if (!TextUtils.isEmpty(UserPassword))
                        sb.append("UserPassword='").append("********").append('\'');

                    sb.append(", DHCP=").append(DHCP);
                    sb.append(", StaticTerminalIp='").append(StaticTerminalIp).append('\'');
                    sb.append(", StaticTerminalGateway='").append(StaticTerminalGateway).append('\'');
                    sb.append(", StaticTerminalSubnet='").append(StaticTerminalSubnet).append('\'');
                    sb.append(", StaticTerminalDns='").append(StaticTerminalDns).append('\'');
                    sb.append(", StaticTerminalDns2='").append(StaticTerminalDns2).append('\'');
                    sb.append('}');
                    return sb.toString();
                }
            }

            public static class Databases implements Serializable {
                @SerializedName("rdidatabase")
                public RDIDatabase RDIDatabase;

                @SerializedName("partnerdatabase")
                public PartnerDatabase PartnerDatabase;


                public static class RDIDatabase implements Serializable {
                    @SerializedName("databasefilesprimary")
                    public String DatabaseFilesPrimary;

                    @SerializedName("databasefilesprimaryport")
                    public String DatabaseFilesPrimaryPort;

                    @SerializedName("databasefilesprimarydirectory")
                    public String DatabaseFilesPrimaryDirectory;

                    @SerializedName("databasefilesprimaryuser")
                    public String DatabaseFilesPrimaryUser;

                    @SerializedName("databasefilesprimarypassword")
                    public String DatabaseFilesPrimaryPassword;

                    @SerializedName("databasefilessecondary")
                    public String DatabaseFilesSecondary;

                    @SerializedName("databasefilessecondaryport")
                    public String DatabaseFilesSecondaryPort;

                    @SerializedName("databasefilessecondarydirectory")
                    public String DatabaseFilesSecondaryDirectory;

                    @SerializedName("databasefilessecondaryuser")
                    public String DatabaseFilesSecondaryUser;

                    @SerializedName("databasefilessecondarypassword")
                    public String DatabaseFilesSecondaryPassword;

                    @SerializedName("ftptype")
                    public String FTPType;

                    @SerializedName("isimplicit")
                    public boolean IsImplicit;
                }


                public static class PartnerDatabase implements Serializable {
                    @SerializedName("databasefilesprimary")
                    public String DatabaseFilesPrimary;

                    @SerializedName("databasefilesprimaryport")
                    public String DatabaseFilesPrimaryPort;

                    @SerializedName("databasefilesprimarydirectory")
                    public String DatabaseFilesPrimaryDirectory;

                    @SerializedName("databasefilesprimaryuser")
                    public String DatabaseFilesPrimaryUser;

                    @SerializedName("databasefilesprimarypassword")
                    public String DatabaseFilesPrimaryPassword;

                    @SerializedName("databasefilessecondary")
                    public String DatabaseFilesSecondary;

                    @SerializedName("databasefilessecondaryport")
                    public String DatabaseFilesSecondaryPort;

                    @SerializedName("databasefilessecondarydirectory")
                    public String DatabaseFilesSecondaryDirectory;

                    @SerializedName("databasefilessecondaryuser")
                    public String DatabaseFilesSecondaryUser;

                    @SerializedName("databasefilessecondarypassword")
                    public String DatabaseFilesSecondaryPassword;

                    @SerializedName("ftptype")
                    public String FTPType;

                    @SerializedName("isimplicit")
                    public boolean IsImplicit;
                }
            }


            public static class FTPServers implements Serializable {
                @SerializedName("ftpserver")
                public List<FTPServer> FTPServer;

                public static class FTPServer {
                    @SerializedName("server")
                    public String Server;

                    @SerializedName("port")
                    public String Port;

                    @SerializedName("user")
                    public String User;

                    @SerializedName("password")
                    public String Password;

                    @SerializedName("key")
                    public String Key;

                    @SerializedName("ftptype")
                    public String FTPType;

                    @SerializedName("isimplicit")
                    public boolean IsImplicit;
                }
            }

            public static class DeviceSettings implements Serializable {
                @SerializedName("standbytimeout")
                public float StandbyTimeout;
            }
        }

        public static class Menuer implements Serializable {
            @SerializedName("menu")
            public List<Menu> Menu;


            public static class Menu implements Serializable {
                @SerializedName("wms-colli")
                public String WMSColli;

                @SerializedName("usepartnerprices")
                public String UsePartnerPrices;

                @SerializedName("isvisible")
                public boolean IsVisible;

                @SerializedName("isdetailedorder")
                public String IsDetailedOrder;

                @SerializedName("isimmediateorder")
                public String IsImmediateOrder;

                @SerializedName("maxquantity")
                public Integer MaxQuantity;

                @SerializedName("maxitemnumber")
                public Integer MaxItemnumber;

                @SerializedName("filename")
                public String Filename;

                @SerializedName("ftpsentserver")
                public String FTPSentServer;

                @SerializedName("ftpsentserver_backup")
                public String FTPSentServer_Backup;

                @SerializedName("ftpsentdirectory")
                public String FTPSentDirectory;

                @SerializedName("ftpsentdirectory_backup")
                public String FTPSentDirectory_Backup;

                @SerializedName("name")
                public String Name;

                @SerializedName("maxinitials")
                public Integer MaxInitials;

                @SerializedName("maxlocation")
                public Integer MaxLocation;

                @SerializedName("showinventory")
                public String ShowInventory;

                @SerializedName("showorder")
                public String ShowOrder;

                @SerializedName("showshelfmark")
                public String ShowShelfmark;
            }
        }
    }
}