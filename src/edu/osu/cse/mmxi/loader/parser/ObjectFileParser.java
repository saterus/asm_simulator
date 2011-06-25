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

    // TODO: Discuss the horrible type heirarchy of Token and remember LineType.
    private enum LineType {
        Header, Text, Exec, Error
    };

    private final BufferedReader reader;
    private final List<Token>    tokens;

    private boolean              foundHeader   = false;
    private boolean              foundText     = false;
    private boolean              foundExec     = false;

    /** [Bits in Hex Rep.] | 6: name | 4: begin address | 4: segment length | */
    private final static int     HEADER_LENGTH = 14;

    /** [Bits in Hex Rep.] | 7: page | 9: address | */
    private final static int     TEXT_LENGTH   = 8;

    /** [Bits in Hex Rep.] | 4: initial exec address | */
    private final static int     EXEC_LENGTH   = 4;

    private int                  lineNumber;

    /**
     * The ObjectFileParser is built around processing an InputStream
     * 
     * @param reader
     *            BufferedReader wrapper over an InputStream containing an ObjectFile.
     */
    public ObjectFileParser(final BufferedReader reader) {
        this.lineNumber = 0;
        this.reader = reader;
        this.tokens = new ArrayList<Token>();
    }

    /**
     * Reads the stream line by line, parsing the line by its appropriate format, and
     * tokenizing the results.
     * 
     * @return list of Tokens obtained from parsing the Stream.
     * @throws IOException
     */
    public List<Token> parse() throws IOException {

        String line = this.reader.readLine();

        while (line != null) {

            if (!this.emptyLine(line)) { // TODO: Skip or Tokenize blank lines? Skipping.

                this.tokens.add(this.tokensizeLine(line));
            }

            this.lineNumber++;
            line = this.parseLine(this.reader.readLine());
        }

        if (this.tokens.size() == 0) {
            this.tokens.add(new Error("Parsing completed, no tokens found! Empty file?"));
        }

        if (!this.foundText) {
            this.tokens.add(new Error("Object File did not contain any Text records!"));
        } else if (!this.foundExec) {
            this.tokens.add(new Error("Object File did not contain an Exec record!"));
        }

        return this.tokens;
    }

    private Token tokensizeLine(final String line) {
        try {
            switch (this.parseLineType(line.charAt(0))) {
            case Text:
                return this.parseTextLine(line.substring(1));

            case Header:
                return this.parseHeader(line.substring(1));

            case Exec:
                return this.parseExec(line.substring(1));

            case Error:
                return new Error(this.lineNumber + ": Unrecognized line type. Line: "
                        + line);
            }
        } catch (final ParseException e) {
            return new Error(e.getMessage());
        }

        return new Error(this.lineNumber + ": Tokenization failed. Line: " + line);
    }

    private LineType parseLineType(final char firstChar) {
        switch (firstChar) {
        case 'H':
        case 'h':
            return LineType.Header;
        case 'T':
        case 't':
            return LineType.Text;

        case 'E':
        case 'e':
            return LineType.Exec;

        default:
            return LineType.Error;
        }
    }

    private Token parseTextLine(final String line) throws ParseException {
        this.foundText = true;

        if (!this.foundHeader) {
            throw new ParseException(this.lineNumber
                    + ": Header not first record! Line: " + line);
        }

        if (line.length() != TEXT_LENGTH) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Expected " + TEXT_LENGTH
                    + " characters to follow T. Found " + line.length()
                    + " characters following T.");
        }

        // TODO: Someone else should look at this to see if we couldn't use Shorts here.
        int address = 0;
        int value = 0;

        try {

            address = Integer.parseInt(line.substring(0, 4), 16);
            value = Integer.parseInt(line.substring(4), 16);

        } catch (final NumberFormatException e) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Unable to parse hex value of: "
                    + e.getMessage());
        }

        return new Text((short) address, (short) value);
    }

    private Token parseHeader(final String line) throws ParseException {
        this.foundHeader = true;

        if (line.length() != HEADER_LENGTH) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Expected " + HEADER_LENGTH
                    + " characters to follow H. Found " + line.length()
                    + " characters following H.");
        }

        final String name = line.substring(0, 6);
        int beginAddress;
        int lengthOffset;

        try {
            beginAddress = Integer.parseInt(line.substring(6, 10), 16);
            lengthOffset = Integer.parseInt(line.substring(10), 16);

        } catch (final NumberFormatException e) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Unable to parse hex value of: "
                    + e.getMessage());
        }

        return new Header(name, (short) beginAddress, (short) lengthOffset);
    }

    private Token parseExec(final String line) throws ParseException {
        this.foundExec = true;

        if (!this.foundHeader) {
            throw new ParseException(this.lineNumber
                    + ": Header not first record! Line: " + line);
        }

        if (line.length() != EXEC_LENGTH) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Expected " + EXEC_LENGTH
                    + " characters to follow E. Found " + line.length()
                    + " characters following E.");
        }

        int address = 0;

        try {

            address = Integer.parseInt(line.substring(0, 4), 16);

        } catch (final NumberFormatException e) {
            throw new ParseException(this.lineNumber
                    + ": Malformed text record! Unable to parse hex value of: "
                    + e.getMessage());
        }

        return new Exec((short) address);
    }

    private String parseLine(final String rawLine) {

        String line = rawLine;

        if (line != null && line.length() > 0) {

            // TODO: Handle Comments within ObjectFiles more gracefully.
            line = line.split(" ")[0];
        }

        return line;
    }

    private boolean emptyLine(final String s) {
        return s.length() <= 0;
    }

}
