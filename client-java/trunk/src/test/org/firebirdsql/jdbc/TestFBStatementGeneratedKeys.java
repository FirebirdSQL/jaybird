/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for retrieval of auto generated keys through {@link java.sql.Statement}
 * implementation {@link FBStatement}.
 * <p>
 * This is an integration test which uses an actual database.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBStatementGeneratedKeys extends FBTestGeneratedKeysBase {

    private static final String TEXT_VALUE = "Some text to insert";
    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('" + TEXT_VALUE + "')";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Test {@link FBStatement#execute(String, int)} with {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: empty generatedKeys resultset.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecute_INSERT_noGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);
            assertFalse("Expected execute to report false (no resultset) for INSERT without generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset without columns", 0, metaData.getColumnCount());

            assertFalse("Expected no rows in resultset", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test {@link FBStatement#executeUpdate(String, int)} with {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: empty generatedKeys resultset.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecuteUpdate_INSERT_noGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);
            assertEquals("Expected update count of 1", 1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset without columns", 0, metaData.getColumnCount());

            assertFalse("Expected no rows in resultset", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: all columns of table returned, single row resultset
     * </p>
     */
    @Test
    public void testExecute_INSERT_returnGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            assertTrue("Expected execute to report true (has resultset) for INSERT with generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 3 columns", 3, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            assertEquals("Unexpected second column", "TEXT", metaData.getColumnName(2));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertEquals(TEXT_VALUE, rs.getString(2));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: all columns of table returned, single row resultset
     * </p>
     */
    @Test
    public void testExecuteUpdate_INSERT_returnGeneratedKeys() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            // TODO: We need to revise how generated keys are processed, we should get the update count!
            assertEquals("Expected -1 update count", -1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 3 columns", 3, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            assertEquals("Unexpected second column", "TEXT", metaData.getColumnName(2));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertEquals(TEXT_VALUE, rs.getString(2));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: all columns of table returned, single row resultset
     * </p>
     */
    @Test
    public void testExecute_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS);
            assertTrue("Expected execute to report true (has resultset) for INSERT with generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: all columns of table returned, single row resultset
     * </p>
     */
    @Test
    public void testExecuteUpdate_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS);
            // TODO: We need to revise how generated keys are processed, we should get the update count!
            assertEquals("Expected -1 update count", -1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT for a non existent table.
     * <p>
     * Expected: SQLException Table unknown
     * </p>
     */
    @Test
    public void testExecute_INSERT_returnGeneratedKeys_nonExistentTable() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    errorCode(equalTo(ISCConstants.isc_dsql_error)),
                    sqlState(equalTo("42000")),
                    message(containsString("Table unknown\nTABLE_NON_EXISTENT"))
            ));

            stmt.execute("INSERT INTO TABLE_NON_EXISTENT(TEXT) VALUES ('" + TEXT_VALUE + "')", Statement.RETURN_GENERATED_KEYS);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int[])} with a single column index.
     * <p>
     * Expected: single row resultset with only the specified column.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecute_INSERT_columnIndexes() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY, new int[] { 1 });
            assertTrue("Expected execute to report true (has resultset) for INSERT with generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, int[])} with a single column index.
     * <p>
     * Expected: single row resultset with only the specified column.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecuteUpdate_INSERT_columnIndexes() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY, new int[] { 1 });
            // TODO: We need to revise how generated keys are processed, we should get the update count!
            assertEquals("Expected -1 update count", -1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, int[])} with multiple indexes, one for a column which requires a quoted name.
     * <p>
     * Expected: single row resultset with only the specified columns.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecute_INSERT_columnIndexes_quotedColumn() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY, new int[] { 1, 3 });
            assertTrue("Expected execute to report true (has resultset) for INSERT with generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 2 column", 2, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            assertEquals("Unexpected second column", "quote_column", metaData.getColumnName(2));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertEquals(2, rs.getInt(2));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, int[])} with multiple indexes, one for a column which requires a quoted name.
     * <p>
     * Expected: single row resultset with only the specified columns.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecuteUpdate_INSERT_columnIndexes_quotedColumn() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY, new int[] { 1, 3 });
            // TODO: We need to revise how generated keys are processed, we should get the update count!
            assertEquals("Expected -1 update count", -1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 2 column", 2, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
            assertEquals("Unexpected second column", "quote_column", metaData.getColumnName(2));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertEquals(2, rs.getInt(2));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    // Other combination for execute(String, int[]) already covered in TestGeneratedKeysQuery

    /**
     * Test for {@link FBStatement#execute(String, String[])} with a single column name.
     * <p>
     * Expected: single row resultset with only the specified column.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecute_INSERT_columnNames() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            boolean producedResultSet = stmt.execute(TEST_INSERT_QUERY, new String[] { "ID" });
            assertTrue("Expected execute to report true (has resultset) for INSERT with generated keys returned", producedResultSet);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#executeUpdate(String, String[])} with a single column name.
     * <p>
     * Expected: single row resultset with only the specified column.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecuteUpdate_INSERT_columnNames() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();

            int updateCount = stmt.executeUpdate(TEST_INSERT_QUERY, new String[] { "ID" });
            // TODO: We need to revise how generated keys are processed, we should get the update count!
            assertEquals("Expected -1 update count", -1, updateCount);

            ResultSet rs = stmt.getGeneratedKeys();
            assertNotNull("Expected a non-null resultset from getGeneratedKeys", rs);

            ResultSetMetaData metaData = rs.getMetaData();
            assertEquals("Expected resultset with 1 column", 1, metaData.getColumnCount());
            assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

            assertTrue("Expected first row in resultset", rs.next());
            assertEquals(513, rs.getInt(1));
            assertFalse("Expected no second row", rs.next());

            closeQuietly(rs);
            closeQuietly(stmt);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Test for {@link FBStatement#execute(String, String[])} with an array of columns containing a non-existent column name.
     * <p>
     * Expected: SQLException for Column unknown.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testExecute_INSERT_columnNames_nonExistentColumn() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    errorCode(equalTo(ISCConstants.isc_dsql_error)),
                    sqlState(equalTo("42000")),
                    message(containsString("Column unknown\nNON_EXISTENT"))
            ));

            stmt.execute(TEST_INSERT_QUERY, new String[] { "ID", "NON_EXISTENT" });
        } finally {
            closeQuietly(con);
        }
    }
}
