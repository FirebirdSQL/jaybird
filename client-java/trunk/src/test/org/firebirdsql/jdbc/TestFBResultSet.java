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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

import junit.textui.TestRunner;

public class TestFBResultSet extends FBTestBase {
    
    public static final String SELECT_STATEMENT = ""
        + "SELECT "
        + "  1 AS col1,"
        + "  2 AS \"col1\","
        + "  3 AS \"Col1\""
        + "FROM rdb$database"
        ;
        
    public static final String CREATE_TABLE_STATEMENT = ""
        + "CREATE TABLE test_table(" 
        + "  id INTEGER NOT NULL PRIMARY KEY, " 
        + "  str VARCHAR(10), " 
        + "  long_str VARCHAR(255), "
        + "  very_long_str VARCHAR(20000), "
        + "  blob_str BLOB SUB_TYPE 1"
        + ")"
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

    public static final String CREATE_SUBSTR_FUNCTION = ""
        + "DECLARE EXTERNAL FUNCTION substr " 
        + "  CSTRING(80), SMALLINT, SMALLINT "
        + "RETURNS CSTRING(80) FREE_IT " 
        + "ENTRY_POINT 'IB_UDF_substr' MODULE_NAME 'ib_udf'"
        ;
    
    public static final String DROP_SUBSTR_FUNCTION = ""
        + "DROP EXTERNAL FUNCTION substr"
        ;
    
    public static final String SELECT_FROM_VIEW_STATEMENT = ""
        + "SELECT * FROM test_empty_string_view"
        ;
    
    public static final String INSERT_INTO_TABLE_STATEMENT = ""
        + "INSERT INTO test_table (id, str) VALUES(?, ?)"
        ;
    
    public static final String INSERT_LONG_STR_STATEMENT = ""
        + "INSERT INTO test_table (id, long_str) VALUES(?, ?)"
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
        
        connection = this.getConnectionViaDriverManager();
            
        Statement stmt = connection.createStatement();
        
        try {
            try {
                stmt.execute(DROP_VIEW_STATEMENT);
            } catch (SQLException ex) {
                // do nothing here
            }
            
            try {
                stmt.execute(DROP_TABLE_STATEMENT);
            } catch (SQLException ex) {
                // do nothing here
            }
            
            try {
                stmt.execute(DROP_SUBSTR_FUNCTION);
            } catch(SQLException ex) {
                // do nothing here
            }
            
            stmt.execute(CREATE_TABLE_STATEMENT);
            stmt.execute(CREATE_VIEW_STATEMENT);
            stmt.execute(CREATE_SUBSTR_FUNCTION);
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
        
        ResultSet rs;
        connection.setAutoCommit(false);
        
        Statement select = connection.createStatement();
        select.setCursorName(CURSOR_NAME);
        try {
            rs = select.executeQuery(
                "SELECT id, str FROM test_table FOR UPDATE OF " + CURSOR_NAME);
                
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
                    
                    assertTrue("ResultSet.next() should return false.",
                        !rs.next());
                    
                } finally {
                    update.close();
                }
                
        } finally {
            select.close();
        }

        connection.commit();
        

        /*
         
        // Commented out by R.Rokytskyy. We no longer throw exception
        // when ResultSet.close() is called twice.
        try {
            rs.close();
            assertTrue(
                "Result set should be closed after statemnet close", false);
        } catch(SQLException ex) {
            // everything is ok
        }
        */
        
        connection.setAutoCommit(false);
        
        select = connection.createStatement();
        try {
            rs = select.executeQuery("SELECT id, str FROM test_table");
            
            int counter = 0;
            
            assertTrue("ResultSet.isBeforeFirst() should be true",
                    rs.isBeforeFirst());
            
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
            
            assertTrue("ResultSet.isAfterLast() should be true",
                    rs.isAfterLast());
            
            assertTrue("ResultSet.next() should return false.",
                    !rs.next());
            
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            select.close();
            connection.commit();
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

    /**
     * Test cursor scrolling in case of ResultSet.TEST_SCROLL_INSENSITIVE.
     * 
     * @throws Exception if something went wrong.
     */
    public void testScrollInsensitive() throws Exception {
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
        
        Statement stmt = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_READ_ONLY);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table");
            
            int testValue;
            
            rs.last();
            testValue = recordCount - 1;
            assertTrue("ID of last record should be equal to " + testValue,
                    rs.getInt(1) == testValue );
            assertTrue("isLast() should return true", rs.isLast());
            
            rs.absolute(recordCount / 2);
            testValue = recordCount / 2 - 1;
            assertTrue("ID after absolute positioning should return " + testValue,
                    rs.getInt(1) == testValue);
            
            rs.absolute(-1);
            testValue = recordCount - 1;
            assertTrue("ID after absolute positioning with negative position " +
                    "should return " + testValue, rs.getInt(1) == testValue);
            
            rs.first();
            testValue = 0;
            assertTrue("ID after first() should return " + testValue,
                    rs.getInt(1) == testValue);
            assertTrue("isFirst() should report true", rs.isFirst());
            
            boolean hasRow = rs.previous();
            assertTrue("Should not point to the row", !hasRow);
            assertTrue("isBeforeFirst() should return true", rs.isBeforeFirst());
            
            rs.relative(5);
            rs.relative(-4);
            testValue = 0;
            assertTrue("ID after relative positioning should return " + testValue,
                    rs.getInt(1) == testValue);
            
            rs.beforeFirst();
            assertTrue("isBeforeFirst() should return true", rs.isBeforeFirst());
            try {
                rs.getInt(1);
                assertTrue("Should not be possibe to access column if cursor " +
                        "does not point to a row.", false);
            } catch(SQLException ex) {
                // everything is fine
            }
            
            rs.afterLast();
            assertTrue("isAfterLast() should return true", rs.isAfterLast());
            try {
                rs.getInt(1);
                assertTrue("Should not be possibe to access column if cursor " +
                        "does not point to a row.", false);
            } catch(SQLException ex) {
                // everything is fine
            }
            assertTrue("ResultSet.next() should return false.", !rs.next());
            
        } finally {
            stmt.close();
        }
    }
    
    /**
     * This test case tries to reproduce a NPE reported in Firebird-Java group
     * by vmdd_tech after JayBird 1.5 beta 1 release.
     *  
     * @throws Exception if something goes wrong.
     */
    public void testBugReport1() throws Exception {
        PreparedStatement insertStmt = 
            connection.prepareStatement(INSERT_LONG_STR_STATEMENT);
        try {
            insertStmt.setInt(1, 1);
            insertStmt.setString(2, "aaa");
            
            insertStmt.execute();
            
            insertStmt.setInt(1, 2);
            insertStmt.setString(2, "'more than 80 chars are in " +
                    "hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                    "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            
            insertStmt.execute();
            
            insertStmt.setInt(1, 3);
            insertStmt.setString(2, "more than 80 chars are in " +
                    "hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                    "eeeeeeeeeeeeeeeeee");
            
            insertStmt.execute();
        } finally {
            insertStmt.close();
        }
        
        Statement stmt = connection.createStatement();
        try {
            String query = "SELECT id, substr(long_str,1,2) FROM test_table ORDER BY id DESC";
            ResultSet rs;
            
            try {
                rs = stmt.executeQuery(query);
                assertTrue("Should have at least one row", rs.next());
                rs.close();
            } catch(SQLException ex) {
                // it is ok as well, since substr is declared as CSTRING(80)
                // and truncation error happens
                System.out.println("First query generated exception" + ex.getMessage());
            }

            try  {
                rs = stmt.executeQuery(query);
                assertTrue("Should have at least one row", rs.next());
                
                Object tempObj = rs.getObject(1);
                
                rs.close();
            } catch(SQLException ex) {
                // it is ok as well, since substr is declared as CSTRING(80)
                // and truncation error happens
                System.out.println("Second query generated exception" + ex.getMessage());
            }
            
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Test if result set type and concurrency is correct.
     * 
     * @throws Exception if something went wrong.
     */
    public void testBugReport2() throws Exception {
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
        
        Statement stmt = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_READ_ONLY);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table");
            
            assertTrue("Should have at least one row", rs.next());
            
            assertTrue("ResultSet type should be TYPE_SCROLL_INSENSITIVE",
                rs.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE);
            
            assertTrue("ResultSet concurrency should be CONCUR_READ_ONLY",
                rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY);
            
            rs.last();
            
            assertTrue("ResultSet type should not change.",
                rs.getType() == ResultSet.TYPE_SCROLL_INSENSITIVE);
            
        } finally {
            stmt.close();
        }
    }
    
    public void testBugReport3() throws Exception {
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
        
        connection.setAutoCommit(true);
        
        Statement stmt = connection.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, 
                ResultSet.CONCUR_READ_ONLY);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT id, str FROM test_table");
            
            try {
                rs.first();
                fail("first() should not work in TYPE_FORWARD_ONLY result sets");
            } catch(SQLException ex) {
                // should fail, everything is fine.
            }
            
            while(rs.next()) {
                // do nothing, just loop.
            }
            
            try {
                rs.first();
                fail("first() should not work in TYPE_FORWARD_ONLY result sets.");
            } catch(SQLException ex) {
                // everything is fine
            }
            
        } finally {
            stmt.close();
        }
    }
    
    public void _testMemoryGrowth() throws Exception {
        
        Properties props = getDefaultPropertiesForConnection();
        props.put("no_result_set_tracking", "");
        
        Class.forName(FBDriver.class.getName());
        Connection connection = DriverManager.getConnection(getUrl(), props);
        
        connection.setAutoCommit(false);
        
        System.out.println("Inserting...");
        int recordCount = 1;
        PreparedStatement ps = connection
                .prepareStatement("INSERT INTO test_table("
                        + "id, very_long_str) VALUES (?, ?)");

        try {

            Random rnd = new Random();
            byte[] string = new byte[19000];
            rnd.nextBytes(string);

            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, new String(string));
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }

        connection.commit();

        System.gc();

        long memoryBeforeSelects = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        System.out.println("Selecting...");
        int selectRuns = 10000;
        for (int i = 0; i < selectRuns; i++) {
            if ((i % 1000) == 0) System.out.println("Select no. " + i);

            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
                while (rs.next()) {
                    // just loop through result set
                }

            } finally {
                stmt.close();
            }
        }
        System.gc();

        long memoryAfterSelects = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        connection.commit();

        System.gc();

        long memoryAfterCommit = Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory();

        System.out.println("Memory before selects " + memoryBeforeSelects);
        System.out.println("Memory after selects " + memoryAfterSelects);
        System.out.println("Memory after commit " + memoryAfterCommit);
        System.out.println("Commit freed "
                + (memoryAfterSelects - memoryAfterCommit));
        
    }

    public void testResultSetNotClosed() throws Exception {
        

        //System.setProperty("test.gds_type", "NATIVE");
        
        connection.setAutoCommit(false);
        
        int recordCount = 1;
        PreparedStatement ps = connection
                .prepareStatement("INSERT INTO test_table("
                        + "id, very_long_str) VALUES (?, ?)");

        try {

            Random rnd = new Random();
            byte[] string = new byte[19000];
            rnd.nextBytes(string);

            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, new String(string));
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }

        connection.commit();

        connection.setAutoCommit(false);
        
        PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM test_table WHERE id = ?");
        try {
            stmt.setInt(1, recordCount + 10);
            
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should not find any record", !rs.next());
            rs.close();
            
            stmt.setInt(1, recordCount - 1);
            rs = stmt.executeQuery();

            assertTrue("Should find a record", rs.next());
            rs.close();
            
        } finally {
            stmt.close();
        }
    }
    
    public void testUpdatableResultSet() throws Exception {
        connection.setAutoCommit(false);
        
        int recordCount = 10;
        PreparedStatement ps = connection
                .prepareStatement("INSERT INTO test_table("
                        + "id, long_str) VALUES (?, ?)");

        try {

            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }

        connection.commit();

        connection.setAutoCommit(true);
        
        connection.clearWarnings();
        Statement stmt = connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, 
            ResultSet.CONCUR_UPDATABLE);
        
        try {
            assertTrue("No warnings should be added", connection.getWarnings() == null);
            
            ResultSet rs = stmt.executeQuery("SELECT id, long_str, str FROM test_table ORDER BY id");

            int counter = 0;
            while(rs.next()) {
                
                int id = rs.getInt(1);
                assertEquals(counter, id);
                
                String longStr = rs.getString(2);
                assertEquals("oldString" + counter, longStr);
                
                rs.updateString(2, "newString" + counter);
                
                assertEquals(counter, rs.getInt(1)); 
                assertEquals("newString" + counter, rs.getString(2));

                assertEquals(null, rs.getString(3));
                rs.updateString(3, "str" + counter);
                
                // check whether row can be updated
                rs.updateRow();
                
                // check whether row can be refreshed
                rs.refreshRow();

                assertEquals(counter, rs.getInt(1)); 
                assertEquals("newString" + counter, rs.getString(2));
                assertEquals("str" + counter, rs.getString(3));
                
                counter++;
            }
            
            assertTrue("Should process " + recordCount + " rows.", counter == recordCount);

            // check the insertRow() feature
            rs.moveToInsertRow();
            rs.updateInt(1, recordCount);
            rs.updateString(2, "newString" + recordCount);
            rs.updateString(3, "bug");
            rs.insertRow();
            rs.moveToCurrentRow();

            // check whether newly inserted row can be updated
            rs.last();
            rs.updateString(3, "str" + recordCount);
            rs.updateRow();
            
            rs = stmt.executeQuery("SELECT id, long_str, str FROM test_table ORDER BY id");
            
            counter = 0;
            while(rs.next()) {
                int id = rs.getInt(1);
                assertTrue(id == counter);
                
                String longStr = rs.getString(2);
                assertTrue(("newString" + counter).equals(longStr));
                assertTrue(("str" + counter).equals(rs.getString(3)));
                counter++;
                
                if (counter == recordCount + 1)
                    rs.deleteRow();
            }
            
            assertTrue(counter == recordCount + 1);
            
            rs = stmt.executeQuery("SELECT count(*) FROM test_table");
            
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) == recordCount);
            
        } finally {
            stmt.close();
        }
        
    }

    public void testGetExecutionPlan() throws Exception {
        Statement stmt = connection.createStatement();
        try {
            FBResultSet rs = (FBResultSet)stmt.executeQuery(
                    "SELECT id, str FROM test_table");
            String execPlan = rs.getExecutionPlan();
            assertTrue("Execution plan should reference test_table",
                    execPlan.toUpperCase().indexOf("TEST_TABLE") >= 0);
        } finally {
            stmt.close();
        }

        PreparedStatement pStmt = 
            connection.prepareStatement("SELECT * FROM TEST_TABLE");
        try {
            FBResultSet rs = (FBResultSet)pStmt.executeQuery();
            String execPlan = rs.getExecutionPlan();
            assertTrue("Execution plan should reference test_table",
                    execPlan.toUpperCase().indexOf("TEST_TABLE") >= 0);
        } finally {
            pStmt.close();
        }
        
        // Ensure there isn't a crash when attempting to retrieve the
        // execution plan from a non-statement-based ResultSet
        java.sql.DatabaseMetaData metaData = connection.getMetaData();
        FBResultSet rs = (FBResultSet)metaData.getSchemas();
        assertEquals("Non-statement-based result set has no execution plan",
                "", rs.getExecutionPlan());
    }
    
    public void testHoldability() throws Exception {
        ((FirebirdConnection)connection).setHoldability(FirebirdResultSet.HOLD_CURSORS_OVER_COMMIT);
        
        try {
            Statement stmt = connection.createStatement(
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            
            fail("Holdable cursor is not compatible with forward-only result set.");
        } catch(FBDriverNotCapableException ex) {
            // everything is ok
        }
        
        Statement stmt = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_READ_ONLY);
        
        try {
            // execute first query
            ResultSet rs = stmt.executeQuery("SELECT * FROM rdb$database");
            
            // now execute another query, causes commit in auto-commit mode
            stmt.executeQuery("SELECT * FROM rdb$database");
            
            // now let's access the result set
            while(rs.next()) {
                String str1 = rs.getString(1);
            }
        } finally {
            stmt.close();
        }
    }

    public void testFetchSize() throws Exception {
        final int FETCH_SIZE = 3;
        Statement stmt = connection.createStatement();
        try {
            int fetchSize = stmt.getFetchSize();
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            assertEquals(
                "Default stmt fetch size must match ResultSet fetch size",
                fetchSize, rs.getFetchSize());

            stmt.setFetchSize(FETCH_SIZE);
            rs = stmt.executeQuery("SELECT * FROM test_table");
            assertEquals("ResultSet fetchsize must match Statement fetchSize",
                    FETCH_SIZE, rs.getFetchSize());

        } finally {
            stmt.close();
        }
    }

    public void testInsertUpdatableCursor() throws Exception {
        
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            // rs.next();
            rs.moveToInsertRow();
            rs.updateInt("id", 1);
            rs.updateString("blob_str", "test");
            try {
                rs.updateRow();
                fail("Should fail, since updateRow() is used to update rows.");
            } catch(SQLException ex) {
                // ok, let's try to insert row
                rs.insertRow();
            }
            
            rs.close();

            rs = stmt.executeQuery("SELECT * FROM test_table");
            assertTrue("Should have at least one row", rs.next());
            assertTrue("First record should have ID=1", rs.getInt("id") == 1);
            assertTrue("BLOB should be also saved", "test".equals(rs.getString("blob_str")));
            assertTrue("Should have only one row.", !rs.next());
            
        } finally {
            stmt.close();
        }
    }
    
    public void testMetaDataQueryShouldKeepRsOpen() throws Exception {
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            
            try {
                
                ResultSet bestRowId = connection.getMetaData().getBestRowIdentifier(
                    null, null, "test_table", 1, false);
                assertTrue("Should have row ID", bestRowId.next());
                bestRowId.close();
                
                rs.next();
            } catch(SQLException ex) {
                fail("Should throw no exception that result set is closed.");
            }
        } finally {
            stmt.close();
        }
    }
    
    public void testUpdatableResultSetMutipleStatements() throws Exception {
        
        int recordCount = 10;
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_table("
            + "id, long_str) VALUES (?, ?)");

        try {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }
        
        connection.setAutoCommit(true);
        Statement stmt = connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, 
            ResultSet.CONCUR_UPDATABLE);
        
        try {
            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM test_table");
            
            rs.first();
            
            PreparedStatement anotherStmt = 
                stmt.getConnection().prepareStatement(
                    "SELECT * FROM rdb$database");
            try {
                ResultSet anotherRs = anotherStmt.executeQuery();
                while (anotherRs.next()) {
                    Object tempObj = anotherRs.getObject(1);
                }
                anotherRs.close();
  
                try {
                    rs.updateInt("id", 1);
                    rs.updateString("blob_str", "test");
                    rs.updateNull("str");
                    rs.updateRow();
                    
                    fail("Should produce exception.");
                    
                } catch(SQLException ex) {
                    // everything is ok
                }

            } finally {
                anotherStmt.close();
            }
            
            rs.close();

        } finally {
            stmt.close();
        }
        connection.setAutoCommit(true);
    }
    
    public void testUpdatableHoldableResultSet() throws Exception {
        
        connection.setAutoCommit(true);
        
        int recordCount = 10;
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO test_table("
            + "id, long_str) VALUES (?, ?)");

        try {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }
        
        connection.setAutoCommit(false);
        
        Statement stmt = connection.createStatement(
            ResultSet.TYPE_SCROLL_INSENSITIVE, 
            ResultSet.CONCUR_UPDATABLE,
            ResultSet.HOLD_CURSORS_OVER_COMMIT);
        
        try {
            ResultSet rs = stmt.executeQuery(
                "SELECT id, long_str FROM test_table");

            while(rs.next()) {
                rs.updateString(2, rs.getString(2) + "a");
                rs.updateRow();
                connection.commit();
            }
            
            int counter = 0;
            
            rs = stmt.executeQuery("SELECT id, long_str FROM test_table");
            while(rs.next()) {
                assertEquals("oldString" + counter + "a", rs.getString(2));
                counter++;
            }
            
        } finally {
            stmt.close();
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(new TestFBResultSet("testMemoryGrowth"));
    }

}
