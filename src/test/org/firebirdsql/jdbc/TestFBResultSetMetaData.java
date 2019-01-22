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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * This method tests correctness of {@link FBResultSetMetaData} class.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBResultSetMetaData {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    //@formatter:off
    private static final String CREATE_TABLE =
        "CREATE TABLE test_rs_metadata (" + 
        "  id INTEGER NOT NULL PRIMARY KEY, " +
        "  simple_field VARCHAR(60) CHARACTER SET WIN1250, " +
        "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
        "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
        "  long_field NUMERIC(15, 2), " +
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
    
    @BeforeClass
    public static void setUp() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            DdlHelper.executeCreateTable(connection, CREATE_TABLE);
        }
    }

    @Test
    public void testResultSetMetaData() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
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
        }
    }
    
    @Test
    public void testResultSetMetaData2() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
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
                fail("Unknown Firebird version, not clear what to compare.");

            int columnDisplaySize2 = metaData.getColumnDisplaySize(4);

            if (firebirdVersion == 1)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 10 ", 10, columnDisplaySize2);
            else if (firebirdVersion >= 2 && firebirdVersion < 4)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 31 ", 31, columnDisplaySize2);
            else if (firebirdVersion >= 4)
                assertEquals("RDB$CHARACTER_SET_NAME must have display size 63 ", 63, columnDisplaySize2);
            else
                fail("Unknown Firebird version, not clear what to compare.");

            stmt.close();
        }
    }
    
    @Test
    public void testColumnTypeName() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
		props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
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
        try (Connection con = getConnectionViaDriverManager()) {
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

            if (con.getMetaData().getDatabaseMajorVersion() < 3) {
                assertEquals("Column 4, unexpected columnName", "", metaData.getColumnName(4));
            } else {
                assertEquals("Column 4, unexpected columnName", "SUBTRACT", metaData.getColumnName(4));
            }
            assertEquals("Column 4, unexpected columnLabel", "COLUMN4ALIAS", metaData.getColumnLabel(4));

            stmt.close();
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

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
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

        try (Connection con = DriverManager.getConnection(getUrl(), props)) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT id, simple_field AS column2, int_field AS column3, 2 - 1 AS column4 FROM test_rs_metadata");

            RowSetFactory aFactory = RowSetProvider.newFactory();
            CachedRowSet rowSet = aFactory.createCachedRowSet();
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
            rowSet.close();
            stmt.close();
        }
    }

    @Test
    public void octetsCharAndVarchar() throws Exception {
        try (Connection con = getConnectionViaDriverManager()) {
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
        }
    }

    @Test
    public void precisionOfCalculatedField() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT calculated_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            // For Firebird 2.5 and earlier we estimate
            assertEquals("Unexpected column precision", 18, rsmd.getPrecision(1));
            assertEquals("Unexpected column display size", 18 + 2, rsmd.getColumnDisplaySize(1));
        }
    }

    @Test
    public void getPrecisionOfNumericColumnWithoutActiveTransaction() throws Exception {
        try (Connection connection = getConnectionViaDriverManager()) {
            connection.setAutoCommit(false);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT long_field FROM test_rs_metadata");
            ResultSetMetaData rsmd = rs.getMetaData();

            connection.commit();

            // Will throw exception in current versions, but should work
            assertEquals(15, rsmd.getPrecision(1));
        }
    }

    /**
     * Test for CORE-5655
     */
    @Test
    public void getTableAliasCTE() throws Exception {
        // TODO Also works for 2.5.8+ and 3.0.3+, relax to isVersionEqualOrAbove(2, 5) after release of 2.5.8 and 3.0.3
        assumeTrue("Firebird 4 or higher", getDefaultSupportInfo().isVersionEqualOrAbove(4, 0));
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            stmt.execute("\n"
                    + "create table tablea("
                    + "column1 varchar(20),"
                    + "column2 varchar(20)"
                    + "); ");

            for (String query : new String[] {
                    "select a.column1 from (select b.column1 from (select c.column1 from tablea c) b) a",
                    "with a as (SELECT column1 from TABLEA)\n"
                            + "select column1 from a",
                    "select * from (select column1 from tablea b) a"
            }) {

                try (ResultSet rs = stmt.executeQuery(query)) {
                    System.out.println(query);
                    FirebirdResultSetMetaData rsmd = rs.getMetaData().unwrap(FirebirdResultSetMetaData.class);
                    final String columnLabel = rsmd.getColumnLabel(1);
                    final String tableAlias = rsmd.getTableAlias(1);
                    System.out.println("'" + columnLabel + "'");
                    System.out.println("'" + tableAlias + "'");
                    assertEquals("columnLabel", "COLUMN1", columnLabel);
                    assertEquals("tableAlias", "A", tableAlias);
                }
                System.out.println("---------");
            }
        }
    }

    /**
     * Test for CORE-5713
     */
    @Test
    public void core5713() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {

            for (String query : new String[] {
                    "select 1 a1, 2 a2\n"
                            + "from rdb$database\n"
                            + "union all\n"
                            + "select 1 a1, coalesce(cast(null as varchar(64)), 0) a2\n"
                            + "from rdb$database ",
                    "select a1, a2\n"
                            + "from\n"
                            + "  (select 1 a1, 2 a2\n"
                            + "  from rdb$database)\n"
                            + "group by 1, 2\n"
                            + "union all\n"
                            + "select 1 a1, coalesce(cast(null as varchar(64)), 0) a2\n"
                            + "from rdb$database"
            }) {

                try (ResultSet rs = stmt.executeQuery(query)) {
                    FirebirdResultSetMetaData rsmd = rs.getMetaData().unwrap(FirebirdResultSetMetaData.class);
                    assertEquals("columnLabel1", "A1", rsmd.getColumnLabel(1));
                    assertEquals("columnLabel2", "A2", rsmd.getColumnLabel(2));
                }
            }
        }
    }
}