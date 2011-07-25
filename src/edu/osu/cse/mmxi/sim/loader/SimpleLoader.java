package edu.osu.cse.mmxi.sim.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.osu.cse.mmxi.sim.error.Error;
import edu.osu.cse.mmxi.sim.error.ErrorCodes;
import edu.osu.cse.mmxi.sim.loader.parser.Exec;
import edu.osu.cse.mmxi.sim.loader.parser.Header;
import edu.osu.cse.mmxi.sim.loader.parser.ObjectFileParser;
import edu.osu.cse.mmxi.sim.loader.parser.Text;
import edu.osu.cse.mmxi.sim.machine.Machine;

public class SimpleLoader {
    public static List<Error> load(final String path, final Machine machine,
        final Map<Short, Integer> lines, final Map<String, Short> symbols) {
        List<Error> errors = new ArrayList<Error>();

        final File file = new File(".", path);

        if (!file.isFile())
            errors.add(new Error(path, ErrorCodes.IO_BAD_PATH));

        if (!file.canRead())
            errors.add(new Error(path, ErrorCodes.IO_BAD_READ));

        if (file.length() <= 0)
            errors.add(new Error(path, ErrorCodes.IO_BAD_FILE));

        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                file)));
        } catch (final FileNotFoundException e) {
            errors.add(new Error("file not found: " + path, ErrorCodes.IO_BAD_PATH));
        }

        if (errors.size() == 0) {
            final ObjectFileParser parser = new ObjectFileParser(fileReader);

            errors = parser.parse();
            final Header header = parser.getParsedHeader();
            final Exec exec = parser.getParsedExec();
            final List<Text> text = parser.getParsedTexts();
            symbols.putAll(parser.getParsedSymbols());

            if (errors.size() == 0) {

                for (final Text t : text)
                    if (!header.isWithinBounds(t.getAddress()))
                        errors.add(new Error(t.getLine(), ErrorCodes.ADDR_OUT_BOUNDS));
                    else {
                        machine.setMemory(t.getAddress(), t.getValue());
                        if (lines != null)
                            lines.put(t.getAddress(), t.getLine());
                    }

                if (!header.isWithinBounds(exec.getAddress()))
                    errors
                        .add(new Error(exec.getLine(), ErrorCodes.ADDR_EXEC_OUT_BOUNDS));
                else
                    machine.getPCRegister().setValue(exec.getAddress());

            }
        }

        try {
            if (fileReader != null)
                fileReader.close();
        } catch (final IOException e) {
        }
        return errors;
    }
}
