package edu.osu.cse.mmxi.asm.table;

public class MachineOpTable {

    public static boolean isInTable2(String candidate) {
        candidate = candidate.toUpperCase();
        boolean status = false;
        if (candidate.equals("ADD"))
            status = true;
        else if (candidate.equals("AND"))
            status = true;
        else if (candidate.equals("BRN"))
            status = true;
        else if (candidate.equals("BRNZ"))
            status = true;
        else if (candidate.equals("BRNZP"))
            status = true;
        else if (candidate.equals("BRNP"))
            status = true;
        else if (candidate.equals("BRNZ"))
            status = true;
        else if (candidate.equals("BRP"))
            status = true;
        else if (candidate.equals("DBUG"))
            status = true;
        else if (candidate.equals("JSR"))
            status = true;
        else if (candidate.equals("JMP"))
            status = true;
        else if (candidate.equals("JSRR"))
            status = true;
        else if (candidate.equals("JMPR"))
            status = true;
        else if (candidate.equals("LD"))
            status = true;
        else if (candidate.equals("LDI"))
            status = true;
        else if (candidate.equals("LDR"))
            status = true;
        else if (candidate.equals("LEA"))
            status = true;
        else if (candidate.equals("NOT"))
            status = true;
        else if (candidate.equals("RET"))
            status = true;
        else if (candidate.equals("ST"))
            status = true;
        else if (candidate.equals("STI"))
            status = true;
        else if (candidate.equals("STR"))
            status = true;
        else if (candidate.equals("TRAP"))
            status = true;
        return status;
    }

    // converts the Instruction to opcode in binary function.
    // use that to concatanate with the rest of the binaries.
    public static String binaryOp(String candidate) {
        candidate = candidate.toUpperCase();
        String opcode = "";
        if (candidate.equals("ADD"))
            opcode = "0001";
        else if (candidate.equals("AND"))
            opcode = "0101";
        else if (candidate.equals("BRN"))
            opcode = "0000";
        else if (candidate.equals("BRNZ"))
            opcode = "0000";
        else if (candidate.equals("BRNZP"))
            opcode = "0000";
        else if (candidate.equals("BRNP"))
            opcode = "0000";
        else if (candidate.equals("BRNZ"))
            opcode = "0000";
        else if (candidate.equals("BRP"))
            opcode = "0000";
        else if (candidate.equals("DBUG"))
            opcode = "1000";
        else if (candidate.equals("JSR"))
            opcode = "0100";
        else if (candidate.equals("JMP"))
            opcode = "0000";
        else if (candidate.equals("JSRR"))
            opcode = "1100";
        else if (candidate.equals("JMPR"))
            opcode = "1100";
        else if (candidate.equals("LD"))
            opcode = "0010";
        else if (candidate.equals("LDI"))
            opcode = "1010";
        else if (candidate.equals("LDR"))
            opcode = "0110";
        else if (candidate.equals("LEA"))
            opcode = "1110";
        else if (candidate.equals("NOT"))
            opcode = "1001";
        else if (candidate.equals("RET"))
            opcode = "1101";
        else if (candidate.equals("ST"))
            opcode = "0011";
        else if (candidate.equals("STI"))
            opcode = "1011";
        else if (candidate.equals("STR"))
            opcode = "0111";
        else if (candidate.equals("TRAP"))
            opcode = "1111";

        return opcode;
    }

}
