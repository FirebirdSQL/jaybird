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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;

/**
 * Generic tests for FbTransaction.
 * <p>
 * This abstract class is subclassed by the tests for specific FbTransaction implementations.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractTransactionTest extends FBJUnit4TestBase {
    //@formatter:off
    protected static final String CREATE_KEY_VALUE_TABLE =
            "CREATE TABLE keyvalue ( " +
            " thekey INTEGER, " +
            " thevalue VARCHAR(255)" +
            ")";
    //@formatter:on

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected final FbConnectionProperties connectionInfo;

    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
    }

    protected FbDatabase db;

    @Before
    public void setUp() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            DdlHelper.executeCreateTable(con, CREATE_KEY_VALUE_TABLE);
        } finally {
            closeQuietly(con);
        }

        db = createDatabase();
        assertEquals("Unexpected FbDatabase implementation", getExpectedDatabaseType(), db.getClass());

        db.attach();
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            try {
                db.detach();
            } catch (SQLException ex) {
                log.debug("Exception on detach", ex);
            }
        }
    }

    protected abstract FbDatabase createDatabase() throws SQLException;

    protected abstract Class<? extends FbDatabase> getExpectedDatabaseType();

    @Test
    public void testBasicCommit() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);
        transaction.commit();
        assertEquals(TransactionState.COMMITTED, transaction.getState());

        assertValueForKey(key, true, value);
    }

    @Test
    public void testBasicRollback() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);
        transaction.rollback();
        assertEquals(TransactionState.ROLLED_BACK, transaction.getState());

        assertValueForKey(key, false, null);
    }

    @Test
    public void testBasicPrepareAndCommit() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);

        transaction.prepare(new byte[0]);

        assertEquals(TransactionState.PREPARED, transaction.getState());
        assertValueForKey(key, false, null);

        transaction.commit();

        assertEquals(TransactionState.COMMITTED, transaction.getState());
        assertValueForKey(key, true, value);
    }

    @Test
    public void testBasicPrepareAndRollback() throws Exception {
        FbTransaction transaction = getTransaction();
        assertEquals(TransactionState.ACTIVE, transaction.getState());
        final int key = 23;
        final String value = "TheValueIs23";
        insertKeyValue(transaction, key, value);

        transaction.prepare(new byte[] { 68, 69, 70 });

        assertEquals(TransactionState.PREPARED, transaction.getState());
        assertValueForKey(key, false, null);

        transaction.rollback();

        assertEquals(TransactionState.ROLLED_BACK, transaction.getState());
        assertValueForKey(key, false, null);
    }

    protected final void assertValueForKey(int key, boolean hasRow, String expectedValue) throws SQLException {
        Connection connection = getConnectionViaDriverManager();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = connection.prepareStatement("SELECT thevalue FROM keyvalue WHERE thekey = ?");
            pstmt.setInt(1, key);
            rs = pstmt.executeQuery();
            assertEquals("Unexpected value for rs.next()", hasRow, rs.next());
            if (hasRow) {
                assertEquals("Unexpected value for rs.getString(1)", expectedValue, rs.getString(1));
            }
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(connection);
        }
    }

    private void insertKeyValue(FbTransaction transaction, int key, String value) throws SQLException {
        FbStatement statement = db.createStatement(transaction);
        try {
            statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

            FieldValue parameter1 = statement.getParameterDescriptor().getFieldDescriptor(0).createDefaultFieldValue();
            FieldValue parameter2 = statement.getParameterDescriptor().getFieldDescriptor(1).createDefaultFieldValue();
            parameter1.setFieldData(db.getDatatypeCoder().encodeInt(key));
            parameter2.setFieldData(db.getEncoding().encodeToCharset(value));

            statement.execute(RowValue.of(parameter1, parameter2));
        } finally {
            statement.close();
        }
    }

    protected final FbTransaction getTransaction() throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }
}
