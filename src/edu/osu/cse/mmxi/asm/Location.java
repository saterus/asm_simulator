package edu.osu.cse.mmxi.asm;

import java.util.Map;
import java.util.Map.Entry;

import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;

/**
 * This is the location counter representation for a given symbol.
 */
public class Location {
    public boolean isRelative = false;
    public int     address    = 0;

    /**
     * Initialize the Location counter.
     * 
     * @param r
     *            True if the is relative, false if absolute.
     * @param addr
     *            The value of the location counter. This is the absolute position in
     *            memory.
     */
    public Location(final boolean r, final int addr) {
        isRelative = r;
        address = addr;
    }

    /**
     * Convert the location counter (in memory) to an offset.
     * 
     * @param se
     *            The Symbol to determin the ofset from.
     * @return
     */
    public static Location convertToRelative(final SymbolExpression se) {
        // Assumes se has already been simplified (by simplify())
        if (se == null)
            return null;
        final Map<SymbolExpression, Integer> terms = ArithmeticParser.getTerms(se, true);
        final Location loc = new Location(false, 0);
        for (final Entry<SymbolExpression, Integer> i : terms.entrySet())
            if (i.getKey() == Symbol.getSymb(":START"))
                if (i.getValue() == 1)
                    loc.isRelative = true;
                else
                    return null;
            else if (i.getKey() instanceof NumExp)
                loc.address = i.getValue() * i.getKey().evaluate();
            else
                return null;
        return loc;
    }
}
