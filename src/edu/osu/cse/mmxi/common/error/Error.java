package edu.osu.cse.mmxi.common.error;

/**
 * Represents an error entry. Can hold the line the error occurred on. Message and context
 * of the message. All errors must reference and errorCode.
 * 
 */
public class Error {
    /**
     * The line the error occurred on. Optional.
     */
    private int              line;
    /**
     * Error message and context. Give more explicit information that just the error code.
     */
    private String           message, context;

    /**
     * The general error code value.
     */
    private final ErrorCodes code;

    public Error(final ErrorCodes c) {
        this(-1, null, c);
    }

    public Error(final String m) {
        this(-1, m, new ErrorCodes.Unknown());
    }

    public Error(final String m, final ErrorCodes c) {
        this(-1, m, c);
    }

    public Error(final int line, final String m) {
        this(line, m, new ErrorCodes.Unknown());
    }

    public Error(final int line, final ErrorCodes c) {
        this(line, null, c);
    }

    public Error(final int line, final String m, final ErrorCodes c) {
        this.line = line;
        message = m;
        code = c;
    }

    /**
     * wrapper for reteiving the line number of the error. -1 is returned if there was no
     * line number given when creating the error.
     * 
     * @return The line number of the error.
     */
    public int getLine() {
        return line;
    }

    /**
     * Get the error message. Can be the empty string or null.
     * 
     * @return String The error message details.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the error code. ErrorCodes will never be null. Generic error code of 999 given
     * at a minimum.
     * 
     * @return ErrorCodes enum value for this error.
     */
    public ErrorCodes getCode() {
        return code;
    }

    /**
     * Get the level of the error code. Wrapper for the enum for ErrorCodes.getLevel()
     * 
     * @return The error code level.
     * @see ErrorLevels
     */
    public ErrorLevels getLevel() {
        return code.getLevel();
    }

    /**
     * Set the line number.
     * 
     * @param line
     *            The line number of the error
     * @return
     */
    public Error setLine(final int line) {
        this.line = line;
        return this;
    }

    /**
     * Set the error message
     * 
     * @param msg
     *            String of the message details.
     * @return Returns this.
     */
    public Error setMessage(final String msg) {
        message = msg;
        return this;
    }

    /**
     * Set the context of the error. This is further detail to the error.
     * 
     * @param ct
     *            A string with context information regading the error.
     * @return Returns this.
     */
    public Error setContext(final String ct) {
        context = ct;
        return this;
    }

    /**
     * Append a message to the current message
     * 
     * @param msg
     *            The message to append.
     * @return Returns this.
     */
    public Error appendMsg(final String msg) {
        return setMessage(getMessage() + msg);
    }

    /**
     * Get a formated string representation of the error.
     * 
     * @return string.
     */
    @Override
    public String toString() {
        String s = code.getLevel() + " " + code.getCode()
            + (line == -1 ? "" : ": Line " + line);
        if (code.getMsg() == null)
            s += message == null ? "" : ": " + message;
        else
            s += ": " + code.getMsg()
                + (message == null ? "" : "\n\tdetails: " + message);
        return s + (context == null ? "\n" : "\n\tContext: " + context + "\n");
    }
}
