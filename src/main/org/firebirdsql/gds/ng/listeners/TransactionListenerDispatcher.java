// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;

/**
 * Dispatcher to maintain and notify other {@link TransactionListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class TransactionListenerDispatcher extends AbstractListenerDispatcher<TransactionListener>
        implements TransactionListener {

    private static final System.Logger log = System.getLogger(TransactionListenerDispatcher.class.getName());

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        notify(listener -> listener.transactionStateChanged(transaction, newState, previousState),
                "transactionStateChanged");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.log(System.Logger.Level.ERROR, message, throwable);
    }
}
