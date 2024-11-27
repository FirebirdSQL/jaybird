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
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.OdsVersion;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for JNA database
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaDatabaseTest {

    // TODO Check if tests can be unified with equivalent wire protocol tests
    // TODO Assert in tests need to be checked (and more need to be added)

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.noDatabase();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    @Test
    void testBasicAttach() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (JnaDatabase db = factory.connect(connectionInfo)) {
            db.attach();

            assertTrue(db.isAttached(), "Expected isAttached() to return true");
            assertThat("Expected non-zero connection handle", db.getHandle(), not(equalTo(0)));
            assertNotNull(db.getServerVersion(), "Expected version string to be not null");
            assertNotEquals(GDSServerVersion.INVALID_VERSION, db.getServerVersion(), "Expected version should not be invalid");
        }
    }

    @Test
    void doubleAttach() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (JnaDatabase db = factory.connect(connectionInfo)) {
            db.attach();

            SQLException exception = assertThrows(SQLException.class, db::attach,
                    "Second attach should throw exception");
            assertThat(exception, message(startsWith("Already attached to a database")));
        }
    }

    @Test
    void basicStatusVectorProcessing_wrongLogin() throws Exception {
        assumeThat("Embedded does not use authentication",
                FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        // set invalid password
        connectionInfo.setPassword("abcd");
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            SQLException exception = assertThrows(SQLException.class, db::attach);
            assertThat(exception, allOf(
                    message(startsWith(getFbMessage(ISCConstants.isc_login))),
                    errorCode(equalTo(ISCConstants.isc_login))));
        } finally {
            closeQuietly(db);
        }
    }

    @Test
    void testBasicStatusVectorProcessing_wrongDatabase() throws Exception {
        // set invalid database
        final String invalidDatabaseName = FBTestProperties.getDatabasePath() + "doesnotexist";
        connectionInfo.setDatabaseName(invalidDatabaseName);
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            SQLException exception = assertThrows(SQLException.class, db::attach);
            assertThat(exception, allOf(
                    // TODO Error parameter is platform dependent
                    anyOf(
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                    invalidDatabaseName))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                    invalidDatabaseName.toUpperCase(Locale.ROOT)))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "open",
                                    invalidDatabaseName))),
                            message(startsWith(getFbMessage(ISCConstants.isc_io_error, "open",
                                    invalidDatabaseName.toUpperCase(Locale.ROOT))))
                    ),
                    errorCode(equalTo(ISCConstants.isc_io_error))
            ));
        } finally {
            closeQuietly(db);
        }
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    void testBasicCreateAndDrop() throws Exception {
        connectionInfo.setSqlDialect(3);
        JnaDatabase db = factory.connect(connectionInfo);
        File dbFile = new File(connectionInfo.getDatabaseName());
        try {
            db.createDatabase();
            assertTrue(db.isAttached(), "Database should be attached after create");
            assertTrue(dbFile.exists() || !FBTestProperties.DB_SERVER_URL.equalsIgnoreCase("localhost"),
                    "Expected database file to exist (NOTE: only works on localhost)");

            db.dropDatabase();
            assertFalse(db.isAttached(), "Database should be detached after drop");
            assertFalse(dbFile.exists(), "Expected database file to have been removed after drop");
        } finally {
            closeQuietly(db);
            if (dbFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dbFile.delete();
            }
        }
    }

    @Test
    void testDrop_NotAttached() throws Exception {
        usesDatabase.createDefaultDatabase();
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            var exception = assertThrows(SQLException.class, db::dropDatabase);
            assertThat(exception, allOf(
                    message(startsWith("The connection is not attached to a database")),
                    sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR)));
        } finally {
            closeQuietly(db);
        }
    }

    @SuppressWarnings("resource")
    @Test
    void testDetach_NotConnected() throws Exception {
        JnaDatabase db = factory.connect(connectionInfo);

        SQLException exception = assertThrows(SQLException.class, db::close);
        // Note: the error is different from the one in the pure java implementation as we cannot discern between
        // not connected and not attached
        assertThat(exception, allOf(
                message(startsWith("The connection is not attached to a database")),
                sqlStateEquals(SQLStateConstants.SQL_STATE_CONNECTION_ERROR)));
    }

    @Test
    void testBasicDetach() throws Exception {
        usesDatabase.createDefaultDatabase();
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            db.attach();

            db.close();

            assertFalse(db.isAttached(), "Expected database not attached");
        } finally {
            closeQuietly(db);
        }
    }

    @Test
    void testDetach_openTransactions() throws Exception {
        usesDatabase.createDefaultDatabase();
        JnaDatabase db = factory.connect(connectionInfo);
        FbTransaction transaction = null;
        try {
            db.attach();
            // Starting an active transaction
            transaction = getTransaction(db);

            var exception = assertThrows(SQLException.class, db::close);
            assertThat(exception, allOf(
                    errorCodeEquals(ISCConstants.isc_open_trans),
                    message(startsWith(getFbMessage(ISCConstants.isc_open_trans, "1")))));
        } finally {
            if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                transaction.commit();
            }
            closeQuietly(db);
        }
    }

    @Test
    void testCancelOperation_abortSupported() throws Exception {
        // TODO Investigate why this doesn't work.
        assumeThat("Test doesn't work with embedded protocol", FBTestProperties.GDS_TYPE, not(isEmbeddedType()));

        usesDatabase.createDefaultDatabase();
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            db.attach();
            assumeTrue(db.isAttached(), "expected database attached");
            assumeTrue(supportInfoFor(db).supportsCancelOperation(), "Test requires cancel support");

            db.cancelOperation(ISCConstants.fb_cancel_abort);

            assertFalse(db.isAttached(), "Expected database not attached after abort");
        } finally {
            closeQuietly(db);
        }
    }

    @Test
    void testExecuteImmediate_createDatabase() throws Exception {
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            String createDb = String.format("CREATE DATABASE '%s' USER '%s' PASSWORD '%s'",
                    getDatabasePath(), DB_USER, DB_PASSWORD);
            db.executeImmediate(createDb, null);
            assertTrue(db.isAttached(), "Expected to be attached after create database");
            db.dropDatabase();
        } finally {
            closeQuietly(db);
            //noinspection ResultOfMethodCallIgnored
            new File(getDatabasePath()).delete();
        }
    }

    @Test
    public void testOdsVersionInformation() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (JnaDatabase db = factory.connect(connectionInfo)) {
            db.attach();

            OdsVersion expectedOds = getDefaultSupportInfo().getDefaultOdsVersion();
            assertEquals(expectedOds, db.getOdsVersion(), "odsVersion");
            assertEquals(expectedOds.major(), db.getOdsMajor(), "odsMajor");
            assertEquals(expectedOds.minor(), db.getOdsMinor(), "odsMinor");
        }
    }

    private FbTransaction getTransaction(FbDatabase db) throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

}
