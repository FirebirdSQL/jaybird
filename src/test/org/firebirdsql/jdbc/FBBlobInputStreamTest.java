/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.EOFException;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobInputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class FBBlobInputStreamTest {

    private static final String CREATE_TABLE =
            "CREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB " +
            ")";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll useDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static final String INSERT_BLOB = "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)";

    private static final String SELECT_BLOB = "SELECT bin_data FROM test_blob WHERE id = ?";

    private static Connection connection;

    @BeforeAll
    static void setupAll() throws Exception{
        connection = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setUp() throws Exception {
        connection.setAutoCommit(true);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from test_blob");
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

    @Test
    void testGetOwner() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertSame(blob, is.getBlob(), "FBBlobInputStream.getBlob() should return owning blob");
            }
        }
    }

    @Test
    void testNewBlob_throwSQLE() throws Exception {
        Blob blob = connection.createBlob();

        SQLException exception = assertThrows(SQLException.class, blob::getBinaryStream);
        assertThat(exception, message(equalTo("You can't read a new blob")));
    }

    @Test
    void testAvailable_noReads_returns0() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertEquals(0, is.available(), "Available() without initial read should return 0");
            }
        }
    }

    @Test
    void testAvailable_singleRead_returnsRemaining() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertEquals(1, is.read(), "Expected first blob value of 1");

                assertEquals(bytes.length - 1, is.available(),
                        "Available() after initial read should return remaining length");
            }
        }
    }

    @Test
    void testAvailable_fullyRead_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[bytes.length];
                is.readFully(buffer);

                assertArrayEquals(bytes, buffer);

                assertEquals(0, is.available(), "Available() after readFully() should return 0");
            }
        }
    }

    @Test
    void testAvailable_singleReadClosed_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertEquals(1, is.read(), "Expected first blob value of 1");
                is.close();

                assertEquals(0, is.available(), "Available() after close() should return 0");
            }
        }
    }

    @Test
    void testRead_byteArr_moreThanAvailable_returnsAvailable() throws Exception {
        final byte[] bytes = DataGenerator.createRandomBytes(128 * 1024);
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertEquals(bytes[0] & 0xFF, is.read(), "Unexpected first byte");
                final int available = is.available();
                assertTrue(available > 0, "Value of available() should be larger than 0");
                assertTrue(available < 128 * 1024 - 1, "Value of available() should be smaller than 128 * 1024 - 1");

                byte[] buffer = new byte[128 * 1024];
                int bytesRead = is.read(buffer, 1, 128 * 1024 - 1);

                assertEquals(available, bytesRead,
                        "Expected to read the number of bytes previously returned by available");
            }
        }
    }

    @Test
    void testRead_byteArr_length0_returns0() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                int bytesRead = is.read(buffer, 0, 0);

                assertEquals(0, bytesRead, "Expected 0 bytes read");
                assertArrayEquals(new byte[] { 0, 0, 0, 0, 0 }, buffer, "Expected buffer to have defaults only");
            }
        }
    }

    @Test
    void testRead_byteArrNull_throwsNPE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                //noinspection ResultOfMethodCallIgnored
                assertThrows(NullPointerException.class, () -> is.read(null, 0, 1));
            }
        }
    }

    @Test
    void testRead_negativeOffset_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                //noinspection ResultOfMethodCallIgnored
                assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, -1, 1));
            }
        }
    }

    @Test
    void testRead_negativeLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                //noinspection ResultOfMethodCallIgnored
                assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 0, -1));
            }
        }
    }

    @Test
    void testRead_offsetBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                //noinspection ResultOfMethodCallIgnored
                assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 5, 1));
            }
        }
    }

    @Test
    void testRead_offsetAndLengthBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                //noinspection ResultOfMethodCallIgnored
                assertThrows(IndexOutOfBoundsException.class, () -> is.read(buffer, 0, 6));
            }
        }
    }

    @Test
    void testReadFully_byteArr_moreThanAvailable_returnsAllRead() throws Exception {
        final byte[] bytes = DataGenerator.createRandomBytes(128 * 1024);
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[128 * 1024];
                int firstValue = is.read();
                assertEquals(bytes[0] & 0xFF, firstValue, "Unexpected first byte");
                buffer[0] = (byte) firstValue;

                final int available = is.available();
                assertTrue(available < 128 * 1024 - 1, "Value of available() should be smaller than 128 * 1024 - 1");

                is.readFully(buffer, 1, 128 * 1024 - 1);

                assertArrayEquals(bytes, buffer, "Full blob should have been read");
            }
        }
    }

    @Test
    void testReadFully_byteArr_length0_readsNothing() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];

                is.readFully(buffer, 0, 0);

                assertArrayEquals(new byte[] { 0, 0, 0, 0, 0 }, buffer, "Expected buffer to still contain 0");
            }
        }
    }

    @Test
    void testReadFully_byteArrNull_throwsNPE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                assertThrows(NullPointerException.class, () -> is.readFully(null, 0, 1));
            }
        }
    }

    @Test
    void testReadFully_negativeOffset_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, -1, 1));
            }
        }
    }

    @Test
    void testReadFully_negativeLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 0, -1));
            }
        }
    }

    @Test
    void testReadFully_offsetBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 5, 1));
            }
        }
    }

    @Test
    void testReadFully_offsetAndLengthBeyondLength_throwsIOBE() throws Exception {
        final byte[] bytes = { 1, 2, 3, 4, 5 };
        populateBlob(1, bytes);

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[5];
                assertThrows(IndexOutOfBoundsException.class, () -> is.readFully(buffer, 0, 6));
            }
        }
    }

    @Test
    void testReadFully_bufferLongerThanBlob_throwsEOFException() throws Exception {
        populateBlob(1, new byte[] { 1, 2, 3, 4, 5 });

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_BLOB)) {
            pstmt.setInt(1, 1);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                Blob blob = rs.getBlob(1);
                FBBlobInputStream is = (FBBlobInputStream) blob.getBinaryStream();

                byte[] buffer = new byte[6];
                assertThrows(EOFException.class, () -> is.readFully(buffer));
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void populateBlob(int id, byte[] bytes) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(INSERT_BLOB)) {
            insert.setInt(1, id);
            insert.setBytes(2, bytes);
            insert.execute();
        }
    }
}
