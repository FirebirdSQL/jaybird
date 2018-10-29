package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.management.FBManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.getFbMessage;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class TestIDatabaseImpl {

    // TODO Assert in tests need to be checked (and more need to be added)

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final String gdsType = "FBOONATIVE";

    private final AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) GDSFactory.getDatabaseFactoryForType(GDSType.getType(gdsType));
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

    public static FBManager createFBManager() {
        return new FBManager(GDSType.getType(gdsType));
    }

    @Test
    public void testBasicAttach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo)) {
            db.attach();

            assertTrue("Expected isAttached() to return true", db.isAttached());
            assertNotNull("Expected version string to be not null", db.getServerVersion());
            assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION, db.getServerVersion());
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void doubleAttach() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("Already attached to a database"));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);

        try (IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo)) {
            db.attach();

            //Second attach should throw exception
            db.attach();
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void basicStatusProcessing_wrongLogin() throws Exception {
        assumeThat("Embedded on windows does not use authentication",
                FBTestProperties.GDS_TYPE, is(not(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME)));
        // set invalid password
        connectionInfo.setPassword("abcd");
        try (IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo)) {

            expectedException.expect(allOf(
                    isA(FbException.class),
                    message(startsWith(getFbMessage(ISCConstants.isc_login))),
                    errorCode(equalTo(ISCConstants.isc_login))
            ));

            db.attach();
        }
    }

    @Test
    public void testBasicStatusProcessing_wrongDatabase() throws Exception {
        // set invalid database
        final String invalidDatabaseName = FBTestProperties.getDatabasePath() + "doesnotexist";
        connectionInfo.setDatabaseName(invalidDatabaseName);
        try (IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo)) {

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    // TODO Error parameter is platform dependent
                    anyOf(
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                    invalidDatabaseName))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                    invalidDatabaseName.toUpperCase()))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "open",
                                    invalidDatabaseName))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "open",
                                    invalidDatabaseName.toUpperCase())))
                    ),
                    errorCode(equalTo(ISCConstants.isc_io_error))
            ));

            db.attach();
        }
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        connectionInfo.getExtraDatabaseParameters()
                .addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
        IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
        File dbFile = new File(connectionInfo.getDatabaseName());
        try {
            db.createDatabase();
            assertTrue("Database should be attached after create", db.isAttached());
            assertTrue("Expected database file to exist (NOTE: only works on localhost)",
                    dbFile.exists() || !FBTestProperties.DB_SERVER_URL.equalsIgnoreCase("localhost") );

            db.dropDatabase();
            assertFalse("Database should be detached after drop", db.isAttached());
            assertFalse("Expected database file to have been removed after drop", dbFile.exists());
        } finally {
            safelyClose(db);
            if (dbFile.exists()) {
                dbFile.delete();
            }
        }
    }

    @Test
    public void testDrop_NotAttached() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(startsWith("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
            db.dropDatabase();
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testDetach_NotConnected() throws Exception {
        IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);

        // Note: the error is different from the one in the pure java implementation as we cannot discern between
        // not connected and not attached
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(startsWith("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR));

        db.close();
    }

    @Test
    public void testBasicDetach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
            try {
                db.attach();

                db.close();

                assertFalse("Expected database not attached", db.isAttached());
            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testDetach_openTransactions() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);

        try {
            IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
            FbTransaction transaction = null;
            try {
                db.attach();
                // Starting an active transaction
                transaction = getTransaction(db);

                expectedException.expect(allOf(
                        errorCodeEquals(ISCConstants.isc_open_trans),
                        message(startsWith(getFbMessage(ISCConstants.isc_open_trans, "1")))
                ));

                db.close();
            } finally {
                if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                    transaction.commit();
                }
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testCancelOperation_abortSupported() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
            try {
                db.attach();
                assumeTrue("expected database attached", db.isAttached());
                assumeTrue("Test requires cancel support", supportInfoFor(db).supportsCancelOperation());

                db.cancelOperation(ISCConstants.fb_cancel_abort);

                assertFalse("Expected database not attached after abort", db.isAttached());
            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testExecuteImmediate_createDatabase() throws Exception {
        IDatabaseImpl db = (IDatabaseImpl) factory.connect(connectionInfo);
        try {
            String createDb = String.format("CREATE DATABASE '%s' USER '%s' PASSWORD '%s'",
                    getDatabasePath(), DB_USER, DB_PASSWORD);
            db.executeImmediate(createDb, null);
            assertTrue("Expected to be attached after create database", db.isAttached());
            db.dropDatabase();
        } finally {
            safelyClose(db);
            new File(getDatabasePath()).delete();
        }
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }

    private static void safelyClose(FbDatabase db) {
        if (db == null) return;
        try {
            db.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            // ignore
        }
    }
}
