package fr.edf.jenkins.plugins.windows.winrm

import org.apache.http.client.config.AuthSchemes

class WinRMTool {
    
    static final String PROTOCOL_HTTP = 'http'
    static final String PROTOCOL_HTTPS = 'https'

    String host
    String user
    String password
    String authSheme
    String domain
    String workstation
    boolean ignoreCertificateError
    String protocol

    String openShell() {
    }

    String executeCommand() {
    }

    /**
     * Creates <code>URL</code> object to connect to remote host by WinRM
     *
     * @param protocol http or https
     * @param address remote host name or ip address
     * @param port port to remote host connection
     * @return created URL object
     */
    private URL buildUrl(String protocol, String address, int port) {
        try {
            new URL(protocol, address, port, "/wsman")
        } catch (MalformedURLException e) {
            throw new WinRMException("Invalid WinRM URL!", e)
        }
    }
}
