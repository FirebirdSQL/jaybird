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

import org.firebirdsql.gds.ng.FbDatabase;
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
 * Tests for {@link org.firebirdsql.gds.ng.listeners.DatabaseListenerDispatcher}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@ExtendWith(MockitoExtension.class)
class DatabaseListenerDispatcherTest {

    private final DatabaseListenerDispatcher dispatcher = new DatabaseListenerDispatcher();
    @Mock
    private DatabaseListener listener;
    @Mock
    private FbDatabase database;

    @BeforeEach
    void setUp() {
        dispatcher.addListener(listener);
    }

    /**
     * Tests if calls to {@link DatabaseListenerDispatcher#detaching(FbDatabase)} are forwarded correctly.
     */
    @Test
    void testDetaching() {
        dispatcher.detaching(database);
        verify(listener).detaching(database);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testDetaching_withException(@Mock DatabaseListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).detaching(database);
        doThrow(new RuntimeException("test")).when(listener2).detaching(database);

        assertDoesNotThrow(() -> dispatcher.detaching(database));
        verify(listener).detaching(database);
        verify(listener2).detaching(database);
    }

    /**
     * Tests if calls to {@link DatabaseListenerDispatcher#detached(FbDatabase)} are forwarded correctly.
     */
    @Test
    void testDetached() {
        dispatcher.detached(database);
        verify(listener).detached(database);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testDetached_withException(@Mock DatabaseListener listener2) {
        dispatcher.addListener(listener2);
        doThrow(new RuntimeException("test")).when(listener).detached(database);
        doThrow(new RuntimeException("test")).when(listener2).detached(database);

        assertDoesNotThrow(() -> dispatcher.detached(database));
        verify(listener).detached(database);
        verify(listener2).detached(database);
    }

    /**
     * Tests if calls to {@link DatabaseListenerDispatcher#warningReceived(FbDatabase, SQLWarning)} are
     * forwarded correctly.
     */
    @Test
    void testWarningReceived() {
        final SQLWarning warning = new SQLWarning();

        dispatcher.warningReceived(database, warning);
        verify(listener).warningReceived(database, warning);
    }

    /**
     * Tests if listeners throwing exceptions will still cause other listeners to be notified and not result in
     * exceptions thrown to call of the dispatcher.
     */
    @Test
    void testWarningReceived_withException(@Mock DatabaseListener listener2) {
        dispatcher.addListener(listener2);
        final SQLWarning warning = new SQLWarning();
        doThrow(new RuntimeException("test")).when(listener).warningReceived(database, warning);
        doThrow(new RuntimeException("test")).when(listener2).warningReceived(database, warning);

        assertDoesNotThrow(() -> dispatcher.warningReceived(database, warning));
        verify(listener).warningReceived(database, warning);
        verify(listener2).warningReceived(database, warning);
    }

    /**
     * Tests if events are not dispatched after call to {@link DatabaseListenerDispatcher#shutdown()}.
     */
    @Test
    void testNoDispatch_afterShutdown() {
        dispatcher.shutdown();

        dispatcher.detaching(database);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if events are not dispatched after call to {@link DatabaseListenerDispatcher#shutdown()}.
     */
    @Test
    void testNoDispatch_afterShutdownAndAdd() {
        dispatcher.shutdown();
        dispatcher.addListener(listener);

        dispatcher.detaching(database);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if call to {@link DatabaseListenerDispatcher#shutdown()} will make
     * {@link DatabaseListenerDispatcher#isShutdown()} return {@code true}.
     */
    @Test
    void testShutdown() {
        assumeFalse(dispatcher.isShutdown());

        dispatcher.shutdown();

        assertTrue(dispatcher.isShutdown(), "Expected isShutDown() true after call to shutdown()");
    }

    /**
     * Tests if listener does not receive events after it has been removed using
     * {@link DatabaseListenerDispatcher#removeListener(DatabaseListener)}.
     */
    @Test
    void testNoDispatch_afterRemove() {
        dispatcher.removeListener(listener);

        dispatcher.detaching(database);
        verifyNoInteractions(listener);
    }

    /**
     * Tests if listener does not receive events after call to {@link DatabaseListenerDispatcher#removeAllListeners()}.
     */
    @Test
    void testNoDispatch_afterRemoveAll() {
        dispatcher.removeAllListeners();

        dispatcher.detaching(database);
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
