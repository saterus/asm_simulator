package edu.osu.cse.mmxi.common.error;

public class Error {
    private int              line;
    private String           message;
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

    public int getLine() {
        return line;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCodes getCode() {
        return code;
    }

    public ErrorLevels getLevel() {
        return code.getLevel();
    }

    public Error setLine(final int line) {
        this.line = line;
        return this;
    }

    public Error setMessage(final String msg) {
        message = msg;
        return this;
    }

    public Error appendMsg(final String msg) {
        return setMessage(msg + getMessage());
    }

    @Override
    public String toString() {
        String s = code.getLevel() + " " + code.getCode()
            + (line == -1 ? "" : ": Line " + line);
        if (code.getMsg() == null)
            s += message == null ? "" : ": " + message;
        else
            s += ": " + code.getMsg()
                + (message == null ? "" : "\n\tdetails: " + message);
        return s + "\n";
    }
}
