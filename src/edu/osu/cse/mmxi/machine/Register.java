package edu.osu.cse.mmxi.machine;

public class Register {

    public enum RegisterType {
        Z, N, P, // Zero Negative Positive Flags
        PC, // Program Counter
        R0, R1, R2, R3, R4, R5, R6, R7
        // General Purpose Registers
    };

    protected short              registerValue;
    protected final RegisterType type;

    public Register(final RegisterType type) {
        this(type, (short) 0);
    }

    public Register(final RegisterType type, final short initialValue) {
        this.type = type;
        this.registerValue = initialValue;
    }

    /**
     * @return the registerValue
     */
    public short getRegisterValue() {
        return this.registerValue;
    }

    /**
     * @param registerValue
     *            the registerValue to set
     */
    public void setRegisterValue(final short registerValue) {
        this.registerValue = registerValue;
    }

    /**
     * @return the type
     */
    public RegisterType getType() {
        return this.type;
    }
}
