package fr.edf.jenkins.plugins.windows.winrm.auth.ntlm

import org.apache.http.Header
import org.apache.http.HttpRequest
import org.apache.http.auth.AuthenticationException
import org.apache.http.auth.Credentials
import org.apache.http.client.config.AuthSchemes
import org.apache.http.impl.auth.NTLMScheme
import org.apache.http.message.BasicHeader

/**
 * 
 * @author cloudsoft
 * @see https://github.com/cloudsoft/winrm4j
 *
 */
class ApacheSpnegoScheme extends NTLMScheme{

    @Override
    String getSchemeName() {
        return AuthSchemes.SPNEGO
    }

    @Override
    Header authenticate(Credentials credentials, HttpRequest request)
    throws AuthenticationException {
        Header hdr = super.authenticate(credentials, request)
        return new BasicHeader(hdr.getName(), hdr.getValue().replace("NTLM", getSchemeName()))
    }
}
