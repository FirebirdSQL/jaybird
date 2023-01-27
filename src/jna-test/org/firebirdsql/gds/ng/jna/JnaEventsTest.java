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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.SimpleEventHandler;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.ISC_STATUS;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.firebirdsql.common.FBTestProperties.getDefaultTpb;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for events in {@link JnaDatabase}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaEventsTest {

    private final Logger log = LoggerFactory.getLogger(JnaEventsTest.class);

    @RegisterExtension
    @Order(1)
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

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
            END""";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            TABLE_DEF,
            TRIGGER_DEF);

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    private JnaDatabase db;

    @AfterEach
    void tearDown() {
        if (db != null && db.isAttached()) {
            try {
                db.close();
            } catch (SQLException ex) {
                log.debug("Exception on detach", ex);
            }
        }
    }

    @Test
    void testCreateEventHandle() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        JnaEventHandle eventHandle = db.createEventHandle("TEST_EVENT", eventHandle1 -> { });

        assertTrue(eventHandle.getSize() > 0, "Event handle should have a size set");
    }

    @Test
    void testQueueEvent_andNotification() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        var eventHandler = new SimpleEventHandler();

        EventHandle eventHandleA = db.createEventHandle("TEST_EVENT_A", eventHandler);
        EventHandle eventHandleB = db.createEventHandle("TEST_EVENT_B", eventHandler);

        // Initial queue will return events immediately
        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);
        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(1, TimeUnit.SECONDS)
                .until(eventHandler::getReceivedEventHandles, hasItems(eventHandleA, eventHandleB));

        db.countEvents(eventHandleA);
        db.countEvents(eventHandleB);

        eventHandler.clearEvents();

        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);

        with().pollInterval(50, TimeUnit.MILLISECONDS).pollDelay(Duration.ZERO).await()
                .during(50, TimeUnit.MILLISECONDS).until(eventHandler::getReceivedEventHandles, empty());

        FbTransaction transaction = getTransaction(db);
        FbStatement statement = db.createStatement(transaction);
        statement.prepare("INSERT INTO TEST VALUES (1)");
        statement.execute(RowValue.EMPTY_ROW_VALUE);
        transaction.commit();

        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().atMost(1, TimeUnit.SECONDS)
                .until(eventHandler::getReceivedEventHandles, hasItems(eventHandleA, eventHandleB));

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
    void cancelAfterCallback_directJNA() throws Exception {
        db = factory.connect(connectionInfo);
        db.attach();

        FbClientLibrary lib = db.getClientLibrary();
        var statusVector = new ISC_STATUS[20];
        var eventHandler = new SimpleEventHandler();

        final var eventHandle = new JnaEventHandle("TEST_EVENT_A", eventHandler, db.getEncoding());
        int size = lib.isc_event_block(eventHandle.getEventBuffer(), eventHandle.getResultBuffer(), (short) 1,
                eventHandle.getEventNameMemory());
        eventHandle.setSize(size);

        // Queue event
        lib.isc_que_events(statusVector, db.getJnaHandle(), eventHandle.getJnaEventId(),
                (short) eventHandle.getSize(), eventHandle.getEventBuffer().getValue(),
                eventHandle.getCallback(), eventHandle.getResultBuffer().getValue());
        // Event will notify almost immediately for initial setup.
        with().pollDelay(50, TimeUnit.MILLISECONDS).untilAsserted(() ->
                // Cancel (no event queued right now)
                lib.isc_cancel_events(statusVector, db.getJnaHandle(), eventHandle.getJnaEventId()));
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }
}
