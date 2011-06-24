package edu.osu.cse.mmxi;

public class Registers {
	public boolean n = false, z = true, p = false, halt = false;
	public short[] r = new short[8];
	public short pc = 0;
}
