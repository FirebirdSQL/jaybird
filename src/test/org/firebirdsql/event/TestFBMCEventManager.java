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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;

/**
 * Test the {@link FBMCEventManager} class
 * Based on {@link org.firebirdsql.event.TestFBEventManager} class with some changes
 */
public class TestFBMCEventManager extends FBJUnit4TestBase {

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
    }

    private void executeSql(String sql) throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMultipleManagersOnOneEvent() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            FBMCEventManager fbmcEventManager1 = new FBMCEventManager(connection);
            FBMCEventManager fbmcEventManager2 = new FBMCEventManager(connection);

            fbmcEventManager1.connect();
            fbmcEventManager2.connect();

            AccumulatingEventListener ael1 = new AccumulatingEventListener();
            AccumulatingEventListener ael2 = new AccumulatingEventListener();
            AccumulatingEventListener ael3 = new AccumulatingEventListener();
            fbmcEventManager1.addEventListener("TEST_EVENT_A", ael1);
            fbmcEventManager1.addEventListener("TEST_EVENT_B", ael2);
            fbmcEventManager1.addEventListener("NOT_REAL_EVENT", ael3);

            AccumulatingEventListener ael4 = new AccumulatingEventListener();
            AccumulatingEventListener ael5 = new AccumulatingEventListener();
            AccumulatingEventListener ael6 = new AccumulatingEventListener();
            fbmcEventManager2.addEventListener("TEST_EVENT_B", ael4);
            fbmcEventManager2.addEventListener("TEST_EVENT_A", ael5);
            fbmcEventManager2.addEventListener("NOT_REAL_EVENT", ael6);

            Thread.sleep(SHORT_DELAY);
            executeSql("INSERT INTO TEST VALUES (4)");
            Thread.sleep(LONG_DELAY);
            fbmcEventManager1.removeEventListener("TEST_EVENT_A", ael1);
            fbmcEventManager2.removeEventListener("TEST_EVENT_B", ael4);
            executeSql("INSERT INTO TEST VALUES (5)");
            Thread.sleep(SHORT_DELAY);
            assertEquals(2, ael1.getTotalEvents());
            assertEquals(2, ael2.getTotalEvents());
            assertEquals(0, ael3.getTotalEvents());
            assertEquals(1, ael4.getTotalEvents());
            assertEquals(4, ael5.getTotalEvents());
            assertEquals(0, ael6.getTotalEvents());

            fbmcEventManager1.disconnect();
            fbmcEventManager2.disconnect();
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

}


