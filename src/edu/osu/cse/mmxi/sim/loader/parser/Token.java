package edu.osu.cse.mmxi.sim.loader.parser;

public class Token {
    protected final int lineNumber, sourceLineNumber;

    public Token(final int line) {
        lineNumber = line;
        sourceLineNumber = 0;
    }

    public Token(final int line, final int source) {
        lineNumber = line;
        sourceLineNumber = source;
    }

    public int getLine() {
        return lineNumber;
    }

    public int getSLine() {
        return sourceLineNumber;
    }

    public int getSFile() {
        return sourceLineNumber;
    }

    @Override
    public String toString() {
        if (sourceLineNumber == 0)
            return "line " + lineNumber;
        else
            return "[" + sourceLineNumber + "]";
    }
}
