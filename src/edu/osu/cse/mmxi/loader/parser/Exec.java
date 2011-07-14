package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Exec extends Token {

    private final short address;

    public Exec(final int line, final short address) {
        super(line);
        this.address = address;
    }

    public short getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Exec, " + super.toString() + ": 0x"
            + MemoryUtilities.uShortToHex(address);
    }

}
