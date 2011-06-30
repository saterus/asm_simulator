package edu.osu.cse.mmxi.machine;

import edu.osu.cse.mmxi.machine.Machine.FillMode;
import edu.osu.cse.mmxi.machine.memory.MemoryUtilities;

public class Register {

    protected short registerValue;

    public Register(final FillMode fill) {
        switch (fill) {
        case ZERO:
            registerValue = 0;
            break;
        case FILL:
            registerValue = (short) 0xED6E;
            break;
        case RAND:
            registerValue = MemoryUtilities.randomShort();
        }
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
