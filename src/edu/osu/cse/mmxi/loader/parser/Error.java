package edu.osu.cse.mmxi.loader.parser;

public class Error implements Token {

    private final String message;

    public Error(final String m) {
        this.message = m;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public short getAddress() {
        return 0;
    }

    @Override
    public short getValue() {
        return 0;
    }

    @Override
    public String toString() {
        return "Error: " + this.message;
    }

}
