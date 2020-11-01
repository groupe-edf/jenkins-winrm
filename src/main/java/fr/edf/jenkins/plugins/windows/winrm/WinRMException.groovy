package fr.edf.jenkins.plugins.windows.winrm

class WinRMException extends Exception {

    public WinRMException(String message) {
        super(message)
    }
    public WinRMException(String message, Throwable t) {
        super(message,t)
    }
}
