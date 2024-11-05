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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.common.function.ThrowingBiConsumer;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.stream.Stream;

import static java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.CONCUR_UPDATABLE;
import static java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT;
import static java.sql.ResultSet.TYPE_FORWARD_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;
import static java.sql.ResultSet.TYPE_SCROLL_SENSITIVE;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link FBTxPreparedStatement}.
 * <p>
 * The happy path of the functionality (execution of {@code COMMIT}, {@code ROLLBACK} and {@code SET TRANSACTION}) is
 * tested in {@link FBConnectionAllowTxStmtsTest}.
 * </p>
 *
 * @author Mark Rotteveel
 */
@SuppressWarnings("SqlSourceToSinkFlow")
class FBTxPreparedStatementTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    /** Connection shared by tests with {@code allowTxStmts=true}. */
    private static Connection connection;
    /** Prepared statement with {@code COMMIT} */
    private static FirebirdPreparedStatement commitStmt;

    @BeforeAll
    static void initConnection() throws SQLException {
        connection = getConnectionViaDriverManager(PropertyNames.allowTxStmts, "true");
        commitStmt = connection.prepareStatement("commit").unwrap(FirebirdPreparedStatement.class);
    }

    @BeforeEach
    void resetConnection() throws SQLException {
        connection.setAutoCommit(true);
    }

    @AfterAll
    static void closeConnection() {
        try {
            closeQuietly(connection);
        } finally {
            connection = null;
        }
    }

    @Test
    void getExecutionPlan_notSupported() {
        assertThrows(SQLFeatureNotSupportedException.class, commitStmt::getExecutionPlan);
    }

    @Test
    void getExplainedExecutionPlan_notSupported() {
        assertThrows(SQLFeatureNotSupportedException.class, commitStmt::getExplainedExecutionPlan);
    }

    @ParameterizedTest
    @MethodSource
    void getStatementType(String sql, int expectedType) throws Exception {
        try (var pstmt = connection.prepareStatement(sql).unwrap(FirebirdPreparedStatement.class)) {
            assertEquals(expectedType, pstmt.getStatementType(), "statement type");
        }
    }

    private static Stream<Arguments> getStatementType() {
        return Stream.of(
                Arguments.of("commit", FirebirdPreparedStatement.TYPE_COMMIT),
                Arguments.of("rollback", FirebirdPreparedStatement.TYPE_ROLLBACK),
                Arguments.of("set transaction", FirebirdPreparedStatement.TYPE_START_TRANS));
    }

    @Test
    void isClosed_and_close() throws Exception {
        try (var pstmt = connection.prepareStatement("commit")) {
            assertFalse(pstmt.isClosed(), "Expected statement initially open");
            assertDoesNotThrow(pstmt::close, "Expected no exception on close");
            assertTrue(pstmt.isClosed(), "Expected statement closed after close()");
            assertDoesNotThrow(pstmt::close, "Expected no exception on repeated close");
        }
    }

    @Test
    void executeQuery_notAllowed() {
        var exception = assertThrows(SQLNonTransientException.class, commitStmt::executeQuery);
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_executeQueryWithTxStmt));
    }

    @Test
    void executeUpdate() throws Exception {
        connection.setAutoCommit(false);
        // NOTE: Actual commit is tested in FBConnectionAllowTxStmtsTest (through execute())
        assertEquals(0, commitStmt.executeUpdate(), "Expected zero update count");
    }

    @Test
    void executeLargeUpdate() throws Exception {
        connection.setAutoCommit(false);
        // NOTE: Actual commit is tested in FBConnectionAllowTxStmtsTest (through execute())
        assertEquals(0, commitStmt.executeLargeUpdate(), "Expected zero update count");
    }

    @Test
    void executeAndRelatedMethods() throws Exception {
        connection.setAutoCommit(false);
        // NOTE: Actual commit is tested in FBConnectionAllowTxStmtsTest (through execute())
        assertFalse(commitStmt.execute(), "Expected no result set (execute() returning false)");
        assertNull(commitStmt.getResultSet(), "Expected null result set");
        assertEquals(-1, commitStmt.getUpdateCount(), "Expected -1 update count");
        assertEquals(-1L, commitStmt.getLargeUpdateCount(), "Expected -1 large update count");
        assertFalse(commitStmt.getMoreResults(), "Expected false getMoreResults");
        assertFalse(commitStmt.getMoreResults(PreparedStatement.CLOSE_ALL_RESULTS), "Expected false getMoreResults");
    }

    @Test
    void closeOnCompletionBehaviour() throws Exception {
        connection.setAutoCommit(false);
        try (var pstmt = connection.prepareStatement("commit")) {
            assertFalse(pstmt.isCloseOnCompletion(), "Expected closeOnCompletion initially false");
            pstmt.closeOnCompletion();
            assertTrue(pstmt.isCloseOnCompletion(), "Expected closeOnCompletion true after closeOnCompletion()");
            assertFalse(pstmt.isClosed(), "Expected statement open before execute");
            pstmt.execute();
            assertTrue(pstmt.isClosed(), "Expected statement closed after execute with closeOnCompletion enabled");
        }
    }

    @Test
    void addBatch_notSupported() {
        var exception = assertThrows(SQLFeatureNotSupportedException.class, commitStmt::addBatch);
        assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_addBatchWithTxStmt));
    }

    @Test
    void clearBatch_doesNothing() {
        assertDoesNotThrow(commitStmt::clearBatch);
    }

    @Test
    void executeBatch_doesNothing() {
        assertArrayEquals(new int[0], assertDoesNotThrow(commitStmt::executeBatch));
    }

    @Test
    void executeLargeBatch_doesNothing() {
        assertArrayEquals(new long[0], assertDoesNotThrow(commitStmt::executeLargeBatch));
    }

    @Test
    void getMetaData_returnsNull() throws SQLException {
        assertNull(commitStmt.getMetaData(), "Expected null ResultSetMetaData");
    }

    @Test
    void getParameterMetaData_returnsInstanceWithoutParameters() throws SQLException {
        ParameterMetaData pmd = assertDoesNotThrow(commitStmt::getParameterMetaData);
        assertNotNull(pmd, "Expected non-null ParameterMetaData");
        assertEquals(0, pmd.getParameterCount(), "Expected no parameters");
    }

    @ParameterizedTest
    @MethodSource
    void resultSetTypeConcurrencyHoldability(int type, int concurrency, int holdability, int expectedType)
            throws SQLException {
        try (var pstmt = connection.prepareStatement("commit", type, concurrency, holdability)) {
            assertEquals(expectedType, pstmt.getResultSetType(), "result set type");
            assertEquals(concurrency, pstmt.getResultSetConcurrency(), "result set concurrency");
            assertEquals(holdability, pstmt.getResultSetHoldability(), "result set holdability");
        }
    }

    private static Stream<Arguments> resultSetTypeConcurrencyHoldability() {
        return Stream.of(
                Arguments.of(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, CLOSE_CURSORS_AT_COMMIT, TYPE_FORWARD_ONLY),
                Arguments.of(TYPE_FORWARD_ONLY, CONCUR_READ_ONLY, HOLD_CURSORS_OVER_COMMIT, TYPE_FORWARD_ONLY),
                Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, CLOSE_CURSORS_AT_COMMIT,
                        TYPE_SCROLL_INSENSITIVE),
                Arguments.of(TYPE_SCROLL_INSENSITIVE, CONCUR_UPDATABLE, HOLD_CURSORS_OVER_COMMIT,
                        TYPE_SCROLL_INSENSITIVE),
                Arguments.of(TYPE_SCROLL_SENSITIVE, CONCUR_READ_ONLY, CLOSE_CURSORS_AT_COMMIT,
                        TYPE_SCROLL_INSENSITIVE),
                Arguments.of(TYPE_SCROLL_SENSITIVE, CONCUR_UPDATABLE, HOLD_CURSORS_OVER_COMMIT,
                        TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    void getConnection() throws Exception {
        assertEquals(connection, commitStmt.getConnection());
    }

    @Test
    void poolable() throws Exception {
        try (var pstmt = connection.prepareStatement("commit")) {
            assertTrue(pstmt.isPoolable(), "Expected statement initially poolable");
            assertDoesNotThrow(() -> pstmt.setPoolable(false));
            assertFalse(pstmt.isPoolable(), "Expected statement not poolable");
            assertDoesNotThrow(() -> pstmt.setPoolable(true));
            assertTrue(pstmt.isPoolable(), "Expected statement poolable");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void unsupportedStringExecuteMethodsFromStatement(String description,
            ThrowingBiConsumer<PreparedStatement, String> execute) {
        var exception = assertThrows(SQLException.class, () -> execute.accept(commitStmt, "commit"));
        assertThat(exception, message(equalTo(FBPreparedStatement.METHOD_NOT_SUPPORTED)));
    }

    private static Stream<Arguments> unsupportedStringExecuteMethodsFromStatement() {
        return Stream.of(
                unsupportedStatementCase("execute(String)", Statement::execute),
                unsupportedStatementCase("execute(String,int)",
                        (stmt, sql) -> stmt.execute(sql, Statement.RETURN_GENERATED_KEYS)),
                unsupportedStatementCase("execute(String, int[])", (stmt, sql) -> stmt.execute(sql, new int[] { 1, 2 })),
                unsupportedStatementCase("execute(String, String[])",
                        (stmt, sql) -> stmt.execute(sql, new String[] { "IGNORED1", "IGNORED2" })),
                unsupportedStatementCase("executeUpdate(String)", Statement::executeUpdate),
                unsupportedStatementCase("executeLargeUpdate(String)", Statement::executeLargeUpdate),
                unsupportedStatementCase("executeUpdate(String, int)",
                        (stmt, sql) -> stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)),
                unsupportedStatementCase("executeLargeUpdate(String, int)",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, Statement.RETURN_GENERATED_KEYS)),
                unsupportedStatementCase("executeUpdate(String, int[])",
                        (stmt, sql) -> stmt.executeUpdate(sql, new int[] { 1, 2 })),
                unsupportedStatementCase("executeLargeUpdate(String, int[])",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, new int[] { 1, 2 })),
                unsupportedStatementCase("executeUpdate(String, String[])",
                        (stmt, sql) -> stmt.executeUpdate(sql, new String[] { "IGNORED1", "IGNORED2" })),
                unsupportedStatementCase("executeLargeUpdate(String, String[])",
                        (stmt, sql) -> stmt.executeLargeUpdate(sql, new String[] { "IGNORED1", "IGNORED2" })),
                unsupportedStatementCase("addBatch(String)", Statement::addBatch));
    }

    private static Arguments unsupportedStatementCase(String description,
            ThrowingBiConsumer<PreparedStatement, String> execute) {
        return Arguments.of(description, execute);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void setXXXMethodsInvalidIndex(String description, ThrowingConsumer<PreparedStatement> setMethod) {
        var exception = assertThrows(SQLException.class, () -> setMethod.accept(commitStmt));
        assertThat(exception, message(startsWith("Invalid column index: ")));
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> setXXXMethodsInvalidIndex() {
        return Stream.of(
                setXXXMethodCase("setNull(int, int)", stmt -> stmt.setNull(1, Types.INTEGER)),
                setXXXMethodCase("setBoolean(int, boolean)", stmt -> stmt.setBoolean(1, false)),
                setXXXMethodCase("setByte(int, byte)", stmt -> stmt.setByte(1, (byte) 0)),
                setXXXMethodCase("setShort(int, short)", stmt -> stmt.setShort(1, (short) 0)),
                setXXXMethodCase("setInt(int, int)", stmt -> stmt.setInt(1, 0)),
                setXXXMethodCase("setLong(int, long)", stmt -> stmt.setLong(1, 0)),
                setXXXMethodCase("setFloat(int, float)", stmt -> stmt.setFloat(1, 0f)),
                setXXXMethodCase("setDouble(int, double)", stmt -> stmt.setDouble(1, 0d)),
                setXXXMethodCase("setBigDecimal(int, BigDecimal)", stmt -> stmt.setBigDecimal(1, null)),
                setXXXMethodCase("setString(int, String)", stmt -> stmt.setString(1, null)),
                setXXXMethodCase("setBytes(int, byte[])", stmt -> stmt.setBytes(1, null)),
                setXXXMethodCase("setDate(int, Date)", stmt -> stmt.setDate(1, null)),
                setXXXMethodCase("setTime(int, Time)", stmt -> stmt.setTime(1, null)),
                setXXXMethodCase("setTimestamp(int, Timestamp)", stmt -> stmt.setTimestamp(1, null)),
                setXXXMethodCase("setAsciiStream(int, InputStream, int)", stmt -> stmt.setAsciiStream(1, null, 1)),
                setXXXMethodCase("setUnicodeStream(int, InputStream, int)", stmt -> stmt.setUnicodeStream(1, null, 1)),
                setXXXMethodCase("setBinaryStream(int, InputStream, int)", stmt -> stmt.setBinaryStream(1, null, 1)),
                setXXXMethodCase("setObject(int, Object, int)", stmt -> stmt.setObject(1, null, Types.INTEGER)),
                setXXXMethodCase("setObject(int, Object)", stmt -> stmt.setObject(1, null)),
                setXXXMethodCase("setCharacterStream(int, Reader, int)", stmt -> stmt.setCharacterStream(1, null, 1)),
                setXXXMethodCase("setRef(int, Ref)", stmt -> stmt.setRef(1, null)),
                setXXXMethodCase("setBlob(int, Blob)", stmt -> stmt.setBlob(1, (Blob) null)),
                setXXXMethodCase("setClob(int, Clob)", stmt -> stmt.setClob(1, (Clob) null)),
                setXXXMethodCase("setArray(int, Array)", stmt -> stmt.setArray(1, null)),
                setXXXMethodCase("setDate(int, Date, Calendar)", stmt -> stmt.setDate(1, null, null)),
                setXXXMethodCase("setTime(int, Time, Calendar)", stmt -> stmt.setTime(1, null, null)),
                setXXXMethodCase("setTimestamp(int, Timestamp, Calendar)", stmt -> stmt.setTimestamp(1, null, null)),
                setXXXMethodCase("setNull(int, int, String)", stmt -> stmt.setNull(1, Types.INTEGER, "INT")),
                setXXXMethodCase("setURL(int, URL)", stmt -> stmt.setURL(1, null)),
                setXXXMethodCase("setRowId(int, RowId)", stmt -> stmt.setRowId(1, null)),
                setXXXMethodCase("setNString(int, String)", stmt -> stmt.setNString(1, null)),
                setXXXMethodCase("setNCharacterStream(int, Reader, long)",
                        stmt -> stmt.setNCharacterStream(1, null, 1L)),
                setXXXMethodCase("setNClob(int, NClob)", stmt -> stmt.setNClob(1, (NClob) null)),
                setXXXMethodCase("setClob(int, Reader, long)", stmt -> stmt.setClob(1, null, 1L)),
                setXXXMethodCase("setBlob(int, InputStream, long)", stmt -> stmt.setBlob(1, null, 1L)),
                setXXXMethodCase("setNClob(int, Reader, long)", stmt -> stmt.setNClob(1, null, 1L)),
                setXXXMethodCase("setSQLXML(int, SQLXML)", stmt -> stmt.setSQLXML(1, null)),
                setXXXMethodCase("setObject(int, Object, int, int)", stmt -> stmt.setObject(1, null, Types.NUMERIC, 2)),
                setXXXMethodCase("setAsciiStream(int, InputStream, long)", stmt -> stmt.setAsciiStream(1, null, 1L)),
                setXXXMethodCase("setBinaryStream(int, InputStream, long)", stmt -> stmt.setBinaryStream(1, null, 1L)),
                setXXXMethodCase("setCharacterStream(int, Reader, long)", stmt -> stmt.setCharacterStream(1, null, 1L)),
                setXXXMethodCase("setAsciiStream(int, InputStream)", stmt -> stmt.setAsciiStream(1, null)),
                setXXXMethodCase("setBinaryStream(int, InputStream)", stmt -> stmt.setBinaryStream(1, null)),
                setXXXMethodCase("setCharacterStream(int, Reader)", stmt -> stmt.setCharacterStream(1, null)),
                setXXXMethodCase("setNCharacterStream(int, Reader)", stmt -> stmt.setNCharacterStream(1, null)),
                setXXXMethodCase("setClob(int, Reader)", stmt -> stmt.setClob(1, (Reader) null)),
                setXXXMethodCase("setBlob(int, InputStream)", stmt -> stmt.setBlob(1, (InputStream) null)),
                setXXXMethodCase("setNClob(int, Reader)", stmt -> stmt.setNClob(1, (Reader) null)));
    }

    private static Arguments setXXXMethodCase(String description, ThrowingConsumer<PreparedStatement> setMethod) {
        return Arguments.of(description, setMethod);
    }

    @Test
    void cancel_notSupported() {
        assertThrows(SQLFeatureNotSupportedException.class, commitStmt::cancel);
    }

    // We think the remaining methods or not interesting to test

}