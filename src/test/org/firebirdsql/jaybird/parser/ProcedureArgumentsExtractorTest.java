// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.parser.ProcedureArgument.Expression;
import org.firebirdsql.jaybird.parser.ProcedureArgument.PositionalParameter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcedureArgumentsExtractorTest {

    @ParameterizedTest
    @MethodSource
    void procedureArgumentExtraction(String statementFragment, List<ProcedureArgument> expectedArguments)
            throws Exception {
        List<ProcedureArgument> extractedArguments = extractProcedureArguments(statementFragment);

        assertExactEquals(expectedArguments, extractedArguments);
    }

    static Stream<Arguments> procedureArgumentExtraction() {
        return Stream.of(
                // Empty argument list
                testCase("()"),
                testCase("( )"),
                testCase("(/*comment*/)"),
                testCase("/*comment*/()"),
                testCase(" ()"),
                // No argument list; end detected by curly brace
                testCase("}"),
                testCase("/*comment*/}"),
                testCase(" }"),
                testCase("(?)", new PositionalParameter("?")),
                testCase("(?,?)", new PositionalParameter("?"), new PositionalParameter("?")),
                testCase("(?, ?)", new PositionalParameter("?"), new PositionalParameter(" ?")),
                testCase("((?))", new Expression("(?)", 1)),
                testCase("('literal', ?)", new Expression("'literal'", 0), new PositionalParameter(" ?")),
                testCase("(some_function(?, 'literal'), ?, 5 * cast(? as integer))",
                        new Expression("some_function(?, 'literal')", 1), new PositionalParameter(" ?"),
                        new Expression(" 5 * cast(? as integer)", 1)),
                // Extraction ends when argument list is complete; subsequent tokens are not the problem of this visitor
                // Closing curly brace (e.g. JDBC escape end)
                testCase("(some_function(?, 'literal'))}", new Expression("some_function(?, 'literal')", 1)),
                // Closing parenthesis (e.g. this occurs in some parenthesized context)
                testCase("(some_function(?, 'literal')))", new Expression("some_function(?, 'literal')", 1)),
                // Other tokens (here an alias) after argument list
                testCase("(some_function(?, 'literal')) AS X", new Expression("some_function(?, 'literal')", 1))
        );
    }

    private static Arguments testCase(String statementFragment, ProcedureArgument... expectedArguments) {
        return Arguments.of(statementFragment, List.of(expectedArguments));
    }

    @ParameterizedTest
    @MethodSource
    void argumentExtractionFailure(String statementFragment, Class<? extends RuntimeException> expectedExceptionClass) {
        assertThrows(expectedExceptionClass, () -> extractProcedureArguments(statementFragment));
    }

    static Stream<Arguments> argumentExtractionFailure() {
        return Stream.of(
                failureCase("", IllegalStateException.class),
                failureCase("(?, ?", IllegalStateException.class),
                failureCase("(,?)", UnexpectedTokenException.class),
                failureCase("( ,?)", UnexpectedTokenException.class),
                failureCase("(/*comment*/,?)", UnexpectedTokenException.class),
                failureCase("('literal',)", UnexpectedTokenException.class),
                failureCase("('literal',,5)", UnexpectedTokenException.class),
                failureCase("NOT_AN_OPEN_PARENS", UnexpectedTokenException.class),
                failureCase("NOT_AN_OPEN_PARENS()", UnexpectedTokenException.class),
                failureCase("   NOT_AN_OPEN_PARENS()", UnexpectedTokenException.class),
                failureCase("/*comment*/NOT_AN_OPEN_PARENS()", UnexpectedTokenException.class)
        );
    }

    private static Arguments failureCase(String statementFragment,
            Class<? extends RuntimeException> expectedExceptionClass) {
        return Arguments.of(statementFragment, expectedExceptionClass);
    }

    private List<ProcedureArgument> extractProcedureArguments(String statementFragment) throws Exception {
        var future = new CompletableFuture<List<ProcedureArgument>>();
        var extractor = new ProcedureArgumentsExtractor(future::complete, future::completeExceptionally);
        SqlParser parser = SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(extractor)
                .of(statementFragment);
        parser.parse();
        try {
            return future.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof Exception wrappedException) {
                // Unwrap the exception
                throw wrappedException;
            }
            // Should not occur in practice
            throw e;
        }
    }

    private void assertExactEquals(List<ProcedureArgument> expected, List<ProcedureArgument> actual) {
        assertEquals(expected.size(), actual.size(), "size");
        for (int idx = 0; idx < expected.size(); idx++) {
            ProcedureArgument expectedArgument = expected.get(idx);
            ProcedureArgument actualArgument = actual.get(idx);

            int idxVal = idx;
            assertTrue(expectedArgument.exactEquals(actualArgument), () ->
                    "Index %d: expected %s (%s) not exactEquals actual %s (%s)"
                            .formatted(idxVal, expectedArgument, expectedArgument.getClass(),
                                    actualArgument, actualArgument.getClass()));
        }
    }

}