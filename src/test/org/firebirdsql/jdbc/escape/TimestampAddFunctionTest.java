/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.firebirdsql.jdbc.escape.EscapeFunctionAsserts.assertParseException;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link TimestampAddFunction} for the conversion specified in JDBC 4.3 Appendix D.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class TimestampAddFunctionTest {

    private static final TimestampAddFunction function = new TimestampAddFunction();

    @Test
    void testZeroParameters_throwsException() {
        assertParseException(function::apply, "Expected 3 parameters for TIMESTAMPADD, received 0");
    }

    @Test
    void testOneParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE"),
                "Expected 3 parameters for TIMESTAMPADD, received 1");
    }

    @Test
    void testTwoParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE", "5"),
                "Expected 3 parameters for TIMESTAMPADD, received 2");
    }

    @Test
    void testFourParameter_throwsException() {
        assertParseException(() -> function.apply("SQL_TSI_MINUTE", "5", "CURRENT_TIMESTAMP", "extra"),
                "Expected 3 parameters for TIMESTAMPADD, received 4");
    }

    @ParameterizedTest(name = "{index}: timestampadd({0}, {1}, {2}) : {3}")
    @MethodSource("timestampaddTestCases")
    void testTimestampadd(String interval, String count, String timestamp, String expectedResult)
            throws Exception {
        assertEquals(expectedResult, function.apply(interval, count, timestamp));
    }

    static Stream<Arguments> timestampaddTestCases() {
        return Stream.of(
//@formatter:off
        // JDBC 4.3 Appendix D cases
        /* 0 */ testCase("SQL_TSI_FRAC_SECOND", "5000000", "STAMP", "DATEADD(MILLISECOND,1.0e-6*(5000000),STAMP)"),
        /* 1 */ testCase("SQL_TSI_SECOND", "5", "STAMP", "DATEADD(SECOND,5,STAMP)"),
        /* 2 */ testCase("SQL_TSI_MINUTE", "15", "CURRENT_TIMESTAMP", "DATEADD(MINUTE,15,CURRENT_TIMESTAMP)"),
        /* 3 */ testCase("SQL_TSI_HOUR", "1", "STAMP", "DATEADD(HOUR,1,STAMP)"),
        /* 4 */ testCase("SQL_TSI_DAY", "5", "STAMP", "DATEADD(DAY,5,STAMP)"),
        /* 5 */ testCase("SQL_TSI_WEEK", "2", "STAMP", "DATEADD(WEEK,2,STAMP)"),
        /* 6 */ testCase("SQL_TSI_MONTH", "4", "STAMP", "DATEADD(MONTH,4,STAMP)"),
        /* 7 */ testCase("SQL_TSI_QUARTER", "1", "STAMP", "DATEADD(MONTH,3*(1),STAMP)"),
        /* 8 */ testCase("SQL_TSI_YEAR", "9", "STAMP", "DATEADD(YEAR,9,STAMP)"),
        // Unsupported / unknown values passed through as-is
        /* 9 */ testCase("INCORRECT_VAL", "5", "STAMP", "DATEADD(INCORRECT_VAL,5,STAMP)")
//@formatter:on
        );
    }

    private static Arguments testCase(String interval, String count, String timestamp, String expectedResult) {
        return Arguments.of(interval, count, timestamp, expectedResult);
    }

}
