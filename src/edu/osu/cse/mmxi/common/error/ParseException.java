package edu.osu.cse.mmxi.common.error;

public class ParseException extends Exception {
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

    public int getLine() {
        return err.getLine();
    }

    public ErrorCodes getErrorCode() {
        return err.getCode();
    }

    public Error getError() {
        return err;
    }
}
