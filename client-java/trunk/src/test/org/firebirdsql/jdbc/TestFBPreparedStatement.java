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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Describe class <code>TestFBPreparedStatement</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBPreparedStatement extends BaseFBTest{
    
    public static final String CREATE_GENERATOR = 
        "CREATE GENERATOR test_generator";
        
    public static final String DROP_GENERATOR = 
        "DROP GENERATOR test_generator";
    
    public static final String CREATE_TEST_BLOB_TABLE = 
        "CREATE TABLE test_blob (" + 
        "  ID INTEGER, " + 
        "  OBJ_DATA BLOB " + 
        ")";
        
    public static final String DROP_TEST_BLOB_TABLE = 
        "DROP TABLE test_blob";
        
    public static final String TEST_STRING = "This is simple test string.";
    
    public static final String ANOTHER_TEST_STRING = "Another test string.";
    
    public TestFBPreparedStatement(String testName) {
        super(testName);
    }

    Connection con;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        con = DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);            

        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        
        try {
            stmt.executeUpdate(DROP_GENERATOR);
        } catch(Exception ex) {
        }
        
        stmt.executeUpdate(CREATE_TEST_BLOB_TABLE);
        
        stmt.executeUpdate(CREATE_GENERATOR);
        
        stmt.close(); 
        
    }

    protected void tearDown() throws Exception {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
        stmt.close();

        con.close();
        
        super.tearDown();
    }
    
    public void testModifyBlob() throws Exception {
        int id = 1;

        PreparedStatement insertPs = con.prepareStatement(
            "INSERT INTO test_blob (id, obj_data) VALUES (?,?);");
            
        insertPs.setInt(1, id);
        insertPs.setBytes(2, TEST_STRING.getBytes());
        
        int inserted = insertPs.executeUpdate();
        
        assertTrue("Row should be inserted.", inserted == 1);
        
        checkSelectString(TEST_STRING, id);
        
        //Update item
        PreparedStatement updatePs = con.prepareStatement(
            "UPDATE test_blob SET obj_data=? WHERE id=?;");
            
        updatePs.setBytes(1, ANOTHER_TEST_STRING.getBytes());
        updatePs.setInt(2, id);
        updatePs.executeUpdate();
        
        updatePs.clearParameters();
        
        checkSelectString(ANOTHER_TEST_STRING, id);
        
        updatePs.setBytes(1, TEST_STRING.getBytes());
        updatePs.setInt(2, id + 1);
        int updated = updatePs.executeUpdate();
        
        assertTrue("No rows should be updated.", updated == 0);
        
        checkSelectString(ANOTHER_TEST_STRING, id);
        
        insertPs.close();
        updatePs.close();
    }
    
    public void testMixedExecution() throws Throwable {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO test_blob VALUES(?, NULL)");
        
        try {

            ps.setInt(1, 100);
            ps.execute();

            ResultSet rs = ps.executeQuery("SELECT * FROM test_blob");
            while (rs.next()) {
                // nothing
            }
        } catch(Throwable t) {
            t.printStackTrace();
            throw t;
        } finally {
            ps.close();
        }
        
    }    
    
    void checkSelectString(String stringToTest, int id) throws Exception {
        PreparedStatement selectPs = con.prepareStatement(
            "SELECT obj_data FROM test_blob WHERE id = ?");
            
        selectPs.setInt(1, id);
        ResultSet rs = selectPs.executeQuery();
        
        assertTrue("There must be at least one row available.", rs.next());
        
        String result = rs.getString(1);
        
        assertTrue("Selected string must be equal to inserted one.", 
            stringToTest.equals(result));
            
        assertTrue("There must be exactly one row.", !rs.next());
        
        rs.close();
        selectPs.close();
    }
    
    public void testGenerator() throws Exception {
        PreparedStatement ps = con.prepareStatement(
            "SELECT gen_id(test_generator, 1) as new_value FROM rdb$database");
            
        ResultSet rs = ps.executeQuery();
        
        assertTrue("Should get at least one row", rs.next());
        
        long genValue = rs.getLong("new_value");
        
        assertTrue("should have only one row", !rs.next());
        
        rs.close();
        ps.close();
    }
    
}
