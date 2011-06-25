package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Header implements Token {

    private final String name;
    private final short  address;
    private final short  value;

    public Header(final String name, final short beginAddress, final short lengthOffset) {
        this.name = name;
        this.address = beginAddress;
        this.value = lengthOffset;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public short getAddress() {
        return this.address;
    }

    @Override
    public short getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "Header: (" + this.name + ", 0x"
                + MemoryUtilities.shortToHex(this.address) + ", 0x"
                + MemoryUtilities.shortToHex(this.value) + ")";
    }

}
