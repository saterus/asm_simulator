package edu.osu.cse.mmxi.sim.machine;

import edu.osu.cse.mmxi.common.Utilities;

public class Register {

    protected short registerValue;

    public Register(final Short fill) {
        registerValue = fill == null ? Utilities.randomShort() : fill;
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
