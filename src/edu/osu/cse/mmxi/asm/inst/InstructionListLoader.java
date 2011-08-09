package edu.osu.cse.mmxi.asm.inst;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import edu.osu.cse.mmxi.asm.InstructionFormat;

public class InstructionListLoader {

    public static void load() {
        load("/edu/osu/cse/mmxi/asm/inst/instructions.xml");
    }

    public static void load(final String file) {
        final List<String> inst = new ArrayList<String>();
        final InputStream is = InstructionFormat.class.getResourceAsStream(file);

        try {
            final XMLReader xr = SAXParserFactory.newInstance().newSAXParser()
                .getXMLReader();
            xr.setContentHandler(new DefaultHandler() {
                @Override
                public void startElement(final String uri, final String localName,
                    final String qName, final Attributes attr) {
                    if (qName.equals("inst")) {
                        inst.add(attr.getValue("name"));
                        inst.add(attr.getValue("args"));
                    } else if (qName.equals("word"))
                        inst.add(attr.getValue("data"));
                }

                @Override
                public void endElement(final String uri, final String localName,
                    final String qName) {
                    if (qName.equals("inst")) {
                        InstructionFormat.addIFRecord(inst.toArray(new String[0]));
                        inst.clear();
                    }
                }
            });
            xr.parse(new InputSource(is));
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }
}
