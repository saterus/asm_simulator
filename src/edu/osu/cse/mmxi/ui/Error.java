package edu.osu.cse.mmxi.ui;

public class Error {

    private final int        line;
    private final String     message;
    private final ErrorCodes code;

    public Error(final ErrorCodes c) {
        this(-1, null, c);
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
        this(line, m, ErrorCodes.UNKNOWN);
    }

    public Error(final int line, final ErrorCodes c) {
        this(line, null, c);
    }

    public Error(final int line, final String m, final ErrorCodes c) {
        this.line = line;
        message = m;
        code = c;
    }

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public ErrorLevels getLevel() {
        return code.getLevel();
    }

    @Override
    public String toString() {
        return code.getLevel() + " " + code.getCode()
            + (line == -1 ? "" : ": Line " + line) + (code == null ? "" : ": " + code)
            + (message == null ? "" : "\n\tdetails: " + message) + "\n";
    }
}
