package edu.osu.cse.mmxi;

public class Simulator {
	public Interpreter interp;
	public Memory m;
	public int begin = 0, len = 0, exec = 0;
	public String name = null;
	public Simulator() {
		interp = new Interpreter();
		m = interp.m;
	}
	public void execute() {
		m.pc = (short)exec;
		for (int i=0; i<10000; i++) {
			if ((m.pc & 0xffff) < begin || (m.pc & 0xffff) > begin+len)
				Loader.error("FATAL ERROR: execution has left allocated space");
			short inst = m.getMem(m.pc++);
			interp.read(inst);
			if (m.halt) {
				System.out.println("Halted after "+i+" steps. Final system state:");
				interp.debug();
				break;
			}
			// interp.debug();
		}
	}
}
