package edu.osu.cse.mmxi.loader.parser;

import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Header extends Token {

    private final String name;
    private final short  begin;
    private final short  length;

    public Header(final int line, final String name, final short beginAddress,
            final short lengthOffset) {
        super(line);
        this.name = name;
        begin = beginAddress;
        length = lengthOffset;
    }

    public String getName() {
        return name;
    }

    public short getBegin() {
        return begin;
    }

    public short getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "Header, line " + lineNumber + ": (" + name + ", 0x"
                + MemoryUtilities.shortToHex(begin) + ", 0x"
                + MemoryUtilities.shortToHex(length) + ")";
    }

    /**
     * Tests whether a given address is in the segment defined by this header.
     * 
     * @param address
     *            the address in memory
     * @return if the address is between begin and begin + length, treated cyclically.
     */
    public boolean isWithinBounds(final short address) {
        return (address - begin & 0xffff) < length;
    }
}
