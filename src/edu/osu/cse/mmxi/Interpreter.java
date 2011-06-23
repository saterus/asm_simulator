package edu.osu.cse.mmxi;

public class Interpreter {
	public Memory m;
	public Interpreter() { this(new Memory()); }
	public Interpreter(Memory _m) { m = _m; }
	public void read(short inst) {
		switch (inst >> 12 & 0xf) {
			case 0: branch(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 1:
				if ((inst & 0x20) == 0)
					add(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
				else
					addImm(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
				break;
			case 2: load(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 3: store(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 4: jumpsub((inst & 0x400) != 0, inst & 0x1ff); break;
			case 5:
				if ((inst & 0x20) == 0)
					and(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
				else
					andImm(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
				break;
			case 6: loadR(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f); break;
			case 7: storeR(inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f); break;
			case 8: debug(); break;
			case 9: not(inst >> 9 & 0x7, inst >> 6 & 0x7); break;
			case 10: loadI(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 11: storeI(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 12: jumpsubR((inst & 0x400) != 0, inst >> 6 & 0x7, inst & 0x3f); break;
			case 13: ret(); break;
			case 14: loadEA(inst >> 9 & 0x7, inst & 0x1ff); break;
			case 15: trap(inst & 0xff); break;
		}
	}
	public void add(int dr, int sr1, int sr2) {
		m.r[dr] = (short)(m.r[sr1] + m.r[sr2]);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void addImm(int dr, int sr, int imm) {
		imm = imm << 27 >> 27; // sign extend imm
		m.r[dr] = (short)(m.r[sr] + imm);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void and(int dr, int sr1, int sr2) {
		m.r[dr] = (short)(m.r[sr1] & m.r[sr2]);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void andImm(int dr, int sr, int imm) {
		imm = imm << 29 >> 29; // sign extend imm
		m.r[dr] = (short)(m.r[sr] & imm);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void branch(int nzp, int pgoff) {
		if ((nzp & 4) != 0 && m.n || (nzp & 2) != 0 && m.z || (nzp & 1) != 0 && m.p)
			m.pc = (short)((m.pc & 0xfe00) + pgoff);
	}
	public void debug() {
		System.out.println("PC "+Integer.toHexString(m.pc+0x10000).substring(1));
		for (int i=0;i<8;i++)
			System.out.println("R"+i+" "+Integer.toHexString(m.r[i]+0x10000).substring(1));
		if(m.n) System.out.print("n");
		if(m.z) System.out.print("z");
		if(m.p) System.out.print("p");
		System.out.println();
	}
	public void jumpsub(boolean l, int pgoff) {
		if (l) m.r[7] = m.pc;
		m.pc = (short)((m.pc & 0xfe00) + pgoff);
	}
	public void jumpsubR(boolean l, int br, int index) {
		if (l) m.r[7] = m.pc;
		m.pc = (short)(m.r[br] + index);
	}
	public void load(int dr, int pgoff) {
		m.r[dr] = m.getMem((short)((m.pc & 0xfe00) + pgoff));
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void loadI(int dr, int pgoff) {
		m.r[dr] = m.getMem(m.getMem((short)((m.pc & 0xfe00) + pgoff)));
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void loadR(int dr, int br, int index) {
		m.r[dr] = m.getMem((short)(m.r[br] + index));
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void loadEA(int dr, int pgoff) {
		m.r[dr] = (short)((m.pc & 0xfe00) + pgoff);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void not(int dr, int sr) {
		m.r[dr] = (short)(~m.r[sr]);
		m.n = m.r[dr] < 0;
		m.z = m.r[dr] == 0;
		m.p = m.r[dr] > 0;
	}
	public void ret() {
		m.pc = m.r[7];
	}
	public void store(int sr, int pgoff) {
		m.setMem((short)((m.pc & 0xfe00) + pgoff), m.r[sr]);
	}
	public void storeI(int sr, int pgoff) {
		m.setMem(m.getMem((short)((m.pc & 0xfe00) + pgoff)), m.r[sr]);
	}
	public void storeR(int sr, int br, int index) {
		m.setMem((short)(m.r[br] + index), m.r[sr]);
	}
	public void trap(int vector) {
		switch (vector) {
			case 0x21: //write the char in R0 to the console
				if ((m.r[0] & 0xff80) != 0)
					Loader.error("Warning: R0 does not contain a character");
				System.out.print((char)(m.r[0] & 0x7f));
				break;
			case 0x22: // OUT
				break; // write the null-terminated string pointed to by R0 to the console
			case 0x23: // PUTS
				break; // print a prompt on screen and read a single character from the prompt
			case 0x25: // HALT: halt execution
				m.halt = true;
				break;
			case 0x31: // OUTN
				System.out.println(m.r[0]);
				break; // write the value of R0 to the console as a decimal integer
			case 0x33: // INN
				break; // print a prompt on screen and read a decimal from the prompt
			case 0x43: // RND: store a random number in R0
				m.r[0]=(short)(Double.doubleToRawLongBits(1+Math.random()) & 0xffffL);
				break;
			default:
				Loader.error("FATAL ERROR: unknown trap vector 0x"+Integer.toHexString(vector));
		}
	}
}
