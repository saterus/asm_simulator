package edu.osu.cse.mmxi;

public class Program {
	private static boolean throwMemErr = true;
	public String name = null;
	private int begin = 0, len = 0;
	private Machine m;
	public Program(String n, int b, int l, Machine _m) {
		name = n;
		begin = b;
		len = l;
		m = _m;
	}
	public boolean checkBounds(short mem) {
		return !throwMemErr || (mem - begin & 0xffff) < len;
	}
	public short getMem(short index) throws MemoryException {
		if (!checkBounds(index))
			throw new MemoryException("attempting to read memory out of bounds");
		return m.mem.getMem(index);
	}
	public void setMem(short index, short val) throws MemoryException {
		if (!checkBounds(index))
			throw new MemoryException("attempting to write memory out of bounds");
		m.mem.setMem(index, val);
	}
}
