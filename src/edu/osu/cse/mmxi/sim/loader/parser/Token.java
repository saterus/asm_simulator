package edu.osu.cse.mmxi.sim.loader.parser;

public class Token {
    protected final int    lineNumber, sourceLineNumber;
    protected final String sourceFile;

    public Token(final int line) {
        lineNumber = line;
        sourceLineNumber = -1;
        sourceFile = null;
    }

    public Token(final int line, final int source, final String file) {
        lineNumber = line;
        sourceLineNumber = source;
        sourceFile = file;
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
        if (sourceFile == null)
            return "line " + lineNumber;
        else
            return sourceFile + "[" + sourceLineNumber + "]";
    }
}
