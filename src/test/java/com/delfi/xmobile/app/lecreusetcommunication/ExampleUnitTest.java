package com.delfi.xmobile.app.lecreusetcommunication;

import android.support.annotation.NonNull;

import com.delfi.xmobile.app.lecreusetcommunication.ftpcom.SSLSessionReuseFTPSClient;
import com.delfi.xmobile.app.lecreusetcommunication.utils.NetworkUtil;
import com.delfi.xmobile.app.lecreusetcommunication.utils.WifiSubnetHelper;
import com.delfi.xmobile.app.lecreusetcommunication.view.CommunicationActivity;

import junit.framework.Assert;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String date = NetworkUtil.getCurrentNetworkTime();
        System.out.println(date);
        String a = null;
        String b = "DANH";

        System.out.println(String.format("Checking %s / %s", b, a));
    }

    @Test
    public void isCorrect() {
        Assert.assertEquals(1, WifiSubnetHelper.getPrefixLength("128.0.0.0"));
        Assert.assertEquals(2, WifiSubnetHelper.getPrefixLength("192.0.0.0"));
        Assert.assertEquals(3, WifiSubnetHelper.getPrefixLength("224.0.0.0"));
        Assert.assertEquals(10, WifiSubnetHelper.getPrefixLength("255.192.0.0"));
        Assert.assertEquals(17, WifiSubnetHelper.getPrefixLength("255.255.128.0"));
        Assert.assertEquals(20, WifiSubnetHelper.getPrefixLength("255.255.240.0"));
        Assert.assertEquals(24, WifiSubnetHelper.getPrefixLength("255.255.255.0"));
        Assert.assertEquals(26, WifiSubnetHelper.getPrefixLength("255.255.255.192"));
        Assert.assertEquals(28, WifiSubnetHelper.getPrefixLength("255.255.255.240"));
        Assert.assertEquals(31, WifiSubnetHelper.getPrefixLength("255.255.255.254"));
        Assert.assertEquals(32, WifiSubnetHelper.getPrefixLength("255.255.255.255"));
    }

    @Test
    public void isCorrect_Name() {
        Assert.assertEquals("Downloading update for startup v0.0.042", makeUserFriendlyName("libcom_delfi_xmobile_app_reitanstartup_0.0.042.so"));
        Assert.assertEquals("Downloading update for xcore v0.0.018", makeUserFriendlyName("libcom_delfi_xmobile_lib_xcore_0.0.018.so"));
        Assert.assertEquals("Downloading update for APK v0.0.013", makeUserFriendlyName("com.delfi.xmobile_v0.0.013-release.apk"));
    }

    private String makeUserFriendlyName(@NonNull String fileName) {
        try {
            if (fileName.contains("-release.apk")) {
                fileName = fileName.replace("com.delfi.xmobile_", "APK ");
                fileName = fileName.replace("-release.apk", "");

            } else {
                fileName = fileName.replace("libcom_delfi_xmobile_app_", "");
                fileName = fileName.replace("libcom_delfi_xmobile_lib_", "");
                fileName = fileName.replace("reitan", "");
                fileName = fileName.replace("_", " v");
                fileName = fileName.substring(0, fileName.length() - 3);
            }
            fileName = "Downloading update for " + fileName;

        } catch (Exception e) {/*ignore*/}

        return fileName;
    }

    @Test
    public void FTPSConnection(){
        SSLSessionReuseFTPSClient ftpClient = new SSLSessionReuseFTPSClient();
        ftpClient.addProtocolCommandListener(new ProtocolCommandListener() {
            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                System.out.println("Sent " + event.getMessage());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                System.out.println("Received " + event.getMessage());
            }
        });
        try {
            ftpClient.connect("192.168.1.134", 21212);
            ftpClient.execPBSZ(0);
            ftpClient.execPROT("P");
            ftpClient.enterLocalPassiveMode();

            ftpClient.login("delfi_transfer", "70222555");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory("/reitan/Production/REIXFV");
            ArrayList<FTPFile> globalFiles = new ArrayList<>();
            Collections.addAll(globalFiles, ftpClient.listFiles());
            System.out.println("globalFiles = " + globalFiles.size());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void isCorrect_SameVersionCode() {

        //same
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 19000));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 19001));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 19121));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 1901));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 1907));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(19, 19));

        Assert.assertTrue(CommunicationActivity.isSameVersionCode(1900, 1900));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(1, 100));
        Assert.assertTrue(CommunicationActivity.isSameVersionCode(1, 101));

        //older
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(19, 20));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(1900, 1901));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(1900, 2100));

        //newer
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(20, 19));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(19, 9));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(1901, 1900));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(1902, 17));
        Assert.assertFalse(CommunicationActivity.isSameVersionCode(2202, 21));
    }

    @Test
    public void isCorrect_NewerVersionCode() {

        //same
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 19000));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 19001));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 19121));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 1901));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 1907));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 19));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(1900, 1900));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(1, 100));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(1, 101));

        //older
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(19, 20));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(1900, 1901));
        Assert.assertFalse(CommunicationActivity.isNewerVersionCode(1900, 2100));

        //newer
        Assert.assertTrue(CommunicationActivity.isNewerVersionCode(20, 19));
        Assert.assertTrue(CommunicationActivity.isNewerVersionCode(19, 9));
        Assert.assertTrue(CommunicationActivity.isNewerVersionCode(1901, 1900));
        Assert.assertTrue(CommunicationActivity.isNewerVersionCode(1902, 17));
        Assert.assertTrue(CommunicationActivity.isNewerVersionCode(2202, 21));
    }
}