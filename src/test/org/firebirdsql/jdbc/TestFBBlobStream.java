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

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.junit.Assert.*;

/**
 * Describe class <code>TestFBBlobAccess</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBBlobStream extends FBJUnit4TestBase {

    private static final Random rnd = new Random();

    //@formatter:off
    public static final String CREATE_TABLE =
        "CREATE TABLE test_blob(" +
        "  id INTEGER, " +
        "  bin_data BLOB, " +
        "  char_data BLOB SUB_TYPE 1  " +
        ")";
    
    public static final String CREATE_PROCEDURE =
        "CREATE PROCEDURE test_procedure(id INTEGER, char_data BLOB SUB_TYPE 1) " +
        "AS BEGIN " +
        "  INSERT INTO test_blob(id, char_data) VALUES (:id, :char_data);" +
        "END";
    //@formatter:on

    private static final int TEST_ROW_COUNT = 100;

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("isc_dpb_use_stream_blobs", "");

        connection = DriverManager.getConnection(getUrl(), props);

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(CREATE_TABLE);
            stmt.execute(CREATE_PROCEDURE);
        }
    }

    @After
    public void tearDown() throws Exception {
        JdbcResourceHelper.closeQuietly(connection);
    }

    /**
     * Test if BLOB length is reported correctly.
     */
    @Test
    public void testBlobLength() throws Exception {
        connection.setAutoCommit(false);
        int size = generateRandomLength();
        byte[] data = DataGenerator.createRandomBytes(size);

        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
            ps.executeUpdate();
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("Should select at least one row", rs.next());

                FBBlob blob = (FBBlob) rs.getBlob(1);
                // Do it repeatedly (TODO: does this make sense)
                for (int i = 0; i < 1000; i++)
                    assertEquals("Reported length should be correct.", size, blob.length());
            }
        }
    }

    /**
     * Test if BLOB seek() method works correctly.
     */
    @Test
    public void testBlobSeek() throws Exception {
        connection.setAutoCommit(false);

        byte[] data = DataGenerator.createRandomBytes(generateRandomLength());
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)")) {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
            ps.executeUpdate();
        }

        connection.commit();

        try (PreparedStatement ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?")) {
            ps.setInt(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue("Should select at least one row", rs.next());

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

                assertArrayEquals("Full and original blobs must be equal.", data, fullBlob);
                assertArrayEquals("Truncated and testing blobs must be equal.", testBlob, truncatedBlob);
            }
        }
    }

    /**
     * Test if byte[] are correctly stored and retrieved from database
     */
    @Test
    public void testFieldTypes() throws Exception {
        connection.setAutoCommit(false);
        byte[][] testData;
        testData = new byte[TEST_ROW_COUNT][0];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = new byte[generateRandomLength()];
            rnd.nextBytes(testData[i]);
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

                        assertArrayEquals("Data read from database for id " + id + " should be equal to generated one.",
                                testData[id], data);
                        counter++;
                    }

                    assertEquals("Unexpected number of rows read", TEST_ROW_COUNT, counter);
                }
            }
        }
    }

    /**
     * Check if specifying more bytes than available in the stream does not cause endless loop.
     */
    @Test
    public void testEndlessLoop() throws Exception {
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
                assertTrue("Should select at least one row", rs.next());
                byte[] blob = rs.getBytes(1);

                assertEquals("Reported length should be correct.", 0, blob.length);
            }
        }
    }

    /**
     * Check if using only 1 byte from the stream works
     */
    @Test
    public void testSingleByteRead() throws Exception {
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
                assertTrue("Should select at least one row", rs.next());
                byte[] blob = rs.getBytes(1);

                assertEquals("Reported length should be correct.", 1, blob.length);
                assertEquals("Unexpected value for first byte", 56, blob[0]);
            }
        }
    }

    @Test
    public void testStreamForLongVarChar() throws Exception {
        byte[] data = DataGenerator.createRandomBytes(generateRandomLength());
        try (PreparedStatement ps = connection.prepareCall("{call test_procedure(?, ?)}")) {
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, data.length);

            ps.execute();
        }

        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT id, char_data FROM test_blob WHERE id = 1")) {
                assertTrue("Should select data from table", rs.next());
                assertTrue("Value should be correct", Arrays.equals(rs.getBytes(2), data));
                assertTrue("Should not have more rows.", !rs.next());
            }
        }
    }

    @Test
    public void testWriteBytes() throws Exception {
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
                assertTrue("Should have selected at least one row.", rs.next());
                byte[] testData = rs.getBytes(1);
                assertArrayEquals("Selected data should be equal.", data, testData);
                assertFalse("Should have selected only one row.", rs.next());
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