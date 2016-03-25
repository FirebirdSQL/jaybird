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
package org.firebirdsql.gds.ng.listeners;

import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.jmock.Expectations.throwException;

/**
 * Tests for {@link org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestTransactionListenerDispatcher {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private TransactionListenerDispatcher dispatcher;
    private TransactionListener listener;
    private FbTransaction transaction;

    @Before
    public void setUp() {
        dispatcher = new TransactionListenerDispatcher();
        listener = context.mock(TransactionListener.class, "listener");
        dispatcher.addListener(listener);
        transaction = context.mock(FbTransaction.class);
    }

    /**
     * Tests if calls to {@link org.firebirdsql.gds.ng.listeners.TransactionListenerDispatcher#transactionStateChanged(org.firebirdsql.gds.ng.FbTransaction, org.firebirdsql.gds.ng.TransactionState, org.firebirdsql.gds.ng.TransactionState)}
     * are forwarded correctly.
     */
    @Test
    public void testTransactionStateChanged() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
        context.checking(expectations);

        dispatcher.transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testTransactionStateChanged_withException() {
        final TransactionListener listener2 = context.mock(TransactionListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (TransactionListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.transactionStateChanged(transaction, TransactionState.ROLLING_BACK, TransactionState.COMMITTED);
    }
}
