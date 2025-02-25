// SPDX-FileCopyrightText: Copyright 2013-2016 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface TransactionListener {

    /**
     * Signals that the transaction state changed.
     * 
     * @param transaction {@link org.firebirdsql.gds.ng.FbTransaction} that changed state
     */
    void transactionStateChanged(FbTransaction transaction, TransactionState newState, TransactionState previousState);
}
