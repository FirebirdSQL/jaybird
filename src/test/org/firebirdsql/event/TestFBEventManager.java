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
package org.firebirdsql.event;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.util.Unstable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;

/** 
 * Test the FBEventManager class
 */
public class TestFBEventManager extends FBJUnit4TestBase {

    private EventManager eventManager;
    private boolean eventManagerDisconnected;

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

    @Before
    public void setUp() throws Exception {
        executeSql(TABLE_DEF);
        executeSql(TRIGGER_DEF);

        eventManager = new FBEventManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") ||  getGdsType() == GDSType.getType("NATIVE")) {
            eventManager.setHost(DB_SERVER_URL);
        }
        eventManager.setUser(DB_USER);
        eventManager.setPassword(DB_PASSWORD);
        eventManager.setPort(DB_SERVER_PORT);
        
        // have to resolve relative path to the absolute one
        File tempFile = new File(getDatabasePath());
        eventManager.setDatabase(tempFile.getAbsolutePath());
        
        eventManager.connect();
        eventManagerDisconnected = false;
    }

    private void executeSql(String sql) throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (!eventManagerDisconnected)
            eventManager.disconnect();
        
        eventManagerDisconnected = true;
    }

    @Test
    public void testWaitForEventNoEvent() throws Exception {
        assertEquals(-1, eventManager.waitForEvent("TEST_EVENT_B", 500));
    }

    @Test
    public void testWaitForEventIndefinitely() throws Exception {
        EventWait eventWait = new EventWait("TEST_EVENT_B", 0);
        Thread waitThread = new Thread(eventWait);
        waitThread.start();
        waitThread.join(1000);
        if (waitThread.isAlive()) waitThread.interrupt();
        assertEquals(0, eventWait.getEventCount());
    }

    @Test
    public void testWaitForEventWithOccurrence() throws Exception {
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
        for (int i = 0; i < THREAD_COUNT; i++){
            final Thread t = producerThreads[i];
            t.join();
        }
        Thread.sleep(LONG_DELAY);
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

    class AccumulatingEventListener implements EventListener {

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


