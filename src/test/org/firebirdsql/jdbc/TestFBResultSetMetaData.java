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

/**
 * This method tests correctness of {@link FBResultSetMetaData} class.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBResultSetMetaData extends BaseFBTest {
    
    public static String CREATE_TABLE = 
        "CREATE TABLE test_rs_metadata (" + 
        "  id INTEGER, " +
        "  simple_field VARCHAR(60) CHARACTER SET WIN1250, " +
        "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
        "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
        "  long_field NUMERIC(15,2), " +
        "  int_field NUMERIC(8, 2), " +
        "  short_field NUMERIC(4, 2) " +
        ")";
        
    public static final String TEST_QUERY = 
        "SELECT " + 
        "simple_field, two_byte_field, three_byte_field, " + 
        "long_field, int_field, short_field " + 
        "FROM test_rs_metadata";
    
        
    public static String DROP_TABLE = 
        "DROP TABLE test_rs_metadata";
    
    public TestFBResultSetMetaData(String testName) {
        super(testName);
    }
    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
        
        java.sql.Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {}

        stmt.executeUpdate(CREATE_TABLE);
        stmt.close();        
        
        connection.close();
    }

    protected void tearDown() throws Exception {
        /*
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "NONE");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
            
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();      
        */
        
        super.tearDown();
    }
    
    public void testResultSetMetaData() throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
        
        Statement stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery(TEST_QUERY);
        
        ResultSetMetaData metaData = rs.getMetaData();
        
        assertTrue("simple_field must have size 60", 
            metaData.getPrecision(1) == 60);
            
        assertTrue("two_byte_field must have size 60", 
            metaData.getPrecision(2) == 60);

        assertTrue("three_byte_field must have size 60", 
            metaData.getPrecision(3) == 60);

        assertTrue("long_field must have precision 15", 
            metaData.getPrecision(4) == 15);

        assertTrue("int_field must have precision 8", 
            metaData.getPrecision(5) == 8);

        assertTrue("long_field must have precision 4", 
            metaData.getPrecision(6) == 4);
            
        stmt.close();
        connection.close();
    }
}
