// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.parser.ProcedureArgument.Expression;
import org.firebirdsql.jaybird.parser.ProcedureArgument.PositionalParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcedureArgumentTest {

    @ParameterizedTest
    @MethodSource
    void testOf(List<Token> tokens, ProcedureArgument expectedArgument) {
        var argument = ProcedureArgument.of(tokens);

        assertExactEquals(expectedArgument, argument);
        assertExactEquals(argument, expectedArgument);
        // Verify that equals is at least true if exactEquals is true
        assertEquals(expectedArgument, argument, "equals");
        assertEquals(argument, expectedArgument, "equals (reversed)");
        // Data-wise this is already covered by the previous assert, but this asserts the accessor methods
        assertEquals(expectedArgument.parameterCount(), argument.parameterCount(), "parameterCount");
        assertEquals(expectedArgument.text(), argument.text(), "text");
    }

    static Stream<Arguments> testOf() {
        return Stream.of(
                // Bare parameter
                testOfTestCase(new PositionalParameter("?"), new PositionalParameterToken(0)),
                // Bare parameter surrounded by comment and whitespace
                testOfTestCase(new PositionalParameter("/*comment*/? "),
                        new CommentToken(0, "/*comment*/"), new PositionalParameterToken(0),
                        new WhitespaceToken(0, " ")),
                // Parenthesized parameter -> expression
                testOfTestCase(new Expression("(?)", 1),
                        new ParenthesisOpen(0), new PositionalParameterToken(0), new ParenthesisClose(0)),
                testOfTestCase(new Expression(" 'literal'", 0),
                        new WhitespaceToken(0, " "), new StringLiteralToken(0, "'literal'")),
                testOfTestCase(new Expression("some_function(?, ?)", 2),
                        new GenericToken(0, "some_function"), new ParenthesisOpen(0), new PositionalParameterToken(0),
                        new CommaToken(0), new WhitespaceToken(0, " "), new PositionalParameterToken(0),
                        new ParenthesisClose(0))
        );
    }

    private static Arguments testOfTestCase(ProcedureArgument expectedArgument, Token... tokens) {
        return Arguments.of(List.of(tokens), expectedArgument);
    }

    @ParameterizedTest
    @MethodSource
    void testOfOnlyWhiteSpaceOrComments_throwsException(List<Token> tokens) {
        assertThrows(IllegalArgumentException.class, () -> ProcedureArgument.of(tokens));
    }

    static Stream<List<Token>> testOfOnlyWhiteSpaceOrComments_throwsException() {
        return Stream.of(
                List.of(),
                List.of(new WhitespaceToken(0, " ")),
                List.of(new CommentToken(0, "/*comment*/")),
                List.of(new WhitespaceToken(0, " "), new CommentToken(0, "--comment"), new WhitespaceToken(0, "\n")),
                List.of(new CommentToken(0, "/*comment*/"), new WhitespaceToken(0, "  "))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "(?)", "'literal'", "some_function(?, ?)" })
    void expressionToString_producesArgumentText(String argumentText) {
        var argument = new Expression(argumentText, -1);

        assertEquals(argumentText, argument.text(), "text");
        assertEquals(argumentText, argument.toString(), "toString");
    }

    @ParameterizedTest
    @ValueSource(strings = { "?", " ? ", "/*comment*/?" })
    void positionalParameterToString_producesArgumentText(String argumentText) {
        var argument = new PositionalParameter(argumentText);

        assertEquals(argumentText, argument.text(), "text");
        assertEquals(argumentText, argument.toString(), "toString");
    }

    @Test
    void positionalParameterCount_one() {
        var argument = new PositionalParameter("?");

        assertEquals(1, argument.parameterCount());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 1, 5 })
    void expressionParameterCount(int count) {
        var argument = new Expression("irrelevant", count);

        assertEquals(count, argument.parameterCount());
    }

    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    @Test
    void equalsConsidersSubclassesWithSameTextEqual() {
        // NOTE: The of-factory cannot produce this; using -1 to demonstrate parameter count is not part of equals
        var expression = new Expression("?", -1);
        var parameter = new PositionalParameter("?");

        assertEquals(expression, parameter, "expresssion.equals(parameter)");
        assertEquals(parameter, expression, "parameter.equals(expression)");
        assertEquals(expression.hashCode(), parameter.hashCode(), "hashCode");
        assertFalse(expression.exactEquals(parameter), "not exactEquals");
        assertFalse(parameter.exactEquals(expression), "not exactEquals (reversed)");
    }

    private static void assertExactEquals(ProcedureArgument expected, ProcedureArgument actual) {
        assertTrue(expected.exactEquals(actual), () -> "Expected %s (%s), but was %s (%s)"
                .formatted(expected, expected.getClass(), actual, actual.getClass()));
    }

}