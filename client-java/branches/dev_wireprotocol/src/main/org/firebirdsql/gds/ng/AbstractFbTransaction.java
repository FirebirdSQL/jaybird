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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public abstract class AbstractFbTransaction implements FbTransaction {

    private static final Object PRESENT = new Object();
    private final Map<TransactionEventListener, Object> transactionEventListeners = Collections
            .synchronizedMap(new WeakHashMap<TransactionEventListener, Object>());
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
     * @throws FbException
     *             If the requested state transition is not allowed or if the
     *             current state is also changed in a concurrent thread.
     */
    protected final void switchState(TransactionState newState) throws FbException {
        TransactionState currentState = state.get();
        if (currentState.isValidTransition(newState)) {
            if (state.compareAndSet(currentState, newState)) {
                fireTransactionStateChanged(newState, currentState);
            } else {
                // TODO: race condition when generating message (get() could return same value as currentState)
                throw new FbException(String.format(
                        "Unable to change transaction state: expected current state %s, but was %s", currentState,
                        state.get()));
            }
        } else {
            throw new FbException(String.format("Unable to change transaction state: state %s is not valid after %s",
                    newState, currentState));
        }
    }

    @Override
    public final void addTransactionEventListener(TransactionEventListener listener) {
        transactionEventListeners.put(listener, PRESENT);
    }

    @Override
    public final void removeTransactionEventListener(TransactionEventListener listener) {
        transactionEventListeners.remove(listener);
    }

    /**
     * Fires the transactionStateChanged event to all listeners.
     * 
     * @param newState
     *            The new state of the transaction
     * @param previousState
     *            The previous state of the transaction
     */
    protected final void fireTransactionStateChanged(TransactionState newState, TransactionState previousState) {
        Set<TransactionEventListener> listeners = new HashSet<TransactionEventListener>(
                transactionEventListeners.keySet());

        for (TransactionEventListener listener : listeners) {
            listener.transactionStateChanged(this, newState, previousState);
        }
    }
}
