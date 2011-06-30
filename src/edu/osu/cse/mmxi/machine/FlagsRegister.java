package edu.osu.cse.mmxi.machine;

import edu.osu.cse.mmxi.machine.Machine.FillMode;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

/**
 * A specific Register that comes with an atomic operation for retrieving and incrementing
 * the Program Counter as an atomic operation.
 */
public class FlagsRegister extends Register {

    public FlagsRegister(final FillMode fill) {
        this(false, false, false);
        if (fill == FillMode.RAND)
            registerValue = (short) (1 << MemoryUtilities.randomShort() % 3);
        else
            registerValue = (short) (fill == FillMode.ZERO ? 2 : 1);
    }

    public FlagsRegister(final boolean initialN, final boolean initialZ,
        final boolean initialP) {
        super((short) ((initialN ? 4 : 0) + (initialZ ? 4 : 0) + (initialP ? 4 : 0)));
    }

    public boolean getN() {
        return getFlag(2);
    }

    public boolean getZ() {
        return getFlag(1);
    }

    public boolean getP() {
        return getFlag(0);
    }

    private boolean getFlag(final int index) {
        return (registerValue & 1 << index) != 0;
    }

    public void setN(final boolean val) {
        setFlag(2, val);
    }

    public void setZ(final boolean val) {
        setFlag(1, val);
    }

    public void setP(final boolean val) {
        setFlag(0, val);
    }

    private void setFlag(final int index, final boolean val) {
        if (val)
            registerValue |= 1 << index;
        else
            registerValue &= ~(1 << index);
    }

    public void setFlags(final Register register) {
        final short s = register.getValue();
        setN(s < 0);
        setZ(s == 0);
        setP(s > 0);
    }

    @Override
    public String toString() {
        return "FLAGS " + (getN() ? "n" : "") + (getZ() ? "z" : "") + (getN() ? "p" : "");
    }
}
