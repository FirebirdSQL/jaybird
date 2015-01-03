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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.management.FBManager;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for JNA database
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaDatabase {

    // TODO Check if tests can be unified with equivalent wire protocol tests
    // TODO Assert in tests need to be checked (and more need to be added)

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final FbClientDatabaseFactory factory = new FbClientDatabaseFactory();
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

    @BeforeClass
    public static void verifyTestType() {
        // Test is for native
        // TODO assumeTrue(FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    @Test
    public void testBasicAttach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            final JnaDatabase db = factory.connect(connectionInfo);
            try {
                db.attach();

                assertTrue("Expected isAttached() to return true", db.isAttached());
                assertThat("Expected non-zero connection handle", db.getHandle(), not(equalTo(0)));
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
    public void doubleAttach() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("Already attached to a database"));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);

        try {
            final JnaDatabase db = factory.connect(connectionInfo);
            try {
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

    @Test
    public void basicStatusVectorProcessing_wrongLogin() throws Exception {
        // set invalid password
        connectionInfo.setPassword("abcd");
        final JnaDatabase db = factory.connect(connectionInfo);

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(startsWith(getFbMessage(ISCConstants.isc_login))),
                errorCode(equalTo(ISCConstants.isc_login))
        ));

        db.attach();
    }

    @Test
    public void testBasicStatusVectorProcessing_wrongDatabase() throws Exception {
        // set invalid database
        final String invalidDatabaseName = FBTestProperties.getDatabasePath() + "doesnotexist";
        connectionInfo.setDatabaseName(invalidDatabaseName);
        final JnaDatabase db = factory.connect(connectionInfo);

        expectedException.expect(allOf(
                isA(SQLException.class),
                // TODO Error parameter might be platform dependent
                anyOf(
                        message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                invalidDatabaseName))),
                        message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)",
                                invalidDatabaseName.toUpperCase())))
                ),
                errorCode(equalTo(ISCConstants.isc_io_error))
        ));

        db.attach();
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        connectionInfo.getExtraDatabaseParameters()
                .addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
        final JnaDatabase db = factory.connect(connectionInfo);
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
        expectedException.expectMessage(equalTo("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_CONNECTION_ERROR));

        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            final JnaDatabase db = factory.connect(connectionInfo);
            db.dropDatabase();
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    @Test
    public void testDetach_NotConnected() throws Exception {
        final JnaDatabase db = factory.connect(connectionInfo);

        // Note: the error is different from the one in the pure java implementation as we cannot discern between
        // not connected and not attached
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("The connection is not attached to a database"));
        expectedException.expect(sqlStateEquals(FBSQLException.SQL_STATE_CONNECTION_ERROR));

        db.detach();
    }

    @Test
    public void testBasicDetach() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            final JnaDatabase db = factory.connect(connectionInfo);
            try {
                db.attach();

                db.detach();

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
            JnaDatabase db = factory.connect(connectionInfo);
            FbTransaction transaction = null;
            try {
                db.attach();
                // Starting an active transaction
                transaction = getTransaction(db);

                expectedException.expect(allOf(
                        errorCodeEquals(ISCConstants.isc_open_trans),
                        message(startsWith(getFbMessage(ISCConstants.isc_open_trans, "1")))
                ));

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
            JnaDatabase db = factory.connect(connectionInfo);
            try {
                db.attach();
                assumeTrue("expected database attached", db.isAttached());

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
        JnaDatabase db = factory.connect(connectionInfo);
        try {
            String createDb = String.format("CREATE DATABASE '%s' USER '%s' PASSWORD '%s'",
                    getDatabasePath(), DB_USER, DB_PASSWORD);
            db.executeImmediate(createDb, null);
            assertTrue("Expected to be attached after create database", db.isAttached());
            db.dropDatabase();
        } finally {
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
            db.detach();
        } catch (SQLException ex) {
            // ignore (TODO: log)
        }
    }
}
