package edu.osu.cse.mmxi.asm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.asm.io.IO;
import edu.osu.cse.mmxi.common.UI;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * This is the file contains the executing main. In addition the user interface,
 * input/output, and errors are instantiated here. This file runs the both passes and
 * prints console error ouputs and warnings.
 * 
 */
public class Assembler {
    public IO         io;
    public String     segName = null;
    final List<Error> errors  = new ArrayList<Error>();
    final UI          ui;

    /**
     * Constructor used for creating opening reading/writing IO and handling errors for
     * Pass 1 and Pass parsing.
     * 
     * @param ui
     *            The User Interface object used for printing errors to screen and
     *            prompting for user input on warning level errors.
     * @param in
     *            The in filename.
     * @param out
     *            The out filename.
     * @param intermediate
     *            The intermediate filename.
     * @throws IOException
     *             Catches file writing/reading errors.
     */
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

    /**
     * Main executing static method for the Assembler. Handles the user input args from
     * the command line.
     * 
     * @param args
     *            The user command line arguments for assebler. args[0] The assembly file
     *            to parse. args[1] (optional) The -i flag for wring an intermediate file.
     *            The filename will be parsed for trailing extension name and will replace
     *            with ".o" for the machine code object file and ".i" for the intermediate
     *            file name.
     * @throws IOException
     */
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
