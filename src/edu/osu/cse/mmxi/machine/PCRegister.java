package edu.osu.cse.mmxi.machine;

/**
 * A specific Register that comes with an atomic operation for retrieving and incrementing
 * the Program Counter as an atomic operation.
 */
public class PCRegister extends Register {

    public PCRegister() {
        this((short) 0);
    }

    public PCRegister(final short initialValue) {
        super(RegisterType.PC, initialValue);
    }

    /**
     * Retrieves the value from the PC Register, then increments it as an atomic
     * operation.
     * 
     * @return the relative memory offset from the current page of the next instruction to
     *         be executed.
     */
    public short nextInstruction() {
        return this.registerValue++;
    }

}
