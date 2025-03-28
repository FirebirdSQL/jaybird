// SPDX-FileCopyrightText: Copyright 2018-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the decfloat support, which is only available in Firebird 4.0 or higher.
 *
 * @author Mark Rotteveel
 */
class DecfloatSupportTest {

    private static final BigDecimal DECFLOAT_16_MAX = new BigDecimal("9999999999999999E+369");
    private static final BigDecimal DECFLOAT_34_MAX = new BigDecimal("9999999999999999999999999999999999E+6111");
    private static final String CREATE_TABLE =
            "create table decfloattest( "
                    + "id integer primary key, "
                    + "decfloat16 decfloat(16), "
                    + "decfloat34 decfloat(34)"
                    + ")";

    @RegisterExtension
    @Order(1)
    // NOTE: native tests also require use of a Firebird 4 client library
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsDecfloat, "requires decfloat support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static Connection connection;

    @BeforeAll
    static void setupAll() throws Exception{
        connection = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setUp() throws Exception {
        connection.setAutoCommit(true);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from decfloattest");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void simpleValueSelects() throws Exception {
        try (Statement stmt = connection.createStatement()) {
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

                assertFalse(rs.next(), "expected no more rows");
            }
        }
    }

    @Test
    void select_decfloat16_ResultSetMetaData() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decfloat16 from decfloattest")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("DECFLOAT", rsmd.getColumnTypeName(1), "typeName");
            assertEquals(JaybirdTypeCodes.DECFLOAT, rsmd.getColumnType(1), "type");
            assertEquals("java.math.BigDecimal", rsmd.getColumnClassName(1), "className");
            assertEquals(16, rsmd.getPrecision(1), "precision");
            assertEquals(0, rsmd.getScale(1), "scale");
            assertEquals(23, rsmd.getColumnDisplaySize(1), "displaySize");
            assertTrue(rsmd.isSearchable(1), "searchable");
            assertTrue(rsmd.isSigned(1), "signed");
        }
    }

    @Test
    void select_decfloat34_ResultSetMetaData() throws Exception {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("select decfloat34 from decfloattest")) {
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("DECFLOAT", rsmd.getColumnTypeName(1), "typeName");
            assertEquals(JaybirdTypeCodes.DECFLOAT, rsmd.getColumnType(1), "type");
            assertEquals("java.math.BigDecimal", rsmd.getColumnClassName(1), "className");
            assertEquals(34, rsmd.getPrecision(1), "precision");
            assertEquals(0, rsmd.getScale(1), "scale");
            assertEquals(42, rsmd.getColumnDisplaySize(1), "displaySize");
            assertTrue(rsmd.isSearchable(1), "searchable");
            assertTrue(rsmd.isSigned(1), "signed");
        }
    }

    /**
     * Tests if values inserted correctly round trip
     */
    @Test
    void insert_parameterized() throws Exception {
        // String comparison might be sensitive to platform-dependent library used by Firebird(?)
        final BigDecimal bd16AllDigits = new BigDecimal("1234567890123456E123");
        final BigDecimal bd34AllDigits = new BigDecimal("1234567890123456789012345678901234E1234");
        final BigDecimal one_pow_300 = BigDecimal.ONE.scaleByPowerOfTen(300);
        final BigDecimal one_pow_3000 = BigDecimal.ONE.scaleByPowerOfTen(3000);
        try (Statement statement = connection.createStatement();
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
                assertFalse(rs.next(), "expected no more rows");
            }
        }
    }

    // This is more a test of Firebird than of Jaybird
    @Test
    void testLiteralConversion() throws Exception {
        try (Statement stmt = connection.createStatement()) {
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
    void insert_decfloat16_ParameterMetaData() throws Exception {
        try (PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (decfloat16) values (?)")) {
            ParameterMetaData pmd = insert.getParameterMetaData();

            assertEquals("DECFLOAT", pmd.getParameterTypeName(1), "typeName");
            assertEquals(JaybirdTypeCodes.DECFLOAT, pmd.getParameterType(1), "type");
            assertEquals("java.math.BigDecimal", pmd.getParameterClassName(1), "className");
            assertEquals(16, pmd.getPrecision(1), "precision");
            assertEquals(0, pmd.getScale(1), "scale");
            assertTrue(pmd.isSigned(1), "signed");
            assertEquals(ParameterMetaData.parameterModeIn, pmd.getParameterMode(1), "parameterMode");
        }
    }

    @Test
    void insert_decfloat34_ParameterMetaData() throws Exception {
        try (PreparedStatement insert = connection.prepareStatement(
                     "insert into decfloattest (decfloat34) values (?)")) {
            ParameterMetaData pmd = insert.getParameterMetaData();

            assertEquals("DECFLOAT", pmd.getParameterTypeName(1), "typeName");
            assertEquals(JaybirdTypeCodes.DECFLOAT, pmd.getParameterType(1), "type");
            assertEquals("java.math.BigDecimal", pmd.getParameterClassName(1), "className");
            assertEquals(34, pmd.getPrecision(1), "precision");
            assertEquals(0, pmd.getScale(1), "scale");
            assertTrue(pmd.isSigned(1), "signed");
            assertEquals(ParameterMetaData.parameterModeIn, pmd.getParameterMode(1), "parameterMode");
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for DECFLOAT).
     */
    @Test
    void databaseMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        DatabaseMetaData dbmd = connection.getMetaData();

        try (ResultSet rs = dbmd.getTypeInfo()) {
            boolean foundDecfloatType = false;
            while (rs.next()) {
                if (!"DECFLOAT".equals(rs.getString("TYPE_NAME"))) {
                    continue;
                }
                foundDecfloatType = true;
                assertEquals(JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                assertEquals(34, rs.getInt("PRECISION"), "Unexpected PRECISION");
                assertEquals(DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                assertFalse(rs.getBoolean("CASE_SENSITIVE"), "Unexpected CASE_SENSITIVE");
                assertEquals(DatabaseMetaData.typeSearchable, rs.getInt("SEARCHABLE"), "Unexpected SEARCHABLE");
                assertFalse(rs.getBoolean("UNSIGNED_ATTRIBUTE"), "Unexpected UNSIGNED_ATTRIBUTE");
                assertFalse(rs.getBoolean("FIXED_PREC_SCALE"), "Unexpected FIXED_PREC_SCALE");
                assertFalse(rs.getBoolean("AUTO_INCREMENT"), "Unexpected AUTO_INCREMENT");
                assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                // Not testing other values
            }
            assertTrue(foundDecfloatType, "Expected to find decfloat type in typeInfo");
        }
    }

    @Test
    void databaseMetaData_getColumns_decfloat16() throws Exception {
        DatabaseMetaData dbmd = connection.getMetaData();
        try (ResultSet rs = dbmd.getColumns(null, null, "DECFLOATTEST", "DECFLOAT16")) {
            assertTrue(rs.next(), "Expected a row");
            assertEquals("DECFLOAT16", rs.getString("COLUMN_NAME"), "Unexpected COLUMN_NAME");
            assertEquals(JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
            assertEquals("DECFLOAT", rs.getString("TYPE_NAME"), "Unexpected TYPE_NAME");
            assertEquals(16, rs.getInt("COLUMN_SIZE"), "Unexpected COLUMN_SIZE");
            assertEquals(0, rs.getInt("DECIMAL_DIGITS"), "Unexpected DECIMAL_DIGITS");
            assertTrue(rs.wasNull(), "Expected null DECIMAL_DIGITS");
            assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
            assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
            assertEquals("NO", rs.getString("IS_AUTOINCREMENT"), "Unexpected IS_AUTOINCREMENT");

            assertFalse(rs.next(), "Expected no second row");
        }
    }

    @Test
    void databaseMetaData_getColumns_decfloat34() throws Exception {
        DatabaseMetaData dbmd = connection.getMetaData();
        try (ResultSet rs = dbmd.getColumns(null, null, "DECFLOATTEST", "DECFLOAT34")) {
            assertTrue(rs.next(), "Expected a row");
            assertEquals("DECFLOAT34", rs.getString("COLUMN_NAME"), "Unexpected COLUMN_NAME");
            assertEquals(JaybirdTypeCodes.DECFLOAT, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
            assertEquals("DECFLOAT", rs.getString("TYPE_NAME"), "Unexpected TYPE_NAME");
            assertEquals(34, rs.getInt("COLUMN_SIZE"), "Unexpected COLUMN_SIZE");
            assertEquals(0, rs.getInt("DECIMAL_DIGITS"), "Unexpected DECIMAL_DIGITS");
            assertTrue(rs.wasNull(), "Expected null DECIMAL_DIGITS");
            assertEquals(10, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
            assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
            assertEquals("NO", rs.getString("IS_AUTOINCREMENT"), "Unexpected IS_AUTOINCREMENT");

            assertFalse(rs.next(), "Expected no second row");
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
        assertTrue(resultSet.next(), "Expected row for id=" + expectedId);
        assertEquals(expectedId, resultSet.getInt("id"), "id");
        assertEquals(expectedDecfloat16, resultSet.getBigDecimal("decfloat16"), "decfloat16 " + expectedId);
        assertEquals(expectedDecfloat16String, resultSet.getString("decfloat16str"), "decfloat16str " + expectedId);
        assertEquals(expectedDecfloat34, resultSet.getBigDecimal("decfloat34"), "decfloat34 " + expectedId);
        assertEquals(expectedDecfloat34String, resultSet.getString("decfloat34str"), "decfloat34str " + expectedId);
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
