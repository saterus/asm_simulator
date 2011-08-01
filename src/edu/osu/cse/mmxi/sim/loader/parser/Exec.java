package edu.osu.cse.mmxi.sim.loader.parser;

import edu.osu.cse.mmxi.common.Utilities;

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
        return "Exec, " + super.toString() + ": 0x" + Utilities.uShortToHex(address);
    }

}
