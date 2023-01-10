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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the boolean support, which is only available in Firebird 3.0 or higher.
 *
 * @author Mark Rotteveel
 */
class BooleanSupportTest {

    @RegisterExtension
    @Order(1)
    // NOTE: native tests require use of a Firebird 3 or higher client library
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsBoolean, "Test requires BOOLEAN support on server")
            .build();

    private static final String CREATE_TABLE =
            "CREATE TABLE withboolean ( id INTEGER, bool BOOLEAN )";
    private static final String INSERT = "INSERT INTO withboolean (id, bool) VALUES (?, ?)";
    private static final String SELECT = "SELECT id, bool FROM withboolean";
    private static final String SELECT_CONDITION_BOOL_FIELD = SELECT + " WHERE bool = ?";
    private static final String SELECT_CONDITION_SINGLETON = SELECT + " WHERE ?";
    private static final String[] TEST_DATA = {
            "INSERT INTO withboolean (id, bool) VALUES (0, FALSE)",
            "INSERT INTO withboolean (id, bool) VALUES (1, TRUE)",
            "INSERT INTO withboolean (id, bool) VALUES (2, UNKNOWN)"
    };

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll useDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private Connection con;

    @BeforeEach
    void setUp() throws Exception {
        con = FBTestProperties.getConnectionViaDriverManager();
        con.setAutoCommit(false);
        try (Statement stmt = con.createStatement()) {
            stmt.execute("delete from withboolean");
            for (String query : TEST_DATA) {
                stmt.execute(query);
            }
        } finally {
            con.setAutoCommit(true);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        con.close();
    }

    /**
     * Test if a simple select returns the right boolean values in the ResultSet.
     */
    @Test
    void testSimpleSelect_Values() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT)) {
            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt(1);
                boolean bool = rs.getBoolean(2);
                switch (id) {
                case 0:
                    assertFalse(bool, "Column with value FALSE should have value false");
                    assertFalse(rs.wasNull(), "Column with value FALSE should not be null");
                    break;
                case 1:
                    assertTrue(bool, "Column with value TRUE should have value true");
                    assertFalse(rs.wasNull(), "Column with value TRUE should not be null");
                    break;
                case 2:
                    assertFalse(bool, "Column with value UNKNOWN should have value false");
                    assertTrue(rs.wasNull(), "Column with value UNKNOWN should be null");
                    break;
                default:
                    fail("Unexpected row in result set");
                }
            }
            assertEquals(3, count, "Expected 3 rows");
        }
    }

    /**
     * Tests if the ResultSetMetaData contains the right information on boolean columns.
     */
    @Test
    void testSimpleSelect_ResultSetMetaData() throws Exception {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT)) {
            final ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals(Types.BOOLEAN, rsmd.getColumnType(2), "Unexpected type for boolean column");
            assertEquals("BOOLEAN", rsmd.getColumnTypeName(2), "Unexpected type name for boolean column");
            assertEquals(1, rsmd.getPrecision(2), "Unexpected precision for boolean column");
            // Not testing other values
        }
    }

    /**
     * Tests if boolean values inserted using a parametrized query are correctly roundtripped in a query.
     */
    @Test
    void testParametrizedInsert() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(INSERT)) {
            pstmt.setInt(1, 3);
            pstmt.setBoolean(2, false);
            pstmt.executeUpdate();

            pstmt.setInt(1, 4);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();

            pstmt.setInt(1, 5);
            pstmt.setNull(2, Types.BOOLEAN);
            pstmt.executeUpdate();

            // Testing for inserted values
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(SELECT)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    boolean bool = rs.getBoolean(2);
                    switch (id) {
                    case 0:
                    case 1:
                    case 2:
                        continue;
                    case 3:
                        assertFalse(bool, "Column with value FALSE should have value false");
                        assertFalse(rs.wasNull(), "Column with value FALSE should not be null");
                        break;
                    case 4:
                        assertTrue(bool, "Column with value TRUE should have value true");
                        assertFalse(rs.wasNull(), "Column with value TRUE should not be null");
                        break;
                    case 5:
                        assertFalse(bool, "Column with value UNKNOWN should have value false");
                        assertTrue(rs.wasNull(), "Column with value null should be null");
                        break;
                    default:
                        fail("Unexpected row in result set");
                    }
                }
                assertEquals(6, count, "Expected 6 rows");
            }

        }
    }

    /**
     * Tests if the ParameterMetaData contains the right information on boolean columns.
     */
    @Test
    void testParametrizedInsert_ParameterMetaData() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(INSERT)) {
            final ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
            assertEquals(Types.BOOLEAN, parameterMetaData.getParameterType(2), "Unexpected type for boolean column");
            assertEquals("BOOLEAN", parameterMetaData.getParameterTypeName(2), "Unexpected type name for boolean column");
            assertEquals(1, parameterMetaData.getPrecision(2), "Unexpected precision for boolean column");
            // Not testing other values
        }
    }

    /**
     * Test select with condition on a boolean field.
     */
    @Test
    void testSelectFieldCondition() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(SELECT_CONDITION_BOOL_FIELD)) {
            pstmt.setBoolean(1, true);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(1, rs.getInt(1), "Expected row with id=1");
                assertFalse(rs.next(), "Did not expect a second row");
            }
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value true
     */
    @Test
    void testSelect_ConditionOnly_true() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON)) {
            pstmt.setBoolean(1, true);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    int id = rs.getInt(1);
                    boolean bool = rs.getBoolean(2);
                    switch (id) {
                    case 0:
                        assertFalse(bool, "Column with value FALSE should have value false");
                        assertFalse(rs.wasNull(), "Column with value FALSE should not be null");
                        break;
                    case 1:
                        assertTrue(bool, "Column with value TRUE should have value true");
                        assertFalse(rs.wasNull(), "Column with value TRUE should not be null");
                        break;
                    case 2:
                        assertFalse(bool, "Column with value UNKNOWN should have value false");
                        assertTrue(rs.wasNull(), "Column with value UNKNOWN should be null");
                        break;
                    default:
                        fail("Unexpected row in result set");
                    }
                }
                assertEquals(3, count, "Expected 3 rows");
            }
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value false
     */
    @Test
    void testSelect_ConditionOnly_false() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON)) {
            pstmt.setBoolean(1, false);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertFalse(rs.next(), "Expected no rows");
            }
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value null
     */
    @Test
    void testSelect_ConditionOnly_null() throws Exception {
        try (PreparedStatement pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON)) {
            pstmt.setNull(1, Types.BOOLEAN);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertFalse(rs.next(), "Expected no rows");
            }
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for BOOLEAN).
     */
    @Test
    void testMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        DatabaseMetaData dbmd = con.getMetaData();

        try (ResultSet rs = dbmd.getTypeInfo()) {
            boolean foundBooleanType = false;
            while (rs.next()) {
                if (!"BOOLEAN".equals(rs.getString("TYPE_NAME"))) {
                    continue;
                }
                foundBooleanType = true;
                assertEquals(Types.BOOLEAN, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
                assertEquals(1, rs.getInt("PRECISION"), "Unexpected PRECISION");
                assertEquals(DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
                assertFalse(rs.getBoolean("CASE_SENSITIVE"), "Unexpected CASE_SENSITIVE");
                assertEquals(DatabaseMetaData.typePredBasic, rs.getInt("SEARCHABLE"), "Unexpected SEARCHABLE");
                assertTrue(rs.getBoolean("UNSIGNED_ATTRIBUTE"), "Unexpected UNSIGNED_ATTRIBUTE");
                assertTrue(rs.getBoolean("FIXED_PREC_SCALE"), "Unexpected FIXED_PREC_SCALE");
                assertFalse(rs.getBoolean("AUTO_INCREMENT"), "Unexpected AUTO_INCREMENT");
                assertEquals(2, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
                // Not testing other values
            }
            assertTrue(foundBooleanType, "Expected to find boolean type in typeInfo");
        }
    }

    /**
     * Test {@link FBDatabaseMetaData#getColumns(String, String, String, String)} for a boolean column.
     */
    @Test
    void testMetaData_getColumns() throws Exception {
        // TODO Consider moving to TestFBDatabaseMetaDataColumns
        DatabaseMetaData dbmd = con.getMetaData();
        try (ResultSet rs = dbmd.getColumns(null, null, "WITHBOOLEAN", "BOOL")) {
            assertTrue(rs.next(), "Expected a row");
            assertEquals("BOOL", rs.getString("COLUMN_NAME"), "Unexpected COLUMN_NAME");
            assertEquals(Types.BOOLEAN, rs.getInt("DATA_TYPE"), "Unexpected DATA_TYPE");
            assertEquals("BOOLEAN", rs.getString("TYPE_NAME"), "Unexpected TYPE_NAME");
            assertEquals(1, rs.getInt("COLUMN_SIZE"), "Unexpected COLUMN_SIZE");
            assertEquals(2, rs.getInt("NUM_PREC_RADIX"), "Unexpected NUM_PREC_RADIX");
            assertEquals(DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"), "Unexpected NULLABLE");
            assertEquals("NO", rs.getString("IS_AUTOINCREMENT"), "Unexpected IS_AUTOINCREMENT");

            assertFalse(rs.next(), "Expected no second row");
        }
    }
}
