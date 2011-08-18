package edu.osu.cse.mmxi.sim.loader.parser;

import edu.osu.cse.mmxi.common.Utilities;

public class Text extends Token {

    private final short  address;
    private final short  value;
    private final byte   m;
    private final String ext;

    public Text(final int line, final int sline, final short address, final short value,
        final int m, final String ext) {
        super(line, sline);
        this.address = address;
        this.value = value;
        this.m = (byte) m;
        this.ext = ext;
    }

    public short getAddress() {
        return address;
    }

    public short getAddress(final short pla, Map<String, Location>) {
        return address;
    }

    public short getValue() {
        return value;
    }

    public short getMask() {
        return (short)(m == 1 ? -1 : m == 0 ? 0x1FF : 0);
    }

    public String getExternal() {
        return ext;
    }

    @Override
    public String toString() {
        return "Text, " + super.toString() + ": (0x" + Utilities.uShortToHex(address)
            + ", 0x" + Utilities.uShortToHex(value) + ")";
    }

}
