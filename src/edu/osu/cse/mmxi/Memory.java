package edu.osu.cse.mmxi;

public class Memory {
	public short[][] memory = new short[0x80][];
	public boolean n = false, z = true, p = false;
	public short[] r = new short[8];
	public short pc = 0;
	public short getMem(short index) {
		return getMem((byte)(index >> 9 & 0x7f), (short)(index & 0x1ff));
	}
	public short getMem(byte page, short off) {
		return getPage(page)[off];
	}
	public void setMem(short index, short val) {
		setMem((byte)(index >> 9 & 0x7f), (short)(index & 0x1ff), val);
	}
	public void setMem(byte page, short off, short val) {
		getPage(page)[off] = val;
	}
	public short[] getPage(byte page) {
		if (memory[page] == null) {
			memory[page] = new short[0x200];
			long rand = 0;
			for (int i=0;i<0x200;i++) {
				if (rand==0)
					rand=Double.doubleToRawLongBits(1+Math.random()) & 0xffffffffffffL;
				memory[page][i]=(short)(rand&0xffffL);
				rand >>= 32;
			}
		}
		return memory[page];
	}
}
