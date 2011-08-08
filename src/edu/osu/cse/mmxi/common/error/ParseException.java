package edu.osu.cse.mmxi.common.error;

/**
 * Very generalized class for handling exceptions in the McMoxell machine. Essentially
 * wraps the Error class and is used within the machine to throw errors up to the main
 * execution for error handling and integration to the UI.
 * 
 */
public class ParseException extends Exception {

    /**
     * The error being thrown.
     * 
     * @see Error
     */
    private Error err;

    public ParseException(final int line, final ErrorCodes errCode) {
        err = new Error(line, errCode);
    }

    public ParseException(final int line, final ErrorCodes errCode, final String msg) {
        super(msg);
        err = new Error(line, msg, errCode);
    }

    public ParseException(final ErrorCodes errCode, final String msg) {
        super(msg);
        err = new Error(msg, errCode);
    }

    public ParseException(final ErrorCodes errCode) {
        err = new Error(errCode);
    }

    public ParseException(final int line, final String msg) {
        super(msg);
        err = new Error(line, msg);
    }

    public ParseException(final String msg) {
        super(msg);
    }

    /**
     * Wrapper for Error.getLine()
     * 
     * @return return the Error.getLine()
     */
    public int getLine() {
        return err.getLine();
    }

    /**
     * Wrapper for Error.getCode()
     * 
     * @return Return the Error.getCode()
     */
    public ErrorCodes getErrorCode() {
        return err.getCode();
    }

    /**
     * Get the actual error that was thrown.
     * 
     * @return Error thrown.
     */
    public Error getError() {
        return err;
    }
}
