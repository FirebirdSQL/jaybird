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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * Dispatcher to maintain and notify other {@link TransactionListener}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class TransactionListenerDispatcher extends AbstractListenerDispatcher<TransactionListener>
        implements TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListenerDispatcher.class);

    @Override
    public void transactionStateChanged(FbTransaction transaction, TransactionState newState,
            TransactionState previousState) {
        notify(listener -> listener.transactionStateChanged(transaction, newState, previousState),
                "transactionStateChanged");
    }

    @Override
    protected void logError(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}
