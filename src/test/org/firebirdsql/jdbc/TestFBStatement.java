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

import java.sql.*;
import java.util.Arrays;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.DdlHelper.*;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.JdbcResourceHelper.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

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
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            stmt.execute(SELECT_DATA);
            ResultSet rs = stmt.getResultSet();
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
        }
    }
    
    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is false, will not close
     * the statement.
     */
    @Test
    public void testNoCloseOnCompletion_StatementOpen_afterExplicitResultSetClose() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            stmt.execute(SELECT_DATA);
            ResultSet rs = stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());

            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());
        }
    }
    
    /**
     * Test if an implicit close (by fully reading the resultset) while closeOnCompletion is true, will close
     * the statement.
     */
    @Test
    public void testCloseOnCompletion_StatementClosed_afterImplicitResultSetClose() throws SQLException {
        prepareTestData();
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            ResultSet rs = stmt.getResultSet();
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
        }
    }
    
    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is true, will close
     * the statement.
     */
    @Test
    public void testCloseOnCompletion_StatementClosed_afterExplicitResultSetClose() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            ResultSet rs = stmt.getResultSet();
            assertFalse("Resultset should be open", rs.isClosed());
            assertFalse("Statement should be open", stmt.isClosed());

            rs.close();

            assertTrue("Resultset should be closed", rs.isClosed());
            assertTrue("Statement should be closed", stmt.isClosed());
        }
    }
    
    /**
     * Test if a executing a query which does not produce a resultset (eg an INSERT without generated keys) will not close the
     * statement.
     */
    @Test
    public void testCloseOnCompletion_StatementOpen_afterNonResultSetQuery() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            stmt.closeOnCompletion();
            stmt.execute("INSERT INTO test(col1) VALUES(" + DATA_ITEMS + ")");

            assertFalse("Statement should be open", stmt.isClosed());
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
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_NO_RESULT_SET));

            stmt.executeQuery("INSERT INTO test(col1) VALUES(6)");
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
        try (Statement stmt = con.createStatement()) {
            assertEquals("Unexpected default value for maxFieldSize", 0, stmt.getMaxFieldSize());
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
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setMaxFieldSize(-1);
        }
    }

    /**
     * Tests if value of maxFieldSize set is also value retrieved with get.
     */
    @Test
    public void testSetMaxFieldSize() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int maxFieldSize = 513;
            stmt.setMaxFieldSize(maxFieldSize);

            assertEquals("Unexpected value for maxFieldSize", maxFieldSize, stmt.getMaxFieldSize());
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
        try (Statement stmt = con.createStatement()) {
            assertEquals("Unexpected default value for maxRows", 0, stmt.getMaxRows());
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
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setMaxRows(-1);
        }
    }

    /**
     * Tests if value of maxRows set is also value retrieved with get.
     */
    @Test
    public void testSetMaxRows() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int maxRows = 513;
            stmt.setMaxRows(maxRows);

            assertEquals("Unexpected value for maxRows", maxRows, stmt.getMaxRows());
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
        try (Statement stmt = con.createStatement(resultSetType, resultSetConcurrency)) {
            stmt.setMaxRows(2);
            try (ResultSet rs = stmt.executeQuery(SELECT_DATA)) {
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected value for first row", 0, rs.getInt(1));
                assertTrue("Expected a row", rs.next());
                assertEquals("Unexpected value for second row", 1, rs.getInt(1));
                assertFalse("Expected only two rows in ResultSet", rs.next());
            }
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
        try (Statement stmt = con.createStatement()) {
            assertEquals("Unexpected default value for queryTimeout", 0, stmt.getQueryTimeout());
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
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE));

            stmt.setQueryTimeout(-1);
        }
    }

    /**
     * Tests if value of queryTimeout set is also value retrieved with get.
     */
    @Test
    public void testSetQueryTimeout() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int queryTimeout = 513;
            stmt.setQueryTimeout(queryTimeout);

            assertEquals("Unexpected value for queryTimeout", queryTimeout, stmt.getQueryTimeout());
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
        try (Statement stmt = con.createStatement()) {
            final String testQuery = "SELECT {fn CURDATE} FROM RDB$DATABASE";
            // First test validity of query with escape processing enabled (default)
            ResultSet rs = stmt.executeQuery(testQuery);
            rs.close();

            stmt.setEscapeProcessing(false);

            expectedException.expect(SQLException.class);
            expectedException.expect(sqlStateEquals("42S22"));
            expectedException.expectMessage(containsString("Column unknown; {FN"));

            stmt.executeQuery(testQuery);
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) of a simple select is non-empty
     */
    @Test
    public void testGetLastExecutionPlan_select() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            rs.close();

            String plan = stmt.getLastExecutionPlan();
            assertThat("Expected non-empty plan", plan, not(isEmptyOrNullString()));
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
        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("No statement was executed, plan cannot be obtained."))
            ));

            stmt.getLastExecutionPlan();
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) of a simple select is non-empty
     */
    @Test
    public void testGetLastExplainedExecutionPlan_select() throws SQLException {
        assumeTrue("Test requires explained execution plan support",
                getDefaultSupportInfo().supportsExplainedExecutionPlan());
        
        executeCreateTable(con, CREATE_TABLE);

        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            rs.close();

            String plan = stmt.getLastExplainedExecutionPlan();
            assertThat("Expected non-empty detailed plan", plan, not(isEmptyOrNullString()));
        }
    }

    /**
     * Test retrieval of detailed execution plan ({@link FBStatement#getLastExplainedExecutionPlan()})
     * when no statement has been executed yet.
     * <p>
     * Expected: exception
     * </p>
     */
    @Test
    public void testGetLastExplainedExecutionPlan_noStatement() throws SQLException {
        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("No statement was executed, detailed plan cannot be obtained."))
            ));

            stmt.getLastExplainedExecutionPlan();
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} returns the expected {@link java.sql.Connection}.
     */
    @Test
    public void testGetConnection() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertSame("Unexpected result for getConnection()", con, stmt.getConnection());
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} throws an exception when called on a closed connection.
     */
    @Test
    public void testGetConnection_closedStatement() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.getConnection();
        }
    }

    /**
     * Test the batch update facility with insert statements.
     */
    @Test
    public void testBatch_Insert() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            for (int item = 1; item <= DATA_ITEMS; item++) {
                stmt.addBatch(String.format("INSERT INTO test(col1) VALUES(%d)", item));
            }
            int[] updateCounts = stmt.executeBatch();

            int[] expectedUpdateCounts = new int[DATA_ITEMS];
            Arrays.fill(expectedUpdateCounts, 1);
            assertArrayEquals(expectedUpdateCounts, updateCounts);

            try (ResultSet rs = stmt.executeQuery(SELECT_DATA)) {
                int expectedItem = 0;
                while (rs.next()) {
                    expectedItem++;
                    int actualItem = rs.getInt(1);
                    assertEquals("Unexpected data item in SELECT", expectedItem, actualItem);
                }
                assertEquals("Unexpected data item in SELECT", DATA_ITEMS, expectedItem);
            }
        }
    }

    /**
     * Tests if the default value of {@link FBStatement#getFetchSize()} is <code>0</code>.
     */
    @Test
    public void testGetFetchSize_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals("Default getFetchSize value should be 0", 0, stmt.getFetchSize());
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
        try (Statement stmt = con.createStatement()) {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.setFetchSize(10);
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
        try (Statement stmt = con.createStatement()) {
            final int testSize = 132;
            stmt.setFetchSize(testSize);

            assertEquals("getFetchSize value should be equal to value set", testSize, stmt.getFetchSize());
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
        try (Statement stmt = con.createStatement()) {
            stmt.close();
            expectedException.expect(fbStatementClosedException());

            stmt.setFetchSize(10);
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
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE))
            ));

            stmt.setFetchSize(-1);
        }
    }

    @Test
    public void testGetFetchDirection_DefaultForward() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals("Unexpected value for fetchDirection", ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_Forward() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.setFetchDirection(ResultSet.FETCH_FORWARD);

            assertEquals("Unexpected value for fetchDirection", ResultSet.FETCH_FORWARD, stmt.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_Reverse() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.setFetchDirection(ResultSet.FETCH_REVERSE);

            assertEquals("Unexpected value for fetchDirection", ResultSet.FETCH_REVERSE, stmt.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_Unknown() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.setFetchDirection(ResultSet.FETCH_UNKNOWN);

            assertEquals("Unexpected value for fetchDirection", ResultSet.FETCH_UNKNOWN, stmt.getFetchDirection());
        }
    }

    @Test
    public void testSetFetchDirection_InvalidValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    not(isA(SQLFeatureNotSupportedException.class)),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_invalidFetchDirection, "-1"),
                    sqlState(equalTo("HY106"))
            ));

            //noinspection MagicConstant
            stmt.setFetchDirection(-1);
        }
    }

    @Test
    public void testSetFetchDirection_statementClosed_throwsException() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        expectedException.expect(fbStatementClosedException());

        stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
    }

    @Test
    public void testGetFetchDirection_statementClosed_throwsException() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        expectedException.expect(fbStatementClosedException());

        stmt.getFetchDirection();
    }

    /**
     * Tests if default value of {@link FBStatement#isPoolable()} is <code>false</code>.
     */
    @Test
    public void testIsPoolable_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertFalse("Unexpected value for isPoolable()", stmt.isPoolable());
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
        try (Statement stmt = con.createStatement()) {
            stmt.setPoolable(true);

            assertFalse("Expected isPoolable() to remain false", stmt.isPoolable());
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
        try (Statement stmt = con.createStatement()) {
            assertTrue("Expected to be wrapper for FirebirdStatement", stmt.isWrapperFor(FirebirdStatement.class));
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with {@link ResultSet}
     * returns <code>false</code>.
     */
    @Test
    public void testIsWrapperFor_ResultSet() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertFalse("Expected not to be wrapper for ResultSet", stmt.isWrapperFor(ResultSet.class));
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with {@link org.firebirdsql.jdbc.FirebirdStatement}
     * successfully unwraps.
     */
    @Test
    public void testUnwrap_FirebirdStatement() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            FirebirdStatement firebirdStatement = stmt.unwrap(FirebirdStatement.class);

            assertThat("Unexpected result for unwrap to FirebirdStatement", firebirdStatement, allOf(
                    notNullValue(),
                    sameInstance(stmt)
            ));
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with {@link ResultSet}
     * throws an Exception.
     */
    @Test
    public void testUnwrap_ResultSet() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("Unable to unwrap to class java.sql.ResultSet"))
            ));

            stmt.unwrap(ResultSet.class);
        }
    }

    /**
     * Tests if Firebird 1.5+ custom exception messages work.
     */
    @Test
    public void testCustomExceptionMessage() throws Exception {
        assumeTrue("Test requires custom exception messages", supportInfoFor(con).supportsCustomExceptionMessages());

        //@formatter:off
        executeDDL(con, "CREATE EXCEPTION simple_exception 'Standard message'");
        executeDDL(con,
                "CREATE PROCEDURE testexception " +
                "AS " +
                "BEGIN " +
                "  EXCEPTION simple_exception 'Custom message';" +
                "END");
        //@formatter:on

        try (Statement stmt = con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("; Custom message; "))
            ));

            stmt.execute("EXECUTE PROCEDURE testexception");
        }
    }

    /**
     * Tests if Firebird 3 parametrized exceptions are correctly rendered.
     */
    @Test
    public void testParametrizedExceptions() throws Exception {
        assumeTrue("Test requires parametrized exceptions", supportInfoFor(con).supportsParametrizedExceptions());
        executeDDL(con, "CREATE EXCEPTION two_param_exception 'Param 1 ''@1'', Param 2 ''@2'''");

        try (Statement stmt = con.createStatement()) {
            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("; Param 1 'value_1', Param 2 'value2'; "))
            ));

            //@formatter:off
            stmt.execute(
                "EXECUTE BLOCK AS " +
                "BEGIN " +
                "  EXCEPTION two_param_exception USING ('value_1', 'value2'); " +
                "END"
            );
            //@formatter:on
        }
    }

    @Test
    public void testRetrievingUpdateCountAndResultSet() throws Exception {
        assumeTrue("Test requires UPDATE .. RETURNING .. support", supportInfoFor(con).supportsUpdateReturning());
        executeDDL(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            boolean isResultSet = stmt.execute("INSERT INTO test(col1) VALUES(5) RETURNING col1");

            assertTrue("Expected first result to be a result set", isResultSet);
            ResultSet rs = stmt.getResultSet();
            assertNotNull("Result set should not be null", rs);
            assertTrue("Expected a row in the result set", rs.next());
            assertEquals("Unexpected value in result set", 5, rs.getInt(1));
            assertFalse("Expected only one row", rs.next());
            assertEquals("Update count should be -1 before first call to getMoreResults", -1, stmt.getUpdateCount());

            assertFalse("Next result should not be a result set", stmt.getMoreResults());
            assertNull("Expected null result set", stmt.getResultSet());
            assertEquals("Update count should be 1 after first call to getMoreResults", 1, stmt.getUpdateCount());

            assertFalse("Next result should not be a result set", stmt.getMoreResults());
            assertNull("Expected null result set", stmt.getResultSet());
            assertEquals("Update count should be -1 after second call to getMoreResults", -1, stmt.getUpdateCount());
        }
    }

    @Test
    public void testEnquoteLiteral() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertEquals("No quotes", "'no quotes'", stmt.enquoteLiteral("no quotes"));
            assertEquals("With quotes", "'with''quotes'", stmt.enquoteLiteral("with'quotes"));
        }
    }

    @Test
    public void testIsSimpleIdentifier() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertTrue("Simple$Identifier_", stmt.isSimpleIdentifier("Simple$Identifier_"));
            assertFalse("1Simple$Identifier_", stmt.isSimpleIdentifier("1Simple$Identifier_"));
            assertFalse("", stmt.isSimpleIdentifier(""));
            assertTrue("A234567890123456789012345678901", stmt.isSimpleIdentifier("A234567890123456789012345678901"));
            FirebirdSupportInfo supportInfo = getDefaultSupportInfo();

            String maxLengthIdentifier = generateIdentifier(supportInfo.maxIdentifierLengthCharacters());
            assertTrue(maxLengthIdentifier, stmt.isSimpleIdentifier(maxLengthIdentifier));

            String tooLongIdentifier = generateIdentifier(supportInfo.maxIdentifierLengthCharacters() + 1);
            assertFalse(tooLongIdentifier, stmt.isSimpleIdentifier(tooLongIdentifier));
        }
    }

    @Test
    public void testEnquoteIdentifier() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertEquals("simple, alwaysQuote:false",
                    "simple$identifier_", stmt.enquoteIdentifier("simple$identifier_", false));
            assertEquals("simple, alwaysQuote:true",
                    "\"simple$identifier_\"", stmt.enquoteIdentifier("simple$identifier_", true));
            assertEquals("already quoted", "\"already quoted\"", stmt.enquoteIdentifier("\"already quoted\"", false));
            assertEquals("needs quotes", "\"has space\"", stmt.enquoteIdentifier("has space", false));
            assertEquals("needs quotes", "\"has\"\"quote\"", stmt.enquoteIdentifier("has\"quote", false));
        }
    }

    @Test
    public void verifySingletonStatementWithException() throws Exception {
// @formatting:off
        executeDDL(con, "create procedure singleton_error returns (intresult int) as "
                + "begin "
                + "  execute statement 'select cast(''x'' as integer) from rdb$database' into intresult;"
                + "end");
// @formatting:on
        try (Statement stmt = con.createStatement()) {
            try {
                stmt.execute("execute procedure singleton_error");
            } catch (SQLException e) {
                // expected
            }

            stmt.getMoreResults();
            assertEquals(0, stmt.getUpdateCount());
        }
    }

    @Test
    public void verifyMultipleResultsWithUpdateCount_autocommit() throws Exception {
        assumeTrue("Test requires execute block support", getDefaultSupportInfo().supportsExecuteBlock());
        executeDDL(con, CREATE_TABLE);
        try (Statement stmt = con.createStatement()) {
            boolean hasResultSet = stmt.execute(
// @formatter:off
                    "execute block returns (intresult integer) as "
                    + "BEGIN "
                    + "  insert into test (col1) VALUES (1) returning col1 into intresult;"
                    + "  suspend;"
                    + "  insert into test (col1) VALUES (2) returning col1 into intresult;"
                    + "  suspend;"
                    + "end"
// @formatter:on
            );
            assertTrue(hasResultSet);

            ResultSet rs = stmt.getResultSet();
            assertNotNull(rs);
            assertTrue("first result", rs.next());
            assertEquals(1, rs.getInt(1));
            assertTrue("second result", rs.next());
            assertEquals(2, rs.getInt(1));
            assertFalse("no more results", rs.next());

            assertFalse(stmt.getMoreResults());
            assertEquals(2, stmt.getUpdateCount());
        }
    }

    @Test
    public void verifyMultipleResultsWithUpdateCount_noAutocommit() throws Exception {
        assumeTrue("Test requires execute block support", getDefaultSupportInfo().supportsExecuteBlock());
        executeDDL(con, CREATE_TABLE);
        con.setAutoCommit(false);
        try (Statement stmt = con.createStatement()) {
            boolean hasResultSet = stmt.execute(
// @formatter:off
                    "execute block returns (intresult integer) as "
                    + "BEGIN "
                    + "  insert into test (col1) VALUES (1) returning col1 into intresult;"
                    + "  suspend;"
                    + "  insert into test (col1) VALUES (2) returning col1 into intresult;"
                    + "  suspend;"
                    + "end"
// @formatter:on
            );
            assertTrue(hasResultSet);

            ResultSet rs = stmt.getResultSet();
            assertNotNull(rs);
            assertTrue("first result", rs.next());
            assertEquals(1, rs.getInt(1));
            assertTrue("second result", rs.next());
            assertEquals(2, rs.getInt(1));
            assertFalse("no more results", rs.next());

            assertFalse(stmt.getMoreResults());
            assertEquals(2, stmt.getUpdateCount());

            con.commit();
        }
    }

    private void prepareTestData() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (PreparedStatement pstmt = con.prepareStatement(INSERT_DATA)) {
            for (int i = 0; i < DATA_ITEMS; i++) {
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            }
        }
    }

    private static String generateIdentifier(final int length) {
        StringBuilder sb = new StringBuilder(length);
        int tempLength = length;
        while (tempLength-- > 0) {
            sb.append('a');
        }
        assert sb.length() == length;
        return sb.toString();
    }
}
