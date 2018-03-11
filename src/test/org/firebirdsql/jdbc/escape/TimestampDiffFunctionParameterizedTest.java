/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link TimestampAddFunction} for the conversion specified in JDBC 4.3 Appendix D.
 * <p>
 * For behaviour with invalid lengths, see {@link TimestampAddFunctionTest}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(Parameterized.class)
public class TimestampDiffFunctionParameterizedTest {

    private static final TimestampDiffFunction function = new TimestampDiffFunction();

    private final String interval;
    private final String timestamp1;
    private final String timestamp2;
    private final String expectedResult;

    public TimestampDiffFunctionParameterizedTest(String interval, String timestamp1, String timestamp2,
            String expectedResult) {
        this.interval = interval;
        this.timestamp1 = timestamp1;
        this.timestamp2 = timestamp2;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testConvert() throws Exception {
        assertEquals(expectedResult, function.apply(interval, timestamp1, timestamp2));
    }

    @Parameterized.Parameters(name = "{index}: timestampadd({0}, {1}, {2}) : {3}")
    public static Collection<Object[]> convertTestCases() {
        return Arrays.asList(
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

    private static Object[] testCase(String interval, String timestamp1, String timestamp2, String expectedResult) {
        return new Object[] { interval, timestamp1, timestamp2, expectedResult };
    }
}
