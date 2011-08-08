package edu.osu.cse.mmxi.asm.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import edu.osu.cse.mmxi.asm.error.AsmCodes;
import edu.osu.cse.mmxi.common.error.ParseException;

/**
 * Handles all the I/O functions for the assebler including: reading input assembly file,
 * writing output file, writing intermediate file.
 * 
 */
public class IO {

    /**
     * Reads the input file
     */
    private BufferedReader iReader;

    /**
     * The file names for intermediate file (iFile), output file (oFile), listing file
     * (lFile)
     */
    private String         iFile, oFile, lFile;

    /**
     * Writes the output file
     */
    private BufferedWriter oWriter;

    /**
     * Writes the listing file (optionally directed to standard out)
     */
    private BufferedWriter lWriter;

    /**
     * Writes a file in bulk, given the filename and a string containing the data to
     * write.
     * 
     * @param file
     *            The filename
     * @param data
     *            The string to write to the file
     * @throws IOException
     */
    public static void writeFile(final String file, final String data) throws IOException {
        if (file == null)
            return;
        final FileOutputStream fos = new FileOutputStream(file);
        fos.write(data.getBytes());
        fos.close();
    }

    /**
     * Open a new buffered reader. Will call closeReader() if reader is already open
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @throws FileNotFoundException
     * @see closeReader()
     */
    public void openReader(final String file) throws ParseException {
        final File f = new File(iFile = file);
        if (!f.exists())
            throw new ParseException(AsmCodes.IO_BAD_PATH, file + " not found");
        if (!f.isFile())
            throw new ParseException(AsmCodes.IO_BAD_PATH, file + " is a directory");
        if (!f.canRead())
            throw new ParseException(AsmCodes.IO_BAD_READ,
                "unable to open file for reading: " + file);
        try {
            if (iReader != null)
                iReader.close();
            iReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        } catch (final IOException e) {
            // failed to close - no big deal.
        }
    }

    /**
     * Starts the reader over from the beginning.
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @see closeReader()
     */
    public void resetReader() throws ParseException {
        openReader(iFile);
    }

    /**
     * Opens a new writer for file. Will close writer if it was already open.
     * 
     * @param oFile
     *            The output file filename
     * @param lFile
     *            The listing file filename, or {@code null} for output to standard out
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void openWriters(final String oFile, final String lFile) throws ParseException {
        final File o = new File(this.oFile = oFile);
        if (o.exists() && !o.canWrite())
            throw new ParseException(AsmCodes.IO_BAD_WRITE,
                "unable to open file for writing: " + oFile);
        try {
            if (oWriter != null)
                oWriter.close();
            oWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(o)));
        } catch (final IOException e) {
            throw new ParseException(AsmCodes.IO_BAD_WRITE,
                "unable to open file for writing: " + oFile);
        }
        this.lFile = lFile;
        if (lFile != null) {
            final File l = new File(lFile);
            if (l.exists() && !l.canWrite())
                throw new ParseException(AsmCodes.IO_BAD_WRITE,
                    "unable to open file for writing: " + lFile);
            try {
                if (lWriter != null)
                    lWriter.close();
                lWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    l)));
            } catch (final IOException e) {
                throw new ParseException(AsmCodes.IO_BAD_WRITE,
                    "unable to open file for writing: " + lFile);
            }
        } else
            lWriter = null;
    }

    /**
     * Retrieves one line of data from the input stream (the input file).
     * 
     * @throws IOException
     */
    public String getLine() throws ParseException {
        try {
            return iReader.readLine();
        } catch (final IOException e) {
            throw new ParseException(AsmCodes.IO_BAD_READ,
                "unable to read line from file");
        }

    }

    /**
     * Writes one line of data to the output file stream.
     * 
     * @param line
     *            the line of data to write to the file
     * @throws IOException
     */
    public void writeOLine(final String line) throws ParseException {
        try {
            oWriter.write(line + "\n");
        } catch (final IOException e) {
            throw new ParseException(AsmCodes.IO_BAD_WRITE, "unable to write to output: "
                + line);
        }
    }

    /**
     * Writes one line of data to the listing file stream.
     * 
     * @param line
     *            the line of data to write to the file
     * @throws IOException
     */
    public void writeLLine(final String line) throws ParseException {
        if (lFile == null)
            System.out.println(line);
        else
            try {
                lWriter.write(line + "\n");
            } catch (final IOException e) {
                throw new ParseException(AsmCodes.IO_BAD_WRITE,
                    "unable to write to listing file: " + line);
            }
    }

    /**
     * Closes the input file stream.
     * 
     */
    public void closeReader() {
        try {
            if (iReader != null)
                iReader.close();
        } catch (final IOException e) {
        }
    }

    /**
     * Closes the writers for both the output and listing files.
     * 
     */
    public void closeWriters(final boolean delete) {
        try {
            if (oWriter != null)
                oWriter.close();
            if (lWriter != null && lFile != null)
                lWriter.close();
            oWriter = lWriter = null;
            if (delete) {
                new File(oFile).delete();
                if (lFile != null)
                    new File(lFile).delete();
            }
        } catch (final IOException e) {
        }
    }

}
