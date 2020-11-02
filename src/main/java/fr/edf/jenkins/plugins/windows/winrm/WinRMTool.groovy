package fr.edf.jenkins.plugins.windows.winrm

import java.nio.charset.Charset
import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.commons.lang.StringUtils
import org.apache.http.Header
import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicSchemeFactory
import org.apache.http.impl.auth.KerberosSchemeFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.HTTP

import fr.edf.jenkins.plugins.windows.winrm.auth.ntlm.SpNegoNTLMSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.auth.spnego.WsmanSPNegoSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.request.WinRMRequest

/**
 * Allow Jenkins to launch PowerShell commands on a windows remote machine
 * @author Mathieu Delrocq
 *
 */
class WinRMTool {

    public static final String PROTOCOL_HTTP = "http"
    public static final String PROTOCOL_HTTPS = "https"
    private static final String SOAP_REQUEST_CONTENT_TYPE = "application/soap+xml; charset=UTF-8"
    private static final String WSMAN_ROOT_URI = "/wsman"
    private static final String TLS = "TLS"

    /** host name or ip adress. */
    String host
    /** username to connect  with. */
    String user
    /** password associated to the user. */
    String password
    /** @see AuthSchemes. */
    String authSheme
    /** windos domain of the machine (**optional value for ntlm authentication**). */
    String domain
    /** name of the windows machine (**optional value for ntlm authentication**). */
    String workstation
    /** "true" to ignore https certificate error. */
    boolean ignoreCertificateError
    /** "http" or "https", usage of static constants recommended. */
    String protocol
    /** timout of the command */
    Integer commandTimeout

    String lastShellId
    String lastCommandId

    HttpClient httpClient

    /**
     *  Open a shell on the remote machine.
     * @return Shell ID
     */
    String openShell() throws WinRMException {
    }

    /**
     * Execute a command on the remote machine. <br/>
     * A Shell must be opened
     * @return commandId
     * @throws WinRMException with code and message if an error occured
     */
    String executeCommand(String shellId = lastShellId, String commandLine, String[] args = []) throws WinRMException {
    }

    /**
     * Return the output for the given command id
     * @param shellId
     * @param commandId
     * @return command output
     * @throws WinRMException
     */
    String getCommandOutput(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
    }

    String cleanupCommand(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
    }

    /**
     * Close the shell on the remote machine
     * @param shellId
     * @throws WinRMException
     */
    void deleteShellRequest(String shellId = lastShellId) throws WinRMException {
    }

    /**
     * Build the HttpClient or return the existing one
     * @return HttpClient
     */
    private HttpClient getHttpClient() {
        if (null == httpClient) {
            HttpClientBuilder builder = new HttpClientBuilder()
            if(ignoreCertificateError) {
                builder.setSSLContext(getIgnoreCertificateErrorContext())
                builder.setSSLHostnameVerifier(ignoreHostnameVerifier())
            }
            builder.setDefaultAuthSchemeRegistry(buildAuthShemeRegistry(authSheme))
            CloseableHttpClient httpclient = builder.build()
            this.httpClient = httpclient
        }
        return httpClient
    }

    /**
     * Build a TrustManager wich accept any certificate
     * @return sslContext
     */
    private SSLContext getIgnoreCertificateErrorContext() {
        def nullTrustManager = [
            checkClientTrusted: { chain, authType ->  },
            checkServerTrusted: { chain, authType ->  },
            getAcceptedIssuers: {
                null
            }
        ]

        SSLContext sslContext = SSLContext.getInstance(TLS)
        sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], new SecureRandom())
        return sslContext
    }

    /**
     * Return the HostNameVerifier of the remote machine.<br/>
     * Do not verify if empty
     * @param String
     * @return
     */
    private HostnameVerifier ignoreHostnameVerifier() {
        def nullHostnameVerifier = [
            verify: { hostname, session ->
                true
            }
        ]
        return nullHostnameVerifier as HostnameVerifier
    }

    /**
     * Build and return AuthShemeRegistry
     * @param authenticationScheme
     * @return authSchemeRegistry
     */
    private Registry<AuthSchemeProvider> buildAuthShemeRegistry(String authenticationScheme) {
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.SPNEGO,
                authenticationScheme.equals(AuthSchemes.NTLM) ? new SpNegoNTLMSchemeFactory() : new WsmanSPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())//
                .build()
        return authSchemeRegistry
    }

    /**
     * Build the request body
     * @param request
     * @return <code>StringEntity</code>
     */
    private StringEntity buildRequestEntity(WinRMRequest request) {
        StringEntity requestEntity = new StringEntity(request.toString())
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, SOAP_REQUEST_CONTENT_TYPE)
        requestEntity.setContentType(contentTypeHeader)
        return requestEntity
    }

    /**
     * Build the request config
     * @return <code>RequestConfig</code>
     */
    private RequestConfig buildRequestConfig() {
        RequestConfig.Builder configBuilder = new RequestConfig.Builder()
        return configBuilder.build()
    }

    private String compilePs(String psScript) {
        byte[] cmd = psScript.getBytes(Charset.forName("UTF-16LE"));
        String arg = javax.xml.bind.DatatypeConverter.printBase64Binary(cmd);
        return "powershell -encodedcommand " + arg;
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
            new URL(protocol, address, port, WSMAN_ROOT_URI)
        } catch (MalformedURLException e) {
            throw new WinRMException("Invalid WinRM URL!", e)
        }
    }
}
