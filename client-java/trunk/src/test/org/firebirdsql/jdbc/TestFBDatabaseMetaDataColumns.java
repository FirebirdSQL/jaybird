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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.firebirdsql.jdbc.MetaDataValidator.MetaDataInfo;

import static org.firebirdsql.common.JdbcResourceHelper.*;

/**
 * Tests for {@link FBDatabaseMetaData} for column related metadata.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBDatabaseMetaDataColumns extends FBMetaDataTestBase<TestFBDatabaseMetaDataColumns.ColumnMetaData> {

    public TestFBDatabaseMetaDataColumns(String name) {
        super(name, ColumnMetaData.class);
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
            "    col_blob_binary BLOB SUB_TYPE 0," +
            "    col_integer_not_null INTEGER NOT NULL," + 
            "    col_varchar_not_null VARCHAR(100) NOT NULL," +
            "    col_integer_default_null INTEGER DEFAULT NULL," + 
            "    col_integer_default_999 INTEGER DEFAULT 999," + 
            "    col_varchar_default_null VARCHAR(100) DEFAULT NULL," + 
            "    col_varchar_default_user VARCHAR(100) DEFAULT USER," + 
            "    col_varchar_default_literal VARCHAR(100) DEFAULT 'literal'," + 
            "    col_varchar_generated VARCHAR(200) COMPUTED BY (col_varchar_default_user || ' ' || col_varchar_default_literal)" +
            ")";

    public static final String ADD_COMMENT_ON_COLUMN = 
            "COMMENT ON COLUMN test_column_metadata.col_integer IS 'Some comment'";

    protected List<String> getCreateStatements() {
        return Arrays.asList(
                CREATE_COLUMN_METADATA_TEST_TABLE,
                ADD_COMMENT_ON_COLUMN);
    }
    
    /**
     * Tests the ordinal positions and types for the metadata columns of getColumns().
     */
    public void testColumnMetaDataColumns() throws Exception {
        ResultSet columns = dbmd.getColumns(null, null, null, null);
        try {
            validateResultSetColumns(columns);
        } finally {
            closeQuietly(columns);
        }
    }

    /**
     * Tests getColumns() metadata for an INTEGER column without further
     * constraints, defaults, but with an explicit remark.
     */
    public void testIntegerColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        // Explicit comment:
        validationRules.put(ColumnMetaData.REMARKS, "Some comment");
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 1);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_integer", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with explicit DEFAULT
     * NULL
     */
    public void testInteger_DefaultNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 32);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.COLUMN_DEF, "NULL");

        validate("test_column_metadata", "col_integer_default_null", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with DEFAULT 999
     */
    public void testInteger_Default999Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 33);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.COLUMN_DEF, "999");

        validate("test_column_metadata", "col_integer_default_999", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with NOT NULL
     * constraint
     */
    public void testInteger_NotNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 30);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNoNulls);
        validationRules.put(ColumnMetaData.IS_NULLABLE, "NO");

        validate("test_column_metadata", "col_integer_not_null", validationRules);
    }

    /**
     * Tests getColumns() metadata for an BIGINT column without further
     * constraints, defaults and remarks.
     */
    public void testBigintColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.BIGINT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BIGINT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 19);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 2);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_bigint", validationRules);
    }

    /**
     * Tests getColumns() metadata for an SMALLINT column without further
     * constraints, defaults and remarks.
     */
    public void testSmallintColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.SMALLINT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "SMALLINT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 5);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 3);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_smallint", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DOUBLE PRECISION column without
     * further constraints, defaults and remarks.
     */
    public void testDoublePrecisionColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DOUBLE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DOUBLE PRECISION");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 15);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 4);

        validate("test_column_metadata", "col_double", validationRules);
    }

    /**
     * Tests getColumns() metadata for an FLOAT column without further
     * constraints, defaults and remarks.
     */
    public void testFloatColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.FLOAT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "FLOAT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 5);

        validate("test_column_metadata", "col_float", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(18,2) column without further
     * constraints, defaults and remarks.
     */
    public void testDecimal18_2Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 2);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 6);

        validate("test_column_metadata", "col_dec18_2", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(18,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     */
    public void testDecimal18_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 7);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_dec18_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(7,3) column without further
     * constraints, defaults and remarks.
     */
    public void testDecimal7_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 8);

        validate("test_column_metadata", "col_dec7_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(7,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     */
    public void testDecimal7_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 9);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_dec7_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(4,3) column without further
     * constraints, defaults and remarks.
     */
    public void testDecimal4_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 10);

        validate("test_column_metadata", "col_dec4_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(4,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     */
    public void testDecimal4_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 11);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_dec4_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(18,2) column without further
     * constraints, defaults and remarks.
     */
    public void testNumeric18_2Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 2);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 12);

        validate("test_column_metadata", "col_num18_2", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(18,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     */
    public void testNumeric18_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 13);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_num18_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(7,3) column without further
     * constraints, defaults and remarks.
     */
    public void testNumeric7_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 14);

        validate("test_column_metadata", "col_num7_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(7,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     */
    public void testNumeric7_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 15);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_num7_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(4,3) column without further
     * constraints, defaults and remarks.
     */
    public void testNumeric4_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 16);

        validate("test_column_metadata", "col_num4_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(4,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     */
    public void testNumeric4_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 17);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate("test_column_metadata", "col_num4_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for a DATE column without further
     * constraints, defaults and remarks.
     */
    public void testDateColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DATE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DATE");
        // TODO: Verify if current value matches JDBC spec
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 18);

        validate("test_column_metadata", "col_date", validationRules);
    }

    /**
     * Tests getColumns() metadata for a TIME column without further
     * constraints, defaults and remarks.
     */
    public void testTimeColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIME);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIME");
        // TODO: Verify if current value matches JDBC spec
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 8);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 19);

        validate("test_column_metadata", "col_time", validationRules);
    }

    /**
     * Tests getColumns() metadata for a TIMESTAMP column without further
     * constraints, defaults and remarks.
     */
    public void testTimestampColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIMESTAMP);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIMESTAMP");
        // TODO: Verify if current value matches JDBC spec
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 19);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 20);

        validate("test_column_metadata", "col_timestamp", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET UTF8 column
     * without further constraints, defaults and remarks.
     */
    public void testChar10_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.CHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 21);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 40);

        validate("test_column_metadata", "col_char_10_utf8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET ISO8859_1
     * column without further constraints, defaults and remarks.
     */
    public void testChar10_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.CHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 22);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate("test_column_metadata", "col_char_10_iso8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET OCTETS column
     * without further constraints, defaults and remarks.
     */
    public void testChar10_OCTETSColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.CHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 23);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate("test_column_metadata", "col_char_10_octets", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET UTF8 column
     * without further constraints, defaults and remarks.
     */
    public void testVarchar10_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 24);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 40);

        validate("test_column_metadata", "col_varchar_10_utf8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET ISO8859_1
     * column without further constraints, defaults and remarks.
     */
    public void testVarchar10_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 25);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate("test_column_metadata", "col_varchar_10_iso8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET OCTETS
     * column without further constraints, defaults and remarks.
     */
    public void testVarchar10_OCTETSColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 26);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate("test_column_metadata", "col_varchar_10_octets", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with explicit
     * DEFAULT NULL.
     */
    public void testVarchar_DefaultNull() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 34);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "NULL");

        validate("test_column_metadata", "col_varchar_default_null", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with explicit
     * DEFAULT USER.
     */
    public void testVarchar_DefaultUser() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 35);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "USER");

        validate("test_column_metadata", "col_varchar_default_user", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with literal
     * DEFAULT (DEFAULT 'literal').
     */
    public void testVarchar_DefaultLiteral() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 36);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "'literal'");

        validate("test_column_metadata", "col_varchar_default_literal", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(200) column COMPUTED BY
     */
    public void testVarchar_Generated() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 200);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 37);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 200);
        validationRules.put(ColumnMetaData.IS_GENERATEDCOLUMN, "YES");

        validate("test_column_metadata", "col_varchar_generated", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) with NOT NULL
     * constraint.
     */
    public void testVarchar_NotNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 31);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNoNulls);
        validationRules.put(ColumnMetaData.IS_NULLABLE, "NO");

        validate("test_column_metadata", "col_varchar_not_null", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET UTF8
     * column without further constraints, defaults and remarks.
     */
    public void testTextBlob_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE 1");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 27);

        validate("test_column_metadata", "col_blob_text_utf8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET
     * ISO8859_1 column without further constraints, defaults and remarks.
     */
    public void testTextBlob_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE 1");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 28);

        validate("test_column_metadata", "col_blob_text_iso8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE 0 column without
     * further constraints, defaults and remarks.
     */
    public void testBlobColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARBINARY);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE 0");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 29);

        validate("test_column_metadata", "col_blob_binary", validationRules);
    }
    
    // TODO: Add more extensive tests of patterns

    /**
     * Method to validate the column metadata for a single column of a table (does not support quoted identifiers).
     * 
     * @param tableName Name of the able
     * @param columnName Name of the column
     * @param validationRules Map of validationRules
     * @throws SQLException
     */
    private void validate(String tableName, String columnName, Map<ColumnMetaData, Object> validationRules) throws Exception {
        validationRules.put(ColumnMetaData.TABLE_NAME, tableName.toUpperCase());
        validationRules.put(ColumnMetaData.COLUMN_NAME, columnName.toUpperCase());
        checkValidationRulesComplete(validationRules);
        
        ResultSet columns = dbmd.getColumns(null, null, tableName, columnName);
        try {
            assertTrue("Expected row in column metadata", columns.next());
            validateRowValues(columns, validationRules);
            assertFalse("Expected only one row in resultset", columns.next());
        } finally {
            closeQuietly(columns);
        }
    }

    private static final Map<ColumnMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<ColumnMetaData, Object> defaults = new EnumMap<ColumnMetaData, Object>(ColumnMetaData.class);
        defaults.put(ColumnMetaData.TABLE_CAT, null);
        defaults.put(ColumnMetaData.TABLE_SCHEM, null);
        defaults.put(ColumnMetaData.BUFFER_LENGTH, null);
        defaults.put(ColumnMetaData.DECIMAL_DIGITS, null);
        defaults.put(ColumnMetaData.NUM_PREC_RADIX, null);
        defaults.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNullable);
        defaults.put(ColumnMetaData.REMARKS, null);
        defaults.put(ColumnMetaData.COLUMN_DEF, null);
        defaults.put(ColumnMetaData.SQL_DATA_TYPE, null);
        defaults.put(ColumnMetaData.SQL_DATETIME_SUB, null);
        defaults.put(ColumnMetaData.CHAR_OCTET_LENGTH, null);
        defaults.put(ColumnMetaData.IS_NULLABLE, "YES");
        defaults.put(ColumnMetaData.SCOPE_SCHEMA, null);
        defaults.put(ColumnMetaData.SCOPE_TABLE, null);
        defaults.put(ColumnMetaData.SOURCE_DATA_TYPE, null);
        defaults.put(ColumnMetaData.IS_AUTOINCREMENT, "NO");
        defaults.put(ColumnMetaData.IS_GENERATEDCOLUMN, "NO");

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    protected Map<ColumnMetaData, Object> getDefaultValueValidationRules() throws Exception {
        Map<ColumnMetaData, Object> defaults = new EnumMap<ColumnMetaData, Object>(DEFAULT_COLUMN_VALUES);
        if (dbmd.getJDBCMajorVersion() > 4 || dbmd.getJDBCMajorVersion() == 4 && dbmd.getJDBCMinorVersion() >= 1) {
            defaults.put(ColumnMetaData.SCOPE_CATALOG, null);
        } else {
            defaults.put(ColumnMetaData.SCOPE_CATLOG, null);
        }
        return defaults;
    }
    
    private static final Set<ColumnMetaData> JDBC_41_COLUMN_METADATA;
    private static final Set<ColumnMetaData> JDBC_40_COLUMN_METADATA;
    static {
        JDBC_41_COLUMN_METADATA = Collections.unmodifiableSet(
                EnumSet.complementOf(EnumSet.of(ColumnMetaData.SCOPE_CATLOG)));
        JDBC_40_COLUMN_METADATA = Collections.unmodifiableSet(
                EnumSet.complementOf(EnumSet.of(ColumnMetaData.SCOPE_CATALOG)));
    }
    
    protected Set<ColumnMetaData> getRequiredMetaData() throws Exception {
        if (dbmd.getJDBCMajorVersion() > 4 || dbmd.getJDBCMajorVersion() == 4 && dbmd.getJDBCMinorVersion() >= 1) {
            return EnumSet.copyOf(JDBC_41_COLUMN_METADATA);
        } else {
            return EnumSet.copyOf(JDBC_40_COLUMN_METADATA);
        }
    }

    /**
     * Columns defined for the getColumns() metadata.
     */
    enum ColumnMetaData implements MetaDataInfo {
        TABLE_CAT(1, String.class), 
        TABLE_SCHEM(2, String.class), 
        TABLE_NAME(3, String.class), 
        COLUMN_NAME(4, String.class), 
        DATA_TYPE(5, Integer.class), 
        TYPE_NAME(6, String.class), 
        COLUMN_SIZE(7, Integer.class), 
        BUFFER_LENGTH(8, Integer.class), 
        DECIMAL_DIGITS(9, Integer.class), 
        NUM_PREC_RADIX(10, Integer.class), 
        NULLABLE(11, Integer.class), 
        REMARKS(12, String.class), 
        COLUMN_DEF(13, String.class), 
        SQL_DATA_TYPE(14, Integer.class), 
        SQL_DATETIME_SUB(15, Integer.class), 
        CHAR_OCTET_LENGTH(16, Integer.class), 
        ORDINAL_POSITION(17, Integer.class), 
        IS_NULLABLE(18, String.class), 
        SCOPE_CATLOG(19, String.class), // JDBC 3.0 and 4.0
        SCOPE_CATALOG(19, String.class), // JDBC 4.1 and up
        SCOPE_SCHEMA(20, String.class), 
        SCOPE_TABLE(21, String.class), 
        SOURCE_DATA_TYPE(22, Short.class), 
        IS_AUTOINCREMENT(23, String.class), 
        IS_GENERATEDCOLUMN(24,String.class);

        private final int position;
        private final Class<?> columnClass;

        private ColumnMetaData(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        public int getPosition() {
            return position;
        }
        
        public Class<?> getColumnClass() {
            return columnClass;
        }
        
        public MetaDataValidator<?> getValidator() {
            return new MetaDataValidator<ColumnMetaData>(this);
        }
    }
}
