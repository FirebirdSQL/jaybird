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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.SimpleServer;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.*;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.*;
import static org.junit.Assert.*;

/**
 * Tests for asynchronous event notification and related features for the V10 protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10EventHandling extends FBJUnit4TestBase {

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludes(
            EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME,
            NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

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

    protected static final WireDatabaseConnection DUMMY_CONNECTION;
    static {
        try {
            FbConnectionProperties connectionInfo = new FbConnectionProperties();
            connectionInfo.setEncoding("NONE");

            DUMMY_CONNECTION = new WireDatabaseConnection(connectionInfo);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version10Descriptor();

    protected final FbConnectionProperties connectionInfo;
    private AbstractFbWireDatabase db;

    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
    public void testInitAsynchronousChannel() throws SQLException {
        db = createAndAttachDatabase();
        assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();

        assertTrue("Expected connected channel", channel.isConnected());
    }

    @Test
    public void testAsynchronousChannelClose() throws SQLException {
        db = createAndAttachDatabase();
        final SimpleChannelListener listener = new SimpleChannelListener();
        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();
        channel.addChannelListener(listener);

        channel.close();

        assertTrue("Expected to have received channel closing event", listener.hasReceivedChannelClosing());
        assertFalse("Expected channel to have been closed", channel.isConnected());
    }

    @Test
    public void testAsynchronousChannelCloseOnDatabaseDetach() throws SQLException {
        db = createAndAttachDatabase();
        final SimpleChannelListener listener = new SimpleChannelListener();
        final FbWireAsynchronousChannel channel = db.initAsynchronousChannel();
        channel.addChannelListener(listener);

        db.close();

        assertTrue("Expected to have received channel closing event", listener.hasReceivedChannelClosing());
        assertFalse("Expected channel to have been closed", channel.isConnected());
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
        final SimpleChannelListener listener = new SimpleChannelListener();
        final SimpleServer simpleServer = new SimpleServer();
        try {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            Thread establishChannel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.connect("localhost", simpleServer.getPort(), 1);
                    } catch (SQLException e) {
                        // suppress
                    }
                }
            });
            establishChannel.start();
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            establishChannel.join(500);
            assertTrue("Expected connected channel", channel.isConnected());

            final XdrOutputStream out = new XdrOutputStream(simpleServer.getOutputStream());
            out.write(op_event);
            out.writeInt(513);
            out.writeBuffer(new byte[] { 1, 3, 69, 86, 84, 3, 0, 0, 0 });
            out.writeLong(0);
            out.writeInt(7);
            out.flush();

            Thread.sleep(500);

            List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
            assertEquals("Unexpected number of events", 1, receivedEvents.size());
            AsynchronousChannelListener.Event event = receivedEvents.get(0);
            assertEquals("Unexpected eventId", 7, event.getEventId());
            assertEquals("Unexpected event count", 3, event.getEventCount());
        } finally {
            simpleServer.close();
        }
    }

    @Test
    public void testAsynchronousDelivery_partialEvent() throws Exception {
        final SimpleChannelListener listener = new SimpleChannelListener();
        final SimpleServer simpleServer = new SimpleServer();
        try {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            Thread establishChannel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.connect("localhost", simpleServer.getPort(), 1);
                    } catch (SQLException e) {
                        // suppress
                    }
                }
            });
            establishChannel.start();
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            establishChannel.join(500);
            assertTrue("Expected connected channel", channel.isConnected());

            final XdrOutputStream out = new XdrOutputStream(simpleServer.getOutputStream());
            out.write(op_dummy);
            out.write(op_event);
            out.writeInt(513);
            out.writeBuffer(new byte[] { 1, 3, 69, 86, 84, 3, 0, 0, 0 });
            // Flushing partial event to test if processing works as expected
            out.flush();

            Thread.sleep(500);

            List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
            assertEquals("Unexpected number of events", 0, receivedEvents.size());

            out.writeLong(0);
            out.writeInt(7);
            out.flush();

            Thread.sleep(500);

            receivedEvents = listener.getReceivedEvents();
            assertEquals("Unexpected number of events", 1, receivedEvents.size());
            AsynchronousChannelListener.Event event = receivedEvents.get(0);
            assertEquals("Unexpected eventId", 7, event.getEventId());
            assertEquals("Unexpected event count", 3, event.getEventCount());
        } finally {
            simpleServer.close();
        }
    }

    @Test
    public void testAsynchronousDelivery_largeNumberOfEvents() throws Exception {
        final SimpleChannelListener listener = new SimpleChannelListener();
        final SimpleServer simpleServer = new SimpleServer();
        try {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            channel.addChannelListener(listener);
            Thread establishChannel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.connect("localhost", simpleServer.getPort(), 1);
                    } catch (SQLException e) {
                        // suppress
                    }
                }
            });
            establishChannel.start();
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);

            assertTrue("Expected connected channel", channel.isConnected());

            final XdrOutputStream out = new XdrOutputStream(simpleServer.getOutputStream());
            // Write a large number of events
            final int testEventCount = 1024;
            for (int count = 1; count <= testEventCount; count++) {
                out.write(op_event);
                out.writeInt(513);
                out.writeBuffer(new byte[] { 1, 5, 69, 86, 69, 78, 84,
                        (byte) count, (byte) (count >> 8), (byte) (count >> 16), (byte) (count >> 24) });
                out.writeLong(0);
                out.writeInt(7);
            }
            out.flush();

            // Need to sleep for thread to process all events, might still fail on slower computers
            Thread.sleep(500);

            List<AsynchronousChannelListener.Event> receivedEvents = listener.getReceivedEvents();
            assertEquals("Unexpected number of events", testEventCount, receivedEvents.size());
            AsynchronousChannelListener.Event event = receivedEvents.get(0);
            assertEquals("Unexpected eventId", 7, event.getEventId());
            assertEquals("Unexpected event count", 1, event.getEventCount());
            AsynchronousChannelListener.Event lastEvent = receivedEvents.get(testEventCount - 1);
            assertEquals("Unexpected eventId", 7, lastEvent.getEventId());
            assertEquals("Unexpected event count", testEventCount, lastEvent.getEventCount());
        } finally {
            simpleServer.close();
        }
    }

    @Test
    public void testQueueEvent_andNotification() throws Exception {
        db = createAndAttachDatabase();
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
        Thread.sleep(50);
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

        int retry = 0;
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
    }

    private void checkAsynchronousDisconnection(int disconnectOperation) throws Exception {
        final SimpleServer simpleServer = new SimpleServer();
        try {
            final FbWireAsynchronousChannel channel = new V10AsynchronousChannel(createDummyDatabase());
            Thread establishChannel = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.connect("localhost", simpleServer.getPort(), 1);
                    } catch (SQLException e) {
                        // suppress
                    }
                }
            });
            establishChannel.start();
            simpleServer.acceptConnection();
            AsynchronousProcessor.getInstance().registerAsynchronousChannel(channel);
            establishChannel.join(500);
            assertTrue("Expected connected channel", channel.isConnected());

            final OutputStream out = simpleServer.getOutputStream();
            out.write(disconnectOperation);
            out.flush();

            Thread.sleep(500);

            assertFalse("Expected disconnected channel", channel.isConnected());
        } finally {
            simpleServer.close();
        }
    }


    private AbstractFbWireDatabase createAndAttachDatabase() throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.getDefaultInstance(), getProtocolCollection());
        gdsConnection.socketConnect();
        final AbstractFbWireDatabase database = (AbstractFbWireDatabase) gdsConnection.identify();
        assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), database.getClass());
        database.attach();
        return database;
    }

    protected AbstractFbWireDatabase createDummyDatabase() {
        return new V10Database(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);
    }

    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version10Descriptor());
    }

    protected Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return V10Database.class;
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }
}
