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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for JNA database
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaDatabase extends FBJUnit4TestBase {

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
        final JnaDatabase db = factory.connect(connectionInfo);
        try {
            db.attach();

            assertTrue("Expected isAttached() to return true", db.isAttached());
            assertThat("Expected non-zero connection handle", db.getHandle(), not(equalTo(0)));
            assertNotNull("Expected version string to be not null", db.getServerVersion());
            assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION, db.getServerVersion());
        } finally {
            if (db != null) {
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
        }
    }

    @Test
    public void testBasicStatusVectorProcessing_wrongLogin() throws Exception {
        // set invalid password
        connectionInfo.setPassword("abcd");
        final JnaDatabase db = factory.connect(connectionInfo);

        expectedException.expect(allOf(
                isA(SQLException.class),
                message(startsWith(getFbMessage(ISCConstants.isc_login))),
                errorCode(equalTo(ISCConstants.isc_login))
        ));

        try {
            db.attach();
        } finally {
            if (db != null) {
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
        }
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
                message(startsWith(getFbMessage(ISCConstants.isc_io_error, "CreateFile (open)", invalidDatabaseName))),
                errorCode(equalTo(ISCConstants.isc_io_error))
        ));

        try {
            db.attach();
        } finally {
            if (db != null) {
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
        }
    }

    @Test
    public void testDetach_openTransactions() throws Exception {
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
            if (db != null) {
                if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                    transaction.commit();
                }
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
        }
    }

    @Test
    public void testCreateAndPrepareStatement() throws Exception {
        JnaDatabase db = factory.connect(connectionInfo);
        FbTransaction transaction = null;
        try {
            db.attach();
            // Starting an active transaction
            transaction = getTransaction(db);

            JnaStatement statement = db.createStatement(transaction);
            statement.prepare("SELECT * FROM RDB$DATABASE");

            statement.close();
            transaction.commit();
        } finally {
            if (db != null) {
                if (transaction != null && transaction.getState() == TransactionState.ACTIVE) {
                    transaction.commit();
                }
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
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
}
