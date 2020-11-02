package fr.edf.jenkins.plugins.windows.winrm

import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.logging.Level
import java.util.logging.Logger

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.commons.lang.StringUtils
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.NTCredentials
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicSchemeFactory
import org.apache.http.impl.auth.KerberosSchemeFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HTTP
import org.apache.http.protocol.HttpContext

import fr.edf.jenkins.plugins.windows.winrm.auth.ntlm.SpNegoNTLMSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.auth.spnego.WsmanSPNegoSchemeFactory
import fr.edf.jenkins.plugins.windows.winrm.request.ExecuteCommandRequest
import fr.edf.jenkins.plugins.windows.winrm.request.OpenShellRequest
import fr.edf.jenkins.plugins.windows.winrm.request.WinRMRequest
import groovy.util.slurpersupport.GPathResult

/**
 * Allow Jenkins to launch PowerShell commands on a windows remote machine
 * @author Mathieu Delrocq
 *
 */
class WinRMTool {

    static final Logger LOGGER = Logger.getLogger(WinRMTool.name)

    public static final String PROTOCOL_HTTP = "http"
    public static final String PROTOCOL_HTTPS = "https"
    private static final String SOAP_REQUEST_CONTENT_TYPE = "application/soap+xml; charset=UTF-8"
    private static final String WSMAN_ROOT_URI = "/wsman"
    private static final String TLS = "TLS"
    final List<Integer> sucessStatus = [200, 201, 202, 204]

    /** username to connect  with. */
    String username
    /** password associated to the user. */
    String password
    /** @see AuthSchemes. */
    String authSheme
    /** windos domain of the machine (**optional value for ntlm authentication**). */
    String domain
    /** name of the windows machine (**optional value for ntlm authentication**). */
    String workstation
    /** "true" to ignore https certificate error. */
    boolean disableCertificateChecks
    /** "http" or "https", usage of static constants recommended. */
    boolean useHttps
    /** timout of the command */
    Integer commandTimeout = 60

    URL url
    String lastShellId
    String lastCommandId
    HttpClient httpClient


    WinRMTool(String address, String port, String username, String password, boolean useHttps,
    boolean disableCertificateChecks, Integer commandTimeout) {
        this.url = buildUrl(useHttps?PROTOCOL_HTTPS:PROTOCOL_HTTP,address,port)
        this.username = username
        this.password = password
        this.useHttps = useHttps
        this.disableCertificateChecks = disableCertificateChecks
        this.commandTimeout = commandTimeout
    }

    /**
     *  Open a shell on the remote machine.
     * @return Shell ID
     */
    String openShell() throws WinRMException {
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new OpenShellRequest(url, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = httpClient.execute(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "OpenShell",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        HttpEntity responseEntity = response.getEntity();
        String responseBody = responseEntity.getContent().text
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + responseBody)
        GPathResult results = new XmlSlurper().parseText(responseBody)
        String shellId = results?.'*:Body'?.'*:Shell'?.'*:ShellId'
        if(StringUtils.isEmpty(shellId)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "OpenShell",
            status.getProtocolVersion(),
            responseCode,
            "Cannot retrieve the shell id in the given response :" + responseBody))
        }
        this.lastShellId = shellId
        return shellId
    }

    /**
     * Compile PS and call executeCommand. <br/>
     * A Shell must be opened
     * @return commandId
     * @throws WinRMException with code and message if an error occured
     */
    String executePSCommand(String shellId = lastShellId, String psCommand, String[] args = []) throws WinRMException {
        return executeCommand(shellId, compilePs(psCommand), args)
    }

    /**
     * Execute a command on the remote machine. <br/>
     * A Shell must be opened
     * @return commandId
     * @throws WinRMException with code and message if an error occured
     */
    String executeCommand(String shellId = lastShellId, String commandLine, String[] args = []) throws WinRMException {
        if(StringUtils.isEmpty(shellId)) {
            throw new WinRMException("Call openShell() before execute command")
        }
        HttpClient httpClient = getHttpClient()
        HttpPost httpPost = buildHttpPostRequest(new ExecuteCommandRequest(url, shellId, commandLine, args, commandTimeout))
        HttpContext context = buildHttpContext()
        HttpResponse response = httpClient.execute(httpPost, context)
        StatusLine status = response.getStatusLine()
        int responseCode = status.getStatusCode()
        if(!sucessStatus.contains(responseCode)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "ExecuteCommand",
            status.getProtocolVersion(),
            responseCode,
            status.getReasonPhrase()))
        }
        HttpEntity responseEntity = response.getEntity();
        String responseBody = responseEntity.getContent().text
        LOGGER.log(Level.FINEST, "RESPONSE BODY :" + responseBody)
        GPathResult results = new XmlSlurper().parseText(responseBody)
        String commandId = results?.'*:Body'?.'*:Receive'?.'*:DesiredStream'?.@'*:CommandId'
        if(StringUtils.isEmpty(commandId)) {
            throw new WinRMException(String.format(
            WinRMException.FORMATTED_MESSAGE,
            "ExecuteCommand",
            status.getProtocolVersion(),
            responseCode,
            "Cannot retrieve the command id in the given response :" + responseBody))
        }
        return commandId
    }

    /**
     * Return the output for the given command id
     * @param shellId
     * @param commandId
     * @return command output
     * @throws WinRMException
     */
    String getCommandOutput(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
        if(StringUtils.isEmpty(commandId)) {
            throw new WinRMException("No command was executed")
        }
        HttpClient httpClient = getHttpClient()
    }

    String cleanupCommand(String shellId = lastShellId, String commandId = lastCommandId) throws WinRMException {
        HttpClient httpClient = getHttpClient()
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
            if(useHttps) {
                if(disableCertificateChecks) {
                    builder.setSSLContext(buildIgnoreCertificateErrorContext())
                    builder.setSSLHostnameVerifier(buildIgnoreHostnameVerifier())
                }
            }
            builder.setDefaultAuthSchemeRegistry(buildAuthShemeRegistry(authSheme))
            CloseableHttpClient httpclient = builder.build()
            this.httpClient = httpclient
        }
        return httpClient
    }

    private HttpPost buildHttpPostRequest(WinRMRequest request) {
        // Init HttpPost
        HttpPost httpPost = new HttpPost(url.toURI())
        // Build request entity
        String requestString = request.toString()
        LOGGER.log(Level.FINEST, "REQUEST BODY :" + requestString)
        StringEntity entity = new StringEntity(requestString)
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, SOAP_REQUEST_CONTENT_TYPE)
        entity.setContentType(contentTypeHeader)
        httpPost.setEntity(entity)
        // Request config
        RequestConfig.Builder configBuilder = new RequestConfig.Builder()
        httpPost.setConfig(configBuilder.build())
        return httpPost
    }

    /**
     * Build a TrustManager wich accept any certificate
     * @return sslContext
     */
    private SSLContext buildIgnoreCertificateErrorContext() {
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
    private HostnameVerifier buildIgnoreHostnameVerifier() {
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

    private HttpContext buildHttpContext() throws UnsupportedOperationException {
        HttpContext localContext = new BasicHttpContext();
        CredentialsProvider credsProvider = new BasicCredentialsProvider()
        switch(authSheme) {
            case AuthSchemes.BASIC:
                credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password))
                break
            case AuthSchemes.NTLM:
                credsProvider.setCredentials(AuthScope.ANY,
                new NTCredentials(username, password, workstation, domain))
                break
            default:
                throw new UnsupportedOperationException("No such authentication scheme " + authSheme)
        }
        localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider)
        return localContext
    }

    /**
     * Compile PowerShell script
     * @param psScript
     * @return encoded PowerShell
     */
    private String compilePs(String psScript) {
        byte[] cmd = psScript.getBytes(Charset.forName("UTF-16LE"))
        String arg = javax.xml.bind.DatatypeConverter.printBase64Binary(cmd)
        return "powershell -encodedcommand " + arg
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
