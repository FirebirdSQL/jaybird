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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Transaction extends FBJUnit4TestBase {

    private static final String CREATE_KEY_VALUE_TABLE =
            "CREATE TABLE keyvalue ( " +
                    " thekey INTEGER, " +
                    " thevalue VARCHAR(255)" +
                    ")";

    private final FbConnectionProperties connectionInfo;
    private FbWireDatabase db;

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

    @BeforeClass
    public static void verifyTestType() {
        // Test irrelevant for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test irrelevant for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    @Before
    public void setUp() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            DdlHelper.executeCreateTable(con, CREATE_KEY_VALUE_TABLE);
        } finally {
            closeQuietly(con);
        }

        WireConnection gdsConnection = new WireConnection(connectionInfo, EncodingFactory.getDefaultInstance(), ProtocolCollection.create(new Version10Descriptor()));
        gdsConnection.socketConnect();
        db = gdsConnection.identify();
        assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());

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

    private void assertValueForKey(int key, boolean hasRow, String expectedValue) throws SQLException {
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
            statement.allocateStatement();
            statement.prepare("INSERT INTO keyvalue (thekey, thevalue) VALUES (?, ?)");

            FieldValue parameter1 = statement.getParameterDescriptor().getFieldDescriptor(0).createDefaultFieldValue();
            FieldValue parameter2 = statement.getParameterDescriptor().getFieldDescriptor(1).createDefaultFieldValue();
            parameter1.setFieldData(new XSQLVAR().encodeInt(key));
            parameter2.setFieldData(db.getEncoding().encodeToCharset(value));

            statement.execute(Arrays.asList(parameter1, parameter2));
        } finally {
            statement.close();
        }
    }

    private FbTransaction getTransaction() throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.createTransaction(tpb);
    }
}
