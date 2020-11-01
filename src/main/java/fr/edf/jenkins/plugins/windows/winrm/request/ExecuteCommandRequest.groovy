/*
 * Copyright (C) 2011-2015 Aestas/IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.edf.jenkins.plugins.windows.winrm.request

import groovy.xml.MarkupBuilder

/**
 * Request that starts remote command execution.
 *
 * @author Sergey Korenko
 */
class ExecuteCommandRequest extends WinRMRequest {

    String commandLine
    String[] commandArguments
    String shellId

    ExecuteCommandRequest(URL toAddress, String shellId, String commandLine, String[] args = [], int timeout = 60) {
        super(toAddress, timeout)
        this.shellId = shellId
        this.commandLine = commandLine
        this.commandArguments = args
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
                'wsa:Action'('s:mustUnderstand': true, 'http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command')
                'wsman:MaxEnvelopeSize'('s:mustUnderstand': true, envelopSize)
                'wsa:MessageID'(composeUUID())
                'wsman:Locale'('s:mustUnderstand': false, 'xml:lang': locale)
                'wsman:SelectorSet' {
                    'wsman:Selector'(Name: 'ShellId', "${shellId}")
                }
                'wsman:OptionSet'('xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance') {
                    'wsman:Option'(Name: 'WINRS_CONSOLEMODE_STDIN', 'TRUE')
                }
                'wsman:OperationTimeout'(timeout)
            }
            's:Body' {
                'rsp:CommandLine'('xmlns:rsp': NMSP_URI_RSP) {
                    'rsp:Command'(commandLine)
                    commandArguments.each { argument ->
                        'rsp:Arguments'(argument)
                    }
                }
            }
        }

        writer.toString()
    }
}
