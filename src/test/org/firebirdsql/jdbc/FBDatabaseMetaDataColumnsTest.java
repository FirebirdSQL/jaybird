/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for {@link FBDatabaseMetaData} for column related metadata.
 * 
 * @author Mark Rotteveel
 */
class FBDatabaseMetaDataColumnsTest {

    private static final String TEST_TABLE = "TEST_COLUMN_METADATA";

    private static final String CREATE_DOMAIN_WITH_DEFAULT =
            "CREATE DOMAIN DOMAIN_WITH_DEFAULT AS VARCHAR(100) DEFAULT 'this is a default'";

    //@formatter:off
    private static final String CREATE_COLUMN_METADATA_TEST_TABLE =
            "CREATE TABLE " + TEST_TABLE + " (" +
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
            "    col_blob_binary BLOB SUB_TYPE BINARY," +
            "    col_integer_not_null INTEGER NOT NULL," + 
            "    col_varchar_not_null VARCHAR(100) NOT NULL," +
            "    col_integer_default_null INTEGER DEFAULT NULL," + 
            "    col_integer_default_999 INTEGER DEFAULT 999," + 
            "    col_varchar_default_null VARCHAR(100) DEFAULT NULL," + 
            "    col_varchar_default_user VARCHAR(100) DEFAULT USER," + 
            "    col_varchar_default_literal VARCHAR(100) DEFAULT 'literal'," + 
            "    col_varchar_generated VARCHAR(200) COMPUTED BY (col_varchar_default_user || ' ' || col_varchar_default_literal)," +
            "    col_domain_with_default DOMAIN_WITH_DEFAULT," +
            "    col_domain_w_default_overridden DOMAIN_WITH_DEFAULT DEFAULT 'overridden default' " +
            "    /* boolean */ " +
            "    /* decfloat */ " +
            "    /* extended numerics */ " +
            "    /* time zone */ " +
            "    /* int128 */ " +
            ")";
    //@formatter:on

    private static final String ADD_COMMENT_ON_COLUMN =
            "COMMENT ON COLUMN test_column_metadata.col_integer IS 'Some comment'";

    private static final MetadataResultSetDefinition getColumnsDefinition =
            new MetadataResultSetDefinition(ColumnMetaData.class);

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            getCreateStatements());

    private static Connection con;
    private static DatabaseMetaData dbmd;

    @BeforeAll
    static void setupAll() throws SQLException {
        con = getConnectionViaDriverManager();
        dbmd = con.getMetaData();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        try {
            con.close();
        } finally {
            con = null;
            dbmd = null;
        }
    }

    private static List<String> getCreateStatements() {
        FirebirdSupportInfo supportInfo = FBTestProperties.getDefaultSupportInfo();
        List<String> statements = new ArrayList<>();
        statements.add(CREATE_DOMAIN_WITH_DEFAULT);
        String createTable = CREATE_COLUMN_METADATA_TEST_TABLE;
        if (!supportInfo.supportsBigint()) {
            // No BIGINT support, replacing type so number of columns remain the same
            createTable = CREATE_COLUMN_METADATA_TEST_TABLE.replace("col_bigint BIGINT,",
                    "col_bigint DOUBLE PRECISION,");
        }
        if (supportInfo.supportsBoolean()) {
            createTable = createTable.replace("/* boolean */", ", col_boolean BOOLEAN");
        }
        if (supportInfo.supportsDecfloat()) {
            createTable = createTable.replace("/* decfloat */",
                    ", col_decfloat16 DECFLOAT(16), col_decfloat34 DECFLOAT(34)");
        }
        if (supportInfo.supportsDecimalPrecision(38)) {
            createTable = createTable.replace("/* extended numerics */",
                    ", col_numeric25_20 NUMERIC(25, 20), col_decimal30_5 DECIMAL(30,5)");
        }
        if (supportInfo.supportsTimeZones()) {
            createTable = createTable.replace("/* time zone */",
                    ", col_timetz TIME WITH TIME ZONE, col_timestamptz TIMESTAMP WITH TIME ZONE");
        }
        if (supportInfo.supportsInt128()) {
            createTable = createTable.replace("/* int128 */",
                    ", col_int128 INT128");
        }

        statements.add(createTable);
        if (supportInfo.supportsComment()) {
            statements.add(ADD_COMMENT_ON_COLUMN);
        }
        return statements;
    }
    
    /**
     * Tests the ordinal positions and types for the metadata columns of getColumns().
     */
    @Test
    void testColumnMetaDataColumns() throws Exception {
        try (ResultSet columns = dbmd.getColumns(null, null, "doesnotexist", null)) {
            getColumnsDefinition.validateResultSetColumns(columns);
        }
    }

    /**
     * Tests getColumns() metadata for an INTEGER column without further
     * constraints, defaults, but with an explicit remark.
     */
    @Test
    void testIntegerColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        // Explicit comment:
        validationRules.put(ColumnMetaData.REMARKS, "Some comment");
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 1);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_INTEGER", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with explicit DEFAULT
     * NULL
     */
    @Test
    void testInteger_DefaultNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 32);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.COLUMN_DEF, "NULL");

        validate(TEST_TABLE, "COL_INTEGER_DEFAULT_NULL", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with DEFAULT 999
     */
    @Test
    void testInteger_Default999Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 33);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.COLUMN_DEF, "999");

        validate(TEST_TABLE, "COL_INTEGER_DEFAULT_999", validationRules);
    }

    /**
     * Tests getColumns() metadata for an INTEGER column with NOT NULL
     * constraint
     */
    @Test
    void testInteger_NotNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.INTEGER);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INTEGER");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 30);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");
        validationRules.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNoNulls);
        validationRules.put(ColumnMetaData.IS_NULLABLE, "NO");

        validate(TEST_TABLE, "COL_INTEGER_NOT_NULL", validationRules);
    }

    /**
     * Tests getColumns() metadata for an BIGINT column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testBigintColumn() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsBigint(), "Test requires BIGINT support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.BIGINT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BIGINT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 19);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 2);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_BIGINT", validationRules);
    }

    /**
     * Tests getColumns() metadata for an SMALLINT column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testSmallintColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.SMALLINT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "SMALLINT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 5);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 3);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_SMALLINT", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DOUBLE PRECISION column without
     * further constraints, defaults and remarks.
     */
    @Test
    void testDoublePrecisionColumn() throws Exception {
        final boolean supportsFloatBinaryPrecision = getDefaultSupportInfo().supportsFloatBinaryPrecision();
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DOUBLE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DOUBLE PRECISION");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, supportsFloatBinaryPrecision ? 53 : 15);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, supportsFloatBinaryPrecision ? 2 : 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 4);

        validate(TEST_TABLE, "COL_DOUBLE", validationRules);
    }

    /**
     * Tests getColumns() metadata for an FLOAT column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testFloatColumn() throws Exception {
        final boolean supportsFloatBinaryPrecision = getDefaultSupportInfo().supportsFloatBinaryPrecision();
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.FLOAT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "FLOAT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, supportsFloatBinaryPrecision ? 24 : 7);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, supportsFloatBinaryPrecision ? 2 : 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 5);

        validate(TEST_TABLE, "COL_FLOAT", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(18,2) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testDecimal18_2Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 2);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 6);

        validate(TEST_TABLE, "COL_DEC18_2", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(18,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     */
    @Test
    void testDecimal18_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 7);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_DEC18_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(7,3) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testDecimal7_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 8);

        validate(TEST_TABLE, "COL_DEC7_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(7,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     */
    @Test
    void testDecimal7_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 9);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_DEC7_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(4,3) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testDecimal4_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 10);

        validate(TEST_TABLE, "COL_DEC4_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an DECIMAL(4,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     */
    @Test
    void testDecimal4_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 11);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_DEC4_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(18,2) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testNumeric18_2Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 2);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 12);

        validate(TEST_TABLE, "COL_NUM18_2", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(18,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a BIGINT
     * </p>
     */
    @Test
    void testNumeric18_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 18);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 13);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_NUM18_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(7,3) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testNumeric7_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 14);

        validate(TEST_TABLE, "COL_NUM7_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(7,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to an INTEGER
     * </p>
     */
    @Test
    void testNumeric7_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 7);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 15);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_NUM7_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(4,3) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testNumeric4_3Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 3);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 16);

        validate(TEST_TABLE, "COL_NUM4_3", validationRules);
    }

    /**
     * Tests getColumns() metadata for an NUMERIC(4,0) column without further
     * constraints, defaults and remarks.
     * <p>
     * Apart from the subtype this is actually identical to a SMALLINT
     * </p>
     */
    @Test
    void testNumeric4_0Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 4);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 17);
        validationRules.put(ColumnMetaData.IS_AUTOINCREMENT, "");

        validate(TEST_TABLE, "COL_NUM4_0", validationRules);
    }

    /**
     * Tests getColumns() metadata for a DATE column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testDateColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DATE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DATE");
        // TODO: Verify if current value matches JDBC spec
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 18);

        validate(TEST_TABLE, "COL_DATE", validationRules);
    }

    /**
     * Tests getColumns() metadata for a TIME column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testTimeColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIME);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIME");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 13);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 19);

        validate(TEST_TABLE, "COL_TIME", validationRules);
    }

    /**
     * Tests getColumns() metadata for a TIMESTAMP column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testTimestampColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIMESTAMP);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIMESTAMP");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 24);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 20);

        validate(TEST_TABLE, "COL_TIMESTAMP", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET UTF8 column
     * without further constraints, defaults and remarks.
     */
    @Test
    void testChar10_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.CHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 21);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 40);

        validate(TEST_TABLE, "COL_CHAR_10_UTF8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET ISO8859_1
     * column without further constraints, defaults and remarks.
     */
    @Test
    void testChar10_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.CHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 22);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate(TEST_TABLE, "COL_CHAR_10_ISO8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a CHAR(10) CHARACTER SET OCTETS column
     * without further constraints, defaults and remarks.
     */
    @Test
    void testChar10_OCTETSColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.BINARY);
        validationRules.put(ColumnMetaData.TYPE_NAME, "CHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 23);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate(TEST_TABLE, "COL_CHAR_10_OCTETS", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET UTF8 column
     * without further constraints, defaults and remarks.
     */
    @Test
    void testVarchar10_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 24);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 40);

        validate(TEST_TABLE, "COL_VARCHAR_10_UTF8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET ISO8859_1
     * column without further constraints, defaults and remarks.
     */
    @Test
    void testVarchar10_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 25);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate(TEST_TABLE, "COL_VARCHAR_10_ISO8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(10) CHARACTER SET OCTETS
     * column without further constraints, defaults and remarks.
     */
    @Test
    void testVarchar10_OCTETSColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARBINARY);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 10);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 26);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 10);

        validate(TEST_TABLE, "COL_VARCHAR_10_OCTETS", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with explicit
     * DEFAULT NULL.
     */
    @Test
    void testVarchar_DefaultNull() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 34);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "NULL");

        validate(TEST_TABLE, "COL_VARCHAR_DEFAULT_NULL", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with explicit
     * DEFAULT USER.
     */
    @Test
    void testVarchar_DefaultUser() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 35);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "USER");

        validate(TEST_TABLE, "COL_VARCHAR_DEFAULT_USER", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) column with literal
     * DEFAULT (DEFAULT 'literal').
     */
    @Test
    void testVarchar_DefaultLiteral() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 36);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "'literal'");

        validate(TEST_TABLE, "COL_VARCHAR_DEFAULT_LITERAL", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(200) column COMPUTED BY
     */
    @Test
    void testVarchar_Generated() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 200);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 37);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 200);
        validationRules.put(ColumnMetaData.IS_GENERATEDCOLUMN, "YES");

        validate(TEST_TABLE, "COL_VARCHAR_GENERATED", validationRules);
    }

    /**
     * Tests getColumns() metadata for a VARCHAR(100) with NOT NULL
     * constraint.
     */
    @Test
    void testVarchar_NotNullColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 31);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNoNulls);
        validationRules.put(ColumnMetaData.IS_NULLABLE, "NO");

        validate(TEST_TABLE, "COL_VARCHAR_NOT_NULL", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET UTF8
     * column without further constraints, defaults and remarks.
     */
    @Test
    void testTextBlob_UTF8Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE TEXT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 27);

        validate(TEST_TABLE, "COL_BLOB_TEXT_UTF8", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE TEXT CHARACTER SET
     * ISO8859_1 column without further constraints, defaults and remarks.
     */
    @Test
    void testTextBlob_ISO8859_1Column() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE TEXT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 28);

        validate(TEST_TABLE, "COL_BLOB_TEXT_ISO8859_1", validationRules);
    }

    /**
     * Tests getColumns() metadata for a BLOB SUB_TYPE 0 column without
     * further constraints, defaults and remarks.
     */
    @Test
    void testBlobColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.LONGVARBINARY);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BLOB SUB_TYPE BINARY");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, null);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 29);

        validate(TEST_TABLE, "COL_BLOB_BINARY", validationRules);
    }

    /**
     * Tests getColumns() metadata for a column that is defined through a domain with a
     * default (VARCHAR(100) DEFAULT 'this is a default')
     */
    @Test
    void testDomainWithDefaultColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 38);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "'this is a default'");

        validate(TEST_TABLE, "COL_DOMAIN_WITH_DEFAULT", validationRules);
    }

    /**
     * Tests getColumns() metadata for a column that is defined through a domain with a
     * default (VARCHAR(100) DEFAULT 'this is a default') and has its own default.
     */
    @Test
    void testDomainWithDefaultOverriddenColumn() throws Exception {
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.VARCHAR);
        validationRules.put(ColumnMetaData.TYPE_NAME, "VARCHAR");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 100);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 39);
        validationRules.put(ColumnMetaData.CHAR_OCTET_LENGTH, 100);
        validationRules.put(ColumnMetaData.COLUMN_DEF, "'overridden default'");

        validate(TEST_TABLE, "COL_DOMAIN_W_DEFAULT_OVERRIDDEN", validationRules);
    }

    @Test
    void testBooleanColumn() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsBoolean(), "Test requires BOOLEAN support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.BOOLEAN);
        validationRules.put(ColumnMetaData.TYPE_NAME, "BOOLEAN");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 1);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 40);
        validationRules.put(ColumnMetaData.NUM_PREC_RADIX, 2);

        validate(TEST_TABLE, "COL_BOOLEAN", validationRules);
    }

    @Test
    void testDecfloat16Column() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDecfloat(), "Test requires DECFLOAT(16) support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, JaybirdTypeCodes.DECFLOAT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECFLOAT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 16);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 41);

        validate(TEST_TABLE, "COL_DECFLOAT16", validationRules);
    }

    @Test
    void testDecfloat34Column() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDecfloat(), "Test requires DECFLOAT(34) support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, JaybirdTypeCodes.DECFLOAT);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECFLOAT");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 34);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 42);

        validate(TEST_TABLE, "COL_DECFLOAT34", validationRules);
    }

    /**
     * Tests getColumns() metadata for a NUMERIC(25,20) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testNumeric25_20Column() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDecimalPrecision(38),
                "Test requires extended numeric precision support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "NUMERIC");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 25);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 20);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 43);

        validate(TEST_TABLE, "COL_NUMERIC25_20", validationRules);
    }

    /**
     * Tests getColumns() metadata for a DECIMAL(30,5) column without further
     * constraints, defaults and remarks.
     */
    @Test
    void testDecimal30_5Column() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsDecimalPrecision(38),
                "Test requires extended numeric precision support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.DECIMAL);
        validationRules.put(ColumnMetaData.TYPE_NAME, "DECIMAL");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 30);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 5);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 44);

        validate(TEST_TABLE, "COL_DECIMAL30_5", validationRules);
    }

    @Test
    void testTimeWithTimezoneColumn() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsTimeZones(),
                "Test requires time zone support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIME_WITH_TIMEZONE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIME WITH TIME ZONE");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 19);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 45);

        validate(TEST_TABLE, "COL_TIMETZ", validationRules);
    }

    @Test
    void testTimestampWithTimezoneColumn() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsTimeZones(),
                "Test requires time zone support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.TIMESTAMP_WITH_TIMEZONE);
        validationRules.put(ColumnMetaData.TYPE_NAME, "TIMESTAMP WITH TIME ZONE");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 30);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 46);

        validate(TEST_TABLE, "COL_TIMESTAMPTZ", validationRules);
    }

    @Test
    void testInt128Column() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsInt128(),
                "Test requires INT128 support");
        Map<ColumnMetaData, Object> validationRules = getDefaultValueValidationRules();
        validationRules.put(ColumnMetaData.DATA_TYPE, Types.NUMERIC);
        validationRules.put(ColumnMetaData.TYPE_NAME, "INT128");
        validationRules.put(ColumnMetaData.COLUMN_SIZE, 38);
        validationRules.put(ColumnMetaData.DECIMAL_DIGITS, 0);
        validationRules.put(ColumnMetaData.ORDINAL_POSITION, 47);

        validate(TEST_TABLE, "COL_INT128", validationRules);
    }
    
    // TODO: Add more extensive tests of patterns

    /**
     * Method to validate the column metadata for a single column of a table (does not support quoted identifiers).
     * 
     * @param tableName Name of the able
     * @param columnName Name of the column
     * @param validationRules Map of validationRules
     */
    @SuppressWarnings("SameParameterValue")
    private void validate(String tableName, String columnName, Map<ColumnMetaData, Object> validationRules) throws Exception {
        validationRules.put(ColumnMetaData.TABLE_NAME, tableName);
        validationRules.put(ColumnMetaData.COLUMN_NAME, columnName);
        getColumnsDefinition.checkValidationRulesComplete(validationRules);

        try (ResultSet columns = dbmd.getColumns(null, null, tableName, columnName)) {
            assertTrue(columns.next(), "Expected row in column metadata");
            getColumnsDefinition.validateRowValues(columns, validationRules);
            assertFalse(columns.next(), "Expected only one row in resultset");
        }
    }

    private static final Map<ColumnMetaData, Object> DEFAULT_COLUMN_VALUES;
    static {
        Map<ColumnMetaData, Object> defaults = new EnumMap<>(ColumnMetaData.class);
        defaults.put(ColumnMetaData.TABLE_CAT, null);
        defaults.put(ColumnMetaData.TABLE_SCHEM, null);
        defaults.put(ColumnMetaData.BUFFER_LENGTH, null);
        defaults.put(ColumnMetaData.DECIMAL_DIGITS, null);
        defaults.put(ColumnMetaData.NUM_PREC_RADIX, 10);
        defaults.put(ColumnMetaData.NULLABLE, DatabaseMetaData.columnNullable);
        defaults.put(ColumnMetaData.REMARKS, null);
        defaults.put(ColumnMetaData.COLUMN_DEF, null);
        defaults.put(ColumnMetaData.SQL_DATA_TYPE, null);
        defaults.put(ColumnMetaData.SQL_DATETIME_SUB, null);
        defaults.put(ColumnMetaData.CHAR_OCTET_LENGTH, null);
        defaults.put(ColumnMetaData.IS_NULLABLE, "YES");
        defaults.put(ColumnMetaData.SCOPE_SCHEMA, null);
        defaults.put(ColumnMetaData.SCOPE_TABLE, null);
        defaults.put(ColumnMetaData.SCOPE_CATALOG, null);
        defaults.put(ColumnMetaData.SOURCE_DATA_TYPE, null);
        defaults.put(ColumnMetaData.IS_AUTOINCREMENT, "NO");
        defaults.put(ColumnMetaData.IS_GENERATEDCOLUMN, "NO");
        defaults.put(ColumnMetaData.JB_IS_IDENTITY, "NO");
        defaults.put(ColumnMetaData.JB_IDENTITY_TYPE, null);

        DEFAULT_COLUMN_VALUES = Collections.unmodifiableMap(defaults);
    }

    private static Map<ColumnMetaData, Object> getDefaultValueValidationRules() {
        return new EnumMap<>(DEFAULT_COLUMN_VALUES);
    }
    
    /**
     * Columns defined for the getColumns() metadata.
     */
    private enum ColumnMetaData implements MetaDataInfo {
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
        SCOPE_CATALOG(19, String.class),
        SCOPE_SCHEMA(20, String.class), 
        SCOPE_TABLE(21, String.class), 
        SOURCE_DATA_TYPE(22, Short.class), 
        IS_AUTOINCREMENT(23, String.class), 
        IS_GENERATEDCOLUMN(24,String.class),
        JB_IS_IDENTITY(25,String.class),
        JB_IDENTITY_TYPE(26,String.class);

        private final int position;
        private final Class<?> columnClass;

        ColumnMetaData(int position, Class<?> columnClass) {
            this.position = position;
            this.columnClass = columnClass;
        }

        @Override
        public int getPosition() {
            return position;
        }

        @Override
        public Class<?> getColumnClass() {
            return columnClass;
        }
    }
}
