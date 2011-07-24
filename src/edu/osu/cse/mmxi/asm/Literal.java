package edu.osu.cse.mmxi.asm;

import java.util.SortedMap;
import java.util.TreeMap;

public class Literal {
    public static int                       tableStart = -1;
    public static SortedMap<Short, Literal> table      = new TreeMap<Short, Literal>();
    public short                            value;

    public static Literal getLiteral(final short value) {
        if (!table.containsKey(value))
            table.put(value, new Literal(value));
        return table.get(value);
    }

    public int getLocation() {
        if (tableStart == -1)
            return -1;
        return tableStart + getIndex() & 0xFFFF;
    }

    public int getIndex() {
        return table.size() - table.tailMap(value).size();
    }

    private Literal(final short v) {
        value = v;
    }
}
