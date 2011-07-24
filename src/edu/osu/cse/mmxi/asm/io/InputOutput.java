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
    private String         readerFile;

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
     * @see closeReader()
     */
    public void openReader(final String file) throws IOException, FileNotFoundException {
        if (reader != null)
            reader.close();
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(
            readerFile = file)));
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
        openReader(readerFile);
    }

    /**
     * Opens a new writer for file. Will close writer if it was already open.
     * 
     * @param file
     *            The filename
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void openWriter(final String file) throws IOException, FileNotFoundException {
        if (writer != null)
            writer.close();
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
    }

    public String getLine() throws IOException {
        String line = null;
        line = reader.readLine();
        return line;
    }

    public void writeLine(final String line) throws IOException {
        writer.write(line);
    }

    public void closeReader() {
        try {
            if (reader != null)
                reader.close();
        } catch (final IOException e) {
        }
    }

    public void closeWriter() {
        try {
            if (writer != null)
                writer.close();
        } catch (final IOException e) {
        }
    }

}
