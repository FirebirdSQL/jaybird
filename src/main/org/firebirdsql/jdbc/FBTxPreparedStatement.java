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

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.parser.LocalStatementClass;
import org.firebirdsql.jaybird.parser.LocalStatementType;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jdbc.FBPreparedStatement.METHOD_NOT_SUPPORTED;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_GENERAL_ERROR;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID;

/**
 * Specialized implementation of {@link FirebirdPreparedStatement}/{@link java.sql.PreparedStatement} for executing
 * transaction management statements.
 * <p>
 * Contrary to normal statement implementations, this doesn't notify a {@link FBObjectListener.StatementListener} about
 * execution starts and completion, as those would start transactions when needed, which will interfere with the goal
 * of this statement type (that is commit, rollback, or start a new transaction). Instead, this implementation will
 * call on {@link FBConnection#handleHardCommitStatement()}, {@link FBConnection#handleHardRollbackStatement()}, or
 * {@link FBConnection#handleSetTransactionStatement(String)}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
@SuppressWarnings("java:S1192")
final class FBTxPreparedStatement implements FirebirdPreparedStatement {

    private final FBConnection connection;
    private final int localStatementId;
    private final LocalStatementType statementType;
    private final String sql;
    private final ResultSetBehavior rsBehavior;

    private volatile boolean closed;
    private boolean closeOnCompletion;
    private boolean poolable = true;

    FBTxPreparedStatement(FBConnection connection, LocalStatementType statementType, String sql,
            ResultSetBehavior rsBehavior) {
        if (statementType.statementClass() != LocalStatementClass.TRANSACTION_BOUNDARY) {
            throw new IllegalArgumentException("Unsupported value for statementType (implementation bug): "
                    + statementType);
        }
        this.connection = requireNonNull(connection, "connection");
        localStatementId = FBStatement.nextLocalStatementId();
        this.statementType = statementType;
        this.sql = sql;
        this.rsBehavior = rsBehavior;
    }

    @Override
    public String getExecutionPlan() throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException(
                "Cannot provide an execution plan for a transaction management statement");
    }

    @Override
    public String getExplainedExecutionPlan() throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException(
                "Cannot provide an explained execution plan for a transaction management statement");
    }

    @Override
    public int getStatementType() {
        return switch (statementType) {
            case HARD_COMMIT -> FirebirdPreparedStatement.TYPE_COMMIT;
            case HARD_ROLLBACK -> FirebirdPreparedStatement.TYPE_ROLLBACK;
            case SET_TRANSACTION -> FirebirdPreparedStatement.TYPE_START_TRANS;
            // Should already be handled by constructor
            default -> throw new AssertionError(
                    "Unsupported value for statementType (implementation bug): " + statementType);
        };
    }

    @Override
    public void close() throws SQLException {
        try (var ignored = withLock()) {
            if (closed) return;
            closed = true;
            connection.notifyStatementClosed(this);
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        checkValidity();
        throw FbExceptionBuilder.forNonTransientException(JaybirdErrorCodes.jb_executeQueryWithTxStmt).toSQLException();
    }

    @Override
    public int executeUpdate() throws SQLException {
        execute0();
        return 0;
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        execute0();
        return 0L;
    }

    @Override
    public boolean execute() throws SQLException {
        execute0();
        return false;
    }

    private void execute0() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            switch (statementType) {
            case HARD_COMMIT -> connection.handleHardCommitStatement();
            case HARD_ROLLBACK -> connection.handleHardRollbackStatement();
            case SET_TRANSACTION -> connection.handleSetTransactionStatement(sql);
            // Should already be handled by constructor
            default -> throw new AssertionError(
                    "Unsupported value for statementType (implementation bug): " + statementType);
            }
            if (closeOnCompletion) {
                close();
            }
        }
    }

    @Override
    public void addBatch() throws SQLException {
        checkValidity();
        throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_addBatchWithTxStmt).toSQLException();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        methodNotSupported();
    }

    @Override
    public void clearBatch() throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public int[] executeBatch() throws SQLException {
        checkValidity();
        // There never is a batch, so always return an empty array
        return new int[0];
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        checkValidity();
        // There never is a batch, so always return an empty array
        return new long[0];
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        checkValidity();
        // There is never a result set, so no metadata
        return null;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        checkValidity();
        // Zero parameters, so empty parameter metadata
        return new FBParameterMetaData(connection.getFbDatabase().emptyRowDescriptor(), connection);
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetType() throws SQLException {
        checkValidity();
        return rsBehavior.type();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetConcurrency() throws SQLException {
        checkValidity();
        return rsBehavior.concurrency();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        checkValidity();
        return rsBehavior.holdability();
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkValidity();
        return connection;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            this.poolable = poolable;
        }
    }

    @Override
    public boolean isPoolable() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            return poolable;
        }
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            closeOnCompletion = true;
        }
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            return closeOnCompletion;
        }
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return methodNotSupported();
    }

    private static <T> T methodNotSupported() throws SQLException {
        throw new SQLNonTransientException(METHOD_NOT_SUPPORTED, SQL_STATE_GENERAL_ERROR);
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return methodNotSupported();
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        noParameters(parameterIndex);
    }

    private void noParameters(int parameterIndex) throws SQLException {
        checkValidity();
        throw new SQLException("Invalid column index: " + parameterIndex,
                SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void clearParameters() throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        noParameters(parameterIndex);
    }

    @Override
    public int getInsertedRowsCount() {
        return -1;
    }

    @Override
    public int getUpdatedRowsCount() {
        return -1;
    }

    @Override
    public int getDeletedRowsCount() {
        return -1;
    }

    @Override
    public boolean hasOpenResultSet() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #getResultSet()} instead, will be removed in Jaybird 7
     */
    @SuppressWarnings("removal")
    @Deprecated(since = "6", forRemoval = true)
    @Override
    public ResultSet getCurrentResultSet() throws SQLException {
        return getResultSet();
    }

    @Override
    public boolean isValid() {
        return isClosed();
    }

    @Override
    public int getLocalStatementId() {
        return localStatementId;
    }

    @Override
    public int hashCode() {
        return localStatementId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FirebirdStatement otherStmt
                && this.localStatementId == otherStmt.getLocalStatementId();
    }

    @SuppressWarnings("java:S4144")
    @Override
    public int getMaxFieldSize() throws SQLException {
        checkValidity();
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @SuppressWarnings("java:S4144")
    @Override
    public int getMaxRows() throws SQLException {
        checkValidity();
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @SuppressWarnings("java:S4144")
    @Override
    public long getLargeMaxRows() throws SQLException {
        checkValidity();
        return 0;
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @SuppressWarnings("java:S4144")
    @Override
    public int getQueryTimeout() throws SQLException {
        checkValidity();
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public void cancel() throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Cannot cancel transaction management statement execution");
    }

    @SuppressWarnings("java:S4144")
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkValidity();
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @SuppressWarnings("java:S4144")
    @Override
    public ResultSet getResultSet() throws SQLException {
        checkValidity();
        return null;
    }

    @SuppressWarnings("java:S4144")
    @Override
    public int getUpdateCount() throws SQLException {
        checkValidity();
        return -1;
    }

    @SuppressWarnings("java:S4144")
    @Override
    public long getLargeUpdateCount() throws SQLException {
        checkValidity();
        return -1;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        checkValidity();
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @Override
    public int getFetchDirection() throws SQLException {
        checkValidity();
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        checkValidity();
        // silently ignored
    }

    @SuppressWarnings("java:S4144")
    @Override
    public int getFetchSize() throws SQLException {
        checkValidity();
        return 1;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        checkValidity();
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        checkValidity();
        return new FBResultSet(connection.getFbDatabase().emptyRowDescriptor(), List.of());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw new SQLException("Unable to unwrap to class " + (iface != null ? iface.getName() : "(null)"));
        }
        return iface.cast(this);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    /**
     * Check if this statement is valid. This method should be invoked before executing any action which requires a
     * valid/open statement.
     *
     * @throws SQLException
     *         if this Statement has been closed and cannot be used anymore.
     */
    private void checkValidity() throws SQLException {
        if (isClosed()) {
            throw new SQLException("Statement is already closed", SQL_STATE_INVALID_STATEMENT_ID);
        }
    }

    /**
     * @see org.firebirdsql.gds.ng.FbAttachment#withLock()
     */
    private LockCloseable withLock() {
        return connection.withLock();
    }

}
