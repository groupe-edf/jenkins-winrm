package fr.edf.jenkins.plugins.windows.winrm.auth.spnego

import org.apache.http.auth.AuthScheme
import org.apache.http.impl.auth.SPNegoSchemeFactory
import org.apache.http.protocol.HttpContext

/**
 *
 * @author cloudsoft
 * @see https://github.com/cloudsoft/winrm4j
 *
 */
class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

    WsmanSPNegoSchemeFactory() {
        super(true, true)
    }

    @Override
    AuthScheme create(final HttpContext context) {
        return new WsmanSPNegoScheme(isStripPort(), isUseCanonicalHostname())
    }
}
