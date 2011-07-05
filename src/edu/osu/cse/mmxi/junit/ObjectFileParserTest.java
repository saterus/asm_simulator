package edu.osu.cse.mmxi.junit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Before;

import edu.osu.cse.mmxi.loader.parser.ObjectFileParser;

public class ObjectFileParserTest {
    private static dir = "src/osu/cse/mmxi/junit/";
    private final ObjectFileParser parser = new ObjectFileParser(new BufferedReader(
                                              new InputStreamReader(new FileInputStream(
                                                  +"sample2.txt"))));

    @Before
    public void init() {

    }
}
