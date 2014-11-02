/*
 * $Id$
 * 
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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.impl.wire.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.management.FBManager;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.util.Arrays;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Database}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Database {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected static final WireConnection DUMMY_CONNECTION;
    static {
        try {
            FbConnectionProperties connectionInfo = new FbConnectionProperties();
            connectionInfo.setEncoding("NONE");

            DUMMY_CONNECTION = new WireConnection(connectionInfo);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version10Descriptor();

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

    protected FbConnectionProperties getConnectionInfo() {
        return connectionInfo;
    }

    @BeforeClass
    public static void verifyTestType() {
        // Test irrelevant for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test irrelevant for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
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

    /**
     * Test if processResponse does not throw an exception if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessResponse_noException() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        db.processResponse(genericResponse);
    }

    /**
     * Test if processResponse throws the exception in the response if the
     * exception is not a warning.
     */
    @Test
    public void testProcessResponse_exception() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SQLException exception = new FbExceptionBuilder().exception(ISCConstants.isc_numeric_out_of_range).toSQLException();
        expectedException.expect(sameInstance(exception));

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

        db.processResponse(genericResponse);
    }

    /**
     * Test if processResponse does not throw an exception if the response
     * contains an exception that is warning.
     */
    @Test
    public void testProcessResponse_warning() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        SQLException exception = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException();
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

        db.processResponse(genericResponse);
    }

    /**
     * Test if no warning is registered with the callback if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessResponseWarnings_noException() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SimpleDatabaseListener callback = new SimpleDatabaseListener();
        db.addDatabaseListener(callback);

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        db.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = callback.getWarnings();
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
    }

    /**
     * Test if no warning is registered with the callback if the response
     * contains an exception that is not a warning.
     */
    @Test
    public void testProcessResponseWarnings_exception() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SimpleDatabaseListener callback = new SimpleDatabaseListener();
        db.addDatabaseListener(callback);

        SQLException exception = new FbExceptionBuilder().exception(ISCConstants.isc_numeric_out_of_range).toSQLException();
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);
        db.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = callback.getWarnings();
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
    }

    /**
     * Test if a warning is registered with the callback if the response
     * contains an exception that is a warning.
     */
    @Test
    public void testProcessResponseWarnings_warning() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SimpleDatabaseListener callback = new SimpleDatabaseListener();
        db.addDatabaseListener(callback);

        SQLWarning warning = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException(SQLWarning.class);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, warning);
        db.processResponseWarnings(genericResponse, null);

        List<SQLWarning> warnings = callback.getWarnings();

        assertEquals("Unexpected warnings registered or no warnings registered", Arrays.asList(warning), warnings);
    }

    /**
     * Test if processing the response warning works even if no warning callback is registered.
     */
    @Test
    public void testProcessResponseWarnings_warning_noCallback() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        SQLException warning = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException();
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, warning);

        db.processResponseWarnings(genericResponse, null);
    }

    /**
     * Tests if attaching to an existing database works.
     */
    @Test
    public void testBasicAttach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

                db.attach();
                System.out.println(db.getHandle());

                assertTrue("Expected isAttached() to return true", db.isAttached());
                assertNotNull("Expected version string to be not null", db.getServerVersion());
                assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION, db.getServerVersion());
            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testAttach_DoubleAttach() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("Already attached to a database"));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                //Second attach should throw exception
                db.attach();
            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    /**
     * Tests if attaching to a non-existent database results in an exception
     */
    @Test
    public void testAttach_NonExistentDatabase() throws Exception {
        WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
        FbWireDatabase db;
        try {
            gdsConnection.socketConnect();
            db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            db.attach();
            fail("Expected the attach to fail because the database doesn't exist");
        } catch (SQLException e) {
            // NOTE: Not using expectedException because of the final assertion that isConnected is false
            // TODO Is this actually the right SQLState?
            assertThat("Expected SQLState for 'Client unable to establish connection' (08001)", e, sqlStateEquals("08001"));
            // TODO Seems to be the least specific error, deeper in there is a more specific 335544734 (isc_io_open_err)
            assertThat("Expected isc_io_error (335544344)", e, errorCodeEquals(ISCConstants.isc_io_error));
        }
        assertFalse(gdsConnection.isConnected());
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
        FbWireDatabase db;
        File dbFile = new File(gdsConnection.getDatabaseName());
        try {
            gdsConnection.socketConnect();
            db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
            dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
            dpb.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
            dpb.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

            db.createDatabase(dpb);
            assertTrue("Database should be attached after create", db.isAttached());
            assertTrue("Connection should be connected after create", gdsConnection.isConnected());
            assertTrue("Expected database file to exist (NOTE: only works on localhost)",
                    dbFile.exists() || !FBTestProperties.DB_SERVER_URL.equalsIgnoreCase("localhost") );

            db.dropDatabase();
            assertFalse(gdsConnection.isConnected());
            assertFalse(dbFile.exists());
        } finally {
            if (gdsConnection.isConnected()) {
                gdsConnection.disconnect();
            }
            if (dbFile.exists()) {
                dbFile.delete();
            }
        }
    }

    @Test
    public void testDrop_NotAttached() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_CONNECTION_ERROR));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

                db.dropDatabase();
            } finally {
                if (gdsConnection.isConnected()) {
                    gdsConnection.disconnect();
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testDetach_NotConnected() throws Exception {
        V10Database db = new V10Database(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("No connection established to the database server"));
        expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_CONNECTION_CLOSED));

        db.detach();
    }

    @Test
    public void testDetach_NotAttached() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

                // Detach for connected but not attached should work
                db.detach();

                assertFalse("Expected connection closed after detach", gdsConnection.isConnected());
            } finally {
                if (gdsConnection.isConnected()) {
                    gdsConnection.disconnect();
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testBasicDetach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                db.detach();

                assertFalse("Expected database not attached", db.isAttached());
                assertFalse("Expected connection closed", gdsConnection.isConnected());
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
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            FbTransaction transaction = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                SimpleDatabaseListener callback = new SimpleDatabaseListener();
                db.addDatabaseListener(callback);

                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();
                // Starting an active transaction
                transaction = getTransaction(db);

                expectedException.expect(allOf(
                        errorCodeEquals(ISCConstants.isc_open_trans),
                        message(startsWith(getFbMessage(ISCConstants.isc_open_trans, "1")))
                ));

                // Triggers exception
                db.detach();
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
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                assumeTrue("expected database attached", db.isAttached());

                db.cancelOperation(ISCConstants.fb_cancel_abort);

                assertFalse("Expected database not attached after abort", db.isAttached());
                assertFalse("Expected connection closed after abort", gdsConnection.isConnected());
            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testCancelOperation_raiseNotSupported() throws Exception {
        checkCancelOperationNotSupported(ISCConstants.fb_cancel_raise);
    }

    @Test
    public void testCancelOperation_disableNotSupported() throws Exception {
        checkCancelOperationNotSupported(ISCConstants.fb_cancel_disable);
    }

    @Test
    public void testCancelOperation_enableNotSupported() throws Exception {
        checkCancelOperationNotSupported(ISCConstants.fb_cancel_enable);
    }

    private void checkCancelOperationNotSupported(int kind) throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                assertTrue("expected database attached", db.isAttached());

                expectedException.expect(allOf(
                        isA(SQLFeatureNotSupportedException.class),
                        message(startsWith("Cancel Operation isn't supported in this version of the wire protocol"))
                ));

                db.cancelOperation(kind);

            } finally {
                safelyClose(db);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
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
            db.detach();
        } catch (SQLException ex) {
            // ignore (TODO: log)
        }
    }
}
