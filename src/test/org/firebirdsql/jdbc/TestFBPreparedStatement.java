/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import java.sql.*;

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
        con =
            java.sql.DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
            

        java.sql.Statement stmt = con.createStatement();
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
        java.sql.Statement stmt = con.createStatement();
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