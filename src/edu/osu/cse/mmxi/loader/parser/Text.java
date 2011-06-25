package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Text implements Token {

    private final short address;
    private final short value;

    public Text(final short address, final short value) {
        this.address = address;
        this.value = value;
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
        return "Text: (0x" + MemoryUtilities.shortToHex(this.address) + ", 0x"
                + MemoryUtilities.shortToHex(this.value) + ")";
    }

}
