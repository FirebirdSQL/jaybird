package org.firebirdsql.event;

import java.io.File;
import java.sql.*;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.impl.GDSType;

/** 
 * Test the FBEventManager class
 */
public class TestFBEventManager extends FBTestBase {

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
    static final int DELAY = 500;

    public TestFBEventManager(String name) throws Exception {
        super(name);
        Class.forName("org.firebirdsql.jdbc.FBDriver");
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        executeSql(TABLE_DEF);
        executeSql(TRIGGER_DEF);

        eventManager = new FBEventManager(getGdsType());
        if (getGdsType() == GDSType.getType("PURE_JAVA") ||  getGdsType() == GDSType.getType("NATIVE")) {
            eventManager.setHost("localhost");
        }
        eventManager.setUser(DB_USER);
        eventManager.setPassword(DB_PASSWORD);
        
        // have to resolve relative path to the absolute one
        File tempFile = new File(getDatabasePath());
        eventManager.setDatabase(tempFile.getAbsolutePath());
        
        eventManager.connect();
        eventManagerDisconnected = false;
    }

    private void executeSql(String sql) throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } finally {
            conn.close();
        }
    }

    protected void tearDown() throws Exception {
        if (!eventManagerDisconnected)
            eventManager.disconnect();
        
        eventManagerDisconnected = true;
        
        super.tearDown();
    }

    public void testWaitForEventNoEvent() throws Exception {
        assertEquals(-1, eventManager.waitForEvent("TEST_EVENT_B", 500));
    }

    public void testWaitForEventIndefinitely() throws Exception {
        EventWaitThread waitThread = new EventWaitThread("TEST_EVENT_B", 0);
        waitThread.start();
        Thread.sleep(1000);
        waitThread.interrupt();
        assertEquals(0, waitThread.getEventCount());
    }

    public void testWaitForEventWithOccurrence() throws Exception {
        EventWaitThread waitThread = 
            new EventWaitThread("TEST_EVENT_B", 10000);
        waitThread.start();
        Thread.sleep(DELAY);
        executeSql("INSERT INTO TEST VALUES (1)");
        waitThread.join();
        assertEquals(1, waitThread.getEventCount());
    }

    public void testWaitForEventWithOccurrenceNoTimeout() throws Exception {
        EventWaitThread waitThread = new EventWaitThread("TEST_EVENT_A", 0);
        waitThread.start();
        Thread.sleep(DELAY);
        executeSql("INSERT INTO TEST VALUES (2)");
        waitThread.join();
        assertEquals(2, waitThread.getEventCount());
    }

    public void testBasicEventMechanism() throws Exception {
        AccumulatingEventListener ael = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael);
        Thread.sleep(DELAY);
        executeSql("INSERT INTO TEST VALUES (2)");
        Thread.sleep(DELAY);
        eventManager.removeEventListener("TEST_EVENT_B", ael);
        int totalEvents = ael.getTotalEvents();
        assertEquals("Assert that all events were recorded", 1, totalEvents);
        executeSql("INSERT INTO TEST VALUES (3)");
        assertEquals("No notification for events after removal of listener", 
                totalEvents, ael.getTotalEvents());
    }
    
    public void testAsyncEventsNoEvents() throws Exception {
        Thread.sleep(DELAY);
        try {
            eventManager.disconnect();
        } finally {
            eventManagerDisconnected = true;
        }
        
    }

    public void testMultipleListenersOnOneEvent() throws Exception {
        AccumulatingEventListener ael1 = new AccumulatingEventListener();
        AccumulatingEventListener ael2 = new AccumulatingEventListener();
        AccumulatingEventListener ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_B", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("NOT_REAL_EVENT", ael3);
        Thread.sleep(DELAY);
        executeSql("INSERT INTO TEST VALUES (4)");
        Thread.sleep(DELAY);
        eventManager.removeEventListener("TEST_EVENT_B", ael1);
        executeSql("INSERT INTO TEST VALUES (5)");
        Thread.sleep(DELAY);
        assertEquals(1, ael1.getTotalEvents());
        assertEquals(2, ael2.getTotalEvents());
        assertEquals(0, ael3.getTotalEvents());
    }

    public void testLargeMultiLoad() throws Exception {
        final int THREAD_COUNT = 5;
        final int REP_COUNT = 100;
        Thread[] producerThreads = new Thread[THREAD_COUNT];
        AccumulatingEventListener ael1 = new AccumulatingEventListener();
        AccumulatingEventListener ael2 = new AccumulatingEventListener();
        AccumulatingEventListener ael3 = new AccumulatingEventListener();
        eventManager.addEventListener("TEST_EVENT_A", ael1);
        eventManager.addEventListener("TEST_EVENT_B", ael2);
        eventManager.addEventListener("TEST_EVENT_A", ael3);
        eventManager.addEventListener("TEST_EVENT_B", ael3);
        Thread.sleep(DELAY);
        for (int i = 0; i < THREAD_COUNT; i++){
            Thread t = new EventProducerThread(REP_COUNT);
            producerThreads[i] = t;
            t.start();
        }
        for (int i = 0; i < THREAD_COUNT; i++){
            Thread t = producerThreads[i];
            t.join();
        }
        Thread.sleep(DELAY);
        eventManager.removeEventListener("TEST_EVENT_A", ael1);
        eventManager.removeEventListener("TEST_EVENT_B", ael2);
        eventManager.removeEventListener("TEST_EVENT_A", ael3);
        eventManager.removeEventListener("TEST_EVENT_B", ael3);
        assertEquals(THREAD_COUNT * REP_COUNT * 2, ael1.getTotalEvents());
        assertEquals(THREAD_COUNT * REP_COUNT, ael2.getTotalEvents());
        assertEquals(THREAD_COUNT * REP_COUNT * 3, ael3.getTotalEvents());
    }

    public void testSlowCallback() throws Exception {
        final int REP_COUNT = 5;
        AccumulatingEventListener ael = new AccumulatingEventListener(){
            public void eventOccurred(DatabaseEvent e) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie){
                }
                super.eventOccurred(e);
            }
        };
        eventManager.addEventListener("TEST_EVENT_B", ael);
        Thread.sleep(DELAY);
        for (int i = 0; i < REP_COUNT; i++){
            executeSql("INSERT INTO TEST VALUES (5)");
        }
        Thread.sleep(REP_COUNT * 300);
        eventManager.removeEventListener("TEST_EVENT_B", ael);
        assertEquals(REP_COUNT, ael.getTotalEvents());
    }


    class EventWaitThread extends Thread {
        
        private String eventName;

        private int eventCount;

        private int timeout;

        public EventWaitThread(String eventName, int timeout){
            this.eventName = eventName;
            this.timeout = timeout;
        }

        public void run(){
            try {
                eventCount = eventManager.waitForEvent(eventName, timeout);
            } catch (InterruptedException ie){
                ie.printStackTrace();
                eventCount = 0;
            } catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }

        public int getEventCount(){
            return this.eventCount;
        }
    }

    class AccumulatingEventListener implements EventListener {

        private int eventCount = 0;
       
        public int getTotalEvents(){
           return eventCount;
        } 
        
        public void eventOccurred(DatabaseEvent event){
            eventCount += event.getEventCount();
        }
    }

    class EventProducerThread extends Thread {

        private int count;

        public EventProducerThread(int count){
            this.count = count;
        }

        public void run(){
            try {
                Connection conn = TestFBEventManager.this.getConnectionViaDriverManager();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO TEST VALUES (?)");
                try {
                    for (int i = 0; i < count; i++){
                        stmt.setInt(1, i);
                        stmt.execute();
                    }
                } finally {
                    conn.close();
                }
            } catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
    }

}


