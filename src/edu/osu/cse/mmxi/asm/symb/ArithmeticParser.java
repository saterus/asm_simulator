package edu.osu.cse.mmxi.asm.symb;

import static edu.osu.cse.mmxi.asm.symb.Operator.GROUP;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public class ArithmeticParser {
    public static final boolean collapseIfEvaluable = false;

    public static SymbolExpression parse(String s) throws ParseException {
        // Holds Operator and SymbolExpression objects
        final Deque<Object> stack = new ArrayDeque<Object>();
        s += ")";
        stack.add(GROUP);
        loop: while (true) {
            s = s.trim();
            if (s.charAt(0) == ')') {
                collapseStack(stack, GROUP);
                if (s.length() == 1)
                    break;
                s = s.substring(1);
                continue;
            }
            for (final Operator o : Operator.values())
                if (s.startsWith(o.value)) {
                    final boolean isUnary = stack.peek() instanceof Operator;
                    if (isUnary && !o.unary)
                        throw new ParseException("two operators in a row");
                    if (!isUnary && !o.binary)
                        throw new ParseException("unary operator used as binary");
                    s = s.substring(o.value.length());
                    if (!isUnary)
                        collapseStack(stack, o);
                    stack.push(o);
                    continue loop;
                }
            final String leaf = s.substring(0, leafLength(s));
            s = s.substring(leaf.length());
            if (stack.peek() instanceof SymbolExpression)
                throw new ParseException("two operands in a row");
            stack.push(parseLeaf(leaf));
        }
        final SymbolExpression res = (SymbolExpression) stack.poll();
        if (stack.size() != 0)
            throw new ParseException("unmatched left paren");
        return res;
    }

    private static void collapseStack(final Deque<Object> stack, final Operator trigger)
        throws ParseException {
        try {
            while (true) {
                final SymbolExpression head = (SymbolExpression) stack.pop();
                final Operator op = (Operator) stack.pop();
                final boolean isUnary = stack.peek() instanceof Operator && op != GROUP;
                stack.push(op);
                stack.push(head);
                if (isUnary || op.prec > trigger.prec || op.prec == trigger.prec
                    && op.lAssoc) {
                    collapseStack(stack);
                    if (op == GROUP && trigger == GROUP)
                        break;
                } else
                    break;
            }
        } catch (final NoSuchElementException e) {
            throw new ParseException("unmatched right paren");
        } catch (final ClassCastException e) {
            throw new ParseException("two operators in a row");
        }
    }

    private static void collapseStack(final Deque<Object> stack) throws ParseException {
        final SymbolExpression last = (SymbolExpression) stack.pop();
        final Operator op = (Operator) stack.pop();
        if (op == GROUP)
            stack.push(last);
        else {
            final SymbolExpression se;
            if (stack.peek() instanceof Operator)
                se = new OpExp(op, last);
            else
                se = new OpExp(op, (SymbolExpression) stack.pop(), last);
            if (collapseIfEvaluable) {
                final Short v = se.evaluate();
                if (v == null)
                    stack.push(se);
                else
                    stack.push(new NumExp(v));
            } else
                stack.push(se);
        }
    }

    private static int leafLength(final String s) throws ParseException {
        final String token = s.split("[^0-9A-Za-z_]", 2)[0];
        if (token.matches("[rR][0-7]"))
            throw new ParseException("symbols can not be register names");
        else if (token.length() == 0)
            throw new ParseException("symbols must begin with an alphabetic character");
        else if (MemoryUtilities.parseShort(token) == -1
            && Character.isDigit(s.charAt(0)))
            throw new ParseException("symbols must not begin with digits");
        return token.length();
    }

    private static SymbolExpression parseLeaf(final String leaf) {
        final int v = MemoryUtilities.parseShort(leaf);
        if (v != -1)
            return new NumExp((short) v);
        return Symbol.getSymb(leaf);
    }

    public static void main(final String[] args) {
        SymbolExpression se;
        try {
            se = parse("x*(3**5)-z");
            System.out.println(se);
            Symbol.setSymb("x", parse("2+3"));
            Symbol.setSymb("y", parse("5*5"));
            Symbol.setSymb("z", parse("6^2"));
            System.out.println(se);
            System.out.println(se.evaluate());
        } catch (final ParseException e) {
            e.printStackTrace();
        }
    }
}
