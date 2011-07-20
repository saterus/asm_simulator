package edu.osu.cse.mmxi.table;

import java.util.HashMap;
import java.util.Map;

public class LiteralTable {

    private final Map<Integer, Integer> table;

    private LiteralTable() {

        table = new HashMap<Integer, Integer>();

    }

    public void add_to(final int address, final int value) {
        // check if we need to save two things on
        // the same address
        table.put(address, value);
    }

    public int get_value(final int address) {

        if (table.containsKey(address))
            return table.get(address);
        else
            return 0;
    }

}
