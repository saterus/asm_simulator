package edu.osu.cse.mmxi.asm;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * This is the literal table row representation.
 * 
 * Representation is in a sortedMap<Short, Literal>
 * 
 */
public class Literal extends Symbol {
    public static SortedMap<Short, Literal> table    = new TreeMap<Short, Literal>();
    public static boolean                   complete = false;
    public short                            contents;

    /**
     * Return the literal representation
     * 
     * @param value
     *            The literal to pull the representation of.
     * @return
     */
    public static Literal getLiteral(final short value) {
        if (!table.containsKey(value))
            table.put(value, new Literal(value));
        return table.get(value);
    }

    /**
     * Get the next index of the table entry for a literal
     * 
     * @return
     */
    public short getIndex() {
        return (short) (table.size() - table.tailMap(contents).size());
    }

    /**
     * Fill the lteral value.
     */
    public void fill() {
        if (Literal.complete && value == null)
            try {
                value = ArithmeticParser.parseF(":0 + :1", ":END", getIndex());
            } catch (final ParseException e) {
                // won't happen
                throw new RuntimeException("wtf");
            }
    }

    /**
     * Wrapper for the fill() and calls the Symbol evaluate for the given symbol.
     */
    @Override
    public Short evaluate(final Set<Symbol> used) {
        fill();
        return super.evaluate(used);
    }

    /**
     * Set the content of the literal
     * 
     * @param v
     *            The short literal value
     */
    private Literal(final short v) {
        super("=#" + v);
        contents = v;
    }
}
