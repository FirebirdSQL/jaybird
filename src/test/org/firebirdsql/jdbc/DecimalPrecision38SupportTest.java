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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for Firebird 4 extended precision of 38 for decimal (and numeric).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DecimalPrecision38SupportTest extends FBJUnit4TestBase {

    private static final String CREATE_TABLE =
            "create table extendeddecimal ("
                    + "id integer primary key,"
                    + "decimal19_2 decimal(19, 2),"
                    + "numeric19_2 numeric(19, 2),"
                    + "decimal38_4 numeric(38, 4),"
                    + "numeric38_4 numeric(38, 4)"
                    + ")";

    @BeforeClass
    public static void checkDecimalPrecisionOf38() {
        assumeTrue("Requires DECIMAL/NUMERIC precision 38 support",
                getDefaultSupportInfo().supportsDecimalPrecision(38));
    }

    @Before
    public void setUp() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            DdlHelper.executeCreateTable(connection, CREATE_TABLE);
        }
    }

    @Test
    public void simpleValuesSelect() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute("insert into extendeddecimal (id, decimal19_2, numeric19_2) "
                    + "values (1, 12345678901234567.89, 12345678901234567.89)");
            stmt.execute("insert into extendeddecimal (id, decimal19_2, numeric19_2) "
                    + "values (2, -12345678901234567.89, -12345678901234567.89)");

            try (ResultSet rs = stmt.executeQuery(
                    "select id, "
                            + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                            + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                            + "from extendeddecimal order by id")) {
                String row1ExpectedString = "12345678901234567.89";
                BigDecimal row1Expected = new BigDecimal(row1ExpectedString);
                assertRow(rs, 1, row1Expected, row1ExpectedString, row1Expected, row1ExpectedString);

                String row2ExpectedString = "-12345678901234567.89";
                BigDecimal row2Expected = new BigDecimal(row2ExpectedString);
                assertRow(rs, 2, row2Expected, row2ExpectedString, row2Expected, row2ExpectedString);
            }
        }
    }

    @Test
    public void simpleInsert() throws Exception {
        final String stringValue = "12345678901234567.89";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        final String stringValue2 = "-12345678901234567.89";
        final BigDecimal bdValue2 = new BigDecimal(stringValue2);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into extendeddecimal(id, decimal19_2, numeric19_2) values (?, ?, ?)")) {
                execute(pstmt, 1, bdValue, bdValue);
                execute(pstmt, 2, bdValue2, bdValue2);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                                 + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                                 + "from extendeddecimal order by id")) {
                assertRow(rs, 1, bdValue, stringValue, bdValue, stringValue);
                assertRow(rs, 2, bdValue2, stringValue2, bdValue2, stringValue2);
            }
        }
    }

    /**
     * Both DECIMAL and NUMERIC with precision >= 19 will actually have precision 38.
     */
    @Test
    public void insert_actualPrecisionIs38() throws Exception {
        final String stringValue = "123456789012345678901234567890123456.78";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into extendeddecimal(id, decimal19_2, numeric19_2) values (?, ?, ?)")) {
                execute(pstmt, 1, bdValue, bdValue);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "decimal19_2, cast(decimal19_2 as varchar(50)) str_decimal19_2, "
                                 + "numeric19_2, cast(numeric19_2 as varchar(50)) str_numeric19_2 "
                                 + "from extendeddecimal order by id")) {
                assertRow(rs, 1, bdValue, stringValue, bdValue, stringValue);
            }
        }
    }

    @Test
    public void select_decimal19_2_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decimal19_2 from extendeddecimal")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("typeName", "DECIMAL", rsmd.getColumnTypeName(1));
            assertEquals("type", Types.DECIMAL, rsmd.getColumnType(1));
            assertEquals("className", "java.math.BigDecimal", rsmd.getColumnClassName(1));
            assertEquals("precision", 19, rsmd.getPrecision(1));
            assertEquals("scale", 2, rsmd.getScale(1));
            assertEquals("displaySize", 21, rsmd.getColumnDisplaySize(1));
            assertTrue("searchable", rsmd.isSearchable(1));
            assertTrue("signed", rsmd.isSigned(1));
        }
    }

    @Test
    public void select_numeric19_2_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select numeric19_2 from extendeddecimal")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("typeName", "NUMERIC", rsmd.getColumnTypeName(1));
            assertEquals("type", Types.NUMERIC, rsmd.getColumnType(1));
            assertEquals("className", "java.math.BigDecimal", rsmd.getColumnClassName(1));
            assertEquals("precision", 19, rsmd.getPrecision(1));
            assertEquals("scale", 2, rsmd.getScale(1));
            assertEquals("displaySize", 21, rsmd.getColumnDisplaySize(1));
            assertTrue("searchable", rsmd.isSearchable(1));
            assertTrue("signed", rsmd.isSigned(1));
        }
    }

    @Test
    public void insertAndSelectMinAndMaxValues() throws Exception {
        final String stringValue = "9999999999999999999999999999999999.9999";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        final String stringValue2 = "-9999999999999999999999999999999999.9999";
        final BigDecimal bdValue2 = new BigDecimal(stringValue2);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into extendeddecimal(id, decimal38_4, numeric38_4) values (?, ?, ?)")) {
                execute(pstmt, 1, bdValue, bdValue);
                execute(pstmt, 2, bdValue2, bdValue2);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "decimal38_4, cast(decimal38_4 as varchar(50)) str_decimal38_4, "
                                 + "numeric38_4, cast(numeric38_4 as varchar(50)) str_numeric38_4 "
                                 + "from extendeddecimal order by id")) {
                assertRow38_4(rs, 1, bdValue, stringValue, bdValue, stringValue);
                assertRow38_4(rs, 2, bdValue2, stringValue2, bdValue2, stringValue2);
            }
        }
    }

    private static void execute(PreparedStatement insert, int index, BigDecimal decimal2, BigDecimal numeric3)
            throws Exception {
        insert.setInt(1, index);
        insert.setBigDecimal(2, decimal2);
        insert.setBigDecimal(3, numeric3);
        insert.execute();
    }

    private static void assertRow(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecimal19_2, String expectedDecimal19_2String,
            BigDecimal expectedNumeric19_2, String expectedNumeric19_2String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(prefix + " expected row", resultSet.next());
        assertEquals(prefix, expectedId, resultSet.getInt("id"));
        assertEquals(prefix + " decimal19_2", expectedDecimal19_2, resultSet.getBigDecimal("decimal19_2"));
        assertEquals(prefix + " numeric19_2", expectedNumeric19_2, resultSet.getBigDecimal("numeric19_2"));
        assertEquals(prefix + " str_decimal19_2", expectedDecimal19_2String, resultSet.getString("str_decimal19_2"));
        assertEquals(prefix + " str_numeric19_2", expectedNumeric19_2String, resultSet.getString("str_numeric19_2"));
    }

    private static void assertRow38_4(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecimal19_2, String expectedDecimal19_2String,
            BigDecimal expectedNumeric19_2, String expectedNumeric19_2String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(prefix + " expected row", resultSet.next());
        assertEquals(prefix, expectedId, resultSet.getInt("id"));
        assertEquals(prefix + " decimal38_4", expectedDecimal19_2, resultSet.getBigDecimal("decimal38_4"));
        assertEquals(prefix + " numeric38_4", expectedNumeric19_2, resultSet.getBigDecimal("numeric38_4"));
        assertEquals(prefix + " str_decimal38_4", expectedDecimal19_2String, resultSet.getString("str_decimal38_4"));
        assertEquals(prefix + " str_numeric38_4", expectedNumeric19_2String, resultSet.getString("str_numeric38_4"));
    }

}
