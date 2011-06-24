package edu.osu.cse.mmxi;

public class Machine {
	public Registers r = new Registers();
	public Memory mem = new Memory();
	public Interpreter ip;
	public UI sys = new UI();
	public Machine() {
		ip = new Interpreter(this);
	}
}
