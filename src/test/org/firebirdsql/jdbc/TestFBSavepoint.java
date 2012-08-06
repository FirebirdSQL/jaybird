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

import java.sql.*;

import org.firebirdsql.common.FBTestBase;

/**
 * Tests for savepoint handling.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBSavepoint extends FBTestBase {

    
    private FirebirdConnection connection;
    
    public TestFBSavepoint(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        connection = (FirebirdConnection)getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
        try {
            stmt.execute("DROP TABLE test_svpt");
        } catch(SQLException ex) {
            // do nothing
        }
        
        try {
            stmt.execute("CREATE TABLE test_svpt(id INTEGER)");
        } catch(SQLException ex) {
            // ignore, most likely table exists
        } finally {
            stmt.close();
        }
    }

    protected void tearDown() throws Exception {
        
        Statement stmt = connection.createStatement();
        
        try {
            stmt.execute("DROP TABLE test_svpt");
        } catch(SQLException ex) {
            ex.printStackTrace();
        } finally {
            stmt.close();
        }
        
        connection.close();
        
        super.tearDown();
    }

    /**
     * Test if basic savepoint handling works.
     * 
     * @throws Exception if something went wrong.
     */
    public void testBasicSavepoint() throws Exception {
        
        connection.setAutoCommit(true);
        try {
            connection.setSavepoint();
            assertTrue("Setting savepoint should not work in auto-commit mode", false);
        } catch(SQLException e) {
            // everything is fine
        }
            
        connection.setAutoCommit(false);
            
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_svpt VALUES(?)");
            
        try {
            stmt.setInt(1, 1);
            stmt.execute();
                
            Savepoint svpt1 = connection.setSavepoint();
            
            try {
                svpt1.getSavepointName();
            } catch(SQLException ex) {
                // everything is fine
            }
                
            stmt.clearParameters();
    
            stmt.setInt(1, 2);
            stmt.execute();
            
            connection.rollback(svpt1);
            connection.commit();
            
            checkRowCount(1);
            
        } finally {
            stmt.close();
        }
    }
   
    /**
     * Test if named savepoint handling works.
     * 
     * @throws Exception if something went wrong.
     */
    public void testNamedSavepoint() throws Exception {
        
        connection.setAutoCommit(false);
            
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_svpt VALUES(?)");
            
        try {
            stmt.setInt(1, 1);
            stmt.execute();
                
            Savepoint svpt1 = connection.setSavepoint("test");
            
            try {
                svpt1.getSavepointId();
            } catch(SQLException ex) {
                // everything is fine
            }
                
            stmt.clearParameters();
    
            stmt.setInt(1, 2);
            stmt.execute();
            
            Savepoint svpt2 = connection.setSavepoint("test");
            
            assertTrue("Savepoints should be equal.", svpt1.equals(svpt2));
            
            stmt.clearParameters();
            stmt.setInt(1, 3);
            stmt.execute();
            
            connection.rollback(svpt1);
            connection.commit();
            
            checkRowCount(2);
            
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if savepoints are released correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testSavepointRelease() throws Exception {
        connection.setAutoCommit(false);
        
        Savepoint svpt1 = connection.setSavepoint("test");
        
        connection.releaseSavepoint(svpt1);
        
        checkInvalidSavepoint(svpt1);
        
        Savepoint svpt2 = connection.setSavepoint();
        
        connection.releaseSavepoint(svpt2);
        
        checkInvalidSavepoint(svpt2);
        
        Savepoint svpt3 = connection.setSavepoint();
        
        connection.commit();
        
        checkInvalidSavepoint(svpt3);
    }
        
    private void checkInvalidSavepoint(Savepoint savepoint) {
        try {
            connection.rollback(savepoint);
            assertTrue("Released savepoint should not work.", false);
        } catch(SQLException ex) {
            // everything is fine
        }

        try {
            connection.releaseSavepoint(savepoint);
            assertTrue("Released savepoint should not work.", false);
        } catch(SQLException ex) {
            // everything is fine
        }
    }

    /**
     * Check if table contains correct number of rows.
     * 
     * @param testRowCount expected number of rows.
     * 
     * @throws SQLException if something went wrong when counting rows.
     */
    private void checkRowCount(int testRowCount) throws SQLException {
        Statement stmt = connection.createStatement();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_svpt");
            
            int counter = 0;
            while(rs.next())
                counter++;
                
            assertTrue("Incorrect result set, expecting " + testRowCount + 
                " rows, obtained " + counter + ".", testRowCount == counter);
            
        } finally {
            stmt.close();
        }
    }
}
