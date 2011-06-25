package edu.osu.cse.mmxi.ui;

public class UI {
	public void error(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	public void warn(String msg) {
		System.err.println(msg);
	}
	public void print(String msg) {
		System.out.print(msg);
	}
}
