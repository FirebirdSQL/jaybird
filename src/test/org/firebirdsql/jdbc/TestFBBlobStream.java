/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/**
 * Describe class <code>TestFBBlobAccess</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBBlobStream extends FBTestBase {

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
    private byte[][] testData;

    public TestFBBlobStream(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = getDefaultPropertiesForConnection();
        props.put("isc_dpb_use_stream_blobs", "");

        connection = DriverManager.getConnection(getUrl(), props);

        Statement stmt = connection.createStatement();
        stmt.executeUpdate(CREATE_TABLE);
        stmt.execute(CREATE_PROCEDURE);
        stmt.close();

        java.util.Random rnd = new java.util.Random();

        testData = new byte[TEST_ROW_COUNT][0];

        for (int i = 0; i < testData.length; i++) {
            int testLength = rnd.nextInt(100 * 1024) + 128;
            testData[i] = new byte[testLength];
            rnd.nextBytes(testData[i]);
        }
    }

    protected void tearDown() throws Exception {
        try {
            connection.close();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Test if BLOB length is reported correctly.
     *
     * @throws java.lang.Exception
     *         if something went wrong.
     */
    public void testBlobLength() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        try {
            long size = testData[0].length;

            ps.setInt(1, 1);
            ps.setBytes(2, testData[0]);
            ps.executeUpdate();
            ps.close();

            connection.commit();

            ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?");

            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();

            assertTrue("Should select at least one row", rs.next());

            FBBlob blob = (FBBlob) rs.getBlob(1);

            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++)
                assertEquals("Reported length should be correct.", size, blob.length());
            System.out.println("Getting info took " + (System.currentTimeMillis() - start));

            rs.close();
        } finally {
            ps.close();
        }
    }

    /**
     * Test if BLOB seek() method works correctly.
     *
     * @throws java.lang.Exception
     *         if something went wrong.
     */
    public void testBlobSeek() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");
        try {
            ps.setInt(1, 1);
            ps.setBytes(2, testData[0]);
            ps.executeUpdate();
        } finally {
            ps.close();
        }

        connection.commit();

        try {
            ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?");

            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();

            assertTrue("Should select at least one row", rs.next());

            FBBlobInputStream in = (FBBlobInputStream) rs.getBinaryStream(1);

            int blobSize = (int) in.length();
            byte[] fullBlob = new byte[blobSize];

            in.readFully(fullBlob);
            in.close();

            in = (FBBlobInputStream) rs.getBinaryStream(1);
            in.seek(10);

            byte[] truncatedBlob = new byte[blobSize - 10];
            in.readFully(truncatedBlob);

            byte[] testBlob = new byte[blobSize - 10];
            System.arraycopy(fullBlob, 10, testBlob, 0, blobSize - 10);

            assertArrayEquals("Full and original blobs must be equal.", testData[0], fullBlob);
            assertArrayEquals("Truncated and testing blobs must be equal.", testBlob, truncatedBlob);

            rs.close();
        } finally {
            ps.close();
        }
    }

    /**
     * Test if byte[] are correctly stored and retrieved from database
     *
     * @throws Exception
     *         if something went wrong.
     */
    public void testFieldTypes() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        long start = System.currentTimeMillis();
        long size = 0;

        for (int i = 0; i < TEST_ROW_COUNT; i++) {
            ps.setInt(1, i);
            ps.setBytes(2, testData[i]);

            size += testData[i].length;

            ps.execute();
        }

        long duration = System.currentTimeMillis() - start;

        System.out.println("Inserted " + size + " bytes in " + duration + " ms, " +
                "speed " + ((size * 1000 * 1000 / duration / 1024 / 1024) / 1000.0) + " MB/s");

        ps.close();

        connection.commit();

        Statement stmt = connection.createStatement();

        for (int i = 0; i < 10; i++) {
            ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob");
            start = System.currentTimeMillis();
            size = 0;

            try {
                int counter = 0;

                while (rs.next()) {
                    int id = rs.getInt("id");
                    byte[] data = rs.getBytes("bin_data");

                    size += data.length;

                    assertArrayEquals("Data read from database for id " + id + " should be equal to generated one.",
                            testData[id], data);

                    counter++;
                }

                assertEquals("Should read " + TEST_ROW_COUNT + " rows, read " + counter, TEST_ROW_COUNT, counter);

                duration = System.currentTimeMillis() - start;

                System.out.println("Read " + size + " bytes in " + duration + " ms, " +
                        "speed " + ((size * 1000 * 1000 / duration / 1024 / 1024) / 1000.0) + " MB/s");
            } finally {
                rs.close();
            }
        }
        stmt.close();
    }

    /**
     * Check if specifying more bytes than available in the stream does not cause endless loop.
     *
     * @throws Exception
     *         if something went wrong.
     */
    public void testEndlessLoop() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, 10);
            ps.executeUpdate();
            ps.close();

            connection.commit();

            ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?");

            ps.setInt(1, 1);
            ResultSet rs = ps.executeQuery();

            assertTrue("Should select at least one row", rs.next());

            byte[] blob = rs.getBytes(1);

            assertEquals("Reported length should be correct.", 0, blob.length);

            rs.close();
        } finally {
            ps.close();
        }

    }

    public void testStreamForLongVarChar() throws Exception {
        PreparedStatement ps = connection.prepareCall("{call test_procedure(?, ?)}");

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(testData[0]);

            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, testData[0].length);
            ps.execute();

            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT id, char_data FROM test_blob WHERE id = 1");

                assertTrue("Should select data from table", rs.next());
                assertArrayEquals("Value should be correct", testData[0], rs.getBytes(2));
                assertFalse("Should not have more rows.", rs.next());
            } finally {
                stmt.close();
            }
        } finally {
            ps.close();
        }
    }

    public void testWriteBytes() throws Exception {
        byte[] data = new byte[75 * 1024]; // should be more than 64k
        Random rnd = new Random();
        rnd.nextBytes(data);

        FirebirdConnection fbConnection = (FirebirdConnection) connection;
        fbConnection.setAutoCommit(false);

        FirebirdBlob blob = (FirebirdBlob) fbConnection.createBlob();
        OutputStream out = blob.setBinaryStream(1);
        out.write(data);
        out.flush();
        out.close();

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
        try {
            ps.setInt(1, 1);
            ps.setBlob(2, blob);
            ps.execute();
        } finally {
            ps.close();
        }

        fbConnection.commit();

        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT bin_data FROM test_blob WHERE id = 1");
            assertTrue("Should have selected at least one row.", rs.next());
            byte[] testData = rs.getBytes(1);
            assertArrayEquals("Selected data should be equal.", data, testData);
            assertFalse("Should have selected only one row.", rs.next());
        } finally {
            stmt.close();
        }
    }
}