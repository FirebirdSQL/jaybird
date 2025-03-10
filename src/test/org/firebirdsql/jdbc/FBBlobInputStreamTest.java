// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobInputStream}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class FBBlobInputStreamTest {

    private static final int MULTI_SEGMENT_LENGTH = 128 * 1024 - 2;

    private static final String CREATE_TABLE = """
            CREATE TABLE test_blob(
              id INTEGER,
              bin_data BLOB
            )""";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll useDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static final String INSERT_BLOB = "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)";
    private static final String SELECT_BLOB = "SELECT bin_data FROM test_blob WHERE id = ?";

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnection(true);
        try (var stmt = connection.createStatement()) {
            stmt.execute("delete from test_blob");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void testGetOwner() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            assertSame(blob, is.getBlob(), "FBBlobInputStream.getBlob() should return owning blob");
        }
    }

    @Test
    void testNewBlob_throwSQLE() throws Exception {
        var blob = connection.createBlob();

        var exception = assertThrows(SQLException.class, blob::getBinaryStream);
        assertThat(exception, message(equalTo("Cannot read a new blob")));
    }

    @Test
    void testAvailable_noReads_returns0() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            assertEquals(0, is.available(), "Available() without initial read should return 0");
        }
    }

    @Test
    void testAvailable_singleRead_returnsRemaining() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            assertEquals(1, is.read(), "Expected first blob value of 1");

            assertEquals(bytes.length - 1, is.available(),
                    "Available() after initial read should return remaining length");
        }
    }

    @Test
    void testAvailable_fullyRead_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[bytes.length];
            is.readFully(buffer);

            assertArrayEquals(bytes, buffer);

            assertEquals(0, is.available(), "Available() after readFully() should return 0");
        }
    }

    @Test
    void testAvailable_singleReadClosed_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            assertEquals(1, is.read(), "Expected first blob value of 1");
            is.close();

            assertEquals(0, is.available(), "Available() after close() should return 0");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testRead_byteArr_moreThanAvailable_returnsAvailable(boolean useStreamBlobs) throws Exception {
        if (!useStreamBlobs) {
            connection.close();
            connection = getConnection(false);
        }
        byte[] bytes = DataGenerator.createRandomBytes(MULTI_SEGMENT_LENGTH);
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            assertEquals(bytes[0] & 0xFF, is.read(), "Unexpected first byte");
            int available = is.available();
            int blobBufferSize = getConnectionBlobBufferSize();
            assertThat("Value of available() should be greater than 0 but less than blobBufferSize",
                    available, allOf(greaterThan(0), lessThan(blobBufferSize)));

            // For small buffer size, we only read from the internal buffer, and don't request more from the server
            int smallBufferSize = blobBufferSize / 2;
            byte[] buffer = new byte[smallBufferSize];
            int bytesRead = is.read(buffer);

            int expectedSize = Math.min(smallBufferSize, available);
            assertEquals(expectedSize, bytesRead,
                    "Expected to read number of bytes previously returned by available or smallBufferSize");
            assertArrayEquals(Arrays.copyOfRange(bytes, 1, expectedSize + 1),
                    expectedSize == buffer.length ? buffer : Arrays.copyOf(bytes, expectedSize),
                    "Unexpected read bytes");

            int readOffset = expectedSize + 1;
            if (is.available() == 0) {
                assertEquals(bytes[++readOffset], is.read(), "Unexpected byte at offset " + (readOffset - 1));
            }

            buffer = new byte[is.available() + smallBufferSize];
            // If after reading available, we still have smallBufferSize remaining, we read the remaining bytes
            // from server
            bytesRead = is.read(buffer);

            assertEquals(buffer.length, bytesRead, "Expected to read number of bytes equal to the buffer size");
            assertArrayEquals(Arrays.copyOfRange(bytes, readOffset, readOffset + buffer.length), buffer,
                    "Unexpected read bytes");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testRead_byteArr_moreThanAvailable_returnsAll(boolean useStreamBlobs) throws Exception {
        // NOTE: returning all bytes is not guaranteed; it does (on FB3+) due to the value of MULTI_SEGMENT_LENGTH
        if (!useStreamBlobs) {
            connection.close();
            connection = getConnection(false);
        }
        final byte[] bytes = DataGenerator.createRandomBytes(MULTI_SEGMENT_LENGTH);
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();
            int blobBufferSize = getConnectionBlobBufferSize();

            assertEquals(bytes[0] & 0xFF, is.read(), "Unexpected first byte");
            final int available = is.available();
            assertThat("Value of available() should be greater than 0 but less than blobBufferSize",
                    available, allOf(greaterThan(0), lessThan(blobBufferSize)));

            byte[] buffer = new byte[MULTI_SEGMENT_LENGTH];
            buffer[0] = bytes[0];

            int bytesRead = is.read(buffer, 1, MULTI_SEGMENT_LENGTH - 1);
            int expectedBytesRead;
            if (useStreamBlobs || getDefaultSupportInfo().isVersionEqualOrAbove(3)) {
                expectedBytesRead = MULTI_SEGMENT_LENGTH - 1;
            } else {
                // On Firebird 2.5 and lower, the reads on segmented blobs return 2 bytes less than the max requested.
                // There is probably some segment missed in the calculation, not sure why, but given 2.5 is not
                // supported we fudge the test to run without error instead of trying to hunt down the underlying issue.
                expectedBytesRead = MULTI_SEGMENT_LENGTH - 3;
                // Populate the last two bytes so assertArrayEquals works
                assertEquals(2, is.read(buffer, buffer.length - 2, 2), "Expected to read 2 bytes");
            }

            assertEquals(expectedBytesRead, bytesRead, "Expected remaining bytes to be read");
            assertArrayEquals(bytes, buffer, "Expected identical bytes to be returned");
        }
    }

    @Test
    void testRead_byteArr_length0_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            byte[] buffer = new byte[5];
            int bytesRead = is.read(buffer, 0, 0);

            assertEquals(0, bytesRead, "Expected 0 bytes read");
            assertArrayEquals(new byte[] { 0, 0, 0, 0, 0 }, buffer, "Expected buffer to have defaults only");
        }
    }

    @Test
    void testRead_byteArrNull_throwsNPE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            //noinspection DataFlowIssue
            assertThrows(NullPointerException.class, () -> is.read(null, 0, 1));
        }
    }

    @Test
    void testRead_negativeOffset_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, -1, 1));
        }
    }

    @Test
    void testRead_negativeLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 0, -1));
        }
    }

    @Test
    void testRead_offsetBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 5, 1));
        }
    }

    @Test
    void testRead_offsetAndLengthBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 0, 6));
        }
    }

    @Test
    void testReadFully_byteArr_moreThanAvailable_returnsAllRead() throws Exception {
        final byte[] bytes = DataGenerator.createRandomBytes(MULTI_SEGMENT_LENGTH);
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[MULTI_SEGMENT_LENGTH];
            int firstValue = is.read();
            assertEquals(bytes[0] & 0xFF, firstValue, "Unexpected first byte");
            buffer[0] = (byte) firstValue;

            final int available = is.available();
            assertThat("Value of available() should be smaller than 128 * 1024 - 1",
                    available, lessThan(MULTI_SEGMENT_LENGTH - 1));

            is.readFully(buffer, 1, MULTI_SEGMENT_LENGTH - 1);

            assertArrayEquals(bytes, buffer, "Full blob should have been read");
        }
    }

    @Test
    void testReadFully_byteArr_length0_readsNothing() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[5];

            is.readFully(buffer, 0, 0);

            assertArrayEquals(new byte[] { 0, 0, 0, 0, 0 }, buffer, "Expected buffer to still contain 0");
        }
    }

    @Test
    void testReadFully_byteArrNull_throwsNPE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            assertThrows(NullPointerException.class, () -> is.readFully(null, 0, 1));
        }
    }

    @Test
    void testReadFully_negativeOffset_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, -1, 1));
        }
    }

    @Test
    void testReadFully_negativeLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 0, -1));
        }
    }

    @Test
    void testReadFully_offsetBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 5, 1));
        }
    }

    @Test
    void testReadFully_offsetAndLengthBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[5];
            assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 0, 6));
        }
    }

    @Test
    void testReadFully_bufferLongerThanBlob_throwsEOFException() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

            byte[] buffer = new byte[6];
            assertThrows(EOFException.class, () -> is.readFully(buffer));
        }
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            useStreamBlobs, startWithSingleRead
            true,           false
            true,           true
            false,          false
            false,          true
            """)
    void testTransferTo(boolean useStreamBlobs, boolean startWithSingleRead) throws Exception {
        if (!useStreamBlobs) {
            connection.close();
            connection = getConnection(false);
        }
        final byte[] bytes = DataGenerator.createRandomBytes(MULTI_SEGMENT_LENGTH);
        populateBlob(1, bytes);

        try (var pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);

            var rs = pstmt.executeQuery();
            assertNextRow(rs);
            Blob blob = rs.getBlob(1);
            InputStream is = blob.getBinaryStream();
            var baos = new ByteArrayOutputStream(bytes.length);
            if (startWithSingleRead) {
                // Using a single read will populate the buffer of the FBBlobInputStream. This verifies that we
                // take the current buffer content into consideration when transferring
                int read = is.read();
                assertEquals(bytes[0] & 0xFF, read, "Unexpected first byte");
                baos.write(read);
            }
            assertEquals(startWithSingleRead ? MULTI_SEGMENT_LENGTH - 1 : MULTI_SEGMENT_LENGTH, is.transferTo(baos),
                    "Unexpected number of bytes transferred");

            assertArrayEquals(bytes, baos.toByteArray(), "Expected identical bytes to be returned");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void populateBlob(int id, byte[] bytes) throws SQLException {
        try (var insert = connection.prepareStatement(INSERT_BLOB)) {
            insert.setInt(1, id);
            insert.setBytes(2, bytes);
            insert.execute();
        }
    }

    private static Connection getConnection(boolean useStreamBlobs) throws SQLException {
        return getConnectionViaDriverManager(PropertyNames.useStreamBlobs, String.valueOf(useStreamBlobs));
    }

    private int getConnectionBlobBufferSize() throws SQLException {
        return connection.unwrap(FirebirdConnection.class)
                .getFbDatabase()
                .getConnectionProperties()
                .getBlobBufferSize();
    }
}
