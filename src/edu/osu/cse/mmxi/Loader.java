package edu.osu.cse.mmxi;

import java.io.*;

public class Loader {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(".",args[1]))));
			String line = br.readLine();
			String name;
			int begin = 0, len = 0, exec = 0;
			boolean foundH = false, foundE = false;
			for (int i=1; line!=null; i++, line = br.readLine()) {
				if (line.length() == 0) continue;
				switch (line.charAt(0)) {
				case 'H': case 'h':
					if (line.length() != 15)
						error("On line "+i+": malformed header");
					name = line.substring(1, 7);
					try {
						begin = Integer.parseInt(line.substring(7, 11), 16);
						len = Integer.parseInt(line.substring(11), 16);
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
					if (loc < begin || loc >= begin + len)
						error("On line "+i+": text record out of reserved space");
					// load <loc, dat> into memory here
					break;
				case 'E': case 'e':
					if (!foundH) error("parser error: header not first record");
					if (line.length() != 5)
						error("On line "+i+": malformed execution record");
					try {
						exec = Integer.parseInt(line.substring(1), 16);
					} catch (NumberFormatException e) {
						error("On line "+i+": malformed execution record");
					}
					if (exec < begin || exec >= begin + len)
						error("On line "+i+": executing out of reserved space");
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
	}

	public static void error(String msg) {
		System.err.println(msg);
	}
}
