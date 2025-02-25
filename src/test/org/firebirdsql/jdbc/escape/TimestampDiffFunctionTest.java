// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link TimestampDiffFunction} for the conversion specified in JDBC 4.3 Appendix D.
 *
 * @author Mark Rotteveel
 */
class TimestampDiffFunctionTest {

    private static final TimestampDiffFunction function = new TimestampDiffFunction();

    @Test
    void testZeroParameters_throwsException() {
        assertParseException(function::apply, "Expected 3 parameters for TIMESTAMPDIFF, received 0");
    }

    @Test
    void testOneParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE"),
                "Expected 3 parameters for TIMESTAMPDIFF, received 1");
    }

    @Test
    void testTwoParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE", "STAMP"),
                "Expected 3 parameters for TIMESTAMPDIFF, received 2");
    }

    @Test
    void testFourParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE", "STAMP", "CURRENT_TIMESTAMP", "extra"),
                "Expected 3 parameters for TIMESTAMPDIFF, received 4");
    }

    @ParameterizedTest(name = "{index}: timestampdiff({0}, {1}, {2}) : {3}")
    @MethodSource("timestampdiffTestCases")
    void testTimestampdiff(String interval, String timestamp1, String timestamp2, String expectedResult)
            throws Exception {
        assertEquals(expectedResult, function.apply(interval, timestamp1, timestamp2));
    }

    static Stream<Arguments> timestampdiffTestCases() {
        return Stream.of(
//@formatter:off
        // JDBC 4.3 Appendix D cases
        /* 0 */ testCase("SQL_TSI_FRAC_SECOND", "STAMP1", "STAMP2", "CAST(DATEDIFF(MILLISECOND,STAMP1,STAMP2)*1.0e6 AS BIGINT)"),
        /* 1 */ testCase("SQL_TSI_SECOND", "STAMP1", "STAMP2", "DATEDIFF(SECOND,STAMP1,STAMP2)"),
        /* 2 */ testCase("SQL_TSI_MINUTE", "STAMP", "CURRENT_TIMESTAMP", "DATEDIFF(MINUTE,STAMP,CURRENT_TIMESTAMP)"),
        /* 3 */ testCase("SQL_TSI_HOUR", "STAMP1", "STAMP2", "DATEDIFF(HOUR,STAMP1,STAMP2)"),
        /* 4 */ testCase("SQL_TSI_DAY", "STAMP1", "STAMP2", "DATEDIFF(DAY,STAMP1,STAMP2)"),
        /* 5 */ testCase("SQL_TSI_WEEK", "STAMP1", "STAMP2", "DATEDIFF(WEEK,STAMP1,STAMP2)"),
        /* 6 */ testCase("SQL_TSI_MONTH", "STAMP1", "STAMP2", "DATEDIFF(MONTH,STAMP1,STAMP2)"),
        /* 7 */ testCase("SQL_TSI_QUARTER", "STAMP1", "STAMP2", "(DATEDIFF(MONTH,STAMP1,STAMP2)/3)"),
        /* 8 */ testCase("SQL_TSI_YEAR", "STAMP1", "STAMP2", "DATEDIFF(YEAR,STAMP1,STAMP2)"),
        // Unsupported / unknown values passed through as-is
        /* 9 */ testCase("INCORRECT_VAL", "STAMP1", "STAMP2", "DATEDIFF(INCORRECT_VAL,STAMP1,STAMP2)")
//@formatter:on
        );
    }

    private static Arguments testCase(String interval, String timestamp1, String timestamp2, String expectedResult) {
        return Arguments.of(interval, timestamp1, timestamp2, expectedResult);
    }

}
