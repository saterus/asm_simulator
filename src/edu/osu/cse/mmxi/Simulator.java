package edu.osu.cse.mmxi;

import java.io.IOException;

public class Simulator {
	public Machine m;
	public Simulator(Machine _m) {
		m = _m;
	}
	public void execute(Program p) throws MemoryException {
		for (int i=0; i<10000; i++) {
			short inst = p.getMem(m.r.pc++);
			m.ip.read(p,inst);
			if (m.r.halt) {
				System.out.println("Halted after "+i+" steps. Final system state:");
				m.ip.debug(p);
				break;
			}
			// m.ip.debug();
		}
	}
	public static void main(String[] args) {
		Machine m = new Machine();
		Simulator sim = new Simulator(m);
		if (args.length != 1)
			m.sys.error("program requires exactly one argument: the file to be processed");
		Program p = null;
		try { p = Loader.load(args[0], m); }
		catch (MemoryException e) {
			m.sys.error("error: attempted to write out of allowed memory");
		} catch (ParseException e) {
			m.sys.error(e.getMessage());
		} catch (IOException e) {
			m.sys.error("I/O Error: "+e.getMessage());
		}
		try { sim.execute(p); }
		catch (MemoryException e) {
			m.sys.error("FATAL ERROR: execution has left allocated space");
		}
	}
}
