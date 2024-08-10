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

import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Stream;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.common.DdlHelper.*;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertResultSetClosed;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertResultSetOpen;
import static org.firebirdsql.common.assertions.SQLExceptionAssertions.assertThrowsFbStatementClosed;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.IsEmptyString.emptyOrNullString;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBStatement}.
 */
class FBStatementTest {

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    private FBConnection con;

    private static final int DATA_ITEMS = 5;
    private static final String CREATE_TABLE = "CREATE TABLE test ( col1 INTEGER )";
    private static final String INSERT_DATA = "INSERT INTO test(col1) VALUES(?)";
    private static final String SELECT_DATA = "SELECT col1 FROM test ORDER BY col1";

    @BeforeEach
    void setUp() throws Exception {
        con = getConnectionViaDriverManager().unwrap(FBConnection.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        con.close();
    }

    /**
     * Closing a statement twice should not result in an Exception.
     */
    @Test
    void testDoubleClose() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        assertDoesNotThrow(stmt::close);
    }

    /**
     * Test of initial value of isCloseOnCompletion, expected: false.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    void testIsCloseOnCompletion_initial() throws SQLException {
        Statement stmt = con.createStatement();
        assertFalse(stmt.isCloseOnCompletion(),
                "Initial value of isCloseOnCompletion expected to be false");
    }

    /**
     * Test of value of isCloseOnCompletion after closeOnCompletion call,
     * expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    void testIsCloseOnCompletion_afterCloseOnCompletion() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.closeOnCompletion();
        assertTrue(stmt.isCloseOnCompletion(),
                "Value of isCloseOnCompletion after closeOnCompletion expected to be true");
    }

    /**
     * Test of value of isCloseOnCompletion after multiple calls to
     * closeOnCompletion call, expected: true.
     * <p>
     * JDBC 4.1 feature.
     * </p>
     */
    @Test
    void testIsCloseOnCompletion_multipleCloseOnCompletion() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.closeOnCompletion();
        stmt.closeOnCompletion();
        assertTrue(stmt.isCloseOnCompletion(),
                "Value of isCloseOnCompletion after closeOnCompletion expected to be true");
    }

    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is false, will not close
     * the statement.
     */
    @Test
    void testNoCloseOnCompletion_StatementOpen_afterExplicitResultSetClose() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            stmt.execute(SELECT_DATA);
            ResultSet rs = stmt.getResultSet();
            assertFalse(rs.isClosed(), "Result set should be open");
            assertFalse(stmt.isClosed(), "Statement should be open");

            rs.close();

            assertTrue(rs.isClosed(), "Result set should be closed");
            assertFalse(stmt.isClosed(), "Statement should be open");
        }
    }

    /**
     * Test if an explicit close (by calling close()) while closeOnCompletion is true, will close
     * the statement.
     */
    @Test
    void testCloseOnCompletion_StatementClosed_afterExplicitResultSetClose() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            stmt.execute(SELECT_DATA);
            stmt.closeOnCompletion();
            ResultSet rs = stmt.getResultSet();
            assertFalse(rs.isClosed(), "Result set should be open");
            assertFalse(stmt.isClosed(), "Statement should be open");

            rs.close();

            assertTrue(rs.isClosed(), "Result set should be closed");
            assertTrue(stmt.isClosed(), "Statement should be closed");
        }
    }

    /**
     * Test if executing a query which does not produce a result set (e.g. an INSERT without generated keys) will not
     * close the statement.
     */
    @Test
    void testCloseOnCompletion_StatementOpen_afterNonResultSetQuery() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            stmt.closeOnCompletion();
            stmt.execute("INSERT INTO test(col1) VALUES(" + DATA_ITEMS + ")");

            assertFalse(stmt.isClosed(), "Statement should be open");
        }
    }

    /**
     * Tests {@link org.firebirdsql.jdbc.FBStatement#executeQuery(String)} with a query that does not produce a
     * ResultSet.
     * <p>
     * Expectation: SQLException
     * </p>
     */
    @Test
    void testExecuteQuery_NonQuery() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class,
                    () -> stmt.executeQuery("INSERT INTO test(col1) VALUES(6)"));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_NO_RESULT_SET));
        }
    }

    /**
     * Test the default value for maxFieldSize property.
     * <p>
     * Expectation: maxFieldSize is 0 by default.
     * </p>
     */
    @Test
    void testMaxFieldSize_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals(0, stmt.getMaxFieldSize(), "Unexpected default value for maxFieldSize");
        }
    }

    /**
     * Test setting max field size to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    void testSetMaxFieldSize_negativeValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () -> stmt.setMaxFieldSize(-1));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH));
        }
    }

    /**
     * Tests if value of maxFieldSize set is also value retrieved with get.
     */
    @Test
    void testSetMaxFieldSize() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int maxFieldSize = 513;
            stmt.setMaxFieldSize(maxFieldSize);

            assertEquals(maxFieldSize, stmt.getMaxFieldSize(), "Unexpected value for maxFieldSize");
        }
    }

    /**
     * Test default value of maxRows property.
     * <p>
     * Expected: default value of 0.
     * </p>
     */
    @Test
    void testMaxRows_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals(0, stmt.getMaxRows(), "Unexpected default value for maxRows");
        }
    }

    /**
     * Test setting max rows to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    void testSetMaxRows_negativeValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () -> stmt.setMaxRows(-1));
            assertThat(exception, sqlStateEquals(SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE));
        }
    }

    /**
     * Tests if value of maxRows set is also value retrieved with get.
     */
    @Test
    void testSetMaxRows() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int maxRows = 513;
            stmt.setMaxRows(maxRows);

            assertEquals(maxRows, stmt.getMaxRows(), "Unexpected value for maxRows");
        }
    }

    /**
     * Checks if the maxRows property is correctly applied to result sets of the specified type and concurrency.
     *
     * @param resultSetType
     *         Type of result set
     * @param resultSetConcurrency
     *         Concurrency of result set
     */
    private void checkMaxRows(int resultSetType, int resultSetConcurrency) throws SQLException {
        prepareTestData();
        try (Statement stmt = con.createStatement(resultSetType, resultSetConcurrency)) {
            stmt.setMaxRows(2);
            try (ResultSet rs = stmt.executeQuery(SELECT_DATA)) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(0, rs.getInt(1), "Unexpected value for first row");
                assertTrue(rs.next(), "Expected a row");
                assertEquals(1, rs.getInt(1), "Unexpected value for second row");
                assertFalse(rs.next(), "Expected only two rows in ResultSet");
            }
        }
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a forward only, readonly result set.
     */
    @Test
    void testMaxRows_ForwardOnly_ReadOnly() throws SQLException {
        checkMaxRows(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a forward only, updatable result set.
     */
    @Test
    void testMaxRows_ForwardOnly_Updatable() throws SQLException {
        checkMaxRows(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a scroll insensitive, readonly result
     * set.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testMaxRows_ScrollInsensitive_ReadOnly(String scrollableCursorPropertyValue) throws SQLException {
        con.close();
        con = createConnection(scrollableCursorPropertyValue);
        checkMaxRows(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Tests if the maxRows property is correctly applied when retrieving rows for a scroll insensitive, updatable
     * result set.
     */
    @ParameterizedTest
    @MethodSource("scrollableCursorPropertyValues")
    void testMaxRows_ScrollInsensitive_Updatable(String scrollableCursorPropertyValue) throws SQLException {
        con.close();
        con = createConnection(scrollableCursorPropertyValue);
        checkMaxRows(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
    }

    /**
     * Test default value of queryTimeout property.
     * <p>
     * Expected: default value of 0.
     * </p>
     */
    @Test
    void testQueryTimeout_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals(0, stmt.getQueryTimeout(), "Unexpected default value for queryTimeout");
        }
    }

    /**
     * Test setting queryTimeout to a negative value.
     * <p>
     * Expected: SQLException.
     * </p>
     */
    @Test
    void testSetQueryTimeout_negativeValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            SQLNonTransientException exception = assertThrows(SQLNonTransientException.class,
                    () -> stmt.setQueryTimeout(-1));
            assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_invalidTimeout));
        }
    }

    /**
     * Tests if value of queryTimeout set is also value retrieved with get.
     */
    @Test
    void testSetQueryTimeout() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int queryTimeout = 513;
            stmt.setQueryTimeout(queryTimeout);

            assertEquals(queryTimeout, stmt.getQueryTimeout(), "Unexpected value for queryTimeout");
        }
    }

    /**
     * Tests if disabling escape processing works.
     * <p>
     * Test uses a query containing a JDBC escape, expected exception is a syntax error.
     * </p>
     */
    @Test
    void testEscapeProcessingDisabled() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final String testQuery = "SELECT {fn CURDATE} FROM RDB$DATABASE";
            // First test validity of query with escape processing enabled (default)
            ResultSet rs = stmt.executeQuery(testQuery);
            rs.close();

            stmt.setEscapeProcessing(false);

            SQLException exception = assertThrows(SQLException.class, () -> stmt.executeQuery(testQuery));
            assertThat(exception, allOf(
                    message(containsString("Column unknown; {FN")),
                    sqlStateEquals("42S22")));
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) of a simple select is non-empty
     */
    @Test
    void testGetLastExecutionPlan_select() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            rs.close();

            String plan = stmt.getLastExecutionPlan();
            assertThat("Expected non-empty plan", plan, not(emptyOrNullString()));
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) when no statement has been executed
     * yet.
     * <p>
     * Expected: exception
     * </p>
     */
    @Test
    void testGetLastExecutionPlan_noStatement() throws SQLException {
        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, stmt::getLastExecutionPlan);
            assertThat(exception, message(equalTo("No statement was executed, plan cannot be obtained")));
        }
    }

    /**
     * Test retrieval of execution plan ({@link FBStatement#getLastExecutionPlan()}) of a simple select is non-empty
     */
    @Test
    void testGetLastExplainedExecutionPlan_select() throws SQLException {
        assumeTrue(getDefaultSupportInfo().supportsExplainedExecutionPlan(),
                "Test requires explained execution plan support");

        executeCreateTable(con, CREATE_TABLE);

        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            ResultSet rs = stmt.executeQuery(SELECT_DATA);
            rs.close();

            String plan = stmt.getLastExplainedExecutionPlan();
            assertThat("Expected non-empty detailed plan", plan, not(emptyOrNullString()));
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
    void testGetLastExplainedExecutionPlan_noStatement() throws SQLException {
        try (FirebirdStatement stmt = (FirebirdStatement) con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, stmt::getLastExplainedExecutionPlan);
            assertThat(exception, message(equalTo("No statement was executed, plan cannot be obtained")));
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} returns the expected {@link java.sql.Connection}.
     */
    @Test
    void testGetConnection() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertSame(con, stmt.getConnection(), "Unexpected result for getConnection()");
        }
    }

    /**
     * Test that {@link FBStatement#getConnection()} throws an exception when called on a closed connection.
     */
    @Test
    void testGetConnection_closedStatement() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(stmt::getConnection);
        }
    }

    /**
     * Test the batch update facility with insert statements.
     */
    @Test
    void testBatch_Insert() throws SQLException {
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
                    assertEquals(expectedItem, actualItem, "Unexpected data item in SELECT");
                }
                assertEquals(DATA_ITEMS, expectedItem, "Unexpected data item in SELECT");
            }
        }
    }

    /**
     * Tests if the default value of {@link FBStatement#getFetchSize()} is {@code 0}.
     */
    @Test
    void testGetFetchSize_default() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals(0, stmt.getFetchSize(), "Default getFetchSize value should be 0");
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#getFetchSize()} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    void testGetFetchSize_statementClosed() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(stmt::getFetchSize);
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} with a non-zero value.
     * <p>
     * Expected: value retrieved with {@link org.firebirdsql.jdbc.FBStatement#getFetchSize()} is same as value set.
     * </p>
     */
    @Test
    void testSetFetchSize() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            final int testSize = 132;
            stmt.setFetchSize(testSize);

            assertEquals(testSize, stmt.getFetchSize(), "getFetchSize value should be equal to value set");
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    void testSetFetchSize_statementClosed() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(() -> stmt.setFetchSize(10));
        }
    }

    /**
     * Test calling {@link org.firebirdsql.jdbc.FBStatement#setFetchSize(int)} with a negative value.
     * <p>
     * Expected: SQLException
     * </p>
     */
    @Test
    void testSetFetchSize_negativeValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () -> stmt.setFetchSize(-1));
            assertThat(exception, sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE)));
        }
    }

    @Test
    void testGetFetchDirection_DefaultForward() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertEquals(ResultSet.FETCH_FORWARD, stmt.getFetchDirection(), "Unexpected value for fetchDirection");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { ResultSet.FETCH_FORWARD, ResultSet.FETCH_REVERSE, ResultSet.FETCH_REVERSE })
    void testSetFetchDirection_validValue(int fetchDirection) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.setFetchDirection(fetchDirection);

            assertEquals(fetchDirection, stmt.getFetchDirection(), "Unexpected value for fetchDirection");
        }
    }

    @Test
    void testSetFetchDirection_invalidValue() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            //noinspection MagicConstant
            SQLException exception = assertThrows(SQLException.class, () -> stmt.setFetchDirection(-1));
            assertThat(exception, allOf(
                    not(instanceOf(SQLFeatureNotSupportedException.class)),
                    fbMessageStartsWith(JaybirdErrorCodes.jb_invalidFetchDirection, "-1"),
                    sqlState(equalTo("HY106"))));
        }
    }

    @Test
    void testSetFetchDirection_statementClosed_throwsException() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(() -> stmt.setFetchDirection(ResultSet.FETCH_FORWARD));
        }
    }

    @Test
    void testGetFetchDirection_statementClosed_throwsException() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(stmt::getFetchDirection);
        }
    }

    /**
     * Tests {@link FBStatement#isPoolable()} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    void testIsPoolable_statementClosed() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(stmt::isPoolable);
        }
    }

    @Test
    void poolable_value() throws SQLException {
        try (var stmt = con.createStatement()) {
            assertFalse(stmt.isPoolable(), "expected poolable initially false");
            stmt.setPoolable(true);
            assertTrue(stmt.isPoolable(), "expected poolable true after set true");
            stmt.setPoolable(false);
            assertFalse(stmt.isPoolable(), "expected poolable false after set false");
        }
    }

    /**
     * Tests {@link org.firebirdsql.jdbc.FBStatement#setPoolable(boolean)} on a closed statement
     * <p>
     * Expected: SQLException statement closed
     * </p>
     */
    @Test
    void testSetPoolable_statementClosed() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.close();

            assertThrowsFbStatementClosed(() -> stmt.setPoolable(true));
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with
     * {@link org.firebirdsql.jdbc.FirebirdStatement} returns {@code true}.
     */
    @Test
    void testIsWrapperFor_FirebirdStatement() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertTrue(stmt.isWrapperFor(FirebirdStatement.class), "Expected to be wrapper for FirebirdStatement");
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with {@link ResultSet}
     * returns {@code false}.
     */
    @Test
    void testIsWrapperFor_ResultSet() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            assertFalse(stmt.isWrapperFor(ResultSet.class), "Expected not to be wrapper for ResultSet");
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#isWrapperFor(Class)} with {@link FbStatement} returns {@code true}.
     */
    @Test
    void isWrapperFor_FbStatement() throws Exception {
        try (var stmt = con.createStatement()) {
            // NOTE: This is an implementation detail and may change
            assertFalse(stmt.isWrapperFor(FbStatement.class),
                    "Initially there is no FbStatement instance available to unwrap");
            stmt.executeQuery("select 1 from RDB$DATABASE");
            assertTrue(stmt.isWrapperFor(FbStatement.class), "Expected to be a wrapper for FbStatement");
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with
     * {@link org.firebirdsql.jdbc.FirebirdStatement} successfully unwraps.
     */
    @Test
    void testUnwrap_FirebirdStatement() throws SQLException {
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
    void testUnwrap_ResultSet() throws SQLException {
        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () -> stmt.unwrap(ResultSet.class));
            assertThat(exception, message(equalTo("Unable to unwrap to class java.sql.ResultSet")));
        }
    }

    /**
     * Tests if {@link org.firebirdsql.jdbc.FBStatement#unwrap(Class)} with {@link FbStatement} successfully unwraps.
     */
    @Test
    void unwrap_FbStatement() throws Exception {
        try (var stmt = con.createStatement()) {
            // NOTE: This is an implementation detail and may change
            assertThrows(SQLException.class, () -> stmt.unwrap(FbStatement.class),
                    "Initially there is no FbStatement instance available to unwrap");
            stmt.executeQuery("select 1 from RDB$DATABASE");
            assertNotNull(stmt.unwrap(FbStatement.class), "Unexpected result for unwrap to FbStatement");
        }
    }

    /**
     * Tests if Firebird 1.5+ custom exception messages work.
     */
    @Test
    void testCustomExceptionMessage() throws Exception {
        assumeTrue(supportInfoFor(con).supportsCustomExceptionMessages(), "Test requires custom exception messages");

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
            SQLException exception = assertThrows(SQLException.class,
                    () -> stmt.execute("EXECUTE PROCEDURE testexception"));
            assertThat(exception, message(containsString("; Custom message; ")));
        }
    }

    /**
     * Tests if Firebird 3 parametrized exceptions are correctly rendered.
     */
    @Test
    void testParametrizedExceptions() throws Exception {
        assumeTrue(supportInfoFor(con).supportsParametrizedExceptions(), "Test requires parametrized exceptions");
        executeDDL(con, "CREATE EXCEPTION two_param_exception 'Param 1 ''@1'', Param 2 ''@2'''");

        try (Statement stmt = con.createStatement()) {
            SQLException exception = assertThrows(SQLException.class, () ->
                            //@formatter:off
                    stmt.execute(
                        "EXECUTE BLOCK AS " +
                        "BEGIN " +
                        "  EXCEPTION two_param_exception USING ('value_1', 'value2'); " +
                        "END"
                    )
                    //@formatter:on
            );
            assertThat(exception, message(containsString("; Param 1 'value_1', Param 2 'value2'; ")));
        }
    }

    @Test
    void testRetrievingUpdateCountAndResultSet() throws Exception {
        assumeTrue(supportInfoFor(con).supportsUpdateReturning(), "Test requires UPDATE .. RETURNING .. support");
        executeDDL(con, CREATE_TABLE);

        try (Statement stmt = con.createStatement()) {
            boolean isResultSet = stmt.execute("INSERT INTO test(col1) VALUES(5) RETURNING col1");

            assertTrue(isResultSet, "Expected first result to be a result set");
            ResultSet rs = stmt.getResultSet();
            assertNotNull(rs, "Result set should not be null");
            assertTrue(rs.next(), "Expected a row in the result set");
            assertEquals(5, rs.getInt(1), "Unexpected value in result set");
            assertFalse(rs.next(), "Expected only one row");
            assertEquals(-1, stmt.getUpdateCount(), "Update count should be -1 before first call to getMoreResults");

            assertFalse(stmt.getMoreResults(), "Next result should not be a result set");
            assertNull(stmt.getResultSet(), "Expected null result set");
            assertEquals(1, stmt.getUpdateCount(), "Update count should be 1 after first call to getMoreResults");

            assertFalse(stmt.getMoreResults(), "Next result should not be a result set");
            assertNull(stmt.getResultSet(), "Expected null result set");
            assertEquals(-1, stmt.getUpdateCount(), "Update count should be -1 after second call to getMoreResults");
        }
    }

    @Test
    void testEnquoteLiteral() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertEquals("'no quotes'", stmt.enquoteLiteral("no quotes"), "No quotes");
            assertEquals("'with''quotes'", stmt.enquoteLiteral("with'quotes"), "With quotes");
        }
    }

    @Test
    void testIsSimpleIdentifier() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertTrue(stmt.isSimpleIdentifier("Simple$Identifier_"), "Simple$Identifier_");
            assertFalse(stmt.isSimpleIdentifier("1Simple$Identifier_"), "1Simple$Identifier_");
            assertFalse(stmt.isSimpleIdentifier(""), "(empty string)");
            assertTrue(stmt.isSimpleIdentifier("A234567890123456789012345678901"), "A234567890123456789012345678901");
            FirebirdSupportInfo supportInfo = getDefaultSupportInfo();

            String maxLengthIdentifier = generateIdentifier(supportInfo.maxIdentifierLengthCharacters());
            assertTrue(stmt.isSimpleIdentifier(maxLengthIdentifier), maxLengthIdentifier);

            String tooLongIdentifier = generateIdentifier(supportInfo.maxIdentifierLengthCharacters() + 1);
            assertFalse(stmt.isSimpleIdentifier(tooLongIdentifier), tooLongIdentifier);
        }
    }

    @Test
    void testEnquoteIdentifier() throws Exception {
        // Only testing dialect 3
        try (FBStatement stmt = (FBStatement) con.createStatement()) {
            assertEquals("simple$identifier_", stmt.enquoteIdentifier("simple$identifier_", false),
                    "simple, alwaysQuote:false");
            assertEquals("\"simple$identifier_\"", stmt.enquoteIdentifier("simple$identifier_", true),
                    "simple, alwaysQuote:true");
            assertEquals("\"already quoted\"", stmt.enquoteIdentifier("\"already quoted\"", false), "already quoted");
            assertEquals("\"has space\"", stmt.enquoteIdentifier("has space", false), "needs quotes");
            assertEquals("\"has\"\"quote\"", stmt.enquoteIdentifier("has\"quote", false), "needs quotes");
        }
    }

    @Test
    void verifySingletonStatementWithException() throws Exception {
        // @formatting:off
        executeDDL(con, "create procedure singleton_error returns (intresult int) as "
                        + "begin "
                        + "  execute statement 'select cast(''x'' as integer) from rdb$database' into intresult;"
                        + "end");
        // @formatting:on
        try (Statement stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.execute("execute procedure singleton_error"));

            assertFalse(stmt.getMoreResults());
            assertEquals(-1, stmt.getUpdateCount());
        }
    }

    @Test
    void verifyMultipleResultsWithUpdateCount_autocommit() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsExecuteBlock(), "Test requires execute block support");
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
            assertTrue(rs.next(), "first result");
            assertEquals(1, rs.getInt(1));
            assertTrue(rs.next(), "second result");
            assertEquals(2, rs.getInt(1));
            assertFalse(rs.next(), "no more results");

            assertFalse(stmt.getMoreResults());
            assertEquals(2, stmt.getUpdateCount());
        }
    }

    @Test
    void verifyMultipleResultsWithUpdateCount_noAutocommit() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsExecuteBlock(), "Test requires execute block support");
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
            assertTrue(rs.next(), "first result");
            assertEquals(1, rs.getInt(1));
            assertTrue(rs.next(), "second result");
            assertEquals(2, rs.getInt(1));
            assertFalse(rs.next(), "no more results");

            assertFalse(stmt.getMoreResults());
            assertEquals(2, stmt.getUpdateCount());

            con.commit();
        }
    }

    @Test
    void testSelectHasNoUpdateCount() throws SQLException {
        prepareTestData();
        try (Statement stmt = con.createStatement()) {
            assertTrue(stmt.execute(SELECT_DATA), "expected a result set");
            ResultSet rs = stmt.getResultSet();
            int count = 0;
            while (rs.next()) {
                assertFalse(rs.isClosed(), "Result set should be open");
                assertFalse(stmt.isClosed(), "Statement should be open");
                assertEquals(count, rs.getInt(1));
                count++;
            }
            assertEquals(DATA_ITEMS, count);
            assertResultSetOpen(rs, "Result set should be still open after last result read in auto-commit");
            assertFalse(stmt.getMoreResults(), "expected no result set for getMoreResults");
            assertResultSetClosed(rs, "Result set should be closed after getMoreResults");
            assertEquals(-1, stmt.getUpdateCount(), "no update count (-1) was expected");
        }
    }

    @Test
    void updateCountOfDDL() throws Exception {
        try (Statement stmt = con.createStatement()) {
            assertFalse(stmt.execute(CREATE_TABLE), "expected no result set");
            assertEquals(-1, stmt.getUpdateCount());
        }
    }

    @Test
    void updateCountOfExecuteProcedure() throws Exception {
        try (Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            stmt.execute(CREATE_TABLE);
            stmt.execute("recreate procedure insert_proc as begin INSERT INTO test(col1) VALUES(1); end");
            con.commit();
            assertFalse(stmt.execute("execute procedure insert_proc"), "expected no result set");
            assertEquals(-1, stmt.getUpdateCount());
        }
    }

    /**
     * Test statement execution with statement text longer than 64KB (requires Firebird 3.0 or higher).
     * <p>
     * NOTE: For native, this also requires a Firebird 3.0 or higher fbclient.dll
     * </p>
     */
    @Test
    void statementTextLongerThan64KB() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsStatementTextLongerThan64K(), "requires long statement text support");
        // For some reason the native implementation can't handle exactly 32KB values, but it can handle 32KB - 1 (this may be version dependent)
        String text32kb = "X".repeat(32 * 1024 - 1);

        try (Statement stmt = con.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "select '" + text32kb + "' val from rdb$database "
                    + "union all "
                    + "select '" + text32kb + "' from rdb$database");
            assertTrue(rs.next(), "expected row");
            assertEquals(text32kb, rs.getString(1));
            assertTrue(rs.next(), "expected row");
            assertEquals(text32kb, rs.getString(1));
        }
    }

    /**
     * Test case for <a href="https://github.com/FirebirdSQL/jaybird/issues/728">jaybird#728</a> with thanks to Lukas
     * Eder.
     */
    @Test
    void testCaseBlobInReturning_728() throws Exception {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate("create table t (a integer not null primary key, b blob)");

            try (ResultSet rs = stmt.executeQuery("insert into t (a, b) values (1, X'010203') returning a, b")) {
                assertTrue(rs.next(), "Expected a row");
                assertArrayEquals(new byte[] { 1, 2, 3 }, rs.getBytes(2));
            }

            try (ResultSet rs = stmt.executeQuery("select a, b from t")) {
                assertTrue(rs.next(), "Expected a row");
                assertArrayEquals(new byte[] { 1, 2, 3 }, rs.getBytes(2));
            }
        }
    }

    /**
     * Tests for <a href="https://github.com/FirebirdSQL/jaybird/issues/729">jaybird#729</a>.
     */
    @Test
    void statementExecuteProcedureShouldNotTrim_729() throws Exception {
        executeDDL(con, """
                create procedure char_return returns (val char(5)) as
                begin
                  val = 'A';
                end""");

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("execute procedure char_return")) {
            assertTrue(rs.next(), "Expected a row");
            assertAll(
                    () -> assertEquals("A    ", rs.getObject(1), "Unexpected trim by getObject"),
                    () -> assertEquals("A    ", rs.getString(1), "Unexpected trim by getString"));
        }
    }

    /**
     * Tests rendering of PSQL exception with parameters ({@code isc_formatted_exception}).
     * <p>
     * Companion to {@code GDSExceptionHelperTest#extraParametersOfFormattedExceptionIgnored()}
     * </p>
     */
    @Test
    void psqlExceptionWithParametersRendering() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsParameterizedExceptions(),
                "test requires parameterized exception support");
        try (var stmt = con.createStatement()) {
            stmt.execute("create exception ex_param 'something wrong in @1'");
            SQLException sqle = assertThrows(SQLException.class, () -> stmt.execute("""
                    execute block as
                    begin
                       exception ex_param using('PARAMETER_1');
                    end
                    """));
            assertThat(sqle, message(allOf(
                    startsWith("exception 1; EX_PARAM; something wrong in PARAMETER_1"),
                    // The exception parameter value should not be repeated after the formatted message
                    not(containsString("something wrong in PARAMETER_1; PARAMETER_1")))));
        }
    }

    @Test
    void getResultSetAfterExecuteQuery_sameResultSet() throws Exception {
        try (var stmt = con.createStatement();
             var rs = stmt.executeQuery("select 1 from rdb$database")) {
            ResultSet sameRs = assertDoesNotThrow(stmt::getResultSet);

            assertSame(rs, sameRs, "Expected ResultSet from getResultSet to be the same as from executeQuery");
        }
    }

    @Test
    void multipleGetResultSetCalls_sameResultSet() throws Exception {
        try (var stmt = con.createStatement()) {
            assertTrue(stmt.execute("select 1 from rdb$database"), "Expected a result set as first result");

            ResultSet rs = stmt.getResultSet();
            ResultSet sameRs = assertDoesNotThrow(stmt::getResultSet);

            assertSame(rs, sameRs, "Expected ResultSet from getResultSet to be the same as from previous getResultSet");
        }
    }

    @Test
    void executeWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, "recreate exception EX_TEST_EXCEPTION 'exception to end execution with error'");
        try (var stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.execute("""
                    execute block as
                    begin
                      exception EX_TEST_EXCEPTION;
                    end"""));
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeWithResultSetWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, """
                recreate procedure RAISE_EXCEPTION_RS (PARAM1 varchar(50) not null) returns (COLUMN1 varchar(50)) as
                begin
                  suspend;
                end""");
        try (var stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.execute("select * from RAISE_EXCEPTION_RS(null)"));
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeUpdateWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, "recreate exception EX_TEST_EXCEPTION 'exception to end execution with error'");
        try (var stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.executeUpdate("""
                    execute block as
                    begin
                      exception EX_TEST_EXCEPTION;
                    end"""));
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    @Test
    void executeQueryWithExceptionShouldEndTransactionInAutocommit() throws Exception {
        executeDDL(con, """
                recreate procedure RAISE_EXCEPTION_RS (PARAM1 varchar(50) not null) returns (COLUMN1 varchar(50)) as
                begin
                  suspend;
                end""");
        try (var stmt = con.createStatement()) {
            assertThrows(SQLException.class, () -> stmt.executeQuery("select * from RAISE_EXCEPTION_RS(null)"));
            assertFalse(con.getLocalTransaction().inTransaction(),
                    "expected no active transaction after exception in auto-commit");
        }
    }

    private void prepareTestData() throws SQLException {
        executeCreateTable(con, CREATE_TABLE);

        try (PreparedStatement pstmt = con.prepareStatement(INSERT_DATA)) {
            for (int i = 0; i < DATA_ITEMS; i++) {
                pstmt.setInt(1, i);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
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

    static Stream<String> scrollableCursorPropertyValues() {
        // We are unconditionally emitting SERVER, to check if the value behaves appropriately on versions that do
        // not support server-side scrollable cursors
        return Stream.of(PropertyConstants.SCROLLABLE_CURSOR_EMULATED, PropertyConstants.SCROLLABLE_CURSOR_SERVER);
    }

    private static FBConnection createConnection(String scrollableCursorPropertyValue) throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.scrollableCursor, scrollableCursorPropertyValue);
        return DriverManager.getConnection(getUrl(), props).unwrap(FBConnection.class);
    }

}
