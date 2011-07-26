package edu.osu.cse.mmxi.asm.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class InputOutput {

    /**
     * Reads the input file
     */
    private BufferedReader iReader;
    private String         iFile, lFile;

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
    public void openReader(final String file) throws IOException, FileNotFoundException {
        if (iReader != null)
            iReader.close();
        iReader = new BufferedReader(new InputStreamReader(new FileInputStream(
            iFile = file)));
    }

    /**
     * Starts the reader over from the beginning.
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @see closeReader()
     */
    public void resetReader() throws IOException {
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
    public void openWriters(final String oFile, final String lFile) throws IOException,
        FileNotFoundException {
        this.lFile = lFile;
        if (oWriter != null)
            oWriter.close();
        if (lWriter != null)
            lWriter.close();
        oWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oFile)));
        lWriter = new BufferedWriter(new OutputStreamWriter(lFile == null ? System.out
            : new FileOutputStream(lFile)));
    }

    /**
     * Retrieves one line of data from the input stream (the input file).
     * 
     * @throws IOException
     */
    public String getLine() throws IOException {
        String line = null;
        line = iReader.readLine();
        return line;
    }

    /**
     * Writes one line of data to the output file stream.
     * 
     * @param line
     *            the line of data to write to the file
     * @throws IOException
     */
    public void writeOLine(final String line) throws IOException {
        oWriter.write(line);
    }

    /**
     * Writes one line of data to the listing file stream.
     * 
     * @param line
     *            the line of data to write to the file
     * @throws IOException
     */
    public void writeLLine(final String line) throws IOException {
        lWriter.write(line);
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
    public void closeWriters() {
        try {
            if (oWriter != null)
                oWriter.close();
            if (lWriter != null && lFile != null) // Don't close System.out!
                lWriter.close();
            oWriter = lWriter = null;
        } catch (final IOException e) {
        }
    }

}
