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

import org.firebirdsql.gds.ng.FbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLWarning;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link ServiceListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class ServiceListenerDispatcherTest {

    private final ServiceListenerDispatcher dispatcher = new ServiceListenerDispatcher();
    @Mock
    private ServiceListener listener;
    @Mock
    private FbService service;

    @BeforeEach
    void setUp() {
        dispatcher.addListener(listener);
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#detaching(FbService)} are forwarded correctly.
     */
    @Test
    void testDetaching() {
        dispatcher.detaching(service);
        verify(listener).detaching(service);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testDetaching_withException(@Mock ServiceListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).detaching(service);
        doThrow(new RuntimeException("test")).when(listener2).detaching(service);

        assertDoesNotThrow(() -> dispatcher.detaching(service));
        verify(listener).detaching(service);
        verify(listener2).detaching(service);
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#detached(FbService)} are forwarded correctly.
     */
    @Test
    void testDetached() {
        dispatcher.detached(service);
        verify(listener).detached(service);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testDetached_withException(@Mock ServiceListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).detached(service);
        doThrow(new RuntimeException("test")).when(listener2).detached(service);

        assertDoesNotThrow(() -> dispatcher.detached(service));
        verify(listener).detached(service);
        verify(listener2).detached(service);
    }

    /**
     * Tests if calls to {@link ServiceListenerDispatcher#warningReceived(FbService, SQLWarning)} are
     * forwarded correctly.
     */
    @Test
    void testWarningReceived() {
        final SQLWarning warning = new SQLWarning();

        dispatcher.warningReceived(service, warning);
        verify(listener).warningReceived(service, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testWarningReceived_withException(@Mock ServiceListener listener2) {
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        doThrow(new RuntimeException("test")).when(listener).warningReceived(service, warning);
        doThrow(new RuntimeException("test")).when(listener2).warningReceived(service, warning);

        assertDoesNotThrow(() -> dispatcher.warningReceived(service, warning));
        verify(listener).warningReceived(service, warning);
        verify(listener2).warningReceived(service, warning);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link ServiceListenerDispatcher#shutdown()}
     */
    @Test
    void testNoDispatch_afterShutdown() {
        dispatcher.shutdown();

        dispatcher.detaching(service);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if events are not dispatched after call to
     * {@link ServiceListenerDispatcher#shutdown()}
     */
    @Test
    void testNoDispatch_afterShutdownAndAdd() {
        dispatcher.shutdown();
        dispatcher.addListener(listener);

        dispatcher.detaching(service);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if call to {@link ServiceListenerDispatcher#shutdown()} will make {@link ServiceListenerDispatcher#isShutdown()}
     * return {@code true}.
     */
    @Test
    void testShutdown() {
        assumeFalse(dispatcher.isShutdown());

        dispatcher.shutdown();

        assertTrue(dispatcher.isShutdown(), "Expected isShutDown() true after call to shutdown()");
    }

    /**
     * Tests if listener does not receive events after it has been removed using
     * {@link ServiceListenerDispatcher#removeListener(ServiceListener)}
     */
    @Test
    void testNoDispatch_afterRemove() {
        dispatcher.removeListener(listener);

        dispatcher.detaching(service);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if listener does not receive events after call to {@link ServiceListenerDispatcher#removeAllListeners()}.
     */
    @Test
    void testNoDispatch_afterRemoveAll() {
        dispatcher.removeAllListeners();

        dispatcher.detaching(service);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if a dispatcher cannot be added to itself.
     */
    @Test
    void testAddingDispatcherToItself_notAllowed() {
        assertThrows(IllegalArgumentException.class, () -> dispatcher.addListener(dispatcher));
    }
}
