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
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.wire.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Blob extends FBJUnit4TestBase {

    private static final String CREATE_BLOB_TABLE =
            "CREATE TABLE blob_table (" +
            "  id INTEGER PRIMARY KEY," +
            "  blobvalue BLOB SUB_TYPE BINARY" +
            ")";

    private static final String INSERT_BLOB_TABLE =
            "INSERT INTO blob_table (id, blobvalue) VALUES (?, ?)";

    private static final String SELECT_BLOB_TABLE =
            "SELECT blobvalue FROM blob_table WHERE id = ?";

    private static final Random rnd = new Random();

    @Before
    public void setUpTables() throws SQLException {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            DdlHelper.executeCreateTable(con, CREATE_BLOB_TABLE);
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    // TODO Need to add a method for verifying implementation of storage and retrieval that doesn't depend on correctness of the other
    // Currently this is possible as we use the old GDS implementation, but once it is removed we no longer have that luxury.

    /**
     * Tests retrieval of a blob (what goes in is what comes out).
     * <p>
     * Test depends on correct working of blob writing (currently using old gds implementation)
     * </p>
     */
    @Test
    public void testBlobRetrieval() throws Exception {
        final int testId = 1;
        // Use sufficiently large value so that multiple segments are used
        final byte[] testBytes = new byte[4 * Short.MAX_VALUE];
        rnd.nextBytes(testBytes);
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            PreparedStatement pstmt = con.prepareStatement(INSERT_BLOB_TABLE);
            try {
                pstmt.setInt(1, testId);
                pstmt.setBytes(2, testBytes);
                pstmt.executeUpdate();
            } finally {
                JdbcResourceHelper.closeQuietly(pstmt);
            }
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }

        // Test new implementation
        final FbWireDatabase db = createDatabaseConnection();
        try {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            statement.allocateStatement();
            statement.prepare(SELECT_BLOB_TABLE);

            RowDescriptor descriptor = statement.getParameterDescriptor();
            FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));

            statement.execute(Arrays.asList(param1));

            assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());

            statement.fetchRows(1);
            assertEquals("Expected a row", 1, listener.getRows().size());

            List<FieldValue> row = listener.getRows().get(0);
            FieldValue blobIdFieldValue = row.get(0);
            long blobId = new XSQLVAR().decodeLong(blobIdFieldValue.getFieldData());

            final FbBlob blob = db.openBlob(transaction, blobId);
            blob.open();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(4 * Short.MAX_VALUE);
            while (!blob.isEof()) {
                bos.write(blob.getSegment(blob.getMaximumSegmentSize()));
            }
            blob.close();
            transaction.commit();
            statement.close();
            byte[] result = bos.toByteArray();
            assertEquals("Unexpected length read from blob", testBytes.length, result.length);
            assertArrayEquals("Unexpected blob content", testBytes, result);

        } finally {
            db.detach();
        }
    }

    /**
     * Tests storage of a blob (what goes in is what comes out).
     * <p>
     * Test depends on correct working of blob reading (currently using old gds implementation)
     * </p>
     */
    @Test
    public void testBlobStorage() throws Exception {
        final int testId = 1;
        // Use sufficiently large value so that multiple segments are used
        final byte[] testBytes = new byte[4 * Short.MAX_VALUE];
        rnd.nextBytes(testBytes);

        final FbWireDatabase db = createDatabaseConnection();
        try {
            final SimpleStatementListener listener = new SimpleStatementListener();
            final FbTransaction transaction = getTransaction(db);
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            statement.allocateStatement();
            final FbBlob blob = db.createBlob(transaction);
            blob.open();
            int bytesWritten = 0;
            while (bytesWritten < testBytes.length) {
                // TODO the interface for writing blobs should be simpler
                byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                blob.putSegment(buffer);
                bytesWritten += buffer.length;
            }
            blob.close();

            statement.prepare(INSERT_BLOB_TABLE);
            RowDescriptor descriptor = statement.getParameterDescriptor();
            FieldValue param1 = new FieldValue(descriptor.getFieldDescriptor(0), XSQLVAR.intToBytes(testId));
            FieldValue param2 = new FieldValue(descriptor.getFieldDescriptor(1), XSQLVAR.longToBytes(blob.getBlobId()));
            statement.execute(Arrays.asList(param1, param2));
            statement.close();
            transaction.commit();
        } finally {
            db.detach();
        }

        // Use old implementation for verification
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            PreparedStatement pstmt = con.prepareStatement(SELECT_BLOB_TABLE);
            try {
                pstmt.setInt(1, testId);
                ResultSet rs = pstmt.executeQuery();
                try {
                    assertTrue("expected a row", rs.next());
                    byte[] result = rs.getBytes(1);
                    assertEquals("Unexpected length read from blob", testBytes.length, result.length);
                    assertArrayEquals("Unexpected blob content", testBytes, result);
                } finally {
                    JdbcResourceHelper.closeQuietly(rs);
                }
            } finally {
                JdbcResourceHelper.closeQuietly(pstmt);
            }
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    /**
     * Creates a database connection to the test database.
     * @return FbWireDatabase instance
     * @throws SQLException
     */
    private FbWireDatabase createDatabaseConnection() throws SQLException {
        final FbConnectionProperties connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
        WireConnection gdsConnection = new WireConnection(connectionInfo, EncodingFactory.getDefaultInstance(), ProtocolCollection.create(new Version10Descriptor()));
        gdsConnection.socketConnect();
        FbWireDatabase db = gdsConnection.identify();
        db.attach();
        return db;
    }

    private FbTransaction getTransaction(FbWireDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.createTransaction(tpb);
    }
}
