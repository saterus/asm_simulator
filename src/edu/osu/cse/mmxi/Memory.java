package edu.osu.cse.mmxi;

public class Memory {
	public short[][] memory = new short[0x80][];
	public boolean n = false, z = true, p = false, halt = false;
	public short[] r = new short[8];
	public short pc = 0;
	public long seed = System.nanoTime();
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
				if (rand==0) {
					seed ^= (seed << 21);
					seed ^= (seed >>> 35);
					seed ^= (seed << 4);
					rand = seed;
				}
				memory[page][i]=(short)(rand&0xffffL);
				rand >>= 32;
			}
		}
		return memory[page];
	}
}
