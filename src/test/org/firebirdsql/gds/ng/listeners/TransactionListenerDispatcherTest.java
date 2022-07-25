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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class TransactionListenerDispatcherTest {

    private final TransactionListenerDispatcher dispatcher = new TransactionListenerDispatcher();
    @Mock
    private TransactionListener listener;
    @Mock
    private FbTransaction transaction;

    @BeforeEach
    void setUp() {
        dispatcher.addListener(listener);
    }

    /**
     * Tests if calls to {@link TransactionListenerDispatcher#transactionStateChanged(FbTransaction, TransactionState, TransactionState)}
     * are forwarded correctly.
     */
    @Test
    void testTransactionStateChanged() {
        dispatcher.transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);

        verify(listener)
                .transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testTransactionStateChanged_withException(@Mock TransactionListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener)
                .transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
        doThrow(new RuntimeException("test")).when(listener2)
                .transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);

        dispatcher.transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);

        verify(listener)
                .transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
        verify(listener2)
                .transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
    }
}
