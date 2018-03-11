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
public class TimestampAddFunctionParameterizedTest {

    private static final TimestampAddFunction function = new TimestampAddFunction();

    private final String interval;
    private final String count;
    private final String timestamp;
    private final String expectedResult;

    public TimestampAddFunctionParameterizedTest(String interval, String count, String timestamp,
            String expectedResult) {
        this.interval = interval;
        this.count = count;
        this.timestamp = timestamp;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testConvert() throws Exception {
        assertEquals(expectedResult, function.apply(interval, count, timestamp));
    }

    @Parameterized.Parameters(name = "{index}: timestampadd({0}, {1}, {2}) : {3}")
    public static Collection<Object[]> convertTestCases() {
        return Arrays.asList(
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

    private static Object[] testCase(String interval, String count, String timestamp, String expectedResult) {
        return new Object[] { interval, count, timestamp, expectedResult };
    }
}
