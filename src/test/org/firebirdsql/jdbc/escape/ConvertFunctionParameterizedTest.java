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
 * Tests for {@link ConvertFunction} for the conversion specified in JDBC 4.3 Appendix D and Jaybird extensions.
 * <p>
 * For behaviour with invalid lengths, see {@link ConvertFunctionTest}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(Parameterized.class)
public class ConvertFunctionParameterizedTest {

    private static final ConvertFunction function = new ConvertFunction();

    private final String value;
    private final String dataType;
    private final String expectedResult;

    public ConvertFunctionParameterizedTest(String value, String dataType, String expectedResult) {
        this.value = value;
        this.dataType = dataType;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testConvert() throws Exception {
        assertEquals(expectedResult, function.apply(value, dataType));
    }

    @Parameterized.Parameters(name = "{index}: convert({0}, {1}) : {2}")
    public static Collection<Object[]> convertTestCases() {
        return Arrays.asList(
//@formatter:off
        // JDBC 4.3 Appendix D cases
        /* 0  */ testCase("val", "SQL_BIGINT", "CAST(val AS BIGINT)"),
        /* 1  */ testCase("val", "sql_bigint", "CAST(val AS BIGINT)"),
        /* 2  */ testCase("val", "BIGINT", "CAST(val AS BIGINT)"),
        /* 3  */ testCase("val", "bigint", "CAST(val AS BIGINT)"),
        // Case sensitivity considered tested sufficiently, further tests will only check uppercase
        /* 4  */ testCase("val", "SQL_BINARY", "CAST(val AS CHAR(50) CHARACTER SET OCTETS)"),
        /* 5  */ testCase("val", "BINARY", "CAST(val AS CHAR(50) CHARACTER SET OCTETS)"),
        // NOTE: BIT not supported by Firebird
        /* 6  */ testCase("val", "SQL_BIT", "CAST(val AS BIT)"),
        /* 7  */ testCase("val", "BIT", "CAST(val AS BIT)"),
        /* 8  */ testCase("val", "SQL_BLOB", "CAST(val AS BLOB SUB_TYPE BINARY)"),
        /* 9  */ testCase("val", "BLOB", "CAST(val AS BLOB SUB_TYPE BINARY)"),
        /* 10 */ testCase("val", "SQL_BOOLEAN", "CAST(val AS BOOLEAN)"),
        /* 11 */ testCase("val", "BOOLEAN", "CAST(val AS BOOLEAN)"),
        /* 12 */ testCase("val", "SQL_CHAR", "CAST(val AS CHAR(50))"),
        /* 13 */ testCase("val", "CHAR", "CAST(val AS CHAR(50))"),
        /* 14 */ testCase("val", "SQL_CLOB", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 15 */ testCase("val", "CLOB", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 16 */ testCase("val", "SQL_DATE", "CAST(val AS DATE)"),
        /* 17 */ testCase("val", "DATE", "CAST(val AS DATE)"),
        /* 18 */ testCase("val", "SQL_DECIMAL", "CAST(val AS DECIMAL)"),
        /* 19 */ testCase("val", "DECIMAL", "CAST(val AS DECIMAL)"),
        // NOTE: DATALINK not supported by Firebird
        /* 20 */ testCase("val", "SQL_DATALINK", "CAST(val AS DATALINK)"),
        /* 21 */ testCase("val", "DATALINK", "CAST(val AS DATALINK)"),
        /* 22 */ testCase("val", "SQL_DOUBLE", "CAST(val AS DOUBLE PRECISION)"),
        /* 23 */ testCase("val", "DOUBLE", "CAST(val AS DOUBLE PRECISION)"),
        /* 24 */ testCase("val", "SQL_FLOAT", "CAST(val AS FLOAT)"),
        /* 25 */ testCase("val", "FLOAT", "CAST(val AS FLOAT)"),
        /* 26 */ testCase("val", "SQL_INTEGER", "CAST(val AS INTEGER)"),
        /* 27 */ testCase("val", "INTEGER", "CAST(val AS INTEGER)"),
        /* 28 */ testCase("val", "SQL_LONGVARBINARY", "CAST(val AS BLOB SUB_TYPE BINARY)"),
        /* 29 */ testCase("val", "LONGVARBINARY", "CAST(val AS BLOB SUB_TYPE BINARY)"),
        /* 30 */ testCase("val", "SQL_LONGNVARCHAR", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 31 */ testCase("val", "LONGNVARCHAR", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 32 */ testCase("val", "SQL_LONGVARCHAR", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 33 */ testCase("val", "LONGVARCHAR", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 34 */ testCase("val", "SQL_NCHAR", "CAST(val AS NCHAR(50))"),
        /* 35 */ testCase("val", "NCHAR", "CAST(val AS NCHAR(50))"),
        /* 36 */ testCase("val", "SQL_NCLOB", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 37 */ testCase("val", "NCLOB", "CAST(val AS BLOB SUB_TYPE TEXT)"),
        /* 38 */ testCase("val", "SQL_NUMERIC", "CAST(val AS NUMERIC)"),
        /* 39 */ testCase("val", "NUMERIC", "CAST(val AS NUMERIC)"),
        // See also cases 76, 77
        /* 40 */ testCase("val", "SQL_NVARCHAR", "TRIM(TRAILING FROM val)"),
        /* 41 */ testCase("val", "NVARCHAR", "TRIM(TRAILING FROM val)"),
        /* 42 */ testCase("val", "SQL_REAL", "CAST(val AS REAL)"),
        /* 43 */ testCase("val", "REAL", "CAST(val AS REAL)"),
        // TODO: Change to CHAR(8) CHARACTER SET OCTETS?
        /* 44 */ testCase("val", "SQL_ROWID", "CAST(val AS ROWID)"),
        /* 45 */ testCase("val", "ROWID", "CAST(val AS ROWID)"),
        // NOTE: SQLXML not supported by Firebird
        /* 46 */ testCase("val", "SQL_SQLXML", "CAST(val AS SQLXML)"),
        /* 47 */ testCase("val", "SQLXML", "CAST(val AS SQLXML)"),
        /* 48 */ testCase("val", "SQL_SMALLINT", "CAST(val AS SMALLINT)"),
        /* 49 */ testCase("val", "SMALLINT", "CAST(val AS SMALLINT)"),
        /* 50 */ testCase("val", "SQL_TIME", "CAST(val AS TIME)"),
        /* 51 */ testCase("val", "TIME", "CAST(val AS TIME)"),
        /* 52 */ testCase("val", "SQL_TIMESTAMP", "CAST(val AS TIMESTAMP)"),
        /* 53 */ testCase("val", "TIMESTAMP", "CAST(val AS TIMESTAMP)"),
        /* 54 */ testCase("val", "SQL_TINYINT", "CAST(val AS SMALLINT)"),
        /* 55 */ testCase("val", "TINYINT", "CAST(val AS SMALLINT)"),
        /* 56 */ testCase("val", "SQL_VARBINARY", "CAST(val AS VARCHAR(50) CHARACTER SET OCTETS)"),
        /* 57 */ testCase("val", "VARBINARY", "CAST(val AS VARCHAR(50) CHARACTER SET OCTETS)"),
        // See also cases 74, 75
        /* 58 */ testCase("val", "SQL_VARCHAR", "TRIM(TRAILING FROM val)"),
        /* 59 */ testCase("val", "VARCHAR", "TRIM(TRAILING FROM val)"),
        // Jaybird-specific extensions; not checking SQL_-prefix variant
        /* 60 */ testCase("val", "BINARY(10)", "CAST(val AS CHAR(10) CHARACTER SET OCTETS)"),
        // Pass-through datatype that doesn't match pattern
        /* 61 */ testCase("val", "BLOB SUB_TYPE -1", "CAST(val AS BLOB SUB_TYPE -1)"),
        /* 62 */ testCase("val", "CHAR(9)", "CAST(val AS CHAR(9))"),
        // Pass-through datatype that doesn't match pattern
        /* 63 */ testCase("val", "CHAR(9) CHARACTER SET UTF8", "CAST(val AS CHAR(9) CHARACTER SET UTF8)"),
        /* 64 */ testCase("val", "DECIMAL(12,2)", "CAST(val AS DECIMAL(12,2))"),
        /* 65 */ testCase("val", "FLOAT(11)", "CAST(val AS FLOAT(11))"),
        /* 66 */ testCase("val", "NCHAR(15)", "CAST(val AS NCHAR(15))"),
        /* 67 */ testCase("val", "NUMERIC(8, 2)", "CAST(val AS NUMERIC(8, 2))"),
        /* 68 */ testCase("val", "NVARCHAR(33)", "CAST(val AS NVARCHAR(33))"),
        /* 69 */ testCase("val", "VARBINARY(99)", "CAST(val AS VARCHAR(99) CHARACTER SET OCTETS)"),
        // Pass-through datatype that doesn't match pattern
        /* 70 */ testCase("val", "VARBINARY(99) invalid", "CAST(val AS VARBINARY(99) invalid)"),
        /* 71 */ testCase("val", "VARCHAR(100)", "CAST(val AS VARCHAR(100))"),
        // Pass-through datatype that doesn't match pattern
        /* 72 */ testCase("val", "VARCHAR(75) CHARACTER SET UTF8", "CAST(val AS VARCHAR(75) CHARACTER SET UTF8)"),
        // Some other exceptional behaviour
        // Pass-through datatype that doesn't match pattern
        /* 73 */ testCase("val", "DOUBLE PRECISION", "CAST(val AS DOUBLE PRECISION)"),
        /* 74 */ testCase("?", "VARCHAR", "CAST(? AS VARCHAR(50))"),
        /* 75 */ testCase("?", "VARCHAR(75)", "CAST(? AS VARCHAR(75))"),
        /* 76 */ testCase("?", "NVARCHAR", "CAST(? AS NVARCHAR(50))"),
        /* 77 */ testCase("?", "NVARCHAR(75)", "CAST(? AS NVARCHAR(75))"),
        // Time zone support (not defined in JDBC nor in ODBC)
        /* 78 */ testCase("val", "SQL_TIME_WITH_TIMEZONE", "CAST(val AS TIME WITH TIME ZONE)"),
        /* 79 */ testCase("val", "TIME_WITH_TIMEZONE", "CAST(val AS TIME WITH TIME ZONE)"),
        /* 80 */ testCase("val", "SQL_TIME_WITH_TIME_ZONE", "CAST(val AS TIME WITH TIME ZONE)"),
        /* 81 */ testCase("val", "TIME_WITH_TIME_ZONE", "CAST(val AS TIME WITH TIME ZONE)"),
        /* 82 */ testCase("val", "SQL_TIMESTAMP_WITH_TIMEZONE", "CAST(val AS TIMESTAMP WITH TIME ZONE)"),
        /* 83 */ testCase("val", "TIMESTAMP_WITH_TIMEZONE", "CAST(val AS TIMESTAMP WITH TIME ZONE)"),
        /* 84 */ testCase("val", "SQL_TIMESTAMP_WITH_TIME_ZONE", "CAST(val AS TIMESTAMP WITH TIME ZONE)"),
        /* 95 */ testCase("val", "TIMESTAMP_WITH_TIME_ZONE", "CAST(val AS TIMESTAMP WITH TIME ZONE)")
//@formatter:on
        );
    }

    private static Object[] testCase(String value, String dataType, String expectedResult) {
        return new Object[] { value, dataType, expectedResult };
    }
}
