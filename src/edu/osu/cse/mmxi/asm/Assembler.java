package edu.osu.cse.mmxi.asm;

import java.io.FileNotFoundException;
import java.io.IOException;

import edu.osu.cse.mmxi.asm.io.InputOutput;

public class Assembler {
    public InputOutput io;
    public String      segName = null;

    public Assembler(final String in, final String out, final String intermediate)
        throws IOException {
        io = new InputOutput();
        try {
            io.openReader(in);
        } catch (final FileNotFoundException e) {
            System.err.println("input file not found");
        }
        Pass1Parser.parse(this);
        if (out != null) {
            io.resetReader();
            try {
                io.openWriter(out);
            } catch (final FileNotFoundException e) {
                System.err.println("output file could not be opened");
            }
            Pass2Parser.parse(this);
            io.closeWriter();
        }
        io.closeReader();
    }

    public static void main(final String[] args) throws IOException {
        new Assembler(args[0], null, null);
    }
}
