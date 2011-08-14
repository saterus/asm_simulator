package edu.osu.cse.mmxi.asm.error;

import edu.osu.cse.mmxi.common.error.ParseException;

public class RecursionException extends RuntimeException {
    public RecursionException(final String msg) {
        super(msg);
    }

    public ParseException toParseException() {
        return new ParseException(AsmCodes.AP_RECURSION, getMessage());
    }
}
