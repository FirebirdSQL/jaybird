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

import junit.framework.TestCase;
import java.sql.*;

public class TestFBResultSet extends BaseFBTest {
    
    public static final String SELECT_STATEMENT = ""
        + "SELECT "
        + "  1 AS col1,"
        + "  2 AS \"col1\","
        + "  3 AS \"Col1\""
        + "FROM rdb$database"
        ;
        
    public static final String CREATE_TABLE_STATEMENT = ""
        + "CREATE TABLE test_table(id INTEGER, str VARCHAR(10))"
        ;
        
    public static final String DROP_TABLE_STATEMENT = ""
        + "DROP TABLE test_table"
        ;
        
    public static final String CREATE_VIEW_STATEMENT = ""
        + "CREATE VIEW test_empty_string_view(marker, id, empty_char) "
        + "  AS  "
        + "  SELECT "
        + "    CAST('marker' AS VARCHAR(6)), "
        + "    id, "
        + "    '' "
        + "  FROM "
        + "    test_table"
        ;
        
    public static final String DROP_VIEW_STATEMENT = ""
        + "DROP VIEW test_empty_string_view"
        ;
        
    public static final String SELECT_FROM_VIEW_STATEMENT = ""
        + "SELECT * FROM test_empty_string_view"
        ;
    
    public static final String INSERT_INTO_TABLE_STATEMENT = ""
        + "INSERT INTO test_table VALUES(?, ?)"
        ;
        
    public static final String CURSOR_NAME = "some_cursor";
        
    public static final String UPDATE_TABLE_STATEMENT = ""
        + "UPDATE test_table SET str = ? WHERE CURRENT OF " + CURSOR_NAME
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
     * Test if positioned updates work correctly.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void testPositionedUpdate() throws Exception {
        int recordCount = 10;
        
        PreparedStatement ps = 
            connection.prepareStatement(INSERT_INTO_TABLE_STATEMENT);

        try {
            for(int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setInt(2, i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }
        
        connection.setAutoCommit(false);
        
        ResultSet rs;
        Statement select = connection.createStatement();
        select.setCursorName(CURSOR_NAME);
        try {
            rs = select.executeQuery(
                "SELECT id, str FROM test_table FOR UPDATE OF " + CURSOR_NAME);
                
            assertTrue("Cursor name should be correct", 
                CURSOR_NAME.equals(rs.getCursorName()));
                
            assertTrue("ResultSet.isBeforeFirst() should be true.", 
                rs.isBeforeFirst());

                PreparedStatement update = connection.prepareStatement(
                    UPDATE_TABLE_STATEMENT);

                try {
                    int counter = 0;
                    
                    while (rs.next()) {
                        
                        if (counter == 0) {
                            assertTrue("ResultSet.isFirst() should be true", 
                                rs.isFirst());
                        } else
                        if (counter == recordCount - 1) {
                            try {
                                rs.isLast();
                                assertTrue("ResultSet.isLast() should be true", 
                                    false);
                            } catch(SQLException ex) {
                                // correct
                            }
                        }

                        counter++;

                        assertTrue("ResultSet.getRow() should be correct", 
                            rs.getRow() == counter);
                        
                        update.setInt(1, rs.getInt(1) + 1);
                        int updatedCount = update.executeUpdate();
                        
                        assertTrue("Number of update rows should be 1, is " + updatedCount, 
                            updatedCount == 1);
                    }
                    
                    assertTrue("ResultSet.isAfterLast() should be true", 
                        rs.isAfterLast());
                    
                } finally {
                    update.close();
                }
                
        } finally {
            select.close();
        }
        
        try {
            rs.close();
            assertTrue(
                "Result set should be closed after statemnet close", false);
        } catch(SQLException ex) {
            // everything is ok
        }

        connection.commit();
        
        select = connection.createStatement();
        try {
            rs = select.executeQuery("SELECT id, str FROM test_table");
            
            int counter = 0;
            
            while (rs.next()) {
                
                if (counter == 0) {
                    assertTrue("ResultSet.isFirst() should be true", 
                        rs.isFirst());
                } else
                if (counter == recordCount - 1) {
                    assertTrue("ResultSet.isLast() should be true", 
                        rs.isLast());
                }

                counter++;

                int idValue = rs.getInt(1);
                int strValue = rs.getInt(2);

                assertTrue("Value of str column must be equal to id + 1, " +
                    "idValue = " + idValue + ", strValue = " + strValue,
                    strValue == (idValue + 1));
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            select.close();
        }
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
                ps.setString(2, "");
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