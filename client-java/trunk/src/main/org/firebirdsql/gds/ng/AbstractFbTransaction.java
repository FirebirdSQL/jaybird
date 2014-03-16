/*
 * $Id$
 * 
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.gds.ng.listeners.TransactionListener;
import org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFbTransaction implements FbTransaction {

    private final Object syncObject = new Object();
    protected final TransactionListenerDispatcher transactionListenerDispatcher = new TransactionListenerDispatcher();
    private volatile TransactionState state = TransactionState.NO_TRANSACTION;

    @Override
    public final TransactionState getState() {
        return state;
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
        synchronized (getSynchronizationObject()) {
            final TransactionState currentState = state;
            if (currentState == newState) return;
            if (currentState.isValidTransition(newState)) {
                state = newState;
                transactionListenerDispatcher.transactionStateChanged(this, newState, currentState);
            } else {
                // TODO Include sqlstate or use ISCConstants.isc_tra_state instead
                throw new SQLException(String.format("Unable to change transaction state: state %s is not valid after %s",
                        newState, currentState));
            }
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

    /**
     * Get synchronization object.
     *
     * @return object, cannot be <code>null</code>.
     */
    protected final Object getSynchronizationObject() {
        return syncObject;
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
