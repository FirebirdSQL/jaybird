package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestIEventBlockImpl extends FBJUnit4TestBase {

    private static final String gdsType = "FBOONATIVE";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) GDSFactory.getDatabaseFactoryForType(GDSType.getType(gdsType));
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
    
    private final FbConnectionProperties connectionInfo;
    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
    }

    private IDatabaseImpl db;

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
        db = (IDatabaseImpl) factory.connect(connectionInfo);
        db.attach();

        IEventBlockImpl eventBlock = (IEventBlockImpl)db.createEventHandle("TEST_EVENT", new EventHandler() {
            @Override
            public void eventOccurred(EventHandle eventHandle) {
            }
        });

        assertTrue("Event handle should have a size set", eventBlock.getSize() > 0);
    }

    @Test
    public void testQueueEvent_andNotification() throws Exception {
        db = (IDatabaseImpl) factory.connect(connectionInfo);
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


    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }
}
