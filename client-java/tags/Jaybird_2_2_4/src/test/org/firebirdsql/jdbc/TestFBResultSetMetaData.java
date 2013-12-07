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

import com.sun.rowset.CachedRowSetImpl;

import java.sql.*;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

/**
 * This method tests correctness of {@link FBResultSetMetaData} class.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBResultSetMetaData extends FBTestBase {
    
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
    
    public TestFBResultSetMetaData(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        Properties props = new Properties();
        props.putAll(this.getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(this.getUrl(), props);
        
        Statement stmt = connection.createStatement();
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
        Properties props = new Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "NONE");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
            
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();      
        */
        
        super.tearDown();
    }
    
    public void testResultSetMetaData() throws Exception {
        Properties props = new Properties();
        props.putAll(this.getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(this.getUrl(), props);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY);

            ResultSetMetaData metaData = rs.getMetaData();

            assertEquals("simple_field must have size 60", 60, metaData.getPrecision(1));
            assertEquals("two_byte_field must have size 60", 60, metaData.getPrecision(2));
            assertEquals("three_byte_field must have size 60", 60, metaData.getPrecision(3));
            assertEquals("long_field must have precision 15", 15, metaData.getPrecision(4));
            assertEquals("int_field must have precision 8", 8, metaData.getPrecision(5));
            assertEquals("short_field must have precision 4", 4, metaData.getPrecision(6));

            stmt.close();
        } finally {
            closeQuietly(connection);
        }
    }
    
    public void testResultSetMetaData2() throws Exception {
        Properties props = new Properties();
        props.putAll(this.getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(this.getUrl(), props);
        try {
            FBDatabaseMetaData dmd = (FBDatabaseMetaData)connection.getMetaData();
            int firebirdVersion = dmd.getDatabaseMajorVersion();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY2);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnDisplaySize = metaData.getColumnDisplaySize(3);

            if (firebirdVersion == 1)
                assertEquals("RDB$SECURITY_CLASS must have display size 10 ", 10, columnDisplaySize);
            else if (firebirdVersion >= 2)
                assertEquals("RDB$SECURITY_CLASS must have display size 31 ", 31, columnDisplaySize);
            else
                fail(String.format("Unknown Firebird version (%d), not clear what to compare, display size was %d.", firebirdVersion, columnDisplaySize));

            int columnDisplaySize2 = metaData.getColumnDisplaySize(4);

            if (firebirdVersion == 1)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 10 ", 10, columnDisplaySize2);
            else if (firebirdVersion >= 2)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 31 ", 31,columnDisplaySize2);
            else
                fail(String.format("Unknown Firebird version (%d), not clear what to compare, display size was %d.", firebirdVersion, columnDisplaySize));
        
            stmt.close();
        } finally {
            closeQuietly(connection);
        }
    }
    
	public void testColumnTypeName() throws Exception {
		Properties props = new Properties();
		props.putAll(this.getDefaultPropertiesForConnection());
		props.put("lc_ctype", "UNICODE_FSS");

		Connection connection = DriverManager.getConnection(this.getUrl(), props);
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY);

            ResultSetMetaData metaData = rs.getMetaData();

            assertEquals("simple_field must be of type VARCHAR", "VARCHAR", metaData.getColumnTypeName(1));
            assertEquals("two_byte_field must be of type VARCHAR", "VARCHAR", metaData.getColumnTypeName(2));
            assertEquals("three_byte_field must be of type VARCHAR", "VARCHAR", metaData.getColumnTypeName(3));
            assertEquals("long_field must be of type NUMERIC", "NUMERIC", metaData.getColumnTypeName(4));
            assertEquals("int_field must be of type NUMERIC", "NUMERIC", metaData.getColumnTypeName(5));
            assertEquals("short_field must be of type NUMERIC", "NUMERIC", metaData.getColumnTypeName(6));

            stmt.close();
        } finally {
            closeQuietly(connection);
        }
	}
	
    /**
     * Tests the default strategy for retrieving columnNames and columnLabels, where the columnName is the original column name
     * and the label is the AS clause (if specified), or the original column name otherwise.
     * <p>
     * This is the JDBC-compliant behavior
     * </p>
     */
    public void columnNameAndLabel_Default() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + 
                "simple_field AS column1Alias, two_byte_field, 1 + 1, 2 - 2 AS column4Alias " +
                "FROM test_rs_metadata");
            ResultSetMetaData metaData = rs.getMetaData();
            
            assertEquals("Column 1, unexpected columnName", "SIMPLE_FIELD", metaData.getColumnName(1));
            assertEquals("Column 1, unexpected columnLabel", "COLUMN1ALIAS", metaData.getColumnLabel(1));
            
            assertEquals("Column 2, unexpected columnName", "TWO_BYTE_FIELD", metaData.getColumnName(2));
            assertEquals("Column 2, unexpected columnLabel", "TWO_BYTE_FIELD", metaData.getColumnLabel(2));

            // TODO: Investigate difference between column 3 and 4
            assertEquals("Column 3, unexpected columnName", "ADD", metaData.getColumnName(3));
            assertEquals("Column 3, unexpected columnLabel", "ADD", metaData.getColumnLabel(3));
            
            assertEquals("Column 4, unexpected columnName", "", metaData.getColumnName(4));
            assertEquals("Column 4, unexpected columnLabel", "COLUMN4ALIAS", metaData.getColumnLabel(4));
            
            stmt.close();
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Tests for columnLabelForName strategy of retrieving columnNames and columnLabels, 
     * where the columnName has the same value as the label.
     * <p>
     * This strategy is enabled by specifying the columnLabelForName property (value true)
     * in the connection properties
     * </p>
     */
    public void columnNameAndLabel_ColumnLabelForName() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("columnLabelForName", "true");
        
        Connection con = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + 
                "simple_field AS column1Alias, two_byte_field, 1 + 1, 2 - 2 AS column4Alias " +
                "FROM test_rs_metadata");
            ResultSetMetaData metaData = rs.getMetaData();
            
            assertEquals("Column 1, unexpected columnName", "COLUMN1ALIAS", metaData.getColumnName(1));
            assertEquals("Column 1, unexpected columnLabel", "COLUMN1ALIAS", metaData.getColumnLabel(1));
            
            assertEquals("Column 2, unexpected columnName", "TWO_BYTE_FIELD", metaData.getColumnName(2));
            assertEquals("Column 2, unexpected columnLabel", "TWO_BYTE_FIELD", metaData.getColumnLabel(2));

            assertEquals("Column 3, unexpected columnName", "ADD", metaData.getColumnName(3));
            assertEquals("Column 3, unexpected columnLabel", "ADD", metaData.getColumnLabel(3));
            
            assertEquals("Column 4, unexpected columnName", "COLUMN4ALIAS", metaData.getColumnName(4));
            assertEquals("Column 4, unexpected columnLabel", "COLUMN4ALIAS", metaData.getColumnLabel(4));
            
            stmt.close();
        } finally {
            closeQuietly(con);
        }
    }
    
    /**
     * Tests if the columnLabelForName strategy allows com.sun.rowset.CachedRowSetImpl to
     * access rows by their columnLabel.
     */
    public void cachedRowSetImpl_columnLabelForName() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("columnLabelForName", "true");
        
        Connection con = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, simple_field AS column2, int_field AS column3, 2 - 1 AS column4 FROM test_rs_metadata");
            CachedRowSet rowSet = new CachedRowSetImpl();
            rowSet.populate(rs);
            
            assertEquals(1, rowSet.findColumn("id"));
            assertEquals(2, rowSet.findColumn("column2"));
            assertEquals(3, rowSet.findColumn("column3"));
            assertEquals(4, rowSet.findColumn("column4"));
            
            try {
                rowSet.findColumn("simple_field");
                fail("Looking up column with original column name should fail with columnLabelForName strategy");
            } catch (SQLException ex) {
                // expected
            }
            stmt.close();
        } finally {
            closeQuietly(con);
        }
    }

}
