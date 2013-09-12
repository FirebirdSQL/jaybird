/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbTransaction implements FbTransaction {

    protected final TransactionListenerDispatcher transactionListenerDispatcher = new TransactionListenerDispatcher();
    private final AtomicReference<TransactionState> state = new AtomicReference<TransactionState>(
            TransactionState.NO_TRANSACTION);

    @Override
    public final TransactionState getState() {
        return state.get();
    }

    /**
     * Switches current state to the supplied newState.
     * 
     * @param newState
     *            New state to switch to
     * @throws SQLException
     *             If the requested state transition is not allowed or if the
     *             current state is also changed in a concurrent thread.
     */
    protected final void switchState(final TransactionState newState) throws SQLException {
        final TransactionState currentState = state.get();
        if (currentState.isValidTransition(newState)) {
            if (state.compareAndSet(currentState, newState)) {
                transactionListenerDispatcher.transactionStateChanged(this, newState, currentState);
            } else {
                // Note: race condition when generating message (get() could return same value as currentState)
                // TODO Include sqlstate
                throw new SQLException(String.format(
                        "Unable to change transaction state: expected current state %s, but was %s", currentState,
                        state.get()));
            }
        } else {
            // TODO Include sqlstate
            throw new SQLException(String.format("Unable to change transaction state: state %s is not valid after %s",
                    newState, currentState));
        }
    }

    @Override
    public final void addTransactionListener(TransactionListener listener) {
        transactionListenerDispatcher.addListener(listener);
    }

    @Override
    public final void removeTransactionListener(TransactionListener listener) {
        transactionListenerDispatcher.removeListener(listener);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            try {
                rollback();
            } catch (Throwable t) {
                // ignore TODO: Log?
            }
        } finally {
            super.finalize();
        }
    }

}
