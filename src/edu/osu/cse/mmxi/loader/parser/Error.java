package edu.osu.cse.mmxi.loader.parser;

public class Error extends Token {

    private final String      message;
    private final errorLevels level;

    public Error(final String m, final errorLevels l) {
        this(0, m, l);
    }

    public Error(final String m) {
        this(0, m, errorLevels.WARN);
    }

    public Error(final int line, final String m) {
        super(line);
        message = m;
        level = errorLevels.WARN;
    }

    public Error(final int line, final String m, final errorLevels l) {
        super(line);
        message = m;
        level = l;
    }

    public String getMessage() {
        return message;
    }

    public errorLevels getLevel() {
        return level;
    }

    @Override
    public String toString() {
        if (lineNumber == 0)
            return "Error: " + message + " errorLevel: " + level.toString();
        else
            return "Error, line " + lineNumber + ": " + message + " errorLevel: "
                + level.toString();
    }
}
