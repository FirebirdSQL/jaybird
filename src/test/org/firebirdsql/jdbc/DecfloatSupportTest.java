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
import java.math.RoundingMode;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

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
            "create table decfloattest( "
                    + "id integer primary key, "
                    + "decfloat16 decfloat(16), "
                    + "decfloat34 decfloat(34)"
                    + ")";

    @BeforeClass
    public static void checkDecfloatSupport() {
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
        // NOTE Currently broken due to CORE-5696
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
        final BigDecimal one_pow_300 = BigDecimal.ONE.scaleByPowerOfTen(300);
        final BigDecimal one_pow_3000 = BigDecimal.ONE.scaleByPowerOfTen(3000);
        try (Connection connection = getConnectionViaDriverManager();
             Statement statement = connection.createStatement();
             PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (id, decfloat16, decfloat34) values (?, ?, ?)")) {
            connection.setAutoCommit(false);
            execute(insert, 1, null, null);
            execute(insert, 2, BigDecimal.ONE, BigDecimal.ONE);
            execute(insert, 3, DECFLOAT_16_MAX, DECFLOAT_34_MAX);
            execute(insert, 4, bd16AllDigits, bd34AllDigits);
            execute(insert, 5, one_pow_300, one_pow_3000);

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
                assertRow(rs, 5, one_pow_300, "1E+300", one_pow_3000, "1E+3000");
                assertFalse("expected no more rows", rs.next());
            }
        }
    }

    // This is more a test of Firebird than of Jaybird
    @Test
    public void testLiteralConversion() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            List<LiteralTestCase> literalTestCases = Arrays.asList(
                    new LiteralTestCase(1, "-0", BigDecimal.ZERO, "-0", BigDecimal.ZERO, "-0"),
                    new LiteralTestCase(2, "-0E300", BigDecimal.ZERO.scaleByPowerOfTen(300), "-0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "-0E+300"),
                    new LiteralTestCase(3, "-0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "-0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "-0E+300"),
                    new LiteralTestCase(4, "-0.0E300", BigDecimal.ZERO.scaleByPowerOfTen(299), "-0E+299", BigDecimal.ZERO.scaleByPowerOfTen(299), "-0E+299"),
                    new LiteralTestCase(5, "0E300", BigDecimal.ZERO.scaleByPowerOfTen(300), "0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "0E+300"),
                    new LiteralTestCase(6, "0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "0E+300", BigDecimal.ZERO.scaleByPowerOfTen(300), "0E+300"),
                    new LiteralTestCase(7, "0.0E300", BigDecimal.ZERO.scaleByPowerOfTen(299), "0E+299", BigDecimal.ZERO.scaleByPowerOfTen(299), "0E+299"),
                    new LiteralTestCase(8, "0E369", BigDecimal.ZERO.scaleByPowerOfTen(369), "0E+369", BigDecimal.ZERO.scaleByPowerOfTen(369), "0E+369"),
                    new LiteralTestCase(9, "0E-398", BigDecimal.ZERO.scaleByPowerOfTen(-398), "0E-398", BigDecimal.ZERO.scaleByPowerOfTen(-398), "0E-398"),
                    new LiteralTestCase(10, "0E370", null, null, BigDecimal.ZERO.scaleByPowerOfTen(370), "0E+370").decfloat34Only(),
                    new LiteralTestCase(11, "0E-399", null, null, BigDecimal.ZERO.scaleByPowerOfTen(-399), "0E-399").decfloat34Only(),
                    new LiteralTestCase(12, "0E6111", null, null, BigDecimal.ZERO.scaleByPowerOfTen(6111), "0E+6111").decfloat34Only(),
                    new LiteralTestCase(13, "0E-6176", null, null, BigDecimal.ZERO.scaleByPowerOfTen(-6176), "0E-6176").decfloat34Only(),
                    new LiteralTestCase(14, "1E6111", null, null, BigDecimal.ONE.scaleByPowerOfTen(6111), "1E+6111").decfloat34Only(),
                    new LiteralTestCase(15, "1E6144", null, null, BigDecimal.ONE.setScale(33, RoundingMode.UNNECESSARY).scaleByPowerOfTen(6144), "1.000000000000000000000000000000000E+6144").decfloat34Only(),
                    new LiteralTestCase(16, "1E-6176", null, null, BigDecimal.ONE.scaleByPowerOfTen(-6176), "1E-6176").decfloat34Only(),
                    new LiteralTestCase(17, "1.234567890123456789012345678901234E0", null, null, new BigDecimal("1.234567890123456789012345678901234"), "1.234567890123456789012345678901234").decfloat34Only(),
                    new LiteralTestCase(18, "1E300", BigDecimal.ONE.scaleByPowerOfTen(300), "1E+300", BigDecimal.ONE.scaleByPowerOfTen(300), "1E+300")
            );
            for (LiteralTestCase testCase : literalTestCases) {
                testCase.createTestCase(stmt);
            }
            try (ResultSet rs = stmt.executeQuery(
                    "select id, "
                            + "decfloat16, cast(decfloat16 as varchar(50)) decfloat16str, "
                            + "decfloat34, cast(decfloat34 as varchar(50)) decfloat34str "
                            + "from decfloattest "
                            + "order by id")) {
                for (LiteralTestCase testCase : literalTestCases) {
                    testCase.assertRow(rs);
                }
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
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for DECFLOAT).
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
                assertTrue("Expected to find decfloat type in typeInfo", foundDecfloatType);
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
        assertEquals("decfloat16 " + expectedId, expectedDecfloat16, resultSet.getBigDecimal("decfloat16"));
        assertEquals("decfloat16str " + expectedId, expectedDecfloat16String, resultSet.getString("decfloat16str"));
        assertEquals("decfloat34 " + expectedId, expectedDecfloat34, resultSet.getBigDecimal("decfloat34"));
        assertEquals("decfloat34str " + expectedId, expectedDecfloat34String, resultSet.getString("decfloat34str"));
    }

    private static class LiteralTestCase {

        private final int id;
        private final String literal;
        private final BigDecimal expectedDecfloat16;
        private final String expectedDecfloat16String;
        private final BigDecimal expectedDecfloat34;
        private final String expectedDecfloat34String;
        private boolean decfloat34Only;

        LiteralTestCase(int id, String literal, BigDecimal expectedDecfloat16, String expectedDecfloat16String,
                BigDecimal expectedDecfloat34, String expectedDecfloat34String) {
            this.id = id;
            this.literal = literal;
            this.expectedDecfloat16 = expectedDecfloat16;
            this.expectedDecfloat16String = expectedDecfloat16String;
            this.expectedDecfloat34 = expectedDecfloat34;
            this.expectedDecfloat34String = expectedDecfloat34String;
        }

        LiteralTestCase decfloat34Only() {
            decfloat34Only = true;
            return this;
        }

        void createTestCase(Statement statement) throws SQLException {
            statement.execute(getInsertStatement());
        }

        String getInsertStatement() {
            if (decfloat34Only) {
                return "insert into decfloattest(id, decfloat34) values (" + id + ", " + literal + ")";
            }
            return "insert into decfloattest(id, decfloat16, decfloat34) values (" + id + ", " + literal + ", "
                    + literal + ")";
        }

        void assertRow(ResultSet resultSet) throws Exception {
            if (decfloat34Only) {
                DecfloatSupportTest.assertRow(resultSet, id, null, null, expectedDecfloat34, expectedDecfloat34String);
            } else {
                DecfloatSupportTest.assertRow(resultSet, id, expectedDecfloat16, expectedDecfloat16String,
                        expectedDecfloat34, expectedDecfloat34String);
            }
        }
    }
}
