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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher;

import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractFbTransaction implements FbTransaction {

    private static final Set<TransactionState> ALLOWED_INITIAL_STATES = Collections.unmodifiableSet(
            EnumSet.of(TransactionState.ACTIVE, TransactionState.PREPARED));
    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    private final FbDatabase database;
    protected final TransactionListenerDispatcher transactionListenerDispatcher = new TransactionListenerDispatcher();
    private volatile TransactionState state;

    /**
     * Initializes AbstractFbTransaction.
     *
     * @param initialState
     *         Initial transaction state (allowed values are {@link org.firebirdsql.gds.ng.TransactionState#ACTIVE}
     *         and {@link org.firebirdsql.gds.ng.TransactionState#PREPARED}).
     * @param database
     *         FbDatabase that created this handle.
     */
    protected AbstractFbTransaction(TransactionState initialState, FbDatabase database) {
        if (!ALLOWED_INITIAL_STATES.contains(initialState)) {
            throw new IllegalArgumentException(String.format("Illegal initial transaction state: %s, allowed states are: %s", initialState, ALLOWED_INITIAL_STATES));
        }
        this.state = initialState;
        this.database = requireNonNull(database, "database");
    }

    @Override
    public final TransactionState getState() {
        return state;
    }

    /**
     * Switches current state to the supplied newState.
     *
     * @param newState
     *         New state to switch to
     * @throws SQLException
     *         If the requested state transition is not allowed or if the
     *         current state is also changed in a concurrent thread.
     */
    protected final void switchState(final TransactionState newState) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            final TransactionState currentState = state;
            if (currentState == newState) return;
            if (currentState.isValidTransition(newState)) {
                state = newState;
                transactionListenerDispatcher.transactionStateChanged(this, newState, currentState);
            } else {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_invalidTransactionStateTransition)
                        .messageParameter(newState, currentState)
                        .toSQLException();
            }
        }
    }

    @Override
    public final void addTransactionListener(TransactionListener listener) {
        transactionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void addWeakTransactionListener(TransactionListener listener) {
        transactionListenerDispatcher.addWeakListener(listener);
    }

    @Override
    public final void removeTransactionListener(TransactionListener listener) {
        transactionListenerDispatcher.removeListener(listener);
    }

    @Override
    public final void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    @Override
    public <T> T getTransactionInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException {
        final byte[] responseBuffer = getTransactionInfo(requestItems, bufferLength);
        try {
            return infoProcessor.process(responseBuffer);
        } catch (SQLException e) {
            exceptionListenerDispatcher.errorOccurred(e);
            throw e;
        }
    }

    @Override
    public long getTransactionId() throws SQLException {
        // TODO As separate class?
        return getTransactionInfo(new byte[] { ISCConstants.isc_info_tra_id }, 16, infoResponse -> {
            if (infoResponse[0] != ISCConstants.isc_info_tra_id) {
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unexpectedInfoResponse)
                        .messageParameter(
                                "transaction", "isc_info_tra_id", ISCConstants.isc_info_tra_id, infoResponse[0])
                        .toSQLException();
            }
            int length = iscVaxInteger2(infoResponse, 1);
            return iscVaxLong(infoResponse, 3, length);
        });
    }

    /**
     * @see FbAttachment#withLock()
     */
    protected final LockCloseable withLock() {
        return database.withLock();
    }

    protected FbDatabase getDatabase() {
        return database;
    }

    protected final void logUnexpectedState(TransactionState expectedState, System.Logger log) {
        final TransactionState transactionState = getState();
        if (transactionState != expectedState && log.isLoggable(System.Logger.Level.WARNING)) {
            String message = "Expected state: " + expectedState + ", but state was: " + transactionState;
            log.log(System.Logger.Level.WARNING, message + "; see debug level for stacktrace");
            if (log.isLoggable(System.Logger.Level.DEBUG)) {
                log.log(System.Logger.Level.DEBUG, message,
                        new RuntimeException("State " + expectedState + " unexpectedly not reached"));
            }
        }
    }
}
