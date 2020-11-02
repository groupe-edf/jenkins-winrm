package org.jenkinsci.plugins

import java.security.SecureRandom

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.auth.AuthScheme
import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.AuthenticationException
import org.apache.http.auth.Credentials
import org.apache.http.auth.KerberosCredentials
import org.apache.http.auth.NTCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicSchemeFactory
import org.apache.http.impl.auth.KerberosSchemeFactory
import org.apache.http.impl.auth.NTLMScheme
import org.apache.http.impl.auth.NTLMSchemeFactory
import org.apache.http.impl.auth.SPNegoScheme
import org.apache.http.impl.auth.SPNegoSchemeFactory
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HTTP
import org.apache.http.protocol.HttpContext
import org.ietf.jgss.GSSContext
import org.ietf.jgss.GSSCredential
import org.ietf.jgss.GSSException
import org.ietf.jgss.GSSManager
import org.ietf.jgss.GSSName
import org.ietf.jgss.Oid
import org.junit.Test

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import groovy.xml.Namespace
/**
 * Initial Workaround
 * @author Mathieu Delrocq
 *
 */
class WinRmConnectionTest {
    String soapNsUrl = 'http://www.w3.org/2003/05/soap-envelope'
    String shellNsUrl = 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell'
    def saopNs = new Namespace(soapNsUrl, 's')
    def shellNs = new Namespace(shellNsUrl, 'rsp')
    URL url = new URL("https://127.0.0.1:5986/wsman")

//    @Test
    void testWinRmConnection() {
        System.out.println("#######################################################################################")
        System.out.println("URL----------------------------------------------------------------------------------")
        System.out.println(url.toString())
        System.out.println("REQUEST----------------------------------------------------------------------------------")
        String authenticationScheme = AuthSchemes.NTLM
        System.out.println("AuthScheme : " + authenticationScheme)
        HttpClientBuilder builder = new HttpClientBuilder();

        def nullTrustManager = [
            checkClientTrusted: { chain, authType ->  },
            checkServerTrusted: { chain, authType ->  },
            getAcceptedIssuers: {
                null
            }
        ]

        def nullHostnameVerifier = [
            verify: { hostname, session ->
                true
            }
        ]
        System.out.println("Protocole : https")
        System.out.println("IgnoreCertificate : " + true)
        SSLContext sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], new SecureRandom())
        builder.setSSLContext(sslContext)
        builder.setSSLHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.SPNEGO,
                authenticationScheme.equals(AuthSchemes.NTLM) ? new SpNegoNTLMSchemeFactory() : new WsmanSPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())//
                .build();

        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry)

        CloseableHttpClient httpclient = builder.build();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httppost = new HttpPost(url.toURI());
        OpenShellRequest request = new OpenShellRequest(url,60)
        StringEntity requestEntity = new StringEntity(request.toString())
        Header contentTypeHeader = new BasicHeader(HTTP.CONTENT_TYPE, "application/soap+xml; charset=UTF-8")
        System.out.println(HTTP.CONTENT_TYPE + " : application/soap+xml; charset=UTF-8")
        requestEntity.setContentType(contentTypeHeader)
        System.out.println("Body : " + request.toString().trim().replace("\n", "").replace("\r", "").replaceAll("\\s+",""))
        System.out.println("#######################################################################################")
        httppost.setEntity(requestEntity)
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new NTCredentials("user", "pass", null, null));
        List<String> authtypes = new ArrayList<String>();
        //            authtypes.add(AuthPolicy.NTLM);
        //            httpclient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF,authtypes);
        RequestConfig.Builder configBuilder = new RequestConfig.Builder()

        //        configBuilder.setTargetPreferredAuthSchemes([AuthSchemes.NTLM])
        RequestConfig config = configBuilder.build()
        httppost.setConfig(config)
        localContext.setAttribute(HttpClientContext.CREDS_PROVIDER, credsProvider);

        HttpResponse response = httpclient.execute(httppost, localContext);
        StatusLine status = response.getStatusLine()
        System.out.println("#######################################################################################")
        System.out.println("RESPONSE-------------------------------------------------------------------------------")
        System.out.println("HEADER-------------------------------------------------------------------------------")
        System.out.println("response code : " + status.getStatusCode())
        System.out.println("response description : " + status.reasonPhrase)
        System.out.println("protocol version : " + status.protocolVersion)
        HttpEntity entity=response.getEntity();
        String responseBody = entity.getContent().text
        System.out.println("BODY---------------------------------------------------------------------------------")
        System.out.println(responseBody)
        System.out.println("DATAS--------------------------------------------------------------------------------")
        GPathResult results = new XmlSlurper().parseText(responseBody)
//        String shellId = results?.'*:Body'?.'*:ResourceCreated'?.'*:ReferenceParameters'?.'*:SelectorSet'?.'*:Selector'?.find {
//            it.@Name == 'ShellId'
//        }?.text()
//        def body = results?.'*:Body'
//        def shell = body?.'*:Shell'
        def shell = results?.'*:Body'?.'*:Shell'
        System.out.println("ShellId : " + shell?.'*:ShellId')
        System.out.println("Owner : " + shell?.'*:Owner')
        System.out.println("ShellRunTime : " + shell?.'*:ShellRunTime')
        System.out.println("#######################################################################################")
    }


    private class SpNegoNTLMSchemeFactory extends NTLMSchemeFactory {

        @Override
        public AuthScheme create(HttpContext context) {
            return new ApacheSpnegoScheme();
        }
    }

    private class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

        public WsmanSPNegoSchemeFactory() {
            super(true, true);
        }

        @Override
        public AuthScheme create(final HttpContext context) {
            return new WsmanSPNegoScheme(isStripPort(), isUseCanonicalHostname());
        }
    }

    private class ApacheSpnegoScheme extends NTLMScheme{

        @Override
        public String getSchemeName() {
            return AuthSchemes.SPNEGO;
        }

        @Override
        public Header authenticate(Credentials credentials, HttpRequest request)
        throws AuthenticationException {
            Header hdr = super.authenticate(credentials, request);
            return new BasicHeader(hdr.getName(), hdr.getValue().replace("NTLM", getSchemeName()));
        }
    }

    public class WsmanSPNegoScheme extends SPNegoScheme {

        public WsmanSPNegoScheme(final boolean stripPort, final boolean useCanonicalHostname) {
            super(stripPort, useCanonicalHostname);
        }

        /**
         * Copied form {@link org.apache.http.impl.auth.GGSSchemeBase#generateGSSToken}.
         * The variable "service" must be set to "WSMAN" but this variable is private.
         */
        @Override
        protected byte[] generateGSSToken(
                final byte[] input, final Oid oid, final String authServer,
                final Credentials credentials) throws GSSException {
            byte[] inputBuff = input;
            if (inputBuff == null) {
                inputBuff = new byte[0];
            }
            final GSSManager manager = getManager();
            final GSSName serverName = manager.createName("WSMAN" + "@" + authServer, GSSName.NT_HOSTBASED_SERVICE);

            final GSSCredential gssCredential;
            if (credentials instanceof KerberosCredentials) {
                gssCredential = ((KerberosCredentials) credentials).getGSSCredential();
            } else {
                gssCredential = null;
            }

            final GSSContext gssContext = manager.createContext(
                    serverName.canonicalize(oid), oid, gssCredential, GSSContext.DEFAULT_LIFETIME);
            gssContext.requestMutualAuth(true);
            gssContext.requestCredDeleg(true);
            return gssContext.initSecContext(inputBuff, 0, inputBuff.length);
        }
    }

    abstract class WinRMRequest {

        /// Constants used during composing WinRM request.
        static String NMSP_URI_S = 'http://www.w3.org/2003/05/soap-envelope'
        static String NMSP_URI_WSA = 'http://schemas.xmlsoap.org/ws/2004/08/addressing'
        static String NMSP_URI_WSMAN = 'http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd'
        static String NMSP_URI_RSP = 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell'
        static String NMSP_URI_XSI = 'http://www.w3.org/2001/XMLSchema-instance'

        static String URI_SHELL_CMD = 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd'
        static String URI_ADDRESS = 'http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous'

        static int WINRS_CODEPAGE = 437
        static String WINRS_NOPROFILE = 'FALSE'

        URL toAddress

        int envelopSize = 153600;
        String locale = "en-US";
        String timeout

        WinRMRequest(URL toAddress, int timeout) {
            this.toAddress = toAddress
            this.timeout = "PT${timeout}S"
        }

    }

    class OpenShellRequest extends WinRMRequest {

        OpenShellRequest(URL toAddress, int timeout = 60) {
            super(toAddress, timeout)
        }

        @Override
        String toString() {

            StringWriter writer = new StringWriter()
            MarkupBuilder xml = new MarkupBuilder(writer)

            xml.'s:Envelope'('xmlns:s': NMSP_URI_S,
            'xmlns:wsa': NMSP_URI_WSA,
            'xmlns:wsman': NMSP_URI_WSMAN) {
                's:Header' {
                    'wsa:To'(toAddress)
                    'wsman:ResourceURI'('s:mustUnderstand': true, URI_SHELL_CMD)
                    'wsa:ReplyTo' {
                        'wsa:Address'('s:mustUnderstand': true, URI_ADDRESS)
                    }
                    'wsa:Action'('s:mustUnderstand': true, 'http://schemas.xmlsoap.org/ws/2004/09/transfer/Create')
                    'wsman:MaxEnvelopeSize'('s:mustUnderstand': true, envelopSize)
                    'wsa:MessageID'("uuid:" + UUID.randomUUID().toString().toUpperCase())
                    'wsman:Locale'('s:mustUnderstand': false, 'xml:lang': locale)
                    'wsman:OptionSet'('xmlns:xsi': NMSP_URI_XSI) {
                        'wsman:Option'(Name: 'WINRS_NOPROFILE', WINRS_NOPROFILE)
                        'wsman:Option'(Name: 'WINRS_CODEPAGE', WINRS_CODEPAGE)
                    }
                    'wsman:OperationTimeout'(timeout)
                }
                's:Body' {
                    'rsp:Shell'('xmlns:rsp': NMSP_URI_RSP) {
                        'rsp:InputStreams'('stdin')
                        'rsp:OutputStreams'('stdout stderr')
                    }
                }
            }

            writer.toString()
        }
    }
}
