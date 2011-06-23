package edu.osu.cse.mmxi;

import java.io.*;

public class Loader {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1)
			error("program requires exactly one argument: the file to be processed");
		Simulator sim = new Simulator();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(".",args[0]))));
			String line = br.readLine();
			if (line != null && line.length()>0)
				line = line.split(" ")[0];
			boolean foundH = false, foundE = false;
			for (int i=1; line!=null; i++) {
				if (line.length() == 0) {
					line = br.readLine();
					if (line != null && line.length()>0)
						line = line.split(" ")[0];
					continue;
				}
				switch (line.charAt(0)) {
				case 'H': case 'h':
					if (line.length() != 15)
						error("On line "+i+": malformed header");
					sim.name = line.substring(1, 7);
					try {
						sim.begin = Integer.parseInt(line.substring(7, 11), 16);
						sim.len = Integer.parseInt(line.substring(11), 16);
					} catch (NumberFormatException e) {
						error("On line "+i+": malformed header");
					}
					foundH = true;
					break;
				case 'T': case 't':
					if (!foundH) error("parser error: header not first record");
					if (line.length() != 9)
						error("On line "+i+": malformed text record");
					int loc = 0, dat = 0;
					try {
						loc = Integer.parseInt(line.substring(1, 5), 16);
						dat = Integer.parseInt(line.substring(5), 16);
					} catch (NumberFormatException e) {
						error("On line "+i+": malformed text record");
					}
					if (loc < sim.begin || loc >= sim.begin + sim.len)
						error("On line "+i+": text record out of reserved space");
					else {
						sim.m.setMem((short)loc, (short)dat);
					}
					break;
				case 'E': case 'e':
					if (!foundH) error("parser error: header not first record");
					if (line.length() != 5)
						error("On line "+i+": malformed execution record");
					int ex = 0;
					try {
						ex = Integer.parseInt(line.substring(1), 16);
					} catch (NumberFormatException e) {
						error("On line "+i+": malformed execution record");
					}
					if (ex < sim.begin || ex >= sim.begin + sim.len)
						error("On line "+i+": executing out of reserved space");
					else
						sim.exec = ex;
					foundE = true;
					break;
				default:
					error("On line "+i+": Unknown record type");
				}
			}
			if (!foundE) error("parser error: no execution record found");
		} catch (IOException e) {
			error("I/O error while trying to load file or file not found");
		}
		sim.execute();
	}

	public static void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
}
