package fr.edf.jenkins.plugins.windows.winrm

import org.apache.http.client.config.AuthSchemes

/**
 * Allow Jenkins to launch OpenShell commands on a windows remote machine
 * @author Mathieu Delrocq
 *
 */
class WinRMTool {

    public static final String PROTOCOL_HTTP = 'http'
    public static final String PROTOCOL_HTTPS = 'https'

    /** host name or ip adress. */
    String host
    /** username to connect  with. */
    String user
    /** password associated to the user. */
    String password
    /** @see AuthSchemes. */
    String authSheme
    /** windos domain of the machine (optional value for ntlm authentication). */
    String domain
    /** name of the windows machine (optional value for ntlm authentication). */
    String workstation
    /** "true" to ignore https certificate error. */
    boolean ignoreCertificateError
    /** "http" or "https", usage of static constants recommended. */
    String protocol

    /**
     *  Open a shell on the remote machine.
     * @return Shell ID
     */
    String openShell() {
    }

    /**
     * Execute a command on the remote machine. <br/>
     * A Shell must be opened
     * @return result output
     * @throws WinRMException with code and message if an error occured
     */
    String executeCommand() throws WinRMException {
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
