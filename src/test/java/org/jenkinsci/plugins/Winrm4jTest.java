package org.jenkinsci.plugins;

//import org.apache.http.client.config.AuthSchemes;
//import org.junit.Test;
//
//import io.cloudsoft.winrm4j.client.WinRmClientContext;
//import io.cloudsoft.winrm4j.winrm.WinRmTool;
//import io.cloudsoft.winrm4j.winrm.WinRmToolResponse;

public class Winrm4jTest {

//    @Test
//    public void test_winrm4j() {
//        System.setProperty("io.cloudsoft.winrm4j", "DEBUG");
//        WinRmClientContext context = WinRmClientContext.newInstance();
//
//        WinRmTool tool = WinRmTool.Builder.builder("127.0.0.1", "user", "password")
//                .authenticationScheme(AuthSchemes.NTLM).port(5986).useHttps(true).disableCertificateChecks(true)
//                .context(context).build();
//        String test2 = "$user = \"toto8\";" + "$homedir = \"C:\\Users\\$user\";"
//                + "New-Item -Path $homedir -ItemType \"directory\" -Force";
//        String test3 = "pwd";
//        // final String test = "New-LocalUser 'toto3' -Password $('Passnotlogic123!' |
//        // ConvertTo-SecureString -AsPlainText -Force) -FullName 'toto3' -Description
//        // \"User automatically created by jenkins\" -PasswordNeverExpires
//        // -UserMayNotChangePassword";
//        // WinRmToolResponse response = tool.executePs(test);
//        WinRmToolResponse response = tool.executePs(test3);
//        System.out.println(response.getStatusCode());
//        System.out.println(response.getStdOut());
//        System.out.println(response.getStdErr());
//        // response =tool.executePs("echo hi");
//        // System.out.println(response.getStdOut());
//        context.shutdown();
//    }
}
