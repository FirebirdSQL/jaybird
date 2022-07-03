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
import org.firebirdsql.common.rules.RunEnvironmentRule;
import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.util.Unstable;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.junit.Assert.*;

/** 
 * Test the FBEventManager class
 */
public class FBEventManagerTest {

    private static final RunEnvironmentRule runEnvironmentRule = RunEnvironmentExtension.builder()
            .requiresEventPortAvailable()
            .build()
            .toRule();

    private static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @ClassRule
    public static final TestRule classRules = RuleChain
            .outerRule(runEnvironmentRule)
            .around(usesDatabase);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private EventManager eventManager;

    public static final String TABLE_DEF = ""
        + "CREATE TABLE TEST ("
        + "     TESTVAL INTEGER NOT NULL"
        + ")";

    public static final String TRIGGER_DEF = ""
        + "CREATE TRIGGER INSERT_TRIG "
        + "     FOR TEST AFTER INSERT "
        + "AS BEGIN "
        + "     POST_EVENT 'TEST_EVENT_A';"
        + "     POST_EVENT 'TEST_EVENT_B';"
        + "     POST_EVENT 'TEST_EVENT_A';"
        + "END";

    // Delay to wait after registering an event listener before testing
    private static final int SHORT_DELAY = 100;
    private static final int LONG_DELAY = 1000;

    @BeforeClass
    public static void setUpTables() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeCreateTable(connection, TABLE_DEF);
            executeDDL(connection, TRIGGER_DEF);
        }
    }

    private void setupDefaultEventManager() throws SQLException {
        eventManager = new FBEventManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") ||  getGdsType() == GDSType.getType("NATIVE")) {
            eventManager.setServerName(DB_SERVER_URL);
        }
        eventManager.setUser(DB_USER);
        eventManager.setPassword(DB_PASSWORD);
        eventManager.setPortNumber(DB_SERVER_PORT);
        
        // have to resolve relative path to the absolute one
        File tempFile = new File(getDatabasePath());
        eventManager.setDatabaseName(tempFile.getAbsolutePath());
        
        eventManager.connect();
    }

    private void executeSql(String sql) throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (eventManager != null && eventManager.isConnected()) {
            eventManager.disconnect();
        }
    }

    @Test
    public void testWaitForEventNoEvent() throws Exception {
        setupDefaultEventManager();
        assertEquals(-1, eventManager.waitForEvent("TEST_EVENT_B", 500));
    }

    @Test
    public void testWaitForEventIndefinitely() throws Exception {
        setupDefaultEventManager();
        EventWait eventWait = new EventWait("TEST_EVENT_B", 0);
        Thread waitThread = new Thread(eventWait);
        waitThread.start();
        waitThread.join(1000);
        if (waitThread.isAlive()) waitThread.interrupt();
        assertEquals(0, eventWait.getEventCount());
    }

    @Test
    public void testWaitForEventWithOccurrence() throws Exception {
        setupDefaultEventManager();
        EventWait eventWait = new EventWait("TEST_EVENT_B", 10000);
        Thread waitThread = new Thread(eventWait);
        waitThread.start();
        Thread.sleep(SHORT_DELAY);
        executeSql("INSERT INTO TEST VALUES (1)");
        waitThread.join();
        assertEquals(1, eventWait.getEventCount());
    }

    @Test
    public void testWaitForEventWithOccurrenceNoTimeout() throws Exception {
        setupDefaultEventManager();
        EventWait eventWait = new EventWait("TEST_EVENT_A", 0);
        Thread waitThread = new Thread(eventWait);
        waitThread.start();
        Thread.sleep(SHORT_DELAY);
        executeSql("INSERT INTO TEST VALUES (2)");
        waitThread.join();
        assertEquals(2, eventWait.getEventCount());
    }

    @Test
    public void testBasicEventMechanism() throws Exception {
        setupDefaultEventManager();
        AccumulatingEventListener ael = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael);
        Thread.sleep(SHORT_DELAY);
        executeSql("INSERT INTO TEST VALUES (2)");
        Thread.sleep(SHORT_DELAY);
        eventManager.removeEventListener("TEST_EVENT_B", ael);
        int totalEvents = ael.getTotalEvents();
        assertEquals("Assert that all events were recorded", 1, totalEvents);
        executeSql("INSERT INTO TEST VALUES (3)");
        Thread.sleep(SHORT_DELAY);
        assertEquals("No notification for events after removal of listener", 
                totalEvents, ael.getTotalEvents());
    }

    @Test
    public void testMultipleListenersOnOneEvent() throws Exception {
        setupDefaultEventManager();
        AccumulatingEventListener ael1 = new AccumulatingEventListener();
        AccumulatingEventListener ael2 = new AccumulatingEventListener();
        AccumulatingEventListener ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("NOT_REAL_EVENT", ael3);
        Thread.sleep(SHORT_DELAY);
        executeSql("INSERT INTO TEST VALUES (4)");
        Thread.sleep(SHORT_DELAY);
        eventManager.removeEventListener("TEST_EVENT_B", ael1);
        executeSql("INSERT INTO TEST VALUES (5)");
        Thread.sleep(SHORT_DELAY);
        assertEquals(1, ael1.getTotalEvents());
        assertEquals(2, ael2.getTotalEvents());
        assertEquals(0, ael3.getTotalEvents());
    }

    @Test
    @Unstable("Performance/timing dependent, may need tweaking LONG_DELAY")
    public void testLargeMultiLoad() throws Exception {
        setupDefaultEventManager();
        final int THREAD_COUNT = 5;
        final int REP_COUNT = 100;
        final Thread[] producerThreads = new Thread[THREAD_COUNT];
        AccumulatingEventListener ael1 = new AccumulatingEventListener();
        AccumulatingEventListener ael2 = new AccumulatingEventListener();
        AccumulatingEventListener ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_A", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("TEST_EVENT_A", ael3);
        eventManager.addEventListener("TEST_EVENT_B", ael3);
        Thread.sleep(SHORT_DELAY);
        for (int i = 0; i < THREAD_COUNT; i++){
            final Thread t = new EventProducerThread(REP_COUNT);
            producerThreads[i] = t;
            t.start();
        }
        for (final Thread t : producerThreads) {
            t.join();
        }
        Thread.sleep(2 * LONG_DELAY);
        eventManager.removeEventListener("TEST_EVENT_A", ael1);
        eventManager.removeEventListener("TEST_EVENT_B", ael2);
        eventManager.removeEventListener("TEST_EVENT_A", ael3);
        eventManager.removeEventListener("TEST_EVENT_B", ael3);
        assertEquals(THREAD_COUNT * REP_COUNT * 2, ael1.getTotalEvents());
        assertEquals(THREAD_COUNT * REP_COUNT, ael2.getTotalEvents());
        assertEquals(THREAD_COUNT * REP_COUNT * 3, ael3.getTotalEvents());
    }

    @Test
    public void testSlowCallback() throws Exception {
        setupDefaultEventManager();
        final int REP_COUNT = 5;
        AccumulatingEventListener ael = new AccumulatingEventListener(){
            public void eventOccurred(DatabaseEvent e) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie){
                    // ignore
                }
                super.eventOccurred(e);
            }
        };
        eventManager.addEventListener("TEST_EVENT_B", ael);
        Thread.sleep(SHORT_DELAY);
        for (int i = 0; i < REP_COUNT; i++){
            executeSql("INSERT INTO TEST VALUES (5)");
        }
        Thread.sleep(REP_COUNT * 300 + SHORT_DELAY);
        eventManager.removeEventListener("TEST_EVENT_B", ael);
        assertEquals(REP_COUNT, ael.getTotalEvents());
    }

    @Test
    public void testMultipleManagersOnExistingConnectionOnOneEvent() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            EventManager eventManager1 = FBEventManager.createFor(connection);
            EventManager eventManager2 = FBEventManager.createFor(connection);

            eventManager1.connect();
            eventManager2.connect();

            AccumulatingEventListener ael1 = new AccumulatingEventListener();
            AccumulatingEventListener ael2 = new AccumulatingEventListener();
            AccumulatingEventListener ael3 = new AccumulatingEventListener();
            eventManager1.addEventListener("TEST_EVENT_A", ael1);
            eventManager1.addEventListener("TEST_EVENT_B", ael2);
            eventManager1.addEventListener("NOT_REAL_EVENT", ael3);

            AccumulatingEventListener ael4 = new AccumulatingEventListener();
            AccumulatingEventListener ael5 = new AccumulatingEventListener();
            AccumulatingEventListener ael6 = new AccumulatingEventListener();
            eventManager2.addEventListener("TEST_EVENT_B", ael4);
            eventManager2.addEventListener("TEST_EVENT_A", ael5);
            eventManager2.addEventListener("NOT_REAL_EVENT", ael6);

            Thread.sleep(SHORT_DELAY);
            executeSql("INSERT INTO TEST VALUES (4)");
            Thread.sleep(LONG_DELAY);
            eventManager1.removeEventListener("TEST_EVENT_A", ael1);
            eventManager2.removeEventListener("TEST_EVENT_B", ael4);
            executeSql("INSERT INTO TEST VALUES (5)");
            Thread.sleep(SHORT_DELAY);
            assertEquals(2, ael1.getTotalEvents());
            assertEquals(2, ael2.getTotalEvents());
            assertEquals(0, ael3.getTotalEvents());
            assertEquals(1, ael4.getTotalEvents());
            assertEquals(4, ael5.getTotalEvents());
            assertEquals(0, ael6.getTotalEvents());

            eventManager1.disconnect();
            eventManager2.disconnect();
        }
    }

    @Test
    public void testEventManagerOnExistingConnectionDisconnectsOnConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);
            eventManager.connect();
            assertTrue("Expected connected event manager", eventManager.isConnected());

            connection.close();

            assertFalse("Expected disconnected event manager after connection close", eventManager.isConnected());
        }
    }

    @Test
    public void testEventManagerOnExistingConnectionThrowsExceptionOnSetter() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);

            // setting waitTimeout should work
            eventManager.setWaitTimeout(1001);

            try {
                eventManager.setUser("abc");
                fail("should not allow setUser");
            } catch (UnsupportedOperationException e) {
                // expected
            }

            try {
                eventManager.setServerName("abc");
                fail("should not allow setHost");
            } catch (UnsupportedOperationException e) {
                // expected
            }

            // not testing other props as that would be testing the immutable implementation of IConnectionProperties
        }
    }

    @Test
    public void testEventManagerOnExistingException_connectThrowsException_afterConnectionClose() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            eventManager = FBEventManager.createFor(connection);

            connection.close();

            expectedException.expect(SQLException.class);
            expectedException.expect(errorCodeEquals(JaybirdErrorCodes.jb_notConnectedToServer));

            eventManager.connect();
        }
    }

    class EventWait implements Runnable {
        
        private final String eventName;
        private int eventCount;
        private final int timeout;

        public EventWait(String eventName, int timeout){
            this.eventName = eventName;
            this.timeout = timeout;
        }

        public void run(){
            try {
                eventCount = eventManager.waitForEvent(eventName, timeout);
            } catch (InterruptedException ie){
                eventCount = 0;
            } catch (Exception e){
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        public int getEventCount(){
            return this.eventCount;
        }
    }

    static class AccumulatingEventListener implements EventListener {

        private volatile int eventCount = 0;
       
        public int getTotalEvents(){
           return eventCount;
        } 
        
        public synchronized void eventOccurred(DatabaseEvent event){
            eventCount += event.getEventCount();
        }
    }

    static class EventProducerThread extends Thread {

        private int count;

        public EventProducerThread(int count){
            this.count = count;
        }

        public void run(){
            try (Connection conn = getConnectionViaDriverManager();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO TEST VALUES (?)")) {
                for (int i = 0; i < count; i++) {
                    stmt.setInt(1, i);
                    stmt.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

}
