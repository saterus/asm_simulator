package edu.osu.cse.mmxi.machine;

/**
 * A specific Register that comes with an atomic operation for retrieving and incrementing
 * the Program Counter as an atomic operation.
 */
public class FlagsRegister extends Register {

    public FlagsRegister() {
        this(false, false, false);
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
        if (val) {
            registerValue |= 1 << index;
        } else {
            registerValue &= ~(1 << index);
        }
    }

    public void setFlags(final short register) {
        setN(register < 0);
        setZ(register == 0);
        setP(register > 0);
    }
}
