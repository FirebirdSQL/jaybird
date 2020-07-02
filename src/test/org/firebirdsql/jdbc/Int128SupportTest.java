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
import java.math.BigInteger;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for Firebird 4 INT128 datatype.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class Int128SupportTest extends FBJUnit4TestBase {

    private static final String CREATE_TABLE =
            "create table int128tbl ("
                    + "id integer primary key,"
                    + "col_int128 INT128"
                    + ")";

    @BeforeClass
    public static void checkInt128Support() {
        assumeTrue("Requires INT128 support",
                getDefaultSupportInfo().supportsInt128());
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
            stmt.execute("insert into int128tbl (id, col_int128) "
                    + "values (1, 123456789012345678901234567890123456789)");
            stmt.execute("insert into int128tbl (id, col_int128) "
                    + "values (2, -123456789012345678901234567890123456789)");

            try (ResultSet rs = stmt.executeQuery(
                    "select id, "
                            + "col_int128, cast(col_int128 as varchar(50)) str_col_int128 "
                            + "from int128tbl order by id")) {
                String row1ExpectedString = "123456789012345678901234567890123456789";
                BigDecimal row1Expected = new BigDecimal(row1ExpectedString);
                assertRow(rs, 1, row1Expected, row1ExpectedString);

                String row2ExpectedString = "-123456789012345678901234567890123456789";
                BigDecimal row2Expected = new BigDecimal(row2ExpectedString);
                assertRow(rs, 2, row2Expected, row2ExpectedString);
            }
        }
    }

    @Test
    public void simpleInsert() throws Exception {
        final String stringValue = "123456789012345678901234567890123456789";
        final BigDecimal bdValue = new BigDecimal(stringValue);
        final String stringValue2 = "-123456789012345678901234567890123456789";
        final BigDecimal bdValue2 = new BigDecimal(stringValue2);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into int128tbl(id, col_int128) values (?, ?)")) {
                execute(pstmt, 1, bdValue);
                execute(pstmt, 2, bdValue2);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "col_int128, cast(col_int128 as varchar(50)) str_col_int128 "
                                 + "from int128tbl order by id")) {
                assertRow(rs, 1, bdValue, stringValue);
                assertRow(rs, 2, bdValue2, stringValue2);
            }
        }
    }

    @Test
    public void select_int128_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select col_int128 from int128tbl")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("typeName", "INT128", rsmd.getColumnTypeName(1));
            assertEquals("type", Types.NUMERIC, rsmd.getColumnType(1));
            assertEquals("className", "java.math.BigDecimal", rsmd.getColumnClassName(1));
            assertEquals("precision", 38, rsmd.getPrecision(1));
            assertEquals("scale", 0, rsmd.getScale(1));
            assertEquals("displaySize", 40, rsmd.getColumnDisplaySize(1));
            assertTrue("searchable", rsmd.isSearchable(1));
            assertTrue("signed", rsmd.isSigned(1));
        }
    }

    @Test
    public void insertAndSelectMinAndMaxValues() throws Exception {
        BigInteger maxValue = BigInteger.ONE.pow(127).subtract(BigInteger.ONE);
        BigInteger minValue = BigInteger.ZERO.min(BigInteger.ONE.pow(127));
        final String stringValue = maxValue.toString();
        final BigDecimal bdValue = new BigDecimal(maxValue);
        final String stringValue2 = minValue.toString();
        final BigDecimal bdValue2 = new BigDecimal(minValue);
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(true);
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "insert into int128tbl(id, col_int128) values (?, ?)")) {
                execute(pstmt, 1, bdValue);
                execute(pstmt, 2, bdValue2);
            }

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "select id, "
                                 + "col_int128, cast(col_int128 as varchar(50)) str_col_int128 "
                                 + "from int128tbl order by id")) {
                assertRow(rs, 1, bdValue, stringValue);
                assertRow(rs, 2, bdValue2, stringValue2);
            }
        }
    }

    private static void execute(PreparedStatement insert, int index, BigDecimal decimal2)
            throws Exception {
        insert.setInt(1, index);
        insert.setBigDecimal(2, decimal2);
        insert.execute();
    }

    private static void assertRow(ResultSet resultSet, int expectedId,
            BigDecimal expectedInt128, String expectedInt128String) throws Exception {
        String prefix = "id=" + expectedId;
        assertTrue(prefix + " expected row", resultSet.next());
        assertEquals(prefix, expectedId, resultSet.getInt("id"));
        assertEquals(prefix + " col_int128", expectedInt128, resultSet.getBigDecimal("col_int128"));
        assertEquals(prefix + " str_col_int128", expectedInt128String, resultSet.getString("str_col_int128"));
    }

}
