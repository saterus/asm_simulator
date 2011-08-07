package edu.osu.cse.mmxi.asm.line;

import edu.osu.cse.mmxi.asm.Symbol;

public class Label {
    public Symbol symb;

    public Label(final String name) {
        symb = Symbol.getSymb(name);
    }
}
