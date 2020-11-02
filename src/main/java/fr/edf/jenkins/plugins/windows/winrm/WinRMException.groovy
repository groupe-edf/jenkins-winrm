package fr.edf.jenkins.plugins.windows.winrm

/**
 * Exception throws were an error occured in {@link fr.edf.jenkins.plugins.windows.winrm}
 * @author Mathieu Delrocq
 *
 */
class WinRMException extends Exception {
    
    public static final String FORMATTED_MESSAGE = "Error during %s command, protocol : %s, code : %s, description : %"
    
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
