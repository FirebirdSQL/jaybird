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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDropTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the boolean support, which is only available in Firebird 3.0 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestBooleanSupport extends FBJUnit4TestBase {

    private static final String CREATE_TABLE =
            "CREATE TABLE withboolean ( id INTEGER, bool BOOLEAN )";
    private static final String DROP_TABLE =
            "DROP TABLE withboolean";
    private static final String INSERT = "INSERT INTO withboolean (id, bool) VALUES (?, ?)";
    private static final String SELECT = "SELECT id, bool FROM withboolean";
    private static final String SELECT_CONDITION_BOOL_FIELD = SELECT + " WHERE bool = ?";
    private static final String SELECT_CONDITION_SINGLETON = SELECT + " WHERE ?";
    private static final String[] TEST_DATA = {
            "INSERT INTO withboolean (id, bool) VALUES (0, FALSE)",
            "INSERT INTO withboolean (id, bool) VALUES (1, TRUE)",
            "INSERT INTO withboolean (id, bool) VALUES (2, UNKNOWN)"
    };

    @Before
    public void setUp() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            assumeTrue("Test only works on Firebird 3 or higher", con.getMetaData().getDatabaseMajorVersion() >= 3);

            executeDropTable(con, DROP_TABLE);
            executeCreateTable(con, CREATE_TABLE);
            con.setAutoCommit(false);
            Statement stmt = con.createStatement();
            try {
                for (String query : TEST_DATA) {
                    stmt.execute(query);
                }
            } finally {
                closeQuietly(stmt);
            }
            con.commit();
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test if a simple select returns the right boolean values in the ResultSet.
     */
    @Test
    public void testSimpleSelect_Values() throws Exception {
        Connection con = getConnectionViaDriverManager();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SELECT);
            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt(1);
                boolean bool = rs.getBoolean(2);
                switch (id) {
                case 0:
                    assertFalse("Column with value FALSE should have value false", bool);
                    assertFalse("Column with value FALSE should not be null", rs.wasNull());
                    break;
                case 1:
                    assertTrue("Column with value TRUE should have value true", bool);
                    assertFalse("Column with value TRUE should not be null", rs.wasNull());
                    break;
                case 2:
                    assertFalse("Column with value UNKNOWN should have value false", bool);
                    assertTrue("Column with value UNKNOWN should be null", rs.wasNull());
                    break;
                default:
                    fail("Unexpected row in result set");
                }
            }
            assertEquals("Expected 3 rows", 3, count);
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(con);
        }
    }

    /**
     * Tests if the ResultSetMetaData contains the right information on boolean columns.
     */
    @Test
    public void testSimpleSelect_ResultSetMetaData() throws Exception {
        Connection con = getConnectionViaDriverManager();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SELECT);
            final ResultSetMetaData rsmd = rs.getMetaData();
            assertEquals("Unexpected type for boolean column", Types.BOOLEAN, rsmd.getColumnType(2));
            assertEquals("Unexpected type name for boolean column", "BOOLEAN", rsmd.getColumnTypeName(2));
            assertEquals("Unexpected precision for boolean column", 1, rsmd.getPrecision(2));
            // Not testing other values
        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(con);
        }
    }

    /**
     * Tests if boolean values inserted using a parametrized query are correctly roundtripped in a query.
     */
    @Test
    public void testParametrizedInsert() throws Exception {
        Connection con = getConnectionViaDriverManager();
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement(INSERT);
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
            stmt = con.createStatement();
            rs = stmt.executeQuery(SELECT);
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
                    assertFalse("Column with value FALSE should have value false", bool);
                    assertFalse("Column with value FALSE should not be null", rs.wasNull());
                    break;
                case 4:
                    assertTrue("Column with value TRUE should have value true", bool);
                    assertFalse("Column with value TRUE should not be null", rs.wasNull());
                    break;
                case 5:
                    assertFalse("Column with value UNKNOWN should have value false", bool);
                    assertTrue("Column with value null should be null", rs.wasNull());
                    break;
                default:
                    fail("Unexpected row in result set");
                }
            }
            assertEquals("Expected 6 rows", 6, count);

        } finally {
            closeQuietly(rs);
            closeQuietly(stmt);
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Tests if the ParameterMetaData contains the right information on boolean columns.
     */
    @Test
    public void testParametrizedInsert_ParameterMetaData() throws Exception {
        Connection con = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        try {
            pstmt = con.prepareStatement(INSERT);
            final ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
            assertEquals("Unexpected type for boolean column", Types.BOOLEAN, parameterMetaData.getParameterType(2));
            assertEquals("Unexpected type name for boolean column", "BOOLEAN", parameterMetaData.getParameterTypeName(2));
            assertEquals("Unexpected precision for boolean column", 1, parameterMetaData.getPrecision(2));
            // Not testing other values
        } finally {
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Test select with condition on a boolean field.
     */
    @Test
    public void testSelectFieldCondition() throws Exception {
        Connection con = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement(SELECT_CONDITION_BOOL_FIELD);
            pstmt.setBoolean(1, true);
            rs = pstmt.executeQuery();

            assertTrue("Expected a row", rs.next());
            assertEquals("Expected row with id=1", 1, rs.getInt(1));
            assertFalse("Did not expect a second row", rs.next());
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value true
     */
    @Test
    public void testSelect_ConditionOnly_true() throws Exception {
        Connection con = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON);
            pstmt.setBoolean(1, true);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt(1);
                boolean bool = rs.getBoolean(2);
                switch (id) {
                case 0:
                    assertFalse("Column with value FALSE should have value false", bool);
                    assertFalse("Column with value FALSE should not be null", rs.wasNull());
                    break;
                case 1:
                    assertTrue("Column with value TRUE should have value true", bool);
                    assertFalse("Column with value TRUE should not be null", rs.wasNull());
                    break;
                case 2:
                    assertFalse("Column with value UNKNOWN should have value false", bool);
                    assertTrue("Column with value UNKNOWN should be null", rs.wasNull());
                    break;
                default:
                    fail("Unexpected row in result set");
                }
            }
            assertEquals("Expected 3 rows", 3, count);
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value false
     */
    @Test
    public void testSelect_ConditionOnly_false() throws Exception {
        Connection con = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON);
            pstmt.setBoolean(1, false);
            rs = pstmt.executeQuery();

            assertFalse("Expected no rows", rs.next());
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Test a select with a boolean parameter only (ie <code>WHERE ?"</code>) with value null
     */
    @Test
    public void testSelect_ConditionOnly_null() throws Exception {
        Connection con = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement(SELECT_CONDITION_SINGLETON);
            pstmt.setNull(1, Types.BOOLEAN);
            rs = pstmt.executeQuery();

            assertFalse("Expected no rows", rs.next());
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(con);
        }
    }

    /**
     * Tests the value returned by {@link FBDatabaseMetaData#getTypeInfo()} (specifically only for BOOLEAN).
     */
    @Test
    public void testMetaData_TypeInfo() throws Exception {
        // TODO Create separate test for all typeinfo information
        Connection con = getConnectionViaDriverManager();
        ResultSet rs = null;
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            rs = dbmd.getTypeInfo();

            boolean foundBooleanType = false;
            while (rs.next()) {
                if (!"BOOLEAN".equals(rs.getString("TYPE_NAME"))) {
                    continue;
                }
                foundBooleanType = true;
                assertEquals("Unexpected DATA_TYPE", Types.BOOLEAN, rs.getInt("DATA_TYPE"));
                assertEquals("Unexpected PRECISION", 1, rs.getInt("PRECISION"));
                assertEquals("Unexpected NULLABLE", DatabaseMetaData.typeNullable, rs.getInt("NULLABLE"));
                assertFalse("Unexpected CASE_SENSITIVE", rs.getBoolean("CASE_SENSITIVE"));
                assertEquals("Unexpected SEARCHABLE", DatabaseMetaData.typePredBasic, rs.getInt("SEARCHABLE"));
                assertTrue("Unexpected UNSIGNED_ATTRIBUTE", rs.getBoolean("UNSIGNED_ATTRIBUTE"));
                assertTrue("Unexpected FIXED_PREC_SCALE", rs.getBoolean("FIXED_PREC_SCALE"));
                assertFalse("Unexpected AUTO_INCREMENT", rs.getBoolean("AUTO_INCREMENT"));
                assertEquals("Unexpected NUM_PREC_RADIX", 2, rs.getInt("NUM_PREC_RADIX"));
                // Not testing other values
            }
            assertTrue("Expected to find boolean type in typeInfo", foundBooleanType);
        } finally {
            closeQuietly(rs);
            closeQuietly(con);
        }
    }

    /**
     * Test {@link FBDatabaseMetaData#getColumns(String, String, String, String)} for a boolean column.
     */
    @Test
    public void testMetaData_getColumns() throws Exception {
        // TODO Consider moving to TestFBDatabaseMetaDataColumns
        Connection con = getConnectionViaDriverManager();
        ResultSet rs = null;
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            rs = dbmd.getColumns(null, null, "WITHBOOLEAN", "BOOL");
            assertTrue("Expected a row", rs.next());
            assertEquals("Unexpected COLUMN_NAME", "BOOL", rs.getString("COLUMN_NAME"));
            assertEquals("Unexpected DATA_TYPE", Types.BOOLEAN, rs.getInt("DATA_TYPE"));
            assertEquals("Unexpected TYPE_NAME", "BOOLEAN", rs.getString("TYPE_NAME"));
            assertEquals("Unexpected COLUMN_SIZE", 1, rs.getInt("COLUMN_SIZE"));
            assertEquals("Unexpected NUM_PREC_RADIX", 2, rs.getInt("NUM_PREC_RADIX"));
            assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, rs.getInt("NULLABLE"));
            assertEquals("Unexpected IS_AUTOINCREMENT", "NO", rs.getString("IS_AUTOINCREMENT"));

            assertFalse("Expected no second row", rs.next());
        } finally {
            closeQuietly(rs);
            closeQuietly(con);
        }
    }
}
