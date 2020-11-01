package fr.edf.jenkins.plugins.windows.winrm.request

import groovy.xml.MarkupBuilder

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
                'wsa:MessageID'(composeUUID())
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