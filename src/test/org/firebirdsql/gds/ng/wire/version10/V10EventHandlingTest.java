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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.SimpleServer;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jaybird.fb.constants.TpbItems;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.Unstable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;
import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for asynchronous event notification and related features for the V10 protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10EventHandlingTest {

    @RegisterExtension
    @Order(1)
    public static final RunEnvironmentExtension runEnvironmentRule = RunEnvironmentExtension.builder()
            .requiresEventPortAvailable()
            .build();

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(10);

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

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
    public static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            TABLE_DEF,
            TRIGGER_DEF);

    private static ExecutorService executorService;
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final V10CommonConnectionInfo commonConnectionInfo = commonConnectionInfo();
    private AbstractFbWireDatabase db;

    protected V10CommonConnectionInfo commonConnectionInfo() {
        return new V10CommonConnectionInfo();
    }

    protected final IConnectionProperties getConnectionInfo() {
        return commonConnectionInfo.getDatabaseConnectionInfo();
    }

    protected final AbstractFbWireDatabase createDummyDatabase() throws SQLException {
        return commonConnectionInfo.createDummyDatabase();
    }

    protected final ProtocolCollection getProtocolCollection() {
        return commonConnectionInfo.getProtocolCollection();
    }

    protected final Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return commonConnectionInfo.getExpectedDatabaseType();
    }

    @BeforeAll
    public static void setupAll() {
        executorService = Executors.newCachedThreadPool();
    }

    @AfterAll
    public static void tearDownAll() {
        try {
            executorService.shutdown();
        } finally {
            executorService = null;
        }
    }

    @AfterEach
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
    public void testInitAsynchronousChannel() throws SQLException {
        db = createAndAttachDatabase();
        assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();

        assertTrue(channel.isConnected(), "Expected connected channel");
    }

    @Test
    public void testAsynchronousChannelClose() throws SQLException {
        db = createAndAttachDatabase();
        final SimpleChannelListener listener = new SimpleChannelListener();
        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();
        channel.addChannelListener(listener);

        channel.close();

        assertTrue(listener.hasReceivedChannelClosing(), "Expected to have received channel closing event");
        assertFalse(channel.isConnected(), "Expected channel to have been closed");
    }

    @Test
    public void testAsynchronousChannelCloseOnDatabaseDetach() throws SQLException {
        db = createAndAttachDatabase();
        final SimpleChannelListener listener = new SimpleChannelListener();
        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();
        channel.addChannelListener(listener);

        db.close();

        assertTrue(listener.hasReceivedChannelClosing(), "Expected to have received channel closing event");
        assertFalse(channel.isConnected(), "Expected channel to have been closed");
    }

    @Test
    public void testRegistrationWithAsynchronousProcessor() throws SQLException {
        db = createAndAttachDatabase();
        final SimpleChannelListener listener = new SimpleChannelListener();
        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();
        channel.addChannelListener(listener);

        AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
    }

    @Test
    public void testAsynchronousDelivery_disconnect() throws Exception {
        checkAsynchronousDisconnection(op_disconnect);
    }

    @Test
    public void testAsynchronousDelivery_exit() throws Exception {
        checkAsynchronousDisconnection(op_exit);
    }

    @Test
    public void testAsynchronousDelivery_fullEvent() throws Exception {
        final var listener = new SimpleChannelListener();
        // Using preallocated message to spend as little time on send as possible
        byte[] eventMessage = generateXdr(out -> {
            out.writeInt(op_event);
            out.writeInt(513);
            out.writeBuffer(new byte[] { 1, 3, 69, 86, 84, 3, 0, 0, 0 });
            out.writeLong(0);
            out.writeInt(7);
        });
        try (var simpleServer = new SimpleServer()) {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            executorService.submit(() -> {
                try {
                    channel.connect("localhost", simpleServer.getPort(), 1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(500, TimeUnit.MILLISECONDS).until(channel::isConnected);

            OutputStream out = simpleServer.getOutputStream();
            out.write(eventMessage);
            out.flush();

            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
                        assertEquals(1, receivedEvents.size(), "Unexpected number of events");
                        AsynchronousChannelListener.Event event = receivedEvents.get(0);
                        assertEquals(7, event.eventId(), "Unexpected eventId");
                        assertEquals(3, event.eventCount(), "Unexpected event count");
                    });
        }
    }

    @Test
    public void testAsynchronousDelivery_partialEvent() throws Exception {
        final var listener = new SimpleChannelListener();
        // Using preallocated message parts to spend as little time on send as possible
        byte[] messagePart1 = generateXdr(out -> {
            out.writeInt(op_dummy);
            out.writeInt(op_event);
            out.writeInt(513);
            out.writeBuffer(new byte[] { 1, 3, 69, 86, 84, 3, 0, 0, 0 });
        });
        byte[] messagePart2 = generateXdr(out -> {
            out.writeLong(0);
            out.writeInt(7);
        });
        try (var simpleServer = new SimpleServer()) {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            executorService.submit(() -> {
                try {
                    channel.connect("localhost", simpleServer.getPort(), 1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(500, TimeUnit.MILLISECONDS).until(channel::isConnected);

            OutputStream out = simpleServer.getOutputStream();
            out.write(messagePart1);
            // Flushing partial event to test if processing works as expected
            out.flush();

            with().pollInterval(50, TimeUnit.MILLISECONDS).pollDelay(Duration.ZERO)
                    .await().during(500, TimeUnit.MILLISECONDS).until(listener::getReceivedEvents, empty());

            out.write(messagePart2);
            out.flush();

            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
                        assertEquals(1, receivedEvents.size(), "Unexpected number of events");
                        AsynchronousChannelListener.Event event = receivedEvents.get(0);
                        assertEquals(7, event.eventId(), "Unexpected eventId");
                        assertEquals(3, event.eventCount(), "Unexpected event count");
                    });
        }
    }

    @Test
    @Unstable("Can spuriously fail on slow computers or when using power saver")
    public void testAsynchronousDelivery_largeNumberOfEvents() throws Exception {
        final var listener = new SimpleChannelListener();
        // Write a large number of events
        final int testEventCount = 1024;
        // Using preallocated message to spend as little time on send as possible
        byte[] eventMessages = generateXdr(out -> {
            for (int count = 1; count <= testEventCount; count++) {
                out.writeInt(op_event);
                out.writeInt(513);
                out.writeBuffer(new byte[] { 1, 5, 69, 86, 69, 78, 84,
                        (byte) count, (byte) (count >> 8), (byte) (count >> 16), (byte) (count >> 24) });
                out.writeLong(0);
                out.writeInt(7);
            }
        });
        try (var simpleServer = new SimpleServer()) {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            executorService.submit(() -> {
                try {
                    channel.connect("localhost", simpleServer.getPort(), 1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(500, TimeUnit.MILLISECONDS).until(channel::isConnected);

            OutputStream out = simpleServer.getOutputStream();
            out.write(eventMessages);
            out.flush();

            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
                        assertEquals(testEventCount, receivedEvents.size(), "Unexpected number of events");
                        AsynchronousChannelListener.Event event = receivedEvents.get(0);
                        assertEquals(7, event.eventId(), "Unexpected eventId");
                        assertEquals(1, event.eventCount(), "Unexpected event count");
                        AsynchronousChannelListener.Event lastEvent = receivedEvents.get(testEventCount - 1);
                        assertEquals(7, lastEvent.eventId(), "Unexpected eventId");
                        assertEquals(testEventCount, lastEvent.eventCount(), "Unexpected event count");
                    });
        }
    }

    @Test
    public void testQueueEvent_andNotification() throws Exception {
        db = createAndAttachDatabase();

        var eventHandler = new SimpleEventHandler();

        EventHandle eventHandleA = db.createEventHandle("TEST_EVENT_A", eventHandler);
        EventHandle eventHandleB = db.createEventHandle("TEST_EVENT_B", eventHandler);

        // Initial queue will return events immediately
        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);

        with().pollInterval(50, TimeUnit.MILLISECONDS)
                .await().until(eventHandler::getReceivedEventHandles, hasSize(2));

        db.countEvents(eventHandleA);
        db.countEvents(eventHandleB);

        eventHandler.clearEvents();

        db.queueEvent(eventHandleA);
        db.queueEvent(eventHandleB);

        with().pollInterval(50, TimeUnit.MILLISECONDS).pollDelay(Duration.ZERO)
                .await().during(50, TimeUnit.MILLISECONDS).until(eventHandler::getReceivedEventHandles, empty());

        FbTransaction transaction = getTransaction(db);
        final FbStatement statement = db.createStatement(transaction);
        statement.setTransaction(transaction);
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
    }

    private void checkAsynchronousDisconnection(int disconnectOperation) throws Exception {
        try (var simpleServer = new SimpleServer()) {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            executorService.submit(() -> {
                try {
                    channel.connect("localhost", simpleServer.getPort(), 1);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(500, TimeUnit.MILLISECONDS).until(channel::isConnected);

            final var out = new XdrOutputStream(simpleServer.getOutputStream());
            out.writeInt(disconnectOperation);
            out.flush();

            with().pollInterval(50, TimeUnit.MILLISECONDS)
                    .await().atMost(1, TimeUnit.SECONDS)
                    .until(() -> !channel.isConnected());
        }
    }

    @SuppressWarnings("resource")
    private AbstractFbWireDatabase createAndAttachDatabase() throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(getConnectionInfo(),
                EncodingFactory.getPlatformDefault(), getProtocolCollection());
        gdsConnection.socketConnect();
        final AbstractFbWireDatabase database = (AbstractFbWireDatabase) gdsConnection.identify();
        assertEquals(getExpectedDatabaseType(), database.getClass(), "Unexpected FbWireDatabase implementation");
        database.attach();
        return database;
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = db.createTransactionParameterBuffer();
        tpb.addArgument(TpbItems.isc_tpb_read_committed);
        tpb.addArgument(TpbItems.isc_tpb_rec_version);
        tpb.addArgument(TpbItems.isc_tpb_write);
        tpb.addArgument(TpbItems.isc_tpb_wait);
        return db.startTransaction(tpb);
    }

    private byte[] generateXdr(ThrowableConsumer<XdrOutputStream> generator) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XdrOutputStream xdrOut = new XdrOutputStream(baos);
            generator.accept(xdrOut);
            xdrOut.flush();
            return baos.toByteArray();
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    private interface ThrowableConsumer<T> {
        void accept(T t) throws Throwable;
    }
}
