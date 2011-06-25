package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Exec implements Token {

    private final short address;

    public Exec(final short address) {
        this.address = address;
    }

    @Override
    public short getAddress() {
        return this.address;
    }

    @Override
    public short getValue() {
        return 0;
    }

    @Override
    public String toString() {
        return "Exec: 0x" + MemoryUtilities.shortToHex(this.address);
    }

}
