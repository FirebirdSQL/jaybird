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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FBBlobAutocommitTest {

    //@formatter:off
    private static final String CREATE_TABLE =
            "CREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB, " +
            "  char_data BLOB SUB_TYPE 1, " +
            "  blob_data BLOB SUB_TYPE -1" +
            ")";
    //@formatter:on

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static final String TEST_STRING = "just a test string";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes();
    private static final int TEST_ID = 1;

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        connection = getConnectionViaDriverManager();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("delete from test_blob");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if correct object types are returned from {@link ResultSet#getObject(int)} depending on column type.
     */
    @Test
    void testFieldTypes() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, TEST_ID);
            stmt.setBytes(2, TEST_BYTES);
            stmt.setBytes(3, TEST_BYTES);
            stmt.setBytes(4, TEST_BYTES);

            int inserted = stmt.executeUpdate();
            assertEquals(1, inserted, "Should insert one row");
        }

        connection.close();
        connection = getConnectionViaDriverManager();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM test_blob WHERE id = ?")) {
            stmt.setInt(1, TEST_ID);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Result set should have at least 1 row");

                assertEquals(TEST_ID, rs.getInt(1), "ID should be the same");
                assertThat("getObject() on binary field should return byte[]",
                        rs.getObject(2), instanceOf(byte[].class));
                assertThat("getObject() on text field should return String", rs.getObject(3), instanceOf(String.class));
                assertThat("getObject() on blob field should return java.sql.Blob",
                        rs.getObject(4), instanceOf(Blob.class));

                assertArrayEquals(TEST_BYTES, rs.getBytes(2), "content of binary field should be same to written");
                assertArrayEquals(TEST_BYTES, rs.getBytes(3), "content of text field should be same to written");

                assertEquals(TEST_STRING, rs.getString(3), "string values should be the same");

                assertArrayEquals(TEST_BYTES, rs.getBlob(4).getBytes(1, TEST_BYTES.length),
                        "content of blob field should be same to written");
                assertArrayEquals(TEST_BYTES, rs.getBytes(4), "content of text field should be same to written");

                assertFalse(rs.next(), "Result set should contain only one row");
            }
        }
    }

    /**
     * Test if {@link PreparedStatement#setBinaryStream(int, InputStream)} stores data correctly in auto-commit case.
     */
    @Test
    void testSetBinaryStream() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 2);
            stmt.setBinaryStream(2, new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);

            int insertedCount = stmt.executeUpdate();
            assertEquals(1, insertedCount, "Should insert one row");
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            stmt.setInt(1, 2);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should get at least one record");

                byte[] bytes = rs.getBytes(1);

                assertArrayEquals(TEST_BYTES, bytes, "Should read previously saved data");
                assertFalse(rs.next(), "Should have exactly one record");
            }
        }
    }

    /**
     * Test if driver returns correctly empty and null blobs.
     */
    @Test
    void testEmptyOrNullBlob() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            stmt.setInt(1, 3);
            stmt.setBytes(2, new byte[0]);

            int insertedCount = stmt.executeUpdate();
            assertEquals(1, insertedCount, "Should insert one row");

            stmt.setInt(1, 4);
            stmt.setNull(2, Types.BINARY);

            insertedCount = stmt.executeUpdate();
            assertEquals(1, insertedCount, "Should insert one row");
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            stmt.setInt(1, 3);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");
                assertArrayEquals(new byte[0], rs.getBytes(1), "Result should byte[0]");
            }

            stmt.setInt(1, 4);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");
                assertNull(rs.getObject(1), "Result should byte[0]");
            }
        }
    }

    /**
     * Test if {@link PreparedStatement#setBinaryStream(int, InputStream)} stores
     * data correctly in auto-commit case.
     */
    @Test
    void testSetBlob() throws Exception {
        connection.setAutoCommit(false);

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            connection.commit();

            stmt.setInt(1, 2);
            stmt.setBlob(2, new TempBlobImpl(TEST_BYTES));

            int insertedCount = stmt.executeUpdate();

            assertEquals(1, insertedCount, "Should insert one row");
        }

        try (PreparedStatement stmt = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            stmt.setInt(1, 2);

            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next(), "Should get at least one record");

                assertArrayEquals(TEST_BYTES, rs.getBytes(1), "Should read previously saved data");
                assertFalse(rs.next(), "Should have exactly one record");
            }
        }
    }

    private static class TempBlobImpl implements Blob {

        private final byte[] data;

        TempBlobImpl(byte[] data) {
            this.data = data;
        }

        public long length() {
            return data.length;
        }

        public byte[] getBytes(long pos, int length) {
            int lastPos = (int) pos + length;

            if (lastPos > data.length)
                lastPos = data.length;

            byte[] result = new byte[lastPos - (int) pos];
            System.arraycopy(data, (int) pos, result, 0, result.length);
            return result;
        }

        public java.io.InputStream getBinaryStream() {
            return new ByteArrayInputStream(data);
        }

        public long position(byte[] pattern, long start) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public long position(Blob pattern, long start) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public int setBytes(long pos, byte[] bytes) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public java.io.OutputStream setBinaryStream(long pos) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public void truncate(long len) throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public void free() throws SQLException {
            throw new FBDriverNotCapableException();
        }

        public InputStream getBinaryStream(long pos, long length)
                throws SQLException {
            throw new FBDriverNotCapableException();
        }
    }
}
