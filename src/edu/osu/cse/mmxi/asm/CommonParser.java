package edu.osu.cse.mmxi.asm;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.asm.InstructionFormat.IFRecord;
import edu.osu.cse.mmxi.asm.table.PsuedoOpTable;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public class CommonParser {
    private static final String zeroArgOps, allOps, pseudoOps;
    static {
        final SortedSet<String> zao = new TreeSet<String>(), ao = new TreeSet<String>();
        for (final List<IFRecord> i : InstructionFormat.instructions.values())
            for (final IFRecord j : i) {
                if (j.signature.length() == 0)
                    zao.add(j.name.toUpperCase());
                ao.add(j.name.toUpperCase());
            }
        String zaoS = "[.]ORIG", aoS = "", poS = "";
        for (final String op : zao)
            zaoS += "|" + op;
        for (final String op : ao)
            aoS += "|" + op;
        for (final String op : PsuedoOpTable.table.keySet())
            poS += "|" + op.substring(1).toUpperCase();
        zeroArgOps = zaoS;
        allOps = aoS.substring(1);
        pseudoOps = "[.](" + poS.substring(1) + ")";
    }

    public static String[] parseLine(String line) throws ParseException {
        if (line.contains(";"))
            line = line.substring(0, line.indexOf(';'));
        if (line.trim().length() == 0)
            return new String[] { null, null };

        if (line.matches("\\s*[0-9A-Za-z_]+\\s*"))
            return parseLine(line, !line.trim().toUpperCase().matches(zeroArgOps));
        final Matcher m = Pattern.compile("\\s*(\\S+)\\s+(\\S+)(.*)").matcher(
            line.toUpperCase());
        if (m.matches())
            // I arrived at the complicated boolean expression below by enumerating all
            // the possibilities of the first two tokens being ops, zero-arg ops, or
            // something else, and determining whether in each case the first token should
            // be considered a label or the opcode. Ex: "NOP NOP" has a label, "NOP" does
            // not, "BRZ" does, "BRZ NOP" does, "BRZ NOP, x0" does not. (The whole reason
            // for this is the ambiguity of that first token, since labels have names that
            // are also opcodes, and we have relaxed the column constraint, so other clues
            // must be taken into account.) See CommonParserTest.testParseLine() for
            // examples.
            return parseLine(
                line,
                m.group(2).equals(".ORIG") || m.group(2).matches(zeroArgOps)
                    ^ m.group(3).trim().length() != 0
                    && m.group(2).matches(allOps + "|" + pseudoOps));
        throw new ParseException("unknown puctuation in label or opcode fields");
    }

    private static String[] parseLine(final String line, final boolean hasLabel)
        throws ParseException {
        final int l = hasLabel ? 1 : 0;
        final String[] tokens = line.trim().split("\\s+", l + 2);
        final String label = hasLabel ? tokens[0] : null;
        if (tokens.length <= l + 1)
            return new String[] { label,
                    tokens.length == l ? null : tokens[l].toUpperCase() };
        final String[] args;
        if (tokens[l + 1].matches("\\s*\".*(?<!\\\\)\"\\s*")) {
            String str = tokens[l + 1].trim();
            str = str.substring(1, str.length() - 1);
            args = new String[] { "\"" + MemoryUtilities.parseString(str) + "\"" };
        } else
            args = tokens[l + 1].split(",");
        final String[] ret = new String[args.length + 2];
        ret[0] = label;
        ret[1] = tokens[l].toUpperCase();
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
        if (line[1] != null)
            if (line[1].matches(allOps)) {
                if (!InstructionFormat.instructions.containsKey(line[1] + ":"
                    + (line.length - 2)))
                    throw new ParseException("incorrect number of arguments");
            } else if (!line[1].matches(pseudoOps))
                throw new ParseException("unknown opcode");
        return line;
    }
}
