/*
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

import org.firebirdsql.gds.ng.FbService;
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
 * Tests for {@link ServiceListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestServiceListenerDispatcher {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private ServiceListenerDispatcher dispatcher;
    private ServiceListener listener;
    private FbService service;

    @Before
    public void setUp() {
        dispatcher = new ServiceListenerDispatcher();
        listener = context.mock(ServiceListener.class, "listener");
        dispatcher.addListener(listener);
        service = context.mock(FbService.class, "service");
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#detaching(FbService)}
     * are forwarded correctly.
     */
    @Test
    public void testDetaching() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).detaching(service);
        context.checking(expectations);

        dispatcher.detaching(service);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testDetaching_withException() {
        final ServiceListener listener2 = context.mock(ServiceListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (ServiceListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).detaching(service);
            expectations.will(throwException(new Exception()));
        }
        context.checking(expectations);

        dispatcher.detaching(service);
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#detached(FbService)}
     * forwarded correctly.
     */
    @Test
    public void testDetached() {
        final Expectations expectations = new Expectations();
        expectations.exactly(1).of(listener).detached(service);
        context.checking(expectations);

        dispatcher.detached(service);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testDetached_withException() {
        final ServiceListener listener2 = context.mock(ServiceListener.class, "listener2");
        dispatcher.addListener(listener2);
        final Expectations expectations = new Expectations();
        for (ServiceListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).detached(service);
            expectations.will(throwException(new Exception()));
        }
        context.checking(expectations);

        dispatcher.detached(service);
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#warningReceived(FbService, SQLWarning)}
     * forwarded correctly.
     */
    @Test
    public void testWarningReceived() {
        final Expectations expectations = new Expectations();
        final SQLWarning warning = new SQLWarning();
        expectations.exactly(1).of(listener).warningReceived(service, warning);
        context.checking(expectations);

        dispatcher.warningReceived(service, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    public void testWarningReceived_withException() {
        final ServiceListener listener2 = context.mock(ServiceListener.class, "listener2");
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        final Expectations expectations = new Expectations();
        for (ServiceListener currentListener : Arrays.asList(listener, listener2)) {
            expectations.exactly(1).of(currentListener).warningReceived(service, warning);
            expectations.will(throwException(new Exception()));
        }
        context.checking(expectations);

        dispatcher.warningReceived(service, warning);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link ServiceListenerDispatcher#shutdown()}
     */
    @Test
    public void testNoDispatch_afterShutdown() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(service);
        context.checking(expectations);

        dispatcher.shutdown();

        dispatcher.detaching(service);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link ServiceListenerDispatcher#shutdown()}
     */
    @Test
    public void testNoDispatch_afterShutdownAndAdd() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(service);
        context.checking(expectations);

        dispatcher.shutdown();
        dispatcher.addListener(listener);

        dispatcher.detaching(service);
    }

    /**
     * Tests if call to {@link ServiceListenerDispatcher#shutdown()} will make {@link ServiceListenerDispatcher#isShutdown()}
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
     * {@link ServiceListenerDispatcher#removeListener(Object)}
     */
    @Test
    public void testNoDispatch_afterRemove() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(service);
        context.checking(expectations);

        dispatcher.removeListener(listener);

        dispatcher.detaching(service);
    }

    /**
     * Tests if listener does not receive events after call to
     * {@link ServiceListenerDispatcher#removeAllListeners()}
     */
    @Test
    public void testNoDispatch_afterRemoveAll() {
        final Expectations expectations = new Expectations();
        expectations.never(listener).detaching(service);
        context.checking(expectations);

        dispatcher.removeAllListeners();

        dispatcher.detaching(service);
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
