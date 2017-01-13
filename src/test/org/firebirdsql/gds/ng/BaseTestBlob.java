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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.fields.FieldValue;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Abstract test class for blob related tests shared by the wire and JNA implementation.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class BaseTestBlob extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected SimpleStatementListener listener;
    protected FbTransaction transaction;
    protected FbStatement statement;

    private static final int BASE_CONTENT_SIZE = 16384;

    //@formatter:off
    protected static final String CREATE_BLOB_TABLE =
            "CREATE TABLE blob_table (" +
            "  id INTEGER PRIMARY KEY," +
            "  blobvalue BLOB SUB_TYPE 0" +
            ")";

    protected static final String CREATE_PROC_FILL_BINARY_BLOB =
            "CREATE PROCEDURE FILL_BINARY_BLOB \n" +
            " (\n" +
            "    ID INTEGER,\n" +
            "    BASE_CONTENT VARCHAR(" + BASE_CONTENT_SIZE + ") CHARACTER SET OCTETS,\n" +
            "    REQUIRED_SIZE INTEGER\n" +
            " ) \n" +
            "AS \n" +
            "  DECLARE VARIABLE REMAINING INTEGER;\n" +
            "  DECLARE VARIABLE BASE_CONTENT_SIZE INTEGER;\n" +
            "  DECLARE VARIABLE TEMP_BLOB BLOB SUB_TYPE 0;\n" +
            "BEGIN\n" +
            "  REMAINING = REQUIRED_SIZE;\n" +
            "  TEMP_BLOB = '';\n" +
            "  BASE_CONTENT_SIZE = OCTET_LENGTH(BASE_CONTENT);\n" +
            "  WHILE (REMAINING > 0) DO\n" +
            "  BEGIN\n" +
            "    TEMP_BLOB = TEMP_BLOB || \n" +
            "        CASE \n" +
            "            WHEN REMAINING > BASE_CONTENT_SIZE \n" +
            "            THEN BASE_CONTENT \n" +
            "            ELSE LEFT(BASE_CONTENT, REMAINING) \n" +
            "        END;\n" +
            "    REMAINING = REMAINING - BASE_CONTENT_SIZE;\n" +
            "  END\n" +
            "  INSERT INTO blob_table (id, blobvalue) VALUES (:ID, :TEMP_BLOB);\n" +
            "END";

    protected static final String EXECUTE_FILL_BINARY_BLOB =
            "{call FILL_BINARY_BLOB(?, ?, ?)}";

    protected static final String CREATE_PROC_CHECK_BINARY_BLOB =
            "CREATE PROCEDURE CHECK_BINARY_BLOB \n" +
            " (  \n" +
            "    ID INTEGER,\n" +
            "    BASE_CONTENT VARCHAR( " + BASE_CONTENT_SIZE + " ) CHARACTER SET OCTETS,\n" +
            "    REQUIRED_SIZE INTEGER \n" +
            " ) \n" +
            "RETURNS \n" +
            " ( MATCHES SMALLINT )\n" +
            "AS \n" +
            "  DECLARE VARIABLE REMAINING INTEGER;\n" +
            "  DECLARE VARIABLE BASE_CONTENT_SIZE INTEGER;\n" +
            "  DECLARE VARIABLE TEMP_BLOB BLOB SUB_TYPE 0;\n" +
            "BEGIN\n" +
            "  REMAINING = REQUIRED_SIZE;\n" +
            "  TEMP_BLOB = '';\n" +
            "  BASE_CONTENT_SIZE = OCTET_LENGTH(BASE_CONTENT);\n" +
            "  WHILE (REMAINING > 0) DO\n" +
            "  BEGIN\n" +
            "    TEMP_BLOB = TEMP_BLOB || \n" +
            "        CASE \n" +
            "            WHEN REMAINING > BASE_CONTENT_SIZE \n" +
            "            THEN BASE_CONTENT \n" +
            "            ELSE LEFT(BASE_CONTENT, REMAINING) \n" +
            "        END;\n" +
            "    REMAINING = REMAINING - BASE_CONTENT_SIZE;\n" +
            "  END\n" +
            "  SELECT \n" +
            "        CASE \n" +
            "          WHEN blobvalue IS DISTINCT FROM :TEMP_BLOB \n" +
            "          THEN 0 \n" +
            "          ELSE 1 \n" +
            "        END \n" +
            "      FROM blob_table \n" +
            "      WHERE ID = :ID \n" +
            "      INTO :MATCHES;\n" +
            "END";

    protected static final String EXECUTE_CHECK_BINARY_BLOB =
            "{call CHECK_BINARY_BLOB(?, ?, ?)}";

    protected static final String INSERT_BLOB_TABLE =
            "INSERT INTO blob_table (id, blobvalue) VALUES (?, ?)";

    protected static final String SELECT_BLOB_TABLE =
            "SELECT blobvalue FROM blob_table WHERE id = ?";
    //@formatter:on

    protected static final Random rnd = new Random();

    /**
     * Queries the blob table for the blob id of the record with the specified (row) id.
     * @param testId Id of the row
     * @param db database to use
     * @return Blob id
     * @throws SQLException For errors executing the query
     */
    protected long getBlobId(int testId, FbDatabase db) throws SQLException {
        listener = new SimpleStatementListener();
        transaction = getTransaction(db);
        statement = db.createStatement(transaction);
        statement.addStatementListener(listener);
        statement.prepare(SELECT_BLOB_TABLE);

        FieldValue param1 = new FieldValue(db.getDatatypeCoder().encodeInt(testId));

        statement.execute(RowValue.of(param1));

        assertEquals("Expected hasResultSet to be set to true", Boolean.TRUE, listener.hasResultSet());

        statement.fetchRows(1);
        assertEquals("Expected a row", 1, listener.getRows().size());

        RowValue row = listener.getRows().get(0);
        FieldValue blobIdFieldValue = row.getFieldValue(0);
        return db.getDatatypeCoder().decodeLong(blobIdFieldValue.getFieldData());
    }

    @Before
    public final void setUp() throws SQLException {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            assumeTrue("Test requires CASE support", supportInfoFor(con).supportsCase());

            DdlHelper.executeCreateTable(con, CREATE_BLOB_TABLE);
            DdlHelper.executeDDL(con, CREATE_PROC_FILL_BINARY_BLOB);
            DdlHelper.executeDDL(con, CREATE_PROC_CHECK_BINARY_BLOB);
        } finally {
            closeQuietly(con);
        }
    }

    /**
     * Generates byte array of {@link #BASE_CONTENT_SIZE} with random content.
     *
     * @return Array
     */
    protected byte[] generateBaseContent() {
        byte[] baseContent = new byte[BASE_CONTENT_SIZE];
        rnd.nextBytes(baseContent);
        return baseContent;
    }

    /**
     * Generates blob content of the specified size based on baseContent.
     *
     * @param baseContent Base content used for populating the array
     * @param requiredSize The required content size
     * @return Blob content array
     */
    protected byte[] generateBlobContent(byte[] baseContent, int requiredSize) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(requiredSize);
        while (bos.size() < requiredSize) {
            bos.write(baseContent, 0, Math.min(baseContent.length, requiredSize - bos.size()));
        }
        return bos.toByteArray();
    }

    /**
     * Checks if the blob content is of the required size and matches the expected content based on baseContent.
     *
     * @param blobContent Blob content
     * @param baseContent Base content
     * @param requiredSize Required size
     * @return <code>true</code> content matches, <code>false</code> otherwise
     */
    protected boolean validateBlobContent(byte[] blobContent, byte[] baseContent, int requiredSize) {
        if (blobContent.length != requiredSize) return false;
        for (int index = 0; index < blobContent.length; index++) {
            if (blobContent[index] != baseContent[index % baseContent.length]) return false;
        }
        return true;
    }

    /**
     * Populates a (segmented) blob using the FILL_BINARY_BLOB stored procedure
     *
     * @param id ID of the record to be created in blob_table
     * @param baseContent Base content
     * @param requiredSize Required size
     * @throws SQLException
     */
    protected void populateBlob(int id, byte[] baseContent, int requiredSize) throws SQLException {
        Connection con = getConnectionViaDriverManager();
        CallableStatement cstmt = null;
        try {
            cstmt = con.prepareCall(EXECUTE_FILL_BINARY_BLOB);
            cstmt.setInt(1, id);
            cstmt.setBytes(2, baseContent);
            cstmt.setInt(3, requiredSize);

            cstmt.execute();
        } finally {
            closeQuietly(cstmt);
            closeQuietly(con);
        }
    }

    /**
     * Populates a stream blob for testing.
     *
     * @param testId Id of the record to be inserted.
     * @throws SQLException
     */
    protected void populateStreamBlob(int testId, byte[] baseContent, int requiredSize) throws SQLException {
        final byte[] testBytes = generateBlobContent(baseContent, requiredSize);

        try (FbDatabase db = createDatabaseConnection()) {
            listener = new SimpleStatementListener();
            transaction = getTransaction(db);
            try {
                statement = db.createStatement(transaction);
                statement.addStatementListener(listener);

                final BlobParameterBuffer blobParameterBuffer = db.createBlobParameterBuffer();
                blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE, BlobParameterBuffer.TYPE_STREAM);
                final FbBlob blob = db.createBlobForOutput(transaction, blobParameterBuffer);
                blob.open();
                int bytesWritten = 0;
                while (bytesWritten < testBytes.length) {
                    byte[] buffer = new byte[Math.min(blob.getMaximumSegmentSize(), testBytes.length - bytesWritten)];
                    System.arraycopy(testBytes, bytesWritten, buffer, 0, buffer.length);
                    blob.putSegment(buffer);
                    bytesWritten += buffer.length;
                }
                blob.close();

                statement.prepare(INSERT_BLOB_TABLE);
                final DatatypeCoder datatypeCoder = db.getDatatypeCoder();
                FieldValue param1 = new FieldValue(datatypeCoder.encodeInt(testId));
                FieldValue param2 = new FieldValue(datatypeCoder.encodeLong(blob.getBlobId()));
                statement.execute(RowValue.of(param1, param2));
                statement.close();
            } finally {
                transaction.commit();
            }
        }
    }

    /**
     * Writes a blob using the gds.ng API.
     *
     * @param testId Id of the record to insert
     * @param testBytes Bytes to write
     * @param db Database to use
     * @param blobParameterBuffer Blob parameter buffer (or null)
     * @throws SQLException
     */
    protected void writeBlob(int testId, byte[] testBytes, FbDatabase db, BlobParameterBuffer blobParameterBuffer) throws SQLException {
        final SimpleStatementListener listener = new SimpleStatementListener();
        final FbTransaction transaction = getTransaction(db);
        try {
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            final FbBlob blob = db.createBlobForOutput(transaction, blobParameterBuffer);
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
            final DatatypeCoder datatypeCoder = db.getDatatypeCoder();
            FieldValue param1 = new FieldValue(datatypeCoder.encodeInt(testId));
            FieldValue param2 = new FieldValue(datatypeCoder.encodeLong(blob.getBlobId()));
            statement.execute(RowValue.of(param1, param2));
            statement.close();
        } finally {
            transaction.commit();
        }
    }

    /**
     * Validates the content of a blob using the CHECK_BINARY_BLOB stored procedure.
     *
     * @param id ID of the record in blob_table
     * @param baseContent Base content
     * @param requiredSize Required (expected) size
     * @return <code>true</code> when the content matches.
     * @throws SQLException
     */
    protected boolean validateBlob(int id, byte[] baseContent, int requiredSize) throws SQLException {
        try (Connection con = getConnectionViaDriverManager();
            CallableStatement cstmt = con.prepareCall(EXECUTE_CHECK_BINARY_BLOB)) {
            cstmt.setInt(1, id);
            cstmt.setBytes(2, baseContent);
            cstmt.setInt(3, requiredSize);

            cstmt.execute();
            return cstmt.getBoolean(1);
        }
    }

    protected abstract FbDatabase createFbDatabase(FbConnectionProperties connectionInfo) throws SQLException;

    /**
     * Creates a database connection to the test database.
     * @return FbWireDatabase instance
     * @throws SQLException
     */
    protected FbDatabase createDatabaseConnection() throws SQLException {
        final FbConnectionProperties connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
        connectionInfo.setEncoding("NONE");
        return createFbDatabase(connectionInfo);
    }

    protected final FbTransaction getTransaction(FbDatabase db) throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.startTransaction(tpb);
    }
}
