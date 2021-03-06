package edu.osu.cse.mmxi.junit.asm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.symb.ArithmeticParser;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression;
import edu.osu.cse.mmxi.common.error.ParseException;

public class ArithmeticParserTest {

    @Test
    public void test2() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("2");
        assertEquals("x2", se.toString());
        assertEquals(2, (short) se.evaluate());
    }

    @Test
    public void test2plus2() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("2+2");
        assertEquals("x4", se.toString());
        assertEquals(4, (short) se.evaluate());
    }

    @Test
    public void testPrec() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("1+2*4-12%5/3&6^0xA**2**2");
        assertEquals("x2710", se.toString());
        assertEquals(10000, (short) se.evaluate());
    }

    @Test
    public void testParen() throws ParseException {
        final SymbolExpression se = ArithmeticParser
            .parse("(1+((2*4)-(12))%(5/(((3))&6))^(0xA**2)**2)");
        assertEquals("x2711", se.toString());
        assertEquals(10001, (short) se.evaluate());
    }

    @Test
    public void testSymb() throws ParseException {
        final SymbolExpression se = ArithmeticParser.parse("(4-2)+x*y-z");
        Symbol.getSymb("x").set(ArithmeticParser.parse("y-z"));
        Symbol.getSymb("y").set(ArithmeticParser.parse("4<<2"));
        Symbol.getSymb("z").set(ArithmeticParser.parse("y*y>>>2"));
        assertEquals("y - z", Symbol.getSymb("x").value.toString());
        assertEquals("x10", Symbol.getSymb("y").value.toString());
        assertEquals("x40", Symbol.getSymb("z").value.toString());
        assertEquals(-48, (short) Symbol.getSymb("x").evaluate());
        assertEquals(16, (short) Symbol.getSymb("y").evaluate());
        assertEquals(64, (short) Symbol.getSymb("z").evaluate());
        assertEquals("x2 + x * y - z", se.toString());
        assertEquals(-830, (short) se.evaluate());
    }

    @Test
    public void testSimplify() throws ParseException {
        Symbol.symbs.clear();
        SymbolExpression se = ArithmeticParser.parse("x+1-(y+2-3)-z-z");
        assertEquals("x + x1 - (y + x2 - x3) - z - z", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("x - y - x2 * z + x2", se.toString());

        se = ArithmeticParser.parse("x+5-(x+2-y)");
        assertEquals("x + x5 - (x + x2 - y)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("y + x3", se.toString());

        se = ArithmeticParser.parse("x+5-(x+7)");
        assertEquals("x + x5 - (x + x7)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("-x2", se.toString());

        se = ArithmeticParser.parse("x+5-(x+5)");
        assertEquals("x + x5 - (x + x5)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("x0", se.toString());

        se = ArithmeticParser.parse("x+5-(y+5)");
        assertEquals("x + x5 - (y + x5)", se.toString());
        se = ArithmeticParser.simplify(se);
        assertEquals("x - y", se.toString());
    }

    @Test
    public void inClass() throws ParseException {
        Symbol.getSymb("x").set(ArithmeticParser.parse("ST+16"));
        Symbol.getSymb("y").set(ArithmeticParser.parse("ST+6"));
        Symbol.getSymb("z").set(ArithmeticParser.parse("4"));

        SymbolExpression se = ArithmeticParser.parse("x+1-z");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("x+1-z");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("2+x/y");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("z+x");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("z-y");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("y-z");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("(x-z)/2");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("((x-y)-(z+y))*y");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("(x/2)-(z/2)");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

        se = ArithmeticParser.parse("(x-z)/2");
        se = ArithmeticParser.simplify(se);
        System.out.println(se);

    }
}
