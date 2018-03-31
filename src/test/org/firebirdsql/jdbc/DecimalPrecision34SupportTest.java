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
 * Tests for Firebird 4 extended precision of 34 for decimal (and numeric).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DecimalPrecision34SupportTest extends FBJUnit4TestBase {

    private static final String CREATE_TABLE =
            "create table extendeddecimal ("
                    + "id integer primary key,"
                    + "decimal19_2 decimal(19, 2),"
                    + "numeric19_2 numeric(19, 2),"
                    + "decimal34_4 numeric(34, 4),"
                    + "numeric34_4 numeric(34, 4)"
                    + ")";

    @BeforeClass
    public static void checkDecimalPrecisionOf34() {
        assumeTrue("Requires DECIMAL/NUMERIC precision 34 support",
                getDefaultSupportInfo().supportsDecimalPrecision(34));
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

            try (ResultSet rs = stmt.executeQuery(
                    "select id, decimal19_2, numeric19_2 from extendeddecimal order by id")) {
                assertTrue(rs.next());
                assertEquals("id=1", 1, rs.getInt("id"));
                final BigDecimal expected = new BigDecimal("12345678901234567.89");
                assertEquals("id=1 decimal19_2", expected, rs.getBigDecimal("decimal19_2"));
                assertEquals("id=1 numeric19_2", expected, rs.getBigDecimal("numeric19_2"));
            }
        }
    }

    @Test
    public void simpleInsert() throws Exception {
        final String stringValue = "12345678901234567.89";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
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

    /**
     * Both DECIMAL and NUMERIC with precision >= 19 will actually have precision 34.
     */
    @Test
    public void insert_actualPrecisionIs34() throws Exception {
        final String stringValue = "12345678901234567890123456789012.34";
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

    private static void execute(PreparedStatement insert, int index, BigDecimal decimal19_2, BigDecimal numeric19_2)
            throws Exception {
        insert.setInt(1, index);
        insert.setBigDecimal(2, decimal19_2);
        insert.setBigDecimal(3, numeric19_2);
        insert.execute();
    }

    private static void assertRow(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecimal19_2, String expectedDecimal19_2String,
            BigDecimal expectedNumeric19_2, String expectedNumeric19_2String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(prefix + " expected row", resultSet.next());
        assertEquals(prefix, 1, resultSet.getInt("id"));
        assertEquals(prefix + " decimal19_2", expectedDecimal19_2, resultSet.getBigDecimal("decimal19_2"));
        assertEquals(prefix + " numeric19_2", expectedNumeric19_2, resultSet.getBigDecimal("numeric19_2"));
        assertEquals(prefix + " str_decimal19_2", expectedDecimal19_2String, resultSet.getString("str_decimal19_2"));
        assertEquals(prefix + " str_numeric19_2", expectedNumeric19_2String, resultSet.getString("str_numeric19_2"));
    }

}
