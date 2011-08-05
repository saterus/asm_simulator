package edu.osu.cse.mmxi.asm.symb;

import static edu.osu.cse.mmxi.asm.symb.Operator.GROUP;
import static edu.osu.cse.mmxi.asm.symb.Operator.HASH;
import static edu.osu.cse.mmxi.asm.symb.Operator.LIT;
import static edu.osu.cse.mmxi.asm.symb.Operator.MINUS;
import static edu.osu.cse.mmxi.asm.symb.Operator.NOT;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.osu.cse.mmxi.asm.Literal;
import edu.osu.cse.mmxi.asm.Symbol;
import edu.osu.cse.mmxi.asm.error.ParseException;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.NumExp;
import edu.osu.cse.mmxi.asm.symb.SymbolExpression.OpExp;
import edu.osu.cse.mmxi.common.Utilities;

public class ArithmeticParser {
    public static final boolean collapseIfEvaluable = true;

    public static SymbolExpression parse(final String s) throws ParseException {
        return parseF(s, new Object[0]);
    }

    public static SymbolExpression parseF(String s, final Object... args)
        throws ParseException {
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
            final String leaf = s.substring(0, leafLength(s, args.length));
            s = s.substring(leaf.length());
            if (stack.peek() instanceof SymbolExpression)
                throw new ParseException("two operands in a row");
            stack.push(parseLeaf(leaf, args));
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
            if (op == HASH)
                se = last;
            else if (op == LIT) {
                final Short v = last.evaluate();
                if (v == null)
                    throw new ParseException(
                        "literals expressions must evaluate on the spot");
                se = Literal.getLiteral(v);
            } else if (stack.peek() instanceof Operator)
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

    private static int leafLength(final String s, final int argc) throws ParseException {
        Matcher m = Pattern.compile("^'.*?(?<!\\\\)'").matcher(s);
        if (m.find())
            return m.end();
        m = Pattern.compile("^:?[0-9A-Za-z_]+").matcher(s);
        if (!m.find())
            return 0;
        final String token = m.group();
        if (token.matches(":\\d+") && Integer.parseInt(token.substring(1)) < argc)
            return token.length();
        else if (token.matches("[rR][0-7]"))
            throw new ParseException("symbols can not be register names");
        else if (token.length() == 0 || token.charAt(0) == ':')
            throw new ParseException("symbols must begin with an alphabetic character");
        else if (Utilities.parseShort(token) == null && Character.isDigit(s.charAt(0)))
            throw new ParseException("symbols must not begin with digits");
        return token.length();
    }

    private static SymbolExpression parseLeaf(final String leaf, final Object[] args)
        throws ParseException {
        if (leaf.matches("'.*'")) {
            final String c = Utilities.parseString(leaf.substring(1, leaf.length() - 1));
            if (c.length() != 1)
                throw new ParseException("character literal " + leaf
                    + " must contain exactly one character");
            return new NumExp((short) c.charAt(0));
        } else if (leaf.matches(":\\d+")) {
            final Object o = args[Integer.parseInt(leaf.substring(1))];
            if (o instanceof Integer)
                return new NumExp((short) (int) (Integer) o);
            else if (o instanceof Short)
                return new NumExp((Short) o);
            else if (o instanceof String)
                return Symbol.getSymb((String) o);
            else
                return (SymbolExpression) o; // throw ClassCastException for other objects

        }
        final Short v = Utilities.parseShort(leaf);
        if (v != null)
            return new NumExp(v);
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
        final SortedMap<SymbolExpression, Integer> terms = getTerms(se, eval);
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

    public static SortedMap<SymbolExpression, Integer> getTerms(SymbolExpression se,
        final boolean eval) {
        final SortedMap<SymbolExpression, Integer> terms = new TreeMap<SymbolExpression, Integer>(
            new Comparator<SymbolExpression>() {
                @Override
                public int compare(final SymbolExpression se1, final SymbolExpression se2) {
                    return se1.toString().compareTo(se2.toString());
                }
            });
        if (eval)
            se = expand(se, null);
        addTerms(se, terms, 1, eval);
        return terms;
    }

    private static void addTerms(final SymbolExpression se,
        final Map<SymbolExpression, Integer> terms, final int mult, final boolean eval) {
        if (se instanceof OpExp && (((OpExp) se).op == PLUS || ((OpExp) se).op == MINUS)) {
            final SymbolExpression[] ops = ((OpExp) se).operands;
            if (ops.length > 1)
                addTerms(ops[0], terms, mult, eval);
            addTerms(ops[ops.length - 1], terms, mult
                * (((OpExp) se).op == MINUS ? -1 : 1), eval);
            return;
        } else if (se instanceof OpExp && ((OpExp) se).op == NOT) {
            addTerms(((OpExp) se).operands[0], terms, -mult, eval);
            addTerms(new NumExp((short) 1), terms, -mult, eval);
            return;
        } else if (se instanceof OpExp && ((OpExp) se).op == TIMES) {
            final Short v0 = ((OpExp) se).operands[0]
                .evaluate(eval ? new HashSet<Symbol>() : null);
            final Short v1 = ((OpExp) se).operands[1]
                .evaluate(eval ? new HashSet<Symbol>() : null);
            if (v0 != null || v1 != null) {
                if (v0 != null)
                    addTerms(((OpExp) se).operands[1], terms, mult * v0, eval);
                else
                    addTerms(((OpExp) se).operands[0], terms, mult * v1, eval);
                return;
            }
        }
        terms.put(se, (terms.containsKey(se) ? terms.get(se) : 0) + mult);
    }

    public static SymbolExpression expand(final SymbolExpression node, final Symbol parent) {
        if (node == null)
            return null;
        if (node instanceof OpExp)
            for (int i = 0; i < ((OpExp) node).operands.length; i++)
                ((OpExp) node).operands[i] = expand(((OpExp) node).operands[i], parent);
        else {
            if (node instanceof Literal)
                ((Literal) node).fill();
            if (node instanceof Symbol && node != parent && ((Symbol) node).value != null)
                return expand(((Symbol) node).value, parent);
        }
        return node;
    }

}
