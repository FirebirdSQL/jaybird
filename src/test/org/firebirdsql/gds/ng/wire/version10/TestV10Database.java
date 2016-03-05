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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.AbstractFbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.management.FBManager;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
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

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(10);

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludes(
            EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME,
            NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final V10CommonConnectionInfo commonConnectionInfo;

    public TestV10Database() {
        this(new V10CommonConnectionInfo());
    }

    protected TestV10Database(V10CommonConnectionInfo commonConnectionInfo) {
        this.commonConnectionInfo = commonConnectionInfo;
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

    /**
     * Test if a warning is registered with the callback if the response
     * contains an exception that is a warning.
     */
    @Test
    public void testWarningOnCallback_warningOnListener() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SimpleDatabaseListener callback = new SimpleDatabaseListener();
        db.addDatabaseListener(callback);

        SQLWarning warning = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException(SQLWarning.class);
        db.getDatabaseWarningCallback().processWarning(warning);

        List<SQLWarning> warnings = callback.getWarnings();
        assertEquals("Unexpected warnings registered or no warnings registered", Collections.singletonList(warning), warnings);
    }

    /**
     * Test if processing the response warning works even if no warning callback is registered.
     */
    @Test
    public void testProcessResponseWarnings_warning_noCallback() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        SQLWarning warning = new FbExceptionBuilder().warning(ISCConstants.isc_numeric_out_of_range).toSQLException(SQLWarning.class);

        db.getDatabaseWarningCallback().processWarning(warning);
    }

    /**
     * Tests if attaching to an existing database works.
     */
    @Test
    public void testBasicAttach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

                db.attach();
                System.out.println(db.getHandle());

                assertTrue("Expected isAttached() to return true", db.isAttached());
                assertNotNull("Expected version string to be not null", db.getServerVersion());
                assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION,
                        db.getServerVersion());
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
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                //Second attach should throw exception
                db.attach();
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
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            try {
                gdsConnection.socketConnect();
                FbWireDatabase db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

                db.attach();
                fail("Expected the attach to fail because the database doesn't exist");
            } catch (SQLException e) {
                // NOTE: Not using expectedException because of the final assertion that isConnected is false
                // TODO Is this actually the right SQLState?
                assertThat("Expected SQLState for 'Client unable to establish connection' (08001)", e,
                        sqlStateEquals("08001"));
                // TODO Seems to be the least specific error, deeper in there is a more specific 335544734 (isc_io_open_err)
                assertThat("Expected isc_io_error (335544344)", e, errorCodeEquals(ISCConstants.isc_io_error));
            }
            assertFalse(gdsConnection.isConnected());
        }
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        IConnectionProperties connectionProperties = getConnectionInfo();
        connectionProperties.getExtraDatabaseParameters()
                .addArgument(ISCConstants.isc_dpb_sql_dialect, 3);

        File dbFile = new File(connectionProperties.getAttachObjectName());
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionProperties,
                EncodingFactory.createInstance((EncodingDefinition) null), getProtocolCollection())) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            db.createDatabase();
            assertTrue("Database should be attached after create", db.isAttached());
            assertTrue("Connection should be connected after create", gdsConnection.isConnected());
            assertTrue("Expected database file to exist (NOTE: only works on localhost)",
                    dbFile.exists() || !FBTestProperties.DB_SERVER_URL.equalsIgnoreCase("localhost"));

            db.dropDatabase();
            assertFalse(gdsConnection.isConnected());
            assertFalse(dbFile.exists());
        } finally {
            if (dbFile.exists()) {
                dbFile.delete();
            }
        }
    }

    @Test
    public void testDrop_NotAttached() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            db.dropDatabase();
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testDetach_NotConnected() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("No connection established to the database server"));
        expectedException.expect(sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_CLOSED));

        db.close();
    }

    @Test
    public void testDetach_NotAttached() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            // Detach for connected but not attached should work
            db.close();

            assertFalse("Expected connection closed after detach", gdsConnection.isConnected());
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testBasicDetach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            try {
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                db.close();

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
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                FbTransaction transaction = null;
                try {
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
                    db.close();
                } finally {
                    if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                        transaction.commit();
                    }
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testCancelOperation_abortSupported() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            try {
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

    // TODO Investigate why this doesn't work in wire protocol, but works in native
    @Ignore("Test not working")
    @Test
    public void testExecuteImmediate_createDatabase() throws Exception {
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());

            String createDb = String.format("CREATE DATABASE '%s' USER '%s' PASSWORD '%s'",
                    getDatabasePath(), DB_USER, DB_PASSWORD);
            db.executeImmediate(createDb, null);
            assertTrue("Expected to be attached after create database", db.isAttached());
            db.dropDatabase();
        } finally {
            new File(getDatabasePath()).delete();
        }
    }

    private void checkCancelOperationNotSupported(int kind) throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                assertTrue("expected database attached", db.isAttached());

                expectedException.expect(allOf(
                        isA(SQLFeatureNotSupportedException.class),
                        message(startsWith("Cancel Operation isn't supported in this version of the wire protocol"))
                ));

                db.cancelOperation(kind);
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    protected WireDatabaseConnection createConnection() throws SQLException {
        return new WireDatabaseConnection(getConnectionInfo(),
                EncodingFactory.createInstance((EncodingDefinition) null), getProtocolCollection());
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
        }
    }
}
