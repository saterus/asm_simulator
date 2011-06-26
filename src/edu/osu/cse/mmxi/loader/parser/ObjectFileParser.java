package edu.osu.cse.mmxi.loader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// TODO: Check where we are throwing ParseExceptions. Build Error tokens instead, maybe.
// Some of them would make sense to just record the error and move on. Others are fatal
// errors that halt the parser and subsequently, the loader.

/**
 * Parses and tokenizes a ObjectFile for consumption by the Loader.
 * 
 * The Parser fundamentally splits the ObjectFile into 3 parts: the HeaderRecord, the
 * TextRecords, and the ExecRecord. If any of these is missing, the parser fails. There
 * must be exactly one HeaderRecord, at least one TextRecord, and exactly one ExecRecord,
 * in that order exclusively.
 * 
 * HeaderRecords contain basic information about the program being loaded. 15 ASCII
 * characters wide, optionally followed by a space and code comments. <code>
 * Format: 0: 'H'
 *       1-6: [Hex] Name.
 *      7-10: [Hex] Beginning absolute memory address.
 *     11-14: [Hex] Length of the block of memory being initially allocated for the program.
 * </code>
 * 
 * TextRecords contain the actual instructions and data being loaded for the program. 9
 * ASCII characters wide, optionally followed by a space and code comments. <code>
 * Format: 0: 'T'
 *       1-4: [Hex] Absolute memory address of the data.
 *       5-8: [Hex] Value of the address.
 * </code>
 * 
 * ExecRecords contain the initial value for the PC Register, where to begin the program.
 * 5 ASCII characters wide, optionally followed by a space and code comments. <code>
 * Format: 0: 'E'
 *       1-4: [Hex] Absolute memory address of the beginning of the program.
 * </code>
 * 
 * @see MMXI Programmer's Guide for sample ObjectFiles.
 * 
 * @note In the event of most parse failures, the parser should continue on with the rest
 *       of the file. The failures are recorded and get reported as a group of all the
 *       parse errors in the file.
 */
public class ObjectFileParser {

    private final BufferedReader reader;

    private int                  lineNumber = 1;
    private final List<Error>    errors     = new ArrayList<Error>();

    /** [Bits in Hex Rep.] | 7: page | 9: address | */
    private final List<Text>     text       = new ArrayList<Text>();

    /** [Bits in Hex Rep.] | 4: initial exec address | */
    private Exec                 exec       = null;

    /** [Bits in Hex Rep.] | 6: name | 4: begin address | 4: segment length | */
    private Header               header     = null;

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
     * @throws IOException
     */
    public List<Error> parse() throws IOException {

        String line = reader.readLine();

        while (line != null) {
            if (line.length() > 0)
                tokensizeLine(line);

            lineNumber++;
            line = parseLine(reader.readLine());
        }

        if (header == null && exec == null && text.size() == 0)
            errors.add(new Error("Parsing completed, no tokens found! Empty file?"));
        else {
            if (header == null)
                errors.add(new Error("Object File did not contain a Header record!"));
            if (text.size() == 0)
                errors.add(new Error("Object File did not contain any Text records!"));
            if (exec == null)
                errors.add(new Error("Object File did not contain an Exec record!"));
        }
        return errors;
    }

    /**
     * Converts a single line of text containing an ObjectFile Record into a Token.
     * 
     * @param line
     * @return a token corresponding to the data parsed from the line.
     */
    private void tokensizeLine(final String line) {
        try {
            if (line.matches("(H|h).{6}[0-9A-Fa-f]{8}"))
                header = parseHeader(line);
            else if (line.matches("(T|t)[0-9A-Fa-f]{8}"))
                text.add(parseTextLine(line));
            else if (line.matches("(E|e)[0-9A-Fa-f]{4}"))
                exec = parseExec(line);
            else
                errors.add(new Error(lineNumber,
                        "Unrecognized or malformed record. Line: " + line));
        } catch (final ParseException e) {
            errors.add(new Error(lineNumber, e.getMessage()));
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
        return new Text(lineNumber, (short) Integer.parseInt(line.substring(1, 5), 16),
                (short) Integer.parseInt(line.substring(5), 16));
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
     * Takes a line and strips, whitespace, and comments from it.
     * 
     * @param rawLine
     *            unprocessed line straight from the stream
     * @return a line ready for more parsing
     */
    private String parseLine(String rawLine) {
        if (rawLine != null) {
            // Comments begin with "//" or "#"
            final String[] pieces = rawLine.split("//|#");

            if (pieces.length > 0)
                rawLine = pieces[0].trim();
        }
        return rawLine;
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
}