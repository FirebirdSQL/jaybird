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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlob}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBBlobTest {

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    //@formatter:off
    private static final String RECREATE_BLOB_TABLE =
            "RECREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB " +
            ")";

    private static final String INSERT_BLOB = "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)";

    private static final String SELECT_BLOB = "SELECT bin_data FROM test_blob WHERE id = ?";
    //@formatter:on

    private Connection conn;

    @BeforeEach
    void setup() throws SQLException {
        conn = getConnectionViaDriverManager();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Tests whether a blob created as segmented or stream blob is correctly reported by {@link FBBlob#isSegmented()}.
     */
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void testIsSegmented(boolean useStreamBlobs) throws SQLException {
        try (Connection conn = getConnection(useStreamBlobs)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });

            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue(rs.next(), "Expected a row in result set");
                    FBBlob blob = (FBBlob) rs.getBlob(1);
                    assertEquals(!useStreamBlobs, blob.isSegmented(),
                            "Expected a " + (useStreamBlobs ? "stream" : "segmented") + " blob");
                    blob.free();
                }
            }
        }
    }

    /**
     * Tests whether a blob is created as stream by default.
     */
    @Test
    void testStreamBlob_isDefault() throws SQLException {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });

        try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
            select.setInt(1, 1);
            try (ResultSet rs = select.executeQuery()) {
                assertTrue(rs.next(), "Expected a row in result set");
                FBBlob blob = (FBBlob) rs.getBlob(1);
                assertFalse(blob.isSegmented(), "Expected a stream blob");
                blob.free();
            }
        }
    }

    /**
     * Tests that closing a blob after opening an InputStream doesn't throw unexpected exceptions
     * <p>
     * Previously {@link FBBlob} threw a ConcurrentModificationException in this case.
     * </p>
     */
    @Test
    void testClose_afterOpeningMultipleIS() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
        try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
            select.setInt(1, 1);
            try (ResultSet rs = select.executeQuery()) {
                assertTrue(rs.next(), "Expected a row in result set");
                FBBlob blob = (FBBlob) rs.getBlob(1);

                blob.getBinaryStream();
                blob.getBinaryStream();

                blob.free();
            }
        }
    }

    @Test
    void testPosition_byteArr_long_throwsSQLFeatureNotSupported() throws Exception {
        Blob blob = conn.createBlob();

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.position(new byte[] { 1, 2, 3 }, 1));
    }

    @Test
    void testPosition_Blob_long_throwsSQLFeatureNotSupported() throws Exception {
        Blob blob = conn.createBlob();
        Blob otherBlob = conn.createBlob();

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.position(otherBlob, 1));
    }

    @Test
    void testTruncate_long_throwsSQLFeatureNotSupported() throws Exception {
        Blob blob = conn.createBlob();

        assertThrows(SQLFeatureNotSupportedException.class, () -> blob.truncate(1));
    }

    @Test
    void testSetBytes_long_byteArr_throwsSQLFeatureNotSupported() throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_BLOB)) {
            pstmt.setInt(1, 1);
            Blob blob = conn.createBlob();
            blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 });
            pstmt.setBlob(2, blob);
            pstmt.execute();
        }

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");

                assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, rs.getBytes(1), "Unexpected blob value");
            }
        }
    }

    @Test
    void testSetBytes_long_byteArr_int_int_throwsSQLFeatureNotSupported() throws Exception {
        executeCreateTable(conn, RECREATE_BLOB_TABLE);
        try (PreparedStatement pstmt = conn.prepareStatement(INSERT_BLOB)) {
            pstmt.setInt(1, 1);
            Blob blob = conn.createBlob();
            blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 }, 1, 3);
            pstmt.setBlob(2, blob);
            pstmt.execute();
        }

        try (PreparedStatement pstmt = conn.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");

                assertArrayEquals(new byte[] { 2, 3, 4 }, rs.getBytes(1), "Unexpected blob value");
            }
        }
    }

    @Test
    void testGetBinaryStream_long_long_throwsSQLFeatureNotSupported() throws Exception {
            Blob blob = conn.createBlob();

            assertThrows(SQLFeatureNotSupportedException.class, () -> blob.getBinaryStream(1, 1));
    }

    @SuppressWarnings("resource")
    @Test
    void testSetBinaryStream_calledTwice_throwsSQLException() throws Exception {
        Blob blob = conn.createBlob();

        blob.setBinaryStream(1);

        SQLException exception = assertThrows(SQLException.class, () -> blob.setBinaryStream(1));
        assertThat(exception, message(containsString("already open")));
    }

    @SuppressWarnings("resource")
    @Test
    void testSetBinaryStream_positionZero_throwsSQLException() throws Exception {
        Blob blob = conn.createBlob();

        SQLException exception = assertThrows(SQLException.class, () -> blob.setBinaryStream(0));
        assertThat(exception, allOf(
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH)),
                message(containsString("before the beginning"))));
    }

    @SuppressWarnings("resource")
    @Test
    void testSetBinaryStream_positionBeyondStart_newBlob_throwsSQLException() throws Exception {
        Blob blob = conn.createBlob();

        SQLException exception = assertThrows(SQLException.class, () -> blob.setBinaryStream(2));
        assertThat(exception, allOf(
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH)),
                message(containsString("must start at position 1"))));
    }

    @SuppressWarnings("resource")
    @Test
    void testSetBinaryStream_positionBeyondStart_existingBlob_throwsSQLFeatureNotSupported() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
        try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
            select.setInt(1, 1);
            try (ResultSet rs = select.executeQuery()) {
                assertTrue(rs.next(), "Expected a row in result set");
                FBBlob blob = (FBBlob) rs.getBlob(1);

                assertThrows(SQLFeatureNotSupportedException.class, () -> blob.setBinaryStream(2));
            }
        }
    }

    @Test
    void testGetBlobId_newBlob_throwsSQLException() throws Exception {
        FBBlob blob = (FBBlob) conn.createBlob();

        SQLException exception = assertThrows(SQLException.class, blob::getBlobId);
        assertThat(exception, message(equalTo("No Blob ID is available in new Blob object")));
    }

    @Test
    void testGetBytes_withOffset_streamBlob() throws Exception {
        try (Connection conn = getConnection(true)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue(rs.next(), "Expected a row in result set");
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    byte[] bytes = blob.getBytes(2, 4);

                    assertArrayEquals(new byte[] { 2, 3, 4, 5 }, bytes,
                            "Expected array equal to original from index 1");
                }
            }
        }
    }

    @Test
    void testGetBytes_withOffset_segmentedBlob_throwsSQLException() throws Exception {
        try (Connection conn = getConnection(false)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue(rs.next(), "Expected a row in result set");
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    SQLException exception = assertThrows(SQLException.class, () -> blob.getBytes(2, 4));
                    assertThat(exception, message(containsString(getFbMessage(ISCConstants.isc_bad_segstr_type))));
                }
            }
        }
    }

    @Test
    void testGetBytes_positionZero_throwsSQLException() throws Exception {
        FBBlob blob = (FBBlob) conn.createBlob();

        SQLException exception = assertThrows(SQLException.class, () -> blob.getBytes(0, 4));
        assertThat(exception, message(containsString("should be >= 1")));
    }

    @Test
    void testGetBytes_positionLargerThanMaxIntValue_throwsSQLException() throws Exception {
        FBBlob blob = (FBBlob) conn.createBlob();

        SQLException exception = assertThrows(SQLException.class, () -> blob.getBytes(Integer.MAX_VALUE + 1L, 4));
        assertThat(exception, allOf(
                message(equalTo("Blob position is limited to 2^31 - 1 due to isc_seek_blob limitations")),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_STRING_LENGTH))));
    }

    /**
     * Checks if the blob close on commit is done successfully.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-400">JDBC-400</a>.
     * </p>
     */
    @Test
    void testBlobCloseOnCommit() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement("SELECT bin_data FROM test_blob");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");
        InputStream binaryStream = rs.getBinaryStream(1);

        conn.commit();

        //noinspection ResultOfMethodCallIgnored
        IOException exception = assertThrows(IOException.class, binaryStream::read);
        assertThat(exception, message(equalTo("Input stream is already closed.")));
    }

    /**
     * Checks if the blob close on rollback is done successfully.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-400">JDBC-400</a>.
     * </p>
     */
    @Test
    void testBlobCloseOnRollback() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement("SELECT bin_data FROM test_blob");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");
        InputStream binaryStream = rs.getBinaryStream(1);

        conn.rollback();

        //noinspection ResultOfMethodCallIgnored
        IOException exception = assertThrows(IOException.class, binaryStream::read);
        assertThat(exception, message(equalTo("Input stream is already closed.")));
    }

    /**
     * Checks if the blob close on close of the connection is done successfully in auto commit mode.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-348">JDBC-348</a>.
     * </p>
     */
    @Test
    void testBlobCloseOnConnectionClose_inAutoCommit() throws Exception {
        OutputStream binaryStream;
        try {
            FBBlob blob = (FBBlob) conn.createBlob();
            binaryStream = blob.setBinaryStream(1);
        } finally {
            // This should not trigger an exception
            conn.close();
        }
        IOException exception = assertThrows(IOException.class, () -> binaryStream.write(1));
        assertThat(exception, message(equalTo("Output stream is already closed.")));
    }

    /**
     * Checks if the blob cannot be used after transaction end.
     */
    @Test
    void testBlobUseAfterCommit_notAllowed() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement("SELECT bin_data FROM test_blob");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");
        Blob blob = rs.getBlob(1);

        conn.commit();

        SQLException exception = assertThrows(SQLException.class, blob::getBinaryStream);
        assertThat(exception, fbMessageStartsWith(JaybirdErrorCodes.jb_blobClosed));
    }

    /**
     * Checks if the cached blob can still be used after transaction end.
     * <p>
     * NOTE: This behaviour only applies to emulated scrollable cursors.
     * </p>
     */
    @Test
    void testCachedBlobUseAfterCommit_allowed() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT bin_data FROM test_blob",  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");
        Blob blob = rs.getBlob(1);

        conn.commit();

        InputStream binaryStream = blob.getBinaryStream();
        assertEquals(1, binaryStream.read());
    }

    /**
     * A stream obtained from a Blob instance should remain open after result set next.
     */
    @Test
    void testStreamFromBlobAfterResultSetNext_shouldRemainOpen() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement("SELECT bin_data FROM test_blob");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");

        Blob blob = rs.getBlob(1);
        InputStream binaryStream = blob.getBinaryStream();

        rs.next();

        assertEquals(1, binaryStream.read());
    }

    /**
     * Bytes from a Blob instance should remain available after result set next.
     */
    @Test
    void testBytesFromCachedBlobAfterResultSetNext_shouldRemainAvailable() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT bin_data FROM test_blob",  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");

        Blob blob = rs.getBlob(1);

        rs.next();

        assertNotNull(blob.getBytes(1, 10));
    }

    /**
     * A stream obtained from the result set should be closed after result set next.
     */
    @Test
    void testStreamFromResultSetAfterResultSetNext_shouldBeClosed() throws Exception {
        populateBlob(conn, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

        conn.setAutoCommit(false);

        // Intentionally not closing the created statement, result set and blob
        PreparedStatement stmt = conn.prepareStatement("SELECT bin_data FROM test_blob");
        ResultSet rs = stmt.executeQuery();
        assertTrue(rs.next(), "Expected at least one row");
        InputStream binaryStream = rs.getBinaryStream(1);

        rs.next();

        //noinspection ResultOfMethodCallIgnored
        IOException exception = assertThrows(IOException.class, binaryStream::read);
        assertThat(exception, message(equalTo("Input stream is already closed.")));
    }

    private void populateBlob(Connection conn, byte[] bytes) throws SQLException {
        executeCreateTable(conn, RECREATE_BLOB_TABLE);
        try (PreparedStatement insert = conn.prepareStatement(INSERT_BLOB)) {
            insert.setInt(1, 1);
            insert.setBytes(2, bytes);
            insert.execute();
        }
    }

    private Connection getConnection(boolean useStreamBlobs) throws SQLException {
        final Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("useStreamBlobs", useStreamBlobs ? "true" : "false");
        return DriverManager.getConnection(getUrl(), connectionProperties);
    }
}
