package edu.osu.cse.mmxi.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.osu.cse.mmxi.loader.parser.Error;
import edu.osu.cse.mmxi.loader.parser.Exec;
import edu.osu.cse.mmxi.loader.parser.Header;
import edu.osu.cse.mmxi.loader.parser.ObjectFileParser;
import edu.osu.cse.mmxi.loader.parser.ParseException;
import edu.osu.cse.mmxi.loader.parser.Text;
import edu.osu.cse.mmxi.machine.Machine;

public class SimpleLoader {
    public static boolean checkBounds = false;

    public static void load(final String path, final Machine machine)
            throws ParseException, IOException {

        final File file = new File(".", path);

        if (!file.isFile())
            throw new IOException("Pathname does not refer to a file: " + path);

        if (!file.canRead())
            throw new IOException("File " + path + " is not readable.");

        if (file.length() <= 0)
            throw new IOException("File " + path + " is empty.");

        final BufferedReader fileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));

        final ObjectFileParser parser = new ObjectFileParser(fileReader);

        final List<Error> errors = parser.parse();
        final Header header = parser.getParsedHeader();
        final Exec exec = parser.getParsedExec();
        final List<Text> text = parser.getParsedTexts();

        System.out.println(header.toString());
        for (final Text t : text)
            System.out.println(t.toString());
        System.out.println(exec.toString());

        if (errors.size() > 0)
            for (final Error e : errors)
                // TODO: Handle in UI or something
                System.out.println(e);
        else {
            // we currently do absolutely nothing with header information.

            for (final Text t : text)
                if (checkBounds && !header.isWithinBounds(t.getAddress()))
                    errors.add(new Error(t.getLine(), "Text address out of bounds"));
                else
                    machine.setMemory(t.getAddress(), t.getValue());

            if (checkBounds && !header.isWithinBounds(exec.getAddress()))
                errors.add(new Error(exec.getLine(), "Execution address out of bounds"));
            else
                machine.getPCRegister().setValue(exec.getAddress());

            if (errors.size() > 0)
                for (final Error e : errors)
                    // TODO: Handle in UI or something
                    System.out.println(e);
        }

        fileReader.close();

    }
}
