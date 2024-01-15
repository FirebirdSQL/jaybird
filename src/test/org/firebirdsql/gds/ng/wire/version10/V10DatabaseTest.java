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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.common.extension.RunEnvironmentExtension.EnvironmentRequirement;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.wire.AbstractFbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.util.Collections;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Database}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10DatabaseTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(10);

    @RegisterExtension
    @Order(1)
    public static final GdsTypeExtension testType = GdsTypeExtension.excludesNativeOnly();

    @RegisterExtension
    public final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();

    private final V10CommonConnectionInfo commonConnectionInfo = commonConnectionInfo();

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

    /**
     * Test if a warning is registered with the callback if the response
     * contains an exception that is a warning.
     */
    @Test
    public void testWarningOnCallback_warningOnListener() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();
        SimpleDatabaseListener callback = new SimpleDatabaseListener();
        db.addDatabaseListener(callback);

        SQLWarning warning = new SQLWarning("test");
        db.getDatabaseWarningCallback().processWarning(warning);

        List<SQLWarning> warnings = callback.getWarnings();
        assertEquals(Collections.singletonList(warning), warnings,
                "Unexpected warnings registered or no warnings registered");
    }

    /**
     * Test if processing the response warning works even if no warning callback is registered.
     */
    @Test
    public void testProcessResponseWarnings_warning_noCallback() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        SQLWarning warning = new SQLWarning("test");

        assertDoesNotThrow(() -> db.getDatabaseWarningCallback().processWarning(warning));
    }

    /**
     * Tests if attaching to an existing database works.
     */
    @Test
    public void testBasicAttach() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

                db.attach();

                assertTrue(db.isAttached(), "Expected isAttached() to return true");
                assertNotNull(db.getServerVersion(), "Expected version string to be not null");
                assertNotEquals(GDSServerVersion.INVALID_VERSION, db.getServerVersion(),
                        "Expected version should not be invalid");
            }
        }
    }

    @Test
    public void testAttach_DoubleAttach() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                db.attach();

                SQLException exception = assertThrows(SQLException.class, db::attach,
                        "Second attach should throw exception");
                assertThat(exception, message(equalTo("Already attached to a database")));
            }
        }
    }

    /**
     * Tests if attaching to a non-existent database results in an exception
     */
    @Test
    @SuppressWarnings("java:S5783")
    public void testAttach_NonExistentDatabase() throws Exception {
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            SQLException exception = assertThrows(SQLException.class, () -> {
                gdsConnection.socketConnect();
                FbWireDatabase db = gdsConnection.identify();
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

                db.attach();
            }, "Expected the attach to fail because the database doesn't exist");
            assertThat(exception, allOf(
                    sqlStateEquals("08001"),
                    errorCodeEquals(ISCConstants.isc_io_error)));
            assertFalse(gdsConnection.isConnected());
        }
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        assumeTrue(EnvironmentRequirement.DB_LOCAL_FS.isMet(), "Requires DB on local file system");
        IConnectionProperties connectionProperties = getConnectionInfo();
        connectionProperties.setSqlDialect(3);

        File dbFile = new File(connectionProperties.getAttachObjectName());
        try (WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionProperties,
                EncodingFactory.getPlatformDefault(), getProtocolCollection())) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

            db.createDatabase();
            assertTrue(db.isAttached(), "Database should be attached after create");
            assertTrue(gdsConnection.isConnected(), "Connection should be connected after create");
            assertTrue(dbFile.exists() || !FBTestProperties.DB_SERVER_URL.equalsIgnoreCase("localhost"),
                    "Expected database file to exist (NOTE: only works on localhost)");

            db.dropDatabase();
            assertFalse(gdsConnection.isConnected());
            assertFalse(dbFile.exists());
        } finally {
            if (dbFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dbFile.delete();
            }
        }
    }

    @Test
    public void testDrop_NotAttached() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

            SQLException exception = assertThrows(SQLException.class, db::dropDatabase);
            assertThat(exception, allOf(
                    message(startsWith("The connection is not attached to a database")),
                    sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR)));
        }
    }

    @Test
    public void testDetach_NotConnected() throws Exception {
        AbstractFbWireDatabase db = createDummyDatabase();

        SQLException exception = assertThrows(SQLException.class, db::close);
        assertThat(exception, allOf(
                message(startsWith("No connection established to the database server")),
                sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_CLOSED)));
    }

    @Test
    public void testDetach_NotAttached() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

            // Detach for connected but not attached should work
            db.close();

            assertFalse(gdsConnection.isConnected(), "Expected connection closed after detach");
        }
    }

    @Test
    public void testBasicDetach() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            try {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                db.attach();

                db.close();

                assertFalse(db.isAttached(), "Expected database not attached");
                assertFalse(gdsConnection.isConnected(), "Expected connection closed");
            } finally {
                closeQuietly(db);
            }
        }
    }

    @Test
    public void testDetach_openTransactions() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                FbTransaction transaction = null;
                try {
                    SimpleDatabaseListener callback = new SimpleDatabaseListener();
                    db.addDatabaseListener(callback);

                    assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                    db.attach();
                    // Starting an active transaction
                    transaction = getTransaction(db);

                    // Triggers exception
                    SQLException exception = assertThrows(SQLException.class, db::close);
                    assertThat(exception, allOf(
                            errorCodeEquals(ISCConstants.isc_open_trans),
                            message(startsWith(getFbMessage(ISCConstants.isc_open_trans, "1")))));
                } finally {
                    if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                        transaction.commit();
                    }
                }
            }
        }
    }

    @Test
    public void testCancelOperation_abortSupported() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            try {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                db.attach();

                assumeTrue(db.isAttached(), "expected database attached");

                db.cancelOperation(ISCConstants.fb_cancel_abort);

                assertFalse(db.isAttached(), "Expected database not attached after abort");
                assertFalse(gdsConnection.isConnected(), "Expected connection closed after abort");
            } finally {
                closeQuietly(db);
            }
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

    @Disabled("Test not working (create database is translated by fbclient, not server)")
    @Test
    public void testExecuteImmediate_createDatabase() throws Exception {
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            FbWireDatabase db = gdsConnection.identify();
            assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");

            String createDb = String.format("CREATE DATABASE '%s' USER '%s' PASSWORD '%s'",
                    getDatabasePath(), DB_USER, DB_PASSWORD);
            db.executeImmediate(createDb, null);
            assertTrue(db.isAttached(), "Expected to be attached after create database");
            db.dropDatabase();
        } finally {
            //noinspection ResultOfMethodCallIgnored
            new File(getDatabasePath()).delete();
        }
    }

    private void checkCancelOperationNotSupported(int kind) throws Exception {
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                db.attach();

                assertTrue(db.isAttached(), "expected database attached");

                SQLException exception = assertThrows(SQLFeatureNotSupportedException.class,
                        () -> db.cancelOperation(kind));
                assertThat(exception,
                        message(startsWith("Cancel Operation isn't supported in this version of the wire protocol")));
            }
        }
    }

    protected WireDatabaseConnection createConnection() throws SQLException {
        return new WireDatabaseConnection(getConnectionInfo(),
                EncodingFactory.getPlatformDefault(), getProtocolCollection());
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

}
