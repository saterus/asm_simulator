package edu.osu.cse.mmxi.loader.parser;

public class Token {
    protected final int lineNumber;

    public Token(final int line) {
        lineNumber = line;
    }

    public int getLine() {
        return lineNumber;
    }
}
