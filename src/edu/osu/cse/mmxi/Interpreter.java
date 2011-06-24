package edu.osu.cse.mmxi;

public class Interpreter {
	public Machine m;
	public Interpreter(Machine _m) { m = _m; }
	public void read(Program p, short inst) throws MemoryException {
		switch (inst >> 12 & 0xf) {
			case 0: branch(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 1:
				if ((inst & 0x20) == 0)
					add(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
				else
					addImm(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
				break;
			case 2: load(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 3: store(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 4: jumpsub(p, (inst & 0x400) != 0, inst & 0x1ff); break;
			case 5:
				if ((inst & 0x20) == 0)
					and(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x7);
				else
					andImm(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x1f);
				break;
			case 6: loadR(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f); break;
			case 7: storeR(p, inst >> 9 & 0x7, inst >> 6 & 0x7, inst & 0x3f); break;
			case 8: debug(p); break;
			case 9: not(p, inst >> 9 & 0x7, inst >> 6 & 0x7); break;
			case 10: loadI(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 11: storeI(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 12: jumpsubR(p, (inst & 0x400) != 0, inst >> 6 & 0x7, inst & 0x3f); break;
			case 13: ret(p); break;
			case 14: loadEA(p, inst >> 9 & 0x7, inst & 0x1ff); break;
			case 15: trap(p, inst & 0xff); break;
		}
	}
	public void add(Program p, int dr, int sr1, int sr2) {
		m.r.r[dr] = (short)(m.r.r[sr1] + m.r.r[sr2]);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void addImm(Program p, int dr, int sr, int imm) {
		imm = imm << 27 >> 27; // sign extend imm
		m.r.r[dr] = (short)(m.r.r[sr] + imm);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void and(Program p, int dr, int sr1, int sr2) {
		m.r.r[dr] = (short)(m.r.r[sr1] & m.r.r[sr2]);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void andImm(Program p, int dr, int sr, int imm) {
		imm = imm << 29 >> 29; // sign extend imm
		m.r.r[dr] = (short)(m.r.r[sr] & imm);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void branch(Program p, int nzp, int pgoff) {
		if ((nzp & 4) != 0 && m.r.n || (nzp & 2) != 0 && m.r.z || (nzp & 1) != 0 && m.r.p)
			m.r.pc = (short)((m.r.pc & 0xfe00) + pgoff);
	}
	public void debug(Program p) {
		m.sys.print("PC "+Integer.toHexString(m.r.pc+0x10000).substring(1)+"\n");
		for (int i=0;i<8;i++)
			m.sys.print("R"+i+" "+Integer.toHexString(m.r.r[i]+0x10000).substring(1)+"\n");
		if(m.r.n) m.sys.print("n");
		if(m.r.z) m.sys.print("z");
		if(m.r.p) m.sys.print("p");
		m.sys.print("\n");
	}
	public void jumpsub(Program p, boolean l, int pgoff) {
		if (l) m.r.r[7] = m.r.pc;
		m.r.pc = (short)((m.r.pc & 0xfe00) + pgoff);
	}
	public void jumpsubR(Program p, boolean l, int br, int index) {
		if (l) m.r.r[7] = m.r.pc;
		m.r.pc = (short)(m.r.r[br] + index);
	}
	public void load(Program p, int dr, int pgoff) throws MemoryException {
		m.r.r[dr] = p.getMem((short)((m.r.pc & 0xfe00) + pgoff));
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void loadI(Program p, int dr, int pgoff) throws MemoryException {
		m.r.r[dr] = p.getMem(p.getMem((short)((m.r.pc & 0xfe00) + pgoff)));
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void loadR(Program p, int dr, int br, int index) throws MemoryException {
		m.r.r[dr] = p.getMem((short)(m.r.r[br] + index));
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void loadEA(Program p, int dr, int pgoff) {
		m.r.r[dr] = (short)((m.r.pc & 0xfe00) + pgoff);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void not(Program p, int dr, int sr) {
		m.r.r[dr] = (short)(~m.r.r[sr]);
		m.r.n = m.r.r[dr] < 0;
		m.r.z = m.r.r[dr] == 0;
		m.r.p = m.r.r[dr] > 0;
	}
	public void ret(Program p) {
		m.r.pc = m.r.r[7];
	}
	public void store(Program p, int sr, int pgoff) throws MemoryException {
		p.setMem((short)((m.r.pc & 0xfe00) + pgoff), m.r.r[sr]);
	}
	public void storeI(Program p, int sr, int pgoff) throws MemoryException {
		p.setMem(p.getMem((short)((m.r.pc & 0xfe00) + pgoff)), m.r.r[sr]);
	}
	public void storeR(Program p, int sr, int br, int index) throws MemoryException {
		p.setMem((short)(m.r.r[br] + index), m.r.r[sr]);
	}
	public void trap(Program p, int vector) {
		switch (vector) {
			case 0x21: //write the char in R0 to the console
				if ((m.r.r[0] & 0xff80) != 0)
					m.sys.error("Warning: R0 does not contain a character");
				System.out.print((char)(m.r.r[0] & 0x7f));
				break;
			case 0x22: // OUT
				break; // write the null-terminated string pointed to by R0 to the console
			case 0x23: // PUTS
				break; // print a prompt on screen and read a single character from the prompt
			case 0x25: // HALT: halt execution
				m.r.halt = true;
				break;
			case 0x31: // OUTN
				m.sys.print(m.r.r[0]+"\n");
				break; // write the value of R0 to the console as a decimal integer
			case 0x33: // INN
				break; // print a prompt on screen and read a decimal from the prompt
			case 0x43: // RND: store a random number in R0
				m.mem.seed ^= (m.mem.seed << 21);
				m.mem.seed ^= (m.mem.seed >>> 35);
				m.mem.seed ^= (m.mem.seed << 4);
				m.r.r[0]=(short)(m.mem.seed & 0xffff);
				break;
			default:
				m.sys.error("Warning: unknown trap vector 0x"+Integer.toHexString(vector));
		}
	}
}
