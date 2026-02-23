// SPDX-FileCopyrightText: Copyright 2025-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.util.Objects.requireNonNull;

/**
 * A class that holds a single {@link Statement} for executing metadata queries.
 * <p>
 * The statement returned by {@link #getStatement()} will piggyback on the active transaction, or start one when needed,
 * but does not commit (not even in auto-commit).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 7
 */
@InternalApi
final class MetadataStatementHolder {

    private final FBConnection connection;
    private @Nullable FBStatement statement;

    MetadataStatementHolder(FBConnection connection) {
        this.connection = requireNonNull(connection, "connection");
    }

    /**
     * Returns an {@link FBStatement} suitable for executing metadata statements.
     * <p>
     * For efficiency reasons, it is recommended that callers do not close the returned statement. If this holder has no
     * statement, or if it has been closed, a new statement will be created.
     * </p>
     *
     * @return statement
     * @throws SQLException
     *         if the connection is closed or the statement could not be allocated
     * @see MetadataStatementHolder
     */
    FBStatement getStatement() throws SQLException {
        try (var ignored = connection.withLock()) {
            FBStatement statement = this.statement;
            if (statement != null && !statement.isClosed()) return statement;
            return this.statement = createStatement();
        }
    }

    private FBStatement createStatement() throws SQLException {
        var metaDataTransactionCoordinator =
                new InternalTransactionCoordinator.MetaDataTransactionCoordinator(connection.txCoordinator);
        var rsBehavior = ResultSetBehavior.of(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
        return new FBStatement(connection, rsBehavior, metaDataTransactionCoordinator);
    }

}
