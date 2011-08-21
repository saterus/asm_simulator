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
import java.util.Map.Entry;

import edu.osu.cse.mmxi.common.Location;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.sim.error.SimCodes;
import edu.osu.cse.mmxi.sim.loader.parser.ObjectFile;
import edu.osu.cse.mmxi.sim.loader.parser.Text;
import edu.osu.cse.mmxi.sim.machine.Machine;

public class SimpleLoader {
    public static List<Error> load(final String path, final Machine machine,
        final Map<Short, SourceLine> lines, final Map<String, Short> symbols) {
        List<Error> errors = new ArrayList<Error>();

        final File file = new File(path);

        if (!file.isFile())
            errors.add(new Error(path, SimCodes.IO_BAD_PATH));

        if (!file.canRead())
            errors.add(new Error(path, SimCodes.IO_BAD_READ));

        if (file.length() <= 0)
            errors.add(new Error(path, SimCodes.IO_BAD_FILE));

        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                file)));
        } catch (final FileNotFoundException e) {
            errors.add(new Error("file not found: " + path, SimCodes.IO_BAD_PATH));
        }

        if (errors.size() == 0) {
            final ObjectFile ofile = new ObjectFile(path, file.getName(), fileReader);

            errors = ofile.parse();
            final List<Text> text = ofile.getParsedTexts();
            for (final Entry<String, Location> e : ofile.getParsedGlobals().entrySet()) {
                assert !e.getValue().isRelative;
                symbols.put(e.getKey(), (short) e.getValue().address);
            }
            assert ofile.getParsedExternals().size() == 0;

            if (errors.size() == 0) {
                for (final Text t : text) {
                    machine.setMemory(t.getAddress(), t.getValue());
                    if (lines != null)
                        lines.put(t.getAddress(), new SourceLine(file, t.getLine()));
                }
                machine.getPCRegister().setValue(symbols.get(ofile.getSegName()));

            }
        }

        try {
            if (fileReader != null)
                fileReader.close();
        } catch (final IOException e) {
        }
        return errors;
    }

    public static class SourceLine {
        public File file;
        public int  line;

        public SourceLine(final File file, final int line) {
            this.file = file;
            this.line = line;
        }
    }
}
