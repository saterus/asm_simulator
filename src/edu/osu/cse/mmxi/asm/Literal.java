package edu.osu.cse.mmxi.asm;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.symb.Operator;

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
            value = new OpExp(Operator.PLUS, Symbol.getSymb(":END"), new NumExp(
                getIndex()));
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
