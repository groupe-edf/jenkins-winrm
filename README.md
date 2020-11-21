# jenkins-winrm
Dev tool to execute command on a remote Windows machine with Jenkins using WinRM specification.

See [4.7.1](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/7f4a1f31-47d8-4599-a23b-c3834ffae21f), [4.7.2](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/d537264b-fda8-4694-a518-ae0085d92441), [4.7.3](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/b8d1b0bd-484e-4ac0-a9dd-9244f13697db), [4.7.5](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/ded708a2-e24e-4284-aac8-35c14801c21b) and [4.7.9](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/4b133c1c-9102-43eb-83ac-60001cebb4a6) of [Microsoft WSMV Documentation](https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/41409c84-afc9-4886-a37e-992e8d1fcced)

## Goal
Build a Winrm client working with NTLM authentication and compatible with Jenkins

## Usage
An usage example can be found in this file : **[JenkinsWinRMUsageTest](https://github.com/groupe-edf/jenkins-winrm/blob/main/src/test/java/org/jenkinsci/plugins/JenkinsWinRMUsageTest.groovy)**
 
## References
This project contains code under Apache-2.0 License from :

 - [cloudsoft/winrm4j](https://github.com/cloudsoft/winrm4j) for authentication process
 - [sshoogr/groovy-winrm-client](https://github.com/sshoogr/groovy-winrm-client) for request process
 

