package edu.osu.cse.mmxi.loader.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectFileParser {

    private enum LineType {
        Header, Text, Exec, Error
    };

    private final BufferedReader reader;
    private final List<Token>    tokens;

    private boolean              foundHeader   = false;
    private boolean              foundText     = false;
    private boolean              foundExec     = false;

    /** |6: name | 4: begin address | 4: segment length | 1: Newline | */
    private final static int     HEADER_LENGTH = 14;

    /** |7: page | 9: address | 1: Newline | */
    private final static int     TEXT_LENGTH   = 8;

    /** |4: initial exec address | 1: Newline | */
    private final static int     EXEC_LENGTH   = 4;

    private int                  lineNumber;

    public ObjectFileParser(final BufferedReader reader) {
        this.lineNumber = 0;
        this.reader = reader;
        this.tokens = new ArrayList<Token>();
    }

    public List<Token> parse() {

        try {
            String line = this.reader.readLine();

            while (line != null) {

                if (this.emptyLine(line)) { // skip blank lines
                    continue;
                }

                this.tokens.add(this.tokensizeLine(line));

                this.lineNumber++;
                line = this.parseLine(this.reader.readLine());
            }

        } catch (final IOException ex) {

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
