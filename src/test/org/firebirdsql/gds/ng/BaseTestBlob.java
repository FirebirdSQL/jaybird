// SPDX-FileCopyrightText: Copyright 2013-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.wire.SimpleStatementListener;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultTpb;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract test class for blob related tests shared by the wire and JNA implementation.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class BaseTestBlob {

    @RegisterExtension
    @Order(1)
    public static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsCase, "Test requires CASE support")
            .build();

    private static final int BASE_CONTENT_SIZE = 16384;

    protected static final String CREATE_BLOB_TABLE = """
            CREATE TABLE blob_table (
              id INTEGER PRIMARY KEY,
              blobvalue BLOB SUB_TYPE 0
            )""";

    protected static final String CREATE_PROC_CHECK_BINARY_BLOB = """
            CREATE PROCEDURE CHECK_BINARY_BLOB
             (
                ID INTEGER,
                BASE_CONTENT VARCHAR(%d) CHARACTER SET OCTETS,
                REQUIRED_SIZE INTEGER
             )
            RETURNS
             ( MATCHES SMALLINT )
            AS
              DECLARE VARIABLE REMAINING INTEGER;
              DECLARE VARIABLE BASE_CONTENT_SIZE INTEGER;
              DECLARE VARIABLE TEMP_BLOB BLOB SUB_TYPE 0;
            BEGIN
              REMAINING = REQUIRED_SIZE;
              TEMP_BLOB = '';
              BASE_CONTENT_SIZE = OCTET_LENGTH(BASE_CONTENT);
              WHILE (REMAINING > 0) DO
              BEGIN
                TEMP_BLOB = TEMP_BLOB ||
                    CASE
                        WHEN REMAINING > BASE_CONTENT_SIZE
                        THEN BASE_CONTENT
                        ELSE LEFT(BASE_CONTENT, REMAINING)
                    END;
                REMAINING = REMAINING - BASE_CONTENT_SIZE;
              END
              SELECT
                    CASE
                      WHEN blobvalue IS DISTINCT FROM :TEMP_BLOB
                      THEN 0
                      ELSE 1
                    END
                  FROM blob_table
                  WHERE ID = :ID
                  INTO :MATCHES;
            END""".formatted(BASE_CONTENT_SIZE);

    protected static final String EXECUTE_CHECK_BINARY_BLOB = "{call CHECK_BINARY_BLOB(?, ?, ?)}";

    protected static final String INSERT_BLOB_TABLE = "INSERT INTO blob_table (id, blobvalue) VALUES (?, ?)";

    protected static final String SELECT_BLOB_TABLE = "SELECT blobvalue FROM blob_table WHERE id = ?";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_BLOB_TABLE,
            CREATE_PROC_CHECK_BINARY_BLOB);

    protected SimpleStatementListener listener;
    protected FbTransaction transaction;
    protected FbStatement statement;

    @BeforeEach
    public final void setup() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.execute("delete from blob_table");
        }
    }

    /**
     * Queries the blob table for the blob id of the record with the specified (row) id.
     *
     * @param testId
     *         id of the row
     * @param db
     *         database to use
     * @return Blob id
     * @throws SQLException
     *         For errors executing the query
     */
    @SuppressWarnings("SameParameterValue")
    protected long getBlobId(int testId, FbDatabase db) throws SQLException {
        listener = new SimpleStatementListener();
        transaction = getTransaction(db);
        statement = db.createStatement(transaction);
        statement.addStatementListener(listener);
        statement.prepare(SELECT_BLOB_TABLE);

        statement.execute(RowValue.of(db.getDatatypeCoder().encodeInt(testId)));

        assertEquals(Boolean.TRUE, listener.hasResultSet(), "Expected hasResultSet to be set to true");

        statement.fetchRows(1);
        assertEquals(1, listener.getRows().size(), "Expected a row");

        RowValue row = listener.getRows().get(0);
        return db.getDatatypeCoder().decodeLong(row.getFieldData(0));
    }

    /**
     * Generates byte array of {@link #BASE_CONTENT_SIZE} with random content.
     *
     * @return Array
     */
    protected byte[] generateBaseContent() {
        return DataGenerator.createRandomBytes(BASE_CONTENT_SIZE);
    }

    /**
     * Generates blob content of the specified size based on baseContent.
     *
     * @param baseContent Base content used for populating the array
     * @param requiredSize The required content size
     * @return Blob content array
     */
    protected byte[] generateBlobContent(byte[] baseContent, int requiredSize) {
        byte[] result = new byte[requiredSize];
        int count = 0;
        while (count < requiredSize) {
            int toCopy = Math.min(baseContent.length, requiredSize - count);
            System.arraycopy(baseContent, 0, result, count, toCopy);
            count += toCopy;
        }
        return result;
    }

    /**
     * Checks if the blob content is of the required size and matches the expected content based on baseContent.
     *
     * @param blobContent
     *         Blob content
     * @param baseContent
     *         Base content
     * @param requiredSize
     *         Required size
     */
    protected void assertBlobContent(byte[] blobContent, byte[] baseContent, int requiredSize) {
        assertEquals(requiredSize, blobContent.length, "Unexpected length of blobContent");
        int pos = 0;
        while (pos + baseContent.length <= blobContent.length) {
            assertArrayEquals(baseContent, Arrays.copyOfRange(blobContent, pos, pos + baseContent.length),
                    "from pos = " + pos);
            pos += baseContent.length;
        }
        if (pos < blobContent.length) {
            assertArrayEquals(Arrays.copyOfRange(baseContent, 0, blobContent.length - pos),
                    Arrays.copyOfRange(blobContent, pos, blobContent.length), "from pos = " + pos);
        }
    }

    /**
     * Populates a segmented blob.
     *
     * @param id
     *         ID of the record to be created in blob_table
     * @param baseContent
     *         Base content
     * @param requiredSize
     *         Required size
     */
    @SuppressWarnings("SameParameterValue")
    protected void populateSegmentedBlob(int id, byte[] baseContent, int requiredSize) throws SQLException {
        populateBlob(id, baseContent, requiredSize, false);
    }

    /**
     * Populates a stream blob.
     *
     * @param id
     *         id of the record to be created in blob_table
     * @param baseContent
     *         Base content
     * @param requiredSize
     *         Required size
     */
    @SuppressWarnings("SameParameterValue")
    protected void populateStreamBlob(int id, byte[] baseContent, int requiredSize) throws SQLException {
        populateBlob(id, baseContent, requiredSize, true);
    }

    /**
     * Populates a blob.
     *
     * @param id
     *         id of the record to be created in blob_table
     * @param baseContent
     *         base content
     * @param requiredSize
     *         required size
     * @param streamBlob
     *         {@code true} create as stream blob, {@code false} create as segmented blob
     */
    protected void populateBlob(int id, byte[] baseContent, int requiredSize, boolean streamBlob)
            throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty(PropertyNames.useStreamBlobs, String.valueOf(streamBlob));
        try (var connection = DriverManager.getConnection(getUrl(), props);
             var pstmt = connection.prepareStatement(INSERT_BLOB_TABLE)) {
            pstmt.setInt(1, id);
            pstmt.setBytes(2, generateBlobContent(baseContent, requiredSize));
            pstmt.execute();
        }
    }

    /**
     * Writes a blob using the gds.ng API.
     *
     * @param testId Id of the record to insert
     * @param testBytes Bytes to write
     * @param db Database to use
     * @param blobParameterBuffer Blob parameter buffer (or null)
     */
    @SuppressWarnings("SameParameterValue")
    protected void writeBlob(int testId, byte[] testBytes, FbDatabase db, BlobParameterBuffer blobParameterBuffer) throws SQLException {
        final SimpleStatementListener listener = new SimpleStatementListener();
        final FbTransaction transaction = getTransaction(db);
        try {
            final FbStatement statement = db.createStatement(transaction);
            statement.addStatementListener(listener);
            final FbBlob blob = db.createBlobForOutput(transaction, blobParameterBuffer);
            blob.open();
            blob.putSegment(testBytes);
            blob.close();

            statement.prepare(INSERT_BLOB_TABLE);
            final DatatypeCoder datatypeCoder = db.getDatatypeCoder();
            RowValue rowValue = RowValue.of(
                    datatypeCoder.encodeInt(testId),
                    datatypeCoder.encodeLong(blob.getBlobId()));
            statement.execute(rowValue);
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
     * @return {@code true} when the content matches.
     */
    @SuppressWarnings("SameParameterValue")
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
     */
    protected FbDatabase createDatabaseConnection() throws SQLException {
        var props = FBTestProperties.getDefaultFbConnectionProperties();
        // Disable inline blobs and blob caching as these tests expect to use server-side blobs
        props.setMaxInlineBlobSize(0);
        props.setMaxBlobCacheSize(0);
        return createFbDatabase(props);
    }

    protected final FbTransaction getTransaction(FbDatabase db) throws SQLException {
        return db.startTransaction(getDefaultTpb());
    }

}
