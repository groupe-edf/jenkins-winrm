package fr.edf.jenkins.plugins.windows.winrm

class WinRMException extends Exception {
    
    private WinRMException() {
        // Hide default constructor
    }

    public WinRMException(String message) {
        super(message)
    }
    public WinRMException(String message, Throwable t) {
        super(message,t)
    }
}
