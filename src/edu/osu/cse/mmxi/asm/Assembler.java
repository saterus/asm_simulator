package edu.osu.cse.mmxi.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.io.IO;
import edu.osu.cse.mmxi.common.UI;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.common.error.ParseException;

public class Assembler {
    public IO         io;
    public String     segName = null;
    final List<Error> errors  = new ArrayList<Error>();
    final UI          ui;

    public Assembler(final UI ui, final String in, final String out,
        final String intermediate) throws IOException {
        this.ui = ui;
        io = new IO();
        try {
            io.openReader(in);
        } catch (final ParseException e) {
            ui.printErrors(e.getError());
        }

        String interDat = null;

        try {
            interDat = new Pass1Parser(this, errors).parse();
        } catch (final ParseException e) {
            errors.add(e.getError());
        } catch (final IOException e) {
            errors.add(new Error(e.getMessage(), AsmCodes.IO_BAD_READ));
        }
        ui.printErrors(errors);

        if (intermediate != null)
            IO.writeFile(intermediate, interDat);
        else
            ;// System.out.print(interDat);
        if (out != null) {
            try {
                io.resetReader();
                io.openWriters(out, null);
            } catch (final ParseException e) {
                errors.add(e.getError());
            }
            ui.printErrors(errors);
            new Pass2Parser(this, errors).parse();
            ui.printErrors(errors);
            io.closeWriters();
        }
        io.closeReader();
    }

    public static void main(final String[] args) throws IOException {
        final UI ui = new UI();
        String file = null;
        boolean intermediate = false;
        final List<Error> errors = new ArrayList<Error>();
        for (final String s : args)
            if (s.equals("-i"))
                intermediate = true;
            else if (file == null)
                file = s;
            else
                errors.add(new Error(s, AsmCodes.IO_MANY_INPUT));
        if (file == null)
            errors.add(new Error(AsmCodes.IO_NO_INPUT));
        ui.printErrors(errors);
        String stem = file;
        if (stem.indexOf('.') != 0)
            stem = stem.substring(0, stem.lastIndexOf('.'));
        new Assembler(ui, args[0], stem + ".o", intermediate ? stem + ".i" : null);
    }
}
