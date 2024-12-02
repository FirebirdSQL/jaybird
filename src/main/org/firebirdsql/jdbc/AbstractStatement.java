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
import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_ATTR_VALUE;

/**
 * Common abstract base class for statement implementations.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link Statement} and {@link FirebirdStatement} interfaces.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
@NullMarked
public abstract class AbstractStatement implements Statement, FirebirdStatement {

    private static final AtomicInteger STATEMENT_ID_GENERATOR = new AtomicInteger();

    private final int localStatementId = STATEMENT_ID_GENERATOR.incrementAndGet();
    protected final FBConnection connection;
    private @Nullable String cursorName;
    @SuppressWarnings("java:S3077")
    private volatile @Nullable SQLWarning warning;
    private FetchConfig fetchConfig;

    private volatile boolean closed;
    private boolean poolable;
    private boolean closeOnCompletion;

    protected AbstractStatement(FBConnection connection, ResultSetBehavior resultSetBehavior) {
        this.connection = requireNonNull(connection, "connection");
        fetchConfig = new FetchConfig(resultSetBehavior);
    }

    @Override
    public final FBConnection getConnection() throws SQLException {
        checkValidity();
        return connection;
    }

    /**
     * @return instance of {@link FbStatement} associated with this statement; cannot be {@code null}
     * @throws SQLException
     *         if this statement is closed
     * @throws java.sql.SQLFeatureNotSupportedException
     *         if this statement implementation does not use statement handles
     */
    protected abstract FbStatement getStatementHandle() throws SQLException;

    /**
     * Get the current statement type of this statement.
     * <p>
     * The returned value is one of the {@code TYPE_*} constant values defined in {@link FirebirdPreparedStatement}, or
     * {@code 0} if the statement currently does not have a statement type.
     * </p>
     *
     * @return The identifier for the given statement's type
     */
    public abstract int getStatementType();

    /**
     * {@inheritDoc}
     * <p>
     * Subclasses overriding this method are expected to call this method with {@code super.close()} at an appropriate
     * point to mark it closed.
     * </p>
     */
    @Override
    public void close() throws SQLException {
        warning = null;
        closed = true;
    }

    @Override
    public final boolean isClosed() {
        return closed;
    }

    @Override
    public boolean isValid() {
        return !closed;
    }

    /**
     * Check if this statement is valid (not closed). This method should be invoked before executing any action which
     * requires a valid/open statement.
     *
     * @throws SQLException
     *         if this Statement has been closed and cannot be used anymore.
     */
    protected final void checkValidity() throws SQLException {
        if (closed) {
            throw FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_stmtClosed);
        }
    }

    /**
     * Completes this statement with {@link CompletionReason#OTHER}.
     *
     * @throws SQLException
     *         for failures completing this statement
     * @see #completeStatement(CompletionReason)
     */
    public final void completeStatement() throws SQLException {
        completeStatement(CompletionReason.OTHER);
    }

    /**
     * Completes this statement with {@code reason}.
     * <p>
     * On completion, any open result set will be closed, and possibly the statement itself may be closed.
     * </p>
     *
     * @param reason
     *         completion reason
     * @throws SQLException
     *         for failures completing this statement
     */
    public abstract void completeStatement(CompletionReason reason) throws SQLException;

    @Override
    public final boolean isPoolable() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            return poolable;
        }
    }

    @Override
    public final void setPoolable(boolean poolable) throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            this.poolable = poolable;
        }
    }

    @Override
    public final boolean isCloseOnCompletion() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            return closeOnCompletion;
        }
    }

    @Override
    public final void closeOnCompletion() throws SQLException {
        try (var ignored = withLock()) {
            checkValidity();
            closeOnCompletion = true;
        }
    }

    /**
     * Closes this statement if {@code closeOnCompletion} is set to {@code true}, does nothing if set to {@code false}.
     *
     * @throws SQLException
     *         for errors closing this statement
     * @see #closeOnCompletion()
     * @see #close()
     */
    protected final void performCloseOnCompletion() throws SQLException {
        if (closeOnCompletion) {
            close();
        }
    }

    /**
     * @return current fetch config for this statement
     * @since 6
     */
    protected final FetchConfig fetchConfig() {
        try (var ignored = withLock()) {
            return fetchConfig;
        }
    }

    /**
     * @return result set behavior for this statement
     * @since 6
     */
    protected final ResultSetBehavior resultSetBehavior() {
        return fetchConfig().resultSetBehavior();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public final int getResultSetType() throws SQLException {
        checkValidity();
        return resultSetBehavior().type();
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public final int getResultSetConcurrency() throws SQLException {
        checkValidity();
        return resultSetBehavior().concurrency();
    }

    @Override
    public final int getResultSetHoldability() throws SQLException {
        checkValidity();
        return resultSetBehavior().holdability();
    }

    @Override
    public final int getMaxRows() throws SQLException {
        checkValidity();
        return fetchConfig().maxRows();
    }

    @Override
    public final void setMaxRows(int max) throws SQLException {
        checkValidity();
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withMaxRows(max);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support maxRows exceeding {@link Integer#MAX_VALUE}, if a larger value is set, Jaybird will
     * add a warning to the statement and reset the maximum to 0.
     * </p>
     */
    @Override
    public final void setLargeMaxRows(long max) throws SQLException {
        if (max > Integer.MAX_VALUE) {
            addWarning(new SQLWarning(
                    "Implementation limit: maxRows cannot exceed Integer.MAX_VALUE, value was %d, reset to 0"
                            .formatted(max), SQL_STATE_INVALID_ATTR_VALUE));
            max = 0;
        }
        setMaxRows((int) max);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Jaybird does not support maxRows exceeding {@link Integer#MAX_VALUE}, the return value of this method is the
     * same as {@link #getMaxRows()}.
     * </p>
     */
    @Override
    public final long getLargeMaxRows() throws SQLException {
        return getMaxRows();
    }

    @Override
    public final int getFetchSize() throws SQLException {
        checkValidity();
        return fetchConfig().fetchSize();
    }

    @Override
    public final void setFetchSize(int rows) throws SQLException {
        checkValidity();
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withFetchSize(rows);
        }
    }

    @SuppressWarnings("MagicConstant")
    @Override
    public final int getFetchDirection() throws SQLException {
        checkValidity();
        return fetchConfig().direction();
    }

    @Override
    public final void setFetchDirection(int direction) throws SQLException {
        checkValidity();
        try (var ignored = withLock()) {
            fetchConfig = fetchConfig.withDirection(direction);
        }
    }

    @Override
    public final void setCursorName(@Nullable String cursorName) throws SQLException {
        checkValidity();
        try (var ignored = withLock()) {
            this.cursorName = cursorName;
        }
    }

    /**
     * @return current value of {@code cursorName}
     * @see #setCursorName(String)
     */
    protected final @Nullable String getCursorName() {
        return cursorName;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If connection property {@code reportSQLWarnings} is set to {@code NONE} (case-insensitive), this method will
     * not report warnings and always return {@code null}.
     * </p>
     */
    @Override
    public final @Nullable SQLWarning getWarnings() throws SQLException {
        checkValidity();
        return warning;
    }

    @Override
    public final void clearWarnings() throws SQLException {
        checkValidity();
        warning = null;
    }

    protected final void addWarning(SQLWarning warning) {
        try (var ignored = withLock()) {
            if (connection.isIgnoreSQLWarnings()) return;
            SQLWarning currentWarning = this.warning;
            if (currentWarning == null) {
                this.warning = warning;
            } else {
                currentWarning.setNextWarning(warning);
            }
        }
    }

    @Override
    public final int getLocalStatementId() {
        return localStatementId;
    }

    @Override
    public final int hashCode() {
        return localStatementId;
    }

    @Override
    public final boolean equals(Object other) {
        return other instanceof FirebirdStatement otherStmt
               && this.localStatementId == otherStmt.getLocalStatementId();
    }

    /**
     * @see FbAttachment#withLock()
     */
    protected final LockCloseable withLock() {
        return connection.withLock();
    }

}
