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

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Describe class <code>TestFBPreparedStatement</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBPreparedStatement extends FBTestBase{
    
    public static final String CREATE_GENERATOR = 
        "CREATE GENERATOR test_generator";
        
    public static final String DROP_GENERATOR = 
        "DROP GENERATOR test_generator";
    
    public static final String CREATE_TEST_BLOB_TABLE = 
        "CREATE TABLE test_blob (" + 
        "  ID INTEGER, " + 
        "  OBJ_DATA BLOB, " +
        "  TS_FIELD TIMESTAMP " +
        ")";
        
    public static final String CREATE_TEST_CHARS_TABLE = ""
        + "CREATE TABLE TESTTAB ("
        + "ID INTEGER, "
        + "FIELD1 VARCHAR(10) NOT NULL PRIMARY KEY,"
        + "FIELD2 VARCHAR(30),"
        + "FIELD3 VARCHAR(20),"
        + "FIELD4 FLOAT,"
        + "FIELD5 CHAR,"
        + "FIELD6 VARCHAR(5)"
        + ")"
        ;
    
    public static final String CREATE_UNRECOGNIZED_TR_TABLE = ""
        + "CREATE TABLE t1("
        + "  c1 CHAR(2) CHARACTER SET ASCII NOT NULL, "
        + "  c2 BLOB SUB_TYPE TEXT CHARACTER SET ASCII NOT NULL "
        + ")"
        ;
    
    public static final String ADD_CONSTRAINT_T1_C1 = ""
        + "ALTER TABLE t1 ADD CONSTRAINT t1_c1 PRIMARY KEY (c1)"
        ;
    
    public static final String INIT_T1 = ""
        + "INSERT INTO t1 VALUES ('XX', 'no more bugs')"
        ;
    
    public static final String DROP_TEST_BLOB_TABLE = 
        "DROP TABLE test_blob";
    
    public static final String DROP_TEST_CHARS_TABLE = ""
        + "DROP TABLE TESTTAB"
        ;
    
    public static final String DROP_UNRECOGNIZED_TR_TABLE = ""
        + "DROP TABLE t1"
        ;
        
    public static final String TEST_STRING = "This is simple test string.";
    
    public static final String ANOTHER_TEST_STRING = "Another test string.";
    
    public TestFBPreparedStatement(String testName) {
        super(testName);
    }

    Connection con;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        con = this.getConnectionViaDriverManager();

        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            stmt.executeUpdate(DROP_UNRECOGNIZED_TR_TABLE);
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            stmt.executeUpdate(DROP_TEST_CHARS_TABLE);
        } catch(Exception e) {
            // ignore
        }
        
        try {
            stmt.executeUpdate(DROP_GENERATOR);
        } catch(Exception ex) {
        }
        
        stmt.executeUpdate(CREATE_TEST_BLOB_TABLE);
        stmt.executeUpdate(CREATE_UNRECOGNIZED_TR_TABLE);
        stmt.executeUpdate(ADD_CONSTRAINT_T1_C1);
        stmt.executeUpdate(INIT_T1);
        stmt.executeUpdate(CREATE_TEST_CHARS_TABLE);
        
        stmt.executeUpdate(CREATE_GENERATOR);
        
        stmt.close(); 
        
    }

    protected void tearDown() throws Exception {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(DROP_TEST_BLOB_TABLE);
        stmt.executeUpdate(DROP_TEST_CHARS_TABLE);
        stmt.executeUpdate(DROP_UNRECOGNIZED_TR_TABLE);
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
            "INSERT INTO test_blob (id, obj_data) VALUES(?, NULL)");
        
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
    
    /**
     * Test case to reproduce problem with the connection when "operation was
     * cancelled" happens. Bug is fixed, however due to workaround for this
     * problem (@see org.firebirdsql.jdbc.field.FBWorkaroundStringField) this
     * test case is no longer relevant. In order to make it execute correctly
     * one has to remove this workaround.
     * 
     * @throws Exception if something went wrong.
     */
    public void _testOpCancelled() throws Exception {
        con.setAutoCommit( true );
        PreparedStatement prep = con.prepareStatement( 
                "INSERT INTO TESTTAB (FIELD1, FIELD3, FIELD4, FIELD5 ) " +
                "VALUES ( ?, ?, ?, ? )");

        try {
            for( int i = 0; i < 5; i++ ){
                try{
                    if( i == 0 ){
                        prep.setObject( 1, "0123456789" );
                        prep.setObject( 2, "01234567890123456789");
                        prep.setObject( 3, "1259.9" );
                        prep.setObject( 4, "A" );
                    }
                    if( i == 1 ){
                        prep.setObject( 1, "0123456787" );
                        prep.setObject( 2, "012345678901234567890");
                        prep.setObject( 3, "0.9" );
                        prep.setObject( 4, "B" );
                    }
                    if( i == 2 ){
                        prep.setObject( 1, "0123456788" );
                        prep.setObject( 2, "Fld3-Rec3");
                        prep.setObject( 3, "0.9" );
                        prep.setObject( 4, "B" );
                    }
                    if( i == 3 ){
                        prep.setObject( 1, "0123456780" );
                        prep.setObject( 2, "Fld3-Rec4");
                        prep.setObject( 3, "1299.5" );
                        prep.setObject( 4, "Q" );
                    }
                    if( i == 4 ){
                        prep.setObject( 1, "0123456779" );
                        prep.setObject( 2, "Fld3-Rec5");
                        prep.setObject( 3, "1844" );
                        prep.setObject( 4, "Z" );
                    }
                    prep.execute();
                } catch(SQLException x){
                    // x.printStackTrace();
                } 
            }
        } finally {
            prep.close();
        }
    }
    
    /**
     * Test if parameters are correctly checked for their length.
     * @throws Exception if something went wrong.
     */
    public void testLongParameter() throws Exception {
        con.setAutoCommit(true);
        Statement stmt = con.createStatement();
        try {
            stmt.execute("INSERT INTO testtab(id, field1, field6) VALUES(1, '', 'a')");
        } finally {
            stmt.close();
        }
        
        con.setAutoCommit(false);
        
        PreparedStatement ps = 
            con.prepareStatement("UPDATE testtab SET field6=? WHERE id = 1");
        try {
            try {
                ps.setString(1, "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                ps.execute();
                con.commit();
            } catch(SQLException ex) {
                con.rollback();
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
            
            fail("No exception should be thrown.");
        } finally {
            ps.close();
        }
    }
    
    /**
     * Test if batch execution works correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testBatch() throws Exception {
        Connection c = getConnectionViaDriverManager();
        try {
            Statement s = c.createStatement();
            s.executeUpdate("CREATE TABLE foo (" +
                    "bar varchar(64) NOT NULL, " +
                    "baz varchar(8) NOT NULL, " +
                    "CONSTRAINT pk_foo PRIMARY KEY (bar, baz))");
            PreparedStatement ps = c
                    .prepareStatement("Insert into foo values (?, ?)");
            ps.setString(1, "one");
            ps.setString(2, "two");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
            ps.setString(1, "one");
            ps.setString(2, "three");
            ps.addBatch();
            ps.executeBatch();
            ps.clearBatch();
        } finally {
            c.close();
        }
    }
    
    public void testTimestampWithCalendar() throws Exception {
        //Connection connection = getConnectionViaDriverManager();
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.setProperty("timestamp_uses_local_timezone", "");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO test_blob(id, ts_field) VALUES (?, ?)");
            
            try {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+01"));
                
                Timestamp ts = new Timestamp(calendar.getTime().getTime());
                
                stmt.setInt(1, 1);
                stmt.setTimestamp(2, ts);
                
                stmt.execute();
                
                stmt.setInt(1, 2);
                stmt.setTimestamp(2, ts, calendar);
                
                stmt.execute();

                stmt.setInt(1, 3);
                stmt.setTimestamp(2, ts, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                
                stmt.execute();
                
                
                Statement selectStmt = connection.createStatement();
                try {
                    ResultSet rs = selectStmt.executeQuery(
                        "SELECT id, CAST(ts_field AS VARCHAR(30)), ts_field FROM test_blob");
                    
                    Timestamp ts1 = null;
                    Timestamp ts2 = null;
                    Timestamp ts3 = null;
                    
                    String ts1Str = null;
                    String ts2Str = null;
                    String ts3Str = null;
                    
                    int maxLength = 23;
                    
                    // workaround for the bug in java.sql.Timestamp in JDK 1.3 
                    if ("1.3".equals(System.getProperty("java.specification.version")))
                        maxLength = 19;
                    
                    while(rs.next()) {
                        
                        switch(rs.getInt(1)) {
                            case 1 :
                                ts1 = rs.getTimestamp(3);
                                ts1Str = rs.getString(2).substring(0, maxLength);
                                break;
                                
                            case 2 :
                                ts2 = rs.getTimestamp(3);
                                ts2Str = rs.getString(2).substring(0, maxLength);
                                break;
                                
                            case 3 : 
                                ts3 = rs.getTimestamp(3);
                                ts3Str = rs.getString(2).substring(0, maxLength);
                                break;
                        }
                        /*
                        System.out.println("ID " + rs.getInt(1) + 
                            ", time_str '" + rs.getString(2) + 
                            "', time ts " + rs.getTimestamp(3) + 
                            ", time ts_cal " + rs.getTimestamp(3, Calendar.getInstance(TimeZone.getTimeZone("GMT+01"))) +
                            ", time ts_zone " + rs.getTimestamp(3, Calendar.getInstance(TimeZone.getTimeZone("UTC"))));
                        */
                    }
                    
                    assertTrue("Timestamps 1 and 2 should be equal", 
                        ts1.getTime() == ts2.getTime());
                    
                    assertTrue("Timestamps 1 and 3 should differ for 3600 seconds.",
                        Math.abs(ts1.getTime() - ts3.getTime()) == 3600*1000);
                    
                    assertTrue("Server should see the same timestamp",
                        ts1.toString().substring(0,  maxLength).equals(ts1Str));

                    assertTrue("Server should see the same timestamp",
                        ts2.toString().substring(0, maxLength).equals(ts2Str));

                    assertTrue("Server should see the same timestamp",
                        ts3.toString().substring(0, maxLength).equals(ts3Str));

                } finally {
                    selectStmt.close();
                }
                
            } finally {
                stmt.close();
            }
            
        } finally {
            connection.close();
        }
    }
    
    /**
     * Test if failure in setting the parameter leaves the driver in
     * correct state (i.e. "not all params were set").
     * @throws Exception
     */
    public void testBindParameter() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        
        try {
            connection.setAutoCommit(false);
            
            PreparedStatement ps = connection.prepareStatement("UPDATE testtab SET field1 = ? WHERE id = ?");
            try {
                try {
                    ps.setString(1, "veeeeeeeeeeeeeeeeeeeeery looooooooooooooooooooooong striiiiiiiiiiiiiiiiiiing");
                } catch(DataTruncation ex) {
                    // ignore
                }
                ps.setInt(2, 1);
                
                try {
                    ps.execute();
                } catch(FBMissingParameterException ex) {
                    // correct
                }
                
                Statement stmt = connection.createStatement();
                try {
                    stmt.execute("SELECT * FROM rdb$database");
                } catch(Throwable t) {
                    if (t instanceof SQLException) {
                        //ignore
                    } else
                        fail("Should not throw exception");
                }
            } finally {
                ps.close();
            }
            
        } finally {
            connection.close();
        }
    }
    
    public void _testUnrecognizedTransaction() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            String sql = "SELECT 1 FROM t1 WHERE c1 = ? AND c2 = ?";
            PreparedStatement ps;
            ResultSet rs;
            
            ps = connection.prepareStatement(sql);
            ps.setString(1, "XX");
            ps.setString(2, "bug busters");
            rs = ps.executeQuery();
            assertTrue("Should find something.", rs.next());
            
        } finally {
            connection.close();
        }
    }
}
