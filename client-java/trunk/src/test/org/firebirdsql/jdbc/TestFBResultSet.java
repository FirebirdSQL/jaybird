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
import java.sql.SQLException;
import java.sql.Statement;

public class TestFBResultSet extends BaseFBTest {
    
    public static final String SELECT_STATEMENT = ""
        + "SELECT "
        + "  1 AS col1,"
        + "  2 AS \"col1\","
        + "  3 AS \"Col1\""
        + "FROM rdb$database"
        ;
        
    public static final String CREATE_TABLE_STATEMENT = ""
        + "CREATE TABLE test_empty_string_bug(id INTEGER)"
        ;
        
    public static final String DROP_TABLE_STATEMENT = ""
        + "DROP TABLE test_empty_string_bug"
        ;
        
    public static final String CREATE_VIEW_STATEMENT = ""
        + "CREATE VIEW test_empty_string_view(marker, id, empty_char) "
        + "  AS  "
        + "  SELECT "
        + "    CAST('marker' AS VARCHAR(6)), "
        + "    id, "
        + "    '' "
        + "  FROM "
        + "    test_empty_string_bug"
        ;
        
    public static final String DROP_VIEW_STATEMENT = ""
        + "DROP VIEW test_empty_string_view"
        ;
        
    public static final String SELECT_FROM_VIEW_STATEMENT = ""
        + "SELECT * FROM test_empty_string_view"
        ;
    
    public static final String INSERT_INTO_TABLE_STATEMENT = ""
        + "INSERT INTO test_empty_string_bug VALUES(?)"
        ;

    public TestFBResultSet(String name) {
        super(name);
    }

    private Connection connection;

    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        connection = DriverManager.getConnection(
            DB_DRIVER_URL, DB_INFO);
            
        Statement stmt = connection.createStatement();
        
        try {
            try {
                stmt.executeUpdate(DROP_VIEW_STATEMENT);
            } catch (SQLException ex) {
                // do nothing here
            }
            
            try {
                stmt.executeUpdate(DROP_TABLE_STATEMENT);
            } catch (SQLException ex) {
                // do nothing here
            }
            
            stmt.executeUpdate(CREATE_TABLE_STATEMENT);
            stmt.executeUpdate(CREATE_VIEW_STATEMENT);
        } finally {
            stmt.close();
        }
    }

    protected void tearDown() throws Exception {
        
        connection.close();
        
        super.tearDown();
    }
    
    /**
     * Test if all columns are found correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testFindColumn() throws Exception {
        Statement stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery(SELECT_STATEMENT);
        
        assertTrue("Should have at least one row.", rs.next());
        
        assertTrue("COL1 should be 1.", rs.getInt("COL1") == 1);
        assertTrue("col1 should be 1.", rs.getInt("col1") == 1);
        assertTrue("\"col1\" should be 2.", rs.getInt("\"col1\"") == 2);
        assertTrue("Col1 should be 1.", rs.getInt("Col1") == 1);
        
        stmt.close();
    }
    
    /**
     * This test checks if an empty column in a view is correctly returned
     * to the client.
     * 
     * @throws Exception if something went wrong.
     */
    public void testEmptyColumnInView() throws Exception {
        PreparedStatement ps = 
            connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT);
            
        try {
            for(int i = 0; i < 10; i++) {
                ps.setInt(1, i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }
        
        
        connection.setAutoCommit(false);

        Statement stmt = connection.createStatement();
        
        try {
            
            ResultSet rs = stmt.executeQuery(SELECT_FROM_VIEW_STATEMENT);
            
            int counter = 0;
            while(rs.next()) {
                String marker = rs.getString(1);
                int key = rs.getInt(2);
                String value = rs.getString(3);
                
                assertTrue("Marker should be correct.", "marker".equals(marker));
                assertTrue("Key should be same as counter.", key == counter);
                assertTrue("EMPTY_CHAR string should be empty.", "".equals(value));
                
                counter++;
            }
            
            assertTrue("Should read 10 records", counter == 10);
            
        }finally {
            stmt.close();
        }
        
        connection.setAutoCommit(true);
    }

}
