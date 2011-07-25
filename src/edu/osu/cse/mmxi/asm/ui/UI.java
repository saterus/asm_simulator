package edu.osu.cse.mmxi.asm.ui;

import edu.osu.cse.mmxi.asm.Assembler;
import edu.osu.cse.mmxi.asm.error.Error;
import edu.osu.cse.mmxi.asm.error.ErrorCodes;
import edu.osu.cse.mmxi.common.UImain;

public class UI extends UImain {
    @Override
    public void printErrorEndOfFile() {
        Assembler.printErrors(this, new Error("while reading number",
            ErrorCodes.EXEC_END_OF_FILE));
    }
}
