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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.osu.cse.mmxi.common.Location;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.sim.error.SimCodes;
import edu.osu.cse.mmxi.sim.loader.parser.ObjectFile;
import edu.osu.cse.mmxi.sim.loader.parser.Text;
import edu.osu.cse.mmxi.sim.machine.Machine;

public class LinkingLoader {
    final Machine                           m;
    private List<ObjectFile>                files;
    private final Map<String, FileLocation> defined;
    private final Set<String>               undefined;
    private final String                    main;

    public LinkingLoader(final String path, final Machine machine,
        final List<Error> errors) {
        m = machine;
        defined = new TreeMap<String, FileLocation>();
        undefined = new TreeSet<String>();
        addFile(path, errors);
        main = files.get(0).getSegName();
    }

    public void addFile(final String path, final List<Error> errors) {
        List<Error> myerrors = new ArrayList<Error>();

        final File file = new File(path);

        if (!file.isFile())
            myerrors.add(new Error(path, SimCodes.IO_BAD_PATH));

        if (!file.canRead())
            myerrors.add(new Error(path, SimCodes.IO_BAD_READ));

        if (file.length() <= 0)
            myerrors.add(new Error(path, SimCodes.IO_BAD_FILE));

        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                file)));
        } catch (final FileNotFoundException e) {
            myerrors.add(new Error("file not found: " + path, SimCodes.IO_BAD_PATH));
        }

        do {
            if (myerrors.size() != 0)
                break;
            final String fName = file.getName();
            final ObjectFile ofile = new ObjectFile(fName, fileReader);

            myerrors = ofile.parse();

            if (myerrors.size() != 0)
                break;

            files.add(ofile);

            undefined.addAll(ofile.getParsedExternals());
            undefined.removeAll(ofile.getParsedGlobals().keySet());
            for (final Entry<String, Location> e : ofile.getParsedGlobals().entrySet())
                defined.put(e.getKey(), FileLocation.make(fName, e.getValue()));
        } while (false);

        try {
            if (fileReader != null)
                fileReader.close();
        } catch (final IOException e) {
        }

        errors.addAll(myerrors);
    }

    public void link(final short ipla, final List<Error> errors) {
        assert undefined.size() == 0;
        final Map<String, Short> plaMap = new TreeMap<String, Short>();
        short pla = ipla;
        for (final ObjectFile ofile : files) {
            plaMap.put(ofile.getFileName(), pla);
            pla += ofile.getSize();
        }
        if ((pla & 0xFE00) != (ipla & 0xFE00)) {
            if ((pla - ipla & 0xFFFF) > 0x1FF)
                errors.add(new Error("program length: "
                    + Utilities.sShortToHex((short) (pla - ipla)),
                    SimCodes.LINK_PROG_TOO_LONG));
            else
                errors.add(new Error("maximum IPLA page offset: "
                    + Utilities.sShortToHex((short) (0x1FF + ipla - pla)),
                    SimCodes.LINK_IPLA_OFF_PAGE));
            return;
        }
        for (final ObjectFile ofile : files) {
            pla = plaMap.get(ofile.getFileName());
            for (final Text t : ofile.getParsedTexts()) {
                final short mask = t.getMask();
                short sum = pla;
                if (t.getExternal() != null) {
                    final FileLocation fl = defined.get(t.getExternal());
                    sum = (short) (fl.file == null ? 0 : fl.address);
                }
                sum += t.getValue() & mask;
                if (mask > 0 && (sum & ~mask) != 0)
                    errors.add(new Error(t.getLine(), "try IPLA page offset < "
                        + (ipla - sum + 0x200), SimCodes.LINK_IPLA_OFF_PAGE));

                m.setMemory((short) (pla + t.getAddress()),
                    (short) (t.getValue() & ~mask | sum & mask));
            }
        }
        final FileLocation fl = defined.get(main);
        m.getPCRegister().setValue((short) (fl.file == null ? 0 : fl.address));
    }

    public Map<String, FileLocation> getSymbols() {
        return defined;
    }

    public Set<String> getMissingSymbols() {
        return undefined;
    }

    public static class FileLocation extends Location {
        public String file;

        public FileLocation(final String file, final int addr) {
            super(file != null, addr);
            this.file = file;
        }

        public static FileLocation make(final String file, final Location loc) {
            return new FileLocation(loc.isRelative ? file : null, loc.address);
        }
    }
}
