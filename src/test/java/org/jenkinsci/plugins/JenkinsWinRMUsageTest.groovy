package org.jenkinsci.plugins

import org.apache.http.client.config.AuthSchemes
import org.junit.Test

import fr.edf.jenkins.plugins.windows.winrm.WinRMTool
import fr.edf.jenkins.plugins.windows.winrm.command.CommandOutput

/**
 * Usage example of jenkins-winrm
 * @author Mathieu Delrocq
 *
 */
class JenkinsWinRMUsageTest {

    @Test
    public void usage() {
        WinRMTool tool = new WinRMTool(
                "127.0.0.1", // ip or hostname
                5986, // port (default 5985 for http and 5986 for https)
                "user", // username
                "password", // password
                AuthSchemes.NTLM, // AuthScheme to connect
                true, // use https
                true, // disable certificate verification
                60) // timeout
        // Optionnal params (can be used for NTLM authentication)
        //tool.domain = "domain" //Domain of the remote Windows
        //tool.workstation = "workstation" //Name of the remote Windows
        String shellId = tool.openShell() // open a shell the remote Windows
        String test = "echo hi"
        String commandId = tool.executePSCommand(shellId, test) // execute command on the remote Windows using shellId
        CommandOutput out = tool.getCommandOutput(shellId, commandId) // get command output for the given commandId
        tool.cleanupCommand() // terminate command (if needed)
        tool.deleteShellRequest() // close and remove remote shell process
        System.out.println("status code : " + out.exitStatus);
        System.out.println("output : " + out.output);
        System.out.println("error : " + out.errorOutput);
    }
}
