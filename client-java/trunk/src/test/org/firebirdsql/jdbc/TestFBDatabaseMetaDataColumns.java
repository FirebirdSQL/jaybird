/*
 * $Id$
 * 
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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.firebirdsql.common.FBTestBase;

/**
 * Tests for {@link FBDatabaseMetaData} for column related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataColumns extends FBTestBase {
    
    public TestFBDatabaseMetaDataColumns(String name) {
        super(name);
    }
    
    public static final String CREATE_COLUMN_METADATA_TEST_TABLE =
            "CREATE TABLE test_column_metadata (" + 
            "    col_integer INTEGER," + 
            "    col_bigint BIGINT," + 
            "    col_smallint SMALLINT," + 
            "    col_double DOUBLE PRECISION," + 
            "    col_float FLOAT," + 
            "    col_dec18_2 DECIMAL(18,2)," +
            "    col_dec18_0 DECIMAL(18,0)," +
            "    col_dec7_3 DECIMAL(7,3)," +
            "    col_dec7_0 DECIMAL(7,0)," +
            "    col_dec4_3 DECIMAL(4,3), " +
            "    col_dec4_0 DECIMAL(4,0), " +
            "    col_num18_2 NUMERIC(18,2)," + 
            "    col_num18_0 NUMERIC(18,0)," + 
            "    col_num7_3 NUMERIC(7,3)," +
            "    col_num7_0 NUMERIC(7,0)," +
            "    col_num4_3 NUMERIC(4,3), " +
            "    col_num4_0 NUMERIC(4,0), " +
            "    col_date DATE," + 
            "    col_time TIME," + 
            "    col_timestamp TIMESTAMP," + 
            "    col_char_10_utf8 CHAR(10) CHARACTER SET UTF8," + 
            "    col_char_10_iso8859_1 CHAR(10) CHARACTER SET ISO8859_1," + 
            "    col_char_10_octets CHAR(10) CHARACTER SET OCTETS," + 
            "    col_varchar_10_utf8 VARCHAR(10) CHARACTER SET UTF8," + 
            "    col_varchar_10_iso8859_1 VARCHAR(10) CHARACTER SET ISO8859_1," + 
            "    col_varchar_10_octets VARCHAR(10) CHARACTER SET OCTETS," + 
            "    col_blob_text_utf8 BLOB SUB_TYPE TEXT CHARACTER SET UTF8," + 
            "    col_blob_text_iso8859_1 BLOB SUB_TYPE TEXT CHARACTER SET ISO8859_1," + 
            "    col_blob_binary BLOB SUB_TYPE 0" + 
            ")";
    
    public static final String DROP_COLUMN_METADATA_TEST_TABLE =
            "DROP TABLE test_column_metadata";
    
    private Connection con;
    private DatabaseMetaData dbmd;
    
    protected void setUp() throws Exception {
        super.setUp();
        con = getConnectionViaDriverManager();
        try {
            executeDropTable(con, DROP_COLUMN_METADATA_TEST_TABLE);
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            throw e;
        }
        executeCreateTable(con, CREATE_COLUMN_METADATA_TEST_TABLE);
        dbmd = con.getMetaData();
    }
    
    protected void tearDown() throws Exception {
        try {
            executeDropTable(con, DROP_COLUMN_METADATA_TEST_TABLE);
        } finally {
            closeQuietly(con);
            super.tearDown();
        }
    }
    
    /**
     * Checked getColumns() metadata for an INTEGER column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testIntegerColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_integer");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_INTEGER", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.INTEGER, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "INTEGER", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for INTEGER", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for INTEGER", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 1, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an BIGINT column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testBigintColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_bigint");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_BIGINT", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.BIGINT, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "BIGINT", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 19, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for BIGINT", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for BIGINT", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 2, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
        /**
     * Checked getColumns() metadata for an SMALLINT column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testSmallintColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_smallint");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_SMALLINT", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.SMALLINT, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "SMALLINT", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 5, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for SMALLINT", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for SMALLINT", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 3, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DOUBLE PRECISION column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testDoublePrecisionColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_double");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DOUBLE", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DOUBLE, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DOUBLE PRECISION", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 15, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for DOUBLE PRECISION", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DOUBLE PRECISION", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 4, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an FLOAT column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testFloatColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_float");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_FLOAT", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.FLOAT, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "FLOAT", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 7, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for FLOAT", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DOUBLE PRECISION", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 5, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(18,2) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testDecimal18_2Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec18_2");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC18_2", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 18, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 2, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 6, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(18,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     * 
     * @throws SQLException
     */
    public void testDecimal18_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec18_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC18_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 18, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for DECIMAL(18,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 7, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(7,3) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testDecimal7_3Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec7_3");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC7_3", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 7, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 3, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 8, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(7,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     * 
     * @throws SQLException
     */
    public void testDecimal7_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec7_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC7_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 7, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for DECIMAL(7,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 9, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(4,3) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testDecimal4_3Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec4_3");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC4_3", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 4, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 3, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 10, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an DECIMAL(4,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     * 
     * @throws SQLException
     */
    public void testDecimal4_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_dec4_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DEC4_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DECIMAL, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DECIMAL", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 4, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for DECIMAL(7,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DECIMAL", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 11, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(18,2) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testNumeric18_2Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num18_2");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM18_2", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 18, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 2, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 12, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(18,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     * 
     * @throws SQLException
     */
    public void testNumeric18_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num18_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM18_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 18, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for NUMERIC(18,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 13, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(7,3) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testNumeric7_3Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num7_3");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM7_3", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 7, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 3, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 14, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(7,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     * 
     * @throws SQLException
     */
    public void testNumeric7_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num7_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM7_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 7, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for NUMERIC(7,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 15, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(4,3) column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testNumeric4_3Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num4_3");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM4_3", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 4, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 3, columns.getInt("DECIMAL_DIGITS"));
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 16, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for an NUMERIC(4,0) column without further constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     * 
     * @throws SQLException
     */
    public void testNumeric4_0Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_num4_0");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_NUM4_0", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.NUMERIC, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "NUMERIC", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 4, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertFalse("DECIMAL_DIGITS expected to be actual 0 for NUMERIC(7,0)", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 10, columns.getInt("NUM_PREC_RADIX"));
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for NUMERIC", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 17, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a DATE column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testDateColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_date");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_DATE", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.DATE, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "DATE", columns.getString("TYPE_NAME"));
        // TODO: Verify if current value matches JDBC spec
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for DATE", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for DATE", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for DATE", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 18, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a TIME column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testTimeColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_time");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_TIME", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.TIME, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "TIME", columns.getString("TYPE_NAME"));
        // TODO: Verify if current value matches JDBC spec
        assertEquals("Unexpected COLUMN_SIZE", 8, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for TIME", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for TIME", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for TIME", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 19, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a TIMESTAMP column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testTimestampColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_timestamp");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_TIMESTAMP", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.TIMESTAMP, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "TIMESTAMP", columns.getString("TYPE_NAME"));
        // TODO: Verify if current value matches JDBC spec
        assertEquals("Unexpected COLUMN_SIZE", 19, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for TIMESTAMP", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for TIMESTAMP", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for TIMESTAMP", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 20, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a CHAR(10) CHARACTER SET UTF8 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testChar10_UTF8Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_char_10_utf8");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_CHAR_10_UTF8", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.CHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "CHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 40, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 21, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a CHAR(10) CHARACTER SET ISO8859_1 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testChar10_ISO8859_1Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_char_10_iso8859_1");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_CHAR_10_ISO8859_1", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.CHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "CHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 10, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 22, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a CHAR(10) CHARACTER SET OCTETS column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testChar10_OCTETSColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_char_10_octets");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_CHAR_10_OCTETS", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.CHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "CHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for CHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 10, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 23, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a VARCHAR(10) CHARACTER SET UTF8 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testVarchar10_UTF8Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_varchar_10_utf8");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_VARCHAR_10_UTF8", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.VARCHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "VARCHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 40, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 24, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a CHAR(10) CHARACTER SET ISO8859_1 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testVarchar10_ISO8859_1Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_varchar_10_iso8859_1");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_VARCHAR_10_ISO8859_1", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.VARCHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "VARCHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 10, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 25, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a CHAR(10) CHARACTER SET OCTETS column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testVarchar10_OCTETSColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_varchar_10_octets");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_VARCHAR_10_OCTETS", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.VARCHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "VARCHAR", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 10, columns.getInt("COLUMN_SIZE"));
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 10, columns.getInt("CHAR_OCTET_LENGTH"));
        assertEquals("Unexpected ORDINAL_POSITION", 26, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET UTF8 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testTextBlob_UTF8Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_blob_text_utf8");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_BLOB_TEXT_UTF8", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.LONGVARCHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "BLOB SUB_TYPE 1", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 0, columns.getInt("COLUMN_SIZE"));
        assertTrue("COLUMN_SIZE expected to ba actual NULL for BLOB SUB_TYPE 1", columns.wasNull());
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for BLOB SUB_TYPE 1", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 27, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET ISO8859_1 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testTextBlob_ISO8859_1Column() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_blob_text_iso8859_1");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_BLOB_TEXT_ISO8859_1", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.LONGVARCHAR, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "BLOB SUB_TYPE 1", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 0, columns.getInt("COLUMN_SIZE"));
        assertTrue("COLUMN_SIZE expected to ba actual NULL for BLOB SUB_TYPE 1", columns.wasNull());
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for BLOB SUB_TYPE 1", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 28, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Checked getColumns() metadata for a BLOB SUB_TYPE 0 column without further constraints, defaults and remarks.
     * 
     * @throws SQLException
     */
    public void testBlobColumn() throws SQLException {
        ResultSet columns = dbmd.getColumns(null, null, "test_column_metadata", "col_blob_binary");
        assertTrue("Expected row in column metadata", columns.next());
        assertEquals("Unexpected TABLE_NAME", "TEST_COLUMN_METADATA", columns.getString("TABLE_NAME"));
        assertEquals("Unexpected COLUMN_NAME", "COL_BLOB_BINARY", columns.getString("COLUMN_NAME"));
        assertInvariantColumnMetaData(columns);
        
        assertEquals("Unexpected DATA_TYPE", Types.LONGVARBINARY, columns.getInt("DATA_TYPE"));
        assertEquals("Unexpected TYPE_NAME", "BLOB SUB_TYPE 0", columns.getString("TYPE_NAME"));
        assertEquals("Unexpected COLUMN_SIZE", 0, columns.getInt("COLUMN_SIZE"));
        assertTrue("COLUMN_SIZE expected to ba actual NULL for BLOB SUB_TYPE 0", columns.wasNull());
        assertEquals("Unexpected DECIMAL_DIGITS", 0, columns.getInt("DECIMAL_DIGITS"));
        assertTrue("DECIMAL_DIGITS expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NUM_PREC_RADIX", 0, columns.getInt("NUM_PREC_RADIX"));
        assertTrue("NUM_PREC_RADIX expected to be actual NULL for VARCHAR", columns.wasNull());
        assertEquals("Unexpected NULLABLE", DatabaseMetaData.columnNullable, columns.getInt("NULLABLE"));
        assertNull("Unexpected REMARKS not NULL", columns.getString("REMARKS"));
        assertNull("Unexpected COLUMN_DEF not NULL", columns.getString("COLUMN_DEF"));
        assertEquals("Unexpected CHAR_OCTET_LENGTH", 0, columns.getInt("CHAR_OCTET_LENGTH"));
        assertTrue("CHAR_OCTET_LENGTH expected to be actual NULL for BLOB SUB_TYPE 0", columns.wasNull());
        assertEquals("Unexpected ORDINAL_POSITION", 29, columns.getInt("ORDINAL_POSITION"));
        assertEquals("Unexpected IS_NULLABLE", "YES", columns.getString("IS_NULLABLE"));
        assertEquals("Unexpected IS_AUTOINCREMENT", "NO", columns.getString("IS_AUTOINCREMENT"));
        
        assertFalse("Expected only one row in resultset", columns.next());
    }
    
    /**
     * Performs the assertion on column metadata that applies to all datatypes for all tables (mostly values that are always null).
     * <p>
     * Method expects resultset that is already positioned on the row/column to be checked.
     * </p>
     * 
     * @param columns ResultSet of the column metadata.
     * @throws SQLException For errors retrieving results
     */
    private void assertInvariantColumnMetaData(ResultSet columns) throws SQLException {
        assertNull("TABLE_CAT should be NULL", columns.getString("TABLE_CAT"));
        assertNull("TABLE_SCHEM should be NULL", columns.getString("TABLE_SCHEM"));
        assertEquals("BUFFER_LENGTH should be NULL/0", 0, columns.getInt("BUFFER_LENGTH"));
        assertTrue("BUFFER_LENGTH should be actual NULL", columns.wasNull());
        assertEquals("SQL_DATA_TYPE should NULL/0", 0, columns.getInt("SQL_DATA_TYPE"));
        assertTrue("SQL_DATA_TYPE should be actual NULL", columns.wasNull());
        assertEquals("SQL_DATETIME_SUB should NULL/0", 0, columns.getInt("SQL_DATETIME_SUB"));
        assertTrue("SQL_DATETIME_SUB should be actual NULL", columns.wasNull());
        final String scopeCatalog;
        if (dbmd.getJDBCMajorVersion() > 4 || dbmd.getJDBCMajorVersion() == 4 && dbmd.getJDBCMinorVersion() >= 1) {
            scopeCatalog = "SCOPE_CATALOG";
        } else {
            scopeCatalog = "SCOPE_CATLOG";
        }
        assertNull(scopeCatalog + " should be NULL", columns.getString(scopeCatalog));
        assertNull("SCOPE_SCHEMA should be NULL", columns.getString("SCOPE_SCHEMA"));
        assertNull("SCOPE_TABLE should be NULL", columns.getString("SCOPE_TABLE"));
        assertEquals("SOURCE_DATA_TYPE  should NULL/0", 0, columns.getShort("SOURCE_DATA_TYPE"));
        assertTrue("SOURCE_DATA_TYPE should be actual NULL", columns.wasNull());
        assertEquals("", columns.getString("IS_GENERATEDCOLUMN"));
    }
}
