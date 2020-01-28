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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for retrieval of auto generated keys through {@link java.sql.PreparedStatement}
 * implementation {@link FBPreparedStatement} created through {@link FBConnection}
 * <p>
 * This is an integration test which uses an actual database.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBPreparedStatementGeneratedKeys extends FBTestGeneratedKeysBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    // TODO Add test cases with quoted and unquoted object names

    private static final String TEXT_VALUE = "Some text to insert";
    private static final String TEST_INSERT_QUERY = "INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES (?)";
    private Connection con;

    @Before
    public void setUpConnection() throws SQLException {
        assumeTrue("Test requires support for INSERT ... RETURNING ...", getDefaultSupportInfo().supportsInsertReturning());
        con = getConnectionViaDriverManager();
    }

    @After
    public void tearDownConnection() throws SQLException {
        closeQuietly(con);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with value {@link Statement#NO_GENERATED_KEYS}.
     * <p>
     * Expected: INSERT statement type and empty generatedKeys result set.
     * </p>
     */
    @Test
    public void testPrepare_INSERT_noGeneratedKeys() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.NO_GENERATED_KEYS);
        assertEquals(FirebirdPreparedStatement.TYPE_INSERT, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement not to produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set without columns", 0, metaData.getColumnCount());

        assertFalse("Expected no rows in result set", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS}.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    public void testPrepare_INSERT_returnGeneratedKeys() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 3 columns", 3, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
        assertEquals("Unexpected second column", "TEXT", metaData.getColumnName(2));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertEquals(TEXT_VALUE, rs.getString(2));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    @Test
    public void testReturnGeneratedKeysWithBatchExecution() throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, TEXT_VALUE + "1");
            stmt.addBatch();
            stmt.setString(1, TEXT_VALUE + "2");
            stmt.addBatch();
            stmt.setString(1, TEXT_VALUE + "3");
            stmt.addBatch();

            int[] updateCounts = stmt.executeBatch();
            assertArrayEquals("update counts", new int[] { 1, 1, 1 }, updateCounts);

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                for (int i = 1; i <= 3; i++) {
                    assertTrue("Expected row " + i, rs.next());
                    assertEquals("Unexpected value", 512 + i, rs.getInt(1));
                    assertEquals("Unexpected value", TEXT_VALUE + i, rs.getString(2));
                }
                assertFalse("Expected no more rows", rs.next());
            }

            stmt.setString(1, TEXT_VALUE);
            stmt.executeUpdate();

            // Checking that generated keys result set of a normal execute doesn't contain the rows from executeBatch
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                assertTrue("Expected row", rs.next());
                assertEquals("Unexpected value", 512 + 4, rs.getInt(1));
                assertEquals("Unexpected value", TEXT_VALUE, rs.getString(2));
                assertFalse("Expected no more rows", rs.next());
            }
        }
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    public void testPrepare_INSERT_returnGeneratedKeys_withReturning() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY + " RETURNING ID", Statement.RETURN_GENERATED_KEYS);
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 1 column", 1, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with {@link Statement#RETURN_GENERATED_KEYS} with an INSERT which already has a RETURNING clause.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, all columns of table returned, single row result set
     * </p>
     */
    @Test
    public void testPrepare_INSERT_returnGeneratedKeys_withReturningAll() throws Exception {
        assumeTrue("requires RETURNING * support", getDefaultSupportInfo().supportsReturningAll());

        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY + " RETURNING *", Statement.RETURN_GENERATED_KEYS);
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 3 columns", 3, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
        assertEquals("Unexpected second column", "TEXT", metaData.getColumnName(2));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertEquals(TEXT_VALUE, rs.getString(2));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int)} with
     * {@link Statement#RETURN_GENERATED_KEYS} with an INSERT for a non existent table.
     * <p>
     * Expected: SQLException Table unknown
     * </p>
     */
    @Test
    public void testPrepare_INSERT_returnGeneratedKeys_nonExistentTable() throws Exception {
        // Firebird 4+ uses RETURNING *, while earlier version produce a custom error as the columns can't be retrieved
        boolean usesReturningAll = getDefaultSupportInfo().supportsReturningAll();
        int errorCode = usesReturningAll
                ? ISCConstants.isc_dsql_relation_err
                : JaybirdErrorCodes.jb_generatedKeysNoColumnsFound;
        expectedException.expect(allOf(
                isA(SQLSyntaxErrorException.class),
                errorCode(equalTo(errorCode)),
                sqlState(equalTo("42S02")),
                fbMessageContains(errorCode, "TABLE_NON_EXISTENT")
        ));

        con.prepareStatement("INSERT INTO TABLE_NON_EXISTENT(TEXT) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int[])} with a single column index.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, single row result set with only the specified column.
     * </p>
     */
    @Test
    public void testPrepare_INSERT_columnIndexes() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new int[] { 1 });
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 1 column", 1, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, int[])} with a multiple indexes, one for a quoted column.
     * <p>
     * Expected: TYPE_EXEC_PROCEDURE statement type, single row result set with only the specified columns
     * </p>
     */
    @Test
    public void testPrepare_INSERT_columnIndexes_quotedColumn() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new int[] { 1, 3 });
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 2 column", 2, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));
        assertEquals("Unexpected second column", "quote_column", metaData.getColumnName(2));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertEquals(2, rs.getInt(2));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    // Other combination for execute(String, int[]) already covered in TestGeneratedKeysQuery

    /**
     * Test for PreparedStatement created through {@link FBConnection#prepareStatement(String, String[])} with a single column name.
     * <p>
     * Expected: single row result set with only the specified column.
     * </p>
     */
    @Test
    public void testPrepare_INSERT_columnNames() throws Exception {
        PreparedStatement stmt = con.prepareStatement(TEST_INSERT_QUERY, new String[] { "ID" });
        assertEquals(FirebirdPreparedStatement.TYPE_EXEC_PROCEDURE, ((FirebirdPreparedStatement) stmt).getStatementType());

        stmt.setString(1, TEXT_VALUE);
        assertFalse("Expected statement to not produce a result set", stmt.execute());

        ResultSet rs = stmt.getGeneratedKeys();
        assertNotNull("Expected a non-null result set from getGeneratedKeys", rs);

        assertEquals("Update count should be directly available", 1, stmt.getUpdateCount());
        assertFalse("Generated keys result set should be open", rs.isClosed());

        ResultSetMetaData metaData = rs.getMetaData();
        assertEquals("Expected result set with 1 column", 1, metaData.getColumnCount());
        assertEquals("Unexpected first column", "ID", metaData.getColumnName(1));

        assertTrue("Expected first row in result set", rs.next());
        assertEquals(513, rs.getInt(1));
        assertFalse("Expected no second row", rs.next());

        closeQuietly(rs);
        closeQuietly(stmt);
    }

    /**
     * Test for {@link FBStatement#execute(String, String[])} with an array of columns containing a non-existent column name.
     * <p>
     * Expected: SQLException for Column unknown.
     * </p>
     */
    @Test
    public void testPrepare_INSERT_columnNames_nonExistentColumn() throws Exception {
        expectedException.expect(allOf(
                isA(SQLException.class),
                errorCode(equalTo(ISCConstants.isc_dsql_field_err)),
                sqlState(equalTo("42S22")),
                message(containsString("Column unknown; NON_EXISTENT"))
        ));

        con.prepareStatement(TEST_INSERT_QUERY, new String[] { "ID", "NON_EXISTENT" });
    }

    // TODO In the current implementation executeUpdate uses almost identical logic as execute, decide to test separately or not

    @Test
    public void testPrepare_SELECT_RETURN_GENERATED_KEYS_handledNormally() throws Exception {
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", Statement.RETURN_GENERATED_KEYS);
        boolean isResultSet = pstmt.execute();
        assertTrue("Expected first result to be a result set", isResultSet);
        ResultSet rs = pstmt.getResultSet();
        assertNotNull("Expected a result set", rs);
        assertTrue("Expected a row", rs.next());
    }

    @Test
    public void testPrepare_SELECT_columnIndexes_handledNormally() throws Exception {
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", new int[] { 1, 2 });
        boolean isResultSet = pstmt.execute();
        assertTrue("Expected first result to be a result set", isResultSet);
        ResultSet rs = pstmt.getResultSet();
        assertNotNull("Expected a result set", rs);
        assertTrue("Expected a row", rs.next());
    }

    @Test
    public void testPrepare_SELECT_columnNames_handledNormally() throws Exception {
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM RDB$DATABASE", new String[] { "field1", "field2" });
        boolean isResultSet = pstmt.execute();
        assertTrue("Expected first result to be a result set", isResultSet);
        ResultSet rs = pstmt.getResultSet();
        assertNotNull("Expected a result set", rs);
        assertTrue("Expected a row", rs.next());
    }
}
