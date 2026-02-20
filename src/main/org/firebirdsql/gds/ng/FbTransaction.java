// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;

/**
 * Handle for a transaction.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbTransaction extends ExceptionListenable {

    /**
     * @return Current transaction state
     */
    TransactionState getState();

    /**
     * @return The Firebird transaction handle identifier
     */
    int getHandle();

    /**
     * Adds a {@link org.firebirdsql.gds.ng.listeners.TransactionListener} to the list of strongly referenced listeners.
     *
     * @param listener
     *         TransactionListener to register
     */
    void addTransactionListener(TransactionListener listener);

    /**
     * Adds a {@link org.firebirdsql.gds.ng.listeners.TransactionListener} to the list of weakly referenced listeners.
     * <p>
     * If the listener is already strongly referenced, this call will be ignored
     * </p>
     *
     * @param listener
     *         TransactionListener to register
     */
    void addWeakTransactionListener(TransactionListener listener);

    /**
     * Removes the {@link org.firebirdsql.gds.ng.listeners.TransactionListener} from the list of listeners.
     *
     * @param listener
     *         TransactionListener to remove
     */
    void removeTransactionListener(TransactionListener listener);

    /**
     * Commit the transaction
     */
    void commit() throws SQLException;

    /**
     * Roll back the transaction
     */
    void rollback() throws SQLException;

    /**
     * Prepare the transaction for two-phase commit/rollback.
     *
     * @param recoveryInformation
     *         Transaction recovery information (stored in RDB$TRANSACTION_DESCRIPTION of RDB$TRANSACTIONS),
     *         or {@code null} to prepare without recovery information.
     */
    void prepare(byte @Nullable [] recoveryInformation) throws SQLException;

    /**
     * Request transaction info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T extends @Nullable Object> T getTransactionInfo(byte[] requestItems, int bufferLength,
            InfoProcessor<T> infoProcessor) throws SQLException;

    /**
     * Performs a transaction info request.
     *
     * @param requestItems
     *         Information items to request
     * @param maxBufferLength
     *         Maximum response buffer length to use
     * @return The response buffer (note: length is the actual length of the
     * response, not <code>maxBufferLength</code>
     * @throws SQLException
     *         For errors retrieving the information.
     */
    byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException;

    /**
     * Retrieves the transaction id.
     * <p>
     * The transaction id is the database transaction id, not to be confused with the attachment level transaction
     * handle provided by {@link #getHandle()}.
     * </p>
     *
     * @return Database transaction id.
     */
    long getTransactionId() throws SQLException;
}
