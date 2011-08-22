package edu.osu.cse.mmxi.sim.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
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

/**
 * This is used to Link and Load
 */
public class LinkingLoader {
    final Machine                           m;
    private final List<ObjectFile>          files;
    private final Map<String, FileLocation> defined;
    private final Set<String>               undefined;
    private final String                    main;
    private short                           ipla;

    /**
     * 
     * Constructor for the LinkingLoader
     * 
     * @param path
     *            name of the path
     * @param machine
     *            the machine that is being used
     */
    public LinkingLoader(final String path, final Machine machine,
        final List<Error> errors) {
        m = machine;
        ipla = 0;
        defined = new TreeMap<String, FileLocation>();
        undefined = new TreeSet<String>();
        files = new LinkedList<ObjectFile>();
        addFile(path, errors);
        main = files.size() == 0 ? null : files.get(0).getSegName();
    }

    /**
     * Essentially adds every file that the user inputs and converts to object file
     * 
     * @param path
     *            The name of the path
     * @param errors
     *            The list of errors that will be used
     */
    public void addFile(final String path, final List<Error> errors) {
        List<Error> myerrors = new ArrayList<Error>();

        final File file = new File(path);

        if (!file.isFile())
            myerrors.add(new Error(path, SimCodes.IO_BAD_PATH));
        else if (!file.canRead())
            myerrors.add(new Error(path, SimCodes.IO_BAD_READ));
        else if (file.length() <= 0)
            myerrors.add(new Error(path, SimCodes.IO_BAD_FILE));

        BufferedReader fileReader = null;
        do {
            if (myerrors.size() != 0)
                break;
            try {
                fileReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file)));
            } catch (final FileNotFoundException e) {
                myerrors.add(new Error("file not found: " + path, SimCodes.IO_BAD_PATH));
            }

            if (myerrors.size() != 0)
                break;
            final String fName = file.getName();
            final ObjectFile ofile = new ObjectFile(path, fName, fileReader);

            for (final ObjectFile f : files)
                if (ofile.getFileName().equals(f.getFileName()))
                    myerrors.add(new Error(path, SimCodes.UI_DUP_FILE));

            myerrors = ofile.parse();

            if (myerrors.size() != 0)
                break;

            files.add(ofile);
            final Set<String> newExt = new HashSet<String>(ofile.getParsedExternals());
            newExt.removeAll(defined.keySet());
            undefined.addAll(newExt);
            undefined.removeAll(ofile.getParsedSymbols().keySet());
            for (final Entry<String, Location> e : ofile.getParsedSymbols().entrySet())
                defined.put(e.getKey(),
                    FileLocation.make(ofile.getFileName(), e.getValue()));
        } while (false);

        try {
            if (fileReader != null)
                fileReader.close();
        } catch (final IOException e) {
        }

        errors.addAll(myerrors);
    }

    /**
     * Sets the ipla
     * 
     * @param ipla
     *            The ipla that will be set
     */
    public void setIPLA(final short ipla) {
        this.ipla = ipla;
    }

    /**
     * Links all the segment
     * 
     * @param errors
     *            The lists of errors that will be added
     * @param symbols
     *            Map of all symbols that are being referenced
     */
    public void link(final List<Error> errors, final Map<String, Short> symbols) {
        assert undefined.size() == 0;
        symbols.clear();
        final Map<String, Short> plaMap = new TreeMap<String, Short>();
        short pla = ipla;
        for (final ObjectFile ofile : files) {
            plaMap.put(ofile.getFileName(), pla);
            pla += ofile.getSize();
        }
        for (final Entry<String, FileLocation> e : defined.entrySet()) {
            final FileLocation fl = e.getValue();
            symbols.put(e.getKey(),
                (short) ((fl.file == null ? 0 : plaMap.get(fl.file)) + fl.address));
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
                final short newAddr = (short) (pla + t.getAddress());
                final short newVal = (short) ((t.getExternal() == null ? pla : symbols
                    .get(t.getExternal())) + (t.getValue() & mask));
                if (mask > 0 && (newVal & ~mask) != (newAddr + 1 & ~mask))
                    errors.add(new Error(t.getLine(), "try IPLA page offset < "
                        + (ipla - newVal + 0x200), SimCodes.LINK_IPLA_OFF_PAGE));
                m.setMemory(newAddr, (short) (t.getValue() & ~mask | newVal & mask));
            }
        }
        final FileLocation fl = defined.get(main);
        m.getPCRegister().setValue(
            (short) (fl.file == null ? 0 : plaMap.get(fl.file) + fl.address));
    }

    public Map<String, FileLocation> getSymbols() {
        return defined;
    }

    public Set<String> getMissingSymbols() {
        return undefined;
    }

    public List<ObjectFile> getOFiles() {
        return files;
    }

    /**
     * Class that deliver informations on location of the file
     */
    public static class FileLocation extends Location {
        public String file;

        public FileLocation(final String file, final int addr) {
            super(file != null, addr);
            this.file = file;
        }

        public static FileLocation make(final String file, final Location loc) {
            return new FileLocation(loc.isRelative ? file : null, loc.address);
        }

        @Override
        public String toString() {
            return (file == null ? "x" : file + "+x") + address;
        }
    }
}
