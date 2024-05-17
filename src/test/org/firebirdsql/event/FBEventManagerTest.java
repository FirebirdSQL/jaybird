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
package org.firebirdsql.event;

import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.AbstractFbAttachment;
import org.firebirdsql.gds.ng.AbstractFbDatabase;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.util.Unstable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the FBEventManager class
 */
class FBEventManagerTest {

    private static final String TABLE_DEF = """
            CREATE TABLE TEST (
              TESTVAL INTEGER NOT NULL
            )""";

    private static final String TRIGGER_DEF = """
            CREATE TRIGGER INSERT_TRIG
              FOR TEST AFTER INSERT
            AS BEGIN
              POST_EVENT 'TEST_EVENT_A';
              POST_EVENT 'TEST_EVENT_B';
              POST_EVENT 'TEST_EVENT_A';
            END""";

    @RegisterExtension
    @Order(1)
    static final RunEnvironmentExtension runEnvironmentRule = RunEnvironmentExtension.builder()
            .requiresEventPortAvailable()
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            TABLE_DEF,
            TRIGGER_DEF);

    private EventManager eventManager;
    private Connection executeSqlConnection;

    // Delay to wait after registering an event listener before testing
    private static final int SHORT_DELAY = 100;
    private static final int LONG_DELAY = 1000;

    private void setupDefaultEventManager() throws SQLException {
        eventManager = configureDefaultAttachmentProperties(new FBEventManager(getGdsType()));

        // have to resolve relative path to the absolute one
        eventManager.setDatabaseName(new File(getDatabasePath()).getAbsolutePath());

        eventManager.connect();
    }

    private void executeSql(String sql) throws SQLException {
        if (executeSqlConnection == null) {
            executeSqlConnection = getConnectionViaDriverManager();
        }
        try (var stmt = executeSqlConnection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @AfterEach
    void disconnectEventManager() throws SQLException {
        if (eventManager != null && eventManager.isConnected()) {
            eventManager.disconnect();
        }
    }

    @AfterEach
    void closeExecuteSqlConnection() throws SQLException {
        if (executeSqlConnection != null) {
            executeSqlConnection.close();
        }
    }

    @Test
    void testWaitForEventNoEvent() throws Exception {
        setupDefaultEventManager();
        assertEquals(-1, eventManager.waitForEvent("TEST_EVENT_B", 500));
    }

    @Test
    void testWaitForEventIndefinitely() throws Exception {
        setupDefaultEventManager();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> eventResult = executorService.submit(new EventWait("TEST_EVENT_B", 0));
            await().during(1, TimeUnit.SECONDS).until(() -> !eventResult.isDone());
            eventResult.cancel(true);
            assertThrows(CancellationException.class, eventResult::get);
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void testWaitForEventWithOccurrence() throws Exception {
        setupDefaultEventManager();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> eventResult = executorService.submit(new EventWait("TEST_EVENT_B", 10000));
            executeSql("INSERT INTO TEST VALUES (1)");
            await().atMost(1, TimeUnit.SECONDS).until(eventResult::isDone);
            assertEquals(1, eventResult.get());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void testWaitForEventWithOccurrenceNoTimeout() throws Exception {
        setupDefaultEventManager();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> eventResult = executorService.submit(new EventWait("TEST_EVENT_A", 0));
            executeSql("INSERT INTO TEST VALUES (2)");
            await().atMost(1, TimeUnit.SECONDS).until(eventResult::isDone);
            assertEquals(2, eventResult.get());
        } finally {
            executorService.shutdownNow();
        }
    }

    @Test
    void testBasicEventMechanism() throws Exception {
        setupDefaultEventManager();
        var ael = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael);

        executeSql("INSERT INTO TEST VALUES (2)");
        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(5 * SHORT_DELAY, TimeUnit.MILLISECONDS).until(ael::getTotalEvents, equalTo(1));
        eventManager.removeEventListener("TEST_EVENT_B", ael);

        executeSql("INSERT INTO TEST VALUES (3)");
        // No notification for events after removal of listener
        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().during(SHORT_DELAY, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertEquals(1, ael.getTotalEvents(),
                        "No notification for events after removal of listener"));
    }

    @Test
    void testMultipleListenersOnOneEvent() throws Exception {
        setupDefaultEventManager();
        var ael1 = new AccumulatingEventListener();
        var ael2 = new AccumulatingEventListener();
        var ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("NOT_REAL_EVENT", ael3);

        executeSql("INSERT INTO TEST VALUES (4)");
        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(5 * SHORT_DELAY, TimeUnit.MILLISECONDS).until(ael1::getTotalEvents, equalTo(1));
        eventManager.removeEventListener("TEST_EVENT_B", ael1);

        executeSql("INSERT INTO TEST VALUES (5)");
        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(5 * SHORT_DELAY, TimeUnit.MILLISECONDS) .until(ael2::getTotalEvents, equalTo(2));

        assertEquals(1, ael1.getTotalEvents(), "ael1 totalEvents");
        assertEquals(2, ael2.getTotalEvents(), "ael2 totalEvents");
        assertEquals(0, ael3.getTotalEvents(), "ael3 totalEvents");
    }

    @Test
    @Unstable("Performance/timing dependent, may need tweaking LONG_DELAY")
    void testLargeMultiLoad() throws Exception {
        setupDefaultEventManager();
        var ael1 = new AccumulatingEventListener();
        var ael2 = new AccumulatingEventListener();
        var ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_A", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("TEST_EVENT_A", ael3);
        eventManager.addEventListener("TEST_EVENT_B", ael3);

        final int threadCount = 5;
        final int repetitionCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        var eventProducer = new EventProducer(repetitionCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(eventProducer);
            }
        } finally {
            executorService.shutdown();
        }

        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(2 * LONG_DELAY, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertEquals(threadCount * repetitionCount * 2, ael1.getTotalEvents(), "ael1 totalEvents");
                    assertEquals(threadCount * repetitionCount, ael2.getTotalEvents(), "ael2 totalEvents");
                    assertEquals(threadCount * repetitionCount * 3, ael3.getTotalEvents(), "ael3 totalEvents");
                });
    }

    @Test
    void testSlowCallback() throws Exception {
        setupDefaultEventManager();
        var ael = new AccumulatingEventListener() {
            @SuppressWarnings("java:S2925")
            public void eventOccurred(DatabaseEvent e) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                    // ignore
                }
                super.eventOccurred(e);
            }
        };
        eventManager.addEventListener("TEST_EVENT_B", ael);

        final int repetitionCount = 5;
        for (int i = 0; i < repetitionCount; i++) {
            executeSql("INSERT INTO TEST VALUES (5)");
        }
        with().pollDelay(1200, TimeUnit.MILLISECONDS)
                .await().until(ael::getTotalEvents, equalTo(repetitionCount));
    }

    @Test
    void testMultipleManagersOnExistingConnectionOnOneEvent() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             var eventManager1 = FBEventManager.createFor(connection);
             var eventManager2 = FBEventManager.createFor(connection)) {
            eventManager1.connect();
            eventManager2.connect();

            var ael1 = new AccumulatingEventListener();
            var ael2 = new AccumulatingEventListener();
            var ael3 = new AccumulatingEventListener();
            eventManager1.addEventListener("TEST_EVENT_A", ael1);
            eventManager1.addEventListener("TEST_EVENT_B", ael2);
            eventManager1.addEventListener("NOT_REAL_EVENT", ael3);

            var ael4 = new AccumulatingEventListener();
            var ael5 = new AccumulatingEventListener();
            var ael6 = new AccumulatingEventListener();
            eventManager2.addEventListener("TEST_EVENT_B", ael4);
            eventManager2.addEventListener("TEST_EVENT_A", ael5);
            eventManager2.addEventListener("NOT_REAL_EVENT", ael6);

            executeSql("INSERT INTO TEST VALUES (4)");
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(LONG_DELAY, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        assertEquals(2, ael1.getTotalEvents(), "ael1 totalEvents");
                        assertEquals(1, ael4.getTotalEvents(), "ael4 totalEvents");
                    });
            eventManager1.removeEventListener("TEST_EVENT_A", ael1);
            eventManager2.removeEventListener("TEST_EVENT_B", ael4);

            executeSql("INSERT INTO TEST VALUES (5)");
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(LONG_DELAY, TimeUnit.MILLISECONDS)
                    .untilAsserted(() -> {
                        assertEquals(2, ael1.getTotalEvents(), "ael1 totalEvents");
                        assertEquals(2, ael2.getTotalEvents(), "ael2 totalEvents");
                        assertEquals(0, ael3.getTotalEvents(), "ael3 totalEvents");
                        assertEquals(1, ael4.getTotalEvents(), "ael4 totalEvents");
                        assertEquals(4, ael5.getTotalEvents(), "ael5 totalEvents");
                        assertEquals(0, ael6.getTotalEvents(), "ael6 totalEvents");
                    });
        }
    }

    @Test
    void testEventManagerOnExistingConnectionDisconnectsOnConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);
            eventManager.connect();
            assertTrue(eventManager.isConnected(), "Expected connected event manager");

            connection.close();

            assertFalse(eventManager.isConnected(), "Expected disconnected event manager after connection close");
        }
    }

    @Test
    void testEventManagerOnExistingConnectionThrowsExceptionOnSetter() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);

            // setting waitTimeout should work
            eventManager.setWaitTimeout(1001);

            assertThrows(UnsupportedOperationException.class, () -> eventManager.setUser("abc"),
                    "should not allow setUser");
            assertThrows(UnsupportedOperationException.class, () -> eventManager.setServerName("abc"),
                    "should not allow setHost");
            // not testing other props as that would be testing the immutable implementation of IConnectionProperties
        }
    }

    @Test
    void testEventManagerOnExistingException_connectThrowsException_afterConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);

            connection.close();

            SQLException exception = assertThrows(SQLException.class, eventManager::connect);
            assertThat(exception, errorCodeEquals(JaybirdErrorCodes.jb_notConnectedToServer));
        }
    }

    @ParameterizedTest
    @MethodSource("fatalExceptions")
    void testDefaultEventManagerDisconnectOnFatalException(SQLException exception) throws Exception {
        setupDefaultEventManager();
        AbstractFbDatabase<?> db = (AbstractFbDatabase<?>) ((FBEventManager) eventManager).getFbDatabase();
        Field eldField = AbstractFbAttachment.class.getDeclaredField("exceptionListenerDispatcher");
        eldField.setAccessible(true);
        ExceptionListenerDispatcher eld = (ExceptionListenerDispatcher) eldField.get(db);

        assertTrue(eventManager.isConnected(), "expected connected event manager");

        eld.errorOccurred(exception);

        assertFalse(eventManager.isConnected(), "expected disconnected event manager");
    }

    static Stream<SQLException> fatalExceptions() {
        return Stream.of(
                new SQLException("broken", "00000", ISCConstants.isc_net_write_err),
                new SQLException("fatal not broken", "00000", ISCConstants.isc_req_sync),
                new SQLException(new SocketException()),
                new SQLException(new SocketTimeoutException()));
    }

    /**
     * Tests if a default event manager is reported as closed when the underlying {@link FbDatabase} detaches.
     * <p>
     * NOTE: This is an unexpected scenario, as the db is owned and controlled by the default event manager behaviour.
     * </p>
     */
    @Test
    void testDefaultEventManagerDisconnectionOnDbClose() throws Exception {
        setupDefaultEventManager();
        FbDatabase db = ((FBEventManager) eventManager).getFbDatabase();

        assertTrue(eventManager.isConnected(), "expected connected event manager");

        db.close();

        assertFalse(eventManager.isConnected(), "expected disconnected event manager");
    }

    private class EventWait implements Callable<Integer> {

        private final String eventName;
        private final int timeout;

        EventWait(String eventName, int timeout) {
            this.eventName = eventName;
            this.timeout = timeout;
        }

        public Integer call() {
            try {
                return eventManager.waitForEvent(eventName, timeout);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class AccumulatingEventListener implements EventListener {

        private volatile int eventCount = 0;

        int getTotalEvents() {
            return eventCount;
        }

        @Override
        public synchronized void eventOccurred(DatabaseEvent event) {
            eventCount += event.getEventCount();
        }
    }

    private record EventProducer(int count) implements Runnable {
        @Override
        public void run() {
            try (var conn = getConnectionViaDriverManager();
                 var stmt = conn.prepareStatement("INSERT INTO TEST VALUES (?)")) {
                for (int i = 0; i < count; i++) {
                    stmt.setInt(1, i);
                    stmt.execute();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
