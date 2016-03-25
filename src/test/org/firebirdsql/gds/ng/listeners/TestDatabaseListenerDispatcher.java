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

import org.firebirdsql.gds.ng.FbDatabase;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLWarning;
import java.util.Arrays;

import static org.jmock.Expectations.throwException;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

/**
 * Tests for {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestDatabaseListenerDispatcher {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private DatabaseListenerDispatcher dispatcher;
    private DatabaseListener listener;
    private FbDatabase database;

    @Before
    public void setUp() {
        dispatcher = new DatabaseListenerDispatcher();
        listener = context.mock(DatabaseListener.class, "listener");
        dispatcher.addListener(listener);
        database = context.mock(FbDatabase.class, "database");
    }

    /**
     * Tests if calls to {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#detaching(org.firebirdsql.gds.ng.FbDatabase)}
     * are forwarded correctly.
     */
    @Test
    public void testDetaching() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).detaching(database);
        context.checking(expectations);

        dispatcher.detaching(database);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testDetaching_withException() {
        final DatabaseListener listener2 = context.mock(DatabaseListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (DatabaseListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).detaching(database);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.detaching(database);
    }

    /**
     * Tests if calls to {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#detached(org.firebirdsql.gds.ng.FbDatabase)}
     * forwarded correctly.
     */
    @Test
    public void testDetached() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).detached(database);
        context.checking(expectations);

        dispatcher.detached(database);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testDetached_withException() {
        final DatabaseListener listener2 = context.mock(DatabaseListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (DatabaseListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).detached(database);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.detached(database);
    }

    /**
     * Tests if calls to {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#warningReceived(org.firebirdsql.gds.ng.FbDatabase, java.sql.SQLWarning)}
     * forwarded correctly.
     */
    @Test
    public void testWarningReceived() {
        final Expectations expectations = new Expectations();
        final SQLWarning warning = new SQLWarning();
        expectations.exactly(1).of(listener).warningReceived(database, warning);
        context.checking(expectations);

        dispatcher.warningReceived(database, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testWarningReceived_withException() {
        final DatabaseListener listener2 = context.mock(DatabaseListener.class, "listener2");
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        final Expectations expectations = new Expectations();
        for (DatabaseListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).warningReceived(database, warning);
            expectations.will(throwException(new RuntimeException()));
        }
        context.checking(expectations);

        dispatcher.warningReceived(database, warning);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#shutdown()}
     */
    @Test
    public void testNoDispatch_afterShutdown() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(database);
        context.checking(expectations);

        dispatcher.shutdown();

        dispatcher.detaching(database);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#shutdown()}
     */
    @Test
    public void testNoDispatch_afterShutdownAndAdd() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(database);
        context.checking(expectations);

        dispatcher.shutdown();
        dispatcher.addListener(listener);

        dispatcher.detaching(database);
    }

    /**
     * Tests if call to {@link DatabaseListenerDispatcher#shutdown()} will make {@link DatabaseListenerDispatcher#isShutdown()}
     * return <code>true</code>
     */
    @Test
    public void testShutdown() {
        assumeFalse(dispatcher.isShutdown());

        dispatcher.shutdown();

        assertTrue("Expected isShutDown() true after call to shutdown()", dispatcher.isShutdown());
    }

    /**
     * Tests if listener does not receive events after it has been removed using
     * {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#removeListener(Object)}
     */
    @Test
    public void testNoDispatch_afterRemove() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(database);
        context.checking(expectations);

        dispatcher.removeListener(listener);

        dispatcher.detaching(database);
    }

    /**
     * Tests if listener does not receive events after call to
     * {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher#removeAllListeners()}
     */
    @Test
    public void testNoDispatch_afterRemoveAll() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(database);
        context.checking(expectations);

        dispatcher.removeAllListeners();

        dispatcher.detaching(database);
    }

    /**
     * Tests if a dispatcher cannot be added to itself.
     */
    @Test
    public void testAddingDispatcherToItself_notAllowed() {
        expectedException.expect(IllegalArgumentException.class);

        dispatcher.addListener(dispatcher);
    }
}
