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

    public Text(final int line, final int sline, final String file, final short address,
        final short value) {
        super(line, sline, file);
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
        return "Text, " + super.toString() + ": (0x"
            + MemoryUtilities.uShortToHex(address) + ", 0x"
            + MemoryUtilities.uShortToHex(value) + ")";
    }

}
