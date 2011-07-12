package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.Register;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ADD;

/**
 * Tests the instruction directly, instruction parser should probably be checked as well.
 * 
 * @author Will Smelser
 * 
 */
public class InstructionTest {
    // private final short help = ///Short.parseShort("FEDCBA9876543210", 16);
    private final short maskAdd    = Short.parseShort("000100000000000", 2);
    private final short maskAddimm = Short.parseShort("000100000100000", 2);
    private final short maskAnd    = Short.parseShort("010100000000000", 2);
    private final short maskAndimm = Short.parseShort("010100000100000", 2);
    private final short maskBrx    = Short.parseShort("000000000000000", 2);
    private final short maskDbug   = Short.parseShort("100000000000000", 2);
    private final short maskJsr    = Short.parseShort("010000000000000", 2);
    private final short maskJsrR   = Short.parseShort("110000000000000", 2);
    private final short maskLd     = Short.parseShort("001000000000000", 2);
    private final short maskLdi    = Short.parseShort("101000000000000", 2);
    private final short maskLdr    = Short.parseShort("011000000000000", 2);
    private final short maskLea    = Short.parseShort("111000000000000", 2);
    private final short maskNot    = Short.parseShort("100100000000000", 2);
    private final short maskRet    = Short.parseShort("110100000000000", 2);
    private final short maskSt     = Short.parseShort("001100000000000", 2);
    private final short maskSti    = Short.parseShort("101100000000000", 2);
    private final short maskStr    = Short.parseShort("011100000000000", 2);
    private final short maskTrap   = Short.parseShort("111100000000000", 2);

    private byte        r0         = 0;
    private byte        r1         = 1;
    private byte        r2         = 2;
    private byte        r3         = 3;
    private byte        r4         = 4;
    private byte        r5         = 5;
    private byte        r6         = 6;
    private byte        r7         = 7;

    private class myMachine extends Machine {
        public void setRegister(final byte reg, final int i) {
            registers[reg].setValue((short) i);
        }
    }

    // private final InstructionParser parser = new InstructionParser();
    private final myMachine m = new myMachine();

    // silly eclipse was setting all registers to final!
    @Before
    public final void setUp() {
        r0 = 0;
        r1 = 1;
        r2 = 2;
        r3 = 3;
        r4 = 4;
        r5 = 5;
        r6 = 6;
        r7 = 7;
    }

    @Test
    public final void AddSimpleTest() {
        m.setRegister(r0, 1);
        m.setRegister(r1, 1);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 2, res.getValue());
    }

    // test that the value wraps
    @Test
    public final void AddLimitTest() {
        m.setRegister(r0, Short.MAX_VALUE);
        m.setRegister(r1, 1);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", Short.MIN_VALUE, res.getValue());
    }

    // test 0 + 0
    @Test
    public final void AddZeroTest() {
        m.setRegister(r0, 0);
        m.setRegister(r1, 0);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", 0, res.getValue());
    }
}
