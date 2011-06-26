package edu.osu.cse.mmxi.loader.parser;

public class Error extends Token {

    private final String message;

    public Error(final String m) {
        this(0, m);
    }

    public Error(final int line, final String m) {
        super(line);
        message = m;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (lineNumber == 0)
            return "Error: " + message;
        else
            return "Error, line " + lineNumber + ": " + message;
    }

}
