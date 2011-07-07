package edu.osu.cse.mmxi.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import edu.osu.cse.mmxi.loader.parser.Error;
import edu.osu.cse.mmxi.loader.parser.ErrorCodes;
import edu.osu.cse.mmxi.loader.parser.Exec;
import edu.osu.cse.mmxi.loader.parser.Header;
import edu.osu.cse.mmxi.loader.parser.ObjectFileParser;
import edu.osu.cse.mmxi.loader.parser.Text;
import edu.osu.cse.mmxi.machine.Machine;

public class SimpleLoader {
    public static List<Error> load(final String path, final Machine machine)
        throws IOException {

        final File file = new File(".", path);

        if (!file.isFile())
            throw new IOException(ErrorCodes.IO_BAD_PATH.toString() + " : " + path);

        if (!file.canRead())
            throw new IOException(ErrorCodes.IO_BAD_READ.toString() + " : " + path);

        if (file.length() <= 0)
            throw new IOException(ErrorCodes.IO_BAD_FILE.toString() + " : " + path);

        final BufferedReader fileReader = new BufferedReader(new InputStreamReader(
            new FileInputStream(file)));

        final ObjectFileParser parser = new ObjectFileParser(fileReader);

        final List<Error> errors = parser.parse();
        final Header header = parser.getParsedHeader();
        final Exec exec = parser.getParsedExec();
        final List<Text> text = parser.getParsedTexts();

        if (errors.size() == 0) {

            for (final Text t : text)
                if (!header.isWithinBounds(t.getAddress()))
                    errors.add(new Error(t.getLine(), ErrorCodes.ADDR_OUT_BOUNDS));
                else
                    machine.setMemory(t.getAddress(), t.getValue());

            if (!header.isWithinBounds(exec.getAddress()))
                errors.add(new Error(exec.getLine(), ErrorCodes.ADDR_EXEC_OUT_BOUNDS));
            else
                machine.getPCRegister().setValue(exec.getAddress());

        }

        fileReader.close();
        return errors;
    }
}
