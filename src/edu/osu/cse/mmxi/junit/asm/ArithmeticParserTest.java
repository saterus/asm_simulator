package edu.osu.cse.mmxi.junit.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.ParseException;

public class ArithmeticParserTest {

    @Test
    public void test2() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("2");
        assertEquals("2", se.toString());
        assertEquals(2, (short) se.evaluate());
    }

    @Test
    public void test2plus2() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("2+2");
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("4", se.toString());
        else
            assertEquals("2 + 2", se.toString());
        assertEquals(4, (short) se.evaluate());
    }

    @Test
    public void testPrec() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("1+2*4-12%5/3&6^0xA**2**2");
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("10000", se.toString());
        else
            assertEquals("1 + 2 * 4 - 12 % 5 / 3 & 6 ^ 10 ** 2 ** 2", se.toString());
        assertEquals(10000, (short) se.evaluate());
    }

    @Test
    public void testParen() throws ParseException {
        final SymbolExpression se = ArithmeticParser
            .parse("(1+((2*4)-(12))%(5/(((3))&6))^(0xA**2)**2)");
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("10001", se.toString());
        else
            assertEquals("1 + (2 * 4 - 12) % (5 / (3 & 6)) ^ (10 ** 2) ** 2",
                se.toString());
        assertEquals(10001, (short) se.evaluate());
    }

    @Test
    public void testSymb() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("(4-2)+x*y-z");
        Symbol.getSymb("x").set(ArithmeticParser.parse("y-z"));
        Symbol.getSymb("y").set(ArithmeticParser.parse("4<<2"));
        Symbol.getSymb("z").set(ArithmeticParser.parse("y*y>>>2"));
        assertEquals("y - z", Symbol.getSymb("x").value.toString());
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("16", Symbol.getSymb("y").value.toString());
        else
            assertEquals("4 << 2", Symbol.getSymb("y").value.toString());
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("64", Symbol.getSymb("z").value.toString());
        else
            assertEquals("y * y >>> 2", Symbol.getSymb("z").value.toString());
        assertEquals(-48, (short) Symbol.getSymb("x").evaluate());
        assertEquals(16, (short) Symbol.getSymb("y").evaluate());
        assertEquals(64, (short) Symbol.getSymb("z").evaluate());
        if (ArithmeticParser.collapseIfEvaluable)
            assertEquals("2 + x * y - z", se.toString());
        else
            assertEquals("4 - 2 + x * y - z", se.toString());
        assertEquals(-830, (short) se.evaluate());
    }

    @Test
    public void testSimplify() throws ParseException {
        if (!ArithmeticParser.collapseIfEvaluable)
            fail("this test requires collapseIfEvaluable = true");
        Symbol.symbs.clear();
        SymbolExpression se = ArithmeticParser.parse("x+1-(y+2-3)-z-z");
        assertEquals("x + 1 - (y + 2 - 3) - z - z", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("x - y - 2 * z + 2", se.toString());

        se = ArithmeticParser.parse("x+5-(x+2-y)");
        assertEquals("x + 5 - (x + 2 - y)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("y + 3", se.toString());

        se = ArithmeticParser.parse("x+5-(x+7)");
        assertEquals("x + 5 - (x + 7)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("-2", se.toString());

        se = ArithmeticParser.parse("x+5-(x+5)");
        assertEquals("x + 5 - (x + 5)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("0", se.toString());

        se = ArithmeticParser.parse("x+5-(y+5)");
        assertEquals("x + 5 - (y + 5)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("x - y", se.toString());
    }
}
