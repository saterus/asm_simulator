package edu.osu.cse.mmxi.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.osu.cse.mmxi.loader.parser.Error;
import edu.osu.cse.mmxi.loader.parser.Exec;
import edu.osu.cse.mmxi.loader.parser.Header;
import edu.osu.cse.mmxi.loader.parser.ObjectFileParser;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.loader.parser.Text;
import edu.osu.cse.mmxi.loader.parser.Token;
import edu.osu.cse.mmxi.machine.Machine;
import edu.osu.cse.mmxi.machine.Register.RegisterType;

public class SimpleLoader {

    public static void load(final String path, final Machine machine)
            throws ParseException, IOException {

        final File file = new File(".", path);

        if (!file.isFile()) {
            throw new IOException("Pathname does not refer to a file: " + path);
        }

        if (!file.canRead()) {
            throw new IOException("File " + path + " is not readable.");
        }

        if (file.length() <= 0) {
            throw new IOException("File " + path + " is empty.");
        }

        final BufferedReader fileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));

        final ObjectFileParser parser = new ObjectFileParser(fileReader);

        final List<Token> tokens = parser.parse();

        final List<Error> errors = new ArrayList<Error>();
        for (final Token t : tokens) {
            if (t instanceof Error) {
                errors.add((Error) t);
            }

            System.out.println("Token: " + t.toString());
        }

        if (errors.size() > 0) {
            for (final Error e : errors) {
                // TODO: Handle in UI or something
                System.out.println(e);
            }
        } else {

            for (final Token t : tokens) {

                if (t instanceof Text) {
                    machine.setMemory(t.getAddress(), t.getValue());

                } else if (t instanceof Header) {
                    // we currently do absolutely nothing with header information.
                    continue;

                } else if (t instanceof Exec) {
                    machine.getRegister(RegisterType.PC).setRegisterValue(t.getAddress());

                } else {
                    throw new ParseException("Token of unexpected type: "
                            + t.getClass().toString());
                }
            }
        }

        // TODO: Close the File/Reader/whatever properly.

    }
}
