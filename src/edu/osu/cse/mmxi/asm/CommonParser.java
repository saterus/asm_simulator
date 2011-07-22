package edu.osu.cse.mmxi.asm;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.asm.InstructionFormat.IFRecord;
import edu.osu.cse.mmxi.sim.loader.parser.ParseException;
import edu.osu.cse.mmxi.sim.machine.memory.MemoryUtilities;

public class CommonParser {
    private static final String zeroArgOps, allOps,
        pseudoOps = "ORIG|EQU|FILL|BLKW|STRZ|END"; // TODO should be exported to
                                                   // PseudoOpTable
    static {
        final SortedSet<String> zao = new TreeSet<String>(), ao = new TreeSet<String>();
        for (final List<IFRecord> i : InstructionFormat.instructions.values())
            for (final IFRecord j : i) {
                if (j.signature.length() == 0)
                    zao.add(j.name);
                ao.add(j.name);
            }
        String zaoS = "", aoS = "";
        for (final String op : zao)
            zaoS += "|" + op;
        for (final String op : ao)
            aoS += "|" + op;
        zeroArgOps = zaoS.substring(1);
        allOps = aoS.substring(1);
    }

    public static String[] parseLine(String line) throws ParseException {
        if (line.contains(";"))
            line = line.substring(line.indexOf(';'));
        if (line.matches("\\s*[0-9A-Za-z_]+\\s*"))
            return parseLine(line, !line.trim().toUpperCase().matches(zeroArgOps));
        final Matcher m = Pattern.compile("\\s*([0-9A-Za-z_]+)\\s+([.]?[A-Za-z]+).*")
            .matcher(line);
        if (m.matches())
            return parseLine(line,
                m.group(1).matches(zeroArgOps) || m.group(2).matches(zeroArgOps));
        throw new ParseException("unknown puctuation in label or opcode fields");
    }

    private static String[] parseLine(final String line, final boolean hasLabel)
        throws ParseException {
        final String[] tokens = line.split("\\s+", 3);
        final String label = hasLabel ? tokens[0] : null;
        final int l = hasLabel ? 1 : 0;
        if (tokens.length <= l + 1)
            return new String[] { label, tokens.length == l ? null : tokens[l] };
        final String[] args = tokens[l + 1].split(","), ret = new String[args.length + 2];
        ret[0] = label;
        ret[1] = tokens[l];
        for (int i = 0; i < args.length; i++)
            ret[i + 2] = args[i].trim();
        return ret;
    }

    public static String[] checkLine(final String[] line) throws ParseException {
        if (line[0] != null) {
            if (MemoryUtilities.parseShort(line[0]) != -1)
                throw new ParseException("numbers can not be labels");
            if (line[0].matches("[rR][0-7]"))
                throw new ParseException("registers can not be labels");
        }
        if (line[1] != null && !line[1].matches(allOps)
            && !(line[1].charAt(0) == '.' && line[1].substring(1).matches(pseudoOps)))
            throw new ParseException("unknown opcode");
        return line;
    }
}
