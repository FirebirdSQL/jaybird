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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.SimpleEventHandler;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.getDefaultTpb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for events in {@link JnaDatabase}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaEvents extends FBJUnit4TestBase {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsNativeOnly();

    //@formatter:off
    public static final String TABLE_DEF =
            "CREATE TABLE TEST (" +
            "     TESTVAL INTEGER NOT NULL" +
            ")";

    public static final String TRIGGER_DEF =
            "CREATE TRIGGER INSERT_TRIG " +
            "     FOR TEST AFTER INSERT " +
            "AS BEGIN " +
            "     POST_EVENT 'TEST_EVENT_A';" +
            "     POST_EVENT 'TEST_EVENT_B';" +
            "END";
    //@formatter:on

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    private JnaDatabase db;

    @After
    public final void tearDown() throws Exception {
        if (db != null && db.isAttached()) {
            try {
                db.close();
            } catch (SQLException ex) {
                log.debug("Exception on detach", ex);
            }
        }
    }

    @Test
    public void testCreateEventHandle() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        JnaEventHandle eventHandle = db.createEventHandle("TEST_EVENT", new EventHandler() {
            @Override
            public void eventOccurred(EventHandle eventHandle) {
            }
        });

        assertTrue("Event handle should have a size set", eventHandle.getSize() > 0);
    }

    @Test
    public void testQueueEvent_andNotification() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        FbTransaction transaction = getTransaction(db);
        final FbStatement statement = db.createStatement(transaction);
        statement.prepare(TABLE_DEF);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        statement.prepare(TRIGGER_DEF);
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        transaction.commit();

        SimpleEventHandler eventHandler = new SimpleEventHandler();

        EventHandle eventHandleA = db.createEventHandle("TEST_EVENT_A", eventHandler);
        EventHandle eventHandleB = db.createEventHandle("TEST_EVENT_B", eventHandler);

        // Initial queue will return events immediately
        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);
        int retry = 0;
        while (!(eventHandler.getReceivedEventHandles().contains(eventHandleA)
                && eventHandler.getReceivedEventHandles().contains(eventHandleB))
                && retry++ < 10) {
            Thread.sleep(50);
        }
        db.countEvents(eventHandleA);
        db.countEvents(eventHandleB);

        eventHandler.clearEvents();

        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);

        Thread.sleep(50);
        assertTrue("Expected events to not have been triggered", eventHandler.getReceivedEventHandles().isEmpty());

        transaction = getTransaction(db);
        statement.setTransaction(transaction);
        statement.prepare("INSERT INTO TEST VALUES (1)");
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        transaction.commit();

        retry = 0;
        while (!(eventHandler.getReceivedEventHandles().contains(eventHandleA)
                && eventHandler.getReceivedEventHandles().contains(eventHandleB))
                && retry++ < 10) {
            Thread.sleep(50);
        }
        assertEquals("Unexpected number of events received", 2, eventHandler.getReceivedEventHandles().size());

        db.countEvents(eventHandleA);
        db.countEvents(eventHandleB);
        assertEquals(1, eventHandleA.getEventCount());
        assertEquals(1, eventHandleB.getEventCount());


        // TODO Workaround for CORE-4794
        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);

        db.cancelEvent(eventHandleA);
        db.cancelEvent(eventHandleB);
    }


    @Test
    public void cancelAfterCallback_directJNA() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        FbClientLibrary lib = db.getClientLibrary();
        ISC_STATUS[] statusVector = new ISC_STATUS[20];
        SimpleEventHandler eventHandler = new SimpleEventHandler();

        final JnaEventHandle eventHandle = new JnaEventHandle("TEST_EVENT_A", eventHandler, db.getEncoding());
        int size = lib.isc_event_block(eventHandle.getEventBuffer(), eventHandle.getResultBuffer(), (short) 1,
                eventHandle.getEventNameMemory());
        eventHandle.setSize(size);

        // Queue event
        lib.isc_que_events(statusVector, db.getJnaHandle(), eventHandle.getJnaEventId(),
                (short) eventHandle.getSize(), eventHandle.getEventBuffer().getValue(),
                eventHandle.getCallback(), eventHandle.getResultBuffer().getValue());
        // Event will notify almost immediately for initial setup.
        Thread.sleep(50);

        // Cancel (no event queued right now)
        lib.isc_cancel_events(statusVector, db.getJnaHandle(), eventHandle.getJnaEventId());
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }
}
