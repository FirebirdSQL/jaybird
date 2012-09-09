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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static junit.framework.Assert.*;

/**
 * This method tests correctness of {@link FBResultSetMetaData} class.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBResultSetMetaData extends FBJUnit4TestBase {
    
    public static String CREATE_TABLE = 
        "CREATE TABLE test_rs_metadata (" + 
        "  id INTEGER NOT NULL PRIMARY KEY, " +
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
    
    public static final String TEST_QUERY2 = 
        "SELECT * from RDB$DATABASE";
        
    public static String DROP_TABLE = 
        "DROP TABLE test_rs_metadata";
    
    @Before
    public void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        
        DdlHelper.executeDropTable(connection, DROP_TABLE);
        DdlHelper.executeCreateTable(connection, CREATE_TABLE);
        
        JdbcResourceHelper.closeQuietly(connection);
    }

    @Test
    public void testResultSetMetaData() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY);
            ResultSetMetaData metaData = rs.getMetaData();
            
            assertEquals("simple_field must have size 60", 
                60, metaData.getPrecision(1));
                
            assertEquals("two_byte_field must have size 60", 
                60, metaData.getPrecision(2));
    
            assertEquals("three_byte_field must have size 60", 
                60, metaData.getPrecision(3));
    
            assertEquals("long_field must have precision 15", 
                15, metaData.getPrecision(4));
    
            assertEquals("int_field must have precision 8", 
                8, metaData.getPrecision(5));
    
            assertEquals("short_field must have precision 4", 
                4, metaData.getPrecision(6));
    
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(connection);
        }
    }
    
    @Test
    public void testResultSetMetaData2() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        
        try {
            FBDatabaseMetaData dmd = (FBDatabaseMetaData)connection.getMetaData();
            int firebirdVersion = dmd.getDatabaseMajorVersion();
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY2);
            ResultSetMetaData metaData = rs.getMetaData();
            		  
            int columnDisplaySize = metaData.getColumnDisplaySize(3);
            
            if (firebirdVersion == 1)
                assertEquals("RDB$SECURITY_CLASS must have display size 10 ", 10, columnDisplaySize);
            else
            if (firebirdVersion >= 2)
                assertEquals("RDB$SECURITY_CLASS must have display size 31 ", 31, columnDisplaySize);
            else
                fail("Unknown Firebird version, not clear what to compare.");
            
            int columnDisplaySize2 = metaData.getColumnDisplaySize(4);
            
            if (firebirdVersion == 1)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 10 ", 10, columnDisplaySize2);
            else
            if (firebirdVersion >= 2)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 31 ", 31, columnDisplaySize2);
            else
                fail("Unknown Firebird version, not clear what to compare.");
            
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(connection);
        }
    }
    
    @Test
	public void testColumnTypeName() throws Exception {
		Properties props = new Properties();
		props.putAll(getDefaultPropertiesForConnection());
		props.put("lc_ctype", "UNICODE_FSS");

		Connection connection = DriverManager.getConnection(getUrl(), props);
		try {
    		Statement stmt = connection.createStatement();
    		ResultSet rs = stmt.executeQuery(TEST_QUERY);
    		ResultSetMetaData metaData = rs.getMetaData();
    
    		assertEquals("simple_field must be of type VARCHAR",
    				"VARCHAR", metaData.getColumnTypeName(1));
    
    		assertEquals("two_byte_field must be of type VARCHAR",
    				"VARCHAR", metaData.getColumnTypeName(2));
    
    		assertEquals("three_byte_field must be of type VARCHAR",
    				"VARCHAR", metaData.getColumnTypeName(3));
    
    		assertEquals("long_field must be of type NUMERIC",
    				"NUMERIC", metaData.getColumnTypeName(4));
    
    		assertEquals("int_field must be of type NUMERIC",
    				"NUMERIC", metaData.getColumnTypeName(5));
    
    		assertEquals("short_field must be of type NUMERIC",
    				"NUMERIC", metaData.getColumnTypeName(6));
    
    		stmt.close();
		} finally {
		    JdbcResourceHelper.closeQuietly(connection);
		}
	}
}
