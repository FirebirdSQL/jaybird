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
import org.firebirdsql.common.JdbcResourceHelper;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import static org.firebirdsql.common.FBTestProperties.*;

/**
 * Describe class <code>TestFBBlobAccess</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBBlobStream extends FBTestBase {
    
    private static final Random rnd = new Random();
    
    public static final String CREATE_TABLE =
        "CREATE TABLE test_blob(" +
        "  id INTEGER, " +
        "  bin_data BLOB, " +
        "  char_data BLOB SUB_TYPE 1  " +
        ")";
    
    public static final String CREATE_PROCEDURE = "" 
        + "CREATE PROCEDURE test_procedure(id INTEGER, char_data BLOB SUB_TYPE 1) "
        + "AS BEGIN "
        + "  INSERT INTO test_blob(id, char_data) VALUES (:id, :char_data);"
        + "END"
        ;

    public static int TEST_ROW_COUNT = 100;

    private Connection connection;

    public TestFBBlobStream(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Properties props = getDefaultPropertiesForConnection();
        props.put("isc_dpb_use_stream_blobs", "");
        
        connection = DriverManager.getConnection(getUrl(), props);

        java.sql.Statement stmt = connection.createStatement();

        stmt.executeUpdate(CREATE_TABLE);
        stmt.execute(CREATE_PROCEDURE);
        stmt.close();
    }

    protected void tearDown() throws Exception {
        JdbcResourceHelper.closeQuietly(connection);
        super.tearDown();
    }
    
    /**
     * Test if BLOB length is reported correctly.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void testBlobLength() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");
        
        int size = generateRandomLength();
        byte[] data = createRandomBytes(size);

        try {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
            ps.executeUpdate();
    
            ps.close();
    
            connection.commit();
    
            ps = connection.prepareStatement(
                "SELECT bin_data FROM test_blob WHERE id = ?");
               
            ps.setInt(1, 1);
            
            ResultSet rs = ps.executeQuery();
            
            assertTrue("Should select at least one row", rs.next());
            
            FBBlob blob = (FBBlob)rs.getBlob(1);
            
            // Do it repeatedly (TODO: does this make sense)
            for(int i = 0; i < 1000; i++)
                assertEquals("Reported length should be correct.", size, blob.length());
            rs.close();
        } finally {
            ps.close();
        }
    }
    
    /**
     * Test if BLOB seek() method works correctly.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void testBlobSeek() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        byte[] data = createRandomBytes(generateRandomLength());
        try {
            ps.setInt(1, 1);
            ps.setBytes(2, data);
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
    
            FBBlobInputStream in = (FBBlobInputStream)rs.getBinaryStream(1);
            
            int blobSize = (int)in.length();
            byte[] fullBlob = new byte[blobSize];
            
            in.readFully(fullBlob);
            
            in.close();
            
            in = (FBBlobInputStream)rs.getBinaryStream(1);
            in.seek(10);
           
            byte[] truncatedBlob = new byte[blobSize - 10];
            in.readFully(truncatedBlob);
            
            byte[] testBlob = new byte[blobSize - 10];
            System.arraycopy(fullBlob, 10, testBlob, 0, blobSize - 10);
    
            assertTrue("Full and original blobs must be equal.", 
                Arrays.equals(data, fullBlob));

            assertTrue("Truncated and testing blobs must be equal.", 
                Arrays.equals(testBlob, truncatedBlob));
            
            rs.close();
        } finally {
            ps.close();
        }
    }
   
    /**
     * Test if byte[] are correctly stored and retrieved from database
     *
     * @throws Exception if something went wrong.
     */
    public void testFieldTypes() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");
        
        byte[][] testData;
        testData = new byte[TEST_ROW_COUNT][0];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = new byte[generateRandomLength()];
            rnd.nextBytes(testData[i]);
        }

        long start = System.currentTimeMillis();
        long size = 0;

        for(int i = 0; i < TEST_ROW_COUNT; i++) {
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
                while(rs.next()) {
                    int id = rs.getInt("id");
                    byte[] data = rs.getBytes("bin_data");
    
                    size += data.length;
    
                    assertTrue(
                        "Data read from database for id " + id +
                        " should be equal to generated one.",
                        Arrays.equals(testData[id], data));
    
                    counter++;
                }
    
                assertEquals("Unexpected number of rows read", TEST_ROW_COUNT, counter);
    
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
     * Check if specifying more bytes than available in the stream does not 
     * cause endless loop.
     * 
     * @throws Exception if something went wrong.
     */
    public void testEndlessLoop() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
    
            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, 10);
            long startTime = System.currentTimeMillis();
            ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            
            if (endTime - startTime > 5000) {
                fail("Executing update with empty binarystream took longer than 5 seconds");
            }
            ps.close();
            connection.commit();
    
            ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?");
            ps.setInt(1, 1);
            
            ResultSet rs = ps.executeQuery();
            
            assertTrue("Should select at least one row", rs.next());
            byte[] blob = rs.getBytes(1);
            
            assertTrue("Reported length should be correct.", blob.length == 0);
    
            rs.close();
        } finally {
            ps.close();
        }
    }
    
    /**
     * Check if using only 1 byte from the stream works
     * 
     * @throws Exception if something went wrong.
     */
    public void testSingleByteRead() throws Exception {
        connection.setAutoCommit(false);

        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");

        final byte[] testData = new byte[] { 56, 54, 52 };
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(testData);
    
            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, 1);
            long startTime = System.currentTimeMillis();
            ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            
            if (endTime - startTime > 5000) {
                fail("Executing update with reading 1 byte from binarystream took longer than 5 seconds");
            }
            ps.close();
            connection.commit();
    
            ps = connection.prepareStatement("SELECT bin_data FROM test_blob WHERE id = ?");
            ps.setInt(1, 1);
            
            ResultSet rs = ps.executeQuery();
            
            assertTrue("Should select at least one row", rs.next());
            byte[] blob = rs.getBytes(1);
            
            assertEquals("Reported length should be correct.", 1, blob.length);
            assertEquals("Unexpected value for first byte", 56, blob[0]);
    
            rs.close();
        } finally {
            ps.close();
        }
    }
    
    public void testStreamForLongVarChar() throws Exception {
        PreparedStatement ps = connection.prepareCall("{call test_procedure(?, ?)}");
        
        try {
            byte[] data = createRandomBytes(generateRandomLength());
            
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            
            ps.setInt(1, 1);
            ps.setBinaryStream(2, in, data.length);
            
            ps.execute();
            
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery(
                    "SELECT id, char_data FROM test_blob WHERE id = 1");
                
                assertTrue("Should select data from table", rs.next());
                assertTrue("Value should be correct", Arrays.equals(rs.getBytes(2), data));
                assertTrue("Should not have more rows.", !rs.next());
            } finally {
                stmt.close();
            }
        } finally {
            ps.close();
        }
    }
    
    public void testWriteBytes() throws Exception {
        final byte[] data = createRandomBytes(75 * 1024); // should be more than 64k
        
        FirebirdConnection fbConnection = (FirebirdConnection)connection;
        fbConnection.setAutoCommit(false);
        
        FirebirdBlob blob = (FirebirdBlob)fbConnection.createBlob();
        OutputStream out = blob.setBinaryStream(1);
        out.write(data);
        out.flush();
        out.close();
        
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
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
            assertTrue("Selected data should be equal.", Arrays.equals(data, testData));
            assertTrue("Should have selected only one row.", !rs.next());
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Creates a byte array with random bytes with the specified length.
     * 
     * @param length Requested length
     * @return Byte array of length filled with random bytes
     */
    private static byte[] createRandomBytes(int length) {
        byte[] randomBytes = new byte[length];
        rnd.nextBytes(randomBytes);
        return randomBytes;
    }
    
    /**
     * Generates a random length betwen 128 and 102528
     * @return generated length
     */
    private static int generateRandomLength() {
        return rnd.nextInt(100 * 1024) + 128;
    }
}