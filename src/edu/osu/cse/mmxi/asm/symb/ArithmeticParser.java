package edu.osu.cse.mmxi.asm.symb;

import static edu.osu.cse.mmxi.asm.symb.Operator.GROUP;
import static edu.osu.cse.mmxi.asm.symb.Operator.MINUS;
import static edu.osu.cse.mmxi.asm.symb.Operator.PLUS;
import static edu.osu.cse.mmxi.asm.symb.Operator.TIMES;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.MemoryUtilities;
import edu.osu.cse.mmxi.common.ParseException;

public class ArithmeticParser {
    public static final boolean collapseIfEvaluable = true;

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
        final Symbol s = Symbol.getSymb(leaf);
        if (collapseIfEvaluable && s.value != null)
            return s.value;
        else
            return s;
    }

    public static SymbolExpression simplify(final SymbolExpression se) {
        return simplify(se, true);
    }

    public static SymbolExpression simplify(final SymbolExpression se, final boolean eval) {
        if (se == null)
            return null;
        final SortedMap<SymbolExpression, Integer> terms = new TreeMap<SymbolExpression, Integer>(
            new Comparator<SymbolExpression>() {
                @Override
                public int compare(final SymbolExpression se1, final SymbolExpression se2) {
                    return se1.toString().compareTo(se2.toString());
                }
            });

        addTerms(se, terms, 1, eval);
        short sum = 0;
        for (final Iterator<Entry<SymbolExpression, Integer>> i = terms.entrySet()
            .iterator(); i.hasNext();) {
            final Entry<SymbolExpression, Integer> e = i.next();
            final Short v = e.getKey().evaluate(eval ? new HashSet<Symbol>() : null);
            if (v != null) {
                sum += e.getValue() * v;
                i.remove();
            } else if (e.getValue() == 0)
                i.remove();
        }
        SymbolExpression ret = null;
        for (final Entry<SymbolExpression, Integer> e : terms.entrySet()) {
            final boolean wasNeg = e.getValue() < 0;
            final int mult = wasNeg ? -e.getValue() : e.getValue();
            SymbolExpression node = e.getKey();
            if (mult != 1)
                node = new OpExp(TIMES, new NumExp((short) mult), node);
            ret = ret == null ? wasNeg ? new OpExp(MINUS, node) : node : new OpExp(
                wasNeg ? MINUS : PLUS, ret, node);
        }
        if (ret == null || sum != 0) {
            final boolean wasNeg = sum < 0;
            if (wasNeg)
                sum = (short) -sum;
            final SymbolExpression node = new NumExp(sum);
            ret = ret == null ? wasNeg ? new OpExp(MINUS, node) : node : new OpExp(
                wasNeg ? MINUS : PLUS, ret, node);
        }
        return ret;
    }

    private static void addTerms(final SymbolExpression se,
        final Map<SymbolExpression, Integer> terms, final int mult, final boolean eval) {
        if (se instanceof OpExp && (((OpExp) se).op == PLUS || ((OpExp) se).op == MINUS)) {
            addTerms(((OpExp) se).operands[0], terms, mult, eval);
            addTerms(((OpExp) se).operands[1], terms, mult
                * (((OpExp) se).op == MINUS ? -1 : 1), eval);
        } else if (se instanceof OpExp && ((OpExp) se).op == TIMES) {
            final Short v0 = ((OpExp) se).operands[0]
                .evaluate(eval ? new HashSet<Symbol>() : null);
            final Short v1 = ((OpExp) se).operands[1]
                .evaluate(eval ? new HashSet<Symbol>() : null);
            if (v0 != null)
                addTerms(((OpExp) se).operands[1], terms, mult * v0, eval);
            else if (v1 != null)
                addTerms(((OpExp) se).operands[0], terms, mult * v1, eval);
            else
                terms.put(se, (terms.containsKey(se) ? terms.get(se) : 0) + mult);
        } else
            terms.put(se, (terms.containsKey(se) ? terms.get(se) : 0) + mult);
    }
}
