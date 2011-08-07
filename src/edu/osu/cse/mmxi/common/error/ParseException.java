package edu.osu.cse.mmxi.common.error;


public class ParseException extends Exception {

    private Integer   line = -1;
    // private String msg = null;
    private ErrorCodes err  = new ErrorCodes.Unknown();

    public ParseException() {
    }

    public ParseException(final Integer lineNumber) {
        line = lineNumber;
    }

    public ParseException(final Integer lineNumber, final ErrorCodes errCode) {
        line = lineNumber;
        err = errCode;
    }

    public ParseException(final Integer lineNumber, final ErrorCodes errCode,
        final String msg) {
        super(msg);
        line = lineNumber;
        err = errCode;
    }

    public ParseException(final ErrorCodes errCode, final String msg) {
        super(msg);
        err = errCode;
    }

    public ParseException(final ErrorCodes errCode) {
        err = errCode;
    }

    public ParseException(final String msg, final Integer lineNumber) {
        super(msg);
        line = lineNumber;
        // msg = message;
    }

    public ParseException(final String msg) {
        super(msg);
    }

    public Integer getLineNumber() {
        return line;
    }

    /*
     * public String getMsgDetails() { return msg; }
     */

    public ErrorCodes getErrorCode() {
        return err;
    }
}
