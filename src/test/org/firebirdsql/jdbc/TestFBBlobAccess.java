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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Random;


/**
 * Describe class <code>TestFBBlobAccess</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBBlobAccess extends FBTestBase {
    public static final String CREATE_TABLE = 
        "CREATE TABLE test_blob(" + 
        "  id INTEGER, " + 
        "  bin_data BLOB " + 
        ")";
        
    public static final String CREATE_VIEW = 
        "CREATE VIEW test_blob_view (id, bin_data) AS " +
        "  SELECT id, bin_data FROM test_blob"
        ;
    
    public static final String DROP_TABLE = 
        "DROP TABLE test_blob";
        
    public static final String DROP_VIEW =
        "DROP VIEW test_blob_view";

    public static int TEST_ROW_COUNT = 10;        
        
    private Connection connection;
    
    private byte[][] testData;

    
    
    public TestFBBlobAccess(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(FBDriver.class.getName());
        connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
        
        try {
            stmt.execute(DROP_VIEW);
        } catch(SQLException ex) {
            // ignore
        }
        
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {
            // e.printStackTrace();
        }

        stmt.executeUpdate(CREATE_TABLE);
        stmt.execute(CREATE_VIEW);
        stmt.close();
        
        Random rnd = new Random();
        
        testData = new byte[TEST_ROW_COUNT][0];
        
        for (int i = 0; i < testData.length; i++) {
            int testLength = rnd.nextInt(100) + 10;
            testData[i] = new byte[testLength];
            rnd.nextBytes(testData[i]);
        }
        
    }

    protected void tearDown() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.execute(DROP_VIEW);
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();
        super.tearDown();
    }
    
    /**
     * Test if byte[] are correctly stored and retrieved from database
     * 
     * @throws Exception if something went wrong.
     */
    public void testFieldTypes() throws Exception {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)");
            
        for(int i = 0; i < TEST_ROW_COUNT; i++) {
            ps.setInt(1, i);
            ps.setBytes(2, testData[i]);
            
            ps.executeUpdate();
        }
        
        ps.close();
        
       
        Statement stmt = connection.createStatement();
            
        ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob");
        
        try {
            int counter = 0;
            
            while(rs.next()) {
                
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");
                
                assertTrue(
                    "Data read from database for id " + id + 
                    " should be equal to generated one.",
                    Arrays.equals(testData[id], data));
                    
                counter++;
            }
            
            assertTrue(
                "Should read " + TEST_ROW_COUNT + 
                " rows, read " + counter, TEST_ROW_COUNT == counter);
            
        } finally {
            rs.close();
            stmt.close();
        }
    }
    
    /**
     * This method checks if we correctly handle assigning null values to blobs.
     * @throws Exception if something went wrong
     */
    public void testSetNull() throws Exception {
        
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
            
        try {
            stmt.setInt(1, 1);
            stmt.setBytes(2, null);
            
            stmt.executeUpdate();
            
            stmt.setInt(1, 2);
            stmt.setBinaryStream(2, null, 0);
            
            stmt.executeUpdate();
            
            stmt.setInt(1, 3);
            stmt.setString(2, null);
            
            stmt.executeUpdate();
            
        } finally {
            stmt.close();
        }
    }
    
    public static final String TEST_TEXT = "Test text";
        
    /**
     * This test initially was created to find the bug when LONGVARCHAR column
     * is used, but it revealed the bug with blobs.
     * 
     * We try to execute some blob updates one by one in non-autocommit mode. 
     * This tests if transaction is started before the blob is created.
     * 
     * @throws Exception if something went wrong.
     */
    public void testSetString() throws Exception {
        
        connection.setAutoCommit(false);
                
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");

        try {
            stmt.setInt(1, 1);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();
            
            connection.commit();
            
            stmt.setInt(1, 2);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();
            
            connection.commit();

            stmt.setInt(1, 3);
            stmt.setString(2, TEST_TEXT);

            stmt.executeUpdate();

            connection.commit();
            
        } finally {
            stmt.close();
        }
        
        connection.setAutoCommit(true);

    }

    /**
     * Test if blobs are correctly accessed via views.
     * 
     * @throws Exception if something went wrong.
     */
    public void testViewAccess() throws Exception {
        
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_blob_view(id, bin_data) VALUES (?, ?)");
            
        for(int i = 0; i < TEST_ROW_COUNT; i++) {
            ps.setInt(1, i);
            ps.setBytes(2, testData[i]);
            
            ps.executeUpdate();
        }
        
        ps.close();
        
       
        Statement stmt = connection.createStatement();
            
        ResultSet rs = stmt.executeQuery("SELECT id, bin_data FROM test_blob_view");
        
        try {
            int counter = 0;
            
            while(rs.next()) {
                
                int id = rs.getInt("id");
                byte[] data = rs.getBytes("bin_data");
                
                assertTrue(
                    "Data read from database for id " + id + 
                    " should be equal to generated one.",
                    Arrays.equals(testData[id], data));
                    
                counter++;
            }
            
            assertTrue(
                "Should read " + TEST_ROW_COUNT + 
                " rows, read " + counter, TEST_ROW_COUNT == counter);
            
        } finally {
            rs.close();
            stmt.close();
        }
    }

}
