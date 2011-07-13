package edu.osu.cse.mmxi.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.Register;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ADD;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.ADDimm;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.AND;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.BRx;
import edu.osu.cse.mmxi.machine.interpreter.instructions.Instruction.DBUG;

/**
 * Tests the instruction directly, instruction parser should probably be checked as well.
 * 
 * @author Will Smelser
 * 
 */
public class InstructionTest {
    // private final short help = ///Short.parseShort("FEDCBA9876543210", 16);
    private final short   maskAdd    = Short.parseShort("000100000000000", 2);
    private final short   maskAddimm = Short.parseShort("000100000100000", 2);
    private final short   maskAnd    = Short.parseShort("010100000000000", 2);
    private final short   maskAndimm = Short.parseShort("010100000100000", 2);
    private final short   maskBrx    = Short.parseShort("000000000000000", 2);
    private final short   maskDbug   = Short.parseShort("100000000000000", 2);
    private final short   maskJsr    = Short.parseShort("010000000000000", 2);
    private final short   maskJsrR   = Short.parseShort("110000000000000", 2);
    private final short   maskLd     = Short.parseShort("001000000000000", 2);
    private final short   maskLdi    = Short.parseShort("101000000000000", 2);
    private final short   maskLdr    = Short.parseShort("011000000000000", 2);
    private final short   maskLea    = Short.parseShort("111000000000000", 2);
    private final short   maskNot    = Short.parseShort("100100000000000", 2);
    private final short   maskRet    = Short.parseShort("110100000000000", 2);
    private final short   maskSt     = Short.parseShort("001100000000000", 2);
    private final short   maskSti    = Short.parseShort("101100000000000", 2);
    private final short   maskStr    = Short.parseShort("011100000000000", 2);
    private final short   maskTrap   = Short.parseShort("111100000000000", 2);

    private final byte    r0         = 0;
    private final byte    r1         = 1;
    private final byte    r2         = 2;
    private final byte    r3         = 3;
    private final byte    r4         = 4;
    private final byte    r5         = 5;
    private final byte    r6         = 6;
    private final byte    r7         = 7;

    // private final InstructionParser parser = new InstructionParser();
    private final Machine m          = new Machine();

    // silly eclipse was setting all registers to final!
    @Before
    public final void setUp() {

    }

    /**
     * ADD
     */

    @Test
    public final void AddSimpleTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 2, res.getValue());
    }

    // test that the value wraps
    @Test
    public final void AddLimitTest() {
        m.getRegister(r0).setValue(Short.MAX_VALUE);
        m.getRegister(r1).setValue((short) 1);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", Short.MIN_VALUE, res.getValue());
    }

    // test 0 + 0
    @Test
    public final void AddZeroTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 0);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", 0, res.getValue());
    }

    // test 1 + 0
    @Test
    public final void AddZero2Test() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 0);

        final ADD add = new ADD(r0, r0, r1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", 1, res.getValue());
    }

    /**
     * ADD IMMEDIATE
     */

    @Test
    public final void AddSimpleImmTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        // m.setMemory((byte) 0, (short) 1, (short) 1);

        final ADDimm add = new ADDimm(r0, r1, 1);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 2, res.getValue());
    }

    @Test
    public final void AddSimpleLimitImmTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        // m.setMemory((byte) 0, (short) 1, (short) 15);

        final ADDimm add = new ADDimm(r0, r1, 15);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 16, res.getValue());
    }

    @Test
    public final void AddSimpleLimit2ImmTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        m.setMemory((byte) 0, (short) 1, (short) 1);

        final ADDimm add = new ADDimm(r0, r1, 16);
        add.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) -15, res.getValue());
    }

    /**
     * AND TESTING
     */

    @Test
    public final void AndNotNotTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 0);

        final AND and = new AND(r0, r1, r0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndNotTrueTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 1);

        final AND and = new AND(r0, r1, r0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndTrueTrueTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        final AND and = new AND(r0, r1, r0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 1, res.getValue());
    }

    /*
     * And testing with more than just right most bit
     */

    @Test
    public final void AndNotTrue2Test() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 2);

        final AND and = new AND(r0, r1, r0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndTrueTrue2Test() {
        m.getRegister(r0).setValue((short) 2);
        m.getRegister(r1).setValue((short) 2);

        final AND and = new AND(r0, r1, r0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 2, res.getValue());
    }

    /**
     * IMM
     */

    @Test
    public final void AndNotNotImmTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 0);

        final AND and = new AND(r0, r0, 0);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndNotTrueImmTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 1);

        final AND and = new AND(r0, r0, 1);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndTrueTrueImmTest() {
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 1);

        final AND and = new AND(r0, r0, 1);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 1, res.getValue());
    }

    /*
     * And testing with more than just right most bit
     */

    @Test
    public final void AndNotTrue2ImmTest() {
        m.getRegister(r0).setValue((short) 0);
        m.getRegister(r1).setValue((short) 2);

        final AND and = new AND(r0, r0, 2);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 0, res.getValue());
    }

    @Test
    public final void AndTrueTrue2ImmTest() {
        m.getRegister(r0).setValue((short) 2);
        m.getRegister(r1).setValue((short) 2);

        final AND and = new AND(r0, r1, 2);
        and.execute(m);

        final Register res = m.getRegister(r0);

        assertEquals("equal", (short) 2, res.getValue());
    }

    /**
     * BranchX MAX = 111111111 = 511 = 0x1FF; atleast 1 of NZP must be set;
     */
    @Test
    public final void BranchAlwaysSimpleTest() {
        m.getPCRegister().setValue((short) 0);

        // 1 register must always be set
        m.getFlags().setN(true);
        m.getFlags().setP(false);
        m.getFlags().setZ(false);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(7, 10);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 10, pc.getValue());
    }

    @Test
    public final void BranchNbitSimpleTest() {
        m.getPCRegister().setValue((short) 0);

        m.getFlags().setN(true);
        m.getFlags().setP(false);
        m.getFlags().setZ(false);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(4, 10);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 10, pc.getValue());
    }

    @Test
    public final void BranchZbitSimpleTest() {
        m.getPCRegister().setValue((short) 0);

        m.getFlags().setN(false);
        m.getFlags().setZ(true);
        m.getFlags().setP(false);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(2, 10);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 10, pc.getValue());
    }

    @Test
    public final void BranchPbitSimpleTest() {
        m.getPCRegister().setValue((short) 0);

        m.getFlags().setN(false);
        m.getFlags().setZ(false);
        m.getFlags().setP(true);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(1, 10);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 10, pc.getValue());
    }

    // try to offset to last entry on page
    @Test
    public final void BranchPageLimitSimpleTest() {
        m.getPCRegister().setValue((short) 0);

        m.getFlags().setN(false);
        m.getFlags().setZ(true);
        m.getFlags().setP(false);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(2, 511);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 511, pc.getValue());
    }

    /**
     * Our implamentation allows for branching off the page
     */
    @Test
    public final void BranchPageLimitPlus1SimpleTest() {
        m.getPCRegister().setValue((short) 0);

        m.getFlags().setN(false);
        m.getFlags().setZ(true);
        m.getFlags().setP(false);

        final short start = m.getPCRegister().getValue();
        final BRx brx = new BRx(2, 512);
        brx.execute(m);

        final Register pc = m.getPCRegister();

        assertEquals("equal", start + 512, pc.getValue());
    }

    /**
     * DEBUG Have to look at console to verify
     * 
     * R0 0001 R1 0002 R2 0003 R3 0004 FLAGS FLAGS np R4 0005 R5 0006 R6 0007 R7 0008 PC
     * 03E7
     */
    @Test
    public final void dbugTest() {
        m.getFlags().setN(true);
        m.getFlags().setZ(false); // default
        m.getFlags().setP(false);
        m.getPCRegister().setValue((short) 999); // 999=x3E7
        m.getMemory((short) 1);
        m.setMemory((short) 1, (short) 2);
        m.getRegister(r0).setValue((short) 1);
        m.getRegister(r1).setValue((short) 2);
        m.getRegister(r2).setValue((short) 3);
        m.getRegister(r3).setValue((short) 4);
        m.getRegister(r4).setValue((short) 5);
        m.getRegister(r5).setValue((short) 6);
        m.getRegister(r6).setValue((short) 7);
        m.getRegister(r7).setValue((short) 8);

        final DBUG dbug = new DBUG();
        dbug.execute(m);

    }
}
