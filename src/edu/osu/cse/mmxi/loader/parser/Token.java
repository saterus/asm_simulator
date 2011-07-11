package edu.osu.cse.mmxi.loader.parser;

public class Token {
    protected final int lineNumber;

    public Token(final int line) {
        lineNumber = line;
    }

    public String getLine() {
        String rtn = String.valueOf(lineNumber);
        int i = rtn.length();

        while (i < 4) {
            rtn = "0" + rtn;
            i++;
        }

        return rtn;
    }
}
