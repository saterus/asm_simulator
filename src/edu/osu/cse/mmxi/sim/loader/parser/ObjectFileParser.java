package edu.osu.cse.mmxi.sim.loader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.common.ParseException;
import edu.osu.cse.mmxi.common.Utilities;
import edu.osu.cse.mmxi.sim.error.Error;
import edu.osu.cse.mmxi.sim.error.ErrorCodes;

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
public class ObjectFileParser {
    private static final Pattern     ppRegex    = Pattern.compile("#![FLS][^!]*!");

    private final BufferedReader     reader;

    private int                      lineNumber = 1;
    private final List<Error>        errors     = new ArrayList<Error>();

    /** [Bits in Hex Rep.] | 7: page | 9: address | */
    private final List<Text>         text       = new ArrayList<Text>();

    /** [Bits in Hex Rep.] | 4: initial exec address | */
    private Exec                     exec       = null;

    /** [Bits in Hex Rep.] | 6: name | 4: begin address | 4: segment length | */
    private Header                   header     = null;

    private final Map<String, Short> symbols    = new TreeMap<String, Short>();

    private int                      sourceLine = -1;

    private String                   sourceFile = null;

    /**
     * The ObjectFileParser is built around processing an InputStream
     * 
     * @param reader
     *            BufferedReader wrapper over an InputStream containing an ObjectFile.
     */
    public ObjectFileParser(final BufferedReader reader) {
        this.reader = reader;
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
                ErrorCodes.IO_BAD_READ));
        }

        while (line != null) {
            tokensizeLine(line);

            lineNumber++;
            try {
                line = reader.readLine();
            } catch (final IOException e) {
                errors.add(new Error("IO error while reading line " + lineNumber + ": "
                    + e.getMessage(), ErrorCodes.IO_BAD_READ));
            }
        }

        if (header == null && exec == null && text.size() == 0)
            errors.add(new Error(ErrorCodes.PARSE_EMPTY));
        else {
            if (header == null)
                errors.add(new Error(ErrorCodes.PARSE_NO_HEADER));
            if (text.size() == 0)
                errors.add(new Error(ErrorCodes.PARSE_NO_RECORDS));
            if (exec == null)
                errors.add(new Error(ErrorCodes.PARSE_NO_EXEC));
        }
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
                if (token.matches("(H|h).{6}[0-9A-Fa-f]{8}"))
                    header = parseHeader(token);
                else if (token.matches("(T|t)[0-9A-Fa-f]{8}(M[01])?"))
                    text.add(parseTextLine(token));
                else if (token.matches("(E|e)[0-9A-Fa-f]{4}"))
                    exec = parseExec(token);
                else
                    errors.add(new Error(lineNumber, token, ErrorCodes.PARSE_BAD_TEXT));
            } catch (final ParseException e) {
                errors.add(new Error(lineNumber, e.getMessage(),
                    ErrorCodes.PARSE_EXECPTION));
            }

    }

    private void parsePreprocessorCommand(final String token) {
        switch (token.charAt(2)) {
        case 'F':
            sourceFile = token.substring(3, token.length() - 1);
            break;
        case 'L':
            try {
                sourceLine = Integer.parseInt(token.substring(3, token.length() - 1));
            } catch (final NumberFormatException e) {
                errors.add(new Error(lineNumber,
                    "could not read LINE preprocessor command",
                    ErrorCodes.PARSE_EXECPTION));
            }
            break;
        case 'S':
            final int colon = token.lastIndexOf(":");
            if (colon == -1)
                errors.add(new Error(lineNumber,
                    "could not read SYMB preprocessor command",
                    ErrorCodes.PARSE_EXECPTION));
            else {
                final String symb = token.substring(3, colon);
                final boolean bad = symb.contains("+") || symb.contains("-")
                    || symb.contains(":") || symb.toLowerCase().matches("pc|r[0-7]");
                if (bad)
                    errors
                        .add(new Error(lineNumber, "invalid symbol name '" + symb + "'",
                            ErrorCodes.PARSE_EXECPTION));
                final Short v = Utilities.parseShort(token.substring(colon + 1,
                    token.length() - 1));
                if (v == null)
                    errors.add(new Error(lineNumber, "'"
                        + token.substring(colon + 1, token.length() - 1)
                        + "' is not a number", ErrorCodes.PARSE_EXECPTION));
                if (!bad && v != null)
                    symbols.put(symb, v);
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
        // It's not necessary to check for a good string here, because we
        // already have, with the pattern matching earlier.
        return new Text(lineNumber, sourceLine, sourceFile, (short) Integer.parseInt(
            line.substring(1, 5), 16), (short) Integer.parseInt(line.substring(5, 9), 16));
    }

    /**
     * Parses a Header Record from a line based on the Header Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private Header parseHeader(final String line) throws ParseException {
        final String name = line.substring(1, 7);
        // TODO: Is there any checking we need to do on the segment name?
        // if (false) {
        // throw new ParseException(lineNumber +
        // ": Malformed header! Segment name has incorrect format.");
        // }

        final int beginAddress = Integer.parseInt(line.substring(7, 11), 16);
        final int lengthOffset = Integer.parseInt(line.substring(11), 16);
        return new Header(lineNumber, name, (short) beginAddress, (short) lengthOffset);
    }

    /**
     * Parses a Exec Record from a line based on the Exec Record Format.
     * 
     * @param line
     * @return the token parsed from the line
     * @throws ParseException
     *             if the line does not conform to the format.
     */
    private Exec parseExec(final String line) {
        // It's not necessary to check for a good string here, because we
        // already have, with the pattern matching earlier.
        return new Exec(lineNumber, (short) Integer.parseInt(line.substring(1), 16));
    }

    /**
     * After parsing, returns the parsed header.
     * 
     * @return the Header of the file
     */
    public Header getParsedHeader() {
        return header;
    }

    /**
     * After parsing, returns the parsed execution record.
     * 
     * @return the Exec of the file
     */
    public Exec getParsedExec() {
        return exec;
    }

    /**
     * After parsing, returns the list of parsed text records.
     * 
     * @return a List of Text records
     */
    public List<Text> getParsedTexts() {
        return text;
    }

    /**
     * After parsing, returns the list of parsed text records.
     * 
     * @return a List of Text records
     */
    public Map<String, Short> getParsedSymbols() {
        return symbols;
    }
}
