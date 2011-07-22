package edu.osu.cse.mmxi.sim.machine;

import edu.osu.cse.mmxi.common.MemoryUtilities;

public class Register {

    protected short registerValue;

    public Register(final int fill) {
        registerValue = fill == -1 ? MemoryUtilities.randomShort() : (short) fill;
    }

    public Register(final short initialValue) {
        registerValue = initialValue;
    }

    /**
     * @return the registerValue
     */
    public short getValue() {
        return registerValue;
    }

    /**
     * @param newValue
     *            the registerValue to set
     */
    public void setValue(final short newValue) {
        registerValue = newValue;
    }

    /**
     * Retrieves the value from the register, then increments it as an atomic operation.
     * 
     * @return the old value of the register.
     */
    public short increment() {
        return registerValue++;
    }
}
