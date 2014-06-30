/*
 * $Id$
 *
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

import java.sql.*;
import java.util.Arrays;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.DdlHelper.*;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBStatement}.
 */
public class TestFBStatement extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private Connection con;

    private static final int DATA_ITEMS = 5;
    private static final String CREATE_TABLE = "CREATE TABLE test ( col1 INTEGER )";
    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";

    @Before
    public void setUp() throws Exception {
        con = getConnectionViaDriverManager();

        try {
            executeCreateTable(con, CREATE_TABLE);
        } finally {
            closeQuietly(con);
        }
        con = getConnectionViaDriverManager();
    }

    @After
    public void tearDown() throws Exception {
        closeQuietly(con);
    }

    /**
     * Closing a statement twice should not result in an Exception.
     */
    @Test
    public void testDoubleClose() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        stmt.close();
    }

    /**
     * Test of initial value of isCloseOnCompletion, expected: false.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    public void testIsCloseOnCompletion_initial() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        assertFalse("Initial value of isCloseOnCompletion expected to be false",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test of value of isCloseOnCompletion after closeOnCompletion call,
     * expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    public void testIsCloseOnCompletion_afterCloseOnCompletion() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        stmt.closeOnCompletion();
        assertTrue("Value of isCloseOnCompletion after closeOnCompletion expected to be true",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test of value of isCloseOnCompletion after multiple calls to
     * closeOnCompletion call, expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    public void testIsCloseOnCompletion_multipleCloseOnCompletion() throws SQLException {
        // Cast so it also works under JDBC 3.0 and 4.0
        FBStatement stmt = (FBStatement) con.createStatement();
        stmt.closeOnCompletion();
        stmt.closeOnCompletion();
        assertTrue("Value of isCloseOnCompletion after closeOnCompletion expected to be true",
                stmt.isCloseOnCompletion());
    }

    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is false, will not close
     * the statement.
     */
    @Test
    public void testNoCloseOnCompletion_StatementOpen_afterImplicitResultSetClose() throws SQLException {
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse("Resultset should be open", rs.isClosed());
                assertFalse("Statement should be open", stmt.isClosed());
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue("Resultset should be closed (automatically closed after last result read)", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is false, will not close
     * the statement.
     */
    @Test
    public void testNoCloseOnCompletion_StatementOpen_afterExplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
            
            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     */
    @Test
    public void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        prepareTestData();
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse("Resultset should be open", rs.isClosed());
                assertFalse("Statement should be open", stmt.isClosed());
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertTrue("Resultset should be closed (automatically closed after last result read)", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is true, will close
     * the statement.
     */
    @Test
    public void testCloseOnCompletion_StatementClosed_afterExplicitResultSetClose() throws SQLException {
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            // Cast so it also works under JDBC 3.0
            FBResultSet rs = (FBResultSet)stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
            
            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if a executing a query which does not produce a resultset (eg an INSERT without generated keys) will not close the
     * statement.
     */
    @Test
    public void testCloseOnCompletion_StatementOpen_afterNonResultSetQuery() throws SQLException {
        FBStatement stmt = (FBStatement)con.createStatement();
        try {
            stmt.closeOnCompletion();
            stmt.execute("INSERT INTO test(col1) VALUES(" + DATA_ITEMS +")");
            
            assertFalse("Statement should be open", stmt.isClosed());
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests {@link org.firebirdsql.jdbc.FBStatement#executeQuery(String)} with a query that does not produce a ResultSet.
     * <p>
     * Expectation: SQLException
     * </p>
     */
    @Test
    public void testExecuteQuery_NonQuery() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_NO_RESULT_SET));

            stmt.executeQuery("INSERT INTO test(col1) VALUES(6)");
        } finally {
            stmt.close();
        }
    }

    /**
     * Test the default value for maxFieldSize property.
     * <p>
     * Expectation: maxFieldSize is 0 by default.
     * </p>
     */
    @Test
    public void testMaxFieldSize_default() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertEquals("Unexpected default value for maxFieldSize", 0, stmt.getMaxFieldSize());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test setting max field size to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    public void testSetMaxFieldSize_negativeValue() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setMaxFieldSize(-1);
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if value of maxFieldSize set is also value retrieved with get.
     */
    @Test
    public void testSetMaxFieldSize() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            final int maxFieldSize = 513;
            stmt.setMaxFieldSize(maxFieldSize);

            assertEquals("Unexpected value for maxFieldSize", maxFieldSize, stmt.getMaxFieldSize());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test default value of maxRows property.
     * <p>
     * Expected: default value of 0.
     * </p>
     */
    @Test
    public void testMaxRows_default() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertEquals("Unexpected default value for maxRows", 0, stmt.getMaxRows());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test setting max rows to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    public void testSetMaxRows_negativeValue() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setMaxRows(-1);
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if value of maxRows set is also value retrieved with get.
     */
    @Test
    public void testSetMaxRows() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            final int maxRows = 513;
            stmt.setMaxRows(maxRows);

            assertEquals("Unexpected value for maxRows", maxRows, stmt.getMaxRows());
        } finally {
            stmt.close();
        }
    }

    /**
     * Checks if the maxRows property is correctly applied to result sets of the specified type and concurrency.
     *
     * @param resultSetType Type of result set
     * @param resultSetConcurrency Concurrency of result set
     */
    private void checkMaxRows(int resultSetType, int resultSetConcurrency) throws SQLException {
        prepareTestData();
        Statement stmt = con.createStatement(resultSetType, resultSetConcurrency);
        try {
            stmt.setMaxRows(2);
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            try {
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected value for first row", 0, rs.getInt(1));
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected value for second row", 1, rs.getInt(1));
                assertFalse("Expected only two rows in ResultSet", rs.next());
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a forward only, readonly result set.
     */
    @Test
    public void testMaxRows_ForwardOnly_ReadOnly() throws SQLException {
        checkMaxRows(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a forward only, updatable result set.
     */
    @Test
    public void testMaxRows_ForwardOnly_Updatable() throws SQLException {
        checkMaxRows(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a scroll insensitive, readonly result set.
     */
    @Test
    public void testMaxRows_ScrollInsensitive_ReadOnly() throws SQLException {
        checkMaxRows(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a scroll insensitive, updatable result set.
     */
    @Test
    public void testMaxRows_ScrollInsensitive_Updatable() throws SQLException {
        checkMaxRows(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    /**
     * Test default value of queryTimeout property.
     * <p>
     * Expected: default value of 0.
     * </p>
     */
    @Test
    public void testQueryTimeout_default() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertEquals("Unexpected default value for queryTimeout", 0, stmt.getQueryTimeout());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test setting queryTimeout to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    public void testSetQueryTimeout_negativeValue() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setQueryTimeout(-1);
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if value of queryTimeout set is also value retrieved with get.
     */
    @Test
    public void testSetQueryTimeout() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            final int queryTimeout = 513;
            stmt.setQueryTimeout(queryTimeout);

            assertEquals("Unexpected value for queryTimeout", queryTimeout, stmt.getQueryTimeout());
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if disabling escape processing works.
     * <p>
     * Test uses a query containing a JDBC escape, expected exception is a syntax error.
     * </p>
     */
    @Test
    public void testEscapeProcessingDisabled() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            final String testQuery = "SELECT {fn CURDATE} FROM RDB$DATABASE";
            // First test validity of query with escape processing enabled (default)
            ResultSet rs = stmt.executeQuery(testQuery);
            rs.close();

            stmt.setEscapeProcessing(false);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals("42S22"));
            expectedException.expectMessage(containsString("Column unknown; {FN"));

            stmt.executeQuery(testQuery);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) of a simple select is non-empty
     */
    @Test
    public void testGetLastExecutionPlan_select() throws SQLException {
        FirebirdStatement stmt = (FirebirdStatement) con.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            rs.close();

            String plan = stmt.getLastExecutionPlan();
            assertThat("Expected non-empty plan", plan, not(isEmptyOrNullString()));
        } finally {
            stmt.close();
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) when no statement has been executed yet.
     * <p>
     * Expected: exception
     * </p>
     */
    @Test
    public void testGetLastExecutionPlan_noStatement() throws SQLException {
        FirebirdStatement stmt = (FirebirdStatement) con.createStatement();
        try {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("No statement was executed, plan cannot be obtained."))
            ));

            stmt.getLastExecutionPlan();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} returns the expected {@link java.sql.Connection}.
     */
    @Test
    public void testGetConnection() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertSame("Unexpected result for getConnection()", con, stmt.getConnection());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} throws an exception when called on a closed connection.
     */
    @Test
    public void testGetConnection_closedStatement() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.getConnection();
        } finally {
            stmt.close();
        }
    }

    /**
     * Test the batch update facility with insert statements.
     */
    @Test
    public void testBatch_Insert() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            for (int item = 1; item <= DATA_ITEMS; item++) {
                stmt.addBatch(String.format("INSERT INTO test(col1) VALUES(%d)", item));
            }
            int[] updateCounts = stmt.executeBatch();

            int[] expectedUpdateCounts = new int[DATA_ITEMS];
            Arrays.fill(expectedUpdateCounts, 1);
            assertArrayEquals(expectedUpdateCounts, updateCounts);

            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            try {
                int expectedItem = 0;
                while (rs.next()) {
                    expectedItem++;
                    int actualItem = rs.getInt(1);
                    assertEquals("Unexpected data item in SELECT", expectedItem, actualItem);
                }
                assertEquals("Unexpected data item in SELECT", DATA_ITEMS, expectedItem);
            } finally {
                rs.close();
            }
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if the default value of {@link FBStatement#getFetchSize()} is <code>0</code>.
     */
    @Test
    public void testGetFetchSize_default() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertEquals("Default getFetchSize value should be 0", 0, stmt.getFetchSize());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#getFetchSize()} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    public void testGetFetchSize_statementClosed() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.setFetchSize(10);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} with a non-zero value.
     * <p>
     * Expected: value retrieved with {@link org.firebirdsql.jdbc.FBStatement#getFetchSize()} is same as value set.
     * </p>
     */
    @Test
    public void testSetFetchSize() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            final int testSize = 132;
            stmt.setFetchSize(testSize);

            assertEquals("getFetchSize value should be equal to value set", testSize, stmt.getFetchSize());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    public void testSetFetchSize_statementClosed() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.setFetchSize(10);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} with a negative value.
     * <p>
     * Expected: SQLException
     * </p>
     */
    @Test
    public void testSetFetchSize_negativeValue() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_ARG_VALUE))
            ));

            stmt.setFetchSize(-1);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchDirection(int)} with {@link ResultSet#FETCH_FORWARD}.
     * <p>
     * No exception, {@link FBStatement#getFetchDirection()} returns {@link ResultSet#FETCH_FORWARD}
     * </p>
     */
    @Test
    public void testSetFetchDirection_Forward() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            stmt.setFetchDirection(ResultSet.FETCH_FORWARD);

            assertEquals("Unexpected value for fetchDirection", ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchDirection(int)} with {@link ResultSet#FETCH_REVERSE}.
     * <p>
     * Exception: SQLFeatureNotSupportedException
     * </p>
     */
    @Test
    public void testSetFetchDirection_Reverse() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLFeatureNotSupportedException.class);

            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchDirection(int)} with {@link ResultSet#FETCH_UNKNOWN}.
     * <p>
     * Exception: SQLFeatureNotSupportedException
     * </p>
     */
    @Test
    public void testSetFetchDirection_Unknown() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(SQLFeatureNotSupportedException.class);

            stmt.setFetchDirection(ResultSet.FETCH_UNKNOWN);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchDirection(int)} with an invalid value.
     * <p>
     * Exception: SQLException
     * </p>
     */
    @Test
    public void testSetFetchDirection_InvalidValue() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    not(isA(SQLFeatureNotSupportedException.class)),
                    sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_ARG_VALUE))
            ));

            //noinspection MagicConstant
            stmt.setFetchDirection(-1);
        } finally {
            stmt.close();
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchDirection(int)} with {@link ResultSet#FETCH_FORWARD}
     * on a closed statement.
     * <p>
     * Exception: SQLException statement closed.
     * </p>
     */
    @Test
    public void testSetFetchDirection_statementClosed() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        expectedException.expect(fbStatementClosedException());

        stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    }

    /**
     * Tests if default value of {@link FBStatement#isPoolable()} is <code>false</code>.
     */
    @Test
    public void testIsPoolable_default() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertFalse("Unexpected value for isPoolable()", stmt.isPoolable());
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests {@link FBStatement#isPoolable()} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    public void testIsPoolable_statementClosed() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        expectedException.expect(fbStatementClosedException());

        stmt.isPoolable();
    }

    /**
     * Tests if calls to {@link org.firebirdsql.jdbc.FBStatement#setPoolable(boolean)} are ignored.
     */
    @Test
    public void testSetPoolable_ignored() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            stmt.setPoolable(true);

            assertFalse("Expected isPoolable() to remain false", stmt.isPoolable());
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests {@link org.firebirdsql.jdbc.FBStatement#setPoolable(boolean)} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    public void testSetPoolable_statementClosed() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        expectedException.expect(fbStatementClosedException());

        stmt.setPoolable(true);
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with {@link org.firebirdsql.jdbc.FirebirdStatement}
     * returns <code>true</code>.
     */
    @Test
    public void testIsWrapperFor_FirebirdStatement() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertTrue("Expected to be wrapper for FirebirdStatement", stmt.isWrapperFor(FirebirdStatement.class));
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with {@link ResultSet}
     * returns <code>false</code>.
     */
    @Test
    public void testIsWrapperFor_ResultSet() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            assertFalse("Expected not to be wrapper for ResultSet", stmt.isWrapperFor(ResultSet.class));
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with {@link org.firebirdsql.jdbc.FirebirdStatement}
     * successfully unwraps.
     */
    @Test
    public void testUnwrap_FirebirdStatement() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            FirebirdStatement firebirdStatement = stmt.unwrap(FirebirdStatement.class);

            assertThat("Unexpected result for unwrap to FirebirdStatement", firebirdStatement, allOf(
                    notNullValue(),
                    sameInstance(stmt)
            ));
        } finally {
            stmt.close();
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with {@link ResultSet}
     * throws an Exception.
     */
    @Test
    public void testUnwrap_ResultSet() throws SQLException {
        Statement stmt = con.createStatement();
        try {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("Unable to unwrap to class java.sql.ResultSet"))
            ));

            stmt.unwrap(ResultSet.class);
        } finally {
            stmt.close();
        }
    }

    private void prepareTestData() throws SQLException {
        PreparedStatement pstmt = con.prepareStatement(INSERT_DATA);
        try {
            for (int i = 0; i < DATA_ITEMS; i++) {
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            }
        } finally {
            pstmt.close();
        }
    }
}
