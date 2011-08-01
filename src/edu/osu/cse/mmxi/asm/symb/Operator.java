package edu.osu.cse.mmxi.asm.symb;

public enum Operator {
    GROUP(0, "(", 5),

    IF(1, "?", 3),

    OR(2, "|", 3),

    XOR(3, "^", 3),

    AND(4, "&", 3),

    USHR(5, ">>>", 3), SHR(5, ">>", 3), SHL(5, "<<", 3),

    PLUS(6, "+", 7), MINUS(6, "-", 7),

    // this one is out of prec. order because TIMES has an operator that is a prefix of
    // this one, so POWER must come first (same with USHR and SHR)
    POWER(8, "**", 2),

    TIMES(7, "*", 3), DIV(7, "/", 3), MOD(7, "%", 3),

    // unary operators automatically get top precedence
    NOT(9, "~", 4), HASH(9, "#", 4), LIT(9, "=", 4);

    public int    prec;
    public String value;
    public boolean unary, binary, lAssoc;

    Operator(final int p, final String v, final int ubl) {
        prec = p;
        value = v;
        unary = (ubl & 4) != 0;
        binary = (ubl & 2) != 0;
        lAssoc = (ubl & 1) != 0;
    }

    public static Operator get(final String v) {
        for (final Operator o : values())
            if (v.equals(o.value))
                return o;
        return null;
    }
}
