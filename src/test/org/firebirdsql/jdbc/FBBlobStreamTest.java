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
import org.firebirdsql.jaybird.props.PropertyNames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Describe class <code>TestFBBlobAccess</code> here.
 *
 * @author Roman Rokytskyy
 * @version 1.0
 */
class FBBlobStreamTest {

    //@formatter:off
    private static final String CREATE_TABLE =
        "CREATE TABLE test_blob(" +
        "  id INTEGER, " +
        "  bin_data BLOB, " +
        "  char_data BLOB SUB_TYPE 1  " +
        ")";

    private static final String CREATE_PROCEDURE =
        "CREATE PROCEDURE test_procedure(id INTEGER, char_data BLOB SUB_TYPE 1) " +
        "AS BEGIN " +
        "  INSERT INTO test_blob(id, char_data) VALUES (:id, :char_data);" +
        "END";
    //@formatter:on

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE,
            CREATE_PROCEDURE);

    private static final int TEST_ROW_COUNT = 100;

    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put(PropertyNames.useStreamBlobs, "");

        connection = DriverManager.getConnection(getUrl(), props);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("delete from test_blob");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    /**
     * Test if BLOB length is reported correctly.
     */
    @Test
    void testBlobLength() throws Exception {
        connection.setAutoCommit(false);
        int size = generateRandomLength();
        byte[] data = DataGenerator.createRandomBytes(size);

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
            ps.execute();
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");

                FBBlob blob = (FBBlob) rs.getBlob(1);
                // Do it repeatedly (TODO: does this make sense)
                for (int i = 0; i < 1000; i++) {
                    assertEquals(size, blob.length(), "Reported length should be correct");
                }
            }
        }
    }

    /**
     * Test if BLOB seek() method works correctly.
     */
    @Test
    void testBlobSeek() throws Exception {
        connection.setAutoCommit(false);

        byte[] data = DataGenerator.createRandomBytes(generateRandomLength());
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
            ps.execute();
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");

                byte[] fullBlob;
                int blobSize;
                try (FBBlobInputStream in = (FBBlobInputStream) rs.getBinaryStream(1)) {
                    blobSize = (int) in.length();
                    fullBlob = new byte[blobSize];
                    in.readFully(fullBlob);
                }

                byte[] truncatedBlob;
                try (FBBlobInputStream in = (FBBlobInputStream) rs.getBinaryStream(1)) {
                    in.seek(10);
                    truncatedBlob = new byte[blobSize - 10];
                    in.readFully(truncatedBlob);
                }

                byte[] testBlob = new byte[blobSize - 10];
                System.arraycopy(fullBlob, 10, testBlob, 0, blobSize - 10);

                assertArrayEquals(data, fullBlob, "Full and original blobs must be equal");
                assertArrayEquals(testBlob, truncatedBlob, "Truncated and testing blobs must be equal");
            }
        }
    }

    /**
     * Test if byte[] are correctly stored and retrieved from database
     */
    @Test
    void testFieldTypes() throws Exception {
        connection.setAutoCommit(false);
        byte[][] testData;
        testData = new byte[TEST_ROW_COUNT][0];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = DataGenerator.createRandomBytes(generateRandomLength());
        }

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            for (int i = 0; i < TEST_ROW_COUNT; i++) {
                ps.setInt(1, i);
                ps.setBytes(2, testData[i]);
                ps.execute();
            }
        }

        connection.commit();

        try (Statement stmt = connection.createStatement()) {
            for (int i = 0; i < 10; i++) {
                try (ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob")) {
                    int counter = 0;
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        byte[] data = rs.getBytes("bin_data");

                        assertArrayEquals(testData[id], data,
                                () -> "Data read from database for id " + id + " should be equal to generated one");
                        counter++;
                    }

                    assertEquals(TEST_ROW_COUNT, counter, "Unexpected number of rows read");
                }
            }
        }
    }

    /**
     * Check if specifying more bytes than available in the stream does not cause endless loop.
     */
    @Test
    @Timeout(10)
    void testEndlessLoop() throws Exception {
        connection.setAutoCommit(false);

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, 10);
            long startTime = System.currentTimeMillis();
            ps.executeUpdate();
            long endTime = System.currentTimeMillis();

            if (endTime - startTime > 5000) {
                fail("Executing update with empty binarystream took longer than 5 seconds");
            }
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");
                byte[] blob = rs.getBytes(1);

                assertEquals(0, blob.length, "Reported length should be correct");
            }
        }
    }

    /**
     * Check if using only 1 byte from the stream works
     */
    @Test
    @Timeout(10)
    void testSingleByteRead() throws Exception {
        connection.setAutoCommit(false);

        final byte[] testData = new byte[] { 56, 54, 52 };
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ByteArrayInputStream in = new ByteArrayInputStream(testData);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, 1);
            long startTime = System.currentTimeMillis();
            ps.executeUpdate();
            long endTime = System.currentTimeMillis();

            if (endTime - startTime > 5000) {
                fail("Executing update with reading 1 byte from binarystream took longer than 5 seconds");
            }
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should select at least one row");
                byte[] blob = rs.getBytes(1);

                assertEquals(1, blob.length, "Reported length should be correct");
                assertEquals(56, blob[0], "Unexpected value for first byte");
            }
        }
    }

    @Test
    void testStreamForLongVarChar() throws Exception {
        byte[] data = DataGenerator.createRandomBytes(generateRandomLength());
        try (PreparedStatement ps = connection.prepareCall("{call test_procedure(?, ?)}")) {
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, data.length);

            ps.execute();
        }

        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT id, char_data FROM test_blob WHERE id = 1")) {
                assertTrue(rs.next(), "Should select data from table");
                assertArrayEquals(data, rs.getBytes(2), "Value should be correct");
                assertFalse(rs.next(), "Should not have more rows");
            }
        }
    }

    @Test
    void testWriteBytes() throws Exception {
        final byte[] data = DataGenerator.createRandomBytes(75 * 1024); // should be more than 64k

        connection.setAutoCommit(false);

        FirebirdBlob blob = (FirebirdBlob) connection.createBlob();
        try (OutputStream out = blob.setBinaryStream(1)) {
            out.write(data);
        }

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)")) {
            ps.setInt(1, 1);
            ps.setBlob(2, blob);
            ps.execute();
        }

        connection.commit();

        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT bin_data FROM test_blob WHERE id = 1")) {
                assertTrue(rs.next(), "Should have selected at least one row");
                byte[] testData = rs.getBytes(1);
                assertArrayEquals(data, testData, "Selected data should be equal");
                assertFalse(rs.next(), "Should have selected only one row");
            }
        }
    }

    /**
     * Generates a random length between 128 and 102400.
     *
     * @return generated length
     */
    private static int generateRandomLength() {
        return DataGenerator.generateRandom(128, 100 * 1024);
    }
}