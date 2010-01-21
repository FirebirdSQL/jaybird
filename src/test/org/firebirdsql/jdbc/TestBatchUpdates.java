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

import java.io.StringReader;
import java.sql.*;

import org.firebirdsql.common.FBTestBase;

/**
 * Test batch updates.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestBatchUpdates extends FBTestBase {

    public TestBatchUpdates(String name) {
        super(name);
    }

    public static final String CREATE_TABLE = ""
        + "CREATE TABLE batch_updates("
        + "  id INTEGER, "
        + "  str_value BLOB, "
        + "  clob_value BLOB SUB_TYPE 1"
        + ")"
        ;
    
    public static final String DROP_TABLE = ""
        + "DROP TABLE batch_updates"
        ;
    
    private Connection connection;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
        try {
            try {
                stmt.execute(DROP_TABLE);
            } catch(SQLException ex) {
                // ignore, most likely - not found
            }
            
            stmt.execute(CREATE_TABLE);
            
        } finally {
            stmt.close();
        }
    }

    protected void tearDown() throws Exception {
        
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(DROP_TABLE);
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
        
        super.tearDown();
    }

    /**
     * Test if batch updates in {@link Statement} implementation works correctly.
     * 
     * @throws SQLException if something went wrong.
     */
    public void _testStatementBatch() throws SQLException {
        Statement stmt = connection.createStatement();
        try {
            stmt.addBatch("INSERT INTO batch_updates VALUES (1, 'test')");
            stmt.addBatch("INSERT INTO batch_updates VALUES (2, 'another')");
            stmt.addBatch("UPDATE batch_updates SET id = 3 WHERE id = 2");
            
            int[] updates = stmt.executeBatch();
            
            assertTrue("Should contain 3 results.", updates.length == 3);
            assertTrue("Should update one row each time", 
                updates[0] == 1 && updates[1] == 1 && updates[2] == 1);
            
            ResultSet rs = stmt.executeQuery("SELECT * FROM batch_updates");
            int counter = 0;
            while(rs.next())
                counter++;
            
            assertTrue("Should insert 2 rows", counter == 2);
            
            stmt.addBatch("DELETE FROM batch_updates");
            stmt.clearBatch();
            updates = stmt.executeBatch();
            
            assertTrue("No updates should have been made.", updates.length == 0);
            
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if batch updates work correctly with prepared statement.
     * 
     * @throws SQLException if something went wrong.
     */
    public void testPreparedStatementBatch() throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO batch_updates(id, str_value, clob_value) VALUES (?, ?, ?)");
        try {
            ps.setInt(1, 1);
            ps.setString(2, "test");
            ps.setNull(3, Types.LONGVARBINARY);
            ps.addBatch();

            ps.setInt(1, 3);
            ps.setCharacterStream(2, new StringReader("stream"), 11);
            ps.setString(3, "string");
            ps.addBatch();
            
            ps.setInt(1, 2);
            ps.setString(2, "another");
            ps.setNull(3, Types.LONGVARBINARY);
            ps.addBatch();

            ps.executeBatch();
            
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM batch_updates");
                int counter = 0;
                while(rs.next()) {
                    counter++;
                    int id = rs.getInt(1);
                    String value = rs.getString(2);
                    String clob = rs.getString(3);
                    assertTrue("Should contain correct ID", id == 1 || id == 2 || id == 3);
                    assertTrue("Should contain correct value",
                        id == 1 ? "test".equals(value) :
                                     id == 2 ? "another".equals(value) : 
                                     id == 3 ? "stream".equals(value) && "string".equals(clob) : false);
                }
                assertTrue("Should insert 3 rows.", counter == 3);
            } finally {
                stmt.close();
            }
            
        } finally {
            ps.close();
        }
    }
}
