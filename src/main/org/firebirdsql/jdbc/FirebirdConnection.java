// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.util.InternalApi;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extension of {@link Connection} interface providing access to Firebird specific features.
 *
 * @author Roman Rokytskyy
 * @since 1.5
 */
public interface FirebirdConnection extends Connection {

    /**
     * {@inheritDoc}
     *
     * @return instance of {@link FirebirdBlob}.
     */
    Blob createBlob() throws SQLException;

    /**
     * Set transaction parameters for the specified isolation level. They will
     * take effect only on the newly started transaction.
     *
     * @param isolationLevel
     *         JDBC isolation level.
     * @param parameters
     *         array of TPB parameters, see all TPB_* constants.
     * @throws SQLException
     *         if specified transaction parameters cannot be set.
     * @deprecated use {@link #setTransactionParameters(int, TransactionParameterBuffer)} instead.
     */
    @Deprecated(since = "2")
    void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException;

    /**
     * Get transaction parameters for the specified transaction isolation level.
     *
     * @param isolationLevel
     *         isolation level defined in the {@link Connection} interface.
     * @return instance of {@link TransactionParameterBuffer} containing current transaction parameters.
     * @throws SQLException
     *         if error occurred obtaining transaction parameters.
     * @since 2
     */
    TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException;

    /**
     * Create new instance of {@link TransactionParameterBuffer}.
     *
     * @return empty instance of {@link TransactionParameterBuffer}.
     * @throws SQLException
     *         if error occurred during this operation.
     * @since 2
     */
    TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException;

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * <p>
     * This method replaces the default TPB mapping with the specified one, changes will be effective from the next
     * transaction start.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with parameters to set.
     * @throws SQLException
     *         if error occurred during this operation.
     * @since 2
     */
    void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Set transaction parameters for the next transactions.
     * <p>
     * This method does not change the TPB mapping, but replaces the mapping for the current transaction isolation
     * until {@link Connection#setTransactionIsolation(int)} is called.
     * </p>
     * <p>
     * This method cannot be called when a transaction is currently active.
     * </p>
     *
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} with new transaction parameters.
     * @throws SQLException
     *         if method is called within a transaction.
     * @since 2
     */
    void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * @return {@code true} if this connection is configured to use {@code isc_tpb_autocommit} when in auto commit.
     * @since 3
     */
    boolean isUseFirebirdAutoCommit();

    /**
     * Provides access to the low-level connection handle.
     * <p>
     * <b>WARNING</b> using this connection handle directly may bring the JDBC connection in an inconsistent state.
     * </p>
     *
     * @return The low-level connection handle.
     * @since 3
     */
    @InternalApi
    FbDatabase getFbDatabase() throws SQLException;

    /**
     * Resets the known client info properties of this connection to the defaults. This does not reset the values of
     * those properties on the server, but only resets the list of known properties held by this connection
     * <p>
     * If this connection is closed, this is effectively a no-op. Primary use-case for this method is to reset a
     * connection held in a connection pool.
     * </p>
     *
     * @since 6
     */
    void resetKnownClientInfoProperties();

}