package edu.osu.cse.mmxi.asm;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.error.ParseException;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;

public class Literal extends Symbol {
    public static SortedMap<Short, Literal> table    = new TreeMap<Short, Literal>();
    public static boolean                   complete = false;
    public short                            contents;

    public static Literal getLiteral(final short value) {
        if (!table.containsKey(value))
            table.put(value, new Literal(value));
        return table.get(value);
    }

    public short getIndex() {
        return (short) (table.size() - table.tailMap(contents).size());
    }

    public void fill() {
        if (Literal.complete && value == null)
            try {
                value = ArithmeticParser.parseF(":0 + :1", ":END", getIndex());
            } catch (final ParseException e) {
                // won't happen
                System.err.println("wtf");
            }
    }

    @Override
    public Short evaluate(final Set<Symbol> used) {
        fill();
        return super.evaluate(used);
    }

    private Literal(final short v) {
        super("=#" + v);
        contents = v;
    }
}
