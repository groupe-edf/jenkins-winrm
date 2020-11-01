package fr.edf.jenkins.plugins.windows.winrm.auth.spnego

import org.apache.http.auth.AuthScheme
import org.apache.http.impl.auth.SPNegoSchemeFactory
import org.apache.http.protocol.HttpContext

class WsmanSPNegoSchemeFactory extends SPNegoSchemeFactory {

    WsmanSPNegoSchemeFactory() {
        super(true, true)
    }

    @Override
    AuthScheme create(final HttpContext context) {
        return new WsmanSPNegoScheme(isStripPort(), isUseCanonicalHostname())
    }
}
