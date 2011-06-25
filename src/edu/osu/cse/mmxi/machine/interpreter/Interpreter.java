package edu.osu.cse.mmxi.machine.interpreter;

import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.memory.MemoryException;

public class Interpreter implements ALU {
    public Machine m;

    public Interpreter(final Machine _m) {
        this.m = _m;
    }

    @Override
    public String executeNextInstruction(final short s) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
    public void read(final Program p, final short inst) throws MemoryException {
        switch (inst >> 12 & 0xf) {
        case 0:
            this.branch(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 1:
            if ((inst & 0x20) == 0) {
                this.add(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
            } else {
                this.addImm(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
            }
            break;
        case 2:
            this.load(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 3:
            this.store(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 4:
            this.jumpsub(p, (inst & 0x400) != 0, inst & 0x1ff);
            break;
        case 5:
            if ((inst & 0x20) == 0) {
                this.and(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
            } else {
                this.andImm(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
            }
            break;
        case 6:
            this.loadR(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f);
            break;
        case 7:
            this.storeR(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f);
            break;
        case 8:
            this.debug(p);
            break;
        case 9:
            this.not(p, inst >> 9 & 0x7, inst >> 6 & 0x7);
            break;
        case 10:
            this.loadI(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 11:
            this.storeI(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 12:
            this.jumpsubR(p, (inst & 0x400) != 0, inst >> 6 & 0x7, inst & 0x3f);
            break;
        case 13:
            this.ret(p);
            break;
        case 14:
            this.loadEA(p, inst >> 9 & 0x7, inst & 0x1ff);
            break;
        case 15:
            this.trap(p, inst & 0xff);
            break;
        }
    }

    public void add(final Program p, final int dr, final int sr1, final int sr2) {
        this.m.r.r[dr] = (short) (this.m.r.r[sr1] + this.m.r.r[sr2]);
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void addImm(final Program p, final int dr, final int sr, int imm) {
        imm = imm << 27 >> 27; // sign extend imm
        this.m.r.r[dr] = (short) (this.m.r.r[sr] + imm);
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void and(final Program p, final int dr, final int sr1, final int sr2) {
        this.m.r.r[dr] = (short) (this.m.r.r[sr1] & this.m.r.r[sr2]);
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void andImm(final Program p, final int dr, final int sr, int imm) {
        imm = imm << 29 >> 29; // sign extend imm
        this.m.r.r[dr] = (short) (this.m.r.r[sr] & imm);
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void branch(final Program p, final int nzp, final int pgoff) {
        if ((nzp & 4) != 0 && this.m.r.n || (nzp & 2) != 0 && this.m.r.z
                || (nzp & 1) != 0 && this.m.r.p) {
            this.m.r.pc = (short) ((this.m.r.pc & 0xfe00) + pgoff);
        }
    }

    public void debug(final Program p) {
        this.m.sys.print("PC " + Integer.toHexString(this.m.r.pc + 0x10000).substring(1)
                + "\n");
        for (int i = 0; i < 8; i++) {
            this.m.sys.print("R" + i + " "
                    + Integer.toHexString(this.m.r.r[i] + 0x10000).substring(1) + "\n");
        }
        if (this.m.r.n) {
            this.m.sys.print("n");
        }
        if (this.m.r.z) {
            this.m.sys.print("z");
        }
        if (this.m.r.p) {
            this.m.sys.print("p");
        }
        this.m.sys.print("\n");
    }

    public void jumpsub(final Program p, final boolean l, final int pgoff) {
        if (l) {
            this.m.r.r[7] = this.m.r.pc;
        }
        this.m.r.pc = (short) ((this.m.r.pc & 0xfe00) + pgoff);
    }

    public void jumpsubR(final Program p, final boolean l, final int br, final int index) {
        if (l) {
            this.m.r.r[7] = this.m.r.pc;
        }
        this.m.r.pc = (short) (this.m.r.r[br] + index);
    }

    public void load(final Program p, final int dr, final int pgoff)
            throws MemoryException {
        this.m.r.r[dr] = p.getMemory((short) ((this.m.r.pc & 0xfe00) + pgoff));
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void loadI(final Program p, final int dr, final int pgoff)
            throws MemoryException {
        this.m.r.r[dr] = p.getMemory(p
                .getMemory((short) ((this.m.r.pc & 0xfe00) + pgoff)));
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void loadR(final Program p, final int dr, final int br, final int index)
            throws MemoryException {
        this.m.r.r[dr] = p.getMemory((short) (this.m.r.r[br] + index));
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void loadEA(final Program p, final int dr, final int pgoff) {
        this.m.r.r[dr] = (short) ((this.m.r.pc & 0xfe00) + pgoff);
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void not(final Program p, final int dr, final int sr) {
        this.m.r.r[dr] = (short) ~this.m.r.r[sr];
        this.m.r.n = this.m.r.r[dr] < 0;
        this.m.r.z = this.m.r.r[dr] == 0;
        this.m.r.p = this.m.r.r[dr] > 0;
    }

    public void ret(final Program p) {
        this.m.r.pc = this.m.r.r[7];
    }

    public void store(final Program p, final int sr, final int pgoff)
            throws MemoryException {
        p.setMemory((short) ((this.m.r.pc & 0xfe00) + pgoff), this.m.r.r[sr]);
    }

    public void storeI(final Program p, final int sr, final int pgoff)
            throws MemoryException {
        p.setMemory(p.getMemory((short) ((this.m.r.pc & 0xfe00) + pgoff)), this.m.r.r[sr]);
    }

    public void storeR(final Program p, final int sr, final int br, final int index)
            throws MemoryException {
        p.setMemory((short) (this.m.r.r[br] + index), this.m.r.r[sr]);
    }

    public void trap(final Program p, final int vector) {
        switch (vector) {
        case 0x21: // write the char in R0 to the console
            if ((this.m.r.r[0] & 0xff80) != 0) {
                this.m.sys.error("Warning: R0 does not contain a character");
            }
            System.out.print((char) (this.m.r.r[0] & 0x7f));
            break;
        case 0x22: // OUT
            break; // write the null-terminated string pointed to by R0 to the
        // console
        case 0x23: // PUTS
            break; // print a prompt on screen and read a single character from
        // the prompt
        case 0x25: // HALT: halt execution
            this.m.r.halt = true;
            break;
        case 0x31: // OUTN
            this.m.sys.print(this.m.r.r[0] + "\n");
            break; // write the value of R0 to the console as a decimal integer
        case 0x33: // INN
            break; // print a prompt on screen and read a decimal from the
        // prompt
        case 0x43: // RND: store a random number in R0
            this.m.mem.seed ^= this.m.mem.seed << 21;
            this.m.mem.seed ^= this.m.mem.seed >>> 35;
            this.m.mem.seed ^= this.m.mem.seed << 4;
            this.m.r.r[0] = (short) (this.m.mem.seed & 0xffff);
            break;
        default:
            this.m.sys.error("Warning: unknown trap vector 0x"
                    + Integer.toHexString(vector));
        }
    }
    */

}
