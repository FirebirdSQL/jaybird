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

import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID;

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
public abstract class AbstractStatement implements Statement, FirebirdStatement {

    private static final AtomicInteger STATEMENT_ID_GENERATOR = new AtomicInteger();

    private final int localStatementId = STATEMENT_ID_GENERATOR.incrementAndGet();

    private volatile boolean closed;
    private boolean poolable;
    private boolean closeOnCompletion;

    /**
     * {@inheritDoc}
     * <p>
     * Subclasses overriding this method are expected to call this method at an appropriate point to mark it closed.
     * </p>
     */
    @Override
    public void close() throws SQLException {
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
        if (isClosed()) {
            throw new SQLNonTransientException("Statement is already closed", SQL_STATE_INVALID_STATEMENT_ID);
        }
    }

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
    protected abstract LockCloseable withLock();

}
