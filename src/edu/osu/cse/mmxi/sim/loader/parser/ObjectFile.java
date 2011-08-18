package edu.osu.cse.mmxi.sim.loader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.common.Location;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.common.error.Error;
import edu.osu.cse.mmxi.common.error.ParseException;
import edu.osu.cse.mmxi.sim.error.SimCodes;

/**
 * <p>
 * Parses and tokenizes a ObjectFile for consumption by the Loader.
 * </p>
 * 
 * <p>
 * The Parser fundamentally splits the ObjectFile into 3 parts: the HeaderRecord, the
 * TextRecords, and the ExecRecord. If any of these is missing, the parser fails. There
 * must be exactly one HeaderRecord, at least one TextRecord, and exactly one ExecRecord,
 * in that order exclusively.
 * </p>
 * 
 * <p>
 * HeaderRecords contain basic information about the program being loaded. 15 ASCII
 * characters wide, optionally followed by a space and code comments.
 * </p>
 * 
 * <pre>
 * Format: 0: 'H'
 *       1-6: [Hex] Name.
 *      7-10: [Hex] Beginning absolute memory address.
 *     11-14: [Hex] Length of the block of memory being initially allocated for the program.
 * </pre>
 * 
 * TextRecords contain the actual instructions and data being loaded for the program. 9
 * ASCII characters wide, optionally followed by a space and code comments.
 * 
 * <pre>
 * Format: 0: 'T'
 *       1-4: [Hex] Absolute memory address of the data.
 *       5-8: [Hex] Value of the address.
 * </pre>
 * 
 * ExecRecords contain the initial value for the PC Register, where to begin the program.
 * 5 ASCII characters wide, optionally followed by a space and code comments.
 * 
 * <pre>
 * Format: 0: 'E'
 *       1-4: [Hex] Absolute memory address of the beginning of the program.
 * </pre>
 * 
 * <p>
 * Also see MMXI Programmer's Guide for sample ObjectFiles.
 * </p>
 * 
 * <p>
 * In the event of most parse failures, the parser should continue on with the rest of the
 * file. The failures are recorded and get reported as a group of all the parse errors in
 * the file.
 * </p>
 */
public class ObjectFile {
    private static final Pattern        ppRegex      = Pattern.compile("#![LS][^!]*!");

    private final BufferedReader        reader;
    private final String                name;

    private int                         lineNumber   = 1;
    private final List<Error>           errors       = new ArrayList<Error>();

    /** [Bits in Hex Rep.] | 7: page | 9: address | */
    private final List<Text>            text         = new ArrayList<Text>();

    /** [Bits in Hex Rep.] | 4: initial exec address | */

    /** [Bits in Hex Rep.] | 6: name | 4: begin address | 4: segment length | */
    private String                      segName      = null;
    private Short                       beginAddress = null;
    private Short                       lengthOffset = null;

    private final Map<String, Location> symbols      = new TreeMap<String, Location>();

    private final Set<String>           externals    = new TreeSet<String>();

    private int                         sourceLine   = 0;

    /**
     * The ObjectFileParser is built around processing an InputStream
     * 
     * @param reader
     *            BufferedReader wrapper over an InputStream containing an ObjectFile.
     */
    public ObjectFile(final String name, final BufferedReader reader) {
        this.reader = reader;
        if (name.lastIndexOf('.') != -1)
            this.name = name;
        else
            this.name = name.substring(0, name.lastIndexOf('.'));
    }

    /**
     * Reads the stream line by line, parsing the line by its appropriate format, and
     * tokenizing the results. The tokenized data is stored in this class, and a list of
     * errors is returned.
     * 
     * @return list of Errors obtained from parsing the Stream.
     */
    public List<Error> parse() {

        String line = null;
        try {
            line = reader.readLine();
        } catch (final IOException e) {
            errors.add(new Error("IO error while reading first line: " + e.getMessage(),
                SimCodes.IO_BAD_READ));
        }

        while (line != null) {
            tokensizeLine(line);

            lineNumber++;
            try {
                line = reader.readLine();
            } catch (final IOException e) {
                errors.add(new Error("IO error while reading line " + lineNumber + ": "
                    + e.getMessage(), SimCodes.IO_BAD_READ));
            }
        }

        if (segName == null)
            errors.add(new Error(SimCodes.PARSE_NO_HEADER));
        return errors;
    }

    /**
     * Converts a single line of text containing an ObjectFile Record into a Token, and
     * updates the state of the parser with the information.
     * 
     * @param line
     */
    private void tokensizeLine(final String line) {
        String token = "";
        // Comments begin with "//" or "#"
        final String[] pieces = line.split("//|#");
        if (pieces.length > 0)
            token = pieces[0].trim();
        sourceLine = -1;
        final Matcher m = ppRegex.matcher(line);
        while (m.find())
            parsePreprocessorCommand(m.group());
        if (token.length() > 0)
            try {
                if (token.matches("[hH].{6}[0-9A-Fa-f]{8}")
                    && token.substring(1, 7).matches("[A-Za-z_][A-Za-z_0-9]* *"))
                    parseHeader(token);
                else if (token.matches("[tT][0-9A-Fa-f]{8}(M[01]|X[01][0-9A-Za-z_]+)?"))
                    text.add(parseTextLine(token));
                else if (token.matches("[lagLAG][0-9A-Za-z_]+=[0-9A-Fa-f]{4}?"))
                    parseSymbolLine(token);
                else if (token.matches("[eE][0-9A-Fa-f]{4}(M1)?"))
                    parseExec(token);
                else
                    errors.add(new Error(lineNumber, token, SimCodes.PARSE_BAD_TEXT));
            } catch (final ParseException e) {
                errors.add(e.getError());
            }
    }

    private void parsePreprocessorCommand(final String token) {
        switch (token.charAt(2)) {
        case 'L':
            try {
                sourceLine = Integer.parseInt(token.substring(3, token.length() - 1));
            } catch (final NumberFormatException e) {
                errors
                    .add(new Error(lineNumber,
                        "could not read LINE preprocessor command",
                        SimCodes.PARSE_EXECPTION));
            }
            break;
        case 'S':
            final int colon = token.lastIndexOf("=");
            if (colon == -1)
                errors
                    .add(new Error(lineNumber,
                        "could not read SYMB preprocessor command",
                        SimCodes.PARSE_EXECPTION));
            else {
                final String symb = token.substring(3, colon);
                final boolean bad = !symb.matches("[A-Za-z0-9_]+")
                    || Character.isDigit(symb.charAt(0))
                    || symb.toLowerCase().matches("pc|r[0-7]");
                if (bad)
                    errors.add(new Error(lineNumber,
                        "invalid symbol name '" + symb + "'", SimCodes.PARSE_EXECPTION));
                final Short v = Utilities.parseShort(token.substring(colon + 1,
                    token.length() - 1));
                if (v == null)
                    errors.add(new Error(lineNumber, "'"
                        + token.substring(colon + 1, token.length() - 1)
                        + "' is not a number", SimCodes.PARSE_EXECPTION));
                if (!bad && v != null)
                    symbols.put(name + "." + symb, new Location(false, v));
            }
            break;
        }
    }

    /**
     * Parses a Text Record from a line based on the Text Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private Text parseTextLine(final String line) throws ParseException {
        final short addr = (short) Integer.parseInt(line.substring(1, 5), 16), val = (short) Integer
            .parseInt(line.substring(5, 9), 16);
        Text t;
        if (line.length() <= 9)
            t = new Text(lineNumber, sourceLine, addr, val, -1, null);
        else if (line.charAt(9) == 'M')
            t = new Text(lineNumber, sourceLine, addr, val, line.charAt(10) - '0', null);
        else {
            externals.add(line.substring(11));
            t = new Text(lineNumber, sourceLine, addr, val, line.charAt(10) - '0',
                line.substring(11));
        }
        if (segName == null)
            throw new ParseException(lineNumber, SimCodes.PARSE_HEADER_FIRST);
        if (!isWithinBounds(addr))
            throw new ParseException(lineNumber, SimCodes.ADDR_OUT_BOUNDS);
        return t;
    }

    /**
     * Parses a Symbol Record from a line based on the Symbol Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private void parseSymbolLine(final String line) {
        final int eq = line.indexOf('=');
        String symb = line.substring(1, eq);
        if (line.charAt(0) != 'G')
            symb = name + "." + symb;
        symbols.put(
            symb,
            new Location(line.charAt(0) != 'A', (short) Integer.parseInt(
                line.substring(eq + 1, eq + 5), 16)));
    }

    /**
     * Parses a Header Record from a line based on the Header Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private Header parseHeader(final String line) {
        segName = line.substring(1, 7).trim();

        beginAddress = (short) Integer.parseInt(line.substring(7, 11), 16);
        lengthOffset = (short) Integer.parseInt(line.substring(11), 16);
        return new Header(lineNumber, name, beginAddress, lengthOffset);
    }

    /**
     * Parses a Exec Record from a line based on the Exec Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private void parseExec(final String line) throws ParseException {
        // It's not necessary to check for a good string here, because we
        // already have, with the pattern matching earlier.
        final short addr = (short) Integer.parseInt(line.substring(1, 5), 16);
        if (segName == null)
            throw new ParseException(lineNumber, SimCodes.PARSE_HEADER_FIRST);
        if (!isWithinBounds(addr))
            throw new ParseException(lineNumber, SimCodes.ADDR_EXEC_OUT_BOUNDS);
        symbols.put(segName, new Location(line.length() > 5, addr));
    }

    /**
     * After parsing, returns the list of parsed text records.
     * 
     * @return a List of Text records
     */
    public List<Text> getParsedTexts() {
        return text;
    }

    public Map<String, Location> getParsedGlobals() {
        return symbols;
    }

    public Set<String> getParsedExternals() {
        return externals;
    }

    public String getSegName() {
        return segName;
    }

    public String getFileName() {
        return name;
    }

    public short getSize() {
        return lengthOffset;
    }

    public boolean isWithinBounds(final short address) {
        return (address - beginAddress & 0xffff) < lengthOffset;
    }
}
