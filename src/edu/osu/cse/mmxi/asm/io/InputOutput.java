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
    private BufferedReader reader;

    /**
     * Writes the intermediate file
     */
    private BufferedWriter writer;

    /**
     * Open a new buffered reader. Will call closeReader() if reader is already open
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SecurityException
     * @see closeReader()
     */
    public void openReader(final String file) throws IOException, FileNotFoundException,
        SecurityException {
        try {
            if (reader != null)
                reader.close();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (final FileNotFoundException e) {

        } catch (final SecurityException e) {

        }
    }

    /**
     * Opens a new writer for file. Will close writer if it was already open.
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SecurityException
     */
    public void openWriter(final String file) throws IOException, FileNotFoundException,
        SecurityException {
        try {
            if (writer != null)
                writer.close();
            writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file)));
        } catch (final FileNotFoundException e) {

        } catch (final SecurityException e) {

        }
    }

    public String getLine() throws IOException {
        String line = null;
        try {
            line = reader.readLine();
        } catch (final IOException e) {

        }
        return line;
    }

    public void writeLine(final String line) throws IOException {
        try {
            writer.write(line);
        } catch (final IOException e) {

        }
    }

    public void closeReader() throws IOException {
        try {
            if (reader != null)
                reader.close();
        } catch (final IOException e) {

        }
    }

    public void closeWriter() throws IOException {
        try {
            if (writer != null)
                writer.close();
        } catch (final IOException e) {

        }
    }

}
