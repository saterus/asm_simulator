package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Text extends Token {

    private final short address;
    private final short value;

    public Text(final int line, final short address, final short value) {
        super(line);
        this.address = address;
        this.value = value;
    }

    public short getAddress() {
        return address;
    }

    public short getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Text, line " + lineNumber + ": (0x" + MemoryUtilities.shortToHex(address)
                + ", 0x" + MemoryUtilities.shortToHex(value) + ")";
    }

}
