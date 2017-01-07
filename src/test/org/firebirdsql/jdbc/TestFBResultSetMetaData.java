/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import com.sun.rowset.CachedRowSetImpl;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Before;
import org.junit.Test;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This method tests correctness of {@link FBResultSetMetaData} class.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBResultSetMetaData extends FBJUnit4TestBase {

    //@formatter:off
    private static final String CREATE_TABLE =
        "CREATE TABLE test_rs_metadata (" + 
        "  id INTEGER NOT NULL PRIMARY KEY, " +
        "  simple_field VARCHAR(60) CHARACTER SET WIN1250, " +
        "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
        "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
        "  long_field NUMERIC(15,2), " +
        "  int_field NUMERIC(8, 2), " +
        "  short_field NUMERIC(4, 2), " +
        "  char_octets_field CHAR(10) CHARACTER SET OCTETS, " +
        "  varchar_octets_field VARCHAR(15) CHARACTER SET OCTETS, " +
        "  calculated_field computed by (int_field + short_field) " +
        ")";
        
    private static final String TEST_QUERY =
        "SELECT " + 
        "simple_field, two_byte_field, three_byte_field, " + 
        "long_field, int_field, short_field " + 
        "FROM test_rs_metadata";
    
    private static final String TEST_QUERY2 =
        "SELECT * from RDB$DATABASE";
    //@formatter:on

    @Before
    public void setUp() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(CREATE_TABLE);
            stmt.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testResultSetMetaData() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
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

    @Test
    public void testResultSetMetaData2() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            int firebirdVersion = dmd.getDatabaseMajorVersion();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(TEST_QUERY2);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnDisplaySize = metaData.getColumnDisplaySize(3);

            if (firebirdVersion == 1)
                assertEquals("RDB$SECURITY_CLASS must have display size 10 ", 10, columnDisplaySize);
            else if (firebirdVersion >= 2 && firebirdVersion < 4)
                assertEquals("RDB$SECURITY_CLASS must have display size 31 ", 31, columnDisplaySize);
            else if (firebirdVersion >= 4)
                assertEquals("RDB$SECURITY_CLASS must have display size 63 ", 63, columnDisplaySize);
            else
                fail(String.format("Unknown Firebird version (%d), not clear what to compare, display size was %d.", firebirdVersion, columnDisplaySize));

            int columnDisplaySize2 = metaData.getColumnDisplaySize(4);

            if (firebirdVersion == 1)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 10 ", 10, columnDisplaySize2);
            else if (firebirdVersion >= 2 && firebirdVersion < 4)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 31 ", 31, columnDisplaySize2);
            else if (firebirdVersion >= 4)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 63 ", 63, columnDisplaySize2);
            else
                fail(String.format("Unknown Firebird version (%d), not clear what to compare, display size was %d.", firebirdVersion, columnDisplaySize));
        
            stmt.close();
        } finally {
            closeQuietly(connection);
        }
    }

    @Test
	public void testColumnTypeName() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
		props.put("lc_ctype", "UNICODE_FSS");

		Connection connection = DriverManager.getConnection(getUrl(), props);
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
    @Test
    public void columnNameAndLabel_Default() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            DatabaseMetaData dmd = con.getMetaData();
            int firebirdVersion = dmd.getDatabaseMajorVersion();

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT " + 
                "simple_field AS column1Alias, two_byte_field, 1 + 1, 2 - 2 AS column4Alias " +
                "FROM test_rs_metadata");
            ResultSetMetaData metaData = rs.getMetaData();
            
            assertEquals("Column 1, unexpected columnName", "SIMPLE_FIELD", metaData.getColumnName(1));
            assertEquals("Column 1, unexpected columnLabel", "COLUMN1ALIAS", metaData.getColumnLabel(1));
            
            assertEquals("Column 2, unexpected columnName", "TWO_BYTE_FIELD", metaData.getColumnName(2));
            assertEquals("Column 2, unexpected columnLabel", "TWO_BYTE_FIELD", metaData.getColumnLabel(2));

            assertEquals("Column 3, unexpected columnName", "ADD", metaData.getColumnName(3));
            assertEquals("Column 3, unexpected columnLabel", "ADD", metaData.getColumnLabel(3));

            String expectedColumn4Name = firebirdVersion >= 3 ? "SUBTRACT" : "";
            assertEquals("Column 4, unexpected columnName", expectedColumn4Name, metaData.getColumnName(4));
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
    @Test
    public void columnNameAndLabel_ColumnLabelForName() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
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
    @Test
    public void cachedRowSetImpl_columnLabelForName() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
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

    @Test
    public void octetsCharAndVarchar_noOctetsAsBytes() throws Exception {
        Connection con = getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT char_octets_field, varchar_octets_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("Unexpected column type, expected CHAR", Types.CHAR, rsmd.getColumnType(1));
            assertEquals("Unexpected column precision", 10, rsmd.getPrecision(1));
            assertEquals("Unexpected column display size", 10, rsmd.getColumnDisplaySize(1));
            assertEquals("Unexpected column class name", "java.lang.String", rsmd.getColumnClassName(1));

            assertEquals("Unexpected column type, expected VARCHAR", Types.VARCHAR, rsmd.getColumnType(2));
            assertEquals("Unexpected column precision", 15, rsmd.getPrecision(2));
            assertEquals("Unexpected column display size", 15, rsmd.getColumnDisplaySize(2));
            assertEquals("Unexpected column class name", "java.lang.String", rsmd.getColumnClassName(2));
        } finally {
            closeQuietly(con);
        }
    }

    @Test
    public void octetsCharAndVarchar_octetsAsBytes() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("octetsAsBytes", "true");

        Connection con = DriverManager.getConnection(getUrl(), props);
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT char_octets_field, varchar_octets_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            assertEquals("Unexpected column type, expected CHAR", Types.BINARY, rsmd.getColumnType(1));
            assertEquals("Unexpected column precision", 10, rsmd.getPrecision(1));
            assertEquals("Unexpected column display size", 10, rsmd.getColumnDisplaySize(1));
            assertEquals("Unexpected column class name", "[B", rsmd.getColumnClassName(1));

            assertEquals("Unexpected column type, expected VARCHAR", Types.VARBINARY, rsmd.getColumnType(2));
            assertEquals("Unexpected column precision", 15, rsmd.getPrecision(2));
            assertEquals("Unexpected column display size", 15, rsmd.getColumnDisplaySize(2));
            assertEquals("Unexpected column class name", "[B", rsmd.getColumnClassName(2));
        } finally {
            closeQuietly(con);
        }
    }

    @Test
    public void precisionOfCalculatedField() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            DatabaseMetaData dmd = connection.getMetaData();
            int firebirdVersion = dmd.getDatabaseMajorVersion();

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT calculated_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            // For Firebird 2.5 and earlier we estimate, which results in 19 instead of 18
            int expectedPrecision = firebirdVersion >= 3 ? 18 : 19;
            assertEquals("Unexpected column precision", expectedPrecision, rsmd.getPrecision(1));
            assertEquals("Unexpected column display size", expectedPrecision, rsmd.getColumnDisplaySize(1));
        } finally {
            connection.close();
        }
    }

    @Test
    public void getPrecisionOfNumericColumnWithoutActiveTransaction() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        try {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT long_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            connection.commit();

            // Will throw exception in current versions, but should work
            assertEquals(15, rsmd.getPrecision(1));
        } finally {
            connection.close();
        }
    }
}
