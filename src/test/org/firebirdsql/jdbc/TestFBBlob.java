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

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import static org.junit.Assert.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlob}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlob extends FBJUnit4TestBase {

    @SuppressWarnings("deprecation")
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    //@formatter:off
    private static final String CREATE_BLOB_TABLE =
            "CREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB " +
            ")";

    private static final String INSERT_BLOB = "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)";

    private static final String SELECT_BLOB = "SELECT bin_data FROM test_blob WHERE id = ?";
    //@formatter:on

    @Before
    public void createTestTable() throws SQLException {
        try (Connection conn = getConnectionViaDriverManager()) {
            executeCreateTable(conn, CREATE_BLOB_TABLE);
        }
    }

    /**
     * Tests whether a blob created as segmented is correctly reported by {@link FBBlob#isSegmented()}.
     */
    @Test
    public void testIsSegmented_segmentedBlob() throws SQLException {
        try (Connection conn = getConnection(false)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });

            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);
                    assertTrue("Expected a segmented blob", blob.isSegmented());
                    blob.free();
                }
            }
        }
    }

    /**
     * Tests whether a blob created as stream is correctly reported by {@link FBBlob#isSegmented()}.
     */
    @Test
    public void testIsSegmented_streamBlob() throws SQLException {
        try (Connection conn = getConnection(true)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });

            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);
                    assertFalse("Expected a stream blob", blob.isSegmented());
                    blob.free();
                }
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
    public void testClose_afterOpeningMultipleIS() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    blob.getBinaryStream();
                    blob.getBinaryStream();

                    blob.free();
                }
            }
        }
    }

    @Test
    public void testPosition_byteArr_long_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.position(new byte[] { 1, 2, 3 }, 1);
        }
    }

    @Test
    public void testPosition_Blob_long_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();
            Blob otherBlob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.position(otherBlob, 1);
        }
    }

    @Test
    public void testTruncate_long_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.truncate(1);
        }
    }

    @Test
    public void testSetBytes_long_byteArr_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_BLOB)) {
                pstmt.setInt(1, 1);
                Blob blob = conn.createBlob();
                blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 });
                pstmt.setBlob(2, blob);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_BLOB)) {
                pstmt.setInt(1, 1);

                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue("Expected a row", rs.next());

                    assertArrayEquals("Unexpected blob value", new byte[] { 1, 2, 3, 4, 5 }, rs.getBytes(1));
                }
            }
        }
    }

    @Test
    public void testSetBytes_long_byteArr_int_int_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_BLOB)) {
                pstmt.setInt(1, 1);
                Blob blob = conn.createBlob();
                blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 }, 1, 3);
                pstmt.setBlob(2, blob);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(SELECT_BLOB)) {
                pstmt.setInt(1, 1);

                try (ResultSet rs = pstmt.executeQuery()) {
                    assertTrue("Expected a row", rs.next());

                    assertArrayEquals("Unexpected blob value", new byte[] { 2, 3, 4 }, rs.getBytes(1));
                }
            }
        }
    }

    @Test
    public void testGetBinaryStream_long_long_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.getBinaryStream(1, 1);
        }
    }

    @Test
    public void testSetBinaryStream_calledTwice_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            blob.setBinaryStream(1);

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("already open"))
            ));

            blob.setBinaryStream(1);
        }
    }

    @Test
    public void testSetBinaryStream_positionZero_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE)),
                    message(containsString("before the beginning"))
            ));

            blob.setBinaryStream(0);
        }
    }

    @Test
    public void testSetBinaryStream_positionBeyondStart_newBlob_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            Blob blob = conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE)),
                    message(containsString("must start at position 1"))
            ));

            blob.setBinaryStream(2);
        }
    }

    @Test
    public void testSetBinaryStream_positionBeyondStart_existingBlob_throwsSQLFeatureNotSupported() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    expectedException.expect(SQLFeatureNotSupportedException.class);

                    blob.setBinaryStream(2);
                }
            }
        }
    }

    @Test
    public void testGetBlobId_newBlob_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("No Blob ID is available in new Blob object."))
            ));

            blob.getBlobId();
        }
    }

    @Test
    public void testGetBytes_withOffset_streamBlob() throws Exception {
        try (Connection conn = getConnection(true)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    byte[] bytes = blob.getBytes(2, 4);

                    assertArrayEquals("Expected array equal to original from index 1",
                            new byte[] { 2, 3, 4, 5 }, bytes);
                }
            }
        }
    }

    @Test
    public void testGetBytes_withOffset_segmentedBlob_throwsSQLException() throws Exception {
        try (Connection conn = getConnection(false)) {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            try (PreparedStatement select = conn.prepareStatement(SELECT_BLOB)) {
                select.setInt(1, 1);
                try (ResultSet rs = select.executeQuery()) {
                    assertTrue("Expected a row in result set", rs.next());
                    FBBlob blob = (FBBlob) rs.getBlob(1);

                    expectedException.expect(allOf(
                            isA(SQLException.class),
                            message(containsString(getFbMessage(ISCConstants.isc_bad_segstr_type)))
                    ));

                    blob.getBytes(2, 4);
                }
            }
        }
    }

    @Test
    public void testGetBytes_positionZero_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("should be >= 1"))
            ));

            blob.getBytes(0, 4);
        }
    }

    @Test
    public void testGetBytes_positionLargerThanMaxIntValue_throwsSQLException() throws Exception {
        try (Connection conn = getConnectionViaDriverManager()) {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("Blob position is limited to 2^31 - 1 due to isc_seek_blob limitations.")),
                    sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE))
            ));

            blob.getBytes(Integer.MAX_VALUE + 1L, 4);
        }
    }

    /**
     * Checks if the blob close on commit is done successfully.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-400">JDBC-400</a>.
     * </p>
     */
    @Test
    public void testBlobCloseOnCommit() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob");
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());
            InputStream binaryStream = rs.getBinaryStream(1);

            connection.commit();

            expectedException.expect(IOException.class);
            expectedException.expectMessage("Input stream is already closed.");
            //noinspection ResultOfMethodCallIgnored
            binaryStream.read();
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * Checks if the blob close on rollback is done successfully.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-400">JDBC-400</a>.
     * </p>
     */
    @Test
    public void testBlobCloseOnRollback() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob");
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());
            InputStream binaryStream = rs.getBinaryStream(1);

            connection.rollback();

            expectedException.expect(IOException.class);
            expectedException.expectMessage("Input stream is already closed.");
            //noinspection ResultOfMethodCallIgnored
            binaryStream.read();
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * Checks if the blob close on close of the connection is done successfully in auto commit mode.
     * <p>
     * See <a href="http://tracker.firebirdsql.org/browse/JDBC-348">JDBC-348</a>.
     * </p>
     */
    @Test
    public void testBlobCloseOnConnectionClose_inAutoCommit() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        OutputStream binaryStream;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            FBBlob blob = (FBBlob) connection.createBlob();
            binaryStream = blob.setBinaryStream(1);
        } finally {
            // This should not trigger an exception
            connection.close();
        }
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Output stream is already closed.");
        binaryStream.write(1);
    }

    /**
     * Checks if the blob cannot be used after transaction end.
     */
    @Test
    public void testBlobUseAfterCommit_notAllowed() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob");
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());
            Blob blob = rs.getBlob(1);

            connection.commit();

            expectedException.expect(SQLException.class);
            expectedException.expect(fbMessageStartsWith(JaybirdErrorCodes.jb_blobClosed));
            blob.getBinaryStream();
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * Checks if the cached blob can still be used after transaction end.
     */
    @Test
    public void testCachedBlobUseAfterCommit_allowed() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT bin_data FROM test_blob",  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());
            Blob blob = rs.getBlob(1);

            connection.commit();

            InputStream binaryStream = blob.getBinaryStream();
            assertEquals(1, binaryStream.read());
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * A stream obtained from a Blob instance should remain open after result set next.
     */
    @Test
    public void testStreamFromBlobAfterResultSetNext_shouldRemainOpen() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob");
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());

            Blob blob = rs.getBlob(1);
            InputStream binaryStream = blob.getBinaryStream();

            rs.next();

            assertEquals(1, binaryStream.read());
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * Bytes from a Blob instance should remain available after result set next.
     */
    @Test
    public void testBytesFromCachedBlobAfterResultSetNext_shouldRemainAvailable() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT bin_data FROM test_blob",  ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());

            Blob blob = rs.getBlob(1);

            rs.next();

            assertNotNull(blob.getBytes(1, 10));
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    /**
     * A stream obtained from the result set should be closed after result set next.
     */
    @Test
    public void testStreamFromResultSetAfterResultSetNext_shouldBeClosed() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        //noinspection TryFinallyCanBeTryWithResources
        try {
            populateBlob(connection, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });

            connection.setAutoCommit(false);

            // Intentionally not closing the created statement, result set and blob
            PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob");
            ResultSet rs = stmt.executeQuery();
            assertTrue("Expected at least one row", rs.next());
            InputStream binaryStream = rs.getBinaryStream(1);

            rs.next();

            expectedException.expect(IOException.class);
            expectedException.expectMessage("Input stream is already closed.");
            //noinspection ResultOfMethodCallIgnored
            binaryStream.read();
        } finally {
            // This should not trigger an exception
            connection.close();
        }
    }

    private void populateBlob(Connection conn, byte[] bytes) throws SQLException {
        try (PreparedStatement insert = conn.prepareStatement(INSERT_BLOB)) {
            insert.setInt(1, 1);
            insert.setBytes(2, bytes);
            insert.executeUpdate();
        }
    }

    private Connection getConnection(boolean useStreamBlobs) throws SQLException {
        final Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("useStreamBlobs", useStreamBlobs ? "true" : "false");
        return DriverManager.getConnection(getUrl(), connectionProperties);
    }
}
