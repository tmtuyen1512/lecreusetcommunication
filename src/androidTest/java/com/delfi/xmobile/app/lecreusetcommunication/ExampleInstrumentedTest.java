package com.delfi.xmobile.app.lecreusetcommunication;

import com.delfi.xmobile.app.lecreusetcommunication.ftpcom.SSLSessionReuseFTPSClient;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.

    }

    @Test
    public void FTPSConnectionAndroid(){
        SSLSessionReuseFTPSClient ftpClient = new SSLSessionReuseFTPSClient();
        //FTPSClient ftpClient = new FTPSClient();
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
            String url, user, pass;
            int port;

            /*url = "192.168.1.134";
            port = 21212;
            user = "delfi_transfer";
            pass = "70222555";*/


            url = "xcloudftp.delfi.com";
            port = 21;
            user = "xcloudpda";
            pass = "$Ewg&<~vS7Dg[=R4";
            ftpClient.connect(url, port);
            ftpClient.execPBSZ(0);
            ftpClient.execPROT("P");
            ftpClient.enterLocalPassiveMode();

            ftpClient.login(user, pass);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory("/xmobile/Production/REIXFV");
            ArrayList<FTPFile> globalFiles = new ArrayList<>();
            Collections.addAll(globalFiles, ftpClient.listFiles());
            System.out.println("globalFiles = " + globalFiles.size());
            for(FTPFile f : globalFiles)
                System.out.println(f.getName());
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
