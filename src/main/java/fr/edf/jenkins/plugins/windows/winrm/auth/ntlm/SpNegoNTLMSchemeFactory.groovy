package fr.edf.jenkins.plugins.windows.winrm.auth.ntlm

import org.apache.http.auth.AuthScheme
import org.apache.http.impl.auth.NTLMSchemeFactory
import org.apache.http.protocol.HttpContext

/**
 *
 * @author cloudsoft
 * @see https://github.com/cloudsoft/winrm4j
 *
 */
class SpNegoNTLMSchemeFactory extends NTLMSchemeFactory {

    @Override
    public AuthScheme create(HttpContext context) {
        return new ApacheSpnegoScheme()
    }
}