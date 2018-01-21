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
import org.firebirdsql.common.FBTestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the decfloat support, which is only available in Firebird 4.0 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class DecfloatSupportTest extends FBJUnit4TestBase {

    private static final BigDecimal DECFLOAT_16_MAX = new BigDecimal("9999999999999999E+369");
    private static final BigDecimal DECFLOAT_34_MAX = new BigDecimal("9999999999999999999999999999999999E+6111");
    private static final String CREATE_TABLE =
            "create table decfloattest(id integer primary key, decfloat16 decfloat(16), decfloat34 decfloat(34))";

    @BeforeClass
    public static void checkBooleanSupport() {
        // NOTE: For native tests will also requires use of a Firebird 4 client library
        assumeTrue("Test requires DECFLOAT support on server", getDefaultSupportInfo().supportsDecfloat());
    }

    @Before
    public void setUp() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            DdlHelper.executeCreateTable(connection, CREATE_TABLE);
        }
    }

    @Test
    public void simpleValueSelects() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            stmt.execute("insert into decfloattest(id, decfloat16, decfloat34) values (1, null, null)");
            stmt.execute("insert into decfloattest(id, decfloat16, decfloat34) values (2, 1, 1)");
            stmt.execute("insert into decfloattest(id, decfloat16, decfloat34) "
                    + "values (3, 9999999999999999E+369, 9999999999999999999999999999999999E+6111)");

            try (ResultSet rs = stmt.executeQuery("select id, decfloat16, decfloat34 from decfloattest order by id")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("id"));
                assertNull(rs.getBigDecimal("decfloat16"));
                assertNull(rs.getBigDecimal("decfloat34"));
                
                assertTrue(rs.next());
                assertEquals(2, rs.getInt("id"));
                assertEquals(BigDecimal.ONE, rs.getBigDecimal("decfloat16"));
                assertEquals(BigDecimal.ONE, rs.getBigDecimal("decfloat34"));

                assertTrue(rs.next());
                assertEquals(3, rs.getInt("id"));
                assertEquals(DECFLOAT_16_MAX, rs.getBigDecimal("decfloat16"));
                assertEquals(DECFLOAT_34_MAX, rs.getBigDecimal("decfloat34"));

                assertFalse("expected no more rows", rs.next());
            }
        }
    }

    @Test
    public void select_decfloat16_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decfloat16 from decfloattest")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("typeName", "DECFLOAT", rsmd.getColumnTypeName(1));
            assertEquals("type", JaybirdTypeCodes.DECFLOAT, rsmd.getColumnType(1));
            assertEquals("className", "java.math.BigDecimal", rsmd.getColumnClassName(1));
            assertEquals("precision", 16, rsmd.getPrecision(1));
            assertEquals("scale", 0, rsmd.getScale(1));
            assertEquals("displaySize", 23, rsmd.getColumnDisplaySize(1));
            assertTrue("searchable", rsmd.isSearchable(1));
            assertTrue("signed", rsmd.isSigned(1));
        }
    }

    @Test
    public void select_decfloat34_ResultSetMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decfloat34 from decfloattest")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("typeName", "DECFLOAT", rsmd.getColumnTypeName(1));
            assertEquals("type", JaybirdTypeCodes.DECFLOAT, rsmd.getColumnType(1));
            assertEquals("className", "java.math.BigDecimal", rsmd.getColumnClassName(1));
            assertEquals("precision", 34, rsmd.getPrecision(1));
            assertEquals("scale", 0, rsmd.getScale(1));
            assertEquals("displaySize", 42, rsmd.getColumnDisplaySize(1));
            assertTrue("searchable", rsmd.isSearchable(1));
            assertTrue("signed", rsmd.isSigned(1));
        }
    }

    /**
     * Tests if values inserted correctly round trip
     */
    @Test
    public void insert_parameterized() throws Exception {
        // String comparison might be sensitive to platform-dependent library used by Firebird(?)
        final BigDecimal bd16AllDigits = new BigDecimal("1234567890123456E123");
        final BigDecimal bd34AllDigits = new BigDecimal("1234567890123456789012345678901234E1234");
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement();
             PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (id, decfloat16, decfloat34) values (?, ?, ?)")) {
            connection.setAutoCommit(false);
            execute(insert, 1, null, null);
            execute(insert, 2, BigDecimal.ONE, BigDecimal.ONE);
            execute(insert, 3, DECFLOAT_16_MAX, DECFLOAT_34_MAX);
            execute(insert, 4, bd16AllDigits, bd34AllDigits);

            try (ResultSet rs = statement.executeQuery(
                    "select id, "
                            + "decfloat16, cast(decfloat16 as varchar(50)) decfloat16str, "
                            + "decfloat34, cast(decfloat34 as varchar(50)) decfloat34str "
                            + "from decfloattest "
                            + "order by id")) {
                assertRow(rs, 1, null, null, null, null);
                assertRow(rs, 2, BigDecimal.ONE, "1", BigDecimal.ONE, "1");
                assertRow(rs, 3, DECFLOAT_16_MAX, "9.999999999999999E+384",
                        DECFLOAT_34_MAX, "9.999999999999999999999999999999999E+6144");
                assertRow(rs, 4, bd16AllDigits, "1.234567890123456E+138",
                        bd34AllDigits, "1.234567890123456789012345678901234E+1267");
                assertFalse("expected no more rows", rs.next());
            }
        }
    }

    @Test
    public void insert_decfloat16_ParameterMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (decfloat16) values (?)")) {
            ParameterMetaData pmd = insert.getParameterMetaData();

            assertEquals("typeName", "DECFLOAT", pmd.getParameterTypeName(1));
            assertEquals("type", JaybirdTypeCodes.DECFLOAT, pmd.getParameterType(1));
            assertEquals("className", "java.math.BigDecimal", pmd.getParameterClassName(1));
            assertEquals("precision", 16, pmd.getPrecision(1));
            assertEquals("scale", 0, pmd.getScale(1));
            assertTrue("signed", pmd.isSigned(1));
            assertEquals("parameterMode", ParameterMetaData.parameterModeIn, pmd.getParameterMode(1));
        }
    }

    @Test
    public void insert_decfloat34_ParameterMetaData() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (decfloat34) values (?)")) {
            ParameterMetaData pmd = insert.getParameterMetaData();

            assertEquals("typeName", "DECFLOAT", pmd.getParameterTypeName(1));
            assertEquals("type", JaybirdTypeCodes.DECFLOAT, pmd.getParameterType(1));
            assertEquals("className", "java.math.BigDecimal", pmd.getParameterClassName(1));
            assertEquals("precision", 34, pmd.getPrecision(1));
            assertEquals("scale", 0, pmd.getScale(1));
            assertTrue("signed", pmd.isSigned(1));
            assertEquals("parameterMode", ParameterMetaData.parameterModeIn, pmd.getParameterMode(1));
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for BOOLEAN).
     */
    @Test
    public void databaseMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        try (Connection con = getConnectionViaDriverManager()) {
            DatabaseMetaData dbmd = con.getMetaData();

            try (ResultSet rs = dbmd.getTypeInfo()) {
                boolean foundDecfloatType = false;
                while (rs.next()) {
                    if (!"DECFLOAT".equals(rs.getString("TYPE_NAME"))) {
                        continue;
                    }
                    foundDecfloatType = true;
                    assertEquals("Unexpected DATA_TYPE", JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"));
                    assertEquals("Unexpected PRECISION", 34, rs.getInt("PRECISION"));
                    assertEquals("Unexpected NULLABLE", DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"));
                    assertFalse("Unexpected CASE_SENSITIVE", rs.getBoolean("CASE_SENSITIVE"));
                    assertEquals("Unexpected SEARCHABLE", DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"));
                    assertFalse("Unexpected UNSIGNED_ATTRIBUTE", rs.getBoolean("UNSIGNED_ATTRIBUTE"));
                    assertFalse("Unexpected FIXED_PREC_SCALE", rs.getBoolean("FIXED_PREC_SCALE"));
                    assertFalse("Unexpected AUTO_INCREMENT", rs.getBoolean("AUTO_INCREMENT"));
                    assertEquals("Unexpected NUM_PREC_RADIX", 10, rs.getInt("NUM_PREC_RADIX"));
                    // Not testing other values
                }
                assertTrue("Expected to find boolean type in typeInfo", foundDecfloatType);
            }
        }
    }

    @Test
    public void databaseMetaData_getColumns_decfloat16() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            DatabaseMetaData dbmd = con.getMetaData();
            try (ResultSet rs = dbmd.getColumns(null, null, "DECFLOATTEST", "DECFLOAT16")) {
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected COLUMN_NAME", "DECFLOAT16", rs.getString("COLUMN_NAME"));
                assertEquals("Unexpected DATA_TYPE", JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"));
                assertEquals("Unexpected TYPE_NAME", "DECFLOAT", rs.getString("TYPE_NAME"));
                assertEquals("Unexpected COLUMN_SIZE", 16, rs.getInt("COLUMN_SIZE"));
                assertEquals("Unexpected DECIMAL_DIGITS", 0, rs.getInt("DECIMAL_DIGITS"));
                assertTrue("Expected null DECIMAL_DIGITS", rs.wasNull());
                assertEquals("Unexpected NUM_PREC_RADIX", 10, rs.getInt("NUM_PREC_RADIX"));
                assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
                assertEquals("Unexpected IS_AUTOINCREMENT", "NO", rs.getString("IS_AUTOINCREMENT"));

                assertFalse("Expected no second row", rs.next());
            }
        }
    }

    @Test
    public void databaseMetaData_getColumns_decfloat34() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            DatabaseMetaData dbmd = con.getMetaData();
            try (ResultSet rs = dbmd.getColumns(null, null, "DECFLOATTEST", "DECFLOAT34")) {
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected COLUMN_NAME", "DECFLOAT34", rs.getString("COLUMN_NAME"));
                assertEquals("Unexpected DATA_TYPE", JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"));
                assertEquals("Unexpected TYPE_NAME", "DECFLOAT", rs.getString("TYPE_NAME"));
                assertEquals("Unexpected COLUMN_SIZE", 34, rs.getInt("COLUMN_SIZE"));
                assertEquals("Unexpected DECIMAL_DIGITS", 0, rs.getInt("DECIMAL_DIGITS"));
                assertTrue("Expected null DECIMAL_DIGITS", rs.wasNull());
                assertEquals("Unexpected NUM_PREC_RADIX", 10, rs.getInt("NUM_PREC_RADIX"));
                assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
                assertEquals("Unexpected IS_AUTOINCREMENT", "NO", rs.getString("IS_AUTOINCREMENT"));

                assertFalse("Expected no second row", rs.next());
            }
        }
    }

    private static void execute(PreparedStatement insert, int index, BigDecimal decfloat16, BigDecimal decfloat34)
            throws Exception {
        insert.setInt(1, index);
        insert.setBigDecimal(2, decfloat16);
        insert.setBigDecimal(3, decfloat34);
        insert.execute();
    }

    private static void assertRow(ResultSet resultSet, int expectedId,
            BigDecimal expectedDecfloat16, String expectedDecfloat16String,
            BigDecimal expectedDecfloat34, String expectedDecfloat34String) throws Exception {
        assertTrue("Expected row for id=" + expectedId, resultSet.next());
        assertEquals("id", expectedId, resultSet.getInt("id"));
        assertEquals("decfloat16", expectedDecfloat16, resultSet.getBigDecimal("decfloat16"));
        assertEquals("decfloat16str", expectedDecfloat16String, resultSet.getString("decfloat16str"));
        assertEquals("decfloat34", expectedDecfloat34, resultSet.getBigDecimal("decfloat34"));
        assertEquals("decfloat34str", expectedDecfloat34String, resultSet.getString("decfloat34str"));
    }
}
