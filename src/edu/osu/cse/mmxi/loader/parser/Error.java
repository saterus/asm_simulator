package edu.osu.cse.mmxi.loader.parser;

public class Error extends Token {

    private String     message = "None";
    private ErrorCodes code    = ErrorCodes.UNKNOWN;

    public Error(final ErrorCodes c) {
        super(-1);
        code = c;
    }

    public Error(final String m) {
        this(-1, m, ErrorCodes.UNKNOWN);
    }

    public Error(final String m, final ErrorCodes c) {
        this(-1, m, c);
    }

    public Error(final String m, final ErrorLevels l) {
        this(-1, m, ErrorCodes.UNKNOWN);
    }

    public Error(final int line, final String m) {
        super(line);
        message = m;
        code = ErrorCodes.UNKNOWN;
    }

    public Error(final int line, final ErrorCodes c) {
        this(0, "None", c);
    }

    public Error(final int line, final String m, final ErrorCodes c) {
        super(line);
        message = m;
        code = c;
    }

    public String getMessage() {
        return message;
    }

    public ErrorLevels getLevel() {
        return code.getErrLevel();
    }

    @Override
    public String toString() {
        if (lineNumber == -1)
            return "ERROR: LINE none : " + code.toString() + "\n\tdetails: " + message;
        else
            return "ERROR: LINE " + getLine() + " : " + code.toString() + "\n\t"
                + "details: " + message;
    }
}
